/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.lookup;

import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.images.ImageRef;
import java.util.ArrayList;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ImageLookup
extends BaseElement {
    public static final String SESSION_VAR = "__imagelookup_data";
    private boolean svg = false;
    private boolean svgContent = false;
    private String fieldid = "";
    private String notifymethod = "";
    private String mediatype = "";
    private String currentIcon = "";
    private String currentSize = "16";
    private boolean currentGray = false;
    private boolean currentFlipV = false;
    private boolean currentFlipH = false;
    private String currentOpac = "100";
    private String currentHue = "0";
    private String currentIconDesc = "";
    private StringBuffer summary = new StringBuffer();
    private int fullTotal = 0;
    private int total = 0;
    private int count = 200;
    private Mode mode = Mode.LOOKUP;
    private ArrayList<ImageRef> imageList = null;

    public ImageLookup() {
    }

    public ImageLookup(PageContext pageContext) {
        this.setPageContext(pageContext);
    }

    public String getFieldId() {
        return this.fieldid;
    }

    public String getMediaType() {
        return this.mediatype;
    }

    public String getCurrentSize() {
        return this.currentSize;
    }

    public String getCurrentHue() {
        return this.currentHue;
    }

    public boolean getCurrentGrayscale() {
        return this.currentGray;
    }

    public boolean getCurrentFlipHorizontal() {
        return this.currentFlipH;
    }

    public boolean getCurrentFlipVertical() {
        return this.currentFlipV;
    }

    public String getCurrentOpacity() {
        return this.currentOpac;
    }

    public String getCurrentIcon() {
        return this.currentIcon;
    }

    public String getCurrentIconDescription() {
        return this.currentIconDesc;
    }

    public String getIcons(int start, int maxNo, String categoryId, String searchText, String media, String resolution, SapphireConnection sapphireConnection) {
        StringBuffer out = new StringBuffer();
        String search = searchText.toLowerCase();
        if ((searchText.startsWith("%") || searchText.startsWith("*")) && searchText.length() > 1) {
            search = search.substring(1);
        }
        if ((searchText.endsWith("%") || searchText.endsWith("*")) && searchText.length() > 1) {
            search = search.substring(0, search.length() - 1);
        }
        this.logger.debug("GetIcons - search = " + search);
        int totalCount = ImageRef.getImageCount(categoryId, searchText, sapphireConnection);
        ArrayList<ImageRef> imageList = ImageRef.getImages(start, -1, sapphireConnection);
        if (imageList != null && imageList.size() > 0) {
            int count = 0;
            int vcount = 0;
            int scount = 0;
            for (int i = 0; i < imageList.size(); ++i) {
                ImageRef im = imageList.get(i);
                boolean cont = false;
                String toSearch = im.getSearchString();
                cont = searchText.length() == 0 ? true : toSearch.contains(searchText);
                if (cont) {
                    if (this.svg) {
                        cont = false;
                        if (this.svgContent) {
                            if (im.getSVGContent().length() > 0) {
                                cont = true;
                            }
                        } else if (im.getRawImage().startsWith("WEB-CORE/imageref/flat/") && im.getRawImage().contains("flat_black_") && im.getRawImage().endsWith(".svg")) {
                            cont = true;
                        } else if (im.getSVGContent().length() > 0) {
                            cont = true;
                        }
                    } else {
                        cont = true;
                    }
                }
                if (cont) {
                    if (categoryId.length() > 0 && !im.getCategories().contains(categoryId)) {
                        cont = false;
                    }
                    if (!(!cont || media.length() <= 0 || (im.getFileType() == ImageRef.FileType.BITMAP || im.getFileType() == ImageRef.FileType.SVG || im.getFileType() == ImageRef.FileType.ICON) && media.equalsIgnoreCase("I") || im.getFileType() == ImageRef.FileType.IMAGE && media.equalsIgnoreCase("P"))) {
                        cont = false;
                    }
                    if (cont && resolution.length() > 0 && im.getFileType() == ImageRef.FileType.BITMAP) {
                        ArrayList<Integer> sizes = im.getSizes();
                        if (!(resolution.equals("16") && sizes.contains(16) || resolution.equals("32") && sizes.contains(32) || resolution.equals("48") && sizes.contains(48))) {
                            cont = false;
                        }
                    }
                }
                if (!cont || ++scount <= start) continue;
                ++vcount;
                if (count >= maxNo) continue;
                ++count;
                ImageLookup.getIcon(out, im);
            }
            this.count = count;
            this.total = vcount + start;
            this.fullTotal = totalCount;
            this.summary.append(count + start).append(" of ").append(this.total).append(" images [").append(this.fullTotal).append("]");
            if (count + start < this.total) {
                this.summary.append(" <a href=\"javascript:imageLookup.loadingMore = true;sapphire.ajax.callClass( 'com.labvantage.sapphire.pageelements.lookup.ImageLookupAjaxRender', 'imageLookup.scroll_Callback', { 'category': imageLookup.currentCategory, 'search': imageLookup.currentSearch, 'media': imageLookup.currentMedia, 'resolution': imageLookup.currentResolution, 'start': imageLookup.count, 'max': imageLookup.startCount, 'returnsvg': imageLookup.returnSVG ? 'Y' : 'N', 'returnsvgcontent': imageLookup.returnSVGContent ? 'Y' : 'N' }, true, false );\">");
                this.summary.append("Load More <img src=\"rc?command=image&image=FlatBlackDownload2&color=blue\"/></a>");
            }
            if (count == 0) {
                out.append("No Icons Returned");
            }
        } else {
            this.count = 0;
            this.total = 0;
            this.fullTotal = 0;
            out.append("No Icons Available");
            this.summary.append(0).append(" images");
        }
        return out.toString();
    }

    public String getIcons() {
        StringBuffer out = new StringBuffer();
        if (this.imageList != null && this.imageList.size() > 0) {
            boolean currentFound = false;
            SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
            this.count = this.imageList.size() < this.count ? this.imageList.size() : this.count;
            this.total = this.imageList.size();
            this.fullTotal = this.imageList.size();
            for (int i = 0; i < this.count; ++i) {
                ImageRef image = this.imageList.get(i);
                if (image.getImageRefId().equals(this.currentIcon)) {
                    currentFound = true;
                    this.currentIconDesc = image.getDescription();
                }
                ImageRef.FileType type = image.getFileType();
                if (this.mediatype.length() != 0 && (type != ImageRef.FileType.BITMAP && type != ImageRef.FileType.ICON || !this.mediatype.equalsIgnoreCase("B") && !this.mediatype.equalsIgnoreCase("I")) && (type != ImageRef.FileType.IMAGE || !this.mediatype.equalsIgnoreCase("P"))) continue;
                ImageLookup.getIcon(out, image);
            }
            this.summary.append(this.count).append(" of ").append(this.total).append(" images [").append(this.fullTotal).append("]");
            if (!currentFound) {
                this.currentIcon = "";
            }
        } else {
            out.append("No Icons Available");
            this.summary.append(0).append(" images");
        }
        return out.toString();
    }

    public void setCurrentIconDescription(String desc) {
        this.currentIconDesc = desc;
    }

    public String getNotifyMethod() {
        return this.notifymethod;
    }

    public String getSummary() {
        return this.summary.toString();
    }

    public PropertyList getIconMap(String url) {
        url = url.substring(3);
        String[] params = url.split("&");
        PropertyList map = new PropertyList();
        for (String param : params) {
            String[] sub = param.split("=");
            map.setProperty(sub[0], sub[1]);
        }
        return map;
    }

    public int getTotal() {
        return this.total;
    }

    public int getFullTotal() {
        return this.fullTotal;
    }

    private static String getRes(ImageRef image) {
        String res = "";
        ArrayList<Integer> sizes = image.getSizes();
        if (sizes.contains(16)) {
            res = res + "16x16";
        }
        if (sizes.contains(32)) {
            res = res + (res.length() > 0 ? ", " : "") + "32x32";
        }
        if (sizes.contains(48)) {
            res = res + (res.length() > 0 ? ", " : "") + "48x48";
        }
        return res;
    }

    private static void getIcon(StringBuffer out, ImageRef image) {
        String res = ImageLookup.getRes(image);
        String style = image.getImageRefId().startsWith("FlatWhite") ? "background-color: #BBBBBB" : "";
        String svgname = "";
        if (image.getRawImage().startsWith("WEB-CORE/imageref/flat/") && image.getRawImage().endsWith(".svg")) {
            svgname = image.getRawImage().substring(image.getRawImage().lastIndexOf("/") + 1);
            svgname = svgname.substring(0, svgname.lastIndexOf(".svg"));
            svgname = svgname.substring(svgname.indexOf("flat_black_") + "flat_black_".length());
        }
        out.append("<div " + (style.length() > 0 ? " style=\"" + style + "\"" : "") + " imageid=\"").append(image.getImageRefId()).append("\" imagedesc=\"").append(image.getDescription()).append("\" svgname=\"").append(svgname).append("\" imagefiletype=\"").append(image.getFileType().getType()).append("\" imageres=\"").append(res).append("\" class=\"icon\" onmouseout=\"imageLookup.mouseOut(this,event);\" onclick=\"imageLookup.click(this,event);\" onmouseover=\"imageLookup.mouseOver(this,event);\">");
        out.append("<div class=\"icon_img\">");
        Image imageElement = new Image();
        imageElement.setImageRef(image);
        imageElement.setRenderAllResolutions(true);
        imageElement.setDimensions(32, 32);
        out.append(imageElement.getHtml());
        out.append("</div>");
        String title = image.getImageRefId();
        title = StringUtil.replaceAll(title, "FlatBlack", "");
        title = StringUtil.replaceAll(title, "FlatWhite", "");
        title = StringUtil.replaceAll(title, "MDI", "");
        out.append("").append(title);
        out.append("</div>");
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return this.mode;
    }

    public String getSearchArea() {
        StringBuffer html = new StringBuffer();
        TranslationProcessor tp = this.getTranslationProcessor();
        RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        html.append(tp.translate("Search")).append(": <input type=text id=\"search\" style=\"width:100px;\" value=\"" + userConfig.getProperty("imagelookup_search") + "\" onkeyup=\"imageLookup.searchKey(this,event)\">");
        html.append("&nbsp;&nbsp;");
        if (!this.svg) {
            html.append(tp.translate("Category:"));
            html.append(" <select id=\"category\" style=\"width:100px;\" onchange=\"imageLookup.filter(this,event);\">");
            html.append(this.getCategories(userConfig.getProperty("imagelookup_category")));
            html.append("</select>");
            html.append("&nbsp;&nbsp;");
            html.append(tp.translate("Media:"));
            String media = this.getMediaType();
            String mediaSelected = userConfig.getProperty("imagelookup_media");
            html.append(" <select id=\"media\" style=\"width:100px;\" onchange=\"imageLookup.filter(this,event);\"").append(media.length() > 0 ? " disabled" : "").append(">");
            html.append("<option value=\"\"").append(media.length() == 0 || mediaSelected.length() == 0 ? " SELECTED" : "").append(">All</option>");
            html.append("<option value=\"I\" ").append(media.equalsIgnoreCase("B") || mediaSelected.equalsIgnoreCase("B") || media.equalsIgnoreCase("I") || mediaSelected.equalsIgnoreCase("I") ? " SELECTED" : "").append(">Icons</option>");
            html.append("<option value=\"P\" ").append(media.equalsIgnoreCase("P") || mediaSelected.equalsIgnoreCase("P") ? " SELECTED" : "").append(">Pictures</option>");
            html.append("</select>");
        }
        return html.toString();
    }

    public void setReturnSVG(boolean returnSVG) {
        this.svg = returnSVG;
    }

    public void setReturnSVGContent(boolean returnSVGContent) {
        this.svgContent = returnSVGContent;
        if (returnSVGContent) {
            this.svg = true;
        }
    }

    public void loadProperties(PageTagInfo pageinfo) {
        String currentURL;
        this.fieldid = pageinfo.getProperty("fieldid");
        this.notifymethod = pageinfo.getProperty("notifymethod");
        this.mediatype = pageinfo.getProperty("type");
        this.svg = pageinfo.getProperty("returnsvg", "N").equalsIgnoreCase("Y");
        this.svgContent = pageinfo.getProperty("returnsvgcontent", "N").equalsIgnoreCase("Y");
        if (this.svgContent) {
            this.svg = true;
        }
        if (pageinfo.getProperty("command").equalsIgnoreCase("page")) {
            PropertyList pd = pageinfo.getPropertyList("pagedata");
            if (pd != null) {
                try {
                    this.mode = Mode.valueOf(pd.getProperty("mode", Mode.LIST.toString()).toUpperCase());
                }
                catch (Exception e) {
                    this.mode = Mode.LIST;
                }
            } else {
                this.mode = Mode.LOOKUP;
            }
        }
        if ((currentURL = pageinfo.getProperty("icon")).length() > 0 && currentURL.startsWith("rc?command=icon")) {
            PropertyList iconQuery = this.getIconMap(currentURL);
            this.currentGray = iconQuery.getProperty("gray", iconQuery.getProperty("grayscale", "N")).equalsIgnoreCase("Y");
            this.currentFlipH = iconQuery.getProperty("fliph", iconQuery.getProperty("fliphorizontal", "N")).equalsIgnoreCase("Y");
            this.currentFlipV = iconQuery.getProperty("flipv", iconQuery.getProperty("flipvertical", "N")).equalsIgnoreCase("Y");
            this.currentOpac = iconQuery.getProperty("opacity", "100");
            this.currentSize = iconQuery.getProperty("size", "16");
            this.currentIcon = iconQuery.getProperty("icon", "");
            this.currentHue = iconQuery.getProperty("hue", "");
        }
    }

    public String getCategories(String selected) {
        DataSet cats;
        StringBuffer out = new StringBuffer();
        out.append("<option value=\"\"></option>");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Category");
        sdiRequest.setQueryOrderBy("categoryid");
        sdiRequest.setQueryFrom("category");
        sdiRequest.setQueryWhere("sdcid='LV_ImageRef'");
        sdiRequest.setRequestItem("primary");
        SDIProcessor sdi = new SDIProcessor(this.pageContext);
        SDIData temp = sdi.getSDIData(sdiRequest);
        if (temp != null && (cats = temp.getDataset("primary")) != null && cats.size() > 0) {
            ArrayList<String> found = new ArrayList<String>();
            for (int c = 0; c < cats.getRowCount(); ++c) {
                String catId = cats.getValue(c, "categoryid", "");
                if (found.contains(catId)) continue;
                found.add(catId);
                if (catId.equalsIgnoreCase("sapphireconfig")) continue;
                out.append("<option " + (catId.equals(selected) ? " SELECTED " : "") + " value=\"").append(catId + "\">").append(SafeHTML.encodeForHTML(catId)).append("</option>");
            }
        }
        return out.toString();
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public String getHtml() {
        return "";
    }

    public static enum Mode {
        LOOKUP,
        LIST;

    }
}

