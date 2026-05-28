/*
 * Decompiled with CFR 0.152.
 */
package sapphire.xml.cmt;

import com.labvantage.sapphire.xml.SaxUtil;
import com.labvantage.sapphire.xml.cmt.SnapshotHandler;
import java.io.File;
import sapphire.SapphireException;
import sapphire.util.SDIData;
import sapphire.xml.cmt.SnapshotItem;

public interface Snapshot {
    public String toXML();

    public Type getType();

    public SnapshotItem getSnapshotItem();

    public SDIData getSDIData();

    public static Snapshot fromXML(String xml) throws SapphireException {
        return Snapshot.fromXML(xml, null);
    }

    public static Snapshot fromXML(String xml, String connectionId) throws SapphireException {
        return Snapshot.fromXML(xml, connectionId, null);
    }

    public static Snapshot fromXML(String xml, String connectionId, File rakFile) throws SapphireException {
        SnapshotHandler snapshotHandler = new SnapshotHandler();
        if (connectionId != null && connectionId.length() > 0) {
            snapshotHandler.setConnectionid(connectionId);
        }
        if (rakFile != null) {
            snapshotHandler.setRakFile(rakFile);
        }
        snapshotHandler.setXMLString(xml);
        SaxUtil.parseString(snapshotHandler);
        return snapshotHandler.getSnapshot();
    }

    public static Snapshot fromXML(File xmlFile) throws SapphireException {
        return Snapshot.fromXML(xmlFile, null);
    }

    public static Snapshot fromXML(File xmlFile, String connectionId) throws SapphireException {
        return Snapshot.fromXML(xmlFile, connectionId, null);
    }

    public static Snapshot fromXML(File xmlFile, String connectionId, File rakFile) throws SapphireException {
        SnapshotHandler snapshotHandler = new SnapshotHandler();
        if (connectionId != null && connectionId.length() > 0) {
            snapshotHandler.setConnectionid(connectionId);
        }
        if (rakFile != null) {
            snapshotHandler.setRakFile(rakFile);
        }
        snapshotHandler.setXMLFile(xmlFile);
        SaxUtil.parseFile(snapshotHandler);
        return snapshotHandler.getSnapshot();
    }

    public static enum Type {
        SDI,
        PROPERTYTREE;

    }
}

