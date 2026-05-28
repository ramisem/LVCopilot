/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.replication;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public class EvaluateReplicationRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int targetArrayIndex = 1;
    private int sourceArrayIndex = 1;

    public ArrayList<TransferMap> createReplica(String subOperation, String sourceArrayType, String targetArrayType, boolean transposeFlag, QueryProcessor queryProcessor) throws SapphireException {
        ArrayList<TransferMap> transferMap;
        block12: {
            transferMap = new ArrayList<TransferMap>();
            int[] sourceArrayDims = this.findArrayDimensions(sourceArrayType, queryProcessor);
            int[] targetArrayDims = this.findArrayDimensions(targetArrayType, queryProcessor);
            int sourceArrayRowSize = sourceArrayDims[0];
            int sourceArrayColSize = sourceArrayDims[1];
            int targetArrayRowSize = targetArrayDims[0];
            int targetArrayColSize = targetArrayDims[1];
            if (transposeFlag) {
                if (sourceArrayColSize % targetArrayRowSize != 0) {
                    throw new SapphireException("Invalid properties. To transpose, number of rows on the target array should be equal to number of columns on the source array");
                }
                if (sourceArrayRowSize % targetArrayColSize != 0) {
                    throw new SapphireException("Invalid properties. To transpose, number of columns on the target array should be equal to number of rows on the source array");
                }
            } else {
                if (sourceArrayColSize % targetArrayColSize != 0) {
                    throw new SapphireException("Invalid properties. Number of columns on the target array should be equal to number of columns on the source array");
                }
                if (sourceArrayRowSize % targetArrayRowSize != 0) {
                    throw new SapphireException("Invalid properties. Number of rows on the target array should be equal to number of rows on the source array");
                }
            }
            int srcColIndex = 0;
            int srcRowIndex = 0;
            if (!subOperation.equalsIgnoreCase("stamping")) break block12;
            if (transposeFlag) {
                for (int r = 0; r < targetArrayRowSize; ++r) {
                    srcRowIndex = 0;
                    int c = 0;
                    while (c < targetArrayColSize) {
                        TransferMap wellMap = new TransferMap();
                        wellMap.targetarray = this.targetArrayIndex;
                        wellMap.targetrowindex = r;
                        wellMap.targetcolindex = c;
                        wellMap.sourcearray = this.sourceArrayIndex;
                        wellMap.sourcerowindex = r;
                        wellMap.sourcecolindex = c++;
                        transferMap.add(wellMap);
                        ++srcRowIndex;
                    }
                    ++srcColIndex;
                }
            } else {
                for (int r = 0; r < targetArrayRowSize; ++r) {
                    srcColIndex = 0;
                    int c = 0;
                    while (c < targetArrayColSize) {
                        TransferMap wellMap = new TransferMap();
                        wellMap.targetarray = this.targetArrayIndex;
                        wellMap.targetrowindex = r;
                        wellMap.targetcolindex = c++;
                        wellMap.sourcearray = this.sourceArrayIndex;
                        wellMap.sourcerowindex = srcRowIndex;
                        wellMap.sourcecolindex = srcColIndex++;
                        transferMap.add(wellMap);
                    }
                    ++srcRowIndex;
                }
            }
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

