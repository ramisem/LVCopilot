/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.labvantage.sapphire.servlet.filter.WebLogicOutputStreamWriter
 *  javax.servlet.FilterChain
 *  javax.servlet.FilterConfig
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.WriteListener
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpServletResponseWrapper
 */
package com.labvantage.sapphire.servlet.filter;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.servlet.filter.BaseServletFilter;
import com.labvantage.sapphire.servlet.filter.WebLogicOutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class Compression
extends BaseServletFilter {
    static final String LABVANTAGE_CVS_ID = "$Revision: 75304 $";
    private static final String NONE = "none";
    private static final String GZIP = "gzip";
    private static final String DEFLATE = "deflate";
    private static final String PASSTHROUGH = "passthrough";
    private static final String JUMPOVER = "jumpover";
    private static final String GZIPDEFLATE = "gzipdeflate";
    private static final String DEFLATEGZIP = "deflategzip";
    private ArrayList<String> patternList;
    private String compression;
    private int thresholdBuffer;
    private int maxThresholdBuffer;
    private int middleBuffer;
    private int compressionBuffer;
    private boolean debug = false;
    private int platform = -1;
    public static final String ENCODING = "UTF-8";
    public static final int DEFAULT_INITHRESHOLD = 10024;
    public static final int DEFAULT_MAXTHRESHOLD = 100024;
    private static final String HEADER = "COMPRESSION FILTER: ";
    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        Compression.enabled = enabled;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (Compression.isEnabled()) {
            super.init(filterConfig);
            if (this.compression == null) {
                this.log(HEADER, "Compression filter init - V6.0");
                this.patternList = this.getPatternList(filterConfig);
                this.compression = filterConfig.getInitParameter("compression");
                if (this.compression != null && (this.compression.equalsIgnoreCase(NONE) || this.compression.equalsIgnoreCase(GZIP) || this.compression.equalsIgnoreCase(DEFLATE) || this.compression.equalsIgnoreCase(GZIPDEFLATE) || this.compression.equalsIgnoreCase(DEFLATEGZIP) || this.compression.equalsIgnoreCase(PASSTHROUGH) || this.compression.equalsIgnoreCase(JUMPOVER))) {
                    this.log(HEADER, "Compression is " + this.compression);
                } else {
                    this.compression = NONE;
                    this.log(HEADER, "Warning: No valid compression mode specified in filter options.");
                }
                String buffer = filterConfig.getInitParameter("initprimarybuffer");
                if (buffer != null && buffer.length() > 0) {
                    try {
                        this.thresholdBuffer = Integer.parseInt(buffer);
                    }
                    catch (NumberFormatException e) {
                        this.thresholdBuffer = 10024;
                    }
                } else {
                    buffer = filterConfig.getInitParameter("primarybuffer");
                    if (buffer != null && buffer.length() > 0) {
                        try {
                            this.thresholdBuffer = Integer.parseInt(buffer);
                        }
                        catch (NumberFormatException e) {
                            this.thresholdBuffer = 10024;
                        }
                    } else {
                        this.thresholdBuffer = 10024;
                    }
                }
                this.log(HEADER, "Initial Primary Buffer is " + this.thresholdBuffer);
                buffer = filterConfig.getInitParameter("maxprimarybuffer");
                if (buffer != null && buffer.length() > 0) {
                    try {
                        this.maxThresholdBuffer = Integer.parseInt(buffer);
                    }
                    catch (NumberFormatException e) {
                        this.maxThresholdBuffer = 100024;
                    }
                } else {
                    this.maxThresholdBuffer = 100024;
                }
                this.log(HEADER, "Maximum Primary Buffer is " + this.maxThresholdBuffer);
                buffer = filterConfig.getInitParameter("middlebuffer");
                if (buffer != null && buffer.length() > 0) {
                    try {
                        this.middleBuffer = Integer.parseInt(buffer);
                        this.log(HEADER, "Middle Buffer is " + this.middleBuffer);
                    }
                    catch (NumberFormatException e) {
                        this.middleBuffer = 0;
                    }
                } else {
                    this.middleBuffer = 0;
                }
                this.log(HEADER, "Direct write is " + (this.middleBuffer > 0 ? "OFF" : "ON"));
                buffer = filterConfig.getInitParameter("compressionbuffer");
                if (buffer != null && buffer.length() > 0) {
                    try {
                        this.compressionBuffer = Integer.parseInt(buffer);
                        this.log(HEADER, "Compression Buffer is " + this.compressionBuffer);
                    }
                    catch (NumberFormatException e) {
                        this.compressionBuffer = 0;
                    }
                } else {
                    this.compressionBuffer = 0;
                }
                this.debug = filterConfig.getInitParameter("debug") != null && filterConfig.getInitParameter("debug").equalsIgnoreCase("y");
            }
        }
    }

    private void debugMsg(String msg) {
        if (this.debug) {
            this.log("DEBUG: " + msg);
        }
    }

    private void debugMsg(String methodname, StackTraceElement[] ste) {
        if (this.debug) {
            if (ste != null && ste.length > 5) {
                this.log("DEBUG: " + methodname + " called from " + ste[5].getClassName() + "." + ste[5].getMethodName());
            } else {
                this.log("DEBUG: " + methodname + " called from anon");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (Compression.isEnabled()) {
            if (this.platform == -1) {
                try {
                    this.platform = Configuration.getInstance().getPlatform();
                }
                catch (SapphireException e) {
                    this.log(HEADER, "Warning: Failed to determine platform - defaulting to JBoss");
                    this.platform = 4;
                }
                this.debugMsg("platform = " + this.platform);
            }
            if (this.platform == 3 && (this.compression.equalsIgnoreCase(DEFLATE) || this.compression.equalsIgnoreCase(GZIPDEFLATE) || this.compression.equalsIgnoreCase(DEFLATEGZIP) || this.compression.equalsIgnoreCase(JUMPOVER))) {
                this.log("Compression '" + this.compression + "' is not supported with WebLogic - defaulting to GZIP");
                this.compression = GZIP;
            }
            this.debugMsg("compression = " + this.compression);
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            if (this.debug) {
                this.debugMsg("request by " + servletRequest.getRemoteHost() + " (" + servletRequest.getRemoteAddr() + ")");
            }
            boolean compress = true;
            if (this.platform == 3 && servletRequest.getAttribute("weblogic.servlet.gzip.filter") != null) {
                compress = false;
            }
            servletRequest.setAttribute("weblogic.servlet.gzip.filter", (Object)"true");
            if (!(servletResponse instanceof HttpServletResponse) || !(servletRequest instanceof HttpServletRequest)) {
                this.debugMsg("not Http");
                compress = false;
            } else if (this.debug) {
                if (request.getQueryString() != null) {
                    this.debugMsg("request url = " + ((HttpServletRequest)servletRequest).getRequestURL().toString() + "?" + request.getQueryString());
                } else {
                    this.debugMsg("request url = " + ((HttpServletRequest)servletRequest).getRequestURL().toString());
                }
            }
            String acceptEncoding = request.getHeader("accept-encoding");
            if (acceptEncoding == null || acceptEncoding.toLowerCase().indexOf(GZIP) == -1 && acceptEncoding.toLowerCase().indexOf(DEFLATE) == -1) {
                compress = false;
            }
            if (this.compression.equalsIgnoreCase(NONE)) {
                compress = false;
            }
            this.debugMsg("compress = " + compress);
            if (compress) {
                if (this.matchRequest(request)) {
                    this.debugMsg("request matched");
                    boolean gzipSupported = false;
                    boolean deflateSupported = false;
                    if (acceptEncoding.toLowerCase().indexOf(GZIP) != -1) {
                        gzipSupported = true;
                    }
                    if (acceptEncoding.toLowerCase().indexOf(DEFLATE) != -1) {
                        deflateSupported = true;
                    }
                    String c = gzipSupported && deflateSupported ? (this.compression.equalsIgnoreCase(GZIP) || this.compression.equalsIgnoreCase(GZIPDEFLATE) ? GZIP : (this.compression.equalsIgnoreCase(DEFLATE) || this.compression.equalsIgnoreCase(DEFLATEGZIP) ? DEFLATE : (this.compression.equalsIgnoreCase(PASSTHROUGH) || this.compression.equalsIgnoreCase(JUMPOVER) ? this.compression : NONE))) : (gzipSupported && !deflateSupported ? (this.compression.equalsIgnoreCase(PASSTHROUGH) || this.compression.equalsIgnoreCase(JUMPOVER) ? this.compression : (!this.compression.equalsIgnoreCase(DEFLATE) ? GZIP : NONE)) : (!gzipSupported && deflateSupported ? (this.compression.equalsIgnoreCase(PASSTHROUGH) || this.compression.equalsIgnoreCase(JUMPOVER) ? this.compression : (!this.compression.equalsIgnoreCase(GZIP) ? DEFLATE : NONE)) : NONE));
                    if (!c.equalsIgnoreCase(NONE)) {
                        CompressionResponseWrapper res = new CompressionResponseWrapper((HttpServletResponse)servletResponse, c, this.platform, this.thresholdBuffer, this.maxThresholdBuffer, this.middleBuffer, this.compressionBuffer);
                        try {
                            this.debugMsg("calling doFilter 1");
                            filterChain.doFilter((ServletRequest)request, (ServletResponse)res);
                        }
                        finally {
                            res.complete();
                        }
                    }
                } else {
                    this.debugMsg("calling doFilter 2");
                    filterChain.doFilter((ServletRequest)request, servletResponse);
                }
            } else {
                this.debugMsg("calling doFilter 3");
                filterChain.doFilter((ServletRequest)request, servletResponse);
            }
        } else {
            this.debugMsg("calling doFilter 4");
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private ArrayList<String> getPatternList(FilterConfig filterConfig) {
        String patternList = filterConfig.getInitParameter("PatternList");
        ArrayList<String> list = new ArrayList<String>();
        if (patternList != null) {
            String[] patterns;
            this.log(HEADER, "PatternList = " + patternList);
            for (String pattern : patterns = StringUtil.split(patternList, ";")) {
                StringBuffer sb = new StringBuffer(pattern.length() + 10);
                for (int j = 0; j < pattern.length(); ++j) {
                    if (pattern.charAt(j) == '*') {
                        sb.append(".*");
                        continue;
                    }
                    sb.append(pattern.charAt(j));
                }
                list.add(sb.toString());
            }
        }
        return list;
    }

    private boolean matchRequest(HttpServletRequest request) {
        String path;
        if (request.getCharacterEncoding() == null) {
            try {
                request.setCharacterEncoding(ENCODING);
                if (request.getParameter("nocompress") != null && request.getParameter("nocompress").equalsIgnoreCase("Y")) {
                    this.log(HEADER, "Compression forced to skip...");
                    return false;
                }
            }
            catch (Exception e) {
                this.log(HEADER, "Could not set encoding. " + e.getMessage());
            }
        }
        if ((path = request.getServletPath()) == null) {
            return false;
        }
        String param = request.getQueryString();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            path = path + pathInfo;
        }
        if (param != null) {
            path = path + "&" + param;
        }
        if (this.patternList == null || this.patternList.size() == 0) {
            return true;
        }
        boolean pathInCollection = false;
        for (String np : this.patternList) {
            Pattern p = Pattern.compile(np);
            Matcher m = p.matcher(path);
            if (!m.matches()) continue;
            pathInCollection = true;
            break;
        }
        String pathSpec = "exclude";
        return pathInCollection && pathSpec.equalsIgnoreCase("include") || !pathInCollection && pathSpec.equalsIgnoreCase("exclude");
    }

    private class CompressedOutputStream
    extends ServletOutputStream {
        protected boolean completed = false;
        protected boolean closed = false;
        private FilterOutputStream compressedOutputStream;
        private ByteArrayOutputStream middleMan;
        private GZIPOutputStream wlOutputStream;
        private HttpServletResponse servletResponse;
        private ServletOutputStream servletOutputStream;
        private String compression;
        private int platform;
        protected int bufferCount = 0;
        protected int length = 0;
        protected int thresholdBuffer = 10024;
        protected int maxthresholdBuffer = 100024;
        protected int middleBuffer = 0;
        protected int compressionBuffer = 0;
        protected ByteArrayOutputStream buffer = null;

        public boolean isClosed() {
            return this.closed;
        }

        public CompressedOutputStream(HttpServletResponse servletResponse, String compression2, int platform, int thresholdBuffer, int maxthresholdBuffer, int middleBuffer, int compressionBuffer) throws IOException {
            this.servletResponse = servletResponse;
            this.platform = platform;
            this.compression = compression2;
            this.thresholdBuffer = thresholdBuffer;
            this.maxthresholdBuffer = maxthresholdBuffer;
            this.middleBuffer = middleBuffer;
            this.compressionBuffer = compressionBuffer;
            this.servletOutputStream = servletResponse.getOutputStream();
            if (platform == 3) {
                if (!servletResponse.containsHeader("Content-Encoding") && (compression2.equalsIgnoreCase(Compression.DEFLATE) || compression2.equalsIgnoreCase(Compression.GZIP))) {
                    servletResponse.addHeader("Content-Encoding", Compression.GZIP);
                }
                this.wlOutputStream = new GZIPOutputStream(new OutputStream(){

                    @Override
                    public void write(int i) throws IOException {
                        CompressedOutputStream.this.servletOutputStream.write(i);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        CompressedOutputStream.this.servletOutputStream.write(b, off, len);
                    }

                    @Override
                    public void flush() throws IOException {
                    }

                    @Override
                    public void close() throws IOException {
                    }
                });
            } else {
                if (!compression2.equalsIgnoreCase(Compression.JUMPOVER) && !servletResponse.containsHeader("Content-Encoding") && (compression2.equalsIgnoreCase(Compression.DEFLATE) || compression2.equalsIgnoreCase(Compression.GZIP))) {
                    servletResponse.addHeader("Content-Encoding", compression2);
                }
                this.setBuffer(thresholdBuffer);
            }
        }

        protected void setBuffer(int threshold) {
            this.thresholdBuffer = threshold;
            this.bufferCount = 0;
            if (threshold > 0) {
                Compression.this.debugMsg("Compression buffer set to " + this.thresholdBuffer);
                this.buffer = new ByteArrayOutputStream(this.thresholdBuffer);
            } else {
                Compression.this.debugMsg("Compression buffer turned off");
                this.buffer = null;
            }
        }

        protected void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public void write(int b) throws IOException {
            if (this.compression.equalsIgnoreCase(Compression.JUMPOVER)) {
                this.servletOutputStream.write(b);
            } else if (this.platform == 3) {
                this.wlOutputStream.write(b);
            } else if (this.closed) {
                Compression.this.log(Compression.HEADER, "WARN: Cannot write to a closed output stream");
            } else if (this.buffer != null) {
                if (this.maxthresholdBuffer > 0 && this.bufferCount >= this.maxthresholdBuffer) {
                    this.flushToGZip();
                }
                this.buffer.write(b);
            } else {
                this.writeToGZip(new byte[]{(byte)b}, 0, 1);
            }
        }

        public void flushToGZip() throws IOException {
            if (this.buffer != null && this.bufferCount > 0 && this.buffer.size() > 0) {
                this.writeToGZip(this.buffer.toByteArray(), 0, this.buffer.size());
                this.buffer.reset();
                this.bufferCount = 0;
            }
        }

        private void setupStreams() throws IOException {
            if (this.compression.equals(Compression.GZIP)) {
                if (this.middleBuffer > 0) {
                    this.middleMan = new ByteArrayOutputStream(this.middleBuffer);
                    this.compressedOutputStream = this.compressionBuffer > 0 ? new GZIPOutputStream((OutputStream)this.middleMan, this.compressionBuffer) : new GZIPOutputStream(new BufferedOutputStream(this.middleMan));
                } else {
                    this.compressedOutputStream = this.compressionBuffer > 0 ? new GZIPOutputStream((OutputStream)this.servletOutputStream, this.compressionBuffer) : new GZIPOutputStream((OutputStream)this.servletOutputStream);
                }
            } else if (this.compression.equals(Compression.DEFLATE)) {
                if (this.middleBuffer > 0) {
                    this.middleMan = new ByteArrayOutputStream(this.middleBuffer);
                    this.compressedOutputStream = this.compressionBuffer > 0 ? new DeflaterOutputStream((OutputStream)this.middleMan, new Deflater(), this.compressionBuffer) : new DeflaterOutputStream(this.middleMan);
                } else {
                    this.compressedOutputStream = this.compressionBuffer > 0 ? new DeflaterOutputStream((OutputStream)this.servletOutputStream, new Deflater(), this.compressionBuffer) : new DeflaterOutputStream((OutputStream)this.servletOutputStream);
                }
            } else if (this.compression.equals(Compression.PASSTHROUGH)) {
                if (this.middleBuffer > 0) {
                    this.middleMan = new ByteArrayOutputStream(this.middleBuffer);
                    this.compressedOutputStream = new FilterOutputStream(this.middleMan);
                } else {
                    this.compressedOutputStream = new FilterOutputStream((OutputStream)this.servletOutputStream);
                }
            }
        }

        public void writeToGZip(byte[] b, int off, int len) throws IOException {
            if (this.compressedOutputStream == null && !this.closed) {
                this.setupStreams();
            }
            this.length += len;
            this.compressedOutputStream.write(b, off, len);
        }

        public void write(byte[] bytes) throws IOException {
            if (this.compression.equalsIgnoreCase(Compression.JUMPOVER)) {
                this.servletOutputStream.write(bytes);
            } else {
                this.write(bytes, 0, bytes.length);
            }
        }

        public void write(byte[] bytes, int o, int leng) throws IOException {
            if (this.compression.equalsIgnoreCase(Compression.JUMPOVER)) {
                this.servletOutputStream.write(bytes, o, leng);
            } else if (this.platform == 3) {
                this.wlOutputStream.write(bytes, o, leng);
            } else {
                if (this.closed) {
                    throw new IOException("Cannot write to a closed output stream");
                }
                if (this.buffer != null) {
                    if (leng > 0) {
                        if (this.maxthresholdBuffer <= 0 || leng <= this.maxthresholdBuffer - this.bufferCount) {
                            this.buffer.write(bytes, o, leng);
                            this.bufferCount += leng;
                        } else {
                            this.flushToGZip();
                            if (leng <= this.maxthresholdBuffer - this.bufferCount) {
                                this.buffer.write(bytes, o, leng);
                                this.bufferCount += leng;
                            } else {
                                this.writeToGZip(bytes, 0, leng);
                            }
                        }
                    }
                } else {
                    this.writeToGZip(bytes, o, leng);
                }
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void close() throws IOException {
            if (this.compression.equalsIgnoreCase(Compression.JUMPOVER)) {
                this.servletOutputStream.close();
                this.closed = true;
            } else if (this.platform != 3) {
                if (this.closed) {
                    Compression.this.log(Compression.HEADER, "WARN:This output stream has already been closed");
                } else {
                    if (!this.servletResponse.containsHeader("Content-Encoding") && (this.compression.equalsIgnoreCase(Compression.DEFLATE) || this.compression.equalsIgnoreCase(Compression.GZIP))) {
                        this.servletResponse.addHeader("Content-Encoding", this.compression);
                    }
                    try {
                        try {
                            try {
                                try {
                                    this.flushToGZip();
                                }
                                finally {
                                    if (this.buffer != null) {
                                        this.buffer.close();
                                        this.bufferCount = 0;
                                        this.buffer = null;
                                    }
                                }
                                if (this.compressedOutputStream != null) {
                                    this.compressedOutputStream.flush();
                                }
                            }
                            finally {
                                if (this.compressedOutputStream != null) {
                                    this.compressedOutputStream.close();
                                    this.compressedOutputStream = null;
                                }
                            }
                            if (this.middleBuffer > 0 && this.middleMan != null && this.servletOutputStream != null) {
                                this.middleMan.flush();
                                byte[] end = this.middleMan.toByteArray();
                                this.servletOutputStream.write(end, 0, end.length);
                            }
                        }
                        finally {
                            if (this.middleMan != null) {
                                this.middleMan.close();
                                this.middleMan = null;
                            }
                        }
                    }
                    finally {
                        this.servletOutputStream.close();
                        this.closed = true;
                    }
                }
            }
        }

        public void flush() throws IOException {
            if (this.compression.equalsIgnoreCase(Compression.JUMPOVER)) {
                this.servletOutputStream.flush();
            } else if (this.platform == 3) {
                if (this.wlOutputStream != null) {
                    this.wlOutputStream.flush();
                }
                if (this.servletOutputStream != null) {
                    this.servletOutputStream.flush();
                }
            } else {
                if (this.closed) {
                    throw new IOException("Cannot flush a closed output stream");
                }
                this.flushToGZip();
                if (this.compressedOutputStream != null) {
                    this.compressedOutputStream.flush();
                }
            }
        }

        public void finish() throws IOException {
            if (this.platform == 3 && this.wlOutputStream != null) {
                this.wlOutputStream.flush();
                this.wlOutputStream.finish();
            }
        }

        public void reset() {
            if (this.platform == 3 && this.wlOutputStream != null) {
                this.wlOutputStream = null;
            }
        }

        public boolean isReady() {
            return true;
        }

        public void setWriteListener(WriteListener writeListener) {
        }
    }

    private class CompressionResponseWrapper
    extends HttpServletResponseWrapper {
        private static final String REDIRECT = "rc?command=file&file=WEB-CORE/error/error.jsp&errormsg=404,%20Resource%20could%20not%20be%20found";
        private HttpServletResponse servletResponse;
        private ServletOutputStream strm;
        private PrintWriter wrtr;
        private String compression;
        private int platform;
        private int thresholdBuffer;
        private int maxThresholdBuffer;
        private int middleBuffer;
        private int compressionBuffer;

        public void setStatus(int sc) {
            if (sc == 404) {
                Compression.this.log(Compression.HEADER, "setStatus: " + sc);
                try {
                    this.servletResponse.sendRedirect(REDIRECT);
                }
                catch (Exception e) {
                    Compression.this.log(Compression.HEADER, "setStatus: Failed to redirect 404");
                }
            } else {
                super.setStatus(sc);
            }
        }

        public void sendError(int sc) throws IOException {
            Compression.this.log(Compression.HEADER, "sendError: " + sc);
            if (sc == 404) {
                this.servletResponse.sendRedirect(REDIRECT);
            } else {
                super.sendError(sc);
            }
        }

        public void sendError(int sc, String msg) throws IOException {
            Compression.this.log(Compression.HEADER, "sendError: " + sc + " " + msg);
            if (sc == 404) {
                this.servletResponse.sendRedirect(REDIRECT);
            } else {
                super.sendError(sc, msg);
            }
        }

        public ServletResponse getResponse() {
            if (Compression.this.debug) {
                Compression.this.debugMsg("getResponse", Thread.currentThread().getStackTrace());
            }
            return this.servletResponse;
        }

        public CompressionResponseWrapper(HttpServletResponse servletResponse, String compression2, int platform, int thresholdBuffer, int maxThresholdBuffer, int middleBuffer, int compressionBuffer) {
            super(servletResponse);
            this.servletResponse = servletResponse;
            if (!servletResponse.containsHeader("Content-Encoding") && (compression2.equalsIgnoreCase(Compression.DEFLATE) || compression2.equalsIgnoreCase(Compression.GZIP))) {
                servletResponse.addHeader("Content-Encoding", compression2);
            }
            this.compression = compression2;
            this.platform = platform;
            this.thresholdBuffer = thresholdBuffer;
            this.maxThresholdBuffer = maxThresholdBuffer;
            this.middleBuffer = middleBuffer;
            this.compressionBuffer = compressionBuffer;
        }

        public ServletOutputStream createOutputStream() throws IOException {
            if (Compression.this.debug) {
                Compression.this.debugMsg("createOutputStream", Thread.currentThread().getStackTrace());
            }
            if (this.strm == null) {
                return new CompressedOutputStream(this.servletResponse, this.compression, this.platform, this.thresholdBuffer, this.maxThresholdBuffer, this.middleBuffer, this.compressionBuffer);
            }
            return this.strm;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public PrintWriter getWriter() throws IOException {
            if (Compression.this.debug) {
                Compression.this.debugMsg("getWriter", Thread.currentThread().getStackTrace());
            }
            if (this.wrtr != null) {
                return this.wrtr;
            }
            if (this.strm != null) {
                if (this.platform == 3) {
                    throw new IllegalStateException("getOutputStream() has already been called for this response(3)");
                }
                Compression.this.debugMsg("Stream already created. Will try and dump and recreate...");
                if (!(this.strm instanceof CompressedOutputStream)) throw new IllegalStateException("getOutputStream() has already been called for this response(2)");
                try {
                    if (!((CompressedOutputStream)this.strm).closed) {
                        this.strm.close();
                    }
                    this.strm = null;
                    this.strm = this.createOutputStream();
                }
                catch (Exception e) {
                    throw new IllegalStateException("getOutputStream() has already been called for this response(1)");
                }
            } else {
                this.strm = this.createOutputStream();
            }
            if (this.platform == 3) {
                try {
                    String charEnc = this.servletResponse.getCharacterEncoding();
                    WebLogicOutputStreamWriter wlos = charEnc != null ? new WebLogicOutputStreamWriter((OutputStream)this.strm, charEnc) : new WebLogicOutputStreamWriter((OutputStream)this.strm, Compression.ENCODING);
                    this.wrtr = new PrintWriter((Writer)wlos);
                    return this.wrtr;
                }
                catch (Exception e) {
                    throw new IOException("Failed to create WebLogic Output Stream. Reason: " + e.getMessage());
                }
            } else {
                String charEnc = this.servletResponse.getCharacterEncoding();
                this.wrtr = charEnc != null ? new PrintWriter(new OutputStreamWriter((OutputStream)this.strm, charEnc)) : new PrintWriter(new OutputStreamWriter((OutputStream)this.strm, Compression.ENCODING));
            }
            return this.wrtr;
        }

        public void complete() throws IOException {
            block16: {
                if (Compression.this.debug) {
                    Compression.this.debugMsg("complete", Thread.currentThread().getStackTrace());
                }
                if (!this.servletResponse.containsHeader("Content-Encoding") && (this.compression.equalsIgnoreCase(Compression.DEFLATE) || this.compression.equalsIgnoreCase(Compression.GZIP))) {
                    this.servletResponse.addHeader("Content-Encoding", this.compression);
                }
                if (this.platform == 3) {
                    this.finish();
                } else {
                    try {
                        if (this.strm != null || this.wrtr != null) {
                            if (this.strm != null) {
                                ((CompressedOutputStream)this.strm).setCompleted(true);
                            }
                            if (this.wrtr != null) {
                                this.wrtr.close();
                            }
                            if (this.strm != null) {
                                if (!((CompressedOutputStream)this.strm).closed) {
                                    this.strm.close();
                                }
                                this.strm = null;
                            }
                            break block16;
                        }
                        Compression.this.debugMsg("Completing URL with no content...");
                        this.wrtr = this.getWriter();
                        try {
                            this.strm = this.getOutputStream();
                            if (this.strm != null) {
                                ((CompressedOutputStream)this.strm).setCompleted(true);
                            }
                        }
                        finally {
                            if (this.wrtr != null) {
                                this.wrtr.close();
                            }
                        }
                    }
                    catch (IOException e) {
                        Compression.this.log(Compression.HEADER, "Exception was thrown in compression wrapper complete(1). Error: " + e.getMessage());
                    }
                }
            }
        }

        public ServletOutputStream getOutputStream() throws IOException {
            if (Compression.this.debug) {
                Compression.this.debugMsg("getOutputStream", Thread.currentThread().getStackTrace());
            }
            if (this.wrtr != null) {
                Compression.this.debugMsg("getWriter() has already been called for this response(4). Will allow continue...");
            }
            if (this.strm == null) {
                this.strm = this.createOutputStream();
            }
            return this.strm;
        }

        public void flushBuffer() throws IOException {
            if (Compression.this.debug) {
                Compression.this.debugMsg("flushBuffer", Thread.currentThread().getStackTrace());
            }
            if (this.strm != null && this.strm instanceof CompressedOutputStream && !((CompressedOutputStream)this.strm).closed) {
                this.strm.flush();
                if (this.platform == 3) {
                    super.flushBuffer();
                }
            }
        }

        public void resetBuffer() {
            if (Compression.this.debug) {
                Compression.this.debugMsg("resetBuffer", Thread.currentThread().getStackTrace());
            }
            if (this.platform == 3) {
                super.resetBuffer();
            }
        }

        void finish() throws IOException {
            if (Compression.this.debug) {
                Compression.this.debugMsg("finish", Thread.currentThread().getStackTrace());
            }
            if (this.strm != null) {
                ((CompressedOutputStream)this.strm).finish();
            }
        }

        public void setContentLength(int len) {
        }
    }
}

