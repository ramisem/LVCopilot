/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.digitalsignature.api;

import java.io.IOException;
import java.io.InputStream;

public interface SignatureValidator {
    public InputStream validateSignature(InputStream var1) throws IOException;
}

