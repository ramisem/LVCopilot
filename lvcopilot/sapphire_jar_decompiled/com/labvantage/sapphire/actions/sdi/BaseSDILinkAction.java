/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.DDTConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SequenceService;
import com.labvantage.sapphire.services.ServiceException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseSDILinkAction
extends BaseAction
implements DDTConstants {
    public static final String REPLACENULL = "__null";
    private static final String PROPERTY_SEPARATOR = "separator";
    private static final String PROPERTY_NULLREPLACE = "nullreplace";
    public static final String PROPERTY_RETURNGENERATEDKEY = "returngeneratedkey";

    protected void maintainSDIDetail(String actionid, PropertyList properties) throws SapphireException {
        block71: {
            block67: {
                String[] rowvalues;
                boolean errors;
                int rownum;
                String[] detailkeycolids;
                ArrayList keycolids;
                String[] sdckeycolids;
                StringBuffer addedNewprops;
                String tableid;
                Calendar now;
                PropertyList sdc;
                boolean nullReplace;
                String sdcid;
                String linkid;
                String separator;
                block68: {
                    String vals;
                    int i;
                    block69: {
                        block70: {
                            SDCProcessor sdcProcessor;
                            separator = properties.getProperty(PROPERTY_SEPARATOR, ";");
                            linkid = properties.getProperty("linkid");
                            if (linkid.length() == 0) {
                                throw new SapphireException("INVALID_PROPERTY", "No linkid specified");
                            }
                            sdcid = properties.getProperty("sdcid");
                            if (sdcid.length() == 0) {
                                throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
                            }
                            String detaillinkid = properties.getProperty("detaillinkid");
                            boolean detailLink = detaillinkid.length() > 0;
                            boolean bl = nullReplace = !properties.getProperty(PROPERTY_NULLREPLACE, "Y").equalsIgnoreCase("N");
                            if (properties.getProperty("tracelogid", "").length() == 0 && properties.getProperty("auditreason", "").length() > 0) {
                                properties.setProperty("tracelogid", this.getTracelogid(properties));
                            }
                            properties.remove("linkid");
                            properties.remove("sdcid");
                            properties.remove(PROPERTY_SEPARATOR);
                            if (detailLink) {
                                properties.remove("detaillinkid");
                            }
                            if ((sdc = (sdcProcessor = this.getSDCProcessor()).getPropertyList(sdcid)) == null) break block67;
                            sdcid = sdc.getProperty("sdcid");
                            now = DateTimeUtil.getNowCalendar();
                            tableid = this.getLinkedTableId(sdc, detailLink, linkid, detaillinkid, sdcid);
                            addedNewprops = new StringBuffer();
                            sdckeycolids = this.getParentKeyCols(sdc, detailLink, linkid, sdcid, sdcProcessor, properties, addedNewprops);
                            HashMap detailKeyCols = this.getDetailKeyCols(detailLink, linkid, detaillinkid, sdcid, sdcProcessor, sdckeycolids);
                            keycolids = (ArrayList)detailKeyCols.get("keycolids");
                            detailkeycolids = (String[])detailKeyCols.get("detailkeycolids");
                            this.logger.info("Checking key columns: " + keycolids.size());
                            rownum = 0;
                            errors = false;
                            for (i = 0; i < keycolids.size(); ++i) {
                                vals = properties.getProperty((String)keycolids.get(i));
                                if (vals.length() <= 0) continue;
                                rowvalues = StringUtil.split(vals, separator);
                                if (rownum == 0) {
                                    rownum = rowvalues.length;
                                    continue;
                                }
                                if (rownum == rowvalues.length) continue;
                                errors = true;
                            }
                            if (rownum != 0) break block68;
                            if (!properties.containsKey("keyid1")) break block69;
                            String vals2 = properties.getProperty("keyid1");
                            if (vals2.length() <= 0) break block68;
                            rowvalues = StringUtil.split(vals2, separator);
                            if (rownum != 0) break block70;
                            rownum = rowvalues.length;
                            break block68;
                        }
                        if (rownum == rowvalues.length) break block68;
                        errors = true;
                        break block68;
                    }
                    for (i = 0; i < detailkeycolids.length; ++i) {
                        vals = properties.getProperty(detailkeycolids[i]);
                        if (vals.length() <= 0) continue;
                        rowvalues = StringUtil.split(vals, separator);
                        if (rownum == 0) {
                            rownum = rowvalues.length;
                            continue;
                        }
                        if (rownum == rowvalues.length) continue;
                        errors = true;
                    }
                }
                if (errors) {
                    throw new SapphireException("INVALID_PROPERTY", "The number of key values is not consistent.");
                }
                this.logger.info("Locking the SDI");
                DAMProcessor dam = this.getDAMProcessor();
                String rsetid = "";
                boolean applylock = properties.getProperty("applylock").equals("Y");
                String tempKeyId1 = properties.getProperty("keyid1");
                String tempKeyId2 = properties.getProperty("keyid2");
                String tempKeyId3 = properties.getProperty("keyid3");
                if (!separator.equals(";")) {
                    tempKeyId1 = StringUtil.replaceAll(tempKeyId1, separator, ";");
                    tempKeyId2 = StringUtil.replaceAll(tempKeyId2, separator, ";");
                    tempKeyId3 = StringUtil.replaceAll(tempKeyId3, separator, ";");
                }
                if ((rsetid = applylock ? dam.createLockedRSet(sdcid, tempKeyId1, tempKeyId2, tempKeyId3) : dam.createRSet(sdcid, tempKeyId1, tempKeyId2, tempKeyId3)).length() == 0) {
                    throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for sdidetail maintenance");
                }
                DataSet detaildata = new DataSet(this.connectionInfo);
                this.logger.info("Adding the rows: " + rownum);
                this.logger.info("number of keycols: " + sdckeycolids.length);
                int current = detaildata.addRow();
                for (int i = 0; i < sdckeycolids.length; ++i) {
                    this.logger.info("Setting: " + sdckeycolids[i] + " with " + properties.getProperty("keyid" + (i + 1)));
                    detaildata.addColumn(sdckeycolids[i], 0);
                    detaildata.setValue(current, sdckeycolids[i], properties.getProperty("keyid" + (i + 1)));
                }
                if (rownum > 1) {
                    detaildata.copyRow(current, rownum - 1);
                }
                this.logger.info("Setting the values");
                properties.remove("keyid1");
                properties.remove("keyid2");
                properties.remove("keyid3");
                String __temp = (String)properties.get("__sdcid");
                if (__temp != null && __temp.length() > 0) {
                    this.logger.debug(this.getClass().getName(), "__sdcid found and replaced.");
                    properties.put("sdcid", __temp);
                }
                if ((__temp = (String)properties.get("__keyid1")) != null && __temp.length() > 0) {
                    this.logger.debug(this.getClass().getName(), "__keyid1 found and replaced.");
                    properties.put("keyid1", __temp);
                }
                if ((__temp = (String)properties.get("__keyid2")) != null && __temp.length() > 0) {
                    this.logger.debug(this.getClass().getName(), "__keyid2 found and replaced.");
                    properties.put("keyid2", __temp);
                }
                if ((__temp = (String)properties.get("__keyid3")) != null && __temp.length() > 0) {
                    this.logger.debug(this.getClass().getName(), "__keyid3 found and replaced.");
                    properties.put("keyid3", __temp);
                }
                ArrayList<String> keygencolids = new ArrayList<String>();
                ArrayList<String> keygenrules = new ArrayList<String>();
                SafeSQL safeSQL = new SafeSQL();
                this.database.createPreparedResultSet("SELECT syscolumn.columnid, syscolumn.datatype, syscolumn.nnflag, propertyvalue FROM   syscolumn LEFT OUTER JOIN syscolumnproperty ON syscolumn.tableid = syscolumnproperty.tableid AND syscolumn.columnid = syscolumnproperty.columnid AND syscolumnproperty.propertyid = " + safeSQL.addVar("keygenerationrule") + " WHERE  LOWER( syscolumn.tableid ) = " + safeSQL.addVar(tableid.toLowerCase()) + " ORDER BY syscolumn.columnsequence", safeSQL.getValues());
                while (this.database.getNext()) {
                    boolean isBlank;
                    String id = this.database.getString("columnid");
                    String value = properties.getProperty(this.database.getString("columnid"));
                    String tempValue = StringUtil.replaceAll(properties.getProperty(this.database.getString("columnid")), "|", "");
                    boolean keycol = false;
                    for (int i = 0; i < keycolids.size() && !keycol; ++i) {
                        if (!id.equalsIgnoreCase((String)keycolids.get(i))) continue;
                        keycol = true;
                    }
                    rowvalues = StringUtil.split(value, separator);
                    boolean bl = isBlank = rowvalues.length == 0 || rowvalues[0].length() == 0 || rowvalues[0].equals("(null)");
                    if (keycol && (isBlank || rowvalues[0].toLowerCase().startsWith("(auto)")) && this.database.getString("propertyvalue") != null) {
                        keygencolids.add(id);
                        keygenrules.add(this.database.getString("propertyvalue"));
                        continue;
                    }
                    if (tempValue.length() > 0) {
                        int i;
                        this.logger.info("Adding the column '" + id + "'");
                        if (this.database.getString("datatype").equalsIgnoreCase("C")) {
                            detaildata.addColumn(id, 0);
                        } else if (this.database.getString("datatype").equalsIgnoreCase("N")) {
                            detaildata.addColumn(id, 1);
                        } else if (this.database.getString("datatype").equalsIgnoreCase("R")) {
                            detaildata.addColumn(id, 1);
                        } else if (this.database.getString("datatype").equalsIgnoreCase("T")) {
                            detaildata.addColumn(id, 3);
                        } else {
                            detaildata.addColumn(id, 2);
                            if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty(sdcid, id, "timezoneindependent"))) {
                                detaildata.setTimeZoneInsensitive(id);
                            }
                        }
                        this.logger.info("Setting the value '" + value + "'");
                        if (rowvalues.length > 1) {
                            for (i = 0; i < rowvalues.length; ++i) {
                                if (rowvalues[i].equalsIgnoreCase("(null)")) {
                                    detaildata.setValue(i, id, "");
                                    continue;
                                }
                                if (nullReplace && rowvalues[i].equalsIgnoreCase(REPLACENULL)) {
                                    detaildata.setValue(i, id, "(null)");
                                    continue;
                                }
                                if ("ParamList".equals(sdcid) && "param list items".equals(linkid) && "defaultvalue".equals(id) && ("N".equals(detaildata.getString(i, "datatypes")) || "NC".equals(detaildata.getString(i, "datatypes")))) {
                                    rowvalues[i] = StringUtil.replaceAll(rowvalues[i], "" + FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator(), FormatUtil.getInstance().getDecimalSeparator() + "");
                                }
                                detaildata.setValue(i, id, rowvalues[i]);
                            }
                            continue;
                        }
                        for (i = 0; i < rownum; ++i) {
                            if (rowvalues[0].equalsIgnoreCase("(null)")) {
                                detaildata.setValue(i, id, "");
                                continue;
                            }
                            if (nullReplace && rowvalues[0].equalsIgnoreCase(REPLACENULL)) {
                                detaildata.setValue(i, id, "(null)");
                                continue;
                            }
                            if ("ParamList".equals(sdcid) && "param list items".equals(linkid) && "defaultvalue".equals(id) && ("N".equals(detaildata.getString(i, "datatypes")) || "NC".equals(detaildata.getString(i, "datatypes")))) {
                                rowvalues[i] = StringUtil.replaceAll(rowvalues[i], "" + FormatUtil.getInstance(this.connectionInfo).getDecimalSeparator(), FormatUtil.getInstance().getDecimalSeparator() + "");
                            }
                            detaildata.setValue(i, id, rowvalues[0]);
                        }
                        continue;
                    }
                    if (!this.database.getString("nnflag").equalsIgnoreCase("Y")) continue;
                    boolean sdckeycol = false;
                    for (int i = 0; i < sdckeycolids.length && !sdckeycol; ++i) {
                        if (!id.equalsIgnoreCase(sdckeycolids[i])) continue;
                        sdckeycol = true;
                    }
                    if (sdckeycol) continue;
                    if ("WorkItem".equals(sdcid) && "WorkItemItems".equals(linkid)) {
                        String[] tempWorkItemVersionIDArray = StringUtil.split(detaildata.getValue(0, "workitemversionid"), "|");
                        this.database.createPreparedResultSet("workitemitem", "SELECT workitemitemid FROM workitemitem WHERE workitemid=? AND workitemversionid=?", new Object[]{detaildata.getValue(0, "workitemid"), tempWorkItemVersionIDArray[0]});
                        int maxid = 1;
                        while (this.database.getNext("workitemitem")) {
                            String workitemitemid = this.database.getString("workitemitem", "workitemitemid");
                            try {
                                int itemid = Integer.parseInt(workitemitemid);
                                if (itemid <= maxid) continue;
                                maxid = itemid;
                            }
                            catch (Exception itemid) {}
                        }
                        this.database.closeResultSet("workitemitem");
                        detaildata.addColumn("workitemitemid", 0);
                        for (int i = 0; i < detaildata.getRowCount(); ++i) {
                            String val;
                            String string = val = rowvalues.length > i ? rowvalues[i] : "";
                            if (val.length() == 0) {
                                val = "" + ++maxid;
                            }
                            detaildata.setValue(i, id, val);
                        }
                        continue;
                    }
                    throw new SapphireException("INVALID_PARAMETER", "A value for NotNull column " + id + " has not be specified");
                }
                if (addedNewprops != null && addedNewprops.length() > 0) {
                    String[] addedNewpropsArr = StringUtil.split(addedNewprops.substring(1), ";");
                    for (int i = 0; i < addedNewpropsArr.length; ++i) {
                        String columnName = addedNewpropsArr[i];
                        if (!properties.containsKey(columnName)) continue;
                        properties.remove(columnName);
                    }
                }
                for (int i = 0; i < keygencolids.size(); ++i) {
                    SequenceService seqService = new SequenceService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    try {
                        String columnid = (String)keygencolids.get(i);
                        seqService.generateKeys(sdcid, tableid + "." + columnid, columnid, (String)keygenrules.get(i), detaildata);
                        if (!properties.getProperty(PROPERTY_RETURNGENERATEDKEY).equals("Y") || properties.getProperty(columnid).length() != 0) continue;
                        properties.setProperty(columnid, detaildata.getColumnValues(columnid, ";"));
                        continue;
                    }
                    catch (ServiceException e) {
                        throw new SapphireException("KEYGENERATOR_FAILED", "Failed to generate keys", e);
                    }
                }
                boolean add = actionid.equals("AddSDIDetail");
                SDIData beforeEditImage = null;
                SDIData sdiData = new SDIData();
                sdiData.setDataset(tableid, detaildata);
                BaseSDCRules sdcPreRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, sdc, add ? "PreAddDetail" : "PreEditDetail");
                if (sdcPreRules.requiresEditDetailPrimary() || sdcPreRules.customRulesRequiresEditDetailPrimary() || sdcPreRules.requiresBeforeEditDetailImage() || sdcPreRules.customRulesRequiresBeforeEditDetailImage()) {
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcid);
                    sdiRequest.setRsetid(rsetid);
                    sdiRequest.setRetainRsetid(true);
                    if (sdcPreRules.requiresEditDetailPrimary() || sdcPreRules.customRulesRequiresEditDetailPrimary()) {
                        sdiRequest.setRequestItem("primary");
                    }
                    if (sdcPreRules.requiresBeforeEditDetailImage() || sdcPreRules.customRulesRequiresBeforeEditDetailImage()) {
                        sdiRequest.setRequestItem(tableid);
                    }
                    SDIProcessor sdiProcessor = this.getSDIProcessor();
                    beforeEditImage = sdiProcessor.getSDIData(sdiRequest);
                    sdcPreRules.setBeforeEditImage(beforeEditImage);
                    if (beforeEditImage != null && beforeEditImage.getDataset("primary") != null) {
                        sdiData.setDataset("primary", beforeEditImage.getDataset("primary"));
                    }
                }
                try {
                    properties.setProperty(PROPERTY_SEPARATOR, separator);
                    if (actionid.equals("AddSDIDetail")) {
                        detaildata.setString(-1, "createby", this.connectionInfo.getSysuserId());
                        detaildata.setString(-1, "createtool", actionid);
                        detaildata.setDate(-1, "createdt", now);
                        detaildata.setString(-1, "modby", this.connectionInfo.getSysuserId());
                        detaildata.setString(-1, "modtool", actionid);
                        detaildata.setDate(-1, "moddt", now);
                        if ("".equals(properties.getProperty("tracelogid", ""))) {
                            detaildata.setString(-1, "tracelogid", null);
                        }
                        Trace.startBusinessRule(sdcid + "." + "PreAddDetail", true);
                        sdcPreRules.preAddDetail(sdiData, properties);
                        Trace.endBusinessRule(sdcid + "." + "PreAddDetail", true);
                        Trace.startBusinessRule(sdcid + "." + "PreAddDetail", false);
                        for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                            customRules.preAddDetail(sdiData, properties);
                        }
                        Trace.endBusinessRule(sdcid + "." + "PreAddDetail", false);
                        sdcPreRules.endRule();
                        DataSetUtil.insert(this.database, detaildata, tableid);
                        BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, null, "PostAddDetail");
                        Trace.startBusinessRule(sdcid + "." + "PostAddDetail", true);
                        sdcPostRules.postAddDetail(sdiData, properties);
                        Trace.endBusinessRule(sdcid + "." + "PostAddDetail", true);
                        Trace.startBusinessRule(sdcid + "." + "PostAddDetail", false);
                        for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                            customRules.postAddDetail(sdiData, properties);
                        }
                        Trace.endBusinessRule(sdcid + "." + "PostAddDetail", false);
                        sdcPostRules.endRule();
                    } else {
                        detaildata.setString(-1, "modby", this.connectionInfo.getSysuserId());
                        detaildata.setString(-1, "modtool", actionid);
                        detaildata.setDate(-1, "moddt", now);
                        if ("".equals(properties.getProperty("tracelogid", ""))) {
                            detaildata.setString(-1, "tracelogid", null);
                        }
                        Trace.startBusinessRule(sdcid + "." + "PreEditDetail", true);
                        sdcPreRules.preEditDetail(sdiData, properties);
                        Trace.endBusinessRule(sdcid + "." + "PreEditDetail", true);
                        Trace.startBusinessRule(sdcid + "." + "PreEditDetail", false);
                        for (BaseSDCRules customRules : sdcPreRules.getCustomRuleList()) {
                            customRules.preEditDetail(sdiData, properties);
                        }
                        Trace.endBusinessRule(sdcid + "." + "PreEditDetail", false);
                        sdcPreRules.endRule();
                        DataSetUtil.update(this.database, detaildata, tableid, detailkeycolids);
                        BaseSDCRules sdcPostRules = BaseSDCRules.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), this.getErrorHandler(), sdcid, null, "PostEditDetail");
                        sdcPostRules.setBeforeEditImage(beforeEditImage);
                        Trace.startBusinessRule(sdcid + "." + "PostEditDetail", true);
                        sdcPostRules.postEditDetail(sdiData, properties);
                        Trace.endBusinessRule(sdcid + "." + "PostEditDetail", true);
                        Trace.startBusinessRule(sdcid + "." + "PostEditDetail", false);
                        for (BaseSDCRules customRules : sdcPostRules.getCustomRuleList()) {
                            customRules.postEditDetail(sdiData, properties);
                        }
                        Trace.endBusinessRule(sdcid + "." + "PostEditDetail", false);
                        sdcPostRules.endRule();
                    }
                }
                catch (SapphireException se) {
                    if (this.connectionInfo.getDbms().equals("ORA") && se.getMessage().indexOf("ORA-00001") > -1 || this.connectionInfo.getDbms().equals("MSS") && se.getMessage().indexOf("duplicate key") > -1) {
                        this.getErrorHandler().add(sdcid, actionid, "CheckUniqueness", "VALIDATION", sdcid + " detail value already exists - choose another name.");
                    }
                    throw se;
                }
                if (rsetid != null) {
                    dam.clearRSet(rsetid);
                }
                break block71;
            }
            throw new SapphireException("INVALID_PROPERTY", "Could not get SDC properties.");
        }
    }

    private HashMap getDetailKeyCols(boolean detailLink, String linkid, String detaillinkid, String sdcid, SDCProcessor sdcProcessor, String[] parentkeycolids) {
        HashMap<String, Object> detailKeyColsHM = new HashMap<String, Object>();
        ArrayList<String> keycolids = new ArrayList<String>();
        String[] detailkeycolids = null;
        if (detailLink) {
            this.logger.info("Retrieving detail link information");
            HashMap linkProperties = sdcProcessor.getDetailLinkProperties(sdcid, linkid + ";" + detaillinkid);
            int keycolcount = Integer.parseInt((String)linkProperties.get("keycolcount"));
            detailkeycolids = new String[keycolcount];
            for (int j = 0; j < keycolcount; ++j) {
                boolean sdclinkkeycol = false;
                detailkeycolids[j] = (String)linkProperties.get("keycolid" + String.valueOf(j + 1));
                for (int k = 0; k < parentkeycolids.length; ++k) {
                    if (!parentkeycolids[k].equalsIgnoreCase(detailkeycolids[j])) continue;
                    sdclinkkeycol = true;
                    break;
                }
                if (sdclinkkeycol) continue;
                keycolids.add(detailkeycolids[j]);
                this.logger.info("Found a new detaillink key column: '" + detailkeycolids[j] + "'");
            }
        } else {
            this.logger.info("Retrieving detail information");
            HashMap linkProperties = sdcProcessor.getLinkProperties(sdcid, linkid);
            int keycolcount = Integer.parseInt((String)linkProperties.get("keycolcount"));
            detailkeycolids = new String[keycolcount];
            for (int i = 0; i < keycolcount; ++i) {
                boolean sdckeycol = false;
                detailkeycolids[i] = (String)linkProperties.get("keycolid" + String.valueOf(i + 1));
                for (int j = 0; j < parentkeycolids.length; ++j) {
                    if (!parentkeycolids[j].equalsIgnoreCase(detailkeycolids[i])) continue;
                    sdckeycol = true;
                }
                if (sdckeycol) continue;
                keycolids.add(detailkeycolids[i]);
                this.logger.info("Found a new detail key column: '" + detailkeycolids[i] + "'");
            }
        }
        detailKeyColsHM.put("keycolids", keycolids);
        detailKeyColsHM.put("detailkeycolids", detailkeycolids);
        return detailKeyColsHM;
    }

    private String getLinkedTableId(PropertyList sdc, boolean detailLink, String linkid, String detaillinkid, String sdcid) throws SapphireException {
        String tableid;
        PropertyList link;
        if (detailLink) {
            PropertyListCollection detaillinks = sdc.getCollectionNotNull("detaillinks");
            link = detaillinks.getPropertyList(linkid + ';' + detaillinkid);
            if (link == null) {
                throw new SapphireException("INVALID_PROPERTY", "The pair (sdcid, linkid, detaillinkid) - (" + sdcid + ", " + linkid + "," + detaillinkid + ") is invalid.");
            }
        } else {
            PropertyListCollection links = sdc.getCollectionNotNull("links");
            link = links.getPropertyList(linkid);
            if (link == null) {
                throw new SapphireException("INVALID_PROPERTY", "The pair (sdcid, linkid) - (" + sdcid + ", " + linkid + ") is invalid.");
            }
        }
        if ((tableid = link.getProperty("linktableid", "")).length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No table id could be found for sdcid " + sdcid + " and linkid " + linkid + ".");
        }
        String linktype = link.getProperty("linktype", "");
        if (!linktype.equalsIgnoreCase("D") && !linktype.equalsIgnoreCase("M")) {
            throw new SapphireException("INVALID_PROPERTY", "This link '" + linkid + "' cannot be updated using this action.");
        }
        this.logger.debug("tableid = " + tableid + " & linktype = " + linktype);
        return tableid;
    }

    private String[] getParentKeyCols(PropertyList sdc, boolean detailLink, String linkid, String sdcid, SDCProcessor sdcProcessor, PropertyList properties, StringBuffer addedNewprops) throws SapphireException {
        String[] parentkeycolids = null;
        if (detailLink) {
            this.logger.info("Checking the SDC Link keycols");
            HashMap linkProperties = sdcProcessor.getLinkProperties(sdcid, linkid);
            int keycolcount = Integer.parseInt((String)linkProperties.get("keycolcount"));
            parentkeycolids = new String[keycolcount];
            for (int i = 1; i <= keycolcount; ++i) {
                parentkeycolids[i - 1] = (String)linkProperties.get("keycolid" + String.valueOf(i));
                String keyvalue = properties.getProperty("keyid" + String.valueOf(i), "");
                if (keyvalue.length() == 0) {
                    keyvalue = properties.getProperty(parentkeycolids[i - 1]);
                    properties.setProperty("keyid" + i, keyvalue);
                }
                if (keyvalue.length() == 0 || keyvalue.equalsIgnoreCase("(null)")) {
                    throw new SapphireException("INVALID_PROPERTY", "The property 'keyid" + String.valueOf(i) + "' cannot be null.");
                }
                if (properties.getProperty(parentkeycolids[i - 1], "").length() != 0) continue;
                properties.setProperty(parentkeycolids[i - 1], keyvalue);
                addedNewprops.append(";").append(parentkeycolids[i - 1]);
            }
        } else {
            this.logger.info("Checking the SDC keycols");
            int sdckeycols = Integer.parseInt(sdc.getProperty("keycolumns"));
            parentkeycolids = new String[sdckeycols];
            for (int i = 1; i <= sdckeycols; ++i) {
                parentkeycolids[i - 1] = sdc.getProperty("keycolid" + String.valueOf(i), "");
                String keyvalue = properties.getProperty("keyid" + String.valueOf(i), "");
                if (keyvalue.length() == 0) {
                    keyvalue = properties.getProperty(parentkeycolids[i - 1]);
                    properties.setProperty("keyid" + i, keyvalue);
                }
                if (keyvalue.length() == 0 || keyvalue.equalsIgnoreCase("(null)")) {
                    throw new SapphireException("INVALID_PROPERTY", "The property 'keyid" + String.valueOf(i) + "' cannot be null.");
                }
                if (properties.getProperty(parentkeycolids[i - 1], "").length() != 0) continue;
                properties.setProperty(parentkeycolids[i - 1], keyvalue);
                addedNewprops.append(";").append(parentkeycolids[i - 1]);
            }
        }
        return parentkeycolids;
    }

    private String getTracelogid(PropertyList properties) throws SapphireException {
        String traceLogId = properties.getProperty("tracelogid", "");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        String sdcid = properties.getProperty("sdcid");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                traceLogId = audit.addTraceLogEntry(auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals("Y"));
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        return traceLogId;
    }
}

