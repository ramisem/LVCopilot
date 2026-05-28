/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 *  org.jsoup.nodes.Element
 *  org.jsoup.nodes.Node
 *  org.jsoup.select.Elements
 */
package com.labvantage.sapphire.pageelements.controls;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.ajax.operations.ImageHandler;
import com.labvantage.sapphire.pageelements.controls.RichTextEditor;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.http.HttpSender;
import com.labvantage.sapphire.util.images.ImageRef;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class HTMLEditorControl {
    private static final String JS_OBJECT = "htmlEditor";
    private static final String JS_ENABLEMETHOD = "init";
    private static final String JS_DATAOBJECT = "data";
    private static final String CSS_CONTENT = "WEB-CORE/elements/richtext/stylesheets/richtexteditor.css";
    public static final int SOURCEMODE_HTML = 0;
    public static final int SOURCEMODE_XHTML = 1;
    private EditorType editorType = EditorType.BASIC;
    private Editor editor = null;
    private String htmlcontent = "";
    private String id = "";
    private int sourcemode = 0;
    private boolean devMode = false;
    private TranslationProcessor tp = null;
    private boolean sourceContainsPages = false;
    private PropertyList userConfig = null;
    private Logger logger = null;
    private PropertyList settings = null;
    private String cssContent = "WEB-CORE/elements/richtext/stylesheets/richtexteditor.css";
    private String sdcid = "";
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private boolean upload = false;
    private boolean licenced = false;
    private boolean debug = false;
    private String phraseType = "";
    private String phraseLookup = "";
    private boolean useFullIncludes = false;
    private boolean rtlFlag = false;

    public HTMLEditorControl() {
        this.settings = new PropertyList();
    }

    public HTMLEditorControl(Logger logger) {
        this.logger = logger;
        this.settings = new PropertyList();
    }

    public HTMLEditorControl(String id, EditorType editorType, Logger logger) {
        this.logger = logger;
        this.settings = new PropertyList();
        this.setEditorType(editorType);
        this.setId(id);
    }

    public void setPhraseType(String phraseType) {
        this.phraseType = phraseType;
    }

    public void setPhraseLookup(String phraseLookup) {
        this.phraseLookup = phraseLookup;
    }

    public String getPhraseType() {
        return this.phraseType;
    }

    public String getPhraseLookup() {
        return this.phraseLookup;
    }

    public void setRtl(boolean rtl) {
        this.rtlFlag = rtl;
    }

    public boolean isRtl() {
        return this.rtlFlag;
    }

    public void setEvent(String id, Events event, String script) {
        if (!this.settings.containsKey("events")) {
            this.settings.setProperty("events", new PropertyListCollection());
        }
        PropertyList eventList = new PropertyList();
        eventList.setProperty("id", id);
        eventList.setProperty("event", event.toString().toLowerCase());
        eventList.setProperty("script", script);
        this.settings.getCollection("events").add(eventList);
    }

    public void setInlineWrap(boolean inlineWrap) {
        this.settings.setProperty("inlinewrap", inlineWrap ? "Y" : "N");
    }

    public void setButton(String id, String text, String script) {
        if (!this.settings.containsKey("buttons")) {
            this.settings.setProperty("buttons", new PropertyListCollection());
        }
        PropertyList buttonList = new PropertyList();
        buttonList.setProperty("id", id);
        buttonList.setProperty("text", text);
        buttonList.setProperty("script", script);
        this.settings.getCollection("buttons").add(buttonList);
    }

    public void setDefaultFontName(String fontName) {
        this.settings.setProperty("defaultfontname", fontName);
    }

    public void setDefaultFontSize(String fontSize) {
        this.settings.setProperty("defaultfontsize", fontSize);
    }

    public void setMenuItem(String id, String text, String menu, String script) {
        if (!this.settings.containsKey("menu")) {
            this.settings.setProperty("menu", new PropertyListCollection());
        }
        PropertyList menuList = new PropertyList();
        menuList.setProperty("id", id);
        menuList.setProperty("text", text);
        menuList.setProperty("context", menu);
        menuList.setProperty("script", script);
        this.settings.getCollection("menu").add(menuList);
    }

    public void setShowToolbar(boolean show) {
        this.settings.setProperty("showtoolbar", show ? "Y" : "N");
    }

    public void setSourceMode(int sourcemode) {
        this.sourcemode = sourcemode;
    }

    public int getSourceMode() {
        return this.sourcemode;
    }

    public void setViewOnly(String viewonly) {
        this.setViewOnly(viewonly.equalsIgnoreCase("y") || viewonly.equalsIgnoreCase("true") || viewonly.equalsIgnoreCase("yes"));
    }

    public void setViewOnly(boolean viewonly) {
        this.settings.setProperty("viewonly", viewonly ? "Y" : "N");
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEditorType(EditorType editorType) {
        this.editorType = editorType;
        this.settings.setProperty("editortype", editorType.toString().toLowerCase());
    }

    public void setInline(boolean inline) {
        this.settings.setProperty("inlineedit", inline ? "Y" : "N");
    }

    private void setLicensed(boolean licensed) {
        this.licenced = licensed;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public Editor getEditor() {
        if (this.editor == null) {
            try {
                this.editor = new Editor(EditorType.valueOf(this.settings.getProperty("editortype", this.editorType.toString()).toUpperCase()), this.settings.getProperty("width", "100%"), this.settings.getProperty("height", "300"), this.settings.getProperty("showtoolbar", "Y").equalsIgnoreCase("Y"), this.settings.getProperty("inlineedit", "N").equalsIgnoreCase("Y"), this.debug);
                this.editor.setPhraseType(this.phraseType);
                this.editor.setPhraseLookup(this.phraseLookup);
                this.editor.setRtl(this.isRtl());
                this.editor.setUseFullIncludes(this.useFullIncludes);
            }
            catch (Exception e) {
                this.logger.warn("Failed to find editor");
            }
        }
        return this.editor;
    }

    public void setSDI(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2;
        this.keyid3 = keyid3;
    }

    public void setCanUpload(boolean canUpload) {
        this.upload = canUpload;
    }

    public void setContent(String htmlcontent) {
        Element page;
        Element element;
        Document doc = Jsoup.parse((String)htmlcontent);
        Elements elements = doc.select("div[sapphire=pagecontainer]");
        if (elements != null && elements.size() > 0) {
            element = (Element)elements.get(0);
            Document temp = new Document("");
            temp.insertChildren(0, (Collection)element.children());
            doc = temp;
        }
        if ((elements = doc.select("div[sapphire=pageshadow]")) != null && elements.size() > 0) {
            element = (Element)elements.get(0);
            element.remove();
        }
        if ((elements = doc.select("span[sapphire=field]")) != null && elements.size() > 0) {
            for (int i = 0; i < elements.size(); ++i) {
                Element field = (Element)elements.get(i);
                field.html("&nbsp;");
            }
        }
        if ((elements = doc.select("div[sapphire=page]")) != null && elements.size() > 0) {
            if (this.getEditor().isResizablePage()) {
                for (int i = 0; i < elements.size(); ++i) {
                    page = (Element)elements.get(i);
                    page.attr("data-lvresizable", "true");
                }
            }
            this.sourceContainsPages = true;
            this.getEditor().setPagedMode(true);
            this.htmlcontent = doc.body() != null ? doc.body().html() : doc.html();
        } else if (this.getEditor().getPagedMode()) {
            this.sourceContainsPages = true;
            PageMode def = this.getEditor().getDefaultPage();
            page = doc.createElement("div");
            page.attr("id", "page001");
            page.attr("sapphire", "page");
            page.attr("class", "page");
            if (this.getEditor().isResizablePage()) {
                page.attr("data-lvresizable", "true");
            }
            if (def == PageMode.GROWABLE) {
                page.attr("style", "min-width:" + def.width + ";min-height:" + def.height + ";padding:" + def.margin + ";" + (this.getEditor().isResizablePage() ? "" : ""));
            } else {
                page.attr("style", "width:" + def.width + ";height:" + def.height + ";padding:" + def.margin + ";" + (this.getEditor().isResizablePage() ? "" : ""));
            }
            page.attr("contenteditable", "true");
            if (doc.body() != null) {
                doc.body().appendChild((Node)page);
            } else {
                doc.appendChild((Node)page);
            }
            this.htmlcontent = doc.body() != null ? doc.body().html() : doc.html();
        } else {
            this.htmlcontent = doc.body() != null ? doc.body().html() : doc.html();
            this.sourceContainsPages = false;
        }
    }

    public void setWidth(String width) {
        if (width.toLowerCase().endsWith("px")) {
            this.settings.setProperty("width", width.substring(0, width.length() - 2));
        } else {
            this.settings.setProperty("width", width);
        }
    }

    public void setHeight(String height) {
        if (height.toLowerCase().endsWith("px")) {
            this.settings.setProperty("height", height.substring(0, height.length() - 2));
        } else {
            this.settings.setProperty("height", height);
        }
    }

    public String getInitScript() {
        return "htmlEditor.init('" + this.id + "')";
    }

    public String getInitScript(String onload) {
        return "htmlEditor.init('" + this.id + "','" + onload + "')";
    }

    public String getId() {
        return this.id;
    }

    public static ArrayList<String> getScriptIncludes() {
        return HTMLEditorControl.getScriptIncludes(false);
    }

    public static ArrayList<String> getScriptIncludes(boolean useFullIncludes) {
        ArrayList<String> scriptIncludes = new ArrayList<String>();
        String t1 = "WEB-CORE/extscripts/tinymce/tinymce.min.js";
        String t2 = "WEB-CORE/extscripts/tinymce/tinymce.labvantage" + (useFullIncludes ? "" : ".min") + ".js";
        scriptIncludes.add(t1);
        scriptIncludes.add(t2);
        scriptIncludes.add("WEB-CORE/extscripts/react/react-with-addons.js");
        scriptIncludes.add("WEB-CORE/extscripts/react/react-dom.js");
        scriptIncludes.add("WEB-CORE/extscripts/literallycanvas/js/literallycanvas" + (useFullIncludes ? "" : ".min") + ".js");
        scriptIncludes.add("WEB-CORE/extscripts/rangy/" + (useFullIncludes ? "uncompressed/" : "") + "rangy-core.js");
        scriptIncludes.add("WEB-CORE/extscripts/rangy/" + (useFullIncludes ? "uncompressed/" : "") + "rangy-textrange.js");
        return scriptIncludes;
    }

    public static ArrayList<String> getStyleIncludes() {
        ArrayList<String> styleIncludes = new ArrayList<String>();
        styleIncludes.add("WEB-CORE/extscripts/literallycanvas/css/literallycanvas.css");
        styleIncludes.add(CSS_CONTENT);
        return styleIncludes;
    }

    public static String getIncludes(HttpServletRequest request) {
        return HTMLEditorControl.getIncludes(request, false, false, false);
    }

    public static String getIncludes(HttpServletRequest request, boolean rtl, boolean useFullIncludes, boolean devMode) {
        StringBuffer includesHTML = new StringBuffer();
        PropertyListCollection plugins = new PropertyListCollection();
        PropertyList plugin = new PropertyList();
        plugin.setProperty("pluginid", "dropzone");
        plugin.setProperty("css", "Y");
        plugin.setProperty("allowminimized", useFullIncludes || devMode ? "N" : "Y");
        plugins.add(plugin);
        includesHTML.append(JavaScriptAPITag.getJQueryAPI(true, false, plugins, "", !useFullIncludes && !devMode, null, null, request));
        ArrayList<String> scriptIncludes = HTMLEditorControl.getScriptIncludes(useFullIncludes || devMode);
        for (int i = 0; i < scriptIncludes.size(); ++i) {
            includesHTML.append("<script type=\"text/javascript\" src=\"").append(scriptIncludes.get(i)).append("\"></script>");
        }
        ArrayList<String> styleIncludes = HTMLEditorControl.getStyleIncludes();
        for (int i = 0; i < styleIncludes.size(); ++i) {
            includesHTML.append("<link href=\"").append(com.labvantage.sapphire.util.http.HttpUtil.getCSS(styleIncludes.get(i), rtl, devMode ? false : useFullIncludes)).append("\" rel=\"stylesheet\">");
        }
        return includesHTML.toString();
    }

    public String getIncludesHTML(HttpServletRequest request) {
        return HTMLEditorControl.getIncludes(request, this.rtlFlag, this.useFullIncludes, this.devMode);
    }

    public void setDevMode(boolean devMode) {
        this.devMode = devMode;
    }

    public String getScript() {
        PropertyListCollection menu;
        PropertyListCollection buttons;
        StringBuffer content = new StringBuffer();
        boolean inlineedit = this.settings.getProperty("inlineedit", "N").equalsIgnoreCase("Y");
        JSONObject job = new JSONObject();
        try {
            job.put("source", false);
            job.put("width", this.settings.getProperty("width", "100%"));
            job.put("height", this.settings.getProperty("height", "300"));
            job.put("viewonly", this.settings.getProperty("viewonly", "N").equalsIgnoreCase("Y"));
            job.put("pagemode", this.getEditor().getPagedMode());
            job.put("defaultPage", this.getEditor().getDefaultPage().toString());
            job.put("resizePage", this.getEditor().isResizablePage());
            job.put("pagedSource", this.sourceContainsPages);
            job.put("toolbarvisible", this.settings.getProperty("showtoolbar", "Y").equalsIgnoreCase("Y"));
            job.put("viewbarvisible", this.settings.getProperty("showtoolbar", "Y").equalsIgnoreCase("Y"));
            job.put("sourcemode", this.sourcemode);
            job.put("inlineedit", inlineedit);
            job.put("usefullincludes", this.useFullIncludes);
            job.put("content", this.htmlcontent);
            if (this.settings != null && this.settings.getProperty("autoupdatefield", "N").equalsIgnoreCase("Y")) {
                job.put("autoupdatefield", true);
            }
            if (this.settings != null && this.settings.getProperty("defaultfontname").length() > 0) {
                job.put("defaultfontname", this.settings.getProperty("defaultfontname"));
            }
            if (this.settings != null && this.settings.getProperty("defaultfontsize").length() > 0) {
                job.put("defaultfontsize", this.settings.getProperty("defaultfontsize"));
            }
            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("']=").append(job.toString()).append(";");
        }
        catch (Exception e) {
            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("']={};");
        }
        content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].setup=").append("function(ed){");
        if (this.getEditor().basedon == EditorType.DRAWING || this.getEditor().basedon == EditorType.EQUATION || this.getEditor().basedon == EditorType.IMAGE) {
            content.append("ed.").append("on('").append("keydown").append("',").append("function(e){e.preventDefault();return false;}").append(");");
        }
        this.getEditor().setUpload(this.sdcid, this.keyid1, this.keyid2, this.keyid3, this.upload);
        PropertyListCollection events = this.settings.getCollection("events");
        if (events != null && events.size() > 0) {
            boolean selectionReg = false;
            for (int e = 0; e < events.size(); ++e) {
                PropertyList event = events.getPropertyList(e);
                try {
                    Events eventDef = Events.valueOf(event.getProperty("event", "").toUpperCase());
                    if (eventDef.getName().equalsIgnoreCase(Events.SELECTED.getName()) || eventDef.getName().equalsIgnoreCase(Events.SELECTIONCLEARED.getName()) || eventDef.getName().equalsIgnoreCase(Events.NODESELECTED.getName())) {
                        if (eventDef.getName().equalsIgnoreCase(Events.SELECTED.getName())) {
                            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onSelect=").append("").append(event.getProperty("script")).append(";");
                        } else if (eventDef.getName().equalsIgnoreCase(Events.SELECTIONCLEARED.getName())) {
                            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onSelectClear=").append("").append(event.getProperty("script")).append(";");
                        } else {
                            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onSelectNode=").append("").append(event.getProperty("script")).append(";");
                        }
                        if (selectionReg) continue;
                        content.append("ed.on('").append(Events.SELECTIONCHANGE.getName()).append("',").append(JS_OBJECT).append(".events.selectionChanged);");
                        selectionReg = true;
                        continue;
                    }
                    if (eventDef.getName().equalsIgnoreCase(Events.SELECTIONCHANGE.getName())) {
                        content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onSelectionChange=").append("").append(event.getProperty("script")).append(";");
                        if (selectionReg) continue;
                        content.append("ed.on('").append(Events.SELECTIONCHANGE.getName()).append("',").append(JS_OBJECT).append(".events.selectionChanged);");
                        selectionReg = true;
                        continue;
                    }
                    if (eventDef.getName().equalsIgnoreCase(Events.COPY.getName()) || eventDef.getName().equalsIgnoreCase(Events.CUT.getName()) || eventDef.getName().equalsIgnoreCase(Events.BEFORECUT.getName()) || eventDef.getName().equalsIgnoreCase(Events.BEFORECOPY.getName()) || eventDef.getName().equalsIgnoreCase(Events.BEFOREPASTE.getName())) {
                        if (eventDef.getName().equalsIgnoreCase(Events.COPY.getName())) {
                            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onCopy=").append("").append(event.getProperty("script")).append(";");
                            continue;
                        }
                        if (eventDef.getName().equalsIgnoreCase(Events.CUT.getName())) {
                            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onCut=").append("").append(event.getProperty("script")).append(";");
                            continue;
                        }
                        if (eventDef.getName().equalsIgnoreCase(Events.BEFORECUT.getName())) {
                            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onBeforeCut=").append("").append(event.getProperty("script")).append(";");
                            continue;
                        }
                        if (eventDef.getName().equalsIgnoreCase(Events.BEFORECOPY.getName())) {
                            content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onBeforeCopy=").append("").append(event.getProperty("script")).append(";");
                            continue;
                        }
                        content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onBeforePaste=").append("").append(event.getProperty("script")).append(";");
                        continue;
                    }
                    if (eventDef.getName().equalsIgnoreCase(Events.CONTENTADDED.getName())) {
                        content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].onContentAdded=").append("").append(event.getProperty("script")).append(";");
                        content.append("ed.on('").append(Events.SETCONENT.getName()).append("',").append(JS_OBJECT).append(".events.setContent);");
                        continue;
                    }
                    if (eventDef.getName().equalsIgnoreCase(Events.FIELDCHANGE.getName())) {
                        if (inlineedit) {
                            String idx = this.id;
                            if (idx.contains("[__row]")) {
                                idx = StringUtil.replaceAll(idx, "[__row]", "_tr");
                            }
                            content.append("var _tc_" + idx + " = 0;");
                            content.append("var _to_" + idx + " = function(){ ");
                            content.append("var o = $('input[name=" + this.id + "]');");
                            content.append("if (_tc_" + idx + " < 20){");
                            content.append("if (o.length > 0){");
                            content.append("o.bind('change',").append(event.getProperty("script")).append(");");
                            content.append("}else{");
                            content.append("_tc_" + idx + "++;");
                            content.append("window.setTimeout(_to_" + idx + ", 150 );");
                            content.append("}");
                            content.append("}");
                            content.append("};");
                            content.append("_to_" + idx + "();");
                            continue;
                        }
                        content.append("ed.on('init', function(){$(this.targetElm).bind('change',").append(event.getProperty("script")).append(")});");
                        continue;
                    }
                    content.append("ed.").append(eventDef.getObject().length() > 0 ? eventDef.getObject() + "." : "").append("").append("on('").append(eventDef.getName()).append("',").append(event.getProperty("script")).append(");");
                    continue;
                }
                catch (Exception ex) {
                    Logger.logWarn("Could not parse event");
                }
            }
        }
        if ((buttons = this.settings.getCollection("buttons")) != null && buttons.size() > 0) {
            this.getEditor().addButton("|");
            for (int b = 0; b < buttons.size(); ++b) {
                PropertyList button = buttons.getPropertyList(b);
                content.append("ed.addButton('").append(button.getProperty("id")).append("',{");
                content.append("text:'").append(button.getProperty("text")).append("',");
                content.append("icon:false,");
                content.append("onclick:").append(button.getProperty("script")).append("");
                content.append("});");
                this.getEditor().addButton(button.getProperty("id"));
            }
        }
        if ((menu = this.settings.getCollection("menu")) != null && menu.size() > 0) {
            for (int m = 0; m < menu.size(); ++m) {
                PropertyList menuitem = menu.getPropertyList(m);
                content.append("ed.addMenuItem('").append(menuitem.getProperty("id")).append("',{");
                content.append("text:'").append(menuitem.getProperty("text")).append("',");
                content.append("context:'").append(menuitem.getProperty("context")).append("',");
                content.append("icon:false,");
                content.append("onclick:").append(menuitem.getProperty("script")).append("");
                content.append("});");
                this.getEditor().addMenu(menuitem.getProperty("context", "Custom"), menuitem.getProperty("id"));
            }
        }
        if (this.getEditor().height.equals("*") || this.getEditor().height.equalsIgnoreCase("auto")) {
            content.append("ed.on('SkinLoaded',function(){");
            content.append(JS_OBJECT).append(".").append("autoHeight(this);");
            content.append("});");
        }
        content.append("};");
        content.append(JS_OBJECT).append(".").append("data['").append(this.id).append("'].editor=").append(this.getEditor().getDefintion().toString()).append(";");
        return content.toString();
    }

    public void setAutoUpdateField(boolean autoUpdateField) {
        if (this.settings != null) {
            this.settings.setProperty("autoupdatefield", "Y");
        }
    }

    public String getHtml() {
        StringBuffer content = new StringBuffer();
        String debugErrorMsg = "";
        if (this.id.length() > 0) {
            if (this.htmlcontent == null || this.htmlcontent.length() == 0) {
                this.setContent("");
            }
            if (this.settings.getProperty("inlineedit", "N").equalsIgnoreCase("Y")) {
                content.append("<div class=\"htmleditor_container\" id=\"").append(this.id).append("\" name=\"").append(this.id).append("_inline\"");
                StringBuffer style = new StringBuffer();
                if (this.settings != null && this.settings.getProperty("height").length() > 0) {
                    int height = 0;
                    try {
                        height = Integer.parseInt(this.settings.getProperty("height"));
                        style.append("max-height:").append(height).append("px;");
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (this.settings != null && this.settings.getProperty("width").length() > 0) {
                    int width = 0;
                    try {
                        width = Integer.parseInt(this.settings.getProperty("width"));
                        style.append("width:").append(width).append("px;");
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (this.settings.getProperty("inlinewrap", "N").equalsIgnoreCase("Y")) {
                    style.append("white-space: normal;");
                } else {
                    style.append("white-space: break-word;");
                }
                if (style.length() > 0) {
                    content.append(" style=\"").append(style).append("overflow:auto;").append("\"");
                }
                content.append(">");
                content.append("</div>");
            } else {
                content.append("<textarea class=\"htmleditor_textarea\" id=\"").append(this.id).append("\" name=\"").append(this.id).append("\" >");
                content.append(this.tp != null ? this.tp.translate("Loading...") : "");
                content.append("</textarea>");
            }
        } else {
            debugErrorMsg = "No Id provided.";
        }
        if (debugErrorMsg.length() > 0) {
            return "<font style=\"color:red\">" + debugErrorMsg + "</font>";
        }
        return content.toString();
    }

    public static String getBody(String html) {
        Document doc = Jsoup.parse((String)html.toString());
        doc.outputSettings().prettyPrint(false);
        Elements e = doc.getElementsByTag("body");
        if (e != null) {
            return e.html();
        }
        return doc.html();
    }

    public static ArrayList processImages(StringBuffer html, boolean merge, String connectionId) {
        return HTMLEditorControl.processImages(html, merge, false, connectionId);
    }

    public static ArrayList processImages(StringBuffer html, final boolean merge, boolean flush, final String connectionId) {
        final ArrayList tempids = new ArrayList();
        final ArrayList toremove = new ArrayList();
        HTMLEditorControl.processHTML(html, "img", flush, new ElementProcessor(){
            final QueryProcessor qp;
            final ActionProcessor ap;
            {
                this.qp = new QueryProcessor(connectionId);
                this.ap = new ActionProcessor(connectionId);
            }

            String mergeImage(String src) {
                String newSrc = "";
                if (!src.startsWith("data:image/") && !src.startsWith("blob:")) {
                    try {
                        URL url = new URL("http://s/" + src);
                        HashMap<String, String> querymap = HttpUtil.getQueryStringMap(url);
                        if (querymap.containsKey("operationclass") && querymap.get("operationclass").equals(ImageHandler.class.getName())) {
                            FileManager.TempFile imagePL;
                            String tempsdcid;
                            String tempid = querymap.containsKey("id") ? querymap.get("id") : "";
                            String string = tempsdcid = querymap.containsKey("sdcid") ? querymap.get("sdcid") : "";
                            if (tempsdcid.length() == 0 || tempsdcid.equalsIgnoreCase("(null)")) {
                                tempsdcid = "SDC";
                            }
                            if ((imagePL = FileManager.TempFile.getTempFile(tempid, false, this.qp, connectionId)) != null) {
                                newSrc = imagePL.getData().getDataURL();
                                tempids.add(tempid);
                                toremove.add(tempid);
                            } else {
                                Trace.logError("Failed to process image from temp area.");
                            }
                        }
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to process image.", e);
                    }
                }
                return newSrc;
            }

            String separateImage(String src) {
                String newSrc = "";
                if (src.startsWith("data:image/")) {
                    FileManager.FileData fileData = new FileManager.FileData(src);
                    String filename = "blob" + FileType.getFileTypeByMime(fileData.getMimetype(), connectionId).getExtension();
                    FileManager.TempFile tempFile = new FileManager.TempFile(fileData, filename, filename, FileManager.TempSource.RICHTEXT, Attachment.AttachmentType.FILE.getFlag(), false, connectionId);
                    int maxsize = 10000000;
                    try {
                        String tempid = tempFile.setTempFile(maxsize, "", connectionId, this.ap);
                        tempids.add(tempid);
                        newSrc = ImageHandler.getOperationURL(tempid);
                    }
                    catch (Exception e) {
                        Trace.logError("Failed to separate image.", e);
                    }
                } else if (src.startsWith("blob:")) {
                    // empty if block
                }
                return newSrc;
            }

            @Override
            public void process(Element element) {
                String n;
                String original = element.attr("data-lvs-original");
                String src = element.attr("src");
                if (original == null || original.length() == 0) {
                    String string = n = merge ? this.mergeImage(src) : this.separateImage(src);
                    if (n.length() > 0) {
                        element.attr("src", n);
                        element.attr("data-lvs-original", n);
                    }
                } else if (src.endsWith(original)) {
                    String string = n = merge ? this.mergeImage(src) : this.separateImage(src);
                    if (n.length() > 0) {
                        element.attr("src", n);
                        element.attr("data-lvs-original", n);
                    }
                } else {
                    String attOrg;
                    String string = attOrg = merge ? this.mergeImage(original) : this.separateImage(original);
                    if (attOrg.length() > 0) {
                        element.attr("data-lvs-original", attOrg);
                    }
                    String attSrc = merge ? this.mergeImage(src) : this.separateImage(src);
                    element.attr("src", attSrc);
                }
                try {
                    JSONObject data;
                    JSONObject shape;
                    JSONObject markup;
                    String markups = element.attr("data-markup");
                    if (markups != null && markups.length() > 0 && (markup = new JSONObject(markups)) != null && markup.has("backgroundShapes") && markup.getJSONArray("backgroundShapes").length() > 0 && (shape = markup.getJSONArray("backgroundShapes").getJSONObject(0)).has(HTMLEditorControl.JS_DATAOBJECT) && (data = shape.getJSONObject(HTMLEditorControl.JS_DATAOBJECT)).has("imageSrc") && data.getString("imageSrc").length() > 0) {
                        String newSrc;
                        String markupSrc = data.getString("imageSrc");
                        String string = newSrc = merge ? this.mergeImage(markupSrc) : this.separateImage(markupSrc);
                        if (newSrc.length() > 0) {
                            data.remove("imageSrc");
                            data.put("imageSrc", newSrc);
                            element.attr("data-markup", markup.toString());
                        }
                    }
                }
                catch (Exception e) {
                    Logger.logError("Failed to upload markup image.", e);
                }
            }

            @Override
            public void complete() {
                try {
                    if (toremove != null && toremove.size() > 0) {
                        Timer timer = new Timer();
                        Calendar c = Calendar.getInstance();
                        c.add(12, 5);
                        int i = 0;
                        while (i < toremove.size()) {
                            final int temp = i++;
                            timer.schedule(new TimerTask(){

                                @Override
                                public void run() {
                                    try {
                                        FileManager.TempFile.removeTempFile((String)toremove.get(temp), ap, qp, connectionId);
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                }
                            }, c.getTime());
                        }
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        return tempids;
    }

    public static void serializeImages(StringBuffer html, final SapphireConnection sapphireConnection, final boolean includeMarkup) {
        HTMLEditorControl.processHTML(html, "img", new ElementProcessor(){

            @Override
            public void complete() {
            }

            @Override
            public void process(Element element) {
                block7: {
                    String src;
                    String string = src = includeMarkup ? element.attr("src") : element.attr("data-lvs-original");
                    if (src.length() > 0 && !src.startsWith("data:image/jpeg;base64") && !src.startsWith("blob:")) {
                        try {
                            URL url = new URL("http://s/" + src);
                            HashMap<String, String> querymap = HttpUtil.getQueryStringMap(url);
                            if (!querymap.containsKey("command") || !querymap.get("command").equalsIgnoreCase("attachment")) break block7;
                            String sdcid = querymap.containsKey("sdcid") ? querymap.get("sdcid") : "";
                            String keyid1 = querymap.containsKey("keyid1") ? querymap.get("keyid1") : "";
                            String keyid2 = querymap.containsKey("keyid2") ? querymap.get("keyid2") : "";
                            String keyid3 = querymap.containsKey("keyid3") ? querymap.get("keyid3") : "";
                            String attnum = querymap.containsKey("attachmentnum") ? querymap.get("attachmentnum") : "";
                            int attNumber = 0;
                            try {
                                attNumber = Integer.parseInt(attnum);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            AttachmentProcessor ap = new AttachmentProcessor(sapphireConnection.getConnectionId());
                            Attachment attachment = ap.getSDIAttachment(sdcid, keyid1, keyid2.length() > 0 ? keyid2 : "(null)", keyid3.length() > 0 ? keyid3 : "(null)", attNumber);
                            if (attachment != null) {
                                String filename = attachment.getFilename().toLowerCase();
                                FileType type = FileType.getFileTypeByFileName(filename, sapphireConnection.getConnectionId());
                                FileManager.FileData fileData = new FileManager.FileData(attachment.getData(), type.getMime());
                                String finalUrl = fileData.getDataURL();
                                element.attr("src", finalUrl);
                            } else {
                                Trace.logError("Failed to obtain attachment.");
                            }
                        }
                        catch (Exception e) {
                            Trace.logError("Failed to process image.", e);
                        }
                    }
                }
            }
        });
    }

    public static void centerImages(StringBuffer html) {
        HTMLEditorControl.processHTML(html, "img", new ElementProcessor(){

            @Override
            public void complete() {
            }

            @Override
            public void process(Element element) {
                String style = element.attr("style");
                if (style != null && style.length() > 0 && style.contains("margin-left: auto") && style.contains("margin-right: auto")) {
                    style = style + ";text-align:center";
                    element.attr("style", style);
                }
            }
        });
    }

    public static void injectImageURI(StringBuffer html, final String uri, final String connectionid) {
        final HashMap tempImageCache = new HashMap();
        HTMLEditorControl.processHTML(html, "img", new ElementProcessor(){

            @Override
            public void complete() {
            }

            @Override
            public void process(Element element) {
                String src = element.attr("src");
                if (!src.startsWith("http")) {
                    if (src.startsWith("rc?command=image") || src.startsWith("rc?command=attachment")) {
                        ImageRef ref = ImageRef.getURLImage(new ConnectionProcessor(connectionid).getSapphireConnection(), src);
                        if (ref.getFileType().equals((Object)ImageRef.FileType.ATTACHMENT)) {
                            Attachment attachment = ref.getAttachment();
                            AttachmentProcessor ap = new AttachmentProcessor(connectionid);
                            ap.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
                            FileManager.FileData fileData = new FileManager.FileData(attachment.getInputStream(), FileType.getFileTypeByName("PNG", connectionid).getMime(), false);
                            src = fileData.getDataURL();
                        } else {
                            String newsrc = (String)tempImageCache.get(src);
                            if (newsrc == null) {
                                String url = uri + "/" + src;
                                HttpSender o = new HttpSender();
                                o.doConnect(url, "GET", false);
                                StringBuffer connectionCookie = new StringBuffer();
                                connectionCookie.append("connectionid=").append(HttpUtil.encodeURIComponent(connectionid));
                                o.addRequestHeader("Cookie", connectionCookie.toString());
                                InputStream res = o.getResponseStream();
                                if (res != null) {
                                    FileManager.FileData fileData = new FileManager.FileData(res, o.getResponseType(), false);
                                    newsrc = fileData.getDataURL();
                                }
                                tempImageCache.put(src, newsrc == null ? "" : newsrc);
                            }
                            src = newsrc;
                        }
                    } else if (!src.startsWith("data:")) {
                        src = uri + "/" + src;
                    }
                    element.attr("src", src);
                }
            }
        });
    }

    public static void removeAnchors(StringBuffer html) {
        HTMLEditorControl.processHTML(html, "a", new ElementProcessor(){

            @Override
            public void complete() {
            }

            @Override
            public void process(Element element) {
                element.unwrap();
            }
        });
    }

    public static void processDynamicFields(StringBuffer html, final DynamicFieldProcessor fieldProcessor, final boolean viewField) {
        HTMLEditorControl.processHTML(html, "span.sf_dynamicfield", new ElementProcessor(){

            @Override
            public void complete() {
            }

            @Override
            public void process(Element element) {
                StringBuffer expression = new StringBuffer((String)element.dataset().get("expression"));
                if (expression != null && expression.length() > 0) {
                    String value = fieldProcessor.process(expression);
                    element.dataset().put("expression", expression.toString());
                    element.html(value);
                    if (viewField) {
                        element.classNames(new HashSet());
                        element.attr("title", "");
                        element.attr("sapphire", "");
                    }
                }
            }
        });
    }

    public static void processHTML(StringBuffer html, String selector, ElementProcessor processor) {
        HTMLEditorControl.processHTML(html, selector, false, processor);
    }

    public static void processHTML(StringBuilder html, String selector, ElementProcessor processor) {
        HTMLEditorControl.processHTML(html, selector, false, processor);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void processHTML(StringBuffer html, String selector, boolean flush, ElementProcessor processor) {
        Document doc = Jsoup.parse((String)html.toString());
        doc.outputSettings().prettyPrint(false);
        Elements elements = doc.select(selector);
        if (flush && elements.size() == 0 && doc.body() != null && doc.body().text().trim().length() == 0) {
            html.delete(0, html.length());
        } else {
            try {
                if (elements != null && elements.size() > 0) {
                    for (int e = 0; e < elements.size(); ++e) {
                        Element element = (Element)elements.get(e);
                        processor.process(element);
                    }
                }
                html.delete(0, html.length());
                html.append(doc.html());
            }
            finally {
                processor.complete();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void processHTML(StringBuilder html, String selector, boolean flush, ElementProcessor processor) {
        Document doc = Jsoup.parse((String)html.toString());
        doc.outputSettings().prettyPrint(false);
        Elements elements = doc.select(selector);
        if (flush && elements.size() == 0 && doc.body() != null && doc.body().text().trim().length() == 0) {
            html.delete(0, html.length());
        } else {
            try {
                if (elements != null && elements.size() > 0) {
                    for (int e = 0; e < elements.size(); ++e) {
                        Element element = (Element)elements.get(e);
                        processor.process(element);
                    }
                }
                html.delete(0, html.length());
                html.append(doc.html());
            }
            finally {
                processor.complete();
            }
        }
    }

    public static String escapeHTML(String html, boolean full) {
        return RichTextEditor.escapeHTML(html, full);
    }

    public static String escapeHTML(char character, boolean full) {
        return RichTextEditor.escapeHTML(character, full);
    }

    public static String unescapeHTML(String input) {
        return input;
    }

    public void setUseFullIncludes(boolean useFullIncludes) {
        this.useFullIncludes = useFullIncludes;
    }

    public static enum EditorType {
        TEXTONLY(null, false, new String[]{"undo redo"}, new String[]{"code"}, false),
        BASIC(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy paste pastetext | selectall'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops deletetable | cell row column'}}"}, false, null, new String[]{"code table"}, false),
        PRINTABLE(null, false, new String[]{"undo | bold italic underline | alignleft aligncenter alignright alignjustify | fontsizeselect forecolor | bullist numlist outdent indent | table lv_phrase"}, new String[]{"code lists paste table advlist lv_phrase"}, false),
        LESTEXT(null, false, new String[]{"undo | bold italic underline | alignleft aligncenter alignright alignjustify | fontsizeselect forecolor | bullist numlist outdent indent | table lv_phrase image leaui_formula"}, new String[]{"code lists paste table advlist imagetools image leaui_formula lv_phrase"}, false),
        EQUATION(null, false, new String[]{"leaui_formula"}, new String[]{"leaui_formula code"}, false),
        DRAWING(null, false, new String[]{"lv_draw"}, new String[]{"lv_insert code"}, false),
        IMAGE(null, false, new String[]{"image lv_draw"}, new String[]{"lv_insert imagetools image code"}, false),
        FULL(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy powerpaste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link media | template hr | lv_phrase'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops lv_tablelayout | deletetable | cell row column'}}"}, false, new String[]{"undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | link image", "fontselect fontsizeselect forecolor | bullist numlist outdent indent"}, new String[]{"advlist autolink lists link image charmap anchor", "searchreplace visualblocks code fullscreen", "insertdatetime media table powerpaste code imagetools lv_insert lv_phrase"}, false),
        ADVANCED(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy powerpaste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link media | template hr | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops deletetable | cell row column'}}"}, false, new String[]{"undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | link image media charmap", "fontselect fontsizeselect forecolor backcolor | bullist numlist outdent indent"}, new String[]{"advlist autolink lists link image charmap print preview hr anchor pagebreak", "searchreplace wordcount visualblocks visualchars code fullscreen", "insertdatetime media nonbreaking save table directionality", "emoticons template powerpaste textpattern imagetools lv_phrase"}, false),
        PAGED(new String[]{"{lv_pages:{title : 'Pages'  , items : 'lv_addpage lv_editpage lv_removepage'}}", "{edit:{title : 'Edit'  , items : 'undo redo | cut copy paste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link media | template hr | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops deletetable | cell row column'}}"}, false, new String[]{"lv_addpage | undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | link image media charmap", "fontselect fontsizeselect forecolor backcolor | bullist numlist outdent indent"}, new String[]{"advlist autolink lists link image charmap print preview hr anchor pagebreak", "searchreplace wordcount visualblocks visualchars code fullscreen", "insertdatetime media nonbreaking save table directionality", "emoticons template paste textpattern imagetools", "lv_pages lv_phrase"}, true),
        FORM(new String[]{"{lv_pages:{title : 'Pages'  , items : 'lv_addpage lv_editpage lv_removepage'}}", "{edit:{title : 'Edit'  , items : 'undo redo | cut copy paste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link anchor | image hr charmap lv_insertdiv lv_insertfieldset lv_button | lv_inserthtml lv_insertdocument | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops lv_tablelayout | deletetable | cell row column'}}"}, false, new String[]{"lv_addpage | restoredraft | undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | link image lv_draw lv_phrase hr charmap lv_button", "fontselect fontsizeselect forecolor backcolor | bullist numlist outdent indent | lv_fields lv_controlled lv_label | lv_formlet"}, new String[]{"advlist autolink lists link image charmap print preview hr anchor pagebreak", "visualblocks visualchars code", "insertdatetime media nonbreaking save table directionality", "emoticons template paste textpattern imagetools", "lv_pages lv_forms lv_insert autosave lv_phrase"}, true),
        FORMLET(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy paste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link anchor | image hr charmap lv_insertdiv lv_insertfieldset lv_button | lv_inserthtml lv_insertdocument | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops lv_tablelayout | deletetable | cell row column'}}"}, false, new String[]{"restoredraft | undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | link image lv_draw lv_phrase hr charmap lv_button", "fontselect fontsizeselect forecolor backcolor | bullist numlist outdent indent | lv_fields lv_controlled lv_label"}, new String[]{"advlist autolink lists link image charmap print preview hr anchor", "visualblocks visualchars code", "insertdatetime media nonbreaking save table directionality", "emoticons template paste textpattern imagetools", "lv_pages lv_forms lv_insert autosave lv_phrase"}, true),
        SIMPLEFORM(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy paste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link anchor | image hr charmap lv_insertdiv lv_insertfieldset lv_button | lv_inserthtml lv_insertdocument | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops lv_tablelayout | deletetable | cell row column'}}"}, false, new String[]{"restoredraft | undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | link image lv_draw lv_phrase hr charmap lv_button", "fontselect fontsizeselect forecolor backcolor | bullist numlist outdent indent | lv_fields lv_controlled lv_label"}, new String[]{"advlist autolink lists link image charmap print preview hr anchor", "visualblocks visualchars code", "insertdatetime media nonbreaking save table directionality", "emoticons template paste textpattern imagetools", "lv_pages lv_forms lv_insert autosave lv_phrase"}, true),
        ELNFORM(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy paste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link anchor | image hr charmap lv_insertdiv lv_insertfieldset | lv_inserthtml | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops lv_tablelayout |deletetable | cell row column'}}"}, false, new String[]{"restoredraft | undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | link image lv_phrase hr charmap", "fontselect fontsizeselect forecolor backcolor | bullist numlist outdent indent | lv_elnfields lv_label"}, new String[]{"advlist autolink lists link image charmap print preview hr anchor", "visualblocks visualchars code", "insertdatetime media nonbreaking save table directionality", "emoticons template paste textpattern imagetools", "lv_pages lv_forms lv_insert autosave lv_phrase"}, true),
        ELN(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy powerpaste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link anchor | image media hr charmap lv_insertdiv lv_insertfieldset | lv_inserthtml lv_insertdocument | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops lv_tablelayout | deletetable | cell row column'}}"}, false, new String[]{"restoredraft | undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | fontselect fontsizeselect forecolor backcolor | image leaui_formula", "bullist numlist outdent indent | link lv_draw lv_phrase lv_dynamicfield hr charmap insertdatetime lv_insertdocument media | table"}, new String[]{"advlist autolink lists link charmap print preview hr anchor pagebreak", "visualblocks visualchars code", "insertdatetime media nonbreaking save table directionality", "emoticons template powerpaste textpattern image imagetools", "lv_pages lv_forms lv_insert leaui_formula lv_phrase"}, false),
        EXPANDABLE(new String[]{"{edit:{title : 'Edit'  , items : 'undo redo | cut copy paste pastetext | selectall'}}", "{insert:{title : 'Insert', items : 'link anchor | image media hr charmap lv_insertdiv lv_insertfieldset | lv_inserthtml lv_insertdocument | lv_phrase'}}", "{view :{title : 'View'  , items : 'visualaid'}}", "{format:{title : 'Format', items : 'bold italic underline strikethrough superscript subscript | formats | removeformat'}}", "{table:{title : 'Table' , items : 'inserttable tableprops lv_tablelayout | deletetable | cell row column'}}"}, false, new String[]{"restoredraft | undo redo | bold italic underline | alignleft aligncenter alignright alignjustify | fontselect fontsizeselect forecolor backcolor | image leaui_formula | lv_more", "bullist numlist outdent indent | link lv_draw lv_phrase lv_dynamicfield hr charmap insertdatetime lv_insertdocument media | table"}, new String[]{"advlist autolink lists link charmap print preview hr anchor pagebreak", "visualblocks visualchars code", "insertdatetime media nonbreaking save table directionality", "emoticons template powerpaste textpattern image imagetools", "lv_pages lv_forms lv_insert lv_more autosave leaui_formula lv_phrase"}, false);

        private String[] toolbar;
        private String[] plugins;
        private String[] menubar;
        private boolean statusbar;
        private boolean pagemode;

        private EditorType(String[] menubar, boolean statusbar, String[] toolbar, String[] plugins, boolean pagemode) {
            this.toolbar = toolbar;
            this.plugins = plugins;
            this.menubar = menubar;
            this.pagemode = pagemode;
            this.statusbar = statusbar;
        }
    }

    public class Editor {
        private EditorType basedon;
        private String[] toolbar;
        private String[] plugins;
        private ArrayList<String> menubar;
        private JSONObject menu;
        private boolean statusbar;
        private boolean inlineEdit;
        private boolean showToolbar;
        private boolean pagemode;
        private PageMode defaultPage;
        private boolean resizablePage;
        private boolean canUpload = false;
        private String sdcid;
        private String keyid1;
        private String keyid2;
        private String keyid3;
        private boolean debug = true;
        private String phraseType = "";
        private String phraseLookup = "";
        private boolean rtlFlag = false;
        private boolean useFullIncludes = false;
        private String width = "";
        private String height = "";

        Editor(EditorType basedon, String width, String height, boolean showToolbar, boolean inlineEdit, boolean debug) {
            this.basedon = basedon;
            this.toolbar = basedon.toolbar == null ? null : (String[])basedon.toolbar.clone();
            this.plugins = basedon.plugins == null ? null : (String[])basedon.plugins.clone();
            this.statusbar = basedon.statusbar;
            this.pagemode = basedon.pagemode;
            this.resizablePage = false;
            this.debug = debug;
            this.defaultPage = PageMode.LETTERPORTRAIT;
            this.showToolbar = showToolbar;
            this.width = width;
            if (width.equalsIgnoreCase("100%")) {
                this.width = "";
            }
            this.height = height;
            this.inlineEdit = inlineEdit;
            if (basedon.menubar == null) {
                this.menu = null;
                this.menubar = null;
            } else {
                this.menu = new JSONObject();
                this.menubar = new ArrayList();
                for (int i = 0; i < basedon.menubar.length; ++i) {
                    try {
                        JSONObject menuitem = new JSONObject(basedon.menubar[i]);
                        JSONArray keys = menuitem.names();
                        if (keys.length() <= 0) continue;
                        String context = keys.getString(0);
                        this.menubar.add(context);
                        this.menu.put(context, menuitem.getJSONObject(context));
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
                if (debug) {
                    try {
                        this.menubar.add("tools");
                        this.menu.put("tools", new JSONObject("{title : 'Tools' , items : 'code'}"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }

        public boolean isResizablePage() {
            return this.resizablePage;
        }

        public void setPhraseType(String phraseType) {
            this.phraseType = phraseType;
        }

        public void setPhraseLookup(String phraseLookup) {
            this.phraseLookup = phraseLookup;
        }

        public void setResizablePage(boolean resizable) {
            this.resizablePage = resizable;
        }

        public PageMode getDefaultPage() {
            return this.defaultPage;
        }

        public void setDefaultPage(PageMode pagemode) {
            this.defaultPage = pagemode;
        }

        public String[] getToolbar() {
            return this.toolbar;
        }

        public String[] getPlugins() {
            return this.plugins;
        }

        public void setUpload(String sdcid, String keyid1, String keyid2, String keyid3, boolean canUpload) {
            this.sdcid = sdcid;
            this.keyid1 = keyid1;
            this.keyid2 = keyid2;
            this.keyid3 = keyid3;
            this.canUpload = canUpload;
        }

        private boolean isLicensed() {
            return true;
        }

        public void setPagedMode(boolean pagemode) {
            this.pagemode = pagemode;
        }

        public boolean getPagedMode() {
            return this.pagemode;
        }

        public ArrayList<String> getMenubar() {
            return this.menubar;
        }

        public JSONObject getMenu() {
            return this.menu;
        }

        public boolean getStatusbar() {
            return this.statusbar;
        }

        public void addButton(String item) {
            if (this.toolbar != null) {
                String tool = this.toolbar[this.toolbar.length - 1];
                this.toolbar[this.toolbar.length - 1] = item.equalsIgnoreCase("-") || item.equalsIgnoreCase("|") ? tool + " |" : tool + " " + item;
            } else {
                this.toolbar = new String[]{item};
            }
        }

        public void addMenu(String title, String item) {
            String context = title.toLowerCase();
            try {
                if (!this.menubar.contains(context)) {
                    JSONObject child = new JSONObject("{title:'" + title + "',items:'" + item + "'}");
                    this.menu.put(context, child);
                    this.menubar.add(context);
                } else {
                    JSONObject child = this.menu.getJSONObject(context);
                    String items = child.getString("items");
                    items = items + " " + item;
                    child.put("items", items);
                    this.menu.put(context, child);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }

        private JSONObject getDefintion() {
            Editor editor = this;
            JSONObject job = new JSONObject();
            try {
                JSONObject menu;
                job.put("statusbar", editor.getStatusbar());
                if (editor.getPlugins() != null && editor.getPlugins().length > 0) {
                    List<String> ps = Arrays.asList(editor.getPlugins());
                    JSONArray jay = new JSONArray(ps);
                    job.put("plugins", jay);
                }
                if (editor.getToolbar() != null && editor.getToolbar().length > 0 && this.showToolbar) {
                    if (editor.getToolbar().length == 1) {
                        job.put("toolbar", editor.getToolbar()[0]);
                    } else {
                        for (int i = 0; i < editor.getToolbar().length; ++i) {
                            job.put("toolbar" + (i + 1), editor.getToolbar()[i]);
                        }
                    }
                } else if (!this.showToolbar) {
                    job.put("toolbar", false);
                }
                job.put("inline", this.inlineEdit);
                if (this.isRtl()) {
                    job.put("directionality", "rtl");
                    job.put("rtl_ui", true);
                }
                job.put("content_css", com.labvantage.sapphire.util.http.HttpUtil.getCSS(HTMLEditorControl.this.cssContent, this.rtlFlag, this.useFullIncludes));
                if (this.getPagedMode()) {
                    job.put("body_class", "pagemode");
                }
                if (!this.width.equals("*") && !this.width.equalsIgnoreCase("auto")) {
                    try {
                        job.put("width", Integer.parseInt(this.width));
                    }
                    catch (Exception e) {
                        job.put("width", this.width);
                    }
                }
                if (!this.height.equals("*") && !this.height.equalsIgnoreCase("auto")) {
                    try {
                        job.put("height", Integer.parseInt(this.height));
                    }
                    catch (Exception e) {
                        job.put("height", this.height);
                    }
                }
                if ((menu = editor.getMenu()) != null && editor.getMenubar() != null) {
                    job.put("menu", menu);
                    StringBuffer menubar = new StringBuffer();
                    for (int i = 0; i < editor.getMenubar().size(); ++i) {
                        String menuitem = editor.getMenubar().get(i);
                        if (menubar.length() > 0) {
                            menubar.append(" ");
                        }
                        menubar.append(menuitem);
                    }
                    job.put("menubar", menubar.toString());
                } else {
                    job.put("menubar", false);
                }
                if (this.basedon == EditorType.FORM) {
                    job.put("autosave_restore_when_empty", false);
                    job.put("autosave_ask_before_unload", false);
                }
                if (this.canUpload) {
                    job.put("images_upload_url", ImageHandler.getOperationURL(HTMLEditorControl.this.id, ""));
                }
                job.put("lv_phrase_type", this.phraseType);
                job.put("lv_phrase_lookup", this.phraseLookup);
            }
            catch (Exception e) {
                HTMLEditorControl.this.logger;
                Logger.logWarn(e.getMessage());
            }
            return job;
        }

        public void setRtl(boolean rtl) {
            this.rtlFlag = rtl;
        }

        public boolean isRtl() {
            return this.rtlFlag;
        }

        public void setUseFullIncludes(boolean useFullIncludes) {
            this.useFullIncludes = useFullIncludes;
        }

        public boolean getFullIncludes() {
            return this.useFullIncludes;
        }
    }

    public static interface DynamicFieldProcessor {
        public String process(StringBuffer var1);
    }

    public static interface ElementProcessor {
        public void process(Element var1);

        public void complete();
    }

    public static enum PageMode {
        LETTERPORTRAIT("8.5in", "11in", "25.4mm"),
        LETTERLANDSCAPE("11in", "8.5in", "25.4mm"),
        LEGALPORTRAIT("8.5in", "14in", "25.4mm"),
        LEGALLANDSCAPE("14in", "8.5in", "25.4mm"),
        A4PORTRAIT("21cm", "29.7cm", "25.4mm"),
        A4LANDSCAPE("29.7cm", "21cm", "25.4mm"),
        SCREEN1280X1024("1280px", "1024px", "10px"),
        SCREEN1024X768("1024px", "768px", "10px"),
        SCREEN800X600("800px", "600px", "10px"),
        GROWABLE("300px", "50px", "0");

        String width = "";
        String height = "";
        String margin = "";

        private PageMode(String width, String height, String margin) {
            this.width = width;
            this.height = height;
            this.margin = margin;
        }
    }

    public static enum Events {
        ACTIVATE("onActivate"),
        BEFOREEXECCOMMAND("BeforeExecCommand"),
        BEFORESETCONTENT("BeforeSetContent"),
        DEACTIVATE("onDeactivate"),
        EXECCOMMAND("ExecCommand"),
        GETCONTENT("GetContent"),
        INIT("onInit"),
        LOADCONTENT("LoadContent"),
        NODECHANGE("NodeChange"),
        PREINIT("PreInit"),
        SETCONENT("SetContent"),
        OBJECTRESIZESTART("ObjectResizeStart"),
        OBJECTRESIZED("ObjectResized"),
        SETATTRIBUTE("SetAttrib"),
        SWITCHMODE("SwitchMode"),
        SCROLLWINDOW("ScrollWindow"),
        RESIZEWINDOW("ResizeWindow"),
        SETUPEDITOR("SetupEditor"),
        BEFOREUNLOAD("BeforeUnload"),
        FOCUS("focus"),
        BLUR("blur"),
        HIDE("hide"),
        SHOW("show"),
        CHANGE("change"),
        PASTE("paste"),
        CUT("lvscut"),
        COPY("lvscopy"),
        BEFOREPASTE("lvsbeforepaste"),
        BEFORECUT("lvsbeforecut"),
        BEFORECOPY("lvsbeforecopy"),
        KEYUP("keyup"),
        DIRTY("dirty"),
        CLICK("click"),
        BEFOREOBJECTSELECTED("BeforeObjectSelected"),
        OBJECTSELECTED("ObjectSelected"),
        NODESELECTED("lvsnodeselected"),
        SELECTED("lvsselected"),
        SELECTIONCLEARED("lvsselectioncleared"),
        CONTENTADDED("lvscontentadded"),
        SELECTIONCHANGE("SelectionChange"),
        FIELDCHANGE("FieldChange");

        String name = "";
        String object = "";

        private Events(String name) {
            this.name = name;
        }

        private Events(String name, String object) {
            this.name = name;
            this.object = object;
        }

        public String getName() {
            return this.name;
        }

        public String getObject() {
            return this.object;
        }
    }
}

