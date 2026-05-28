/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRExpression
 *  net.sf.jasperreports.engine.component.XmlDigesterConfigurer
 *  net.sf.jasperreports.engine.type.EvaluationTimeEnum
 *  net.sf.jasperreports.engine.type.HorizontalImageAlignEnum
 *  net.sf.jasperreports.engine.type.JREnum
 *  net.sf.jasperreports.engine.type.NamedEnum
 *  net.sf.jasperreports.engine.type.ScaleImageEnum
 *  net.sf.jasperreports.engine.type.VerticalImageAlignEnum
 *  net.sf.jasperreports.engine.xml.JRExpressionFactory$StringExpressionFactory
 *  net.sf.jasperreports.engine.xml.XmlConstantPropertyRule
 *  org.apache.commons.digester.Digester
 *  org.apache.commons.digester.Rule
 */
package net.sf.jasperreports.components.html;

import net.sf.jasperreports.components.html.HtmlComponent;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.component.XmlDigesterConfigurer;
import net.sf.jasperreports.engine.type.EvaluationTimeEnum;
import net.sf.jasperreports.engine.type.HorizontalImageAlignEnum;
import net.sf.jasperreports.engine.type.JREnum;
import net.sf.jasperreports.engine.type.NamedEnum;
import net.sf.jasperreports.engine.type.ScaleImageEnum;
import net.sf.jasperreports.engine.type.VerticalImageAlignEnum;
import net.sf.jasperreports.engine.xml.JRExpressionFactory;
import net.sf.jasperreports.engine.xml.XmlConstantPropertyRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

public class HtmlComponentDigester
implements XmlDigesterConfigurer {
    public void configureDigester(Digester digester) {
        this.setHtmlComponentRules(digester);
    }

    protected void setHtmlComponentRules(Digester digester) {
        String htmlComponentPattern = "*/componentElement/html";
        digester.addObjectCreate(htmlComponentPattern, HtmlComponent.class.getName());
        digester.addSetProperties(htmlComponentPattern, new String[]{"scaleType", "horizontalAlign", "verticalAlign", "evaluationTime"}, new String[0]);
        digester.addRule(htmlComponentPattern, (Rule)new XmlConstantPropertyRule("scaleType", (JREnum[])ScaleImageEnum.values()));
        digester.addRule(htmlComponentPattern, (Rule)new XmlConstantPropertyRule("horizontalAlign", "horizontalImageAlign", (NamedEnum[])HorizontalImageAlignEnum.values()));
        digester.addRule(htmlComponentPattern, (Rule)new XmlConstantPropertyRule("verticalAlign", "verticalImageAlign", (NamedEnum[])VerticalImageAlignEnum.values()));
        digester.addRule(htmlComponentPattern, (Rule)new XmlConstantPropertyRule("evaluationTime", (JREnum[])EvaluationTimeEnum.values()));
        String htmlContentPattern = htmlComponentPattern + "/htmlContentExpression";
        digester.addFactoryCreate(htmlContentPattern, JRExpressionFactory.StringExpressionFactory.class.getName());
        digester.addCallMethod(htmlContentPattern, "setText", 0);
        digester.addSetNext(htmlContentPattern, "setHtmlContentExpression", JRExpression.class.getName());
    }
}

