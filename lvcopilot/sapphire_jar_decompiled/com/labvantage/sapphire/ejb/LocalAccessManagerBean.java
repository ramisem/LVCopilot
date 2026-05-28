/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.BaseAccessManager;
import com.labvantage.sapphire.ejb.LocalAccessManagement;
import javax.ejb.SessionBean;

public class LocalAccessManagerBean
extends BaseAccessManager
implements SessionBean,
LocalAccessManagement {
    public LocalAccessManagerBean() {
        this.logName = "LocalAccessManager";
    }
}

