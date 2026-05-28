/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.split;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;

public class SplitInterspersedUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int sourceArrayIndex = 1;
    private int targetArrayIndex = 1;

    public ArrayList<TransferMap> split(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, boolean transpose) {
        int srcRowIndex = 0;
        int srcColIndex = 0;
        if (transpose) {
            if (noOfRowsInSS == 1) {
                while (srcColIndex < noOfColsInSS) {
                    this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                    ++this.targetArrayIndex;
                    srcColIndex = srcColIndex + noOfColsInSS - 1;
                }
            } else {
                while (srcRowIndex < noOfRowsInSS) {
                    if (noOfColsInSS == 1) {
                        this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                        ++this.targetArrayIndex;
                    } else {
                        while (srcColIndex < noOfColsInSS) {
                            this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                            ++this.targetArrayIndex;
                            srcColIndex = srcColIndex + noOfColsInSS - 1;
                        }
                    }
                    srcRowIndex = srcRowIndex + noOfRowsInSS - 1;
                    srcColIndex = 0;
                }
            }
        } else if (noOfRowsInSS == 1) {
            while (srcColIndex < noOfColsInSS) {
                this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                ++this.targetArrayIndex;
                srcColIndex = srcColIndex + noOfColsInSS - 1;
            }
        } else {
            while (srcRowIndex < noOfRowsInSS) {
                if (noOfColsInSS == 1) {
                    this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                    ++this.targetArrayIndex;
                } else {
                    while (srcColIndex < noOfColsInSS) {
                        this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                        ++this.targetArrayIndex;
                        srcColIndex = srcColIndex + noOfColsInSS - 1;
                    }
                }
                srcRowIndex = srcRowIndex + noOfRowsInSS - 1;
                srcColIndex = 0;
            }
        }
        return transferMap;
    }

    private void populateMapFromLtoR(int srcRowIndex, int srcColIndex, int targetArrayRowSize, int targetArrayColSize, int noOfRowsInSS, int noOfColsInSS, ArrayList<TransferMap> transferMap, boolean transpose) {
        if (noOfColsInSS == 1) {
            this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
        } else {
            while (srcColIndex < noOfColsInSS) {
                this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                ++this.targetArrayIndex;
                srcColIndex = srcColIndex + noOfColsInSS - 1;
            }
        }
    }

    private ArrayList<TransferMap> populateTransferMapForTargetArray(int srcRowIndex, int srcColIndex, int targetArrayRowSize, int targetArrayColSize, int noOfRowsInSS, int noOfColsInSS, ArrayList<TransferMap> transferMap, boolean transpose) {
        if (transpose) {
            int rowIndex = srcRowIndex;
            for (int r = 0; r < targetArrayRowSize; ++r) {
                rowIndex = srcRowIndex;
                int c = 0;
                while (c < targetArrayColSize) {
                    TransferMap wellMap = new TransferMap();
                    wellMap.targetarray = this.targetArrayIndex;
                    wellMap.targetrowindex = r;
                    wellMap.targetcolindex = c++;
                    wellMap.sourcearray = this.sourceArrayIndex;
                    wellMap.sourcerowindex = rowIndex;
                    wellMap.sourcecolindex = srcColIndex;
                    transferMap.add(wellMap);
                    rowIndex += noOfRowsInSS;
                }
                srcColIndex += noOfColsInSS;
            }
        } else {
            int colIndex = srcColIndex;
            for (int r = 0; r < targetArrayRowSize; ++r) {
                colIndex = srcColIndex;
                int c = 0;
                while (c < targetArrayColSize) {
                    TransferMap wellMap = new TransferMap();
                    wellMap.targetarray = this.targetArrayIndex;
                    wellMap.targetrowindex = r;
                    wellMap.targetcolindex = c++;
                    wellMap.sourcearray = this.sourceArrayIndex;
                    wellMap.sourcerowindex = srcRowIndex;
                    wellMap.sourcecolindex = colIndex;
                    transferMap.add(wellMap);
                    colIndex += noOfColsInSS;
                }
                srcRowIndex += noOfRowsInSS;
            }
        }
        return transferMap;
    }
}

