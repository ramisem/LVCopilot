/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.BaseConverter;
import java.io.File;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sapphire.SapphireException;
import sapphire.xml.DOMUtil;

public class JndiConverterJBoss
extends BaseConverter {
    @Override
    public void convertEjbJndiName(File source, File target, String ejbPrifix) throws SapphireException {
        Document dom = DOMUtil.getNewDocument(source, false, "-//JBoss//DTD JBOSS//EN");
        this.convertEjbJndiName(dom, ejbPrifix);
        DOMUtil.save(dom, target != null ? target : source);
    }

    private void convertEjbJndiName(Document dom, String jndiPrefix) {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("session");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); ++i) {
                Element el = (Element)nl.item(i);
                String ejbName = JndiConverterJBoss.getTextValue(el, "ejb-name");
                String localEjbJndiName = JndiConverterJBoss.getTextValue(el, "local-jndi-name");
                String ejbJndiName = JndiConverterJBoss.getTextValue(el, "jndi-name");
                if (jndiPrefix != null && jndiPrefix.length() > 0) {
                    if (ejbJndiName != null && ejbJndiName.length() > 0) {
                        this.setTextValue(el, "jndi-name", jndiPrefix + (jndiPrefix.endsWith("/") ? "" : "/") + "ejb/" + ejbName);
                        continue;
                    }
                    this.setTextValue(el, "local-jndi-name", jndiPrefix + (jndiPrefix.endsWith("/") ? "" : "/") + "ejb/" + ejbName);
                    continue;
                }
                if (ejbJndiName != null && ejbJndiName.length() > 0) {
                    this.setTextValue(el, "jndi-name", ejbJndiName);
                    continue;
                }
                this.setTextValue(el, "local-jndi-name", localEjbJndiName);
            }
        }
    }

    @Override
    public void changeWARName(File source, File target, String warName) throws SapphireException {
        Document dom = DOMUtil.getNewDocument(source, false);
        this.changeWARName(dom, warName);
        DOMUtil.save(dom, target != null ? target : source);
    }

    private void changeWARName(Document dom, String warName) {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("sub-deployment");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); ++i) {
                Element el = (Element)nl.item(i);
                if (!el.getAttribute("name").endsWith(".war")) continue;
                el.setAttribute("name", warName);
            }
        }
    }
}

