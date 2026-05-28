/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.condense;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;

public class CondenseSourceRowsUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int targetArrayIndex = 1;
    private int sourceArrayIndex = 1;
    private int sourceRowIndex = 0;
    private int sourceColIndex = 0;
    private int targetRowIndex = 0;

    public ArrayList<TransferMap> condense(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, boolean transpose) {
        if (transpose) {
            int noOfSrcColsToFitOnTargetRow = targetArrayColSize / sourceArrayRowSize;
            int noOfTargetRowsForSource = sourceArrayColSize / noOfSrcColsToFitOnTargetRow;
            int noOfSourceArraysToFitOnTarget = targetArrayColSize * targetArrayRowSize / (sourceArrayColSize * sourceArrayRowSize);
            for (int i = 0; i < noOfSourceArraysToFitOnTarget; ++i) {
                transferMap = this.populateTargetSSWithSourceCols(transferMap, noOfTargetRowsForSource, noOfSrcColsToFitOnTargetRow, sourceArrayRowSize, targetArrayColSize);
                ++this.sourceArrayIndex;
                this.sourceColIndex = 0;
            }
        } else {
            int noOfSrcRowsToFitOnTarget = targetArrayColSize / sourceArrayColSize;
            int noOfTargetRowsForSource = sourceArrayRowSize / noOfSrcRowsToFitOnTarget;
            int noOfSourceArraysToFitOnTarget = targetArrayColSize * targetArrayRowSize / (sourceArrayColSize * sourceArrayRowSize);
            for (int i = 0; i < noOfSourceArraysToFitOnTarget; ++i) {
                transferMap = this.populateSourceArrayOnTarget(transferMap, noOfTargetRowsForSource, noOfSrcRowsToFitOnTarget, sourceArrayColSize, targetArrayColSize);
                ++this.sourceArrayIndex;
                this.sourceRowIndex = 0;
            }
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateTargetSSWithSourceCols(ArrayList<TransferMap> transferMap, int noOfTargetRowsForSource, int noOfSrcColsToFitOnTargetRow, int sourceArrayRowSize, int targetArrayColSize) {
        for (int i = 0; i < noOfTargetRowsForSource; ++i) {
            transferMap = this.populateTargetRowWithSourceCol(transferMap, noOfSrcColsToFitOnTargetRow, sourceArrayRowSize, targetArrayColSize);
            ++this.targetRowIndex;
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateTargetRowWithSourceCol(ArrayList<TransferMap> transferMap, int noOfSrcColsToFitOnTargetRow, int sourceArrayRowSize, int targetArrayColSize) {
        int cIndex = 0;
        while (cIndex < targetArrayColSize) {
            for (int c = 0; c < noOfSrcColsToFitOnTargetRow; ++c) {
                int r = 0;
                while (r < sourceArrayRowSize) {
                    TransferMap wellMap = new TransferMap();
                    wellMap.targetarray = this.targetArrayIndex;
                    wellMap.targetrowindex = this.targetRowIndex;
                    wellMap.targetcolindex = cIndex++;
                    wellMap.sourcearray = this.sourceArrayIndex;
                    wellMap.sourcerowindex = r++;
                    wellMap.sourcecolindex = this.sourceColIndex;
                    transferMap.add(wellMap);
                }
                ++this.sourceColIndex;
            }
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateSourceArrayOnTarget(ArrayList<TransferMap> transferMap, int noOfTargetRowsForSource, int noOfSrcRowsToFitOnTarget, int sourceArrayColSize, int targetArrayColSize) {
        for (int i = 0; i < noOfTargetRowsForSource; ++i) {
            transferMap = this.populateTargetRow(transferMap, noOfSrcRowsToFitOnTarget, sourceArrayColSize, targetArrayColSize);
            ++this.targetRowIndex;
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateTargetRow(ArrayList<TransferMap> transferMap, int noOfSrcRowsToFitOnTarget, int sourceArrayColSize, int targetArrayColSize) {
        int cIndex = 0;
        while (cIndex < targetArrayColSize) {
            for (int r = 0; r < noOfSrcRowsToFitOnTarget; ++r) {
                int c = 0;
                while (c < sourceArrayColSize) {
                    TransferMap wellMap = new TransferMap();
                    wellMap.targetarray = this.targetArrayIndex;
                    wellMap.targetrowindex = this.targetRowIndex;
                    wellMap.targetcolindex = cIndex++;
                    wellMap.sourcearray = this.sourceArrayIndex;
                    wellMap.sourcerowindex = this.sourceRowIndex;
                    wellMap.sourcecolindex = c++;
                    transferMap.add(wellMap);
                }
                ++this.sourceRowIndex;
            }
        }
        return transferMap;
    }
}

