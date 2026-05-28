/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.evaluator;

import com.labvantage.sapphire.util.evaluator.ExpressionParam;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Random;
import sapphire.SapphireException;
import sapphire.util.Logger;

public class CalcFunctions {
    public static String external(String className, String methodName, Object[] objArg) {
        try {
            Class<?> c = Class.forName(className);
            Method[] theMethods = c.getMethods();
            for (int i = 0; i < theMethods.length; ++i) {
                String methodString = theMethods[i].getName();
                if (!methodString.equals(methodName)) continue;
                try {
                    Object o = theMethods[i].invoke(null, objArg);
                    return o.toString();
                }
                catch (Exception e) {
                    // empty catch block
                }
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return "";
    }

    public static double sum(ExpressionParam ep) {
        BigDecimal sum = new BigDecimal(0);
        BigDecimal[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
            if (nlist.length == 0) {
                return 1 / nlist.length;
            }
            for (int i = 0; i < nlist.length; ++i) {
                sum = sum.add(nlist[i]);
            }
            return sum.doubleValue();
        }
        return Double.NaN;
    }

    public static double avg(ExpressionParam ep) {
        BigDecimal[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
            double avg = CalcFunctions.sum(ep) / (double)nlist.length;
            return avg;
        }
        return Double.NaN;
    }

    public static double count(ExpressionParam ep) {
        Object[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
        } else if (ep.getType() == 2) {
            nlist = ep.getStringValues();
        } else if (ep.getType() == 1) {
            nlist = ep.getDateValues();
        }
        return nlist.length == 0 ? 1 / nlist.length : nlist.length;
    }

    public static Object first(ExpressionParam ep) {
        Object[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
        } else if (ep.getType() == 2) {
            nlist = ep.getStringValues();
        } else if (ep.getType() == 1) {
            nlist = ep.getDateValues();
        }
        return nlist[0];
    }

    public static Object last(ExpressionParam ep) {
        Object[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
        } else if (ep.getType() == 2) {
            nlist = ep.getStringValues();
        } else if (ep.getType() == 1) {
            nlist = ep.getDateValues();
        }
        return nlist[nlist.length - 1];
    }

    public static Object max(ExpressionParam ep) {
        Object[] values;
        int type = ep.getType();
        Object omax = new Object();
        if (type == 0) {
            values = ep.getNumberValues();
            double max = values[0].doubleValue();
            for (int i = 0; i < values.length; ++i) {
                max = Math.max(max, ((BigDecimal)values[i]).doubleValue());
            }
            omax = new Double(max);
        }
        if (type == 2) {
            values = ep.getStringValues();
            Object max = values[0];
            for (int i = 0; i < values.length; ++i) {
                max = ((String)max).compareTo((String)values[i]) > 0 ? max : values[i];
            }
            omax = max;
        }
        if (type == 1) {
            values = ep.getDateValues();
            Object max = values[0];
            long maxtime = ((Calendar)max).getTime().getTime();
            for (int i = 0; i < values.length; ++i) {
                max = Math.max(maxtime, ((Calendar)values[i]).getTime().getTime()) == maxtime ? max : values[i];
            }
            omax = max;
        }
        return omax;
    }

    public static Object min(ExpressionParam ep) {
        Object[] values;
        int type = ep.getType();
        Object omin = new Object();
        if (type == 0) {
            values = ep.getNumberValues();
            double min = values[0].doubleValue();
            for (int i = 0; i < values.length; ++i) {
                min = Math.min(min, ((BigDecimal)values[i]).doubleValue());
            }
            omin = new Double(min);
        }
        if (type == 2) {
            values = ep.getStringValues();
            Object min = values[0];
            for (int i = 0; i < values.length; ++i) {
                min = ((String)min).compareTo((String)values[i]) < 0 ? min : values[i];
            }
            omin = min;
        }
        if (type == 1) {
            values = ep.getDateValues();
            Object min = values[0];
            long mintime = ((Calendar)min).getTime().getTime();
            for (int i = 0; i < values.length; ++i) {
                min = Math.min(mintime, ((Calendar)values[i]).getTime().getTime()) == mintime ? min : values[i];
            }
            omin = min;
        }
        return omin;
    }

    public static double mode(ExpressionParam ep) {
        BigDecimal[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
            double[] sortList = CalcFunctions.sort(nlist);
            double max_cnt = 0.0;
            double count = 0.0;
            double mode = 0.0;
            for (int i = 0; i < sortList.length; ++i) {
                count = 0.0;
                for (int j = i + 1; j < sortList.length; ++j) {
                    if (sortList[i] != sortList[j]) continue;
                    count += 1.0;
                }
                if (!(count > max_cnt)) continue;
                max_cnt = count;
                mode = sortList[i];
            }
            return mode;
        }
        return Double.NaN;
    }

    public static double median(ExpressionParam ep) {
        BigDecimal[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
            double[] sort = CalcFunctions.sort(nlist);
            boolean EVEN_NUMBER_OF_ELEMENTS = false;
            int result = sort.length % 2;
            double median = 0.0;
            if (sort.length == 1) {
                median = sort[0];
            } else if (result == 0) {
                int rightNumber = sort.length / 2;
                int leftNumber = rightNumber - 1;
                median = (sort[rightNumber] + sort[leftNumber]) / 2.0;
            } else {
                int rightNumber = sort.length / 2;
                median = sort[rightNumber];
            }
            return median;
        }
        return Double.NaN;
    }

    public static double[] sort(BigDecimal[] nlist) {
        double[] sort = new double[nlist.length];
        sort[0] = nlist[0].doubleValue();
        for (int i = 0; i < nlist.length; ++i) {
            sort[i] = nlist[i].doubleValue();
            for (int j = i + 1; j < nlist.length; ++j) {
                sort[i] = Math.min(sort[i], nlist[j].doubleValue());
            }
        }
        return sort;
    }

    public static double stdev(ExpressionParam ep) {
        BigDecimal[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
            double stdev = 0.0;
            double sum2 = 0.0;
            double sum = 0.0;
            if (nlist.length == 0) {
                return 1 / nlist.length;
            }
            for (int i = 0; i < nlist.length; ++i) {
                sum2 += Math.pow(nlist[i].doubleValue(), 2.0);
                sum += nlist[i].doubleValue();
            }
            stdev = Math.sqrt((sum2 * (double)nlist.length - Math.pow(sum, 2.0)) / (double)(nlist.length * (nlist.length - 1)));
            if (new Double(stdev).isNaN()) {
                stdev = 0.0;
            }
            return stdev;
        }
        return Double.NaN;
    }

    public static double var(ExpressionParam ep) {
        BigDecimal[] nlist = null;
        if (ep.getType() == 0) {
            nlist = ep.getNumberValues();
            double stdev = 0.0;
            double sum2 = 0.0;
            double sum = 0.0;
            if (nlist.length == 0) {
                return 1 / nlist.length;
            }
            for (int i = 0; i < nlist.length; ++i) {
                sum2 += Math.pow(nlist[i].doubleValue(), 2.0);
                sum += nlist[i].doubleValue();
            }
            stdev = (sum2 * (double)nlist.length - Math.pow(sum, 2.0)) / (double)(nlist.length * (nlist.length - 1));
            if (new Double(stdev).isNaN()) {
                stdev = 0.0;
            }
            return stdev;
        }
        return Double.NaN;
    }

    public static double factorial(Double a) {
        double result = 0.0;
        String s = a.toString();
        int idx = s.indexOf(".");
        if (idx != -1) {
            Double d = new Double(s.substring(idx));
            if (d > 0.0) {
                result = 0.0;
                return result;
            }
        }
        double n = a;
        result = 1.0;
        if (n < 0.0) {
            result = 0.0;
        }
        if (n == 0.0) {
            result = 1.0;
        }
        while (0.0 < n) {
            result *= n;
            n -= 1.0;
        }
        a = new Double(result);
        if (a.toString().equals("Infinity")) {
            a = new Double(0.0);
        }
        return a;
    }

    public static double exp(Double a) {
        if ((a = new Double(Math.exp(a))).toString().equals("Infinity")) {
            a = new Double(0.0);
        }
        return a;
    }

    public static double log(Double a) throws SapphireException {
        if (a != null) {
            if (a > 0.0) {
                if ((a = new Double(Math.log(a))).toString().equals("NaN")) {
                    a = new Double(0.0);
                }
            } else {
                throw new SapphireException("- Infinity");
            }
        }
        return a;
    }

    public static double mod(Double a, Double b) {
        if ((a = new Double(a % b)) == 0.0) {
            a = new Double(0.0);
        }
        return a;
    }

    public static double random(Double a) {
        Random randomgen = new Random();
        if (a < 0.0) {
            a = new Double(0.0);
        } else if ((a = new Double(randomgen.nextInt(a.intValue() + 1))).intValue() == 0) {
            a = new Double(1.0);
        }
        return a;
    }

    public static double round(Double a, Double b) {
        double factor = Math.pow(10.0, b.intValue());
        double a1 = a;
        int scaler = 12 - (int)Math.log10(Math.abs(a1 *= factor));
        if (scaler < 0) {
            scaler = 0;
        }
        double adjust = Math.pow(10.0, -1 * scaler);
        if (a1 > 0.0) {
            a1 += adjust;
        } else if (a1 < 0.0) {
            a1 -= adjust;
        }
        long tmp = Math.round(a1);
        return (double)tmp / factor;
    }

    public static double sign(Double a) {
        double aa = a;
        a = aa < -1.0E-15 ? new Double(-1.0) : (aa > 1.0E-15 ? new Double(1.0) : new Double(0.0));
        return a;
    }

    public static double truncate(Double a, Double b) {
        String trimmedPoint;
        String beforePoint;
        String s = new BigDecimal(a.toString()).stripTrailingZeros().toPlainString();
        String afterPoint = "";
        if (s.indexOf(".") >= 0) {
            beforePoint = s.substring(0, s.indexOf("."));
            afterPoint = s.substring(s.indexOf(".") + 1, s.length());
            trimmedPoint = b.intValue() <= afterPoint.length() ? afterPoint.substring(0, b.intValue()) : afterPoint;
        } else {
            beforePoint = s;
            trimmedPoint = "";
        }
        return new Double(beforePoint + "." + trimmedPoint);
    }

    public static double logten(Double a) {
        if (a > 0.0) {
            if ((a = new Double(Math.log(a))).toString().equals("NaN")) {
                a = new Double(0.0);
            } else {
                Double b = new Double(Math.log(10.0));
                a = new Double(a / b);
            }
        } else {
            a = new Double(0.0);
        }
        return a;
    }

    public static String color(double a) {
        if (a == 1.0) {
            return "RED";
        }
        if (a == 2.0) {
            return "GREEN";
        }
        if (a == 3.0) {
            return "BLUE";
        }
        return "Error";
    }
}

