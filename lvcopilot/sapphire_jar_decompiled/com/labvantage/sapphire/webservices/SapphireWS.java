/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.xml.soap.SOAPException
 */
package com.labvantage.sapphire.webservices;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.AttachmentManagerLocal;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import com.labvantage.sapphire.webservices.SapphireBasicWS;
import com.labvantage.sapphire.webservices.messages.BaseSECMessage;
import com.labvantage.sapphire.webservices.transport.ActionBlockTransportBean;
import com.labvantage.sapphire.webservices.transport.AttachmentTransportBean;
import com.labvantage.sapphire.webservices.transport.DataSetTransportBean;
import com.labvantage.sapphire.webservices.transport.PropertyListTransportBean;
import com.labvantage.sapphire.webservices.transport.SDIDataTransportBean;
import com.labvantage.sapphire.webservices.transport.SDIRequestTransportBean;
import java.util.HashMap;
import javax.xml.soap.SOAPException;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class SapphireWS
extends SapphireBasicWS {
    public String processApplicationCommand(String command, String paramsXML) throws SOAPException {
        Trace.startThreadMDCBlank("SOAP");
        try {
            PropertyList propertyList = new PropertyList();
            propertyList.setPropertyList(paramsXML);
            HashMap params = new HashMap(propertyList);
            params.put("command", command);
            params = ServiceLocator.getInstance().getSapphireManager().processCommand(params);
            return new PropertyList(params).toXMLString();
        }
        catch (Exception e) {
            throw new SOAPException("Failed to process command. Reason: " + e.getMessage());
        }
    }

    public String getSqlDataSet(String connectionid, int sqlCode) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            QueryProcessor qp = new QueryProcessor(connectionid);
            DataSet ds = qp.getSqlDataSet(sqlCode);
            if (ds != null) {
                return ds.toXML();
            }
            throw new SOAPException(qp.getLastErrorMessage());
        }
        catch (Exception e) {
            throw new SOAPException("Failed to getSqlDataSet with sqlCode '" + sqlCode + "'. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public String getSqlDataSet(String connectionid, int sqlCode, Object[] bindVars) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            QueryProcessor qp = new QueryProcessor(connectionid);
            DataSet ds = qp.getPreparedSqlDataSet(sqlCode, bindVars);
            if (ds != null) {
                return ds.toXML();
            }
            throw new SOAPException(qp.getLastErrorMessage());
        }
        catch (Exception e) {
            throw new SOAPException("Failed to getSqlDataSet with sqlCode '" + sqlCode + "'. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public int execSQL(String connectionid, String sql) throws SOAPException {
        if (SecurityPolicyUtil.isUnregisteredSQLPermitted(connectionid = this.startSOAPMethod(connectionid), "webservices", "execSQL", sql)) {
            try {
                QueryProcessor qp = new QueryProcessor(connectionid);
                int rc = qp.execSQL(sql);
                return rc;
            }
            catch (Exception e) {
                throw new SOAPException("Failed to execSQL with sql '" + sql + "'. Reason: " + e.getMessage(), (Throwable)e);
            }
        }
        throw new SOAPException("Failed to perform web service request. Reason: execSQL using unregistered SQL disabled in security policy");
    }

    public int execSQL(String connectionid, int sqlCode) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            QueryProcessor qp = new QueryProcessor(connectionid);
            int rc = qp.execSQL(sqlCode);
            return rc;
        }
        catch (Exception e) {
            throw new SOAPException("Failed to execSQL with sqlCode '" + sqlCode + "'. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public int execSQL(String connectionid, int sqlCode, Object[] bindVars) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            QueryProcessor qp = new QueryProcessor(connectionid);
            int rc = qp.execSQL(sqlCode, bindVars);
            return rc;
        }
        catch (Exception e) {
            throw new SOAPException("Failed to execSQL with sqlCode '" + sqlCode + "'. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public String translateTable(String connectionid, String languageid, String propertyListXML) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            TranslationProcessor tp = new TranslationProcessor(connectionid);
            PropertyList propertyList = new PropertyList();
            propertyList.setPropertyList(propertyListXML);
            HashMap transTable = new HashMap(propertyList);
            tp.translateTable(languageid, transTable);
            String xml = new PropertyList(transTable).toXMLString();
            return xml;
        }
        catch (SapphireException e) {
            throw new SOAPException("Failed to parse propertylist. Reason: " + e.getMessage(), (Throwable)e);
        }
        catch (Exception e) {
            throw new SOAPException("Failed to translate table. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public PropertyListTransportBean translateTable(String connectionid, String languageid, PropertyListTransportBean propertyList) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        try {
            TranslationProcessor tp = new TranslationProcessor(connectionid);
            HashMap transTable = new HashMap(propertyList.toPropertyList());
            tp.translateTable(languageid, transTable);
            PropertyListTransportBean out = new PropertyListTransportBean(new PropertyList(transTable));
            return out;
        }
        catch (Exception e) {
            throw new SOAPException("Failed to translate table. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public String[] getDatabaseList() throws SOAPException {
        Trace.startThreadMDCBlank("SOAP");
        try {
            ConnectionProcessor cp = new ConnectionProcessor();
            String[] list = cp.getDatabaseList();
            if (list != null && list.length > 0) {
                return list;
            }
            return new String[0];
        }
        catch (Exception e) {
            throw new SOAPException("Failed to obtain database list. Reason: " + e.getMessage(), (Throwable)e);
        }
    }

    public DataSetTransportBean getSqlDataSet(String connectionid, String sql, boolean returnClobs) throws SOAPException {
        if (SecurityPolicyUtil.isUnregisteredSQLPermitted(connectionid = this.startSOAPMethod(connectionid), "webservices", "getSqlDataSet", sql)) {
            if (connectionid != null && connectionid.length() > 0) {
                try {
                    QueryProcessor qp = new QueryProcessor(connectionid);
                    DataSet ds = qp.getSqlDataSet(sql, returnClobs);
                    if (ds != null) {
                        return new DataSetTransportBean(ds, true, false);
                    }
                    throw new SOAPException(qp.getLastErrorMessage());
                }
                catch (Exception e) {
                    throw new SOAPException("Failed to obtain data with sql '" + sql + "'. Reason: " + e.getMessage(), (Throwable)e);
                }
            }
            throw new SOAPException("Failed to obtain data. Reason: Connection id invalid.");
        }
        throw new SOAPException("Failed to perform web service request. Reason: getSqlDataSet using unregistered SQL disabled in security policy");
    }

    public DataSetTransportBean getSqlDataSet(String connectionid, int sqlCode, boolean returnClobs) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) != null && connectionid.length() > 0) {
            try {
                QueryProcessor qp = new QueryProcessor(connectionid);
                DataSet ds = qp.getSqlDataSet(sqlCode, returnClobs);
                if (ds != null) {
                    return new DataSetTransportBean(ds, true, false);
                }
                throw new SOAPException(qp.getLastErrorMessage());
            }
            catch (Exception e) {
                throw new SOAPException("Failed to obtain data with sqlcode '" + sqlCode + "'. Reason: " + e.getMessage(), (Throwable)e);
            }
        }
        throw new SOAPException("Failed to obtain data. Reason: Connection id invalid.");
    }

    public DataSetTransportBean getSqlDataSet(String connectionid, int sqlCode, Object[] bindVars, boolean returnClobs) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) != null && connectionid.length() > 0) {
            try {
                QueryProcessor qp = new QueryProcessor(connectionid);
                DataSet ds = qp.getSqlDataSet(sqlCode, bindVars, returnClobs);
                if (ds != null) {
                    return new DataSetTransportBean(ds, true, false);
                }
                throw new SOAPException(qp.getLastErrorMessage());
            }
            catch (Exception e) {
                throw new SOAPException("Failed to obtain data with sqlcode '" + sqlCode + "'. Reason: " + e.getMessage(), (Throwable)e);
            }
        }
        throw new SOAPException("Failed to obtain data. Reason: Connection id invalid.");
    }

    public PropertyListTransportBean getSDCProperties(String connectionid, String sdcid) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) != null && connectionid.length() > 0) {
            try {
                SDCProcessor sdc = new SDCProcessor(connectionid);
                if (sdc != null) {
                    PropertyList props = sdc.getPropertyList(sdcid);
                    if (props != null) {
                        return new PropertyListTransportBean(props);
                    }
                    throw new SOAPException("Failed to obtain sdc properties. Reason: SDC Id is invalid.");
                }
                throw new SOAPException("Failed to obtain sdc properties. Reason: SDC processor could not be created");
            }
            catch (Exception e) {
                throw new SOAPException("Failed to obtain sdc properties. Reason: " + e.getMessage(), (Throwable)e);
            }
        }
        throw new SOAPException("Failed to obtain sdc properties. Reason: Connection id invalid.");
    }

    public SDIDataTransportBean getSDIData(String connectionid, SDIRequestTransportBean sdireqbean) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) != null && connectionid.length() > 0) {
            if (sdireqbean != null) {
                try {
                    SDIRequest sdireq = sdireqbean.toSDIRequest();
                    String sdcid = sdireq.getSDCid();
                    if (sdcid != null && sdcid.length() > 0) {
                        SDCProcessor sdcProc = new SDCProcessor(connectionid);
                        PropertyList sdcProps = sdcProc.getPropertyList(sdcid);
                        if (sdcProps != null) {
                            if (sdireq.getQueryFrom() == null || sdireq.getQueryFrom().length() == 0) {
                                sdireq.setQueryFrom(sdcProps.getProperty("tableid"));
                            }
                            if (sdireq.getRequestItems() == null || sdireq.getRequestItems().length == 0) {
                                sdireq.setRequestItem("primary[]");
                            }
                            SDIProcessor sdiproc = new SDIProcessor(connectionid);
                            SDIData sdi = sdiproc.getSDIData(sdireq);
                            return new SDIDataTransportBean(sdi);
                        }
                        throw new SOAPException("Failed to obtain sdi data. Reason: SDC Id is invalid.");
                    }
                    throw new SOAPException("Failed to obtain sdi data. Reason: No SDC Id provided.");
                }
                catch (Exception e) {
                    throw new SOAPException("Failed to obtain sdi data. Reason: " + e.getMessage(), (Throwable)e);
                }
            }
            throw new SOAPException("Failed to obtain sdi data. Reason: SDI Request is invalid.");
        }
        throw new SOAPException("Failed to obtain sdi data. Reason: Connection id invalid.");
    }

    public PropertyListTransportBean processAction(String connectionid, String actionid, String actionversionid, PropertyListTransportBean propertyList) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        if (propertyList != null) {
            HashMap props = new HashMap(propertyList.toPropertyList());
            HashMap retProps = this.processAction(connectionid, actionid, actionversionid, props);
            return new PropertyListTransportBean(new PropertyList(retProps));
        }
        throw new SOAPException("No property list provided.");
    }

    public ActionBlockTransportBean processActionBlock(String connectionid, ActionBlockTransportBean block) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) != null && connectionid.length() > 0) {
            ActionBlock ab = block.toActionBlock();
            if (SecurityPolicyUtil.isActionBlockPermitted(connectionid, "webservices", "actionprocessing", ab)) {
                try {
                    ActionProcessor ap = new ActionProcessor(connectionid);
                    ap.processActionBlock(ab);
                    return new ActionBlockTransportBean(ab);
                }
                catch (Exception e) {
                    throw new SOAPException(e.getMessage());
                }
            }
            throw new SOAPException("Failed to process actionblock. Reason: Execution of one or more actions in the actionclock not permitted by security policy.");
        }
        throw new SOAPException("No connection id provided.");
    }

    @Override
    public BaseSECMessage processMessage(String connectionid, BaseSECMessage message, String processingMode) throws SOAPException {
        connectionid = this.startSOAPMethod(connectionid);
        return super.processMessage(connectionid, message, processingMode);
    }

    public AttachmentTransportBean getSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, boolean zip) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) != null && connectionid.length() > 0) {
            if (sdcid != null && sdcid.length() > 0) {
                if (keyid1 != null && keyid1.length() > 0) {
                    if (keyid2 == null || keyid2.length() == 0) {
                        keyid2 = "(null)";
                    }
                    if (keyid3 == null || keyid3.length() == 0) {
                        keyid3 = "(null)";
                    }
                    try {
                        AttachmentManagerLocal atman = ServiceLocator.getInstance().getAttachmentManager();
                        Attachment att = (Attachment)atman.getSDIAttachment(connectionid, sdcid, keyid1, keyid2, keyid3, attachmentNum);
                        if (att != null) {
                            try {
                                return new AttachmentTransportBean(att, zip);
                            }
                            catch (Exception e2) {
                                throw new SOAPException("Could not create SOAP attachment. Error = " + e2.getMessage());
                            }
                        }
                        throw new SOAPException("No attachment obtained.");
                    }
                    catch (SapphireException e) {
                        throw new SOAPException((Throwable)e);
                    }
                }
                throw new SOAPException("Key Id 1 not provided.");
            }
            throw new SOAPException("SDC Id not provided.");
        }
        throw new SOAPException("Connection Id not provided.");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void editSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum, AttachmentTransportBean attachment) throws SOAPException {
        AttachmentProcessor atProc;
        if ((connectionid = this.startSOAPMethod(connectionid)) == null || connectionid.length() <= 0) throw new SOAPException("Connection Id not provided.");
        if (sdcid == null || sdcid.length() <= 0) throw new SOAPException("SDC Id not provided.");
        if (keyid1 == null || keyid1.length() <= 0) throw new SOAPException("Key Id 1 not provided.");
        if (keyid2 == null || keyid2.length() == 0) {
            keyid2 = "(null)";
        }
        if (keyid3 == null || keyid3.length() == 0) {
            keyid3 = "(null)";
        }
        if ((atProc = new AttachmentProcessor(connectionid)) == null) throw new SOAPException("Could not create attachment processor.");
        Attachment att = attachment.toAttachment();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("keyid1", keyid1);
        props.put("keyid2", keyid2);
        props.put("keyid3", keyid3);
        props.put("sdcid", sdcid);
        props.put("attachmentnum", attachmentNum + "");
        props.put("description", att.getDescription());
        props.put("type", att.getType());
        props.put("oleclass", att.getOleClass());
        props.put("filename", att.getFilename());
        props.put("sourcefilename", att.getSourceFilename());
        props.put("applylock", "Y");
        if (atProc.editSDIAttachment(props, att.getData()) != 2) return;
        throw new SOAPException("Could not update attachment.");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void addSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, AttachmentTransportBean attachment) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) == null || connectionid.length() <= 0) throw new SOAPException("Connection Id not provided.");
        if (sdcid == null || sdcid.length() <= 0) throw new SOAPException("SDC Id not provided.");
        if (keyid1 == null || keyid1.length() <= 0) throw new SOAPException("Key Id 1 not provided.");
        if (keyid2 == null || keyid2.length() == 0) {
            keyid2 = "(null)";
        }
        if (keyid3 == null || keyid3.length() == 0) {
            keyid3 = "(null)";
        }
        AttachmentProcessor atProc = new AttachmentProcessor(connectionid);
        ConfigurationProcessor cp = new ConfigurationProcessor(connectionid);
        if (atProc == null) throw new SOAPException("Could not create attachment processor.");
        Attachment att = attachment.toAttachment();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("keyid1", keyid1);
        props.put("keyid2", keyid2);
        props.put("keyid3", keyid3);
        props.put("sdcid", sdcid);
        props.put("description", att.getDescription());
        props.put("type", "F");
        props.put("oleclass", att.getOleClass());
        props.put("sourcefilename", att.getSourceFilename());
        props.put("filename", att.getFilename());
        props.put("applylock", "N");
        if (atProc.addSDIAttachment(props, att.getData()) != 2) return;
        throw new SOAPException("Could not update attachment.");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void deleteSDIAttachment(String connectionid, String sdcid, String keyid1, String keyid2, String keyid3, int attachmentNum) throws SOAPException {
        if ((connectionid = this.startSOAPMethod(connectionid)) == null || connectionid.length() <= 0) throw new SOAPException("Connection Id not provided.");
        if (sdcid == null || sdcid.length() <= 0) throw new SOAPException("SDC Id not provided.");
        if (keyid1 == null || keyid1.length() <= 0) throw new SOAPException("Key Id 1 not provided.");
        if (keyid2 == null || keyid2.length() == 0) {
            keyid2 = "(null)";
        }
        if (keyid3 == null || keyid3.length() == 0) {
            keyid3 = "(null)";
        }
        try {
            AttachmentManagerLocal atman = ServiceLocator.getInstance().getAttachmentManager();
            atman.deleteSDIAttachment(connectionid, sdcid, keyid1, keyid2, keyid3, "" + attachmentNum, true);
            return;
        }
        catch (SapphireException e) {
            throw new SOAPException((Throwable)e);
        }
    }
}

