/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.condense;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;

public class CondenseSourceColsUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int targetArrayIndex = 1;
    private int sourceArrayIndex = 1;
    private int sourceRowIndex = 0;
    private int sourceColIndex = 0;
    private int targetColIndex = 0;

    public ArrayList<TransferMap> condense(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, boolean transpose) {
        if (transpose) {
            int noOfSrcRowsToFitOnTargetCol = targetArrayRowSize / sourceArrayColSize;
            int noOfTargetColsForSource = sourceArrayRowSize / noOfSrcRowsToFitOnTargetCol;
            int noOfSourceArraysToFitOnTarget = targetArrayColSize * targetArrayRowSize / (sourceArrayColSize * sourceArrayRowSize);
            for (int i = 0; i < noOfSourceArraysToFitOnTarget; ++i) {
                transferMap = this.populateTargetSSWithSourceCols(transferMap, noOfTargetColsForSource, noOfSrcRowsToFitOnTargetCol, sourceArrayColSize, targetArrayRowSize);
                ++this.sourceArrayIndex;
                this.sourceRowIndex = 0;
            }
        } else {
            int noOfSrcColsToFitOnTarget = targetArrayRowSize / sourceArrayRowSize;
            int noOfTargetColsForSource = sourceArrayColSize / noOfSrcColsToFitOnTarget;
            int noOfSourceArraysToFitOnTarget = targetArrayColSize * targetArrayRowSize / (sourceArrayColSize * sourceArrayRowSize);
            for (int i = 0; i < noOfSourceArraysToFitOnTarget; ++i) {
                transferMap = this.populateSourceArrayOnTarget(transferMap, noOfTargetColsForSource, noOfSrcColsToFitOnTarget, sourceArrayRowSize, targetArrayRowSize);
                ++this.sourceArrayIndex;
                this.sourceColIndex = 0;
            }
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateTargetSSWithSourceCols(ArrayList<TransferMap> transferMap, int noOfTargetColsForSource, int noOfSrcRowsToFitOnTargetCol, int sourceArrayColSize, int targetArrayRowSize) {
        for (int i = 0; i < noOfTargetColsForSource; ++i) {
            transferMap = this.populateTargetColWithSourceRows(transferMap, noOfSrcRowsToFitOnTargetCol, sourceArrayColSize, targetArrayRowSize);
            ++this.targetColIndex;
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateTargetColWithSourceRows(ArrayList<TransferMap> transferMap, int noOfSrcRowsToFitOnTargetCol, int sourceArrayColSize, int targetArrayRowSize) {
        int rIndex = 0;
        while (rIndex < targetArrayRowSize) {
            for (int r = 0; r < noOfSrcRowsToFitOnTargetCol; ++r) {
                int c = 0;
                while (c < sourceArrayColSize) {
                    TransferMap wellMap = new TransferMap();
                    wellMap.targetarray = this.targetArrayIndex;
                    wellMap.targetrowindex = rIndex++;
                    wellMap.targetcolindex = this.targetColIndex;
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

    private ArrayList<TransferMap> populateSourceArrayOnTarget(ArrayList<TransferMap> transferMap, int noOfTargetColsForSource, int noOfSrcColsToFitOnTarget, int sourceArrayRowSize, int targetArrayRowSize) {
        for (int i = 0; i < noOfTargetColsForSource; ++i) {
            transferMap = this.populateTargetCol(transferMap, noOfSrcColsToFitOnTarget, sourceArrayRowSize, targetArrayRowSize);
            ++this.targetColIndex;
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateTargetCol(ArrayList<TransferMap> transferMap, int noOfSrcColsToFitOnTarget, int sourceArrayRowSize, int targetArrayRowSize) {
        int rIndex = 0;
        while (rIndex < targetArrayRowSize) {
            for (int c = 0; c < noOfSrcColsToFitOnTarget; ++c) {
                int r = 0;
                while (r < sourceArrayRowSize) {
                    TransferMap wellMap = new TransferMap();
                    wellMap.targetarray = this.targetArrayIndex;
                    wellMap.targetrowindex = rIndex++;
                    wellMap.targetcolindex = this.targetColIndex;
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
}

