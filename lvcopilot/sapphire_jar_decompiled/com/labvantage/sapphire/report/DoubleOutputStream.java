/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report;

import java.io.IOException;
import java.io.OutputStream;

public class DoubleOutputStream
extends OutputStream {
    private OutputStream output1;
    private OutputStream output2;

    public DoubleOutputStream(OutputStream output1, OutputStream output2) {
        this.output1 = output1;
        this.output2 = output2;
    }

    @Override
    public void close() throws IOException {
        this.output1.close();
        this.output2.close();
    }

    @Override
    public void flush() throws IOException {
        this.output1.flush();
        this.output2.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.output1.write(b);
        this.output2.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.output1.write(b, off, len);
        this.output2.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.output1.write(b);
        this.output2.write(b);
    }
}

