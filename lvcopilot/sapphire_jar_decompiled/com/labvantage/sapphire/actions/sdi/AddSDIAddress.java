/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.StringHolder;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIAddress
extends BaseAction
implements sapphire.action.AddSDIAddress {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block36: {
            int rc = 1;
            boolean deleterset = false;
            String rsetid = properties.getProperty("rsetid");
            DAMProcessor dam = null;
            DataSet sdiaddresses = null;
            DataSet newadd = null;
            String sdcid = properties.getProperty("sdcid", "");
            String keyid1p = properties.getProperty("keyid1", "");
            String addressid = properties.getProperty("addressid", "");
            String addressType = properties.getProperty("addresstype", "");
            String contactFunction = properties.getProperty("contactfunction", "");
            if (sdcid.length() == 0) {
                throw new SapphireException("INVALID_PROPERTIES", "sdcid is mandatory");
            }
            if (keyid1p.length() == 0) {
                throw new SapphireException("INVALID_PROPERTIES", "keyid1 is mandatory");
            }
            if (addressid.length() == 0) {
                throw new SapphireException("INVALID_PROPERTIES", "addressid is mandatory");
            }
            if (addressType.length() == 0) {
                throw new SapphireException("INVALID_PROPERTIES", "addresstype is mandatory");
            }
            if (contactFunction.length() == 0) {
                throw new SapphireException("INVALID_PROPERTIES", "contactfunction is mandatory");
            }
            if (rsetid.length() == 0) {
                boolean applylock = properties.getProperty("applylock").equals("Y");
                StringHolder rsetidHolder = new StringHolder();
                dam = this.getDAMProcessor();
                rc = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), rsetidHolder) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), rsetidHolder);
                if (rc == 1) {
                    rsetid = rsetidHolder.value;
                }
                deleterset = true;
            }
            if (rc == 1) {
                try {
                    newadd = new DataSet();
                    sdiaddresses = new DataSet();
                    String selectSDIAddress = "SELECT sdiaddress.sdcid, sdiaddress.keyid1, sdiaddress.keyid2, sdiaddress.keyid3,  sdiaddress.addressid, sdiaddress.addresstype, sdiaddress.contactfunction, sdiaddress.usersequence FROM sdiaddress, rsetitems WHERE rsetitems.sdcid = ? AND rsetitems.rsetid = ? AND rsetitems.sdcid = sdiaddress.sdcid AND rsetitems.keyid1 = sdiaddress.keyid1 AND rsetitems.keyid2 = sdiaddress.keyid2 AND rsetitems.keyid3 = sdiaddress.keyid3 ORDER BY sdiaddress.sdcid, sdiaddress.keyid1, sdiaddress.keyid2, sdiaddress.keyid3, sdiaddress.usersequence desc";
                    try {
                        this.database.createPreparedResultSet(selectSDIAddress, new Object[]{sdcid, rsetid});
                        sdiaddresses.setResultSet(this.database.getResultSet());
                    }
                    catch (SapphireException ex) {
                        throw new SapphireException("CREATE_RESULTSET_FAILED", "Failed to get result set for: " + selectSDIAddress, ex);
                    }
                    DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
                    String separator = properties.getProperty("separator", ";");
                    boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
                    HashMap<String, String> findmap = new HashMap<String, String>();
                    String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
                    String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
                    String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
                    String[] addressidprop = StringUtil.split(properties.getProperty("addressid"), separator);
                    String[] addresstypeprop = null;
                    String addresstypestr = properties.getProperty("addresstype");
                    if (addresstypestr != null && addresstypestr.length() > 0) {
                        addresstypeprop = StringUtil.split(addresstypestr, separator);
                    }
                    String[] functionprop = null;
                    String functionstr = properties.getProperty("contactfunction");
                    if (functionstr != null && functionstr.length() > 0) {
                        functionprop = StringUtil.split(functionstr, separator);
                    }
                    String[] functiondtprop = StringUtil.split(properties.getProperty("functiondt"), separator);
                    Calendar now = DateTimeUtil.getNowCalendar();
                    properties.setProperty("tracelogid", this.getTracelogid(properties));
                    if (addressidprop.length == addresstypeprop.length) {
                        DataSet filteredUserDefCols = this.getUserColList(properties);
                        for (int sdi = 0; sdi < (propsmatch ? 1 : keyid1prop.length); ++sdi) {
                            for (int address = 0; address < addressidprop.length; ++address) {
                                String keyid1 = "";
                                String keyid2 = "";
                                String keyid3 = "";
                                if (propsmatch) {
                                    keyid1 = keyid1prop[address];
                                    keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[address];
                                    keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[address];
                                } else {
                                    keyid1 = keyid1prop[sdi];
                                    keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[sdi].length() == 0 ? "(null)" : keyid2prop[sdi];
                                    keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[sdi].length() == 0 ? "(null)" : keyid3prop[sdi];
                                }
                                findmap.put("keyid1", keyid1);
                                findmap.put("keyid2", keyid2);
                                findmap.put("keyid3", keyid3);
                                findmap.put("addressid", addressidprop[address]);
                                findmap.put("addresstype", addresstypeprop[address]);
                                findmap.put("contactfunction", functionprop[address]);
                                int findrow = sdiaddresses.findRow(findmap);
                                if (findrow == -1) {
                                    int rowCnt;
                                    int newrow = newadd.addRow();
                                    newadd.setString(newrow, "sdcid", sdcid);
                                    newadd.setString(newrow, "keyid1", keyid1);
                                    newadd.setString(newrow, "keyid2", keyid2);
                                    newadd.setString(newrow, "keyid3", keyid3);
                                    newadd.setString(newrow, "addressid", addressidprop[address]);
                                    newadd.setString(newrow, "addresstype", addresstypeprop[address]);
                                    newadd.setString(newrow, "contactfunction", functionprop[address]);
                                    if (functiondtprop.length == 1) {
                                        newadd.setDate(newrow, "functiondt", dtu.getCalendar(functiondtprop[0]));
                                    } else {
                                        newadd.setDate(newrow, "functiondt", dtu.getCalendar(functiondtprop[address]));
                                    }
                                    newadd.setDate(newrow, "createdt", now);
                                    newadd.setString(newrow, "createtool", "AddSDIAddress");
                                    newadd.setString(newrow, "createby", this.connectionInfo.getSysuserId());
                                    newadd.setDate(newrow, "moddt", now);
                                    newadd.setString(newrow, "modtool", "AddSDIAddress");
                                    newadd.setString(newrow, "modby", this.connectionInfo.getSysuserId());
                                    newadd.setString(newrow, "tracelogid", properties.getProperty("tracelogid"));
                                    int n = rowCnt = filteredUserDefCols != null ? filteredUserDefCols.getRowCount() : 0;
                                    if (rowCnt <= 0) continue;
                                    String colId = null;
                                    String[] colValArr = null;
                                    String colVal = null;
                                    block17: for (int i = 0; i < rowCnt; ++i) {
                                        colId = filteredUserDefCols.getValue(i, "columnid");
                                        colValArr = StringUtil.split(properties.getProperty(colId), separator);
                                        colVal = propsmatch ? colValArr[address] : (colValArr.length > address ? colValArr[address] : colValArr[0]);
                                        switch (filteredUserDefCols.getValue(i, "datatype").charAt(0)) {
                                            case 'C': {
                                                newadd.setString(newrow, colId, colVal);
                                                continue block17;
                                            }
                                            case 'D': {
                                                newadd.setDate(newrow, colId, dtu.getCalendar(colVal));
                                                continue block17;
                                            }
                                            case 'R': {
                                                newadd.setNumber(newrow, colId, colVal);
                                                continue block17;
                                            }
                                            case 'N': {
                                                newadd.setNumber(newrow, colId, colVal);
                                            }
                                        }
                                    }
                                    continue;
                                }
                                HashMap<String, String> valueMap = new HashMap<String, String>();
                                valueMap.put("addressid", addressidprop[address]);
                                valueMap.put("addresstype", addressidprop[address]);
                                throw new SapphireException("INVALID_PROPERTIES", this.getTranslationProcessor().translate("Combination of Address Id '[addressid]' and Function '[addresstype]' already present.", valueMap));
                            }
                        }
                    } else {
                        throw new SapphireException("INVALID_PROPERTIES", this.getTranslationProcessor().translate("There must be an addressid and an address type for each addresses"));
                    }
                    this.logger.info("Processing the sdiaddress inserts: " + newadd);
                    try {
                        DataSetUtil.insert(this.database, newadd, "sdiaddress");
                        break block36;
                    }
                    catch (SapphireException ex) {
                        throw new SapphireException("DB_INSERT_FAILED", "Failed to insert data in sdi addresses", ex);
                    }
                }
                catch (Exception ex) {
                    throw new SapphireException("Error: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                }
                finally {
                    if (deleterset) {
                        dam.clearRSet(rsetid);
                    }
                    newadd.reset();
                    sdiaddresses.reset();
                }
            }
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET");
        }
    }

    private DataSet getUserColList(PropertyList props) throws SapphireException {
        DataSet userCols = null;
        String sql = "select columnid, datatype from syscolumn where tableid = 'sdiaddress' and columnid not in ('sdcid','keyid1','keyid2','keyid3', 'addressid','addresstype','contactfunction','usersequence','auditsequence','createdt','createby','createtool','functiondt','moddt','modby','modtool','tracelogid')";
        try {
            int rowCnt;
            userCols = this.getQueryProcessor().getSqlDataSet(sql);
            for (int i = rowCnt = userCols != null ? userCols.getRowCount() : 0; i > 0; --i) {
                if (props.containsKey(userCols.getValue(i - 1, "columnid"))) continue;
                userCols.deleteRow(i - 1);
            }
        }
        catch (Exception ex) {
            throw new SapphireException("Error: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
        }
        return userCols;
    }

    private String getTracelogid(PropertyList properties) throws SapphireException {
        String traceLogId = properties.getProperty("tracelogid", "");
        String auditReason = properties.getProperty("auditreason");
        String auditActivity = properties.getProperty("auditactivity");
        String auditSignedFlag = properties.getProperty("auditsignedflag");
        String sdcid = properties.getProperty("sdcid", "");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        if (!sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N") && traceLogId.length() == 0 && auditReason.length() > 0) {
            this.logger.info("Generate the tracelog records");
            String promptflag = sdcProcessor.getProperty(sdcid, "auditpromptflag");
            String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
            AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            try {
                traceLogId = audit.addSDITraceLogEntry(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), auditReason, auditActivity, auditSignedFlag, properties.getProperty("auditdt"), "Data editing", standard.equals("Y"));
            }
            catch (ServiceException e) {
                throw new SapphireException("Failed to add audit records", e);
            }
        }
        return traceLogId;
    }
}

