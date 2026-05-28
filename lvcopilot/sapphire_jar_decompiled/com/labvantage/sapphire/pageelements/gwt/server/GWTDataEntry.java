/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.gwt.server.ApprovalStepUtil;
import com.labvantage.sapphire.pageelements.gwt.server.DataItemCrossTabModel;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.SDITagInfo;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GWTDataEntry
extends BaseElement {
    private SDIData sdiData = null;
    private String webpageid = null;
    private boolean activityLogReasonRequired = false;
    int keycolumns = 1;
    String keycolid1 = "";
    private String preferredView = "";

    public GWTDataEntry() {
    }

    public GWTDataEntry(PageContext pageContext, SDIData sdiData) {
        this.setPageContext(pageContext);
        this.setSDIData(sdiData);
        PropertyList requestPL = HttpUtil.getRequestPropertyList(pageContext.getRequest());
        String forceview = requestPL.getProperty("forceview");
        String currentpageid = requestPL.getProperty("page");
        if (!"Y".equals(forceview)) {
            DataSet sdiworkitemDS = sdiData.getDataset("sdiworkitem");
            DataSet sdidataset = sdiData.getDataset("dataset");
            HashSet<String> sourceWISet = new HashSet<String>();
            for (int i = 0; i < sdidataset.getRowCount(); ++i) {
                if (sdidataset.getValue(i, "sourceworkitemid").length() <= 0) continue;
                sourceWISet.add(sdidataset.getValue(i, "sourceworkitemid") + ";" + sdidataset.getValue(i, "sourceworkiteminstance"));
            }
            String preferredview = "";
            if (sdiworkitemDS != null) {
                for (int i = 0; i < sdiworkitemDS.getRowCount(); ++i) {
                    if (sdiworkitemDS.getValue(i, "preferreddataentryview").length() <= 0 || !sourceWISet.contains(sdiworkitemDS.getValue(i, "workitemid") + ";" + sdiworkitemDS.getValue(i, "workiteminstance"))) continue;
                    if (preferredview.length() == 0) {
                        preferredview = sdiworkitemDS.getValue(i, "preferreddataentryview");
                        continue;
                    }
                    if (preferredview.equals(sdiworkitemDS.getValue(i, "preferreddataentryview"))) continue;
                    preferredview = "";
                    break;
                }
            }
            if (preferredview.length() > 0) {
                boolean isDataApprovalPage;
                boolean bl = isDataApprovalPage = currentpageid.indexOf("gDEGridApproval") == 0 || "Y".equals(this.element.getProperty("showapproval"));
                if (!isDataApprovalPage) {
                    this.preferredView = currentpageid = preferredview;
                    this.webpageid = preferredview;
                }
            }
        }
        RequestProcessor requestProcessor = new RequestProcessor(pageContext);
        try {
            PropertyList pagedata = requestProcessor.getWebPageProperties(currentpageid, new RequestContext(new PropertyList()));
            PropertyList gwtdataentrygridElement = pagedata.getPropertyList("gwtdataentrygrid");
            if (this.preferredView != null && this.preferredView.length() > 0) {
                PropertyListCollection columns = gwtdataentrygridElement.getCollection("columns");
                DataSet dataitemDS = sdiData.getDataset("dataitem");
                boolean dataNotRetrieved = false;
                for (int i = 0; i < columns.size(); ++i) {
                    String columnid = columns.getPropertyList(i).getProperty("columnid");
                    if (columnid.length() <= 0 || "hasdataitemnotes".equals(columnid = RequestParser.parseAlias(columnid)) || dataitemDS.isValidColumn(columnid)) continue;
                    dataNotRetrieved = true;
                    break;
                }
                if (dataNotRetrieved) {
                    SDIRequest sdiRequestUsed = (SDIRequest)pageContext.getAttribute("__sdirequestUsed");
                    String retainedrsetid = (String)pageContext.getAttribute("__rsetidRetained");
                    String requestdataset = GWTDataEntry.buildRequest(gwtdataentrygridElement);
                    String[] request = RequestParser.parseRequestItem(requestdataset);
                    sdiRequestUsed.setCountRequest(false);
                    for (int i = 0; i < request.length; ++i) {
                        sdiRequestUsed.setRequestItem(request[i].trim());
                    }
                    sdiRequestUsed.setRsetid(retainedrsetid);
                    SDIData sdidataNew = this.getSDIProcessor().getSDIData(sdiRequestUsed);
                    this.setSDIData(sdidataNew);
                }
            }
            this.setElementid("gwtdataentrygrid");
            this.setElementProperties(gwtdataentrygridElement);
        }
        catch (Exception e) {
            this.logger.error(e.getMessage(), e);
        }
    }

    public void setSDIData(SDIData sdiData) {
        this.sdiData = sdiData;
    }

    public void setWebpageid(String webpageid) {
        this.webpageid = webpageid;
    }

    public String getPreferredView() {
        return this.preferredView;
    }

    public String getJSONString() {
        DataSet sdinotes;
        DataSet primary;
        DataSet dataset;
        DataSet dataspecs;
        if (this.connectionInfo == null) {
            this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        }
        StringBuffer serverlog = new StringBuffer();
        long starttime = System.currentTimeMillis();
        Trace.log("Start GWTDataEntry:" + starttime);
        serverlog.append("Start GWTDataEntry:" + starttime);
        SDITagInfo sdiInfo = this.getSDIInfo();
        if (this.sdiData == null && (sdiInfo == null || sdiInfo.getQueryData("dataitem") == null || sdiInfo.getQueryData("dataitem").getQuerydata() == null)) {
            return "SDIDataItem data not found. DataEntryGrid must be inside an SDI tag and include dataitem in the request attribute.";
        }
        DataSet dataitems = this.sdiData != null ? this.sdiData.getDataset("dataitem") : sdiInfo.getQueryData("dataitem").getQuerydata();
        dataitems.addColumn("protection", 0, 1);
        dataitems.addColumn("samplestatus", 0, 40);
        DataSet dataSet = this.sdiData != null ? this.sdiData.getDataset("dataspec") : (dataspecs = sdiInfo.getQueryData("dataspec") != null && sdiInfo.getQueryData("dataspec").getQuerydata() != null ? sdiInfo.getQueryData("dataspec").getQuerydata() : null);
        DataSet dataSet2 = this.sdiData != null ? this.sdiData.getDataset("dataset") : (dataset = sdiInfo.getQueryData("dataset") != null && sdiInfo.getQueryData("dataset").getQuerydata() != null ? sdiInfo.getQueryData("dataset").getQuerydata() : null);
        DataSet dataSet3 = this.sdiData != null ? this.sdiData.getDataset("primary") : (primary = sdiInfo.getQueryData("primary") != null && sdiInfo.getQueryData("primary").getQuerydata() != null ? sdiInfo.getQueryData("primary").getQuerydata() : null);
        DataSet dataSet4 = this.sdiData != null ? this.sdiData.getDataset("notes") : (sdinotes = sdiInfo.getQueryData("notes") != null && sdiInfo.getQueryData("notes").getQuerydata() != null ? sdiInfo.getQueryData("notes").getQuerydata() : null);
        if (primary.isValidColumn("samplestatus")) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                int row = dataitems.findRow("keyid1", primary.getValue(i, "s_sampleid"));
                if (row <= -1) continue;
                dataitems.setValue(row, "samplestatus", primary.getValue(i, "samplestatus"));
            }
        }
        DataSet dataapproval = null;
        boolean showapproval = "Y".equals(this.element.getProperty("showapproval"));
        if (showapproval) {
            dataapproval = this.sdiData != null ? this.sdiData.getDataset("dataapproval") : (sdiInfo.getQueryData("dataapproval") != null && sdiInfo.getQueryData("dataapproval").getQuerydata() != null ? sdiInfo.getQueryData("dataapproval").getQuerydata() : null);
        }
        SDIRequest secondarySDIRequest = GWTDataEntry.appendSecondarySDCDataSets(true, this.element, this.element.getPropertyList("secondarysdc"), primary, dataset, dataitems, dataspecs, this.getSDIProcessor(), this.getTranslationProcessor());
        JSONObject html = new JSONObject();
        try {
            String keyid1list;
            String specid;
            if (this.pageContext != null) {
                I18nUtil.localizeDisplayValues(dataitems, this.pageContext);
            }
            this.logger.info("Done localizing data:" + (System.currentTimeMillis() - starttime) + "ms");
            serverlog.append("\nDone localizing data:" + (System.currentTimeMillis() - starttime) + "ms");
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            PropertyList sdcProps = sdcProcessor.getPropertyList(this.sdiData != null ? this.sdiData.getSdcid() : sdiInfo.getSdcid());
            this.keycolumns = Integer.parseInt(sdcProps.getProperty("keycolumns"));
            this.keycolid1 = sdcProps.getProperty("keycolid1");
            serverlog.append("\nDone filtering data:" + (System.currentTimeMillis() - starttime) + "ms");
            if (this.requestContext != null && this.requestContext.getPropertyList("pagedata").getProperty("dataitemdisplayrule").length() > 0) {
                this.element.setProperty("dataitemdisplayrule", this.requestContext.getPropertyList("pagedata").getProperty("dataitemdisplayrule"));
            }
            if (this.requestContext != null && this.requestContext.getPropertyList("pagedata").getProperty("savevalidationoption").length() > 0) {
                this.element.setProperty("savevalidationoption", this.requestContext.getPropertyList("pagedata").getProperty("savevalidationoption"));
            }
            html.put("totalitemcount", dataitems.getRowCount());
            html.put("loglevel", this.requestContext != null ? this.requestContext.getProperty("loglevel") : "1");
            if (secondarySDIRequest != null) {
                html.put("redosdcid", secondarySDIRequest.getSDCid());
                html.put("redokeyid1", secondarySDIRequest.getKeyid1List());
                html.put("redokeyid2=", secondarySDIRequest.getKeyid2List());
                html.append("redokeyid3", secondarySDIRequest.getKeyid3List());
            }
            html.put("translate", "parent.translate");
            PropertyListCollection details = this.element.getCollection("details");
            if (details != null) {
                ArrayList<PropertyList> toberemoved = new ArrayList<PropertyList>();
                for (int i = 0; i < details.size(); ++i) {
                    if (!"N".equals(details.getPropertyList(i).getProperty("show"))) continue;
                    toberemoved.add(details.getPropertyList(i));
                }
                details.removeAll(toberemoved);
            }
            serverlog.append("\nBefore toJSONObjectCrossTabModel call:" + (System.currentTimeMillis() - starttime) + "ms");
            PropertyList datantryPolicy = GWTDataEntry.getDataEntryPolicy(this.getConnectionId(), this.element);
            this.activityLogReasonRequired = "2".equals(datantryPolicy.getProperty("auditenteredtextoption"));
            PropertyListCollection visualpolicies = datantryPolicy.getCollection("visualpolicies");
            this.processIconVisualMode(visualpolicies);
            DataItemCrossTabModel crossTabModel = new DataItemCrossTabModel(this.element, datantryPolicy, this.getConnectionId(), sdinotes);
            String string = specid = this.requestContext != null ? this.requestContext.getPropertyList("pagedata").getProperty("specid") : "";
            String string2 = this.requestContext != null && this.requestContext.getPropertyList("pagedata").getProperty("action_maintainkeyid1orderlist").length() > 0 ? this.requestContext.getPropertyList("pagedata").getProperty("action_maintainkeyid1orderlist") : (keyid1list = this.requestContext != null ? this.requestContext.getPropertyList("pagedata").getProperty("keyid1") : "");
            if ("Y".equals(this.element.getProperty("sortbykeyid1list"))) {
                if (keyid1list.length() == 0) {
                    keyid1list = primary.getColumnValues(this.keycolid1, ";");
                }
                crossTabModel.setSortbyKeyid1list(keyid1list);
            }
            if (specid.length() > 0) {
                String specversionid = this.requestContext.getPropertyList("pagedata").getProperty("specversionid");
                if (specversionid.length() == 0) {
                    html.put("errormessage", "Error:Got only specid " + specid + ". Must pass in both specid and specversionid.");
                } else {
                    crossTabModel.setPassedinSpec(specid, specversionid);
                    String passedinspecdisplay = this.element.getProperty("passedinspecdisplay");
                    if (passedinspecdisplay.length() > 0) {
                        String[] tokens = StringUtil.getTokens(passedinspecdisplay);
                        DataSet specDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM spec WHERE specid=? and specversionid=?", new Object[]{specid, specversionid});
                        if (specDS.getRowCount() == 1) {
                            for (int i = 0; i < tokens.length; ++i) {
                                passedinspecdisplay = StringUtil.replaceAll(passedinspecdisplay, "[" + tokens[i] + "]", specDS.getValue(0, tokens[i]));
                            }
                        }
                    } else {
                        passedinspecdisplay = "<span style=\"background-color:orange;font-size:14px\">" + this.getTranslationProcessor().translate("Display Spec Condition for") + "<b>&nbsp;" + specid + " (ver." + specversionid + ")</b></span>";
                    }
                    this.element.setProperty("passedinspecdisplay", passedinspecdisplay);
                }
            } else {
                this.element.setProperty("passedinspecdisplay", "");
            }
            JSONObject dataitemJSON = crossTabModel.toJSONObjectCrossTabModel(dataitems, dataset, primary, dataspecs);
            dataitems = crossTabModel.getProcessedDataitems();
            html.put("totalitemcount", crossTabModel.getTotalItemCount());
            if (this.requestContext != null && "Y".equals(this.requestContext.getPropertyList("pagedata").getProperty("readonly"))) {
                this.element.setProperty("readonly", "Y");
            }
            ElementUtil.setColumnDisplayValue(this.element.getPropertyList("rowheader").getCollection("columns"), sdcProps, this.getTranslationProcessor(), this.getQueryProcessor(), true);
            ElementUtil.setColumnDisplayValue(this.element.getPropertyList("columnheader").getCollection("columns"), sdcProps, this.getTranslationProcessor(), this.getQueryProcessor(), true);
            html.put("gwtdataentrygrid", this.element.toJSONObject(false, false));
            if (this.webpageid == null) {
                this.webpageid = this.requestContext != null ? this.requestContext.getPropertyList("pagedata").getProperty("webpageid") : "";
            }
            dataitemJSON.put("webpageid", this.webpageid);
            dataitemJSON.put("elementid", this.element.getId());
            serverlog.append("\nAfter toJSONObjectCrossTabModel call:" + (System.currentTimeMillis() - starttime) + "ms");
            dataitemJSON.put("sdcid", this.element.getProperty("sdcid").length() > 0 ? this.element.getProperty("sdcid") : dataitems.getString(0, "sdcid"));
            dataitemJSON.put("keycolumns", this.keycolumns);
            html.put("sdidataitemJSON", dataitemJSON);
            if (showapproval) {
                serverlog.append("\nBefore showapproval:" + (System.currentTimeMillis() - starttime) + "ms");
                HashSet<String> dsKeys = new HashSet<String>();
                dsKeys.add("paramlistid");
                dsKeys.add("paramlistversionid");
                dsKeys.add("variantid");
                dsKeys.add("dataset");
                dsKeys.add("keyid1");
                if (2 == this.keycolumns) {
                    dsKeys.add("keyid2");
                } else if (3 == this.keycolumns) {
                    dsKeys.add("keyid2");
                    dsKeys.add("keyid3");
                }
                PropertyListCollection rheaderCollection = this.element.getPropertyList("rowheader").getCollection("columns");
                for (int i = 0; i < rheaderCollection.size(); ++i) {
                    String rheadercolid = rheaderCollection.getPropertyList(i).getProperty("columnid");
                    if (dsKeys.size() == 1 && !"dataset".equals(rheadercolid) || "dataset".equals(rheadercolid) && "N".equals(rheaderCollection.getPropertyList(i).getProperty("show"))) {
                        throw new Exception("Error:Data entry grid with Show Approval property set to Yes must have dataset column shown and defined as the last dataset key column in the rowheader.");
                    }
                    dsKeys.remove(rheaderCollection.getPropertyList(i).getProperty("columnid"));
                }
                if (dsKeys.size() > 0) {
                    throw new Exception("Error:Data entry grid with Show Approval property set to Yes must have all dataset keys, except sdcid and keyid2/keyid3, defined in the row header collections. Following missing - " + dsKeys.toString());
                }
                html.put("sdidataapprovalJSON", this.getValidatedSDIDataApproval(primary, dataset, dataitems, dataapproval, this.element.getProperty("datasetalertrule"), this.connectionInfo, sdcProps, crossTabModel.getPrimaryKeyIndexMap()));
                html.put("sdidataJSON", JSONUtil.toJSONObject(dataset));
                serverlog.append("\nAfter approval processing:" + (System.currentTimeMillis() - starttime) + "ms");
            }
            html.put("dataentrypolicyJSON", datantryPolicy.toJSONObject(false, false));
            html.put("sysUserId", this.connectionInfo.getSysuserId());
            Trace.log("Done Processing crosstab grid data:" + (System.currentTimeMillis() - starttime) + "ms");
            serverlog.append("\nDone Processing crosstab grid data:" + (System.currentTimeMillis() - starttime) + "ms");
            html.put("serverlog", JSONUtil.escape(serverlog.toString()));
        }
        catch (Exception e) {
            try {
                if (e.getMessage() != null) {
                    if (e.getMessage().indexOf("No qualified data items found") >= 0) {
                        html.put("errormessage", this.getTranslationProcessor().translate("No qualified data items found for data entry."));
                    } else {
                        html.put("errormessage", this.getTranslationProcessor().translate("An unexpected error occurred. Please contact your administrator.") + " " + e.getMessage() + "</p>");
                    }
                } else {
                    html.put("errormessage", this.getTranslationProcessor().translate("An unexpected error occurred. Please contact your administrator to check log file.") + "</p>");
                }
                Trace.logError("An unexpected error occurred. Please contact your administrator.", e);
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }
        return html.toString();
    }

    @Override
    public String getHtml() {
        if (this.connectionInfo == null) {
            this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
        }
        StringBuffer html = new StringBuffer();
        html.append("<script>\n");
        html.append("var gwtdataentryjson=" + this.getJSONString() + ";\n");
        html.append("</script>\n");
        html.append("<script>if ( window.parent.document.getElementById( 'rightframe' ) ) { window.parent.document.getElementById( 'rightframe' ).scrolling='no';} sapphire.gwt.addGWTElement( \"gwtdataentry\", \"gwtdataentry\", {} );</script>\n");
        if (this.activityLogReasonRequired) {
            html.append("<style>\n        input[type=text]::-ms-clear { display: none; }\n    </style>");
        }
        return html.toString();
    }

    public String getViewHTML() {
        StringBuilder html = new StringBuilder();
        try {
            int row;
            int i;
            int i2;
            String keyid1list;
            DataSet sdidataitem = this.sdiData.getDataset("dataitem");
            sdidataitem.addColumn("protection", 0, 1);
            DataSet sdidataitemspec = this.sdiData.getDataset("dataspec");
            DataSet sdidata = this.sdiData.getDataset("dataset");
            DataSet primary = this.sdiData.getDataset("primary");
            DataSet sdinotes = this.sdiData.getDataset("notes");
            GWTDataEntry.appendSecondarySDCDataSets(false, this.element, this.element.getPropertyList("secondarysdc"), primary, sdidata, sdidataitem, sdidataitemspec, this.getSDIProcessor(), this.getTranslationProcessor());
            DataItemCrossTabModel model = new DataItemCrossTabModel(this.element, null, this.getConnectionId());
            String string = keyid1list = this.requestContext != null ? this.requestContext.getPropertyList("pagedata").getProperty("keyid1") : "";
            if ("Y".equals(this.element.getProperty("sortbykeyid1list"))) {
                if (keyid1list.length() == 0 && (this.sdiData != null && this.sdiData.getSdcid().length() > 0 || this.sdiInfo != null)) {
                    SDCProcessor sdcProcessor = this.getSDCProcessor();
                    PropertyList sdcProps = sdcProcessor.getPropertyList(this.sdiData != null ? this.sdiData.getSdcid() : this.sdiInfo.getSdcid());
                    this.keycolid1 = sdcProps.getProperty("keycolid1");
                    keyid1list = primary.getColumnValues(this.keycolid1, ";");
                }
                if (keyid1list.length() > 0) {
                    model.setSortbyKeyid1list(keyid1list);
                }
            }
            JSONObject jsonResponseObj = model.toJSONObjectCrossTabModel(sdidataitem, sdidata, primary, sdidataitemspec);
            JSONArray rowHeaderHeader = new JSONArray();
            JSONArray rowHeaderData = jsonResponseObj.getJSONArray("rowheader");
            JSONArray rowHeader = new JSONArray();
            JSONArray colHeaderData = jsonResponseObj.getJSONArray("columnheader");
            JSONArray colHeader = new JSONArray();
            JSONObject keyIndexMap = jsonResponseObj.getJSONObject("keyindex");
            JSONObject columnIndexMap = jsonResponseObj.getJSONObject("columns");
            JSONArray datasetArray = jsonResponseObj.getJSONArray("dataset");
            PropertyListCollection headerColumns = this.element.getPropertyList("columnheader").getCollection("columns");
            String[] columnids = new String[headerColumns.size()];
            for (i2 = 0; i2 < columnids.length; ++i2) {
                columnids[i2] = headerColumns.getPropertyList(i2).getProperty("columnid");
            }
            for (i2 = 0; i2 < colHeaderData.length(); ++i2) {
                String headerValue = GWTDataEntry.getEvaluatedHeader(colHeaderData.getString(i2), headerColumns, columnids, this.getTranslationProcessor());
                colHeader.put(headerValue);
            }
            headerColumns = this.element.getPropertyList("rowheader").getCollection("columns");
            columnids = new String[headerColumns.size()];
            HashMap<String, String> rowHeaderWidthMap = new HashMap<String, String>();
            for (i = 0; i < columnids.length; ++i) {
                columnids[i] = headerColumns.getPropertyList(i).getProperty("columnid");
                if ("N".equals(headerColumns.getPropertyList(i).getProperty("show"))) continue;
                String rowHeaderHeaderValue = headerColumns.getPropertyList(i).getProperty("title").length() > 0 ? headerColumns.getPropertyList(i).getProperty("title") : headerColumns.getPropertyList(i).getProperty("columnid");
                rowHeaderHeader.put(rowHeaderHeaderValue);
                rowHeaderWidthMap.put(rowHeaderHeaderValue, headerColumns.getPropertyList(i).getProperty("width") + "px");
            }
            for (i = 0; i < rowHeaderData.length(); ++i) {
                String headerValue = GWTDataEntry.getEvaluatedHeader(rowHeaderData.getString(i), headerColumns, columnids, this.getTranslationProcessor());
                rowHeader.put(headerValue);
            }
            ArrayList tableRows = new ArrayList();
            int colCount = rowHeaderHeader.length() + colHeader.length();
            int colHeaderRowCount = StringUtil.split(colHeader.get(0).toString(), ";").length;
            for (int colheaderRow = 0; colheaderRow < colHeaderRowCount; ++colheaderRow) {
                int col;
                ArrayList<GridCell> currentRow = new ArrayList<GridCell>();
                for (col = 0; col < rowHeaderHeader.length(); ++col) {
                    GridCell headerCell = new GridCell(rowHeaderHeader.get(col).toString(), null, (String)rowHeaderWidthMap.get(rowHeaderHeader.get(col).toString()));
                    if (colheaderRow == 0) {
                        headerCell.setRowSpan(colHeaderRowCount);
                    } else {
                        headerCell.setRender(false);
                    }
                    currentRow.add(headerCell);
                }
                for (col = 0; col < colHeader.length(); ++col) {
                    String[] colheaders = StringUtil.split(colHeader.get(col).toString(), ";");
                    GridCell headerCell = new GridCell(colheaders[colheaderRow], colheaders, "");
                    currentRow.add(headerCell);
                    int currentIndex = currentRow.indexOf(headerCell);
                    if (currentIndex <= 0) continue;
                    GridCell previousCell = (GridCell)currentRow.get(currentIndex - 1);
                    while (previousCell != null && previousCell.isHeaderValueToIndexEqual(headerCell, colheaderRow)) {
                        headerCell.setRender(false);
                        previousCell.setColSpan(currentIndex - currentRow.indexOf(previousCell) + 1);
                        if (currentRow.indexOf(previousCell) > 0) {
                            previousCell = (GridCell)currentRow.get(currentRow.indexOf(previousCell) - 1);
                            continue;
                        }
                        previousCell = null;
                    }
                }
                tableRows.add(currentRow);
            }
            int fixedCols = StringUtil.split(rowHeader.get(0).toString(), ";").length;
            String[][] policyMatrix = new String[][]{{"speccondition", "FAIL", "color", "red"}, {"releasedflag", "Y", "fontStyle", "italic"}, {"mandatoryflag", "Y", "backgroundColor", "orange"}};
            PropertyList policy = GWTDataEntry.getDataEntryPolicy(this.getConnectionId(), this.element);
            PropertyListCollection visualpolicies = policy.getCollection("visualpolicies");
            if (visualpolicies.size() > 0) {
                this.processIconVisualMode(visualpolicies);
                int totalitems = 0;
                for (int i3 = 0; i3 < visualpolicies.size(); ++i3) {
                    PropertyListCollection values;
                    String columnid = visualpolicies.getPropertyList(i3).getProperty("columnid");
                    if (columnid == null || columnid.length() <= 0 || (values = visualpolicies.getPropertyList(i3).getCollection("values")) == null) continue;
                    totalitems += values.size();
                }
                policyMatrix = new String[totalitems][4];
                int item = 0;
                for (int i4 = 0; i4 < visualpolicies.size(); ++i4) {
                    PropertyListCollection values;
                    String columnid = visualpolicies.getPropertyList(i4).getProperty("columnid");
                    if (columnid == null || columnid.length() <= 0 || (values = visualpolicies.getPropertyList(i4).getCollection("values")) == null) continue;
                    for (int v = 0; v < values.size(); ++v) {
                        PropertyList cueObject = values.getPropertyList(v);
                        String value = cueObject.getProperty("value");
                        if ("[currentuser]".equals(value)) {
                            value = this.connectionInfo.getSysuserId();
                        } else if ("(null)".equals(value)) {
                            value = "";
                        }
                        policyMatrix[item][0] = columnid;
                        policyMatrix[item][1] = value;
                        policyMatrix[item][2] = cueObject.getProperty("styleattribute");
                        policyMatrix[item][3] = cueObject.getProperty("stylevalue");
                        ++item;
                    }
                }
            }
            PropertyListCollection additionalcolumns = this.element.getCollection("columns");
            for (row = 0; row < rowHeader.length(); ++row) {
                ArrayList<GridCell> currentRow = new ArrayList<GridCell>();
                for (int col = 0; col < colCount; ++col) {
                    GridCell gridcell;
                    int itemindex;
                    String[] rowHeaders = StringUtil.split(rowHeader.get(row).toString(), ";");
                    if (col < fixedCols) {
                        GridCell headerCell = new GridCell(rowHeaders[col], rowHeaders, "");
                        currentRow.add(headerCell);
                        if (row <= 0) continue;
                        int tableRowNo = row + colHeaderRowCount;
                        ArrayList previousRow = (ArrayList)tableRows.get(tableRowNo - 1);
                        GridCell previousCell = (GridCell)previousRow.get(col);
                        while (previousCell != null && previousCell.isHeaderValueToIndexEqual(headerCell, col)) {
                            headerCell.setRender(false);
                            previousCell.setRowSpan(row + colHeaderRowCount - tableRows.indexOf(previousRow) + 1);
                            if (tableRows.indexOf(previousRow) > colHeaderRowCount) {
                                previousRow = (ArrayList)tableRows.get(tableRows.indexOf(previousRow) - 1);
                                previousCell = (GridCell)previousRow.get(col);
                                continue;
                            }
                            previousRow = null;
                            previousCell = null;
                        }
                        continue;
                    }
                    String crosstabKey = rowHeaderData.getString(row) + ";" + colHeaderData.getString(col - rowHeaderHeader.length());
                    int n = itemindex = keyIndexMap.has(crosstabKey) ? keyIndexMap.getInt(crosstabKey) : -1;
                    if (itemindex >= 0) {
                        gridcell = new GridCell(datasetArray.getJSONArray(itemindex).getString(12), itemindex, datasetArray, columnIndexMap, policyMatrix, "110px", additionalcolumns, this.getQueryProcessor());
                        currentRow.add(gridcell);
                        continue;
                    }
                    gridcell = new GridCell();
                    currentRow.add(gridcell);
                }
                tableRows.add(currentRow);
            }
            html.append("<table style=\"border-collapse:collapse\">");
            for (row = 0; row < tableRows.size(); ++row) {
                html.append("<tr>");
                ArrayList rowCells = (ArrayList)tableRows.get(row);
                for (int col = 0; col < rowCells.size(); ++col) {
                    html.append(((GridCell)rowCells.get(col)).getHtml());
                }
                html.append("</tr>");
            }
            html.append("</table>");
        }
        catch (JSONException | SapphireException e) {
            if (e.getMessage().indexOf("No qualified data items found") >= 0) {
                return "<p style=\"color:red\">" + this.getTranslationProcessor().translate("No qualified data items found for data entry.");
            }
            this.logger.error("An unexpected error occurred.", e);
            return "<p style=\"color:red\">" + this.getTranslationProcessor().translate("An unexpected error occurred. Please contact your administrator.") + " " + e.getMessage() + "</p>";
        }
        return html.toString();
    }

    private void processIconVisualMode(PropertyListCollection visualpolicies) {
        int iconColIndex = -1;
        for (int i = 0; i < visualpolicies.size(); ++i) {
            String value;
            if (!"1".equals(visualpolicies.getPropertyList(i).getProperty("mode"))) continue;
            HashMap<String, String> displayValueMap = new HashMap<String, String>();
            String reftypeid = visualpolicies.getPropertyList(i).getProperty("reftypeid");
            PropertyListCollection valueicons = visualpolicies.getPropertyList(i).getCollection("valueicons");
            if (valueicons != null) {
                for (int j = 0; j < valueicons.size(); ++j) {
                    String icon = valueicons.getPropertyList(j).getProperty("icon");
                    if (icon.trim().length() > 0 && icon.trim().indexOf("<") != 0) {
                        icon = "<img src=\"" + icon + "\"/>";
                    }
                    displayValueMap.put(valueicons.getPropertyList(j).getProperty("value"), icon);
                }
            }
            if (reftypeid.length() > 0) {
                DataSet ds = new QueryProcessor(this.getConnectionId()).getRefTypeDataSet(reftypeid);
                for (int j = 0; j < ds.getRowCount(); ++j) {
                    value = ds.getValue(j, "refdisplayicon").length() > 0 ? ds.getValue(j, "refdisplayicon") : (ds.getValue(j, "refdisplayvalue").length() > 0 ? ds.getValue(j, "refdisplayvalue") : ds.getValue(j, "refvalueid"));
                    value = ds.getValue(j, "refdisplayicon").length() > 0 && value.trim().indexOf("<") != 0 ? "<img src=\"" + value + "\" title=\"" + this.getTranslationProcessor().translate(ds.getValue(j, "refdisplayvalue")) + "\"/>" : this.getTranslationProcessor().translatePartial(value);
                    displayValueMap.put(ds.getValue(j, "refvalueid"), value);
                }
            }
            Iterator keysetItr = displayValueMap.keySet().iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (keysetItr.hasNext()) {
                value = (String)keysetItr.next();
                stringBuilder.append(";" + value + "=" + (String)displayValueMap.get(value));
            }
            PropertyListCollection columns = this.element.getCollection("columns");
            if (columns == null || columns.size() <= 0) continue;
            String columnid = visualpolicies.getPropertyList(i).getProperty("columnid");
            PropertyList column = columns.find("columnid", columnid);
            if (column == null) {
                column = new PropertyList();
                column.setProperty("columnid", columnid);
                if (iconColIndex < 0) {
                    for (int c = 0; c < columns.size(); ++c) {
                        if (!"displayunits".equals(columns.getPropertyList(c).getProperty("columnid"))) continue;
                        iconColIndex = c + 1;
                    }
                    if (iconColIndex < 0) {
                        iconColIndex = 0;
                    }
                    columns.add(iconColIndex, column);
                } else {
                    columns.add(++iconColIndex, column);
                }
            }
            column.setProperty("displayvalue", stringBuilder.indexOf(";") == 0 ? stringBuilder.substring(1) : "");
        }
    }

    public static PropertyList getDataEntryPolicy(String connectionid, PropertyList element) {
        PropertyList pl = null;
        try {
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(connectionid);
            pl = configProcessor.getPolicy("DataEntryPolicy", "Sapphire Custom");
            if (element != null) {
                if (element.getCollection("applicablevisualpolicies") != null && element.getCollection("applicablevisualpolicies").size() > 0) {
                    PropertyListCollection applicables = element.getCollection("applicablevisualpolicies");
                    ArrayList<String> applicableList = new ArrayList<String>();
                    for (int i = 0; i < applicables.size(); ++i) {
                        if ("N".equals(applicables.getPropertyList(i).getProperty("show"))) continue;
                        applicableList.add(applicables.getPropertyList(i).getProperty("columnid"));
                    }
                    ArrayList<PropertyList> toberemovedList = new ArrayList<PropertyList>();
                    PropertyListCollection visualpolicies = pl.getCollection("visualpolicies");
                    for (int i = 0; i < visualpolicies.size(); ++i) {
                        if (applicableList.contains(visualpolicies.getPropertyList(i).getProperty("columnid"))) continue;
                        toberemovedList.add(visualpolicies.getPropertyList(i));
                    }
                    visualpolicies.removeAll(toberemovedList);
                }
                if (element.getProperty("livelimitcheck").length() > 0) {
                    pl.setProperty("livelimitcheck", element.getProperty("livelimitcheck"));
                }
                if (element.getProperty("allowautoload").length() > 0) {
                    pl.setProperty("allowautoload", element.getProperty("allowautoload"));
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to retrieve data entry policy.", e);
        }
        return pl;
    }

    private JSONObject getValidatedSDIDataApproval(DataSet primary, DataSet dataset, DataSet dataitems, DataSet dataapproval, String datasetstatusrule, ConnectionInfo connectionInfo, PropertyList sdcProps, HashMap primaryKeyIndexMap) throws JSONException, SapphireException {
        JSONObject obj = new JSONObject();
        dataapproval.addColumn("stepstatusflag", 0);
        dataset.addColumn("datasetstatusalert", 0);
        String rolelist = ";" + connectionInfo.getRoleList();
        String sysuserid = connectionInfo.getSysuserId();
        HashMap<String, Object> datasetFilter = new HashMap<String, Object>();
        HashMap<String, Cloneable> bindMap = new HashMap<String, Cloneable>();
        bindMap.put("user", connectionInfo.getUserAttributeMap());
        bindMap.put("sdc", sdcProps);
        bindMap.put("element", this.element);
        bindMap.put("pagedata", this.requestContext.getPropertyList("pagedata"));
        HashMap<String, String> samplestatusMap = null;
        for (int ds = 0; ds < dataset.getRowCount(); ++ds) {
            DataSet currentapproval;
            dataset.setValue(ds, "datasetstatusalert", "");
            String datasetkey = dataset.getValue(ds, "keyid1") + ";" + dataset.getValue(ds, "keyid2") + ";" + dataset.getValue(ds, "keyid3") + ";" + dataset.getValue(ds, "paramlistid") + ";" + dataset.getValue(ds, "paramlistversionid") + ";" + dataset.getValue(ds, "variantid") + ";" + dataset.getValue(ds, "dataset");
            datasetFilter.put("keyid1", dataset.getValue(ds, "keyid1"));
            datasetFilter.put("keyid2", dataset.getValue(ds, "keyid2"));
            datasetFilter.put("keyid3", dataset.getValue(ds, "keyid3"));
            datasetFilter.put("paramlistid", dataset.getValue(ds, "paramlistid"));
            datasetFilter.put("paramlistversionid", dataset.getValue(ds, "paramlistversionid"));
            datasetFilter.put("variantid", dataset.getValue(ds, "variantid"));
            datasetFilter.put("dataset", dataset.getBigDecimal(ds, "dataset"));
            String datasetstatusalert = "";
            if (datasetstatusrule.length() > 0) {
                bindMap.put("sdidata", (Cloneable)dataset.get(ds));
                DataSet diDs = dataitems.getFilteredDataSet(datasetFilter);
                int findrow = diDs.findRow("speccondition", "Fail");
                bindMap.put("sdidataitemdataset", diDs);
                bindMap.put("primary", (Cloneable)primaryKeyIndexMap.get(dataset.getValue(ds, "keyid1") + (this.keycolumns == 2 ? ";" + dataset.getValue(ds, "keyid2") : "") + (this.keycolumns == 3 ? ";" + dataset.getValue(ds, "keyid3") : "")));
                try {
                    datasetstatusalert = GroovyUtil.getInstance(connectionInfo).evaluateSecure(datasetstatusrule, bindMap);
                }
                catch (Exception e) {
                    throw new SapphireException("Data Set Status Alert Rule groovy syntax error:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())));
                }
            }
            dataset.setValue(ds, "datasetstatusalert", datasetstatusalert);
            String datasetstatus = dataset.getValue(ds, "s_datasetstatus");
            if (samplestatusMap == null) {
                samplestatusMap = new HashMap<String, String>();
                if (primary.isValidColumn("samplestatus")) {
                    for (int i = 0; i < primary.getRowCount(); ++i) {
                        samplestatusMap.put(primary.getValue(i, "s_sampleid"), primary.getValue(i, "samplestatus"));
                    }
                }
            }
            if ("Reviewed".equals(samplestatusMap.get(dataset.getValue(ds, "keyid1"))) || !"Completed".equals(datasetstatus) && !"Released".equals(datasetstatus) || (currentapproval = dataapproval.getFilteredDataSet(datasetFilter)) == null || currentapproval.getRowCount() <= 0) continue;
            String approvalsequenceflag = dataset.getValue(ds, "approvalsequenceflag");
            String uniquenessflag = dataset.getValue(ds, "uniquenessflag");
            ApprovalStepUtil.checkApprovalSteps(currentapproval, rolelist, sysuserid, approvalsequenceflag, uniquenessflag, dataitems.getFilteredDataSet(datasetFilter), "s_analystid");
            obj.put(datasetkey, JSONUtil.toJSONObject(currentapproval).toString());
        }
        return obj;
    }

    private static String getSecondaryKey(String secondarykeyidexp, DataSet primary, DataSet dataset, DataSet dataitem) {
        if (secondarykeyidexp.indexOf("$G{") != 0) {
            if (secondarykeyidexp.indexOf("primary.") == 0) {
                String secondarycolumnid = secondarykeyidexp.substring(8);
                secondarykeyidexp = primary.getColumnValues(secondarycolumnid, ";");
            } else if (secondarykeyidexp.indexOf("sdidata.") == 0) {
                String secondarycolumnid = secondarykeyidexp.substring(8);
                secondarykeyidexp = dataset.getColumnValues(secondarycolumnid, ";");
            } else if (secondarykeyidexp.indexOf("sdidataitem.") == 0) {
                String secondarycolumnid = secondarykeyidexp.substring(12);
                secondarykeyidexp = dataitem.getColumnValues(secondarycolumnid, ";");
            }
        }
        return secondarykeyidexp;
    }

    public static SDIRequest appendSecondarySDCDataSets(boolean isInitialLoad, PropertyList element, PropertyList secondarySDCPL, DataSet primary, DataSet dataset, DataSet dataitems, DataSet dataitemspec, SDIProcessor sdiProcessor, TranslationProcessor tp) {
        boolean merge = true;
        SDIRequest sdiRequest = null;
        if (merge && secondarySDCPL != null) {
            String secondarySDC = secondarySDCPL.getProperty("sdcid");
            String secondarykeyid1exp = secondarySDCPL.getProperty("keyid1");
            String secondarykeyid2exp = secondarySDCPL.getProperty("keyid2");
            String secondarykeyid3exp = secondarySDCPL.getProperty("keyid3");
            boolean keyCount = false;
            secondarykeyid1exp = GWTDataEntry.getSecondaryKey(secondarykeyid1exp, primary, dataset, dataitems);
            DataSet secondary = null;
            DataSet secondarydataset = null;
            DataSet secondarydataitem = null;
            DataSet secondarydataspec = null;
            if (secondarykeyid1exp != null && secondarykeyid1exp.length() > 0) {
                if (secondary == null) {
                    sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(secondarySDC);
                    String requestStr = GWTDataEntry.buildRequest(element);
                    String[] datarequest = RequestParser.parseRequestItem(requestStr);
                    for (int i = 0; i < datarequest.length; ++i) {
                        sdiRequest.setRequestItem(datarequest[i].trim());
                    }
                    sdiRequest.setKeyid1List(secondarykeyid1exp);
                    if (secondarykeyid2exp.length() > 0) {
                        secondarykeyid2exp = GWTDataEntry.getSecondaryKey(secondarykeyid2exp, primary, dataset, dataitems);
                        sdiRequest.setKeyid2List(secondarykeyid2exp);
                    }
                    if (secondarykeyid3exp.length() > 0) {
                        secondarykeyid3exp = GWTDataEntry.getSecondaryKey(secondarykeyid3exp, primary, dataset, dataitems);
                        sdiRequest.setKeyid3List(secondarykeyid3exp);
                    }
                    SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                    secondary = sdiData.getDataset("primary");
                    secondarydataset = sdiData.getDataset("dataset");
                    secondarydataitem = sdiData.getDataset("dataitem");
                    secondarydataspec = sdiData.getDataset("dataspec");
                }
                if (secondary != null && secondary.getRowCount() > 0) {
                    String value;
                    int i;
                    PropertyListCollection columns;
                    PropertyList propertyList;
                    String sdcidExpression = "";
                    boolean translatevalue = false;
                    if (element != null && (propertyList = element.getPropertyList("rowheader")) != null && (columns = propertyList.getCollection("columns")) != null) {
                        for (int c = 0; c < columns.size(); ++c) {
                            if (!"sdcid".equals(columns.getPropertyList(c).getProperty("columnid"))) continue;
                            String sdcDisplayValue = columns.getPropertyList(c).getProperty("displayvalue");
                            sdcidExpression = SDITagUtil.getDisplayValue(secondarySDC, sdcDisplayValue);
                            translatevalue = "Y".equals(columns.getPropertyList(c).getProperty("translatevalue"));
                            break;
                        }
                    }
                    PropertyListCollection colmappings = secondarySDCPL.getCollection("columns");
                    HashMap<String, String> map = new HashMap<String, String>();
                    if (colmappings != null) {
                        for (int i2 = 0; i2 < colmappings.size(); ++i2) {
                            PropertyList col = colmappings.getPropertyList(i2);
                            map.put(col.getProperty("maptocolumnid"), col.getProperty("columnid"));
                        }
                    }
                    int row = 0;
                    String[] primarycolumns = primary.getColumns();
                    for (int i3 = 0; i3 < secondary.getRowCount(); ++i3) {
                        row = primary.addRow();
                        for (int c = 0; c < primarycolumns.length; ++c) {
                            String secondarycolid = (String)map.get(primarycolumns[c]);
                            if (secondarycolid == null) continue;
                            primary.setValue(row, primarycolumns[c], secondary.getValue(i3, secondarycolid));
                        }
                    }
                    String[] columns2 = dataset.getColumns();
                    for (i = 0; i < secondarydataset.getRowCount(); ++i) {
                        row = dataset.addRow();
                        for (int c = 0; c < columns2.length; ++c) {
                            value = secondarydataset.getValue(i, columns2[c]);
                            dataset.setValue(row, columns2[c], value);
                        }
                    }
                    if (isInitialLoad) {
                        columns2 = dataitems.getColumns();
                        for (i = 0; i < secondarydataitem.getRowCount(); ++i) {
                            row = dataitems.addRow();
                            for (int c = 0; c < columns2.length; ++c) {
                                value = secondarydataitem.getValue(i, columns2[c]);
                                dataitems.setValue(row, columns2[c], value);
                            }
                            dataitems.setValue(row, "protection", "S");
                        }
                    }
                    String[] tokens = StringUtil.getTokens(sdcidExpression);
                    for (int i4 = 0; i4 < dataitems.getRowCount(); ++i4) {
                        if (sdcidExpression.length() <= 0 || !dataitems.getValue(i4, "sdcid").equals(secondarySDC)) continue;
                        dataitems.setValue(row, "protection", "S");
                        String sdcidHeaderValue = sdcidExpression;
                        if (tokens != null && tokens.length > 0) {
                            for (int t = 0; t < tokens.length; ++t) {
                                if (dataitems.getValue(i4, tokens[t]) == null) continue;
                                sdcidHeaderValue = StringUtil.replaceAll(sdcidHeaderValue, "[" + tokens[t] + "]", translatevalue ? tp.translate(dataitems.getValue(i4, tokens[t])) : dataitems.getValue(i4, tokens[t]));
                            }
                        }
                        String value2 = "zzz_" + sdcidHeaderValue;
                        dataitems.setValue(i4, "sdcid", value2);
                    }
                    String sdcids = dataitems.getColumnValues("sdcid", ";");
                    columns2 = dataitemspec.getColumns();
                    for (int i5 = 0; i5 < secondarydataspec.getRowCount(); ++i5) {
                        row = dataitemspec.addRow();
                        for (int c = 0; c < columns2.length; ++c) {
                            String value3 = secondarydataspec.getValue(i5, columns2[c]);
                            dataitemspec.setValue(row, columns2[c], value3);
                        }
                    }
                }
            }
        }
        return sdiRequest;
    }

    private static String getEvaluatedHeader(String headerValues, PropertyListCollection headerColumns, String[] columnids, TranslationProcessor tp) {
        StringBuilder sb = new StringBuilder();
        String[] allheadervalues = StringUtil.split(headerValues, ";");
        for (int col = 0; col < headerColumns.size(); ++col) {
            boolean hasLink;
            PropertyList headerRow = headerColumns.getPropertyList(col);
            if ("N".equals(headerRow.getProperty("show"))) continue;
            boolean translateValue = "Y".equals(headerRow.getProperty("translatevalue"));
            HashMap<String, String> valueMap = new HashMap<String, String>();
            for (int i = 0; i < columnids.length; ++i) {
                valueMap.put(columnids[i], translateValue ? tp.translate(allheadervalues[i]) : allheadervalues[i]);
            }
            String pseudocolumn = headerRow.getProperty("pseudocolumn");
            PropertyList link = headerRow.getPropertyList("link");
            String headerValue = allheadervalues[col].replaceAll("#semicolon#", ";");
            String displaylist = headerRow.getProperty("displayvalue");
            if (displaylist.length() > 0) {
                displaylist = GWTDataEntry.evaluateExpression(displaylist, valueMap);
                headerValue = GWTDataEntry.getDisplayValue(headerValue, displaylist);
            }
            if (translateValue) {
                headerValue = tp.translate(headerValue);
            }
            boolean hasPseudoCol = pseudocolumn.length() > 0;
            boolean bl = hasLink = link != null && link.getProperty("href").length() > 0;
            if (hasPseudoCol) {
                headerValue = GWTDataEntry.evaluateExpression(pseudocolumn, valueMap);
            } else if (hasLink) {
                for (int i = 0; i < columnids.length; ++i) {
                    valueMap.put(columnids[i], allheadervalues[i]);
                }
                headerValue = GWTDataEntry.evaluateLinkHtml(link, headerValue, valueMap);
            }
            sb.append(";" + headerValue.replaceAll(";", "#semicolon#"));
        }
        return sb.substring(1);
    }

    public static String evaluateLinkHtml(PropertyList link, String linkdisplay, HashMap valueMap) {
        StringBuffer html = new StringBuffer();
        String target = link.getProperty("target");
        if (target == null || target.length() == 0) {
            target = "_self";
        }
        String href = link.getProperty("href");
        href = GWTDataEntry.evaluateExpression(href, valueMap);
        String title = link.getProperty("tip");
        title = GWTDataEntry.evaluateExpression(title, valueMap);
        href = "JavaScript:sapphire.lookup.util.openWindow('View','Sapphire','" + href.replaceAll("%", "%25") + "'," + 800 + "," + 600 + ",false,null,false)";
        html.append("<a href=\"").append(href + "\" target=\"").append("_self").append("\"");
        if (title != null && title.length() > 0) {
            html.append(" title=\"").append(title).append("\"");
        }
        if (linkdisplay.length() > 0) {
            html.append(">").append(linkdisplay).append("</a>");
        } else if (valueMap.get("value") != null && ((String)valueMap.get("value")).length() > 0) {
            String value = (String)valueMap.get("value");
            if (value != null) {
                value = value.replaceAll("<", "&lt;");
            }
            html.append(">").append(value).append("</a>");
        } else {
            return "";
        }
        return html.toString();
    }

    public static String getDisplayValue(String value, String displaylist) {
        String displayValue = value;
        String wildcardValue = "";
        boolean displayValueFound = false;
        boolean wildCardFound = false;
        String[] displays = displaylist.split(";");
        if (displays != null && displays.length > 0) {
            for (int i = 0; i < displays.length; ++i) {
                String display = displays[i].trim();
                int pos = display.indexOf("=");
                if (pos <= -1) continue;
                if (display.substring(0, pos).equals(value)) {
                    displayValue = display.substring(pos + 1).trim();
                    displayValueFound = true;
                    break;
                }
                if (wildCardFound || !display.substring(0, pos).equals("*")) continue;
                wildcardValue = display.substring(pos + 1).trim();
                wildCardFound = true;
            }
        }
        if (!displayValueFound && wildCardFound) {
            displayValue = wildcardValue;
        }
        return displayValue;
    }

    public static String evaluateExpression(String expression, HashMap valueMap, HashMap displaylistMap) {
        Iterator itr = valueMap.keySet().iterator();
        HashMap<String, String> tempValueMap = new HashMap<String, String>();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            if (displaylistMap.get(key) != null) {
                tempValueMap.put(key, GWTDataEntry.getDisplayValue((String)valueMap.get(key), (String)displaylistMap.get(key)));
                continue;
            }
            tempValueMap.put(key, (String)valueMap.get(key));
        }
        return GWTDataEntry.evaluateExpression(expression, tempValueMap);
    }

    public static String evaluateExpression(String expression, HashMap valueMap) {
        String[] tokens = StringUtil.getTokens(expression);
        if (tokens != null && tokens.length > 0) {
            for (int i = 0; i < tokens.length; ++i) {
                if (valueMap.get(tokens[i]) == null) continue;
                expression = StringUtil.replaceAll(expression, "[" + tokens[i] + "]", (String)valueMap.get(tokens[i]));
            }
        }
        return expression;
    }

    public static String buildRequest(PropertyList gwtdataentrygrid) {
        boolean showApproval = "Y".equals(gwtdataentrygrid.getProperty("showapproval"));
        boolean showNotes = "Y".equals(gwtdataentrygrid.getProperty("shownotes"));
        HashMap<String, String> rMap = GWTDataEntry.buildRequestMap(gwtdataentrygrid, showApproval, showNotes);
        return rMap.get("primary") + "," + rMap.get("dataset") + "," + rMap.get("sdiworkitem") + "," + rMap.get("dataitem") + "," + rMap.get("dataspec") + (rMap.get("dataapproval").length() > 0 ? "," + rMap.get("dataapproval") : "") + (rMap.get("notes").length() > 0 ? "," + rMap.get("notes") : "");
    }

    public static HashMap<String, String> buildRequestMap(PropertyList gwtdataentrygrid, boolean showApproval, boolean showNotes) {
        ArrayList<String> additionalcolumnsList = new ArrayList<String>();
        if (gwtdataentrygrid.getCollection("columns") != null) {
            PropertyListCollection columns = gwtdataentrygrid.getCollection("columns");
            for (int i = 0; i < columns.size(); ++i) {
                additionalcolumnsList.add(columns.getPropertyList(i).getProperty("columnid"));
            }
        }
        HashMap<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("primary", "primary");
        requestMap.put("dataset", "dataset[paramlistid.displayunderparamlistname displayunderparamlistname, paramlistid.displayundervariantname displayundervariantname, paramlistid.s_trainingreqflag s_trainingreqflag, paramlistid.s_overrideallowedflag s_overrideallowedflag]");
        StringBuffer itemSb = new StringBuffer("dataitem");
        String extraparamlistitemcols = "paramlistitem.displayunderparamname displayunderparamname, paramlistitem.displayunderparamtype displayunderparamtype, paramlistitem.displaywidth, paramlistitem.editorstyleid";
        if (additionalcolumnsList.size() > 0) {
            StringBuffer dataitemcols = new StringBuffer();
            for (int i = 0; i < additionalcolumnsList.size(); ++i) {
                String columnid = (String)additionalcolumnsList.get(i);
                if (columnid.indexOf(".") <= 0 && columnid.indexOf("(") < 0 || columnid.indexOf("primary.") == 0 || columnid.indexOf("sdidata.") == 0) continue;
                columnid = StringUtil.replaceAll(columnid, "[specid]", gwtdataentrygrid.getProperty("specid"));
                columnid = StringUtil.replaceAll(columnid, "[specversionid]", gwtdataentrygrid.getProperty("specversionid"));
                dataitemcols.append((dataitemcols.length() == 0 ? "" : ",") + columnid);
            }
            if (dataitemcols.length() > 0) {
                itemSb.append("[" + extraparamlistitemcols + "," + dataitemcols + "]");
            } else {
                itemSb.append("[" + extraparamlistitemcols + "]");
            }
        } else {
            itemSb.append("[" + extraparamlistitemcols + "]");
        }
        requestMap.put("sdiworkitem", "sdiworkitem[workitemid.preferreddataentryview preferreddataentryview]");
        requestMap.put("dataitem", itemSb.toString());
        requestMap.put("dataspec", "dataspec[sdispec.oosgeneratingflag]");
        requestMap.put("dataapproval", showApproval ? "dataapproval" : "");
        if (!showNotes) {
            // empty if block
        }
        requestMap.put("notes", "notes[context]");
        return requestMap;
    }

    private static class GridCell {
        private String value;
        private String valuestatus;
        private String width;
        private boolean isEmpty;
        private int colSpan;
        private int rowSpan;
        private boolean render;
        private boolean isHeader;
        private String[] headerValues;
        private int datarow;
        private JSONArray dataitems;
        private JSONObject attributeIndexMap;
        private String[][] policyMatrix;
        private PropertyListCollection additionColumns;
        private static HashMap<String, String> jsStyleHTMLStyleMap = new HashMap();

        GridCell() {
            this.width = "";
            this.colSpan = 1;
            this.rowSpan = 1;
            this.render = true;
            this.isHeader = false;
            this.datarow = -1;
            this.setIsEmpty(true);
        }

        GridCell(String value, String[] headerValues, String width) {
            this.width = "";
            this.colSpan = 1;
            this.rowSpan = 1;
            this.render = true;
            this.isHeader = false;
            this.datarow = -1;
            this.value = value;
            this.headerValues = headerValues;
            this.isHeader = true;
            this.width = width;
        }

        GridCell(String value, int datarow, JSONArray dataitems, JSONObject attributeIndexMap, String[][] policyMatrix, String width, PropertyListCollection additionColumns, QueryProcessor queryProcessor) {
            block4: {
                this.width = "";
                this.colSpan = 1;
                this.rowSpan = 1;
                this.render = true;
                this.isHeader = false;
                this.datarow = -1;
                this.value = value;
                this.datarow = datarow;
                this.dataitems = dataitems;
                this.attributeIndexMap = attributeIndexMap;
                this.policyMatrix = policyMatrix;
                this.width = width;
                this.additionColumns = additionColumns;
                try {
                    this.valuestatus = dataitems.getJSONArray(datarow).getString(10);
                    if (this.valuestatus != null && this.valuestatus.length() > 0) {
                        this.value = dataitems.getJSONArray(datarow).getString(14);
                        break block4;
                    }
                    if (!"V".equals(dataitems.getJSONArray(datarow).getString(15)) || value == null || value.length() <= 0) break block4;
                    String entryreftypeid = dataitems.getJSONArray(datarow).getString(19);
                    DataSet refValuesDataset = queryProcessor.getRefTypeDataSet(entryreftypeid);
                    for (int i = 0; i < refValuesDataset.getRowCount(); ++i) {
                        if (!value.equals(refValuesDataset.getValue(i, "refvalueid"))) continue;
                        this.value = refValuesDataset.getValue(i, "refdisplayvalue", value);
                        break;
                    }
                }
                catch (JSONException jSONException) {
                    // empty catch block
                }
            }
        }

        String getHtml() {
            if (this.render) {
                if (this.isHeader) {
                    return "<th" + (this.colSpan > 1 ? " colspan=" + this.colSpan : (this.rowSpan > 0 ? " rowspan=" + this.rowSpan : "")) + " style=\"" + (this.width.length() > 0 ? "min-width:" + this.width + ";" : "") + "border:1px solid gray;background-color:whitesmoke;text-align:left\">" + this.value.replaceAll("#semicolon#", ";") + "</th>";
                }
                if (this.isEmpty) {
                    return "<td style=\"border:1px solid gray;background-color:lightgray\"></td>";
                }
                StringBuilder sb = new StringBuilder("<td style=\"text-align:left; white-space:nowrap;border:1px solid gray;background-color:white\">");
                String appliedstyle = this.getStyleAttribute();
                sb.append("<div style=\"display:inline-block;border:1px;height:17px;" + appliedstyle + "\" " + (this.valuestatus.length() > 0 ? " title=\"" + this.valuestatus + "\"" : "") + ">");
                sb.append(SafeHTML.encodeForHTML(this.value));
                sb.append("</div>");
                if (this.additionColumns != null) {
                    for (int i = 0; i < this.additionColumns.size(); ++i) {
                        if (!"readonly".equals(this.additionColumns.getPropertyList(i).getProperty("mode")) && (!"".equals(this.additionColumns.getPropertyList(i).getProperty("mode")) || !"speccondition".equals(this.additionColumns.getPropertyList(i).getProperty("columnid")))) continue;
                        PropertyList columnPL = this.additionColumns.getPropertyList(i);
                        String columnid = columnPL.getProperty("columnid");
                        if (columnid.indexOf(" ") > 0) {
                            columnid = columnid.substring(columnid.lastIndexOf(" "));
                        }
                        String width = columnPL.getProperty("width").length() > 0 ? columnPL.getProperty("width") + "px" : "35px";
                        sb.append("<div style=\"margin-left:3px;display:inline-block;height:17px;min-width:" + width + "\">");
                        try {
                            int valueIndex = this.attributeIndexMap.getInt(columnid);
                            String value = this.dataitems.getJSONArray(this.datarow).getString(valueIndex);
                            String displaylist = columnPL.getProperty("displayvalue");
                            if (displaylist.length() > 0) {
                                if (displaylist.indexOf("[") >= 0 && displaylist.indexOf("]") > 0) {
                                    HashMap<String, String> valueMap = new HashMap<String, String>();
                                    String[] columnids = StringUtil.getTokens(displaylist);
                                    for (int c = 0; c < columnids.length; ++c) {
                                        int colindex = this.attributeIndexMap.getInt(columnids[c]);
                                        valueMap.put(columnids[c], this.dataitems.getJSONArray(this.datarow).getString(colindex));
                                    }
                                    displaylist = GWTDataEntry.evaluateExpression(displaylist, valueMap);
                                }
                                value = GWTDataEntry.getDisplayValue(value, displaylist);
                            }
                            sb.append(value);
                        }
                        catch (JSONException jSONException) {
                            // empty catch block
                        }
                        sb.append("</div>");
                    }
                }
                sb.append("</td>");
                return sb.toString();
            }
            return "";
        }

        public String getStyleAttribute() {
            HashSet<String> processedSet = new HashSet<String>();
            StringBuilder sb = new StringBuilder();
            try {
                JSONArray dataRow = this.dataitems.getJSONArray(this.datarow);
                String datatypes = dataRow.getString(15);
                String displaywidth = dataRow.getString(29);
                sb.append(displaywidth.length() > 0 ? "min-width:" + displaywidth : (this.width != null && this.width.length() > 0 ? "min-width:" + this.width : ""));
                if (!("N".equals(datatypes) || "NC".equals(datatypes) || "D".equals(datatypes) || "O".equals(datatypes) || "DC".equals(datatypes) || "OC".equals(datatypes))) {
                    sb.append(";text-align;left");
                } else {
                    sb.append(";text-align:right");
                }
                for (int i = 0; i < this.policyMatrix.length; ++i) {
                    String attributeid = this.policyMatrix[i][0];
                    int valueIndex = this.attributeIndexMap.getInt(attributeid);
                    String value = dataRow.getString(valueIndex);
                    String matchValue = this.policyMatrix[i][1];
                    String matchStyle = jsStyleHTMLStyleMap.get(this.policyMatrix[i][2]);
                    String styleValue = this.policyMatrix[i][3];
                    if (!matchValue.equals(value) && (!"(notnull)".equals(matchValue) || value.length() <= 0) || processedSet.contains(matchStyle)) continue;
                    sb.append(";" + matchStyle + ":" + styleValue);
                    processedSet.add(matchStyle);
                }
            }
            catch (JSONException jsonEx) {
                jsonEx.printStackTrace();
            }
            return sb.indexOf(";") == 0 ? sb.substring(1) : sb.toString();
        }

        public String getValue() {
            return this.value;
        }

        public boolean isHeader() {
            return this.isHeader;
        }

        public String[] getHeaderValues() {
            return this.headerValues;
        }

        private boolean isHeaderValueToIndexEqual(GridCell gridCell, int index) {
            String[] compareToHeaders = gridCell.getHeaderValues();
            if (this.headerValues != null && index < this.headerValues.length && compareToHeaders != null && index < compareToHeaders.length) {
                for (int i = 0; i <= index; ++i) {
                    if (this.headerValues[i].equals(compareToHeaders[i])) continue;
                    return false;
                }
                return true;
            }
            return false;
        }

        public void setIsEmpty(boolean isEmpty) {
            this.isEmpty = isEmpty;
            this.value = "";
        }

        public void setColSpan(int colSpan) {
            this.colSpan = colSpan;
        }

        public void setRender(boolean render) {
            this.render = render;
        }

        public void setRowSpan(int rowSpan) {
            this.rowSpan = rowSpan;
        }

        static {
            jsStyleHTMLStyleMap.put("backgroundColor", "background-color");
            jsStyleHTMLStyleMap.put("borderColor", "border-color");
            jsStyleHTMLStyleMap.put("color", "color");
            jsStyleHTMLStyleMap.put("fontStyle", "font-style");
        }
    }
}

