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
package com.labvantage.opal.elements.chart;

import com.labvantage.opal.elements.chart.util.BufferedImageWrapper;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ChartOperation
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String chartid = request.getParameter("chartid");
        Object o = request.getSession().getAttribute(chartid);
        if (o != null && o instanceof BufferedImageWrapper) {
            BufferedImageWrapper mimage = (BufferedImageWrapper)o;
            BufferedImage image = mimage.getImage();
            if (image != null) {
                try {
                    this.streamImage(response, image);
                }
                catch (IOException e) {
                    this.logger.error("Error in ChartOperation", e);
                }
            }
        } else if (o != null && o instanceof BufferedImage) {
            BufferedImage image = (BufferedImage)o;
            try {
                this.streamImage(response, image);
            }
            catch (IOException e) {
                this.logger.error("Error in ChartOperation", e);
            }
        } else {
            File file = new File(servletContext.getRealPath("/") + "/WEB-OPAL/images/charterror.gif");
            try {
                this.streamImage(response, ImageIO.read(file));
            }
            catch (IOException e) {
                this.logger.error("Error in ChartOperation", e);
            }
        }
        if (chartid != null) {
            request.getSession().removeAttribute(chartid);
        }
    }

    private void streamImage(HttpServletResponse response, BufferedImage image) throws IOException {
        response.setContentType("image/png");
        ServletOutputStream outputStream = response.getOutputStream();
        ImageIO.write((RenderedImage)image, "png", (OutputStream)outputStream);
        outputStream.flush();
        outputStream.close();
    }
}

