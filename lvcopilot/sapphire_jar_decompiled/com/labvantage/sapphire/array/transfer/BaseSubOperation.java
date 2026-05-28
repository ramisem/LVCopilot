/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer;

import com.labvantage.sapphire.array.TransferMap;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public abstract class BaseSubOperation {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    public abstract PropertyList getSubOperationProperties(PropertyList var1, TranslationProcessor var2);

    public abstract int getNumOfSourceArrays(String var1, String var2, QueryProcessor var3);

    public abstract int getNumOfTargetArrays(String var1, String var2, QueryProcessor var3);

    public abstract HashMap<String, String> getSaveJavaScript();

    public abstract ArrayList<TransferMap> generateMap(HashMap<String, String> var1, QueryProcessor var2) throws SapphireException;

    public abstract String generateColorMap(String var1, String var2, QueryProcessor var3);

    int[] findArrayDimensions(String arrayType, QueryProcessor queryProcessor) {
        int[] arrayDim = new int[2];
        String sql = "SELECT numrows, numcolumns FROM arraytype WHERE arraytypeid =?";
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{arrayType});
        arrayDim[0] = ds.getInt(0, "numrows");
        arrayDim[1] = ds.getInt(0, "numcolumns");
        return arrayDim;
    }
}

