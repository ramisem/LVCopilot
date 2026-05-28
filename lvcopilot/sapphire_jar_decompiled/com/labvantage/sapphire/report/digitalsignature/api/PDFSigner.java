/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.digitalsignature.api;

import com.labvantage.sapphire.report.digitalsignature.api.SignatureData;
import java.io.IOException;
import java.io.InputStream;
import sapphire.xml.PropertyList;

public interface PDFSigner {
    public InputStream signDocument(PropertyList var1, InputStream var2, SignatureData var3) throws IOException;
}

