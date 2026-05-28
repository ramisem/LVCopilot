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
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.util.cache.CacheUtil;
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

public class LV_Form
extends BaseSDCRules {
    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.checkVirtualFormsCount(sdiData, actionProps);
        this.setThumbnailHtml(sdiData);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        CacheUtil.clear(this.connectionInfo.getDatabaseId(), "FormDefinition");
        DataSet primary = sdiData.getDataset("primary");
        if (primary.size() > 1) {
            throw new SapphireException("INVALID_PROPERTY", "VALIDATION", "Only 1 Form may be maintained in any edit!");
        }
        this.checkVirtualFormsCount(sdiData, actionProps);
        this.setThumbnailHtml(sdiData);
    }

    private void setThumbnailHtml(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        String formlayout = primary.getValue(0, "formlayout");
        if (formlayout.length() > 0) {
            int pos = Form.findStartOfTag("<div ", formlayout, "sapphire=\"page\"", 0);
            int pos2 = Form.findStartOfTag("<div ", formlayout, "class=\"pageshadow\"", pos);
            formlayout = pos2 > 0 ? formlayout.substring(pos, pos2 - 1) : formlayout.substring(pos);
            try {
                Tidy tidy = new Tidy();
                byte[] htmlbytes = formlayout.getBytes();
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
                formlayout = RichTextEditor.serializeDocument(doc, false);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        primary.setClob(0, "thumbnailhtml", StringUtil.replaceAll(formlayout, "contenteditable=\"true\"", "contenteditable=\"false\""));
    }

    private void checkVirtualFormsCount(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        int licvirtualcount;
        int virtualcount;
        DataSet primary = sdiData.getDataset("primary");
        if (primary.getValue(0, "virtualformflag", "N").equals("Y") && (virtualcount = this.database.getCount("SELECT count(*) FROM form WHERE virtualformflag='Y'")) >= (licvirtualcount = Configuration.getInstance().getLicense(this.getDatabaseid()).getVirtualFormCount())) {
            throw new SapphireException("You have exceeded your virtual forms license count (" + licvirtualcount + ")!");
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String check = "SELECT sdiformrule.sdcid, sdiformrule.keyid1, sdiformrule.keyid2, sdiformrule.keyid3 FROM   sdiformrule, rsetitems WHERE  rsetitems.rsetid = ? AND    sdiformrule.formid = rsetitems.keyid1 AND sdiformrule.formversionid = rsetitems.keyid2 ORDER BY 1, 2, 3, 4";
        this.database.createPreparedResultSet(check, new Object[]{rsetid});
        StringBuffer refs = new StringBuffer();
        for (int i = 0; i < 10 && this.database.getNext(); ++i) {
            refs.append("<br/>").append(this.database.getString("sdcid")).append(": ").append(this.database.getString("keyid1"));
        }
        if (refs.length() > 0) {
            boolean more = this.database.getNext();
            this.throwError("FormUsed", "VALIDATION", "Form(s) cannot be deleted because of " + (more ? "at least" : "") + " the following references:" + refs + (more ? "<br/>..." : ""));
        }
    }
}

