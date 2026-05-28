/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.platform.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RichText
extends BaseWorksheetItem {
    public static final int MODE_VIEW = 1;
    public static final int MODE_EDIT = 2;
    public static final int MODE_COMPLETE = 3;
    public static final int MODE_COPY = 4;
    private ArrayList<String> tempids = null;

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        PropertyList config = this.getConfig();
        worksheetItemOptions.setConsumesFields(true);
        worksheetItemOptions.setSupportsExport(true);
        worksheetItemOptions.setSupportsHistory(true);
        worksheetItemOptions.setViewerHTMLasTOC(true);
        worksheetItemOptions.setEditorMaxSize(config.getProperty("openmaximized", "N").equals("Y"));
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        try {
            boolean devMode = Configuration.isDevmode(this.getSapphireConnection().getDatabaseId());
            worksheetItemIncludes.getScriptIncludes().addAll(HTMLEditorControl.getScriptIncludes(this.getSapphireConnection().getUseFullIncludes() || devMode));
            worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/richtext.js");
            worksheetItemIncludes.getStyleIncludes().addAll(HTMLEditorControl.getStyleIncludes());
        }
        catch (SapphireException e) {
            this.logError(e);
        }
        worksheetItemIncludes.setJSObjectName("richText");
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getContents(1);
    }

    @Override
    public String getCopyHTML() throws SapphireException {
        return this.getContents(4);
    }

    @Override
    public String getCompleteHTML() throws SapphireException {
        return this.getContents(3);
    }

    @Override
    public String getContents() {
        return this.getContents(2);
    }

    private String getContents(int mode) {
        String c = super.getContents();
        StringBuffer contents = new StringBuffer(c);
        try {
            if (c.contains("$F{")) {
                final DataSet fields = this.getWorksheetFields();
                HTMLEditorControl.processDynamicFields(contents, new HTMLEditorControl.DynamicFieldProcessor(){

                    @Override
                    public String process(StringBuffer expression) {
                        String ex = expression.toString();
                        if (ex.startsWith("$F{") && ex.endsWith("}")) {
                            ex = ex.substring(3, ex.length() - 1);
                            int row = -1;
                            String[] parts = StringUtil.split(ex, ";");
                            HashMap<String, String> find = new HashMap<String, String>();
                            if (parts.length == 3) {
                                find.put("worksheetitemid", parts[0]);
                                find.put("worksheetitemversionid", parts[1]);
                                find.put("fieldname", parts[2]);
                            } else if (parts.length == 1) {
                                find.put("fieldname", parts[0]);
                            }
                            row = fields.findRow(find);
                            if (row > -1) {
                                if (parts.length == 1) {
                                    String newex = "$F{" + fields.getValue(row, "worksheetitemid", "") + ";" + fields.getValue(row, "worksheetitemversionid", "") + ";" + fields.getValue(row, "fieldname", "") + "}";
                                    expression.delete(0, expression.length());
                                    expression.append(newex);
                                }
                                return fields.getValue(row, "dislayvalue", fields.getValue(row, "enteredtext"));
                            }
                            return "";
                        }
                        return "";
                    }
                }, mode == 1 || mode == 3);
            }
            if (mode != 3 && mode != 4) {
                this.tempids = HTMLEditorControl.processImages(contents, false, mode == 1, this.getSapphireConnection().getConnectionId());
            }
        }
        catch (Exception fields) {
            // empty catch block
        }
        String instructionText = this.getDetokenizedConfigProperty("instructiontext");
        if (mode == 1 && contents.length() == 0 && instructionText.length() > 0) {
            return "<div class=\"worksheet_instructiontext\">" + SafeHTML.encodeForHTML(instructionText, true) + "</div>";
        }
        StringBuilder out = new StringBuilder();
        out.append("<div");
        String dfontname = this.config.getProperty("defaultfontname");
        String dfontsize = this.config.getProperty("defaultfontsize");
        if (dfontname.length() > 0 || dfontsize.length() > 0) {
            out.append(" style=\"");
            if (dfontname.length() > 0) {
                out.append("font-family:").append(dfontname).append(";");
            }
            if (dfontsize.length() > 0) {
                out.append("font-size:").append(dfontsize).append(";");
            }
            out.append("\"");
        }
        out.append(">");
        out.append(contents);
        out.append("</div>");
        return out.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        StringBuffer html = new StringBuffer();
        HTMLEditorControl htmlEditorControl = new HTMLEditorControl(new Logger(this.logContext));
        htmlEditorControl.setRtl(this.getSapphireConnection().isRtl());
        htmlEditorControl.setUseFullIncludes(this.getSapphireConnection().getUseFullIncludes());
        htmlEditorControl.setId(this.getElementId());
        htmlEditorControl.setSDI("LV_WorksheetItem", this.getWorksheetItemId(), this.getWorksheetItemVersionId(), "");
        htmlEditorControl.setCanUpload(true);
        boolean devMode = Configuration.isDevmode(this.getSapphireConnection().getDatabaseId());
        htmlEditorControl.setDebug(devMode);
        HTMLEditorControl.EditorType toolbarType = HTMLEditorControl.EditorType.EXPANDABLE;
        if (this.config.getProperty("toolbar").length() > 0) {
            try {
                toolbarType = HTMLEditorControl.EditorType.valueOf(this.config.getProperty("toolbar").toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        htmlEditorControl.setEditorType(toolbarType);
        if (this.config.getProperty("defaultfontname").length() > 0) {
            htmlEditorControl.setDefaultFontName(this.config.getProperty("defaultfontname"));
        }
        if (this.config.getProperty("defaultfontsize").length() > 0) {
            htmlEditorControl.setDefaultFontSize(this.config.getProperty("defaultfontsize"));
        }
        if (this.config.getProperty("phrasetype").length() > 0) {
            htmlEditorControl.setPhraseType(this.config.getProperty("phrasetype"));
        }
        if (this.config.getProperty("phraselookup").length() > 0) {
            htmlEditorControl.setPhraseLookup(this.config.getProperty("phraselookup"));
        }
        html.append(htmlEditorControl.getHtml());
        html.append("<script>").append(htmlEditorControl.getScript()).append("</script>");
        return html.toString();
    }

    @Override
    public String getLiveIndexingText() {
        if (this.hasContents()) {
            Document jdoc = Jsoup.parse((String)this.getContents());
            return jdoc.body().text();
        }
        return "";
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        StringBuffer unescaped = new StringBuffer();
        unescaped.append(HttpUtil.decodeURIComponent(contents));
        HTMLEditorControl.processImages(unescaped, true, false, this.getSapphireConnection().getConnectionId());
        return super.validateContents(unescaped.toString());
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        StringBuffer html = new StringBuffer(this.getContents());
        HTMLEditorControl.processImages(html, true, this.getSapphireConnection().getConnectionId());
        HTMLEditorControl.centerImages(html);
        return html.toString();
    }
}

