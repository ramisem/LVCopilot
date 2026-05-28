/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.DefaultJasperReportsContext
 *  net.sf.jasperreports.engine.JRAbstractSvgRenderer
 *  net.sf.jasperreports.engine.JRRuntimeException
 *  net.sf.jasperreports.engine.JasperReportsContext
 *  net.sf.jasperreports.renderers.Graphics2DRenderable
 */
package net.sf.jasperreports.renderers;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import net.sf.jasperreports.CustomJava2DRenderer;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRAbstractSvgRenderer;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.renderers.Graphics2DRenderable;
import org.w3c.dom.Document;

public class FlyingSaucerXhtmlToImageRenderer
extends JRAbstractSvgRenderer
implements Graphics2DRenderable {
    private static final long serialVersionUID = 1L;
    private int width;
    private int height;
    private CustomJava2DRenderer renderer;

    public FlyingSaucerXhtmlToImageRenderer(Document document, int width, int height) {
        this.width = width;
        this.renderer = new CustomJava2DRenderer(document, width, height);
        this.height = this.renderer.getComputedHeight();
    }

    public Dimension getComputedSize() {
        return new Dimension(this.width, this.height);
    }

    public Dimension2D getDimension(JasperReportsContext jasperReportsContext) {
        return new Dimension(this.width, this.height);
    }

    public Dimension2D getDimension() {
        return this.getDimension((JasperReportsContext)DefaultJasperReportsContext.getInstance());
    }

    public void render(Graphics2D grx, Rectangle2D rectangle) {
        this.render((JasperReportsContext)DefaultJasperReportsContext.getInstance(), grx, rectangle);
    }

    public void render(JasperReportsContext jasperReportsContext, Graphics2D grx, Rectangle2D rectangle) {
        AffineTransform origTransform = grx.getTransform();
        try {
            grx.translate(rectangle.getX(), rectangle.getY());
            if (rectangle.getWidth() != (double)this.width || rectangle.getHeight() != (double)this.height) {
                grx.scale(rectangle.getWidth() / (double)this.width, rectangle.getHeight() / (double)this.height);
            }
            this.renderer.paint(grx);
        }
        catch (Exception e) {
            throw new JRRuntimeException((Throwable)e);
        }
        finally {
            grx.setTransform(origTransform);
        }
    }
}

