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

public class JndiConverterWebSphere
extends BaseConverter {
    @Override
    public void convertEjbJndiName(File source, File target, String ejbPrifix) throws SapphireException {
        Document dom = DOMUtil.getNewDocument(source, false);
        this.convertEjbJndiName(dom, ejbPrifix);
        DOMUtil.save(dom, target != null ? target : source);
    }

    private Document convertEjbJndiName(Document dom, String jndiPrefix) {
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("ejbBindings");
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); ++i) {
                Element el = (Element)nl.item(i);
                String ejbName = el.getAttribute("xmi:id");
                if (jndiPrefix == null || jndiPrefix.length() <= 0) continue;
                el.setAttribute("jndiName", jndiPrefix + (jndiPrefix.endsWith("/") ? "" : "/") + "ejb/" + ejbName);
            }
        }
        return dom;
    }
}

