/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.split;

import com.labvantage.sapphire.array.TransferMap;
import com.labvantage.sapphire.array.transfer.algorithm.split.SplitInterspersedUtil;
import com.labvantage.sapphire.array.transfer.algorithm.split.SplitWholeArrayUtil;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class EvaluateSplittingRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    public ArrayList<TransferMap> splitArray(String subOperation, String sourceArrayType, String targetArrayType, boolean transposeFlag, QueryProcessor queryProcessor) throws SapphireException {
        ArrayList<TransferMap> transferMap = new ArrayList<TransferMap>();
        int[] sourceArrayDims = this.findArrayDimensions(sourceArrayType, queryProcessor);
        int[] targetArrayDims = this.findArrayDimensions(targetArrayType, queryProcessor);
        int sourceArrayRowSize = sourceArrayDims[0];
        int sourceArrayColSize = sourceArrayDims[1];
        int targetArrayRowSize = targetArrayDims[0];
        int targetArrayColSize = targetArrayDims[1];
        if (transposeFlag) {
            if (sourceArrayColSize % targetArrayRowSize != 0) {
                throw new SapphireException("Invalid properties. To transpose, number of rows on the source array should be either equal to/multiple of number of columns on the target array");
            }
            if (sourceArrayRowSize % targetArrayColSize != 0) {
                throw new SapphireException("Invalid properties. To transpose, number of columns on the source array should be either equal to/multiple of number of rows on the target array");
            }
            if (targetArrayColSize == sourceArrayRowSize && targetArrayRowSize == sourceArrayColSize) {
                throw new SapphireException("Invalid properties. The source array type and the target array type combination is not valid input for Splitting in transpose mode.");
            }
        } else {
            if (sourceArrayColSize % targetArrayColSize != 0) {
                throw new SapphireException("Invalid properties. Number of columns on the source array should be either equal to/multiple of number of columns on the target array");
            }
            if (sourceArrayRowSize % targetArrayRowSize != 0) {
                throw new SapphireException("Invalid properties. Number of rows on the source array should be either equal to/multiple of number of rows on the target array");
            }
            if (targetArrayColSize == sourceArrayColSize && targetArrayRowSize == sourceArrayRowSize) {
                throw new SapphireException("Invalid properties. The source array type and the target array type combination is not valid input for Splitting.");
            }
        }
        if (subOperation.equalsIgnoreCase("WholeArray")) {
            SplitWholeArrayUtil splitWholeArrayUtil = new SplitWholeArrayUtil();
            transferMap = splitWholeArrayUtil.split(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, transposeFlag);
        } else if (subOperation.equalsIgnoreCase("Interspersed")) {
            int noOfColsInSS = 0;
            int noOfRowsInSS = 0;
            if (transposeFlag) {
                noOfColsInSS = sourceArrayRowSize / targetArrayColSize;
                noOfRowsInSS = sourceArrayColSize / targetArrayRowSize;
            } else {
                noOfColsInSS = sourceArrayColSize / targetArrayColSize;
                noOfRowsInSS = sourceArrayRowSize / targetArrayRowSize;
            }
            SplitInterspersedUtil splitInterspersedUtil = new SplitInterspersedUtil();
            transferMap = splitInterspersedUtil.split(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transposeFlag);
        }
        return transferMap;
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

