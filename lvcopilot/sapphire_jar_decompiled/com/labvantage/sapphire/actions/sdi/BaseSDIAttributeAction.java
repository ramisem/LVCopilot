/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.SdiInfo;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDIAttribute;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.actions.sdi.DeleteSDIAttribute;
import com.labvantage.sapphire.actions.sdi.EditSDIAttribute;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.MiscUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class BaseSDIAttributeAction
extends BaseAction {
    public static final String SKIPPED_DUPLICATES = "duplicates";
    public static final String SKIPPED_MASTERLIST = "masterlist";
    public static final String ATTRIBUTEIDCOLUMN = "attributeid";

    private static DataSet getSDIAttachmentId(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, QueryProcessor qp) throws SapphireException {
        if (keyid2 == null || keyid2.length() == 0) {
            keyid2 = "(null)";
        }
        if (keyid3 == null || keyid3.length() == 0) {
            keyid3 = "(null)";
        }
        return qp.getPreparedSqlDataSet("SELECT sdiattachmentid FROM sdiattachment WHERE sdcid=? AND keyid1=? AND keyid2=? AND keyid3=? AND attachmentnum=?", new Object[]{sdcid, keyid1, keyid2, keyid3, attachmentNum});
    }

    public static void addAttachmentMetaData(PropertyList metadata, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, QueryProcessor qp, SDCProcessor sdcProcessor, ActionProcessor ap) throws SapphireException {
        DataSet ds = BaseSDIAttributeAction.getSDIAttachmentId(sdcid, keyid1, keyid2, keyid3, attachmentNum, qp);
        if (ds != null && ds.size() > 0) {
            BaseSDIAttributeAction.addMetaData(metadata, "SDIAttachment", ds.getValue(0, "sdiattachmentid"), "", "", sdcProcessor, ap);
        }
    }

    public static void addMetaData(PropertyList metadata, String sdcid, String keyid1, String keyid2, String keyid3, SDCProcessor sdcProcessor, ActionProcessor ap) throws SapphireException {
        BaseSDIAttributeAction.addMetaData(metadata, sdcid, keyid1, keyid2, keyid3, true, sdcProcessor, ap);
    }

    public static void addMetaData(PropertyList metadata, String sdcid, String keyid1, String keyid2, String keyid3, boolean updateable, SDCProcessor sdcProcessor, ActionProcessor ap) throws SapphireException {
        PropertyListCollection masteratts = sdcProcessor.getPropertyList(sdcid).getCollection("attributes");
        Iterator it = metadata.keySet().iterator();
        StringBuilder attributes = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder datatypes = new StringBuilder();
        StringBuilder instructionflags = new StringBuilder();
        StringBuilder updateableflags = new StringBuilder();
        DateTimeUtil dateTimeUtil = null;
        try {
            dateTimeUtil = new DateTimeUtil(new ConnectionProcessor(ap.getConnectionid()).getConnectionInfo(ap.getConnectionid()));
        }
        catch (Throwable e) {
            dateTimeUtil = new DateTimeUtil(Calendar.getInstance().getTimeZone());
        }
        while (it.hasNext()) {
            String key = it.next().toString();
            if (attributes.length() > 0) {
                attributes.append(";");
                values.append(";");
                datatypes.append(";");
                instructionflags.append(";");
                updateableflags.append(";");
            }
            attributes.append(key);
            String value = metadata.getProperty(key);
            boolean hasMultipleInstances = false;
            if (value.startsWith("{|") && value.endsWith("|}")) {
                value = value.substring(2, value.length() - 2);
                hasMultipleInstances = true;
            }
            if (hasMultipleInstances) {
                String[] valueArr = StringUtil.split(value, ";");
                int count = 0;
                for (String textValue : valueArr) {
                    if (count++ > 0 && attributes.length() > 0) {
                        attributes.append(";");
                        values.append(";");
                        datatypes.append(";");
                        instructionflags.append(";");
                        updateableflags.append(";");
                        attributes.append(key);
                    }
                    BaseSDIAttributeAction.setSDIAttibuteProperties(key, textValue, values, datatypes, updateableflags, instructionflags, masteratts, dateTimeUtil, updateable);
                }
                continue;
            }
            BaseSDIAttributeAction.setSDIAttibuteProperties(key, value, values, datatypes, updateableflags, instructionflags, masteratts, dateTimeUtil, updateable);
        }
        if (attributes.length() > 0) {
            PropertyList addSDIAttribute = new PropertyList();
            addSDIAttribute.setProperty("sdcid", sdcid);
            addSDIAttribute.setProperty("keyid1", keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                addSDIAttribute.setProperty("keyid2", keyid2);
            }
            if (keyid3 != null && keyid3.length() > 0) {
                addSDIAttribute.setProperty("keyid3", keyid3);
            }
            addSDIAttribute.setProperty("datatype", datatypes.toString());
            addSDIAttribute.setProperty(ATTRIBUTEIDCOLUMN, attributes.toString());
            addSDIAttribute.setProperty("value", values.toString());
            addSDIAttribute.setProperty("instructionflag", instructionflags.toString());
            addSDIAttribute.setProperty("instructiontext", attributes.toString());
            if (!updateable) {
                addSDIAttribute.setProperty("updatable", updateableflags.toString());
            }
            ap.processAction("AddSDIAttribute", "1", addSDIAttribute);
        }
    }

    public static void setSDIAttibuteProperties(String key, String value, StringBuilder values, StringBuilder datatypes, StringBuilder updateableflags, StringBuilder instructionflags, PropertyListCollection masteratts, DateTimeUtil dateTimeUtil, boolean updateable) {
        String datatype = "S";
        if (!value.contains(";")) {
            try {
                double d = Double.parseDouble(value);
                datatype = d > 2.147483646E9 || d < -2.147483646E9 ? "S" : "N";
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (!datatype.equalsIgnoreCase("N")) {
                try {
                    if (dateTimeUtil.getCalendar(value) != null) {
                        datatype = "D";
                    }
                }
                catch (Exception exception) {}
            }
        } else {
            value = StringUtil.replaceAll(value, ";", "#semicolon#");
        }
        values.append(value);
        datatypes.append(datatype);
        updateableflags.append(updateable ? "Y" : "N");
        if (masteratts.find(ATTRIBUTEIDCOLUMN, key) != null) {
            instructionflags.append("N");
        } else {
            instructionflags.append("M");
        }
    }

    public static PropertyList getAttachmentMetaData(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, QueryProcessor qp, ActionProcessor ap) throws SapphireException {
        DataSet ds = BaseSDIAttributeAction.getSDIAttachmentId(sdcid, keyid1, keyid2, keyid3, attachmentNum, qp);
        if (ds != null && ds.size() > 0) {
            return BaseSDIAttributeAction.getMetaData("SDIAttachment", ds.getValue(0, "sdiattachmentid"), "", "", ap);
        }
        return new PropertyList();
    }

    public static PropertyList getMetaData(String sdcid, String keyid1, String keyid2, String keyid3, ActionProcessor ap) throws SapphireException {
        PropertyList get = new PropertyList();
        get.setProperty("sdcid", sdcid);
        get.setProperty("keyid1", keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            get.setProperty("keyid2", keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            get.setProperty("keyid3", keyid3);
        }
        get.setProperty("separator", "#semicolon#");
        ap.processAction("GetSDIAttribute", "1", get);
        String[] attributeid = StringUtil.split(get.getProperty(ATTRIBUTEIDCOLUMN), "#semicolon#");
        String[] values = StringUtil.split(get.getProperty("value"), "#semicolon#");
        if (attributeid.length > 0) {
            if (values.length >= attributeid.length) {
                if (attributeid.length != values.length) {
                    Trace.logWarn("Invalid attributes found. You may have a ; in one of your attribute values.");
                }
                PropertyList out = new PropertyList();
                for (int i = 0; i < attributeid.length; ++i) {
                    out.setProperty(attributeid[i], values[i]);
                }
                return out;
            }
            throw new SapphireException("Invalid attributes found. Attributes do not match values.");
        }
        return new PropertyList();
    }

    public static String getAttributeType(AttributeType type) {
        switch (type) {
            case sdc: {
                return "SDC";
            }
            case adhoc: {
                return "Adhoc";
            }
            case template: {
                return "Template";
            }
            case link: {
                return "Link";
            }
            case linkdef: {
                return "LinkDef";
            }
        }
        return "Adhoc";
    }

    public static AttributeType getAttributeTypeFromString(String type) {
        if (type.equalsIgnoreCase("SDC")) {
            return AttributeType.sdc;
        }
        if (type.equalsIgnoreCase("Adhoc")) {
            return AttributeType.adhoc;
        }
        if (type.equalsIgnoreCase("Template")) {
            return AttributeType.template;
        }
        if (type.equalsIgnoreCase("Link")) {
            return AttributeType.link;
        }
        if (type.equalsIgnoreCase("LinkDef")) {
            return AttributeType.linkdef;
        }
        return AttributeType.adhoc;
    }

    public static void templateCopyDownAttibutes(DataSet attributeData, DataSet existingattributedata, DataSet primaryData, DataSet templateattributedata, PropertyList sdcprops, String keycolid1, String keycolid2, String keycolid3, boolean copyAll, boolean fromTemplateSDI, HashMap<String, ArrayList<String>> skipped, String rsetid, QueryProcessor qp, M18NUtil m18n, Logger logger) {
        String sdcid = sdcprops.getProperty("sdcid");
        boolean allowattributes = sdcprops.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y");
        if (allowattributes) {
            int row;
            DataSet linkdeftemplateattibutedata;
            HashMap<String, String> hmfilt;
            if (templateattributedata.isValidColumn("usersequence")) {
                templateattributedata.sort("usersequence");
            }
            if (copyAll) {
                if (fromTemplateSDI) {
                    hmfilt = new HashMap<String, String>();
                    hmfilt.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                    hmfilt.put("attributesdcid", sdcid);
                    linkdeftemplateattibutedata = templateattributedata.getFilteredDataSet(hmfilt, true);
                } else {
                    linkdeftemplateattibutedata = templateattributedata;
                }
                if (linkdeftemplateattibutedata.getRowCount() > 0) {
                    logger.debug("Adding template data to attribute data...");
                    int rows = primaryData.getRowCount();
                    for (row = 0; row < rows; ++row) {
                        if (StringUtil.getLen(keycolid1) > 0L) {
                            linkdeftemplateattibutedata.setString(-1, "keyid1", primaryData.getString(row, keycolid1));
                        }
                        if (StringUtil.getLen(keycolid2) > 0L) {
                            linkdeftemplateattibutedata.setString(-1, "keyid2", primaryData.getString(row, keycolid2));
                        }
                        if (StringUtil.getLen(keycolid3) > 0L) {
                            linkdeftemplateattibutedata.setString(-1, "keyid3", primaryData.getString(row, keycolid3));
                        }
                        attributeData.copyRow(linkdeftemplateattibutedata, -1, 1);
                    }
                }
            }
            if (fromTemplateSDI) {
                hmfilt = new HashMap();
                hmfilt.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                hmfilt.put("attributesdcid", sdcid);
                linkdeftemplateattibutedata = templateattributedata.getFilteredDataSet(hmfilt);
                if (linkdeftemplateattibutedata.getRowCount() > 0) {
                    BaseSDIAttributeAction.syncroniseDataSet(attributeData, true);
                    if (existingattributedata == null) {
                        if (rsetid != null && rsetid.length() > 0) {
                            existingattributedata = BaseSDIAttributeAction.getExistingAttributes(rsetid, qp, logger);
                        } else {
                            logger.warn("No rsetid or existing data provided.");
                        }
                    }
                    if (existingattributedata != null) {
                        PropertyListCollection attributedefs = sdcprops.getCollection("attributes");
                        for (row = 0; row < primaryData.getRowCount(); ++row) {
                            for (int ar = 0; ar < linkdeftemplateattibutedata.getRowCount(); ++ar) {
                                String currentattribute = linkdeftemplateattibutedata.getString(ar, ATTRIBUTEIDCOLUMN);
                                String current_instructionflag = linkdeftemplateattibutedata.getValue(ar, "instructionflag");
                                PropertyList attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                                if (attributedef != null || current_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("O")) {
                                    int instance = BaseSDIAttributeAction.getDuplicateInstance(currentattribute, sdcid, primaryData.getString(row, keycolid1), primaryData.getString(row, keycolid2, ""), primaryData.getString(row, keycolid3, ""), sdcid, attributeData, existingattributedata, attributedef, skipped, logger);
                                    if (instance > 0) {
                                        attributeData.copyRow(linkdeftemplateattibutedata, ar, 1);
                                        int newrow = attributeData.getRowCount() - 1;
                                        BaseSDIAttributeAction.doCopyValues(attributeData, newrow, sdcid, instance, keycolid1.length() > 0 ? primaryData.getString(row, keycolid1) : "", keycolid2.length() > 0 ? primaryData.getString(row, keycolid2) : "", keycolid3.length() > 0 ? primaryData.getString(row, keycolid3) : "", linkdeftemplateattibutedata, ar, m18n, logger);
                                        continue;
                                    }
                                    logger.debug("Instance id is 0 which means duplicate was found for non duplicate attribute and thus attribute is being skipped.");
                                    continue;
                                }
                                if (skipped != null) {
                                    ArrayList<Object> ml;
                                    if (skipped.containsKey(SKIPPED_MASTERLIST)) {
                                        ml = skipped.get(SKIPPED_MASTERLIST);
                                    } else {
                                        ml = new ArrayList();
                                        skipped.put(SKIPPED_MASTERLIST, ml);
                                    }
                                    ml.add(currentattribute);
                                }
                                logger.warn("Attribute " + currentattribute + " is not in " + sdcid + " master list and was therefore skipped.");
                            }
                        }
                    } else {
                        logger.error("Could not obtain attribute data for SDI.");
                    }
                } else {
                    logger.debug("No template attributes to copy.");
                }
            }
        } else {
            logger.warn("SDC " + sdcid + " does not allow attributes and therefore template attributes will not be copied.");
        }
    }

    public static void copyAdocToTemplateAttibutes(DataSet attributeData, DataSet existingattributedata, DataSet primaryData, DataSet templateattributedata, PropertyList sdcprops, String keycolid1, String keycolid2, String keycolid3, HashMap<String, ArrayList<String>> skipped, String rsetid, QueryProcessor qp, M18NUtil m18n, Logger logger) {
        String sdcid = sdcprops.getProperty("sdcid");
        boolean allowattributes = sdcprops.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y");
        if (allowattributes) {
            if (templateattributedata.isValidColumn("usersequence")) {
                templateattributedata.sort("usersequence");
            }
            HashMap<String, String> hmfilt = new HashMap<String, String>();
            hmfilt.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.adhoc));
            hmfilt.put("attributesdcid", sdcid);
            DataSet tocopy = templateattributedata.getFilteredDataSet(hmfilt, false);
            if (tocopy.getRowCount() > 0) {
                for (int i = 0; i < tocopy.getRowCount(); ++i) {
                    tocopy.setValue(i, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                    tocopy.setValue(i, "sourcesdcid", sdcid);
                    if (tocopy.getValue(i, "textvalue", "").length() > 0) {
                        tocopy.setValue(i, "defaulttextvalue", tocopy.getValue(i, "textvalue"));
                        tocopy.setValue(i, "textvalue", null);
                    }
                    if (tocopy.getValue(i, "numericvalue", "").length() > 0) {
                        tocopy.setNumber(i, "defaultnumericvalue", tocopy.getBigDecimal(i, "numericvalue"));
                        tocopy.setValue(i, "numericvalue", null);
                    }
                    if (tocopy.getValue(i, "datevalue", "").length() > 0) {
                        tocopy.setDate(i, "defaultdatevalue", tocopy.getCalendar(i, "datevalue"));
                        tocopy.setValue(i, "datevalue", null);
                    }
                    if (tocopy.getValue(i, "clobvalue", "").length() <= 0) continue;
                    tocopy.setClob(i, "defaultclobvalue", tocopy.getClob(i, "clobvalue"));
                    tocopy.setValue(i, "clobvalue", null);
                }
                logger.debug("Adding template data to attribute data...");
                int rows = primaryData.getRowCount();
                for (int row = 0; row < rows; ++row) {
                    if (StringUtil.getLen(keycolid1) > 0L) {
                        tocopy.setString(-1, "keyid1", primaryData.getString(row, keycolid1));
                    }
                    if (StringUtil.getLen(keycolid2) > 0L) {
                        tocopy.setString(-1, "keyid2", primaryData.getString(row, keycolid2));
                    }
                    if (StringUtil.getLen(keycolid3) > 0L) {
                        tocopy.setString(-1, "keyid3", primaryData.getString(row, keycolid3));
                    }
                    attributeData.copyRow(tocopy, -1, 1);
                }
            }
        } else {
            logger.warn("SDC " + sdcid + " does not allow attributes and therefore template attributes will not be copied.");
        }
    }

    private static void doCopyValues(DataSet attributeData, int newrow, String sdcid, int instance, String keyid1, String keyid2, String keyid3, DataSet copyingFromAttributes, int copyingFromRow, M18NUtil m18n, Logger logger) {
        BaseSDIAttributeAction.doCopyValues(attributeData, newrow, sdcid, instance, keyid1, keyid2, keyid3, copyingFromAttributes, copyingFromRow, m18n, logger, false);
    }

    private static void doCopyValues(DataSet attributeData, int newrow, String sdcid, int instance, String keyid1, String keyid2, String keyid3, DataSet copyingFromAttributes, int copyingFromRow, M18NUtil m18n, Logger logger, boolean copyValues) {
        attributeData.setString(newrow, "sourcekeyid1", attributeData.getValue(newrow, "keyid1", "(null)"));
        attributeData.setString(newrow, "sourcekeyid2", attributeData.getValue(newrow, "keyid2", "(null)"));
        attributeData.setString(newrow, "sourcekeyid3", attributeData.getValue(newrow, "keyid3", "(null)"));
        attributeData.setNumber(newrow, "sourceattributeinstance", attributeData.getBigDecimal(newrow, "attributeinstance"));
        String instructionflag = attributeData.getValue(newrow, "instructionflag", "N");
        attributeData.setValue(newrow, "instructionflag", instructionflag);
        attributeData.setValue(newrow, "instructiontext", "");
        attributeData.setString(newrow, "sdcid", sdcid);
        if (StringUtil.getLen(keyid1) > 0L) {
            attributeData.setString(newrow, "keyid1", keyid1);
        }
        if (StringUtil.getLen(keyid2) > 0L) {
            attributeData.setString(newrow, "keyid2", keyid2);
        } else {
            attributeData.setString(newrow, "keyid2", "(null)");
        }
        if (StringUtil.getLen(keyid3) > 0L) {
            attributeData.setString(newrow, "keyid3", keyid3);
        } else {
            attributeData.setString(newrow, "keyid3", "(null)");
        }
        attributeData.setString(newrow, "attributesourcetype", AddSDIAttribute.getAttributeType(AttributeType.link));
        String atdatatype = attributeData.getValue(newrow, "datatype", "S");
        if (atdatatype.equalsIgnoreCase("n")) {
            try {
                BigDecimal v = copyingFromAttributes.getBigDecimal(copyingFromRow, "numericvalue");
                BigDecimal n = copyingFromAttributes.getBigDecimal(copyingFromRow, "defaultnumericvalue");
                if (copyValues && v != null) {
                    attributeData.setNumber(newrow, "numericvalue", v);
                    attributeData.setValue(newrow, "textvalue", copyingFromAttributes.getValue(copyingFromRow, "textvalue"));
                } else {
                    attributeData.setNumber(newrow, "numericvalue", n);
                    attributeData.setValue(newrow, "textvalue", copyingFromAttributes.getValue(copyingFromRow, "defaulttextvalue"));
                }
            }
            catch (Exception e) {
                attributeData.setValue(newrow, "numericvalue", "");
            }
            attributeData.setValue(newrow, "defaulttextvalue", "");
            attributeData.setValue(newrow, "defaultnumericvalue", "");
        } else if (atdatatype.equalsIgnoreCase("d") || atdatatype.equalsIgnoreCase("o")) {
            Calendar v = copyingFromAttributes.getCalendar(copyingFromRow, "datevalue");
            if (copyValues && v != null) {
                attributeData.setDate(newrow, "datevalue", v);
            } else {
                String date = BaseSDIAttributeAction.getRealDate(copyingFromAttributes.getValue(copyingFromRow, "defaulttextvalue"), copyingFromAttributes.getValue(copyingFromRow, "defaultdatevalue"), m18n, atdatatype.equalsIgnoreCase("d"), logger);
                attributeData.setValue(newrow, "datevalue", date);
            }
            attributeData.setValue(newrow, "defaultdatevalue", "");
            attributeData.setValue(newrow, "defaulttextvalue", "");
        } else if (atdatatype.equalsIgnoreCase("c")) {
            String v = copyingFromAttributes.getClob(copyingFromRow, "clobvalue");
            if (copyValues && v != null) {
                attributeData.setValue(newrow, "clobvalue", v);
            } else {
                attributeData.setClob(newrow, "clobvalue", copyingFromAttributes.getClob(copyingFromRow, "defaultclobvalue"));
            }
            attributeData.setValue(newrow, "defaultclobvalue", "");
        } else {
            String v = copyingFromAttributes.getValue(copyingFromRow, "textvalue");
            if (copyValues && v != null) {
                attributeData.setValue(newrow, "textvalue", v);
            } else {
                attributeData.setValue(newrow, "textvalue", copyingFromAttributes.getValue(copyingFromRow, "defaulttextvalue"));
            }
            attributeData.setValue(newrow, "defaulttextvalue", "");
        }
        attributeData.setNumber(newrow, "attributeinstance", instance);
    }

    protected static void copyDownAttributes(DataSet attributeData, DataSet existingattributedata, DataSet primaryData, DataSet beforePrimaryData, String[] columnIds, PropertyList targetsdc, String keycolid1, String keycolid2, String keycolid3, ArrayList<PropertyList> copyDownPolicy, HashMap<String, ArrayList<String>> skipped, String rsetid, QueryProcessor qp, SDIProcessor sdiProc, M18NUtil m18n, ConfigurationProcessor cp, SDCProcessor sdcProc, ConnectionProcessor connectionProcessor, Logger logger) throws SapphireException {
        String sdcid = targetsdc.getProperty("sdcid");
        boolean allowattributestarget = targetsdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y");
        PropertyListCollection links = targetsdc.getCollection("links");
        if (links != null) {
            if (copyDownPolicy == null) {
                logger.debug("No copydown policy provided therefore generate.");
                copyDownPolicy = BaseSDIAction.getCopyDownPolicy(null, targetsdc, new String[]{"attributes"}, cp).get("attributes");
            }
            if (copyDownPolicy != null && copyDownPolicy.size() > 0) {
                BaseSDIAttributeAction.syncroniseDataSet(attributeData, true);
                if (existingattributedata == null) {
                    if (rsetid != null && rsetid.length() > 0) {
                        existingattributedata = BaseSDIAttributeAction.getExistingAttributes(rsetid, qp, logger);
                    } else {
                        logger.warn("No rsetid or existing data provided.");
                    }
                }
                if (existingattributedata != null) {
                    PropertyListCollection attributedefs = targetsdc.getCollection("attributes");
                    for (String columnId : columnIds) {
                        String currentcol = columnId.toLowerCase();
                        PropertyList cdlink = null;
                        PropertyList copyDownDef = null;
                        for (int l = 0; l < links.size(); ++l) {
                            PropertyList link = links.getPropertyList(l);
                            if (!link.getProperty("linktype").equalsIgnoreCase("F") || !link.getProperty("sdccolumnid").equalsIgnoreCase(currentcol)) continue;
                            cdlink = link;
                            break;
                        }
                        if (cdlink == null) continue;
                        for (PropertyList plcd : copyDownPolicy) {
                            if (!plcd.getProperty("sdcid").equalsIgnoreCase(cdlink.getProperty("linksdcid")) || !plcd.getProperty("fkcolumnid").equalsIgnoreCase(currentcol) || !plcd.getProperty("copyattributes").equalsIgnoreCase("Y")) continue;
                            copyDownDef = plcd;
                            break;
                        }
                        logger.debug("currentcol: " + currentcol);
                        if (copyDownDef == null) continue;
                        if (!allowattributestarget) {
                            logger.warn("Primary SDC " + sdcid + " does not allow attributes but link " + cdlink.getProperty("linkid") + " will copy down attributes.");
                        }
                        for (int row = 0; row < primaryData.getRowCount(); ++row) {
                            String oldKey3;
                            String oldKey2;
                            String oldValue;
                            String linkSDCid;
                            String key3;
                            String value;
                            if (primaryData.getValue(row, "templateflag", "N").equalsIgnoreCase("Y") || (value = primaryData.getValue(row, currentcol, "")).length() <= 0) continue;
                            SDIRequest attributerequest = new SDIRequest();
                            String key2 = cdlink.getProperty("sdccolumnid2", "").length() > 0 ? primaryData.getValue(row, cdlink.getProperty("sdccolumnid2", ""), "") : "";
                            String string = key3 = cdlink.getProperty("sdccolumnid3", "").length() > 0 ? primaryData.getValue(row, cdlink.getProperty("sdccolumnid3", ""), "") : "";
                            if ((key2.length() == 0 || "C".equalsIgnoreCase(key2)) && "Y".equalsIgnoreCase(sdcProc.getProperty(linkSDCid = cdlink.getProperty("linksdcid"), "versionedflag"))) {
                                key2 = SdiInfo.getCurrentVersion(linkSDCid, value, key3, connectionProcessor.getSapphireConnection());
                            }
                            logger.debug("keyid1: " + value + ", keyid2: " + key2 + ", keyid3: " + key3);
                            String string2 = oldValue = beforePrimaryData != null ? beforePrimaryData.getValue(row, currentcol, "") : "";
                            String string3 = beforePrimaryData != null ? (cdlink.getProperty("sdccolumnid2", "").length() > 0 ? beforePrimaryData.getValue(row, cdlink.getProperty("sdccolumnid2", ""), "") : "") : (oldKey2 = "");
                            String string4 = beforePrimaryData != null ? (cdlink.getProperty("sdccolumnid3", "").length() > 0 ? beforePrimaryData.getValue(row, cdlink.getProperty("sdccolumnid3", ""), "") : "") : (oldKey3 = "");
                            if (!(value.equals(oldValue) && key2.equals(oldKey2) && key3.equals(oldKey3))) {
                                attributerequest.setSDIList(cdlink.getProperty("linksdcid"), value, key2.length() > 0 ? key2 : null, key3.length() > 0 ? key3 : null);
                                attributerequest.setRequestItem("attribute");
                                attributerequest.setExtendedDataTypes(true);
                                attributerequest.setRetrieveMappedKey(false);
                                try {
                                    SDIData targetAttributeData = sdiProc.getSDIData(attributerequest);
                                    if (targetAttributeData != null && targetAttributeData.getDataset("attribute") != null) {
                                        HashMap<String, String> filteratts = new HashMap<String, String>();
                                        filteratts.put("attributesourcetype", AddSDIAttribute.getAttributeType(AttributeType.linkdef));
                                        filteratts.put("attributesdcid", sdcid);
                                        logger.debug("SOURCE LINK FROM: " + cdlink.getProperty("linksdcid"));
                                        logger.debug("SOURCE LINK COLUMN: " + cdlink.getProperty("sdccolumnid"));
                                        DataSet targetAttributes = targetAttributeData.getDataset("attribute").getFilteredDataSet(filteratts);
                                        if (targetAttributes.getRowCount() > 0) {
                                            for (int ar = 0; ar < targetAttributes.getRowCount(); ++ar) {
                                                String currentattribute = targetAttributes.getString(ar, ATTRIBUTEIDCOLUMN);
                                                String currentinstructionflag = targetAttributes.getValue(ar, "instructionflag", "");
                                                PropertyList attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                                                if (attributedef != null || currentinstructionflag.equalsIgnoreCase("R") || currentinstructionflag.equalsIgnoreCase("A") || currentinstructionflag.equalsIgnoreCase("O")) {
                                                    int instance = BaseSDIAttributeAction.getDuplicateInstance(currentattribute, sdcid, primaryData.getString(row, keycolid1), primaryData.getString(row, keycolid2, ""), primaryData.getString(row, keycolid3, ""), sdcid, attributeData, existingattributedata, attributedef, skipped, logger);
                                                    if (instance > 0) {
                                                        attributeData.copyRow(targetAttributes, ar, 1);
                                                        int newrow = attributeData.getRowCount() - 1;
                                                        BaseSDIAttributeAction.doCopyValues(attributeData, newrow, sdcid, instance, keycolid1.length() > 0 ? primaryData.getString(row, keycolid1) : "", keycolid2.length() > 0 ? primaryData.getString(row, keycolid2) : "", keycolid3.length() > 0 ? primaryData.getString(row, keycolid3) : "", targetAttributes, ar, m18n, logger);
                                                        continue;
                                                    }
                                                    logger.debug("Instance id is 0 which means duplicate was found for non duplicate attribute and thus attribute is being skipped.");
                                                    continue;
                                                }
                                                if (skipped != null) {
                                                    ArrayList<Object> ml;
                                                    if (skipped.containsKey(SKIPPED_MASTERLIST)) {
                                                        ml = skipped.get(SKIPPED_MASTERLIST);
                                                    } else {
                                                        ml = new ArrayList();
                                                        skipped.put(SKIPPED_MASTERLIST, ml);
                                                    }
                                                    ml.add(currentattribute);
                                                }
                                                logger.warn("Attribute " + currentattribute + " is not in " + sdcid + " master list and was therefore skipped.");
                                            }
                                            continue;
                                        }
                                        logger.info("No attribute data available from source " + cdlink.getProperty("linksdcid") + "." + cdlink.getProperty("sdccolumnid") + ".");
                                        continue;
                                    }
                                    logger.warn("Could not obtain attribute data from source " + cdlink.getProperty("linksdcid") + "." + cdlink.getProperty("sdccolumnid") + ".");
                                    continue;
                                }
                                catch (Exception e) {
                                    throw new SapphireException("Failed to load attributes from source: " + cdlink.getProperty("linksdcid") + "." + cdlink.getProperty("linksdccolumnid"), e);
                                }
                            }
                            logger.debug("Copy down of attributes for " + value + " (" + key2 + ", " + key3 + ") skipped because value has not changed.");
                        }
                    }
                } else {
                    logger.error("Could not obtain attribute data for SDI.");
                }
            } else {
                logger.debug("No copy down policy defined.");
            }
        } else {
            logger.debug("No links defined.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void copyDownWorkItemAttributesToDataSet(DataSet sdiattributes, DataSet sdiwi, DataSet sdiwiitem, DataSet allWorkItemAttributes, ConnectionInfo connectionInfo, DBAccess database, SDCProcessor sdcProcessor, Logger logger) throws SapphireException {
        for (int i = 0; i < sdiwi.getRowCount(); ++i) {
            String c_sdcid = sdiwi.getValue(i, "sdcid");
            String c_keyid1 = sdiwi.getValue(i, "keyid1");
            String c_keyid2 = sdiwi.getValue(i, "keyid2");
            String c_keyid3 = sdiwi.getValue(i, "keyid3");
            String c_workitemid = sdiwi.getValue(i, "workitemid");
            String c_workitemversionid = sdiwi.getValue(i, "workitemversionid");
            String c_workiteminstance = sdiwi.getValue(i, "workiteminstance");
            int i_workiteminstance = 0;
            try {
                i_workiteminstance = Integer.parseInt(c_workiteminstance);
            }
            catch (Exception e) {
                logger.warn("Could not find workiteminstance");
            }
            HashMap<String, String> filterdatt = new HashMap<String, String>();
            filterdatt.put("sdcid", "WorkItem");
            filterdatt.put("keyid1", c_workitemid);
            filterdatt.put("keyid2", c_workitemversionid);
            filterdatt.put("attributesdcid", "DataSet");
            DataSet allParentAttributes = allWorkItemAttributes.getFilteredDataSet(filterdatt);
            if (allParentAttributes.getRowCount() <= 0) continue;
            HashMap<String, Object> filterdwiwi = new HashMap<String, Object>();
            filterdwiwi.put("sdcid", c_sdcid);
            filterdwiwi.put("keyid1", c_keyid1);
            if (!c_keyid2.equalsIgnoreCase("(null)") && c_keyid2.length() > 0) {
                filterdwiwi.put("keyid2", c_keyid2);
            }
            if (!c_keyid3.equalsIgnoreCase("(null)") && c_keyid3.length() > 0) {
                filterdwiwi.put("keyid3", c_keyid3);
            }
            filterdwiwi.put("workitemid", c_workitemid);
            filterdwiwi.put("workiteminstance", new BigDecimal(i_workiteminstance));
            DataSet filteredSDIWiWi = sdiwiitem.getFilteredDataSet(filterdwiwi);
            for (int r = 0; r < filteredSDIWiWi.getRowCount(); ++r) {
                String t_workitemitemid = filteredSDIWiWi.getValue(r, "workitemitemid");
                String c_workitemitemid = t_workitemitemid.substring(0, t_workitemitemid.indexOf("."));
                String c_paramlistid = filteredSDIWiWi.getValue(r, "itemkeyid1");
                String c_paramlistversionid = filteredSDIWiWi.getValue(r, "itemkeyid2");
                String c_variantid = filteredSDIWiWi.getValue(r, "itemkeyid3");
                HashMap<String, String> filterFurtherAtt = new HashMap<String, String>();
                filterFurtherAtt.put("copydowncontext", c_workitemitemid);
                DataSet parentAttributes = allParentAttributes.getFilteredDataSet(filterFurtherAtt);
                if (parentAttributes.size() <= 0) continue;
                String sql = "SELECT sdidataid FROM sdidata WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND paramlistid = ? AND paramlistversionid = ? AND variantid = ? AND sourceworkitemid = ? AND sourceworkiteminstance = ? ";
                database.createPreparedResultSet("GetSDIDataForAttributes", sql, new Object[]{c_sdcid, c_keyid1, c_keyid2, c_keyid3, c_paramlistid, c_paramlistversionid, c_variantid, c_workitemid, c_workiteminstance});
                try {
                    DataSet sdidatarecs = new DataSet(database.getResultSet("GetSDIDataForAttributes"));
                    for (int k = 0; k < sdidatarecs.getRowCount(); ++k) {
                        String c_sdidataid = sdidatarecs.getValue(k, "sdidataid");
                        HashMap<String, ArrayList<String>> hmskipped = new HashMap<String, ArrayList<String>>();
                        BaseSDIAttributeAction.coreCopyDownAttributes(sdiattributes, parentAttributes, sdcProcessor.getPropertyList("DataSet"), c_sdidataid, "", "", hmskipped, new M18NUtil(connectionInfo), logger);
                        BaseSDIAttributeAction.logSkipped(hmskipped, "DataSet", logger);
                    }
                    continue;
                }
                catch (Exception e) {
                    logger.warn(e.getMessage());
                    continue;
                }
                finally {
                    database.closeResultSet("GetSDIDataForAttributes");
                }
            }
        }
    }

    public static void coreCopyDownAttributes(DataSet attributeData, DataSet parentAttributes, PropertyList targetsdc, String keyid1, String keyid2, String keyid3, HashMap<String, ArrayList<String>> skipped, M18NUtil m18n, Logger logger) throws SapphireException {
        BaseSDIAttributeAction.coreCopyDownAttributes(attributeData, parentAttributes, targetsdc, keyid1, keyid2, keyid3, skipped, m18n, logger, AttributeType.linkdef, false);
    }

    public static void coreCopyDownAttributes(DataSet attributeData, DataSet parentAttributes, PropertyList targetsdc, String keyid1, String keyid2, String keyid3, HashMap<String, ArrayList<String>> skipped, M18NUtil m18n, Logger logger, AttributeType atttributeType, boolean copyValues) throws SapphireException {
        if (parentAttributes != null && parentAttributes.getRowCount() > 0) {
            String targetsdcid = targetsdc.getProperty("sdcid");
            boolean allowattributestarget = targetsdc.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y");
            BaseSDIAttributeAction.syncroniseDataSet(attributeData, true);
            PropertyListCollection attributedefs = targetsdc.getCollection("attributes");
            if (!allowattributestarget) {
                logger.warn("Primary SDC " + targetsdc + " does not allow attributes but will copy down attributes.");
            }
            HashMap<String, String> filteratts = new HashMap<String, String>();
            filteratts.put("attributesourcetype", AddSDIAttribute.getAttributeType(atttributeType));
            filteratts.put("attributesdcid", targetsdcid);
            DataSet targetAttributes = parentAttributes.getFilteredDataSet(filteratts);
            if (targetAttributes.getRowCount() > 0) {
                for (int ar = 0; ar < targetAttributes.getRowCount(); ++ar) {
                    String currentattribute = targetAttributes.getString(ar, ATTRIBUTEIDCOLUMN);
                    String current_instructionflag = targetAttributes.getValue(ar, "instructionflag");
                    PropertyList attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                    if (attributedef != null || current_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("R")) {
                        int instance = BaseSDIAttributeAction.getDuplicateInstance(currentattribute, targetsdcid, keyid1, keyid2 == null ? "" : keyid2, keyid3 == null ? "" : keyid3, targetsdcid, attributeData, new DataSet(), attributedef, skipped, logger);
                        if (instance > 0) {
                            attributeData.copyRow(targetAttributes, ar, 1);
                            int newrow = attributeData.getRowCount() - 1;
                            BaseSDIAttributeAction.doCopyValues(attributeData, newrow, targetsdcid, instance, keyid1 != null && keyid1.length() > 0 ? keyid1 : "", keyid2 != null && keyid2.length() > 0 ? keyid2 : "", keyid3 != null && keyid3.length() > 0 ? keyid3 : "", targetAttributes, ar, m18n, logger, copyValues);
                            continue;
                        }
                        logger.debug("Instance id is 0 which means duplicate was found for non duplicate attribute and thus attribute is being skipped.");
                        continue;
                    }
                    if (skipped != null) {
                        ArrayList<Object> ml;
                        if (skipped.containsKey(SKIPPED_MASTERLIST)) {
                            ml = skipped.get(SKIPPED_MASTERLIST);
                        } else {
                            ml = new ArrayList();
                            skipped.put(SKIPPED_MASTERLIST, ml);
                        }
                        ml.add(currentattribute);
                    }
                    logger.warn("Attribute " + currentattribute + " is not in " + targetsdcid + " master list and was therefore skipped.");
                }
                if (attributeData.getRowCount() > 0) {
                    for (int s = 0; s < attributeData.getRowCount(); ++s) {
                        attributeData.setValue(s, "sdiattributeid", "");
                        if (!OpalUtil.isNotEmpty(attributeData.getString(s, "tracelogid"))) continue;
                        attributeData.setString(s, "tracelogid", "");
                    }
                }
            } else {
                logger.info("No attribute data available from source.");
            }
        } else {
            logger.debug("No attribute data provided or no attributes available for copydown.");
        }
    }

    private static void syncroniseDataSet(DataSet dsnew, boolean full) {
        dsnew.addColumn("sdcid", 0);
        dsnew.addColumn("keyid1", 0);
        dsnew.addColumn("keyid2", 0);
        dsnew.addColumn("keyid3", 0);
        dsnew.addColumn(ATTRIBUTEIDCOLUMN, 0);
        dsnew.addColumn("attributesdcid", 0);
        dsnew.addColumn("attributeinstance", 1);
        if (full) {
            dsnew.addColumn("attributesourcetype", 0);
            dsnew.addColumn("sourcesdcid", 0);
            dsnew.addColumn("datatype", 0);
            dsnew.addColumn("editorstyleid", 0);
            dsnew.addColumn("editsdcid", 0);
            dsnew.addColumn("editreftypeid", 0);
            dsnew.addColumn("mandatoryflag", 0);
            dsnew.addColumn("hiddenflag", 0);
            dsnew.addColumn("updateableflag", 0);
            dsnew.addColumn("textvalue", 0);
            dsnew.addColumn("numericvalue", 1);
            dsnew.addColumn("datevalue", 2);
            dsnew.addColumn("clobvalue", 3);
            dsnew.addColumn("defaulttextvalue", 0);
            dsnew.addColumn("defaultnumericvalue", 1);
            dsnew.addColumn("defaultdatevalue", 2);
            dsnew.addColumn("defaultclobvalue", 3);
            dsnew.addColumn("usersequence", 1);
            dsnew.addColumn("instructiontext", 3);
            dsnew.addColumn("instructionflag", 0);
            dsnew.addColumn("worksheetcontext", 3);
            dsnew.addColumn("copydowncontext", 0);
            dsnew.addColumn("attributetypeflag", 0);
        }
    }

    public static void logSkipped(HashMap<String, ArrayList<String>> skippedAttributes, String sdcid, Logger logger) {
        if (skippedAttributes != null && skippedAttributes.size() > 0) {
            if (skippedAttributes.containsKey(SKIPPED_DUPLICATES)) {
                for (String skipatt : skippedAttributes.get(SKIPPED_DUPLICATES)) {
                    logger.info("Attribute " + skipatt + " for " + sdcid + " was skipped due to duplicate.");
                }
            }
            if (skippedAttributes.containsKey(SKIPPED_MASTERLIST)) {
                for (String skipatt : skippedAttributes.get(SKIPPED_MASTERLIST)) {
                    logger.info("Attribute " + skipatt + " for " + sdcid + " was skipped due to not being present in master list.");
                }
            }
        }
    }

    public static DataSet getExistingAttributes(String sdcid, String[] attributeIds, QueryProcessor qp, Logger logger) {
        StringBuffer sqlbuf = new StringBuffer();
        StringBuffer a = new StringBuffer("'");
        for (String s : attributeIds) {
            if (a.length() > 0) {
                a.append("','");
            }
            a.append(s);
        }
        a.append("'");
        SafeSQL safeSQL = new SafeSQL();
        sqlbuf.append("SELECT sdiattribute.* ");
        sqlbuf.append("FROM sdiattribute ");
        sqlbuf.append("WHERE ( sdiattribute.attributesdcid = ").append(safeSQL.addVar(sdcid)).append(" ) AND ");
        sqlbuf.append("sdiattribute.").append(ATTRIBUTEIDCOLUMN).append(" IN ( ").append(safeSQL.addIn(a.toString())).append(" )");
        sqlbuf.append("ORDER BY ").append(ATTRIBUTEIDCOLUMN);
        logger.debug("Attribute SQL = " + sqlbuf.toString());
        DataSet ds = qp != null ? qp.getPreparedSqlDataSet(sqlbuf.toString(), safeSQL.getValues()) : null;
        if (ds != null) {
            return ds;
        }
        return null;
    }

    protected static DataSet getExistingAttributes(String rsetid, QueryProcessor qp, Logger logger) {
        StringBuffer sqlbuf = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlbuf.append("SELECT sdiattribute.* ");
        sqlbuf.append("FROM sdiattribute, rsetitems ");
        sqlbuf.append("WHERE sdiattribute.sdcid = rsetitems.sdcid AND sdiattribute.keyid1 = rsetitems.keyid1 AND ");
        sqlbuf.append("sdiattribute.keyid2 = rsetitems.keyid2 AND ");
        sqlbuf.append("sdiattribute.keyid3 = rsetitems.keyid3 AND ");
        sqlbuf.append("rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" ");
        sqlbuf.append("ORDER BY ").append(ATTRIBUTEIDCOLUMN);
        logger.debug("Attribute SQL = " + sqlbuf.toString());
        DataSet ds = qp != null ? qp.getPreparedSqlDataSet(sqlbuf.toString(), safeSQL.getValues(), true) : null;
        if (ds != null) {
            return ds;
        }
        return null;
    }

    protected static int getDuplicateInstance(String currentattribute, String sdcid, String keyid1, String keyid2, String keyid3, String currentattributesdcid, DataSet dsnew, DataSet dsexisting, PropertyList attributedef, HashMap<String, ArrayList<String>> skipped, Logger logger) {
        int instanceid;
        HashMap<String, String> find = new HashMap<String, String>();
        find.put(ATTRIBUTEIDCOLUMN, currentattribute);
        find.put("sdcid", sdcid);
        find.put("keyid1", keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            find.put("keyid2", keyid2);
        }
        if (keyid3 != null && keyid3.length() > 0) {
            find.put("keyid3", keyid3);
        }
        find.put("attributesdcid", currentattributesdcid);
        DataSet filtered = dsnew.getFilteredDataSet(find);
        if (filtered.isValidColumn("__rowstatus")) {
            for (int i = 0; i < filtered.getRowCount(); ++i) {
                if (!filtered.getValue(i, "__rowstatus", "S").equalsIgnoreCase("D")) continue;
                filtered.deleteRow(i);
                --i;
            }
        }
        if (filtered.getRowCount() == 0) {
            filtered = dsexisting.getFilteredDataSet(find);
        } else {
            logger.debug("Existing attribute " + currentattribute + " found in data to save.");
        }
        if (filtered.getRowCount() > 0) {
            logger.debug("Existing attribute " + currentattribute + " found in data on sdi.");
            if (attributedef == null || attributedef.getProperty("allowduplicatesflag").equalsIgnoreCase("Y")) {
                int temp = 0;
                for (int r = 0; r < filtered.size(); ++r) {
                    int curr;
                    if (filtered.getColumnType("attributeinstance") == 1) {
                        curr = filtered.getBigDecimal(r, "attributeinstance", new BigDecimal(1)).intValue();
                    } else {
                        try {
                            curr = Integer.parseInt(filtered.getValue(r, "attributeinstance", "1"));
                        }
                        catch (Exception e) {
                            curr = 1;
                        }
                    }
                    if (curr <= temp) continue;
                    temp = curr;
                }
                instanceid = temp + 1;
            } else {
                if (skipped != null) {
                    ArrayList<Object> dupskipped;
                    if (skipped.containsKey(SKIPPED_DUPLICATES)) {
                        dupskipped = skipped.get(SKIPPED_DUPLICATES);
                    } else {
                        dupskipped = new ArrayList();
                        skipped.put(SKIPPED_DUPLICATES, dupskipped);
                    }
                    dupskipped.add(currentattribute);
                }
                logger.warn("Attribute " + currentattribute + " is already present on sdi and duplicates are not allowed. Attribute will be skipped.");
                instanceid = 0;
            }
        } else {
            logger.debug("Attribute " + currentattribute + " not found on sdi or in data to save.");
            instanceid = 1;
        }
        return instanceid;
    }

    public static void createAttributeData(DataSet dsnew, DataSet dsexisting, PropertyList sdcprops, String keyid1, String keyid2, String keyid3, String attributeid, String attributesdc, AttributeType type, String value, String mandatoryflag, String updatableflag, String hiddenflag, String editorstyleid, String editsdcid, String editreftypeid, String instructionflag, String instructiontext, String copydowncontext, String worksheetcontext, String usersequence, HashMap<String, ArrayList<String>> skipped, boolean templateAttributes, String rsetid, String delim, SDCProcessor sdcProc, QueryProcessor qp, M18NUtil m18n, ConnectionInfo connectionInfo, Logger logger) throws SapphireException {
        BaseSDIAttributeAction.createAttributeData(dsnew, dsexisting, sdcprops, keyid1, keyid2, keyid3, attributeid, attributesdc, "", type, value, mandatoryflag, updatableflag, hiddenflag, editorstyleid, editsdcid, editreftypeid, instructionflag, instructiontext, copydowncontext, "", worksheetcontext, usersequence, skipped, templateAttributes, rsetid, delim, sdcProc, qp, m18n, connectionInfo, logger);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void createAttributeData(DataSet dsnew, DataSet dsexisting, PropertyList sdcprops, String keyid1, String keyid2, String keyid3, String attributeid, String attributesdc, String datatype, AttributeType type, String value, String mandatoryflag, String updatableflag, String hiddenflag, String editorstyleid, String editsdcid, String editreftypeid, String instructionflag, String instructiontext, String copydowncontext, String attributetypeflag, String worksheetcontext, String usersequence, HashMap<String, ArrayList<String>> skipped, boolean templateAttributes, String rsetid, String delim, SDCProcessor sdcProc, QueryProcessor qp, M18NUtil m18n, ConnectionInfo connectionInfo, Logger logger) throws SapphireException {
        String[] key3ids;
        String[] attributesdcids;
        String[] attributeids;
        String sdcid = sdcprops.getProperty("sdcid");
        String attributetype = BaseSDIAttributeAction.getAttributeType(type);
        if ((rsetid == null || rsetid.length() == 0) && dsexisting == null) {
            throw new SapphireException("CREATE_RSET_FAILURE", "No RSET or existing data for sdiattibute maintenance");
        }
        if (dsexisting == null) {
            logger.debug("rset = " + rsetid);
            dsexisting = BaseSDIAttributeAction.getExistingAttributes(rsetid, qp, logger);
        }
        PropertyListCollection attributedefs = null;
        if (dsexisting == null) throw new SapphireException("Could not obtain attribute data from sdi.");
        BaseSDIAttributeAction.syncroniseDataSet(dsnew, true);
        if (type == AttributeType.sdc) {
            attributedefs = sdcprops.getCollection("attributes");
            if (attributedefs == null || attributedefs.size() <= 0) throw new SapphireException("Cannot add attribute. No attributes defined for SDC (1).");
            ArrayList<String> sbatts = new ArrayList<String>();
            for (int i = 0; i < attributedefs.size(); ++i) {
                int count;
                PropertyList attrPL = attributedefs.getPropertyList(i);
                if (!attrPL.getProperty("alwaysaddflag").equalsIgnoreCase("Y")) continue;
                String attributeidsdctype = attrPL.getProperty(ATTRIBUTEIDCOLUMN);
                try {
                    count = Integer.parseInt(attrPL.getProperty("alwaysaddcount", "1"));
                }
                catch (NumberFormatException e) {
                    count = 1;
                }
                HashMap<String, String> hsfindsdctype = new HashMap<String, String>();
                hsfindsdctype.put("sdcid", sdcid);
                hsfindsdctype.put("attributesdcid", sdcid);
                hsfindsdctype.put("keyid1", keyid1);
                if (keyid2 != null && keyid2.length() > 0) {
                    hsfindsdctype.put("keyid2", keyid2);
                    if (keyid3 != null && keyid3.length() > 0) {
                        hsfindsdctype.put("keyid3", keyid3);
                    }
                }
                hsfindsdctype.put(ATTRIBUTEIDCOLUMN, attributeidsdctype);
                DataSet dsfindsdctype = dsnew.getFilteredDataSet(hsfindsdctype);
                if ((count -= dsfindsdctype.getRowCount()) <= 0) continue;
                for (int k = 0; k < count; ++k) {
                    sbatts.add(attributeidsdctype);
                }
            }
            attributeids = new String[sbatts.size()];
            sbatts.toArray(attributeids);
        } else {
            attributeids = StringUtil.split(attributeid, delim);
        }
        if (type == AttributeType.linkdef) {
            attributesdcids = StringUtil.split(attributesdc, delim);
            if (attributesdcids.length != 1 && attributesdcids.length != attributeids.length) {
                throw new SapphireException("Invalid attributesdcid passed. Either pass one value for entire set of attributes or an sdc per attribute.");
            }
        } else {
            attributesdcids = new String[]{sdcid};
        }
        String[] values = null;
        String[] datatypes = null;
        String[] mandatoryflags = null;
        String[] hiddenflags = null;
        String[] updatableflags = null;
        String[] editorstyles = null;
        String[] editsdcs = null;
        String[] editreftypes = null;
        String[] instructionflags = null;
        String[] instructiontexts = null;
        String[] copydowncontexts = null;
        String[] attributetypeflags = null;
        String[] worksheetcontexts = null;
        if (type == AttributeType.adhoc) {
            values = StringUtil.split(value, delim);
            if (value.length() > 0 && values.length != attributeids.length) {
                throw new SapphireException("Values passed to action do not match attributes.");
            }
            instructionflags = StringUtil.split(instructionflag, delim);
            instructiontexts = StringUtil.split(instructiontext, delim);
            if (instructionflag.equalsIgnoreCase("M") && updatableflag.length() > 0) {
                updatableflags = StringUtil.split(updatableflag, delim);
            }
        } else if (type == AttributeType.linkdef) {
            if (value.length() > 0 && (values = StringUtil.split(value, delim)).length != attributeids.length) {
                throw new SapphireException("Values passed to action do not match attributes.");
            }
            mandatoryflags = StringUtil.split(mandatoryflag, delim);
            hiddenflags = StringUtil.split(hiddenflag, delim);
            updatableflags = StringUtil.split(updatableflag, delim);
            editorstyles = StringUtil.split(editorstyleid, delim);
            editsdcs = StringUtil.split(editsdcid, delim);
            editreftypes = StringUtil.split(editreftypeid, delim);
            instructionflags = StringUtil.split(instructionflag, delim);
            instructiontexts = StringUtil.split(instructiontext, delim);
            copydowncontexts = StringUtil.split(copydowncontext, delim);
            attributetypeflags = StringUtil.split(attributetypeflag, delim);
            worksheetcontexts = StringUtil.split(worksheetcontext, delim);
        } else if (value.length() > 0) {
            logger.warn("Values passed for non-adhoc and non-link type. Values will be ignored.");
        }
        datatypes = datatype == null || datatype.length() == 0 ? null : StringUtil.split(datatype, delim);
        String[] usersequences = usersequence == null || usersequence.length() == 0 ? null : StringUtil.split(usersequence, delim);
        HashMap<String, PropertyList> processedtargetsdcs = new HashMap<String, PropertyList>();
        ConfigurationProcessor cp = new ConfigurationProcessor(sdcProc.getConnectionid());
        String[] key1ids = StringUtil.split(keyid1, delim);
        String[] key2ids = keyid2 != null && keyid2.length() > 0 ? StringUtil.split(keyid2, delim) : null;
        String[] stringArray = key3ids = keyid3 != null && keyid3.length() > 0 ? StringUtil.split(keyid3, delim) : null;
        if (key1ids.length > 1 && key1ids.length != attributeids.length) {
            throw new SapphireException("INVALID_PROPERTIES", "For multiple SDIs, number of keyids must be same as number of attributeids.");
        }
        for (int i = 0; i < attributeids.length; ++i) {
            String current_instructionflag;
            String current_instructiontext;
            PropertyList attributedef;
            String key1 = key1ids != null && key1ids.length > i ? key1ids[i] : keyid1;
            String key2 = key2ids != null && key2ids.length > i ? key2ids[i] : keyid2;
            String key3 = key3ids != null && key3ids.length > i ? key3ids[i] : keyid3;
            String currentattribute = attributeids[i];
            String currentattributesdcid = attributesdcids.length > 1 ? attributesdcids[i] : attributesdcids[0];
            String com_currentattributesdcid = currentattributesdcid.toLowerCase();
            String current_worksheetcontext = "";
            String current_copydowncontext = "";
            String current_attributetypeflag = "";
            if (!currentattributesdcid.equalsIgnoreCase(sdcid)) {
                if (!processedtargetsdcs.containsKey(com_currentattributesdcid)) {
                    PropertyList targetprops = sdcProc.getPropertyList(currentattributesdcid);
                    if (targetprops == null) {
                        throw new SapphireException("Invalid target SDC " + currentattributesdcid + " provided.");
                    }
                    if (targetprops.getProperty("allowattributesflag", "N").equalsIgnoreCase("N")) {
                        if (type != AttributeType.linkdef) throw new SapphireException("Target SDC " + currentattributesdcid + " does not allow attributes.");
                        logger.warn("Target SDC " + currentattributesdcid + " does not allow attributes, but linkdef will be added.");
                    }
                    if (sdcid.equalsIgnoreCase("WorkItem") && (currentattributesdcid.equalsIgnoreCase("DataSet") || currentattributesdcid.equalsIgnoreCase("SDIWorkItem") || currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem"))) {
                        attributedefs = targetprops.getCollection("attributes");
                        attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                        processedtargetsdcs.put(com_currentattributesdcid, targetprops);
                    } else if (sdcid.equalsIgnoreCase("ParamList") && currentattributesdcid.equalsIgnoreCase("DataSet")) {
                        attributedefs = targetprops.getCollection("attributes");
                        attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                        processedtargetsdcs.put(com_currentattributesdcid, targetprops);
                    } else if (sdcid.equalsIgnoreCase("SpecSDC") && currentattributesdcid.equalsIgnoreCase("SDISpec")) {
                        attributedefs = targetprops.getCollection("attributes");
                        attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                        processedtargetsdcs.put(com_currentattributesdcid, targetprops);
                    } else if ((sdcid.equalsIgnoreCase("QCMethod") || sdcid.equalsIgnoreCase("SampleType") || sdcid.equalsIgnoreCase("LV_ReagentType")) && currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                        attributedefs = targetprops.getCollection("attributes");
                        attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                        processedtargetsdcs.put(com_currentattributesdcid, targetprops);
                    } else if (sdcid.equalsIgnoreCase("LV_InstrumentModel") && currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                        attributedefs = targetprops.getCollection("attributes");
                        attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                        processedtargetsdcs.put(com_currentattributesdcid, targetprops);
                    } else if (sdcid.equalsIgnoreCase("LV_InstrumentType") && currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                        attributedefs = targetprops.getCollection("attributes");
                        attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                        processedtargetsdcs.put(com_currentattributesdcid, targetprops);
                    } else {
                        PropertyListCollection links = targetprops.getCollection("links");
                        if (links == null) throw new SapphireException("Could not validate links to target SDC " + currentattributesdcid + ".");
                        boolean foundlink = false;
                        for (int l = 0; l < links.size(); ++l) {
                            PropertyList link = links.getPropertyList(l);
                            if (!link.getProperty("linktype").equalsIgnoreCase("F") || !link.getProperty("linksdcid").equalsIgnoreCase(sdcid) || !link.getProperty("sdcid").equalsIgnoreCase(currentattributesdcid)) continue;
                            ArrayList<PropertyList> copyDownPolicy = BaseSDIAction.getCopyDownPolicy(null, sdcProc.getPropertyList(currentattributesdcid), new String[]{"attributes"}, cp).get("attributes");
                            if (copyDownPolicy != null) {
                                for (PropertyList cd : copyDownPolicy) {
                                    if (!cd.getProperty("sdcid").equalsIgnoreCase(sdcid) || !cd.getProperty("copyattributes").equalsIgnoreCase("Y")) continue;
                                    foundlink = true;
                                    break;
                                }
                            }
                            if (foundlink) break;
                        }
                        if (!foundlink) throw new SapphireException("Target SDC " + currentattributesdcid + " is not linked to SDC " + sdcid + " or has no copy down defined.");
                        logger.debug("Linked target sdc " + currentattributesdcid + " validated as having attributes and defined as copy down.");
                        attributedefs = targetprops.getCollection("attributes");
                        attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                        processedtargetsdcs.put(com_currentattributesdcid, targetprops);
                    }
                } else {
                    attributedefs = ((PropertyList)processedtargetsdcs.get(com_currentattributesdcid)).getCollection("attributes");
                    attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
                }
                if (sdcid.equalsIgnoreCase("WorkItem") && (currentattributesdcid.equalsIgnoreCase("DataSet") || currentattributesdcid.equalsIgnoreCase("SDIWorkItem") || currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem"))) {
                    String string = worksheetcontexts.length > i ? worksheetcontexts[i] : (current_worksheetcontext = worksheetcontexts.length > 0 ? worksheetcontexts[0] : "");
                    String string2 = copydowncontexts.length > i ? copydowncontexts[i] : (current_copydowncontext = copydowncontexts.length > 0 ? copydowncontexts[0] : "");
                    if (currentattributesdcid.equalsIgnoreCase("SDIWorkItem")) {
                        current_attributetypeflag = attributetypeflags.length > i ? attributetypeflags[i] : (attributetypeflags.length > 0 ? attributetypeflags[0] : "");
                    }
                } else if ((sdcid.equalsIgnoreCase("QCMethod") || sdcid.equalsIgnoreCase("SampleType") || sdcid.equalsIgnoreCase("LV_ReagentType")) && currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                    current_worksheetcontext = worksheetcontexts.length > i ? worksheetcontexts[i] : (worksheetcontexts.length > 0 ? worksheetcontexts[0] : "");
                } else if (sdcid.equalsIgnoreCase("LV_InstrumentModel") && currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                    String string = worksheetcontexts.length > i ? worksheetcontexts[i] : (current_worksheetcontext = worksheetcontexts.length > 0 ? worksheetcontexts[0] : "");
                    current_copydowncontext = copydowncontexts.length > i ? copydowncontexts[i] : (copydowncontexts.length > 0 ? copydowncontexts[0] : "");
                } else if (sdcid.equalsIgnoreCase("LV_InstrumentType") && currentattributesdcid.equalsIgnoreCase("LV_WorksheetItem")) {
                    String string = worksheetcontexts.length > i ? worksheetcontexts[i] : (current_worksheetcontext = worksheetcontexts.length > 0 ? worksheetcontexts[0] : "");
                    current_copydowncontext = copydowncontexts.length > i ? copydowncontexts[i] : (copydowncontexts.length > 0 ? copydowncontexts[0] : "");
                }
            } else {
                if (attributedefs == null) {
                    attributedefs = sdcprops.getCollection("attributes");
                }
                attributedef = attributedefs.find(ATTRIBUTEIDCOLUMN, currentattribute);
            }
            String def_instructionflag = "";
            String def_instructiontext = "";
            if (instructionflags != null && instructiontexts != null) {
                String string = instructiontexts.length == attributeids.length ? instructiontexts[i] : (current_instructiontext = instructiontexts.length > 0 ? instructiontexts[0] : "");
                String string3 = instructionflags.length == attributeids.length ? instructionflags[i] : (current_instructionflag = instructionflags.length > 0 ? instructionflags[0] : "N");
                if (current_instructiontext.length() > 0 && current_instructionflag.length() == 0) {
                    current_instructionflag = "R";
                }
            } else {
                current_instructionflag = "N";
                current_instructiontext = "";
            }
            if (attributedef != null || current_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("O") || current_instructionflag.equalsIgnoreCase("R") || current_instructionflag.equalsIgnoreCase("M")) {
                def_instructionflag = attributedef != null ? attributedef.getProperty("instructionflag", "") : "";
                def_instructiontext = attributedef != null ? attributedef.getProperty("instructiontext", "") : "";
                int instanceid = BaseSDIAttributeAction.getDuplicateInstance(currentattribute, sdcid, key1, key2, key3, currentattributesdcid, dsnew, dsexisting, attributedef, skipped, logger);
                if (instanceid > 0) {
                    StringBuffer enteredtext;
                    String enteredvalue;
                    String pref;
                    String defdatatype;
                    logger.debug("Attribute " + currentattribute + " instance = " + instanceid);
                    int row = dsnew.addRow();
                    String string = defdatatype = attributedef != null ? attributedef.getProperty("datatype", "") : "";
                    String useddatatype = defdatatype.length() > 0 ? defdatatype : (datatypes != null && datatypes.length > i ? datatypes[i] : "S");
                    dsnew.setValue(row, "sdcid", sdcid);
                    dsnew.setValue(row, "keyid1", key1);
                    dsnew.setValue(row, "keyid2", key2 == null || key2.length() == 0 ? "(null)" : key2);
                    dsnew.setValue(row, "keyid3", key3 == null || key3.length() == 0 ? "(null)" : key3);
                    dsnew.setValue(row, "attrsdcid", sdcid);
                    dsnew.setValue(row, "primary1", key1);
                    dsnew.setValue(row, "primary2", key2 == null || key2.length() == 0 ? "(null)" : key2);
                    dsnew.setValue(row, "primary3", key3 == null || key3.length() == 0 ? "(null)" : key3);
                    dsnew.setValue(row, ATTRIBUTEIDCOLUMN, currentattribute);
                    dsnew.setValue(row, "attributesdcid", currentattributesdcid);
                    dsnew.setValue(row, "attributeinstance", "" + instanceid);
                    dsnew.setValue(row, "attributesourcetype", templateAttributes ? BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef) : attributetype);
                    dsnew.setValue(row, "sourcesdcid", sdcid);
                    dsnew.setValue(row, "datatype", useddatatype);
                    dsnew.setValue(row, "instructionflag", current_instructionflag);
                    dsnew.setValue(row, "instructiontext", current_instructiontext);
                    dsnew.setValue(row, "copydowncontext", current_copydowncontext);
                    dsnew.setValue(row, "worksheetcontext", current_worksheetcontext);
                    dsnew.setValue(row, "attributetypeflag", current_attributetypeflag);
                    dsnew.setValue(row, "emptyattribute", "false");
                    if (usersequences != null && usersequences.length > i) {
                        try {
                            dsnew.setNumber(row, "usersequence", Integer.parseInt(usersequences[i]));
                        }
                        catch (NumberFormatException e) {
                            logger.warn("Could not set usersequence.");
                            usersequences = null;
                        }
                    }
                    if (type == AttributeType.linkdef) {
                        dsnew.setValue(row, "mandatoryflag", mandatoryflags != null && mandatoryflags.length > i && mandatoryflags[i].equalsIgnoreCase("Y") ? "Y" : "N");
                        dsnew.setValue(row, "hiddenflag", hiddenflags != null && hiddenflags.length > i && hiddenflags[i].equalsIgnoreCase("Y") ? "Y" : "N");
                        dsnew.setValue(row, "updateableflag", updatableflags != null && updatableflags.length > i && updatableflags[i].equalsIgnoreCase("N") ? "N" : "Y");
                        if (attributedef != null) {
                            dsnew.setValue(row, "editorstyleid", editorstyleid.length() > 0 && editorstyles != null && editorstyles.length > i ? editorstyles[i] : attributedef.getProperty("editorstyleid"));
                            dsnew.setValue(row, "editsdcid", editsdcid.length() > 0 && editsdcs != null && editsdcs.length > i ? editsdcs[i] : attributedef.getProperty("editsdcid"));
                            dsnew.setValue(row, "editreftypeid", editreftypeid.length() > 0 && editreftypes != null && editreftypes.length > i ? editreftypes[i] : attributedef.getProperty("editreftypeid"));
                            if (def_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("R")) {
                                dsnew.setValue(row, "instructionflag", "N");
                            }
                        } else if (current_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("A")) {
                            dsnew.setValue(row, "editorstyleid", "Yes No Checkbox");
                            dsnew.setValue(row, "editsdcid", "");
                            dsnew.setValue(row, "editreftypeid", "");
                        } else if (current_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O")) {
                            dsnew.setValue(row, "editorstyleid", "");
                            dsnew.setValue(row, "editsdcid", "");
                            dsnew.setValue(row, "editreftypeid", "");
                        } else {
                            dsnew.setValue(row, "editorstyleid", editorstyleid.length() > 0 && editorstyles != null && editorstyles.length > i ? editorstyles[i] : "");
                            dsnew.setValue(row, "editsdcid", editsdcid.length() > 0 && editsdcs != null && editsdcs.length > i ? editsdcs[i] : "");
                            dsnew.setValue(row, "editreftypeid", editreftypeid.length() > 0 && editreftypes != null && editreftypes.length > i ? editreftypes[i] : "");
                        }
                        pref = "default";
                    } else {
                        dsnew.setValue(row, "mandatoryflag", "N");
                        dsnew.setValue(row, "hiddenflag", "N");
                        dsnew.setValue(row, "updateableflag", "Y");
                        if (updatableflags != null && updatableflags.length > 0) {
                            dsnew.setValue(row, "updateableflag", updatableflags != null && updatableflags.length > i && updatableflags[i].equalsIgnoreCase("N") ? "N" : "Y");
                        }
                        if (attributedef != null) {
                            dsnew.setValue(row, "editorstyleid", attributedef.getProperty("editorstyleid"));
                            dsnew.setValue(row, "editsdcid", attributedef.getProperty("editsdcid"));
                            dsnew.setValue(row, "editreftypeid", attributedef.getProperty("editreftypeid"));
                        } else {
                            dsnew.setValue(row, "editorstyleid", "");
                            dsnew.setValue(row, "editsdcid", "");
                            dsnew.setValue(row, "editreftypeid", "");
                        }
                        pref = "";
                        if (def_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("A") || def_instructionflag.equalsIgnoreCase("R")) {
                            dsnew.setValue(row, "instructionflag", "N");
                        }
                    }
                    String string4 = enteredvalue = values != null && values.length > i ? values[i] : "";
                    if (enteredvalue.length() == 0) {
                        String defs;
                        String string5 = defs = attributedef != null ? attributedef.getProperty("defaulttextvalue") : "";
                        if (useddatatype.equalsIgnoreCase("n")) {
                            StringBuffer enteredtext2 = new StringBuffer();
                            StringBuffer enterednumber = new StringBuffer();
                            BaseSDIAttributeAction.processEnteredNumber(defs.length() == 0 ? (attributedef != null ? attributedef.getProperty("defaultnumericvalue") : "") : defs, enteredtext2, enterednumber, m18n, dsnew.getLocale().equals(m18n.getLocale()), logger);
                            dsnew.setValue(row, pref + "numericvalue", enterednumber.toString());
                            dsnew.setValue(row, pref + "textvalue", enteredtext2.toString());
                            dsnew.setValue(row, pref + "clobvalue", "");
                            dsnew.setValue(row, pref + "datevalue", "");
                        } else if (useddatatype.equalsIgnoreCase("d") || useddatatype.equalsIgnoreCase("o")) {
                            if (type == AttributeType.linkdef) {
                                String date;
                                String string6 = date = attributedef == null ? "" : attributedef.getProperty("defaultdatevalue");
                                if (date.length() > 0) {
                                    date = BaseSDIAttributeAction.getRealDate("", date, m18n, new M18NUtil(), useddatatype.equalsIgnoreCase("d"), logger);
                                }
                                dsnew.setValue(row, pref + "datevalue", date);
                                dsnew.setValue(row, pref + "textvalue", "");
                                dsnew.setValue(row, pref + "clobvalue", "");
                                dsnew.setValue(row, pref + "numericvalue", "");
                            } else {
                                String defdate = attributedef == null ? "" : attributedef.getProperty("defaultdatevalue");
                                String date = BaseSDIAttributeAction.getRealDate(defs, defdate, m18n, new M18NUtil(), useddatatype.equalsIgnoreCase("d"), logger);
                                dsnew.setValue(row, pref + "datevalue", date);
                                dsnew.setValue(row, pref + "textvalue", "");
                                dsnew.setValue(row, pref + "clobvalue", "");
                                dsnew.setValue(row, pref + "numericvalue", "");
                            }
                        } else if (useddatatype.equalsIgnoreCase("c")) {
                            dsnew.setValue(row, pref + "clobvalue", attributedef == null ? "" : attributedef.getProperty("defaultclobvalue"));
                            dsnew.setValue(row, pref + "textvalue", "");
                            dsnew.setValue(row, pref + "datevalue", "");
                            dsnew.setValue(row, pref + "numericvalue", "");
                        } else {
                            if (pref.equalsIgnoreCase("default") && (def_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("A"))) {
                                dsnew.setValue(row, pref + "textvalue", "");
                            } else {
                                dsnew.setValue(row, pref + "textvalue", defs);
                            }
                            dsnew.setValue(row, pref + "clobvalue", "");
                            dsnew.setValue(row, pref + "datevalue", "");
                            dsnew.setValue(row, pref + "numericvalue", "");
                        }
                    } else if (useddatatype.equalsIgnoreCase("n")) {
                        enteredtext = new StringBuffer();
                        StringBuffer enterednumber = new StringBuffer();
                        BaseSDIAttributeAction.processEnteredNumber(enteredvalue, enteredtext, enterednumber, m18n, dsnew.getLocale().equals(m18n.getLocale()), logger);
                        dsnew.setValue(row, pref + "numericvalue", enterednumber.toString());
                        dsnew.setValue(row, pref + "textvalue", enteredtext.toString());
                        dsnew.setValue(row, pref + "clobvalue", "");
                        dsnew.setValue(row, pref + "datevalue", "");
                    } else if (useddatatype.equalsIgnoreCase("d") || useddatatype.equalsIgnoreCase("o")) {
                        if (type == AttributeType.linkdef) {
                            enteredtext = new StringBuffer();
                            StringBuffer entereddate = new StringBuffer();
                            BaseSDIAttributeAction.processEnteredDate(enteredvalue, enteredtext, entereddate, "", m18n, useddatatype.equalsIgnoreCase("d"), logger);
                            if (entereddate.length() > 0 && !useddatatype.equalsIgnoreCase("d")) {
                                dsnew.setDate(row, pref + "datevalue", m18n.parseCalendar(entereddate.toString(), false));
                            } else {
                                dsnew.setValue(row, pref + "datevalue", entereddate.toString());
                            }
                            dsnew.setValue(row, pref + "textvalue", enteredtext.toString());
                            dsnew.setValue(row, pref + "clobvalue", "");
                            dsnew.setValue(row, pref + "numericvalue", "");
                        } else if (enteredvalue.length() > 0) {
                            Calendar c = m18n.parseCalendar(enteredvalue, useddatatype.equalsIgnoreCase("d"));
                            if (c != null) {
                                dsnew.setDate(row, pref + "datevalue", c);
                            }
                        } else {
                            dsnew.setValue(row, pref + "datevalue", "");
                        }
                        dsnew.setValue(row, pref + "clobvalue", "");
                        dsnew.setValue(row, pref + "numericvalue", "");
                    } else if (useddatatype.equalsIgnoreCase("c")) {
                        dsnew.setValue(row, pref + "clobvalue", enteredvalue);
                        dsnew.setValue(row, pref + "textvalue", "");
                        dsnew.setValue(row, pref + "datevalue", "");
                        dsnew.setValue(row, pref + "numericvalue", "");
                    } else {
                        if (pref.equalsIgnoreCase("default") && (def_instructionflag.equalsIgnoreCase("A") || current_instructionflag.equalsIgnoreCase("A"))) {
                            dsnew.setValue(row, pref + "textvalue", "");
                        } else {
                            dsnew.setValue(row, pref + "textvalue", enteredvalue);
                        }
                        dsnew.setValue(row, pref + "textvalue", enteredvalue);
                        dsnew.setValue(row, pref + "clobvalue", "");
                        dsnew.setValue(row, pref + "datevalue", "");
                        dsnew.setValue(row, pref + "numericvalue", "");
                    }
                    if (dsnew.isValidColumn("__rowstatus")) {
                        dsnew.setValue(row, "__rowstatus", "I");
                    }
                    logger.debug("Attribute " + currentattribute + " of type " + useddatatype + " added.");
                    continue;
                }
                logger.debug("Instance id is 0 which means duplicate was found for non duplicate attribute and thus attribute is being skipped.");
                continue;
            }
            if (skipped != null) {
                ArrayList<Object> ml;
                if (skipped.containsKey(SKIPPED_MASTERLIST)) {
                    ml = skipped.get(SKIPPED_MASTERLIST);
                } else {
                    ml = new ArrayList();
                    skipped.put(SKIPPED_MASTERLIST, ml);
                }
                ml.add(currentattribute);
            }
            logger.warn("Attribute " + currentattribute + " is not in " + (type == AttributeType.linkdef ? " linked sdc " + currentattributesdcid : " primary sdc " + sdcid) + " master list and was therefore skipped.");
        }
        logger.debug("Finished successfully updating attribute dataset.");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected static DataSet updateAttributeData(PropertyList changes, DataSet existingdata, String sdcid, SDCProcessor sdcProcessor, String keyid1, String keyid2, String keyid3, String attributeid, String attributesdc, String attributeinstance, String rsetid, String delim, QueryProcessor qp, M18NUtil m18n, ConnectionInfo connectionInfo, Logger logger) throws SapphireException {
        DataSet dsout = new DataSet();
        BaseSDIAttributeAction.syncroniseDataSet(dsout, true);
        if (attributeid.length() > 0) {
            String[] keyid3ds;
            DataSet sdiattribute;
            logger.debug("Attribute update dataSet created.");
            if (existingdata == null) {
                if (rsetid != null && rsetid.length() > 0) {
                    sdiattribute = BaseSDIAttributeAction.getExistingAttributes(rsetid, qp, logger);
                } else {
                    logger.warn("No rset provided or existing data.");
                    sdiattribute = null;
                }
            } else {
                sdiattribute = existingdata.copy(null, true);
                BaseSDIAttributeAction.syncroniseDataSet(sdiattribute, true);
            }
            if (sdiattribute == null) throw new SapphireException("Could not obtain attribute data.");
            if (sdiattribute.getRowCount() <= 0) throw new SapphireException("No data in sdiattribute to update.");
            String[] attributeids = StringUtil.split(attributeid, delim);
            String[] attributeinstances = StringUtil.split(attributeinstance, delim);
            String[] attributesdcids = StringUtil.split(attributesdc, delim);
            String[] keyid1ds = StringUtil.split(keyid1, delim);
            String[] keyid2ds = keyid2 != null && keyid2.length() > 0 ? StringUtil.split(keyid2, delim) : null;
            String[] stringArray = keyid3ds = keyid3 != null && keyid3.length() > 0 ? StringUtil.split(keyid3, delim) : null;
            if (keyid1ds.length > 1 && keyid1ds.length != attributeids.length) {
                throw new SapphireException("INVALID_PROPERTIES", "For multiple SDIs, number of keyids must be same as number of attributeids.");
            }
            HashMap<String, String[]> properties = new HashMap<String, String[]>();
            for (Object o : changes.keySet()) {
                String key = o.toString();
                if (key.equalsIgnoreCase("sdcid") || key.equalsIgnoreCase("keyid1") || key.equalsIgnoreCase("keyid2") || key.equalsIgnoreCase("keyid3") || key.equalsIgnoreCase(ATTRIBUTEIDCOLUMN) || key.equalsIgnoreCase(ATTRIBUTEIDCOLUMN) || key.equalsIgnoreCase("attributesdcid") || key.equalsIgnoreCase("attributeinstance")) continue;
                String value = changes.getProperty(key, "");
                String[] values = StringUtil.split(value, delim);
                if (values.length == attributeids.length) {
                    properties.put(key, values);
                    continue;
                }
                logger.warn("Property " + key + " does not match attributeids and will be skipped.");
            }
            if (attributeids.length != attributeinstances.length || attributesdcids.length != attributeids.length) throw new SapphireException("Attribute key strings do not match.");
            for (int at = 0; at < attributeids.length; ++at) {
                PropertyList sdcProps = sdcProcessor.getPropertyList(attributesdcids[at]);
                PropertyListCollection attributedefs = sdcProps != null ? sdcProps.getCollection("attributes") : null;
                PropertyList attributedef = attributedefs != null ? attributedefs.find(ATTRIBUTEIDCOLUMN, attributeids[at]) : null;
                HashMap<String, Object> find = new HashMap<String, Object>();
                find.put(ATTRIBUTEIDCOLUMN, attributeids[at]);
                find.put("attributesdcid", attributesdcids[at]);
                if (keyid1ds != null && keyid1ds.length > at) {
                    find.put("keyid1", keyid1ds[at]);
                }
                if (keyid2ds != null && keyid2ds.length > at) {
                    find.put("keyid2", keyid2ds[at]);
                }
                if (keyid3ds != null && keyid3ds.length > at) {
                    find.put("keyid3", keyid3ds[at]);
                }
                try {
                    find.put("attributeinstance", OpalUtil.isNotEmpty(attributeinstances[at]) ? new BigDecimal(attributeinstances[at]) : "");
                }
                catch (Exception e) {
                    throw new SapphireException("Invalid attributeinstance provided.");
                }
                int findrow = sdiattribute.findRow(find);
                if (findrow <= -1) throw new SapphireException("Attribute keys did not return an attribute = " + attributeids[at] + " " + attributesdcids[at] + " " + attributeinstances[at]);
                boolean changeMade = false;
                AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(sdiattribute.getValue(findrow, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.adhoc)));
                String sourcesdc = sdiattribute.getValue(findrow, "sourcesdcid", sdcid);
                String datatype = sdiattribute.getValue(findrow, "datatype", "S");
                for (int col = 0; col < dsout.getColumnCount(); ++col) {
                    String def_instructionflag;
                    String column = dsout.getColumnId(col);
                    String instructionflag = "";
                    if (type == AttributeType.linkdef) {
                        String[] inf = (String[])properties.get("instructionflag");
                        instructionflag = inf != null ? inf[at] : "";
                    }
                    String string = def_instructionflag = attributedef != null ? attributedef.getProperty("instructionflag", "") : "";
                    if (column.equalsIgnoreCase("sdcid") || column.equalsIgnoreCase("keyid1") || column.equalsIgnoreCase("keyid2") || column.equalsIgnoreCase("keyid3") || column.equalsIgnoreCase(ATTRIBUTEIDCOLUMN) || column.equalsIgnoreCase("attributesdcid") || column.equalsIgnoreCase("attributeinstance")) continue;
                    if ((column.equalsIgnoreCase("attributesourcetype") || column.equalsIgnoreCase("sourcesdcid") || column.equalsIgnoreCase("datatype")) && properties.containsKey(column)) {
                        logger.info("Cannot update core attribute column " + column + ".");
                        continue;
                    }
                    if ((column.equalsIgnoreCase("textvalue") || column.equalsIgnoreCase("clobvalue") || column.equalsIgnoreCase("datevalue") || column.equalsIgnoreCase("numericvalue")) && properties.containsKey("value") && type != AttributeType.linkdef) {
                        if (!sdiattribute.isValidColumn("textvalue")) {
                            sdiattribute.addColumn("textvalue", 0);
                        }
                        if (datatype.equalsIgnoreCase("s") && column.equalsIgnoreCase("textvalue")) {
                            sdiattribute.setValue(findrow, "textvalue", ((String[])properties.get("value"))[at]);
                            changeMade = true;
                            continue;
                        }
                        if ((datatype.equalsIgnoreCase("d") || datatype.equalsIgnoreCase("o")) && column.equalsIgnoreCase("datevalue")) {
                            if (!sdiattribute.isValidColumn("datevalue")) {
                                sdiattribute.addColumn("datevalue", 2);
                            }
                            sdiattribute.setValue(findrow, "textvalue", "");
                            if (((String[])properties.get("value"))[at].length() > 0) {
                                Calendar c = m18n.parseCalendar(((String[])properties.get("value"))[at], datatype.equalsIgnoreCase("d"));
                                if (c != null) {
                                    sdiattribute.setDate(findrow, "datevalue", c);
                                }
                            } else {
                                sdiattribute.setValue(findrow, "datevalue", "");
                            }
                            changeMade = true;
                            continue;
                        }
                        if (datatype.equalsIgnoreCase("n") && column.equalsIgnoreCase("numericvalue")) {
                            if (!sdiattribute.isValidColumn("numericvalue")) {
                                sdiattribute.addColumn("numericvalue", 1);
                            }
                            StringBuffer enteredtext = new StringBuffer();
                            StringBuffer enterednumber = new StringBuffer();
                            BaseSDIAttributeAction.processEnteredNumber(((String[])properties.get("value"))[at], enteredtext, enterednumber, m18n, sdiattribute.getLocale().equals(m18n.getLocale()), logger);
                            sdiattribute.setValue(findrow, "textvalue", enteredtext.toString());
                            sdiattribute.setValue(findrow, "numericvalue", enterednumber.toString());
                            changeMade = true;
                            continue;
                        }
                        if (!datatype.equalsIgnoreCase("c") || !column.equalsIgnoreCase("clobvalue")) continue;
                        if (!sdiattribute.isValidColumn("clobvalue")) {
                            sdiattribute.addColumn("clobvalue", 3);
                        }
                        sdiattribute.setValue(findrow, "clobvalue", ((String[])properties.get("value"))[at]);
                        sdiattribute.setValue(findrow, "textvalue", "");
                        changeMade = true;
                        continue;
                    }
                    if ((column.equalsIgnoreCase("defaultvalue") || column.equalsIgnoreCase("defaulttextvalue") || column.equalsIgnoreCase("defaultclobvalue") || column.equalsIgnoreCase("defaultdatevalue") || column.equalsIgnoreCase("defaultnumericvalue")) && (properties.containsKey("defaultvalue") || properties.containsKey("value"))) {
                        if (!sdiattribute.isValidColumn("defaulttextvalue")) {
                            sdiattribute.addColumn("defaulttextvalue", 0);
                        }
                        String valueprop = "defaultvalue";
                        if (properties.containsKey("value")) {
                            valueprop = "value";
                        }
                        if (type == AttributeType.linkdef && sourcesdc.equalsIgnoreCase(sdcid)) {
                            StringBuffer enteredtext;
                            if (datatype.equalsIgnoreCase("s") && column.equalsIgnoreCase("defaulttextvalue")) {
                                sdiattribute.setValue(findrow, "defaulttextvalue", ((String[])properties.get(valueprop))[at]);
                                changeMade = true;
                                continue;
                            }
                            if ((datatype.equalsIgnoreCase("d") || datatype.equalsIgnoreCase("o")) && column.equalsIgnoreCase("defaultdatevalue")) {
                                if (!sdiattribute.isValidColumn("defaultdatevalue")) {
                                    sdiattribute.addColumn("defaultdatevalue", 2);
                                }
                                enteredtext = new StringBuffer();
                                StringBuffer entereddate = new StringBuffer();
                                BaseSDIAttributeAction.processEnteredDate(((String[])properties.get(valueprop))[at], enteredtext, entereddate, "", m18n, datatype.equalsIgnoreCase("d"), logger);
                                if (entereddate.length() > 0) {
                                    Calendar c = m18n.parseCalendar(entereddate.toString(), !datatype.equalsIgnoreCase("o"));
                                    if (c != null) {
                                        sdiattribute.setDate(findrow, "defaultdatevalue", c);
                                    }
                                } else {
                                    sdiattribute.setValue(findrow, "defaultdatevalue", "");
                                }
                                sdiattribute.setValue(findrow, "defaulttextvalue", enteredtext.toString());
                                changeMade = true;
                                continue;
                            }
                            if (datatype.equalsIgnoreCase("n") && column.equalsIgnoreCase("defaultnumericvalue")) {
                                if (!sdiattribute.isValidColumn("defaultnumericvalue")) {
                                    sdiattribute.addColumn("defaultnumericvalue", 1);
                                }
                                enteredtext = new StringBuffer();
                                StringBuffer enterednumber = new StringBuffer();
                                BaseSDIAttributeAction.processEnteredNumber(((String[])properties.get(valueprop))[at], enteredtext, enterednumber, m18n, sdiattribute.getLocale().equals(m18n.getLocale()), logger);
                                sdiattribute.setValue(findrow, "defaulttextvalue", enteredtext.toString());
                                sdiattribute.setValue(findrow, "defaultnumericvalue", enterednumber.toString());
                                changeMade = true;
                                continue;
                            }
                            if (!datatype.equalsIgnoreCase("c") || !column.equalsIgnoreCase("defaultclobvalue")) continue;
                            if (!sdiattribute.isValidColumn("defaultclobvalue")) {
                                sdiattribute.addColumn("defaultclobvalue", 3);
                            }
                            sdiattribute.setValue(findrow, "defaultclobvalue", ((String[])properties.get(valueprop))[at]);
                            sdiattribute.setValue(findrow, "defaulttextvalue", "");
                            changeMade = true;
                            continue;
                        }
                        logger.info("Cannot update defaultvalue for non-linkdef. Use value instead.");
                        continue;
                    }
                    if (column.equalsIgnoreCase("editorstyleid") && properties.containsKey("editorstyle")) {
                        if (type == AttributeType.linkdef && sourcesdc.equalsIgnoreCase(sdcid)) {
                            if (!sdiattribute.isValidColumn("editorstyleid")) {
                                sdiattribute.addColumn("editorstyleid", 0);
                            }
                            if (instructionflag.equalsIgnoreCase("A")) {
                                sdiattribute.setValue(findrow, "editorstyleid", "Yes No Checkbox");
                            } else if (instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("A")) {
                                sdiattribute.setValue(findrow, "editorstyleid", "");
                            } else {
                                sdiattribute.setValue(findrow, "editorstyleid", ((String[])properties.get("editorstyle"))[at]);
                            }
                            changeMade = true;
                            continue;
                        }
                        logger.info("Cannot update editorstyleid for non-link.");
                        continue;
                    }
                    if (column.equalsIgnoreCase("editsdcid") && properties.containsKey("editsdcid")) {
                        if (type == AttributeType.linkdef && sourcesdc.equalsIgnoreCase(sdcid)) {
                            if (!sdiattribute.isValidColumn("editsdcid")) {
                                sdiattribute.addColumn("editsdcid", 0);
                            }
                            if (instructionflag.equalsIgnoreCase("A") || instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("A")) {
                                sdiattribute.setValue(findrow, "editsdcid", "");
                            } else {
                                sdiattribute.setValue(findrow, "editsdcid", ((String[])properties.get("editsdcid"))[at]);
                            }
                            changeMade = true;
                            continue;
                        }
                        logger.info("Cannot update editsdcid for non-link.");
                        continue;
                    }
                    if (column.equalsIgnoreCase("editreftypeid") && properties.containsKey("editreftypeid")) {
                        if (type == AttributeType.linkdef && sourcesdc.equalsIgnoreCase(sdcid)) {
                            if (!sdiattribute.isValidColumn("editreftypeid")) {
                                sdiattribute.addColumn("editreftypeid", 0);
                            }
                            if (instructionflag.equalsIgnoreCase("A") || instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("O") || def_instructionflag.equalsIgnoreCase("A")) {
                                sdiattribute.setValue(findrow, "editreftypeid", "");
                            } else {
                                sdiattribute.setValue(findrow, "editreftypeid", ((String[])properties.get("editreftypeid"))[at]);
                            }
                            changeMade = true;
                            continue;
                        }
                        logger.info("Cannot update editreftypeid for non-link.");
                        continue;
                    }
                    if (column.equalsIgnoreCase("updateableflag") && properties.containsKey("updatable")) {
                        if (!sdiattribute.isValidColumn("updateableflag")) {
                            sdiattribute.addColumn("updateableflag", 0);
                        }
                        sdiattribute.setValue(findrow, "updateableflag", ((String[])properties.get("updatable"))[at].equalsIgnoreCase("Y") ? "Y" : "N");
                        changeMade = true;
                        continue;
                    }
                    if (column.equalsIgnoreCase("mandatoryflag") && properties.containsKey("mandatory")) {
                        if (!sdiattribute.isValidColumn("mandatoryflag")) {
                            sdiattribute.addColumn("mandatoryflag", 0);
                        }
                        sdiattribute.setValue(findrow, "mandatoryflag", ((String[])properties.get("mandatory"))[at].equalsIgnoreCase("Y") ? "Y" : "N");
                        changeMade = true;
                        continue;
                    }
                    if (column.equalsIgnoreCase("hiddenflag") && properties.containsKey("hidden")) {
                        if (!sdiattribute.isValidColumn("hiddenflag")) {
                            sdiattribute.addColumn("hiddenflag", 0);
                        }
                        sdiattribute.setValue(findrow, "hiddenflag", ((String[])properties.get("hidden"))[at].equalsIgnoreCase("Y") ? "Y" : "N");
                        changeMade = true;
                        continue;
                    }
                    if (column.equalsIgnoreCase("usersequence") && properties.containsKey("usersequence")) {
                        String[] us = (String[])properties.get("usersequence");
                        if (us == null || us.length <= at) continue;
                        try {
                            sdiattribute.setNumber(findrow, "usersequence", Integer.parseInt(us[at]));
                            changeMade = true;
                        }
                        catch (NumberFormatException e) {
                            logger.warn("Could not set usersequence.");
                        }
                        continue;
                    }
                    if (column.equalsIgnoreCase("copydowncontext") && properties.containsKey("copydowncontext") && type == AttributeType.linkdef) {
                        if (!sdiattribute.isValidColumn("copydowncontext")) {
                            sdiattribute.addColumn("copydowncontext", 0);
                        }
                        sdiattribute.setValue(findrow, "copydowncontext", ((String[])properties.get("copydowncontext"))[at]);
                        changeMade = true;
                        continue;
                    }
                    if (column.equalsIgnoreCase("attributetypeflag") && properties.containsKey("attributetypeflag") && type == AttributeType.linkdef) {
                        if (!sdiattribute.isValidColumn("attributetypeflag")) {
                            sdiattribute.addColumn("attributetypeflag", 0);
                        }
                        sdiattribute.setValue(findrow, "attributetypeflag", ((String[])properties.get("attributetypeflag"))[at]);
                        changeMade = true;
                        continue;
                    }
                    if (column.equalsIgnoreCase("worksheetcontext") && properties.containsKey("worksheetcontext") && type == AttributeType.linkdef) {
                        if (!sdiattribute.isValidColumn("worksheetcontext")) {
                            sdiattribute.addColumn("worksheetcontext", 0);
                        }
                        sdiattribute.setValue(findrow, "worksheetcontext", ((String[])properties.get("worksheetcontext"))[at]);
                        changeMade = true;
                        continue;
                    }
                    if (column.equalsIgnoreCase("instructiontext") && type == AttributeType.linkdef) {
                        String def_instructiontext = attributedef != null ? attributedef.getProperty("instructiontext", "") : "";
                        String[] it = (String[])properties.get("instructiontext");
                        if (it == null || it.length <= at) continue;
                        String instructiontext = it[at];
                        if (attributedef == null && (instructionflag.equalsIgnoreCase("A") || instructionflag.equalsIgnoreCase("O"))) {
                            sdiattribute.setValue(findrow, "instructiontext", instructiontext);
                            sdiattribute.setValue(findrow, "instructionflag", instructionflag);
                            continue;
                        }
                        if (def_instructionflag.equalsIgnoreCase("o") || def_instructionflag.equalsIgnoreCase("r") || def_instructionflag.equalsIgnoreCase("a")) {
                            if (instructiontext.length() > 0 && !instructiontext.equals(def_instructiontext)) {
                                sdiattribute.setValue(findrow, "instructiontext", instructiontext);
                                sdiattribute.setValue(findrow, "instructionflag", "R");
                                continue;
                            }
                            sdiattribute.setValue(findrow, "instructiontext", "");
                            sdiattribute.setValue(findrow, "instructionflag", "N");
                            continue;
                        }
                        if (instructiontext.length() > 0) {
                            sdiattribute.setValue(findrow, "instructiontext", instructiontext);
                            sdiattribute.setValue(findrow, "instructionflag", "R");
                            continue;
                        }
                        sdiattribute.setValue(findrow, "instructiontext", "");
                        sdiattribute.setValue(findrow, "instructionflag", "N");
                        continue;
                    }
                    if (!properties.containsKey(column)) continue;
                }
                if (!changeMade) continue;
                dsout.copyRow(sdiattribute, findrow, 1);
            }
            return dsout;
        }
        logger.warn("No attribute ids provided.");
        return dsout;
    }

    protected static void removeAttributeData(PropertyList sdcprops, String rsetid, String keyid1, String keyid2, String keyid3, String[] attributeid, String[] attributesdc, int[] attributeInstance, DBAccess database, Logger logger) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM sdiattribute WHERE ");
        SafeSQL safeSQL = new SafeSQL();
        if (rsetid.length() > 0) {
            if (database.isOracle()) {
                if (keyid2 != null && keyid2.length() > 0) {
                    if (keyid3 != null && keyid3.length() > 0) {
                        sql.append("( sdcid, keyid1, keyid2, keyid3 ) IN (SELECT sdcid, keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=").append(safeSQL.addVar(rsetid)).append(") AND ");
                    } else {
                        sql.append("( sdcid, keyid1, keyid2 ) IN (SELECT sdcid, keyid1, keyid2 FROM rsetitems WHERE rsetid=").append(safeSQL.addVar(rsetid)).append(") AND ");
                    }
                } else {
                    sql.append("( sdcid, keyid1 ) IN (SELECT sdcid, keyid1 FROM rsetitems WHERE rsetid=").append(safeSQL.addVar(rsetid)).append(") AND ");
                }
            } else {
                sql.append("sdcid IN (SELECT sdcid FROM rsetitems WHERE rsetid=").append(safeSQL.addVar(rsetid)).append(") AND ");
                sql.append("\tkeyid1 IN (SELECT keyid1 FROM rsetitems WHERE rsetid=").append(safeSQL.addVar(rsetid)).append(") AND ");
                if (keyid2 != null && keyid2.length() > 0) {
                    sql.append("\tkeyid2 IN (SELECT keyid2 FROM rsetitems WHERE rsetid=").append(safeSQL.addVar(rsetid)).append(") AND ");
                    if (keyid3 != null && keyid3.length() > 0) {
                        sql.append("\tkeyid3 IN (SELECT keyid3 FROM rsetitems WHERE rsetid=").append(safeSQL.addVar(rsetid)).append(") AND ");
                    }
                }
            }
        } else {
            sql.append("sdcid =").append(safeSQL.addVar(sdcprops.getProperty("sdcid"))).append(" AND ");
            sql.append("keyid1 =").append(safeSQL.addVar(keyid1)).append(" AND ");
            if (keyid2 != null && keyid2.length() > 0) {
                sql.append("keyid2 =").append(safeSQL.addVar(keyid1)).append(" AND ");
                if (keyid3 != null && keyid3.length() > 0) {
                    sql.append("keyid3 =").append(safeSQL.addVar(keyid1)).append(" AND ");
                }
            }
        }
        sql.append(" ( ");
        for (int i = 0; i < attributeid.length; ++i) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append(" ( ");
            sql.append("").append(ATTRIBUTEIDCOLUMN).append(" =").append(safeSQL.addVar(attributeid[i])).append(" AND ");
            sql.append("attributesdcid =").append(safeSQL.addVar(attributesdc[i])).append(" AND ");
            sql.append("attributeinstance =").append(safeSQL.addVar(attributeInstance[i])).append(" ");
            sql.append(" ) ");
        }
        sql.append(" ) ");
        logger.debug("Delete Attribute SQL = " + sql.toString());
        database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
    }

    public static String getDateAttributeValue(String textvalue, String datevalue, boolean defaults) {
        if (defaults) {
            if (textvalue.length() > 0) {
                return textvalue;
            }
            return datevalue;
        }
        return datevalue;
    }

    public static String getNumericAttributeValue(String textvalue, String numericvalue, M18NUtil m18n) {
        String out;
        if (textvalue.length() > 0) {
            if (!textvalue.contains("/")) {
                String defn = numericvalue;
                int z = textvalue.length() - defn.length();
                if (z > 0) {
                    FormatUtil fu = FormatUtil.getInstance(m18n.getLocale());
                    String ds = fu.getDecimalSeparator() + "";
                    if (!defn.contains(ds)) {
                        defn = defn + ds;
                        --z;
                    }
                    for (int zC = 0; zC < z; ++zC) {
                        defn = defn + '0';
                    }
                }
                out = defn;
            } else {
                out = textvalue;
            }
        } else {
            out = numericvalue;
        }
        return out;
    }

    public static String getAttributeValue(DataSet attributedata, int row, M18NUtil m18n) {
        String val;
        String out = "";
        String datatype = attributedata.getValue(row, "datatype", "S");
        AttributeType type = BaseSDIAttributeAction.getAttributeTypeFromString(attributedata.getValue(row, "attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.adhoc)));
        String prefix = "";
        if (type == AttributeType.linkdef) {
            prefix = "default";
        }
        out = datatype.equalsIgnoreCase("N") ? (val = BaseSDIAttributeAction.getNumericAttributeValue(attributedata.getValue(row, prefix + "textvalue", ""), attributedata.getValue(row, prefix + "numericvalue", ""), m18n)) : (datatype.equalsIgnoreCase("D") || datatype.equalsIgnoreCase("O") ? (val = BaseSDIAttributeAction.getDateAttributeValue(attributedata.getValue(row, prefix + "textvalue", ""), attributedata.getValue(row, prefix + "datevalue", ""), type == AttributeType.linkdef)) : (datatype.equalsIgnoreCase("C") ? attributedata.getValue(row, prefix + "clobvalue", "") : attributedata.getValue(row, prefix + "textvalue", "")));
        return StringUtil.replaceAll(out, ";", "#semicolon#");
    }

    public static ArrayList<String> getValues(DataSet attributedata, M18NUtil m18n) {
        ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < attributedata.getRowCount(); ++i) {
            out.add(BaseSDIAttributeAction.getAttributeValue(attributedata, i, m18n));
        }
        return out;
    }

    public static String getValues(DataSet attributedata, String delimeter, M18NUtil m18n) {
        StringBuffer out = new StringBuffer();
        ArrayList<String> vals = BaseSDIAttributeAction.getValues(attributedata, m18n);
        for (int i = 0; i < vals.size(); ++i) {
            if (i > 0) {
                out.append(delimeter);
            }
            out.append(vals.get(i));
        }
        return out.toString();
    }

    public static void addSaveAttributeDataToActionBlock(DataSet attributedata, ActionBlock ab, M18NUtil m18n) throws SapphireException {
        String sep = ";";
        if (attributedata != null) {
            if (attributedata.getRowCount() > 0) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("__rowstatus", "D");
                DataSet fordelete = attributedata.getFilteredDataSet(filter);
                if (fordelete.getRowCount() > 0) {
                    Logger.logDebug("Attributes for delete...");
                    fordelete.sort("sdcid, keyid1, keyid2,keyid3");
                    ArrayList<DataSet> sdiAttributeGroups = fordelete.getGroupedDataSets("sdcid, keyid1, keyid2,keyid3");
                    for (int g = 0; g < sdiAttributeGroups.size(); ++g) {
                        DataSet sdiAttributes = sdiAttributeGroups.get(g);
                        PropertyList deleteprops = new PropertyList();
                        deleteprops.setProperty("keyid1", sdiAttributes.getValue(0, "keyid1", ""));
                        deleteprops.setProperty("keyid2", sdiAttributes.getValue(0, "keyid2", ""));
                        deleteprops.setProperty("keyid3", sdiAttributes.getValue(0, "keyid3", ""));
                        deleteprops.setProperty("sdcid", sdiAttributes.getValue(0, "sdcid", ""));
                        deleteprops.setProperty(ATTRIBUTEIDCOLUMN, sdiAttributes.getColumnValues(ATTRIBUTEIDCOLUMN, sep));
                        deleteprops.setProperty("attributesdcid", sdiAttributes.getColumnValues("attributesdcid", sep));
                        deleteprops.setProperty("attributeinstance", sdiAttributes.getColumnValues("attributeinstance", sep));
                        ab.setActionClass("deleteattributes_" + g, DeleteSDIAttribute.class.getName(), deleteprops);
                    }
                } else {
                    Logger.logDebug("No attributes for delete.");
                }
                filter = new HashMap();
                filter.put("__rowstatus", "I");
                DataSet foraddition = attributedata.getFilteredDataSet(filter);
                if (foraddition.getRowCount() > 0) {
                    Logger.logDebug("Attributes for addition...");
                    filter = new HashMap();
                    filter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                    DataSet foraddition_link = foraddition.getFilteredDataSet(filter);
                    DataSet foraddition_adhoc = foraddition.getFilteredDataSet(filter, true);
                    if (foraddition_link.getRowCount() > 0) {
                        Logger.logDebug("LinkDef Attributes for addition...");
                        PropertyList addlinkprops = new PropertyList();
                        addlinkprops.setProperty("keyid1", foraddition_link.getValue(0, "keyid1", ""));
                        addlinkprops.setProperty("keyid2", foraddition_link.getValue(0, "keyid2", ""));
                        addlinkprops.setProperty("keyid3", foraddition_link.getValue(0, "keyid3", ""));
                        addlinkprops.setProperty("sdcid", foraddition_link.getValue(0, "sdcid", ""));
                        addlinkprops.setProperty("type", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                        addlinkprops.setProperty(ATTRIBUTEIDCOLUMN, foraddition_link.getColumnValues(ATTRIBUTEIDCOLUMN, sep));
                        addlinkprops.setProperty("datatype", foraddition_link.getColumnValues("datatype", sep));
                        addlinkprops.setProperty("attributesdcid", foraddition_link.getColumnValues("attributesdcid", sep));
                        addlinkprops.setProperty("value", AddSDIAttribute.getValues(foraddition_link, sep, m18n));
                        addlinkprops.setProperty("mandatory", foraddition_link.getColumnValues("mandatoryflag", sep));
                        addlinkprops.setProperty("updatable", foraddition_link.getColumnValues("updateableflag", sep));
                        addlinkprops.setProperty("hidden", foraddition_link.getColumnValues("hiddenflag", sep));
                        addlinkprops.setProperty("editorstyle", foraddition_link.getColumnValues("editorstyleid", sep));
                        addlinkprops.setProperty("editsdcid", foraddition_link.getColumnValues("editsdcid", sep));
                        addlinkprops.setProperty("editreftypeid", foraddition_link.getColumnValues("editreftypeid", sep));
                        addlinkprops.setProperty("usersequence", foraddition_link.getColumnValues("usersequence", sep));
                        addlinkprops.setProperty("instructionflag", foraddition_link.getColumnValues("instructionflag", sep));
                        addlinkprops.setProperty("instructiontext", foraddition_link.getColumnValues("instructiontext", sep));
                        addlinkprops.setProperty("copydowncontext", foraddition_link.getColumnValues("copydowncontext", sep));
                        addlinkprops.setProperty("attributetypeflag", foraddition_link.getColumnValues("attributetypeflag", sep));
                        addlinkprops.setProperty("worksheetcontext", foraddition_link.getColumnValues("worksheetcontext", sep));
                        ab.setActionClass("addlinkattributes", AddSDIAttribute.class.getName(), addlinkprops);
                    }
                    if (foraddition_adhoc.getRowCount() > 0) {
                        Logger.logDebug("Adhoc Attributes for addition...");
                        foraddition_adhoc.sort("sdcid, keyid1, keyid2,keyid3");
                        ArrayList<DataSet> sdiAttributeGroups = foraddition_adhoc.getGroupedDataSets("sdcid, keyid1, keyid2,keyid3");
                        for (int g = 0; g < sdiAttributeGroups.size(); ++g) {
                            DataSet sdiAttributes = sdiAttributeGroups.get(g);
                            PropertyList addadhocprops = new PropertyList();
                            addadhocprops.setProperty("keyid1", sdiAttributes.getValue(0, "keyid1", ""));
                            addadhocprops.setProperty("keyid2", sdiAttributes.getValue(0, "keyid2", ""));
                            addadhocprops.setProperty("keyid3", sdiAttributes.getValue(0, "keyid3", ""));
                            addadhocprops.setProperty("sdcid", sdiAttributes.getValue(0, "sdcid", ""));
                            addadhocprops.setProperty("type", BaseSDIAttributeAction.getAttributeType(AttributeType.adhoc));
                            addadhocprops.setProperty(ATTRIBUTEIDCOLUMN, sdiAttributes.getColumnValues(ATTRIBUTEIDCOLUMN, sep));
                            addadhocprops.setProperty("attributesdcid", sdiAttributes.getColumnValues("attributesdcid", sep));
                            addadhocprops.setProperty("value", AddSDIAttribute.getValues(sdiAttributes, sep, m18n));
                            addadhocprops.setProperty("usersequence", sdiAttributes.getColumnValues("usersequence", sep));
                            ab.setActionClass("addadhocattributes_" + g, AddSDIAttribute.class.getName(), addadhocprops);
                        }
                    }
                } else {
                    Logger.logDebug("No attributes for addition.");
                }
                filter = new HashMap();
                filter.put("__rowstatus", "U");
                DataSet foredit = attributedata.getFilteredDataSet(filter);
                if (foredit.getRowCount() > 0) {
                    Logger.logDebug("Attributes for edit...");
                    filter = new HashMap();
                    filter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                    DataSet foredit_link = foredit.getFilteredDataSet(filter);
                    DataSet foredit_adhoc = foredit.getFilteredDataSet(filter, true);
                    if (foredit_link.getRowCount() > 0) {
                        Logger.logDebug("LinkDef Attributes for edit...");
                        PropertyList editprops_link = new PropertyList();
                        editprops_link.setProperty("keyid1", foredit_link.getValue(0, "keyid1", ""));
                        editprops_link.setProperty("keyid2", foredit_link.getValue(0, "keyid2", ""));
                        editprops_link.setProperty("keyid3", foredit_link.getValue(0, "keyid3", ""));
                        editprops_link.setProperty("sdcid", foredit_link.getValue(0, "sdcid", ""));
                        editprops_link.setProperty(ATTRIBUTEIDCOLUMN, foredit_link.getColumnValues(ATTRIBUTEIDCOLUMN, sep));
                        editprops_link.setProperty("attributesdcid", foredit_link.getColumnValues("attributesdcid", sep));
                        editprops_link.setProperty("attributeinstance", foredit_link.getColumnValues("attributeinstance", sep));
                        editprops_link.setProperty("defaultvalue", EditSDIAttribute.getValues(foredit_link, sep, m18n));
                        editprops_link.setProperty("mandatory", foredit_link.getColumnValues("mandatoryflag", sep));
                        editprops_link.setProperty("updatable", foredit_link.getColumnValues("updateableflag", sep));
                        editprops_link.setProperty("hidden", foredit_link.getColumnValues("hiddenflag", sep));
                        editprops_link.setProperty("editorstyle", foredit_link.getColumnValues("editorstyleid", sep));
                        editprops_link.setProperty("editsdcid", foredit_link.getColumnValues("editsdcid", sep));
                        editprops_link.setProperty("editreftypeid", foredit_link.getColumnValues("editreftypeid", sep));
                        editprops_link.setProperty("instructionflag", foredit_link.getColumnValues("instructionflag", sep));
                        editprops_link.setProperty("instructiontext", foredit_link.getColumnValues("instructiontext", sep));
                        editprops_link.setProperty("copydowncontext", foredit_link.getColumnValues("copydowncontext", sep));
                        editprops_link.setProperty("attributetypeflag", foredit_link.getColumnValues("attributetypeflag", sep));
                        editprops_link.setProperty("worksheetcontext", foredit_link.getColumnValues("worksheetcontext", sep));
                        editprops_link.setProperty("usersequence", foredit_link.getColumnValues("usersequence", sep));
                        ab.setActionClass("editlinkattributes", EditSDIAttribute.class.getName(), editprops_link);
                    }
                    if (foredit_adhoc.getRowCount() > 0) {
                        Logger.logDebug("Adhoc/SDC/Link Attributes for edit...");
                        foredit_adhoc.sort("sdcid");
                        ArrayList<DataSet> sdiAttributeGroups = foredit_adhoc.getGroupedDataSets("sdcid");
                        for (int g = 0; g < sdiAttributeGroups.size(); ++g) {
                            DataSet sdiAttributes = sdiAttributeGroups.get(g);
                            PropertyList editprops_adhoc = new PropertyList();
                            editprops_adhoc.setProperty("sdcid", sdiAttributes.getValue(0, "sdcid", ""));
                            editprops_adhoc.setProperty("keyid1", sdiAttributes.getColumnValues("keyid1", sep));
                            editprops_adhoc.setProperty("keyid2", sdiAttributes.getColumnValues("keyid2", sep));
                            editprops_adhoc.setProperty("keyid3", sdiAttributes.getColumnValues("keyid3", sep));
                            editprops_adhoc.setProperty(ATTRIBUTEIDCOLUMN, sdiAttributes.getColumnValues(ATTRIBUTEIDCOLUMN, sep));
                            editprops_adhoc.setProperty("attributesdcid", sdiAttributes.getColumnValues("attributesdcid", sep));
                            editprops_adhoc.setProperty("attributeinstance", sdiAttributes.getColumnValues("attributeinstance", sep));
                            editprops_adhoc.setProperty("value", EditSDIAttribute.getValues(sdiAttributes, sep, m18n));
                            editprops_adhoc.setProperty("usersequence", sdiAttributes.getColumnValues("usersequence", sep));
                            ab.setActionClass("editadhocattributes_" + g, EditSDIAttribute.class.getName(), editprops_adhoc);
                        }
                    }
                } else {
                    Logger.logDebug("No attributes for edit.");
                }
            } else {
                Logger.logDebug("No attributes in attribute data.");
            }
        } else {
            throw new SapphireException("No attribute data provided.");
        }
    }

    public static void saveAttributeData(DataSet attributedata, ActionProcessor actionProc, M18NUtil m18n) throws SapphireException {
        BaseSDIAttributeAction.saveAttributeData(attributedata, actionProc, m18n, "", "", "");
    }

    public static void saveAttributeData(DataSet attributedata, ActionProcessor actionProc, M18NUtil m18n, String auditReason, String auditActivity, String auditSignedFlag) throws SapphireException {
        try {
            ActionBlock ab = new ActionBlock();
            BaseSDIAttributeAction.addSaveAttributeDataToActionBlock(attributedata, ab, m18n);
            if (auditReason.length() > 0) {
                for (int actionCount = 0; actionCount < ab.getActionCount(); ++actionCount) {
                    HashMap props = ab.getActionProperties(actionCount);
                    props.put("auditreason", auditReason);
                    props.put("auditactivity", auditActivity);
                    props.put("auditsignedflag", auditSignedFlag);
                }
            }
            actionProc.processActionBlock(ab);
        }
        catch (Exception e) {
            Logger.logError("Failed to process attributes.", e);
            throw new SapphireException("Could not save attribute data.", e);
        }
    }

    protected static String getRealDate(String textstring, String datestring, M18NUtil m18n, boolean userTimezoneAdjust, Logger logger) {
        return BaseSDIAttributeAction.getRealDate(textstring, datestring, m18n, m18n, userTimezoneAdjust, logger);
    }

    protected static String getRealDate(String textstring, String datestring, M18NUtil m18nTo, M18NUtil m18nFrom, boolean userTimezoneAdjust, Logger logger) {
        String date;
        if (textstring.length() > 0) {
            try {
                date = m18nTo.format(m18nTo.parseCalendar(textstring, false), false);
            }
            catch (Exception e) {
                logger.warn("Invalid default date expression found.");
                date = m18nTo.format(m18nFrom.parseCalendar(datestring, userTimezoneAdjust), userTimezoneAdjust);
            }
        } else {
            date = datestring.length() > 0 ? m18nTo.format(m18nFrom.parseCalendar(datestring, userTimezoneAdjust), userTimezoneAdjust) : "";
        }
        return date;
    }

    public static void processEnteredNumber(String currentdefault, StringBuffer defaulttexts, StringBuffer defaultnumerics, M18NUtil userM18N, Logger logger) {
        BaseSDIAttributeAction.processEnteredNumber(currentdefault, defaulttexts, defaultnumerics, userM18N, false, logger);
    }

    public static void processEnteredNumber(String currentdefault, StringBuffer defaulttexts, StringBuffer defaultnumerics, M18NUtil userM18N, boolean returnNumberInUserLocal, Logger logger) {
        StringBuffer value = new StringBuffer();
        StringBuffer valuenum = new StringBuffer();
        try {
            MiscUtil.MiscString.parseComplexNumber(currentdefault, valuenum, value, userM18N, true);
            defaultnumerics.append(valuenum);
            defaulttexts.append(value);
        }
        catch (NumberFormatException e) {
            logger.warn("Could not parse number provided.");
        }
    }

    public static void processEnteredDate(String currentdefault, StringBuffer defaulttexts, StringBuffer defaultdates, String nullvalue, M18NUtil m18n, boolean userTimeZoneSensitive, Logger logger) {
        Calendar cal = m18n.parseCalendar(currentdefault, userTimeZoneSensitive);
        if (cal != null) {
            if (currentdefault.length() == 0) {
                defaulttexts.append(nullvalue);
                defaultdates.append(nullvalue);
            } else if (m18n.isRelDate(currentdefault, !userTimeZoneSensitive)) {
                defaulttexts.append(currentdefault);
                defaultdates.append(nullvalue);
            } else {
                defaultdates.append(currentdefault);
                defaulttexts.append(nullvalue);
            }
        } else {
            logger.warn("Could not parse date provided.");
        }
    }

    public static void processDefaults(PropertyList properties, String nullvalue, DateTimeUtil usersdtu, M18NUtil usersM18N, Logger logger) {
        String[] defaults;
        String[] datatypes = StringUtil.split(properties.getProperty("datatype", "S"), ";");
        if (datatypes.length == (defaults = StringUtil.split(properties.getProperty("default", ""), ";")).length) {
            StringBuffer defaulttexts = new StringBuffer();
            StringBuffer defaultnumerics = new StringBuffer();
            StringBuffer defaultclobs = new StringBuffer();
            StringBuffer defaultdates = new StringBuffer();
            for (int i = 0; i < datatypes.length; ++i) {
                if (i > 0) {
                    defaulttexts.append(";");
                    defaultnumerics.append(";");
                    defaultclobs.append(";");
                    defaultdates.append(";");
                }
                String datatype = datatypes[i];
                String currentdefault = defaults[i];
                if (datatype.equalsIgnoreCase("C")) {
                    defaultclobs.append(currentdefault);
                    defaultdates.append(nullvalue);
                    defaulttexts.append(nullvalue);
                    defaultnumerics.append(nullvalue);
                    continue;
                }
                if (datatype.equalsIgnoreCase("D") || datatype.equalsIgnoreCase("O")) {
                    if (currentdefault.length() == 0 || currentdefault.equalsIgnoreCase(nullvalue)) {
                        defaultdates.append(nullvalue);
                        defaulttexts.append(nullvalue);
                    } else {
                        BaseSDIAttributeAction.processEnteredDate(currentdefault, defaulttexts, defaultdates, nullvalue, usersM18N, datatype.equalsIgnoreCase("D"), logger);
                    }
                    defaultnumerics.append(nullvalue);
                    defaultclobs.append(nullvalue);
                    continue;
                }
                if (datatype.equalsIgnoreCase("N")) {
                    if (currentdefault.length() == 0 || currentdefault.equalsIgnoreCase(nullvalue)) {
                        defaultnumerics.append(nullvalue);
                        defaulttexts.append(nullvalue);
                    } else {
                        BaseSDIAttributeAction.processEnteredDate(currentdefault, defaulttexts, defaultdates, nullvalue, usersM18N, datatype.equalsIgnoreCase("D"), logger);
                    }
                    BaseSDIAttributeAction.processEnteredNumber(currentdefault, defaulttexts, defaultnumerics, usersM18N, true, logger);
                    defaultclobs.append(nullvalue);
                    defaultdates.append(nullvalue);
                    continue;
                }
                defaulttexts.append(currentdefault);
                defaultdates.append(nullvalue);
                defaultclobs.append(nullvalue);
                defaultnumerics.append(nullvalue);
            }
            properties.setProperty("defaulttextvalue", defaulttexts.toString());
            properties.setProperty("defaultnumericvalue", defaultnumerics.toString());
            properties.setProperty("defaultclobvalue", defaultclobs.toString());
            properties.setProperty("defaultdatevalue", defaultdates.toString());
            properties.remove("default");
        } else {
            logger.warn("Datatypes and defaults do not match.");
        }
    }

    public static boolean isRequiredComplete(PropertyList sdcprops, DataSet updatedData, DataSet existingData, String rsetid, QueryProcessor qp, Logger logger) {
        if (sdcprops.getProperty("allowattributesflag", "N").equalsIgnoreCase("Y")) {
            if (sdcprops.getProperty("requiredattributesflag", "N").equalsIgnoreCase("Y")) {
                if (existingData == null) {
                    existingData = BaseSDIAttributeAction.getExistingAttributes(rsetid, qp, logger);
                }
                if (existingData != null) {
                    HashMap<String, String> hmfilter = new HashMap<String, String>();
                    hmfilter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                    DataSet filteredExisting = existingData.getFilteredDataSet(hmfilter, true);
                    hmfilter = new HashMap();
                    hmfilter.put("mandatoryflag", "Y");
                    filteredExisting = filteredExisting.getFilteredDataSet(hmfilter, false);
                    if (updatedData != null && updatedData.getRowCount() > 0) {
                        hmfilter = new HashMap();
                        hmfilter.put("attributesourcetype", BaseSDIAttributeAction.getAttributeType(AttributeType.linkdef));
                        DataSet filteredUpdated = updatedData.getFilteredDataSet(hmfilter, true);
                        hmfilter = new HashMap();
                        hmfilter.put("mandatoryflag", "Y");
                        filteredUpdated = filteredUpdated.getFilteredDataSet(hmfilter, false);
                        for (int i = 0; i < filteredUpdated.getRowCount(); ++i) {
                            String attributeid = filteredUpdated.getValue(i, ATTRIBUTEIDCOLUMN, "");
                            String attributesdcid = filteredUpdated.getValue(i, "attributesdcid", "");
                            BigDecimal attributeinstance = filteredUpdated.getBigDecimal(i, "attributeinstance");
                            String sourcesdcid = filteredUpdated.getValue(i, "sourcesdcid", "");
                            String attributesourcetype = filteredUpdated.getValue(i, "attributesourcetype", "");
                            HashMap<String, Object> find = new HashMap<String, Object>();
                            find.put(ATTRIBUTEIDCOLUMN, attributeid);
                            find.put("attributesdcid", attributesdcid);
                            find.put("attributeinstance", attributeinstance);
                            find.put("sourcesdcid", sourcesdcid);
                            find.put("attributesourcetype", attributesourcetype);
                            int found = filteredExisting.findRow(find);
                            if (found > -1) {
                                filteredExisting.deleteRow(found);
                            }
                            filteredExisting.copyRow(updatedData, i, 1);
                        }
                        logger.debug("Updated data merged with existing data to give " + filteredExisting.getRowCount() + " row(s) of required non-linkdef attributes.");
                    }
                    if (filteredExisting.getRowCount() > 0) {
                        ArrayList<String> vals = BaseSDIAttributeAction.getValues(filteredExisting, new M18NUtil());
                        boolean empty = false;
                        for (String val : vals) {
                            if (val != null && val.length() != 0) continue;
                            empty = true;
                            break;
                        }
                        return !empty;
                    }
                    logger.debug("No data remaining after filter and merge.");
                    return false;
                }
                logger.debug("Could not obtain attribute data.");
                return false;
            }
            logger.debug("SDC does not use required attributes.");
            return false;
        }
        logger.debug("SDC does not use attributes.");
        return false;
    }

    public static enum AttributeType {
        sdc,
        adhoc,
        template,
        linkdef,
        link;

    }
}

