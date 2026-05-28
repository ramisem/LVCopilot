/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import java.io.File;

public interface FileOperationListener {
    public void fileDeleted(File var1);

    public void fileCopied(File var1, File var2);

    public void fileRenamed(File var1, File var2);
}

