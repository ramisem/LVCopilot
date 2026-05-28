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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetSpecConditionForSDI
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 65842 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdc = ajaxResponse.getRequestParameter("sdc", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        boolean customerFlag = ajaxResponse.getRequestParameter("customerflag", "N").equalsIgnoreCase("Y");
        StringBuffer buffer = new StringBuffer();
        String warningPolicy = "";
        String sqlInClause = "";
        SafeSQL safeSQL = new SafeSQL();
        TranslationProcessor processor = this.getTranslationProcessor();
        if (sdc.equals("Sample")) {
            sqlInClause = safeSQL.addIn(keyid1, ";");
        } else if (sdc.equals("Batch")) {
            sqlInClause = "SELECT s_sampleid FROM s_sample WHERE batchid IN(" + safeSQL.addIn(keyid1, ";") + ")";
        }
        try {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("DataEntryPolicy", "Sapphire Custom");
            PropertyListCollection list = policy.getCollection("SpecConditions");
            ArrayList<String> conditionList = new ArrayList<String>();
            for (int i = 0; i < list.size(); ++i) {
                PropertyList propList = (PropertyList)list.get(i);
                if (!propList.getProperty("warningoncoa").equals("Y")) continue;
                conditionList.add(propList.getProperty("SpecCond"));
            }
            if (policy != null) {
                DataSet sdiSpecData;
                String sql = "SELECT keyid1, specid, specversionid, condition FROM sdispec WHERE sdcid='Sample' AND  keyid1 in(" + sqlInClause + ") AND oosgeneratingflag='Y'";
                if (customerFlag) {
                    sql = "SELECT keyid1, specid, specversionid, condition FROM sdispec, spec WHERE sdcid='Sample' AND  keyid1 in(" + sqlInClause + ") AND spec.specusetype='Customer' AND spec.specid=sdispec.specid AND spec.specversonid = sdispec.specversionid";
                }
                if ((sdiSpecData = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())) != null) {
                    for (int i = 0; i < sdiSpecData.size(); ++i) {
                        String keyid1String = sdiSpecData.getValue(i, "keyid1");
                        String specidString = sdiSpecData.getValue(i, "specid");
                        String versionString = sdiSpecData.getValue(i, "specversionid");
                        String conditionString = sdiSpecData.getValue(i, "condition");
                        if (conditionString == null || !conditionList.contains(conditionString)) continue;
                        buffer.append("Spec : ").append(specidString).append("(").append(versionString).append(") ").append(conditionString).append(" ").append(processor.translate("for")).append("Sample:").append(keyid1String).append("\n");
                    }
                } else {
                    buffer = new StringBuffer("No data found");
                }
                if (buffer.length() == 0) {
                    buffer = new StringBuffer("No data found");
                } else {
                    StringBuffer message = new StringBuffer(processor.translate("Spec-conditions of the following specs") + " ");
                    message.append(warningPolicy.toLowerCase()).append(" " + processor.translate("for the selected") + " ").append(sdc).append("(s)").append(" " + processor.translate("are matching with warning-condition.")).append("\n\n");
                    StringBuffer msg = new StringBuffer("\n" + processor.translate("Are you sure you want to go ahead and print the") + " ");
                    msg.append("COA?");
                    buffer = new StringBuffer(message.append(buffer).append(msg));
                }
            } else {
                buffer = new StringBuffer("No data found");
            }
        }
        catch (SapphireException e) {
            this.logger.error("Failed to retrive spec policy", e);
        }
        ajaxResponse.addCallbackArgument("data", buffer.toString());
        ajaxResponse.print();
    }
}

