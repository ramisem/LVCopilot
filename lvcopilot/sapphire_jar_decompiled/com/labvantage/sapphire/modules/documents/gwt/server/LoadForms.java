/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LoadForms
extends BaseDocumentCommand
implements DocumentCommand {
    public LoadForms(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String excludedFormsWhere = this.sapphireConnection.isOracle() ? "( form.formtype <> 'Worksheet' OR form.worksheettype IS NULL OR length( form.worksheettype ) = 0 )" : "( form.formtype <> 'Worksheet' OR form.worksheettype IS NULL OR len( form.worksheettype ) = 0 )";
        String versionstatus = requestData.getProperty("versionstatus");
        String hostpageid = requestData.getProperty("hostpageid");
        String searchrows = requestData.getProperty("searchrows", "10000");
        int maxdocs = Integer.parseInt(searchrows);
        DataSet primary = null;
        boolean loadrecentforms = requestData.getProperty("recentforms", "N").equals("Y");
        if (loadrecentforms) {
            try {
                ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
                StringBuffer querywhere = new StringBuffer();
                String[] recentforms = StringUtil.split(configProcessor.getProfileProperty(this.sapphireConnection.getSysuserId(), "userconfig_efm_" + hostpageid + "_recentforms"), ";");
                for (int i = 0; i < recentforms.length; ++i) {
                    int pos = recentforms[i].indexOf("|");
                    if (recentforms[i].length() <= 0 || pos <= 0) continue;
                    querywhere.append(" OR ( formid = '").append(recentforms[i].substring(0, pos)).append("' AND formversionid = '").append(recentforms[i].substring(pos + 1)).append("' )");
                }
                DataSet temp = this.loadSDIData("LV_Form", "form", querywhere.length() > 0 ? querywhere.substring(3) : "1=2", "", "primary[formid, formversionid, formtitle]", versionstatus, false);
                HashMap<String, String> findMap = new HashMap<String, String>();
                primary = new DataSet();
                for (int i = 0; i < recentforms.length; ++i) {
                    int pos = recentforms[i].indexOf("|");
                    if (recentforms[i].length() <= 0 || pos <= 0) continue;
                    findMap.put("formid", recentforms[i].substring(0, pos));
                    findMap.put("formversionid", recentforms[i].substring(pos + 1));
                    int findRow = temp.findRow(findMap);
                    if (findRow <= -1) continue;
                    primary.copyRow(temp, findRow, 1);
                }
            }
            catch (Exception e) {
                primary = new DataSet();
            }
        } else if ("queryform".equals(requestData.getProperty("type"))) {
            PropertyListCollection queryargs = requestData.getCollection("queryargs");
            String[] queryparams = null;
            if (queryargs != null && queryargs.size() > 0) {
                queryparams = new String[queryargs.size()];
                for (int i = 0; i < queryargs.size(); ++i) {
                    queryparams[i] = queryargs.getPropertyList(i).getProperty("argvalue");
                }
            }
            primary = this.loadSDIData(requestData.getProperty("sdcid"), requestData.getProperty("queryid"), queryparams, requestData.getProperty("includecontextforms").equals("true") ? "(1=1)" : excludedFormsWhere, "primary[formid, formversionid, formtitle]", versionstatus, false);
        } else {
            boolean querywhere = requestData.getProperty("querywhere").length() > 0;
            boolean restrictivewhere = requestData.getProperty("restrictivewhere").length() > 0;
            primary = this.loadSDIData(requestData.getProperty("sdcid"), this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("queryfrom", "(default)"))), this.evalTokens(requestData, (requestData.getProperty("includecontextforms").equals("true") ? "(1=1)" : excludedFormsWhere) + (querywhere ? " AND (" + EncryptDecrypt.unobfsql(requestData.getProperty("querywhere")) + ")" : "") + (restrictivewhere ? " AND (" + EncryptDecrypt.unobfsql(requestData.getProperty("restrictivewhere")) + ")" : "")), this.evalTokens(requestData, EncryptDecrypt.unobfsql(requestData.getProperty("queryorderby", "form.formtitle"))), "primary[formid, formversionid, formtitle]", versionstatus, false);
        }
        if (primary.size() > maxdocs) {
            for (int i = primary.size() - 1; i >= maxdocs; --i) {
                primary.deleteRow(i);
            }
        }
        HashMap<String, String> responseData = new HashMap<String, String>();
        responseData.put("jsonreturn", primary != null ? JSONUtil.toJSONString(primary) : JSONUtil.toJSONString(new DataSet()));
        this.debugReturn(requestData, primary);
        if (!loadrecentforms) {
            responseData.put("userconfig_efm_" + hostpageid + "_lastrequest", "loadforms");
        }
        return responseData;
    }

    private String getFormThumbnail() {
        return "<div id=\"page001\" style=\"padding-bottom: 25.4mm; padding-left: 25.4mm; width: 215.9mm !important; padding-right: 25.4mm; height: 279.4mm !important; padding-top: 25.4mm;\">Form thumbnail not available</div>";
    }
}

