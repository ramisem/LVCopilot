/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import com.labvantage.opal.actions.MultiSampleChild;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.util.UnitsUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ApplyChildSamplePlan
extends BaseAction
implements sapphire.action.ApplyChildSamplePlan {
    public static final String PLANTYPE_ALIQUOT = "Aliquot";
    public static final String PLANTYPE_DERIVATIVE = "Derivative";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String childsampleplanid = actionProps.getProperty("childsampleplanid");
        String childsampleplanversionid = actionProps.getProperty("childsampleplanversionid");
        String sampleid = actionProps.getProperty("sampleid");
        String sdiworkitemid = actionProps.getProperty("sdiworkitemid", "");
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select s_childsampleplanid, s_childsampleplanversionid, sampletypeid, markparentconsumedflag, quantityvalidationflag, childsamplestatus");
        sql.append(" from s_childsampleplan");
        sql.append(" where s_childsampleplanid = ").append(safeSQL.addVar(childsampleplanid));
        if ("C".equals(childsampleplanversionid)) {
            sql.append(" and versionstatus = 'C'");
        } else {
            sql.append(" and s_childsampleplanversionid = ").append(safeSQL.addVar(childsampleplanversionid));
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds == null || ds.size() <= 0) {
            String error = "";
            error = "C".equals(childsampleplanversionid) ? this.getTranslationProcessor().translate("No current version found for Child Sample Plan") + ": " + childsampleplanid : this.getTranslationProcessor().translate("No Child Sample Plan found") + " :: " + childsampleplanid + "," + childsampleplanversionid;
            throw new SapphireException("ApplyChildSamplePlan", "VALIDATION", error);
        }
        String childsamplestatus = ds.getString(0, "childsamplestatus", "");
        String markparentconsumedflag = ds.getString(0, "markparentconsumedflag", "N");
        String quantityvalidationflag = ds.getString(0, "quantityvalidationflag", "N");
        String childsampleplansampletypeid = ds.getString(0, "sampletypeid", "");
        childsampleplanversionid = ds.getString(0, "s_childsampleplanversionid");
        safeSQL.reset();
        if (childsampleplansampletypeid.length() > 0 && this.database.getPreparedCount("select count(s_sampleid) from s_sample where sampletypeid != " + safeSQL.addVar(childsampleplansampletypeid) + " and s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")", safeSQL.getValues()) > 0) {
            throw new SapphireException("ApplyChildSamplePlan", "VALIDATION", childsampleplanid + "::" + childsampleplanversionid + " " + this.getTranslationProcessor().translate("Child Sample Plan can only be applied on Sample Type") + ": " + childsampleplansampletypeid);
        }
        String sysuserid = this.connectionInfo.getSysuserId();
        if ("(system)".equals(sysuserid)) {
            boolean throwException = false;
            if ("In Circulation".equals(childsamplestatus)) {
                throwException = true;
            } else {
                PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
                String[] bbpolicychildsamplestatus = policy.getProperty("childsamplestatus");
                if ("In Circulation".equals(bbpolicychildsamplestatus)) {
                    throwException = true;
                } else if ("inherit".equals(bbpolicychildsamplestatus)) {
                    safeSQL.reset();
                    DataSet dsinherit = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, storagestatus from s_sample where s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")", safeSQL.getValues());
                    for (int i = 0; i < dsinherit.size(); ++i) {
                        String storagestatus = dsinherit.getString(i, "storagestatus", "");
                        if ("Allocated".equals(storagestatus) || "In Prep".equals(storagestatus)) continue;
                        throwException = true;
                        break;
                    }
                }
            }
            if (throwException) {
                throw new SapphireException("ApplyChildSamplePlan", "VALIDATION", "ApplyChildSamplePlan action does not support running under \"(system)\" user. If running action from a Task, please set the property \"processassysuserid\" to a valid application user.");
            }
        }
        DataSet parentQuantityDS = new DataSet();
        safeSQL.reset();
        sql.setLength(0);
        sql.append("select s_childsampleplanid, s_childsampleplanversionid, s_childsampleplanitemid, plantype, sampletemplateid, childsamplecount, ");
        sql.append("        derivativesampletypeid, derivativepreptypeid, derivativetreatmenttypeid, derivativeparentquantity, derivativeparentquantityunits,");
        sql.append("        containertypeid, quantity, quantityunits, parentitemid,");
        sql.append("        processtype, processinstruction, todepartmentid, labelmethodid, labelmethodversionid, sdiworkitemcompletionstatus");
        sql.append("  from s_childsampleplanitem");
        sql.append("  where activeflag != 'N'");
        sql.append("  and s_childsampleplanid = ").append(safeSQL.addVar(childsampleplanid));
        sql.append("          and s_childsampleplanversionid = ").append(safeSQL.addVar(childsampleplanversionid));
        sql.append("  order by usersequence");
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            if ("Y".equals(quantityvalidationflag)) {
                String[] parentSampleArray;
                for (String parentSample : parentSampleArray = StringUtil.split(sampleid, ";")) {
                    sql.setLength(0);
                    safeSQL.reset();
                    DataSet qtyds = this.getQueryProcessor().getPreparedSqlDataSet("select linkkeyid1, trackitemid, qtycurrent, qtyunits from trackitem where linksdcid = 'Sample' and linkkeyid1 = " + safeSQL.addVar(parentSample), safeSQL.getValues());
                    if (qtyds == null || qtyds.size() <= 0) continue;
                    double parentQuantity = qtyds.getDouble(0, "qtycurrent", 0.0);
                    String parentQuantityUnit = qtyds.getValue(0, "qtyunits", "");
                    if (StringUtil.getLen(parentQuantityUnit) <= 0L) continue;
                    double childQuantity = 0.0;
                    for (int i = 0; i < ds.size(); ++i) {
                        if (!PLANTYPE_ALIQUOT.equals(ds.getString(i, "plantype"))) continue;
                        String quantity = ds.getValue(i, "quantity", "");
                        String quantityunits = ds.getString(i, "quantityunits", "");
                        if (quantity.length() <= 0 || quantityunits.length() <= 0) continue;
                        String aliquotQuantity = UnitsUtil.getConvertedValue(this.getQueryProcessor(), quantityunits, parentQuantityUnit, quantity);
                        if (aliquotQuantity == null) {
                            throw new SapphireException("ApplyChildSamplePlan", "VALIDATION", this.getTranslationProcessor().translate("Unable to translate units") + " (" + parentQuantityUnit + " >> " + quantityunits + ")");
                        }
                        childQuantity += Double.parseDouble(aliquotQuantity) * ds.getDouble(i, "childsamplecount", 0.0);
                    }
                    if (childQuantity > parentQuantity) {
                        throw new SapphireException("ApplyChildSamplePlan", "VALIDATION", this.getTranslationProcessor().translate("Not enough parent sample quantity to apply Child Sample plan") + " (" + childsampleplanid + ":" + childsampleplanversionid + ")");
                    }
                    if ("Y".equals(markparentconsumedflag)) continue;
                    int row = parentQuantityDS.addRow();
                    parentQuantityDS.setString(row, "trackitemid", qtyds.getValue(0, "trackitemid", ""));
                    parentQuantityDS.setNumber(row, "qtycurrent", parentQuantity - childQuantity);
                }
            }
            PropertyList props = new PropertyList();
            for (int i = 0; i < ds.size(); ++i) {
                props.clear();
                String childsampleplanitemid = ds.getString(i, "s_childsampleplanitemid");
                String plantype = ds.getString(i, "plantype");
                String sampletemplateid = ds.getString(i, "sampletemplateid", "");
                String derivativesampletypeid = ds.getString(i, "derivativesampletypeid", "");
                String derivativepreptypeid = ds.getString(i, "derivativepreptypeid", "");
                String derivativetreatmenttypeid = ds.getString(i, "derivativetreatmenttypeid", "");
                double derivativeparentquantity = ds.getDouble(i, "derivativeparentquantity", 0.0);
                String derivativeparentquantityunits = ds.getValue(i, "derivativeparentquantityunits");
                String quantity = ds.getValue(i, "quantity");
                String quantityunits = ds.getString(i, "quantityunits");
                String parentitemid = ds.getString(i, "parentitemid", "");
                String sdiworkitemcompletionstatus = ds.getString(i, "sdiworkitemcompletionstatus", "");
                String containertypeid = ds.getString(i, "containertypeid", "");
                String processtype = ds.getString(i, "processtype", "");
                String processinstruction = ds.getString(i, "processinstruction", "");
                int childsamplecount = ds.getInt(i, "childsamplecount", 0);
                if (childsamplecount <= 0) continue;
                try {
                    if (!PLANTYPE_ALIQUOT.equals(plantype) && !PLANTYPE_DERIVATIVE.equals(plantype)) continue;
                    String parentsampleid = parentitemid.length() > 0 ? ds.getString(i, "parentsampleid", "") : sampleid;
                    props.setProperty("sdiworkitemid", sdiworkitemid);
                    props.setProperty("sdiworkitemcompletionstatus", sdiworkitemcompletionstatus);
                    props.setProperty("containertypeid", containertypeid);
                    props.setProperty("childcolumn_processtype", processtype);
                    props.setProperty("childcolumn_processinstruction", processinstruction);
                    props.setProperty("mode", plantype);
                    props.setProperty("child_copies", String.valueOf(childsamplecount));
                    props.setProperty("parent_sampleid", parentsampleid);
                    if (sampletemplateid.trim().length() > 0) {
                        props.setProperty("child_templateid", sampletemplateid);
                    }
                    props.setProperty("child_quantity", quantity);
                    props.setProperty("child_unit", quantityunits);
                    if (PLANTYPE_DERIVATIVE.equals(plantype)) {
                        props.setProperty("child_sampletypeid", derivativesampletypeid);
                        props.setProperty("child_preptypeid", derivativepreptypeid);
                        props.setProperty("child_treatmentid", derivativetreatmenttypeid);
                        if ((!"Y".equals(markparentconsumedflag) || parentitemid.length() > 0) && derivativeparentquantity > 0.0 && derivativeparentquantityunits.length() > 0) {
                            safeSQL.reset();
                            DataSet qtyds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid, linkkeyid1, qtycurrent, qtyunits from trackitem where linksdcid = 'Sample' and linkkeyid1 in ( " + safeSQL.addIn(StringUtil.replaceAll(parentsampleid, ";", "','")) + " )", safeSQL.getValues());
                            if (qtyds != null && qtyds.size() > 0) {
                                for (int qrow = 0; qrow < qtyds.size(); ++qrow) {
                                    String parentQuantityUnits;
                                    double parentQuantity = qtyds.getDouble(qrow, "qtycurrent", 0.0);
                                    if (!(parentQuantity > 0.0) || (parentQuantityUnits = qtyds.getString(qrow, "qtyunits", "")).length() <= 0) continue;
                                    String decrementQuantity = UnitsUtil.getConvertedValue(this.getQueryProcessor(), derivativeparentquantityunits, parentQuantityUnits, String.valueOf(derivativeparentquantity));
                                    if (decrementQuantity != null) {
                                        double finalParentQuantity;
                                        HashMap<String, String> filter = new HashMap<String, String>();
                                        filter.put("trackitemid", qtyds.getString(qrow, "trackitemid"));
                                        int row = parentQuantityDS.findRow(filter);
                                        if (row != -1) {
                                            double d = parentQuantityDS.getDouble(row, "qtycurrent");
                                            finalParentQuantity = d - (double)childsamplecount * Double.parseDouble(decrementQuantity);
                                            parentQuantityDS.setNumber(row, "qtycurrent", finalParentQuantity);
                                        } else {
                                            finalParentQuantity = parentQuantity - (double)childsamplecount * Double.parseDouble(decrementQuantity);
                                            finalParentQuantity = new BigDecimal(finalParentQuantity).setScale(3, RoundingMode.HALF_UP).doubleValue();
                                            row = parentQuantityDS.addRow();
                                            parentQuantityDS.setString(row, "trackitemid", qtyds.getValue(qrow, "trackitemid", ""));
                                            parentQuantityDS.setNumber(row, "qtycurrent", finalParentQuantity);
                                        }
                                        if (!(finalParentQuantity < 0.0)) continue;
                                        throw new SapphireException("ApplyChildSamplePlan", "VALIDATION", this.getTranslationProcessor().translate("Not enough parent sample quantity to apply Child Sample plan") + " (" + childsampleplanid + ":" + childsampleplanversionid + ")");
                                    }
                                    this.logger.info("ApplyChildSamplePlan: No unit conversion found from " + derivativeparentquantityunits + " to " + parentQuantityUnits);
                                }
                            }
                        }
                    }
                    if (childsamplestatus.length() > 0) {
                        props.setProperty("child_storagestatus", childsamplestatus);
                    }
                    props.setProperty("__childsampleplanid", childsampleplanid);
                    props.setProperty("__childsampleplanversionid", childsampleplanversionid);
                    props.setProperty("__childsampleplanitemid", childsampleplanitemid);
                    this.getActionProcessor().processActionClass(MultiSampleChild.class.getName(), props);
                    String childsampleid = props.getProperty("newkeyid1");
                    ds.setString(i, "newkeyid1", childsampleid);
                    if (i >= ds.size() - 1) continue;
                    for (int j = i + 1; j < ds.size() && ds.getString(j, "parentitemid", "").equals(childsampleplanitemid); ++j) {
                        ds.setString(j, "parentsampleid", childsampleid);
                    }
                    continue;
                }
                catch (ActionException e) {
                    this.setErrors(e.getErrorHandler());
                }
            }
            if (parentQuantityDS != null && parentQuantityDS.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", parentQuantityDS.getColumnValues("trackitemid", ";"));
                props.setProperty("qtycurrent", parentQuantityDS.getColumnValues("qtycurrent", ";"));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
            DataSet wids = new DataSet();
            for (int i = 0; i < ds.size(); ++i) {
                String childsampleid = ds.getString(i, "newkeyid1", "");
                if (!OpalUtil.isNotEmpty(childsampleid)) continue;
                String _childsampleplanid = ds.getString(i, "s_childsampleplanid");
                String _childsampleplanversionid = ds.getString(i, "s_childsampleplanversionid");
                String childsampleplanitemid = ds.getString(i, "s_childsampleplanitemid");
                sql.setLength(0);
                sql.append("select s_childsampleplanid, s_childsampleplanversionid, s_childsampleplanitemid, workitemid, workitemversionid, repeatcount, activeflag, applyonaddflag, embedchildsampleplanid, embedchildsampleplanversionid, assigneddepartmentid");
                sql.append("  from s_childsampleplanworkitem");
                sql.append("  where s_childsampleplanid = ? and s_childsampleplanversionid = ? and s_childsampleplanitemid = ?");
                sql.append("  and (activeflag != 'N' or activeflag is null)");
                DataSet testds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{_childsampleplanid, _childsampleplanversionid, childsampleplanitemid});
                if (testds == null || testds.size() <= 0) continue;
                for (int testcount = 0; testcount < testds.size(); ++testcount) {
                    String[] samples;
                    String childworkitemid = testds.getString(testcount, "workitemid", "");
                    String childworkitemversionid = testds.getString(testcount, "workitemversionid", "");
                    String applyonaddflag = testds.getString(testcount, "applyonaddflag", "N");
                    String assigneddepartmentid = testds.getString(testcount, "assigneddepartmentid", "");
                    if (!OpalUtil.isNotEmpty(childworkitemid)) continue;
                    for (String sample : samples = StringUtil.split(childsampleid, ";")) {
                        if (!OpalUtil.isNotEmpty(sample)) continue;
                        int row = wids.addRow();
                        wids.setString(row, "keyid1", sample);
                        wids.setString(row, "workitemid", childworkitemid);
                        wids.setString(row, "workitemversionid", childworkitemversionid);
                        wids.setString(row, "applyworkitem", applyonaddflag);
                        wids.setString(row, "embedchildsampleplanid", testds.getString(testcount, "embedchildsampleplanid", ""));
                        wids.setString(row, "embedchildsampleplanversionid", testds.getString(testcount, "embedchildsampleplanversionid", ""));
                        wids.setString(row, "s_assigneddepartment", assigneddepartmentid);
                        wids.setString(row, "sourcesstudyid", "");
                    }
                }
            }
            if (wids.size() > 0) {
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", wids.getColumnValues("keyid1", ";"));
                props.setProperty("workitemid", wids.getColumnValues("workitemid", ";"));
                props.setProperty("workitemversionid", wids.getColumnValues("workitemversionid", ";"));
                props.setProperty("applyworkitem", wids.getColumnValues("applyworkitem", ";"));
                props.setProperty("propsmatch", "Y");
                props.setProperty("embedchildsampleplanid", wids.getColumnValues("embedchildsampleplanid", ";"));
                props.setProperty("embedchildsampleplanversionid", wids.getColumnValues("embedchildsampleplanversionid", ";"));
                props.setProperty("s_assigneddepartment", wids.getColumnValues("s_assigneddepartment", ";"));
                this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
            }
            if ("Y".equals(markparentconsumedflag)) {
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", sampleid);
                props.setProperty("samplestatus", "Disposed");
                props.setProperty("storagestatus", "Disposed");
                props.setProperty("storagedisposalstatus", "Consumed");
                props.setProperty("disposalstatus", "Disposed");
                props.setProperty("__sdcruleconfirm", "Y");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", sampleid);
                props.setProperty("qtycurrent", "0");
                props.setProperty("__sdcruleconfirm", "Y");
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
            }
            DataSet eventlogds = new DataSet();
            String[] sampleArray = StringUtil.split(sampleid, ";");
            String[] sdiworkitemArray = StringUtil.split(sdiworkitemid, ";");
            boolean setSDIWorkItem = sdiworkitemArray.length == sampleArray.length;
            int i = 0;
            int logsequence = this.getSequenceProcessor().getSequence("Sample", "ChildSamplePlanLog", sampleArray.length);
            for (String sample : sampleArray) {
                int row = eventlogds.addRow();
                eventlogds.setString(row, "s_childsampleplanlogid", "CSPL-" + logsequence++);
                eventlogds.setString(row, "s_sampleid", sample);
                eventlogds.setString(row, "childsampleplanid", childsampleplanid);
                eventlogds.setString(row, "childsampleplanversionid", childsampleplanversionid);
                if (!setSDIWorkItem) continue;
                eventlogds.setString(row, "sdiworkitemid", sdiworkitemArray[i++]);
            }
            eventlogds.setString(-1, "appliedflag", "Y");
            eventlogds.setString(-1, "createby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
            eventlogds.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
            eventlogds.setString(-1, "createtool", "ApplyChildSamplePlan");
            eventlogds.setString(-1, "modby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
            eventlogds.setDate(-1, "moddt", DateTimeUtil.getNowCalendar());
            eventlogds.setString(-1, "modtool", "ApplyChildSamplePlan");
            if (eventlogds.size() > 0) {
                DataSetUtil.insert(this.database, eventlogds, "s_childsampleplanlog");
            }
        }
        actionProps.setProperty("newkeyid1", ds != null && ds.size() > 0 ? ds.getColumnValues("newkeyid1", ";") : "");
    }
}

