/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MarvinEditor
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setSupportsExport(true);
        worksheetItemOptions.setSupportsFields(true);
        worksheetItemOptions.setSupportsHistory(true);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/extscripts/marvinjs/gui/gui.nocache.js");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/extscripts/marvinjs/gui/lib/promise-1.0.0.min.js");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/extscripts/marvinjs/js/marvinjslauncher.js");
        worksheetItemIncludes.addStyleInclude("WEB-CORE/extscripts/marvinjs/gui/css/editor.min.css");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/controls/marvin/scripts/marvineditor.js");
        worksheetItemIncludes.setJSObjectName("marvineditor");
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTML(false);
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getHTML(true);
    }

    private String getHTML(boolean export) throws SapphireException {
        String viewhtml = "No molecule defined";
        String contents = this.getContents();
        if (contents.length() > 0) {
            try {
                JSONObject jContents = new JSONObject(contents);
                String image = jContents.optString("image");
                viewhtml = "<img src=\"" + image + "\">";
            }
            catch (JSONException e) {
                throw new SapphireException("Unable to render Marvinn molecule image", e);
            }
        }
        return viewhtml;
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        StringBuilder html = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getSapphireConnection().getConnectionId());
        PropertyList policy = configProcessor.getPolicy("ELNPolicy", "Sapphire Custom");
        PropertyList chemaxon = policy.getPropertyListNotNull("chemaxon");
        String licenseType = chemaxon.getProperty("marvinjslicensetype", "None (Evaluation Only)");
        String marvinLicenseFile = chemaxon.getProperty("marvinjslicensefile", "[applicationhome]/chemaxon/licenseMarvinJS.cxl");
        String marvinWSLicenseFile = chemaxon.getProperty("marvinjswebservicelicensefile", "chemaxon/licenseMarvinJS.cxl");
        String webApp = chemaxon.getProperty("wswebapp", "webservices");
        String serverName = chemaxon.getProperty("wsservername", "");
        String id = this.getElementId();
        if (id.length() > 0) {
            String container = "marvineditor_" + id;
            String extraurl = "";
            if (licenseType.equalsIgnoreCase("M")) {
                File f = new File(marvinLicenseFile = FileUtil.substituteConfigurationPaths(marvinLicenseFile));
                if (!f.exists()) {
                    this.logWarn("Unable to file Marvin license file " + marvinLicenseFile);
                    marvinLicenseFile = "";
                }
                extraurl = extraurl + "&type=marvin&licensefile=" + marvinLicenseFile;
            } else {
                extraurl = licenseType.equalsIgnoreCase("W") ? "&type=webservices&servername=" + serverName + "&webapp=" + webApp + "&licensefile=" + marvinWSLicenseFile : "&type=none";
            }
            boolean hideTemplates = this.config.getProperty("hidemarvintemplates", "N").equals("Y");
            int height = 400;
            html.append("<div style=\"position:relative;border:1px solid gray;width:100%;height:" + height + "px\" id=\"" + container + "\">");
            html.append("<iframe style=\"border:none\" " + (hideTemplates ? "data-templateurl=\"emptytemplates.json\"" : "") + " src=\"rc?command=file&file=WEB-CORE/modules/eln/worksheetitem/controls/marvin/editor.jsp" + extraurl + "\" id=\"sketch\" class=\"sketcher-frame\"></iframe>");
            html.append("</div>");
            html.append("<script>");
            JSONObject options = new JSONObject();
            try {
                options.put("toolbar", this.config.getProperty("toolbar", "search"));
                options.put("copymode", this.config.getProperty("copymode", "MDL"));
                options.put("selectionmode", this.config.getProperty("selectionmode", "Rectangle"));
                options.put("imagewidth", this.config.getProperty("defaultimagewidth", "400"));
                options.put("imageheight", this.config.getProperty("defaultimageheight", "400"));
                options.put("imagezoommode", this.config.getProperty("imagezoommode", "fit"));
                options.put("imagebackgroundcolor", this.config.getProperty("imagebackgroundcolor", "#FFFFFF"));
                PropertyList editorsettings = this.config.getPropertyListNotNull("editorsettings");
                options.put("carbonlabelvisible", editorsettings.getProperty("carbonlabelvisible", "N"));
                options.put("atomindicesvisible", editorsettings.getProperty("atomindicesvisible", "N"));
                options.put("atommapsvisible", editorsettings.getProperty("atommapsvisible", "Y"));
                options.put("chiralflagvisible", editorsettings.getProperty("chiralflagvisible", "Y"));
                options.put("valenceerrorvisible", editorsettings.getProperty("valenceerrorvisible", "Y"));
                options.put("lonepairsvisible", editorsettings.getProperty("lonepairsvisible", "N"));
                options.put("lonepaircalculationenabled", editorsettings.getProperty("lonepaircalculationenabled", "Y"));
                options.put("cpkcoloring", editorsettings.getProperty("cpkcoloring", "Y"));
                options.put("implicithydrogen", editorsettings.getProperty("implicithydrogen", "TERMINAL_AND_HETERO"));
                options.put("displaymode", editorsettings.getProperty("displaymode", "WIREFRAME"));
                PropertyListCollection templates = this.config.getCollectionNotNull("templates");
                JSONArray jtemplates = new JSONArray();
                long maxSequence = 0L;
                for (int i = 0; i < templates.size(); ++i) {
                    PropertyList template = templates.getPropertyList(i);
                    if (template.getSequence() > maxSequence) {
                        maxSequence = template.getSequence();
                    }
                    if (!template.getProperty("show", "Y").equals("Y")) continue;
                    JSONObject jTemplate = new JSONObject();
                    jTemplate.put("structure", template.getProperty("structure"));
                    jTemplate.put("name", template.getProperty("name"));
                    jTemplate.put("icon", template.getProperty("icon"));
                    jtemplates.put(jTemplate);
                }
                options.put("templates", jtemplates);
                options.put("maxtemplatesequence", maxSequence);
                html.append(" marvineditor.setOptions( '" + HttpUtil.encodeURIComponent(options.toString()) + "' );");
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            html.append("</script>");
        } else {
            html.append("<font style=\"color:red\">\"No ElementId provided.\"</font>");
        }
        return html.toString();
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        return contents;
    }
}

