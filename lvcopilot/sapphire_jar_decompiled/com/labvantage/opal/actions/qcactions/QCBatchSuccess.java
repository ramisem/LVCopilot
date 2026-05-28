/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.QCBatchBaseAction;
import java.util.HashMap;

public class QCBatchSuccess
extends QCBatchBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    int rc = 1;

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        return this.rc;
    }
}

