/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JasperReportsContext
 *  net.sf.jasperreports.engine.component.ComponentXmlWriter
 *  net.sf.jasperreports.engine.component.DefaultComponentManager
 */
package net.sf.jasperreports.components.html;

import net.sf.jasperreports.components.html.HtmlComponentXmlWriter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.component.ComponentXmlWriter;
import net.sf.jasperreports.engine.component.DefaultComponentManager;

public class HtmlComponentManager
extends DefaultComponentManager {
    public ComponentXmlWriter getComponentXmlWriter(JasperReportsContext jasperReportsContext) {
        return new HtmlComponentXmlWriter(jasperReportsContext);
    }
}

