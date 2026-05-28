/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.documents.BaseAddDocumentAction;
import com.labvantage.sapphire.actions.eln.AddWorksheet;
import com.labvantage.sapphire.actions.eln.AddWorksheetSDI;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class CreateWorksheet
extends BaseAddDocumentAction
implements sapphire.action.CreateWorksheet,
DocumentConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String formid = properties.getProperty("formid");
        String formversionid = properties.getProperty("formversionid");
        String documentid = properties.getProperty("documentid");
        String documentversionid = properties.getProperty("documentversionid");
        if (formid.length() == 0 && documentid.length() == 0) {
            String templateid = properties.getProperty("templateid");
            String templateversionid = properties.getProperty("templateversionid", "1");
            String workbookid = properties.getProperty("workbookid");
            String workbookversionid = properties.getProperty("workbookversionid", "1");
            String authorid = properties.getProperty("authorid", this.connectionInfo.getSysuserId());
            String worksheetname = properties.getProperty("worksheetname");
            if (authorid.equalsIgnoreCase("(system)") || authorid.length() == 0) {
                throw new ActionException("You must specify an author to create a worksheet via automation");
            }
            if (templateid.length() == 0) {
                throw new ActionException("You must specify a template to create a worksheet");
            }
            if (workbookid.length() == 0) {
                String[] userworkbook = BaseELNAction.getUserWorkbook(authorid, this.database, this.getActionProcessor(), new ConfigurationProcessor(this.connectionInfo.getConnectionId()), true);
                workbookid = userworkbook[0];
                workbookversionid = userworkbook[1];
            }
            String metadata_id = properties.getProperty("metadata_id");
            String metadata_value = properties.getProperty("metadata_value");
            String limsdata_sdcid = properties.getProperty("limsdata_sdcid");
            String limsdata_keyid1 = properties.getProperty("limsdata_keyid1");
            String limsdata_keyid2 = properties.getProperty("limsdata_keyid2");
            String limsdata_keyid3 = properties.getProperty("limsdata_keyid3");
            PropertyList substitutions = new PropertyList();
            substitutions.putAll(properties);
            if (metadata_id.length() > 0 && metadata_value.length() > 0) {
                String[] metadata_ids = StringUtil.split(metadata_id, ";");
                String[] metadata_values = StringUtil.split(metadata_value, ";");
                for (int i = 0; i < metadata_values.length; ++i) {
                    substitutions.setProperty(metadata_ids[i], metadata_values[i]);
                }
            }
            if (worksheetname.length() == 0) {
                DataSet template = this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{templateid, templateversionid}, true);
                PropertyList options = new PropertyList();
                options.setPropertyList(template.getClob(0, "options", ""));
                SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
                worksheetname = BaseELNAction.resolveWorksheetName(sapphireConnection, this.getSequenceProcessor(), options.getProperty("worksheetnametemplate"), substitutions, null);
            }
            PropertyList wsProps = new PropertyList();
            wsProps.setProperty("sdcid", "LV_Worksheet");
            wsProps.setProperty("worksheetversionid", "1");
            wsProps.setProperty("worksheetdesc", worksheetname);
            wsProps.setProperty("worksheetname", worksheetname);
            wsProps.setProperty("authorid", authorid);
            wsProps.setProperty("authordt", authorid.length() > 0 ? "now" : "(null)");
            wsProps.setProperty("workbookid", workbookid);
            wsProps.setProperty("workbookversionid", workbookversionid);
            wsProps.setProperty("templateid", templateid);
            wsProps.setProperty("templateversionid", templateversionid);
            wsProps.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processActionClass(AddWorksheet.class.getName(), wsProps);
            String worksheetid = wsProps.getProperty("worksheetid");
            String worksheetversionid = wsProps.getProperty("worksheetversionid");
            if (metadata_id.length() > 0 && metadata_value.length() > 0) {
                DataSet attributes = this.getQueryProcessor().getPreparedSqlDataSet("SELECT attributeid FROM sdiattribute WHERE sdcid='LV_Worksheet' AND keyid1=? AND keyid2=?", (Object[])new String[]{worksheetid, worksheetversionid});
                StringBuffer editMetaData_id = new StringBuffer();
                StringBuffer editMetaData_value = new StringBuffer();
                StringBuffer addMetaData_id = new StringBuffer();
                StringBuffer addMetaData_value = new StringBuffer();
                String[] metadata_ids = StringUtil.split(metadata_id, ";");
                String[] metadata_values = StringUtil.split(metadata_value, ";");
                int editCount = 0;
                int addCount = 0;
                for (int i = 0; i < metadata_values.length; ++i) {
                    if (attributes.findRow("attributeid", metadata_ids[i]) >= 0) {
                        editMetaData_id.append(";").append(metadata_ids[i]);
                        editMetaData_value.append(";").append(metadata_values[i]);
                        ++editCount;
                        continue;
                    }
                    addMetaData_id.append(";").append(metadata_ids[i]);
                    addMetaData_value.append(";").append(metadata_values[i]);
                    ++addCount;
                }
                if (addMetaData_id.length() > 0) {
                    PropertyList addMetadataValues = new PropertyList();
                    addMetadataValues.setProperty("sdcid", "LV_Worksheet");
                    addMetadataValues.setProperty("keyid1", worksheetid);
                    addMetadataValues.setProperty("keyid2", worksheetversionid);
                    addMetadataValues.setProperty("attributeid", addMetaData_id.substring(1));
                    addMetadataValues.setProperty("value", addMetaData_value.substring(1));
                    addMetadataValues.setProperty("attributesdcid", StringUtil.repeat(";LV_Worksheet", addCount).substring(1));
                    this.getActionProcessor().processAction("AddSDIAttribute", "1", addMetadataValues);
                }
                if (editMetaData_id.length() > 0) {
                    PropertyList editMetadataValues = new PropertyList();
                    editMetadataValues.setProperty("sdcid", "LV_Worksheet");
                    editMetadataValues.setProperty("keyid1", worksheetid);
                    editMetadataValues.setProperty("keyid2", worksheetversionid);
                    editMetadataValues.setProperty("attributeid", editMetaData_id.substring(1));
                    editMetadataValues.setProperty("value", editMetaData_value.substring(1));
                    editMetadataValues.setProperty("attributesdcid", StringUtil.repeat(";LV_Worksheet", editCount).substring(1));
                    editMetadataValues.setProperty("attributeinstance", StringUtil.repeat(";1", editCount).substring(1));
                    this.getActionProcessor().processAction("EditSDIAttribute", "1", editMetadataValues);
                }
            }
            if (limsdata_sdcid.length() > 0 && limsdata_keyid1.length() > 0) {
                PropertyList wssdiProps = new PropertyList();
                wssdiProps.setProperty("worksheetid", worksheetid);
                wssdiProps.setProperty("worksheetversionid", worksheetversionid);
                wssdiProps.setProperty("sdcid", limsdata_sdcid);
                wssdiProps.setProperty("keyid1", limsdata_keyid1);
                wssdiProps.setProperty("keyid2", limsdata_keyid2);
                wssdiProps.setProperty("keyid3", limsdata_keyid3);
                this.getActionProcessor().processActionClass(AddWorksheetSDI.class.getName(), wssdiProps);
            }
            properties.setProperty("worksheetid", worksheetid);
            properties.setProperty("worksheetversionid", worksheetversionid);
        } else {
            Form form = null;
            try {
                this.logger.info("Creating a new worksheet");
                SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
                Document document = null;
                PropertyList datasourceParams = new PropertyList();
                PropertyList inputFieldValues = null;
                if (formid.length() > 0) {
                    form = Form.getInstance(sapphireConnection, formid, formversionid);
                    ArrayList<String> worksheetParams = form.getWorksheetParams();
                    if (worksheetParams != null) {
                        for (int i = 0; i < worksheetParams.size(); ++i) {
                            String paramid = worksheetParams.get(i);
                            if (!properties.containsKey(paramid)) {
                                throw new SapphireException("Missing worksheet creation parameter '" + paramid + "'");
                            }
                            datasourceParams.setProperty(paramid, properties.getProperty(paramid));
                        }
                    }
                    inputFieldValues = this.getInputFieldValues(form, properties);
                } else {
                    document = Document.getInstance(sapphireConnection, documentid, documentversionid, "", false, false);
                    form = document.getForm();
                    if (!(document.getDocumentStatus().equals("PD") || document.getDocumentStatus().equals("DR") || document.getDocumentStatus().equals("PA"))) {
                        throw new SapphireException("Worksheet must be in a Pending, Draft or Pending Approval status for regeneration!");
                    }
                    PropertyList setStatusProps = new PropertyList();
                    setStatusProps.setProperty("documentid", documentid);
                    setStatusProps.setProperty("documentversionid", documentversionid);
                    setStatusProps.setProperty("documentstatus", "CN");
                    setStatusProps.setProperty("statusmessage", properties.getProperty("auditreason"));
                    setStatusProps.setProperty("auditreason", properties.getProperty("auditreason"));
                    setStatusProps.setProperty("auditactivity", properties.getProperty("auditactivity", ""));
                    setStatusProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag", "N"));
                    setStatusProps.setProperty("auditdt", properties.getProperty("auditdt"));
                    setStatusProps.setProperty("newversionstatus", "X");
                    this.getActionProcessor().processAction("SetDocumentStatus", "1", setStatusProps);
                    datasourceParams = Document.getDocumentParams(document.getDocumentObjects());
                    properties.setProperty("overrideexistingbindings", "Y");
                    properties.setProperty("overlayinputvalues", properties.getProperty("copyfieldvalues", "Y"));
                    properties.setProperty("newdocumentid", documentid);
                    this.database.createPreparedResultSet("SELECT " + (this.database.isOracle() ? " nvl( max( to_number( documentversionid ) ) + 1, 1 )" : " isnull( max( cast( documentversionid AS Integer ) ) + 1, 1 )") + " \"max\" FROM document WHERE documentid = ?", new Object[]{documentid});
                    if (!this.database.getNext()) {
                        throw new SapphireException("Cannot generate new versionid for document '" + documentid + "'");
                    }
                    properties.setProperty("newdocumentversionid", this.database.getValue("max"));
                    PropertyListCollection fieldValues = document.getFieldValues();
                    inputFieldValues = new PropertyList();
                    for (int i = 0; i < fieldValues.size(); ++i) {
                        PropertyList fieldValue = fieldValues.getPropertyList(i);
                        inputFieldValues.setProperty(fieldValue.getProperty("fieldid"), fieldValue.getCollection("instances"));
                    }
                }
                if (!form.isWorksheet()) {
                    throw new SapphireException("Form '" + form.getFormid() + "' is not defined as a worksheet - update the form definition or use the AddDocument action!");
                }
                int worksheetqty = form.getWorksheetqty();
                StringBuffer returndocumentid = new StringBuffer();
                StringBuffer returndocumentversionid = new StringBuffer();
                PropertyList datasourceParamsSubList = new PropertyList();
                if (form.getWorksheettype().equals("sdi") || form.getWorksheettype().equals("dataset") || form.getWorksheettype().equals("workitem")) {
                    String[] keyid1 = StringUtil.split(datasourceParams.getProperty("keyid1"), ";");
                    String[] keyid2 = datasourceParams.getProperty("keyid2").length() > 0 ? StringUtil.split(datasourceParams.getProperty("keyid2"), ";") : null;
                    String[] keyid3 = datasourceParams.getProperty("keyid3").length() > 0 ? StringUtil.split(datasourceParams.getProperty("keyid3"), ";") : null;
                    String[] paramlistid = datasourceParams.getProperty("paramlistid").length() > 0 ? StringUtil.split(datasourceParams.getProperty("paramlistid"), ";") : null;
                    String[] paramlistversionid = datasourceParams.getProperty("paramlistversionid").length() > 0 ? StringUtil.split(datasourceParams.getProperty("paramlistversionid"), ";") : null;
                    String[] variantid = datasourceParams.getProperty("variantid").length() > 0 ? StringUtil.split(datasourceParams.getProperty("variantid"), ";") : null;
                    String[] dataset = datasourceParams.getProperty("dataset").length() > 0 ? StringUtil.split(datasourceParams.getProperty("dataset"), ";") : null;
                    String[] workitemid = datasourceParams.getProperty("workitemid").length() > 0 ? StringUtil.split(datasourceParams.getProperty("workitemid"), ";") : null;
                    String[] workiteminstance = datasourceParams.getProperty("workiteminstance").length() > 0 ? StringUtil.split(datasourceParams.getProperty("workiteminstance"), ";") : null;
                    for (int i = 0; i < keyid1.length; i += worksheetqty) {
                        int j;
                        StringBuffer keyid1SubList = new StringBuffer();
                        StringBuffer keyid2SubList = new StringBuffer();
                        StringBuffer keyid3SubList = new StringBuffer();
                        StringBuffer paramlistidSubList = new StringBuffer();
                        StringBuffer paramlistversionidSubList = new StringBuffer();
                        StringBuffer variantidSubList = new StringBuffer();
                        StringBuffer datasetSubList = new StringBuffer();
                        StringBuffer workitemidSubList = new StringBuffer();
                        StringBuffer workiteminstanceSubList = new StringBuffer();
                        for (j = 0; j < worksheetqty; ++j) {
                            if (i + j >= keyid1.length) continue;
                            keyid1SubList.append(";").append(keyid1[i + j]);
                            if (keyid2 != null) {
                                keyid2SubList.append(";").append(keyid2[i + j]);
                            }
                            if (keyid3 != null) {
                                keyid3SubList.append(";").append(keyid3[i + j]);
                            }
                            if (form.getWorksheettype().equals("dataset")) {
                                if (paramlistid != null) {
                                    paramlistidSubList.append(";").append(paramlistid[i + j < paramlistid.length ? i + j : paramlistid.length - 1]);
                                }
                                if (paramlistversionid != null) {
                                    paramlistversionidSubList.append(";").append(paramlistversionid[i + j < paramlistversionid.length ? i + j : paramlistversionid.length - 1]);
                                }
                                if (variantid != null) {
                                    variantidSubList.append(";").append(variantid[i + j < variantid.length ? i + j : variantid.length - 1]);
                                }
                                if (dataset == null) continue;
                                datasetSubList.append(";").append(dataset[i + j < dataset.length ? i + j : dataset.length - 1]);
                                continue;
                            }
                            if (!form.getWorksheettype().equals("workitem")) continue;
                            if (workitemid != null) {
                                workitemidSubList.append(";").append(workitemid[i + j < workitemid.length ? i + j : workitemid.length - 1]);
                            }
                            if (workiteminstance == null) continue;
                            workiteminstanceSubList.append(";").append(workiteminstance[i + j < workiteminstance.length ? i + j : workiteminstance.length - 1]);
                        }
                        datasourceParamsSubList.setProperty("sdcid", datasourceParams.getProperty("sdcid"));
                        datasourceParamsSubList.setProperty("keyid1", keyid1SubList.substring(1));
                        datasourceParamsSubList.setProperty("keyid2", keyid2SubList.length() > 0 ? keyid2SubList.substring(1) : "");
                        datasourceParamsSubList.setProperty("keyid3", keyid3SubList.length() > 0 ? keyid3SubList.substring(1) : "");
                        datasourceParamsSubList.setProperty("paramlistid", paramlistidSubList.length() > 0 ? paramlistidSubList.substring(1) : "");
                        datasourceParamsSubList.setProperty("paramlistversionid", paramlistversionidSubList.length() > 0 ? paramlistversionidSubList.substring(1) : "");
                        datasourceParamsSubList.setProperty("variantid", variantidSubList.length() > 0 ? variantidSubList.substring(1) : "");
                        datasourceParamsSubList.setProperty("dataset", datasetSubList.length() > 0 ? datasetSubList.substring(1) : "");
                        datasourceParamsSubList.setProperty("workitemid", workitemidSubList.length() > 0 ? workitemidSubList.substring(1) : "");
                        datasourceParamsSubList.setProperty("workiteminstance", workiteminstanceSubList.length() > 0 ? workiteminstanceSubList.substring(1) : "");
                        this.addDocument(sapphireConnection, form.getFormid(), form.getFormversionid(), properties, datasourceParamsSubList, inputFieldValues);
                        returndocumentid.append(";").append(properties.getProperty("documentid"));
                        returndocumentversionid.append(";").append(properties.getProperty("documentversionid"));
                        if (!form.getWorksheettype().equals("workitem")) continue;
                        for (j = 0; j < worksheetqty && i + j < keyid1.length; ++j) {
                            this.database.executePreparedUpdate("UPDATE sdiworkitem SET documentid = ?, documentversionid = ? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND workitemid = ? AND workiteminstance = ?", new Object[]{properties.getProperty("documentid"), properties.getProperty("documentversionid"), datasourceParams.getProperty("sdcid"), keyid1[i + j], keyid2 != null ? keyid2[i + j] : "(null)", keyid3 != null ? keyid3[i + j] : "(null)", workitemid[i + j < workitemid.length ? i + j : workitemid.length - 1], workiteminstance[i + j < workiteminstance.length ? i + j : workiteminstance.length - 1]});
                        }
                    }
                } else if (form.getWorksheettype().equals("qcbatch")) {
                    String[] qcbatchid;
                    String[] stringArray = qcbatchid = datasourceParams.getProperty("qcbatchid").length() > 0 ? StringUtil.split(datasourceParams.getProperty("qcbatchid"), ";") : null;
                    if (qcbatchid == null) {
                        throw new SapphireException("QCBatchId not defined correctly!");
                    }
                    for (int i = 0; i < qcbatchid.length; ++i) {
                        datasourceParamsSubList.setProperty("qcbatchid", qcbatchid[i]);
                        this.addDocument(sapphireConnection, form.getFormid(), form.getFormversionid(), properties, datasourceParamsSubList, inputFieldValues);
                        returndocumentid.append(";").append(properties.getProperty("documentid"));
                        returndocumentversionid.append(";").append(properties.getProperty("documentversionid"));
                        this.database.executePreparedUpdate("UPDATE s_qcbatch SET documentid = ?, documentversionid = ?, blockflag = 'Y' WHERE s_qcbatchid = ?", new Object[]{properties.getProperty("documentid"), properties.getProperty("documentversionid"), qcbatchid[i]});
                    }
                }
                properties.setProperty("documentid", returndocumentid.length() > 0 ? returndocumentid.substring(1) : "");
                properties.setProperty("documentversionid", returndocumentversionid.length() > 0 ? returndocumentversionid.substring(1) : "");
            }
            catch (Exception e) {
                throw new SapphireException("Error creating new document using form '" + (form != null ? form.getFormid() : formid) + "'. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
    }
}

