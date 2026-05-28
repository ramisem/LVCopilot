/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetSDIVersionStatus
extends BaseSDIAction
implements sapphire.action.SetSDIVersionStatus {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SafeSQL safeSQL;
        String sdcid = properties.getProperty("sdcid");
        String overwriteApprovedDtFlag = properties.getProperty("overwriteapproveddtflag", "Y");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "No SDC specified");
        }
        this.logger.debug("Getting SDC information");
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String[] keyid1list = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2list = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3list = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] versionstatus = StringUtil.split(properties.getProperty("versionstatus"), ";");
        PropertyList editproperties = new PropertyList();
        ArrayList<String> currentItemsKeyid = new ArrayList<String>();
        boolean tripleKey = sdcProcessor.getProperty(sdcid, "keycolumns").equals("3");
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        String effectivitydateflag = sdcPropertyList.getProperty("versionuseeffectivedtflag");
        String tableid = sdcPropertyList.getProperty("tableid");
        String rsetid = null;
        DataSet dsforeffectivedate = null;
        if ("Y".equalsIgnoreCase(effectivitydateflag)) {
            rsetid = this.getDAMProcessor().createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            StringBuilder sqltofetcheffectivedate = new StringBuilder();
            StringBuilder sbtriplekeywhereclause = new StringBuilder("");
            String currentdateclause = this.database.isSqlServer() ? "getdate() sysdate" : "sysdate";
            safeSQL = new SafeSQL();
            sqltofetcheffectivedate.append("SELECT ").append(sdcPropertyList.getProperty("keycolid1")).append(", ").append(sdcPropertyList.getProperty("keycolid2"));
            if (tripleKey) {
                sqltofetcheffectivedate.append(", ").append(sdcPropertyList.getProperty("keycolid3"));
                sbtriplekeywhereclause.append(" AND r.keyid3 = ").append(sdcPropertyList.getProperty("keycolid3"));
            }
            sqltofetcheffectivedate.append(", ").append("versionstatus");
            sqltofetcheffectivedate.append(", ").append("versioneffectivedt").append(", ").append("versionapproveddt").append(", ").append(currentdateclause).append(" FROM ").append(tableid).append(", rsetitems r ").append(" WHERE ").append(" r.sdcid = ").append(safeSQL.addVar(sdcid)).append(" ").append(" AND r.keyid1 = ").append(sdcPropertyList.getProperty("keycolid1")).append(" AND r.keyid2 = ").append(sdcPropertyList.getProperty("keycolid2")).append((CharSequence)sbtriplekeywhereclause).append(" AND r.rsetid = ").append(safeSQL.addVar(rsetid));
            dsforeffectivedate = this.getQueryProcessor().getPreparedSqlDataSet(sqltofetcheffectivedate.toString(), safeSQL.getValues());
        }
        if (rsetid != null) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (keyid1list.length > 0 && keyid1list.length != versionstatus.length && versionstatus.length == 1) {
            DataSet inputDS = new DataSet();
            inputDS.addColumnValues("keyid1", 0, properties.getProperty("keyid1"), ";");
            inputDS.addColumnValues("versionstatus", 0, properties.getProperty("versionstatus"), ";");
            inputDS.padColumns();
            versionstatus = StringUtil.split(inputDS.getColumnValues("versionstatus", ";"), ";");
        }
        if (!(keyid1list.length != keyid2list.length || keyid1list.length != versionstatus.length || tripleKey && keyid3list.length != keyid1list.length)) {
            int i;
            for (i = 0; i < keyid1list.length; ++i) {
                editproperties.setProperty("keyid1", properties.getProperty("keyid1"));
                editproperties.setProperty("keyid2", properties.getProperty("keyid2"));
                editproperties.setProperty("keyid3", properties.getProperty("keyid3"));
                if (versionstatus[i].equals("C") && "Y".equals(overwriteApprovedDtFlag)) {
                    editproperties.setProperty("versionapproveddt", "n");
                }
                if ("Y".equalsIgnoreCase(effectivitydateflag)) {
                    if (dsforeffectivedate != null && dsforeffectivedate.getRowCount() > 0) {
                        int rowid;
                        HashMap<String, String> hmfilter = new HashMap<String, String>();
                        hmfilter.put(sdcPropertyList.getProperty("keycolid1"), keyid1list[i]);
                        hmfilter.put(sdcPropertyList.getProperty("keycolid2"), keyid2list[i]);
                        if (tripleKey) {
                            hmfilter.put(sdcPropertyList.getProperty("keycolid3"), keyid3list[i]);
                        }
                        if ((rowid = dsforeffectivedate.findRow(hmfilter)) > -1) {
                            String effectivedate = dsforeffectivedate.getValue(rowid, "versioneffectivedt", "");
                            Calendar currentdatefromdataset = dsforeffectivedate.getCalendar(rowid, "sysdate", Calendar.getInstance());
                            Calendar effectivedatecalendar = dsforeffectivedate.getCalendar(rowid, "versioneffectivedt", currentdatefromdataset);
                            int calcomparison = currentdatefromdataset.compareTo(effectivedatecalendar);
                            if (effectivedate != null && effectivedate.length() > 0 && calcomparison < 0) {
                                versionstatus[i] = "P";
                                this.logger.info("SetSDIVersionStatus", this.getTranslationProcessor().translate("The selected SDI will move to Current on : " + effectivedate));
                            } else {
                                editproperties.setProperty("versionstatus", properties.getProperty("versionstatus"));
                                if (!("E".equals(properties.getProperty("versionstatus")) || effectivedate != null && effectivedate.trim().length() != 0)) {
                                    editproperties.setProperty("versioneffectivedt", "n");
                                }
                            }
                        }
                    }
                } else {
                    editproperties.setProperty("versionstatus", properties.getProperty("versionstatus"));
                }
                if (versionstatus[i].equals("A") || versionstatus[i].equals("C") || versionstatus[i].equals("H") || versionstatus[i].equals("P") || versionstatus[i].equals("E")) {
                    if (!versionstatus[i].equals("C")) continue;
                    if (tripleKey) {
                        if (currentItemsKeyid.contains(keyid1list[i] + ";" + keyid3list[i])) {
                            throw new SapphireException("INVALID_PARAMETER", keyid1list[i] + ";" + keyid3list[i] + " set to Current for multiple versions - only 1 version may be Current");
                        }
                        currentItemsKeyid.add(keyid1list[i] + ";" + keyid3list[i]);
                        continue;
                    }
                    if (currentItemsKeyid.contains(keyid1list[i])) {
                        throw new SapphireException("INVALID_PARAMETER", keyid1list[i] + " set to Current for multiple versions - only 1 version may be Current");
                    }
                    currentItemsKeyid.add(keyid1list[i]);
                    continue;
                }
                throw new SapphireException("INVALID_PROPERTY", "Illegal version status value '" + versionstatus[i] + "'. Must be one of 'A', 'C', 'H', 'P' or 'E'.");
            }
            editproperties.setProperty("sdcid", sdcid);
            editproperties.setProperty("auditreason", properties.getProperty("auditreason"));
            editproperties.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
            editproperties.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", "N"));
            editproperties.setProperty("auditdt", properties.getProperty("auditdt"));
            editproperties.setProperty("worksheet_action", properties.getProperty("worksheet_action"));
            if (properties.containsKey("importsnapshot")) {
                editproperties.setProperty("importsnapshot", properties.getProperty("importsnapshot"));
            }
            try {
                try {
                    for (i = 0; i < currentItemsKeyid.size(); ++i) {
                        String keyid1 = tripleKey ? StringUtil.split((String)currentItemsKeyid.get(i), ";")[0] : (String)currentItemsKeyid.get(i);
                        String keyid3 = tripleKey ? StringUtil.split((String)currentItemsKeyid.get(i), ";")[1] : "";
                        safeSQL = new SafeSQL();
                        String sql = "UPDATE " + sdcProcessor.getProperty(sdcid, "tableid") + " SET versionstatus='A' WHERE versionstatus='C' AND " + sdcProcessor.getProperty(sdcid, "keycolid1") + "=" + safeSQL.addVar(keyid1);
                        if (tripleKey) {
                            sql = sql + " AND " + sdcProcessor.getProperty(sdcid, "keycolid3") + "=" + safeSQL.addVar(keyid3);
                        }
                        this.database.executePreparedUpdate(sql, safeSQL.getValues());
                    }
                }
                catch (SapphireException e) {
                    throw new SapphireException("DB_ACTION_FAILED", "Failed to reset versionstatus from Current to Active", e);
                }
                editproperties.setProperty("applylock", "Y");
                this.editSDI("SetSDIVersionStatus", editproperties);
            }
            catch (SapphireException se) {
                throw new SapphireException("DB_ACTION_FAILED", ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId())), se);
            }
            finally {
                if (rsetid != null) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
        }
        throw new SapphireException("INVALID_PARAMETER", "Property lists do not match");
    }
}

