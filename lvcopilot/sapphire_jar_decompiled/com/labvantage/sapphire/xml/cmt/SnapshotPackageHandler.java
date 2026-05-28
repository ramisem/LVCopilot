/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml.cmt;

import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.xml.SapphireSaxHandler;
import com.labvantage.sapphire.xml.cmt.SnapshotHandler;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.xml.cmt.Snapshot;
import sapphire.xml.cmt.SnapshotItem;

public class SnapshotPackageHandler
extends SapphireSaxHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    protected StringBuffer currentElementChars = new StringBuffer();
    private SnapshotPackage snapshotPackage;
    private SnapshotHandler snapshotHandler;
    boolean isRequested;
    String snapshotImageSource;
    private Snapshot snapshot;
    private boolean isNULLSnapshot;
    private SnapshotItem currSnapshotItem;

    public SnapshotPackage getSnapshotPackage() {
        return this.snapshotPackage;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Properties attr = this.getAttributes(attributes);
        this.currentElementChars.setLength(0);
        if (qName.equalsIgnoreCase("snapshotpackage")) {
            String isFromChangeLog = attr.getProperty("isFromChangeLog");
            this.snapshotPackage = new SnapshotPackage("Y".equals(isFromChangeLog));
            this.snapshotPackage.setCreateTool("xml");
            String uuid = attr.getProperty("uuid");
            this.snapshotPackage.setUUID(uuid);
        } else if (qName.equalsIgnoreCase("snapshotpackageitem")) {
            this.snapshot = null;
            this.isRequested = "Y".equals(attr.getProperty("isRequested"));
            Snapshot.Type type = Snapshot.Type.valueOf(attr.getProperty("type"));
            switch (type) {
                case SDI: {
                    this.currSnapshotItem = new SDISnapshotItem(attr.getProperty("id"));
                    break;
                }
                case PROPERTYTREE: {
                    this.currSnapshotItem = new PropertyTreeSnapshotItem(attr.getProperty("id"));
                }
            }
            this.snapshotImageSource = attr.getProperty("imageSource");
            boolean bl = this.isNULLSnapshot = "Y".equals(attr.getProperty("nullSnapshot"));
            if (!this.isNULLSnapshot) {
                this.snapshotHandler = new SnapshotHandler();
                this.transferHandler(this.snapshotHandler);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("snapshotpackageitem")) {
            this.snapshot = this.isNULLSnapshot ? null : this.snapshotHandler.getSnapshot();
            if ("PRE_IMAGE".equals(this.snapshotImageSource)) {
                this.snapshotPackage.addPreSnapshot(this.snapshot == null ? this.currSnapshotItem : this.snapshot.getSnapshotItem(), this.snapshot);
            } else {
                this.snapshotPackage.addSnapshot(this.snapshot == null ? this.currSnapshotItem : this.snapshot.getSnapshotItem(), this.snapshot, this.isRequested);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.currentElementChars != null) {
            this.currentElementChars.append(this.getCharacters(ch, start, length));
        }
    }
}

