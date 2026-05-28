/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.exception.MaxLoginExceededException;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.SDI;
import java.util.HashMap;
import java.util.Set;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class COCActions
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54722 $";
    private SQLGenerator __SqlGenerator;

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        int rc = 1;
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        if (actionid.equals("CheckCOCCustodians")) {
            rc = this.doCheckCOCCustodians(props);
        } else if (actionid.equals("AddSDICOC")) {
            rc = this.doAddSDICOC(props);
        }
        return rc;
    }

    private int doCheckCOCCustodians(HashMap props) {
        int rc = 1;
        boolean valid = true;
        String result = "SUCCESS";
        String[] fromid = StringUtil.split((String)props.get("fromid"), ";");
        String[] frompwd = StringUtil.split((String)props.get("frompwd"), ";");
        String toid = (String)props.get("toid");
        String topwd = (String)props.get("topwd");
        String witnessid = (String)props.get("witnessid");
        String witnesspwd = (String)props.get("witnesspwd");
        String faileduserid = "";
        if (fromid != null && fromid.length > 0) {
            this.logger.debug("Validating fromcustodians...");
            for (int i = 0; i < fromid.length; ++i) {
                String id = fromid[i];
                if (id == null || id.length() <= 0 || id.equals("Not Started")) continue;
                String pwd = frompwd[i];
                if (pwd == null) {
                    pwd = "";
                }
                try {
                    if (this.validateCustodian(id, pwd)) continue;
                    faileduserid = id;
                    valid = false;
                    result = "FAILURE";
                }
                catch (MaxLoginExceededException e) {
                    faileduserid = id;
                    valid = false;
                    result = "LOGINEXCEEDED";
                }
                break;
            }
        }
        if (valid && witnessid != null && witnessid.length() > 0) {
            this.logger.debug("Validating witness...");
            if (witnesspwd == null || witnesspwd.length() == 0) {
                witnesspwd = "";
            }
            try {
                if (!this.validateCustodian(witnessid, witnesspwd)) {
                    faileduserid = witnessid;
                    valid = false;
                    result = "FAILURE";
                }
            }
            catch (MaxLoginExceededException e) {
                faileduserid = witnessid;
                valid = false;
                result = "LOGINEXCEEDED";
            }
        }
        if (valid) {
            this.logger.debug("Validating tocustodian...");
            if (topwd == null || topwd.length() == 0) {
                topwd = "";
            }
            try {
                if (!this.validateCustodian(toid, topwd)) {
                    faileduserid = toid;
                    valid = false;
                    result = "FAILURE";
                }
            }
            catch (MaxLoginExceededException e) {
                faileduserid = witnessid;
                valid = false;
                result = "LOGINEXCEEDED";
            }
        }
        if (valid) {
            props.put("result", result);
        } else {
            props.put("result", result);
            props.put("faileduserid", faileduserid);
        }
        return rc;
    }

    private boolean validateCustodian(String custodianid, String custodianpwd) throws MaxLoginExceededException {
        boolean valid = false;
        String custodiantype = this.getUserType(custodianid);
        if (custodiantype == null) {
            return false;
        }
        if ("SYSUSER".equalsIgnoreCase(custodiantype)) {
            ConnectionProcessor cp = this.getConnectionProcessor();
            valid = cp.checkUser(custodianid, custodianpwd);
            if (cp.getLastErrorMessage().indexOf("Max login attempts exceeded") > 0) {
                throw new MaxLoginExceededException(custodianid);
            }
        } else if ("CUSTODIAN".equalsIgnoreCase(custodiantype)) {
            if ("Y".equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "custodian", "passwordflag", "custodianid = ?", new String[]{custodianid}))) {
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("custodianid", custodianid);
                    props.setProperty("custodianpwd", custodianpwd);
                    this.getActionProcessor().processAction("CheckCustodian", "1", props);
                    String result = (String)props.get("result");
                    valid = "Yes".equals(result);
                }
                catch (ActionException e) {
                    this.logger.error("Error running CheckCustodian", e);
                    valid = false;
                }
            } else {
                valid = true;
            }
        }
        return valid;
    }

    private String getUserType(String userid) {
        SafeSQL safeSQL = this.__SqlGenerator.getCustodianAndUserInfo(userid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            if (!ds.getValue(i, "ID").equalsIgnoreCase(userid)) continue;
            return ds.getValue(i, "TYPE");
        }
        return null;
    }

    private int doAddSDICOC(HashMap props) {
        int rc = 1;
        boolean invalid = false;
        boolean allownullflag = false;
        String sdcid = (String)props.get("sdcid");
        String keyid1 = (String)props.get("keyid1");
        String keyid2 = (String)props.get("keyid2");
        String keyid3 = (String)props.get("keyid3");
        String fromid = (String)props.get("fromcustodianid");
        String toid = (String)props.get("tocustodianid");
        String witnessid = (String)props.get("witnessid");
        String sysuserid = (String)props.get("sysuserid");
        String allownull = (String)props.get("allownull");
        if (allownull != null && allownull.equals("Y")) {
            allownullflag = true;
        }
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        if (OpalUtil.isEmpty(toid)) {
            if (!allownullflag) {
                invalid = true;
            } else {
                toid = "(null)";
            }
        }
        if (OpalUtil.isEmpty(fromid)) {
            if (!allownullflag) {
                invalid = true;
            } else {
                fromid = "(null)";
            }
        }
        if (!sdi.isValid() || invalid) {
            return this.setError("OPAL-ERROR: Invalid action input.");
        }
        try {
            DataSet dsinsert = new DataSet();
            dsinsert.addColumnValues("sdcid", 0, sdi.getSdcid(), ";");
            dsinsert.addColumnValues("keyid1", 0, sdi.getKeyid1(), ";");
            dsinsert.addColumnValues("keyid2", 0, sdi.getKeyid2(), ";");
            dsinsert.addColumnValues("keyid3", 0, sdi.getKeyid3(), ";");
            dsinsert.addColumnValues("fromcustodianid", 0, fromid, ";");
            dsinsert.addColumnValues("tocustodianid", 0, toid, ";");
            dsinsert.addColumnValues("witnessid", 0, witnessid, ";");
            dsinsert.addColumnValues("createby", 0, sysuserid, ";");
            dsinsert.addColumnValues("createtool", 0, "ELIMS", ";");
            dsinsert.addColumn("createdt", 2);
            dsinsert.setDate(0, "createdt", DateTimeUtil.getNowCalendar());
            props.remove("sdcid");
            props.remove("keyid1");
            props.remove("keyid2");
            props.remove("keyid3");
            props.remove("fromcustodianid");
            props.remove("tocustodianid");
            props.remove("witnessid");
            props.remove("createby");
            Set keyset = props.keySet();
            HashMap<String, String> columnmap = OpalUtil.getColumnDataTypeMap("s_sdicoc", this.getQueryProcessor());
            this.logger.debug(columnmap.toString());
            for (String column : keyset) {
                if (!columnmap.containsKey(column)) continue;
                String columntype = columnmap.get(column);
                String columnvalue = (String)props.get(column);
                if (columntype.equals("C")) {
                    dsinsert.addColumnValues(column, 0, columnvalue, ";");
                    continue;
                }
                if (columntype.equals("D")) {
                    if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty(sdi.getSdcid(), column, "timezoneindependent"))) {
                        dsinsert.setTimeZoneInsensitive(column);
                    }
                    dsinsert.addColumnValues(column, 2, columnvalue, ";");
                    continue;
                }
                if (!columntype.equals("N")) continue;
                dsinsert.addColumnValues(column, 1, columnvalue, ";");
            }
            dsinsert.padColumns();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dsinsert.size(); ++i) {
                sb.append(this.getSequenceProcessor().getSequence("S_SDICOC", sdcid)).append(";");
            }
            if (sb.length() > 0) {
                sb = sb.deleteCharAt(sb.length() - 1);
            }
            dsinsert.addColumnValues("s_sdicocid", 0, sb.toString(), ";");
            DataSetUtil.insert(this.database, dsinsert, "s_sdicoc");
            dsinsert = null;
        }
        catch (Exception e) {
            return this.setError("OPAL-ERROR: Exception caught: " + e.getMessage(), e);
        }
        props.put("result", "SUCCESS");
        return rc;
    }
}

