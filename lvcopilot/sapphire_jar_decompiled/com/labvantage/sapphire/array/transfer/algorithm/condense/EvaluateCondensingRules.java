/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.condense;

import com.labvantage.sapphire.array.TransferMap;
import com.labvantage.sapphire.array.transfer.algorithm.condense.CondenseSourceColsUtil;
import com.labvantage.sapphire.array.transfer.algorithm.condense.CondenseSourceRowsUtil;
import com.labvantage.sapphire.array.transfer.algorithm.condense.CondenseWholeArrayUtil;
import com.labvantage.sapphire.array.transfer.algorithm.condense.InterspersedArrayUtil;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class EvaluateCondensingRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int sourceArrayIndex = 1;
    private int targetArrayIndex = 1;

    public ArrayList<TransferMap> condenseArray(String subOperation, String sourceArrayType, String targetArrayType, String direction, String startPos, boolean transpose, QueryProcessor queryProcessor) throws SapphireException {
        ArrayList<TransferMap> transferMap = new ArrayList<TransferMap>();
        int[] sourceArrayDims = this.findArrayDimensions(sourceArrayType, queryProcessor);
        int[] targetArrayDims = this.findArrayDimensions(targetArrayType, queryProcessor);
        int sourceArrayRowSize = sourceArrayDims[0];
        int sourceArrayColSize = sourceArrayDims[1];
        int targetArrayRowSize = targetArrayDims[0];
        int targetArrayColSize = targetArrayDims[1];
        if (transpose) {
            if (targetArrayColSize % sourceArrayRowSize != 0) {
                throw new SapphireException("Invalid properties. To transpose, number of rows on the target array should be either equal to/multiple of number of columns on the source array");
            }
            if (targetArrayRowSize % sourceArrayColSize != 0) {
                throw new SapphireException("Invalid properties. To transpose, number of columns on the target array should be either equal to/multiple of number of rows on the source array");
            }
            if (targetArrayColSize == sourceArrayRowSize && targetArrayRowSize == sourceArrayColSize) {
                throw new SapphireException("Invalid properties. The source array type and the target array type combination is not valid input for Condensing in transpose mode.");
            }
        } else {
            if (targetArrayColSize % sourceArrayColSize != 0) {
                throw new SapphireException("Invalid properties. Number of columns on the target array should be either equal to/multiple of number of columns on the source array");
            }
            if (targetArrayRowSize % sourceArrayRowSize != 0) {
                throw new SapphireException("Invalid properties. Number of rows on the target array should be either equal to/multiple of number of rows on the source array");
            }
            if (targetArrayColSize == sourceArrayColSize && targetArrayRowSize == sourceArrayRowSize) {
                throw new SapphireException("Invalid properties. The source array type and the target array type combination is not valid input for Condensing.");
            }
        }
        if (subOperation.equalsIgnoreCase("WholeArray")) {
            int rowIndex = 0;
            int colIndex = 0;
            if (direction == null || direction.length() == 0) {
                direction = "clockwise";
            }
            if (direction.equals("clockwise") || direction.equals("anticlockwise")) {
                int[] startIndexDims = this.findArrayIndex(startPos, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, transpose);
                rowIndex = startIndexDims[0];
                colIndex = startIndexDims[1];
            } else if (direction.equals("horizontal") || direction.equals("vertical")) {
                rowIndex = 0;
                colIndex = 0;
            }
            CondenseWholeArrayUtil condenseWholeArrayUtil = new CondenseWholeArrayUtil();
            transferMap = condenseWholeArrayUtil.condense(startPos, direction, transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
        } else if (subOperation.equalsIgnoreCase("interspersed")) {
            int noOfColsInSS = 0;
            int noOfRowsInSS = 0;
            if (transpose) {
                noOfColsInSS = targetArrayColSize / sourceArrayRowSize;
                noOfRowsInSS = targetArrayRowSize / sourceArrayColSize;
            } else {
                noOfColsInSS = targetArrayColSize / sourceArrayColSize;
                noOfRowsInSS = targetArrayRowSize / sourceArrayRowSize;
            }
            int targetRowIndex = 0;
            int targetColIndex = 0;
            if (direction == null || direction.length() == 0) {
                direction = "clockwise";
            }
            if (direction.equals("clockwise") || direction.equals("anticlockwise")) {
                int[] startIndexDims = this.findArrayIndex(startPos, 1, 1, noOfRowsInSS, noOfColsInSS, transpose);
                targetRowIndex = startIndexDims[0];
                targetColIndex = startIndexDims[1];
            } else if (direction.equals("horizontal") || direction.equals("vertical")) {
                targetRowIndex = 0;
                targetColIndex = 0;
            }
            InterspersedArrayUtil interspersedArrayUtil = new InterspersedArrayUtil();
            transferMap = interspersedArrayUtil.condense(startPos, direction, transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
        } else if (subOperation.equalsIgnoreCase("CondenseSourceRows")) {
            CondenseSourceRowsUtil condenseSourceRowsUtil = new CondenseSourceRowsUtil();
            transferMap = condenseSourceRowsUtil.condense(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, transpose);
        } else if (subOperation.equalsIgnoreCase("CondenseSourceColumns")) {
            CondenseSourceColsUtil condenseSourceColsUtil = new CondenseSourceColsUtil();
            transferMap = condenseSourceColsUtil.condense(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, transpose);
        }
        return transferMap;
    }

    private int[] findArrayIndex(String startPos, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, boolean transpose) {
        int[] startIndexDims = new int[2];
        if (transpose) {
            if (startPos.equals("topleft")) {
                startIndexDims[0] = 0;
                startIndexDims[1] = 0;
            } else if (startPos.equals("topright")) {
                startIndexDims[0] = 0;
                startIndexDims[1] = targetArrayColSize - sourceArrayRowSize;
            } else if (startPos.equals("bottomleft")) {
                startIndexDims[0] = targetArrayRowSize - sourceArrayColSize;
                startIndexDims[1] = 0;
            } else if (startPos.equals("bottomright")) {
                startIndexDims[0] = targetArrayRowSize - sourceArrayColSize;
                startIndexDims[1] = targetArrayColSize - sourceArrayRowSize;
            }
        } else if (startPos.equals("topleft")) {
            startIndexDims[0] = 0;
            startIndexDims[1] = 0;
        } else if (startPos.equals("topright")) {
            startIndexDims[0] = 0;
            startIndexDims[1] = targetArrayColSize - sourceArrayColSize;
        } else if (startPos.equals("bottomleft")) {
            startIndexDims[0] = targetArrayRowSize - sourceArrayRowSize;
            startIndexDims[1] = 0;
        } else if (startPos.equals("bottomright")) {
            startIndexDims[0] = targetArrayRowSize - sourceArrayRowSize;
            startIndexDims[1] = targetArrayColSize - sourceArrayColSize;
        }
        return startIndexDims;
    }

    private int[] findArrayDimensions(String arrayType, QueryProcessor queryProcessor) {
        int[] arrayDim = new int[2];
        String sql = "SELECT numrows, numcolumns FROM arraytype WHERE arraytypeid =?";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{arrayType});
        arrayDim[0] = ds.getInt(0, "numrows");
        arrayDim[1] = ds.getInt(0, "numcolumns");
        return arrayDim;
    }
}

