/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFields;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFieldsField;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.controls.Image;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.attachment.Attachment;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Attachment
extends BaseWorksheetItem {
    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        String mode = this.config.getProperty("attachmentmode", "standard");
        String worksheetsource = this.config.getProperty("worksheetmodesource", "Control");
        String sourcesdcid = mode.equals("standard") ? this.config.getProperty("sourcesdcid") : (worksheetsource.equalsIgnoreCase("worksheet") ? "LV_Worksheet" : (worksheetsource.equalsIgnoreCase("section") ? "LV_WorksheetSection" : "LV_WorksheetItem"));
        String source = this.config.getProperty("source", "Control");
        worksheetItemOptions.setSupportsFields(this.config.getProperty("generatefields").equals("Y"));
        worksheetItemOptions.setSupportsDataAvailablity(true);
        if (mode.equals("standard") && sourcesdcid.length() > 0) {
            worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), sourcesdcid);
            worksheetItemOptions.setSupportsQuerySDIs(source.equalsIgnoreCase("query"));
            worksheetItemOptions.setViewOnly(true);
            worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        } else if (mode.equals("worksheet")) {
            worksheetItemOptions.setDefaultSDCId(sourcesdcid);
            worksheetItemOptions.setViewOnly(false);
        }
        worksheetItemOptions.setHasExportHTML(this.config.getProperty("exportimagemode").length() > 0);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/attachment.js");
        worksheetItemIncludes.setJSObjectName("attachmentEditor");
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getViewHTML(true);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getViewHTML(false);
    }

    private String getViewHTML(boolean export) throws SapphireException {
        boolean generateFields;
        boolean table;
        DataSet allAttachments;
        String mode = this.config.getProperty("attachmentmode", "standard");
        String worksheetsource = this.config.getProperty("worksheetmodesource", "Control");
        String sourcesdcid = mode.equals("standard") ? this.config.getProperty("sourcesdcid") : (worksheetsource.equalsIgnoreCase("worksheet") ? "LV_Worksheet" : (worksheetsource.equalsIgnoreCase("section") ? "LV_WorksheetSection" : "LV_WorksheetItem"));
        String source = this.config.getProperty("source", "Control");
        StringBuffer html = new StringBuffer();
        DataSet controlData = null;
        PropertyListCollection columns = null;
        PropertyListCollection operations = null;
        M18NUtil m18NUtil = new M18NUtil(this.getSapphireConnection());
        PropertyList sdcProps = sourcesdcid.length() > 0 ? this.getSDCProcessor().getProperties(sourcesdcid) : null;
        WorksheetItemFields worksheetItemFields = null;
        if (mode.equals("standard") && sourcesdcid.length() > 0 && source.length() > 0) {
            columns = this.config.getCollectionNotNull("columns");
            operations = this.config.getCollectionNotNull("operations");
            PropertyListCollection secondaryColumns = new PropertyListCollection();
            SDIRequest sdiRequest = this.getSDIRequest(source, sourcesdcid, columns, secondaryColumns);
            sdiRequest.setRequestItem("attachment");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            if (sdiData == null) {
                this.worksheetItemOptions.setRequiresConfig(true, "Unable to fetch data for the attachment control - check your definition.");
                return "";
            }
            controlData = sdiData.getDataset("primary");
            allAttachments = sdiData.getDataset("attachment");
            if (controlData == null) {
                this.worksheetItemOptions.setRequiresConfig(true, "Unable to fetch data for the attachment control - check your definition.");
                return "";
            }
            PropertyListCollection sortby = this.config.getCollectionNotNull("sortby");
            String sort = "";
            for (int i = 0; i < sortby.size(); ++i) {
                sort = sort + "," + sortby.getPropertyList(i).getProperty("columnid") + " " + sortby.getPropertyList(i).getProperty("asc_desc");
            }
            if (sort.length() > 0) {
                controlData.sort(sort);
            }
            for (int col = 0; col < columns.size(); ++col) {
                PropertyListCollection sdccolumns;
                PropertyList columnDef;
                PropertyList column = columns.getPropertyList(col);
                String columnid = column.getProperty("columnid");
                String format = column.getProperty("format");
                if (format.length() <= 0) continue;
                boolean isDateOnly = false;
                if (sdcProps != null && (columnDef = (sdccolumns = this.getSDCProcessor().getColumns(sourcesdcid)).getPropertyList(columnid)) != null && "Y".equals(columnDef.getProperty("timezoneindependent"))) {
                    isDateOnly = true;
                }
                if (isDateOnly) {
                    controlData.setDateDisplayFormat(columnid, ElementUtil.getDateFormat("O" + format, true, m18NUtil, m18NUtil.getTimezone()));
                    continue;
                }
                controlData.setDateDisplayFormat(columnid, ElementUtil.getDateFormat("O" + format, true, m18NUtil, m18NUtil.getTimezone()));
            }
        } else if (mode.equals("worksheet") && sourcesdcid.length() > 0) {
            String id;
            String string = sourcesdcid.equals("LV_Worksheet") ? this.getWorksheetId() : (id = sourcesdcid.equals("LV_WorksheetSection") ? this.getWorksheetSectionId() : this.getWorksheetItemId());
            String version = sourcesdcid.equals("LV_Worksheet") ? this.getWorksheetVersionId() : (sourcesdcid.equals("LV_WorksheetSection") ? this.getWorksheetSectionVersionId() : this.getWorksheetItemVersionId());
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getSapphireConnection().getConnectionId());
            PropertyList policy = configProcessor.getPolicy("ELNPolicy", "Sapphire Custom");
            allAttachments = policy != null && policy.getPropertyListNotNull("attachments").getProperty("attachmentclass", "").length() > 0 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiattachment.* FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' AND attachmentclass = ? ORDER BY usersequence, attachmentnum", new Object[]{sourcesdcid, id, version, policy.getPropertyListNotNull("attachments").getProperty("attachmentclass")}) : this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiattachment.* FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' ORDER BY usersequence, attachmentnum", new Object[]{sourcesdcid, id, version});
            for (int i = 0; i < allAttachments.size(); ++i) {
                int attachmentNum = allAttachments.getInt(i, "attachmentnum");
                allAttachments.setString(i, "_fieldname", "Att_" + attachmentNum);
            }
            controlData = null;
        } else {
            this.worksheetItemOptions.setRequiresConfig(true, "SDI Viewer Control requires configuration - click to configure");
            return "";
        }
        allAttachments.sort("usersequence,attachmentnum");
        String attachmentClass = this.config.getProperty("attachmentclass");
        boolean imagesOnly = this.config.getProperty("imagesonly", "N").equals("Y");
        String layoutStyle = this.config.getProperty("style", "A");
        boolean imagemode_full = this.config.getProperty(export ? "exportimagemode" : "imagemode", "R").equals("R");
        boolean imagemode_fixed = this.config.getProperty(export ? "exportimagemode" : "imagemode", "R").equals("F");
        boolean imagemode_thumbnail = this.config.getProperty(export ? "exportimagemode" : "imagemode", "R").equals("T");
        int imageWidth = Attachment.getInt(this.config.getProperty(export ? "exportimagewidth" : "imagewidth"), -1);
        int imageHeight = Attachment.getInt(this.config.getProperty(export ? "exportimageheight" : "imageheight"), -1);
        if (imageHeight == -1 && imageWidth == -1) {
            imageHeight = 300;
        }
        boolean showBorder = this.config.getProperty("showborder", "Y").equals("Y");
        boolean attachmentOnly = layoutStyle.equals("A");
        boolean bl = table = mode.equals("standard") && layoutStyle.equals("T");
        if (table) {
            controlData.addColumn("_display", 0);
        }
        DataSet tableData = new DataSet();
        boolean showDescriptionInTitle = this.config.getProperty("showdescription", "Y").equals("Y");
        boolean bl2 = generateFields = mode.equals("worksheet") && this.config.getProperty("generatefields").equals("Y");
        if (generateFields) {
            worksheetItemFields = this.getWorksheetItemFields();
        }
        if (allAttachments.size() == 0) {
            this.setAvailability("NoData");
            if (this.isTemplate()) {
                html.append("<table>");
                html.append("<tr><td>");
                html.append("<img src=\"WEB-CORE/images/logo/64.png\">");
                html.append("</td></tr>");
                html.append("</table>");
            } else {
                String noSDIMsg = this.config.getProperty("nosdiavailablemessage");
                html.append(noSDIMsg.length() > 0 ? SafeHTML.encodeForHTML(noSDIMsg, true) : this.getTranslationProcessor().translate("No attachments found"));
            }
        } else {
            for (int i = 0; i < (controlData == null ? 1 : controlData.size()); ++i) {
                DataSet attachments;
                HashMap<String, String> filter = new HashMap<String, String>();
                if (controlData != null) {
                    filter.put("keyid1", controlData.getString(i, sdcProps.getProperty("keycolid1")));
                    if (sdcProps.getProperty("keycolid2").length() > 0) {
                        filter.put("keyid2", controlData.getString(i, sdcProps.getProperty("keycolid2")));
                    }
                    if (sdcProps.getProperty("keycolid3").length() > 0) {
                        filter.put("keyid3", controlData.getString(i, sdcProps.getProperty("keycolid3")));
                    }
                }
                if (attachmentClass.length() > 0) {
                    filter.put("attachmentclass", attachmentClass);
                }
                DataSet dataSet = attachments = filter.size() > 0 ? allAttachments.getFilteredDataSet(filter) : allAttachments;
                if (attachments.size() <= 0) continue;
                for (int j = 0; j < attachments.size(); ++j) {
                    Attachment.ThumbnailGeneration thumbnailGeneration;
                    String sdcid = attachments.getValue(j, "sdcid");
                    String keyid1 = attachments.getValue(j, "keyid1");
                    String keyid2 = attachments.getValue(j, "keyid2");
                    String keyid3 = attachments.getValue(j, "keyid3");
                    int attachmentNum = attachments.getInt(j, "attachmentnum");
                    String filename = attachments.getValue(j, "filename");
                    String attachmentDesc = attachments.getValue(j, "attachmentdesc");
                    String fieldname = attachments.getValue(j, "_fieldname");
                    boolean isImage = false;
                    FileType fileType = FileType.getFileType(filename, this.getSapphireConnection().getConnectionId());
                    String mimeType = fileType.getMime();
                    if (filename.length() > 0) {
                        boolean bl3 = isImage = fileType.getType() == FileType.NamedType.IMAGE;
                    }
                    if (imagesOnly && !isImage) continue;
                    StringBuffer imageHTML = new StringBuffer();
                    Image image = new Image();
                    image.setConnectionId(this.getSapphireConnection().getConnectionId());
                    String img = "";
                    String title = "";
                    if (generateFields && fieldname.length() > 0 && worksheetItemFields != null) {
                        WorksheetItemFieldsField field = worksheetItemFields.getField(fieldname);
                        if (field != null) {
                            String fieldStart;
                            String fieldValue = field.getFieldValue(0);
                            String string = fieldStart = fieldValue.indexOf(" -") > 0 ? fieldValue.substring(0, fieldValue.indexOf(" -")) : fieldValue;
                            title = showDescriptionInTitle ? fieldStart + "<br>" + attachmentDesc : fieldStart;
                        } else if (showDescriptionInTitle) {
                            title = attachmentDesc;
                        }
                    } else if (showDescriptionInTitle) {
                        title = attachmentDesc;
                    }
                    ConfigurationProcessor cp = new ConfigurationProcessor(this.getSapphireConnection().getConnectionId());
                    PropertyList attachmentPolicy = null;
                    try {
                        attachmentPolicy = cp.getPolicy("AttachmentPolicy", "Sapphire Custom");
                    }
                    catch (Exception e) {
                        attachmentPolicy = null;
                    }
                    Attachment.ThumbnailGeneration thumbnailGeneration2 = thumbnailGeneration = attachmentPolicy != null ? Attachment.ThumbnailGeneration.getThumbnailGeneration(attachmentPolicy.getProperty("thumbnails", Attachment.ThumbnailGeneration.GENERATEANDSTORE.getTitle()), Attachment.ThumbnailGeneration.GENERATEANDSTORE) : Attachment.ThumbnailGeneration.DONOTSTORE;
                    if (export) {
                        sapphire.attachment.Attachment attachmentPre;
                        AttachmentProcessor ap = new AttachmentProcessor(this.getSapphireConnection().getConnectionId());
                        sapphire.attachment.Attachment attachment = ap.getSDIAttachment(attachmentPre = com.labvantage.sapphire.services.Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, attachmentNum), thumbnailGeneration);
                        if (attachment.getType().equalsIgnoreCase("L")) {
                            img = "<a href= \"" + attachment.getUrl() + "\"><img src=\"WEB-CORE/images/png/WebPages.png\"/></a> ";
                        } else {
                            FileManager.FileData fileData = isImage && !imagemode_thumbnail ? new FileManager.FileData(attachment.getData(), mimeType) : new FileManager.FileData(attachment.getThumbnailImage());
                            if (imagemode_fixed) {
                                image.setStyle("width:" + imageWidth + "px;height:" + imageHeight + "px");
                            }
                            image.setImageSrc(fileData.getDataURL());
                            img = image.getHtml();
                        }
                    } else {
                        if (isImage && !imagemode_thumbnail) {
                            image.setAttachment(sdcid, keyid1, keyid2, keyid3, attachmentNum);
                        } else {
                            image.setAttachmentThumbnail(sdcid, keyid1, keyid2, keyid3, attachmentNum, thumbnailGeneration);
                        }
                        image.setNoCache(true);
                        if (imagemode_fixed) {
                            image.setWidth(imageWidth);
                            image.setHeight(imageHeight);
                        }
                        img = image.getHtml();
                        if (this.config.getProperty("showviewhyperlink").equals("Y")) {
                            img = "<a target=\"_blank\" href=\"rc?command=viewattachment&sdcid=" + sdcid + "&keyid1=" + keyid1 + "&keyid2=" + keyid2 + "&keyid3=" + keyid3 + "&attachmentnum=" + attachmentNum + "\">" + img + "</a>";
                        }
                    }
                    imageHTML.append("<div style=\"display: inline-block;padding:3px;margin:4px;" + (showBorder ? "border: 1px solid #C3DAF9;border-radius:4px" : "") + " \">");
                    imageHTML.append("<table cellspacing=\"0\" cellpadding=\"0\">");
                    imageHTML.append("<tr><td style=\"text-align:center\">");
                    imageHTML.append(img);
                    imageHTML.append("</td></tr>");
                    if (title.length() > 0) {
                        imageHTML.append("<tr><td style=\"text-align:center\">");
                        imageHTML.append(title);
                        imageHTML.append("</td></tr>");
                    }
                    imageHTML.append("</table>");
                    imageHTML.append("</div>");
                    if (attachmentOnly) {
                        html.append(imageHTML);
                        continue;
                    }
                    if (!table) continue;
                    tableData.copyRow(controlData, i, 1);
                    int maxRow = tableData.size() - 1;
                    tableData.setValue(maxRow, "_display", imageHTML.toString());
                }
            }
            if (table) {
                PropertyList column = new PropertyList();
                column.setProperty("columnid", "_display");
                column.setProperty("title", "");
                column.setProperty("_skipsanitize", "Y");
                columns.add(column);
                HashSet<String> skipColumns = new HashSet<String>();
                html.append(this.getTableHTML(export, columns, skipColumns, operations, tableData, sdcProps.getProperty("plural"), sdcProps.getProperty("keycolid1"), sdcProps.getProperty("keycolid2"), sdcProps.getProperty("keycolid3")));
            }
        }
        return html.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        String id;
        String sourcesdcid;
        String mode = this.config.getProperty("attachmentmode", "standard");
        String worksheetsource = this.config.getProperty("worksheetmodesource", "Control");
        String string = mode.equals("standard") ? this.config.getProperty("sourcesdcid") : (worksheetsource.equalsIgnoreCase("worksheet") ? "LV_Worksheet" : (sourcesdcid = worksheetsource.equalsIgnoreCase("section") ? "LV_WorksheetSection" : "LV_WorksheetItem"));
        if (mode.equals("standard") && sourcesdcid.length() > 0) {
            return this.getViewHTML();
        }
        String string2 = sourcesdcid.equals("LV_Worksheet") ? this.getWorksheetId() : (id = sourcesdcid.equals("LV_WorksheetSection") ? this.getWorksheetSectionId() : this.getWorksheetItemId());
        String version = sourcesdcid.equals("LV_Worksheet") ? this.getWorksheetVersionId() : (sourcesdcid.equals("LV_WorksheetSection") ? this.getWorksheetSectionVersionId() : this.getWorksheetItemVersionId());
        StringBuilder html = new StringBuilder();
        String elId = this.getElementId();
        html.append("<iframe style=\"border: solid 1px #C3D4D3;background: white;width: 100%;height: 450px;\"name=\"").append(elId).append("_frame\" id=\"").append(elId).append("_frame\">");
        html.append("</iframe>");
        String usePage = this.config.getProperty("usepageproperties", "AttachmentEmbedded");
        html.append("<form method=\"POST\" name=\"").append(elId).append("_form\" id=\"").append(elId).append("_form\" action=\"rc?command=page&page=" + usePage + "\" target=\"").append(elId).append("_frame\">");
        html.append("<input name=\"csrftoken\" type=\"hidden\" value=\"").append("").append("\">");
        html.append("<input name=\"sdcid\" type=\"hidden\" value=\"").append(sourcesdcid).append("\">");
        html.append("<input name=\"keyid1\" type=\"hidden\" value=\"").append(id).append("\">");
        html.append("<input name=\"keyid2\" type=\"hidden\" value=\"").append(version).append("\">");
        html.append("<input name=\"candelete\" type=\"hidden\" value=\"Y\">");
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getSapphireConnection().getConnectionId());
        PropertyList options = Worksheet.getOptions(this.getQueryProcessor(), this.getWorksheetId(), this.getWorksheetVersionId());
        PropertyList policy = configProcessor.getPolicy("ELNPolicy", options != null ? options.getProperty("worksheetpolicynode", "Sapphire Custom") : "Sapphire Custom");
        String filerepositoryid = "";
        String filerepositorynode = "";
        String attachmentclass = "";
        if (policy != null && policy.containsKey("attachments")) {
            PropertyList attachments = policy.getPropertyList("attachments");
            filerepositoryid = attachments.getProperty("filerepositoryid", "");
            filerepositorynode = attachments.getProperty("filerepositorynode", "");
            attachmentclass = attachments.getProperty("attachmentclass", "");
        }
        html.append("<input name=\"desceditable\" type=\"hidden\" value=\"Y\">");
        html.append("<input name=\"filerepositoryid\" type=\"hidden\" value=\"").append(filerepositoryid).append("\">");
        html.append("<input name=\"filerepositorynode\" type=\"hidden\" value=\"").append(filerepositorynode).append("\">");
        html.append("<input name=\"attachmentclass\" type=\"hidden\" value=\"").append(attachmentclass).append("\">");
        html.append("</form>");
        return html.toString();
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        block8: {
            String fieldPrefix = this.config.getProperty("fieldprefix", "Figure");
            if (contents.length() > 0) {
                String mode = this.config.getProperty("attachmentmode", "standard");
                String worksheetsource = this.config.getProperty("worksheetmodesource", "Control");
                String sourcesdcid = mode.equals("standard") ? this.config.getProperty("sourcesdcid") : (worksheetsource.equalsIgnoreCase("worksheet") ? "LV_Worksheet" : (worksheetsource.equalsIgnoreCase("section") ? "LV_WorksheetSection" : "LV_WorksheetItem"));
                DataSet attachmentdata = null;
                try {
                    attachmentdata = new DataSet(new JSONObject(contents));
                    attachmentdata.sort("usersequence,attachmentnum");
                    FileManager.saveAttachmentData(sourcesdcid, attachmentdata, this.getSapphireConnection().getConnectionId());
                    PropertyList config = this.getConfig();
                    boolean generateFields = config.getProperty("generatefields").equals("Y");
                    if (generateFields) {
                        HashMap<String, Integer> counters = new HashMap<String, Integer>();
                        SafeSQL safeSQL = new SafeSQL();
                        String sql = "SELECT max( fieldcontext ) maxvalue FROM worksheetitemfield wsif, worksheetitem wsi WHERE wsif.worksheetitemid=wsi.worksheetitemid AND wsif.worksheetitemversionid=wsi.worksheetitemversionid AND wsi.worksheetid =" + safeSQL.addVar(this.getWorksheetId()) + " AND wsi.worksheetversionid=" + safeSQL.addVar(this.getWorksheetVersionId()) + " AND wsif.fieldcontext like '" + fieldPrefix + ";%'";
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        if (ds.size() == 1) {
                            try {
                                String value = ds.getValue(0, "maxvalue");
                                String[] parts = StringUtil.split(value, ";");
                                if (parts.length > 1) {
                                    int maxCount = Integer.parseInt(parts[1].trim());
                                    counters.put(fieldPrefix, maxCount);
                                }
                            }
                            catch (Exception value) {
                                // empty catch block
                            }
                        }
                        WorksheetItemFields worksheetItemFields = this.getWorksheetItemFields();
                        boolean addDescriptionToFieldValue = config.getProperty("adddescriptiontofield", "Y").equals("Y");
                        Attachment.generateFields(fieldPrefix, attachmentdata, worksheetItemFields, addDescriptionToFieldValue, counters, false);
                        break block8;
                    }
                    this.deleteAllFields();
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to save attachment data.", e);
                }
            }
        }
        return super.validateContents("");
    }

    public static void generateFields(String fieldPrefix, DataSet attachments, WorksheetItemFields worksheetItemFields, boolean addDescriptionToFieldValue, HashMap<String, Integer> counters, boolean resequence) throws SapphireException {
        int maxCount = counters.containsKey(fieldPrefix) ? counters.get(fieldPrefix) : 0;
        HashSet<String> foundFields = new HashSet<String>();
        int localCounter = 0;
        for (int i = 0; i < attachments.size(); ++i) {
            String value;
            if (attachments.getValue(i, "__rowstatus").equals("D")) continue;
            String attachmentDesc = attachments.getValue(i, "attachmentdesc");
            String fieldid = attachments.getString(i, "_fieldname", "Att_" + attachments.getValue(i, "attachmentnum"));
            foundFields.add(fieldid);
            WorksheetItemFieldsField field = worksheetItemFields.getField(fieldid);
            if (field == null) {
                field = worksheetItemFields.addField(fieldid, fieldid, "string", i, null);
            }
            String string = value = resequence ? "" : field.getFieldValue(0);
            if (value.length() > 0) {
                int pos = value.indexOf("-");
                String fieldValue = pos >= 0 ? value.substring(0, pos) + (addDescriptionToFieldValue ? " - " + attachmentDesc : "") : value + (addDescriptionToFieldValue ? " - " + attachmentDesc : "");
                worksheetItemFields.updateFieldSequence(fieldid, i);
                worksheetItemFields.enterFieldValue(fieldid, 0, fieldValue);
                continue;
            }
            int figureValue = maxCount + ++localCounter;
            counters.put(fieldPrefix, figureValue);
            String fieldValue = fieldPrefix + " " + figureValue + (addDescriptionToFieldValue ? " - " + attachmentDesc : "");
            worksheetItemFields.enterFieldValue(fieldid, 0, fieldValue);
            field.setFieldContext(Attachment.getFieldContext(fieldPrefix, figureValue, attachments.getString(i, "sdcid"), attachments.getString(i, "keyid1"), attachments.getString(i, "keyid2"), attachments.getString(i, "keyid3"), attachments.getInt(i, "attachmentnum")));
        }
        Iterator<String> iterator = worksheetItemFields.iterator();
        while (iterator.hasNext()) {
            String fieldid = iterator.next();
            if (foundFields.contains(fieldid)) continue;
            worksheetItemFields.deleteField(fieldid);
        }
        worksheetItemFields.save();
    }

    public void deleteAllFields() throws SapphireException {
        WorksheetItemFields worksheetItemFields = this.getWorksheetItemFields();
        Iterator<String> iterator = worksheetItemFields.iterator();
        while (iterator.hasNext()) {
            String fieldid = iterator.next();
            worksheetItemFields.deleteField(fieldid);
        }
        worksheetItemFields.save();
    }

    public static String getFieldContext(String fieldPrefix, int figureValue, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum) {
        return fieldPrefix + ";" + String.format("%09d", figureValue) + ";" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + attachmentnum;
    }
}

