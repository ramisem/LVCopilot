/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.RequestParser;
import java.util.ArrayList;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ReportDataRetriever {
    private static void getRequestItemCols(PropertyListCollection columns, ArrayList columnlist) {
        if (columns != null && columns.size() > 0) {
            for (int i = 0; i < columns.size(); ++i) {
                String colid;
                if ("Do Not Retrieve".equals(columns.getPropertyList(i).getProperty("mode")) || (colid = columns.getPropertyList(i).getProperty("columnid")).length() <= 0 || columnlist.indexOf(colid) >= 0) continue;
                columnlist.add(colid);
            }
        }
    }

    private static String getColumnList(ArrayList columnlist) {
        StringBuffer cols = new StringBuffer();
        for (int i = 0; i < columnlist.size(); ++i) {
            if (i == 0) {
                cols.append((String)columnlist.get(i));
                continue;
            }
            cols.append("," + (String)columnlist.get(i));
        }
        return cols.toString();
    }

    private static void processElementProperty(PropertyList listElement, RequestContext requestContext) {
        PropertyListCollection columns = listElement.getCollection("columns");
        String sdcid = listElement.getProperty("sdcid");
        SDCProcessor sdcProcessor = new SDCProcessor(requestContext.getConnectionId());
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        String desccol = sdcProcessor.getProperty(sdcid, "desccol");
        if (columns != null && columns.size() > 0) {
            ArrayList<PropertyList> toberemoved = new ArrayList<PropertyList>();
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (columnid.equals("keycolid1")) {
                    column.setProperty("columnid", keycolid1);
                } else if (columnid.equals("keycolid2")) {
                    if (keycolid2 != null && keycolid2.length() > 0) {
                        column.setProperty("columnid", keycolid2);
                    } else {
                        toberemoved.add(column);
                    }
                } else if (columnid.equals("keycolid3")) {
                    if (keycolid3 != null && keycolid3.length() > 0) {
                        column.setProperty("columnid", keycolid3);
                    } else {
                        toberemoved.add(column);
                    }
                } else if (columnid.equals("desccolid") || columnid.equals("desccol")) {
                    if (desccol != null && desccol.length() > 0) {
                        column.setProperty("columnid", desccol);
                    } else {
                        toberemoved.add(column);
                    }
                }
                if (columnid.indexOf("[currentuser]") < 0) continue;
                columnid = StringUtil.replaceAll(columnid, "[currentuser]", new ConnectionProcessor(requestContext.getConnectionId()).getSapphireConnection().getSysuserId());
                column.setProperty("columnid", columnid);
            }
            if (toberemoved.size() > 0) {
                columns.removeAll(toberemoved);
            }
        }
    }

    public static SDIRequest buildSDIRequest(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList element, SDCProcessor sdcProcessor, String sysuserid) {
        String _request = "primary";
        PropertyListCollection columns = element.getCollection("columns");
        PropertyListCollection groupby = element.getCollection("groupby");
        PropertyListCollection sortby = element.getCollection("sortby");
        ArrayList<String> linkcolist = new ArrayList<String>();
        ReportDataRetriever.getRequestItemCols(columns, linkcolist);
        if (groupby != null && groupby.size() > 0) {
            String groupbycolid = groupby.getPropertyList(0).getProperty("columnid");
            PropertyListCollection groupbycols = groupby.getPropertyList(0).getCollection("columns");
            if (groupbycolid.length() > 0 && linkcolist.indexOf(groupbycolid) < 0) {
                linkcolist.add(groupbycolid);
            }
            if (groupbycols != null) {
                ReportDataRetriever.getRequestItemCols(groupbycols, linkcolist);
            }
        }
        if (sortby != null && sortby.size() > 0) {
            ReportDataRetriever.getRequestItemCols(sortby, linkcolist);
        }
        if (linkcolist.size() > 0) {
            _request = _request.replaceFirst("primary", "primary[" + ReportDataRetriever.getColumnList(linkcolist) + "]");
        }
        String _retrievelimit = element.getProperty("retrievelimit");
        String _versionstatus = element.getProperty("versionstatus");
        String _mergequerywhere = element.getProperty("mergequerywhere");
        SDIRequest _sdirequest = new SDIRequest();
        int retrievelimit = 0;
        try {
            retrievelimit = Integer.parseInt(_retrievelimit);
        }
        catch (Exception exception) {
            // empty catch block
        }
        _sdirequest.setRetrieveLimit(retrievelimit);
        _sdirequest.setVersionStatus(_versionstatus);
        _sdirequest.setSDIList(sdcid, keyid1, keyid2, keyid3);
        if (_request != null && _request.indexOf("[currentuser]") >= 0) {
            _request = StringUtil.replaceAll(_request, "[currentuser]", sysuserid);
        }
        if (_request != null && _request.indexOf("[sdcid]") >= 0) {
            _request = StringUtil.replaceAll(_request, "[sdcid]", sdcid);
        }
        if (_request != null && (_request.indexOf("[keycolid1]") >= 0 || _request.indexOf("[primarytable]") >= 0) && sdcProcessor != null) {
            _request = StringUtil.replaceAll(_request, "[keycolid1]", sdcProcessor.getProperty(sdcid, "keycolid1"));
            _request = StringUtil.replaceAll(_request, "[keycolid2]", sdcProcessor.getProperty(sdcid, "keycolid2"));
            _request = StringUtil.replaceAll(_request, "[keycolid3]", sdcProcessor.getProperty(sdcid, "keycolid3"));
            _request = StringUtil.replaceAll(_request, "[primarytable]", sdcProcessor.getProperty(sdcid, "tableid"));
        }
        String[] request = RequestParser.parseRequestItem(_request);
        for (int i = 0; i < request.length; ++i) {
            _sdirequest.setRequestItem(request[i].trim());
        }
        _sdirequest.setLockOption("");
        _sdirequest.setPrimaryLockOption("");
        _sdirequest.setDataLockOption("");
        return _sdirequest;
    }

    public static SDIData getSDIData(String sdcid, String keyid1, String keyid2, String keyid3, PropertyList element, RequestContext requestContext) {
        ReportDataRetriever.processElementProperty(element, requestContext);
        SDIProcessor sdireq = new SDIProcessor(requestContext.getConnectionId());
        SDCProcessor sdcProcessor = new SDCProcessor(requestContext.getConnectionId());
        String sysuserid = new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionId()).getSysuserId();
        String keycols = sdcProcessor.getProperty(sdcid, "keycolumns");
        SDIRequest sdiRequest = ReportDataRetriever.buildSDIRequest(sdcid, keyid1, "2".equals(keycols) || "3".equals(keycols) ? keyid2 : "", "3".equals(keycols) ? keyid3 : "", element, sdcProcessor, sysuserid);
        sdiRequest.setQueryWhere("1=1");
        sdiRequest.setReturnMaskedData(true);
        SDIData sdidata = sdireq.getSDIData(sdiRequest);
        return sdidata;
    }
}

