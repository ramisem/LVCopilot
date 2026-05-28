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
package com.labvantage.sapphire.pageelements.genealogyviewer;

import com.labvantage.sapphire.pageelements.genealogyviewer.GenealogyViewerUtil;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenealogyOperation
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 74955 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String rootBatchId = request.getParameter("batchid");
        String cacheKey = "genealogy_model_" + rootBatchId + "_" + this.getConnectionProcessor().getConnectionid();
        SoftReference modelReference = (SoftReference)CacheUtil.get(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "BatchImageMap", cacheKey);
        GenealogyViewerUtil model = (GenealogyViewerUtil)modelReference.get();
        int totalWidth = model.getImageWidth();
        int totalHeight = model.getImageHeight();
        BufferedImage img = new BufferedImage(totalWidth, totalHeight, 1);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, totalWidth, totalHeight);
        try {
            model.drawImage(g);
            this.streamImage(img, response);
        }
        catch (IOException e) {
            throw new ServletException(this.getTranslationProcessor().translate("Failed to stream image") + ". Exception: " + e.getMessage());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void streamImage(BufferedImage bufferedImage, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);
        response.setContentType("image/jpeg");
        try (ServletOutputStream ouputStream = response.getOutputStream();){
            ImageIO.write((RenderedImage)bufferedImage, "png", (OutputStream)ouputStream);
            ouputStream.flush();
        }
    }
}

