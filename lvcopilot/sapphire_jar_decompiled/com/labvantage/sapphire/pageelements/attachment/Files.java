/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.attachment;

import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.attachment.AttachmentRule;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.pageelements.maint.LockedImage;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.attachment.Attachment;
import sapphire.attachment.BaseAttachmentRepository;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SDIList;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Files
extends BaseElement {
    public static String TYPEDISPLAY = "S=File;R=File;U=File;F=File;L=URL;P=Plain Text;D=Linked Ref;M=RichText";
    public static final String PROPERTYHANDLER = "com.labvantage.sapphire.pageelements.attachment.AttachmentPropertyHandler";
    public static final String DATAFIELD = "__attachment_data";
    public static final String POLICYFIELD = "__attachment_policy";
    private boolean allowRepositoryBrowse = false;
    private long maxsize = 10L;
    private int maxattachments = -1;
    private String phrasetype = "";
    private String phraselookup = "";
    private View view = View.LARGEICONS;
    private boolean ajax = false;
    private boolean ajaxCreate = false;
    private DataSet attachment = null;
    private String sdcid = "";
    private String keyid1 = "";
    private String keyid2 = "";
    private String keyid3 = "";
    private boolean viewonly = false;
    private ArrayList<Attachment.AttachmentType> types = new ArrayList();
    private ArrayList<Attachment.AttachmentType> editabletypes = new ArrayList();
    private HashMap<String, ArrayList<Attachment.AttachmentType>> classtypes = new HashMap();
    private ArrayList<String> attachmentclassfilter = new ArrayList();
    private DataSet attachmentclasses = null;
    private BaseAttachmentRepository attachmentRepository = null;
    private boolean upload = false;
    private boolean clipboard = false;
    private boolean takephoto = false;
    private boolean livephoto = false;
    private boolean adddrawing = false;
    private String fileTypes = null;
    private String imageTypes = null;
    private StringBuilder script = null;
    private PropertyList attachmentPolicy = null;
    private boolean validUserForReversioning = true;
    private boolean userHasAttachmentAdminRole = false;
    private boolean manageAttachmentAccess = true;
    private boolean viewAttachmentAccess = true;
    private Attachment.ThumbnailGeneration thumbnailGeneration = Attachment.ThumbnailGeneration.GENERATEANDSTORE;

    public void setAttachmentData(DataSet dataSet) {
        this.attachment = dataSet;
    }

    public DataSet getAttachmentData() {
        return this.attachment;
    }

    public Files() {
    }

    public Files(PageContext pageContext) {
        this.setPageContext(pageContext);
        this.setAjax(true);
    }

    private void setUpProps() {
        if (this.requestContext != null) {
            this.viewonly = this.requestContext.getProperty("mode").equalsIgnoreCase("view");
        }
        if (!this.viewonly && this.element.getProperty("readonly", this.element.getProperty("viewonly", "")).length() > 0) {
            this.viewonly = this.element.getProperty("readonly", "N").equalsIgnoreCase("N") ? this.element.getProperty("viewonly", this.viewonly ? "Y" : "N").equalsIgnoreCase("Y") : true;
        }
        if (this.attachmentPolicy == null) {
            ConfigurationProcessor cp = new ConfigurationProcessor(this.pageContext);
            try {
                this.attachmentPolicy = cp.getPolicy("AttachmentPolicy", "Sapphire Custom");
            }
            catch (Exception e) {
                this.attachmentPolicy = null;
            }
        }
        try {
            this.logger.debug("Attachment Repository TO Set = " + this.element.getProperty("filerepositoryid") + " - " + this.element.getProperty("filerepositorynode"));
            this.attachmentRepository = BaseAttachmentRepository.getRepository(this.element.getProperty("filerepositoryid"), this.element.getProperty("filerepositorynode"), this.getConnectionProcessor().getSapphireConnection());
        }
        catch (Exception e) {
            this.logger.warn(e.getMessage());
            this.attachmentRepository = null;
        }
        this.allowRepositoryBrowse = this.attachmentRepository != null && !this.attachmentRepository.isManaged() && this.attachmentRepository.canBrowseRepository();
        this.logger.debug("Is Managed Repro = " + (this.attachmentRepository != null && this.attachmentRepository.isManaged() ? "Yes" : "No"));
        this.logger.debug("allowRepositoryBrowse = " + (this.allowRepositoryBrowse ? "Yes" : "No"));
        try {
            this.maxsize = FileManager.getUploadMaxFileSizeMB("Sapphire Custom", this.getConnectionId());
        }
        catch (NumberFormatException e) {
            this.logger.warn("Invalid max size provided. Default to 5.");
        }
        try {
            this.maxattachments = this.element.getProperty("maxattachments").length() > 0 ? Integer.parseInt(this.element.getProperty("maxattachments", "-1")) : -1;
        }
        catch (NumberFormatException e) {
            this.logger.warn("Invalid max size provided. Default to 5.");
        }
        this.validUserForReversioning = this.validUserRoleForReversioning();
        this.userHasAttachmentAdminRole = this.checkAttachmentAdminRole();
        this.thumbnailGeneration = Attachment.ThumbnailGeneration.getThumbnailGeneration(this.attachmentPolicy.getProperty("thumbnails", Attachment.ThumbnailGeneration.GENERATEANDSTORE.getTitle()), Attachment.ThumbnailGeneration.GENERATEANDSTORE);
        if (this.viewonly && this.thumbnailGeneration.canStore()) {
            this.thumbnailGeneration = Attachment.ThumbnailGeneration.SHOWIFAVAILABLE;
        }
        this.phraselookup = this.attachmentPolicy != null && this.attachmentPolicy.getPropertyList("phrases") != null ? this.attachmentPolicy.getPropertyList("phrases").getProperty("phraselookup", "") : "";
        this.phrasetype = this.attachmentPolicy != null && this.attachmentPolicy.getPropertyList("phrases") != null ? this.attachmentPolicy.getPropertyList("phrases").getProperty("phrasetype", "") : "";
        PropertyListCollection elementtypes = this.element.getCollectionNotNull("attachmenttypes");
        for (int t = 0; t < elementtypes.size(); ++t) {
            Attachment.AttachmentType eltype = Attachment.AttachmentType.getType(elementtypes.getPropertyList(t).getProperty("type"));
            if (eltype == null) continue;
            boolean editable = elementtypes.getPropertyList(t).getProperty("contenteditable", "Y").equalsIgnoreCase("Y");
            this.types.add(eltype);
            if (!editable || !eltype.isEditable()) continue;
            this.editabletypes.add(eltype);
        }
        PropertyListCollection filterclasses = this.element.getCollection("filterclasses");
        String defaultclass = this.element.getProperty("defaultclass");
        boolean defaultfound = false;
        if (filterclasses != null && filterclasses.size() > 0) {
            for (int i = 0; i < filterclasses.size(); ++i) {
                PropertyList classpl = filterclasses.getPropertyList(i);
                String classname = classpl.getProperty("class", "");
                if (classname.length() <= 0) continue;
                if (defaultclass.length() > 0 && classname.equalsIgnoreCase(defaultclass)) {
                    defaultfound = true;
                }
                this.attachmentclassfilter.add(classname);
            }
        }
        if (defaultclass.length() > 0 && filterclasses != null && filterclasses.size() > 0 && !defaultfound) {
            this.attachmentclassfilter.add(defaultclass);
        }
        this.attachmentclasses = this.getQueryProcessor().getRefTypeDataSet("AttachmentClass");
        if (this.element.getProperty("ajax", "N").equalsIgnoreCase("Y")) {
            this.ajax = true;
        }
    }

    public void setPrimary(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2 != null ? keyid2 : "";
        this.keyid3 = keyid3 != null ? keyid3 : "";
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewonly = viewOnly;
    }

    public boolean getViewOnly() {
        return this.viewonly;
    }

    public void setAjax(boolean ajax) {
        this.ajax = ajax;
    }

    public void setAjaxCreate(boolean ajaxCreate) {
        this.ajaxCreate = ajaxCreate;
    }

    private void setUpData() {
        if (this.attachment == null) {
            if (this.ajax || this.sdiInfo == null) {
                Object[] params;
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT * FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? ");
                if (this.keyid2 != null && this.keyid2.length() > 0) {
                    sql.append("AND keyid2 = ? ");
                    if (this.keyid3 != null && this.keyid3.length() > 0) {
                        sql.append("AND keyid3 = ? ");
                        params = new Object[]{this.sdcid, this.keyid1, this.keyid2, this.keyid3};
                    } else {
                        params = new Object[]{this.sdcid, this.keyid1, this.keyid2};
                    }
                } else {
                    params = new Object[]{this.sdcid, this.keyid1};
                }
                sql.append(" ORDER BY usersequence, attachmentnum");
                this.attachment = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params, true);
            } else {
                LockedImage lockedImageObj;
                String[] keycols = this.sdiInfo.getKeycols();
                this.setPrimary(this.sdiInfo.getSdcid(), this.sdiInfo.getDataSet("primary").getValue(0, keycols[0], ""), keycols.length > 1 ? this.sdiInfo.getDataSet("primary").getValue(0, this.sdiInfo.getKeycols()[1], "") : "", keycols.length > 2 ? this.sdiInfo.getDataSet("primary").getValue(0, this.sdiInfo.getKeycols()[2], "") : "");
                this.attachment = this.sdiInfo.getDataSet("attachment");
                String lockedBy = this.sdiInfo.getDataSet("primary").getValue(0, "__lockedby", "");
                String checkedoutbyuser = this.sdiInfo.getDataSet("primary").getValue(0, "__checkedoutbyuser", "");
                String checkedoutbydepartment = this.sdiInfo.getDataSet("primary").getValue(0, "__checkedoutbydepartment", "");
                if (lockedBy != null && lockedBy.length() > 0) {
                    this.viewonly = true;
                    this.logger.debug("Locked by " + lockedBy + ".");
                }
                if ((lockedImageObj = LockedImage.getLockedImage(lockedBy, checkedoutbyuser, checkedoutbydepartment, this.connectionInfo, this.getTranslationProcessor())).isLocked()) {
                    this.viewonly = true;
                    this.logger.debug("Checked out by " + lockedImageObj.getLockedImage());
                }
            }
            if (this.attachment != null) {
                if (!this.attachment.isValidColumn("__rowstatus")) {
                    this.attachment.addColumn("__rowstatus", 0);
                }
                this.attachment.setValue(-1, "__rowstatus", "S");
                if (!this.attachment.isValidColumn("__tempid")) {
                    this.attachment.addColumn("__tempid", 0);
                }
                if (!this.attachment.isValidColumn("__uploadto")) {
                    this.attachment.addColumn("__uploadto", 0);
                }
                if (!this.attachment.isValidColumn("__markup")) {
                    this.attachment.addColumn("__markup", 0);
                }
                if (!this.attachment.isValidColumn("__rowid")) {
                    this.attachment.addColumn("__rowid", 0);
                }
                if (!this.attachment.isValidColumn("__editable")) {
                    this.attachment.addColumn("__editable", 0);
                }
                if (!this.attachment.isValidColumn("__showlockingdialog")) {
                    this.attachment.addColumn("__showlockingdialog", 0);
                }
                this.attachment.setValue(-1, "__showlockingdialog", "N");
                this.attachment.setValue(-1, "thumbnailimage", "");
                if (this.attachmentPolicy == null) {
                    ConfigurationProcessor cp = new ConfigurationProcessor(this.pageContext);
                    try {
                        this.attachmentPolicy = cp.getPolicy("AttachmentPolicy", "Sapphire Custom");
                    }
                    catch (Exception e) {
                        this.attachmentPolicy = null;
                    }
                }
                for (int i = 0; i < this.attachment.getRowCount(); ++i) {
                    PropertyListCollection plc;
                    Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(this.attachment.getValue(i, "typeflag", ""));
                    if (type == Attachment.AttachmentType.RICHTEXT) {
                        StringBuffer content = new StringBuffer(this.attachment.getClob(i, "attachmentclob", ""));
                        HTMLEditorControl.processImages(content, false, this.getConnectionId());
                        this.attachment.setClob(i, "attachmentclob", content.toString());
                    } else if (type == Attachment.AttachmentType.FILE && this.attachment.getClob(i, "attachmentclob", "").length() > 0) {
                        try {
                            JSONObject job = new JSONObject(this.attachment.getClob(i, "attachmentclob", ""));
                            if (job.has("attributes") && job.getJSONObject("attributes").has("markup")) {
                                this.attachment.setString(i, "__markup", job.getJSONObject("attributes").getJSONObject("markup").toString());
                            }
                            this.attachment.setClob(i, "attachmentclob", "{}");
                        }
                        catch (Exception job) {
                            // empty catch block
                        }
                    }
                    this.attachment.setValue(i, "__rowid", "" + i);
                    AttachmentRule attachmentRule = null;
                    PropertyListCollection propertyListCollection = plc = this.attachmentPolicy != null ? this.attachmentPolicy.getCollection("classes") : null;
                    attachmentRule = type == Attachment.AttachmentType.FILE ? (this.attachment.getLong(i, "attachmentsize") > 0L ? AttachmentRule.evaluateRule(this.attachment.getValue(i, "sourcefilename", ""), this.attachment.getValue(i, "attachmentclass", ""), this.attachment.getLong(i, "attachmentsize"), plc, this.getConfigurationProcessor()) : AttachmentRule.evaluateRule(this.attachment.getValue(i, "sourcefilename", ""), this.attachment.getValue(i, "attachmentclass", ""), plc, this.getConfigurationProcessor())) : AttachmentRule.evaluateRule(this.attachment.getValue(i, "attachmentclass", ""), plc, this.getConfigurationProcessor());
                    if (attachmentRule == null) continue;
                    this.attachment.setValue(i, "__editable", attachmentRule.isContentEditable() ? "Y" : "N");
                }
            }
        }
    }

    private boolean renderAttachmentActions(StringBuffer html, int row, FileTypeGroup fileTypeGroup, boolean allowimageediting, Attachment.AttachmentType type, boolean editableFlag, String rowstatus, int dimension, int x, boolean renderIncludes) {
        boolean out = renderIncludes;
        if (fileTypeGroup == FileTypeGroup.IMAGE && allowimageediting && !this.viewonly && (this.isEditableType(type) && this.isEditableRule(row) && editableFlag || rowstatus.equalsIgnoreCase("I"))) {
            PropertyList menuitem;
            StringBuilder temp = new StringBuilder();
            temp.append("<div class=\"icon_view\" onmouseover=\"$(this).addClass('icon_view_over')\" onmouseout=\"$(this).removeClass('icon_view_over')\">");
            Image viewim = new Image(this.pageContext);
            viewim.setImageId("FlatBlackSmallRenameEllipsis");
            viewim.setDimensions(dimension, dimension);
            viewim.setTitle(this.getTranslationProcessor().translate("More options"));
            temp.append(viewim.getHtml());
            temp.append("</div>");
            MenuGizmo actionsMenu = new MenuGizmo();
            actionsMenu.init();
            actionsMenu.setPageContext(this.pageContext);
            PropertyList menuProps = new PropertyList();
            if (out) {
                out = false;
                menuProps.setProperty("renderincludes", "Y");
            } else {
                menuProps.setProperty("renderincludes", "N");
            }
            menuProps.setProperty("customhtml", temp.toString());
            PropertyListCollection menuitems = new PropertyListCollection();
            if (this.isViewableInline(this.attachment, row, this.getConfigurationProcessor())) {
                menuitem = new PropertyList();
                menuitem.setProperty("imageid", "FlatBlackEye");
                menuitem.setProperty("imagetitle", "View Attachment");
                menuitem.setProperty("link", "javascript:fileElement.view('" + this.elementid + "',fileElement.getAttachmentFromData(" + row + "));");
                menuitems.add(menuitem);
            }
            menuitem = new PropertyList();
            menuitem.setProperty("imageid", "FlatBlackEditBox");
            menuitem.setProperty("imagetitle", "Edit Image");
            menuitem.setProperty("link", "javascript:fileElement.crop('" + this.elementid + "',fileElement.getAttachmentFromData(" + row + "));");
            menuitems.add(menuitem);
            menuitem = new PropertyList();
            menuitem.setProperty("imageid", "FlatBlackDrawPaintbrush");
            menuitem.setProperty("imagetitle", "Draw On Image");
            menuitem.setProperty("link", "javascript:fileElement.paint('" + this.elementid + "',fileElement.getAttachmentFromData(" + row + "));");
            menuitems.add(menuitem);
            if (this.attachment.getValue(row, "attachmentclob", "").length() > 0) {
                menuitem = new PropertyList();
                menuitem.setProperty("imageid", "FlatBlackUndo");
                menuitem.setProperty("imagetitle", "Revert Image");
                menuitem.setProperty("link", "javascript:fileElement.revert('" + this.elementid + "',fileElement.getAttachmentFromData(" + row + "));");
                menuitems.add(menuitem);
            }
            menuProps.setProperty("custommenu", menuitems);
            menuProps.setProperty("menutype", "custom");
            menuProps.setProperty("click", "Y");
            menuProps.setProperty("customy", "-30");
            menuProps.setProperty("customx", "" + x);
            menuProps.setProperty("customclass", "files_attmenu");
            menuProps.setProperty("mouseover", "Y");
            actionsMenu.setElementProperties(menuProps);
            actionsMenu.setElementid("actionsmenu" + row);
            html.append(actionsMenu.getHtml());
            if (this.script != null) {
                this.script.append(actionsMenu.getScript());
            } else {
                html.append("<script>" + actionsMenu.getScript() + "</script>");
            }
        } else if (this.isViewableInline(this.attachment, row, this.getConfigurationProcessor())) {
            html.append("<div class=\"icon_view\" onmouseover=\"$(this).addClass('icon_view_over')\" onmouseout=\"$(this).removeClass('icon_view_over')\" onclick=\"fileElement.view('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(row).append("));\">");
            Image viewim = new Image(this.pageContext);
            viewim.setImageId("FlatBlackEye");
            viewim.setDimensions(dimension, dimension);
            viewim.setTitle(this.getTranslationProcessor().translate("View Attachment"));
            html.append(viewim.getHtml());
            html.append("</div>");
        }
        return out;
    }

    private void renderGridContainer(StringBuffer html) {
        html.append("<div class=\"gridcontainer\" id=\"").append(this.elementid).append("_gridcontainer").append("\" data-sortable=\"" + !this.element.getProperty("allowsort", "Y").equalsIgnoreCase("N") + "\">");
    }

    private boolean renderThumbnailView(StringBuffer html, DataSet attachment) {
        boolean rendered = false;
        boolean allowimageediting = this.element.getProperty("allowimageediting", "Y").equalsIgnoreCase("Y");
        boolean renderIncludes = true;
        int actualRow = 0;
        this.renderGridContainer(html);
        for (int i = 0; i < attachment.getRowCount(); ++i) {
            boolean cont;
            Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachment.getValue(i, "typeflag", ""));
            String attachmentclass = attachment.getValue(i, "attachmentclass", "");
            boolean bl = cont = this.isValidType(type) && this.isValidClass(attachmentclass);
            if (cont) {
                boolean bl2 = cont = !attachment.getValue(i, "__rowstatus", "S").equalsIgnoreCase("D") && !attachment.getValue(i, "__rowstatus", "S").equalsIgnoreCase("X");
            }
            if (!cont) continue;
            String filename = attachment.getValue(i, "filename", "");
            String lfi = filename.toLowerCase();
            String desc = attachment.getValue(i, "attachmentdesc", type == Attachment.AttachmentType.FILE ? filename : this.getTranslationProcessor().translate("No Description"));
            int attachmentnum = 0;
            try {
                attachmentnum = Integer.parseInt(attachment.getValue(i, "attachmentnum", "0"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            String currentSDCId = attachment.getValue(i, "sdcid", "");
            String curretentKeyid1 = attachment.getValue(i, "keyid1", "");
            String curretentKeyid2 = attachment.getValue(i, "keyid2", "");
            String curretentKeyid3 = attachment.getValue(i, "keyid3", "");
            this.renderAttachmentRowHead(html, i, attachmentnum, "icon", actualRow);
            ++actualRow;
            FileTypeGroup fileTypeGroup = FileTypeGroup.getFileTypeGroupByFileName(filename);
            boolean editableFlag = attachment.getValue(i, "editableflag", "Y").equalsIgnoreCase("Y");
            String rowstatus = attachment.getValue(i, "__rowstatus", "S");
            renderIncludes = this.renderAttachmentActions(html, i, fileTypeGroup, allowimageediting, type, this.viewonly ? false : editableFlag, rowstatus, 10, -150, renderIncludes);
            if (fileTypeGroup.isPreviewable() || type == Attachment.AttachmentType.RICHTEXT || type == Attachment.AttachmentType.PLAINTEXT) {
                html.append("<div class=\"icon_prev\">");
                html.append("<div class=\"icon_pad\">");
                String cid = SecurityService.decryptConnectionId(this.getConnectionId());
                String databaseid = SecurityService.getDatabaseId(cid);
                cid = cid.substring(databaseid.length() + 1);
                String[] cparts = StringUtil.split(cid, "-");
                Image image = new Image(this.pageContext);
                if (this.thumbnailGeneration.canShow()) {
                    if (attachmentnum > -1) {
                        if (attachment.getValue(i, "__rowstatus", "").equalsIgnoreCase("U") && attachment.getValue(i, "__tempid", "").length() > 0) {
                            image.setSDITempThumbnail(FileManager.TempFile.SDCTEMP, FileManager.TempFile.CONNECTIONTEMP, cparts.length > 0 ? cparts[0] : "", cparts.length > 1 ? cparts[1] : "", attachment.getValue(i, "__tempid", ""), this.thumbnailGeneration);
                        } else {
                            image.setAttachmentThumbnail(currentSDCId, curretentKeyid1, curretentKeyid2, curretentKeyid3, attachmentnum, this.thumbnailGeneration);
                        }
                    } else if (type == Attachment.AttachmentType.RICHTEXT || type == Attachment.AttachmentType.PLAINTEXT) {
                        image.setImageId(type.getImageId());
                        image.setHeight(64);
                    } else {
                        image.setSDITempThumbnail(FileManager.TempFile.SDCTEMP, FileManager.TempFile.CONNECTIONTEMP, cparts.length > 0 ? cparts[0] : "", cparts.length > 1 ? cparts[1] : "", attachment.getValue(i, "__tempid", ""), this.thumbnailGeneration);
                    }
                } else {
                    if (type == Attachment.AttachmentType.FILE) {
                        FileType fileType = FileType.getFileType(filename, this.getConfigurationProcessor());
                        if (fileType.getImageRefId().length() > 0) {
                            image.setImageId(fileType.getImageRefId());
                        } else {
                            image.setImageId(type.getImageId());
                        }
                    } else {
                        image.setImageId(type.getImageId());
                    }
                    image.setHeight(64);
                }
                image.setTitle(desc + (attachmentnum < 0 ? "(" + this.getTranslationProcessor().translate("Not Yet Saved") + ")" : " (" + attachmentnum + ")"));
                html.append(image.getHtml());
                html.append("</div>");
                html.append("</div>");
            } else {
                html.append("<div class=\"icon_img\">");
                Image image = new Image(this.pageContext);
                if (type == Attachment.AttachmentType.FILE) {
                    FileType fileType = FileType.getFileType(filename, this.getConfigurationProcessor());
                    if (fileType.getImageRefId().length() > 0) {
                        image.setImageId(fileType.getImageRefId());
                    } else {
                        image.setImageId(type.getImageId());
                    }
                } else {
                    image.setImageId(type.getImageId());
                }
                image.setHeight(128);
                image.setTitle(desc + (attachmentnum < 0 ? "(" + this.getTranslationProcessor().translate("Not Yet Saved") + ")" : " (" + attachmentnum + ")"));
                html.append(image.getHtml());
                html.append("</div>");
            }
            html.append("<div class=\"icon_txt\">");
            html.append("").append(this.getShortDesc(desc, this.view));
            html.append("</div>");
            html.append("</div>");
            rendered = true;
        }
        html.append("</div>");
        return rendered;
    }

    private boolean renderIconView(StringBuffer html, DataSet attachment) {
        boolean rendered = false;
        boolean allowimageediting = this.element.getProperty("allowimageediting", "Y").equalsIgnoreCase("Y");
        boolean renderIncludes = true;
        int actualRow = 0;
        this.renderGridContainer(html);
        for (int i = 0; i < attachment.getRowCount(); ++i) {
            boolean cont;
            Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachment.getValue(i, "typeflag", ""));
            String attachmentclass = attachment.getValue(i, "attachmentclass", "");
            boolean bl = cont = this.isValidType(type) && this.isValidClass(attachmentclass);
            if (cont) {
                boolean bl2 = cont = !attachment.getValue(i, "__rowstatus", "S").equalsIgnoreCase("D") && !attachment.getValue(i, "__rowstatus", "S").equalsIgnoreCase("X");
            }
            if (!cont) continue;
            String filename = attachment.getValue(i, "filename", "");
            String lfi = filename.toLowerCase();
            String desc = attachment.getValue(i, "attachmentdesc", type == Attachment.AttachmentType.FILE ? filename : this.getTranslationProcessor().translate("No Description"));
            int attachmentnum = 0;
            try {
                attachmentnum = Integer.parseInt(attachment.getValue(i, "attachmentnum", "0"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            String currentSDCId = attachment.getValue(i, "sdcid", "");
            String curretentKeyid1 = attachment.getValue(i, "keyid1", "");
            String curretentKeyid2 = attachment.getValue(i, "keyid2", "");
            String curretentKeyid3 = attachment.getValue(i, "keyid3", "");
            this.renderAttachmentRowHead(html, i, attachmentnum, "icon", actualRow);
            ++actualRow;
            FileTypeGroup fileTypeGroup = FileTypeGroup.getFileTypeGroupByFileName(filename);
            boolean editableFlag = attachment.getValue(i, "editableflag", "Y").equalsIgnoreCase("Y");
            String rowstatus = attachment.getValue(i, "__rowstatus", "S");
            renderIncludes = this.renderAttachmentActions(html, i, fileTypeGroup, allowimageediting, type, this.viewonly ? false : editableFlag, rowstatus, this.view == View.LARGEICONS ? 10 : 8, this.view == View.LARGEICONS ? -145 : -115, renderIncludes);
            html.append("<div class=\"icon_img\">");
            Image image = new Image(this.pageContext);
            if (type == Attachment.AttachmentType.FILE) {
                FileType fileType = FileType.getFileType(filename, this.getConfigurationProcessor());
                if (fileType.getImageRefId().length() > 0) {
                    image.setImageId(fileType.getImageRefId());
                } else {
                    image.setImageId(type.getImageId());
                }
            } else {
                image.setImageId(type.getImageId());
            }
            image.setWidth(this.view == View.LARGEICONS ? 64 : 32);
            image.setHeight(this.view == View.LARGEICONS ? 64 : 32);
            image.setTitle(desc + (attachmentnum < 0 ? "(" + this.getTranslationProcessor().translate("Not Yet Saved") + ")" : " (" + attachmentnum + ")"));
            html.append(image.getHtml());
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            html.append("").append(this.getShortDesc(desc, this.view));
            html.append("</div>");
            html.append("</div>");
            rendered = true;
        }
        html.append("</div>");
        return rendered;
    }

    private String getShortDesc(String desc, View view) {
        String out = desc;
        if (desc.length() > (view == View.LARGEICONS ? 18 : (view == View.THUMBNAIL ? 18 : 14))) {
            int m = desc.length() % 2;
            String p1 = desc.substring(0, m == 0 ? desc.length() / 2 : desc.length() / 2 + 1);
            String p2 = desc.substring(m == 0 ? desc.length() / 2 + 1 : desc.length() / 2 + 2);
            if (p1.length() > 8) {
                p1 = p1.substring(0, view == View.LARGEICONS ? 8 : (view == View.THUMBNAIL ? 8 : 6));
            }
            if (p2.length() > 8) {
                p2 = p2.substring(p2.length() - (view == View.LARGEICONS ? 8 : (view == View.THUMBNAIL ? 8 : 6)));
            }
            out = p1 + "..." + p2;
        }
        return out;
    }

    private boolean isValidType(Attachment.AttachmentType type) {
        if (type != null) {
            if (this.types.size() == 0) {
                return true;
            }
            return this.types.contains((Object)type);
        }
        return false;
    }

    private boolean isValidClass(String attclass) {
        if (this.attachmentclassfilter.size() == 0) {
            return true;
        }
        return attclass.length() > 0 && this.attachmentclassfilter.contains(attclass);
    }

    private boolean isValidTypeForClass(ArrayList<String> classnames, Attachment.AttachmentType type) {
        for (String c : classnames) {
            if (this.isValidClassType(c, type)) continue;
            return false;
        }
        return true;
    }

    private boolean isValidTypeForClass(String classname, Attachment.AttachmentType type) {
        if (classname.length() > 0) {
            return this.isValidClassType(classname, type);
        }
        boolean out = false;
        if (this.classtypes.size() > 0) {
            for (ArrayList<Attachment.AttachmentType> classtype : this.classtypes.values()) {
                if (classtype != null && classtype.size() > 0) {
                    if (!classtype.contains((Object)type)) continue;
                    out = true;
                    continue;
                }
                out = true;
            }
        } else {
            out = true;
        }
        return out;
    }

    private boolean isValidClassType(String classname, Attachment.AttachmentType type) {
        if (this.classtypes != null && this.classtypes.size() > 0) {
            if (classname.length() > 0) {
                if (this.classtypes.containsKey(classname)) {
                    ArrayList<Attachment.AttachmentType> typelist = this.classtypes.get(classname);
                    if (typelist != null && typelist.size() > 0) {
                        return typelist.contains((Object)type);
                    }
                    return true;
                }
                return false;
            }
            return false;
        }
        return true;
    }

    private boolean isEditableRule(int rownum) {
        if (this.attachment != null) {
            return this.attachment.getValue(rownum, "__editable", "Y").equalsIgnoreCase("Y");
        }
        return false;
    }

    private boolean isEditableType(Attachment.AttachmentType type) {
        if (this.types.size() == 0 && type.isEditable()) {
            return true;
        }
        return this.editabletypes.contains((Object)type) && type.isEditable();
    }

    private boolean renderListView(StringBuffer html, DataSet attachment) {
        boolean rendered = false;
        AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionId());
        int actualRow = 0;
        boolean allowimageediting = this.element.getProperty("allowimageediting", "Y").equalsIgnoreCase("Y");
        this.renderGridContainer(html);
        for (int i = 0; i < attachment.getRowCount(); ++i) {
            boolean cont;
            Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachment.getValue(i, "typeflag", ""));
            String attachmentclass = attachment.getValue(i, "attachmentclass", "");
            boolean bl = cont = this.isValidType(type) && this.isValidClass(attachmentclass);
            if (cont) {
                boolean bl2 = cont = !attachment.getValue(i, "__rowstatus", "S").equalsIgnoreCase("D") && !attachment.getValue(i, "__rowstatus", "S").equalsIgnoreCase("X");
            }
            if (!cont) continue;
            String filename = attachment.getValue(i, "filename", "");
            String lfi = filename.toLowerCase();
            String desc = attachment.getValue(i, "attachmentdesc", type == Attachment.AttachmentType.FILE ? filename : this.getTranslationProcessor().translate("No Description"));
            int attachmentnum = 0;
            try {
                attachmentnum = Integer.parseInt(attachment.getValue(i, "attachmentnum", "0"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            String currentSDCId = attachment.getValue(i, "sdcid", "");
            String curretentKeyid1 = attachment.getValue(i, "keyid1", "");
            String curretentKeyid2 = attachment.getValue(i, "keyid2", "");
            String curretentKeyid3 = attachment.getValue(i, "keyid3", "");
            String curretentTempid = attachment.getValue(i, "__tempid", "");
            this.renderAttachmentRowHead(html, i, attachmentnum, "icon", actualRow);
            ++actualRow;
            long size = 0L;
            FileTypeGroup fileTypeGroup = FileTypeGroup.getFileTypeGroupByFileName(filename);
            html.append("<div class=\"icon_img\">");
            Image image = new Image(this.pageContext);
            if (type == Attachment.AttachmentType.FILE) {
                FileType fileType;
                Attachment att;
                String cid = SecurityService.decryptConnectionId(this.getConnectionId());
                String databaseid = SecurityService.getDatabaseId(cid);
                cid = cid.substring(databaseid.length() + 1);
                String[] cparts = StringUtil.split(cid, "-");
                Attachment attachment2 = curretentTempid.length() > 0 ? ap.getTempAttachment(FileManager.TempFile.SDCTEMP, FileManager.TempFile.CONNECTIONTEMP, cparts.length > 0 ? cparts[0] : "", cparts.length > 1 ? cparts[1] : "", curretentTempid) : (att = ap.getSDIAttachment(this.sdcid, this.keyid1, this.keyid2.length() > 0 ? this.keyid2 : "(null)", this.keyid3.length() > 0 ? this.keyid3 : "(null)", attachmentnum));
                if (att != null) {
                    byte[] data = att.getData();
                    long l = size = data != null ? (long)data.length : 0L;
                }
                if ((fileType = FileType.getFileType(filename, this.getConfigurationProcessor())).getImageRefId().length() > 0) {
                    image.setImageId(fileType.getImageRefId());
                } else {
                    image.setImageId(type.getImageId());
                }
                att = null;
            } else {
                image.setImageId(type.getImageId());
            }
            image.setWidth(16);
            image.setHeight(16);
            image.setTitle(desc + (attachmentnum < 0 ? "(" + this.getTranslationProcessor().translate("Not Yet Saved") + ")" : " (" + attachmentnum + ")"));
            html.append(image.getHtml());
            html.append("</div>");
            html.append("<div class=\"icon_txt\">");
            html.append("").append(SafeHTML.encodeForHTML(desc));
            if (size > 0L) {
                BigDecimal l;
                String s = "";
                if (size < 100L) {
                    s = size + " bytes";
                } else if (size < 1000000L) {
                    l = new BigDecimal((double)size / 1024.0);
                    l = l.setScale(2, 4);
                    s = l.toString() + " KB";
                } else {
                    l = new BigDecimal((double)size / 1048576.0);
                    l = l.setScale(2, 4);
                    s = l.toString() + " MB";
                }
                html.append(" (").append(s).append(")");
            }
            html.append("</div>");
            if (this.isViewableInline(attachment, i, this.getConfigurationProcessor())) {
                html.append("<div class=\"icon_view\" onmouseover=\"$(this).addClass('icon_view_over')\" onmouseout=\"$(this).removeClass('icon_view_over')\" onclick=\"fileElement.view('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                Image viewim = new Image(this.pageContext);
                viewim.setImageId("FlatBlackEye");
                viewim.setDimensions(10, 10);
                viewim.setTitle(this.getTranslationProcessor().translate("View Attachment"));
                html.append(viewim.getHtml());
                html.append("</div>");
            }
            boolean editableFlag = attachment.getValue(i, "editableflag", "Y").equalsIgnoreCase("Y");
            String rowstatus = attachment.getValue(i, "__rowstatus", "S");
            if (fileTypeGroup == FileTypeGroup.IMAGE && allowimageediting && !this.viewonly && (this.isEditableType(type) && this.isEditableRule(i) && editableFlag || rowstatus.equalsIgnoreCase("I"))) {
                html.append("<div class=\"icon_crop\" onmouseover=\"$(this).addClass('icon_crop_over')\" onmouseout=\"$(this).removeClass('icon_crop_over')\" onclick=\"fileElement.crop('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                Image cropim = new Image(this.pageContext);
                cropim.setImageId("FlatBlackEditBox");
                cropim.setDimensions(10, 10);
                cropim.setTitle(this.getTranslationProcessor().translate("Edit Image"));
                html.append(cropim.getHtml());
                html.append("</div>");
                html.append("<div class=\"icon_paint\" onmouseover=\"$(this).addClass('icon_paint_over')\" onmouseout=\"$(this).removeClass('icon_paint_over')\" onclick=\"fileElement.paint('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                Image paintim = new Image(this.pageContext);
                paintim.setImageId("FlatBlackDrawPaintbrush");
                paintim.setDimensions(10, 10);
                paintim.setTitle(this.getTranslationProcessor().translate("Draw On Image"));
                html.append(paintim.getHtml());
                html.append("</div>");
                if (attachment.getValue(i, "attachmentclob", "").length() > 0) {
                    html.append("<div class=\"icon_revert\" onmouseover=\"$(this).addClass('icon_revert_over')\" onmouseout=\"$(this).removeClass('icon_revert_over')\" onclick=\"fileElement.revert('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                    Image revertim = new Image(this.pageContext);
                    revertim.setImageId("FlatBlackUndo");
                    revertim.setDimensions(10, 10);
                    revertim.setTitle(this.getTranslationProcessor().translate("Revert Image"));
                    html.append(revertim.getHtml());
                    html.append("</div>");
                }
            }
            html.append("</div>");
            rendered = true;
        }
        html.append("</div>");
        return rendered;
    }

    private void renderAttachmentRowHead(StringBuffer html, int row, int attachmentnum, String cssclass, int viewableRow) {
        html.append("<div data-attachmentrow=\"").append(row).append("\" data-viewrow=\"").append(viewableRow).append("\" data-attachmentnum=\"").append(attachmentnum).append("\" class=\"").append(cssclass).append("\" onmouseout=\"fileElement.mouseout(event, this,'").append(this.elementid).append("')\" ondblclick=\"fileElement.dblClick(event,this,'").append(this.elementid).append("')\" onclick=\"fileElement.click(event,this,'").append(this.elementid).append("')\" onmouseover=\"fileElement.mouseover(event, this,'").append(this.elementid).append("')\">");
    }

    private boolean renderTableView(StringBuffer html, DataSet attachment) {
        boolean rendered = false;
        String currentUser = this.connectionInfo.getSysuserId();
        html.append("<div class=\"tablecontainer\">");
        html.append("<div class=\"title\">");
        if (this.validUserForReversioning) {
            html.append("<div class=\"title_txt title_noborder\">");
            html.append("&nbsp;");
            html.append("</div>");
        }
        html.append("<div class=\"title_txt title_noborder\">");
        html.append("&nbsp;");
        html.append("</div>");
        PropertyListCollection columns = this.element.getCollection("columns");
        if (!this.element.getProperty("showselect", "Y").equalsIgnoreCase("N")) {
            html.append("<div class=\"title_txt title_noborder\">");
            html.append("<input type=\"checkbox\" id=\"").append(this.elementid).append("_selectall\" onclick=\"fileElement.selectAll('").append(this.elementid).append("', this)\">");
            html.append("</div>");
        }
        for (int c = 0; c < columns.size(); ++c) {
            PropertyList column = columns.getPropertyList(c);
            if (column.getProperty("columnid").length() <= 0) continue;
            html.append("<div class=\"title_txt\">");
            html.append(column.getProperty("title"));
            html.append("</div>");
        }
        html.append("</div>");
        boolean allowimageediting = this.element.getProperty("allowimageediting", "Y").equalsIgnoreCase("Y");
        this.renderGridContainer(html);
        int actualRow = 0;
        for (int i = 0; i < attachment.getRowCount(); ++i) {
            boolean cont;
            Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachment.getValue(i, "typeflag", ""));
            String rowstatus = attachment.getValue(i, "__rowstatus", "S");
            String attachmentclass = attachment.getValue(i, "attachmentclass", "");
            boolean bl = cont = this.isValidType(type) && this.isValidClass(attachmentclass);
            if (cont) {
                boolean bl2 = cont = !rowstatus.equalsIgnoreCase("D") && !rowstatus.equalsIgnoreCase("X");
            }
            if (!cont) continue;
            boolean editableFlag = attachment.getValue(i, "editableflag", "Y").equalsIgnoreCase("Y");
            boolean lockedFlag = attachment.getValue(i, "lockedflag", "N").equalsIgnoreCase("Y");
            String lockedBy = attachment.getValue(i, "lockedby", "");
            boolean userHasUnLockPermission = this.userHasAttachmentAdminRole || lockedBy.equalsIgnoreCase(currentUser);
            String filename = attachment.getValue(i, "filename", "");
            String lfi = filename.toLowerCase();
            String desc = attachment.getValue(i, "attachmentdesc", type == Attachment.AttachmentType.FILE ? filename : this.getTranslationProcessor().translate("No Description"));
            int attachmentnum = 0;
            try {
                attachmentnum = Integer.parseInt(attachment.getValue(i, "attachmentnum", "0"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            String currentSDCId = attachment.getValue(i, "sdcid", "");
            String curretentKeyid1 = attachment.getValue(i, "keyid1", "");
            String curretentKeyid2 = attachment.getValue(i, "keyid2", "");
            String curretentKeyid3 = attachment.getValue(i, "keyid3", "");
            this.renderAttachmentRowHead(html, i, attachmentnum, "icon", actualRow);
            ++actualRow;
            if (this.validUserForReversioning) {
                if (lockedFlag) {
                    if (currentUser.equalsIgnoreCase(lockedBy)) {
                        html.append("<div class=\"icon_lock\">");
                    } else {
                        html.append("<div class=\"icon_btn\" onclick=\"sapphire.notification.send('").append(lockedBy).append("','','Send Message to ").append(lockedBy).append("','Enter message'").append(");\">");
                    }
                    Image image = new Image(this.pageContext);
                    image.setImageSrc("WEB-CORE/elements/images/locked.gif");
                    image.setWidth(16);
                    image.setHeight(16);
                    if (currentUser.equalsIgnoreCase(lockedBy)) {
                        image.setTitle("Locked by Login User");
                    } else {
                        image.setTitle("Locked by " + lockedBy + ". Click to send message");
                    }
                    html.append(image.getHtml());
                    html.append("</div>");
                } else {
                    html.append("<div class=\"icon_lock\">");
                    html.append("&nbsp;");
                    html.append("</div>");
                }
            }
            FileTypeGroup fileTypeGroup = FileTypeGroup.getFileTypeGroupByFileName(filename);
            html.append("<div class=\"icon_img\">");
            Image image = new Image(this.pageContext);
            if (type == Attachment.AttachmentType.FILE) {
                FileType fileType = FileType.getFileType(filename, this.getConfigurationProcessor());
                if (fileType.getImageRefId().length() > 0) {
                    image.setImageId(fileType.getImageRefId());
                } else {
                    image.setImageId(type.getImageId());
                }
            } else {
                image.setImageId(type.getImageId());
            }
            image.setWidth(16);
            image.setHeight(16);
            image.setTitle(desc + (attachmentnum < 0 ? "(" + this.getTranslationProcessor().translate("Not Yet Saved") + ")" : " (" + attachmentnum + ")"));
            html.append(image.getHtml());
            html.append("</div>");
            if (!this.element.getProperty("showselect", "Y").equalsIgnoreCase("N")) {
                html.append("<div class=\"icon_txt\">");
                html.append("<input type=\"checkbox\" class=\"select\" id=\"").append(this.elementid).append("_select").append(i).append("\" data-attachmentnum=\"").append(attachmentnum).append("\">");
                html.append("</div>");
            }
            for (int c = 0; c < columns.size(); ++c) {
                PropertyList column = columns.getPropertyList(c);
                if (column.getProperty("columnid").length() <= 0) continue;
                if (column.getProperty("columnid", "").equalsIgnoreCase("attachmentclass")) {
                    String dv = this.getAttachmentClassDisplayValue(type);
                    if (dv.length() > 0) {
                        if (!this.viewonly && attachmentclass.length() == 0 || this.isEditableRule(i)) {
                            column.setProperty("mode", "dropdownlist");
                            if (this.attachmentclassfilter != null && this.attachmentclassfilter.size() > 0) {
                                dv = dv + ";=__!HIDEDEFAULT!__";
                            }
                        } else {
                            column.setProperty("mode", "readonly");
                        }
                        column.setProperty("displayvalue", dv);
                    } else {
                        column.setProperty("mode", "hidden");
                        column.setProperty("displayvalue", "");
                    }
                }
                html.append("<div class=\"icon_txt\">");
                String value = attachment.getValue(i, column.getProperty("columnid"), "");
                if (column.getProperty("columnid").equalsIgnoreCase("attachmentnum") && attachmentnum < 0) {
                    value = "(auto)";
                }
                EditorStyleField editorStyleField = new EditorStyleField(this.pageContext);
                column.setProperty("extraattributes", "data-columnid=" + column.getProperty("columnid") + ";data-row=" + i);
                if (column.getProperty("pseudocolumn").indexOf("</script>") > -1) {
                    column.setProperty("pseudocolumn", "");
                }
                editorStyleField.setColumn(column.copy());
                if (editorStyleField.getColumn().getProperty("width").length() > 0) {
                    String w = editorStyleField.getColumn().getProperty("width").toLowerCase();
                    editorStyleField.getColumn().setProperty("width", "");
                    if (!w.endsWith("px") || !w.endsWith("%")) {
                        w = w + "px";
                    }
                    editorStyleField.getColumn().setProperty("css", "min-width:" + w + ";");
                }
                editorStyleField.setFieldName(this.elementid + i + '_' + column.getProperty("columnid"));
                editorStyleField.setDatasetname("attachment");
                editorStyleField.setFieldValue(value);
                editorStyleField.setChangeEvent("fileElement.change('" + this.elementid + "',event,this)");
                if (this.viewonly || !editableFlag) {
                    editorStyleField.setReadonly(true);
                }
                html.append(editorStyleField.getHtml());
                html.append("</div>");
            }
            if (this.isViewableInline(attachment, i, this.getConfigurationProcessor())) {
                html.append("<div class=\"icon_btn\" onclick=\"fileElement.view('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                image = new Image(this.pageContext);
                image.setImageId("FlatBlackEye");
                image.setWidth(16);
                image.setHeight(16);
                image.setTitle(this.getTranslationProcessor().translate("View attachment"));
                html.append(image.getHtml());
                html.append("</div>");
            }
            boolean showLockConfirmDialog = this.showLockConfirmDialog(attachment, i);
            html.append("<div class=\"icon_btn\" onclick=\"fileElement.download('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append(")").append(",").append(showLockConfirmDialog).append(");\">");
            image = new Image(this.pageContext);
            image.setImageId("FlatBlackDownload");
            image.setWidth(16);
            image.setHeight(16);
            image.setTitle(this.getTranslationProcessor().translate("Download attachment"));
            html.append(image.getHtml());
            html.append("</div>");
            if (!this.viewonly) {
                if (this.isEditableType(type) && this.isEditableRule(i) && editableFlag && this.validUserForReversioning || rowstatus.equalsIgnoreCase("I")) {
                    if (!lockedFlag || rowstatus.equalsIgnoreCase("I")) {
                        html.append("<div class=\"icon_btn\" onclick=\"fileElement.edit('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                        image = new Image(this.pageContext);
                        image.setImageId("FlatBlackEdit1");
                        image.setWidth(16);
                        image.setHeight(16);
                        image.setTitle(this.getTranslationProcessor().translate("Re-upload or Edit Attachment"));
                        html.append(image.getHtml());
                        html.append("</div>");
                    } else if (userHasUnLockPermission) {
                        html.append("<div class=\"icon_btn\" onclick=\"fileElement.edit('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append(")").append(",true").append(");\">");
                        image = new Image(this.pageContext);
                        image.setImageId("FlatBlackEdit1");
                        image.setWidth(16);
                        image.setHeight(16);
                        image.setTitle(this.getTranslationProcessor().translate("Upload and Unlock Attachment"));
                        html.append(image.getHtml());
                        html.append("</div>");
                        html.append("<div class=\"icon_btn\" onclick=\"fileElement.lockUnlockAttachment('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("),'N','Y');\">");
                        image = new Image(this.pageContext);
                        image.setImageId("FlatBlackUnlock");
                        image.setWidth(16);
                        image.setHeight(16);
                        image.setTitle(this.getTranslationProcessor().translate("Unlock Attachment"));
                        html.append(image.getHtml());
                        html.append("</div>");
                    } else {
                        html.append("<div class=\"icon_btn icon_disabled\">");
                        image = new Image(this.pageContext);
                        image.setImageId("FlatBlackEdit1");
                        image.setWidth(16);
                        image.setHeight(16);
                        image.setColor("#C0C0C0");
                        image.setTitle(this.getTranslationProcessor().translate("User does not have proper role to edit Attachment"));
                        html.append(image.getHtml());
                        html.append("</div>");
                        html.append("<div class=\"icon_btn icon_disabled\">");
                        image = new Image(this.pageContext);
                        image.setImageId("FlatBlackUnlock");
                        image.setWidth(16);
                        image.setHeight(16);
                        image.setColor("#C0C0C0");
                        image.setTitle(this.getTranslationProcessor().translate("User does not have proper role to unlock Attachment"));
                        html.append(image.getHtml());
                        html.append("</div>");
                    }
                } else {
                    html.append("<div class=\"icon_btn icon_disabled\">");
                    image = new Image(this.pageContext);
                    image.setImageId("FlatBlackEdit1");
                    image.setWidth(16);
                    image.setHeight(16);
                    image.setColor("#C0C0C0");
                    image.setTitle(this.getTranslationProcessor().translate("Attachment not editable or User does not have proper role."));
                    html.append(image.getHtml());
                    html.append("</div>");
                }
            }
            if (attachmentnum > -1) {
                boolean enablePromote = !this.viewonly && this.isEditableType(type) && this.isEditableRule(i) && editableFlag && this.validUserForReversioning && !lockedFlag && type != Attachment.AttachmentType.LINKEDREFERENCE;
                html.append("<div class=\"icon_btn\" onclick=\"fileElement.audit('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("),").append(enablePromote).append(");\">");
                image = new Image(this.pageContext);
                image.setImageId("FlatBlackTimerRewind");
                image.setWidth(16);
                image.setHeight(16);
                image.setTitle(this.getTranslationProcessor().translate("View Attachment Versions"));
                html.append(image.getHtml());
                html.append("</div>");
            } else {
                html.append("<div class=\"icon_btn icon_disabled\">");
                image = new Image(this.pageContext);
                image.setImageId("FlatBlackTimerRewind");
                image.setWidth(16);
                image.setHeight(16);
                image.setColor("#C0C0C0");
                image.setTitle(this.getTranslationProcessor().translate("No audit history available"));
                html.append(image.getHtml());
                html.append("</div>");
            }
            if (!this.element.getProperty("allowattributes", "Y").equalsIgnoreCase("N")) {
                if (attachmentnum > -1) {
                    html.append("<div class=\"icon_btn\" onclick=\"fileElement.attributes('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                    image = new Image(this.pageContext);
                    image.setImageId("FlatBlackInformationCircle");
                    image.setWidth(16);
                    image.setHeight(16);
                    image.setTitle(this.getTranslationProcessor().translate("View and Edit Metadata"));
                    html.append(image.getHtml());
                    html.append("</div>");
                } else {
                    html.append("<div class=\"icon_btn icon_disabled\">");
                    image = new Image(this.pageContext);
                    image.setImageId("FlatBlackInformationCircle");
                    image.setWidth(16);
                    image.setHeight(16);
                    image.setColor("#C0C0C0");
                    image.setTitle(this.getTranslationProcessor().translate("Save attachment to view and edit metadata"));
                    html.append(image.getHtml());
                    html.append("</div>");
                }
            }
            if (fileTypeGroup == FileTypeGroup.IMAGE && allowimageediting && !this.viewonly && (this.isEditableType(type) && this.isEditableRule(i) && editableFlag || rowstatus.equalsIgnoreCase("I"))) {
                html.append("<div class=\"icon_btn\" onclick=\"fileElement.crop('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                Image cropim = new Image(this.pageContext);
                cropim.setImageId("FlatBlackEditBox");
                cropim.setWidth(16);
                cropim.setHeight(16);
                cropim.setTitle(this.getTranslationProcessor().translate("Edit Image"));
                html.append(cropim.getHtml());
                html.append("</div>");
                html.append("<div class=\"icon_btn\" onclick=\"fileElement.paint('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                Image paintim = new Image(this.pageContext);
                paintim.setImageId("FlatBlackDrawPaintbrush");
                paintim.setWidth(16);
                paintim.setHeight(16);
                paintim.setTitle(this.getTranslationProcessor().translate("Draw On Image"));
                html.append(paintim.getHtml());
                html.append("</div>");
                if (attachment.getValue(i, "attachmentclob", "").length() > 0) {
                    html.append("<div class=\"icon_revert\" onclick=\"fileElement.revert('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append("));\">");
                    Image revertim = new Image(this.pageContext);
                    revertim.setImageId("FlatBlackUndo");
                    revertim.setDimensions(16, 16);
                    revertim.setTitle(this.getTranslationProcessor().translate("Revert Image"));
                    html.append(revertim.getHtml());
                    html.append("</div>");
                }
            }
            String datahash = attachment.getValue(i, "datahash", "0");
            long dh = 0L;
            try {
                dh = Long.parseLong(datahash);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (this.connectionInfo.getRoleList().length() > 0 && Arrays.asList(StringUtil.split(this.connectionInfo.getRoleList(), ";")).contains("Administrator") && dh == -1L) {
                html.append("<div class=\"icon_btn\" onclick=\"fileElement.resetHash('").append(this.elementid).append("',fileElement.getAttachmentFromData(").append(i).append(")").append(");\">");
                image = new Image(this.pageContext);
                image.setImageId("FlatBlackFingerprint");
                image.setWidth(16);
                image.setHeight(16);
                image.setColor("Red");
                image.setTitle(this.getTranslationProcessor().translate("Reset invalid hash"));
                html.append(image.getHtml());
                html.append("</div>");
            } else if (dh == -1L) {
                html.append("<div class=\"icon_btn\">");
                image = new Image(this.pageContext);
                image.setImageId("FlatBlackFingerprint");
                image.setWidth(16);
                image.setHeight(16);
                image.setColor("Grey");
                image.setTitle(this.getTranslationProcessor().translate("Hash is invalid and file is deemed corrupt. Only administrators can reset."));
                html.append(image.getHtml());
                html.append("</div>");
            }
            html.append("</div>");
            rendered = true;
        }
        html.append("</div>");
        html.append("</div>");
        return rendered;
    }

    private String getAttachmentClassDisplayValue(Attachment.AttachmentType type) {
        StringBuffer output = new StringBuffer();
        if (this.attachmentclasses != null) {
            for (int i = 0; i < this.attachmentclasses.getRowCount(); ++i) {
                String dvalue;
                String refvalueid = this.attachmentclasses.getString(i, "refvalueid");
                if (!this.isValidClass(refvalueid) || type != null && !this.isValidClassType(refvalueid, type)) continue;
                String string = dvalue = this.attachmentclasses.getString(i, "refdisplayvalue") != null && this.attachmentclasses.getString(i, "refdisplayvalue").length() > 0 ? this.attachmentclasses.getString(i, "refdisplayvalue") : this.attachmentclasses.getString(i, "refvalueid");
                if (output.length() > 0) {
                    output.append(";");
                }
                output.append(refvalueid).append("=").append(dvalue);
            }
        }
        return output.toString();
    }

    private void checkColumns() {
        PropertyListCollection columns = this.element.getCollection("columns");
        if (columns != null) {
            PropertyList column;
            PropertyList pl = columns.find("columnid", "attachmentnum");
            if (pl != null) {
                pl.setProperty("mode", "readonly");
                pl.setProperty("class", "attachmentnum");
            }
            if (columns.find("columnid", "attachmentdesc") == null) {
                column = new PropertyList();
                column.setProperty("columnid", "attachmentdesc");
                column.setProperty("title", this.getTranslationProcessor().translate("Description"));
                column.setProperty("mode", "input");
                columns.add(column);
            }
            if ((pl = columns.find("columnid", "attachmentclass")) == null) {
                column = new PropertyList();
                column.setProperty("columnid", "attachmentclass");
                column.setProperty("title", this.getTranslationProcessor().translate("Class"));
                column.setProperty("mode", "dropdownlist");
                column.setProperty("displayvalue", "");
                columns.add(column);
            } else if (!(pl.getProperty("mode", "").equalsIgnoreCase("hidden") && pl.getProperty("mode", "").equalsIgnoreCase("retrievedata") && pl.getProperty("mode", "").equalsIgnoreCase("readonly"))) {
                pl.setProperty("mode", "dropdownlist");
                pl.setProperty("displayvalue", "");
                pl.setProperty("reftypeid", "");
                pl.setProperty("sql", "");
            }
            pl = columns.find("columnid", "typeflag");
            if (pl == null) {
                column = new PropertyList();
                column.setProperty("columnid", "typeflag");
                column.setProperty("title", this.getTranslationProcessor().translate("Type"));
                column.setProperty("mode", "readonly");
                column.setProperty("displayvalue", TYPEDISPLAY);
                columns.add(column);
            } else {
                if (!pl.getProperty("mode", "").equalsIgnoreCase("hidden") || !pl.getProperty("mode", "").equalsIgnoreCase("retrievedata")) {
                    pl.setProperty("mode", "readonly");
                }
                pl.setProperty("displayvalue", TYPEDISPLAY);
            }
        } else {
            columns = new PropertyListCollection();
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "attachmentdesc");
            column.setProperty("title", this.getTranslationProcessor().translate("Description"));
            column.setProperty("mode", "input");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "attachmentclass");
            column.setProperty("title", this.getTranslationProcessor().translate("Class"));
            column.setProperty("mode", "dropdownlist");
            column.setProperty("displayvalue", "");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "typeflag");
            column.setProperty("title", this.getTranslationProcessor().translate("Type"));
            column.setProperty("mode", "readonly");
            column.setProperty("displayvalue", TYPEDISPLAY);
            columns.add(column);
            this.element.setProperty("columns", columns);
        }
    }

    public JSONObject getFileElement() {
        this.checkFileTypes();
        JSONObject out = new JSONObject();
        try {
            out.put("attachmentpolicynode", "Sapphire Custom");
            out.put("maxsize", this.maxsize);
            out.put("clipboardtext", this.isValidType(Attachment.AttachmentType.PLAINTEXT) && this.isValidTypeForClass(this.attachmentclassfilter, Attachment.AttachmentType.PLAINTEXT) ? "Y" : "N");
            out.put("maxattachments", this.maxattachments);
            out.put("livephoto", this.livephoto);
            out.put("filetypes", this.fileTypes);
            out.put("imagetypes", this.imageTypes);
            out.put("imagetypes", this.imageTypes);
            JSONArray array = new JSONArray();
            for (String s : this.attachmentclassfilter) {
                array.put(s);
            }
            out.put("attachmentclass", array);
            String defaultclass = this.element.getProperty("defaultclass", "");
            if (defaultclass.length() == 0 && this.element.getCollectionNotNull("filterclasses").size() > 0) {
                defaultclass = this.element.getCollectionNotNull("filterclasses").getPropertyList(0).getProperty("class", "");
            }
            out.put("defaultclass", defaultclass);
            out.put("viewmode", this.view.toString());
            out.put("phrasetype", this.phraselookup);
            out.put("selection", new JSONArray());
        }
        catch (Exception exception) {
            // empty catch block
        }
        return out;
    }

    public String getScript() {
        StringBuilder html = new StringBuilder();
        if (this.script != null) {
            html.append((CharSequence)this.script);
        }
        return html.toString();
    }

    private String getStartScript() {
        StringBuilder html = new StringBuilder();
        if (!this.ajax) {
            this.checkFileTypes();
            html.append("if (typeof(Dropzone) != 'undefined' ){");
            html.append("Dropzone.autoDiscover = false;");
            html.append("}");
            html.append("fileElement.dZMsg = '").append(this.getTranslationProcessor().translate(this.browser.getGUIMode().hastouch() ? "Touch here to select your files." : "Drag over your files or click to select files.")).append("';");
            html.append("fileElement.cbMsg = '").append(this.getTranslationProcessor().translate(this.browser.getGUIMode().hastouch() ? "Paste your image or text here." : "Paste your image or text here.")).append("';");
            html.append("fileElement.dZPhotoMsg = '").append(this.getTranslationProcessor().translate(this.browser.getGUIMode().hastouch() ? "Touch here to select your photo." : "Drag over your photos or click to select them.")).append("';");
            html.append("fileElement.sdcid = '").append(this.sdcid).append("';");
            html.append("fileElement.keyid1 = '").append(this.keyid1).append("';");
            html.append("fileElement.keyid2 = '").append(this.keyid2).append("';");
            html.append("fileElement.keyid3 = '").append(this.keyid3).append("';");
            html.append("fileElement.viewonly = ").append(this.viewonly).append(";");
            html.append("fileElement.thumbnailFlag = '").append(this.thumbnailGeneration.getFlag()).append("';");
            html.append("fileElement.viewInPopup = ").append(this.getElementProperties().getProperty("viewinpopup", "N").equalsIgnoreCase("Y")).append(";");
        }
        return html.toString();
    }

    private String getScriptAndStyleIncludes() {
        String[] includes;
        StringBuilder html = new StringBuilder();
        String[] stringArray = includes = this.attachmentRepository != null && this.allowRepositoryBrowse ? this.attachmentRepository.getBrowseIncludes() : null;
        if (includes != null) {
            for (int i = 0; i < includes.length; ++i) {
                html.append("<script src=\"").append(includes[i]).append("\"></script>");
            }
        }
        html.append(Files.getScriptAndStyle());
        return html.toString();
    }

    public static String getScriptAndStyle() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/elements/files/scripts/files.js\"></script>");
        html.append("<link href=\"WEB-CORE/elements/files/style/files.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        html.append("<script src=\"WEB-CORE/extscripts/react/react-with-addons.js\"></script>");
        html.append("<script src=\"WEB-CORE/extscripts/literallycanvas/js/literallycanvas.min.js\"></script>");
        html.append("<link href=\"WEB-CORE/extscripts/literallycanvas/css/literallycanvas.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        html.append("<script src=\"WEB-CORE/extscripts/sortable/Sortable.min.js\"></script>");
        return html.toString();
    }

    private void checkFileTypes() {
        if (this.fileTypes == null) {
            this.upload = this.element.getProperty("allowfileupload", "Y").equalsIgnoreCase("Y");
            this.clipboard = this.element.getProperty("pastefromclipboard", "Y").equalsIgnoreCase("Y");
            this.takephoto = this.element.getProperty("takephoto", "Y").equalsIgnoreCase("Y");
            this.livephoto = this.takephoto ? this.element.getProperty("livephoto", "Y").equalsIgnoreCase("Y") : false;
            this.adddrawing = this.element.getProperty("adddrawing", "Y").equalsIgnoreCase("Y");
            this.fileTypes = FileManager.getDefaultExtensions(this.element);
            this.imageTypes = "";
            if (!FileManager.isValidFileType("*.png", this.element)) {
                this.clipboard = false;
                this.livephoto = false;
                this.takephoto = false;
                this.adddrawing = false;
                this.logger.info("Clipboard paste disabled as PNG is not an allowed file type.");
                this.logger.info("Take Photo disabled as PNG is not an allowed file type.");
                this.logger.info("Add Drawing disabled as PNG is not an allowed file type.");
            } else if (!FileManager.isValidFileType("*.jpg", this.element)) {
                this.takephoto = false;
                this.livephoto = false;
                this.logger.info("Take Photo disabled as JPG is not an allowed file type.");
            } else {
                this.imageTypes = "image/jpeg,image/png" + (this.browser.getGUIMode().isTablet() || this.browser.getGUIMode().isPhone() ? ";capture=camera" : "");
            }
        }
    }

    public void evaluateRules() {
        if (this.attachment != null) {
            // empty if block
        }
    }

    @Override
    public String getHtml() {
        this.script = new StringBuilder();
        this.setUpProps();
        this.setUpData();
        this.setUpDepartmentalSecurityProps();
        this.setShowLockingConfirmDialogFlag();
        Object o = ((HttpServletRequest)this.pageContext.getRequest()).getSession().getAttribute("userconfig");
        PropertyList userconfig = o != null ? (PropertyList)o : this.requestContext.getPropertyList("userconfig");
        try {
            this.view = userconfig.getProperty("fileelement_view_" + this.sdcid, "").length() > 0 ? View.valueOf(userconfig.getProperty("fileelement_view_" + this.sdcid, "")) : View.valueOf(this.element.getProperty("view", View.LARGEICONS.toString()).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.checkFileTypes();
        StringBuffer html = new StringBuffer();
        if (this.attachment != null) {
            if (!this.ajax) {
                this.pageContext.getSession().setAttribute("filesajaxhandler_attachmentnum", new HashMap());
                View[] plugins = new PropertyListCollection();
                PropertyList plugin = new PropertyList();
                plugin.setProperty("pluginid", "dropzone");
                plugin.setProperty("css", "Y");
                plugin.setProperty("allowminimized", "Y");
                plugins.add(plugin);
                if (this.clipboard) {
                    plugin = new PropertyList();
                    plugin.setProperty("pluginid", "paste");
                    plugin.setProperty("css", "N");
                    plugin.setProperty("allowminimized", "Y");
                    plugins.add(plugin);
                }
                plugin = new PropertyList();
                plugin.setProperty("pluginid", "cropper");
                plugin.setProperty("css", "Y");
                plugin.setProperty("allowminimized", "Y");
                plugins.add(plugin);
                com.labvantage.sapphire.admin.system.ConfigurationProcessor config = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.pageContext);
                boolean devMode = false;
                try {
                    devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                }
                catch (Exception e) {
                    devMode = false;
                }
                html.append(JavaScriptAPITag.getJQueryAPI(true, false, (PropertyListCollection)plugins, "", devMode, this.pageContext));
                html.append(this.getScriptAndStyleIncludes());
                html.append("<script>");
                html.append(this.getStartScript());
                html.append("fileElement.elements['").append(this.elementid).append("'] = ").append(this.getFileElement().toString()).append(";");
                html.append("fileElement.elements['").append(this.elementid).append("'].properties = sapphire.util.propertyList.create( ").append(this.element.toJSONString()).append(");");
                html.append("fileElement.elements['").append(this.elementid).append("'].selection = new Array();");
                html.append("if (!fileElement.dataLoaded){");
                html.append("fileElement.data = sapphire.util.dataSet.create( ").append(this.attachment.toJSONString()).append(");");
                html.append("fileElement.dataLoaded = true;");
                html.append("}");
                html.append("$().ready( function(){");
                html.append("fileElement.onload('").append(this.elementid).append("');");
                html.append("});");
                html.append("</script>");
            }
            if (!this.ajax || this.ajaxCreate) {
                html.append("<div id=\"").append(this.elementid).append("_viewmode\" class=\"files_viewmode").append(this.browser.isIE() || this.browser.isEdge() ? " files_viewmode_ieedge" : "").append("\">");
                if (!this.element.getProperty("showviewpicker", "Y").equalsIgnoreCase("N")) {
                    for (View curview : View.values()) {
                        html.append("<div onclick=\"fileElement.changeView('").append(this.elementid).append("','").append(curview.toString()).append("');\" class=\"viewicon").append(this.view == curview ? " viewselected" : "").append("\" data-view=\"").append(curview.toString().toLowerCase()).append("\" onmouseover=\"$(this).addClass('viewover');\" onmouseout=\"$(this).removeClass('viewover');\" >");
                        Image image = new Image(this.pageContext);
                        image.setDimensions(16, 16);
                        image.setImageId(curview.image);
                        image.setTitle(curview.name);
                        html.append(image.getHtml());
                        html.append("</div>");
                    }
                }
                html.append("</div>");
                html.append("<div id=\"").append(this.elementid).append("_parent\" class=\"files_parent\">");
                html.append("<div id=\"").append(this.elementid).append("_dropzone\" class=\"dropzone files_dropzone\" style=\"display:none;\">");
                html.append("</div>");
                html.append("<div id=\"").append(this.elementid).append("_paste\" class=\"files_paste\" contenteditable=\"true\" style=\"display:none;\">");
                html.append("</div>");
                html.append("<div id=\"").append(this.elementid).append("_photo\" class=\"files_photo\" style=\"display:none;\">");
                html.append("<video id=\"").append(this.elementid).append("_photopreview\" class=\"files_photopreview\">").append(this.getTranslationProcessor().translate("Video stream not available.")).append("</video>");
                html.append("<div id=\"").append(this.elementid).append("_swapphotoview\" class=\"files_swapphotoview\" alt=\"").append(this.getTranslationProcessor().translate("Swap Camera or View.")).append("\" onclick=\"fileElement.changeCamera('").append(this.elementid).append("');\"></div>");
                html.append("<video id=\"").append(this.elementid).append("_photovideo\" style=\"display:none;\" >").append(this.getTranslationProcessor().translate("Video stream not available.")).append("</video>");
                html.append("<canvas id=\"").append(this.elementid).append("_photocanvas\" style=\"display:none;\"></canvas>");
                html.append("<div id=\"").append(this.elementid).append("_photooutput\" style=\"display:none;\"><img id=\"").append(this.elementid).append("_photoimg\" class=\"files_photoimg\"></div>");
                html.append("</div>");
                html.append("<div id=\"").append(this.elementid).append("_cropper\" class=\"files_cropper\" style=\"display:none;\"><img class=\"files_cropperimg\" id=\"").append(this.elementid).append("_cropperimg\"></div>");
                html.append("<div id=\"").append(this.elementid).append("_painter\" class=\"files_painter\" style=\"display:none;\"></div>");
                html.append("<div id=\"").append(this.elementid).append("_container\" class=\"files_container ").append(this.view.toString().toLowerCase()).append("\">");
            }
            boolean rendered = false;
            this.checkColumns();
            switch (this.view) {
                case LIST: {
                    rendered = this.renderListView(html, this.attachment);
                    break;
                }
                case GRID: {
                    rendered = this.renderTableView(html, this.attachment);
                    break;
                }
                case THUMBNAIL: {
                    rendered = this.renderThumbnailView(html, this.attachment);
                    break;
                }
                default: {
                    rendered = this.renderIconView(html, this.attachment);
                }
            }
            if (!rendered) {
                html.append(this.getTranslationProcessor().translate("No attachments found"));
            }
            if (!this.ajax || this.ajaxCreate) {
                html.append("</div>");
                html.append("</div>");
                if (!this.ajax && !this.ajaxCreate) {
                    html.append("<input type=\"hidden\" name=\"").append("__postpropertyhandler_post_").append(this.elementid).append("\" value=\"").append(PROPERTYHANDLER).append("\"/>");
                    html.append("<input type=\"hidden\" name=\"").append(DATAFIELD).append("\" id=\"").append(DATAFIELD).append("\">");
                    html.append("<input type=\"hidden\" name=\"").append(POLICYFIELD).append("\" id=\"").append(POLICYFIELD).append("\" value=\"").append("Sapphire Custom").append("\">");
                }
                if (!this.element.getProperty("showbuttons", "Y").equalsIgnoreCase("N")) {
                    html.append(this.getButtons());
                }
            }
        }
        if (!this.ajax) {
            html.append("<script>");
            html.append(this.getScript());
            html.append("fileElement.manageAttachmentAccess=" + this.manageAttachmentAccess + ";");
            html.append("fileElement.viewAttachmentAccess=" + this.viewAttachmentAccess + ";");
            html.append("</script>");
        }
        return html.toString();
    }

    public String getButtons() {
        Button btn;
        boolean showbuttondropdown;
        StringBuilder html = new StringBuilder();
        html.append("<span id=\"").append(this.elementid).append("_buttons\">");
        PropertyListCollection buttons = new PropertyListCollection();
        if (!this.viewonly) {
            PropertyList button;
            if (this.isValidType(Attachment.AttachmentType.FILE) && this.isValidTypeForClass(this.attachmentclassfilter, Attachment.AttachmentType.FILE)) {
                if (this.upload) {
                    button = new PropertyList();
                    button.setProperty("id", "btnUpload");
                    button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_upload.svg");
                    button.setProperty("text", this.getTranslationProcessor().translate("Upload"));
                    button.setProperty("title", this.getTranslationProcessor().translate("Upload File"));
                    button.setProperty("action", "fileElement.upload('" + this.elementid + "')");
                    buttons.add(button);
                }
                if (this.takephoto) {
                    button = new PropertyList();
                    button.setProperty("id", "btnPhoto");
                    button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_camera.svg");
                    button.setProperty("text", this.getTranslationProcessor().translate("Photo"));
                    button.setProperty("title", this.getTranslationProcessor().translate("Take Photo"));
                    button.setProperty("action", "fileElement.photo('" + this.elementid + "')");
                    buttons.add(button);
                }
                if (this.allowRepositoryBrowse) {
                    button = new PropertyList();
                    button.setProperty("id", "btnBrowse");
                    button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_upload.svg");
                    String t = this.attachmentRepository.getBrowseButtonText();
                    button.setProperty("text", this.getTranslationProcessor().translate(t.length() > 0 ? t : "Browse Repository"));
                    button.setProperty("title", this.getTranslationProcessor().translate(t.length() > 0 ? t : "Browse Repository"));
                    button.setProperty("action", this.attachmentRepository.getBrowseScript(this.elementid));
                    buttons.add(button);
                }
                if (this.clipboard) {
                    button = new PropertyList();
                    button.setProperty("id", "btnClipboard");
                    button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_clipboard_paper.svg");
                    button.setProperty("text", this.getTranslationProcessor().translate("Clipboard"));
                    button.setProperty("title", this.getTranslationProcessor().translate("Paste From Clipboard"));
                    button.setProperty("action", "fileElement.clipboard('" + this.elementid + "')");
                    buttons.add(button);
                }
                if (this.adddrawing) {
                    button = new PropertyList();
                    button.setProperty("id", "btnDrawing");
                    button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_draw_paintbrush.svg");
                    button.setProperty("text", this.getTranslationProcessor().translate("Drawing"));
                    button.setProperty("title", this.getTranslationProcessor().translate("Add Drawing"));
                    button.setProperty("action", "fileElement.draw('" + this.elementid + "')");
                    buttons.add(button);
                }
            }
            if (this.isValidType(Attachment.AttachmentType.PLAINTEXT) && this.isValidTypeForClass(this.attachmentclassfilter, Attachment.AttachmentType.PLAINTEXT)) {
                button = new PropertyList();
                button.setProperty("id", "btPlainText");
                button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_text1.svg");
                button.setProperty("text", this.getTranslationProcessor().translate("Plain Text"));
                button.setProperty("title", this.getTranslationProcessor().translate("Add Plain Text"));
                button.setProperty("action", "fileElement.addPlainText('" + this.elementid + "')");
                buttons.add(button);
            }
            if (this.isValidType(Attachment.AttachmentType.RICHTEXT) && this.isValidTypeForClass(this.attachmentclassfilter, Attachment.AttachmentType.RICHTEXT)) {
                button = new PropertyList();
                button.setProperty("id", "btRichText");
                button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_markup.svg");
                button.setProperty("text", this.getTranslationProcessor().translate("Rich Text"));
                button.setProperty("title", this.getTranslationProcessor().translate("Add Rich Text"));
                button.setProperty("action", "fileElement.addRichText('" + this.elementid + "')");
                buttons.add(button);
            }
            if (this.isValidType(Attachment.AttachmentType.URL) && this.isValidTypeForClass(this.attachmentclassfilter, Attachment.AttachmentType.URL)) {
                button = new PropertyList();
                button.setProperty("id", "btURL");
                button.setProperty("image", "WEB-CORE/imageref/flat/16/flat_black_chain_link.svg");
                button.setProperty("text", this.getTranslationProcessor().translate("URL"));
                button.setProperty("title", this.getTranslationProcessor().translate("Add URL"));
                button.setProperty("action", "fileElement.addURL('" + this.elementid + "')");
                buttons.add(button);
            }
        }
        int numbuts = (showbuttondropdown = this.element.getProperty("showbuttondropdown", "Y").equalsIgnoreCase("Y")) ? 1 : buttons.size();
        PropertyList buttonprops = new PropertyList();
        if (numbuts != 0) {
            if (buttons.size() > 1 && numbuts > 0) {
                PropertyListCollection menubuttons = new PropertyListCollection();
                buttonprops.setProperty("dropdownbuttons", menubuttons);
                buttonprops.setProperty("isdropdown", this.manageAttachmentAccess ? "Y" : "N");
                int num = buttons.size() - (buttons.size() - numbuts);
                if (num < 1) {
                    num = 1;
                }
                for (int i = num; i < buttons.size(); ++i) {
                    PropertyList b = buttons.getPropertyList(i);
                    PropertyList menubutton = new PropertyList();
                    menubutton.setProperty("id", b.getProperty("id"));
                    menubutton.setProperty("text", b.getProperty("title"));
                    menubutton.setProperty("link", "javascript:" + b.getProperty("action"));
                    menubuttons.add(menubutton);
                }
            }
            if (buttons.size() > 0) {
                for (int i = 0; i < numbuts; ++i) {
                    PropertyList b = buttons.getPropertyList(i);
                    Button btn2 = new Button(this.pageContext);
                    btn2.setId(b.getProperty("id"));
                    btn2.setImg(b.getProperty("image"));
                    if (numbuts == 1 && i == 0) {
                        btn2.setText(b.getProperty("title"));
                    } else {
                        btn2.setText(b.getProperty("text"));
                    }
                    btn2.setAction(b.getProperty("action"));
                    if (i == numbuts - 1 && showbuttondropdown) {
                        btn2.setElementProperties(buttonprops);
                    }
                    btn2.setAppearance("modern");
                    html.append(btn2.getHtml());
                    html.append("&nbsp;");
                }
            }
        }
        if (!this.viewonly && !this.element.getProperty("allowremove", "Y").equalsIgnoreCase("N")) {
            btn = new Button(this.pageContext);
            btn.setId("btnRemove");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_close_remove2.svg");
            btn.setText(this.getTranslationProcessor().translate("Remove"));
            btn.setAction("fileElement.remove('" + this.elementid + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;");
        }
        btn = new Button(this.pageContext);
        btn.setId("btnDownload");
        btn.setImg("WEB-CORE/imageref/flat/16/flat_black_download.svg");
        btn.setText(this.getTranslationProcessor().translate("Download"));
        btn.setAction("fileElement.download('" + this.elementid + "')");
        html.append(btn.getHtml());
        if (!this.element.getProperty("allowattributes", "Y").equalsIgnoreCase("N")) {
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId("btnAttributes");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_information_circle.svg");
            btn.setText(this.getTranslationProcessor().translate("View Metadata"));
            btn.setAction("fileElement.attributes('" + this.elementid + "')");
            html.append(btn.getHtml());
        }
        if (!this.viewonly && !this.element.getProperty("allowsort", "Y").equalsIgnoreCase("N")) {
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId(this.elementid + "_Sort");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_reorder.svg");
            btn.setTip(this.getTranslationProcessor().translate("Start Reordering"));
            btn.setAction("fileElement.sort('" + this.elementid + "')");
            html.append(btn.getHtml());
        }
        if (!this.viewonly && !this.element.getProperty("allowmove", "Y").equalsIgnoreCase("N")) {
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId(this.elementid + "_MoveUp");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_arrow_up.svg");
            btn.setTip(this.getTranslationProcessor().translate("Move Up"));
            btn.setAction("fileElement.move('" + this.elementid + "',-1)");
            btn.setStyle("display:none");
            html.append(btn.getHtml());
            html.append("&nbsp;");
            btn = new Button(this.pageContext);
            btn.setId(this.elementid + "_MoveDown");
            btn.setImg("WEB-CORE/imageref/flat/16/flat_black_arrow_down.svg");
            btn.setTip(this.getTranslationProcessor().translate("Move Down"));
            btn.setAction("fileElement.move('" + this.elementid + "',1)");
            btn.setStyle("display:none");
            html.append(btn.getHtml());
        }
        html.append("</span>");
        html.append("<span id=\"").append(this.elementid).append("_uploadbtns\" style=\"display:none;\">");
        Button cancelbtn = new Button(this.pageContext);
        cancelbtn.setId("btnCancelUpload");
        cancelbtn.setText(this.getTranslationProcessor().translate("Cancel"));
        cancelbtn.setAction("fileElement.cancelUpload('" + this.elementid + "')");
        html.append(cancelbtn.getHtml());
        html.append("</span>");
        html.append("<span id=\"").append(this.elementid).append("_clipboardbtns\" style=\"display:none;\">");
        cancelbtn = new Button(this.pageContext);
        cancelbtn.setId("btnCancelClipboard");
        cancelbtn.setText(this.getTranslationProcessor().translate("Cancel"));
        cancelbtn.setAction("fileElement.cancelClipboard('" + this.elementid + "')");
        html.append(cancelbtn.getHtml());
        html.append("&nbsp;");
        Button uploadbtn = new Button(this.pageContext);
        uploadbtn.setId("btnUploadClipboard");
        uploadbtn.setText(this.getTranslationProcessor().translate("OK"));
        uploadbtn.setAction("fileElement.uploadClipboard('" + this.elementid + "')");
        html.append(uploadbtn.getHtml());
        html.append("</span>");
        html.append("<span id=\"").append(this.elementid).append("_photobtns\" style=\"display:none;\">");
        cancelbtn = new Button(this.pageContext);
        cancelbtn.setId("btn" + this.elementid + "_cancelphoto");
        cancelbtn.setText(this.getTranslationProcessor().translate("Cancel"));
        cancelbtn.setAction("fileElement.cancelPhoto('" + this.elementid + "')");
        html.append(cancelbtn.getHtml());
        html.append("&nbsp;");
        Button takebtn = new Button(this.pageContext);
        takebtn.setId("btn" + this.elementid + "_photobrowse");
        takebtn.setText(this.getTranslationProcessor().translate("Browse"));
        takebtn.setAction("fileElement.browsePhoto('" + this.elementid + "')");
        html.append(takebtn.getHtml());
        html.append("&nbsp;");
        takebtn = new Button(this.pageContext);
        takebtn.setId("btn" + this.elementid + "_take");
        takebtn.setText(this.getTranslationProcessor().translate("Capture"));
        takebtn.setImg("WEB-CORE/imageref/flat/16/flat_black_camera2.svg");
        takebtn.setAction("fileElement.takePhoto('" + this.elementid + "')");
        html.append(takebtn.getHtml());
        takebtn = new Button(this.pageContext);
        takebtn.setId("btn" + this.elementid + "_retake");
        takebtn.setText(this.getTranslationProcessor().translate("Retake"));
        takebtn.setAction("fileElement.retakePhoto('" + this.elementid + "')");
        html.append(takebtn.getHtml());
        html.append("&nbsp;");
        uploadbtn = new Button(this.pageContext);
        uploadbtn.setId("btn" + this.elementid + "_okphoto");
        uploadbtn.setText(this.getTranslationProcessor().translate("OK"));
        uploadbtn.setAction("fileElement.uploadPhoto('" + this.elementid + "')");
        html.append(uploadbtn.getHtml());
        html.append("</span>");
        html.append("<span id=\"").append(this.elementid).append("_cropbtns\" style=\"display:none;\">");
        cancelbtn = new Button(this.pageContext);
        cancelbtn.setId("btn" + this.elementid + "_cancelcrop");
        cancelbtn.setText(this.getTranslationProcessor().translate("Cancel"));
        cancelbtn.setAction("fileElement.cancelCrop('" + this.elementid + "')");
        html.append(cancelbtn.getHtml());
        html.append("&nbsp;");
        Button cropbtn = new Button(this.pageContext);
        cropbtn.setId("btn" + this.elementid + "_crop");
        cropbtn.setText(this.getTranslationProcessor().translate("Crop"));
        cropbtn.setImg("WEB-CORE/imageref/flat/16/flat_black_crop.svg");
        cropbtn.setAction("fileElement.cropImage('" + this.elementid + "')");
        html.append(cropbtn.getHtml());
        html.append("&nbsp;");
        html.append("&nbsp;");
        cropbtn = new Button(this.pageContext);
        cropbtn.setId("btn" + this.elementid + "_rotate");
        cropbtn.setText(this.getTranslationProcessor().translate("Rotate"));
        cropbtn.setImg("WEB-CORE/imageref/flat/16/flat_black_transform_rotate_clockwise.svg");
        cropbtn.setAction("fileElement.rotateImage('" + this.elementid + "')");
        html.append(cropbtn.getHtml());
        html.append("&nbsp;");
        cropbtn = new Button(this.pageContext);
        cropbtn.setId("btn" + this.elementid + "_fliph");
        cropbtn.setText(this.getTranslationProcessor().translate("Flip H"));
        cropbtn.setImg("WEB-CORE/imageref/flat/16/flat_black_transform_flip_horizontal.svg");
        cropbtn.setAction("fileElement.flipImageH('" + this.elementid + "')");
        html.append(cropbtn.getHtml());
        html.append("&nbsp;");
        cropbtn = new Button(this.pageContext);
        cropbtn.setId("btn" + this.elementid + "_flipv");
        cropbtn.setText(this.getTranslationProcessor().translate("Flip V"));
        cropbtn.setImg("WEB-CORE/imageref/flat/16/flat_black_transform_flip_vertical.svg");
        cropbtn.setAction("fileElement.flipImageV('" + this.elementid + "')");
        html.append(cropbtn.getHtml());
        html.append("&nbsp;");
        cropbtn = new Button(this.pageContext);
        cropbtn.setId("btn" + this.elementid + "_okcrop");
        cropbtn.setText(this.getTranslationProcessor().translate("OK"));
        cropbtn.setAction("fileElement.okCrop('" + this.elementid + "')");
        html.append(cropbtn.getHtml());
        html.append("</span>");
        html.append("<span id=\"").append(this.elementid).append("_painterbtns\" style=\"display:none;\">");
        cancelbtn = new Button(this.pageContext);
        cancelbtn.setId("btn" + this.elementid + "_cancelpaint");
        cancelbtn.setText(this.getTranslationProcessor().translate("Cancel"));
        cancelbtn.setAction("fileElement.cancelPaint('" + this.elementid + "')");
        html.append(cancelbtn.getHtml());
        html.append("&nbsp;");
        cropbtn = new Button(this.pageContext);
        cropbtn.setId("btn" + this.elementid + "_okpaint");
        cropbtn.setText(this.getTranslationProcessor().translate("OK"));
        cropbtn.setAction("fileElement.okPaint('" + this.elementid + "')");
        html.append(cropbtn.getHtml());
        html.append("</span>");
        return html.toString();
    }

    private boolean validUserRoleForReversioning() {
        boolean validRole = true;
        String role = this.attachmentPolicy.getProperty("userroleforattachmentreversioning", "");
        if (role.length() > 0) {
            String[] userRolesArr;
            validRole = false;
            String adminRole = this.attachmentPolicy.getProperty("adminroleforattachmentreversioning", "AttachmentAdmin");
            String userRoles = this.connectionInfo.getRoleList();
            for (String userRole : userRolesArr = userRoles.split(";")) {
                if (!userRole.equalsIgnoreCase(role) && !userRole.equalsIgnoreCase(adminRole)) continue;
                validRole = true;
                break;
            }
        }
        return validRole;
    }

    private boolean checkAttachmentAdminRole() {
        String[] userRolesArr;
        boolean hasAdminRole = false;
        String adminRole = this.attachmentPolicy.getProperty("adminroleforattachmentreversioning", "AttachmentAdmin");
        String userRoles = this.connectionInfo.getRoleList();
        for (String userRole : userRolesArr = userRoles.split(";")) {
            if (!userRole.equalsIgnoreCase(adminRole)) continue;
            hasAdminRole = true;
            break;
        }
        return hasAdminRole;
    }

    private void setUpDepartmentalSecurityProps() {
        boolean deptSecurityEnabled = "D".equalsIgnoreCase(this.getSDCProcessor().getProperty(this.sdcid, "accesscontrolledflag"));
        try {
            if (deptSecurityEnabled && this.hasAttachmentOperation()) {
                DAMProcessor damProcessor = new DAMProcessor(this.connectionInfo.getConnectionId());
                SDIList sdiList = damProcessor.checkSDIAccess(this.sdcid, this.keyid1, this.keyid2, this.keyid3, true, "ManageAttachment");
                DataSet accessibleSDIs = sdiList.toDataSet();
                this.manageAttachmentAccess = accessibleSDIs.size() > 0;
                sdiList = damProcessor.checkSDIAccess(this.sdcid, this.keyid1, this.keyid2, this.keyid3, true, "ViewAttachment");
                accessibleSDIs = sdiList.toDataSet();
                this.viewAttachmentAccess = this.manageAttachmentAccess || accessibleSDIs.size() > 0;
            }
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
    }

    private boolean hasAttachmentOperation() {
        boolean hasAttachmentOperation = false;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select operationid from sdcoperation where sdcid = " + safeSQL.addVar(this.sdcid) + " and operationid in ('ManageAttachment','ViewAttachment')";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            hasAttachmentOperation = true;
        }
        return hasAttachmentOperation;
    }

    private boolean showLockConfirmDialog(DataSet attachment, int i) {
        if (this.manageAttachmentAccess) {
            Attachment.AttachmentType type = Attachment.AttachmentType.getTypeByAttachmentTypeFlag(attachment.getValue(i, "typeflag", ""));
            boolean editableFlag = attachment.getValue(i, "editableflag", "Y").equalsIgnoreCase("Y");
            boolean lockedFlag = attachment.getValue(i, "lockedflag", "N").equalsIgnoreCase("Y");
            String rowstatus = attachment.getValue(i, "__rowstatus", "S");
            return this.isEditableType(type) && this.isEditableRule(i) && editableFlag && this.validUserForReversioning && !rowstatus.equalsIgnoreCase("I") && !lockedFlag;
        }
        return false;
    }

    private void setShowLockingConfirmDialogFlag() {
        if (this.attachment != null) {
            for (int i = 0; i < this.attachment.getRowCount(); ++i) {
                String flag = this.showLockConfirmDialog(this.attachment, i) ? "Y" : "N";
                this.attachment.setString(i, "__showlockingdialog", flag);
            }
        }
    }

    private boolean isViewableInline(DataSet attachment, int row, ConfigurationProcessor cp) {
        return !FileManager.isForceDownload(cp);
    }

    public static enum View {
        LIST("List View", "FlatBlackList"),
        GRID("Grid View", "FlatBlackTable"),
        THUMBNAIL("Thumbnails", "FlatBlackPicture"),
        LARGEICONS("Large Icons", "FlatBlackTilesFour"),
        MEDIUMICONS("Medium Icons", "FlatBlackTilesNine");

        String name;
        String image;

        private View(String name, String image) {
            this.name = name;
            this.image = image;
        }
    }
}

