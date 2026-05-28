/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import com.labvantage.sapphire.util.format.NumericFormatter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import sapphire.util.Logger;

public class IndustryFunctions {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static double sigfig(double value, int scale) {
        double result = 0.0;
        String returnValue = "";
        try {
            returnValue = NumericFormatter.formatNumber(new BigDecimal(String.valueOf(value)), "[sigfig;" + scale + "]");
            if (returnValue.trim().length() > 0) {
                result = NumberFormat.getInstance().parse(returnValue).doubleValue();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return result;
    }

    public static double clporgsigfig(double value, int scale) {
        double result = 0.0;
        String returnValue = "";
        try {
            returnValue = NumericFormatter.formatNumber(new BigDecimal(String.valueOf(value)), "[clporgsigfig;" + scale + "]");
            if (returnValue.trim().length() > 0) {
                result = NumberFormat.getInstance().parse(returnValue).doubleValue();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return result;
    }

    public static double clpinorgsigfig(double value, int scale) {
        double result = 0.0;
        String returnValue = "";
        try {
            returnValue = NumericFormatter.formatNumber(new BigDecimal(String.valueOf(value)), "[clpinorgsigfig;" + scale + "]");
            if (returnValue.trim().length() > 0) {
                result = NumberFormat.getInstance().parse(returnValue).doubleValue();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return result;
    }

    public static double astmround(double value, int scale) {
        double result = 0.0;
        String returnValue = "";
        try {
            returnValue = NumericFormatter.formatNumber(new BigDecimal(String.valueOf(value)), "[astmround;" + scale + "]");
            if (returnValue.trim().length() > 0) {
                result = NumberFormat.getInstance().parse(returnValue).doubleValue();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return result;
    }

    public static double maxsigfigdp(double value, int scale) {
        double result = 0.0;
        String returnValue = "";
        try {
            returnValue = NumericFormatter.formatNumber(new BigDecimal(String.valueOf(value)), "[maxsigfigdp;" + scale + "]");
            if (returnValue.trim().length() > 0) {
                result = NumberFormat.getInstance().parse(returnValue).doubleValue();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return result;
    }
}

