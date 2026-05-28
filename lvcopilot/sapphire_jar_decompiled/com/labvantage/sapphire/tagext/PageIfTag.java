/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.PageRegion;
import com.labvantage.sapphire.tagext.jstl.core.ConditionalTagSupport;
import java.util.ArrayList;

public class PageIfTag
extends ConditionalTagSupport {
    private String test = "";
    private String name = "";

    public void setTest(String test) {
        this.test = test;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean condition() {
        PageRegion pageRegion;
        ArrayList regionList;
        boolean condition = false;
        if (this.test.equalsIgnoreCase("pageareadefined") && (regionList = (ArrayList)this.pageContext.getRequest().getAttribute("RegionList")) != null && (pageRegion = (PageRegion)regionList.get(regionList.size() - 1)) != null) {
            condition = pageRegion.getPageContent(this.name) != null;
        }
        return condition;
    }
}

