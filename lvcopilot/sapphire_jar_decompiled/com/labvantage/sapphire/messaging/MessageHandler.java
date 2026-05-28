/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.messaging;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.xml.PropertyList;

public class MessageHandler
extends SapphireSaxHandler {
    private StringBuffer currentElementChars = new StringBuffer();
    private static final String ZHEADER = "ZHeader";
    private static final String MSGID = "MsgID";
    private static final String MSGREFID = "MsgRefID";
    private static final String MSGNAME = "MsgName";
    private static final String MSGVERSION = "MsgVersion";
    private static final String MSGFLOW = "MsgFlow";
    private static final String MSGTYPE = "MsgType";
    private static final String ZDATA = "ZData";
    PropertyList header = null;
    String body = "";
    boolean inbody;

    @Override
    public void startDocument() throws SAXException {
        this.header = new PropertyList();
        this.body = "";
        this.inbody = false;
    }

    public PropertyList getHeader() {
        return this.header;
    }

    public String getBody() {
        return this.body;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        if (ZDATA.equals(qName) && !this.inbody) {
            this.inbody = true;
            this.body = this.body + "<ZSEC>";
        }
        if (this.inbody) {
            this.body = this.body + "<" + qName;
            int count = attributes.getLength();
            for (int i = 0; i < count; ++i) {
                String attr = attributes.getQName(i);
                String val = attributes.getValue(i);
                this.body = this.body + " " + attr + "=\"" + val + "\"";
            }
            this.body = this.body + ">\n";
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (MSGID.equals(qName)) {
            this.header.put(MSGID, this.currentElementChars.toString());
        } else if (MSGREFID.equals(qName)) {
            this.header.put(MSGREFID, this.currentElementChars.toString());
        } else if (MSGNAME.equals(qName)) {
            this.header.put(MSGNAME, this.currentElementChars.toString());
        } else if (MSGVERSION.equals(qName)) {
            this.header.put(MSGVERSION, this.currentElementChars.toString());
        } else if (MSGTYPE.equals(qName)) {
            this.header.put(MSGTYPE, this.currentElementChars.toString());
        } else if (MSGFLOW.equals(qName)) {
            this.header.put(MSGFLOW, this.currentElementChars.toString());
        } else if (ZHEADER.equals(qName)) {
            if (this.header.get(MSGID) == null || this.header.get(MSGID).toString().length() == 0) {
                throw new SAXException("Incoming message does not have a MsgID");
            }
            if (this.header.get(MSGNAME) == null || this.header.get(MSGNAME).toString().length() == 0) {
                throw new SAXException("Incoming message does not have a MsgName");
            }
            if (this.header.get(MSGVERSION) == null || this.header.get(MSGVERSION).toString().length() == 0) {
                throw new SAXException("Incoming message does not have a MsgVersion");
            }
        } else if (this.inbody) {
            if (!"ZSEC".equals(qName)) {
                this.body = this.body + this.currentElementChars.toString();
                this.body = this.body + "\n</" + qName + ">";
            } else {
                this.body = this.body + "\n</ZSEC>";
            }
        }
        this.currentElementChars.delete(0, this.currentElementChars.length());
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }
}

