/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.stats;

import com.labvantage.opal.stats.Stats;
import java.util.ArrayList;

public class StatsList
extends ArrayList {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String __StatsListParamid;
    private double __OverallMax = 9.0E99;
    private double __OverallMin = 9.0E99;
    private double __OverallSD = 9.0E99;
    private double __OverallCL = 9.0E99;
    private double __OverallUCL = 9.0E99;
    private double __OverallLCL = 9.0E99;

    public void setStatsListParamid(String paramid) {
        this.__StatsListParamid = paramid;
    }

    public String getStatsListParamid() {
        return this.__StatsListParamid;
    }

    public double getOverallMax() {
        if (this.size() > 0) {
            double max = ((Stats)((Object)this.get(0))).getMax();
            if (this.__OverallMax == 9.0E99) {
                for (int i = 1; i < this.size(); ++i) {
                    Stats stats = (Stats)((Object)this.get(i));
                    if (!(stats.getMax() > max)) continue;
                    max = stats.getMax();
                }
                this.__OverallMax = max;
            }
        }
        return this.__OverallMax;
    }

    public void setOverallMax(double max) {
        this.__OverallMax = max;
    }

    public double getOverallMin() {
        if (this.size() > 0) {
            double min = ((Stats)((Object)this.get(0))).getMin();
            if (this.__OverallMin == 9.0E99) {
                for (int i = 1; i < this.size(); ++i) {
                    Stats stats = (Stats)((Object)this.get(i));
                    if (!(stats.getMin() < min)) continue;
                    min = stats.getMin();
                }
                this.__OverallMin = min;
            }
        }
        return this.__OverallMin;
    }

    public void setOverallMin(double min) {
        this.__OverallMin = min;
    }

    public double getOverallSD() {
        double sd = 0.0;
        if (this.__OverallSD == 9.0E99) {
            for (int i = 0; i < this.size(); ++i) {
                Stats stats = (Stats)((Object)this.get(i));
                sd += stats.getSD();
            }
            this.__OverallSD = sd / (double)this.size();
        }
        return this.__OverallSD;
    }

    public void setOverallSD(double sd) {
        this.__OverallSD = sd;
    }

    public double getOverallCL() {
        double cl = 0.0;
        if (this.__OverallCL == 9.0E99) {
            for (int i = 0; i < this.size(); ++i) {
                Stats stats = (Stats)((Object)this.get(i));
                cl += stats.getCL();
            }
            this.__OverallCL = cl / (double)this.size();
        }
        return this.__OverallCL;
    }

    public void setOverallCL(double cl) {
        this.__OverallCL = cl;
    }

    public double getOverallUCL() {
        double ucl = 0.0;
        if (this.__OverallUCL == 9.0E99) {
            for (int i = 0; i < this.size(); ++i) {
                Stats stats = (Stats)((Object)this.get(i));
                ucl += stats.getUCL();
            }
            this.__OverallUCL = ucl / (double)this.size();
        }
        return this.__OverallUCL;
    }

    public void setOverallUCL(double ucl) {
        this.__OverallUCL = ucl;
    }

    public double getOverallLCL() {
        double lcl = 0.0;
        if (this.__OverallLCL == 9.0E99) {
            for (int i = 0; i < this.size(); ++i) {
                Stats stats = (Stats)((Object)this.get(i));
                lcl += stats.getLCL();
            }
            this.__OverallLCL = lcl / (double)this.size();
        }
        return this.__OverallLCL;
    }

    public void setOverallLCL(double lcl) {
        this.__OverallLCL = lcl;
    }
}

