/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.xpath.XPathAPI
 *  org.w3c.tidy.Tidy
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.pageelements.controls.RichTextEditor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_Formlet
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.setThumbnailHtml(sdiData);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.setThumbnailHtml(sdiData);
    }

    private void setThumbnailHtml(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        String formletlayout = primary.getValue(0, "formletlayout");
        if (formletlayout.length() > 0) {
            int pos = Form.findStartOfTag("<div ", formletlayout, "sapphire=\"page\"", 0);
            int pos2 = Form.findStartOfTag("<div ", formletlayout, "class=\"pageshadow\"", pos > 0 ? pos : 0);
            if (pos2 > 0) {
                formletlayout = formletlayout.substring(pos, pos2 - 1);
            } else if (pos > 0) {
                formletlayout = formletlayout.substring(pos);
            }
            try {
                Tidy tidy = new Tidy();
                byte[] htmlbytes = formletlayout.getBytes();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(htmlbytes);
                tidy.setOnlyErrors(true);
                tidy.setErrout(new PrintWriter(new ByteArrayOutputStream()));
                Document doc = tidy.parseDOM((InputStream)byteArrayInputStream, null);
                NodeList buttonlist = XPathAPI.selectNodeList((Node)doc, (String)"//button");
                for (int j = 0; j < buttonlist.getLength(); ++j) {
                    Node button = buttonlist.item(j);
                    button.getAttributes().getNamedItem("onclick").setNodeValue("");
                }
                NodeList linklist = XPathAPI.selectNodeList((Node)doc, (String)"//a");
                for (int j = 0; j < linklist.getLength(); ++j) {
                    Node link = linklist.item(j);
                    if (link.getAttributes().getNamedItem("href") == null) continue;
                    Element input = doc.createElement("SPAN");
                    link.getParentNode().replaceChild(input, link);
                }
                formletlayout = RichTextEditor.serializeDocument(doc, false);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        primary.setClob(0, "thumbnailhtml", StringUtil.replaceAll(formletlayout, "contenteditable=\"true\"", "contenteditable=\"false\""));
    }
}

