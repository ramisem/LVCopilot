/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.maskingrules;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.util.M18NUtil;

public class BaseMasker {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private M18NUtil m18NUtil;
    private SapphireConnection sapphireConnection;

    protected BaseMasker() {
    }

    protected BaseMasker(SapphireConnection sapphireConnection) {
        this.sapphireConnection = sapphireConnection;
    }

    protected final M18NUtil getM18NUtil() {
        if (this.m18NUtil == null) {
            this.m18NUtil = new M18NUtil(this.sapphireConnection);
        }
        return this.m18NUtil;
    }
}

