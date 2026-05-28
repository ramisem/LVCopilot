/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class IsConditionMet
extends BaseAction
implements sapphire.action.IsConditionMet {
    public static final String ID = "IsConditionMet";
    public static final String VERSION = "1";
    public static final String RETURN_ISCONDITIONMET_YES = "Yes";
    public static final String RETURN_ISCONDITIONMET_NO = "No";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String condition = properties.getProperty("condition");
        if (condition.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Condition not set");
        }
        properties.setProperty("isconditionmet", RETURN_ISCONDITIONMET_NO);
        try {
            String result = ExpressionUtil.evaluate(condition, new HashMap());
            if (result.equalsIgnoreCase("true")) {
                properties.setProperty("isconditionmet", RETURN_ISCONDITIONMET_YES);
            }
        }
        catch (Exception e) {
            throw new SapphireException("INVALID_PROPERTY", "Failed to evaluate condition: " + condition, e);
        }
    }
}

