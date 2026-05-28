/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer;

import com.labvantage.sapphire.array.TransferMap;
import com.labvantage.sapphire.array.transfer.BaseSubOperation;
import com.labvantage.sapphire.array.transfer.algorithm.replication.EvaluateReplicationRules;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class StampWholeArraySubOp
extends BaseSubOperation {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

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
        return 1;
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
        EvaluateReplicationRules evalRules = new EvaluateReplicationRules();
        ArrayList<TransferMap> transferMap = evalRules.createReplica("Stamping", subOpProperties.get("sourcearraytypeid"), subOpProperties.get("targetarraytypeid"), Boolean.valueOf(subOpProperties.get("transpose")), queryProcessor);
        return transferMap;
    }

    @Override
    public String generateColorMap(String sourceArrayType, String targetArrayType, QueryProcessor queryProcessor) {
        String colorMap = "";
        int[] sourceArrayDims = this.findArrayDimensions(sourceArrayType, queryProcessor);
        int srcArrRowSize = sourceArrayDims[0];
        int srcArrColSize = sourceArrayDims[1];
        for (int r = 0; r < srcArrRowSize; ++r) {
            for (int c = 0; c < srcArrColSize; ++c) {
                String map = "1;" + r + ";" + c + "-1";
                colorMap = colorMap.length() > 0 ? colorMap + "|" + map : map;
            }
        }
        return colorMap;
    }
}

