/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.datachart.servlet;

import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChartImageStreamer
extends BaseRequest {
    private static final String JPEG_SUFFIX = ".jpeg";
    private static final String PNG_SUFFIX = ".png";
    private static final int BUFFER_SIZE = 1024;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        BufferedOutputStream bos;
        BufferedInputStream bis;
        String mimeType;
        String fileNameParam = request.getParameter("filename");
        if (fileNameParam.isEmpty()) {
            throw new IllegalArgumentException("File name is empty");
        }
        File file = new File(System.getProperty("java.io.tmpdir"), fileNameParam);
        String fileName = file.getName();
        if (fileName.length() <= JPEG_SUFFIX.length()) throw new IllegalArgumentException("Invalid filename: " + fileName);
        if (fileName.substring(fileName.length() - JPEG_SUFFIX.length(), fileName.length()).equals(JPEG_SUFFIX)) {
            mimeType = "image/jpeg";
        } else {
            if (!fileName.substring(fileName.length() - PNG_SUFFIX.length(), fileName.length()).equals(PNG_SUFFIX)) throw new IllegalArgumentException("Unknown file extension: " + fileName);
            mimeType = "image/png";
        }
        if (!file.exists()) throw new ServletException("File not found: " + file.getAbsolutePath());
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
        }
        catch (FileNotFoundException e) {
            throw new ServletException("Image file not found", (Throwable)e);
        }
        response.setHeader("Content-Type", mimeType);
        response.setContentLength((int)file.length());
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        response.setHeader("Last-Modified", sdf.format(new Date(file.lastModified())));
        try {
            bos = new BufferedOutputStream((OutputStream)response.getOutputStream());
        }
        catch (IOException e) {
            throw new ServletException("Response output stream not available", (Throwable)e);
        }
        byte[] input = new byte[1024];
        boolean eof = false;
        while (!eof) {
            int length;
            try {
                length = bis.read(input);
            }
            catch (IOException e) {
                throw new ServletException("Image file cannot be read", (Throwable)e);
            }
            if (length == -1) {
                eof = true;
                continue;
            }
            try {
                bos.write(input, 0, length);
            }
            catch (IOException e) {
                throw new ServletException("Response output stream not writable", (Throwable)e);
            }
        }
        try {
            bos.flush();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        try {
            bis.close();
            return;
        }
        catch (IOException iOException) {}
    }
}

