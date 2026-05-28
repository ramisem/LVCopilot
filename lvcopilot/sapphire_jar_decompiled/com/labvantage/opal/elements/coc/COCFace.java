/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.coc;

import java.util.List;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;

public interface COCFace {
    public String getGroupBar();

    public String getFromTab();

    public String getToTab();

    public String getWitnessTab(List var1);

    public String getDataTab();

    public void setElement(PropertyList var1);

    public void setTranslationProcessor(TranslationProcessor var1);
}

