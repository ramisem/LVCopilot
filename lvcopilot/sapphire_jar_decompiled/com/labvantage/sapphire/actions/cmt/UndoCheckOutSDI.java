/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.sapphire.actions.cmt.ImportSnapshot;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshot;
import com.labvantage.sapphire.cmt.PropertyTreeSnapshotItem;
import com.labvantage.sapphire.cmt.SDISnapshot;
import com.labvantage.sapphire.cmt.SnapshotFactory;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.cmt.Snapshot;

public class UndoCheckOutSDI
extends BaseAction
implements sapphire.action.UndoCheckOutSDI {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String sql;
        String changelogid = actionProps.getProperty("changelogid");
        String sdcid = actionProps.getProperty("sdcid");
        String keyid1 = actionProps.getProperty("keyid1");
        String keyid2 = actionProps.getProperty("keyid2");
        String keyid3 = actionProps.getProperty("keyid3");
        String propertytreenodeid = actionProps.getProperty("propertytreenodeid");
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm", "N"));
        ArrayList ds = null;
        if (changelogid.length() > 0) {
            sql = "select c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.propertytreenodeid, c.originalsnapshot from changelog c where c.changelogstatus = 'Checked Out' and c.changelogid = ?";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{changelogid}, true);
        } else if (sdcid.length() > 0 && keyid1.length() > 0) {
            if ("PropertyTree".equals(sdcid)) {
                if (propertytreenodeid.length() == 0) {
                    throw new SapphireException("MissingInputs", "VALIDATION", this.getTranslationProcessor().translate("PropertyTree Node Id is mandatory for PropertyTree snapshots."));
                }
                sql = "select c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.propertytreenodeid, c.originalsnapshot from changelog c where c.linksdcid = 'PropertyTree' and c.linkkeyid1 = ? and c.propertytreenodeid = ? and c.changelogstatus = 'Checked Out'";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyid1, propertytreenodeid}, true);
            } else {
                String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                String sql2 = "select c.changelogid, c.linksdcid, c.linkkeyid1, c.linkkeyid2, c.linkkeyid3, c.originalsnapshot from changelog c, rsetitems r where r.sdcid = c.linksdcid and r.keyid1 = c.linkkeyid1 and r.keyid2 = c.linkkeyid2 and r.keyid3 = c.linkkeyid3 and c.changelogstatus = 'Checked Out' and r.rsetid = ?";
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql2, (Object[])new String[]{rsetid}, true);
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                changelogid = ((DataSet)ds).getString(i, "changelogid", "");
                String linksdcid = ((DataSet)ds).getString(i, "linksdcid", "");
                String linkkeyid1 = ((DataSet)ds).getString(i, "linkkeyid1", "");
                String linkkeyid2 = ((DataSet)ds).getString(i, "linkkeyid2", "");
                String linkkeyid3 = ((DataSet)ds).getString(i, "linkkeyid3", "");
                String propertyTreeNodeId = ((DataSet)ds).getString(i, "propertytreenodeid", "");
                String originalsnapshotXML = ((DataSet)ds).getValue(i, "originalsnapshot", "");
                String changelogstatus = actionProps.getProperty("changelogstatus", "CheckOut Aborted");
                if ("SDC".equals(linksdcid)) {
                    throw new SapphireException("InvalidOperation", "VALIDATION", this.getTranslationProcessor().translate("Undo Checkout operation is not allowed on SDC SDC."));
                }
                if (originalsnapshotXML.length() == 0) {
                    throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Undo Checkout Error"), this.getTranslationProcessor().translate("Cannot Undo. SDI is recently created. Use Delete operation to delete the SDI."));
                }
                if (!"CheckOut Rolledback".equals(changelogstatus)) {
                    SDISnapshot currentSnapshot = null;
                    currentSnapshot = "PropertyTree".equals(linksdcid) ? new SnapshotFactory(this.getConnectionId()).generatePropertyTreeSnapshot(linkkeyid1, propertyTreeNodeId) : new SnapshotFactory(this.getConnectionId()).generateSDISnapshot(linksdcid, linkkeyid1, linkkeyid2, linkkeyid3);
                    String currentSnapshotXML = currentSnapshot.toXML().trim();
                    if (!currentSnapshotXML.equals(originalsnapshotXML.trim())) {
                        if (!forceUpdate) {
                            throw new SapphireException("CONFIRM", this.getTranslationProcessor().translate("Confirm Undo"), this.getTranslationProcessor().translate("Changes have been detected. Would you like the system to attempt to rollback to prior state?") + "<br><br>" + this.getTranslationProcessor().translate("Please ensure to review your data after this process."));
                        }
                        changelogstatus = "CheckOut Rolledback";
                    }
                }
                Snapshot originalSnapshot = Snapshot.fromXML(originalsnapshotXML, this.getConnectionId());
                SnapshotPackage snapshotPackage = new SnapshotPackage(false);
                snapshotPackage.addSnapshot(originalSnapshot.getSnapshotItem(), originalSnapshot, true);
                ImportSnapshot.ImportInstructions importInstructions = new ImportSnapshot.ImportInstructions();
                if (Snapshot.Type.SDI.equals((Object)originalSnapshot.getType())) {
                    importInstructions.addInstruction(linksdcid, linkkeyid1, linkkeyid2, linkkeyid3, "Override If Provisional");
                    importInstructions.setValidateFK(true);
                    importInstructions.setIgnoreMissingObjects(true);
                } else if (Snapshot.Type.PROPERTYTREE.equals((Object)originalSnapshot.getType())) {
                    PropertyTreeSnapshot ps = (PropertyTreeSnapshot)originalSnapshot;
                    PropertyTreeSnapshotItem psi = ps.getSnapshotItem();
                    importInstructions.addInstruction("PropertyTree", psi.getPropertyTreeId(), psi.getNodeId(), "", "Override Existing");
                    importInstructions.setValidateFK(true);
                    importInstructions.setIgnoreMissingObjects(true);
                }
                PropertyList props = new PropertyList();
                props.put("exportpackagexml", snapshotPackage.toXML());
                props.put("importInstructions", importInstructions.toString());
                props.put("createtransferlog", "N");
                props.put("undocheckoutflag", "Y");
                this.getActionProcessor().processActionClass(ImportSnapshot.class.getName(), props);
                props.clear();
                props.setProperty("sdcid", "LV_ChangeLog");
                props.setProperty("keyid1", changelogid);
                props.setProperty("changelogstatus", changelogstatus);
                props.setProperty("rolledbackby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                props.setProperty("rolledbackdt", "n");
                props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                props.setProperty("auditdt", actionProps.getProperty("auditdt"));
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        } else {
            throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Undo Checkout Error"), this.getTranslationProcessor().translate("SDI is not checked out"));
        }
    }
}

