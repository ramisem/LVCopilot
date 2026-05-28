/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.xhtmlrenderer.extend.FontContext
 *  org.xhtmlrenderer.extend.NamespaceHandler
 *  org.xhtmlrenderer.extend.OutputDevice
 *  org.xhtmlrenderer.extend.UserInterface
 *  org.xhtmlrenderer.layout.BoxBuilder
 *  org.xhtmlrenderer.layout.LayoutContext
 *  org.xhtmlrenderer.layout.SharedContext
 *  org.xhtmlrenderer.render.BlockBox
 *  org.xhtmlrenderer.render.Box
 *  org.xhtmlrenderer.render.RenderingContext
 *  org.xhtmlrenderer.render.ViewportBox
 *  org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler
 *  org.xhtmlrenderer.swing.Java2DFontContext
 *  org.xhtmlrenderer.swing.Java2DOutputDevice
 *  org.xhtmlrenderer.swing.Java2DRenderer
 *  org.xhtmlrenderer.util.Configuration
 *  org.xhtmlrenderer.util.ImageUtil
 */
package net.sf.jasperreports;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FontContext;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.Java2DFontContext;
import org.xhtmlrenderer.swing.Java2DOutputDevice;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.ImageUtil;

public class CustomJava2DRenderer
extends Java2DRenderer {
    private static final int DEFAULT_HEIGHT = 1000;
    private static final int DEFAULT_DOTS_PER_POINT = 1;
    private SharedContext sharedContext;
    private BufferedImage outputImage;
    private Java2DOutputDevice outputDevice;
    private Document doc;
    private Box root;
    private int width;
    private Map renderingHints;

    public CustomJava2DRenderer(Document doc, int width, int height) {
        super(doc, width, height);
        this.doc = doc;
        this.width = width;
        this.prepareLayout();
    }

    public void setRenderingHints(Map hints) {
        this.renderingHints = hints;
        super.setRenderingHints(hints);
    }

    private void prepareLayout() {
        this.outputImage = ImageUtil.createCompatibleBufferedImage((int)1, (int)1);
        this.outputDevice = new Java2DOutputDevice(this.outputImage);
        this.sharedContext = this.getSharedContext();
        this.setDocument(this.doc, null, (NamespaceHandler)new XhtmlNamespaceHandler());
        this.layout(this.width);
    }

    public int getComputedHeight() {
        return this.root.getHeight();
    }

    public void paint(Graphics2D newG) {
        this.outputDevice = new Java2DOutputDevice(newG);
        if (this.renderingHints != null) {
            newG.getRenderingHints().putAll((Map<?, ?>)this.renderingHints);
        }
        RenderingContext rc = this.sharedContext.newRenderingContextInstance();
        rc.setFontContext((FontContext)new Java2DFontContext(newG));
        rc.setOutputDevice((OutputDevice)this.outputDevice);
        this.sharedContext.getTextRenderer().setup(rc.getFontContext());
        this.root.getLayer().paint(rc);
    }

    private void setDocument(Document doc, String url, NamespaceHandler nsh) {
        this.sharedContext.reset();
        if (Configuration.isTrue((String)"xr.cache.stylesheets", (boolean)true)) {
            this.sharedContext.getCss().flushStyleSheets();
        } else {
            this.sharedContext.getCss().flushAllStyleSheets();
        }
        this.sharedContext.setBaseURL(url);
        this.sharedContext.setNamespaceHandler(nsh);
        this.sharedContext.getCss().setDocumentContext(this.sharedContext, this.sharedContext.getNamespaceHandler(), doc, (UserInterface)new NullUserInterface());
    }

    private void layout(int width) {
        Rectangle rect = new Rectangle(0, 0, width, 1000);
        this.sharedContext.set_TempCanvas(rect);
        LayoutContext c = this.newLayoutContext();
        BlockBox root = BoxBuilder.createRootBox((LayoutContext)c, (Document)this.doc);
        root.setContainingBlock((Box)new ViewportBox(rect));
        root.layout(c);
        this.root = root;
    }

    private LayoutContext newLayoutContext() {
        LayoutContext result = this.sharedContext.newLayoutContextInstance();
        result.setFontContext((FontContext)new Java2DFontContext(this.outputDevice.getGraphics()));
        this.sharedContext.getTextRenderer().setup(result.getFontContext());
        return result;
    }

    private static final class NullUserInterface
    implements UserInterface {
        private NullUserInterface() {
        }

        public boolean isHover(Element e) {
            return false;
        }

        public boolean isActive(Element e) {
            return false;
        }

        public boolean isFocus(Element e) {
            return false;
        }
    }
}

