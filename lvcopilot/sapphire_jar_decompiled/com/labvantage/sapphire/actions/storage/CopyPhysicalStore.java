/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.cmt.ImportSnapshot;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class CopyPhysicalStore
extends BaseAction
implements sapphire.action.CopyPhysicalStore {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String storageunitid;
        String physicalstoreid = actionProps.getProperty("physicalstoreid");
        if (OpalUtil.isEmpty(physicalstoreid) && OpalUtil.isNotEmpty(storageunitid = actionProps.getProperty("storageunitid"))) {
            if (physicalstoreid.contains(";")) {
                throw new SapphireException(this.getTranslationProcessor().translate("Not allowed to copy multiple"));
            }
            physicalstoreid = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "linkkeyid1", "storageunitid=? and linksdcid='PhysicalStore'", new String[]{storageunitid});
        }
        if (OpalUtil.isEmpty(physicalstoreid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Missing mandatory action input") + " [physicalstoreid]");
        }
        if (physicalstoreid.contains(";")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Not allowed to copy multiple"));
        }
        String label = actionProps.getProperty("label");
        if (OpalUtil.isEmpty(label)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Missing mandatory action input") + " [label]");
        }
        if (this.database.getPreparedCount("select count(s_physicalstoreid) from s_physicalstore where s_physicalstoreid=?", new String[]{physicalstoreid}) == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Physical Store to copy does not exists"));
        }
        SnapshotFactory snapshotUtil = new SnapshotFactory(this.getConnectionId());
        snapshotUtil.setVerbose(false);
        SnapshotPackage snapshotPackage = snapshotUtil.packageSnapshot("PhysicalStore", physicalstoreid, null, null, "PhysicalStore Custom", false);
        String fileName = OpalUtil.getUniqueID() + ".zip";
        FileManager.TempFile tempZipFile = new FileManager.TempFile(fileName, FileManager.TempSource.DOWNLOAD, this.getConnectionId());
        File file = tempZipFile.getData().getFile().toFile();
        file.deleteOnExit();
        file.getParentFile().mkdirs();
        snapshotPackage.toFile(file.getParent(), file.getName(), this.getConnectionId(), this.getRakFile());
        PropertyList props = new PropertyList();
        props.put("tempFile", file);
        this.getActionProcessor().processActionClass(ImportSnapshot.class.getName(), props);
        Map map = (Map)props.get("oldnewkeymap");
        String newphysicalstoreid = (String)((Map)map.get("PhysicalStore")).get(physicalstoreid + ";;");
        newphysicalstoreid = newphysicalstoreid.substring(0, newphysicalstoreid.indexOf(";"));
        String newstorageunitid = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "storageunitid", "linkkeyid1=?", new String[]{newphysicalstoreid});
        props.clear();
        props.setProperty("sdcid", "StorageUnitSDC");
        props.setProperty("keyid1", newstorageunitid);
        props.setProperty("storageunitlabel", label);
        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
    }
}

