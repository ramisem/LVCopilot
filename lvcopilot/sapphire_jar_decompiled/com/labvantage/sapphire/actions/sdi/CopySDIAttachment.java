/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.services.AttachmentService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CopySDIAttachment
extends BaseAction
implements sapphire.action.CopySDIAttachment {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77314 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String fromsdcid = properties.getProperty("fromsdcid", "");
        String fromkeyid1 = properties.getProperty("fromkeyid1", "");
        String fromkeyid2 = properties.getProperty("fromkeyid2", "");
        String fromkeyid3 = properties.getProperty("fromkeyid3", "");
        String fromcopymodeflag = properties.getProperty("fromcopymodeflag", "");
        String fromattachmentnum = properties.getProperty("fromattachmentnum", "");
        String fromattachmentclass = properties.getProperty("fromattachmentclass", "");
        if (fromsdcid.length() == 0 || fromkeyid1.length() == 0) {
            throw new SapphireException("ATTACHMENT_SERVICE_FAILED", this.getTranslationProcessor().translate("Failed to copy the attachment: Wrong fromsdcid/fromkeyid1 configuration."));
        }
        String[] tosdcid = properties.getProperty("tosdcid", fromsdcid).split(";");
        String[] tokeyid1 = properties.getProperty("tokeyid1", tosdcid.equals(fromsdcid) ? fromkeyid1 : "").split(";");
        String[] tokeyid2 = properties.getProperty("tokeyid2", tosdcid.equals(fromsdcid) ? fromkeyid2 : "").split(";");
        String[] tokeyid3 = properties.getProperty("tokeyid3", tosdcid.equals(fromsdcid) ? fromkeyid3 : "").split(";");
        if (tosdcid.length < tokeyid1.length && tosdcid.length == 1) {
            String tempsdc = tosdcid[0];
            tosdcid = new String[tokeyid1.length];
            for (int i = 0; i < tosdcid.length; ++i) {
                tosdcid[i] = tempsdc;
            }
        }
        if ((tosdcid.length ^ tokeyid1.length) != 0 && tokeyid1[0].length() == 0) {
            throw new SapphireException("ATTACHMENT_SERVICE_FAILED", this.getTranslationProcessor().translate("Failed to copy the attachment: Wrong tosdc/tokeyid1 configuration."));
        }
        PropertyList fromsdcprops = new PropertyList();
        fromsdcprops.setProperty("sdcid", fromsdcid);
        fromsdcprops.setProperty("keyid1", fromkeyid1);
        fromsdcprops.setProperty("keyid2", fromkeyid2);
        fromsdcprops.setProperty("keyid3", fromkeyid3);
        fromsdcprops.setProperty("attachmentnum", fromattachmentnum);
        fromsdcprops.setProperty("attachmentclass", fromattachmentclass);
        fromsdcprops.setProperty("copymodeflag", fromcopymodeflag);
        AttachmentService attachment = new AttachmentService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        for (int i = 0; i < tosdcid.length; ++i) {
            PropertyList tosdcprops = new PropertyList();
            tosdcprops.setProperty("sdcid", tosdcid[i]);
            tosdcprops.setProperty("keyid1", tokeyid1[i]);
            tosdcprops.setProperty("keyid2", tokeyid2.length > i ? tokeyid2[i] : "");
            tosdcprops.setProperty("keyid3", tokeyid3.length > i ? tokeyid3[i] : "");
            try {
                attachment.copySDIAttachment(fromsdcprops, tosdcprops);
                continue;
            }
            catch (ServiceException e) {
                throw new SapphireException("ATTACHMENT_SERVICE_FAILED", this.getTranslationProcessor().translate("Failed to copy the attachment:") + " " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }

    public static void copyAttachmentClasses(boolean newVersion, String sdcid, PropertyList attachmentPropsFromPolicy, SapphireConnection sapphireConnection, String templatekeyid1, String templatekeyid2, String templatekeyid3, String newkeyid1, String newkeyid2, String newkeyid3) throws SapphireException {
        PropertyListCollection classes = attachmentPropsFromPolicy.containsKey("classes") ? attachmentPropsFromPolicy.getCollectionNotNull("classes") : attachmentPropsFromPolicy.getCollectionNotNull("attachmentclasses");
        StringBuffer attachmentClassBuffer = new StringBuffer();
        StringBuffer attachmentClassCopyModeBuffer = new StringBuffer();
        for (int i = 0; i < classes.size(); ++i) {
            PropertyList attachmentClass = classes.getPropertyList(i);
            attachmentClassBuffer.append(";").append(attachmentClass.getProperty("class"));
            String copyClassMode = "";
            copyClassMode = newVersion ? attachmentClass.getProperty("upversioncopymode", "Do Not Copy") : attachmentClass.getProperty("copysdicopymode", "Do Not Copy");
            if (copyClassMode.equals("Editable Copy")) {
                attachmentClassCopyModeBuffer.append(";").append("E");
                continue;
            }
            if (copyClassMode.equals("Non Editable Copy")) {
                attachmentClassCopyModeBuffer.append(";").append("F");
                continue;
            }
            if (copyClassMode.equals("Linked Reference")) {
                attachmentClassCopyModeBuffer.append(";").append("L");
                continue;
            }
            attachmentClassCopyModeBuffer.append(";").append("N");
        }
        if (attachmentClassBuffer.length() > 0) {
            String[] newKeyId1Array = StringUtil.split(newkeyid1, ";", true);
            String[] newKeyId2Array = StringUtil.split(newkeyid2, ";", true);
            String[] newKeyId3Array = StringUtil.split(newkeyid3, ";", true);
            AttachmentService attachment = new AttachmentService(sapphireConnection);
            for (int i = 0; i < newKeyId1Array.length; ++i) {
                PropertyList fromsdcprops = new PropertyList();
                fromsdcprops.setProperty("sdcid", sdcid);
                fromsdcprops.setProperty("keyid1", templatekeyid1);
                fromsdcprops.setProperty("keyid2", templatekeyid2.length() > 0 ? templatekeyid2 : "");
                fromsdcprops.setProperty("keyid3", templatekeyid3.length() > 0 ? templatekeyid3 : "");
                fromsdcprops.setProperty("attachmentclass", attachmentClassBuffer.substring(1));
                fromsdcprops.setProperty("copymodeflag", attachmentClassCopyModeBuffer.substring(1));
                PropertyList tosdcprops = new PropertyList();
                tosdcprops.setProperty("sdcid", sdcid);
                tosdcprops.setProperty("keyid1", newKeyId1Array[i]);
                tosdcprops.setProperty("keyid2", newkeyid2.length() > 0 ? newKeyId2Array[i] : "");
                tosdcprops.setProperty("keyid3", newkeyid3.length() > 0 ? newKeyId3Array[i] : "");
                try {
                    attachment.copySDIAttachment(fromsdcprops, tosdcprops);
                    continue;
                }
                catch (ServiceException e) {
                    throw new SapphireException("ATTACHMENT_SERVICE_FAILED", "Failed to copy the attachment: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
                }
            }
        }
    }

    public static void copyIntraSDIAttachmentClasses(String sdcid, PropertyList attachmentPropsFromPolicy, SapphireConnection sapphireConnection, String templatekeyid1, String templatekeyid2, String templatekeyid3, String newkeyid1, String newkeyid2, String newkeyid3) throws SapphireException {
        PropertyList plAttachmentDetails = attachmentPropsFromPolicy.getPropertyListNotNull("attachmentdetails");
        PropertyListCollection classes = plAttachmentDetails.getCollectionNotNull("attachmentclasses");
        StringBuffer attachmentClassBuffer = new StringBuffer();
        StringBuffer attachmentClassCopyModeBuffer = new StringBuffer();
        for (int i = 0; i < classes.size(); ++i) {
            PropertyList attachmentClass = classes.getPropertyList(i);
            if (attachmentClass.size() == 0) continue;
            attachmentClassBuffer.append(";").append(attachmentClass.getProperty("attachmentclass"));
            String copyClassMode = "";
            copyClassMode = attachmentClass.getProperty("attachmentcopyoption", "Do Not Copy");
            if (copyClassMode.equals("Editable Copy")) {
                attachmentClassCopyModeBuffer.append(";").append("E");
                continue;
            }
            if (copyClassMode.equals("Non Editable Copy")) {
                attachmentClassCopyModeBuffer.append(";").append("F");
                continue;
            }
            if (copyClassMode.equals("Linked Reference")) {
                attachmentClassCopyModeBuffer.append(";").append("L");
                continue;
            }
            if (copyClassMode.equals("Same As Source")) {
                attachmentClassCopyModeBuffer.append(";").append("S");
                continue;
            }
            attachmentClassCopyModeBuffer.append(";").append("N");
        }
        if (attachmentClassBuffer.length() > 0) {
            String[] newKeyId1Array = StringUtil.split(newkeyid1, ";", true);
            String[] newKeyId2Array = StringUtil.split(newkeyid2, ";", true);
            String[] newKeyId3Array = StringUtil.split(newkeyid3, ";", true);
            AttachmentService attachment = new AttachmentService(sapphireConnection);
            for (int i = 0; i < newKeyId1Array.length; ++i) {
                PropertyList fromsdcprops = new PropertyList();
                fromsdcprops.setProperty("sdcid", sdcid);
                fromsdcprops.setProperty("keyid1", templatekeyid1);
                fromsdcprops.setProperty("keyid2", templatekeyid2.length() > 0 ? templatekeyid2 : "");
                fromsdcprops.setProperty("keyid3", templatekeyid3.length() > 0 ? templatekeyid3 : "");
                fromsdcprops.setProperty("attachmentclass", attachmentClassBuffer.substring(1));
                fromsdcprops.setProperty("copymodeflag", attachmentClassCopyModeBuffer.substring(1));
                PropertyList tosdcprops = new PropertyList();
                tosdcprops.setProperty("sdcid", sdcid);
                tosdcprops.setProperty("keyid1", newKeyId1Array[i]);
                tosdcprops.setProperty("keyid2", newkeyid2.length() > 0 ? newKeyId2Array[i] : "");
                tosdcprops.setProperty("keyid3", newkeyid3.length() > 0 ? newKeyId3Array[i] : "");
                try {
                    attachment.copySDIAttachment(fromsdcprops, tosdcprops);
                    continue;
                }
                catch (ServiceException e) {
                    throw new SapphireException("ATTACHMENT_SERVICE_FAILED", "Failed to copy the attachment: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
                }
            }
        }
    }

    public static void copyDownSDIAttachment(PropertyList sdcProps, DataSet primaryData, DataSet beforeEditPrimaryData, ArrayList<PropertyList> copyDownPolicy, SapphireConnection sapphireConnection) throws SapphireException {
        String sdcid = sdcProps.getProperty("sdcid");
        String keycolid1 = sdcProps.getProperty("keycolid1");
        String keycolid2 = sdcProps.getProperty("keycolid2");
        String keycolid3 = sdcProps.getProperty("keycolid3");
        AttachmentService attachment = new AttachmentService(sapphireConnection);
        for (PropertyList copyFrom : copyDownPolicy) {
            String fromSdc = copyFrom.getProperty("sdcid");
            String fkcolumnid = copyFrom.getProperty("fkcolumnid", "");
            String fkcolumnid2 = copyFrom.getProperty("fkcolumnid2", "");
            String fkcolumnid3 = copyFrom.getProperty("fkcolumnid3", "");
            PropertyList attachmentdetails = copyFrom.getPropertyListNotNull("attachmentdetails");
            String copyall = attachmentdetails.getProperty("copyall", "Y");
            String copymode = "";
            StringBuffer attachmentclassBuffer = new StringBuffer();
            StringBuffer classcopymodeBuffer = new StringBuffer();
            if (copyall.equals("Y")) {
                String copyallmode = attachmentdetails.getProperty("copyallmode", "Do Not Copy");
                copymode = copyallmode.equals("Editable Copy") ? "E" : (copyallmode.equals("Non Editable Copy") ? "F" : (copyallmode.equals("Linked Reference") ? "L" : "N"));
            } else {
                PropertyListCollection copyclasses = attachmentdetails.getCollection("copyclasses");
                for (int i = 0; i < copyclasses.size(); ++i) {
                    attachmentclassBuffer.append(";").append(copyclasses.getPropertyList(i).getProperty("class"));
                    String copyclassmode = copyclasses.getPropertyList(i).getProperty("copymode", "Do Not Copy");
                    if (copyclassmode.equals("Editable Copy")) {
                        classcopymodeBuffer.append(";").append("E");
                        continue;
                    }
                    if (copyclassmode.equals("Non Editable Copy")) {
                        classcopymodeBuffer.append(";").append("F");
                        continue;
                    }
                    if (copyclassmode.equals("Linked Reference")) {
                        classcopymodeBuffer.append(";").append("L");
                        continue;
                    }
                    classcopymodeBuffer.append(";").append("N");
                }
                if (attachmentclassBuffer.length() > 0) {
                    copymode = classcopymodeBuffer.substring(1);
                }
            }
            if (fkcolumnid.length() <= 0 || copymode.length() <= 0) continue;
            for (int i = 0; i < primaryData.getRowCount(); ++i) {
                boolean copy = true;
                if (beforeEditPrimaryData != null) {
                    if (SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, "templateflag", "N").equals("Y")) {
                        copy = false;
                    } else {
                        String newValue;
                        String oldValue = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, fkcolumnid, "");
                        if (oldValue.equals(newValue = primaryData.getValue(i, fkcolumnid, ""))) {
                            if (fkcolumnid.length() > 0 && fkcolumnid2.length() > 0) {
                                oldValue = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, fkcolumnid2, "");
                                if (oldValue.equals(newValue = primaryData.getValue(i, fkcolumnid2, ""))) {
                                    if (fkcolumnid.length() > 0 && fkcolumnid3.length() > 0) {
                                        oldValue = SdiInfo.getOldPrimaryValue(sdcProps, keycolid1, keycolid2, keycolid3, primaryData, beforeEditPrimaryData, i, fkcolumnid3, "");
                                        if (oldValue.equals(newValue = primaryData.getValue(i, fkcolumnid3, ""))) {
                                            copy = false;
                                        }
                                    } else {
                                        copy = false;
                                    }
                                }
                            } else {
                                copy = false;
                            }
                        }
                    }
                }
                if (!copy) continue;
                PropertyList fromsdcprops = new PropertyList();
                fromsdcprops.setProperty("sdcid", fromSdc);
                fromsdcprops.setProperty("keyid1", primaryData.getValue(i, fkcolumnid, ""));
                fromsdcprops.setProperty("keyid2", fkcolumnid2.length() > 0 ? primaryData.getValue(i, fkcolumnid2, "") : "");
                fromsdcprops.setProperty("keyid3", fkcolumnid3.length() > 0 ? primaryData.getValue(i, fkcolumnid3, "") : "");
                if (attachmentclassBuffer.length() > 0) {
                    fromsdcprops.setProperty("attachmentclass", attachmentclassBuffer.substring(1));
                }
                fromsdcprops.setProperty("copymodeflag", copymode);
                PropertyList tosdcprops = new PropertyList();
                tosdcprops.setProperty("sdcid", sdcid);
                tosdcprops.setProperty("keyid1", primaryData.getValue(i, keycolid1));
                tosdcprops.setProperty("keyid2", primaryData.getValue(i, keycolid2, "(null)"));
                tosdcprops.setProperty("keyid3", primaryData.getValue(i, keycolid3, "(null)"));
                try {
                    attachment.copySDIAttachment(fromsdcprops, tosdcprops);
                    continue;
                }
                catch (ServiceException e) {
                    throw new SapphireException("ATTACHMENT_SERVICE_FAILED", "Failed to copy the attachment: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(sapphireConnection.getConnectionId())), e);
                }
            }
        }
    }
}

