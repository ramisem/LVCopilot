/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.StringHolder;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditSDIAddress
extends BaseAction
implements sapphire.action.EditSDIAddress {
    private static final String PROPERTY_SEPARATOR = "separator";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block38: {
            int rc = 1;
            boolean deleterset = false;
            String rsetid = properties.getProperty("rsetid");
            DAMProcessor dam = null;
            DataSet sdiaddresses = null;
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
            PreparedStatement pstmt = null;
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
                    sdiaddresses = new DataSet();
                    DataSet filteredUserDefCols = this.getUserColList(properties);
                    String updateSQL = null;
                    int userRowCnt = filteredUserDefCols != null ? filteredUserDefCols.getRowCount() : 0;
                    updateSQL = userRowCnt > 0 ? "UPDATE sdiaddress set contactfunction = ?, functiondt = ?, modby = ?, modtool = ?, moddt = ?, tracelogid = ?, " + filteredUserDefCols.getColumnValues("columnid", " = ?, ") + " = ? WHERE sdcid = '" + sdcid + "' AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND addressid = ? AND addresstype = ? and contactfunction = ?" : "UPDATE sdiaddress set contactfunction = ?, functiondt = ?, modby = ?, modtool = ?, moddt = ?, tracelogid = ? WHERE sdcid = '" + sdcid + "' AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND addressid = ? AND addresstype = ? and contactfunction = ?";
                    try {
                        pstmt = this.database.prepareStatement("psmt", updateSQL);
                    }
                    catch (Exception ex) {
                        throw new SapphireException("PREPARE_STMT_FAILED", ErrorUtil.extractMessage("Failed to use the SQL. Reason: " + updateSQL, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                    }
                    String selectSDIAddress = "SELECT sdiaddress.sdcid, sdiaddress.keyid1, sdiaddress.keyid2, sdiaddress.keyid3,  sdiaddress.addressid, sdiaddress.addresstype, sdiaddress.contactfunction, sdiaddress.usersequence FROM sdiaddress, rsetitems WHERE rsetitems.sdcid = ? AND rsetitems.rsetid = ? AND rsetitems.sdcid = sdiaddress.sdcid AND rsetitems.keyid1 = sdiaddress.keyid1 AND rsetitems.keyid2 = sdiaddress.keyid2 AND rsetitems.keyid3 = sdiaddress.keyid3 ORDER BY sdiaddress.sdcid, sdiaddress.keyid1, sdiaddress.keyid2, sdiaddress.keyid3, sdiaddress.usersequence desc";
                    try {
                        this.database.createPreparedResultSet(selectSDIAddress, new Object[]{sdcid, rsetid});
                        sdiaddresses.setResultSet(this.database.getResultSet());
                    }
                    catch (SapphireException ex) {
                        throw new SapphireException("CREATE_RESULTSET_FAILED", ErrorUtil.extractMessage("Failed to get result set for: " + selectSDIAddress, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                    }
                    DateTimeUtil dtu = new DateTimeUtil(this.connectionInfo);
                    String separator = properties.getProperty(PROPERTY_SEPARATOR, ";");
                    boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
                    HashMap<String, String> findmap = new HashMap<String, String>();
                    String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
                    String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
                    String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
                    String[] addressidprop = StringUtil.split(properties.getProperty("addressid"), separator);
                    String[] addresstypeprop = StringUtil.split(properties.getProperty("addresstype"), separator);
                    String[] functionprop = StringUtil.split(properties.getProperty("contactfunction"), separator);
                    String[] functiondtprop = StringUtil.split(properties.getProperty("functiondt"), separator);
                    properties.setProperty("tracelogid", this.getTracelogid(properties));
                    if (addressidprop.length == addresstypeprop.length) {
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
                                if (findrow == -1) continue;
                                try {
                                    pstmt.setString(userRowCnt + 7, keyid1);
                                    pstmt.setString(userRowCnt + 8, keyid2);
                                    pstmt.setString(userRowCnt + 9, keyid3);
                                    pstmt.setString(userRowCnt + 10, addressidprop[address]);
                                    pstmt.setString(userRowCnt + 11, addresstypeprop[address]);
                                    if (functionprop.length == 1) {
                                        pstmt.setString(1, functionprop[0]);
                                        pstmt.setString(userRowCnt + 12, functionprop[0]);
                                    } else if (functionprop.length > address) {
                                        pstmt.setString(1, functionprop[address]);
                                        pstmt.setString(userRowCnt + 12, functionprop[address]);
                                    }
                                    if (functiondtprop.length == 1) {
                                        pstmt.setTimestamp(2, dtu.getTimestamp(functiondtprop[0]));
                                    } else {
                                        pstmt.setTimestamp(2, dtu.getTimestamp(functiondtprop[address]));
                                    }
                                    pstmt.setString(3, this.connectionInfo.getSysuserId());
                                    pstmt.setString(4, "EditSDIAddress");
                                    pstmt.setTimestamp(5, dtu.getNowTimestamp());
                                    pstmt.setString(6, properties.getProperty("tracelogid"));
                                    if (userRowCnt > 0) {
                                        String colId = null;
                                        String[] colValueArr = null;
                                        String colVal = null;
                                        block19: for (int i = 7; i <= userRowCnt + 6; ++i) {
                                            colId = filteredUserDefCols.getValue(i - 7, "columnid");
                                            colValueArr = StringUtil.split(properties.getProperty(colId), separator);
                                            colVal = propsmatch ? colValueArr[address] : (colValueArr.length > address ? colValueArr[address] : colValueArr[0]);
                                            switch (filteredUserDefCols.getValue(i - 7, "datatype").charAt(0)) {
                                                case 'C': {
                                                    pstmt.setString(i, colVal);
                                                    continue block19;
                                                }
                                                case 'D': {
                                                    pstmt.setTimestamp(i, dtu.getTimestamp(colVal));
                                                    continue block19;
                                                }
                                                case 'N': {
                                                    pstmt.setInt(i, Integer.parseInt(colVal));
                                                    continue block19;
                                                }
                                                case 'R': {
                                                    pstmt.setDouble(i, Double.parseDouble(colVal));
                                                }
                                            }
                                        }
                                    }
                                    this.logger.info("Updating the sdiaddress record of {" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + addressidprop[address] + ";" + addresstypeprop[address] + "}");
                                    int rows = pstmt.executeUpdate();
                                    if (rows == 1) continue;
                                    throw new SapphireException("EXECUTE_STMT_FAILED", "Failed to run update statement for: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
                                }
                                catch (Exception ex) {
                                    throw new SapphireException("PREPARE_STMT_FAILED", "Failed to set parameters the update statement for: " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + " because: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                                }
                            }
                        }
                        break block38;
                    }
                    throw new SapphireException("INVALID_PROPERTIES", "There must be an addressid and an address type for each addresses");
                }
                catch (Exception ex) {
                    throw new SapphireException("Error: " + ErrorUtil.extractMessageFromException(ex, ErrorUtil.isUserAdmin(this.getConnectionId())), ex);
                }
                finally {
                    this.database.closeStatement("psmt");
                    if (deleterset) {
                        dam.clearRSet(rsetid);
                    }
                    pstmt = null;
                    sdiaddresses.reset();
                }
            }
            throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET.");
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

