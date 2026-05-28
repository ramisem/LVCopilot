/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.location;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ImageMapEditor
extends BaseElement {
    public static final String DIV_TOP = "top";
    public static final String DIV_BOTTOM = "bottom";
    public static final String DIV_LEFT = "left";
    public static final String DIV_RIGHT = "right";
    public static final String PROPERTY_MAP = "map";
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_LOCATIONMAINT_GIZMOID = "Location";
    public static final String PROPERTY_COORDINATES = "coordinates";
    public static final String PROPERTY_IMAGESOURCE = "imagesource";
    private String keyid1;
    private String keyid2;
    private String keyid3;
    private int divCounter = 1;
    private String imageurl;
    private boolean forcesize;
    private String forcewidth;
    private String hotspotprop;
    private String attachmentClass;
    private boolean addmode;
    private HashMap<String, String> coords;
    private HashMap<String, HashMap<String, String>> divs;
    private int left;
    private int top;
    private int right;
    private int bottom;
    private StringBuffer endScript;
    private String forceheight;

    public String getKeyid2() {
        return this.keyid2;
    }

    public void setKeyid2(String keyid2) {
        this.keyid2 = keyid2;
    }

    public String getKeyid3() {
        return this.keyid3;
    }

    public void setKeyid3(String keyid3) {
        this.keyid3 = keyid3;
    }

    public int getDivCounter() {
        return this.divCounter;
    }

    public void updateDivCounter() {
        ++this.divCounter;
    }

    public String getAttachmentClass() {
        return this.attachmentClass;
    }

    public void setAttachmentClass(String attachmentClass) {
        this.attachmentClass = attachmentClass;
    }

    public boolean isAddmode() {
        return this.addmode;
    }

    public void setAddmode(boolean addmode) {
        this.addmode = addmode;
    }

    public StringBuffer getEndScript() {
        return this.endScript;
    }

    public int getLeft() {
        return this.left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return this.top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getRight() {
        return this.right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return this.bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public String getKeyid1() {
        return this.keyid1;
    }

    public void setKeyid1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public String getImageurl() {
        return this.imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public boolean getForcesize() {
        return this.forcesize;
    }

    public void setForcesize(boolean forcesize) {
        this.forcesize = forcesize;
    }

    public String getForcewidth() {
        return this.forcewidth;
    }

    public void setForcewidth(String forcewidth) {
        this.forcewidth = forcewidth;
    }

    public String getForceheight() {
        return this.forceheight;
    }

    public void setForceheight(String forceheight) {
        this.forceheight = forceheight;
    }

    public void initializeEndScript() {
        if (this.endScript == null) {
            this.endScript = new StringBuffer();
            this.endScript.append("<script>");
        }
    }

    public ImageMapEditor(PageContext pageContext) {
        this.setPageContext(pageContext);
        try {
            RequestContext rc = RequestContext.getRequestContext(pageContext);
            ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
            this.setUpProperties(rc.getPropertyList(), (HttpServletRequest)pageContext.getRequest());
        }
        catch (Exception e) {
            this.logger.error("Could not set up properties: " + e.getMessage(), e);
        }
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        html.append("<script>");
        html.append("var ").append("hotspotproperty='").append(this.hotspotprop).append("';");
        html.append("</script>");
        if (this.validateAttachment()) {
            if (this.getAttachmentClass() != null && this.getAttachmentClass().equalsIgnoreCase("ImageMap")) {
                Button addButton = new Button(this.pageContext);
                addButton.setText(tp.translate("Add Hotspot"));
                addButton.setAction("createHotspot()");
                addButton.setImg("WEB-CORE/images/png/Add.png");
                addButton.setStyle("position:left;");
                Button expandButton = new Button(this.pageContext);
                expandButton.setId("expand");
                expandButton.setTip(tp.translate("Resize Image"));
                expandButton.setAction("expandHotspotImageCol( '" + this.getKeyid1() + "' ) ");
                expandButton.setImg("WEB-CORE/images/png/expandHotspot.png");
                Button restoreButton = new Button(this.pageContext);
                restoreButton.setId("restore");
                restoreButton.setTip(tp.translate("Restore Image"));
                String ac = "restoreHotspotImageCol( '" + this.getKeyid1() + "' ) ";
                restoreButton.setAction(ac);
                restoreButton.setStyle("display:none");
                restoreButton.setImg("WEB-CORE/images/png/shrinkHotspot.png");
                html.append("<div>");
                html.append("<span>");
                html.append(addButton.getHtml());
                html.append("</span>");
                html.append("<span style=\"float: right;\">");
                html.append(expandButton.getHtml());
                html.append(restoreButton.getHtml());
                html.append("</span>");
                html.append("</div>");
                html.append("<script>\n$(\"#expand\").click(function(){\n    $(\"#restore\").show();\n    $(\"#expand\").hide();\n  });\n$(\"#restore\").click(function(){\n    $(\"#expand\").show();\n    $(\"#restore\").hide();\n  });\n</script>");
                String iu = this.pageContext.getRequest().getParameter(PROPERTY_IMAGESOURCE);
                String imageId = "masterimage_" + this.getKeyid1();
                html.append("<div id=\"hotspotimage\" style=\"vertical-align:middle;align:center;width:100%;height:95%;border:1px solid black;overflow:scroll;position:absolute\">");
                html.append("<img id= \"masterimage_" + this.getKeyid1() + "\" src= \"" + this.getImageurl() + "\" />");
                this.diplayHotspots(html);
                if (this.getEndScript() != null) {
                    html.append(this.getEndScript());
                }
                html.append("</div>");
            } else {
                html.append(this.getTranslationProcessor().translate("Please select attachment class as 'ImageMap'"));
            }
        } else {
            html.append(this.getTranslationProcessor().translate("No image file found in attachment"));
        }
        return html.toString();
    }

    private boolean validateAttachment() {
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select filename,attachmentclass from sdiattachment where keyid1= " + safeSQL.addVar(this.getKeyid1()), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            if (ds.getValue(0, "filename") != null && !ds.getValue(0, "filename").isEmpty()) {
                if (ds.getValue(0, "attachmentclass") != null) {
                    this.setAttachmentClass(ds.getValue(0, "attachmentclass").toString());
                }
                return true;
            }
            return false;
        }
        return false;
    }

    private void diplayHotspots(StringBuffer html) {
        String id = "";
        String coords = "";
        String sdcid = "";
        String keyid1 = "";
        String label = "";
        try {
            JSONArray hotspotsPropArray = new JSONArray(this.hotspotprop);
            JSONObject hotspotsProp = new JSONObject(hotspotsPropArray.get(0).toString());
            JSONArray hotspotsArray = new JSONArray(hotspotsProp.has("hotspot") ? hotspotsProp.getString("hotspot") : "[]");
            if (hotspotsArray.length() > 0) {
                this.initializeEndScript();
                for (int hotspot = 0; hotspot < hotspotsArray.length(); ++hotspot) {
                    String[] coordbits;
                    JSONObject hotspots = new JSONObject(hotspotsArray.get(hotspot).toString());
                    if (hotspots.has("id")) {
                        id = hotspots.get("id").toString();
                    }
                    if (hotspots.has("coords")) {
                        coords = hotspots.get("coords").toString();
                    }
                    if (hotspots.has("sdcId")) {
                        sdcid = hotspots.get("sdcId").toString();
                    }
                    if (hotspots.has("keyid1")) {
                        keyid1 = hotspots.get("keyid1").toString();
                    }
                    if (hotspots.has("label")) {
                        label = hotspots.get("label").toString();
                    }
                    if ((coordbits = StringUtil.split(coords, ",")).length == 4) {
                        this.left = Integer.parseInt(coordbits[0]);
                        this.top = Integer.parseInt(coordbits[1]);
                        this.right = Integer.parseInt(coordbits[2]);
                        this.bottom = Integer.parseInt(coordbits[3]);
                    }
                    html.append("<div id=\"hotspotdiv_" + id + "\"class=\"ui-widget-content\" style=\"position:absolute; background-color: beige; justify-content:safe center;left:" + this.left + "px;top:" + this.top + "px;width:" + (this.right - this.left) + "px;height:" + (this.bottom - this.top) + "px;opacity:0.8\" >");
                    html.append("<button id = \"linkbtn_" + id + " \" class = \"btn\" title = \" Link Child \" onclick=\"linkchildHotspot(this)\" style= \" float:left;width: 22px;height: 22px;padding-left: 1px;padding-right: 1px; \" >");
                    html.append("<img src = \"WEB-CORE/images/png/add-link.png\" ></i");
                    html.append("/img>");
                    html.append("</button>");
                    html.append("<button id = \"createbtn_" + id + " \" class = \"btn\" title = \" Create New SDI \" onclick=\"editandcreateNewHotspot(this)\" style= \" float:left;width: 22px;height: 22px;padding-left: 1px;padding-right: 1px; \" >");
                    html.append("<img src = \"WEB-CORE/images/png/create.png\" ></i");
                    html.append("/img>");
                    html.append("</button>");
                    html.append("<button id = \"addbtn_" + id + " \" class = \"btn\" title = \" Link Existing SDI \" onclick=\"linkexistingsdiHotspot(this)\" style= \" float:left;width: 22px;height: 22px;padding-left: 1px;padding-right: 1px; \" >");
                    html.append("<img src = \"WEB-CORE/images/png/add-new.png\" ></i");
                    html.append("/img>");
                    html.append("</button>");
                    html.append("<button id = \"deletebtn_" + id + " \" class = \"btn\" onclick=\"deleteHotspot(this)\" style= \" float:right;color:Tomato;width: 22px;height: 22px;padding-left: 1px;padding-right: 1px; \" >");
                    html.append("<img src = \"WEB-CORE/images/png/close-windows.png\" ></i");
                    html.append("/img>");
                    html.append("</button>");
                    html.append("\n");
                    html.append("<br>");
                    html.append("<br>");
                    html.append("<label id=hotspotLabel style= \" left:" + (this.right - this.left) / 2 + "px top:" + (this.top + this.bottom / 2) + ";text-align:center;position: absolute;top: 50%;\nleft: 50%;\">" + label + "</label>");
                    html.append("</div>");
                    this.endScript.append("$( function () {");
                    this.endScript.append("$( \"#hotspotdiv_" + id + "\" ).draggable( {");
                    this.endScript.append("containment: \"#hotspotimage\",\n                    scroll: true,\n                    drag: function () {\n                        updateHotspot( \"hotspotdiv_" + id + "\" );\n                    },\n                    stop: function () {\n                        updateHotspot( \"hotspotdiv_" + id + "\" );\n                    }");
                    this.endScript.append("} );");
                    this.endScript.append("$( \"#hotspotdiv_" + id + "\" ).resizable( {");
                    this.endScript.append("scroll: false,\n                    resize: function () {\n                        updateHotspot( \"hotspotdiv_" + id + "\" );\n                    },\n                    stop: function () {\n                        updateHotspot( \"hotspotdiv_" + id + "\" );\n                    }");
                    this.endScript.append("} );");
                    this.endScript.append("} );\n");
                    this.endScript.append("$( \"#hotspotdiv_" + id + "\" ).hover(function(){\n    $(this).css(\"background-color\", \"#ffdc5d\");\n    $(this).css(\"color\", \"white\");\n    }, function(){\n    $(this).css(\"background-color\", \"beige\");\n    $(this).css(\"color\", \"black\");\n  });");
                }
                this.endScript.append("</script>");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpProperties(PropertyList pagedata, HttpServletRequest request) throws Exception {
        this.hotspotprop = request.getParameter("hotspotprop");
        this.setKeyid1(request.getParameter("keyid1"));
        this.setKeyid2(request.getParameter("keyid2"));
        this.setKeyid3(request.getParameter("keyid3"));
        JSONArray hotspotsPropArray = new JSONArray(this.hotspotprop);
        JSONObject obj = new JSONObject(hotspotsPropArray.get(0).toString());
        String sdcid = "";
        if (obj.has("sdcid") && OpalUtil.isNotEmpty(obj.get("sdcid").toString())) {
            sdcid = obj.get("sdcid").toString();
        }
        this.validateAttachment();
        String imageurl = "rc?command=image&attachment=" + sdcid + ";" + this.getKeyid1() + ";" + this.getKeyid2() + ";" + this.getKeyid3() + ";" + this.getAttachmentClass() + "&nocache=Y";
        this.setImageurl(imageurl);
    }
}

