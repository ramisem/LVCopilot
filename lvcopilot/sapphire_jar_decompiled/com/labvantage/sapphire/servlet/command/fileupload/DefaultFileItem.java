/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.command.fileupload;

import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUploadException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class DefaultFileItem
implements FileItem {
    private static int counter = 0;
    private String fileName;
    private String contentType;
    private byte[] content;
    private File storeLocation;
    private ByteArrayOutputStream byteStream;
    private String fieldName;
    private boolean isFormField;

    public DefaultFileItem() {
    }

    protected DefaultFileItem(String fileName, String contentType) {
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (this.content == null) {
            if (this.storeLocation != null) {
                return new FileInputStream(this.storeLocation);
            }
            this.content = this.byteStream.toByteArray();
            this.byteStream = null;
        }
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public String getName() {
        return this.fileName;
    }

    @Override
    public boolean isInMemory() {
        return this.content != null || this.byteStream != null;
    }

    @Override
    public long getSize() {
        if (this.storeLocation != null) {
            return this.storeLocation.length();
        }
        if (this.byteStream != null) {
            return this.byteStream.size();
        }
        return this.content.length;
    }

    @Override
    public byte[] get() {
        if (this.content == null) {
            if (this.storeLocation != null) {
                this.content = new byte[(int)this.getSize()];
                try {
                    FileInputStream fis = new FileInputStream(this.storeLocation);
                    fis.read(this.content);
                }
                catch (Exception e) {
                    this.content = null;
                }
            } else {
                this.content = this.byteStream.toByteArray();
                this.byteStream = null;
            }
        }
        return this.content;
    }

    @Override
    public String getString(String encoding) throws UnsupportedEncodingException {
        return new String(this.get(), encoding);
    }

    @Override
    public String getString() {
        return new String(this.get());
    }

    @Override
    public File getStoreLocation() {
        return this.storeLocation;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(String file) throws Exception {
        if (this.isInMemory()) {
            try (FileOutputStream fout = null;){
                fout = new FileOutputStream(file);
                fout.write(this.get());
            }
        }
        if (this.storeLocation != null) {
            if (!this.storeLocation.renameTo(new File(file))) {
                BufferedInputStream in = null;
                FilterOutputStream out = null;
                try {
                    in = new BufferedInputStream(new FileInputStream(this.storeLocation));
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    byte[] bytes = new byte[2048];
                    int s = 0;
                    while ((s = in.read(bytes)) != -1) {
                        ((BufferedOutputStream)out).write(bytes, 0, s);
                    }
                }
                finally {
                    try {
                        in.close();
                    }
                    catch (Exception exception) {}
                    try {
                        out.close();
                    }
                    catch (Exception exception) {}
                }
            }
        } else {
            throw new FileUploadException("Cannot write uploaded file to disk!");
        }
    }

    @Override
    public void delete() {
        this.byteStream = null;
        this.content = null;
        if (this.storeLocation != null && this.storeLocation.exists()) {
            this.storeLocation.delete();
        }
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public boolean isFormField() {
        return this.isFormField;
    }

    @Override
    public void setIsFormField(boolean state) {
        this.isFormField = state;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (this.storeLocation == null) {
            return this.byteStream;
        }
        return new FileOutputStream(this.storeLocation);
    }

    protected void finalize() {
        if (this.storeLocation != null && this.storeLocation.exists()) {
            this.storeLocation.delete();
        }
    }

    public static FileItem newInstance(String path, String name, String contentType, long requestSize, long threshold) {
        DefaultFileItem item = new DefaultFileItem(name, contentType);
        if (requestSize > threshold) {
            String fileName = DefaultFileItem.getUniqueId();
            fileName = "upload_" + fileName + ".tmp";
            fileName = path + "/" + fileName;
            File f = new File(fileName);
            f.deleteOnExit();
            item.storeLocation = f;
        } else {
            item.byteStream = new ByteArrayOutputStream();
        }
        return item;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String getUniqueId() {
        Class<DefaultFileItem> clazz = DefaultFileItem.class;
        synchronized (DefaultFileItem.class) {
            int current = counter++;
            // ** MonitorExit[var1] (shouldn't be in output)
            String id = Integer.toString(current);
            if (current < 100000000) {
                id = ("00000000" + id).substring(id.length());
            }
            return id;
        }
    }
}

