/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.beans;

import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Image
extends BaseRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        BufferedImage image = new BufferedImage(500, 400, 1);
        Graphics2D graphics = (Graphics2D)image.getGraphics();
        graphics.drawRect(10, 10, 480, 380);
        graphics.drawString("This image is rendered dynamically", 100, 50);
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            response.setContentType("image/png");
            ImageIO.write((RenderedImage)image, "png", (OutputStream)out);
            out.close();
        }
        catch (IOException e) {
            this.logger.error(e.getMessage(), e);
        }
    }
}

