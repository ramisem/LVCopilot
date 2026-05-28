/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.stats;

import com.labvantage.opal.stats.math.UnivariateStats;
import com.labvantage.opal.stats.rule.PatternRule;
import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import sapphire.util.Logger;

public class Stats
extends UnivariateStats {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89929 $";
    static final int CL_FACTOR = 3;
    private List __RulesList = new ArrayList();
    private List __ResultList = new ArrayList();
    private List __FailureList;
    private TreeSet __PatternSet = new TreeSet();
    private double __SD;
    private double __CL;
    private double __UCL;
    private double __LCL;
    private int __CLFactor = 3;
    private boolean __CLSetFlag;
    private boolean __SDSetFlag;
    private boolean __UclSetFlag;
    private boolean __LclSetFlag;
    private boolean __IgnoreForward;
    private boolean __IgnoreBackward;
    private String __ChartType;
    private String __YParam;
    private List __EvaluationStatus;
    private List __DisplayValuesForMasterSampleSet;
    private List __TransformValuesForMasterSampleSet;
    private List __QCBatchIds;
    private List __ParamlistIds;
    private List __PositionValues;
    private List __ParamlistVersionIds;
    private List __Createdates;

    public Stats() {
        this.__FailureList = new ArrayList();
        this.__EvaluationStatus = new ArrayList();
        this.__DisplayValuesForMasterSampleSet = new ArrayList();
        this.__TransformValuesForMasterSampleSet = new ArrayList();
        this.__QCBatchIds = new ArrayList();
        this.__ParamlistIds = new ArrayList();
        this.__PositionValues = new ArrayList();
        this.__ParamlistVersionIds = new ArrayList();
        this.__Createdates = new ArrayList();
    }

    public Stats(double[] values) {
        this();
        for (int i = 0; i < values.length; ++i) {
            this.addValue(values[i]);
        }
    }

    public Stats(int[] values) {
        this();
        for (int i = 0; i < values.length; ++i) {
            this.addValue(values[i]);
        }
    }

    public Stats(String values, String delimiter) {
        this();
        StringTokenizer st = new StringTokenizer(values, delimiter);
        while (st.hasMoreTokens()) {
            double dvalue = Double.parseDouble(st.nextToken());
            this.addValue(dvalue);
        }
    }

    public Stats(double[] values, String[] x) {
        this(values);
        for (int i = 0; i < x.length; ++i) {
            this.addX(x[i]);
        }
    }

    public void addRule(PatternRule rule) {
        this.__RulesList.add(rule);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Stats: \n");
        sb.append("n   : ").append(this.getN()).append("\n");
        sb.append("min : ").append(this.getMin()).append("\n");
        sb.append("max : ").append(this.getMax()).append("\n");
        sb.append("sum : ").append(this.getSum()).append("\n");
        sb.append("SD  : ").append(this.getSD()).append("\n");
        sb.append("CL  : ").append(this.getCL()).append("\n");
        sb.append("UCL : ").append(this.getUCL()).append("\n");
        sb.append("LCL : ").append(this.getLCL()).append("\n");
        if (this.__RulesList.size() > 0) {
            sb.append("Pattern Rules: \n");
            for (int i = 0; i < this.__RulesList.size(); ++i) {
                sb.append(this.__RulesList.get(i).toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public double getSD() {
        if (!this.__SDSetFlag) {
            return this.getStandardDeviation();
        }
        return this.__SD;
    }

    public void setSD(double SD) {
        this.__SD = SD;
        this.__SDSetFlag = true;
    }

    public double getCL() {
        if (!this.__CLSetFlag) {
            return this.getMean();
        }
        return this.__CL;
    }

    public void setCL(double CL) {
        this.__CL = CL;
        this.__CLSetFlag = true;
    }

    public double getUCL() {
        double ucl = 0.0;
        ucl = !this.__UclSetFlag ? this.getCL() + this.getSD() * (double)this.__CLFactor : this.__UCL;
        return ucl;
    }

    public void setUCL(double UCL) {
        this.__UCL = UCL;
        this.__UclSetFlag = true;
    }

    public double getLCL() {
        double lcl = 0.0;
        lcl = !this.__LclSetFlag ? this.getCL() - this.getSD() * (double)this.__CLFactor : this.__LCL;
        return lcl;
    }

    public void setLCL(double LCL) {
        this.__LCL = LCL;
        this.__LclSetFlag = true;
    }

    public void validate(String[] arrDataPointsResult) {
        double cl = this.getCL();
        double sd = this.getSD();
        double ucl = this.getUCL();
        double lcl = this.getLCL();
        int index = 0;
        Iterator iterator = this._Values.iterator();
        while (iterator.hasNext()) {
            double value = (Double)iterator.next();
            this.matchPattern(index++, value, sd, cl, ucl, lcl, arrDataPointsResult);
        }
    }

    private void matchPattern(int index, double value, double sd, double cl, double ucl, double lcl, String[] arrDataPointsResult) {
        HashMap<String, CharSequence> datamap = new HashMap<String, CharSequence>();
        StringBuffer pattern = new StringBuffer("");
        datamap.put("value", Double.toString(value));
        datamap.put("pattern", pattern);
        if (value > ucl || value < lcl) {
            datamap.put("isfailed", "y");
            this.__FailureList.add(Integer.toString(index) + ":" + Double.toString(value));
        } else {
            datamap.put("isfailed", "n");
        }
        for (int i = 0; i < this.__RulesList.size(); ++i) {
            int subtrigger;
            int idx;
            int j;
            double abovelimit;
            double sigmabelowcl;
            PatternRule rule = (PatternRule)this.__RulesList.get(i);
            int rulepattern = rule.getRulePattern();
            int totalsubgroup = rule.getTotalSubgroup();
            int triggersubgroup = rule.getTriggerSubgroup();
            ArrayList<Integer> failDataIdxHolder = new ArrayList<Integer>();
            if (rulepattern != 0) {
                double tempvalue2;
                if (index - triggersubgroup + 1 < 0) continue;
                if (rulepattern == 1) {
                    boolean increaseflag = true;
                    double tempvalue1 = this.getValue(index);
                    tempvalue2 = this.getValue(index - 1);
                    if (tempvalue2 < tempvalue1) {
                        failDataIdxHolder.add(new Integer(index));
                        failDataIdxHolder.add(new Integer(index - 1));
                        tempvalue1 = tempvalue2;
                        for (int j2 = index - 2; j2 > index - triggersubgroup; --j2) {
                            tempvalue2 = this.getValue(j2);
                            if (tempvalue2 < tempvalue1) {
                                tempvalue1 = tempvalue2;
                                failDataIdxHolder.add(new Integer(j2));
                                continue;
                            }
                            increaseflag = false;
                            j2 = index - triggersubgroup;
                        }
                    } else {
                        increaseflag = false;
                    }
                    if (!increaseflag) continue;
                    pattern = (StringBuffer)datamap.get("pattern");
                    if (pattern.length() == 0) {
                        pattern.append(rule.getRuleNumber());
                    } else {
                        pattern.append(" ").append(rule.getRuleNumber());
                    }
                    this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
                    for (int idx2 = 0; idx2 < failDataIdxHolder.size(); ++idx2) {
                        Integer intObj = (Integer)failDataIdxHolder.get(idx2);
                        arrDataPointsResult[intObj.intValue()] = "Fail";
                    }
                    failDataIdxHolder.clear();
                    continue;
                }
                if (rulepattern == 2) {
                    boolean decreaseflag = true;
                    double tempvalue1 = this.getValue(index);
                    tempvalue2 = this.getValue(index - 1);
                    if (tempvalue2 > tempvalue1) {
                        failDataIdxHolder.add(new Integer(index));
                        failDataIdxHolder.add(new Integer(index - 1));
                        tempvalue1 = tempvalue2;
                        for (int j3 = index - 2; j3 > index - triggersubgroup; --j3) {
                            tempvalue2 = this.getValue(j3);
                            if (tempvalue2 > tempvalue1) {
                                tempvalue1 = tempvalue2;
                                failDataIdxHolder.add(new Integer(j3));
                                continue;
                            }
                            decreaseflag = false;
                            j3 = index - triggersubgroup;
                        }
                    } else {
                        decreaseflag = false;
                    }
                    if (!decreaseflag) continue;
                    pattern = (StringBuffer)datamap.get("pattern");
                    if (pattern.length() == 0) {
                        pattern.append(rule.getRuleNumber());
                    } else {
                        pattern.append(" ").append(rule.getRuleNumber());
                    }
                    this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
                    for (int idx3 = 0; idx3 < failDataIdxHolder.size(); ++idx3) {
                        Integer intObj = (Integer)failDataIdxHolder.get(idx3);
                        arrDataPointsResult[intObj.intValue()] = "Fail";
                    }
                    failDataIdxHolder.clear();
                    continue;
                }
                if (rulepattern == 3) {
                    boolean alternateflag = true;
                    boolean ascend = false;
                    double tempvalue1 = this.getValue(index);
                    double tempvalue22 = this.getValue(index - 1);
                    if (tempvalue22 == tempvalue1) {
                        alternateflag = false;
                    } else if (tempvalue22 > tempvalue1) {
                        failDataIdxHolder.add(new Integer(index));
                        failDataIdxHolder.add(new Integer(index - 1));
                        tempvalue1 = tempvalue22;
                        for (int j4 = index - 2; j4 > index - triggersubgroup; --j4) {
                            tempvalue22 = this.getValue(j4);
                            if (!ascend) {
                                if (tempvalue22 < tempvalue1) {
                                    tempvalue1 = tempvalue22;
                                    ascend = !ascend;
                                    failDataIdxHolder.add(new Integer(j4));
                                    continue;
                                }
                                alternateflag = false;
                                j4 = index - triggersubgroup;
                                continue;
                            }
                            if (tempvalue22 > tempvalue1) {
                                tempvalue1 = tempvalue22;
                                failDataIdxHolder.add(new Integer(j4));
                                ascend = !ascend;
                                continue;
                            }
                            alternateflag = false;
                            j4 = index - triggersubgroup;
                        }
                    } else if (tempvalue22 < tempvalue1) {
                        failDataIdxHolder.add(new Integer(index));
                        failDataIdxHolder.add(new Integer(index - 1));
                        tempvalue1 = tempvalue22;
                        for (int j5 = index - 2; j5 > index - triggersubgroup; --j5) {
                            tempvalue22 = this.getValue(j5);
                            if (!ascend) {
                                if (tempvalue22 > tempvalue1) {
                                    tempvalue1 = tempvalue22;
                                    ascend = !ascend;
                                    failDataIdxHolder.add(new Integer(j5));
                                    continue;
                                }
                                alternateflag = false;
                                j5 = index - triggersubgroup;
                                continue;
                            }
                            if (tempvalue22 < tempvalue1) {
                                tempvalue1 = tempvalue22;
                                ascend = !ascend;
                                failDataIdxHolder.add(new Integer(j5));
                                continue;
                            }
                            alternateflag = false;
                            j5 = index - triggersubgroup;
                        }
                    }
                    if (!alternateflag) continue;
                    pattern = (StringBuffer)datamap.get("pattern");
                    if (pattern.length() == 0) {
                        pattern.append(rule.getRuleNumber());
                    } else {
                        pattern.append(" ").append(rule.getRuleNumber());
                    }
                    this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
                    for (int idx4 = 0; idx4 < failDataIdxHolder.size(); ++idx4) {
                        Integer intObj = (Integer)failDataIdxHolder.get(idx4);
                        arrDataPointsResult[intObj.intValue()] = "Fail";
                    }
                    failDataIdxHolder.clear();
                    continue;
                }
                if (rulepattern == 4) {
                    boolean differenceflag = true;
                    double sigmaabovecl = rule.getSigmaAboveCL();
                    double tempvalue1 = this.getValue(index);
                    double tempvalue23 = this.getValue(index - 1);
                    if (tempvalue23 == tempvalue1) {
                        differenceflag = false;
                    } else if (tempvalue23 > tempvalue1) {
                        if (tempvalue23 - tempvalue1 <= sigmaabovecl * sd) {
                            differenceflag = false;
                        }
                    } else if (tempvalue1 - tempvalue23 <= sigmaabovecl * sd) {
                        differenceflag = false;
                    }
                    if (!differenceflag) continue;
                    arrDataPointsResult[index] = "Fail";
                    arrDataPointsResult[index - 1] = "Fail";
                    pattern = (StringBuffer)datamap.get("pattern");
                    if (pattern.length() == 0) {
                        pattern.append(rule.getRuleNumber());
                    } else {
                        pattern.append(" ").append(rule.getRuleNumber());
                    }
                    this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
                    continue;
                }
                if (rulepattern != 5) continue;
                double sigmaabovecl = rule.getSigmaAboveCL();
                sigmabelowcl = rule.getSigmaBelowCL();
                if (sigmaabovecl == 9.99999999E8 && sigmabelowcl == 9.99999999E8) {
                    int subtriggerabovecl = 0;
                    int subtriggerbelowcl = 0;
                    if (index - totalsubgroup + 1 >= 0) {
                        for (int j6 = index; j6 > index - totalsubgroup; --j6) {
                            double tempvalue = this.getValue(j6);
                            if (tempvalue > cl) {
                                ++subtriggerabovecl;
                                failDataIdxHolder.add(new Integer(j6));
                                continue;
                            }
                            if (!(tempvalue < cl)) continue;
                            ++subtriggerbelowcl;
                            failDataIdxHolder.add(new Integer(j6));
                        }
                    }
                    if (subtriggerabovecl < triggersubgroup && subtriggerbelowcl < triggersubgroup) continue;
                    pattern = (StringBuffer)datamap.get("pattern");
                    if (pattern.length() == 0) {
                        pattern.append(rule.getRuleNumber());
                    } else {
                        pattern.append(" ").append(rule.getRuleNumber());
                    }
                    this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
                    for (int idx5 = 0; idx5 < failDataIdxHolder.size(); ++idx5) {
                        Integer intObj = (Integer)failDataIdxHolder.get(idx5);
                        arrDataPointsResult[intObj.intValue()] = "Fail";
                    }
                    failDataIdxHolder.clear();
                    continue;
                }
                if (sigmaabovecl == 9.99999999E8 || sigmabelowcl == 9.99999999E8) continue;
                abovelimit = 9.99999999E8;
                double belowlimit = 9.99999999E8;
                try {
                    abovelimit = cl + sd * sigmaabovecl;
                }
                catch (Exception n) {
                    Logger.logError(n.getMessage(), n);
                }
                try {
                    belowlimit = cl - sd * sigmabelowcl;
                }
                catch (Exception n) {
                    Logger.logError(n.getMessage(), n);
                }
                int subtriggerabovecl = 0;
                int subtriggerbelowcl = 0;
                if (index - totalsubgroup + 1 >= 0) {
                    for (j = index; j > index - totalsubgroup; --j) {
                        if (belowlimit != 9.99999999E8 && this.getValue(j) < belowlimit) {
                            ++subtriggerbelowcl;
                            failDataIdxHolder.add(new Integer(j));
                            continue;
                        }
                        if (abovelimit == 9.99999999E8 || !(this.getValue(j) > abovelimit)) continue;
                        ++subtriggerabovecl;
                        failDataIdxHolder.add(new Integer(j));
                    }
                }
                if (subtriggerabovecl < triggersubgroup && subtriggerbelowcl < triggersubgroup) continue;
                pattern = (StringBuffer)datamap.get("pattern");
                if (pattern.length() == 0) {
                    pattern.append(rule.getRuleNumber());
                } else {
                    pattern.append(" ").append(rule.getRuleNumber());
                }
                this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
                for (idx = 0; idx < failDataIdxHolder.size(); ++idx) {
                    Integer intObj = (Integer)failDataIdxHolder.get(idx);
                    arrDataPointsResult[intObj.intValue()] = "Fail";
                }
                failDataIdxHolder.clear();
                continue;
            }
            double sigmaabovecl = rule.getSigmaAboveCL();
            sigmabelowcl = rule.getSigmaBelowCL();
            abovelimit = 9.99999999E8;
            double belowlimit = 9.99999999E8;
            boolean skipforwardvalidation = false;
            if (sigmaabovecl != 9.99999999E8) {
                abovelimit = cl + sd * sigmaabovecl;
                if (sigmabelowcl != 9.99999999E8) {
                    belowlimit = cl - sd * sigmabelowcl;
                }
            } else if (sigmabelowcl != 9.99999999E8) {
                belowlimit = cl - sd * sigmabelowcl;
            }
            if (index - totalsubgroup + 1 >= 0 && !this.__IgnoreBackward) {
                subtrigger = 0;
                if (abovelimit != 9.99999999E8) {
                    if (belowlimit == 9.99999999E8) {
                        for (j = index; j > index - totalsubgroup; --j) {
                            if (!(this.getValue(j) > abovelimit)) continue;
                            ++subtrigger;
                            failDataIdxHolder.add(new Integer(j));
                        }
                    } else {
                        for (j = index; j > index - totalsubgroup; --j) {
                            double tempvalue = this.getValue(j);
                            if (rule.isWithinlimits()) {
                                if (!(tempvalue < abovelimit) || !(tempvalue > belowlimit)) continue;
                                ++subtrigger;
                                failDataIdxHolder.add(new Integer(j));
                                continue;
                            }
                            if (!(tempvalue > abovelimit) && !(tempvalue < belowlimit)) continue;
                            ++subtrigger;
                            failDataIdxHolder.add(new Integer(j));
                        }
                    }
                } else if (belowlimit != 9.99999999E8) {
                    for (j = index; j > index - totalsubgroup; --j) {
                        if (!(this.getValue(j) < belowlimit)) continue;
                        ++subtrigger;
                        failDataIdxHolder.add(new Integer(j));
                    }
                }
                if (subtrigger >= triggersubgroup) {
                    pattern = (StringBuffer)datamap.get("pattern");
                    if (pattern.length() == 0) {
                        pattern.append(rule.getRuleNumber());
                    } else {
                        pattern.append(" ").append(rule.getRuleNumber());
                    }
                    this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
                    skipforwardvalidation = true;
                    for (idx = 0; idx < failDataIdxHolder.size(); ++idx) {
                        Integer intObj = (Integer)failDataIdxHolder.get(idx);
                        arrDataPointsResult[intObj.intValue()] = "Fail";
                    }
                    failDataIdxHolder.clear();
                }
            }
            if ((long)(index + totalsubgroup) >= this.getN() || this.__IgnoreForward || skipforwardvalidation) continue;
            subtrigger = 0;
            if (abovelimit != 9.99999999E8) {
                if (belowlimit == 9.99999999E8) {
                    for (j = index; j < index + totalsubgroup; ++j) {
                        if (!(this.getValue(j) > abovelimit)) continue;
                        ++subtrigger;
                        failDataIdxHolder.add(new Integer(j));
                    }
                } else {
                    for (j = index; j < index + totalsubgroup; ++j) {
                        double tempvalue = this.getValue(j);
                        if (rule.isWithinlimits()) {
                            if (!(tempvalue < abovelimit) || !(tempvalue > belowlimit)) continue;
                            ++subtrigger;
                            failDataIdxHolder.add(new Integer(j));
                            continue;
                        }
                        if (!(tempvalue > abovelimit) && !(tempvalue < belowlimit)) continue;
                        ++subtrigger;
                        failDataIdxHolder.add(new Integer(j));
                    }
                }
            } else if (belowlimit != 9.99999999E8) {
                for (j = index; j < index + totalsubgroup; ++j) {
                    if (!(this.getValue(j) < belowlimit)) continue;
                    ++subtrigger;
                    failDataIdxHolder.add(new Integer(j));
                }
            }
            if (subtrigger < triggersubgroup) continue;
            pattern = (StringBuffer)datamap.get("pattern");
            if (pattern.length() == 0) {
                pattern.append(rule.getRuleNumber());
            } else {
                pattern.append(" ").append(rule.getRuleNumber());
            }
            this.__PatternSet.add(Integer.toString(rule.getRuleNumber()));
            for (idx = 0; idx < failDataIdxHolder.size(); ++idx) {
                Integer intObj = (Integer)failDataIdxHolder.get(idx);
                arrDataPointsResult[intObj.intValue()] = "Fail";
            }
            failDataIdxHolder.clear();
        }
        this.__ResultList.add(index, datamap);
    }

    public List getResultlist() {
        return this.__ResultList;
    }

    public String getFailedPatterns() {
        StringBuffer sb = new StringBuffer();
        Iterator iterator = this.__PatternSet.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            sb.append(" ");
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    public String getFailedValues() {
        StringBuffer sb = new StringBuffer();
        Iterator iterator = this.__FailureList.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            sb.append(" ");
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }

    public static void print(String msg) {
        Trace.log(msg);
    }

    public boolean isIgnoreforward() {
        return this.__IgnoreForward;
    }

    public void setIgnoreforward(boolean ignoreforward) {
        this.__IgnoreForward = ignoreforward;
    }

    public boolean isIgnorebackward() {
        return this.__IgnoreBackward;
    }

    public void setIgnorebackward(boolean ignorebackward) {
        this.__IgnoreBackward = ignorebackward;
    }

    public int getClfactor() {
        return this.__CLFactor;
    }

    public void setClfactor(int clfactor) {
        this.__CLFactor = clfactor;
    }

    public List getValuesAsList() {
        return this._Values;
    }

    public String getChartType() {
        return this.__ChartType;
    }

    public void setChartType(String parChartType) {
        this.__ChartType = parChartType;
    }

    public String getYparam() {
        return this.__YParam;
    }

    public void setYparam(String parYparam) {
        this.__YParam = parYparam;
    }

    public void addTransformValue(double transformVals) {
        this.__TransformValuesForMasterSampleSet.add(new Double(transformVals));
    }

    public List getAllTransformValues() {
        return this.__TransformValuesForMasterSampleSet;
    }

    public void setEvalStatus(String status) {
        this.__EvaluationStatus.add(status);
    }

    public List getAllEvalStatus() {
        return this.__EvaluationStatus;
    }

    public void setQCBatchId(String qcbatchid) {
        this.__QCBatchIds.add(qcbatchid);
    }

    public List getAllQCBatchIds() {
        return this.__QCBatchIds;
    }

    public void setParamListId(String paramlistid) {
        this.__ParamlistIds.add(paramlistid);
    }

    public List getAllParamListIds() {
        return this.__ParamlistIds;
    }

    public void setPositionValue(String positionValue) {
        this.__PositionValues.add(positionValue);
    }

    public List getAllPositionValues() {
        return this.__PositionValues;
    }

    public void setParamListVersionId(String paramlistversionid) {
        this.__ParamlistVersionIds.add(paramlistversionid);
    }

    public List getAllParamListVersionIds() {
        return this.__ParamlistVersionIds;
    }

    public void setCreateDateValue(String createdate) {
        this.__Createdates.add(createdate);
    }

    public List getAllCreateDates() {
        return this.__Createdates;
    }
}

