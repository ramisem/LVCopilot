/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.util.DataSet;

public class DataSetHandler
extends SapphireSaxHandler {
    private String _currentColumnId = "";
    private int _currentRow = -1;
    private StringBuffer value = new StringBuffer();
    private boolean CDATAEncountered;
    private DataSet ds;

    public DataSetHandler(DataSet ds) {
        this.ds = ds;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Properties attr = this.getAttributes(attributes);
        if (qName.equalsIgnoreCase("COLDEF")) {
            String id = attr.getProperty("id");
            String type = attr.getProperty("type");
            this.ds.addColumn(id, type.equalsIgnoreCase("CLOB") ? 3 : (type.equalsIgnoreCase("DATE") ? 2 : (type.equalsIgnoreCase("NUMBER") ? 1 : 0)));
        } else if (qName.equalsIgnoreCase("COL")) {
            this._currentColumnId = attr.getProperty("id");
        } else if (qName.equalsIgnoreCase("ROW")) {
            this._currentRow = this.ds.addRow();
        } else if (qName.equalsIgnoreCase("DATASET")) {
            this.ds.setId(attr.getProperty("id"));
            this.ds.setCdataEscape(attr.getProperty("cdataescape"));
            String forceISOFormat = attr.getProperty("forceISOFormat");
            if (forceISOFormat != null && "Y".equals(forceISOFormat)) {
                this.ds.setForceISOFormat(true);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("COL")) {
            String val = this.value.toString().trim();
            if (this.ds.getCdataEscape() != null && this.ds.getCdataEscape().length() > 0) {
                val = val.replaceAll(this.ds.getCdataEscape(), "]]>");
            }
            this.ds.setValue(this._currentRow, this._currentColumnId, val);
        } else if (qName.equalsIgnoreCase("DATASET") && this.ds.isForceISOFormat()) {
            this.ds.setForceISOFormat(false);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.CDATAEncountered) {
            this.value.append(this.getCharacters(ch, start, length));
        }
    }

    @Override
    public void startCDATA() throws SAXException {
        this.value.setLength(0);
        this.CDATAEncountered = true;
    }

    @Override
    public void endCDATA() throws SAXException {
        this.CDATAEncountered = false;
    }
}

