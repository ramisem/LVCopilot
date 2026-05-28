/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.http;

import com.labvantage.sapphire.Trace;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MultipartOutputStream {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private static final String NEWLINE = "\r\n";
    private static final String PREFIX = "--";
    private DataOutputStream outputStream;
    private String boundary;

    public MultipartOutputStream(OutputStream theOutput, String theBoundary) {
        this.outputStream = new DataOutputStream(theOutput);
        this.boundary = theBoundary;
    }

    public void writeField(String name, String value) throws IOException {
        if (value == null) {
            value = "";
        }
        this.outputStream.writeBytes(PREFIX);
        this.outputStream.writeBytes(this.boundary);
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"");
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.writeBytes(value);
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.flush();
    }

    public void writeFile(String name, String mimeType, File file) throws IOException {
        this.writeFile(name, mimeType, file.getCanonicalPath(), new FileInputStream(file));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeFile(String name, String mimeType, String fileName, InputStream inputStream) throws IOException {
        this.outputStream.writeBytes(PREFIX);
        this.outputStream.writeBytes(this.boundary);
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"");
        this.outputStream.writeBytes(NEWLINE);
        if (mimeType != null && mimeType.length() > 0) {
            this.outputStream.writeBytes("Content-Type: " + mimeType);
            this.outputStream.writeBytes(NEWLINE);
        }
        this.outputStream.writeBytes(NEWLINE);
        byte[] data = new byte[1024];
        try {
            int read;
            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                this.outputStream.write(data, 0, read);
            }
        }
        finally {
            try {
                inputStream.close();
            }
            catch (Exception e) {
                Trace.logWarn(this.getClass().getName(), "Could not close input stream.");
            }
        }
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.flush();
    }

    public void writeFile(String name, String mimeType, String fileName, byte[] data) throws IOException {
        this.outputStream.writeBytes(PREFIX);
        this.outputStream.writeBytes(this.boundary);
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"");
        this.outputStream.writeBytes(NEWLINE);
        if (mimeType != null && mimeType.length() > 0) {
            this.outputStream.writeBytes("Content-Type: " + mimeType);
            this.outputStream.writeBytes(NEWLINE);
        }
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.write(data, 0, data.length);
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.flush();
    }

    public void close() throws IOException {
        this.outputStream.writeBytes(PREFIX);
        this.outputStream.writeBytes(this.boundary);
        this.outputStream.writeBytes(PREFIX);
        this.outputStream.writeBytes(NEWLINE);
        this.outputStream.flush();
        this.outputStream.close();
    }

    public String getBoundary() {
        return this.boundary;
    }

    public static String createBoundary() {
        return "--------------------" + Long.toString(System.currentTimeMillis(), 16);
    }

    public static String getContentType(String theBoundary) {
        return "multipart/form-data; boundary=" + theBoundary;
    }
}

