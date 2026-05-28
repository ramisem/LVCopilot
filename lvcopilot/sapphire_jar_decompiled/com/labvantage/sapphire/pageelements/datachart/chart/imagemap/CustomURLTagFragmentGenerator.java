/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.imagemap.URLTagFragmentGenerator
 */
package com.labvantage.sapphire.pageelements.datachart.chart.imagemap;

import java.io.Serializable;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;

public class CustomURLTagFragmentGenerator
implements URLTagFragmentGenerator,
Serializable {
    public String generateURLFragment(String urlText) {
        return urlText;
    }
}

