/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.sapphire.SDI;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DBAccess;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyListCollection;

public class SDIProps {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";
    private DBAccess __Database;
    private SDI __SDI;
    private boolean __DsInfo;
    private boolean __SpInfo;
    private boolean __WiInfo;
    private StringBuffer __Samples = new StringBuffer();
    private String __ParamListID;
    private String __ParamListVersionID;
    private String __VariantID;
    private String __SpecID;
    private String __SpecVersionID;
    private String __SpecAutoApplyFlag;
    private String __SpecOOSGeneratingFlag;
    private String __WorkitemID;
    private String __WorkitemVersionID;
    private String __WorkitemTypeFlag;
    private String __WorkitemReflexRule;
    private SQLGenerator __SqlGenerator;
    private SDCProcessor __SdcProcessor;

    public SDIProps(DBAccess database, SDI sdi, SQLGenerator sqlgenerator, boolean dsinfo, boolean spinfo, boolean wiinfo) {
        this.__Database = database;
        this.__SDI = sdi;
        this.__DsInfo = dsinfo;
        this.__SpInfo = spinfo;
        this.__WiInfo = wiinfo;
        this.__SqlGenerator = sqlgenerator;
    }

    public SDIProps(DBAccess database, SDI sdi, SQLGenerator sqlgenerator, SDCProcessor sdcProcessor, boolean dsinfo, boolean spinfo, boolean wiinfo) {
        this.__Database = database;
        this.__SDI = sdi;
        this.__DsInfo = dsinfo;
        this.__SpInfo = spinfo;
        this.__WiInfo = wiinfo;
        this.__SqlGenerator = sqlgenerator;
        this.__SdcProcessor = sdcProcessor;
    }

    public void addKey(String key) {
        if (this.__Samples.length() == 0) {
            this.__Samples.append(key);
        } else {
            this.__Samples.append(";").append(key);
        }
    }

    public String getKey() {
        return this.__Samples.toString();
    }

    public void init() throws SapphireException {
        SafeSQL safeSQL;
        StringBuffer ds1 = new StringBuffer();
        StringBuffer ds2 = new StringBuffer();
        StringBuffer ds3 = new StringBuffer();
        StringBuffer ds4 = new StringBuffer();
        if (this.__DsInfo) {
            safeSQL = this.__SqlGenerator.getColumnValue(this.__SDI, "PARAMLISTID, PARAMLISTVERSIONID, VARIANTID", "SDIDATA", "", "USERSEQUENCE");
            this.__Database.createPreparedResultSet("_rs_SDIProps_001", safeSQL.getPreparedSQL(), safeSQL.getValues());
            ds1.setLength(0);
            ds2.setLength(0);
            ds3.setLength(0);
            while (this.__Database.getNext("_rs_SDIProps_001")) {
                ds1.append(this.__Database.getString("_rs_SDIProps_001", "PARAMLISTID")).append(";");
                ds2.append(this.__Database.getString("_rs_SDIProps_001", "PARAMLISTVERSIONID")).append(";");
                ds3.append(this.__Database.getString("_rs_SDIProps_001", "VARIANTID")).append(";");
            }
            this.__Database.closeResultSet("_rs_SDIProps_001");
            if (ds1.length() > 0) {
                ds1.deleteCharAt(ds1.length() - 1);
                ds2.deleteCharAt(ds2.length() - 1);
                ds3.deleteCharAt(ds3.length() - 1);
                this.__ParamListID = ds1.toString();
                this.__ParamListVersionID = ds2.toString();
                this.__VariantID = ds3.toString();
            }
        }
        if (this.__SpInfo) {
            String sdcId;
            PropertyListCollection columns;
            safeSQL = this.__SqlGenerator.getColumnValue(this.__SDI, "SPECID, SPECVERSIONID, APPLIEDFLAG, AUTOAPPLYFLAG, OOSGENERATINGFLAG", "SDISPEC", "", "USERSEQUENCE");
            this.__Database.createPreparedResultSet("_rs_SDIProps_003", safeSQL.getPreparedSQL(), safeSQL.getValues());
            ds1.setLength(0);
            ds2.setLength(0);
            ds3.setLength(0);
            ds4.setLength(0);
            while (this.__Database.getNext("_rs_SDIProps_003")) {
                ds1.append(this.__Database.getString("_rs_SDIProps_003", "SPECID")).append(";");
                ds2.append(this.__Database.getString("_rs_SDIProps_003", "SPECVERSIONID")).append(";");
                ds3.append(this.__Database.getString("_rs_SDIProps_003", "AUTOAPPLYFLAG") == null ? "Y" : this.__Database.getString("_rs_SDIProps_003", "AUTOAPPLYFLAG")).append(";");
                ds4.append(this.__Database.getString("_rs_SDIProps_003", "OOSGENERATINGFLAG") == null ? "Y" : this.__Database.getString("_rs_SDIProps_003", "OOSGENERATINGFLAG")).append(";");
            }
            this.__Database.closeResultSet("_rs_SDIProps_003");
            if (ds1.length() > 0) {
                ds1.deleteCharAt(ds1.length() - 1);
                ds2.deleteCharAt(ds2.length() - 1);
                ds3.deleteCharAt(ds3.length() - 1);
                ds4.deleteCharAt(ds4.length() - 1);
                this.__SpecID = ds1.toString();
                this.__SpecVersionID = ds2.toString();
                this.__SpecAutoApplyFlag = ds3.toString();
                this.__SpecOOSGeneratingFlag = ds4.toString();
            }
            if ((columns = this.__SdcProcessor.getColumns(sdcId = this.__SDI.getSdcid())).find("columnid", "embeddedspecid", true) != null && columns.find("columnid", "embeddedspecversionid", true) != null) {
                String tableId = this.__SdcProcessor.getProperty(sdcId, "tableid");
                int keyCount = Integer.parseInt(this.__SdcProcessor.getProperty(sdcId, "keycolumns"));
                String keycol1 = this.__SdcProcessor.getProperty(sdcId, "keycolid1");
                safeSQL.reset();
                String sql = " SELECT embeddedspecid, embeddedspecversionid FROM " + tableId + " WHERE " + keycol1 + " = " + safeSQL.addVar(this.__SDI.getKeyid1());
                if (keyCount > 1) {
                    String keycol2 = this.__SdcProcessor.getProperty(sdcId, "keycolid2");
                    sql = sql + " AND " + keycol2 + " =" + safeSQL.addVar(this.__SDI.getKeyid2());
                }
                if (keyCount > 2) {
                    String keycol3 = this.__SdcProcessor.getProperty(sdcId, "keycolid3");
                    sql = sql + " AND " + keycol3 + " =" + safeSQL.addVar(this.__SDI.getKeyid3());
                }
                this.__Database.createPreparedResultSet("_rs_SDIPropsEmbeddedSpec", sql, safeSQL.getValues());
                while (this.__Database.getNext("_rs_SDIPropsEmbeddedSpec")) {
                    if (this.__SpecID != null && this.__SpecID.length() > 0) {
                        if (this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecid") == null || this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecversionid") == null) continue;
                        this.__SpecID = this.__SpecID.concat(";").concat(this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecid"));
                        this.__SpecVersionID = this.__SpecVersionID.concat(";").concat(this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecversionid"));
                        continue;
                    }
                    if (this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecid") == null || this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecversionid") == null) continue;
                    this.__SpecID = this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecid");
                    this.__SpecVersionID = this.__Database.getString("_rs_SDIPropsEmbeddedSpec", "embeddedspecversionid");
                }
                this.__Database.closeResultSet("_rs_SDIPropsEmbeddedSpec");
            }
        }
        if (this.__WiInfo) {
            safeSQL = this.__SqlGenerator.getColumnValue(this.__SDI, "WORKITEMID, WORKITEMINSTANCE, WORKITEMVERSIONID, WORKITEMTYPEFLAG, REFLEXRULE ", "SDIWORKITEM", "( workitemtypeflag = 'P' OR groupid IS NULL )", "USERSEQUENCE");
            this.__Database.createPreparedResultSet("_rs_SDIProps_005", safeSQL.getPreparedSQL(), safeSQL.getValues());
            ds1.setLength(0);
            ds2.setLength(0);
            ds3.setLength(0);
            ds4.setLength(0);
            while (this.__Database.getNext("_rs_SDIProps_005")) {
                ds1.append(this.__Database.getString("_rs_SDIProps_005", "WORKITEMID")).append(";");
                ds2.append(this.__Database.getString("_rs_SDIProps_005", "WORKITEMVERSIONID") == null ? "C" : this.__Database.getString("_rs_SDIProps_005", "WORKITEMVERSIONID")).append(";");
                ds3.append(this.__Database.getString("_rs_SDIProps_005", "WORKITEMTYPEFLAG")).append(";");
                String reflexRule = this.__Database.getString("_rs_SDIProps_005", "REFLEXRULE");
                if (reflexRule == null || reflexRule.equals("null")) {
                    reflexRule = "";
                }
                ds4.append(reflexRule).append(";");
            }
            this.__Database.closeResultSet("_rs_SDIProps_005");
            if (ds1.length() > 0) {
                ds1.deleteCharAt(ds1.length() - 1);
                ds2.deleteCharAt(ds2.length() - 1);
                ds3.deleteCharAt(ds3.length() - 1);
                ds4.deleteCharAt(ds4.length() - 1);
                this.__WorkitemID = ds1.toString();
                this.__WorkitemVersionID = ds2.toString();
                this.__WorkitemTypeFlag = ds3.toString();
                this.__WorkitemReflexRule = ds4.toString();
            }
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SDIProps)) {
            return false;
        }
        SDIProps sdiProps = (SDIProps)o;
        if (!this.__SDI.getSdcid().equals(sdiProps.__SDI.getSdcid())) {
            return false;
        }
        if (!this.__SDI.getKeyid1().equals(sdiProps.__SDI.getKeyid1())) {
            return false;
        }
        if (!this.__SDI.getKeyid2().equals(sdiProps.__SDI.getKeyid2())) {
            return false;
        }
        return this.__SDI.getKeyid3().equals(sdiProps.__SDI.getKeyid3());
    }

    public int hashCode() {
        int result = this.__SDI.hashCode();
        result = 29 * result + this.__SDI.hashCode();
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SDC: " + this.__SDI.getSdcid());
        sb.append("\nSDI: " + this.__SDI.getKeyid1());
        sb.append("\nKey ID1: " + this.getKey());
        sb.append("\nCopy DataSets: " + this.__DsInfo);
        sb.append("\nCopy Specs: " + this.__SpInfo);
        sb.append("\nCopy Workitems: " + this.__WiInfo);
        sb.append("\nParamList ID: " + this.__ParamListID);
        sb.append("\nParamList Version ID: " + this.__ParamListVersionID);
        sb.append("\nVariant ID: " + this.__VariantID);
        sb.append("\nSpec ID: " + this.__SpecID);
        sb.append("\nSpec Version ID: " + this.__SpecVersionID);
        sb.append("\nWorkItem ID: " + this.__WorkitemID);
        sb.append("\nWorkItem Version ID: " + this.__WorkitemVersionID);
        return sb.toString();
    }

    public String getParamListID() {
        return this.__ParamListID;
    }

    public String getParamListVersionID() {
        return this.__ParamListVersionID;
    }

    public String getVariantID() {
        return this.__VariantID;
    }

    public String getSpecID() {
        return this.__SpecID;
    }

    public String getSpecVersionID() {
        return this.__SpecVersionID;
    }

    public String getSpecAppliedFlag() {
        return this.__SpecAutoApplyFlag;
    }

    public String getSpecOOSGeneratingFlag() {
        return this.__SpecOOSGeneratingFlag;
    }

    public String getWorkitemID() {
        return this.__WorkitemID;
    }

    public String getWorkitemVersionID() {
        return this.__WorkitemVersionID;
    }

    public String getWorkitemTypeFlag() {
        return this.__WorkitemTypeFlag;
    }

    public String getWorkitemReflexRule() {
        return this.__WorkitemReflexRule;
    }

    public SDI getSDI() {
        return this.__SDI;
    }
}

