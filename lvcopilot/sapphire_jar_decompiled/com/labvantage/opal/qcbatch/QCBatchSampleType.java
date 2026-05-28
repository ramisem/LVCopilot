/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.qcbatch;

import com.labvantage.opal.actions.qcactions.QCBaseAction;
import com.labvantage.opal.qcbatch.QCBatchEvalRule;
import com.labvantage.opal.qcbatch.QCBatchParamSet;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class QCBatchSampleType {
    public static String LABVANTAGE_CVS_ID = "$Revision: 82881 $";
    private String __QCBatchSampleTypeID;
    private QueryProcessor __QueryProcessor;
    public static final String COLUMN_S_QCBATCHSAMPLETYPEID = "s_qcbatchsampletypeid";
    public static final String COLUMN_QCBATCHID = "qcbatchid";
    public static final String COLUMN_EVALSTATUS = "evalstatus";
    public static final String COLUMN_QCSAMPLETYPE = "qcsampletype";
    public static final String COLUMN_WORKITEMID = "workitemid";
    public static final String COLUMN_ACTIONAPPLY = "actionapply";
    public static final String COLUMN_ACTIONCALC = "actioncalc";
    public static final String COLUMN_ACTIONEVAL = "actioneval";
    public static final String COLUMN_POSITIONTYPE = "positiontype";
    public static final String COLUMN_POSITIONSTART = "positionstart";
    public static final String COLUMN_POSITIONEND = "positionend";
    public static final String COLUMN_POSITIONEVERY = "positionevery";
    public static final String COLUMN_POSITIONCOUNT = "positioncount";
    public static final String COLUMN_QCTEMPLATEKEYID1 = "qctemplatekeyid1";
    public static final String COLUMN_SPECID = "specid";
    public static final String COLUMN_SPECVERSIONID = "specversionid";
    public static final String COLUMN_SPECCONDITION = "speccondition";
    public static final String COLUMN_EVALUATEPARAMTYPE = "evaluateparamtype";
    public static final String COLUMN_STANDARDLEVEL = "standardlevel";
    public static final String COLUMN_LINKEDTO = "linkedto";
    private String __QCParameterType;
    private List __QCBatchParamSetList;
    private List __QCBatchEvalRuleList;
    private List __PrimaryColumnList;
    private HashMap __PrimaryInfoMap = new HashMap();

    public QCBatchSampleType(QueryProcessor queryProcessor, String id) {
        this.__PrimaryColumnList = this.getPrimaryColumns();
        this.__QueryProcessor = queryProcessor;
        this.__QCBatchSampleTypeID = id;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT S_QCBATCHSAMPLETYPEID, QCBATCHID, EVALSTATUS, QCSAMPLETYPE, WORKITEMID, ACTIONAPPLY,");
        sql.append(" ACTIONCALC, ACTIONEVAL, POSITIONTYPE, POSITIONSTART, POSITIONEND, POSITIONEVERY, POSITIONCOUNT,");
        sql.append(" QCTEMPLATEKEYID1, EVALUATEPARAMTYPE, SPECID, SPECVERSIONID, SPECCONDITION, STANDARDLEVEL, LINKEDTO ");
        sql.append(" FROM S_QCBATCHSAMPLETYPE WHERE S_QCBATCHSAMPLETYPEID = ");
        sql.append(safeSQL.addVar(this.__QCBatchSampleTypeID));
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() == 1) {
            for (int i = 0; i < this.__PrimaryColumnList.size(); ++i) {
                String columnid = (String)this.__PrimaryColumnList.get(i);
                this.__PrimaryInfoMap.put(columnid, ds.getValue(0, columnid));
            }
        }
    }

    public List getPrimaryColumns() {
        ArrayList<String> list = new ArrayList<String>();
        list.add(COLUMN_S_QCBATCHSAMPLETYPEID);
        list.add(COLUMN_QCBATCHID);
        list.add(COLUMN_EVALSTATUS);
        list.add(COLUMN_QCSAMPLETYPE);
        list.add(COLUMN_WORKITEMID);
        list.add(COLUMN_ACTIONAPPLY);
        list.add(COLUMN_ACTIONCALC);
        list.add(COLUMN_ACTIONEVAL);
        list.add(COLUMN_POSITIONTYPE);
        list.add(COLUMN_POSITIONSTART);
        list.add(COLUMN_POSITIONEND);
        list.add(COLUMN_POSITIONEVERY);
        list.add(COLUMN_POSITIONCOUNT);
        list.add(COLUMN_QCTEMPLATEKEYID1);
        list.add(COLUMN_EVALUATEPARAMTYPE);
        list.add(COLUMN_SPECID);
        list.add(COLUMN_SPECVERSIONID);
        list.add(COLUMN_SPECCONDITION);
        list.add(COLUMN_STANDARDLEVEL);
        list.add(COLUMN_LINKEDTO);
        return list;
    }

    public String getQCParameterType() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        String actionCalc;
        if (this.__QCParameterType == null && (actionCalc = this.getColumnValue(COLUMN_ACTIONCALC)) != null && actionCalc.length() > 0) {
            this.__QCParameterType = QCBaseAction.getParameterType(this.__QueryProcessor, actionCalc);
        }
        return this.__QCParameterType;
    }

    public String getColumnValue(String column) {
        Object o = this.getColumnObject(column);
        if (o != null) {
            return (String)o;
        }
        return null;
    }

    public Object getColumnObject(String column) {
        String columnid = column.toLowerCase();
        if (this.__PrimaryInfoMap.containsKey(columnid)) {
            return this.__PrimaryInfoMap.get(columnid);
        }
        return null;
    }

    public List getEvalRules() {
        return this.getEvalRules(false);
    }

    public List getEvalRules(boolean refresh) {
        if (this.__QCBatchEvalRuleList == null || refresh) {
            this.__QCBatchEvalRuleList = new ArrayList();
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT S_QCBATCHEVALRULEID, VIOLATIONCOUNT, WINDOWSIZE, SIGMABELOWCL, SIGMAABOVECL, RULEPATTERNFLAG, INSIDELIMITFLAG, WARNINGFLAG ");
            sql.append(" FROM S_QCBATCHEVALRULE");
            sql.append(" WHERE S_QCBATCHSAMPLETYPEID = " + safeSQL.addVar(this.__QCBatchSampleTypeID));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    QCBatchEvalRule qcBatchEvalRule = new QCBatchEvalRule(this.__QCBatchSampleTypeID, ds.getValue(i, "s_qcbatchevalruleid"));
                    if (ds.getBigDecimal(i, "violationcount") != null) {
                        qcBatchEvalRule.setViolationCount(ds.getBigDecimal(i, "violationcount").toString());
                    }
                    if (ds.getBigDecimal(i, "windowsize") != null) {
                        qcBatchEvalRule.setWindowSize(ds.getBigDecimal(i, "windowsize").toString());
                    }
                    if (ds.getBigDecimal(i, "sigmaabovecl") != null) {
                        qcBatchEvalRule.setSigmaAboveCL(ds.getBigDecimal(i, "sigmaabovecl").toString());
                    }
                    if (ds.getBigDecimal(i, "sigmabelowcl") != null) {
                        qcBatchEvalRule.setSigmaBelowCL(ds.getBigDecimal(i, "sigmabelowcl").toString());
                    }
                    qcBatchEvalRule.setRulePatternFlag(ds.getValue(i, "rulepatternflag"));
                    qcBatchEvalRule.setInsideLimitFlag(ds.getValue(i, "insidelimitflag"));
                    qcBatchEvalRule.setWarningFlag(ds.getValue(i, "warningflag"));
                    this.__QCBatchEvalRuleList.add(qcBatchEvalRule);
                }
            }
        }
        return this.__QCBatchEvalRuleList;
    }

    public List getParamSet() {
        return this.getParamSet(false);
    }

    public List getParamSet(boolean refresh) {
        if (this.__QCBatchParamSetList == null || refresh) {
            this.__QCBatchParamSetList = new ArrayList();
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT S_QCBATCHPARAMSETID, PARAMID, TARGETVALUE, TARGETUNITS, SD");
            sql.append(" FROM S_QCBATCHPARAMSET");
            sql.append(" WHERE S_QCBATCHSAMPLETYPEID = " + safeSQL.addVar(this.__QCBatchSampleTypeID));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    QCBatchParamSet qcBatchParamSet = new QCBatchParamSet(this.__QCBatchSampleTypeID, ds.getValue(i, "s_qcbatchparamsetid"));
                    qcBatchParamSet.setParamId(ds.getString(i, "paramid"));
                    qcBatchParamSet.setTargetValue(ds.getValue(i, "targetvalue"));
                    qcBatchParamSet.setTargetUnits(ds.getValue(i, "targetunits"));
                    qcBatchParamSet.setSD(ds.getValue(i, "sd"));
                    this.__QCBatchParamSetList.add(qcBatchParamSet);
                }
            }
        }
        return this.__QCBatchParamSetList;
    }

    public String getQCBatchSampleTypeID() {
        return this.__QCBatchSampleTypeID;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QCBatchSampleType)) {
            return false;
        }
        QCBatchSampleType qcBatchSampleType = (QCBatchSampleType)o;
        return this.__QCBatchSampleTypeID.equals(qcBatchSampleType.__QCBatchSampleTypeID);
    }

    public int hashCode() {
        return this.__QCBatchSampleTypeID.hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ QCBatchSampleType@" + this.hashCode());
        sb.append("\n" + this.__PrimaryInfoMap + "]");
        return sb.toString();
    }

    public void setQueryProcessor(QueryProcessor qp) {
        this.__QueryProcessor = qp;
    }

    public QueryProcessor getQueryProcessor() {
        return this.__QueryProcessor;
    }
}

