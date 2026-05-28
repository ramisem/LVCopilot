/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.nio.file.Paths;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseDataCapture
extends BaseAction
implements SDMSConstants {
    public static final String PROPERTY_TEMPFILEID = "tempfileid";
    public static final String PROPERTY_FILEPATH = "filepath";
    public static final String PROPERTY_ATTACHMENTCLASS = "attachmentclass";
    public static final String PROPERTY_FILEREFERENCE = "filereference";
    public static final String PROPERTY_FILEMETADATA = "filemetadata";
    public static final String PROPERTY_ATTACHMENTPOLICYNODE = "attachmentpolicynode";

    public void addAttachmentOperations(String instrumentid, String datacaptureid) throws SapphireException {
        BaseDataCapture.addAttachmentOperations(instrumentid, datacaptureid, false, this.getQueryProcessor(), this.database, this.logger);
    }

    public static void addAttachmentOperations(String instrumentid, String datacaptureid, boolean clear, QueryProcessor qp, DBAccess database, Logger logger) throws SapphireException {
        if (clear) {
            try {
                database.executeSQL("DELETE sdiattachmentoperation WHERE sdcid='LV_DataCapture' AND keyid1='" + datacaptureid + "'");
            }
            catch (Exception e) {
                logger.error("Failed to remove old capture operations.");
                throw new SapphireException(e);
            }
        }
        DataSet sdicaptureops = null;
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select sdaoInstr.* ,sdaoModel.propertyclob modelsvs,attachmenthandler.propertyclob ahsvs from sdiattachmentoperation sdaoInstr ");
        sql.append(" left join instrument on");
        sql.append(" instrument.instrumentid=sdaoInstr.keyid1");
        sql.append(" left join sdiattachmentoperation sdaoModel on");
        sql.append(" sdaoModel.attachmentoperationid=sdaoInstr.attachmentoperationid");
        sql.append(" and sdaoModel.sdcid='LV_InstrumentModel'");
        sql.append(" and sdaoModel.keyid1=instrument.instrumentmodelid");
        sql.append(" and sdaoModel.keyid2=instrument.instrumenttype");
        sql.append(" left join attachmenthandler on attachmenthandler.attachmenthandlerid=sdaoInstr.operationkeyid1");
        sql.append(" where sdaoInstr.sdcid='Instrument'");
        sql.append(" and sdaoInstr.keyid1=").append(safeSQL.addVar(instrumentid));
        try {
            sdicaptureops = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
        }
        catch (Exception e) {
            logger.info("Failed to fetch capture operation from Instrument.");
            throw new SapphireException(e);
        }
        if (sdicaptureops != null && sdicaptureops.getRowCount() > 0) {
            BaseDataCapture.mergPropertyclob(sdicaptureops);
            DataSet toInsert = sdicaptureops.copy();
            toInsert.setValue(-1, "sdcid", "LV_DataCapture");
            toInsert.setValue(-1, "keyid1", datacaptureid);
            toInsert.setValue(-1, "operationstatus", "ready");
            toInsert.addColumnValues("propertyclob", 3, sdicaptureops.getColumnValues("propertyclob", "|;|"), "|;|");
            DataSetUtil.insert(database, toInsert, "sdiattachmentoperation");
            logger.info("Capture operations located and copied down to Data Capture.");
        } else {
            logger.info("No capture operations located on instrument.");
        }
    }

    private static void mergPropertyclob(DataSet ds) throws SapphireException {
        for (int indx = 0; indx < ds.getRowCount(); ++indx) {
            String instrSVs = ds.getClob(indx, "propertyclob", "");
            String modelSVs = ds.getClob(indx, "modelsvs", "");
            String ahSVs = ds.getClob(indx, "ahsvs", "");
            PropertyList properties = BaseDataCapture.getPropsFromJSONString(ahSVs);
            PropertyListCollection plc = properties.getCollectionNotNull("variables");
            if (plc.size() > 0 && (instrSVs.length() > 0 || modelSVs.length() > 0)) {
                PropertyList instrSVsPL = BaseDataCapture.getPropsFromJSONString(instrSVs);
                PropertyList modelSVsPL = BaseDataCapture.getPropsFromJSONString(modelSVs);
                for (int i = 0; i < plc.size(); ++i) {
                    PropertyList varProps = (PropertyList)plc.get(i);
                    String variableid = varProps.getProperty("variableid", "");
                    if (instrSVsPL.containsKey(variableid)) {
                        varProps.setProperty("value", instrSVsPL.getProperty(variableid, ""));
                        continue;
                    }
                    if (!modelSVsPL.containsKey(variableid)) continue;
                    varProps.setProperty("value", modelSVsPL.getProperty(variableid, ""));
                }
            }
            if (properties.size() <= 0) continue;
            ds.setClob(indx, "propertyclob", properties.toJSONString());
        }
        ds.removeColumn("modelsvs");
        ds.removeColumn("ahsvs");
    }

    private static PropertyList getPropsFromJSONString(String json) throws SapphireException {
        PropertyList props = new PropertyList();
        if (json.length() > 0) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                props = new PropertyList(jsonObj);
            }
            catch (Exception e) {
                throw new SapphireException("Failed to parse JSON." + e.getMessage());
            }
        }
        return props;
    }

    public void addAttachments(PropertyList properties, String datacaptureid, PropertyList metadata, String connectionId) throws SapphireException {
        int f;
        String[] fa;
        String ac;
        String[] attachmentClass;
        AttachmentProcessor ap;
        String attachmentpolicynode = properties.getProperty(PROPERTY_ATTACHMENTPOLICYNODE, "Sapphire Custom");
        AttachmentProcessor attachmentProcessor = ap = this.getRakFile() != null ? new AttachmentProcessor(this.getRakFile(), this.getConnectionid()) : new AttachmentProcessor(this.getConnectionid());
        if (metadata != null && metadata.containsKey("filename") && !metadata.containsKey("mime")) {
            String fname = metadata.getProperty("filename");
            metadata.setProperty("mime", FileType.getFileTypeByFileName(fname, connectionId).getMime());
        }
        String[] stringArray = attachmentClass = properties.getProperty(PROPERTY_ATTACHMENTCLASS, "").length() > 0 ? StringUtil.split(properties.getProperty(PROPERTY_ATTACHMENTCLASS, ""), ";") : new String[]{};
        if (properties.getProperty(PROPERTY_TEMPFILEID).length() > 0) {
            String[] ta = StringUtil.split(properties.getProperty(PROPERTY_TEMPFILEID), ";");
            for (int t = 0; t < ta.length; ++t) {
                ac = attachmentClass.length > 0 ? (attachmentClass.length >= t ? attachmentClass[t] : attachmentClass[0]) : "";
                FileManager.addFileAttachment("LV_DataCapture", datacaptureid, "", "", ta[t], ac, metadata, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
            }
        }
        if (properties.getProperty(PROPERTY_FILEPATH).length() > 0) {
            fa = StringUtil.split(properties.getProperty(PROPERTY_FILEPATH), ";");
            for (f = 0; f < fa.length; ++f) {
                ac = attachmentClass.length > 0 ? (attachmentClass.length >= f ? attachmentClass[f] : attachmentClass[0]) : "";
                FileManager.addFileAttachment("LV_DataCapture", datacaptureid, "", "", Paths.get(fa[f], new String[0]), ac, false, metadata, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
            }
        }
        if (properties.getProperty(PROPERTY_FILEREFERENCE).length() > 0) {
            fa = StringUtil.split(properties.getProperty(PROPERTY_FILEREFERENCE), ";");
            for (f = 0; f < fa.length; ++f) {
                ac = attachmentClass.length > 0 ? (attachmentClass.length >= f ? attachmentClass[f] : attachmentClass[0]) : "";
                FileManager.addFileReferenceAttachment("LV_DataCapture", datacaptureid, "", "", Paths.get(fa[f], new String[0]), ac, metadata, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
            }
        }
    }

    public void addMetaData(PropertyList properties, String datacaptureid, boolean updateable) throws SapphireException {
        BaseSDIAttributeAction.addMetaData(properties, "LV_DataCapture", datacaptureid, null, null, updateable, this.getSDCProcessor(), this.getActionProcessor());
    }
}

