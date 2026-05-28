/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.cmt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.cmt.CheckInSDI;
import com.labvantage.sapphire.actions.cmt.CheckOutSDI;
import com.labvantage.sapphire.actions.cmt.ExportSnapshot;
import com.labvantage.sapphire.actions.cmt.ImportSnapshot;
import com.labvantage.sapphire.cmt.SnapshotPackage;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.servlet.ExternalHandlerProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RepositoryOperations
extends BaseAction {
    public static final String OPERATION_CHECKOUT = "checkout";
    public static final String OPERATION_CHECKIN = "checkin";
    public static final String OPERATION_UPDATE = "update";
    public static final String OPERATION_GETSNAPSHOT = "getsnapshot";
    public static final String OPERATION_GET_REMOTE_USER = "getremoteuser";
    public static final String PROPERTY_REMOTE_CHECKOUT = "remotecheckout";

    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String operation = actionProps.getProperty("operation");
        String sdcid = actionProps.getProperty("sdcid").trim();
        CMTPolicy policy = CMTPolicy.getPolicy(this.getConnectionid(), sdcid);
        String keyid1 = StringUtil.replaceAll(actionProps.getProperty("keyid1").trim(), "%3B", ";");
        String keyid2 = StringUtil.replaceAll(actionProps.getProperty("keyid2").trim(), "%3B", ";");
        String keyid3 = StringUtil.replaceAll(actionProps.getProperty("keyid3").trim(), "%3B", ";");
        String propertytreenodeid = actionProps.getProperty("propertytreenodeid").trim();
        if (operation.equals(OPERATION_CHECKOUT)) {
            this.checkout(actionProps, sdcid, keyid1, keyid2, keyid3, propertytreenodeid, policy);
        } else if (operation.equals(OPERATION_CHECKIN)) {
            this.checkin(actionProps, sdcid, keyid1, keyid2, keyid3, propertytreenodeid, policy);
        } else if (operation.equals(OPERATION_UPDATE)) {
            this.update(actionProps, sdcid, keyid1, keyid2, keyid3, propertytreenodeid, policy);
        } else if (operation.equals(OPERATION_GETSNAPSHOT)) {
            this.getSnapshot(actionProps, sdcid, keyid1, keyid2, keyid3, propertytreenodeid, policy);
        } else if (operation.equals(OPERATION_GET_REMOTE_USER)) {
            if (policy.isMasterRepositoryEnabled()) {
                String remoteServerURL = policy.getRepositoryURL();
                if (remoteServerURL.indexOf("/sc") != remoteServerURL.length() - 3) {
                    remoteServerURL = remoteServerURL + "/sc";
                }
                String authtoken = policy.getRepositoryAuthToken();
                ExternalHandlerProcessor externalHandlerProcessor = new ExternalHandlerProcessor(authtoken, remoteServerURL);
                try {
                    PropertyList returnPL = externalHandlerProcessor.sendCommandToLIMS("COMMAND_CHECK_CONNECTION", new PropertyList());
                    actionProps.setProperty("repositoryuser", returnPL);
                    actionProps.setProperty("repositoryurl", remoteServerURL);
                }
                catch (Exception e) {
                    actionProps.setProperty("error", "Error connecting to Master Repository:" + e.getMessage());
                }
            } else {
                actionProps.setProperty("error", "Master Repository is not enabled!");
            }
        } else {
            throw new SapphireException("RepositoryOperations must be called with a valid operation of checkout, checkin, update, compare.");
        }
    }

    private File getSnapshotPackageCommon(PropertyList actionProps, String sdcid, String keyid1, String keyid2, String keyid3, String propertytreenodeid, CMTPolicy policy, boolean checkout) throws SapphireException {
        File snapshotpageFile = null;
        if (policy.isMasterRepositoryEnabled()) {
            String remoteServerURL = policy.getRepositoryURL();
            if (remoteServerURL.indexOf("/sc") != remoteServerURL.length() - 3) {
                remoteServerURL = remoteServerURL + "/sc";
            }
            String authtoken = policy.getRepositoryAuthToken();
            ExternalHandlerProcessor externalHandlerProcessor = new ExternalHandlerProcessor(authtoken, remoteServerURL);
            PropertyList props = new PropertyList();
            DataSet dsWithIdentifyCol = null;
            String identifycolumns = policy.getIndentifyColumn();
            if (identifycolumns.length() > 0) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid(sdcid);
                sdiRequest.setKeyid1List(keyid1);
                sdiRequest.setKeyid2List(keyid2);
                sdiRequest.setKeyid3List(keyid3);
                sdiRequest.setRequestItem("primary[" + identifycolumns + "]");
                sdiRequest.setRetrieve(true);
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                dsWithIdentifyCol = sdiData.getDataset("primary");
            }
            if (dsWithIdentifyCol != null && dsWithIdentifyCol.getRowCount() > 0) {
                String[] columns = dsWithIdentifyCol.getColumns();
                for (int c = 0; c < columns.length; ++c) {
                    if (columns[c].indexOf("__") == 0) continue;
                    props.setProperty(columns[c], dsWithIdentifyCol.getColumnValues(columns[c], ";"));
                }
            } else {
                props.setProperty("keyfromremotelookup", "Y");
            }
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            props.setProperty("keyid2", keyid2);
            props.setProperty("keyid3", keyid3);
            props.setProperty("propertytreenodeid", propertytreenodeid);
            props.setProperty("changerequestid", actionProps.getProperty("changerequestid"));
            props.setProperty(OPERATION_CHECKOUT, checkout ? "Y" : "N");
            try {
                Path exportpackage = Files.createTempFile("exportpackage", ".zip", new FileAttribute[0]);
                externalHandlerProcessor.sendDownloadFileCommandToLIMS("COMMAND_CHECK_OUT", props.toJSONObject(false, false), exportpackage);
                PropertyList importProps = new PropertyList();
                importProps.setProperty(PROPERTY_REMOTE_CHECKOUT, "Y");
                boolean fileExist = exportpackage.toFile().exists();
                if (!fileExist) {
                    throw new SapphireException("Failed to download file to " + exportpackage.toFile().getAbsolutePath());
                }
                snapshotpageFile = exportpackage.toFile();
            }
            catch (Exception e) {
                try {
                    String remoteerror = e.getMessage();
                    remoteerror = remoteerror.substring(remoteerror.indexOf("LIMS: ") + 6);
                    JSONObject errorprops = new JSONObject(remoteerror);
                    remoteerror = errorprops.getString("message");
                    throw new SapphireException(remoteerror + ":" + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3 + ";" + propertytreenodeid, e);
                }
                catch (JSONException je) {
                    throw new SapphireException(je);
                }
            }
        } else {
            throw new SapphireException("Master Repository not enabled.");
        }
        return snapshotpageFile;
    }

    private void checkout(PropertyList actionProps, String sdcid, String keyid1, String keyid2, String keyid3, String propertytreenodeid, CMTPolicy policy) throws SapphireException {
        try {
            File exportpackage = this.getSnapshotPackageCommon(actionProps, sdcid, keyid1, keyid2, keyid3, propertytreenodeid, policy, true);
            PropertyList importProps = new PropertyList();
            importProps.setProperty(PROPERTY_REMOTE_CHECKOUT, "Y");
            String absolutePath = exportpackage.getAbsolutePath();
            boolean isUpdateLocal = "Y".equals(actionProps.getProperty("overridelocalchanges"));
            if (isUpdateLocal) {
                importProps.put("filename", absolutePath);
                this.getActionProcessor().processActionClass(ImportSnapshot.class.getName(), importProps);
            } else {
                importProps.put("filename", absolutePath);
                importProps.put("ignoreifexist", "Y");
                this.getActionProcessor().processActionClass(ImportSnapshot.class.getName(), importProps);
            }
            String[] newkeys = this.getNewKeys(sdcid, keyid1, keyid2, keyid3, importProps);
            if (newkeys != null) {
                keyid1 = newkeys[0];
                keyid2 = newkeys[0];
                keyid3 = newkeys[0];
            }
            PropertyList localchangeoutprops = new PropertyList();
            localchangeoutprops.setProperty("sdcid", sdcid);
            localchangeoutprops.setProperty("keyid1", keyid1);
            localchangeoutprops.setProperty("keyid2", keyid2);
            localchangeoutprops.setProperty("keyid3", keyid3);
            if ("PropertyTree".equals(sdcid)) {
                localchangeoutprops.setProperty("propertytreenodeid", propertytreenodeid);
            }
            localchangeoutprops.setProperty("changelogreason", "Remote Check Out From Repository");
            localchangeoutprops.setProperty("notes", "Remote check out changerequestid:" + actionProps.getProperty("changerequestid"));
            this.getActionProcessor().processActionClass(CheckOutSDI.class.getName(), localchangeoutprops);
        }
        catch (Exception e) {
            throw new SapphireException("Remote Checkout failed! " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
    }

    private String[] getNewKeys(String sdcid, String keyid1, String keyid2, String keyid3, HashMap importProps) {
        String[] newkeys = null;
        HashMap oldnewkeymap = (HashMap)importProps.get("oldnewkeymap");
        if (oldnewkeymap != null && oldnewkeymap.get(sdcid) != null) {
            HashMap sdcoldnewkeymap = (HashMap)oldnewkeymap.get(sdcid);
            DataSet oldnewkeyDs = new DataSet();
            oldnewkeyDs.addColumnValues("keyid1", 0, keyid1, ";");
            oldnewkeyDs.addColumnValues("keyid2", 0, keyid2, ";");
            oldnewkeyDs.addColumnValues("keyid3", 0, keyid3, ";");
            oldnewkeyDs.addColumn("newkeyid1", 0);
            oldnewkeyDs.addColumn("newkeyid2", 0);
            oldnewkeyDs.addColumn("newkeyid3", 0);
            for (int i = 0; i < oldnewkeyDs.getRowCount(); ++i) {
                String newkey = (String)sdcoldnewkeymap.get(oldnewkeyDs.getValue(i, "keyid1") + ";" + oldnewkeyDs.getValue(i, "keyid2") + ";" + oldnewkeyDs.getValue(i, "keyid3"));
                if (newkey == null || !newkey.contains(";")) continue;
                String[] tempnewkeys = StringUtil.split(newkey, ";");
                oldnewkeyDs.setValue(i, "newkeyid1", tempnewkeys[0]);
                oldnewkeyDs.setValue(i, "newkeyid2", tempnewkeys[1]);
                oldnewkeyDs.setValue(i, "newkeyid3", tempnewkeys[2]);
            }
            if (oldnewkeyDs.getRowCount() > 0) {
                newkeys = new String[]{oldnewkeyDs.getColumnValues("newkeyid1", ";"), oldnewkeyDs.getColumnValues("newkeyid2", ";"), oldnewkeyDs.getColumnValues("newkeyid3", ";")};
            }
        }
        return newkeys;
    }

    private void checkin(PropertyList actionProps, String sdcid, String keyid1, String keyid2, String keyid3, String propertytreenodeid, CMTPolicy policy) throws SapphireException {
        if (policy.isMasterRepositoryEnabled()) {
            if (actionProps.getProperty("changelogid").length() == 0) {
                String keycol = this.getSDCProcessor().getProperty(sdcid, "keycolumns");
                int keycolumns = keycol.length() > 0 ? Integer.parseInt(keycol) : 1;
                String rsetid = "";
                rsetid = "PropertyTree".equals(sdcid) ? this.getDAMProcessor().createRSet(sdcid, keyid1, propertytreenodeid, "") : this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                String sql = " SELECT linkkeyid1 keyid1, linkkeyid2 keyid2, linkkeyid3 keyid3 FROM changelog, rsetitems WHERE changelogstatus='Checked Out' AND rsetitems.sdcid = changelog.linksdcid AND rsetitems.keyid1 = changelog.linkkeyid1";
                if ("PropertyTree".equals(sdcid)) {
                    sql = sql + " AND rsetitems.keyid2 = changelog.propertytreenodeid";
                } else {
                    if (keycolumns > 1) {
                        sql = sql + " AND rsetitems.keyid2 = changelog.linkkeyid2";
                    }
                    if (keycolumns > 2) {
                        sql = sql + " AND rsetitems.keyid3 = changelog.linkkeyid3";
                    }
                }
                sql = sql + " AND rsetitems.rsetid = ?";
                DataSet checkedoutds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
                if (checkedoutds.getRowCount() < StringUtil.split(keyid1, ";").length) {
                    DataSet notcheckedoutDS = new DataSet();
                    notcheckedoutDS.addColumnValues("keyid1", 0, keyid1, ";");
                    notcheckedoutDS.addColumnValues("keyid2", 0, keyid2, ";");
                    notcheckedoutDS.addColumnValues("keyid3", 0, keyid3, ";");
                    if ("PropertyTree".equals(sdcid)) {
                        notcheckedoutDS.addColumnValues("propertytreenodeid", 0, propertytreenodeid, ";");
                    }
                    HashMap<String, String> filterMap = new HashMap<String, String>();
                    for (int i = notcheckedoutDS.getRowCount(); i >= 0; --i) {
                        filterMap.clear();
                        filterMap.put("keyid1", notcheckedoutDS.getString(i, "keyid1"));
                        filterMap.put("keyid2", notcheckedoutDS.getString(i, "keyid2"));
                        filterMap.put("keyid3", notcheckedoutDS.getString(i, "keyid3"));
                        if ("PropertyTree".equals(sdcid)) {
                            filterMap.put("propertytreenodeid", notcheckedoutDS.getString(i, "propertytreenodeid"));
                        }
                        if (checkedoutds.findRow(filterMap) <= -1) continue;
                        notcheckedoutDS.deleteRow(i);
                    }
                    if (notcheckedoutDS.getRowCount() > 0) {
                        PropertyList checkoutProps = new PropertyList();
                        checkoutProps.setProperty("sdcid", sdcid);
                        checkoutProps.setProperty("changelogreason", "Remote Check Into Respository");
                        checkoutProps.setProperty("keyid1", notcheckedoutDS.getColumnValues("keyid1", ";"));
                        checkoutProps.setProperty("keyid2", notcheckedoutDS.getColumnValues("keyid2", ";"));
                        checkoutProps.setProperty("keyid3", notcheckedoutDS.getColumnValues("keyid3", ";"));
                        if ("PropertyTree".equals(sdcid)) {
                            checkoutProps.setProperty("propertytreenodeid", notcheckedoutDS.getColumnValues("propertytreenodeid", ";"));
                        }
                        this.getActionProcessor().processActionClass(CheckOutSDI.class.getName(), checkoutProps);
                        actionProps.setProperty("changelogid", checkoutProps.getProperty("changelogid"));
                    }
                }
            }
            actionProps.setProperty("changelogreason", "Remote Check Into Respository");
            this.getActionProcessor().processActionClass(CheckInSDI.class.getName(), actionProps);
            String changelogidList = actionProps.getProperty("changelogid");
            PropertyList exportprops = new PropertyList();
            if (changelogidList.length() > 0) {
                exportprops.setProperty("sdcid", "LV_ChangeLog");
                exportprops.setProperty("keyid1", changelogidList);
            } else {
                exportprops.setProperty("sdcid", sdcid);
                exportprops.setProperty("keyid1", keyid1);
                exportprops.setProperty("keyid2", keyid2);
                exportprops.setProperty("keyid3", keyid3);
                exportprops.setProperty("istransfer", "N");
                if ("PropertyTree".equals(sdcid)) {
                    exportprops.setProperty("nodelist", propertytreenodeid);
                }
            }
            this.getActionProcessor().processActionClass(ExportSnapshot.class.getName(), exportprops);
            String zipfilepath = exportprops.getProperty("zipfilepath");
            Path file = Paths.get(zipfilepath, new String[0]);
            file.toFile().deleteOnExit();
            String remoteServerURL = policy.getRepositoryURL();
            if (remoteServerURL.indexOf("/sc") != remoteServerURL.length() - 3) {
                remoteServerURL = remoteServerURL + "/sc";
            }
            String authtoken = policy.getRepositoryAuthToken();
            ExternalHandlerProcessor externalHandlerProcessor = new ExternalHandlerProcessor(authtoken, remoteServerURL);
            try {
                JSONObject remotecheckinCommand = new JSONObject();
                remotecheckinCommand.put("notes", "Remote Checked In - " + actionProps.getProperty("notes"));
                remotecheckinCommand.put("changerequestid", actionProps.getProperty("changerequestid"));
                JSONObject jSONObject = externalHandlerProcessor.sendFileCommandToLIMS("COMMAND_IMPORT", file, remotecheckinCommand);
            }
            catch (JSONException e) {
                throw new SapphireException(e);
            }
        }
    }

    private void update(PropertyList actionProps, String sdcid, String keyid1, String keyid2, String keyid3, String propertytreenodeid, CMTPolicy policy) throws SapphireException {
        File exportpackage = this.getSnapshotPackageCommon(actionProps, sdcid, keyid1, keyid2, keyid3, propertytreenodeid, policy, false);
        PropertyList importProps = new PropertyList();
        String absolutePath = exportpackage.getAbsolutePath();
        importProps.put("filename", absolutePath);
        importProps.setProperty(PROPERTY_REMOTE_CHECKOUT, "Y");
        int keycolumns = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
        String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
        String sql = " select linkkeyid1 keyid1, linkkeyid2 keyid2, linkkeyid3 keyid3 from changelog, rsetitems";
        sql = sql + " where changelogstatus='Checked Out' and rsetitems.sdcid = changelog.linksdcid";
        sql = sql + " and rsetitems.keyid1 = changelog.linkkeyid1";
        if (keycolumns > 1) {
            sql = sql + " and rsetitems.keyid2 = changelog.linkkeyid2";
        }
        if (keycolumns > 2) {
            sql = sql + " and rsetitems.keyid3 = changelog.linkkeyid3";
        }
        sql = sql + " and rsetitems.rsetid = ?";
        DataSet checkedoutds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
        this.getDAMProcessor().clearRSet(rsetid);
        actionProps.setProperty("changelogreason", "Remote Update From Respository");
        actionProps.setProperty("notes", "Remote Update From Respository");
        this.getActionProcessor().processActionClass(ImportSnapshot.class.getName(), importProps);
        boolean hasSDIToCheckIn = true;
        if (checkedoutds.getRowCount() > 0) {
            DataSet checkinDS = new DataSet();
            checkinDS.addColumnValues("keyid1", 0, keyid1, ";");
            checkinDS.addColumnValues("keyid2", 0, keyid2, ";");
            checkinDS.addColumnValues("keyid3", 0, keyid3, ";");
            checkinDS.removeAll(checkedoutds);
            if (checkinDS.getRowCount() > 0) {
                actionProps.setProperty("keyid1", checkinDS.getColumnValues("keyid1", ";"));
                actionProps.setProperty("keyid2", checkinDS.getColumnValues("keyid2", ";"));
                actionProps.setProperty("keyid3", checkinDS.getColumnValues("keyid3", ";"));
            } else {
                hasSDIToCheckIn = false;
            }
        }
        if (hasSDIToCheckIn) {
            this.getActionProcessor().processActionClass(CheckInSDI.class.getName(), actionProps);
        }
    }

    private void getSnapshot(PropertyList actionProps, String sdcid, String keyid1, String keyid2, String keyid3, String propertytreenodeid, CMTPolicy policy) throws SapphireException {
        File exportpackage = this.getSnapshotPackageCommon(actionProps, sdcid, keyid1, keyid2, keyid3, propertytreenodeid, policy, false);
        boolean fileExist = exportpackage.exists();
        if (!fileExist) {
            throw new SapphireException("Failed to retrieve Snapshot from Master Repository for " + sdcid + ";" + keyid1 + ";" + keyid2 + ";" + keyid3);
        }
        ArrayList<SnapshotPackage> snapshotPackageList = new ArrayList<SnapshotPackage>();
        snapshotPackageList.add(SnapshotPackage.fromFile(exportpackage, this.getConnectionId()));
        actionProps.put("snapshotpackage", snapshotPackageList.get(0));
    }
}

