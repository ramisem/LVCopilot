/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetCustomerSpecDetails
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String addspecs = ajaxResponse.getRequestParameter("addedspecs", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String responseData = "";
        String errormsg = "";
        try {
            responseData = this.getSpecDetails(addspecs, this.getQueryProcessor(), this.getActionProcessor(), sdcid, keyid1);
        }
        catch (Exception e) {
            errormsg = e.getMessage();
        }
        ajaxResponse.addCallbackArgument("data", responseData);
        ajaxResponse.addCallbackArgument("errormsg", errormsg);
        ajaxResponse.print();
    }

    private String getSpecDetails(String addedSpecs, QueryProcessor qp, ActionProcessor ap, String sdcid, String keyid1) throws ActionException {
        StringBuffer json = new StringBuffer();
        String[] specs = StringUtil.split(addedSpecs, "|");
        StringBuffer specIds = new StringBuffer();
        StringBuffer specVerIds = new StringBuffer();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        DataSet dsAllSpecs = new DataSet();
        DataSet dsRefValues = qp.getRefTypeDataSet("Spec Condition");
        for (int i = 0; i < specs.length; ++i) {
            safeSQL.reset();
            sql.setLength(0);
            String[] spec = StringUtil.split(specs[i], ";");
            String specId = spec[0];
            String specVersionId = spec[1];
            sql.append("SELECT address.addressid, address.addresstype, address.state, spec.specid, spec.specversionid, spec.oosgeneratingflag, 'CustomerSpec' contactfunction").append(" FROM spec, address WHERE address.addressid = spec.specuseaddressid AND address.addresstype = spec.specuseaddresstype AND spec.specid = ").append(safeSQL.addVar(specId)).append(" AND spec.specversionid = ").append(safeSQL.addVar(specVersionId)).append(" UNION").append(" SELECT address.addressid, address.addresstype, address.state, spec.specid, spec.specversionid, spec.oosgeneratingflag, 'CustomerSpecSecondary' contactfunction").append(" FROM sdiaddress, address, spec  WHERE sdiaddress.sdcid = ").append(safeSQL.addVar("SpecSDC")).append(" AND sdiaddress.keyid1 = ").append(safeSQL.addVar(specId)).append(" AND sdiaddress.keyid2 = ").append(safeSQL.addVar(specVersionId)).append(" AND address.addressid = sdiaddress.addressid AND address.addresstype = sdiaddress.addresstype").append(" AND spec.specid = sdiaddress.keyid1 AND spec.specversionid = sdiaddress.keyid2");
            DataSet specAddresses = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            specAddresses.addColumn("condition", 0);
            dsAllSpecs.copyRow(specAddresses, -1, 1);
            specIds.append(";").append(specId);
            specVerIds.append(";").append(specVersionId);
        }
        if (specIds.length() > 0) {
            DataSet ds;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("specid", specIds.substring(1));
            props.setProperty("specversionid", specVerIds.substring(1));
            ap.processAction("CheckSpecs", "1", props);
            String outkeyid1 = props.getProperty("outkeyid1");
            String outSpecId = props.getProperty("outspecid");
            String outSpecVersionId = props.getProperty("outspecversionid");
            String outCondition = props.getProperty("outcondition");
            DataSet dsOutSpec = new DataSet();
            dsOutSpec.addColumnValues("keyid1", 0, outkeyid1, ";");
            dsOutSpec.addColumnValues("specid", 0, outSpecId, ";");
            dsOutSpec.addColumnValues("specversionid", 0, outSpecVersionId, ";");
            dsOutSpec.addColumnValues("condition", 0, outCondition, ";");
            for (int i = 0; i < dsOutSpec.getRowCount(); ++i) {
                HashMap<String, String> find = new HashMap<String, String>();
                find.put("specid", dsOutSpec.getValue(i, "specid"));
                find.put("specversionid", dsOutSpec.getValue(i, "specversionid"));
                ds = dsAllSpecs.getFilteredDataSet(find);
                ds.setString(-1, "condition", dsOutSpec.getValue(i, "condition"));
            }
            dsAllSpecs.sort("specid,specversionid,contactfunction");
            ArrayList<DataSet> al = dsAllSpecs.getGroupedDataSets("specid,specversionid");
            json.append("{ speccustomers:[");
            for (int i = 0; i < al.size(); ++i) {
                ds = al.get(i);
                for (int k = 0; k < ds.getRowCount(); ++k) {
                    String condition = ds.getValue(0, "condition");
                    int f = dsRefValues.findRow("refvalueid", condition);
                    String refDispIcon = "";
                    if (f > -1) {
                        refDispIcon = dsRefValues.getValue(f, "refdisplayicon");
                    }
                    if (k == 0) {
                        json.append("{\"spec\":\"").append(ds.getValue(0, "specid") + ";" + ds.getValue(0, "specversionid") + ";" + ds.getValue(0, "oosgeneratingflag") + ";" + ds.getValue(0, "condition") + ";" + refDispIcon).append("\", \"customers\":[");
                    }
                    json.append("\"").append(ds.getValue(k, "addressid")).append(";").append(ds.getValue(k, "state")).append(";").append(ds.getValue(k, "contactfunction")).append("\",");
                }
                if (json.toString().endsWith(",")) {
                    json.setLength(json.length() - 1);
                }
                json.append("]},");
            }
            if (json.toString().endsWith(",")) {
                json.setLength(json.length() - 1);
            }
            json.append("]}");
        }
        return json.toString();
    }
}

