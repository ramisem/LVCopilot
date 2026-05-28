/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml.cmt;

import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.xml.SDIDataHandler;
import com.labvantage.sapphire.xml.cmt.SnapshotHandler;
import java.util.Properties;
import org.json.JSONException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SDISnapshotHandler
extends SnapshotHandler {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    SDISnapshot sdiSnapshot;
    SDISnapshotItem currentSDIDataSnapshotItem;
    String currentPolicyNodeItemId;

    @Override
    public SDISnapshot getSnapshot() {
        return this.sdiSnapshot;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        Properties attr = this.getAttributes(attributes);
        this.currentElementChars.setLength(0);
        if (qName.equalsIgnoreCase("sdisnapshot")) {
            this.sdiSnapshot = new SDISnapshot();
            this.sdiSnapshot.setDevMode("Y".equals(attr.getProperty("devmode")));
            this.sdiSnapshot.setCompCode(attr.getProperty("compcode"));
        } else if (qName.equalsIgnoreCase("sdidatalistitem")) {
            String sdiDataListItemId = attr.getProperty("id");
            this.currentSDIDataSnapshotItem = new SDISnapshotItem(sdiDataListItemId);
            SDIDataHandler sdiDataHandler = new SDIDataHandler(new SDIData());
            sdiDataHandler.setConnectionid(this.connectionid);
            sdiDataHandler.setRakFile(this.rakFile);
            this.transferHandler(sdiDataHandler);
            this.sdiSnapshot.addSDIData(this.currentSDIDataSnapshotItem, sdiDataHandler.getSDIData());
        } else if (!qName.equalsIgnoreCase("sdidata") && qName.equalsIgnoreCase("policynodelistitem")) {
            this.currentPolicyNodeItemId = attr.getProperty("id");
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("sdisnapshot")) {
            this.returnHandler();
        } else if (qName.equalsIgnoreCase("sditree")) {
            String sdiTreeChars = this.currentElementChars.toString();
            SDISnapshotItem primarySnapshotItem = SDISnapshotItem.fromJSON(sdiTreeChars);
            this.sdiSnapshot.setSnapshotItem(primarySnapshotItem);
        } else if (qName.equalsIgnoreCase("policynodelistitem")) {
            try {
                String policyNodeJSONStr = this.currentElementChars.toString();
                PropertyList policyProps = new PropertyList();
                policyProps.setJSONString(policyNodeJSONStr, true);
                this.sdiSnapshot.addPolicyProps(this.currentPolicyNodeItemId, policyProps);
            }
            catch (JSONException e) {
                throw new SAXException("Exception occurred when parsing Policy Node");
            }
        }
    }
}

