/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.component.Component
 *  net.sf.jasperreports.engine.component.ComponentFillFactory
 *  net.sf.jasperreports.engine.component.FillComponent
 *  net.sf.jasperreports.engine.fill.JRFillCloneFactory
 *  net.sf.jasperreports.engine.fill.JRFillObjectFactory
 */
package net.sf.jasperreports.components.html;

import net.sf.jasperreports.components.html.HtmlComponent;
import net.sf.jasperreports.components.html.HtmlComponentFill;
import net.sf.jasperreports.engine.component.Component;
import net.sf.jasperreports.engine.component.ComponentFillFactory;
import net.sf.jasperreports.engine.component.FillComponent;
import net.sf.jasperreports.engine.fill.JRFillCloneFactory;
import net.sf.jasperreports.engine.fill.JRFillObjectFactory;

public class HtmlComponentFillFactory
implements ComponentFillFactory {
    public FillComponent toFillComponent(Component component, JRFillObjectFactory factory) {
        HtmlComponent htmlComponent = (HtmlComponent)component;
        return new HtmlComponentFill(htmlComponent);
    }

    public FillComponent cloneFillComponent(FillComponent component, JRFillCloneFactory factory) {
        HtmlComponentFill htmlComponentFill = (HtmlComponentFill)component;
        return new HtmlComponentFill(htmlComponentFill.getHtmlComponent());
    }
}

