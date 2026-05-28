/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import java.util.zip.ZipEntry;

public interface ZipFileListener {
    public void fileExtracted(ZipEntry var1, int var2);

    public void fileAdded(ZipEntry var1);
}

