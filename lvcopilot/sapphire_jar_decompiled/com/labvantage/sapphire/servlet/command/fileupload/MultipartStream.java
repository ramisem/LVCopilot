/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.command.fileupload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import sapphire.util.Logger;

public class MultipartStream {
    public static final int HEADER_PART_SIZE_MAX = 10240;
    protected static final int DEFAULT_BUFSIZE = 4096;
    protected static final byte[] HEADER_SEPARATOR = new byte[]{13, 10, 13, 10};
    protected static final byte[] FIELD_SEPARATOR = new byte[]{13, 10};
    protected static final byte[] STREAM_TERMINATOR = new byte[]{45, 45};
    private InputStream input;
    private int boundaryLength;
    private int keepRegion;
    private byte[] boundary;
    private int bufSize;
    private byte[] buffer;
    private int head;
    private int tail;

    public MultipartStream() {
    }

    public MultipartStream(InputStream input, byte[] boundary, int bufSize) {
        this.input = input;
        this.bufSize = bufSize;
        this.buffer = new byte[bufSize];
        this.boundary = new byte[boundary.length + 4];
        this.boundaryLength = boundary.length + 4;
        this.keepRegion = boundary.length + 3;
        this.boundary[0] = 13;
        this.boundary[1] = 10;
        this.boundary[2] = 45;
        this.boundary[3] = 45;
        System.arraycopy(boundary, 0, this.boundary, 4, boundary.length);
        this.head = 0;
        this.tail = 0;
    }

    public MultipartStream(InputStream input, byte[] boundary) throws IOException {
        this(input, boundary, 4096);
    }

    public byte readByte() throws IOException {
        if (this.head == this.tail) {
            this.head = 0;
            this.tail = this.input.read(this.buffer, this.head, this.bufSize);
            if (this.tail == -1) {
                throw new IOException("No more data is available");
            }
        }
        return this.buffer[this.head++];
    }

    public boolean readBoundary() throws MalformedStreamException {
        boolean nextChunk;
        block4: {
            byte[] marker = new byte[2];
            nextChunk = false;
            this.head += this.boundaryLength;
            try {
                marker[0] = this.readByte();
                marker[1] = this.readByte();
                if (MultipartStream.arrayequals(marker, STREAM_TERMINATOR, 2)) {
                    nextChunk = false;
                    break block4;
                }
                if (MultipartStream.arrayequals(marker, FIELD_SEPARATOR, 2)) {
                    nextChunk = true;
                    break block4;
                }
                throw new MalformedStreamException("Unexpected characters follow a boundary");
            }
            catch (IOException e) {
                throw new MalformedStreamException("Stream ended unexpectedly");
            }
        }
        return nextChunk;
    }

    public void setBoundary(byte[] boundary) throws IllegalBoundaryException {
        if (boundary.length != this.boundaryLength - 4) {
            throw new IllegalBoundaryException("The length of a boundary token can not be changed");
        }
        System.arraycopy(boundary, 0, this.boundary, 4, boundary.length);
    }

    public String readHeaders() throws MalformedStreamException {
        int i = 0;
        byte[] b = new byte[1];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int sizeMax = 10240;
        int size = 0;
        while (i < 4) {
            try {
                b[0] = this.readByte();
            }
            catch (IOException e) {
                throw new MalformedStreamException("Stream ended unexpectedly");
            }
            i = b[0] == HEADER_SEPARATOR[i] ? ++i : 0;
            if (++size > sizeMax) continue;
            baos.write(b[0]);
        }
        try {
            return new String(baos.toByteArray(), "UTF-8");
        }
        catch (Exception e) {
            Logger.logWarn("Could not retrieve file in UTF-8 encoding.");
            return baos.toString();
        }
    }

    public int readBodyData(OutputStream output) throws MalformedStreamException, IOException {
        boolean done = false;
        int total = 0;
        while (!done) {
            int pos = this.findSeparator();
            if (pos != -1) {
                output.write(this.buffer, this.head, pos - this.head);
                total += pos - this.head;
                this.head = pos;
                done = true;
                continue;
            }
            int pad = this.tail - this.head > this.keepRegion ? this.keepRegion : this.tail - this.head;
            output.write(this.buffer, this.head, this.tail - this.head - pad);
            total += this.tail - this.head - pad;
            System.arraycopy(this.buffer, this.tail - pad, this.buffer, 0, pad);
            this.head = 0;
            int bytesRead = this.input.read(this.buffer, pad, this.bufSize - pad);
            if (bytesRead != -1) {
                this.tail = pad + bytesRead;
                continue;
            }
            output.write(this.buffer, 0, pad);
            output.flush();
            total += pad;
            throw new MalformedStreamException("Stream ended unexpectedly");
        }
        output.flush();
        return total;
    }

    public int discardBodyData() throws MalformedStreamException, IOException {
        boolean done = false;
        int total = 0;
        while (!done) {
            int pos = this.findSeparator();
            if (pos != -1) {
                total += pos - this.head;
                this.head = pos;
                done = true;
                continue;
            }
            int pad = this.tail - this.head > this.keepRegion ? this.keepRegion : this.tail - this.head;
            total += this.tail - this.head - pad;
            System.arraycopy(this.buffer, this.tail - pad, this.buffer, 0, pad);
            this.head = 0;
            int bytesRead = this.input.read(this.buffer, pad, this.bufSize - pad);
            if (bytesRead != -1) {
                this.tail = pad + bytesRead;
                continue;
            }
            total += pad;
            throw new MalformedStreamException("Stream ended unexpectedly");
        }
        return total;
    }

    public boolean skipPreamble() throws IOException {
        System.arraycopy(this.boundary, 2, this.boundary, 0, this.boundary.length - 2);
        this.boundaryLength = this.boundary.length - 2;
        try {
            this.discardBodyData();
            boolean bl = this.readBoundary();
            return bl;
        }
        catch (MalformedStreamException e) {
            boolean bl = false;
            return bl;
        }
        finally {
            System.arraycopy(this.boundary, 0, this.boundary, 2, this.boundary.length - 2);
            this.boundaryLength = this.boundary.length;
            this.boundary[0] = 13;
            this.boundary[1] = 10;
        }
    }

    public static boolean arrayequals(byte[] a, byte[] b, int count) {
        for (int i = 0; i < count; ++i) {
            if (a[i] == b[i]) continue;
            return false;
        }
        return true;
    }

    protected int findByte(byte value, int pos) {
        for (int i = pos; i < this.tail; ++i) {
            if (this.buffer[i] != value) continue;
            return i;
        }
        return -1;
    }

    protected int findSeparator() {
        int first;
        int match = 0;
        int maxpos = this.tail - this.boundaryLength;
        for (first = this.head; first <= maxpos && match != this.boundaryLength; ++first) {
            if ((first = this.findByte(this.boundary[0], first)) == -1 || first > maxpos) {
                return -1;
            }
            for (match = 1; match < this.boundaryLength && this.buffer[first + match] == this.boundary[match]; ++match) {
            }
        }
        if (match == this.boundaryLength) {
            return first - 1;
        }
        return -1;
    }

    public class IllegalBoundaryException
    extends IOException {
        public IllegalBoundaryException() {
        }

        public IllegalBoundaryException(String message) {
            super(message);
        }
    }

    public class MalformedStreamException
    extends IOException {
        public MalformedStreamException() {
        }

        public MalformedStreamException(String message) {
            super(message);
        }
    }
}

