/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents.gwt.server;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.Field;
import com.labvantage.sapphire.modules.documents.FieldSetter;
import com.labvantage.sapphire.modules.documents.Form;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentCodes;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.util.groovy.GroovyLogger;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.groovy.ProcessingUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseDocumentCommand
implements DocumentConstants {
    public static final String LOGNAME = "DOCUMENTREQUEST";
    public static final String JSON_RETURN = "jsonreturn";
    protected SapphireConnection sapphireConnection;
    protected Logger logger;
    protected ActionProcessor actionProcessor;
    protected TranslationProcessor trans;
    protected DDTService ddtService;
    protected boolean debug = false;

    public BaseDocumentCommand(SapphireConnection sapphireConnection, boolean debug) {
        this.sapphireConnection = sapphireConnection;
        this.logger = new Logger(sapphireConnection.getConnectionId());
        this.logger.setLoggerName(LOGNAME);
        this.actionProcessor = new ActionProcessor(sapphireConnection.getConnectionId());
        this.trans = new TranslationProcessor(sapphireConnection.getConnectionId());
        this.ddtService = new DDTService(sapphireConnection);
        this.debug = debug;
        if (!DocumentCodes.translationsLoaded) {
            if (this.trans.getLanguage() != null && this.trans.getLanguage().length() > 0) {
                this.trans.translateTable(this.trans.getLanguage(), DocumentCodes.transMap);
            }
            DocumentCodes.translationsLoaded = true;
        }
    }

    protected DataSet loadSDIData(String sdcid, String queryfrom, String querywhere, String queryorderby, String request, String versionstatus, boolean extendeddatatypes) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setRequestItem(request);
        sdiRequest.setQueryFrom(queryfrom);
        sdiRequest.setQueryWhere(querywhere);
        sdiRequest.setQueryOrderBy(queryorderby);
        sdiRequest.setExtendedDataTypes(extendeddatatypes);
        sdiRequest.setVersionStatus(versionstatus);
        SDIData sdiData = new SDIProcessor(this.sapphireConnection.getConnectionId()).getSDIData(sdiRequest);
        return sdiData != null ? sdiData.getDataset("primary") : new DataSet();
    }

    protected DataSet loadSDIData(String sdcid, String queryid, String[] queryparams, String extendedwhere, String request, String versionstatus, boolean extendeddatatypes) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setRequestItem(request);
        sdiRequest.setQueryid(queryid);
        sdiRequest.setQueryParams(queryparams);
        sdiRequest.setQueryWhere(extendedwhere);
        sdiRequest.setExtendedDataTypes(extendeddatatypes);
        sdiRequest.setVersionStatus(versionstatus);
        SDIData sdiData = new SDIProcessor(this.sapphireConnection.getConnectionId()).getSDIData(sdiRequest);
        return sdiData != null ? sdiData.getDataset("primary") : new DataSet();
    }

    protected PropertyList loadDatasourceFieldValues(PropertyList datasource, String fieldinstance, Form form, HashMap bindings, boolean rejectBindErrors, boolean overrideExistingBindings) throws SapphireException {
        SDIData sdiData = null;
        DataSet data = null;
        DataSet repeatdata = null;
        String[] primaryKeys = null;
        String type = datasource.getProperty("type");
        if (type.equals("sdi") || type.equals("workitem") || type.equals("dataset") || type.equals("qcbatch")) {
            sdiData = this.loadSDIDatasource(datasource, bindings);
            primaryKeys = sdiData != null ? sdiData.getKeys("primary") : null;
        } else if (type.equals("sql")) {
            data = this.loadSQLDatasource(datasource, bindings);
        }
        PropertyListCollection datasourceFields = datasource.getCollection("fields");
        PropertyList datasourceSections = new PropertyList();
        if (datasourceFields != null) {
            PropertyListCollection fieldValues;
            PropertyList field;
            int i;
            HashMap fieldMap = (HashMap)bindings.get("fields");
            HashMap<String, Object> parentDataBinding = new HashMap<String, Object>();
            PropertyListCollection nobindFields = new PropertyListCollection();
            for (i = 0; i < datasourceFields.size(); ++i) {
                field = datasourceFields.getPropertyList(i);
                if (field.getProperty("nobind", "N").equals("N")) {
                    PropertyList binding = field.getPropertyList("binding");
                    String fieldid = field.getProperty("fieldid");
                    String autosave = field.getProperty("autosave");
                    String requestitem = autosave.equals("dataentry") ? "dataitem" : (autosave.equals("datareagent") ? "reagentrelation" : (autosave.length() > 0 ? autosave : (binding != null && binding.containsKey("requestitem") ? binding.getProperty("requestitem") : "dataitem")));
                    String sectionid = field.getProperty("sectionid", "(default)");
                    boolean uniqueBinding = autosave.equals("dataentry") || requestitem.equals("dataspec");
                    PropertyList datasourceSection = datasourceSections.getPropertyList(sectionid);
                    if (datasourceSection == null) {
                        datasourceSection = new PropertyList();
                        datasourceSection.setProperty("sectionid", sectionid);
                        datasourceSection.setProperty("fieldvalues", new PropertyListCollection());
                        PropertyList section = form.getSection(sectionid);
                        datasourceSection.setProperty("binding", section != null && section.containsKey("binding") ? section.getPropertyList("binding") : new PropertyList());
                        datasourceSection.setProperty("datasourcerepeater", section != null ? section.getProperty("datasourcerepeater") : requestitem);
                        datasourceSection.setProperty("datasourcefilter", section != null ? section.getCollection("datasourcefilter") : null);
                        datasourceSection.setProperty("excludebindingvalues", section != null ? section.getProperty("excludebindingvalues") : "N");
                        datasourceSections.setProperty(sectionid, datasourceSection);
                    }
                    fieldValues = datasourceSection.getCollection("fieldvalues");
                    String datasourceRepeater = datasourceSection.getProperty("datasourcerepeater", requestitem);
                    boolean excludeBindingValues = datasourceSection.getProperty("excludebindingvalues", "N").equals("Y");
                    PropertyList fieldValue = new PropertyList();
                    fieldValue.setProperty("fieldid", fieldid);
                    fieldValue.setProperty("instances", new PropertyListCollection());
                    fieldValues.add(fieldValue);
                    if (type.equals("sdi") || type.equals("dataset") || type.equals("workitem") || type.equals("qcbatch")) {
                        if (sdiData != null) {
                            parentDataBinding.clear();
                            parentDataBinding.putAll(datasourceSection.getPropertyList("binding"));
                            if (parentDataBinding.containsKey("dataset")) {
                                parentDataBinding.put("dataset", new BigDecimal((String)parentDataBinding.get("dataset")));
                            }
                            if (parentDataBinding.containsKey("replicateid")) {
                                parentDataBinding.put("replicateid", new BigDecimal((String)parentDataBinding.get("replicateid")));
                            }
                            if (field.getProperty("repeatable", "N").equals("Y") && !datasourceRepeater.equals("manual")) {
                                repeatdata = sdiData.getDataset(datasourceRepeater);
                                data = sdiData.getDataset(requestitem);
                                if (repeatdata != null && data != null) {
                                    int newinstances = 0;
                                    boolean repeaterField = true;
                                    for (int j = 0; j < repeatdata.size(); ++j) {
                                        if (datasourceRepeater.equals(requestitem)) {
                                            String existingdocumentid;
                                            repeaterField = true;
                                            HashMap bindingMap = this.createBindingMap(field.getProperty("fieldid"), binding, parentDataBinding, bindings);
                                            boolean exclude = false;
                                            Iterator it = bindingMap.keySet().iterator();
                                            while (!exclude && it.hasNext()) {
                                                int k;
                                                boolean match;
                                                String bindingid = (String)it.next();
                                                if (!(bindingMap.get(bindingid) instanceof String) || ((String)bindingMap.get(bindingid)).length() <= 0) continue;
                                                String[] values = StringUtil.split((String)bindingMap.get(bindingid), ";");
                                                if (excludeBindingValues) {
                                                    match = true;
                                                    for (k = 0; match && k < values.length; ++k) {
                                                        if (values[k].length() <= 0) continue;
                                                        match = !data.getValue(j, bindingid).equals(values[k]);
                                                    }
                                                    exclude = !match;
                                                    continue;
                                                }
                                                match = false;
                                                for (k = 0; !match && k < values.length; ++k) {
                                                    if (values[k].length() <= 0 || !data.getValue(j, bindingid).equals(values[k])) continue;
                                                    match = true;
                                                }
                                                exclude = !match;
                                            }
                                            if (exclude) continue;
                                            String string = existingdocumentid = overrideExistingBindings ? "" : this.getExistingDocument(field, sdiData, data, j);
                                            if (existingdocumentid.length() == 0) {
                                                this.createNewBoundInstance(field, fieldValue, String.valueOf(newinstances), binding, bindings, sdiData, data, j);
                                                ++newinstances;
                                                continue;
                                            }
                                            if (rejectBindErrors) {
                                                throw new SapphireException("One or more fields are already bound to another worksheet (" + existingdocumentid + ")!");
                                            }
                                            this.createUnboundInstance(field, fieldValue, String.valueOf(newinstances), "Already bound to worksheet '" + existingdocumentid + "'");
                                            ++newinstances;
                                            continue;
                                        }
                                        repeaterField = false;
                                        if (fieldMap.containsKey(sectionid + "_" + j)) {
                                            bindings.put("fieldinstance", fieldMap.get(sectionid + "_" + j));
                                        }
                                        if (datasourceRepeater.equals("primary") && (requestitem.equals("dataitem") || requestitem.equals("datalimit") || requestitem.equals("dataspec") || requestitem.equals("dataset"))) {
                                            parentDataBinding.put("keyid1", repeatdata.getValue(j, primaryKeys[0]));
                                        } else if (datasourceRepeater.equals("qcbatchitem") && (requestitem.equals("dataitem") || requestitem.equals("datalimit") || requestitem.equals("dataspec") || requestitem.equals("dataset"))) {
                                            parentDataBinding.put("__sdidata_s_qcbatchitemid", repeatdata.getValue(j, "s_qcbatchitemid"));
                                        } else if (datasourceRepeater.equals("qcbatchitem") && requestitem.equals("primary")) {
                                            parentDataBinding.put(primaryKeys[0], repeatdata.getValue(j, primaryKeys[0]));
                                        } else if (datasourceRepeater.equals("reagents") && requestitem.equals("dataitem")) {
                                            parentDataBinding.put("keyid1", repeatdata.getValue(j, "keyid1"));
                                        } else if (datasourceRepeater.equals("dataset")) {
                                            if (requestitem.equals("primary")) {
                                                parentDataBinding.put(primaryKeys[0], repeatdata.getValue(j, "keyid1"));
                                            } else if (requestitem.equals("dataitem") || requestitem.equals("datalimit") || requestitem.equals("dataspec")) {
                                                parentDataBinding.put("keyid1", repeatdata.getValue(j, "keyid1"));
                                                parentDataBinding.put("keyid2", repeatdata.getValue(j, "keyid2"));
                                                parentDataBinding.put("keyid3", repeatdata.getValue(j, "keyid3"));
                                            }
                                        } else if (datasourceRepeater.equals("dataitem")) {
                                            if (requestitem.equals("primary")) {
                                                parentDataBinding.put(primaryKeys[0], repeatdata.getValue(j, "keyid1"));
                                            } else if (requestitem.equals("qcbatchitem")) {
                                                parentDataBinding.put(primaryKeys[0], repeatdata.getValue(j, "keyid1"));
                                                parentDataBinding.put("__sdidata_s_qcbatchitemid", repeatdata.getValue(j, "s_qcbatchitemid"));
                                            } else if (requestitem.equals("dataset")) {
                                                parentDataBinding.put("keyid1", repeatdata.getValue(j, "keyid1"));
                                                parentDataBinding.put("keyid2", repeatdata.getValue(j, "keyid2"));
                                                parentDataBinding.put("keyid3", repeatdata.getValue(j, "keyid3"));
                                                parentDataBinding.put("paramlistid", repeatdata.getValue(j, "paramlistid"));
                                                parentDataBinding.put("paramlistversionid", repeatdata.getValue(j, "paramlistversionid"));
                                                parentDataBinding.put("variantid", repeatdata.getValue(j, "variantid"));
                                            } else if (requestitem.equals("datalimit") || requestitem.equals("dataspec")) {
                                                parentDataBinding.put("keyid1", repeatdata.getValue(j, "keyid1"));
                                                parentDataBinding.put("keyid2", repeatdata.getValue(j, "keyid2"));
                                                parentDataBinding.put("keyid3", repeatdata.getValue(j, "keyid3"));
                                                parentDataBinding.put("paramlistid", repeatdata.getValue(j, "paramlistid"));
                                                parentDataBinding.put("paramlistversionid", repeatdata.getValue(j, "paramlistversionid"));
                                                parentDataBinding.put("variantid", repeatdata.getValue(j, "variantid"));
                                                parentDataBinding.put("paramid", repeatdata.getValue(j, "paramid"));
                                                parentDataBinding.put("paramtype", repeatdata.getValue(j, "paramtype"));
                                                parentDataBinding.put("replicateid", repeatdata.getBigDecimal(j, "replicateid"));
                                            }
                                        } else if (datasourceRepeater.equals("dataitemparam") && (requestitem.equals("dataitem") || requestitem.equals("datalimit") || requestitem.equals("dataspec"))) {
                                            parentDataBinding.put("paramid", repeatdata.getValue(j, "paramid"));
                                            repeaterField = true;
                                        } else if (datasourceRepeater.equals("dataitemrep") && (requestitem.equals("dataitem") || requestitem.equals("datalimit") || requestitem.equals("dataspec"))) {
                                            parentDataBinding.put("replicateid", repeatdata.getBigDecimal(j, "replicateid"));
                                            repeaterField = true;
                                        }
                                        this.findNewInstance(field, fieldValue, String.valueOf(newinstances), binding, parentDataBinding, bindings, sdiData, data, datasourceSection, uniqueBinding, rejectBindErrors, overrideExistingBindings);
                                        ++newinstances;
                                    }
                                    ProcessingUtil.addSectionInstances(fieldMap, fieldid);
                                    if (repeaterField) {
                                        datasourceSection.setProperty("newinstances", String.valueOf(Math.max(Integer.parseInt(datasourceSection.getProperty("newinstances", "0")), newinstances)));
                                    }
                                }
                            } else {
                                data = sdiData.getDataset(requestitem);
                                if (data != null) {
                                    if (requestitem.equals("primary") && data.size() == 1) {
                                        parentDataBinding.put(primaryKeys[0], data.getValue(0, primaryKeys[0]));
                                    }
                                    this.findNewInstance(field, fieldValue, fieldinstance, binding, parentDataBinding, bindings, sdiData, data, datasourceSection, uniqueBinding, rejectBindErrors, overrideExistingBindings);
                                }
                            }
                        } else {
                            fieldValue.setProperty("fieldinstance", fieldinstance);
                        }
                    } else if (type.equals("sql") && data != null) {
                        PropertyListCollection instances = fieldValue.getCollection("instances");
                        boolean resetSection = datasourceSection.getProperty("reset", "Y").equals("Y");
                        int nextinstance = resetSection ? 0 : Integer.parseInt(datasourceSection.getProperty("nextinstance", "0"));
                        for (int j = 0; j < data.size(); ++j) {
                            PropertyList instance = new PropertyList();
                            instance.setProperty("fieldid", fieldid);
                            instance.setProperty("fieldinstance", String.valueOf(nextinstance + j));
                            instance.setProperty("enteredtext", data.getValue(j, fieldid));
                            instance.setProperty("displayvalue", instance.getProperty("enteredtext"));
                            instances.add(instance);
                        }
                        datasourceSection.setProperty("newinstances", String.valueOf(data.size()));
                    }
                    if (!field.getProperty("multisamplecalcs").equals("Y")) continue;
                    field.remove("multisamplecalcs");
                    datasource.setProperty("multisamplecalcs", "Y");
                    continue;
                }
                nobindFields.add(field);
            }
            for (i = 0; i < nobindFields.size(); ++i) {
                field = nobindFields.getPropertyList(i);
                String fieldid = field.getProperty("fieldid");
                PropertyList datasourceSection = datasourceSections.getPropertyList(field.getProperty("sectionid"));
                fieldValues = datasourceSection.getCollection("fieldvalues");
                int newinstances = Integer.parseInt(datasourceSection.getProperty("newinstances", "1"));
                PropertyList fieldValue = new PropertyList();
                fieldValue.setProperty("fieldid", fieldid);
                PropertyListCollection instances = new PropertyListCollection();
                fieldValue.setProperty("instances", instances);
                fieldValues.add(fieldValue);
                boolean resetSection = datasourceSection.getProperty("reset", "Y").equals("Y");
                int nextinstance = resetSection ? 0 : Integer.parseInt(datasourceSection.getProperty("nextinstance", "0"));
                for (int j = 0; j < newinstances; ++j) {
                    PropertyList instance = new PropertyList();
                    instance.setProperty("fieldid", fieldid);
                    instance.setProperty("fieldinstance", String.valueOf(nextinstance + j));
                    instance.setProperty("enteredtext", "");
                    instance.setProperty("displayvalue", "");
                    instances.add(instance);
                }
            }
        }
        return datasourceSections;
    }

    private DataSet loadSQLDatasource(PropertyList datasource, HashMap bindings) throws SapphireException {
        String datasourceid = datasource.getProperty("datasourceid");
        PropertyList sql = datasource.getPropertyList("sql");
        String select = this.evalGroovyExpression(datasourceid, bindings, sql.getProperty("select"), false);
        QueryProcessor queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
        return queryProcessor.getSqlDataSet(select);
    }

    private SDIData loadSDIDatasource(PropertyList datasource, HashMap bindings) throws SapphireException {
        DataSet data;
        String datasourceid = datasource.getProperty("datasourceid");
        PropertyList typeProps = datasource.getPropertyList(datasource.getProperty("type"));
        String requestitems = datasource.getProperty("requestitems");
        PropertyList params = new PropertyList();
        boolean qcbatch = typeProps.getProperty("qcbatchid").length() > 0;
        QueryProcessor queryProcessor = new QueryProcessor(this.sapphireConnection.getConnectionId());
        SDIProcessor sdiProcessor = new SDIProcessor(this.sapphireConnection.getConnectionId());
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("dataset" + (datasource.containsKey("dataset_cols") ? "[" + datasource.getProperty("dataset_cols").substring(1) + "]" : ""));
        if (requestitems.indexOf("primary") > -1 || qcbatch) {
            sdiRequest.setRequestItem("primary" + (datasource.containsKey("primary_cols") ? "[" + datasource.getProperty("primary_cols").substring(1) + "]" : ""));
        }
        if (requestitems.indexOf("dataitem") > -1 || qcbatch) {
            sdiRequest.setRequestItem("dataitem" + (datasource.containsKey("dataitem_cols") ? "[" + datasource.getProperty("dataitem_cols").substring(1) + "]" : ""));
        }
        if (requestitems.indexOf("datalimit") > -1) {
            sdiRequest.setRequestItem("datalimit" + (datasource.containsKey("datalimit_cols") ? "[" + datasource.getProperty("datalimit_cols").substring(1) + "]" : ""));
        }
        if (requestitems.indexOf("dataspec") > -1) {
            sdiRequest.setRequestItem("dataspec" + (datasource.containsKey("dataspec_cols") ? "[" + datasource.getProperty("dataspec_cols").substring(1) + "]" : ""));
        }
        if (requestitems.indexOf("reagentrelation") > -1) {
            sdiRequest.setRequestItem("reagentrelation" + (datasource.containsKey("reagentrelation_cols") ? "[" + datasource.getProperty("reagentrelation_cols").substring(1) + "]" : ""));
        }
        boolean getData = true;
        if (qcbatch) {
            String qcbatchid = this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("qcbatchid"), false);
            data = queryProcessor.getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3, paramlistid, paramlistversionid, variantid, dataset FROM sdidata WHERE s_qcbatchid = ?", new Object[]{qcbatchid});
            sdiRequest.setSDCid(data.getValue(0, "sdcid"));
            sdiRequest.setKeyid1List(data.getColumnValues("keyid1", ";"));
            sdiRequest.setKeyid2List(data.getColumnValues("keyid2", ";"));
            sdiRequest.setKeyid3List(data.getColumnValues("keyid3", ";"));
            sdiRequest.setParamlistidList(data.getColumnValues("paramlistid", ";"));
            sdiRequest.setParamlistversionidList(data.getColumnValues("paramlistversionid", ";"));
            sdiRequest.setVariantidList(data.getColumnValues("variantid", ";"));
            sdiRequest.setDatasetList(data.getColumnValues("dataset", ";"));
            params.setProperty("qcbatchid", qcbatchid);
        } else if (typeProps.getProperty("queryid").length() <= 0) {
            if (typeProps.getProperty("queryfrom").length() > 0) {
                sdiRequest.setSDCid(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("sdcid"), false));
                sdiRequest.setQueryFrom(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("queryfrom"), false));
                sdiRequest.setQueryWhere(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("querywhere"), false));
                sdiRequest.setQueryOrderBy(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("queryorderby"), false));
                if (sdiRequest.getQueryFrom().length() == 0) {
                    getData = false;
                }
                params.setProperty("sdcid", sdiRequest.getSDCid());
                params.setProperty("queryfrom", sdiRequest.getQueryFrom());
                params.setProperty("querywhere", sdiRequest.getQueryWhere());
                params.setProperty("queryorderby", sdiRequest.getQueryOrderBy());
            } else {
                sdiRequest.setSDCid(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("sdcid"), false));
                sdiRequest.setKeyid1List(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("keyid1"), false));
                sdiRequest.setKeyid2List(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("keyid2"), false));
                sdiRequest.setKeyid3List(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("keyid3"), false));
                if (sdiRequest.getKeyid1List().length() == 0) {
                    getData = false;
                }
                params.setProperty("sdcid", sdiRequest.getSDCid());
                params.setProperty("keyid1", sdiRequest.getKeyid1List());
                params.setProperty("keyid2", sdiRequest.getKeyid2List());
                params.setProperty("keyid3", sdiRequest.getKeyid3List());
            }
        }
        if (!qcbatch) {
            if (typeProps.getProperty("workitemid").length() > 0) {
                if (typeProps.getProperty("workiteminstance").length() == 0) {
                    throw new SapphireException("Workitem instance not specified with workitem '" + typeProps.getProperty("workitemid") + "' when loading datasource '" + datasource.getProperty("datasourceid") + "'!");
                }
                sdiRequest.setWorkitemidList(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("workitemid"), false));
                sdiRequest.setWorkiteminstanceList(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("workiteminstance"), false));
                params.setProperty("workitemid", sdiRequest.getWorkitemidList());
                params.setProperty("workiteminstance", sdiRequest.getWorkiteminstanceList());
            } else if (typeProps.getProperty("paramlistid").length() > 0) {
                sdiRequest.setParamlistidList(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("paramlistid"), false));
                sdiRequest.setParamlistversionidList(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("paramlistversionid"), false));
                sdiRequest.setVariantidList(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("variantid"), false));
                sdiRequest.setDatasetList(this.evalGroovyExpression(datasourceid, bindings, typeProps.getProperty("dataset"), false));
                params.setProperty("paramlistid", sdiRequest.getParamlistidList());
                params.setProperty("paramlistversionid", sdiRequest.getParamlistversionidList());
                params.setProperty("variantid", sdiRequest.getVariantidList());
                params.setProperty("dataset", sdiRequest.getDatasetList());
            }
        }
        datasource.setProperty("params", params);
        SDIData sdiData = getData ? sdiProcessor.getSDIData(sdiRequest) : null;
        data = null;
        if (sdiData != null) {
            data = sdiData.getDataset("primary");
            if (data != null) {
                data.sort("keyid1");
            }
            if ((data = sdiData.getDataset("dataset")) != null) {
                data.sort("keyid1, __sdidata_usersequence, paramlistid, paramlistversionid, variantid, dataset");
            }
            if ((data = sdiData.getDataset("dataitem")) != null) {
                int i;
                data.sort("keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicateid");
                DataSet dataitemparams = new DataSet();
                String lastparam = "";
                for (i = 0; i < data.size(); ++i) {
                    String currentparam = data.getValue(i, "keyid1") + ";" + data.getValue(i, "paramlistid") + ";" + data.getValue(i, "paramlistversion") + ";" + data.getValue(i, "variantid") + ";" + data.getValue(i, "dataset") + ";" + data.getValue(i, "paramid");
                    if (lastparam.equals(currentparam)) continue;
                    dataitemparams.copyRow(data, i, 1);
                    lastparam = currentparam;
                }
                sdiData.setDataset("dataitemparam", dataitemparams);
                data.sort("keyid1, paramlistid, paramlistversionid, variantid, dataset, replicateid, paramid, paramtype");
                DataSet dataitemreps = new DataSet();
                String lastrep = "";
                for (i = 0; i < data.size(); ++i) {
                    String currentrep = data.getValue(i, "keyid1") + ";" + data.getValue(i, "paramlistid") + ";" + data.getValue(i, "paramlistversion") + ";" + data.getValue(i, "variantid") + ";" + data.getValue(i, "dataset") + ";" + data.getValue(i, "replicateid");
                    if (lastrep.equals(currentrep)) continue;
                    dataitemreps.copyRow(data, i, 1);
                    lastrep = currentrep;
                }
                sdiData.setDataset("dataitemrep", dataitemreps);
                data.sort("keyid1, __sdidata_usersequence, paramlistid, paramlistversionid, variantid, dataset, usersequence, paramid, paramtype, replicateid");
            }
            if ((data = sdiData.getDataset("datarelation")) != null) {
                data.sort("keyid1, __sdidata_usersequence, paramlistid, paramlistversionid, variantid, dataset");
            }
            if (qcbatch) {
                SDIRequest qcBatchRequest = new SDIRequest();
                qcBatchRequest.setSDIList("QCBatch", params.getProperty("qcbatchid"), "", "");
                qcBatchRequest.setRequestItem("primary");
                SDIData qcBatchData = sdiProcessor.getSDIData(qcBatchRequest);
                if (qcBatchData != null && qcBatchData.getDataset("primary") != null) {
                    String qcbatchsdcid = qcBatchData.getDataset("primary").getValue(0, "qcbatchsdcid");
                    SDCProcessor sdcProcessor = new SDCProcessor(this.sapphireConnection.getConnectionId());
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(qcbatchsdcid, "keycolumns"));
                    DataSet qcBatchItems = queryProcessor.getPreparedSqlDataSet("SELECT DISTINCT s_qcbatchitem.*, " + sdcProcessor.getProperty(qcbatchsdcid, "tableid") + "." + sdcProcessor.getProperty(qcbatchsdcid, "keycolid1") + " " + (keycols > 1 ? ", " + sdcProcessor.getProperty(qcbatchsdcid, "tableid") + "." + sdcProcessor.getProperty(qcbatchsdcid, "keycolid2") : "") + (keycols > 2 ? ", " + sdcProcessor.getProperty(qcbatchsdcid, "tableid") + "." + sdcProcessor.getProperty(qcbatchsdcid, "keycolid3") : "") + "FROM s_qcbatchitem, " + sdcProcessor.getProperty(qcbatchsdcid, "tableid") + ", sdidata WHERE s_qcbatchitem.s_qcbatchitemid = sdidata.s_qcbatchitemid AND " + sdcProcessor.getProperty(qcbatchsdcid, "tableid") + "." + sdcProcessor.getProperty(qcbatchsdcid, "keycolid1") + " = sdidata.keyid1 " + (keycols > 1 ? " AND " + sdcProcessor.getProperty(qcbatchsdcid, "tableid") + "." + sdcProcessor.getProperty(qcbatchsdcid, "keycolid2") + " = sdidata.keyid2 " : "") + (keycols > 2 ? " AND " + sdcProcessor.getProperty(qcbatchsdcid, "tableid") + "." + sdcProcessor.getProperty(qcbatchsdcid, "keycolid3") + " = sdidata.keyid3 " : "") + "AND sdidata.sdcid = ? AND s_qcbatchitem.s_qcbatchid = ? ORDER BY s_qcbatchitem.usersequence", new Object[]{qcbatchsdcid, params.getProperty("qcbatchid")});
                    sdiData.setDataset("qcbatchitem", qcBatchItems);
                }
            }
        } else if (getData) {
            throw new SapphireException("Failed to load data for datasource '" + datasourceid + "' - check your datasource definition for this form and/or the sapphire log file.");
        }
        return sdiData;
    }

    private void findNewInstance(PropertyList field, PropertyList fieldValue, String fieldinstance, PropertyList binding, HashMap parentDataBinding, HashMap bindings, SDIData sdiData, DataSet data, PropertyList datasourceSection, boolean uniqueBinding, boolean rejectBindErrors, boolean overrideExistingBindings) throws SapphireException {
        HashMap bindingMap = this.createBindingMap(field.getProperty("fieldid"), binding, parentDataBinding, bindings);
        boolean multiBind = false;
        boolean maxdataset = false;
        boolean maxreplicate = false;
        boolean firstspec = false;
        boolean lastspec = false;
        for (String bindingid : bindingMap.keySet()) {
            if (!(bindingMap.get(bindingid) instanceof String)) continue;
            String value = (String)bindingMap.get(bindingid);
            if (value.indexOf(";") > -1) {
                multiBind = true;
                continue;
            }
            if (value.equals("[maxdataset]")) {
                maxdataset = true;
                continue;
            }
            if (value.equals("[maxreplicate]")) {
                maxreplicate = true;
                continue;
            }
            if (value.equals("[firstspec]")) {
                firstspec = true;
                continue;
            }
            if (!value.equals("[lastspec]")) continue;
            lastspec = true;
        }
        if (maxdataset) {
            bindingMap.remove("dataset");
        }
        if (maxreplicate) {
            bindingMap.remove("replicateid");
        }
        if (firstspec || lastspec) {
            bindingMap.remove("specid");
        }
        if (!multiBind) {
            int dataRow;
            int n = dataRow = bindingMap.size() > 0 ? data.findRow(bindingMap) : 0;
            if (dataRow > -1) {
                DataSet filtered;
                int dataRowNext;
                int n2 = bindingMap.size() > 0 ? data.findRow(bindingMap, dataRow + 1) : (dataRowNext = data.size() == 1 ? -1 : 0);
                if (dataRowNext == -1) {
                    String existingdocumentid;
                    String string = existingdocumentid = overrideExistingBindings ? "" : this.getExistingDocument(field, sdiData, data, dataRow);
                    if (existingdocumentid.length() == 0) {
                        this.createNewBoundInstance(field, fieldValue, fieldinstance, binding, bindings, sdiData, data, dataRow);
                    } else {
                        if (rejectBindErrors) {
                            throw new SapphireException("One or more fields are already bound to another worksheet (" + existingdocumentid + ")!");
                        }
                        this.createUnboundInstance(field, fieldValue, fieldinstance, "Already bound to worksheet '" + existingdocumentid + "'");
                    }
                } else if (maxdataset || maxreplicate || lastspec || firstspec) {
                    String existingdocumentid;
                    filtered = data.getFilteredDataSet(bindingMap);
                    String string = overrideExistingBindings ? "" : (existingdocumentid = this.getExistingDocument(field, sdiData, filtered, firstspec ? 0 : filtered.size() - 1));
                    if (existingdocumentid.length() == 0) {
                        this.createNewBoundInstance(field, fieldValue, fieldinstance, binding, bindings, sdiData, filtered, firstspec ? 0 : filtered.size() - 1);
                    } else {
                        if (rejectBindErrors) {
                            throw new SapphireException("One or more fields are already bound to another worksheet (" + existingdocumentid + ")!");
                        }
                        this.createUnboundInstance(field, fieldValue, fieldinstance, "Already bound to worksheet '" + existingdocumentid + "'");
                    }
                } else if (!uniqueBinding) {
                    filtered = data.getFilteredDataSet(bindingMap);
                    String existingdocumentid = "";
                    for (int i = 0; i < filtered.size(); ++i) {
                        String string = existingdocumentid = overrideExistingBindings ? "" : this.getExistingDocument(field, sdiData, filtered, i);
                        if (existingdocumentid.length() > 0) break;
                    }
                    if (existingdocumentid.length() == 0) {
                        this.createNewBoundInstance(field, fieldValue, fieldinstance, binding, bindings, sdiData, filtered, -1);
                    } else {
                        if (rejectBindErrors) {
                            throw new SapphireException("One or more fields are already bound to another worksheet (" + existingdocumentid + ")!");
                        }
                        this.createUnboundInstance(field, fieldValue, fieldinstance, "Already bound to worksheet '" + existingdocumentid + "'");
                    }
                } else {
                    if (rejectBindErrors) {
                        throw new SapphireException("Non-unique binding for field '" + field.getProperty("fieldid") + "' - add additional binding properties to field definition!");
                    }
                    this.createUnboundInstance(field, fieldValue, fieldinstance, "Non-unique binding for field '" + field.getProperty("fieldid") + "' - add additional binding properties to field definition!");
                }
            } else {
                this.createUnboundInstance(field, fieldValue, fieldinstance, null);
            }
        } else {
            DataSet bindingValues = new DataSet();
            for (String key : bindingMap.keySet()) {
                bindingValues.addColumnValues(key, 0, (String)bindingMap.get(key), ";");
            }
            bindingValues.padColumns();
            DataSet bindData = new DataSet();
            for (int i = 0; i < bindingValues.size(); ++i) {
                bindingMap.clear();
                String[] columns = bindingValues.getColumns();
                for (int j = 0; j < columns.length; ++j) {
                    bindingMap.put(columns[j], bindingValues.getValue(i, columns[j]));
                }
                int dataRow = data.findRow(bindingMap);
                if (dataRow <= -1) continue;
                if (data.findRow(bindingMap, dataRow + 1) == -1) {
                    bindData.copyRow(data, dataRow, 1);
                    continue;
                }
                bindData.copyRow(data.getFilteredDataSet(bindingMap), -1, 1);
            }
            String existingdocumentid = "";
            for (int i = 0; i < bindData.size() && (existingdocumentid = this.getExistingDocument(field, sdiData, bindData, i)).length() <= 0; ++i) {
            }
            if (existingdocumentid.length() == 0) {
                this.createNewBoundInstance(field, fieldValue, fieldinstance, binding, bindings, sdiData, bindData, -1);
            } else {
                if (rejectBindErrors) {
                    throw new SapphireException("One or more fields are already bound to another worksheet (" + existingdocumentid + ")!");
                }
                this.createUnboundInstance(field, fieldValue, fieldinstance, "Already bound to worksheet '" + existingdocumentid + "'");
            }
        }
    }

    private String getExistingDocument(PropertyList field, SDIData sdiData, DataSet data, int dataRow) {
        String existingdocumentid = "";
        String autosave = field.getProperty("autosave");
        if (autosave.length() > 0) {
            if (autosave.equals("dataset")) {
                existingdocumentid = data.getValue(dataRow, "documentid");
            } else if (!autosave.equals("primary")) {
                HashMap<String, Object> bindingMap = new HashMap<String, Object>();
                bindingMap.put("sdcid", data.getValue(dataRow, "sdcid"));
                bindingMap.put("keyid1", data.getValue(dataRow, "keyid1"));
                bindingMap.put("keyid2", data.getValue(dataRow, "keyid2"));
                bindingMap.put("keyid3", data.getValue(dataRow, "keyid3"));
                bindingMap.put("paramlistid", data.getValue(dataRow, "paramlistid"));
                bindingMap.put("paramlistversionid", data.getValue(dataRow, "paramlistversionid"));
                bindingMap.put("variantid", data.getValue(dataRow, "variantid"));
                bindingMap.put("dataset", data.getBigDecimal(dataRow, "dataset"));
                DataSet dataset = sdiData.getDataset("dataset");
                int datasetRow = dataset.findRow(bindingMap);
                if (datasetRow > -1) {
                    existingdocumentid = dataset.getValue(datasetRow, "documentid");
                }
            }
        }
        return existingdocumentid;
    }

    private void createUnboundInstance(PropertyList field, PropertyList fieldValue, String fieldinstance, String errorMsg) {
        PropertyList instance = new PropertyList();
        instance.setProperty("fieldid", field.getProperty("fieldid"));
        instance.setProperty("fieldinstance", fieldinstance);
        instance.setProperty("bound", "N");
        if (errorMsg != null && field.getProperty("binderror", "error").equals("error")) {
            PropertyList reviewItem = new PropertyList();
            reviewItem.setProperty("reviewitemtype", "V");
            reviewItem.setProperty("reviewitemtext", errorMsg);
            reviewItem.setProperty("reviewitemstatus", "F");
            reviewItem.setProperty("createby", this.sapphireConnection.getSysuserId());
            PropertyListCollection reviewItems = instance.getCollection("reviewitems");
            if (reviewItems == null) {
                reviewItems = new PropertyListCollection();
            }
            reviewItems.add(reviewItem);
            instance.setProperty("reviewitems", reviewItems);
        }
        fieldValue.getCollection("instances").add(instance);
    }

    private PropertyList createNewBoundInstance(PropertyList field, PropertyList fieldValue, String fieldinstance, PropertyList binding, HashMap bindings, SDIData sdiData, DataSet data, int dataRow) {
        String fieldid = field.getProperty("fieldid");
        HashMap fieldMap = (HashMap)bindings.get("fields");
        Field mapfield = (Field)fieldMap.get(fieldid);
        String defaultValue = mapfield != null ? mapfield.getProperty("defaultvalue") : "";
        String autosave = field.getProperty("autosave");
        String tableid = null;
        String columnid = null;
        PropertyList instance = new PropertyList();
        instance.setProperty("fieldid", fieldid);
        instance.setProperty("fieldinstance", fieldinstance);
        int bindKeysReq = 0;
        if (autosave.equals("dataentry")) {
            tableid = "sdidataitem";
            columnid = "enteredtext";
            instance.setProperty("enteredtext", dataRow >= 0 ? data.getValue(dataRow, columnid) : data.getColumnValues(columnid, ";"));
            instance.setProperty("displayvalue", dataRow >= 0 ? data.getValue(dataRow, "displayvalue") : data.getColumnValues("displayvalue", ";"));
            bindKeysReq = 11;
        } else if (autosave.equals("dataitem") && binding != null) {
            tableid = "sdidataitem";
            columnid = binding.getProperty("columnid");
            this.setInstanceValue(data, dataRow, columnid, instance);
            bindKeysReq = 11;
        } else if (autosave.equals("dataset") && binding != null) {
            tableid = "sdidata";
            columnid = binding.getProperty("columnid");
            this.setInstanceValue(data, dataRow, columnid, instance);
            bindKeysReq = 8;
        } else if (autosave.equals("datareagent") && binding != null) {
            tableid = "sdidatarelation";
            columnid = this.getReagentColumn(binding.getProperty("reagentcomponent"));
            if (data.isValidColumn("relationid")) {
                this.setInstanceValue(data, dataRow, columnid, instance);
            } else {
                instance.setProperty("enteredtext", "");
                instance.setProperty("displayvalue", "");
            }
            bindKeysReq = 9;
        } else if (autosave.equals("primary") && binding != null) {
            try {
                tableid = this.ddtService.getSDCProperties(sdiData.getSdcid()).getProperty("tableid");
            }
            catch (ServiceException serviceException) {
                // empty catch block
            }
            columnid = binding.getProperty("columnid");
            this.setInstanceValue(data, dataRow, columnid, instance);
            bindKeysReq = 1;
        } else if (binding != null) {
            if (binding.containsKey("columnid")) {
                columnid = binding.getProperty("columnid");
                this.setInstanceValue(data, dataRow, columnid, instance);
                instance.setProperty("bound", "Y");
            } else if (binding.containsKey("limittypeid")) {
                instance.setProperty("enteredtext", dataRow >= 0 ? data.getValue(dataRow, "operator") + " " + data.getValue(dataRow, "value1") + " " + data.getValue(dataRow, "value2") : "");
                instance.setProperty("displayvalue", instance.getProperty("enteredtext"));
                instance.setProperty("bound", "Y");
            } else if (binding.containsKey("requestitem") && binding.getProperty("requestitem").equals("reagentrelation")) {
                columnid = this.getReagentColumn(binding.getProperty("reagentcomponent"));
                this.setInstanceValue(data, dataRow, columnid, instance);
                instance.setProperty("bound", "Y");
            } else if (binding.containsKey("requestitem") && binding.getProperty("requestitem").equals("dataspec")) {
                String enteredtext;
                this.getSpecDetails(data, dataRow, sdiData);
                bindings.put("spec", data.get(dataRow));
                try {
                    String expression = binding.getProperty("specexpression");
                    if (expression.length() == 0) {
                        enteredtext = data.getValue(dataRow, "specid");
                    } else {
                        if (!expression.startsWith("$G{")) {
                            expression = "$G{spec['" + expression + "'].operator1 + spec['" + expression + "'].value1 + ' ' + spec['" + expression + "'].operator2 + spec['" + expression + "'].value2}";
                        }
                        enteredtext = this.evalGroovyExpression(data.getValue(dataRow, "specid"), bindings, expression, false);
                    }
                }
                catch (SapphireException e) {
                    enteredtext = "Spec expression error!";
                }
                instance.setProperty("enteredtext", dataRow >= 0 ? enteredtext : "");
                instance.setProperty("displayvalue", instance.getProperty("enteredtext"));
                instance.setProperty("bound", "Y");
            }
        }
        PropertyList fieldBinding = new PropertyList();
        if (tableid != null && tableid.length() > 0 && columnid != null && columnid.length() > 0) {
            try {
                fieldBinding.setProperty("maxlength", this.ddtService.getColumnProperty(tableid, columnid, "columnlength"));
            }
            catch (ServiceException e) {
                // empty catch block
            }
        }
        if (bindKeysReq == 1) {
            fieldBinding.setProperty("sdcid", sdiData.getSdcid());
            String[] keys = sdiData.getKeys("primary");
            for (int i = 0; i < keys.length; ++i) {
                if (keys[i].length() <= 0) continue;
                fieldBinding.setProperty(keys[i], dataRow >= 0 ? data.getValue(dataRow, keys[i]) : data.getColumnValues(keys[i], ";"));
                fieldBinding.setProperty("keyid" + (i + 1), dataRow >= 0 ? data.getValue(dataRow, keys[i]) : data.getColumnValues(keys[i], ";"));
            }
            fieldBinding.setProperty("columnid", binding.getProperty("columnid"));
        } else if (bindKeysReq > 1) {
            fieldBinding.setProperty("sdcid", sdiData.getSdcid());
            fieldBinding.setProperty("keyid1", dataRow >= 0 ? data.getValue(dataRow, "keyid1") : data.getColumnValues("keyid1", ";"));
            fieldBinding.setProperty("keyid2", dataRow >= 0 ? data.getValue(dataRow, "keyid2") : data.getColumnValues("keyid2", ";"));
            fieldBinding.setProperty("keyid3", dataRow >= 0 ? data.getValue(dataRow, "keyid3") : data.getColumnValues("keyid3", ";"));
            fieldBinding.setProperty("paramlistid", dataRow >= 0 ? data.getValue(dataRow, "paramlistid") : data.getColumnValues("paramlistid", ";"));
            fieldBinding.setProperty("paramlistversionid", dataRow >= 0 ? data.getValue(dataRow, "paramlistversionid") : data.getColumnValues("paramlistversionid", ";"));
            fieldBinding.setProperty("variantid", dataRow >= 0 ? data.getValue(dataRow, "variantid") : data.getColumnValues("variantid", ";"));
            fieldBinding.setProperty("dataset", dataRow >= 0 ? data.getValue(dataRow, "dataset") : data.getColumnValues("dataset", ";"));
            if (bindKeysReq == 8) {
                fieldBinding.setProperty("sdidataid", dataRow >= 0 ? data.getValue(dataRow, "sdidataid") : data.getColumnValues("sdidataid", ";"));
            }
            if (bindKeysReq == 9) {
                fieldBinding.setProperty("relationid", dataRow >= 0 ? data.getValue(dataRow, "relationid") : data.getColumnValues("relationid", ";"));
                fieldBinding.setProperty("reagenttypeid", dataRow >= 0 ? data.getValue(dataRow, "reagenttypeid") : data.getColumnValues("reagenttypeid", ";"));
                fieldBinding.setProperty("reagentcomponent", binding.getProperty("reagentcomponent"));
            }
            if (bindKeysReq == 11) {
                fieldBinding.setProperty("paramid", data.getValue(dataRow, "paramid"));
                fieldBinding.setProperty("paramtype", data.getValue(dataRow, "paramtype"));
                fieldBinding.setProperty("replicateid", data.getValue(dataRow, "replicateid"));
                fieldBinding.setProperty("sdidataitemid", data.getValue(dataRow, "sdidataitemid"));
                if (autosave.equals("dataentry")) {
                    fieldBinding.setProperty("dataentrytype", data.getValue(dataRow, "datatypes"));
                    fieldBinding.setProperty("datareleased", data.getValue(dataRow, "releasedflag"));
                    fieldBinding.setProperty("mandatory", data.getValue(dataRow, "datatypes").equals("NC") ? "N" : data.getValue(dataRow, "mandatoryflag"));
                    fieldBinding.setProperty("entrysdcid", data.getValue(dataRow, "entrysdcid"));
                    fieldBinding.setProperty("entryreftypeid", data.getValue(dataRow, "entryreftypeid"));
                    fieldBinding.setProperty("instrumentfieldid", data.getValue(dataRow, "instrumentfieldid"));
                    fieldBinding.setProperty("calcexclude", data.getValue(dataRow, "calcexcludeflag"));
                    fieldBinding.setProperty("speccondition", "");
                    String calcrule = data.getValue(dataRow, "calcrule");
                    if (calcrule.contains(":")) {
                        field.setProperty("multisamplecalcs", "Y");
                    }
                }
            }
            if (binding != null) {
                if (binding.containsKey("columnid")) {
                    fieldBinding.setProperty("columnid", binding.getProperty("columnid"));
                } else if (binding.containsKey("limittypeid")) {
                    fieldBinding.setProperty("limittypeid", dataRow >= 0 ? data.getValue(dataRow, "limittypeid") : data.getColumnValues("limittypeid", ";"));
                }
            }
        }
        if (bindKeysReq > 0) {
            if (dataRow == -1) {
                fieldBinding.setProperty("binds", String.valueOf(data.size()));
            }
            instance.setProperty("binding", fieldBinding);
            instance.setProperty("bound", "Y");
        }
        fieldValue.getCollection("instances").add(instance);
        if (mapfield == null) {
            mapfield = new Field(field, fieldValue.getCollection("instances"), new M18NUtil(this.sapphireConnection));
            fieldMap.put(fieldid, mapfield);
        } else {
            FieldSetter.setValue(mapfield, instance);
        }
        return instance;
    }

    private void setInstanceValue(DataSet data, int dataRow, String columnid, PropertyList instance) {
        if (dataRow >= 0) {
            instance.setProperty("enteredtext", data.getValue(dataRow, columnid));
            instance.setProperty("displayvalue", instance.getProperty("enteredtext"));
        } else {
            String enteredtext = data.size() > 0 ? data.getValue(0, columnid) : "";
            for (int i = 1; i < data.size(); ++i) {
                if (enteredtext.equals(data.getValue(i, columnid))) continue;
                enteredtext = "(" + enteredtext + "+)";
                break;
            }
            instance.setProperty("enteredtext", enteredtext);
            instance.setProperty("displayvalue", enteredtext);
        }
    }

    private String getReagentColumn(String reagentComponent) {
        if (reagentComponent.equals("reagenttype")) {
            return "reagenttypeid";
        }
        if (reagentComponent.equals("lot")) {
            return "reagentlotid";
        }
        if (reagentComponent.equals("container")) {
            return "containerid";
        }
        if (reagentComponent.equals("quantity")) {
            return "amountused";
        }
        if (reagentComponent.equals("units")) {
            return "amountusedunits";
        }
        if (reagentComponent.equals("unitstype")) {
            return "amountusedunitstype";
        }
        if (reagentComponent.equals("defaultquantity")) {
            return "recommendedamount";
        }
        if (reagentComponent.equals("defaultunits")) {
            return "recommendedamountunits";
        }
        if (reagentComponent.equals("defaultunitstype")) {
            return "recommendedamountunitstype";
        }
        return "";
    }

    private void getSpecDetails(DataSet data, int dataRow, SDIData sdiData) {
        String specid = data.getValue(dataRow, "specid");
        String specversionid = data.getValue(dataRow, "specversionid");
        DataSet specdetails = sdiData.getDataset("spec_" + specid + "_" + specversionid);
        if (specdetails == null) {
            QueryProcessor qp = new QueryProcessor(this.sapphireConnection.getConnectionId());
            specdetails = qp.getPreparedSqlDataSet("SELECT spl.paramlistid, spl.paramlistversionid, spl.variantid, spl.paramid, spl.paramtype, limittypeid, operator1, operator2, value1, value2, allowanyparamlistflag FROM specparamitems spi, specparamlimits spl, speclimittype slt WHERE spi.paramlistid = spl.paramlistid AND spi.paramlistversionid = spl.paramlistversionid AND spi.variantid = spl.variantid AND spi.paramid = spl.paramid AND spi.paramtype = spl.paramtype AND spl.limittypesequence = slt.limittypesequence   AND spi.specid = slt.specid AND spi.specversionid = slt.specversionid AND spl.specid = slt.specid AND spl.specversionid = slt.specversionid AND spl.specid = ? AND spl.specversionid = ? ORDER BY 1, 2, 3, 4, 5", new Object[]{specid, specversionid});
            sdiData.setDataset("spec_" + specid + "_" + specversionid, specdetails);
        }
        String allowanyparamlistflag = "N";
        if (specdetails.size() > 0) {
            allowanyparamlistflag = specdetails.getValue(0, "allowanyparamlistflag", "N");
        }
        HashMap<String, String> findMap = new HashMap<String, String>();
        if (allowanyparamlistflag.equals("N")) {
            findMap.put("paramlistid", data.getValue(dataRow, "paramlistid"));
            findMap.put("paramlistversionid", data.getValue(dataRow, "paramlistversionid"));
            findMap.put("variantid", data.getValue(dataRow, "variantid"));
        } else if (allowanyparamlistflag.equals("V")) {
            findMap.put("paramlistid", data.getValue(dataRow, "paramlistid"));
            findMap.put("variantid", data.getValue(dataRow, "variantid"));
        } else if (allowanyparamlistflag.equals("A")) {
            findMap.put("paramlistid", data.getValue(dataRow, "paramlistid"));
        }
        findMap.put("paramid", data.getValue(dataRow, "paramid"));
        findMap.put("paramtype", data.getValue(dataRow, "paramtype"));
        int row = specdetails.findRow(findMap);
        while (row > -1) {
            HashMap<String, String> limitvalues = new HashMap<String, String>();
            limitvalues.put("specid", specid);
            limitvalues.put("specversionid", specversionid);
            limitvalues.put("operator1", specdetails.getValue(row, "operator1"));
            limitvalues.put("operator2", specdetails.getValue(row, "operator2"));
            limitvalues.put("value1", specdetails.getValue(row, "value1"));
            limitvalues.put("value2", specdetails.getValue(row, "value2"));
            ((HashMap)data.get(dataRow)).put(specdetails.getValue(row, "limittypeid"), limitvalues);
            row = specdetails.findRow(findMap, row + 1);
        }
    }

    private HashMap createBindingMap(String fieldid, PropertyList binding, HashMap parentDataBinding, HashMap bindings) throws SapphireException {
        HashMap<String, Object> bindingMap = new HashMap<String, Object>(parentDataBinding);
        if (binding != null) {
            if (binding.containsKey("keyid1") && binding.getProperty("keyid1").length() > 0) {
                bindingMap.put("keyid1", this.evalGroovyExpression(fieldid + ".binding.keyid1", bindings, binding.getProperty("keyid1"), false));
            }
            if (binding.containsKey("keyid2") && binding.getProperty("keyid2").length() > 0) {
                bindingMap.put("keyid2", this.evalGroovyExpression(fieldid + ".binding.keyid2", bindings, binding.getProperty("keyid2"), false));
            }
            if (binding.containsKey("keyid3") && binding.getProperty("keyid3").length() > 0) {
                bindingMap.put("keyid3", this.evalGroovyExpression(fieldid + ".binding.keyid3", bindings, binding.getProperty("keyid3"), false));
            }
            if (binding.containsKey("paramlistid") && binding.getProperty("paramlistid").length() > 0) {
                bindingMap.put("paramlistid", this.evalGroovyExpression(fieldid + ".binding.paramlistid", bindings, binding.getProperty("paramlistid"), false));
            }
            if (binding.containsKey("paramlistversionid") && binding.getProperty("paramlistversionid").length() > 0) {
                bindingMap.put("paramlistversionid", this.evalGroovyExpression(fieldid + ".binding.paramlistversionid", bindings, binding.getProperty("paramlistversionid"), false));
            }
            if (binding.containsKey("variantid") && binding.getProperty("variantid").length() > 0) {
                bindingMap.put("variantid", this.evalGroovyExpression(fieldid + ".binding.variantid", bindings, binding.getProperty("variantid"), false));
            }
            if (binding.containsKey("dataset") && binding.getProperty("dataset").length() > 0) {
                String dataset = this.evalGroovyExpression(fieldid + ".binding.dataset", bindings, binding.getProperty("dataset"), false);
                try {
                    bindingMap.put("dataset", dataset.equals("[max]") ? "[maxdataset]" : new BigDecimal(dataset));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (binding.containsKey("paramid") && binding.getProperty("paramid").length() > 0) {
                bindingMap.put("paramid", this.evalGroovyExpression(fieldid + ".binding.paramid", bindings, binding.getProperty("paramid"), false));
            }
            if (binding.containsKey("paramtype") && binding.getProperty("paramtype").length() > 0) {
                bindingMap.put("paramtype", this.evalGroovyExpression(fieldid + ".binding.parantype", bindings, binding.getProperty("paramtype"), false));
            }
            if (binding.containsKey("replicateid") && binding.getProperty("replicateid").length() > 0) {
                String repid = this.evalGroovyExpression(fieldid + ".binding.replicateid", bindings, binding.getProperty("replicateid"), false);
                try {
                    bindingMap.put("replicateid", repid.equals("[max]") ? "[maxreplicate]" : new BigDecimal(repid));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (binding.containsKey("limittypeid") && binding.getProperty("limittypeid").length() > 0) {
                bindingMap.put("limittypeid", this.evalGroovyExpression(fieldid + ".binding.limittypeid", bindings, binding.getProperty("limittypeid"), false));
            }
            if (binding.containsKey("specid") && binding.getProperty("specid").length() > 0) {
                String specid = this.evalGroovyExpression(fieldid + ".binding.specid", bindings, binding.getProperty("specid"), false);
                bindingMap.put("specid", specid.equals("[first]") ? "[firstspec]" : (specid.equals("[last]") || specid.equals("[max]") ? "[lastspec]" : specid));
            }
            if (binding.containsKey("specversionid") && binding.getProperty("specversionid").length() > 0) {
                bindingMap.put("specversionid", this.evalGroovyExpression(fieldid + ".binding.specversionid", bindings, binding.getProperty("specversionid"), false));
            }
            if (binding.containsKey("reagenttypeid") && binding.getProperty("reagenttypeid").length() > 0) {
                bindingMap.put("relationtype", this.evalGroovyExpression(fieldid + ".binding.reagenttypeid", bindings, binding.getProperty("reagenttypeid"), false));
            }
        }
        return bindingMap;
    }

    protected void evalGroovyYNProperty(PropertyList propertyList, String propertyid, HashMap bindings) throws SapphireException {
        this.evalGroovyYNProperty(propertyList, propertyList, propertyid, bindings);
    }

    protected void evalGroovyYNProperty(PropertyList source, PropertyList target, String propertyid, HashMap bindings) throws SapphireException {
        String expression = source.getProperty(propertyid);
        if (expression.length() > 0 && expression.startsWith("$G{") && expression.endsWith("}")) {
            if (this.debug) {
                target.setProperty(propertyid + "_orig", expression);
            }
            target.setProperty(propertyid, this.isExpressionY(source.getProperty("fieldid"), expression, bindings) ? "Y" : "N");
        }
    }

    protected void evalGroovyProperty(PropertyList propertyList, String propertyid, HashMap bindings) throws SapphireException {
        this.evalGroovyProperty(propertyList, propertyList, propertyid, bindings);
    }

    protected void evalGroovyProperty(PropertyList source, PropertyList target, String propertyid, HashMap bindings) throws SapphireException {
        String expression = source.getProperty(propertyid);
        boolean obf = false;
        if (expression.startsWith("{@}")) {
            expression = EncryptDecrypt.unobfsql(expression);
            obf = true;
        }
        if (expression.length() > 0 && expression.startsWith("$G{") && expression.endsWith("}")) {
            if (this.debug) {
                target.setProperty(propertyid + "_orig", expression);
            }
            if (obf) {
                target.setProperty(propertyid, EncryptDecrypt.obfsql(this.evalGroovyExpression(source.getProperty("fieldid"), bindings, expression, false)));
            } else {
                target.setProperty(propertyid, this.evalGroovyExpression(source.getProperty("fieldid"), bindings, expression, false));
            }
        }
    }

    protected boolean isExpressionY(String expressionowner, String expression, HashMap bindings) throws SapphireException {
        return (expression = this.evalGroovyExpression(expressionowner, bindings, expression, false)).equalsIgnoreCase("Y") || expression.equalsIgnoreCase("true") || expression.equals("1");
    }

    protected String evalGroovyExpression(String expressionowner, HashMap bindings, String expression, boolean dbAccess) throws SapphireException {
        return this.evalGroovyExpression(expressionowner, bindings, expression, dbAccess, dbAccess, dbAccess, dbAccess, dbAccess, dbAccess);
    }

    protected String evalGroovyExpression(String expressionowner, HashMap bindings, String expression, boolean dbAccess, boolean ap, boolean qp, boolean sdcp, boolean sdip, boolean seqp) throws SapphireException {
        boolean obf = false;
        if (expression.startsWith("{@}")) {
            expression = EncryptDecrypt.unobfsql(expression);
            obf = true;
        }
        if (expression.startsWith("$G{") && expression.endsWith("}")) {
            String origExpression = expression = expression.substring(3, expression.length() - "}".length());
            HashMap bindingMap = new HashMap();
            for (String name : bindings.keySet()) {
                bindingMap.put(name, bindings.get(name));
            }
            bindingMap.put("user", new ConnectionInfo(this.sapphireConnection).getUserAttributeMap());
            DBUtil dbu = null;
            StringBuffer log = new StringBuffer();
            if (dbAccess) {
                dbu = new DBUtil();
                ProcessingUtil.getSapphireObjectBindings(this.sapphireConnection, bindingMap, dbu, log, "DOCUMENTPROCESSING", ap, qp, sdcp, sdip, seqp, false);
                expression = ProcessingUtil.insertHeaderCode(expression, false);
            } else {
                LogContext logContext = new LogContext("GROOVY", this.sapphireConnection.getConnectionId());
                GroovyLogger logger = new GroovyLogger(logContext, log);
                M18NUtil m18n = new M18NUtil(this.sapphireConnection);
                HashMap<String, Object> sapphireObjects = new HashMap<String, Object>();
                sapphireObjects.put("logger", logger);
                sapphireObjects.put("m18n", m18n);
                bindingMap.put("sapphireobjects", sapphireObjects);
                expression = "//startinsert\ndef logger = sapphireobjects.logger;def m18n = sapphireobjects.m18n;//endinsert\n" + expression;
            }
            try {
                if (this.debug) {
                    this.logger.info("Evalating expression: " + origExpression);
                }
                expression = GroovyUtil.getInstance(this.sapphireConnection).evaluateSecure(expression, bindingMap, "Error evaluating expression for " + expressionowner + ".\n\n[exception] when evaluating " + expression);
                if (this.debug) {
                    this.logger.info("Result: " + expression);
                }
            }
            catch (Exception e) {
                String message = e.getMessage();
                throw new SapphireException(message.contains("//startinsert") ? message.substring(0, message.indexOf("//startinsert")) + "\n" + message.substring(message.indexOf("//endinsert") + 11) : message, e);
            }
            finally {
                if (dbu != null) {
                    dbu.reset();
                }
            }
        }
        return obf ? EncryptDecrypt.obfsql(expression) : expression;
    }

    protected String evalTokens(PropertyList substData, String value) {
        String[] tokens;
        String newValue = value;
        if (substData != null && (tokens = StringUtil.getTokens(value)) != null && tokens.length > 0) {
            M18NUtil m18n = new M18NUtil(this.sapphireConnection);
            PropertyList contextvars = substData.getPropertyList("contextvars") != null ? substData.getPropertyList("contextvars") : new PropertyList();
            PropertyList requestvars = substData.getPropertyList("requestvars") != null ? substData.getPropertyList("requestvars") : new PropertyList();
            for (int i = 0; i < tokens.length; ++i) {
                newValue = tokens[i].equalsIgnoreCase("currentuser") ? StringUtil.replaceAll(newValue, "[currentuser]", this.sapphireConnection.getSysuserId()) : (tokens[i].equalsIgnoreCase("currentdatetime") ? StringUtil.replaceAll(newValue, "[currentdatetime]", m18n.format(m18n.getNowCalendar())) : (tokens[i].equalsIgnoreCase("currentdate") ? StringUtil.replaceAll(newValue, "[currentdate]", m18n.formatDateOnly(m18n.getNowCalendar())) : (tokens[i].equalsIgnoreCase("currentsearchvalue") ? StringUtil.replaceAll(newValue, "[currentsearchvalue]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("documentid") ? StringUtil.replaceAll(newValue, "[documentid]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("documentversionid") ? StringUtil.replaceAll(newValue, "[documentversionid]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("formid") ? StringUtil.replaceAll(newValue, "[formid]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("formversionid") ? StringUtil.replaceAll(newValue, "[formversionid]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("fieldid") ? StringUtil.replaceAll(newValue, "[fieldid]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("groupid") ? StringUtil.replaceAll(newValue, "[groupid]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("title") ? StringUtil.replaceAll(newValue, "[title]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("value1") ? StringUtil.replaceAll(newValue, "[value1]", substData.getProperty("value1_value", substData.getProperty(tokens[i]))) : (tokens[i].equalsIgnoreCase("value2") ? StringUtil.replaceAll(newValue, "[value2]", substData.getProperty("value2_value", substData.getProperty(tokens[i]))) : (tokens[i].equalsIgnoreCase("enteredtext") ? StringUtil.replaceAll(newValue, "[enteredtext]", substData.getProperty(tokens[i])) : (tokens[i].equalsIgnoreCase("groupvalue") ? StringUtil.replaceAll(newValue, "[groupvalue]", substData.getProperty(tokens[i])) : (tokens[i].startsWith("request.") ? StringUtil.replaceAll(newValue, "[" + tokens[i] + "]", requestvars.getProperty(tokens[i].substring(8))) : StringUtil.replaceAll(newValue, "[" + tokens[i] + "]", contextvars.getProperty(tokens[i])))))))))))))))));
            }
        }
        return newValue;
    }

    protected void clearRSet(String rsetid) {
        if (rsetid.length() > 0) {
            DAMProcessor dam = new DAMProcessor(this.sapphireConnection.getConnectionId());
            dam.clearRSet(rsetid);
        }
    }

    protected void debugReturn(PropertyList requestData, Object returnData) {
        if (this.debug) {
            String out = "RETURNDATA for " + requestData.getProperty("requestcommand") + "\n";
            if (returnData == null) {
                this.logger.info(out + "NULL");
            } else if (returnData instanceof PropertyList) {
                this.logger.info(out + ((PropertyList)returnData).toXMLString());
            } else if (returnData instanceof DataSet) {
                this.logger.info(out + ((DataSet)returnData).toXML());
            } else {
                this.logger.info(out + returnData.toString());
            }
        }
    }
}

