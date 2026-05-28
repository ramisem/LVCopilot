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

import com.labvantage.opal.validation.misc.ConvertUnits;
import com.labvantage.sapphire.I18nUtil;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateChildSampleQuantity
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 61718 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String copy = ajaxResponse.getRequestParameter("copy", "");
        String quantity = ajaxResponse.getRequestParameter("quantity", "");
        DecimalFormat decimalFormat = (DecimalFormat)NumberFormat.getInstance(I18nUtil.getSessionLocale(request));
        decimalFormat.applyPattern("###,###.######");
        if (StringUtil.getLen(copy) > 0L) {
            String[] c = StringUtil.split(copy, ";");
            String[] q = StringUtil.split(quantity, ";");
            float qty = 0.0f;
            if (c.length == q.length) {
                try {
                    for (int i = 0; i < c.length; ++i) {
                        qty = (float)((double)qty + Double.parseDouble(c[i]) * decimalFormat.parse(StringUtil.replaceAll(q[i], " ", "")).doubleValue());
                    }
                }
                catch (ParseException e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Invalid Quantity"));
                }
            }
            ajaxResponse.addCallbackArgument("quantity", qty);
        } else if (StringUtil.getLen(quantity) > 0L) {
            String sampleid = ajaxResponse.getRequestParameter("sampleid", "");
            String copies = ajaxResponse.getRequestParameter("copies", "");
            String unit = ajaxResponse.getRequestParameter("unit", "");
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("select s_sample.s_sampleid, 'blank' newqtycurrent, 'blank' newqtyunits, 'N' uniterror, '' errormsg,");
            sql.append(" (select sum(trackitem.qtycurrent) from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) qtycurrent,");
            sql.append(" (select max(trackitem.qtyunits) from trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid) qtyunits");
            sql.append(" from s_sample");
            sql.append(" where s_sample.s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    boolean uniterror = false;
                    double qtycurrent = ds.getDouble(i, "qtycurrent", -9999.0);
                    String qtyunits = ds.getValue(i, "qtyunits", "");
                    if (qtycurrent == -9999.0 || StringUtil.getLen(qtyunits) <= 0L) continue;
                    double childQuantity = (double)Integer.parseInt(copies) * Double.parseDouble(quantity);
                    if (StringUtil.getLen(qtyunits) > 0L && StringUtil.getLen(unit) > 0L && !qtyunits.equals(unit)) {
                        try {
                            childQuantity = Double.parseDouble(ConvertUnits.convertUnits(this.getQueryProcessor(), unit, qtyunits, String.valueOf(childQuantity)));
                        }
                        catch (SapphireException e) {
                            uniterror = true;
                            ds.setValue(i, "uniterror", "Y");
                            ds.setValue(i, "errormsg", e.getMessage());
                        }
                    }
                    if (uniterror) continue;
                    ds.setValue(i, "newqtycurrent", decimalFormat.format(qtycurrent - childQuantity));
                }
            }
            ajaxResponse.addCallbackArgument("ds", ds);
        }
        ajaxResponse.print();
    }
}

