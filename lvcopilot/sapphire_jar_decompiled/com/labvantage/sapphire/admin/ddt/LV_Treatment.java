/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.util.array.ArrayUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.xml.PropertyList;

public class LV_Treatment
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        ArrayUtil.checkExistenceInArray(rsetid, actionProps, this.getQueryProcessor(), this.getTranslationProcessor(), false);
    }
}

