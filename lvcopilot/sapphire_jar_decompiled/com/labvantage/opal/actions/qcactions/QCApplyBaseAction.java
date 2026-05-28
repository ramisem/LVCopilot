/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.QCBaseAction;
import java.util.HashMap;

public abstract class QCApplyBaseAction
extends QCBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public abstract int processAction(String var1, String var2, HashMap var3);

    protected void applyTestMethod(String sdcid, String keyid1, String keyid2, String keyid3, String testMethod) {
    }
}

