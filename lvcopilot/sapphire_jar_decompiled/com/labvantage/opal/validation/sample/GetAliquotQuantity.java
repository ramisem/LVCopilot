/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.misc.ConvertUnits;
import com.labvantage.sapphire.I18nUtil;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetAliquotQuantity
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        AjaxResponse ajaxResponse;
        String message;
        block15: {
            message = "";
            DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getInstance(I18nUtil.getSessionLocale(request));
            decimalFormat.applyPattern("###,###.######");
            ajaxResponse = new AjaxResponse(request, response);
            String sampleid = ajaxResponse.getRequestParameter("sampleid");
            String childdata = ajaxResponse.getRequestParameter("childdata");
            ds = null;
            try {
                String[] parentSampleArray;
                JSONArray childJSONArray = new JSONArray(childdata);
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                sql.append("select s_sample.s_sampleid, 'blank' newqtycurrent, 'blank' newqtyunits, 'N' uniterror, '' errormsg,");
                sql.append(" (select sum(trackitem.qtycurrent) from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) qtycurrent,");
                sql.append(" (select max(trackitem.qtyunits) from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) qtyunits,");
                sql.append(" concentration, concentrationunits, '' diluentvolume");
                sql.append(" from s_sample");
                sql.append(" where s_sample.s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null || ds.size() <= 0) break block15;
                DataSet parentDS = new DataSet();
                for (String parentsampleid : parentSampleArray = StringUtil.split(sampleid, ";")) {
                    int row = ds.findRow("s_sampleid", parentsampleid);
                    parentDS.copyRow(ds, row, 1);
                }
                ds = parentDS;
                for (int parentrow = 0; parentrow < ds.size(); ++parentrow) {
                    ArrayList<String> childDiluentList = new ArrayList<String>();
                    double parent_qty = ds.getDouble(parentrow, "qtycurrent", 0.0);
                    String parent_qtyunit = ds.getString(parentrow, "qtyunits", "");
                    double parent_concentration = ds.getDouble(parentrow, "concentration", 0.0);
                    String parent_concentrationunit = ds.getString(parentrow, "concentrationunits", "");
                    if (!(parent_qty > 0.0) || parent_qtyunit.length() <= 0) continue;
                    double child_total_quantity = 0.0;
                    double child_total_diluentvolume = 0.0;
                    for (int childrow = 0; childrow < childJSONArray.length(); ++childrow) {
                        JSONObject childObject = childJSONArray.getJSONObject(childrow);
                        int child_copies = Integer.parseInt(childObject.getString("copies"));
                        double child_diluentvolume = 0.0;
                        double child_quantity = Double.parseDouble(childObject.getString("quantity"));
                        String child_quantityunit = childObject.getString("quantityunit");
                        if (!child_quantityunit.equals(parent_qtyunit)) {
                            try {
                                child_quantity = Double.parseDouble(ConvertUnits.convertUnits(this.getQueryProcessor(), child_quantityunit, parent_qtyunit, String.valueOf(child_quantity)));
                            }
                            catch (SapphireException e) {
                                ds.setValue(parentrow, "uniterror", "Y");
                                ds.setValue(parentrow, "errormsg", e.getMessage());
                            }
                        }
                        if (childObject.has("concentration") && childObject.getString("concentration").length() > 0 && childObject.has("concentrationunit") && childObject.getString("concentrationunit").length() > 0) {
                            double concConversionRatio = 1.0;
                            double child_concentration = Double.parseDouble(childObject.getString("concentration"));
                            String child_concentrationunit = childObject.getString("concentrationunit");
                            if (parent_concentration > 0.0 && child_concentration > 0.0) {
                                if (!parent_concentrationunit.equals(child_concentrationunit)) {
                                    try {
                                        child_concentration = Double.parseDouble(ConvertUnits.convertUnits(this.getQueryProcessor(), child_concentrationunit, parent_concentrationunit, String.valueOf(child_concentration)));
                                    }
                                    catch (SapphireException e) {
                                        ds.setValue(parentrow, "uniterror", "Y");
                                        ds.setValue(parentrow, "errormsg", e.getMessage());
                                        break block15;
                                    }
                                }
                                if (child_concentration < parent_concentration) {
                                    concConversionRatio = child_concentration / parent_concentration;
                                }
                                if (concConversionRatio != 1.0) {
                                    child_diluentvolume = child_quantity - child_quantity * concConversionRatio;
                                }
                            }
                        }
                        child_total_quantity += (child_quantity - child_diluentvolume) * (double)child_copies;
                        childDiluentList.add(String.valueOf(decimalFormat.format(child_diluentvolume)));
                    }
                    ds.setValue(parentrow, "newqtycurrent", String.valueOf(decimalFormat.format(parent_qty - child_total_quantity)));
                    ds.setValue(parentrow, "newqtyunit", parent_qtyunit);
                    ds.setValue(parentrow, "diluentvolume", OpalUtil.toDelimitedString(childDiluentList, ";"));
                }
            }
            catch (JSONException e) {
                message = this.getTranslationProcessor().translate("Error passing quantity data") + "<hr>" + e.getMessage();
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("ds", ds == null ? new DataSet() : ds);
        ajaxResponse.addCallbackArgument("diluentvolume", ds != null ? ds.getColumnValues("diluentvolume", "|") : "");
        ajaxResponse.print();
    }
}

