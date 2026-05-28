/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer;

import com.labvantage.sapphire.array.TransferMap;
import com.labvantage.sapphire.array.transfer.BaseSubOperation;
import com.labvantage.sapphire.array.transfer.algorithm.condense.EvaluateCondensingRules;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CondenseInterspersedSubOp
extends BaseSubOperation {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int numSrcArrays = 0;
    private int srcArrColSize = 0;
    private int srcArrRowSize = 0;

    @Override
    public PropertyList getSubOperationProperties(PropertyList algorithmicRule, TranslationProcessor translationProcessor) {
        String defaultDirection = "";
        String defaultStartPos = "";
        String defaultTranspose = "";
        if (algorithmicRule != null && algorithmicRule.size() > 0) {
            defaultDirection = algorithmicRule.getProperty("direction");
            defaultStartPos = algorithmicRule.getProperty("startpos");
            defaultTranspose = algorithmicRule.getProperty("transpose");
        }
        PropertyList subOpProps = new PropertyList();
        PropertyListCollection columns = new PropertyListCollection();
        PropertyList dir = new PropertyList();
        dir.setProperty("columnid", "direction");
        dir.setProperty("mode", "dropdownlist");
        if (defaultDirection.length() > 0) {
            dir.setProperty("value", defaultDirection);
        }
        dir.setProperty("title", translationProcessor.translate("Direction"));
        dir.setProperty("dropdownvalues", "clockwise=Clockwise;anticlockwise=AntiClockwise;horizontal=Horizontal;vertical=Vertical");
        columns.add(dir);
        PropertyList startpos = new PropertyList();
        startpos.setProperty("columnid", "startpos");
        startpos.setProperty("mode", "dropdownlist");
        if (defaultStartPos.length() > 0) {
            startpos.setProperty("value", defaultStartPos);
        }
        startpos.setProperty("title", translationProcessor.translate("Start Position"));
        startpos.setProperty("dropdownvalues", "topleft=Top Left;topright=Top \tRight;bottomleft=Bottom Left;bottomright=Bottom Right");
        columns.add(startpos);
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
        if (this.numSrcArrays == 0 && this.srcArrColSize == 0 && this.srcArrRowSize == 0) {
            this.setSourceArrayDims(sourceArrayType, targetArrayType, queryProcessor);
        }
        return this.numSrcArrays;
    }

    @Override
    public int getNumOfTargetArrays(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        return 1;
    }

    @Override
    public HashMap<String, String> getSaveJavaScript() {
        HashMap<String, String> saveJavaScriptHM = new HashMap<String, String>();
        String directionJS = "document.getElementById(\"direction\").options[document.getElementById(\"direction\").selectedIndex].value";
        saveJavaScriptHM.put("direction", directionJS);
        String startPositionJS = "document.getElementById(\"startpos\").options[document.getElementById(\"startpos\").selectedIndex].value";
        saveJavaScriptHM.put("startposition", startPositionJS);
        String transposeJS = "document.getElementById( \"transpose\" ).checked";
        saveJavaScriptHM.put("transpose", transposeJS);
        return saveJavaScriptHM;
    }

    @Override
    public ArrayList<TransferMap> generateMap(HashMap<String, String> subOpProperties, QueryProcessor queryProcessor) throws SapphireException {
        EvaluateCondensingRules evalRules = new EvaluateCondensingRules();
        ArrayList<TransferMap> transferMap = evalRules.condenseArray("interspersed", subOpProperties.get("sourcearraytypeid"), subOpProperties.get("targetarraytypeid"), subOpProperties.get("direction"), subOpProperties.get("startpos"), Boolean.valueOf(subOpProperties.get("transpose")), queryProcessor);
        return transferMap;
    }

    public void setSourceArrayDims(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        int numSrcArrays;
        int[] sourceArrayDims = this.findArrayDimensions(sourceArrayType, queryProcessor);
        int[] targetArrayDims = this.findArrayDimensions(targetArrayType, queryProcessor);
        this.srcArrRowSize = sourceArrayDims[0];
        this.srcArrColSize = sourceArrayDims[1];
        int targetArrayRowSize = targetArrayDims[0];
        int targetArrayColSize = targetArrayDims[1];
        this.numSrcArrays = numSrcArrays = targetArrayColSize * targetArrayRowSize / (this.srcArrColSize * this.srcArrRowSize);
    }

    @Override
    public String generateColorMap(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        String colorMap = "";
        if (this.numSrcArrays == 0 && this.srcArrColSize == 0 && this.srcArrRowSize == 0) {
            this.setSourceArrayDims(sourceArrayType, targetArrayType, queryProcessor);
        }
        for (int i = 1; i <= this.numSrcArrays; ++i) {
            for (int r = 0; r < this.srcArrRowSize; ++r) {
                for (int c = 0; c < this.srcArrColSize; ++c) {
                    String map = i + ";" + r + ";" + c + "-" + i;
                    colorMap = colorMap.length() > 0 ? colorMap + "|" + map : map;
                }
            }
        }
        return colorMap;
    }
}

