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

public class SplitWholeArraySubOp
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
        ArrayList<TransferMap> transferMap = evalRules.splitArray("WholeArray", subOpProperties.get("sourcearraytypeid"), subOpProperties.get("targetarraytypeid"), Boolean.valueOf(subOpProperties.get("transpose")), queryProcessor);
        return transferMap;
    }

    @Override
    public String generateColorMap(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        String colorMap = "";
        if (this.numtargetArrays == 0 && this.targetArrayColSize == 0 && this.targetArrayRowSize == 0) {
            this.setSourceArrayDims(sourceArrayType, targetArrayType, queryProcessor);
        }
        if (this.sourceArrayColSize % this.targetArrayColSize == 0 && this.sourceArrayRowSize % this.targetArrayRowSize == 0) {
            int startRowIndex = 0;
            int startColIndex = 0;
            for (int i = 1; i <= this.numtargetArrays; ++i) {
                for (int r = startRowIndex; r < this.targetArrayRowSize + startRowIndex; ++r) {
                    for (int c = startColIndex; c < this.targetArrayColSize + startColIndex; ++c) {
                        String map = "1;" + r + ";" + c + "-" + i;
                        colorMap = colorMap.length() > 0 ? colorMap + "|" + map : map;
                    }
                }
                if ((startRowIndex += this.targetArrayRowSize) < this.sourceArrayRowSize) continue;
                startColIndex += this.targetArrayColSize;
                startRowIndex = 0;
            }
        } else {
            int startRowIndex = 0;
            int startColIndex = 0;
            for (int i = 1; i <= this.numtargetArrays; ++i) {
                for (int r = startRowIndex; r < this.targetArrayColSize + startRowIndex; ++r) {
                    for (int c = startColIndex; c < this.targetArrayRowSize + startColIndex; ++c) {
                        String map = "1;" + r + ";" + c + "-" + i;
                        colorMap = colorMap.length() > 0 ? colorMap + "|" + map : map;
                    }
                }
                if ((startRowIndex += this.targetArrayColSize) < this.sourceArrayRowSize) continue;
                startColIndex += this.targetArrayRowSize;
                startRowIndex = 0;
            }
        }
        return colorMap;
    }

    public void setSourceArrayDims(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        int[] sourceArrayDims = this.findArrayDimensions(sourceArrayType, queryProcessor);
        int[] targetArrayDims = this.findArrayDimensions(targetArrayType, queryProcessor);
        this.sourceArrayRowSize = sourceArrayDims[0];
        this.sourceArrayColSize = sourceArrayDims[1];
        this.targetArrayRowSize = targetArrayDims[0];
        this.targetArrayColSize = targetArrayDims[1];
        this.numtargetArrays = this.sourceArrayColSize * this.sourceArrayRowSize / (this.targetArrayColSize * this.targetArrayRowSize);
    }
}

