/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer.algorithm.condense;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;

public class CondenseWholeArrayUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int sourceArrayIndex = 1;
    private int targetArrayIndex = 1;

    public ArrayList<TransferMap> condense(String startPos, String direction, ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        block17: {
            block20: {
                block23: {
                    block22: {
                        block21: {
                            block15: {
                                block19: {
                                    block18: {
                                        block16: {
                                            if (!direction.equals("clockwise")) break block15;
                                            if (!startPos.equals("topleft")) break block16;
                                            transferMap = this.condenseClockwiseFromTopLeft(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                                            break block17;
                                        }
                                        if (!startPos.equals("topright")) break block18;
                                        transferMap = this.condenseClockwiseFromTopRight(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                                        break block17;
                                    }
                                    if (!startPos.equals("bottomright")) break block19;
                                    transferMap = this.condenseClockwiseFromBottomRight(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                                    break block17;
                                }
                                if (!startPos.equals("bottomleft")) break block17;
                                transferMap = this.condenseClockwiseFromBottomLeft(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                                break block17;
                            }
                            if (!direction.equals("anticlockwise")) break block20;
                            if (!startPos.equals("topleft")) break block21;
                            transferMap = this.condenseAnticlockwiseFromTopLeft(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                            break block17;
                        }
                        if (!startPos.equals("topright")) break block22;
                        transferMap = this.condenseAnticlockwiseFromTopRight(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                        break block17;
                    }
                    if (!startPos.equals("bottomright")) break block23;
                    transferMap = this.condenseAnticlockwiseFromBottomRight(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                    break block17;
                }
                if (!startPos.equals("bottomleft")) break block17;
                transferMap = this.condenseAnticlockwiseFromBottomLeft(transferMap, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, targetArrayColSize, rowIndex, colIndex, transpose);
                break block17;
            }
            if (direction.equals("horizontal")) {
                if (transpose) {
                    while (rowIndex < targetArrayRowSize) {
                        colIndex = 0;
                        while (colIndex < targetArrayColSize) {
                            this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                            colIndex += sourceArrayRowSize;
                            ++this.sourceArrayIndex;
                        }
                        rowIndex += sourceArrayColSize;
                    }
                } else {
                    while (rowIndex < targetArrayRowSize) {
                        colIndex = 0;
                        while (colIndex < targetArrayColSize) {
                            this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                            colIndex += sourceArrayColSize;
                            ++this.sourceArrayIndex;
                        }
                        rowIndex += sourceArrayRowSize;
                    }
                }
            } else if (direction.equals("vertical")) {
                if (transpose) {
                    while (colIndex < targetArrayColSize) {
                        rowIndex = 0;
                        while (rowIndex < targetArrayRowSize) {
                            this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                            rowIndex += sourceArrayColSize;
                            ++this.sourceArrayIndex;
                        }
                        colIndex += sourceArrayRowSize;
                    }
                } else {
                    while (colIndex < targetArrayColSize) {
                        rowIndex = 0;
                        while (rowIndex < targetArrayRowSize) {
                            this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                            rowIndex += sourceArrayRowSize;
                            ++this.sourceArrayIndex;
                        }
                        colIndex += sourceArrayColSize;
                    }
                }
            }
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseAnticlockwiseFromBottomLeft(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayColSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex -= sourceArrayRowSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayColSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
        } else {
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayRowSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex -= sourceArrayColSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayRowSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseAnticlockwiseFromBottomRight(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex -= sourceArrayRowSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayColSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex += sourceArrayRowSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
        } else {
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex -= sourceArrayColSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayRowSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex += sourceArrayColSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseAnticlockwiseFromTopRight(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayColSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex += sourceArrayRowSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayColSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        } else {
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayRowSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex += sourceArrayColSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayRowSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseAnticlockwiseFromTopLeft(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex += sourceArrayRowSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayColSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex -= sourceArrayRowSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        } else {
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex += sourceArrayColSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayRowSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex -= sourceArrayColSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseClockwiseFromBottomLeft(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex += sourceArrayRowSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayColSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex -= sourceArrayRowSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        } else {
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex += sourceArrayColSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayRowSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex -= sourceArrayColSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseClockwiseFromBottomRight(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayColSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex += sourceArrayRowSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayColSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
        } else {
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayRowSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex += sourceArrayColSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayRowSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseClockwiseFromTopRight(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex -= sourceArrayRowSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayColSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex += sourceArrayRowSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
        } else {
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex -= sourceArrayColSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayRowSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            colIndex += sourceArrayColSize;
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
        }
        return transferMap;
    }

    private ArrayList<TransferMap> condenseClockwiseFromTopLeft(ArrayList<TransferMap> transferMap, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, int targetArrayColSize, int rowIndex, int colIndex, boolean transpose) {
        if (transpose) {
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayColSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex -= sourceArrayRowSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayColSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        } else {
            colIndex = this.populateRowFromLtoR(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayColSize, transferMap, transpose);
            rowIndex += sourceArrayRowSize;
            rowIndex = this.populateColFromTtoB(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, targetArrayRowSize, transferMap, transpose);
            colIndex -= sourceArrayColSize;
            colIndex = this.populateRowFromRtoL(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
            rowIndex -= sourceArrayRowSize;
            rowIndex = this.populateColFromBtoT(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
        }
        return transferMap;
    }

    private int populateRowFromLtoR(int rowIndex, int colIndex, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        int index = 0;
        if (transpose) {
            while (colIndex < targetArrayColSize) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                colIndex += sourceArrayRowSize;
                ++this.sourceArrayIndex;
            }
            index = colIndex - sourceArrayRowSize;
        } else {
            while (colIndex < targetArrayColSize) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                colIndex += sourceArrayColSize;
                ++this.sourceArrayIndex;
            }
            index = colIndex - sourceArrayColSize;
        }
        return index;
    }

    private int populateRowFromRtoL(int rowIndex, int colIndex, int sourceArrayRowSize, int sourceArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        int index = 0;
        if (transpose) {
            while (colIndex >= 0) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                colIndex -= sourceArrayRowSize;
                ++this.sourceArrayIndex;
            }
            index = colIndex + sourceArrayRowSize;
        } else {
            while (colIndex >= 0) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                colIndex -= sourceArrayColSize;
                ++this.sourceArrayIndex;
            }
            index = colIndex + sourceArrayColSize;
        }
        return index;
    }

    private int populateColFromTtoB(int rowIndex, int colIndex, int sourceArrayRowSize, int sourceArrayColSize, int targetArrayRowSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        int index = 0;
        if (transpose) {
            while (rowIndex < targetArrayRowSize) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                rowIndex += sourceArrayColSize;
                ++this.sourceArrayIndex;
            }
            index = rowIndex - sourceArrayColSize;
        } else {
            while (rowIndex < targetArrayRowSize) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                rowIndex += sourceArrayRowSize;
                ++this.sourceArrayIndex;
            }
            index = rowIndex - sourceArrayRowSize;
        }
        return index;
    }

    private int populateColFromBtoT(int rowIndex, int colIndex, int sourceArrayRowSize, int sourceArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        int index = 0;
        if (transpose) {
            while (rowIndex >= 0) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                rowIndex -= sourceArrayColSize;
                ++this.sourceArrayIndex;
            }
            index = rowIndex + sourceArrayColSize;
        } else {
            while (rowIndex >= 0) {
                this.populateTargetArrayWithSubsection(rowIndex, colIndex, sourceArrayRowSize, sourceArrayColSize, transferMap, transpose);
                rowIndex -= sourceArrayRowSize;
                ++this.sourceArrayIndex;
            }
            index = rowIndex + sourceArrayRowSize;
        }
        return index;
    }

    private ArrayList<TransferMap> populateTargetArrayWithSubsection(int startRowIndex, int startColIndex, int sourceArrayRowSize, int sourceArrayColSize, ArrayList<TransferMap> transferMap, boolean transpose) {
        block8: {
            int endRowIndex = 0;
            int endColIndex = 0;
            if (transpose) {
                endRowIndex = startRowIndex + sourceArrayColSize;
                endColIndex = startColIndex + sourceArrayRowSize;
            } else {
                endRowIndex = startRowIndex + sourceArrayRowSize;
                endColIndex = startColIndex + sourceArrayColSize;
            }
            boolean isSubsectionPopulated = this.checkIfTransferMapIsPopulated(transferMap, startRowIndex, startColIndex);
            if (isSubsectionPopulated) break block8;
            if (transpose) {
                int sourceColIndex = 0;
                for (int r = startRowIndex; r < endRowIndex; ++r) {
                    int sourceRowIndex = 0;
                    int c = startColIndex;
                    while (c < endColIndex) {
                        TransferMap wellMap = new TransferMap();
                        wellMap.targetarray = this.targetArrayIndex;
                        wellMap.targetrowindex = r;
                        wellMap.targetcolindex = c++;
                        wellMap.sourcearray = this.sourceArrayIndex;
                        wellMap.sourcerowindex = sourceRowIndex++;
                        wellMap.sourcecolindex = sourceColIndex;
                        transferMap.add(wellMap);
                    }
                    ++sourceColIndex;
                }
            } else {
                int sourceRowIndex = 0;
                for (int r = startRowIndex; r < endRowIndex; ++r) {
                    int sourceColIndex = 0;
                    int c = startColIndex;
                    while (c < endColIndex) {
                        TransferMap wellMap = new TransferMap();
                        wellMap.targetarray = this.targetArrayIndex;
                        wellMap.targetrowindex = r;
                        wellMap.targetcolindex = c++;
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

