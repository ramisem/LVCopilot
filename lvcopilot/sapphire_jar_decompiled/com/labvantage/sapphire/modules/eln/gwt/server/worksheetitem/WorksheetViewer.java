/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.gwt.server.LoadWorksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class WorksheetViewer
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/worksheetviewer.js");
        worksheetItemIncludes.setJSObjectName("worksheetViewer");
    }

    @Override
    public String getDockViewHTML(String prefix) throws SapphireException {
        return this.getHTML(true, false);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTML(false, false);
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getHTML(false, true);
    }

    public String getHTML(boolean dock, boolean export) throws SapphireException {
        DataSet ref = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitemreference WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
        String referenceSDC = ref.getValue(0, "refsdcid");
        String refworksheetid = ref.getValue(0, "refworksheetid");
        String refworksheetversionid = ref.getValue(0, "refworksheetversionid");
        if (ref.size() == 1 && refworksheetid.length() > 0 && referenceSDC.length() > 0 && ref.getValue(0, "refkeyid1").length() > 0) {
            JSONObject contents;
            boolean expand = export || this.config.getProperty("expandcontent", "Y").equals("Y");
            try {
                contents = new JSONObject(this.getContents());
            }
            catch (JSONException e) {
                contents = new JSONObject();
            }
            DataSet refWorksheetDetails = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetname FROM worksheet WHERE worksheetid=? AND worksheetversionid=?", (Object[])new String[]{refworksheetid, refworksheetversionid});
            String refworksheetname = refWorksheetDetails.getValue(0, "worksheetname", contents.optString("worksheetname"));
            String function = ref.getValue(0, "reffunction", "link");
            StringBuffer html = new StringBuffer();
            html.append("<table width=\"100%\"><tr><td title=\"").append(BaseELNAction.getIdVersionText(refworksheetid, ref.getValue(0, "refworksheetversionid", "1"))).append("\" style=\"font-weight:bold\">");
            if (function.equals("include")) {
                html.append("<a href=\"javascript:sapphire.alert( '<b>Copied</b> references take a copy of the referenced section or control at the time the control is added to the worksheet and are static.')\">");
            } else {
                html.append("<a href=\"javascript:sapphire.alert( '<b>Linked</b> references are updated each time the worksheet is loaded and may change over time as the referenced worksheet changes.')\">");
            }
            if (refworksheetid.equals(this.getWorksheetId())) {
                html.append(this.getTranslationProcessor().translate((function.equals("include") ? "Copied" : "Linked") + "</a> reference to " + (referenceSDC.equals("LV_Worksheet") ? "this worksheet" : (referenceSDC.equals("LV_WorksheetSection") ? "a section in this worksheet" : "a <a href=\"javascript:sapphire.worksheet.setCurrentWorksheetItem( '" + ref.getValue(0, "refkeyid1") + "', '" + ref.getValue(0, "refkeyid2") + "' )\">control</a> in this worksheet"))));
            } else {
                html.append(this.getTranslationProcessor().translate((function.equals("include") ? "Copied" : "Linked") + "</a> reference to a <a href=\"javascript:sapphire.page.navigate( 'rc?command=page&page=WorksheetManager&worksheetid=" + refworksheetid + "&worksheetversionid=" + refworksheetversionid + "&selecttype=" + (referenceSDC.equals("LV_Worksheet") ? "W" : (referenceSDC.equals("LV_WorksheetSection") ? "S" : "I")) + "&selectid=" + ref.getValue(0, "refkeyid1") + "&selectversion=" + ref.getValue(0, "refkeyid2") + "', false, 'openworksheet' )\">" + (referenceSDC.equals("LV_Worksheet") ? "worksheet" : (referenceSDC.equals("LV_WorksheetSection") ? "section" : "control")) + "</a>")).append(referenceSDC.equals("LV_Worksheet") ? "" : " in worksheet '" + refworksheetname + "'");
            }
            if (!export && !dock) {
                html.append("&nbsp;&nbsp;-&nbsp;&nbsp;<a style=\"font-weight:normal\" href=\"javascript:sapphire.worksheet.setConfigProperty('expandcontent', '" + (expand ? "N" : "Y") + "', true)\">show " + (expand ? "less" : "more") + "</a></td></tr>");
            }
            if (expand) {
                html.append("<tr><td style=\"background-color:#F6F6F6;border:1px solid darkgrey\">");
                if (ref.getValue(0, "reffunction", "link").equals("include")) {
                    return html.append(contents.optString("html")).append("</td></tr></table>").toString();
                }
                try {
                    return html.append(this.getRefHTML(ref, false)).append("</td></tr></table>").toString();
                }
                catch (SapphireException e) {
                    return html.append("Failed to render content. Contact your administrator for more information.").append("</td></tr></table>").toString();
                }
            }
            return html.append("</table>").toString();
        }
        this.worksheetItemOptions.setRequiresConfig(true, "Worksheet Viewer Control requires configuration - click to select worksheet section or control");
        return "";
    }

    private String getRefHTML(DataSet ref, boolean copy) throws SapphireException {
        String refworksheetid = ref.getValue(0, "refworksheetid");
        String refworksheetversionid = ref.getValue(0, "refworksheetversionid", "1");
        String refsdcid = ref.getValue(0, "refsdcid");
        String refworksheetsectionid = refsdcid.equals("LV_WorksheetSection") ? ref.getValue(0, "refkeyid1") : "";
        String refworksheetsectionversionid = refsdcid.equals("LV_WorksheetSection") ? ref.getValue(0, "refkeyid2", "1") : "";
        String refworksheetitemid = refsdcid.equals("LV_WorksheetItem") ? ref.getValue(0, "refkeyid1") : "";
        String refworksheetitemversionid = refsdcid.equals("LV_WorksheetItem") ? ref.getValue(0, "refkeyid2", "1") : "";
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("loadviewhtml", "Y");
        actionProps.setProperty("worksheetid", refworksheetid);
        actionProps.setProperty("worksheetversionid", refworksheetversionid);
        actionProps.setProperty("worksheetsectionid", refworksheetsectionid);
        actionProps.setProperty("worksheetsectionversionid", refworksheetsectionversionid);
        actionProps.setProperty("worksheetitemid", refworksheetitemid);
        actionProps.setProperty("worksheetitemversionid", refworksheetitemversionid);
        actionProps.setProperty("includesubsections", "Y");
        actionProps.setProperty("preview", "Y");
        if (refworksheetsectionid.length() == 0 && refworksheetitemid.length() == 0) {
            actionProps.setProperty("worksheetonly", "Y");
        }
        actionProps.setProperty("loadforcopy", "Y");
        this.getActionProcessor().processActionClass(LoadWorksheet.class.getName(), actionProps);
        SDIData worksheetData = (SDIData)actionProps.get("worksheet");
        StringBuffer html = new StringBuffer();
        html.append("<table width=\"100%\">");
        if (refworksheetsectionid.length() > 0) {
            DataSet worksheetsections = worksheetData.getSDIData("sections").getDataset("primary");
            DataSet worksheetitems = worksheetData.getSDIData("items").getDataset("primary");
            HashMap<String, String> filterMap = new HashMap<String, String>();
            for (int i = 0; i < worksheetsections.size(); ++i) {
                String level = worksheetsections.getValue(i, "sectionlevel");
                html.append("<tr><td><div class=\"viewsection").append(level).append("\" style=\"display:table\"><div style=\"display:table-row\">");
                html.append("<div class=\"viewsectionnum").append(level).append("\" style=\"display:table-cell\">").append(worksheetsections.getValue(i, "sectionnum")).append("</div>");
                html.append("<div class=\"viewsectiontext").append(level).append("\" style=\"display:table-cell\">").append(worksheetsections.getValue(i, "worksheetsectiondesc")).append("</div></div></div></td></tr>");
                filterMap.put("worksheetsectionid", worksheetsections.getValue(i, "worksheetsectionid"));
                filterMap.put("worksheetsectionversionid", worksheetsections.getValue(i, "worksheetsectionversionid"));
                DataSet sectionitems = worksheetitems.getFilteredDataSet(filterMap);
                if (sectionitems == null || sectionitems.size() <= 0) continue;
                for (int j = 0; j < sectionitems.size(); ++j) {
                    html.append("<tr><td><div class=\"viewgeneric").append(level).append("\">");
                    html.append(sectionitems.getValue(j, "html"));
                    html.append("</div></td></tr>");
                }
            }
        } else if (refworksheetitemid.length() > 0) {
            DataSet worksheetitems = worksheetData.getSDIData("items").getDataset("primary");
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("worksheetitemid", refworksheetitemid);
            findMap.put("worksheetitemversionid", refworksheetitemversionid);
            int row = worksheetitems.findRow(findMap);
            if (row >= 0) {
                html.append("<tr><td><div class=\"viewgeneric1\">").append(worksheetitems.getValue(row, "html")).append("</div></td></tr>");
            }
        } else if (refworksheetid.length() > 0) {
            DataSet worksheet = worksheetData.getDataset("primary");
            String worksheetname = worksheet.getValue(0, "worksheetname");
            html.append("<tr><td><div class=\"viewgeneric1\">" + worksheetname + "</div></td></tr>");
        }
        html.append("</table>");
        return html.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getViewHTML();
    }

    @Override
    public void validateReference(int referenceid) throws SapphireException {
        DataSet ref = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitemreference WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId()});
        if (ref.size() == 1 && ref.getValue(0, "refworksheetid").length() > 0 && ref.getValue(0, "refsdcid").length() > 0 && ref.getValue(0, "refkeyid1").length() > 0) {
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_WorksheetItem");
            editProps.setProperty("keyid1", this.getWorksheetItemId());
            editProps.setProperty("keyid2", this.getWorksheetItemVersionId());
            DataSet worksheet = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetname FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{ref.getValue(0, "refworksheetid"), ref.getValue(0, "refworksheetversionid", "1")});
            boolean isCopied = ref.getValue(0, "reffunction", "link").equals("include");
            JSONObject contents = new JSONObject();
            try {
                contents.put("worksheetname", worksheet.getValue(0, "worksheetname"));
                if (isCopied) {
                    contents.put("html", this.getRefHTML(ref, true));
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            editProps.setProperty("contents", contents.toString());
            editProps.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", editProps);
        }
    }
}

