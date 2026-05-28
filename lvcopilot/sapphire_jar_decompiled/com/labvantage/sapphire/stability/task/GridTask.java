/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.task;

import sapphire.xml.PropertyList;

public interface GridTask {
    public String getTitle();

    public String getColor();

    public String getSummaryText(PropertyList var1, String var2);

    public String getSummaryHTML(PropertyList var1, String var2);

    public String[] getDetailLevels();
}

