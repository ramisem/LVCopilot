/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.digitalsignature.api;

import java.io.IOException;
import sapphire.xml.PropertyList;

public interface CSRHandler {
    public String generateCSR(PropertyList var1, String var2, String var3) throws IOException;
}

