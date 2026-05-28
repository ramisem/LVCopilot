/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pages.prodvariantruleeditor;

import java.util.Calendar;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;

public interface RuleTypeProcessor {
    public String getEditor(QueryProcessor var1, TranslationProcessor var2);

    public String getSummary(String var1, QueryProcessor var2, TranslationProcessor var3) throws SapphireException;

    public boolean evaluateRule(String var1, String var2, String var3, String var4, QueryProcessor var5, TranslationProcessor var6, HashMap var7, Calendar var8, String var9) throws SapphireException;
}

