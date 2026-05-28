/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.OutputStream;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.Browser;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;

public class GradientImageRequest
extends BaseRequest {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        block36: {
            try {
                boolean noCache;
                int iwidth;
                int iheight;
                String width;
                String height;
                String color3;
                String color2;
                String color1 = request.getParameter("color1");
                if (color1 == null || color1.length() == 0) {
                    color1 = "#F2FFDF";
                }
                if ((color2 = request.getParameter("color2")) == null || color2.length() == 0) {
                    color2 = color1;
                }
                if ((color3 = request.getParameter("color3")) == null || color3.length() == 0) {
                    color3 = color2;
                }
                if ((height = request.getParameter("height")) == null || height.length() == 0) {
                    height = "200";
                }
                if ((width = request.getParameter("width")) == null || width.length() == 0) {
                    width = "200";
                }
                try {
                    iheight = Integer.parseInt(height);
                }
                catch (Exception e) {
                    iheight = 200;
                }
                try {
                    iwidth = Integer.parseInt(width);
                }
                catch (Exception e) {
                    iwidth = 200;
                }
                boolean forcePNG = request.getParameter("png") != null && request.getParameter("png").equalsIgnoreCase("Y");
                boolean bl = noCache = request.getParameter("nocache") != null && request.getParameter("nocache").equalsIgnoreCase("Y");
                if (noCache) {
                    response.setHeader("Cache-Control", "no-store");
                    response.setHeader("Pragma", "no-cache");
                    response.setDateHeader("Expires", 0L);
                } else {
                    response.setHeader("Cache-Control", "max-age=43200, must-revalidate");
                    response.setHeader("Pragma", "Public");
                }
                Browser b = new Browser(request);
                if (forcePNG || b.isIE() && b.getVersion() < 9.0) {
                    Color c3;
                    Color c2;
                    Field field;
                    Color c1;
                    if (color1.startsWith("#")) {
                        c1 = Color.decode("0x" + color1.substring(1));
                    } else {
                        try {
                            field = Color.class.getField(color1.toLowerCase());
                            c1 = (Color)field.get(null);
                        }
                        catch (Exception e) {
                            c1 = Color.decode("0xF2FFDF");
                        }
                    }
                    if (color2.startsWith("#")) {
                        c2 = Color.decode("0x" + color2.substring(1));
                    } else {
                        try {
                            field = Color.class.getField(color2.toLowerCase());
                            c2 = (Color)field.get(null);
                        }
                        catch (Exception e) {
                            c2 = Color.decode("0xF2FFDF");
                        }
                    }
                    if (color3.startsWith("#")) {
                        c3 = Color.decode("0x" + color3.substring(1));
                    } else {
                        try {
                            field = Color.class.getField(color3.toLowerCase());
                            c3 = (Color)field.get(null);
                        }
                        catch (Exception e) {
                            c3 = Color.decode("0xF2FFDF");
                        }
                    }
                    BufferedImage gradientImage = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(iwidth, iheight);
                    Color[] colors = new Color[]{c1, c2, c3};
                    Point2D.Float start = new Point2D.Float(iwidth / 2, 0.0f);
                    Point2D.Float end = new Point2D.Float(iwidth / 2, iheight);
                    float[] dist = new float[]{0.0f, 0.5f, 1.0f};
                    LinearGradientPaint gradient = new LinearGradientPaint(start, end, dist, colors);
                    Graphics2D g2 = (Graphics2D)gradientImage.getGraphics();
                    try {
                        g2.setPaint(gradient);
                        g2.fillRect(0, 0, iwidth, iheight);
                    }
                    finally {
                        g2.dispose();
                    }
                    response.setHeader("Content-Disposition", "inline;filename=" + HttpUtil.encodeURIComponent("gradient.png"));
                    response.setContentType("image/png");
                    try (ServletOutputStream outputStream = response.getOutputStream();){
                        ImageIO.write((RenderedImage)gradientImage, "png", (OutputStream)outputStream);
                        outputStream.flush();
                        break block36;
                    }
                }
                StringBuffer svg = new StringBuffer();
                svg.append("<?xml version=\"1.0\" ?>");
                svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" preserveAspectRatio=\"none\" version=\"1.0\" width=\"100%\" height=\"100%\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
                svg.append("<defs>");
                svg.append("<linearGradient id=\"myLinearGradient1\" x1=\"0%\" y1=\"0%\" x2=\"0%\" y2=\"100%\" spreadMethod=\"pad\">");
                svg.append("<stop offset=\"0%\"   stop-color=\"").append(color1.startsWith("#") ? color1 : color1).append("\" stop-opacity=\"1\"/>");
                svg.append("<stop offset=\"50%\"   stop-color=\"").append(color2.startsWith("#") ? color2 : color2).append("\" stop-opacity=\"1\"/>");
                svg.append("<stop offset=\"100%\" stop-color=\"").append(color3.startsWith("#") ? color3 : color3).append("\" stop-opacity=\"1\"/>");
                svg.append("</linearGradient>");
                svg.append("</defs>");
                svg.append("<rect width=\"100%\" height=\"100%\" style=\"fill:url(#myLinearGradient1);\" />");
                svg.append("</svg>");
                byte[] out = svg.toString().getBytes("UTF-8");
                try (ServletOutputStream outputStream = response.getOutputStream();){
                    response.setContentType("image/svg+xml");
                    outputStream.write(out);
                    response.setContentLength(out.length);
                }
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
            }
        }
    }
}

