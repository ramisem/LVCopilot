/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.zxing.BarcodeFormat
 *  com.google.zxing.MultiFormatWriter
 *  com.google.zxing.client.j2se.MatrixToImageWriter
 *  com.google.zxing.common.BitMatrix
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.labvantage.sapphire.util.images.ImageRef;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;

public class ImageRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block27: {
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            ConnectionProcessor cp = new ConnectionProcessor();
            cp.setConnectionid(requestContext.getConnectionId());
            boolean cont = true;
            if (request.getParameter("svgimage") != null && request.getParameter("svgimage").length() > 0) {
                String imagexml;
                String svgimage = request.getParameter("svgimage");
                String image = svgimage;
                if (!image.startsWith("flat_black_")) {
                    image = "flat_black_" + image;
                }
                if ((imagexml = ImageRef.getSVGXML(image, request.getParameter("color") != null ? request.getParameter("color") : "", servletContext)) != null && imagexml.length() > 0) {
                    cont = false;
                    response.setHeader("Cache-Control", "max-age=43200, must-revalidate");
                    response.setHeader("Pragma", "Public");
                    response.setHeader("Content-Type", "image/svg+xml");
                    response.setHeader("Content-Disposition", "inline;filename=" + HttpUtil.encodeURIComponent("icon.svg"));
                    try {
                        response.getWriter().write(imagexml);
                    }
                    catch (Exception e) {
                        throw new ServletException("Image failed to be written.");
                    }
                } else {
                    requestContext.setProperty("image", svgimage);
                }
            } else if (request.getParameter("qrdata") != null && !request.getParameter("qrdata").isEmpty()) {
                cont = false;
                response.setHeader("Cache-Control", "max-age=43200, must-revalidate");
                response.setHeader("Pragma", "Public");
                response.setHeader("Content-Type", "image/png");
                response.setHeader("Content-Disposition", "inline;filename=" + HttpUtil.encodeURIComponent(".png"));
                String qrdata = request.getParameter("qrdata");
                String charset = "UTF-8";
                int width = request.getParameter("width") == null ? 200 : Integer.parseInt(request.getParameter("width"));
                int height = request.getParameter("height") == null ? 200 : Integer.parseInt(request.getParameter("height"));
                try {
                    BitMatrix matrix = new MultiFormatWriter().encode(new String(qrdata.getBytes(charset), charset), BarcodeFormat.QR_CODE, width, height);
                    ServletOutputStream out = response.getOutputStream();
                    MatrixToImageWriter.writeToStream((BitMatrix)matrix, (String)"png", (OutputStream)out);
                    out.close();
                    out.close();
                }
                catch (Exception e) {
                    throw new ServletException("QRCode Image failed to be written.");
                }
            }
            if (cont) {
                ImageRef imageRef = ImageRef.getRequestImage(cp.getSapphireConnection(), requestContext);
                if (requestContext.getProperty("clearcache").equalsIgnoreCase("Y")) {
                    imageRef.clearCache(false);
                } else if (requestContext.getProperty("clearcache").equalsIgnoreCase("A")) {
                    imageRef.clearCache(true);
                }
                try {
                    boolean noCache = requestContext.getProperty("nocache").equalsIgnoreCase("Y");
                    if (noCache) {
                        response.setHeader("Cache-Control", "no-store");
                        response.setHeader("Pragma", "no-cache");
                        response.setDateHeader("Expires", 0L);
                    } else {
                        response.setHeader("Cache-Control", "max-age=43200, must-revalidate");
                        response.setHeader("Pragma", "Public");
                    }
                    if (imageRef.getFileType() == ImageRef.FileType.SVG) {
                        String xml = imageRef.getSVGXML(servletContext);
                        response.setHeader("Content-Disposition", "inline;filename=" + HttpUtil.encodeURIComponent("icon.svg"));
                        response.setContentType("image/svg+xml");
                        try {
                            response.getWriter().write(xml);
                            break block27;
                        }
                        catch (Exception e1) {
                            throw new SapphireException("Could not write image to response.");
                        }
                    }
                    byte[] data = imageRef.getBytes(servletContext);
                    if (data != null && data.length > 0) {
                        response.setHeader("Content-Disposition", "inline;filename=" + HttpUtil.encodeURIComponent("icon.png"));
                        response.setContentType("image/png");
                        try (ServletOutputStream outputStream = response.getOutputStream();){
                            outputStream.write(data, 0, data.length);
                            outputStream.flush();
                            break block27;
                        }
                        catch (Exception e1) {
                            throw new SapphireException("Could not write image to response.");
                        }
                    }
                    throw new SapphireException("Could not read resource.");
                }
                catch (Exception e2) {
                    Logger.logError(e2.getMessage());
                    throw new ServletException((Throwable)e2);
                }
            }
        }
    }
}

