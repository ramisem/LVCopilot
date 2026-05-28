/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.cache.CacheNames;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class ExpressionSDC
extends BaseSDCRules
implements CacheNames {
    static final String LABVANTAGE_CVS_ID = "$Revision: 101802 $";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean cacheCleared = false;
        for (int i = 0; i < primary.size(); ++i) {
            String typeflag = primary.getString(i, "typeflag");
            String namespace = primary.getString(i, "namespace");
            String expressionimplementation = primary.getString(i, "expressionimplementation");
            String expression = primary.getString(i, "expression");
            if (cacheCleared || typeflag == null || typeflag.length() != 1 || namespace == null || namespace.length() <= 0 || expression == null || expression.length() <= 0 || expressionimplementation == null || expressionimplementation.length() <= 0) continue;
            SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
            CacheUtil.clear(sapphireConnection.getDatabaseId(), "ExpressionPrefixDef");
            cacheCleared = true;
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        CacheUtil.clear(sapphireConnection.getDatabaseId(), "ExpressionPrefixDef");
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        CacheUtil.clear(sapphireConnection.getDatabaseId(), "ExpressionPrefixDef");
        GroovyUtil.clearScriptCache();
    }
}

