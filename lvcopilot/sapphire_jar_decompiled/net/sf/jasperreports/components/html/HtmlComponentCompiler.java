/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRExpressionCollector
 *  net.sf.jasperreports.engine.base.JRBaseObjectFactory
 *  net.sf.jasperreports.engine.component.Component
 *  net.sf.jasperreports.engine.component.ComponentCompiler
 *  net.sf.jasperreports.engine.design.JRVerifier
 */
package net.sf.jasperreports.components.html;

import net.sf.jasperreports.components.html.HtmlComponent;
import net.sf.jasperreports.engine.JRExpressionCollector;
import net.sf.jasperreports.engine.base.JRBaseObjectFactory;
import net.sf.jasperreports.engine.component.Component;
import net.sf.jasperreports.engine.component.ComponentCompiler;
import net.sf.jasperreports.engine.design.JRVerifier;

public class HtmlComponentCompiler
implements ComponentCompiler {
    public void collectExpressions(Component component, JRExpressionCollector collector) {
        HtmlComponent htmlComponent = (HtmlComponent)component;
        collector.addExpression(htmlComponent.getHtmlContentExpression());
    }

    public Component toCompiledComponent(Component component, JRBaseObjectFactory baseFactory) {
        HtmlComponent htmlComponent = (HtmlComponent)component;
        return new HtmlComponent(htmlComponent, baseFactory);
    }

    public void verify(Component component, JRVerifier verifier) {
    }
}

