/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.split;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;

public class SplitWholeArrayUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int sourceArrayIndex = 1;
    private int targetArrayIndex = 1;

    public ArrayList<TransferMap> split(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, boolean transpose) {
        int srcRowIndex;
        int srcColIndex = 0;
        if (transpose) {
            for (srcRowIndex = 0; srcRowIndex < sourceArrayRowSize; srcRowIndex += targetArrayColSize) {
                while (srcColIndex < sourceArrayColSize) {
                    this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
                    ++this.targetArrayIndex;
                    srcColIndex += targetArrayRowSize;
                }
                srcColIndex = 0;
            }
        } else {
            while (srcRowIndex < sourceArrayRowSize) {
                while (srcColIndex < sourceArrayColSize) {
                    this.populateTransferMapForTargetArray(srcRowIndex, srcColIndex, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
                    ++this.targetArrayIndex;
                    srcColIndex += targetArrayColSize;
                }
                srcRowIndex += targetArrayRowSize;
                srcColIndex = 0;
            }
        }
        return transferMap;
    }

    private ArrayList<TransferMap> populateTransferMapForTargetArray(int srcRowIndex, int srcColIndex, int targetArrayRowSize, int targetArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
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
                    wellMap.sourcerowindex = rowIndex++;
                    wellMap.sourcecolindex = srcColIndex;
                    transferMap.add(wellMap);
                }
                ++srcColIndex;
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
                    wellMap.sourcecolindex = colIndex++;
                    transferMap.add(wellMap);
                }
                ++srcRowIndex;
            }
        }
        return transferMap;
    }
}

