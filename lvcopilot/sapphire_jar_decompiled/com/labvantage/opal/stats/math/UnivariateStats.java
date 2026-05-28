/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
 */
package com.labvantage.opal.stats.math;

import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class UnivariateStats
extends DescriptiveStatistics {
    static final String LABVANTAGE_CVS_ID = "$Revision: 89929 $";
    protected List _Values = new ArrayList();
    protected List _NoDataList = new ArrayList();
    protected List _XList = new ArrayList();
    protected double _SD = 0.0;
    public static final double NODATA = 9.0E99;

    public void addValue(double value) {
        try {
            this._Values.add(new Double(value));
            if (value != 9.0E99) {
                super.addValue(value);
            }
        }
        catch (Exception e) {
            Trace.log("Exception caught: " + e.getMessage());
        }
    }

    public double getValue(int index) {
        double value = 0.0;
        try {
            value = (Double)this._Values.get(index);
        }
        catch (Exception e) {
            Trace.log("Exception caught: " + e.getMessage());
        }
        return value;
    }

    public void addX(String x) {
        try {
            this._XList.add(this._XList.size(), x);
        }
        catch (Exception e) {
            Trace.log("Exception caught: " + e.getMessage());
        }
    }

    public String getX(int index) {
        if (index > this._XList.size() || index < 0) {
            return null;
        }
        return (String)this._XList.get(index);
    }

    public double getStandardDeviation() {
        this._SD = super.getStandardDeviation();
        return this._SD;
    }

    public static double factorial(double n) {
        if (n == 1.0 || n == 0.0) {
            return 1.0;
        }
        if (n == 0.5) {
            return 0.5 * Math.sqrt(Math.PI);
        }
        return n * UnivariateStats.factorial(n - 1.0);
    }
}

