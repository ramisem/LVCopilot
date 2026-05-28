/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.data.xy.XYDataset
 */
package com.labvantage.sapphire.pageelements.datachart.util;

import com.labvantage.sapphire.I18nUtil;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jfree.data.xy.XYDataset;
import sapphire.util.DataSet;

public final class Util {
    public static final String DELIMITER = ";";

    private Util() {
    }

    public static Set<String> getColumnValueList(DataSet dataSet, String datasetIdColumn) {
        String columnValues = dataSet.getColumnValues(datasetIdColumn, DELIMITER);
        return new HashSet<String>(Arrays.asList(columnValues.split(DELIMITER)));
    }

    public static ArrayList<String> getOrderedColumnValueList(DataSet dataSet, String datasetIdColumn) {
        String columnValues = dataSet.getColumnValues(datasetIdColumn, DELIMITER);
        ArrayList<String> returnList = new ArrayList<String>();
        List<String> strings = Arrays.asList(columnValues.split(DELIMITER));
        for (String item : strings) {
            if (returnList.contains(item)) continue;
            returnList.add(item);
        }
        return returnList;
    }

    public static double[] getPolynomialRegression(XYDataset dataset, int series, int order) {
        double rSquare;
        int coe;
        int eq;
        if (dataset == null) {
            throw new IllegalArgumentException("Null 'dataset' argument.");
        }
        int itemCount = dataset.getItemCount(series);
        if (itemCount < order + 1) {
            throw new IllegalArgumentException("Not enough data.");
        }
        int validItems = 0;
        double[][] data = new double[2][itemCount];
        for (int item = 0; item < itemCount; ++item) {
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);
            if (Double.isNaN(x) || Double.isNaN(y)) continue;
            data[0][validItems] = x;
            data[1][validItems] = y;
            ++validItems;
        }
        if (validItems < order + 1) {
            throw new IllegalArgumentException("Not enough data.");
        }
        int equations = order + 1;
        int coefficients = order + 2;
        double[] result = new double[equations + 1];
        double[][] matrix = new double[equations][coefficients];
        double sumX = 0.0;
        double sumY = 0.0;
        for (int item = 0; item < validItems; ++item) {
            sumX += data[0][item];
            sumY += data[1][item];
            for (eq = 0; eq < equations; ++eq) {
                for (coe = 0; coe < coefficients - 1; ++coe) {
                    double[] dArray = matrix[eq];
                    int n = coe;
                    dArray[n] = dArray[n] + Math.pow(data[0][item], eq + coe);
                }
                double[] dArray = matrix[eq];
                int n = coefficients - 1;
                dArray[n] = dArray[n] + data[1][item] * Math.pow(data[0][item], eq);
            }
        }
        double[][] subMatrix = Util.calculateSubMatrix(matrix);
        for (eq = 1; eq < equations; ++eq) {
            matrix[eq][0] = 0.0;
            for (coe = 1; coe < coefficients; ++coe) {
                matrix[eq][coe] = subMatrix[eq - 1][coe - 1];
            }
        }
        for (eq = equations - 1; eq > -1; --eq) {
            double value = matrix[eq][coefficients - 1];
            for (int coe2 = eq; coe2 < coefficients - 1; ++coe2) {
                value -= matrix[eq][coe2] * result[coe2];
            }
            result[eq] = value / matrix[eq][eq];
        }
        double meanY = sumY / (double)validItems;
        double yObsSquare = 0.0;
        double yRegSquare = 0.0;
        for (int item = 0; item < validItems; ++item) {
            double yCalc = 0.0;
            for (int eq2 = 0; eq2 < equations; ++eq2) {
                yCalc += result[eq2] * Math.pow(data[0][item], eq2);
            }
            yRegSquare += Math.pow(yCalc - meanY, 2.0);
            yObsSquare += Math.pow(data[1][item] - meanY, 2.0);
        }
        result[equations] = rSquare = yRegSquare / yObsSquare;
        return result;
    }

    private static double[][] calculateSubMatrix(double[][] matrix) {
        int equations = matrix.length;
        int coefficients = matrix[0].length;
        double[][] result = new double[equations - 1][coefficients - 1];
        for (int eq = 1; eq < equations; ++eq) {
            double factor = matrix[0][0] / matrix[eq][0];
            for (int coe = 1; coe < coefficients; ++coe) {
                result[eq - 1][coe - 1] = matrix[0][coe] - matrix[eq][coe] * factor;
            }
        }
        if (equations == 1) {
            return result;
        }
        if (result[0][0] == 0.0) {
            boolean found = false;
            for (int i = 0; i < result.length; ++i) {
                int j;
                if (result[i][0] == 0.0) continue;
                found = true;
                double[] temp = result[0];
                for (j = 0; j < result[i].length; ++j) {
                    result[0][j] = result[i][j];
                }
                for (j = 0; j < temp.length; ++j) {
                    result[i][j] = temp[j];
                }
                break;
            }
            if (!found) {
                throw new IllegalStateException("Equation has no solution");
            }
        }
        double[][] subMatrix = Util.calculateSubMatrix(result);
        for (int eq = 1; eq < equations - 1; ++eq) {
            result[eq][0] = 0.0;
            for (int coe = 1; coe < coefficients - 1; ++coe) {
                result[eq][coe] = subMatrix[eq - 1][coe - 1];
            }
        }
        return result;
    }

    private static ArrayList getValuesLessThan(ArrayList<Number> values, double limit, boolean orEqualTo) {
        ArrayList<Number> modValues = new ArrayList<Number>();
        for (Number value : values) {
            if (!(value.doubleValue() < limit) && (value.doubleValue() != limit || !orEqualTo)) continue;
            modValues.add(value);
        }
        return modValues;
    }

    public static double median(List<Number> values) {
        ArrayList<BigDecimal> bigDecimalValues = new ArrayList<BigDecimal>();
        for (int i = 0; i < values.size(); ++i) {
            BigDecimal d = BigDecimal.valueOf(values.get(i).doubleValue());
            bigDecimalValues.add(d);
        }
        Collections.sort(bigDecimalValues);
        if (values.size() % 2 == 1) {
            return ((BigDecimal)bigDecimalValues.get((bigDecimalValues.size() + 1) / 2 - 1)).doubleValue();
        }
        double lower = ((BigDecimal)bigDecimalValues.get(bigDecimalValues.size() / 2 - 1)).doubleValue();
        double upper = ((BigDecimal)bigDecimalValues.get(bigDecimalValues.size() / 2)).doubleValue();
        return (lower + upper) / 2.0;
    }

    public static BigDecimal parseBigDecimal(String numberStr) throws ParseException {
        BigDecimal returnValue = null;
        NumberFormat numberFormat = NumberFormat.getInstance(I18nUtil.getSysLocale());
        returnValue = BigDecimal.valueOf(numberFormat.parse(numberStr).doubleValue());
        return returnValue;
    }

    public static Integer parseInteger(String numberStr) throws ParseException {
        Integer returnValue = null;
        NumberFormat numberFormat = NumberFormat.getInstance(I18nUtil.getSysLocale());
        returnValue = numberFormat.parse(numberStr).intValue();
        return returnValue;
    }

    public static String getUserChartCacheId(String chartId, String connectionId) {
        return chartId + DELIMITER + connectionId;
    }

    public static void populateKeyLists(DataSet dataSet, List<String> keyId1List, List<String> keyId2List, List<String> keyId3List, String sdcId, String keyId1Column, String keyId2Column, String keyId3Column) {
        ArrayList<String> addedKeys = new ArrayList<String>();
        for (int i = 0; i < dataSet.getRowCount(); ++i) {
            String keyId1 = dataSet.getString(i, keyId1Column);
            String keyId2 = dataSet.getString(i, keyId2Column, "(null)");
            String keyId3 = dataSet.getString(i, keyId3Column, "(null)");
            if (keyId1.isEmpty()) {
                throw new IllegalArgumentException("Key ID 1 is empty in column " + keyId1 + " and row " + i);
            }
            String key = sdcId + keyId1 + keyId2 + keyId3;
            if (addedKeys.contains(key)) continue;
            addedKeys.add(key);
            keyId1List.add(keyId1);
            keyId2List.add(keyId2);
            keyId3List.add(keyId3);
        }
    }
}

