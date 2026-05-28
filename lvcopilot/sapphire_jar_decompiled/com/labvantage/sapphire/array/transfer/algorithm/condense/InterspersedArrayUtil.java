/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.condense;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;

public class InterspersedArrayUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int sourceArrayIndex = 1;
    private int targetArrayIndex = 1;

    public ArrayList<TransferMap> condense(String startPos, String direction, ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        block9: {
            block12: {
                block15: {
                    block14: {
                        block13: {
                            block7: {
                                block11: {
                                    block10: {
                                        block8: {
                                            if (!direction.equals("clockwise")) break block7;
                                            if (!startPos.equals("topleft")) break block8;
                                            transferMap = this.intersperseClockwiseFromTopLeft(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                                            break block9;
                                        }
                                        if (!startPos.equals("topright")) break block10;
                                        transferMap = this.intersperseClockwiseFromTopRight(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                                        break block9;
                                    }
                                    if (!startPos.equals("bottomright")) break block11;
                                    transferMap = this.intersperseClockwiseFromBottomRight(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                                    break block9;
                                }
                                if (!startPos.equals("bottomleft")) break block9;
                                transferMap = this.intersperseClockwiseFromBottomLeft(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                                break block9;
                            }
                            if (!direction.equals("anticlockwise")) break block12;
                            if (!startPos.equals("topleft")) break block13;
                            transferMap = this.intersperseAntiClockwiseFromTopLeft(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                            break block9;
                        }
                        if (!startPos.equals("topright")) break block14;
                        transferMap = this.intersperseAntiClockwiseFromTopRight(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                        break block9;
                    }
                    if (!startPos.equals("bottomright")) break block15;
                    transferMap = this.intersperseAntiClockwiseFromBottomRight(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                    break block9;
                }
                if (!startPos.equals("bottomleft")) break block9;
                transferMap = this.intersperseAntiClockwiseFromBottomLeft(transferMap, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, targetRowIndex, targetColIndex, transpose);
                break block9;
            }
            if (direction.equals("horizontal")) {
                while (targetRowIndex < noOfRowsInSS) {
                    targetColIndex = 0;
                    while (targetColIndex < noOfColsInSS) {
                        transferMap = this.populateTargetArrayWithSource(targetRowIndex, targetColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                        ++targetColIndex;
                        ++this.sourceArrayIndex;
                    }
                    ++targetRowIndex;
                }
            } else if (direction.equals("vertical")) {
                while (targetColIndex < noOfColsInSS) {
                    targetRowIndex = 0;
                    while (targetRowIndex < noOfRowsInSS) {
                        transferMap = this.populateTargetArrayWithSource(targetRowIndex, targetColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
                        ++targetRowIndex;
                        ++this.sourceArrayIndex;
                    }
                    ++targetColIndex;
                }
            }
        }
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseAntiClockwiseFromBottomLeft(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetRowIndex;
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetColIndex;
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetRowIndex;
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseAntiClockwiseFromBottomRight(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetColIndex;
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetRowIndex;
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetColIndex;
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseAntiClockwiseFromTopRight(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetRowIndex;
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetColIndex;
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetRowIndex;
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseAntiClockwiseFromTopLeft(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetColIndex;
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetRowIndex;
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetColIndex;
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseClockwiseFromTopLeft(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetRowIndex;
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetColIndex;
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetRowIndex;
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseClockwiseFromTopRight(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetColIndex;
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetRowIndex;
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetColIndex;
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseClockwiseFromBottomRight(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetRowIndex;
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetColIndex;
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetRowIndex;
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private ArrayList<TransferMap> intersperseClockwiseFromBottomLeft(ArrayList<TransferMap> transferMap, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, int targetRowIndex, int targetColIndex, boolean transpose) {
        targetRowIndex = this.populateArrayFromBtoT(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetColIndex;
        targetColIndex = this.populateArrayFromLtoR(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        ++targetRowIndex;
        targetRowIndex = this.populateArrayFromTtoB(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        --targetColIndex;
        targetColIndex = this.populateArrayFromRtoL(targetRowIndex, targetColIndex, noOfRowsInSS, noOfColsInSS, targetArrayRowSize, targetArrayColSize, transferMap, transpose);
        return transferMap;
    }

    private int populateArrayFromBtoT(int targetRowIndex, int targetColIndex, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        while (targetRowIndex >= 0) {
            transferMap = this.populateTargetArrayWithSource(targetRowIndex, targetColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
            --targetRowIndex;
            ++this.sourceArrayIndex;
        }
        return targetRowIndex + 1;
    }

    private int populateArrayFromRtoL(int targetRowIndex, int targetColIndex, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        while (targetColIndex >= 0) {
            transferMap = this.populateTargetArrayWithSource(targetRowIndex, targetColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
            --targetColIndex;
            ++this.sourceArrayIndex;
        }
        return targetColIndex + 1;
    }

    private int populateArrayFromTtoB(int targetRowIndex, int targetColIndex, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        while (targetRowIndex < noOfRowsInSS) {
            transferMap = this.populateTargetArrayWithSource(targetRowIndex, targetColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
            ++targetRowIndex;
            ++this.sourceArrayIndex;
        }
        return targetRowIndex - 1;
    }

    private int populateArrayFromLtoR(int targetRowIndex, int targetColIndex, int noOfRowsInSS, int noOfColsInSS, int targetArrayRowSize, int targetArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        while (targetColIndex < noOfColsInSS) {
            transferMap = this.populateTargetArrayWithSource(targetRowIndex, targetColIndex, targetArrayRowSize, targetArrayColSize, noOfRowsInSS, noOfColsInSS, transferMap, transpose);
            ++targetColIndex;
            ++this.sourceArrayIndex;
        }
        return targetColIndex - 1;
    }

    private ArrayList<TransferMap> populateTargetArrayWithSource(int targetRowIndex, int targetColIndex, int targetArrayRowSize, int targetArrayColSize, int noOfRowsInSS, int noOfColsInSS, ArrayList<TransferMap> transferMap, boolean transpose) {
        block6: {
            boolean isSubsectionPopulated;
            block5: {
                isSubsectionPopulated = this.checkIfTransferMapIsPopulated(transferMap, targetRowIndex, targetColIndex);
                if (!transpose) break block5;
                if (isSubsectionPopulated) break block6;
                int sourceColIndex = 0;
                for (int r = targetRowIndex; r < targetArrayRowSize; r += noOfRowsInSS) {
                    int sourceRowIndex = 0;
                    for (int c = targetColIndex; c < targetArrayColSize; c += noOfColsInSS) {
                        TransferMap wellMap = new TransferMap();
                        wellMap.targetarray = this.targetArrayIndex;
                        wellMap.targetrowindex = r;
                        wellMap.targetcolindex = c;
                        wellMap.sourcearray = this.sourceArrayIndex;
                        wellMap.sourcerowindex = sourceRowIndex++;
                        wellMap.sourcecolindex = sourceColIndex;
                        transferMap.add(wellMap);
                    }
                    ++sourceColIndex;
                }
                break block6;
            }
            if (!isSubsectionPopulated) {
                int sourceRowIndex = 0;
                for (int r = targetRowIndex; r < targetArrayRowSize; r += noOfRowsInSS) {
                    int sourceColIndex = 0;
                    for (int c = targetColIndex; c < targetArrayColSize; c += noOfColsInSS) {
                        TransferMap wellMap = new TransferMap();
                        wellMap.targetarray = this.targetArrayIndex;
                        wellMap.targetrowindex = r;
                        wellMap.targetcolindex = c;
                        wellMap.sourcearray = this.sourceArrayIndex;
                        wellMap.sourcerowindex = sourceRowIndex;
                        wellMap.sourcecolindex = sourceColIndex++;
                        transferMap.add(wellMap);
                    }
                    ++sourceRowIndex;
                }
            }
        }
        return transferMap;
    }

    private boolean checkIfTransferMapIsPopulated(ArrayList<TransferMap> transferMap, int startRowIndex, int startColIndex) {
        boolean isSubsectionPopulated = false;
        for (int i = 0; i < transferMap.size(); ++i) {
            TransferMap wellMap = transferMap.get(i);
            if (wellMap.targetrowindex != startRowIndex || wellMap.targetcolindex != startColIndex) continue;
            isSubsectionPopulated = true;
        }
        return isSubsectionPopulated;
    }
}

