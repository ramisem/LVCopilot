/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MoveStorageUnit
extends BaseAction
implements sapphire.action.MoveStorageUnit {
    private HashMap allowedChildMap = new HashMap();
    private String auditreason;
    private String auditactivity;
    private String auditsignedflag;
    private String sdcruleconfirm;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        String storageunitid = props.getProperty("storageunitid");
        String parentid = props.getProperty("parentid");
        String propsmatch = props.getProperty("propsmatch", "N");
        this.sdcruleconfirm = props.getProperty("__sdcruleconfirm", "N");
        this.auditreason = props.getProperty("auditreason");
        this.auditactivity = props.getProperty("auditactivity", "");
        this.auditsignedflag = props.getProperty("auditsignedflag", "N");
        if (StringUtil.getLen(storageunitid) == 0L) {
            throw new SapphireException("Storageunit ID is null");
        }
        if (StringUtil.getLen(parentid) == 0L) {
            throw new SapphireException("Parent ID is null");
        }
        storageunitid = StringUtil.replaceAll(storageunitid, "%3B", ";");
        parentid = StringUtil.replaceAll(parentid, "%3B", ";");
        String[] s = StringUtil.split(storageunitid, ";");
        String[] p = StringUtil.split(parentid, ";");
        if ("Y".equals(propsmatch)) {
            if (s.length != p.length) throw new SapphireException("Invalid property value");
            for (int i = 0; i < s.length; ++i) {
                this.moveStorageUnit(s[i], p[i]);
            }
            return;
        } else {
            for (int i = 0; i < s.length; ++i) {
                for (int j = 0; j < p.length; ++j) {
                    this.moveStorageUnit(s[i], p[j]);
                }
            }
        }
    }

    private void moveStorageUnit(String storageunitid, String parentid) throws SapphireException {
        block15: {
            if (storageunitid.equals(parentid)) {
                throw new SapphireException("Move not allowed", "VALIDATION", this.getTranslationProcessor().translatePartial("{{Cannot move to self}}"));
            }
            StringBuilder sql = new StringBuilder();
            sql.append("select storageunitid, storageunittype, labelpath, moveableflag, linksdcid, linkkeyid1, parentid,");
            sql.append(" (select trackitem.trackitemid from trackitem where trackitem.linksdcid = storageunit.linksdcid and trackitem.linkkeyid1 = storageunit.linkkeyid1) trackitemid");
            sql.append(" from storageunit");
            sql.append(" where storageunitid = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageunitid});
            if (ds != null && ds.size() > 0) {
                String moveableflag = ds.getValue(0, "moveableflag");
                if (!"Y".equals(moveableflag)) {
                    throw new SapphireException("Move not allowed", "VALIDATION", this.getTranslationProcessor().translatePartial("{{Storageunit}} \"" + storageunitid + "\" {{is not moveable}}"));
                }
                String currentparentid = ds.getValue(0, "parentid");
                if (!parentid.equals(currentparentid)) {
                    String trackitemid = ds.getValue(0, "trackitemid");
                    if (StringUtil.getLen(trackitemid) > 0L) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "TrackItemSDC");
                        props.setProperty("keyid1", trackitemid);
                        props.setProperty("auditreason", this.auditreason);
                        props.setProperty("auditactivity", this.auditactivity);
                        props.setProperty("auditsignedflag", this.auditsignedflag);
                        props.setProperty("__sdcruleconfirm", this.sdcruleconfirm);
                        props.setProperty("currentstorageunitid", parentid);
                        try {
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                            if (errorHandler != null) {
                                this.setErrors(errorHandler);
                            }
                            break block15;
                        }
                        catch (ActionException e) {
                            ErrorHandler errorHandler = e.getErrorHandler();
                            if (errorHandler != null) {
                                this.setErrors(errorHandler);
                            }
                            break block15;
                        }
                    }
                    String storageunittype = ds.getValue(0, "storageunittype");
                    if (this.isChildAllowed(parentid, storageunittype)) {
                        try {
                            PropertyList props = new PropertyList();
                            PropertyList sprops = StorageUnitSDC.getStorageUnitProps(this.getQueryProcessor(), parentid);
                            String targetcd = sprops.getProperty("custodialdepartmentid");
                            if (StringUtil.getLen(targetcd) > 0L && !this.getDepartmentList().contains(targetcd)) {
                                StringBuffer sb = new StringBuffer();
                                sb.append("Storage Unit movement is not allowed. User is not a member of target custodial domain.\"");
                                throw new SapphireException("StorageUnitMove", "VALIDATION", this.getTranslationProcessor().translate(sb.toString()));
                            }
                            props.clear();
                            props.setProperty("sdcid", "StorageUnitSDC");
                            props.setProperty("keyid1", storageunitid);
                            props.setProperty("auditreason", this.auditreason);
                            props.setProperty("auditactivity", this.auditactivity);
                            props.setProperty("auditsignedflag", this.auditsignedflag);
                            props.setProperty("__sdcruleconfirm", this.sdcruleconfirm);
                            props.setProperty("parentid", parentid);
                            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                            ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                            if (errorHandler != null) {
                                this.setErrors(errorHandler);
                            }
                            break block15;
                        }
                        catch (ActionException e) {
                            ErrorHandler errorHandler = e.getErrorHandler();
                            if (errorHandler != null) {
                                this.setErrors(errorHandler);
                            }
                            break block15;
                        }
                    }
                    QueryProcessor queryProcessor = this.getQueryProcessor();
                    String storageunitlabelpath = ds.getValue(0, "labelpath", storageunitid);
                    String parentlabelpath = OpalUtil.getColumnValue(queryProcessor, "storageunit", "labelpath", "storageunitid = ?", new String[]{parentid});
                    StringBuilder sb = new StringBuilder();
                    sb.append("{{Following movement is not allowed}}<br>");
                    sb.append("{{StorageUnit}} \"").append(storageunitlabelpath).append("\"");
                    sb.append(" {{to}} ");
                    sb.append("{{StorageUnit}} \"").append(parentlabelpath).append("\"");
                    throw new SapphireException("StorageUnitValidChildrenRule", "VALIDATION", this.getTranslationProcessor().translatePartial(sb.toString()));
                }
            }
        }
    }

    private boolean isChildAllowed(String parentid, String childtype) throws SapphireException {
        if (!this.allowedChildMap.containsKey(parentid)) {
            this.allowedChildMap.put(parentid, StorageUnitSDC.getValidChildList(this.getQueryProcessor(), parentid));
        }
        List allowedChilds = (List)this.allowedChildMap.get(parentid);
        return allowedChilds.contains(childtype);
    }
}

