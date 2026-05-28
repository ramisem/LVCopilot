/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.stats.rule;

import com.labvantage.opal.stats.exception.RuleException;

public class PatternRule {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __RuleName = "";
    private int __RuleNumber;
    private String __RuleDesc = "";
    private int __RuleType = 0;
    private int __RulePattern = 0;
    private double __SigmaAboveCL = 9.99999999E8;
    private double __SigmaBelowCL = 9.99999999E8;
    private int __TotalSubgroup = 0;
    private int __TriggerSubgroup = 0;
    private boolean __WithinLimits = true;
    private int __TriggerLocation = 0;
    public static final int NOTDEFINED = 0;
    public static final int INCREASING = 1;
    public static final int DECREASING = 2;
    public static final int ALTERNATING = 3;
    public static final int DIFFERENCE = 4;
    public static final int SAMESIDE = 5;
    public static final int CUSTOM = 0;
    public static final int WE1 = 1;
    public static final int WE2 = 2;
    public static final int WE3 = 3;
    public static final int WE4 = 4;
    public static final int WE5 = 5;
    public static final int WE6 = 6;
    public static final int WITHIN = 0;
    public static final int EXCEEDS = 1;
    public static final double DONOTCALCULATE = 9.99999999E8;

    public PatternRule(int ruleNumber) {
        this.__RuleNumber = ruleNumber;
        this.setRuleType(0);
    }

    public PatternRule(int ruleNumber, String ruleName) {
        this.__RuleNumber = ruleNumber;
        this.__RuleName = ruleName;
        this.setRuleType(0);
    }

    public PatternRule(int ruleNumber, String ruleName, int ruleType) throws RuleException {
        this.__RuleNumber = ruleNumber;
        this.__RuleName = ruleName;
        if (ruleType == 1) {
            this.setRuleType(1);
            this.setRuleDesc("2 of 3 above 2/3 of CL");
            this.setTriggerSubgroup(2);
            this.setTotalSubgroup(3);
            this.setSigmaAboveCL(2.0);
            this.setSigmaBelowCL(9.99999999E8);
        } else if (ruleType == 2) {
            this.setRuleType(2);
            this.setRuleDesc("2 of 3 below 2/3 of CL");
            this.setTriggerSubgroup(2);
            this.setTotalSubgroup(3);
            this.setSigmaAboveCL(9.99999999E8);
            this.setSigmaBelowCL(2.0);
        } else if (ruleType == 3) {
            this.setRuleType(3);
            this.setRuleDesc("4 of 5 above 1/3 of CL");
            this.setTriggerSubgroup(4);
            this.setTotalSubgroup(5);
            this.setSigmaAboveCL(1.0);
            this.setSigmaBelowCL(9.99999999E8);
        } else if (ruleType == 4) {
            this.setRuleType(4);
            this.setRuleDesc("4 of 5 below 1/3 of CL");
            this.setTriggerSubgroup(4);
            this.setTotalSubgroup(5);
            this.setSigmaAboveCL(9.99999999E8);
            this.setSigmaBelowCL(1.0);
        } else if (ruleType == 5) {
            this.setRuleType(5);
            this.setRuleDesc("8 consecutive points above CL");
            this.setTriggerSubgroup(8);
            this.setTotalSubgroup(8);
            this.setSigmaAboveCL(0.0);
            this.setSigmaBelowCL(9.99999999E8);
        } else if (ruleType == 6) {
            this.setRuleType(6);
            this.setRuleDesc("8 consecutive points below CL");
            this.setTriggerSubgroup(8);
            this.setTotalSubgroup(8);
            this.setSigmaAboveCL(9.99999999E8);
            this.setSigmaBelowCL(0.0);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Rule: \n");
        sb.append("Rule Number         : ").append(this.getRuleNumber()).append("\n");
        sb.append("Rule Name           : ").append(this.getRuleName()).append("\n");
        sb.append("Rule Desc           : ").append(this.getRuleDesc()).append("\n");
        sb.append("Rule Type           : ").append(this.getRuleType(this.getRuleType())).append("\n");
        sb.append("Trigger Subgroup    : ").append(this.getTriggerSubgroup()).append("\n");
        sb.append("Total Subgroup      : ").append(this.getTotalSubgroup()).append("\n");
        sb.append("Sigma above CL      : ").append(this.getSigmaAboveCLStr()).append("\n");
        sb.append("Sigma below CL      : ").append(this.getSigmaBelowCLStr()).append("\n");
        sb.append("Rule Pattern        : ").append(this.getRulePattern(this.getRulePattern())).append("\n");
        return sb.toString();
    }

    public String getRuleName() {
        return this.__RuleName;
    }

    public void setRuleName(String ruleName) {
        this.__RuleName = ruleName;
    }

    public int getRuleNumber() {
        return this.__RuleNumber;
    }

    public void setRuleNumber(int ruleNumber) {
        this.__RuleNumber = ruleNumber;
    }

    public String getRuleDesc() {
        return this.__RuleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.__RuleDesc = ruleDesc;
    }

    public int getRuleType() {
        return this.__RuleType;
    }

    public String getRuleType(int ruletype) {
        String type = "";
        if (ruletype == 0) {
            type = "Custom";
        } else if (ruletype == 1) {
            type = "Western Electric Sandard Rule 1";
        } else if (ruletype == 2) {
            type = "Western Electric Sandard Rule 2";
        } else if (ruletype == 3) {
            type = "Western Electric Sandard Rule 3";
        } else if (ruletype == 4) {
            type = "Western Electric Sandard Rule 4";
        } else if (ruletype == 5) {
            type = "Western Electric Sandard Rule 5";
        } else if (ruletype == 6) {
            type = "Western Electric Sandard Rule 6";
        }
        return type;
    }

    public void setRuleType(int ruleType) {
        this.__RuleType = ruleType;
    }

    public int getRulePattern() {
        return this.__RulePattern;
    }

    public String getRulePattern(int rulepattern) {
        String pattern = "";
        if (rulepattern == 0) {
            pattern = "Not Defined";
        }
        if (rulepattern == 1) {
            pattern = "Increasing";
        }
        if (rulepattern == 2) {
            pattern = "Decreasing";
        }
        if (rulepattern == 3) {
            pattern = "Alternating";
        }
        if (rulepattern == 4) {
            pattern = "Difference";
        }
        if (rulepattern == 5) {
            pattern = "Sameside";
        }
        return pattern;
    }

    public void setRulePattern(int rulePattern) throws RuleException {
        if (rulePattern != 0 && rulePattern != 1 && rulePattern != 2 && rulePattern != 3 && rulePattern != 4 && rulePattern != 5) {
            throw new RuleException("Rule Pattern can only be INCREASING, DECREASING,ALTERNATING,DIFFERENCE or SAMESIDE.");
        }
        this.__RulePattern = rulePattern;
    }

    public double getSigmaAboveCL() {
        return this.__SigmaAboveCL;
    }

    public String getSigmaAboveCLStr() {
        String str = "";
        str = this.getSigmaAboveCL() == 9.99999999E8 ? "DONOTCALCULATE" : Double.toString(this.getSigmaAboveCL());
        return str;
    }

    public void setSigmaAboveCL(double sigmaAboveCL) throws RuleException {
        if (sigmaAboveCL != 9.99999999E8 && sigmaAboveCL <= 0.0) {
            throw new RuleException("SIGMA-ABOVE-CL must be an integer greater than 0.");
        }
        this.__SigmaAboveCL = sigmaAboveCL;
    }

    public double getSigmaBelowCL() {
        return this.__SigmaBelowCL;
    }

    public String getSigmaBelowCLStr() {
        String str = "";
        str = this.getSigmaBelowCL() == 9.99999999E8 ? "DONOTCALCULATE" : Double.toString(this.getSigmaBelowCL());
        return str;
    }

    public void setSigmaBelowCL(double sigmaBelowCL) throws RuleException {
        if (sigmaBelowCL != 9.99999999E8 && sigmaBelowCL <= 0.0) {
            throw new RuleException("SIGMA-BELOW-CL must be an integer greater than 0.");
        }
        this.__SigmaBelowCL = sigmaBelowCL;
    }

    public int getTotalSubgroup() {
        return this.__TotalSubgroup;
    }

    public void setTotalSubgroup(int totalSubgroup) throws RuleException {
        if (totalSubgroup < 0) {
            throw new RuleException("TOTAL-SUBGROUP must be a positive integer.");
        }
        if (this.__TriggerSubgroup > 0 && totalSubgroup < this.__TriggerSubgroup) {
            throw new RuleException("TOTAL-SUBGROUP cannot be less than TRIGGER-SUBGROUP.");
        }
        this.__TotalSubgroup = totalSubgroup;
    }

    public int getTriggerSubgroup() {
        return this.__TriggerSubgroup;
    }

    public void setTriggerSubgroup(int triggerSubgroup) throws RuleException {
        if (triggerSubgroup < 0) {
            throw new RuleException("TRIGGER-SUBGROUP must be a positive integer.");
        }
        if (this.__TotalSubgroup > 0 && this.__TotalSubgroup < triggerSubgroup) {
            throw new RuleException("TOTAL-SUBGROUP cannot be less than TRIGGER-SUBGROUP.");
        }
        this.__TriggerSubgroup = triggerSubgroup;
    }

    public boolean isWithinlimits() {
        return this.__WithinLimits;
    }

    public void setWithinlimits(boolean withinlimits) {
        this.__WithinLimits = withinlimits;
    }

    public int getTriggerlocation() {
        return this.__TriggerLocation;
    }

    public void setTriggerlocation(int parTriggerlocation) {
        this.__TriggerLocation = parTriggerlocation;
    }
}

