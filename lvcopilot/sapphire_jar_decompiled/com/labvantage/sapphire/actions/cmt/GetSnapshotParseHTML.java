/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.cmt.CMTUtil;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class GetSnapshotParseHTML
extends BaseAction {
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_TEMPID = "tempid";
    public static final String PROPERTY_PARSEDHTML = "parsedhtml";
    public static final String PROPERTY_TRANSFERUUID = "transferuuid";
    public static final String IMG_DO_NOT_IMPORT = "rc?command=image&image=FlatBlackCancel&color=red";
    public static final String IMG_IMPORT_PASS = "WEB-CORE/imageref/basic_application_icons/status_and_signs/16/check.png";
    public static final String IMG_IMPORT_ERROR = "WEB-CORE/imageref/basic_application_icons/toolbar/others/16/delete.png";
    public static final String IMG_IMPORT_DELETE = "WEB-CORE/imageref/computer_communication_and_media/data/data/16/data_delete.png";
    int Index_isRequested = 0;
    int Index_sdcid = 1;
    int Index_sourceSDIkeys = 2;
    int Index_matchedby = 3;
    int Index_matchedSDIkeys = 4;
    int Index_importoptions = 5;
    int Index_hasmatch = 6;
    int Index_hasdiff = 7;
    int Index_changelogcheck = 8;
    int Index_versionStatus = 9;
    int Index_message = 10;
    int Index_overrideoption = 10;
    int[] renderColIndexes = new int[]{this.Index_sdcid, this.Index_sourceSDIkeys, this.Index_matchedSDIkeys, this.Index_matchedby, this.Index_changelogcheck, this.Index_importoptions, this.Index_message};
    String[] columnids = new String[]{"isRequested", "sdcid", "keys", "matchedby", "matchedSDIkeys", "importoptions", "hasmatch", "hasdiff", "changelogcheck", "versionstatus", "message", "overrideoption"};
    String[] columntitles = null;
    TranslationProcessor tp = null;
    String notexist = "Not exist";
    String notcheckedin = "Not Checked In";
    String preimagediff = "Pre Image not Match";
    String noChangesFound = "No Changes Found";
    String preimagematch = "Pre Image Match";
    String preimagenotchecked = "Pre Image Not Checked";
    String notchangecontrolled = "Not Change Controlled";
    String nochangelog = "No Change Log Found";
    private static HashMap<String, SoftReference<HashMap>> snapshotCache = new HashMap();

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        if ("(system)".equals(this.connectionInfo.getSysuserId())) {
            throw new SapphireException("CMT Import by System account not allowed");
        }
        this.tp = this.getTranslationProcessor();
        this.columntitles = new String[]{this.tp.translate("SDC ID"), this.tp.translate("SDI To Import"), this.tp.translate("Matched SDI"), this.tp.translate("Matched By"), this.tp.translate("Change Log Check"), this.tp.translate("Import Option"), this.tp.translate("Import")};
        String filename = props.getProperty(PROPERTY_FILENAME);
        String tempid = props.getProperty(PROPERTY_TEMPID);
        File file = null;
        if (tempid.length() > 0) {
            FileManager.TempFile tempFile = FileManager.TempFile.getTempFile(tempid, false, this.getQueryProcessor(), this.getConnectionId());
            if (tempFile != null) {
                FileManager.FileData fileData = tempFile.getData();
                if (fileData != null) {
                    file = fileData.getFile().toFile();
                } else {
                    this.logger.warn("Failed to obtain file data.");
                }
            } else {
                this.logger.warn("Failed to obtain temp file.");
            }
        } else {
            file = new File(filename);
        }
        if (file == null) {
            throw new SapphireException("Failed to find import file.");
        }
        SnapshotPackage snapshotPackage = SnapshotPackage.fromFile(file, this.getConnectionId());
        String transferuuid = snapshotPackage.getUUID();
        String sql = "SELECT transferlogid, transferlogdesc, transferstatus, lasttransferreddt, lasttransferredby, transferoptions, checksumvalidationflag FROM transferlog WHERE transferuuid=? AND transferstatus in ('Started', 'Error')";
        DataSet existingTransferLogDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{transferuuid}, true);
        String transferlogid = "";
        String transferoptions = "";
        PropertyList transferoptionsPL = new PropertyList();
        boolean isResumeTransfer = false;
        if (existingTransferLogDs == null || existingTransferLogDs.getRowCount() == 0) {
            PropertyList transferlogProps = new PropertyList();
            transferlogProps.setProperty("sdcid", "TransferLog");
            transferlogProps.setProperty("transfertype", "Import");
            transferlogProps.setProperty("transferstatus", "Started");
            transferlogProps.setProperty("lasttransferreddt", "n");
            transferlogProps.setProperty("lasttransferredby", this.connectionInfo.getSysuserId());
            transferlogProps.setProperty(PROPERTY_TRANSFERUUID, transferuuid);
            if (snapshotPackage.isManifestCorrupted() || snapshotPackage.isContentCorrupted()) {
                transferlogProps.setProperty("checksumvalidationflag", "N");
            } else {
                transferlogProps.setProperty("checksumvalidationflag", "Y");
            }
            this.getActionProcessor().processAction("AddSDI", "1", transferlogProps);
            transferlogid = transferlogProps.getProperty("newkeyid1");
            Attachment attachment = Attachment.getAttachment("TransferLog", transferlogid, null, null);
            attachment.setDescription(props.getProperty(PROPERTY_FILENAME) + ".zip");
            try {
                attachment.setInputStream(new FileInputStream(file));
            }
            catch (FileNotFoundException e) {
                throw new SapphireException(e);
            }
            attachment.setSourceFilename(props.getProperty(PROPERTY_FILENAME) + ".zip");
            attachment.setAttachmentType(Attachment.AttachmentType.FILE);
            AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionid());
            ap.addSDIAttachment(attachment, false, false, "Sapphire Custom");
        } else {
            String newChecksumStatus;
            transferlogid = existingTransferLogDs.getValue(0, "transferlogid");
            transferoptions = existingTransferLogDs.getValue(0, "transferoptions");
            try {
                transferoptionsPL.setJSONString(transferoptions);
            }
            catch (JSONException transferlogProps) {
                // empty catch block
            }
            isResumeTransfer = true;
            String previousChecksumStatus = existingTransferLogDs.getValue(0, "checksumvalidationflag", "");
            String string = newChecksumStatus = snapshotPackage.isContentCorrupted() || snapshotPackage.isManifestCorrupted() ? "N" : "Y";
            if (!previousChecksumStatus.equals(newChecksumStatus)) {
                PropertyList transferlogProps = new PropertyList();
                transferlogProps.setProperty("sdcid", "TransferLog");
                transferlogProps.setProperty("keyid1", transferlogid);
                transferlogProps.setProperty("checksumvalidationflag", newChecksumStatus);
                this.getActionProcessor().processAction("EditSDI", "1", transferlogProps);
            }
        }
        DataSet parsedDs = new DataSet();
        for (int i = 0; i < this.columnids.length; ++i) {
            parsedDs.addColumn(this.columnids[i], 0);
        }
        if (snapshotPackage != null && snapshotPackage.getSnapshotItems().size() > 0) {
            List<SnapshotItem> itemList = snapshotPackage.getSnapshotItems();
            List<SnapshotItem> requestedItemList = snapshotPackage.getRequestedSnapshotItems();
            HashMap<String, HashSet<String>> pTreeNodeIds = new HashMap<String, HashSet<String>>();
            HashMap<String, ArrayList<Node>> pTreeNodes = new HashMap<String, ArrayList<Node>>();
            for (SnapshotItem item : itemList) {
                if (item.getType() == Snapshot.Type.SDI) {
                    SDISnapshotItem sdiSnapshotItem = (SDISnapshotItem)item;
                    this.buildParsedDataSet(sdiSnapshotItem, snapshotPackage, parsedDs, requestedItemList);
                    continue;
                }
                if (item.getType() == Snapshot.Type.PROPERTYTREE) {
                    PropertyTreeSnapshotItem propertyTreeSnapshotItem = (PropertyTreeSnapshotItem)item;
                    this.buildParsedPropertyTreeDataSet(propertyTreeSnapshotItem, snapshotPackage, parsedDs, requestedItemList, pTreeNodeIds, pTreeNodes);
                    continue;
                }
                throw new SapphireException("Unknown Snapshot Item type");
            }
            parsedDs.sort(this.columnids[0] + " d," + this.columnids[1]);
            StringBuilder sb = new StringBuilder();
            sb.append("<table style=\"cell-padding:5px;\" class=\"gridmaint_table\">");
            boolean isImportBlocked = false;
            if (snapshotPackage.isManifestCorrupted() || snapshotPackage.isContentCorrupted()) {
                sb.append("<tr>");
                CMTPolicy targetPolicy = CMTPolicy.getPolicy(this.getConnectionid(), "Param");
                if (targetPolicy.isBlockInvalidChecksum()) {
                    sb.append("<td colspan = '" + this.columntitles.length + "'><font color='red'><b>" + this.tp.translate("Checksum validation failed - Snapshot Package has been changed since generation! Import not allowed.") + "</b></font></td>");
                    isImportBlocked = true;
                } else {
                    sb.append("<td colspan = '" + this.columntitles.length + "'><font color='red'><b>" + this.tp.translate("Checksum validation failed - Snapshot Package has been changed since generation! Please proceed with caution.") + "</b></font></td>");
                }
                sb.append("</tr>");
            }
            if (!snapshotPackage.isManifestCorrupted() && !snapshotPackage.isContentCorrupted()) {
                sb.append("<tr>");
                sb.append("<td colspan = '" + this.columntitles.length + "'><font color='green'><b>" + this.tp.translate("Checksum validation - Ok.") + "</b></font></td>");
                sb.append("</tr>");
            }
            for (int row = 0; row < parsedDs.getRowCount(); ++row) {
                int col;
                String sdikeys;
                String sdidisplay = sdikeys = parsedDs.getValue(row, this.columnids[this.Index_sourceSDIkeys]);
                if (sdikeys.indexOf("|%|") > 0) {
                    String[] keyDisplays = StringUtil.split(sdikeys, "|%|");
                    sdikeys = keyDisplays[0];
                    sdidisplay = keyDisplays[1];
                }
                String changelogcheck = parsedDs.getValue(row, this.columnids[this.Index_changelogcheck]);
                String isExactMatch = parsedDs.getString(row, "__isExactMatch", "N");
                if (row == 0) {
                    sb.append("<tr style=\"height:30px\">");
                    for (col = 0; col < this.columntitles.length; ++col) {
                        sb.append("<td style=\"padding:5px\" class=\"maintform_fieldtitle\">" + this.tp.translate(this.columntitles[col]) + "</td>");
                    }
                    sb.append("</tr>");
                }
                sb.append("<tr style=\"height:30px\">");
                for (col = 0; col < this.renderColIndexes.length; ++col) {
                    int colIndex = this.renderColIndexes[col];
                    String value = parsedDs.getValue(row, this.columnids[colIndex]);
                    if (colIndex == this.Index_sourceSDIkeys) {
                        sb.append("<td style=\"width:250px;padding:5px\" class=\"maintform_field\">");
                        String statustitle = parsedDs.getValue(row, this.columnids[this.Index_message]);
                        if (statustitle.length() == 0) {
                            if ("Override Existing".equals(parsedDs.getValue(row, this.columnids[this.Index_importoptions]))) {
                                String changelogcheckmsg = parsedDs.getValue(row, this.columnids[this.Index_changelogcheck]);
                                if ("Y".equals(parsedDs.getValue(row, this.columnids[this.Index_hasmatch])) && this.notcheckedin.equals(parsedDs.getValue(row, this.columnids[this.Index_changelogcheck]))) {
                                    statustitle = "Target SDI not checked in";
                                } else if ("Y".equals(parsedDs.getValue(row, this.columnids[this.Index_hasmatch]))) {
                                    statustitle = "PreImage Not matching. Please choose to ignore import or override";
                                } else if (parsedDs.getValue(row, this.columnids[this.Index_hasmatch]).length() > 0 && !"Y".equals(parsedDs.getValue(row, this.columnids[this.Index_hasmatch]))) {
                                    statustitle = "Override Existing but multiple matches found";
                                }
                            } else if ("Do Not Import".equals(parsedDs.getString(row, this.columnids[this.Index_importoptions]))) {
                                statustitle = "Can not Import.";
                            }
                            parsedDs.setValue(row, this.columnids[this.Index_message], statustitle);
                        }
                        boolean isRequested = "Requested".equals(parsedDs.getValue(row, this.columnids[this.Index_isRequested]));
                        sb.append("<div style=\"display:inline-block;width:230px\">");
                        sb.append("<a title=\"" + (isRequested ? this.tp.translate("View Requested SDI") : this.tp.translate("View Referenced SDI")) + "\" style=\"text-decoration:underline\" href=\"Javascript:viewSnapShot( '" + sdikeys + "' )\">" + sdidisplay + (isRequested ? "" : "*") + "</a>");
                        if (parsedDs.getValue(row, "attachmentsize", "").length() > 0) {
                            sb.append(CMTUtil.getAttachmentIconHTML(parsedDs.getLong(row, "attachmentsize"), this.getTranslationProcessor()));
                        }
                        sb.append("</div>");
                        sb.append("</td>");
                        continue;
                    }
                    if (colIndex == this.Index_matchedSDIkeys) {
                        sb.append("<td style=\"padding:5px\" class=\"maintform_field\">");
                        String matchString = parsedDs.getValue(row, this.columnids[this.Index_hasmatch]);
                        if ("Y".equals(matchString)) {
                            String targetSDIKey = value;
                            if (targetSDIKey.indexOf("(") > 0 && targetSDIKey.lastIndexOf(")") == targetSDIKey.length() - 1) {
                                String[] tokens = StringUtil.getTokens(targetSDIKey, "(", ")");
                                targetSDIKey = tokens[tokens.length - 1];
                            }
                            sb.append("<div style=\"display:inline-block;width:180px\">" + value + "</div>");
                            if ("Y".equals(parsedDs.getString(row, "__isExactMatch", "N"))) {
                                sb.append("No Changes Found");
                            } else {
                                sb.append("<div style=\"display:inline-block;width:80px\"><a style=\"margin-left:20px;text-decoration:underline\" href=\"Javascript:compareSourceTarget( '" + sdikeys + "', '" + targetSDIKey + "' )\">" + this.tp.translate("Show Diff") + "</a></div>");
                            }
                        } else if ("N".equals(matchString) || matchString.length() == 0) {
                            sb.append(value);
                        } else {
                            sb.append("<div style=\"margin-bottom:5px;color:red\">" + parsedDs.getValue(row, this.columnids[this.Index_hasmatch]) + this.tp.translate(" matches found! "));
                            if (sdikeys.indexOf("PhysicalStore|") != 0) {
                                sb.append("<p>" + this.tp.translate("Please Choose One if Override Existing") + "</div>");
                            }
                            String[] targetSDIKeys = StringUtil.split(value, ";");
                            for (int i = 0; i < targetSDIKeys.length; ++i) {
                                String targetSDIKey = targetSDIKeys[i];
                                sb.append("<div>");
                                sb.append("<div style=\"display:inline-block;width:180px\"><label><input onchange=\"multipleMatchSelected( this )\" name=\"" + sdikeys + "\" type=\"radio\" id=\"" + sdikeys + "_" + targetSDIKey + "\"/>" + targetSDIKey + "</label></div>");
                                sb.append("<div style=\"display:inline-block;width:80px\"><a style=\"margin-left:20px;text-decoration:underline\" href=\"Javascript:compareSourceTarget( '" + sdikeys + "', '" + targetSDIKey + "' )\">" + this.tp.translate("Show Diff") + "</a></div>");
                                sb.append("</div>");
                            }
                        }
                        sb.append("</td>");
                        continue;
                    }
                    if (colIndex == this.Index_changelogcheck) {
                        sb.append("<td style=\"padding:5px\" class=\"maintform_field\">");
                        if (this.preimagediff.equals(value)) {
                            if (!"M".equals(parsedDs.getValue(row, this.columnids[this.Index_hasmatch]))) {
                                sb.append("<div style=\"color:red;display:inline-block;width:100px\">" + value + "</div>");
                                sb.append("<div style=\"display:inline-block;width:80px\"><a style=\"margin-left:20px;text-decoration:underline\" href=\"Javascript:compareSourceTarget( '" + sdikeys + "_preimage', '" + parsedDs.getValue(row, this.columnids[this.Index_matchedSDIkeys]) + "' )\">" + this.tp.translate("Show Diff") + "</a></div>");
                            }
                        } else if (this.notcheckedin.equals(value)) {
                            sb.append("<div style=\"color:red;display:inline-block;width:100px\">" + value + "</div>");
                        } else {
                            sb.append(value);
                        }
                        sb.append("</td>");
                        continue;
                    }
                    if (colIndex == this.Index_importoptions) {
                        String overrideOption = "";
                        if (transferoptionsPL.getProperty(sdikeys).length() > 0) {
                            overrideOption = transferoptionsPL.getProperty(sdikeys);
                            sb.append(transferoptionsPL.getProperty(sdikeys));
                        }
                        sb.append("<td style=\"padding:5px\" class=\"maintform_field\">");
                        sb.append("<select name=\"importoptions\" onchange=\"importOptionChanged( this, '" + sdikeys + "' )\">");
                        String[] optionvalues = StringUtil.split(value, ";");
                        for (int i = 0; i < optionvalues.length; ++i) {
                            String selected = "";
                            if ("Do Not Import".equals(optionvalues[i]) && (this.preimagediff.equals(changelogcheck) || isExactMatch.contains("Y"))) {
                                selected = "selected";
                                if (this.preimagediff.equals(changelogcheck)) {
                                    parsedDs.setValue(row, this.columnids[this.Index_message], this.tp.translate("Do Not Import Due to") + ": " + this.tp.translate(this.preimagediff));
                                } else {
                                    parsedDs.setValue(row, this.columnids[this.Index_message], this.tp.translate("Do Not Import Due to") + ": " + this.tp.translate(this.noChangesFound));
                                }
                            } else if (overrideOption.length() > 0 && overrideOption.equals(optionvalues[i])) {
                                selected = "selected";
                                parsedDs.setValue(row, this.columnids[this.Index_message], overrideOption);
                            }
                            String optionvalue = optionvalues[i];
                            if ("Delete".equals(optionvalue)) {
                                optionvalue = "Import";
                            } else if ("Do Not Delete".equals(optionvalue) || "Ignore Delete".equals(optionvalue)) {
                                optionvalue = "Do Not Import";
                            }
                            sb.append("<option value=\"" + optionvalue + "\" " + selected + ">" + this.tp.translate(optionvalues[i]) + "</option>");
                        }
                        sb.append("</select>");
                        sb.append("</td>");
                        continue;
                    }
                    if (colIndex == this.Index_message) {
                        sb.append("<td style=\"padding:5px\" class=\"maintform_field\">");
                        String message = parsedDs.getValue(row, this.columnids[this.Index_message]);
                        if (!(message.length() <= 0 || "Override Existing".equals(message) || "Override If Provisional".equals(message) || "Create New Version".equals(message))) {
                            if (message.indexOf("Do Not Import") == 0) {
                                sb.append("<img title=\"" + this.tp.translate(message) + "\" src=\"" + IMG_DO_NOT_IMPORT + "\">");
                            } else if (message.indexOf("Delete") == 0) {
                                sb.append("<img title=\"" + this.tp.translate(message) + "\" src=\"" + IMG_IMPORT_DELETE + "\">");
                            } else {
                                sb.append("<img title=\"" + this.tp.translate(message) + "\" src=\"" + IMG_IMPORT_ERROR + "\">");
                            }
                        } else {
                            sb.append("<img title=\"" + this.tp.translate("Ready to Import") + "\" src=\"" + IMG_IMPORT_PASS + "\">");
                        }
                        sb.append("</td>");
                        continue;
                    }
                    sb.append("<td style=\"padding:5px\" class=\"maintform_field\">");
                    sb.append(value);
                    sb.append("</td>");
                }
                sb.append("</tr>");
            }
            sb.append("</table>");
            sb.append("<script>");
            sb.append("\nvar transferoptions = " + (transferoptions.length() > 0 ? transferoptions : "{}") + ";");
            sb.append("\nvar isImportBlocked = " + isImportBlocked + ";");
            sb.append("\nvar isImportBlockedMsg = '" + this.tp.translate("Checksum validation failed. Import Blocked.") + "';");
            sb.append("\nfunction importOptionChanged( element, sdikey ) { var optionvalue = $(element).val(); var optionlabel = $(element).find('option:selected').text(); transferoptions[sdikey]=optionvalue; $(element).parent().next().html( optionvalue=='Do Not Import' ? '<img title=\"Do not import this item\" src=\"rc?command=image&image=FlatBlackCancel&color=red\">' : optionlabel=='Delete' ? '<img title=\"Delete\" src=\"WEB-CORE/imageref/computer_communication_and_media/data/data/16/data_delete.png\">' : '<img title=\"Read to Import\" src=\"WEB-CORE/imageref/basic_application_icons/status_and_signs/16/check.png\">' )} ");
            sb.append("\nfunction viewSnapShot( sdikey ) { var url='rc?command=page&page=SDISnapshotViewer&layoutscrolling=N&snapshotdiff=N&cachedsnapshotkey=' + sdikey; sapphire.ui.dialog.open( sapphire.translate( 'View Source Snapshot' ), url, 800, 450 ); } ");
            sb.append("\nfunction compareSourceTarget( sdikey, targetsdikey ) { var url='rc?command=page&page=SDISnapshotViewer&includeauditcolumns=N&snapshotdiff=Y&cachedsnapshotkey=' + sdikey + '&targetsdikey=' + targetsdikey; sapphire.ui.dialog.open( sapphire.translate( 'View Diff With Target' ), url, 800, 450 ); } ");
            sb.append("\nfunction multipleMatchSelected( element ) { $(element).parent().parent().parent().siblings().first().css( 'display', 'none' )} ");
            sb.append("\nfunction saveforlater(){ parent.setProperty( \"transferoptions\", JSON.stringify( transferoptions ) ); parent.next() }");
            sb.append("</script>");
            props.setProperty(PROPERTY_PARSEDHTML, sb.toString());
            props.setProperty("transferlogid", transferlogid);
            if (isResumeTransfer) {
                props.setProperty("transferlogdesc", existingTransferLogDs.getValue(0, "transferlogdesc"));
                props.setProperty("transferstatus", existingTransferLogDs.getValue(0, "transferstatus"));
                props.setProperty("lasttransferredby", existingTransferLogDs.getValue(0, "lasttransferredby"));
                props.setProperty("lasttransferreddt", existingTransferLogDs.getValue(0, "lasttransferreddt"));
            }
        } else {
            props.setProperty(PROPERTY_PARSEDHTML, "<p>" + this.tp.translate("No Items found in the transfer file! The file may not be a CMT transfer package!") + "</p>");
        }
    }

    private void buildParsedDataSet(SDISnapshotItem sdiSnapshotItemFromPkg, SnapshotPackage snapshotPackage, DataSet parsedDs, List requestedItemList) throws SapphireException {
        try {
            String verStatusNoProvWhereClause;
            long attSize;
            String sourcesdiDisplay;
            PropertyList transferoptions;
            String overrideimportoption;
            int row = parsedDs.addRow();
            if (requestedItemList.indexOf(sdiSnapshotItemFromPkg) >= 0) {
                parsedDs.setValue(row, this.columnids[this.Index_isRequested], this.tp.translate("Requested"));
            } else {
                parsedDs.setValue(row, this.columnids[this.Index_isRequested], this.tp.translate("Referenced"));
            }
            boolean isDelete = sdiSnapshotItemFromPkg.isDeleted();
            SDISnapshot sourceSnapShot = (SDISnapshot)snapshotPackage.getSnapshot(sdiSnapshotItemFromPkg);
            SDISnapshot sourcePreImage = (SDISnapshot)snapshotPackage.getPreSnapshot(sdiSnapshotItemFromPkg);
            SDISnapshotItem sdiSnapshotItem = isDelete ? sourcePreImage.getSnapshotItem() : sourceSnapShot.getSnapshotItem();
            SDIData importSDI = sdiSnapshotItem.getSDIData();
            String sdcid = sdiSnapshotItem.getSDCId();
            String transpolicyNodeid = sdiSnapshotItem.getPolicyNodeId();
            CMTPolicy targetPolicy_SDI = CMTPolicy.getPolicy(this.getConnectionid(), sdcid, transpolicyNodeid);
            boolean isTemplate = "Y".equals(importSDI.getDataset("primary").getValue(0, "templateflag"));
            boolean isChangeControlled = "Y".equals(targetPolicy_SDI.getChangeControlledFlag()) || "T".equals(targetPolicy_SDI.getChangeControlledFlag()) && isTemplate;
            PropertyList transPolicyNodePL = sdiSnapshotItem.getPolicyNodeProps();
            PropertyList linkprops = sdiSnapshotItem.getParentLinkProps();
            String string = overrideimportoption = linkprops != null && linkprops.getPropertyListNotNull("transferoption").getProperty("importoption").length() > 0 ? linkprops.getPropertyListNotNull("transferoption").getProperty("importoption") : null;
            if (overrideimportoption != null) {
                transferoptions = transPolicyNodePL.getPropertyList("primary").getPropertyListNotNull("transferoption");
                transferoptions.setProperty("importoption", overrideimportoption);
            } else if ("Category".equals(sdcid) && !requestedItemList.contains(sdiSnapshotItem)) {
                transferoptions = transPolicyNodePL.getPropertyList("primary").getPropertyListNotNull("transferoption");
                transferoptions.setProperty("importoption", "Ignore If Exists");
            }
            CMTPolicy sourcePolicy = new CMTPolicy(transPolicyNodePL);
            PropertyList sdcProps = this.getSDCProcessor().getPropertyList(sdcid);
            parsedDs.setValue(row, this.columnids[this.Index_sdcid], sdcid);
            String table = sdcProps.getProperty("tableid");
            String keyColId1 = sdcProps.getProperty("keycolid1");
            String keyColId2 = sdcProps.getProperty("keycolid2");
            String keyColId3 = sdcProps.getProperty("keycolid3");
            String descColName = sdcProps.getProperty("desccol");
            int keyColsCount = Integer.parseInt(sdcProps.getProperty("keycolumns"));
            boolean isVersioned = "Y".equals(sdcProps.getProperty("versionedflag"));
            boolean isAutoKey = sdcProps.getProperty("keygenerationrule").length() > 0;
            boolean hasUUID = "Y".equals(sdcProps.getProperty("uuidflag"));
            DataSet primaryDs = importSDI.getDataset("primary");
            String uuid = primaryDs.getValue(0, "uuid");
            String keyid1 = primaryDs.getValue(0, keyColId1);
            String keyid2 = keyColId2.length() > 0 ? primaryDs.getValue(0, keyColId2) : "";
            String keyid3 = keyColId3.length() > 0 ? primaryDs.getValue(0, keyColId3) : "";
            boolean isExpired = false;
            if (isVersioned) {
                isExpired = primaryDs.getValue(0, "versionstatus").equals("E");
            }
            String sdikeyidonly = keyid1 + (keyid2.length() > 0 ? "|" + keyid2 : "") + (keyid3.length() > 0 ? "|" + keyid3 : "");
            String sourcesdikeys = sdcid + "|" + sdikeyidonly;
            String altIdentifierColumn = sourcePolicy.getIndentifyColumn();
            String[] identifyColumns = null;
            StringBuffer identifyColValues = new StringBuffer();
            if (altIdentifierColumn.contains("LV_QUERY")) {
                altIdentifierColumn = targetPolicy_SDI.getIndentifyColumn();
                identifyColumns = new String[]{altIdentifierColumn};
            } else {
                for (String identifyColId : identifyColumns = StringUtil.split(altIdentifierColumn, ",")) {
                    String colVal = primaryDs.getValue(0, identifyColId);
                    if (colVal.length() <= 0) continue;
                    identifyColValues.append("<br>");
                    identifyColValues.append(identifyColId).append(" = ").append(colVal);
                }
            }
            String string2 = sourcesdiDisplay = altIdentifierColumn.length() > 0 ? "(" + sdikeyidonly + ") " + identifyColValues : sdikeyidonly;
            if (snapshotCache.get(this.getConnectionId()) == null) {
                snapshotCache.put(this.getConnectionId(), new SoftReference(new HashMap()));
            }
            if (isDelete) {
                snapshotCache.get(this.getConnectionId()).get().put(sourcesdikeys, sourcePreImage);
            } else {
                snapshotCache.get(this.getConnectionId()).get().put(sourcesdikeys, sourceSnapShot);
            }
            snapshotCache.get(this.getConnectionId()).get().put(sourcesdikeys + "_preimage", sourcePreImage);
            parsedDs.setValue(row, this.columnids[this.Index_sourceSDIkeys], sourcesdikeys + "|%|" + sourcesdiDisplay);
            if (!isDelete && (attSize = sdiSnapshotItem.getAttachmentSize()) > 0L) {
                parsedDs.setNumber(row, "attachmentsize", attSize);
            }
            String sql = "";
            String sqlNoVersion = "";
            SafeSQL safeSQL = new SafeSQL();
            SafeSQL safeSQLNoProvisional = new SafeSQL();
            boolean matchFound = false;
            boolean multipleFound = false;
            String matchedBy = "";
            String matchedByNoProvisional = "";
            DataSet targetSDIs = null;
            DataSet targetSDIsNoProvisional = null;
            String verStatusWhereClause = isVersioned && !isExpired ? " AND versionstatus = 'P'" : "";
            String string3 = verStatusNoProvWhereClause = isVersioned && !isExpired ? " AND versionstatus != 'P'" : "";
            if (hasUUID) {
                String uuidSQL = "SELECT * FROM " + table + " WHERE uuid=" + safeSQL.addVar(uuid);
                this.database.createPreparedResultSet(uuidSQL + verStatusWhereClause, safeSQL.getValues());
                targetSDIs = new DataSet(this.database.getResultSet());
                if (targetSDIs.getRowCount() > 0) {
                    matchFound = true;
                }
                if (targetSDIs.getRowCount() > 1) {
                    multipleFound = true;
                }
                matchedBy = "UUID: " + uuid;
                if (isVersioned && !matchFound && (targetSDIsNoProvisional = this.getQueryProcessor().getPreparedSqlDataSet(uuidSQL + verStatusNoProvWhereClause, safeSQL.getValues())).getRowCount() > 0) {
                    matchedByNoProvisional = "UUID: " + uuid + ". " + this.tp.translate("Non-Provisional SDIs only.");
                }
            }
            if (!matchFound) {
                String additionalSelectClause = (hasUUID ? ", uuid" : "") + (isVersioned ? ",versionstatus " : "");
                safeSQL.reset();
                safeSQLNoProvisional.reset();
                if (altIdentifierColumn.length() > 0) {
                    String[] altIDColumns = RequestParser.parseColItem("primary[" + altIdentifierColumn + "]");
                    sql = "SELECT * FROM " + table + " WHERE ";
                    for (int c = 0; c < altIDColumns.length; ++c) {
                        String columnid = altIDColumns[c];
                        String columnalias = altIDColumns[c];
                        if (altIDColumns[c].lastIndexOf(" ") > 0) {
                            columnid = altIDColumns[c].substring(0, altIDColumns[c].lastIndexOf(" "));
                            columnalias = altIDColumns[c].substring(altIDColumns[c].lastIndexOf(" ")).trim();
                        }
                        String colvalue = primaryDs.getValue(0, columnalias);
                        sql = sql + (c == 0 ? "" : " AND ") + "(" + columnid + "=" + safeSQL.addVar(colvalue) + (colvalue.length() == 0 ? " OR " + columnid + " is null" : "") + ")";
                    }
                    sqlNoVersion = sql;
                    sql = sql + verStatusWhereClause;
                    matchedBy = this.tp.translate("Alter Identifier Column ") + "(" + altIdentifierColumn + verStatusWhereClause + ")";
                    matchedByNoProvisional = this.tp.translate("Alter Identifier Column ") + "(" + altIdentifierColumn + "). " + this.tp.translate("Non-Provisional SDIs only.");
                } else {
                    if (keyColsCount == 1) {
                        sql = "SELECT " + keyColId1 + additionalSelectClause + " FROM " + table + " WHERE " + keyColId1 + "=" + safeSQL.addVar(keyid1);
                        sqlNoVersion = "SELECT " + keyColId1 + additionalSelectClause + " FROM " + table + " WHERE " + keyColId1 + "=" + safeSQLNoProvisional.addVar(keyid1);
                    } else if (keyColsCount == 2) {
                        sql = "SELECT " + keyColId1 + "," + keyColId2 + additionalSelectClause + " FROM " + table + " WHERE " + keyColId1 + "=" + safeSQL.addVar(keyid1) + verStatusWhereClause + " AND " + keyColId2 + "=" + safeSQL.addVar(keyid2);
                        sqlNoVersion = "SELECT " + keyColId1 + "," + keyColId2 + additionalSelectClause + " FROM " + table + " WHERE " + keyColId1 + "=" + safeSQLNoProvisional.addVar(keyid1) + verStatusNoProvWhereClause + " AND " + keyColId2 + "=" + safeSQLNoProvisional.addVar(keyid2);
                    } else if (keyColsCount == 3) {
                        sql = "SELECT " + keyColId1 + "," + keyColId2 + "," + keyColId3 + additionalSelectClause + " FROM " + table + " WHERE " + keyColId1 + "=" + safeSQL.addVar(keyid1) + verStatusWhereClause + " AND " + keyColId2 + "=" + safeSQL.addVar(keyid2) + " AND " + keyColId3 + "=" + safeSQL.addVar(keyid3);
                        sqlNoVersion = "SELECT " + keyColId1 + "," + keyColId2 + "," + keyColId3 + additionalSelectClause + " FROM " + table + " WHERE " + keyColId1 + "=" + safeSQLNoProvisional.addVar(keyid1) + verStatusNoProvWhereClause + " AND " + keyColId2 + "=" + safeSQLNoProvisional.addVar(keyid2) + " AND " + keyColId3 + "=" + safeSQLNoProvisional.addVar(keyid3);
                    }
                    matchedBy = this.tp.translate("Key ID ") + "(" + keyColId1 + (keyColsCount >= 2 ? ";" + keyColId2 : "") + (keyColsCount == 3 ? ";" + keyColId3 : "") + verStatusWhereClause + ")";
                    matchedByNoProvisional = this.tp.translate("Key ID ") + "(" + keyColId1 + (keyColsCount >= 2 ? ";" + keyColId2 : "") + (keyColsCount == 3 ? ";" + keyColId3 : "") + "). " + this.tp.translate("Non-Provisional SDIs only.");
                }
                targetSDIs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (targetSDIs == null) {
                    throw new SapphireException("Couldn't retrieve matched SDIs");
                }
                if (isVersioned && (targetSDIsNoProvisional = this.getQueryProcessor().getPreparedSqlDataSet(sqlNoVersion, altIdentifierColumn.length() > 0 ? safeSQL.getValues() : safeSQLNoProvisional.getValues())) == null) {
                    throw new SapphireException("Couldn't retrieve matched Non-Provisional SDIs");
                }
                if (targetSDIs.getRowCount() > 0) {
                    matchFound = true;
                }
                if (targetSDIs.getRowCount() > 1) {
                    multipleFound = true;
                }
            }
            if (matchFound) {
                String matchedkeyid1 = targetSDIs.getValue(0, keyColId1);
                String matchedkeyid2 = targetSDIs.getValue(0, keyColId2);
                String matchedkeyid3 = targetSDIs.getValue(0, keyColId3);
                if (isChangeControlled) {
                    Object changelogstatus;
                    safeSQL = new SafeSQL();
                    sql = "SELECT * FROM changelog WHERE linksdcid=" + safeSQL.addVar(sdcid) + " AND linkkeyid1=" + safeSQL.addVar(matchedkeyid1);
                    if (keyColsCount == 2) {
                        sql = sql + " AND linkkeyid2=" + safeSQL.addVar(matchedkeyid2);
                    } else if (keyColsCount == 3) {
                        sql = sql + " AND linkkeyid2=" + safeSQL.addVar(matchedkeyid2) + " AND linkkeyid3=" + safeSQL.addVar(matchedkeyid3);
                    }
                    sql = sql + " order by moddt desc";
                    this.database.createPreparedResultSet(sql, safeSQL.getValues());
                    if (this.database.getNext()) {
                        changelogstatus = this.database.getString("changelogstatus");
                        if ("Checked Out".equals(changelogstatus)) {
                            parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.notcheckedin);
                        } else if ("Checked In".equals(changelogstatus)) {
                            String modifiedsnapshotXML = this.database.getClob("modifiedsnapshot");
                            if (modifiedsnapshotXML != null && modifiedsnapshotXML.length() > 0) {
                                SDISnapshot targetSnapshot = (SDISnapshot)Snapshot.fromXML(modifiedsnapshotXML, this.getConnectionId());
                                if (sourcePreImage != null) {
                                    boolean hasDiff = SDISnapshotViewer.hasDiff((SapphireConnection)this.connectionInfo, sourcePreImage, targetSnapshot, true);
                                    if (hasDiff) {
                                        parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.preimagediff);
                                    } else {
                                        parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.preimagematch);
                                    }
                                } else {
                                    parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.preimagenotchecked);
                                }
                            }
                        } else {
                            parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.notexist);
                        }
                    } else {
                        parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.nochangelog);
                    }
                    if (!multipleFound) {
                        changelogstatus = new SnapshotFactory(this.getConnectionId()).generateSDISnapshot(sdcid, targetSDIs.getValue(0, keyColId1), targetSDIs.getValue(0, keyColId2), targetSDIs.getValue(0, keyColId3));
                    }
                }
                parsedDs.setValue(row, this.columnids[this.Index_matchedby], matchedBy);
                if (matchFound) {
                    if (matchFound) {
                        parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "Y");
                    }
                    if (multipleFound) {
                        parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "" + targetSDIs.getRowCount());
                    }
                    String targetSDIKeys = this.getTargetSDIKeys(keyColId1, keyColId2, keyColId3, descColName, keyColsCount, isAutoKey, false, targetSDIs);
                    parsedDs.setValue(row, this.columnids[this.Index_matchedSDIkeys], targetSDIKeys);
                    if (!sdiSnapshotItemFromPkg.isDeleted()) {
                        SnapshotFactory snapshotFactory = new SnapshotFactory(this.getConnectionId());
                        StringBuilder isExactMatch = new StringBuilder();
                        for (int i = 0; i < targetSDIs.getRowCount(); ++i) {
                            String targetSDCId = sdcid;
                            String targetKeyId1 = targetSDIs.getString(i, keyColId1);
                            String targetKeyId2 = targetSDIs.getString(i, keyColId2, "");
                            String targetKeyId3 = targetSDIs.getString(i, keyColId3, "");
                            String sourceCMTPolicyNodeId = sdiSnapshotItemFromPkg.getPolicyNodeId();
                            SDISnapshot targetSnapshot = snapshotFactory.generateSDISnapshot(targetSDCId, targetKeyId1, targetKeyId2, targetKeyId3);
                            SDISnapshot sourceSnapshot = sdiSnapshotItemFromPkg.getSnapshot();
                            HashSet<String> ignoreColList = new HashSet<String>();
                            ignoreColList.add(keyColId1);
                            ignoreColList.add(keyColId2);
                            ignoreColList.add(keyColId3);
                            ignoreColList.add("uuid");
                            boolean hasDiff = SDISnapshotViewer.hasDiff((SapphireConnection)this.connectionInfo, sourceSnapshot, targetSnapshot, false, ignoreColList);
                            isExactMatch.append(";").append(hasDiff ? "N" : "Y");
                        }
                        if (isExactMatch.length() > 0) {
                            isExactMatch.deleteCharAt(0);
                        }
                        parsedDs.setString(row, "__isExactMatch", isExactMatch.toString());
                    }
                } else {
                    parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "N");
                }
                String sourceImportOption = sourcePolicy.getImportOption();
                String sourceImportVersionedOption = sourcePolicy.getImportVersionedSDIOption();
                StringBuilder importoptions = new StringBuilder();
                if (multipleFound) {
                    if (!"PhysicalStore".equals(sdcid)) {
                        importoptions.append(";Do Not Import");
                        String message = "Do Not Import As Multiple Match Found";
                        parsedDs.setValue(row, this.columnids[this.Index_message], message);
                    }
                    if (isVersioned) {
                        importoptions.append(";Create New Version");
                    } else if (sourceImportOption.equals("Regenerate Auto Key")) {
                        importoptions.append(";Regenerate Auto Key");
                    }
                    if (!"PhysicalStore".equals(sdcid) && (!isVersioned || "P".equals(targetSDIs.getValue(0, "versionstatus")) || isExpired)) {
                        importoptions.append(";Override Existing");
                    }
                } else if (matchFound) {
                    if (isDelete) {
                        importoptions.append(";Delete");
                        parsedDs.setValue(row, this.columnids[this.Index_overrideoption], "Delete");
                    } else {
                        if (sourceImportOption.equals("Ignore If Exists")) {
                            importoptions.append(";Do Not Import");
                        }
                        if (isVersioned) {
                            if (sourceImportVersionedOption.equals("Create New Version")) {
                                importoptions.append(";Create New Version");
                                parsedDs.setValue(row, this.columnids[this.Index_overrideoption], "Create New Version");
                                if (sourcePolicy.isAllowImporterToChoose() && "P".equals(targetSDIs.getValue(0, "versionstatus"))) {
                                    importoptions.append(";Override If Provisional");
                                }
                            } else if (sourceImportVersionedOption.equals("Override If Provisional")) {
                                if ("P".equals(targetSDIs.getValue(0, "versionstatus"))) {
                                    importoptions.append(";Override If Provisional");
                                    parsedDs.setValue(row, this.columnids[this.Index_overrideoption], "Override If Provisional");
                                    if (sourcePolicy.isAllowImporterToChoose()) {
                                        importoptions.append(";Create New Version");
                                    }
                                } else {
                                    if (isExpired) {
                                        importoptions.append(";Override and Expire Version");
                                    } else {
                                        importoptions.append(";Create New Version");
                                    }
                                    parsedDs.setValue(row, this.columnids[this.Index_overrideoption], "Create New Version");
                                }
                            }
                        } else {
                            if (sourceImportOption.equals("Regenerate Auto Key")) {
                                importoptions.append(";Regenerate Auto Key");
                            }
                            if (!"PhysicalStore".equals(sdcid)) {
                                importoptions.append(";Override Existing");
                            }
                        }
                        if (sourceImportOption.equals("Ignore If Exists")) {
                            String message = "Do Not Import As Import Option is Ignore if exists";
                            parsedDs.setValue(row, this.columnids[this.Index_message], message);
                        }
                    }
                } else if (isDelete) {
                    importoptions.append(";Ignore Delete");
                } else {
                    importoptions.append(";Import");
                }
                if (sourcePolicy.isAllowImporterToChoose()) {
                    if (isDelete) {
                        importoptions.append(";Do Not Delete");
                    } else if (importoptions.indexOf("Do Not Import") < 0) {
                        importoptions.append(";Do Not Import");
                    }
                }
                parsedDs.setValue(row, this.columnids[this.Index_importoptions], importoptions.substring(1));
            } else if (isDelete) {
                parsedDs.setValue(row, this.columnids[this.Index_importoptions], "Ignore Delete");
            } else if (isVersioned) {
                if (isExpired) {
                    parsedDs.setValue(row, this.columnids[this.Index_importoptions], "Create Same Expired Version;Do Not Import");
                } else if (targetSDIsNoProvisional != null && targetSDIsNoProvisional.getRowCount() > 0) {
                    String targetSDIKeys = this.getTargetSDIKeys(keyColId1, keyColId2, keyColId3, descColName, keyColsCount, isAutoKey, true, targetSDIsNoProvisional);
                    parsedDs.setString(row, this.columnids[this.Index_hasmatch], "N");
                    StringBuilder isExactMatch = new StringBuilder();
                    SnapshotFactory snapshotFactory = new SnapshotFactory(this.getConnectionId());
                    for (int i = 0; i < targetSDIsNoProvisional.getRowCount(); ++i) {
                        String targetSDCId = sdcid;
                        String targetKeyId1 = targetSDIsNoProvisional.getString(i, keyColId1);
                        String targetKeyId2 = targetSDIsNoProvisional.getString(i, keyColId2, "");
                        String targetKeyId3 = targetSDIsNoProvisional.getString(i, keyColId3, "");
                        String sourceCMTPolicyNodeId = sdiSnapshotItemFromPkg.getPolicyNodeId();
                        SDISnapshot targetSnapshot = snapshotFactory.generateSDISnapshot(targetSDCId, targetKeyId1, targetKeyId2, targetKeyId3);
                        SDISnapshot sourceSnapshot = sdiSnapshotItemFromPkg.getSnapshot();
                        HashSet<String> ignoreColList = new HashSet<String>();
                        ignoreColList.add(keyColId1);
                        ignoreColList.add(keyColId2);
                        ignoreColList.add(keyColId3);
                        ignoreColList.add("uuid");
                        boolean hasDiff = SDISnapshotViewer.hasDiff((SapphireConnection)this.connectionInfo, sourceSnapshot, targetSnapshot, false, ignoreColList);
                        isExactMatch.append(";").append(hasDiff ? "N" : "Y");
                    }
                    if (isExactMatch.length() > 0) {
                        isExactMatch.deleteCharAt(0);
                    }
                    parsedDs.setString(row, "__isExactMatch", isExactMatch.toString());
                    if (isExactMatch.indexOf("Y") > -1) {
                        parsedDs.setString(row, this.columnids[this.Index_matchedSDIkeys], "<table width='100%' border=0><tr><td align='left'><font>" + targetSDIKeys + "</font></td><td align='right'><span>" + this.tp.translate("No Changes Found") + "</span></td></tr></table>");
                        parsedDs.setValue(row, this.columnids[this.Index_importoptions], "Do Not Import;Create New Version");
                        parsedDs.setString(row, this.columnids[this.Index_matchedby], "<font>" + matchedByNoProvisional + "</font>");
                    } else {
                        parsedDs.setString(row, this.columnids[this.Index_matchedSDIkeys], "<table width='100%' border=0><tr><td align='left'><font color=darkred>" + targetSDIKeys + "</font></td><td align='right'><img src='WEB-CORE/imageref/finance_business_and_trade/office/notes/48/note_warning.png' title='" + this.tp.translate("A matching SDI was found but it cannot be updated.") + "' width=20 height=20 /></td></tr></table>");
                        parsedDs.setValue(row, this.columnids[this.Index_importoptions], "Create New Version;Do Not Import");
                        parsedDs.setString(row, this.columnids[this.Index_matchedby], "<font color=darkred>" + matchedByNoProvisional + "</font>");
                    }
                } else {
                    parsedDs.setValue(row, this.columnids[this.Index_importoptions], "Create New Version;Do Not Import");
                }
            } else {
                parsedDs.setValue(row, this.columnids[this.Index_importoptions], "Import;Do Not Import");
            }
        }
        catch (Exception e) {
            throw new SapphireException("Exception occurred when rendering row for: " + sdiSnapshotItemFromPkg + ". Reason: " + e.getMessage(), e);
        }
    }

    private String getTargetSDIKeys(String keyColId1, String keyColId2, String keyColId3, String descColName, int keyColsCount, boolean isAutoKey, boolean includeVersionStatus, DataSet targetSDIs) {
        StringBuilder targetSDIKeys = new StringBuilder();
        for (int i = 0; i < targetSDIs.getRowCount(); ++i) {
            String sdikey = targetSDIs.getValue(i, keyColId1);
            if (keyColsCount >= 2) {
                sdikey = sdikey + "|" + targetSDIs.getValue(i, keyColId2);
            }
            if (keyColsCount == 3) {
                sdikey = sdikey + "|" + targetSDIs.getValue(i, keyColId3);
            }
            if (isAutoKey) {
                sdikey = targetSDIs.getValue(i, descColName) + " (" + sdikey + ")";
            }
            if (includeVersionStatus) {
                sdikey = sdikey + this.tp.translate(" Ver: ") + targetSDIs.getString(i, "versionstatus", "");
            }
            targetSDIKeys.append(";").append(sdikey);
        }
        return targetSDIKeys.deleteCharAt(0).toString();
    }

    private void buildParsedPropertyTreeDataSet(PropertyTreeSnapshotItem item, SnapshotPackage snapshotPackage, DataSet parsedDs, List requestedItemList, HashMap<String, HashSet<String>> pTreeNodesIds, HashMap<String, ArrayList<Node>> pTreeNodes) throws SapphireException {
        block63: {
            try {
                HashSet<String> dbTreeNodeIds;
                int row = parsedDs.addRow();
                if (requestedItemList.indexOf(item) >= 0) {
                    parsedDs.setValue(row, this.columnids[this.Index_isRequested], this.tp.translate("Requested"));
                } else {
                    parsedDs.setValue(row, this.columnids[this.Index_isRequested], this.tp.translate("Referenced"));
                }
                parsedDs.setValue(row, this.columnids[this.Index_sdcid], "PropertyTree");
                String sourcesdikeys = "PropertyTree|" + item.getPropertyTreeId() + "|" + item.getNodeId();
                String sourcesdiDisplay = item.getPropertyTreeId() + "|" + item.getNodeId();
                if (!("__FULL".equals(item.getNodeId()) || "__DEFINITION".equals(item.getNodeId()) || "__root".equals(item.getNodeId()))) {
                    if (item.isRenamed()) {
                        sourcesdiDisplay = sourcesdiDisplay + this.tp.translate(" renamed to ") + item.getRenamedNodeId();
                    } else if (!item.isDeleted()) {
                        sourcesdiDisplay = sourcesdiDisplay + this.tp.translate(" extends ") + item.getExtendsNodeId();
                    }
                }
                if (snapshotCache.get(this.getConnectionId()) == null) {
                    snapshotCache.put(this.getConnectionId(), new SoftReference(new HashMap()));
                }
                Snapshot snapshot = snapshotPackage.getSnapshot(item);
                Snapshot preSnapshot = snapshotPackage.getPreSnapshot(item);
                PropertyTreeSnapshot pTreeSnapshot = snapshot != null ? (PropertyTreeSnapshot)snapshot : null;
                PropertyTreeSnapshot pTreePreSnapshot = preSnapshot != null ? (PropertyTreeSnapshot)preSnapshot : null;
                snapshotCache.get(this.getConnectionId()).get().put(sourcesdikeys, snapshot);
                snapshotCache.get(this.getConnectionId()).get().put(sourcesdikeys + "_preimage", preSnapshot);
                parsedDs.setValue(row, this.columnids[this.Index_sourceSDIkeys], sourcesdikeys + "|%|" + sourcesdiDisplay);
                CMTPolicy targetPolicy_PropertyTree = CMTPolicy.getPolicy(this.getConnectionid(), "PropertyTree", "PropertyTree");
                boolean isChangeControlled = "Y".equals(targetPolicy_PropertyTree.getChangeControlledFlag());
                String propertyTreeId = item.getPropertyTreeId();
                DataSet targetSDIs = null;
                boolean matchFound = false;
                boolean isRenameNodeFound = false;
                String matchedby = "";
                DataSet allChangeLogs = null;
                SafeSQL safeSQL = new SafeSQL();
                targetSDIs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT propertytreeid FROM propertytree WHERE propertytreeid = " + safeSQL.addVar(propertyTreeId), safeSQL.getValues());
                if ("__FULL".equals(item.getNodeId()) || "__DEFINITION".equals(item.getNodeId())) {
                    if (targetSDIs.getRowCount() > 0) {
                        matchFound = true;
                        matchedby = this.tp.translate("KeyId1 ID ") + "( propertytreeid )";
                        parsedDs.setValue(row, this.columnids[this.Index_matchedby], matchedby);
                        parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "Y");
                        parsedDs.setValue(row, this.columnids[this.Index_matchedSDIkeys], targetSDIs.getString(0, "propertytreeid"));
                    }
                    if (!matchFound) {
                        matchedby = "";
                        parsedDs.setValue(row, this.columnids[this.Index_matchedby], matchedby);
                        parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "N");
                    }
                    if (isChangeControlled) {
                        safeSQL = new SafeSQL();
                        String sql = "SELECT * FROM changelog WHERE linksdcid='PropertyTree' AND linkkeyid1 = '" + targetSDIs.getString(0, "propertytreeid") + "' AND changelogstatus = '" + "Checked Out" + "' ORDER BY moddt desc";
                        allChangeLogs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues(), false);
                        if ("__FULL".equals(item.getNodeId())) {
                            if (allChangeLogs.getRowCount() > 0) {
                                targetSDIs.setString(0, this.columnids[this.Index_changelogcheck], this.notcheckedin);
                            }
                        } else if ("__DEFINITION".equals(item.getNodeId())) {
                            for (int j = 0; j < allChangeLogs.getRowCount(); ++j) {
                                String checkedOutNodeId = allChangeLogs.getString(j, "propertytreenodeid", "");
                                if (!"__FULL".equals(checkedOutNodeId) && !"__DEFINITION".equals(checkedOutNodeId)) continue;
                                targetSDIs.setString(0, this.columnids[this.Index_changelogcheck], this.notcheckedin);
                            }
                        }
                        if ("".equals(targetSDIs.getString(0, this.columnids[this.Index_changelogcheck], ""))) {
                            targetSDIs.setString(0, this.columnids[this.Index_changelogcheck], this.notexist);
                        }
                        parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], targetSDIs.getString(0, this.columnids[this.Index_changelogcheck]));
                    } else {
                        parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.notchangecontrolled);
                    }
                    StringBuilder importoptions = new StringBuilder();
                    if (item.isDeleted()) {
                        if (matchFound) {
                            importoptions.append(";Delete");
                            importoptions.append(";Do Not Delete");
                            parsedDs.setValue(row, this.columnids[this.Index_overrideoption], "Delete");
                        } else {
                            importoptions.append(";Ignore Delete");
                            String message = "Do Not Import As Target PropertyTree does not Exists.";
                            parsedDs.setValue(row, this.columnids[this.Index_message], message);
                        }
                    } else if (matchFound) {
                        if (pTreeSnapshot.getExists().length() == 0) {
                            importoptions.append(";Override Existing");
                            importoptions.append(";Do Not Import");
                        } else if ("ignore".equals(pTreeSnapshot.getExists())) {
                            String message = "Do Not Import As Target Node Exists Action is Ignore.";
                            parsedDs.setValue(row, this.columnids[this.Index_message], message);
                            importoptions.append(";Do Not Import");
                        } else {
                            String message = "Force Import As Target Node Exists Action is Replace.";
                            parsedDs.setValue(row, this.columnids[this.Index_message], message);
                            importoptions.append(";Override Existing");
                        }
                    } else {
                        importoptions.append(";Import");
                        importoptions.append(";Do Not Import");
                    }
                    parsedDs.setValue(row, this.columnids[this.Index_importoptions], importoptions.substring(1));
                    break block63;
                }
                boolean isNodeMoved = false;
                if (!pTreeNodesIds.containsKey(item.getPropertyTreeId())) {
                    try {
                        HashSet treeNodes = new HashSet();
                        PropertyTree propertyTree = PropertyTreeUtil.getPropertyTree(this.database, item.getPropertyTreeId(), false);
                        ArrayList allNodes = propertyTree.getAllNodes();
                        allNodes.forEach(node -> treeNodes.add(node.getId()));
                        pTreeNodesIds.put(item.getPropertyTreeId(), treeNodes);
                        pTreeNodes.put(item.getPropertyTreeId(), allNodes);
                    }
                    catch (SapphireException e2) {
                        pTreeNodesIds.put(item.getPropertyTreeId(), null);
                        pTreeNodes.put(item.getPropertyTreeId(), null);
                    }
                }
                if ((dbTreeNodeIds = pTreeNodesIds.get(item.getPropertyTreeId())) == null) {
                    matchFound = false;
                    parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "N");
                } else {
                    String itemNodeId = item.getNodeId();
                    if ("__root".equals(itemNodeId)) {
                        matchFound = true;
                        parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "Y");
                        parsedDs.setValue(row, this.columnids[this.Index_matchedSDIkeys], item.getPropertyTreeId() + "|" + itemNodeId);
                        matchedby = this.tp.translate("KeyId1 ID, Node ") + "( propertytreeid, nodeid )";
                    } else if (dbTreeNodeIds.contains(itemNodeId)) {
                        ArrayList<Node> dbTreeNodes = pTreeNodes.get(item.getPropertyTreeId());
                        Node dbNode = dbTreeNodes.stream().filter(e -> e.getId().equals(itemNodeId)).findFirst().get();
                        String dbNodeExtendNodeId = "";
                        Node dbParentNode = SnapshotFactory.getNonComponentParentNode(dbNode);
                        dbNodeExtendNodeId = dbParentNode != null ? dbParentNode.getId() : "root";
                        if (!(item.isDeleted() || item.isRenamed() || item.getExtendsNodeId().equals(dbNodeExtendNodeId))) {
                            isNodeMoved = true;
                        }
                        matchFound = true;
                        matchedby = this.tp.translate("KeyId1 ID, Node ") + "( propertytreeid, nodeid )";
                        parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "Y");
                        parsedDs.setValue(row, this.columnids[this.Index_matchedSDIkeys], item.getPropertyTreeId() + "|" + itemNodeId + this.tp.translate(" extends ") + dbNodeExtendNodeId);
                    } else {
                        matchFound = false;
                        parsedDs.setValue(row, this.columnids[this.Index_hasmatch], "N");
                    }
                    if (item.isRenamed() && dbTreeNodeIds.contains(item.getRenamedNodeId())) {
                        isRenameNodeFound = true;
                    }
                }
                parsedDs.setValue(row, this.columnids[this.Index_matchedby], matchedby);
                if (isChangeControlled) {
                    safeSQL = new SafeSQL();
                    String sql = "SELECT * FROM changelog WHERE linksdcid = 'PropertyTree' AND linkkeyid1 = '" + targetSDIs.getString(0, "propertytreeid") + "' AND changelogstatus = '" + "Checked Out" + "' ORDER BY moddt desc";
                    allChangeLogs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues(), false);
                    for (int j = 0; j < allChangeLogs.getRowCount(); ++j) {
                        String checkedOutNodeId = allChangeLogs.getString(j, "propertytreenodeid", "");
                        if (!"__FULL".equals(checkedOutNodeId) && (!matchFound || !item.getNodeId().equals(checkedOutNodeId))) continue;
                        targetSDIs.setString(0, this.columnids[this.Index_changelogcheck], this.notcheckedin);
                    }
                    if ("".equals(targetSDIs.getString(0, this.columnids[this.Index_changelogcheck], ""))) {
                        targetSDIs.setString(0, this.columnids[this.Index_changelogcheck], this.notexist);
                    }
                    parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], targetSDIs.getString(0, this.columnids[this.Index_changelogcheck]));
                } else {
                    parsedDs.setValue(row, this.columnids[this.Index_changelogcheck], this.notchangecontrolled);
                }
                StringBuilder importoptions = new StringBuilder();
                if (matchFound) {
                    if (item.isDeleted()) {
                        importoptions.append(";Delete");
                        importoptions.append(";Do Not Delete");
                    } else if (item.isRenamed()) {
                        if (!isRenameNodeFound) {
                            importoptions.append(";Rename");
                            importoptions.append(";Do Not Import");
                        } else {
                            String message = "Do Not Import As Target Rename Node already Exists.";
                            parsedDs.setValue(row, this.columnids[this.Index_message], message);
                            importoptions.append(";Do Not Import");
                        }
                    } else if (isNodeMoved) {
                        importoptions.append(";Move & Override Existing");
                        importoptions.append(";Do Not Import");
                    } else if (pTreeSnapshot.getExists().length() == 0) {
                        importoptions.append(";Override Existing");
                        importoptions.append(";Do Not Import");
                    } else if ("ignore".equals(pTreeSnapshot.getExists())) {
                        String message = "Do Not Import As Target Node Exists Action is Ignore.";
                        parsedDs.setValue(row, this.columnids[this.Index_message], message);
                        importoptions.append(";Do Not Import");
                    } else {
                        importoptions.append(";Override Existing");
                        importoptions.append(";Do Not Import");
                    }
                } else if (item.isDeleted()) {
                    String message = "Do Not Import As Target Node for Deletion doesnot Exists.";
                    parsedDs.setValue(row, this.columnids[this.Index_message], message);
                    importoptions.append(";Ignore Delete");
                } else if (item.isRenamed()) {
                    String message = "Do Not Import as Node does not Exist.";
                    parsedDs.setValue(row, this.columnids[this.Index_message], message);
                    importoptions.append(";Do Not Import");
                } else {
                    importoptions.append(";Import");
                    importoptions.append(";Do Not Import");
                }
                parsedDs.setValue(row, this.columnids[this.Index_importoptions], importoptions.substring(1));
            }
            catch (Exception e3) {
                throw new SapphireException("Exception occurred when rendering row for: " + item + ". Reason: " + ErrorUtil.extractMessageFromException(e3, ErrorUtil.isUserAdmin(this.getConnectionId())), e3);
            }
        }
    }

    public static Snapshot getCachedSnapshot(String connectionid, String key) {
        if (snapshotCache.get(connectionid) != null) {
            if (snapshotCache.get(connectionid).get().get(key) != null) {
                return (Snapshot)snapshotCache.get(connectionid).get().get(key);
            }
            Trace.logError("Cached Snapshot Not found", connectionid + ";" + key);
            return null;
        }
        Trace.logError("Cached Snapshot Not found", connectionid);
        return null;
    }
}

