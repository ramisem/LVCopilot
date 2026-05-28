/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.childsampleplan;

import com.labvantage.opal.elements.childsampleplan.BaseChildSamplePlan;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;

public class ChildSamplePlan
extends BaseChildSamplePlan {
    @Override
    public String getHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append(JavaScriptAPITag.getJQueryAPI(true, false, null, this.pageContext));
        sb.append(this.getElementHtml(this.requestContext.getProperty("keyid1"), this.requestContext.getProperty("keyid2")));
        return sb.toString();
    }
}

