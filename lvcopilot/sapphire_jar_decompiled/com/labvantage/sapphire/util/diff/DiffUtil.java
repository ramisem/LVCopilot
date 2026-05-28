/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.services.QueryService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.diff.DiffConnectionDetails;
import com.labvantage.sapphire.xml.ExportPackageHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class DiffUtil {
    public static SDIData getSDIData(String connectionid, String sdcid, String sdiFilter, DiffConnectionDetails diffConnection, boolean includeDetails) {
        SDCProcessor sdcp = new SDCProcessor(connectionid);
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setRequestItem(includeDetails ? "all" : "primary");
        sdiRequest.setExtendedDataTypes(true);
        sdiRequest.setOverrideLoadFlag(true);
        sdiRequest.setQueryFrom(sdcp.getProperty(sdcid, "tableid"));
        if (sdiFilter.length() > 0) {
            sdiRequest.setQueryWhere(sdcp.getProperty(sdcid, "keycolid1") + " like '" + sdiFilter + "%'");
        }
        sdiRequest.setOverrideLoadFlag(true);
        SDIData sdiData = null;
        String type = diffConnection.getType();
        if ("current".equals(type)) {
            sdiData = DiffUtil.getCurrentSDIData(connectionid, sdiRequest);
        } else if ("other".equals(type)) {
            sdiData = DiffUtil.getOtherSDIData(connectionid, sdiRequest, diffConnection);
        } else if ("jdbc".equals(type)) {
            sdiData = DiffUtil.getJDBCSDIDAta(sdiRequest, diffConnection);
        } else if ("snapshot".equals(type)) {
            sdiData = DiffUtil.getSnapshotSDIDAta(sdcid, diffConnection);
        }
        return sdiData;
    }

    public static SDIData getCurrentSDIData(String connectionid, SDIRequest sdiRequest) {
        SDIProcessor sdip = new SDIProcessor(connectionid);
        SDIData sdiData = sdip.getSDIData(sdiRequest);
        return sdiData;
    }

    private static SDIData getOtherSDIData(String connectionid, SDIRequest sdiRequest, DiffConnectionDetails diffConnection) {
        SDIData sdiData = null;
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        String newConnectionid = cp.getConnectionid(diffConnection.getDatabase(), diffConnection.getUsername(), diffConnection.getPassword());
        SDIProcessor sdip = new SDIProcessor(newConnectionid);
        sdiData = sdip.getSDIData(sdiRequest);
        return sdiData;
    }

    private static SDIData getSnapshotSDIDAta(String sdcid, DiffConnectionDetails diffConnection) {
        SDIData sdiData = null;
        try {
            ExportPackageHandler handler = new ExportPackageHandler();
            handler.setXMLFile(new File(diffConnection.getFilename()));
            handler.setLogFile(new File(diffConnection.getFilename() + "_log"));
            handler.setImportTarget(1);
            SaxUtil.parseFile(handler);
            List importObjects = handler.getImportObjects();
            for (int i = 0; sdiData == null && i < importObjects.size(); ++i) {
                SDIData tempSdiData;
                Object importObject = importObjects.get(i);
                if (!(importObject instanceof SDIData) || !(tempSdiData = (SDIData)importObject).getSdcid().equals(sdcid)) continue;
                sdiData = tempSdiData;
            }
        }
        catch (SapphireException e) {
            Logger.logStackTrace(e);
        }
        return sdiData;
    }

    private static SDIData getJDBCSDIDAta(SDIRequest sdiRequest, DiffConnectionDetails diffConnection) {
        try {
            DBUtil database = new DBUtil();
            database.setDatabase(diffConnection.getDbservername(), diffConnection.getDbport(), diffConnection.getDbsid(), diffConnection.getDbusername(), diffConnection.getDbpassword());
            String connectionid = "dummy|" + System.currentTimeMillis();
            database.executePreparedUpdate("INSERT INTO connection ( connectionid ) VALUES  ( ? )", new Object[]{connectionid});
            SapphireConnection sapphireConnection = new SapphireConnection();
            sapphireConnection.setConnectionId(connectionid);
            sapphireConnection.setConnection(database.getConnection());
            sapphireConnection.setDbms("ORA");
            QueryService qs = new QueryService(sapphireConnection);
            SDIData sdiData = qs.getSDIData(sdiRequest);
            database.executeSQL("rollback");
            return sdiData;
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    public static ArrayList getSDCList(String connectionid, DiffConnectionDetails diffConnection) {
        ArrayList sdcList = null;
        String type = diffConnection.getType();
        if ("current".equals(type)) {
            sdcList = DiffUtil.getCurrentSDCList(connectionid);
        } else if ("other".equals(type)) {
            sdcList = DiffUtil.getOtherSDCList(connectionid, diffConnection);
        } else if ("jdbc".equals(type)) {
            sdcList = DiffUtil.getJDBCSDCList(diffConnection);
        } else if ("snapshot".equals(type)) {
            sdcList = DiffUtil.getSnapshotSDCList(diffConnection);
        }
        return sdcList;
    }

    public static ArrayList getCurrentSDCList(String connectionid) {
        QueryProcessor qp = new QueryProcessor(connectionid);
        DataSet sdcs = qp.getSqlDataSet("SELECT sdcid FROM sdc");
        ArrayList<String> sdcList = new ArrayList<String>();
        for (int i = 0; i < sdcs.size(); ++i) {
            sdcList.add(sdcs.getString(i, "sdcid"));
        }
        return sdcList;
    }

    private static ArrayList getOtherSDCList(String connectionid, DiffConnectionDetails diffConnection) {
        ConnectionProcessor cp = new ConnectionProcessor(connectionid);
        String newConnectionid = cp.getConnectionid(diffConnection.getDatabase(), diffConnection.getUsername(), diffConnection.getPassword());
        return DiffUtil.getCurrentSDCList(newConnectionid);
    }

    private static ArrayList getJDBCSDCList(DiffConnectionDetails diffConnection) {
        ArrayList<String> sdcList = new ArrayList<String>();
        try {
            DBUtil database = new DBUtil();
            database.setDatabase(diffConnection.getDbservername(), diffConnection.getDbport(), diffConnection.getDbsid(), diffConnection.getDbusername(), diffConnection.getDbpassword());
            database.createResultSet("SELECT sdcid FROM sdc");
            while (database.getNext()) {
                sdcList.add(database.getString("sdcid"));
            }
        }
        catch (SapphireException e) {
            Logger.logStackTrace(e);
        }
        return sdcList;
    }

    private static ArrayList getSnapshotSDCList(DiffConnectionDetails diffConnection) {
        ArrayList<String> sdcList = new ArrayList<String>();
        try {
            ExportPackageHandler handler = new ExportPackageHandler();
            handler.setXMLFile(new File(diffConnection.getFilename()));
            handler.setLogFile(new File(diffConnection.getFilename() + "_log"));
            handler.setImportTarget(1);
            SaxUtil.parseFile(handler);
            List importObjects = handler.getImportObjects();
            for (int i = 0; i < importObjects.size(); ++i) {
                Object importObject = importObjects.get(i);
                if (!(importObject instanceof SDIData)) continue;
                SDIData sdiData = (SDIData)importObject;
                sdcList.add(sdiData.getSdcid());
            }
        }
        catch (SapphireException e) {
            Logger.logStackTrace(e);
        }
        return sdcList;
    }

    public static String formatResultsHTML(String sdcid, DataSet results) {
        StringBuffer output = new StringBuffer();
        results.sort("sdcid");
        ArrayList<DataSet> sdcs = results.getGroupedDataSets("sdcid");
        int detailCount = 0;
        for (DataSet sdc : sdcs) {
            output.append("<table class=\"maintform_table\" border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
            output.append("<tr>");
            output.append("<th class=\"maintform_fieldtitle\" align=\"left\" colspan=\"2\">");
            output.append("<input type=\"checkbox\" onClick=\"selectAll( this )\" sdcid=\"" + sdcid + "\">");
            output.append("SDI</th>");
            output.append("<th class=\"maintform_fieldtitle\" >Change Type</th>");
            output.append("<th class=\"maintform_fieldtitle\" align=\"left\"><img collapsed=\"Y\" src=\"WEB-CORE/elements/images/plus.gif\" onclick=\"javascript:showAllDetails( this, '" + sdcid + "'," + detailCount + ")\"></img>&nbsp;&nbsp;Details</th>");
            output.append("</tr>");
            sdc.sort("status,keyid1,keyid2,keyid3");
            ArrayList<DataSet> sdis = sdc.getGroupedDataSets("keyid1,keyid2,keyid3");
            Iterator<DataSet> it2 = sdis.iterator();
            while (it2.hasNext()) {
                output.append("<tr>");
                DataSet sdi = it2.next();
                String keyid1 = sdi.getString(0, "keyid1");
                String keyid2 = sdi.getString(0, "keyid2");
                String keyid3 = sdi.getString(0, "keyid3");
                String status = sdi.getString(0, "status");
                output.append("<td class=\"maintform_field\" style=\"border-right: none\">");
                if (!status.equals("Missing from source")) {
                    output.append("<input name=\"" + sdcid + "_sdilist\" sdcid=\"" + sdcid + "\" keyid1=\"" + keyid1 + "\" keyid2=\"" + keyid2 + "\" keyid3=\"" + keyid3 + "\" type=\"checkbox\">");
                } else {
                    output.append("&nbsp;");
                }
                output.append("</td>");
                output.append("<td class=\"maintform_field\" style=\"border-left: none\">" + keyid1);
                if (keyid2 != null && keyid2.length() > 0) {
                    output.append(" (ver: " + keyid2 + ")");
                }
                if (keyid3 != null && keyid3.length() > 0) {
                    output.append(" - " + keyid3);
                }
                output.append("</td>");
                output.append("<td class=\"maintform_field\" >" + status + "</td>");
                output.append("<td class=\"maintform_field\" nowrap>");
                if (status.equals("Difference Found")) {
                    output.append("<img src=\"WEB-CORE/elements/images/plus.gif\" id=\"details_" + sdcid + "_" + ++detailCount + "_image\" onclick=\"javascript:showDetails( '" + sdcid + "', " + detailCount + ")\"></img>");
                    output.append("<span collapsed=\"Y\" id=\"details_" + sdcid + "_" + detailCount + "_controller\">&nbsp;&nbsp;Show Details</span>");
                    output.append("<div style=\"display:none\" id=\"details_" + sdcid + "_" + detailCount + "\">");
                    output.append("<table class=\"maintform_table\" border=\"1\" cellspacing=\"0\" cellpadding=\"1\">");
                    output.append("<tr>");
                    output.append("<th class=\"maintform_fieldtitle\" colspan=\"2\">Change Type</th>");
                    output.append("<th class=\"maintform_fieldtitle\" >Source Value</th>");
                    output.append("<th class=\"maintform_fieldtitle\" >Target Value</th>");
                    output.append("</tr>");
                    for (int i = 0; i < sdi.size(); ++i) {
                        String changeType = sdi.getString(i, "changetype");
                        String changeTypeDetail = sdi.getValue(i, "changetypedetail");
                        String sourceValue = sdi.getString(i, "sourcevalue");
                        String targetValue = sdi.getString(i, "targetvalue");
                        String longDescription = sdi.getString(i, "longdescription");
                        output.append("<tr>");
                        output.append("<td class=\"maintform_field\" >" + changeType + "</td>");
                        output.append("<td class=\"maintform_field\" >" + (changeTypeDetail.length() > 0 ? changeTypeDetail : "&nbsp;") + "</td>");
                        if (longDescription != null && longDescription.length() > 0) {
                            output.append("<td class=\"maintform_field\" colspan=\"2\">" + longDescription + "</td>");
                        } else {
                            output.append("<td class=\"maintform_field\" >" + (sourceValue.length() > 0 ? sourceValue : "&nbsp;") + "</td>");
                            output.append("<td class=\"maintform_field\" >" + (targetValue.length() > 0 ? targetValue : "&nbsp;") + "</td>");
                        }
                        output.append("</tr>");
                    }
                    output.append("</table>");
                    output.append("</div>");
                } else {
                    output.append("&nbsp;");
                }
                output.append("</td>");
                output.append("</tr>");
            }
            output.append("</table>");
        }
        output.append("<span id=\"" + sdcid + "_detailcount\" count=" + detailCount + " />");
        return output.toString();
    }
}

