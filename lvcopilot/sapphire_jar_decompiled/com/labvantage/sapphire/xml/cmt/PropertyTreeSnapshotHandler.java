/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml.cmt;

import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeHandler;
import com.labvantage.sapphire.xml.SDIDataHandler;
import com.labvantage.sapphire.xml.cmt.SnapshotHandler;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.util.SDIData;

public class PropertyTreeSnapshotHandler
extends SnapshotHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    PropertyTreeSnapshot snapshot;

    @Override
    public PropertyTreeSnapshot getSnapshot() {
        return this.snapshot;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Properties attr = this.getAttributes(attributes);
        this.currentElementChars.setLength(0);
        if (qName.equalsIgnoreCase("propertytreesnapshot")) {
            PropertyTreeSnapshotItem snapshotItem = new PropertyTreeSnapshotItem();
            snapshotItem.setPropertyTreeId(attr.getProperty("propertytreeid"));
            snapshotItem.setNodeId(attr.getProperty("nodeid"));
            snapshotItem.setDeleted("Y".equals(attr.getProperty("isDeleted")));
            this.snapshot = new PropertyTreeSnapshot(snapshotItem);
            this.snapshot.setDevMode("Y".equals(attr.getProperty("devmode")));
            this.snapshot.setCompCode(attr.getProperty("compcode"));
        } else if (qName.equalsIgnoreCase("propertytreesdidata")) {
            SDIDataHandler sdiDataHandler = new SDIDataHandler(new SDIData());
            sdiDataHandler.setConnectionid(this.connectionid);
            sdiDataHandler.setRakFile(this.rakFile);
            this.transferHandler(sdiDataHandler);
            this.snapshot.addSDIData(this.snapshot.getSnapshotItem(), sdiDataHandler.getSDIData());
        } else if (qName.equalsIgnoreCase("propertytreenode")) {
            if (!"__DEFINITION".equals(this.snapshot.getSnapshotItem().getNodeId())) {
                PropertyTree propertyTree = new PropertyTree();
                this.snapshot.setPropertyTree(propertyTree);
                PropertyTreeHandler propertyTreeHandler = new PropertyTreeHandler(propertyTree);
                propertyTreeHandler.setConnectionid(this.connectionid);
                propertyTreeHandler.setRakFile(this.rakFile);
                propertyTreeHandler.setPrintStream(null);
                propertyTreeHandler.setCreateTransferableObjects(true);
                this.transferHandler(propertyTreeHandler);
                this.snapshot.setExtendsNodeId(attr.getProperty("extendnodeid"));
            }
            this.snapshot.setExists(attr.getProperty("exists"));
            this.snapshot.setNotExists(attr.getProperty("notexists"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("propertytreesnapshot")) {
            this.returnHandler();
        } else if (qName.equalsIgnoreCase("sditree")) {
            String sdiTreeChars = this.currentElementChars.toString();
            PropertyTreeSnapshotItem parsedSnapshotItem = PropertyTreeSnapshotItem.fromJSON(sdiTreeChars);
            PropertyTreeSnapshotItem pSnapshotItem = this.snapshot.getSnapshotItem();
            parsedSnapshotItem.setDeleted(pSnapshotItem.isDeleted());
            this.snapshot.setSnapshotItem(parsedSnapshotItem);
        }
    }
}

