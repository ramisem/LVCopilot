/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataSource
 */
package com.labvantage.sapphire.webservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;

public class ByteArrayDataSource
implements DataSource {
    private byte[] bytes;
    private String contentType;

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getName() {
        return "ByteArrayDataSource";
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.bytes);
    }

    public OutputStream getOutputStream() {
        final ByteArrayDataSource bads = this;
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return new FilterOutputStream(baos){

            @Override
            public void close() throws IOException {
                baos.close();
                bads.setBytes(baos.toByteArray());
            }
        };
    }
}

