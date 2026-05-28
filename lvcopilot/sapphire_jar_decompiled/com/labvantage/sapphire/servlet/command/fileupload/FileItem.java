/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.command.fileupload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public interface FileItem
extends Serializable {
    public static final int DEFAULT_UPLOAD_SIZE_THRESHOLD = 10240;

    public InputStream getInputStream() throws IOException;

    public String getContentType();

    public String getName();

    public boolean isInMemory();

    public long getSize();

    public byte[] get();

    public String getString(String var1) throws UnsupportedEncodingException;

    public String getString();

    public File getStoreLocation();

    public void write(String var1) throws Exception;

    public void delete();

    public String getFieldName();

    public void setFieldName(String var1);

    public boolean isFormField();

    public void setIsFormField(boolean var1);

    public OutputStream getOutputStream() throws IOException;
}

