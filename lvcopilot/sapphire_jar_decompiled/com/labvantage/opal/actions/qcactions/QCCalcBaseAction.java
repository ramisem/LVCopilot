/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.ParameterType;
import com.labvantage.opal.actions.qcactions.QCBaseAction;
import java.util.HashMap;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;

public abstract class QCCalcBaseAction
extends QCBaseAction
implements ParameterType {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public abstract int processAction(String var1, String var2, HashMap var3);

    public abstract Boolean hasBracket();

    public DataSet addQCCalcDataItems(String qcBatchId, String bstId, String actionId, QueryProcessor qp) {
        DataSet ds = new DataSet();
        return ds;
    }
}

