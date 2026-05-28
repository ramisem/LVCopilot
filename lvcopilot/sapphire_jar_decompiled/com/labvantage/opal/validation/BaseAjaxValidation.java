/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.validation;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public abstract class BaseAjaxValidation
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54719 $";
    public static final String YES = "Y";
    public static final String NO = "N";
    private SapphireConnection sapphireConnection;

    protected SapphireConnection getSapphireConnection() {
        if (this.sapphireConnection == null) {
            this.sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
        }
        return this.sapphireConnection;
    }

    protected boolean isDepartmentMember(String department) {
        return this.getDepartmentList().contains(department);
    }

    protected String getSysUserId() {
        return this.getSapphireConnection().getSysuserId();
    }

    protected boolean isBBRuleActive(String ruleName) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        return policy != null && "Active".equals(policy.getPropertyListNotNull("rules").getProperty(ruleName, "Active"));
    }

    protected String translate(String text) {
        return this.getTranslationProcessor().translate(text);
    }
}

