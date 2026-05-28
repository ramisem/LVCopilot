/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml.cmt;

import com.labvantage.sapphire.xml.SapphireSaxHandler;
import com.labvantage.sapphire.xml.cmt.PropertyTreeSnapshotHandler;
import com.labvantage.sapphire.xml.cmt.SDISnapshotHandler;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.SapphireException;
import sapphire.xml.cmt.Snapshot;

public class SnapshotHandler
extends SapphireSaxHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    protected StringBuffer currentElementChars = new StringBuffer();
    private Snapshot snapshot;
    private SnapshotHandler currentHandler;

    public Snapshot getSnapshot() {
        return this.snapshot;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            Properties attr = this.getAttributes(attributes);
            this.currentElementChars.setLength(0);
            if (qName.equalsIgnoreCase("snapshot")) {
                String snapshotTypeStr = attr.getProperty("type");
                Snapshot.Type snapshotType = Snapshot.Type.valueOf(snapshotTypeStr);
                switch (snapshotType) {
                    case SDI: {
                        this.currentHandler = new SDISnapshotHandler();
                        this.currentHandler.setConnectionid(this.connectionid);
                        this.currentHandler.setRakFile(this.rakFile);
                        this.transferHandler(this.currentHandler);
                        break;
                    }
                    case PROPERTYTREE: {
                        this.currentHandler = new PropertyTreeSnapshotHandler();
                        this.currentHandler.setConnectionid(this.connectionid);
                        this.currentHandler.setRakFile(this.rakFile);
                        this.transferHandler(this.currentHandler);
                        break;
                    }
                    default: {
                        throw new SapphireException("Unknown Snapshot Type found.");
                    }
                }
            }
        }
        catch (SapphireException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("snapshot")) {
            this.snapshot = this.currentHandler.getSnapshot();
            this.returnHandler();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }
}

