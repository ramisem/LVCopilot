/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  net.sf.image4j.codec.ico.ICODecoder
 *  net.sf.image4j.codec.ico.ICOImage
 *  org.apache.batik.anim.dom.SAXSVGDocumentFactory
 *  org.apache.batik.transcoder.TranscoderInput
 *  org.apache.batik.transcoder.TranscoderOutput
 *  org.apache.batik.transcoder.image.PNGTranscoder
 *  org.apache.batik.util.XMLResourceDescriptor
 *  org.apache.xpath.XPathAPI
 */
package com.labvantage.sapphire.util.images;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.awt.image.RenderedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import net.sf.image4j.codec.ico.ICODecoder;
import net.sf.image4j.codec.ico.ICOImage;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.attachment.Attachment;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;

public class ImageRef
implements Serializable,
Cloneable {
    public static final String COMMAND = "command=image";
    public static final String ATTACHMENTCOMMAND = "rc?command=image&attachment=";
    public static final String ATTACHMENTTHUMBNAILCOMMAND = "rc?command=image&attachmentthumb=";
    public static final String SDITEMPCOMMAND = "rc?command=image&sditemp=";
    public static final String SDITEMPTHUMBNAILCOMMAND = "rc?command=image&sditempthumb=";
    public static final String IMAGECOMMANDURL = "rc?command=image&image=";
    public static final String FILECOMMANDURL = "rc?command=image&file=";
    public static final String DEFAULTIMAGE = "WEB-CORE/images/blank.gif";
    public static final String LISTCACHE = "$LIST$";
    private SapphireConnection sapphireConnection = null;
    private String defaultimage = "";
    private String imageref = "";
    private String description = "";
    private String imageUrl = "";
    private String smallicon = "";
    private String mediumicon = "";
    private String largeicon = "";
    private String svgcontent = "";
    private String iconfile = "";
    private String imagefile = "";
    private String svgfile = "";
    private String attachmentKeyid1 = "";
    private String attachmentKeyid2 = "";
    private String attachmentKeyid3 = "";
    private String attachmentSDCId = "";
    private int attachmentNum = 0;
    private String attachmentClass = "";
    private boolean attachmentThumbnail = false;
    private String tempid = "";
    private Attachment.ThumbnailGeneration attachmentThumbnailGeneration = null;
    private FileType filetypeflag = FileType.BITMAP;
    private ImageType imagetypeflag = ImageType.USER;
    private boolean grayscale = false;
    private boolean flipvertical = false;
    private boolean fliphorizontal = false;
    private int hue = 0;
    private float opacity = 0.0f;
    private String color = "";
    private boolean nocache = false;
    private int width = 0;
    private int height = 0;
    private int size = 16;
    private String overlay = "";
    byte[] image = null;
    String svgImage = null;
    private ArrayList<String> categories = new ArrayList();

    public ImageRef(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
    }

    public void setAttachment(String attachment) {
        String[] att = StringUtil.split(attachment, ";");
        int a = 0;
        if (att.length == 3) {
            try {
                a = Integer.parseInt(att[2]);
                this.setAttachment(att[0], att[1], att[2], att[3], a);
            }
            catch (NumberFormatException e) {
                a = 0;
                this.setAttachment(att[0], att[1], "", "", att[2]);
            }
        } else if (att.length == 4) {
            try {
                a = Integer.parseInt(att[3]);
                this.setAttachment(att[0], att[1], att[2], "", a);
            }
            catch (NumberFormatException e) {
                a = 0;
                this.setAttachment(att[0], att[1], att[2], "", att[3]);
            }
        } else {
            try {
                a = Integer.parseInt(att[4]);
                this.setAttachment(att[0], att[1], att[2], att[3], a);
            }
            catch (NumberFormatException e) {
                a = 0;
                this.setAttachment(att[0], att[1], att[2], att[3], att[4]);
            }
        }
    }

    public void setSDITemp(String sditemp) {
        String[] att = StringUtil.split(sditemp, ";");
        if (att.length == 1) {
            this.setSDITemp("", "", "", "", att[0]);
        } else if (att.length == 2) {
            this.setSDITemp(att[0], "", "", "", att[1]);
        } else if (att.length == 3) {
            this.setSDITemp(att[0], att[1], "", "", att[2]);
        } else if (att.length == 4) {
            this.setSDITemp(att[0], att[1], att[2], "", att[3]);
        } else {
            this.setSDITemp(att[0], att[1], att[2], att[3], att[4]);
        }
    }

    public void setAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int num) {
        this.attachmentSDCId = sdcid;
        this.attachmentKeyid1 = keyid1;
        this.attachmentKeyid2 = keyid2;
        this.attachmentKeyid3 = keyid3;
        this.attachmentNum = num;
        this.attachmentClass = "";
        this.tempid = "";
        this.filetypeflag = FileType.ATTACHMENT;
        this.imagetypeflag = ImageType.USER;
    }

    public void setSDITemp(String sdcid, String keyid1, String keyid2, String keyid3, String tempid) {
        this.attachmentSDCId = sdcid;
        this.attachmentKeyid1 = keyid1;
        this.attachmentKeyid2 = keyid2;
        this.attachmentKeyid3 = keyid3;
        this.tempid = tempid;
        this.attachmentClass = "";
        this.attachmentNum = 0;
        this.filetypeflag = FileType.SDITEMP;
        this.imagetypeflag = ImageType.USER;
    }

    public void setAttachment(String sdcid, String keyid1, String keyid2, String keyid3, String attclass) {
        this.attachmentSDCId = sdcid;
        this.attachmentKeyid1 = keyid1;
        this.attachmentKeyid2 = keyid2;
        this.attachmentKeyid3 = keyid3;
        this.attachmentNum = 0;
        this.attachmentClass = attclass;
        this.tempid = "";
        this.filetypeflag = FileType.ATTACHMENT;
        this.imagetypeflag = ImageType.USER;
    }

    public Attachment getAttachment() {
        Attachment attachment = new Attachment();
        attachment.setSDCId(this.attachmentSDCId);
        attachment.setKeyId1(this.attachmentKeyid1);
        attachment.setKeyId2(this.attachmentKeyid2);
        attachment.setKeyId3(this.attachmentKeyid3);
        attachment.setAttachmentNum(this.attachmentNum);
        return attachment;
    }

    public void setThumbnailGeneration(Attachment.ThumbnailGeneration thumbnailGeneration) {
        this.attachmentThumbnailGeneration = thumbnailGeneration;
    }

    public void setDefault(String defaultImage) {
        this.defaultimage = defaultImage.length() > 0 ? defaultImage : DEFAULTIMAGE;
    }

    private void getData(DataSet pri, int row) {
        this.description = pri.getValue(row, "imagerefdesc");
        this.filetypeflag = FileType.getType(pri.getValue(row, "filetypeflag", "B"));
        this.imagetypeflag = ImageType.getType(pri.getValue(row, "imagetypeflag", "U"));
        switch (this.filetypeflag) {
            case IMAGE: {
                this.imagefile = pri.getValue(row, "imagefile", "");
                break;
            }
            case ICON: {
                this.iconfile = pri.getValue(row, "iconfile", "");
                break;
            }
            case SVG: {
                this.svgcontent = pri.getValue(row, "svgcontent", "");
            }
            case BITMAP: {
                this.largeicon = pri.getValue(row, "largeicon", "");
                this.mediumicon = pri.getValue(row, "mediumicon", "");
                this.smallicon = pri.getValue(row, "smallicon", "");
            }
        }
    }

    private boolean getData() {
        boolean out = false;
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_ImageRef");
        sdiRequest.setKeyid1List(this.imageref);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("category");
        sdiRequest.setExtendedDataTypes(true);
        SDIProcessor sdi = new SDIProcessor(this.sapphireConnection.getConnectionId());
        try {
            SDIData sdiData = sdi.getSDIData(sdiRequest);
            if (sdiData == null || sdiData.getDataset("primary") == null || sdiData.getDataset("primary").size() == 0) {
                Logger.logWarn("Could not obtain image data for " + this.imageref + ".");
                out = false;
            } else {
                DataSet cats;
                DataSet pri = sdiData.getDataset("primary");
                if (pri != null && pri.getRowCount() > 0) {
                    this.getData(pri, 0);
                }
                if ((cats = sdiData.getDataset("category")) != null) {
                    for (int i = 0; i < cats.getRowCount(); ++i) {
                        this.categories.add(cats.getValue(i, "categoryid", ""));
                    }
                    this.categories = this.categories;
                }
                out = true;
            }
        }
        catch (Exception e) {
            Logger.logWarn("Could not obtain icon SDI.");
            out = false;
        }
        return out;
    }

    public ArrayList<Integer> getSizes() {
        ArrayList<Integer> out = new ArrayList<Integer>();
        if (this.filetypeflag == FileType.BITMAP || this.filetypeflag == FileType.SVG) {
            if (this.smallicon.length() > 0) {
                out.add(16);
            }
            if (this.mediumicon.length() > 0) {
                out.add(32);
            }
            if (this.largeicon.length() > 0) {
                out.add(48);
            }
        }
        return out;
    }

    public ArrayList<String> getCategories() {
        return this.categories;
    }

    public String getSearchString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.imageref).append(" ");
        sb.append(this.description).append(" ");
        switch (this.filetypeflag) {
            case SVG: 
            case BITMAP: {
                sb.append(this.smallicon.length() > 0 ? this.smallicon : (this.mediumicon.length() > 0 ? this.mediumicon : this.largeicon));
                break;
            }
            case ICON: {
                sb.append(this.iconfile);
                break;
            }
            case IMAGE: {
                sb.append(this.imagefile);
                break;
            }
            case URL: {
                sb.append(this.imageUrl);
                break;
            }
            case ATTACHMENT: {
                sb.append(this.attachmentKeyid1);
                break;
            }
            case SDITEMP: {
                sb.append(this.tempid);
            }
        }
        return sb.toString().toLowerCase();
    }

    public void clearCache(boolean all) {
        if (!all) {
            CacheUtil.remove(this.sapphireConnection.getDatabaseId(), "ImageRef", this.getId());
        } else {
            CacheUtil.clear(this.sapphireConnection.getDatabaseId(), "ImageRef");
        }
    }

    public void setImage(String imageRef) {
        this.setImage(imageRef, null);
    }

    public void setImage(String imageRef, FileType fileTypeFlag) {
        Object o;
        this.imageref = imageRef;
        if (fileTypeFlag != null) {
            this.filetypeflag = fileTypeFlag;
        }
        if ((o = CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ImageRef", this.getId())) == null || !(o instanceof ImageRef)) {
            if (this.getData()) {
                try {
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ImageRef", this.getId(), this.clone());
                }
                catch (Exception e) {
                    Logger.logWarn("Could not cache data.");
                }
            }
        } else {
            this.copy((ImageRef)o, this);
        }
    }

    public void setRequest(RequestContext requestContext) {
        this.imagetypeflag = ImageType.USER;
        if (requestContext.getProperty("image").length() > 0) {
            this.setImage(requestContext.getProperty("image"));
        } else if (requestContext.getProperty("file").length() > 0) {
            this.imageUrl = requestContext.getProperty("file");
            this.filetypeflag = FileType.URL;
        } else if (requestContext.getProperty("attachment").length() > 0) {
            this.setAttachment(requestContext.getProperty("attachment"));
            if (requestContext.getProperty("thumbnailflag").length() > 0) {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.getThumbnailGeneration(requestContext.getProperty("thumbnailflag"), Attachment.ThumbnailGeneration.DISABLED));
            } else {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.DISABLED);
            }
        } else if (requestContext.getProperty("attachmentthumb").length() > 0) {
            this.setAttachment(requestContext.getProperty("attachmentthumb"));
            this.attachmentThumbnail = true;
            if (requestContext.getProperty("thumbnailflag").length() > 0) {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.getThumbnailGeneration(requestContext.getProperty("thumbnailflag"), Attachment.ThumbnailGeneration.SHOWIFAVAILABLE));
            } else {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.SHOWIFAVAILABLE);
            }
        } else if (requestContext.getProperty("sditemp").length() > 0) {
            this.setSDITemp(requestContext.getProperty("sditemp"));
            if (requestContext.getProperty("thumbnailflag").length() > 0) {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.getThumbnailGeneration(requestContext.getProperty("thumbnailflag"), Attachment.ThumbnailGeneration.DISABLED));
            } else {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.DISABLED);
            }
        } else if (requestContext.getProperty("sditempthumb").length() > 0) {
            this.setSDITemp(requestContext.getProperty("sditempthumb"));
            this.attachmentThumbnail = true;
            if (requestContext.getProperty("thumbnailflag").length() > 0) {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.getThumbnailGeneration(requestContext.getProperty("thumbnailflag"), Attachment.ThumbnailGeneration.SHOWIFAVAILABLE));
            } else {
                this.setThumbnailGeneration(Attachment.ThumbnailGeneration.SHOWIFAVAILABLE);
            }
        }
        switch (this.filetypeflag) {
            case ICON: 
            case SVG: 
            case BITMAP: {
                if (requestContext.getProperty("size").length() <= 0) break;
                if (requestContext.getProperty("size").equalsIgnoreCase("large") || requestContext.getProperty("size").equalsIgnoreCase("48x48") || requestContext.getProperty("size").equalsIgnoreCase("48")) {
                    this.size = 48;
                    break;
                }
                if (requestContext.getProperty("size").equalsIgnoreCase("medium") || requestContext.getProperty("size").equalsIgnoreCase("32x32") || requestContext.getProperty("size").equalsIgnoreCase("32")) {
                    this.size = 32;
                    break;
                }
                this.size = 16;
                break;
            }
            default: {
                if (requestContext.getProperty("width").length() > 0) {
                    try {
                        this.width = Integer.parseInt(requestContext.getProperty("width"));
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if (requestContext.getProperty("height").length() <= 0) break;
                try {
                    this.height = Integer.parseInt(requestContext.getProperty("height"));
                    break;
                }
                catch (NumberFormatException numberFormatException) {
                    // empty catch block
                }
            }
        }
        if (requestContext.getProperty("flipvertical").length() > 0 || requestContext.getProperty("flipv").length() > 0) {
            boolean bl = this.flipvertical = requestContext.getProperty("flipvertical").length() > 0 ? requestContext.getProperty("flipvertical").equalsIgnoreCase("Y") : requestContext.getProperty("flipv").equalsIgnoreCase("Y");
        }
        if (requestContext.getProperty("fliphorizontal").length() > 0 || requestContext.getProperty("fliph").length() > 0) {
            boolean bl = this.fliphorizontal = requestContext.getProperty("fliphorizontal").length() > 0 ? requestContext.getProperty("fliphorizontal").equalsIgnoreCase("Y") : requestContext.getProperty("fliph").equalsIgnoreCase("Y");
        }
        if (requestContext.getProperty("grayscale").length() > 0 || requestContext.getProperty("gray").length() > 0) {
            boolean bl = this.grayscale = requestContext.getProperty("grayscale").length() > 0 ? requestContext.getProperty("grayscale").equalsIgnoreCase("Y") : requestContext.getProperty("gray").equalsIgnoreCase("Y");
        }
        if (requestContext.getProperty("opacity").length() > 0) {
            try {
                this.opacity = Float.parseFloat(requestContext.getProperty("opacity"));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (requestContext.getProperty("hue").length() > 0) {
            try {
                this.hue = Integer.parseInt(requestContext.getProperty("hue"));
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (requestContext.getProperty("overlay").length() > 0) {
            this.overlay = requestContext.getProperty("overlay");
        }
        if (requestContext.getProperty("color").length() > 0) {
            this.color = requestContext.getProperty("color");
        }
    }

    public void setURL(String url) {
        this.imagetypeflag = ImageType.USER;
        int i = url.indexOf("?");
        if (i > -1) {
            String t = url.substring(i);
            String[] parts = StringUtil.split(t, "&");
            for (int p = 0; p < parts.length; ++p) {
                String[] pair = StringUtil.split(parts[p], "=");
                if (pair.length != 2) continue;
                if (pair[0].equalsIgnoreCase("image")) {
                    this.setImage(pair[1]);
                    continue;
                }
                if (pair[0].equalsIgnoreCase("file")) {
                    this.imageUrl = pair[1];
                    this.filetypeflag = FileType.URL;
                    continue;
                }
                if (pair[0].equalsIgnoreCase("attachment")) {
                    this.setAttachment(pair[1]);
                    continue;
                }
                if (pair[0].equalsIgnoreCase("attachmentthumb")) {
                    this.setAttachment(pair[1]);
                    this.attachmentThumbnail = true;
                    continue;
                }
                if (pair[0].equalsIgnoreCase("sditemp")) {
                    this.setSDITemp(pair[1]);
                    continue;
                }
                if (pair[0].equalsIgnoreCase("size")) {
                    if (pair[1].equalsIgnoreCase("large") || pair[1].equalsIgnoreCase("48x48") || pair[1].equalsIgnoreCase("48")) {
                        this.size = 48;
                        continue;
                    }
                    if (pair[1].equalsIgnoreCase("medium") || pair[1].equalsIgnoreCase("32x32") || pair[1].equalsIgnoreCase("32")) {
                        this.size = 32;
                        continue;
                    }
                    this.size = 16;
                    continue;
                }
                if (pair[0].equalsIgnoreCase("width")) {
                    try {
                        this.width = Integer.parseInt(pair[1]);
                    }
                    catch (NumberFormatException numberFormatException) {}
                    continue;
                }
                if (pair[0].equalsIgnoreCase("height")) {
                    try {
                        this.height = Integer.parseInt(pair[1]);
                    }
                    catch (NumberFormatException numberFormatException) {}
                    continue;
                }
                if (pair[0].equalsIgnoreCase("flipvertical") || pair[0].equalsIgnoreCase("flipv")) {
                    this.flipvertical = pair[1].equalsIgnoreCase("Y");
                    continue;
                }
                if (pair[0].equalsIgnoreCase("fliphorizontal") || pair[0].equalsIgnoreCase("fliph")) {
                    this.fliphorizontal = pair[1].equalsIgnoreCase("Y");
                    continue;
                }
                if (pair[0].equalsIgnoreCase("grayscale") || pair[0].equalsIgnoreCase("gray")) {
                    this.grayscale = pair[1].equalsIgnoreCase("Y");
                    continue;
                }
                if (pair[0].equalsIgnoreCase("opacity")) {
                    try {
                        this.opacity = Float.parseFloat(pair[1]);
                    }
                    catch (NumberFormatException numberFormatException) {}
                    continue;
                }
                if (pair[0].equalsIgnoreCase("hue")) {
                    try {
                        this.hue = Integer.parseInt(pair[1]);
                    }
                    catch (NumberFormatException numberFormatException) {}
                    continue;
                }
                if (pair[0].equalsIgnoreCase("overlay")) {
                    this.overlay = pair[1];
                    continue;
                }
                if (!pair[0].equalsIgnoreCase("color")) continue;
                this.color = pair[1];
            }
        } else {
            this.filetypeflag = FileType.URL;
            this.imageUrl = url;
        }
    }

    private static int addImages(SDIData iconData, ArrayList outList, SapphireConnection sapphireConnection) {
        DataSet primary;
        int added = 0;
        if (iconData != null && (primary = iconData.getDataset("primary")) != null) {
            added = 0;
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String im = primary.getValue(i, "imagerefid", "");
                String desc = primary.getValue(i, "imagerefdesc", "");
                boolean cont = true;
                if (!cont) continue;
                ImageRef in = new ImageRef(sapphireConnection);
                in.imageref = im;
                Object o = CacheUtil.get(sapphireConnection.getDatabaseId(), "ImageRef", in.getId());
                if (o == null || !(o instanceof ImageRef)) {
                    in.getData(primary, i);
                    DataSet cats = iconData.getDataset("category");
                    if (cats != null) {
                        HashMap<String, String> filtermap = new HashMap<String, String>();
                        filtermap.put("keyid1", im);
                        DataSet catfilt = cats.getFilteredDataSet(filtermap);
                        ArrayList<String> categories = new ArrayList<String>();
                        for (int c = 0; c < catfilt.getRowCount(); ++c) {
                            categories.add(catfilt.getValue(c, "categoryid", ""));
                        }
                        in.categories = categories;
                    }
                    CacheUtil.put(sapphireConnection.getDatabaseId(), "ImageRef", in.getId(), in);
                } else {
                    in = (ImageRef)o;
                }
                outList.add(in);
                ++added;
            }
        }
        return added;
    }

    public static int getImageCount(String categoryid, String searchText, SapphireConnection sapphireConnection) {
        int count = -1;
        QueryProcessor qp = new QueryProcessor(sapphireConnection.getConnectionId());
        try {
            String search;
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT count(imagerefid) FROM imageref");
            StringBuilder where = new StringBuilder();
            ArrayList<String> params = new ArrayList<String>();
            if (categoryid.length() > 0) {
                where.append("imagerefid in (SELECT keyid1 FROM categoryitem WHERE categoryid=").append(safeSQL.addVar(categoryid)).append(" AND sdcid='LV_ImageRef')");
                params.add(categoryid);
            }
            if ((search = searchText.toLowerCase()).length() > 0) {
                if (!(search = StringUtil.replaceAll(search, "*", "%")).endsWith("%")) {
                    search = search + "%";
                }
                if (!search.startsWith("%")) {
                    search = "%" + search;
                }
                if (where.length() > 0) {
                    where.append(" OR ");
                }
                where.append("LOWER(imagerefid) LIKE ").append(safeSQL.addVar(search)).append("");
                where.append(" OR ");
                where.append("LOWER(imagerefdesc) LIKE ").append(safeSQL.addVar(search)).append("");
                params.add(search);
                params.add(search);
            }
            if (where.length() > 0) {
                sql.append(" WHERE ").append((CharSequence)where);
            }
            Logger.logDebug("getImageCount Sql - " + sql.toString());
            count = qp.getPreparedCount(sql.toString(), safeSQL.getValues());
        }
        catch (Exception exception) {
            // empty catch block
        }
        return count;
    }

    public static ArrayList<ImageRef> getImages(int startNo, int maxNo, SapphireConnection sapphireConnection) {
        Object l = CacheUtil.get(sapphireConnection.getDatabaseId(), "ImageRef", LISTCACHE);
        ArrayList outList = l == null || !(l instanceof ArrayList) ? new ArrayList() : (ArrayList)l;
        int count = ImageRef.getImageCount("", "", sapphireConnection);
        if (maxNo < 0) {
            maxNo = count;
            if (outList.size() < count) {
                outList = new ArrayList();
            }
        }
        if (outList.size() < count && outList.size() < startNo + maxNo) {
            int limit = -1;
            try {
                ConfigurationProcessor cp = new ConfigurationProcessor(sapphireConnection.getConnectionId());
                String lim = cp.getSysConfigProperty("RSetQueryLimit");
                if (lim != null && lim.length() > 0) {
                    limit = Integer.parseInt(lim);
                }
            }
            catch (Exception cp) {
                // empty catch block
            }
            if (limit > -1) {
                int added;
                for (int block = 0; block < count; block += added) {
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid("LV_ImageRef");
                    sdiRequest.setQueryOrderBy("imagerefid");
                    sdiRequest.setQueryFrom("imageref");
                    sdiRequest.setRequestItem("primary");
                    sdiRequest.setRequestItem("category");
                    int max = 0;
                    max = maxNo < count ? maxNo : count;
                    int start = block;
                    int end = block + (limit - 1);
                    if (end > max) {
                        end = max;
                    }
                    String sqlwhere = "imagerefid in (SELECT irv.imagerefid FROM imageref, ( SELECT imagerefid, Row_Number () OVER (ORDER BY imagerefid) rnum FROM imageref ) irv WHERE imageref.imagerefid = irv.imagerefid AND irv.rnum BETWEEN " + start + " AND " + end + " )";
                    sdiRequest.setQueryWhere(sqlwhere);
                    sdiRequest.setRetrieveLimit(limit - 1);
                    sdiRequest.setExtendedDataTypes(true);
                    SDIProcessor sdi = new SDIProcessor(sapphireConnection.getConnectionId());
                    SDIData iconData = sdi.getSDIData(sdiRequest);
                    added = ImageRef.addImages(iconData, outList, sapphireConnection);
                    if (added != 0) {
                        continue;
                    }
                    break;
                }
            } else {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_ImageRef");
                sdiRequest.setQueryOrderBy("imagerefid");
                sdiRequest.setQueryFrom("imageref");
                String sqlwhere = "imagerefid in (SELECT irv.imagerefid FROM imageref, ( SELECT imagerefid, Row_Number () OVER (ORDER BY imagerefid) rnum FROM imageref ) irv WHERE imageref.imagerefid = irv.imagerefid AND irv.rnum BETWEEN " + startNo + " AND " + (startNo + maxNo) + " )";
                sdiRequest.setQueryWhere(sqlwhere);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRequestItem("category");
                sdiRequest.setExtendedDataTypes(true);
                SDIProcessor sdi = new SDIProcessor(sapphireConnection.getConnectionId());
                SDIData iconData = sdi.getSDIData(sdiRequest);
                ImageRef.addImages(iconData, outList, sapphireConnection);
            }
            CacheUtil.put(sapphireConnection.getDatabaseId(), "ImageRef", LISTCACHE, outList);
        }
        return outList;
    }

    public static ImageRef getRequestImage(SapphireConnection sapphireConnection, RequestContext requestContext) {
        ImageRef i = new ImageRef(sapphireConnection);
        i.setRequest(requestContext);
        return i;
    }

    public static ImageRef getAttachmentImage(SapphireConnection sapphireConnection, String sdcid, String keyid1, String keyid2, String keyid3, int num) {
        ImageRef i = new ImageRef(sapphireConnection);
        i.setAttachment(sdcid, keyid1, keyid2, keyid3, num);
        return i;
    }

    public static ImageRef getAttachmentThumbnailImage(SapphireConnection sapphireConnection, String sdcid, String keyid1, String keyid2, String keyid3, int num) {
        ImageRef i = new ImageRef(sapphireConnection);
        i.setAttachment(sdcid, keyid1, keyid2, keyid3, num);
        i.attachmentThumbnail = true;
        return i;
    }

    public static ImageRef getSDITempThumbnailImage(SapphireConnection sapphireConnection, String sdcid, String keyid1, String keyid2, String keyid3, String tempid) {
        ImageRef i = new ImageRef(sapphireConnection);
        i.setSDITemp(sdcid, keyid1, keyid2, keyid3, tempid);
        i.attachmentThumbnail = true;
        return i;
    }

    public static ImageRef getSDITempImage(SapphireConnection sapphireConnection, String sdcid, String keyid1, String keyid2, String keyid3, String tempid) {
        ImageRef i = new ImageRef(sapphireConnection);
        i.setSDITemp(sdcid, keyid1, keyid2, keyid3, tempid);
        return i;
    }

    public static ImageRef getURLImage(SapphireConnection sapphireConnection, String url) {
        ImageRef i = new ImageRef(sapphireConnection);
        i.setURL(url);
        return i;
    }

    public static ImageRef getImage(SapphireConnection sapphireConnection, String imageRef) {
        ImageRef i = new ImageRef(sapphireConnection);
        i.setImage(imageRef);
        return i;
    }

    public void setOverlay(String overlay) {
        this.overlay = overlay;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public void setGrayscale(boolean grayscale) {
        this.grayscale = grayscale;
    }

    public void setColor(String color) {
        this.color = color.equalsIgnoreCase("black") || color.equalsIgnoreCase("#000000") ? "Black" : (color.equalsIgnoreCase("white") || color.equalsIgnoreCase("#FFFFFF") ? "White" : color);
    }

    public int getSize() {
        return this.size;
    }

    public void setDimensions(int width, int height) {
        switch (this.filetypeflag) {
            case IMAGE: 
            case URL: 
            case ATTACHMENT: 
            case SDITEMP: {
                this.width = width;
                this.height = height;
                this.size = 0;
                break;
            }
            default: {
                if (width > 0 && width >= height) {
                    if (width <= 16) {
                        this.size = 16;
                    } else if (width > 16 && width <= 32) {
                        this.size = 32;
                    } else if (width > 32) {
                        this.size = 48;
                    }
                } else if (height > 0 && height > width) {
                    if (height <= 16) {
                        this.size = 16;
                    } else if (height > 16 && height <= 32) {
                        this.size = 32;
                    } else if (height > 32) {
                        this.size = 48;
                    }
                }
                this.width = 0;
                this.height = 0;
            }
        }
    }

    public FileType getFileType() {
        return this.filetypeflag;
    }

    public void setNoCache(boolean noCache) {
        this.nocache = noCache;
    }

    public String getSrc() {
        String raw = this.getRawImage();
        if (raw.length() > 0) {
            return raw;
        }
        StringBuffer url = new StringBuffer();
        switch (this.filetypeflag) {
            case ATTACHMENT: {
                if (this.attachmentThumbnail) {
                    url.append(ATTACHMENTTHUMBNAILCOMMAND);
                } else {
                    url.append(ATTACHMENTCOMMAND);
                }
                url.append(this.attachmentSDCId).append(";").append(this.attachmentKeyid1).append(";").append(this.attachmentKeyid2).append(";").append(this.attachmentKeyid3).append(";").append(this.attachmentClass.length() > 0 ? this.attachmentClass : "" + this.attachmentNum);
                if (this.attachmentThumbnailGeneration == null) break;
                url.append("&thumbnailflag=" + this.attachmentThumbnailGeneration.getFlag());
                break;
            }
            case SDITEMP: {
                if (this.attachmentThumbnail) {
                    url.append(SDITEMPTHUMBNAILCOMMAND);
                } else {
                    url.append(SDITEMPCOMMAND);
                }
                url.append(this.attachmentSDCId).append(";").append(this.attachmentKeyid1).append(";").append(this.attachmentKeyid2).append(";").append(this.attachmentKeyid3).append(";").append(this.tempid);
                if (this.attachmentThumbnailGeneration == null) break;
                url.append("&thumbnailflag=" + (Object)((Object)this.attachmentThumbnailGeneration));
                break;
            }
            case URL: {
                url.append(FILECOMMANDURL);
                url.append(this.imageUrl);
                break;
            }
            default: {
                url.append(IMAGECOMMANDURL);
                url.append(this.imageref);
            }
        }
        if (this.attachmentThumbnail || this.nocache) {
            url.append("&nocache=Y");
            Random rand = new Random();
            int n = rand.nextInt(10000) + 1;
            url.append("&rnd=" + n);
        }
        if (this.size > 0) {
            url.append("&size=").append(this.size);
        } else {
            if (this.width > 0) {
                url.append("&width=").append(this.width);
            }
            if (this.height > 0) {
                url.append("&height=").append(this.height);
            }
        }
        if (this.grayscale) {
            url.append("&gray=Y");
        }
        if (this.fliphorizontal) {
            url.append("&fliph=Y");
        }
        if (this.flipvertical) {
            url.append("&flipv=Y");
        }
        if (this.opacity < 1.0f && this.opacity > 0.0f) {
            url.append("&opacity=").append(this.opacity);
        }
        if (this.hue != 0) {
            url.append("&hue=").append(this.hue);
        }
        if (this.overlay.length() > 0) {
            url.append("&overlay=").append(this.overlay);
        }
        if (this.color.length() > 0) {
            url.append("&color=").append(HttpUtil.encodeURIComponent(this.color));
        }
        return url.toString();
    }

    public boolean isCacheable() {
        switch (this.filetypeflag) {
            case ATTACHMENT: 
            case SDITEMP: {
                return false;
            }
        }
        return !this.isRaw();
    }

    protected Object clone() throws CloneNotSupportedException {
        ImageRef ir = new ImageRef(this.sapphireConnection);
        this.copy(this, ir);
        return ir;
    }

    private void copy(ImageRef from, ImageRef to) {
        to.imageref = from.imageref;
        to.sapphireConnection = from.sapphireConnection;
        to.description = from.description;
        to.filetypeflag = from.filetypeflag;
        to.imagetypeflag = from.imagetypeflag;
        switch (from.filetypeflag) {
            case SVG: {
                to.svgcontent = from.svgcontent;
            }
            case BITMAP: {
                to.smallicon = from.smallicon;
                to.largeicon = from.largeicon;
                to.mediumicon = from.mediumicon;
                to.size = from.size;
                break;
            }
            case ICON: {
                to.iconfile = from.iconfile;
                to.size = from.size;
                break;
            }
            case IMAGE: {
                to.imagefile = from.imagefile;
                to.width = from.width;
                to.height = from.height;
                break;
            }
            case URL: {
                to.imageUrl = from.imageUrl;
                to.width = from.width;
                to.height = from.height;
                break;
            }
            case ATTACHMENT: {
                to.attachmentSDCId = from.attachmentSDCId;
                to.attachmentKeyid1 = from.attachmentKeyid1;
                to.attachmentKeyid2 = from.attachmentKeyid2;
                to.attachmentKeyid3 = from.attachmentKeyid3;
                to.attachmentNum = from.attachmentNum;
                to.attachmentClass = from.attachmentClass;
                to.attachmentThumbnail = from.attachmentThumbnail;
                to.attachmentThumbnailGeneration = from.attachmentThumbnailGeneration;
                to.width = from.width;
                to.height = from.height;
                break;
            }
            case SDITEMP: {
                to.attachmentSDCId = from.attachmentSDCId;
                to.attachmentKeyid1 = from.attachmentKeyid1;
                to.attachmentKeyid2 = from.attachmentKeyid2;
                to.attachmentKeyid3 = from.attachmentKeyid3;
                to.attachmentThumbnail = from.attachmentThumbnail;
                to.attachmentThumbnailGeneration = from.attachmentThumbnailGeneration;
                to.tempid = from.tempid;
                to.width = from.width;
                to.height = from.height;
                break;
            }
        }
        if (from.image != null && from.image.length > 0) {
            to.image = Arrays.copyOf(from.image, from.image.length);
        }
        if (from.categories != null) {
            to.categories = (ArrayList)from.categories.clone();
        }
        to.grayscale = from.grayscale;
        to.opacity = from.opacity;
        to.color = from.color;
        to.hue = from.hue;
        to.fliphorizontal = from.fliphorizontal;
        to.flipvertical = from.flipvertical;
    }

    public String getDescription() {
        return this.description;
    }

    public String getImageRefId() {
        return this.imageref;
    }

    public String getId() {
        StringBuffer id = new StringBuffer();
        switch (this.filetypeflag) {
            case ATTACHMENT: {
                id.append(this.attachmentSDCId).append(";").append(this.attachmentKeyid1).append(";").append(this.attachmentKeyid2).append(";").append(this.attachmentKeyid3).append(";").append(this.attachmentClass.length() > 0 ? this.attachmentClass : "" + this.attachmentNum).append(this.attachmentThumbnail ? "T" : "");
                break;
            }
            case SDITEMP: {
                id.append(this.attachmentSDCId).append(";").append(this.attachmentKeyid1).append(";").append(this.attachmentKeyid2).append(";").append(this.attachmentKeyid3).append(";").append(this.tempid).append(this.attachmentThumbnail ? "T" : "");
                break;
            }
            case URL: {
                id.append(this.imageUrl);
                break;
            }
            default: {
                id.append(this.imageref);
            }
        }
        id.append(";").append(this.grayscale ? "Y" : "N").append(";").append(this.flipvertical ? "Y" : "N").append(";").append(this.fliphorizontal ? "Y" : "N").append(this.hue).append(";").append(this.opacity).append(";").append(this.overlay).append(";").append(this.color);
        switch (this.filetypeflag) {
            case IMAGE: 
            case URL: 
            case ATTACHMENT: 
            case SDITEMP: {
                id.append(";").append(this.width).append(";").append(this.height);
                break;
            }
            default: {
                id.append(";").append(this.size);
            }
        }
        return id.toString();
    }

    public boolean isRaw() {
        String raw = this.getRawImage();
        return raw.length() > 0;
    }

    public String getRawImage() {
        switch (this.filetypeflag) {
            case ATTACHMENT: 
            case SDITEMP: {
                return "";
            }
            case ICON: {
                return "";
            }
            case SVG: 
            case BITMAP: {
                if (this.size == 48 && this.largeicon.length() > 0 || this.size == 32 && this.mediumicon.length() > 0 || this.size == 16 && this.smallicon.length() > 0) {
                    if (!(this.grayscale || this.fliphorizontal || this.flipvertical)) {
                        if (this.hue == 0 && (this.opacity >= 1.0f || this.opacity <= 0.0f)) {
                            if (this.overlay.length() == 0) {
                                if (this.color.length() == 0) {
                                    return this.size == 48 ? this.largeicon : (this.size == 32 ? this.mediumicon : this.smallicon);
                                }
                                if (this.color.equalsIgnoreCase("white") || this.color.equalsIgnoreCase("black")) {
                                    String colorfrom;
                                    String s;
                                    String colorto = "_" + this.color.toLowerCase() + "_";
                                    String string = this.size == 48 ? this.largeicon : (s = this.size == 32 ? this.mediumicon : this.smallicon);
                                    if (s.contains("_white_")) {
                                        colorfrom = "_white_";
                                    } else if (this.imageref.contains("_black_")) {
                                        colorfrom = "_black_";
                                    } else {
                                        return "";
                                    }
                                    if (colorfrom.equalsIgnoreCase(colorto)) {
                                        return this.size == 48 ? this.largeicon : (this.size == 32 ? this.mediumicon : this.smallicon);
                                    }
                                    return StringUtil.replaceAll(this.size == 48 ? this.largeicon : (this.size == 32 ? this.mediumicon : this.smallicon), colorfrom, colorto);
                                }
                                return "";
                            }
                            return "";
                        }
                        return "";
                    }
                    return "";
                }
                return "";
            }
        }
        if (this.width > 0 || this.height > 0) {
            return "";
        }
        if (this.grayscale || this.fliphorizontal || this.flipvertical) {
            return "";
        }
        if (this.hue != 0) {
            return "";
        }
        if (this.opacity < 1.0f && this.opacity > 0.0f) {
            return "";
        }
        if (this.overlay.length() > 0) {
            return "";
        }
        return this.filetypeflag == FileType.URL ? this.imageUrl : this.imagefile;
    }

    private InputStream getImageStream(ServletContext servletContext) {
        InputStream imageStream = null;
        switch (this.filetypeflag) {
            case ATTACHMENT: 
            case SDITEMP: {
                AttachmentProcessor arp = new AttachmentProcessor(this.sapphireConnection.getConnectionId());
                sapphire.attachment.Attachment att = null;
                if (this.tempid.length() > 0) {
                    att = this.attachmentThumbnailGeneration != null ? arp.getTempAttachment(this.attachmentSDCId, this.attachmentKeyid1, this.attachmentKeyid2.length() > 0 ? this.attachmentKeyid2 : "(null)", this.attachmentKeyid3.length() > 0 ? this.attachmentKeyid3 : "(null)", this.tempid, this.attachmentThumbnailGeneration.getFlag()) : arp.getTempAttachment(this.attachmentSDCId, this.attachmentKeyid1, this.attachmentKeyid2.length() > 0 ? this.attachmentKeyid2 : "(null)", this.attachmentKeyid3.length() > 0 ? this.attachmentKeyid3 : "(null)", this.tempid);
                } else if (this.attachmentNum > -1 || this.attachmentClass.length() > 0) {
                    Attachment input = (Attachment)Attachment.getAttachment(this.attachmentSDCId, this.attachmentKeyid1, this.attachmentKeyid2, this.attachmentKeyid3);
                    if (this.attachmentClass.length() > 0) {
                        input.setAttachmentClass(this.attachmentClass);
                    } else {
                        input.setAttachmentNum(this.attachmentNum);
                    }
                    if (Attachment.getAttachment(input, new QueryProcessor(this.sapphireConnection.getConnectionId()), this.sapphireConnection.getConnectionId()) != null) {
                        att = this.attachmentThumbnailGeneration != null ? (Attachment)arp.getSDIAttachment(input, this.attachmentThumbnailGeneration) : (Attachment)arp.getSDIAttachment(input, Attachment.ThumbnailGeneration.DISABLED);
                    }
                }
                if (att == null) break;
                if (this.attachmentThumbnail) {
                    boolean thumb;
                    boolean bl = thumb = this.attachmentThumbnailGeneration != null && this.attachmentThumbnailGeneration.canShow() && att.getThumbnailImage() != null && att.getThumbnailImage().length() > 0 && !att.getThumbnailImage().equalsIgnoreCase("data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw==");
                    if (thumb && (this.height <= 32 && this.height > 0 || this.width <= 32 && this.width > 0)) {
                        Attachment.AttachmentType t = att.getAttachmentType();
                        thumb = t == Attachment.AttachmentType.FILE ? FileTypeGroup.getFileTypeGroupByFileName(att.getFilename()) == FileTypeGroup.IMAGE : false;
                    }
                    if (thumb) {
                        FileManager.FileData fileData = new FileManager.FileData(att.getThumbnailImage());
                        imageStream = new ByteArrayInputStream(fileData.getData());
                        break;
                    }
                    String typeflag = att.getType();
                    com.labvantage.sapphire.util.file.FileType fileType = typeflag.equalsIgnoreCase("P") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("TXT", this.sapphireConnection.getConnectionId()) : (typeflag.equalsIgnoreCase("L") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("URL", this.sapphireConnection.getConnectionId()) : (typeflag.equalsIgnoreCase("D") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("UNKNOWN", this.sapphireConnection.getConnectionId()) : (typeflag.equalsIgnoreCase("M") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("RTF", this.sapphireConnection.getConnectionId()) : (att.getFilename() != null && att.getFilename().length() > 0 ? com.labvantage.sapphire.util.file.FileType.getFileTypeByFileName(att.getFilename(), this.sapphireConnection.getConnectionId()) : com.labvantage.sapphire.util.file.FileType.getFileTypeByName("UNKNOWN", this.sapphireConnection.getConnectionId())))));
                    try {
                        ImageRef subimage = (ImageRef)this.clone();
                        subimage.setImage(fileType.getImageRefId(), FileType.SVG);
                        subimage.setDefault(fileType.getImage());
                        subimage.setDimensions(64, 64);
                        imageStream = subimage.getImageStream(servletContext);
                        this.filetypeflag = FileType.SVG;
                    }
                    catch (Exception exception) {}
                    break;
                }
                String typeflag = att.getType();
                com.labvantage.sapphire.util.file.FileType fileType = typeflag.equalsIgnoreCase("P") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("TXT", this.sapphireConnection.getConnectionId()) : (typeflag.equalsIgnoreCase("L") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("URL", this.sapphireConnection.getConnectionId()) : (typeflag.equalsIgnoreCase("D") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("MDB", this.sapphireConnection.getConnectionId()) : (typeflag.equalsIgnoreCase("M") ? com.labvantage.sapphire.util.file.FileType.getFileTypeByName("RTF", this.sapphireConnection.getConnectionId()) : com.labvantage.sapphire.util.file.FileType.getFileType(att.getFilename(), this.sapphireConnection.getConnectionId()))));
                if (fileType.getType() == FileType.NamedType.IMAGE) {
                    if (fileType.getName().equals("SVG")) {
                        imageStream = att.getInputStream();
                        this.filetypeflag = FileType.SVG;
                        break;
                    }
                    imageStream = att.getInputStream();
                    break;
                }
                try {
                    ImageRef subimage = (ImageRef)this.clone();
                    subimage.setImage(fileType.getImageRefId());
                    subimage.setDefault(fileType.getImage());
                    subimage.setDimensions(this.width, this.height);
                    imageStream = subimage.getImageStream(servletContext);
                    this.filetypeflag = subimage.filetypeflag;
                }
                catch (Exception exception) {}
                break;
            }
            case IMAGE: {
                if (this.imagefile.length() <= 0) break;
                imageStream = servletContext != null ? servletContext.getResourceAsStream("/" + this.imagefile) : null;
                break;
            }
            case ICON: {
                if (this.iconfile.length() <= 0) break;
                imageStream = servletContext != null ? servletContext.getResourceAsStream("/" + this.iconfile) : null;
                break;
            }
            case URL: {
                if (this.imageUrl.length() > 0) {
                    imageStream = servletContext != null ? servletContext.getResourceAsStream("/" + this.imageUrl) : null;
                }
            }
            case SVG: {
                if (this.svgcontent.length() > 0) {
                    imageStream = new ByteArrayInputStream(this.svgcontent.getBytes(StandardCharsets.UTF_8));
                    break;
                }
            }
            case BITMAP: {
                String filename = this.getClosestMatchingImageSize();
                if (filename.length() <= 0) break;
                imageStream = servletContext != null ? servletContext.getResourceAsStream("/" + filename) : null;
            }
        }
        return imageStream;
    }

    private String getClosestMatchingImageSize() {
        String filename = this.size >= 40 ? (this.largeicon.length() > 0 ? this.largeicon : (this.mediumicon.length() > 0 ? this.mediumicon : this.smallicon)) : (this.size >= 24 ? (this.mediumicon.length() > 0 ? this.mediumicon : (this.largeicon.length() > 0 ? this.largeicon : this.smallicon)) : (this.smallicon.length() > 0 ? this.smallicon : (this.mediumicon.length() > 0 ? this.mediumicon : this.largeicon)));
        return filename;
    }

    private BufferedImage getSourceImage(InputStream imageStream, int width, int height) throws IOException {
        BufferedImage srcImage = null;
        if (this.filetypeflag == FileType.ICON) {
            List icons = ICODecoder.readExt((InputStream)imageStream);
            boolean i = false;
            for (ICOImage iconImg : icons) {
                if (iconImg.getColourDepth() != 32) continue;
                if (iconImg.getWidth() == width) {
                    srcImage = iconImg.getImage();
                    break;
                }
                if (srcImage == null) {
                    srcImage = iconImg.getImage();
                    continue;
                }
                if (iconImg.getWidth() <= srcImage.getWidth()) continue;
                srcImage = iconImg.getImage();
            }
            if (srcImage == null) {
                for (ICOImage iconImg : icons) {
                    if (iconImg.getWidth() == width) {
                        srcImage = iconImg.getImage();
                        break;
                    }
                    if (srcImage == null) {
                        srcImage = iconImg.getImage();
                        continue;
                    }
                    if (iconImg.getWidth() <= srcImage.getWidth()) continue;
                    srcImage = iconImg.getImage();
                }
            }
        } else if (this.filetypeflag == FileType.SVG) {
            BufferedImage[] imagePointer = new BufferedImage[1];
            try {
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
                Document result = f.createDocument(null, imageStream);
                Element style = result.createElement("style");
                String css = "path {stroke: #006600;fill: #00cc00;}";
                style.setTextContent(css);
                TranscoderInput svgImage = new TranscoderInput(result);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                TranscoderOutput transcoderOutput = new TranscoderOutput((OutputStream)baos);
                PNGTranscoder converter = new PNGTranscoder();
                converter.transcode(svgImage, transcoderOutput);
                imagePointer[0] = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
            }
            catch (Throwable e) {
                Trace.logError("Failed to process SVG Image", e);
            }
            srcImage = imagePointer[0];
        } else {
            srcImage = ImageIO.read(imageStream);
        }
        if (!(srcImage == null || width <= 0 && height <= 0 || srcImage.getWidth() == width && srcImage.getHeight() == height)) {
            if (width == 0) {
                width = (int)Math.round((double)height / (double)srcImage.getHeight() * (double)srcImage.getWidth());
            }
            if (height == 0) {
                height = (int)Math.round((double)width / (double)srcImage.getWidth() * (double)srcImage.getHeight());
            }
            BufferedImage tmpImage = new BufferedImage(width, height, 2);
            tmpImage.getGraphics().drawImage(srcImage.getScaledInstance(width, height, 4), 0, 0, width, height, null);
            srcImage = tmpImage;
        }
        return srcImage;
    }

    public String getSVGContent() {
        return this.svgcontent;
    }

    public static String getSVGXML(String svgname, String color, ServletContext servletContext) {
        String svg = svgname;
        if (!svgname.toLowerCase().endsWith(".svg")) {
            svg = svg + ".svg";
        }
        if (!svg.startsWith("/WEB-CORE/imageref/")) {
            svg = "/WEB-CORE/imageref/flat/48/" + svg;
        }
        String outSVG = "";
        InputStream imageStream = servletContext.getResourceAsStream(svg);
        if (imageStream != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                FileTransferOptions fileTransferOptions = new FileTransferOptions();
                fileTransferOptions.setCloseOutputStream(true);
                fileTransferOptions.setCloseInputStream(true);
                FileTransfer.safeDataTransfer(imageStream, byteArrayOutputStream, fileTransferOptions);
                outSVG = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
            }
            catch (Exception exception) {
                // empty catch block
            }
            outSVG = outSVG.length() > 0 ? ImageRef.adjustSVGXML(outSVG, false, -1.0f, color) : "";
        }
        return outSVG;
    }

    private static String adjustSVGXML(String imageSVG, boolean grayscale, float opacity, String color) {
        String outSVG = imageSVG;
        if (imageSVG.length() > 0) {
            if (grayscale) {
                color = "#777777";
            } else if (color.equalsIgnoreCase("black")) {
                color = "#000000";
            } else if (color.equalsIgnoreCase("white")) {
                color = "#FFFFFF";
            } else if (color.length() == 0) {
                color = "";
            }
            if (color.length() > 0) {
                String keywords_color_regex = "^[a-z]*$";
                String hex_color_regex = "^#[0-9a-f]{3}([0-9a-f]{3})?$";
                String rgb_color_regex = "^rgb\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])%?\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])%?\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])%?\\s*\\)$";
                String rgba_color_regex = "^rgba\\(\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])%?\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])%?\\s*,\\s*(0|[1-9]\\d?|1\\d\\d?|2[0-4]\\d|25[0-5])%?\\s*,\\s*((0.[1-9])|[01])\\s*\\)$";
                String hsl_color_regex = "^hsl\\(\\s*(0|[1-9]\\d?|[12]\\d\\d|3[0-5]\\d)\\s*,\\s*((0|[1-9]\\d?|100)%)\\s*,\\s*((0|[1-9]\\d?|100)%)\\s*\\)$";
                if (!(color.toLowerCase().matches(keywords_color_regex) || color.toLowerCase().matches(hex_color_regex) || color.matches(rgb_color_regex) || color.matches(rgba_color_regex) || color.matches(hsl_color_regex))) {
                    Logger.logWarn("Invalid color " + color + " passed to image command.");
                    color = "";
                }
            }
            if (color.length() > 0 || opacity > -1.0f) {
                try {
                    Document document = DOMUtil.getNewDocument(imageSVG, false);
                    if (document != null) {
                        Element svg = (Element)XPathAPI.selectSingleNode((Node)document, (String)"//svg");
                        boolean fillFound = false;
                        if (svg.hasAttribute("fill") && color.length() > 0) {
                            svg.setAttribute("fill", color);
                            fillFound = true;
                        }
                        if (opacity < 1.0f && opacity > 0.0f) {
                            svg.setAttribute("fill-opacity", "" + opacity);
                        }
                        if (color.length() > 0) {
                            NodeList nodeList = svg.getChildNodes();
                            for (int n = 0; n < nodeList.getLength(); ++n) {
                                Node node = nodeList.item(n);
                                if (node.getNodeType() != 1 || !((Element)node).hasAttribute("fill")) continue;
                                ((Element)node).setAttribute("fill", color);
                                fillFound = true;
                            }
                        }
                        if (!fillFound && color.length() > 0) {
                            svg.setAttribute("fill", color);
                        }
                        outSVG = DOMUtil.toString(document.getDocumentElement());
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        return outSVG;
    }

    public String getSVGXML(ServletContext servletContext) {
        try {
            String imageSVG = "";
            if (this.svgcontent.length() > 0) {
                imageSVG = this.svgcontent;
            } else {
                StringBuffer xml = new StringBuffer();
                String filename = this.getClosestMatchingImageSize();
                if (filename.length() > 0) {
                    String line;
                    URL url = servletContext.getResource("/" + filename);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    while ((line = reader.readLine()) != null) {
                        xml.append(line);
                    }
                    reader.close();
                    imageSVG = xml.toString();
                } else {
                    return null;
                }
            }
            return ImageRef.adjustSVGXML(imageSVG, this.grayscale, this.opacity, this.color);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public byte[] getBytes(ServletContext servletContext) {
        block56: {
            byte[] byArray;
            ImageRef cached;
            boolean store = false;
            if (this.isCacheable()) {
                Object o = CacheUtil.get(this.sapphireConnection.getDatabaseId(), "ImageRef", this.getId());
                if (o == null || !(o instanceof ImageRef)) {
                    store = true;
                    try {
                        cached = (ImageRef)this.clone();
                    }
                    catch (Exception e) {
                        cached = new ImageRef(this.sapphireConnection);
                    }
                } else {
                    cached = (ImageRef)o;
                    if (cached.image != null && cached.image.length > 0) {
                        return cached.image;
                    }
                    store = true;
                }
            } else {
                store = false;
                try {
                    cached = (ImageRef)this.clone();
                }
                catch (Exception e) {
                    cached = new ImageRef(this.sapphireConnection);
                }
            }
            if (cached == null) return null;
            InputStream imageStream = cached.getImageStream(servletContext);
            boolean process = true;
            if (imageStream == null) {
                store = false;
                process = false;
                imageStream = servletContext != null ? servletContext.getResourceAsStream("/" + (this.defaultimage.length() > 0 ? this.defaultimage : DEFAULTIMAGE)) : null;
            }
            if (imageStream == null) return null;
            try {
                BufferedImage dstImage;
                block58: {
                    BufferedImage overlayImage;
                    ImageRef overlayIR;
                    int oy;
                    int ox;
                    boolean ofile;
                    block60: {
                        int olh;
                        int olw;
                        block62: {
                            block61: {
                                block59: {
                                    BufferedImage srcImage = cached.getSourceImage(imageStream, cached.width, cached.height);
                                    if (srcImage == null) break block56;
                                    dstImage = srcImage;
                                    cached.width = srcImage.getWidth();
                                    cached.height = srcImage.getHeight();
                                    if (process) {
                                        AffineTransformOp op;
                                        AffineTransform tx;
                                        if (cached.flipvertical) {
                                            tx = AffineTransform.getScaleInstance(1.0, -1.0);
                                            tx.translate(0.0, -dstImage.getHeight(null));
                                            op = new AffineTransformOp(tx, 1);
                                            dstImage = op.filter(dstImage, null);
                                        }
                                        if (cached.fliphorizontal) {
                                            tx = AffineTransform.getScaleInstance(-1.0, 1.0);
                                            tx.translate(-dstImage.getWidth(null), 0.0);
                                            op = new AffineTransformOp(tx, 1);
                                            dstImage = op.filter(dstImage, null);
                                        }
                                        if (cached.hue != 0) {
                                            HueFilter colorfilter = null;
                                            if (cached.hue > 0 && cached.hue <= 255) {
                                                colorfilter = new HueFilter(cached.hue, 0, 0);
                                            } else if (cached.hue > 255 && cached.hue <= 510) {
                                                colorfilter = new HueFilter(0, cached.hue - 255, 0);
                                            } else if (cached.hue > 510 && cached.hue <= 765) {
                                                colorfilter = new HueFilter(0, 0, cached.hue - 510);
                                            }
                                            if (colorfilter != null) {
                                                FilteredImageSource filteredSrc = new FilteredImageSource(dstImage.getSource(), colorfilter);
                                                Image finalImage = Toolkit.getDefaultToolkit().createImage(filteredSrc);
                                                dstImage = new BufferedImage(cached.width, cached.height, 2);
                                                dstImage.getGraphics().drawImage(finalImage, 0, 0, null);
                                            }
                                        }
                                    }
                                    if (this.overlay.length() <= 0) break block58;
                                    ofile = false;
                                    ox = 0;
                                    oy = 0;
                                    if (!this.overlay.contains("/") && !this.overlay.contains("\\") || !this.overlay.endsWith(".png")) break block59;
                                    overlayIR = ImageRef.getURLImage(this.sapphireConnection, this.overlay);
                                    ofile = true;
                                    break block60;
                                }
                                String[] overlayparts = StringUtil.split(this.overlay, ";");
                                overlayIR = ImageRef.getImage(this.sapphireConnection, overlayparts[0]);
                                olw = 16;
                                olh = 16;
                                ox = 0;
                                oy = 0;
                                if (overlayparts.length <= 1) break block61;
                                try {
                                    olw = Integer.parseInt(overlayparts[1]);
                                    if (olw < 1) {
                                        olw = cached.width;
                                    }
                                }
                                catch (Exception e) {
                                    olw = 16;
                                }
                                try {
                                    olh = Integer.parseInt(overlayparts[1]);
                                    if (olh < 1) {
                                        olh = cached.height;
                                    }
                                }
                                catch (Exception e) {
                                    olh = 16;
                                }
                                ox = cached.width - olw;
                                oy = cached.height - olh;
                                if (overlayparts.length > 2) {
                                    try {
                                        ox = Integer.parseInt(overlayparts[2]);
                                    }
                                    catch (Exception e) {
                                        ox = cached.width - olw;
                                    }
                                    if (overlayparts.length > 3) {
                                        try {
                                            oy = Integer.parseInt(overlayparts[3]);
                                        }
                                        catch (Exception e) {
                                            oy = cached.height - olh;
                                        }
                                    }
                                }
                                break block62;
                            }
                            ox = cached.width - olw;
                            oy = cached.height - olh;
                        }
                        overlayIR.setDimensions(olw, olh);
                    }
                    InputStream overlayStream = overlayIR.getImageStream(servletContext);
                    if (overlayStream != null && (overlayImage = overlayIR.getSourceImage(overlayStream, ofile ? 0 : (process ? cached.width : (this.width > 0 ? this.width : this.size)), ofile ? 0 : (process ? cached.height : (this.height > 0 ? this.height : this.size)))) != null) {
                        if (process) {
                            int oh;
                            int ow;
                            if (ofile) {
                                ox = cached.width / 2 - overlayImage.getWidth() / 2;
                                oy = cached.height / 2 - overlayImage.getHeight() / 2;
                                ow = overlayImage.getWidth();
                                oh = overlayImage.getHeight();
                            } else {
                                ow = overlayIR.getSize();
                                oh = overlayIR.getSize();
                            }
                            dstImage.getGraphics().drawImage(overlayImage, ox, oy, ow, oh, null);
                        } else {
                            dstImage = overlayImage;
                        }
                    }
                }
                if (process) {
                    if (cached.grayscale) {
                        ColorConvertOp cop = new ColorConvertOp(ColorSpace.getInstance(1003), null);
                        BufferedImage tmp = new BufferedImage(cached.width, cached.height, 2);
                        ((Graphics2D)tmp.getGraphics()).drawImage(dstImage, cop, 0, 0);
                        dstImage = tmp;
                    }
                    if (cached.opacity < 1.0f && cached.opacity > 0.0f) {
                        float[] scales = new float[]{1.0f, 1.0f, 1.0f, cached.opacity};
                        float[] offsets = new float[4];
                        RescaleOp rop = new RescaleOp(scales, offsets, null);
                        BufferedImage tmp = new BufferedImage(cached.width, cached.height, 2);
                        ((Graphics2D)tmp.getGraphics()).drawImage(dstImage, rop, 0, 0);
                        dstImage = tmp;
                    }
                }
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream();){
                    ImageIO.write((RenderedImage)dstImage, "png", baos);
                    baos.flush();
                    cached.image = baos.toByteArray();
                }
                if (store) {
                    CacheUtil.put(this.sapphireConnection.getDatabaseId(), "ImageRef", this.getId(), cached);
                }
                byArray = cached.image;
            }
            catch (IOException e1) {
                Logger.logError("Failed to load image.", e1);
                return null;
            }
            catch (Throwable throwable) {
                throw throwable;
            }
            try {
                imageStream.close();
                return byArray;
            }
            catch (Exception e) {
                Logger.logError("Could not close down.", e);
            }
            return byArray;
        }
        return null;
    }

    public static enum FileType {
        BITMAP("B"),
        IMAGE("P"),
        ICON("I"),
        SVG("S"),
        URL("U"),
        ATTACHMENT("A"),
        SDITEMP("T");

        private String t;

        private FileType(String t) {
            this.t = t;
        }

        public static FileType getType(String t) {
            if (t.equalsIgnoreCase("I")) {
                return ICON;
            }
            if (t.equalsIgnoreCase("P")) {
                return IMAGE;
            }
            if (t.equalsIgnoreCase("S")) {
                return SVG;
            }
            return BITMAP;
        }

        public String getType() {
            return this.t;
        }
    }

    public static enum ImageType {
        SYSTEM("S"),
        CORE("C"),
        USER("U");

        private String t;

        private ImageType(String t) {
            this.t = t;
        }

        public static ImageType getType(String t) {
            if (t.equalsIgnoreCase("S")) {
                return SYSTEM;
            }
            if (t.equalsIgnoreCase("C")) {
                return CORE;
            }
            return USER;
        }

        public String getType() {
            return this.t;
        }
    }

    class HueFilter
    extends RGBImageFilter {
        private int red;
        private int green;
        private int blue;

        public HueFilter(int red, int green, int blue) {
            this.canFilterIndexColorModel = true;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public int filterRGB(int x, int y, int rgb) {
            int originalAlpha = (rgb & 0xFF000000) >> 24;
            int originalRed = (rgb & 0xFF0000) >> 16;
            int originalGreen = (rgb & 0xFF00) >> 8;
            int originalBlue = rgb & 0xFF;
            return originalAlpha << 24 | (originalRed += this.red) << 16 | (originalGreen += this.green) << 8 | (originalBlue += this.blue);
        }
    }
}

