/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.actions.documents.CreateWorksheet;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.modules.documents.gwt.server.BaseDocumentCommand;
import com.labvantage.sapphire.modules.documents.gwt.server.DocumentCommand;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.util.HashMap;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Rebind
extends BaseDocumentCommand
implements DocumentCommand {
    public Rebind(SapphireConnection sapphireConnection, boolean debug) {
        super(sapphireConnection, debug);
    }

    @Override
    public HashMap execute(PropertyList requestData) {
        String rebindoperation = requestData.getProperty("rebindoperation");
        PropertyList document = requestData.getPropertyList("document");
        String formid = document.getProperty("formid");
        String formversionid = document.getProperty("formversionid");
        Form form = null;
        try {
            form = Form.getInstance(this.sapphireConnection, formid, formversionid, this.debug);
            form.setOverrides(requestData.getPropertyList("formoverrides"));
            PropertyList rebind = new PropertyList();
            if (rebindoperation.equals("regen")) {
                PropertyList properties = new PropertyList();
                properties.setProperty("documentid", document.getProperty("documentid"));
                properties.setProperty("documentversionid", document.getProperty("documentversionid"));
                properties.setProperty("auditreason", requestData.getProperty("auditreason"));
                properties.setProperty("auditactivity", requestData.getProperty("auditactivity"));
                properties.setProperty("auditsigned", requestData.getProperty("auditsigned"));
                this.actionProcessor.processActionClass(CreateWorksheet.class.getName(), properties);
                rebind.setProperty("documentid", properties.getProperty("documentid"));
                rebind.setProperty("documentversionid", properties.getProperty("documentversionid"));
            } else if (rebindoperation.equals("replicate") || rebindoperation.equals("dataset")) {
                PropertyList fieldbinding = requestData.getPropertyList("fieldbinding");
                HashMap<String, String> actionProps = new HashMap<String, String>();
                if (rebindoperation.equals("replicate")) {
                    actionProps.put("sdcid", fieldbinding.getProperty("sdcid"));
                    actionProps.put("keyid1", fieldbinding.getProperty("keyid1"));
                    actionProps.put("keyid2", fieldbinding.getProperty("keyid2"));
                    actionProps.put("keyid3", fieldbinding.getProperty("keyid3"));
                    actionProps.put("paramlistid", fieldbinding.getProperty("paramlistid"));
                    actionProps.put("paramlistversionid", fieldbinding.getProperty("paramlistversionid"));
                    actionProps.put("variantid", fieldbinding.getProperty("variantid"));
                    actionProps.put("dataset", fieldbinding.getProperty("dataset"));
                    actionProps.put("paramid", fieldbinding.getProperty("paramid"));
                    actionProps.put("paramtype", fieldbinding.getProperty("paramtype"));
                    actionProps.put("numreplicate", requestData.getProperty("newinstances", "1"));
                    this.actionProcessor.processAction("AddReplicate", "1", actionProps);
                } else if (rebindoperation.equals("dataset")) {
                    actionProps.put("sdcid", fieldbinding.getProperty("sdcid"));
                    actionProps.put("keyid1", fieldbinding.getProperty("keyid1"));
                    actionProps.put("keyid2", fieldbinding.getProperty("keyid2"));
                    actionProps.put("keyid3", fieldbinding.getProperty("keyid3"));
                    actionProps.put("paramlistid", fieldbinding.getProperty("paramlistid"));
                    actionProps.put("paramlistversionid", fieldbinding.getProperty("paramlistversionid"));
                    actionProps.put("variantid", fieldbinding.getProperty("variantid"));
                    this.actionProcessor.processAction("AddDataSet", "1", actionProps);
                }
                HashMap bindings = ProcessingUtil.createBindingsMap(this.sapphireConnection, "REBIND");
                PropertyList params = Document.getDocumentParams(document.getPropertyList("documentobjects"));
                if (params != null) {
                    bindings.put("params", params);
                }
                bindings.put("fields", new HashMap());
                PropertyListCollection datasources = requestData.getCollection("datasources");
                for (int i = 0; i < datasources.size(); ++i) {
                    PropertyList datasource = datasources.getPropertyList(i);
                    datasource.setProperty("sections", this.loadDatasourceFieldValues(datasource, "0", form, bindings, false, true));
                }
                rebind.setProperty("datasources", datasources);
            }
            HashMap<String, String> responseData = new HashMap<String, String>();
            responseData.put("jsonreturn", rebind.toJSONString(false));
            this.debugReturn(requestData, rebind);
            return responseData;
        }
        catch (Exception e) {
            this.logger.error("Failed to get form instance '" + formid + "(v" + formversionid + ")' - rebind. Exception: " + e.getMessage(), e);
            return null;
        }
    }
}

