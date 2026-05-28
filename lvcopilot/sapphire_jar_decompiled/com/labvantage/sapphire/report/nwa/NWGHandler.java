/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.nwa;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.xml.PropertyList;

public class NWGHandler
extends SapphireSaxHandler {
    private StringBuffer currentElementChars = new StringBuffer();
    private String parseFlag = "";
    private static final String ROW = "Row";
    private static final String STATID = "STAT_ID";
    private PropertyList stats;
    private String currentStat = "";
    private String currentStatValue = "";
    private String statsString = "";

    @Override
    public void startDocument() throws SAXException {
        this.stats = new PropertyList();
    }

    @Override
    public void endDocument() throws SAXException {
    }

    public PropertyList getStats() {
        return this.stats;
    }

    public String getStatString() {
        return this.statsString;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        this.currentElementChars.delete(0, this.currentElementChars.length());
        if (ROW.equals(qName)) {
            this.currentStat = "";
            this.currentStatValue = "";
            for (int i = 0; i < attributes.getLength(); ++i) {
                if (!STATID.equals(attributes.getQName(i))) continue;
                this.currentStat = attributes.getValue(i);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("T".equals(qName) && this.currentStat.length() > 0) {
            this.currentStatValue = this.currentElementChars.toString();
            this.stats.setProperty(this.currentStat, this.currentStatValue);
            this.statsString = this.statsString + this.currentStat + " = \"" + this.currentStatValue + "\", \n";
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }
}

