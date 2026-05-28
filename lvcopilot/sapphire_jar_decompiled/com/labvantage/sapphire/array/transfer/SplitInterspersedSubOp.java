/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer;

import com.labvantage.sapphire.array.TransferMap;
import com.labvantage.sapphire.array.transfer.BaseSubOperation;
import com.labvantage.sapphire.array.transfer.algorithm.split.EvaluateSplittingRules;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SplitInterspersedSubOp
extends BaseSubOperation {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int sourceArrayRowSize = 0;
    private int targetArrayColSize = 0;
    private int sourceArrayColSize = 0;
    private int targetArrayRowSize = 0;
    private int numtargetArrays = 0;

    @Override
    public PropertyList getSubOperationProperties(PropertyList algorithmicRule, TranslationProcessor translationProcessor) {
        String defaultTranspose = "";
        if (algorithmicRule != null && algorithmicRule.size() > 0) {
            defaultTranspose = algorithmicRule.getProperty("transpose");
        }
        PropertyList subOpProps = new PropertyList();
        PropertyListCollection columns = new PropertyListCollection();
        PropertyList transpose = new PropertyList();
        transpose.setProperty("columnid", "transpose");
        transpose.setProperty("mode", "checkbox");
        if (defaultTranspose.length() > 0) {
            transpose.setProperty("value", defaultTranspose);
        }
        transpose.setProperty("title", translationProcessor.translate("Transpose"));
        columns.add(transpose);
        subOpProps.setProperty("columns", columns);
        return subOpProps;
    }

    @Override
    public int getNumOfSourceArrays(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        return 1;
    }

    @Override
    public int getNumOfTargetArrays(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        if (this.numtargetArrays == 0 && this.targetArrayColSize == 0 && this.targetArrayRowSize == 0) {
            this.setSourceArrayDims(sourceArrayType, targetArrayType, queryProcessor);
        }
        return this.numtargetArrays;
    }

    @Override
    public HashMap<String, String> getSaveJavaScript() {
        HashMap<String, String> saveJavaScriptHM = new HashMap<String, String>();
        String transposeJS = "document.getElementById( \"transpose\" ).checked";
        saveJavaScriptHM.put("transpose", transposeJS);
        return saveJavaScriptHM;
    }

    @Override
    public ArrayList<TransferMap> generateMap(HashMap<String, String> subOpProperties, QueryProcessor queryProcessor) throws SapphireException {
        EvaluateSplittingRules evalRules = new EvaluateSplittingRules();
        ArrayList<TransferMap> transferMap = evalRules.splitArray("Interspersed", subOpProperties.get("sourcearraytypeid"), subOpProperties.get("targetarraytypeid"), Boolean.valueOf(subOpProperties.get("transpose")), queryProcessor);
        return transferMap;
    }

    @Override
    public String generateColorMap(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        String colorMap = "";
        if (this.numtargetArrays == 0 && this.targetArrayColSize == 0 && this.targetArrayRowSize == 0) {
            this.setSourceArrayDims(sourceArrayType, targetArrayType, queryProcessor);
        }
        if (this.sourceArrayColSize % this.targetArrayColSize == 0 && this.sourceArrayRowSize % this.targetArrayRowSize == 0) {
            int noOfColsInSS = this.sourceArrayColSize / this.targetArrayColSize;
            int noOfRowsInSS = this.sourceArrayRowSize / this.targetArrayRowSize;
            for (int startRowIndex = 0; startRowIndex < this.sourceArrayRowSize; startRowIndex += noOfRowsInSS) {
                for (int startColIndex = 0; startColIndex < this.sourceArrayColSize; startColIndex += noOfColsInSS) {
                    colorMap = this.setColorMap(colorMap, startRowIndex, noOfRowsInSS, startColIndex, noOfColsInSS);
                }
            }
        } else {
            int noOfColsInSS = this.sourceArrayRowSize / this.targetArrayColSize;
            int noOfRowsInSS = this.sourceArrayColSize / this.targetArrayRowSize;
            for (int startRowIndex = 0; startRowIndex < this.sourceArrayRowSize; startRowIndex += noOfColsInSS) {
                for (int startColIndex = 0; startColIndex < this.sourceArrayColSize; startColIndex += noOfRowsInSS) {
                    colorMap = this.setColorMap(colorMap, startRowIndex, noOfColsInSS, startColIndex, noOfRowsInSS);
                }
            }
        }
        return colorMap;
    }

    private String setColorMap(String colorMap, int startRowIndex, int noOfRowsInSS, int startColIndex, int noOfColsInSS) {
        int index = 1;
        for (int r = startRowIndex; r < noOfRowsInSS + startRowIndex; ++r) {
            for (int c = startColIndex; c < noOfColsInSS + startColIndex; ++c) {
                String map = "1;" + r + ";" + c + "-" + index;
                colorMap = colorMap.length() > 0 ? colorMap + "|" + map : map;
                ++index;
            }
        }
        return colorMap;
    }

    private void setSourceArrayDims(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        int[] sourceArrayDims = this.findArrayDimensions(sourceArrayType, queryProcessor);
        int[] targetArrayDims = this.findArrayDimensions(targetArrayType, queryProcessor);
        this.sourceArrayRowSize = sourceArrayDims[0];
        this.sourceArrayColSize = sourceArrayDims[1];
        this.targetArrayRowSize = targetArrayDims[0];
        this.targetArrayColSize = targetArrayDims[1];
        this.numtargetArrays = this.sourceArrayColSize * this.sourceArrayRowSize / (this.targetArrayColSize * this.targetArrayRowSize);
    }
}

