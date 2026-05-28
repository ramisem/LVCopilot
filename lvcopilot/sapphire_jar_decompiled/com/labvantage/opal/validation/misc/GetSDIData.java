/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.opal.util.SdiInfo;
import java.util.ArrayList;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetSDIData
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 65109 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            boolean isOracle = this.getConnectionProcessor().isOra();
            String error = "";
            String requestid = ajaxResponse.getRequestParameter("requestid");
            String sdcid = "";
            String queryfrom = "";
            String querywhere = "";
            String queryorderby = "";
            String showtemplates = "N";
            String columnid = "";
            String queryid = "";
            switch (requestid) {
                case "0001": {
                    sdcid = "SampleType";
                    queryfrom = "s_sampletype";
                    querywhere = "";
                    queryorderby = "s_sampletypeid";
                    columnid = "s_sampletypeid";
                    break;
                }
                case "0002": {
                    sdcid = "SampleType";
                    queryfrom = "s_sampletype";
                    querywhere = "s_sampletypeid in (select s.destsampletypeid from s_preptypesampletypemap s where s.sourcesampletypeid = '" + SafeSQL.encodeForSQL(ajaxResponse.getRequestParameter("sourcesampletypeid"), isOracle) + "')";
                    queryorderby = "s_sampletypeid";
                    columnid = "s_sampletypeid";
                    break;
                }
                case "0003": {
                    sdcid = "LV_PrepType";
                    queryfrom = "s_preptype";
                    querywhere = "s_preptypeid in (select s.s_preptypeid from s_preptypesampletypemap s where s.sourcesampletypeid = '" + SafeSQL.encodeForSQL(ajaxResponse.getRequestParameter("sourcesampletypeid"), isOracle) + "' and s.destsampletypeid = '" + SafeSQL.encodeForSQL(ajaxResponse.getRequestParameter("destsampletypeid"), isOracle) + "')";
                    queryorderby = "s_preptypeid";
                    columnid = "s_preptypeid";
                    break;
                }
                case "0004": {
                    sdcid = "LV_Treatment";
                    queryfrom = "s_treatmenttype";
                    querywhere = "s_treatmenttypeid in (select s.s_treatmenttypeid from s_preptypetreatmenttypemap s where s.s_preptypeid = '" + SafeSQL.encodeForSQL(ajaxResponse.getRequestParameter("preptypeid"), isOracle) + "')";
                    queryorderby = "s_treatmenttypeid";
                    columnid = "s_treatmenttypeid";
                    break;
                }
                case "0005": {
                    sdcid = "WorkItem";
                    break;
                }
                case "0006": {
                    sdcid = "LV_InstrumentType";
                    break;
                }
                case "0007": {
                    sdcid = "LV_ReagentType";
                    break;
                }
                case "0008": {
                    sdcid = "ContainerType";
                    break;
                }
                case "0009": {
                    sdcid = "TrackItemSDC";
                    break;
                }
                case "0010": {
                    sdcid = "LV_Incdt";
                    queryfrom = "incident";
                    querywhere = "incidentcategory = 'UnPlanned' and templateflag = 'Y'";
                    queryorderby = "incidentid";
                    columnid = "incidentid";
                    showtemplates = "Y";
                    break;
                }
            }
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid(sdcid);
            if (StringUtil.getLen(sdcid) > 0L) {
                boolean isVersionedSDC = "Y".equals(StringUtil.getYN(this.getSDCProcessor().getProperty(sdcid, "versionedflag"), "N"));
                String requesttype = ajaxResponse.getRequestParameter("requesttype");
                if (StringUtil.getLen(requesttype) == 0L) {
                    requesttype = "primary";
                }
                sdiRequest.setRequestItem(requesttype);
                sdiRequest.setShowTemplates("Y".equals(showtemplates));
                String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                if (StringUtil.getLen(keyid1) == 0L && StringUtil.getLen(queryfrom) == 0L) {
                    error = "CONFIGURATION ERROR: Either \"keyid1\" or \"queryfrom\" must be given";
                } else {
                    SDIData sdidata;
                    DataSet ds;
                    String keyId1List = ajaxResponse.getRequestParameter("keyid1", "");
                    String keyId2List = ajaxResponse.getRequestParameter("keyid2", "");
                    String keyId3List = ajaxResponse.getRequestParameter("keyid3", "");
                    if (isVersionedSDC) {
                        keyId2List = this.evalKeyId2List(sdcid, keyId1List, keyId2List, keyId3List);
                    }
                    if (StringUtil.getLen(keyId1List) > 0L) {
                        sdiRequest.setKeyid1List(keyId1List);
                    }
                    if (StringUtil.getLen(keyId2List) > 0L) {
                        sdiRequest.setKeyid2List(keyId2List);
                    }
                    if (StringUtil.getLen(keyId3List) > 0L) {
                        sdiRequest.setKeyid3List(keyId3List);
                    }
                    if (StringUtil.getLen(querywhere) > 0L) {
                        sdiRequest.setQueryWhere(querywhere);
                    }
                    if (StringUtil.getLen(queryfrom) > 0L) {
                        sdiRequest.setQueryFrom(queryfrom);
                    }
                    if (StringUtil.getLen(queryid) > 0L) {
                        sdiRequest.setQueryid(queryid);
                    }
                    if (StringUtil.getLen(queryorderby) > 0L) {
                        sdiRequest.setQueryOrderBy(queryorderby);
                    }
                    ArrayList<String> params = null;
                    for (int i = 1; i < 12 && StringUtil.getLen(ajaxResponse.getRequestParameter("param" + i, "")) > 0L; ++i) {
                        if (params == null) {
                            params = new ArrayList<String>();
                        }
                        params.add(ajaxResponse.getRequestParameter("param" + i, ""));
                    }
                    if (params != null && params.size() > 0) {
                        sdiRequest.setQueryParams((String[])params.toArray());
                    }
                    if ((ds = (sdidata = this.getSDIProcessor().getSDIData(sdiRequest)).getDataset(requesttype)) != null) {
                        if (StringUtil.getLen(columnid) > 0L) {
                            String[] columns = StringUtil.split(columnid, ";");
                            DataSet _ds = new DataSet();
                            for (String column : columns) {
                                String columnname;
                                if (!column.trim().contains(" ")) {
                                    if (!ds.isValidColumn(column)) continue;
                                    int columntype = ds.getColumnType(column);
                                    _ds.addColumnValues(column, columntype, ds.getColumnValues(column, ";"), ";");
                                    continue;
                                }
                                String[] s = StringUtil.split(column, " ");
                                String columnalias = columnname = s[0];
                                try {
                                    columnalias = s[1];
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                if (!ds.isValidColumn(columnname)) continue;
                                int columntype = ds.getColumnType(columnname);
                                _ds.addColumnValues(columnalias, columntype, ds.getColumnValues(columnname, ";"), ";");
                            }
                            ajaxResponse.addCallbackArgument("sdidata", _ds);
                        } else {
                            ajaxResponse.addCallbackArgument("sdidata", ds);
                        }
                    } else {
                        error = "DATA ERROR: Error while fetching data.";
                    }
                }
            } else {
                error = "CONFIGURATION ERROR: SDCID not found";
            }
            if (StringUtil.getLen(error) > 0L) {
                ajaxResponse.setError(error);
            }
            Map parameters = ajaxResponse.getRequestParameters();
            for (Object o : parameters.keySet()) {
                String param = (String)o;
                if (!param.startsWith("return_")) continue;
                ajaxResponse.addCallbackArgument(param, ajaxResponse.getRequestParameter(param));
            }
            ajaxResponse.print();
        }
        catch (SapphireException e) {
            throw new ServletException((Throwable)e);
        }
    }

    private String evalKeyId2List(String sdcId, String keyId1List, String keyId2List, String keyId3List) throws SapphireException {
        if (keyId2List.length() == 0 || keyId2List.startsWith(";") || keyId2List.endsWith(";") || keyId2List.contains(";;") || keyId2List.contains("; ;") || keyId2List.contains("C") || keyId2List.contains("c")) {
            String currentKeyId2s = SdiInfo.getCurrentVersion(sdcId, keyId1List, keyId3List, this.getConnectionProcessor().getSapphireConnection());
            DataSet temp = new DataSet();
            temp.addColumnValues("keyid2", 0, keyId2List, ";");
            temp.addColumnValues("currkeyid2", 0, currentKeyId2s, ";");
            for (int i = 0; i < temp.getRowCount(); ++i) {
                String keyId2 = temp.getValue(i, "keyid2", "");
                if (keyId2.length() != 0 && !"C".equalsIgnoreCase(keyId2)) continue;
                temp.setValue(i, "keyid2", temp.getValue(i, "currkeyid2", "1"));
            }
            return temp.getColumnValues("keyid2", ";");
        }
        return keyId2List;
    }
}

