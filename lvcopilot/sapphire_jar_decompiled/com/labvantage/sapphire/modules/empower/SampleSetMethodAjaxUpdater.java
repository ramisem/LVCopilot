/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.empower.DownloadMappingPage;
import com.labvantage.sapphire.modules.empower.DownloadMappingPageArea;
import com.labvantage.sapphire.modules.empower.EmpowerDownloadProcessor;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SampleSetMethodAjaxUpdater
extends BaseAjaxRequest {
    public static final String EMPOWER_POLICY = "EmpowerPolicy";
    public static final String EMPOWER_DEFAULT_NODE = "Sapphire Product";
    public static final String DELIMITER = ";";
    private ConnectionInfo connectionInfo;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.wizard_iframe.updatedSampleSetMethod");
        this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        try {
            DataSet downloadedReagentLotComponents;
            DataSet downloadedReagentLots;
            DataSet downloadedSampleComponents;
            DataSet downloadedSampleSetLines;
            DataSet downloadedSampleSetMethod;
            PropertyList sampleSetMethod;
            String limsqcbatchidcol = ajaxResponse.getRequestParameter("limsQCBatchIdCol");
            String limsSampleIdCol = ajaxResponse.getRequestParameter("limsSampleIdCol");
            String limsSdcIdCol = ajaxResponse.getRequestParameter("limsSdcIdCol");
            String limsDataSetKeyesCol = ajaxResponse.getRequestParameter("limsDataSetKeyesCol");
            String empowerPolicyNodeCol = ajaxResponse.getRequestParameter("policyNodeCol");
            DownloadMappingPage.MappingMode mode = DownloadMappingPage.MappingMode.valueOf(ajaxResponse.getRequestParameter("mappingMode"));
            String policyNode = ajaxResponse.getRequestParameter("policyNode");
            String empowerProject = ajaxResponse.getRequestParameter("empowerProject");
            String empowerDatabase = ajaxResponse.getRequestParameter("empowerDatabase");
            String newSampleSetMethod = ajaxResponse.getRequestParameter("newSampleSetMethod");
            PropertyList policy = this.getConfigurationProcessor().getPolicy(EMPOWER_POLICY, policyNode);
            EmpowerPolicyDef policyDef = new EmpowerPolicyDef(policy);
            try {
                HashMap<String, DataSet> dataMap = DownloadMappingPage.getCachedDataMap(request.getSession());
                HashMap<String, DataSet> componentMap = DownloadMappingPage.getCachedComponentMap(request.getSession());
                sampleSetMethod = DownloadMappingPage.getCachedSampleSet(request.getSession());
                if (sampleSetMethod.containsKey("comment")) {
                    sampleSetMethod.deleteProperty("comment");
                }
                downloadedSampleSetMethod = DownloadMappingPage.getCachedSampleSetMethod(request.getSession());
                if (dataMap != null && componentMap != null) {
                    DataSet samplesetLineData = dataMap.get(DownloadMappingPageArea.PageArea.SampleSetMethod.toString());
                    DownloadMappingPageArea.updateSampleSetLinesData(sampleSetMethod, samplesetLineData, "samplesetlines");
                    if (mode == DownloadMappingPage.MappingMode.AQCMode) {
                        downloadedSampleSetLines = dataMap.get(DownloadMappingPageArea.PageArea.QCBatch.toString());
                        downloadedSampleComponents = componentMap.get(DownloadMappingPageArea.PageArea.QCBatch.toString());
                        downloadedReagentLots = dataMap.get(DownloadMappingPageArea.PageArea.QCBatchSampleTypes.toString());
                        downloadedReagentLotComponents = componentMap.get(DownloadMappingPageArea.PageArea.QCBatchSampleTypes.toString());
                    } else {
                        downloadedSampleSetLines = dataMap.get(DownloadMappingPageArea.PageArea.UnknownSamples.toString());
                        downloadedSampleComponents = componentMap.get(DownloadMappingPageArea.PageArea.UnknownSamples.toString());
                        downloadedReagentLots = dataMap.get(DownloadMappingPageArea.PageArea.Reagents.toString());
                        downloadedReagentLotComponents = componentMap.get(DownloadMappingPageArea.PageArea.Reagents.toString());
                    }
                    downloadedSampleSetLines = this.modifyColNames(downloadedSampleSetLines);
                    downloadedSampleComponents = this.modifyColNames(downloadedSampleComponents);
                } else {
                    this.logger.warn("No data or components provided.");
                    downloadedSampleSetLines = null;
                    downloadedSampleComponents = null;
                    downloadedReagentLots = null;
                    downloadedReagentLotComponents = null;
                }
            }
            catch (Exception e) {
                ajaxResponse.setError("samplesetmethodprops is invalid");
                ajaxResponse.print();
                return;
            }
            if (mode != DownloadMappingPage.MappingMode.AQCMode) {
                DataSet qcBatchItems;
                DataSet qcBatchSampleTypes;
                PropertyList qcBatchProps;
                String qcBatchId;
                block20: {
                    Trace.logDebug("Creating a lite QCBatch");
                    qcBatchId = "";
                    try {
                        qcBatchProps = new PropertyList();
                        qcBatchSampleTypes = this.getQCBatchSampleTypes(limsSampleIdCol, limsSdcIdCol, sampleSetMethod, downloadedReagentLots);
                        qcBatchItems = this.getQCBatchItems(limsSampleIdCol, limsSdcIdCol, limsDataSetKeyesCol, sampleSetMethod, downloadedSampleSetLines);
                        if (qcBatchItems.getRowCount() != 0) break block20;
                        ajaxResponse.setError("No unknown samples have been mapped to the SampleSetMethod.");
                        return;
                    }
                    catch (SapphireException e) {
                        ajaxResponse.setError("Failed to create light weight qcbatch:" + e.getMessage());
                        ajaxResponse.print();
                        return;
                    }
                }
                String externalReference = empowerProject + "_" + empowerDatabase + "_" + newSampleSetMethod;
                qcBatchProps.setProperty("externalreference", externalReference);
                qcBatchId = EmpowerDownloadProcessor.createLightWeightQCBatch(this.getActionProcessor(), this.getQueryProcessor(), "Sample", qcBatchProps, qcBatchSampleTypes, qcBatchItems);
                Trace.logDebug("Created light QCBatch:" + qcBatchId);
                sampleSetMethod = this.updateSampleSetMethod(sampleSetMethod, limsqcbatchidcol, qcBatchId, empowerPolicyNodeCol, policyNode);
            } else {
                sampleSetMethod = this.updateSampleSetMethod(policyDef, sampleSetMethod, downloadedSampleSetMethod, policyNode, empowerPolicyNodeCol);
            }
            sampleSetMethod = this.updateSampleSetLines(policyDef, sampleSetMethod, downloadedSampleSetLines, downloadedSampleComponents, downloadedReagentLots, downloadedReagentLotComponents, limsSampleIdCol, limsSdcIdCol, limsDataSetKeyesCol);
            sampleSetMethod = this.addTranslations(sampleSetMethod, policyDef);
            ajaxResponse.addCallbackArgument("samplesetmethod", sampleSetMethod.toJSONString(true));
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Failed to update SampleSetMethod");
        }
        finally {
            ajaxResponse.print();
        }
    }

    private PropertyList addTranslations(PropertyList ssM, EmpowerPolicyDef policyDef) {
        PropertyListCollection translations = policyDef.getTranslations();
        ssM.setProperty("translations", translations);
        return ssM;
    }

    private PropertyList updateSampleSetMethod(PropertyList sampleSetMethod, String limsqcbatchidcol, String qcBatchId, String policyNodeCol, String policyNode) {
        PropertyList field;
        PropertyListCollection sampleSetMethodFields = sampleSetMethod.getCollection("fields");
        if (sampleSetMethodFields == null) {
            sampleSetMethodFields = new PropertyListCollection();
            sampleSetMethod.setProperty("fields", sampleSetMethodFields);
        }
        if ((field = sampleSetMethodFields.find("name", limsqcbatchidcol, true)) != null) {
            field.setProperty("value", qcBatchId);
        } else {
            field = new PropertyList();
            field.setProperty("name", limsqcbatchidcol);
            field.setProperty("type", "string");
            field.setProperty("value", qcBatchId);
            sampleSetMethodFields.add(field);
        }
        field = sampleSetMethodFields.find("name", policyNodeCol, true);
        if (field != null) {
            field.setProperty("value", policyNode);
        } else {
            field = new PropertyList();
            field.setProperty("name", policyNodeCol);
            field.setProperty("type", "string");
            field.setProperty("value", policyNode);
            sampleSetMethodFields.add(field);
        }
        sampleSetMethod.setProperty("fields", sampleSetMethodFields);
        return sampleSetMethod;
    }

    private DataSet getQCBatchSampleTypes(String limssampleidcol, String limssdcidcol, PropertyList sampleSetMethod, DataSet reagentLots) {
        PropertyListCollection empowerSSLs = sampleSetMethod.getCollection("samplesetlines");
        DataSet qcBatchSampleTypes = new DataSet(this.connectionInfo);
        for (int row = 0; row < empowerSSLs.size(); ++row) {
            PropertyList currentEmpowerSSL = empowerSSLs.getPropertyList(row);
            PropertyListCollection currentSSLFields = currentEmpowerSSL.getCollection("fields");
            if (currentSSLFields == null) continue;
            String limsid = currentSSLFields.find("name", limssampleidcol, true).getProperty("value", "");
            String sourcesdcid = currentSSLFields.find("name", limssdcidcol, true).getProperty("value", "");
            if (!sourcesdcid.equals("LV_ReagentLot")) continue;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("reagentlotid", limsid);
            int curr = qcBatchSampleTypes.findRow(filter);
            if (curr != -1) continue;
            curr = qcBatchSampleTypes.addRow();
            filter = new HashMap();
            filter.put("reagentlotid", limsid);
            DataSet matchReagent = reagentLots.getFilteredDataSet(filter);
            qcBatchSampleTypes.setString(curr, "reagentlotid", matchReagent.getString(0, "reagentlotid"));
            qcBatchSampleTypes.setString(curr, "reagenttypeid", matchReagent.getString(0, "reagenttypeid"));
        }
        return qcBatchSampleTypes;
    }

    private DataSet getQCBatchItems(String limssampleidcol, String limssdcidcol, String limsdatasetkeyescol, PropertyList sampleSetMethod, DataSet sampleSetLines) {
        PropertyListCollection empowerSSLs = sampleSetMethod.getCollection("samplesetlines");
        DataSet qcBatchItems = new DataSet(this.connectionInfo);
        for (int row = 0; row < empowerSSLs.size(); ++row) {
            PropertyList currentEmpowerSSL = empowerSSLs.getPropertyList(row);
            PropertyListCollection currentSSLFields = currentEmpowerSSL.getCollection("fields");
            if (currentSSLFields == null) continue;
            String sampleid = currentSSLFields.find("name", limssampleidcol, true).getProperty("value", "");
            String limsdatasetkeyid = currentSSLFields.find("name", limsdatasetkeyescol, true).getProperty("value", "");
            String sourcesdcid = currentSSLFields.find("name", limssdcidcol, true).getProperty("value", "");
            if (!sourcesdcid.equals("Sample")) continue;
            int curr = qcBatchItems.addRow();
            qcBatchItems.setString(curr, limssampleidcol, sampleid);
            qcBatchItems.setString(curr, limsdatasetkeyescol, limsdatasetkeyid);
            qcBatchItems.setString(curr, "empowerlimssampleid", sampleid);
            qcBatchItems.setString(curr, "empowerlimsdatasetkey", limsdatasetkeyid);
        }
        return qcBatchItems;
    }

    private DataSet modifyColNames(DataSet input) {
        DataSet ret = new DataSet(this.connectionInfo);
        String[] colNames = input.getColumns();
        for (int i = 0; i < colNames.length; ++i) {
            String currCol = colNames[i].replaceAll("_space_", " ");
            ret.addColumnValues(currCol, input.getColumnType(colNames[i]), input.getColumnValues(colNames[i], DELIMITER), DELIMITER);
        }
        return ret;
    }

    private PropertyList updateSampleSetMethod(EmpowerPolicyDef policyDef, PropertyList sampleSetMethod, DataSet downloadedSampleSetMethod, String policyNode, String policyNodeCol) {
        PropertyListCollection sampleSetMethodFields = sampleSetMethod.getCollection("fields");
        if (sampleSetMethodFields == null) {
            sampleSetMethodFields = new PropertyListCollection();
            sampleSetMethod.setProperty("fields", sampleSetMethodFields);
        }
        String[] downloadedColumns = downloadedSampleSetMethod.getColumns();
        for (int column = 0; column < downloadedColumns.length; ++column) {
            String empowerColumn = downloadedColumns[column];
            PropertyList field = sampleSetMethodFields.find("name", policyDef.getTranslate(empowerColumn), true);
            if (field != null) {
                if (field.getProperty("type").equalsIgnoreCase("enum")) {
                    field.setProperty("enumvalue", downloadedSampleSetMethod.getValue(0, empowerColumn, ""));
                    field.setProperty("value", "");
                    continue;
                }
                if (field.getProperty("type").equalsIgnoreCase("date")) {
                    String dateval = downloadedSampleSetMethod.getValue(0, empowerColumn, "");
                    if (dateval.length() <= 0) continue;
                    M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                    Calendar cal = m18n.parseCalendar(dateval);
                    long val = cal.getTimeInMillis();
                    field.setProperty("value", "" + val);
                    continue;
                }
                field.setProperty("value", downloadedSampleSetMethod.getValue(0, empowerColumn, ""));
                continue;
            }
            field = new PropertyList();
            field.setProperty("name", policyDef.getTranslate(empowerColumn));
            field.setProperty("type", "string");
            field.setProperty("value", downloadedSampleSetMethod.getValue(0, empowerColumn, ""));
            sampleSetMethodFields.add(field);
        }
        PropertyList field = sampleSetMethodFields.find("name", policyNodeCol, true);
        if (field != null) {
            field.setProperty("value", policyNode);
        } else {
            field = new PropertyList();
            field.setProperty("name", policyNodeCol);
            field.setProperty("type", "string");
            field.setProperty("value", policyNode);
            sampleSetMethodFields.add(field);
        }
        sampleSetMethod.setProperty("fields", sampleSetMethodFields);
        return sampleSetMethod;
    }

    private PropertyList updateSampleSetLines(EmpowerPolicyDef policyDef, PropertyList sampleSetMethod, DataSet sapphireSSLs, DataSet sampleComponents, DataSet sapphireRLLs, DataSet reagentLotComponents, String limssampleidcol, String limssdcidcol, String limsdatasetkeyescol) {
        PropertyListCollection empowerSSLs = sampleSetMethod.getCollection("samplesetlines");
        PropertyListCollection updatedSSLs = new PropertyListCollection();
        for (int row = 0; row < empowerSSLs.size(); ++row) {
            PropertyList currentEmpowerSSL = empowerSSLs.getPropertyList(row);
            PropertyListCollection currentSSLFields = currentEmpowerSSL.getCollection("fields");
            if (currentSSLFields == null) {
                currentSSLFields = new PropertyListCollection();
                currentEmpowerSSL.setProperty("fields", currentSSLFields);
            }
            String function = currentSSLFields.find("name", policyDef.getTranslate("Function"), true).getProperty("enumvalue", "");
            String limsid = currentSSLFields.find("name", limssampleidcol, true).getProperty("value", "");
            String sourcesdcid = currentSSLFields.find("name", limssdcidcol, true).getProperty("value", "");
            String datasetkeyes = currentSSLFields.find("name", limsdatasetkeyescol, true).getProperty("value", "");
            String[] downloadedColumns = null;
            DataSet match = new DataSet(this.connectionInfo);
            if (datasetkeyes.length() > 0) {
                String currdsk;
                if (sourcesdcid.equals("Sample")) {
                    downloadedColumns = sapphireSSLs.getColumns();
                    for (int i = 0; i < sapphireSSLs.getRowCount(); ++i) {
                        currdsk = sapphireSSLs.getString(i, limsdatasetkeyescol, "");
                        if (!currdsk.equals(datasetkeyes)) continue;
                        match.copyRow(sapphireSSLs, i, 1);
                        break;
                    }
                } else {
                    downloadedColumns = sapphireRLLs.getColumns();
                    for (int i = 0; i < sapphireRLLs.getRowCount(); ++i) {
                        currdsk = sapphireRLLs.getString(i, limsdatasetkeyescol, "");
                        if (!currdsk.equals(datasetkeyes)) continue;
                        match.copyRow(sapphireRLLs, i, 1);
                    }
                }
            }
            if (match == null || match.getRowCount() == 0) {
                updatedSSLs.add(currentEmpowerSSL);
                continue;
            }
            if ((function.equals(policyDef.getTranslate("Inject Standards")) || function.equals(policyDef.getTranslate("Inject Samples")) || function.contains(policyDef.getTranslate("Inject Controls"))) && limsid.length() > 0) {
                for (int column = 0; column < downloadedColumns.length; ++column) {
                    PropertyList field;
                    String empowerColumn = downloadedColumns[column];
                    if (empowerColumn.equalsIgnoreCase(policyDef.getTranslate("Level")) || empowerColumn.startsWith("__") || (field = currentSSLFields.find("name", policyDef.getTranslate(empowerColumn), true)) == null) continue;
                    if (field.getProperty("type").equalsIgnoreCase("enum")) {
                        field.setProperty("enumvalue", match.getValue(0, empowerColumn, ""));
                        field.setProperty("value", "");
                        continue;
                    }
                    if (field.getProperty("type").equalsIgnoreCase("date")) {
                        String dateval = match.getValue(0, empowerColumn, "");
                        if (dateval.length() <= 0) continue;
                        M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
                        Calendar cal = m18n.parseCalendar(dateval);
                        long val = cal.getTimeInMillis();
                        field.setProperty("value", "" + val);
                        continue;
                    }
                    if (field.getProperty("type").equalsIgnoreCase("number")) {
                        NumberFormat nf;
                        String formatvalue = field.getProperty("formatvalue", "0.0000");
                        BigDecimal number = null;
                        if (match.getColumnType(empowerColumn) == 1) {
                            number = match.getBigDecimal(0, empowerColumn);
                        } else {
                            try {
                                number = new BigDecimal(NumberFormat.getNumberInstance(match.getLocale()).parse(match.getValue(0, empowerColumn, "")).toString());
                            }
                            catch (Exception cal) {
                                // empty catch block
                            }
                        }
                        String separator = ".";
                        int sep = formatvalue.indexOf(separator);
                        if (sep == -1) {
                            separator = ",";
                            sep = formatvalue.indexOf(separator);
                        }
                        if (number == null) continue;
                        if (sep > -1) {
                            nf = NumberFormat.getNumberInstance(Locale.US);
                            DecimalFormat df = (DecimalFormat)nf;
                            df.setGroupingUsed(false);
                            df.setMaximumFractionDigits(100);
                            df.setMinimumFractionDigits(0);
                            field.setProperty("value", df.format(number));
                            continue;
                        }
                        number = number.setScale(0, 4);
                        nf = NumberFormat.getNumberInstance(Locale.US);
                        nf.setGroupingUsed(false);
                        field.setProperty("value", nf.format(number.intValue()));
                        continue;
                    }
                    if (match.getValue(0, empowerColumn, "").length() <= 0) continue;
                    field.setProperty("value", match.getValue(0, empowerColumn, ""));
                }
                PropertyListCollection currentEmpowerSSLComponents = currentEmpowerSSL.getCollection("components");
                if (sourcesdcid.equals("Sample")) {
                    currentEmpowerSSL.setProperty("components", this.currSSLComponents(policyDef, currentEmpowerSSLComponents, datasetkeyes, sampleComponents, limsdatasetkeyescol, limssampleidcol));
                } else {
                    currentEmpowerSSL.setProperty("components", this.currReagentLotComponents(policyDef, currentEmpowerSSLComponents, limsid, reagentLotComponents, limssampleidcol));
                }
            }
            updatedSSLs.add(currentEmpowerSSL);
        }
        sampleSetMethod.setProperty("samplesetlines", updatedSSLs);
        return sampleSetMethod;
    }

    private PropertyListCollection currSSLComponents(EmpowerPolicyDef policyDef, PropertyListCollection empowerSSLComponents, String datasetkeyes, DataSet components, String limsdatasetkeycol, String limssampleidcol) {
        PropertyListCollection currEmpowerComponents = empowerSSLComponents;
        DataSet currComponentsList = new DataSet(this.connectionInfo);
        for (int i = 0; i < components.getRowCount(); ++i) {
            String currdsk = components.getString(i, limsdatasetkeycol.toLowerCase(), "");
            if (!currdsk.equals(datasetkeyes)) continue;
            currComponentsList.copyRow(components, i, 1);
        }
        String[] colnames = components.getColumns();
        for (int i = 0; i < currComponentsList.getRowCount(); ++i) {
            PropertyListCollection currentEmpowerComponentFields = null;
            for (int j = 0; j < currEmpowerComponents.size(); ++j) {
                String processComponent = currComponentsList.getValue(i, "Component".toLowerCase());
                PropertyListCollection tempFieldsDetails = currEmpowerComponents.getPropertyList(j).getCollection("fields");
                Object match = null;
                for (int k = 0; k < tempFieldsDetails.size(); ++k) {
                    PropertyList currFieldPL = tempFieldsDetails.getPropertyList(k);
                    if (!currFieldPL.getProperty("name", "").equals(policyDef.getTranslate("Component")) || !currFieldPL.getProperty(policyDef.getTranslate("Value"), "").equals(processComponent)) continue;
                    currentEmpowerComponentFields = tempFieldsDetails;
                    Trace.logDebug("Found current empower component for: " + processComponent);
                }
            }
            if (currentEmpowerComponentFields != null) {
                block4: for (int col = 0; col < colnames.length; ++col) {
                    if (colnames[col].equalsIgnoreCase("Component") || colnames[col].equalsIgnoreCase("SampleType") || colnames[col].equalsIgnoreCase("Sample Position") || colnames[col].equalsIgnoreCase("Level") || colnames[col].equalsIgnoreCase(limssampleidcol) || colnames[col].equalsIgnoreCase(limsdatasetkeycol)) continue;
                    Trace.logDebug("Handling column:" + colnames[col]);
                    for (int c = 0; c < currentEmpowerComponentFields.size(); ++c) {
                        PropertyList pl = currentEmpowerComponentFields.getPropertyList(c);
                        if (!pl.getProperty("name", "").equalsIgnoreCase(policyDef.getTranslate(colnames[col]))) continue;
                        pl.setProperty("value", currComponentsList.getValue(i, colnames[col].toLowerCase()));
                        Trace.logDebug("Found matching propertylist in current empower component fields and set value to:" + currComponentsList.getValue(i, colnames[col]));
                        continue block4;
                    }
                }
                continue;
            }
            PropertyList currentEmpowerComponent = new PropertyList();
            String compval = currComponentsList.getValue(i, "Component".toLowerCase(), "NULL");
            currentEmpowerComponent.setProperty("componentvalue", compval);
            PropertyListCollection fields = new PropertyListCollection();
            boolean component_found = false;
            boolean value_found = false;
            for (int col = 0; col < colnames.length; ++col) {
                if (colnames[col].equalsIgnoreCase("Component") || colnames[col].equalsIgnoreCase("SampleType") || colnames[col].equalsIgnoreCase("Sample Position") || colnames[col].equalsIgnoreCase("Level") || colnames[col].equalsIgnoreCase(limssampleidcol) || colnames[col].equalsIgnoreCase(limsdatasetkeycol)) continue;
                PropertyList field = new PropertyList();
                String val = currComponentsList.getValue(i, colnames[col], "");
                if (colnames[col].equalsIgnoreCase("Component")) {
                    if (val.length() <= 0) continue;
                    field.setProperty("name", policyDef.getTranslate(colnames[col]));
                    field.setProperty("value", val);
                    component_found = true;
                    field.setProperty("type", "string");
                    fields.add(field);
                    continue;
                }
                if (colnames[col].equalsIgnoreCase("Value")) {
                    if (val.length() <= 0) continue;
                    field.setProperty("name", policyDef.getTranslate(colnames[col]));
                    field.setProperty("value", val);
                    value_found = true;
                    field.setProperty("type", "string");
                    fields.add(field);
                    continue;
                }
                field.setProperty("name", policyDef.getTranslate(colnames[col]));
                field.setProperty("value", val);
                field.setProperty("type", "string");
                fields.add(field);
            }
            Trace.logDebug("component_found:" + component_found);
            if (!component_found) {
                PropertyList field = new PropertyList();
                Trace.logDebug("Setting chinese component column:" + policyDef.getTranslate("Component"));
                field.setProperty("name", policyDef.getTranslate("Component"));
                field.setProperty("value", currComponentsList.getValue(i, "Component"));
                field.setProperty("type", "string");
                fields.add(field);
            }
            Trace.logDebug("value_found:" + value_found);
            if (!value_found) {
                PropertyList field = new PropertyList();
                field.setProperty("name", policyDef.getTranslate("Value"));
                field.setProperty("value", "");
                field.setProperty("type", "string");
                fields.add(field);
            }
            currentEmpowerComponent.setProperty("fields", fields);
            currEmpowerComponents.add(currentEmpowerComponent);
        }
        return currEmpowerComponents;
    }

    private PropertyListCollection currReagentLotComponents(EmpowerPolicyDef policyDef, PropertyListCollection empowerSSLComponents, String reagentlotid, DataSet reagentLotComponents, String limsidcol) {
        PropertyListCollection currEmpowerComponents = empowerSSLComponents;
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("reagentlotid", reagentlotid);
        DataSet currComponentsList = reagentLotComponents.getFilteredDataSet(filter);
        String[] colnames = reagentLotComponents.getColumns();
        for (int i = 0; i < currComponentsList.getRowCount(); ++i) {
            ArrayList currentEmpowerComponentFields = null;
            for (int j = 0; j < currEmpowerComponents.size(); ++j) {
                String processComponent = currComponentsList.getValue(i, "component");
                PropertyListCollection tempFieldsDetails = currEmpowerComponents.getPropertyList(j).getCollection("fields");
                Object match = null;
                for (int k = 0; k < tempFieldsDetails.size(); ++k) {
                    PropertyList currFieldPL = tempFieldsDetails.getPropertyList(k);
                    if (!currFieldPL.getProperty("name", "").equals(policyDef.getTranslate("Component")) || !currFieldPL.getProperty("value", "").equals(processComponent)) continue;
                    currentEmpowerComponentFields = tempFieldsDetails;
                }
            }
            if (currentEmpowerComponentFields != null) {
                block3: for (int col = 0; col < colnames.length; ++col) {
                    if (colnames[col].equalsIgnoreCase("reagentlotid") || colnames[col].equalsIgnoreCase("reagenttypeid") || colnames[col].equalsIgnoreCase("Level")) continue;
                    for (int c = 0; c < currentEmpowerComponentFields.size(); ++c) {
                        PropertyList pl = ((PropertyListCollection)currentEmpowerComponentFields).getPropertyList(c);
                        if (!pl.getProperty("name", "").equalsIgnoreCase(colnames[col])) continue;
                        pl.setProperty("value", currComponentsList.getValue(i, colnames[col]));
                        continue block3;
                    }
                }
                continue;
            }
            PropertyList currentEmpowerComponent = new PropertyList();
            currentEmpowerComponent.setProperty("componentvalue", currComponentsList.getValue(i, "Value".toLowerCase()));
            PropertyListCollection fields = new PropertyListCollection();
            boolean component_found = false;
            boolean value_found = false;
            for (int col = 0; col < colnames.length; ++col) {
                if (colnames[col].equalsIgnoreCase("reagentlotid") || colnames[col].equalsIgnoreCase("reagenttypeid") || colnames[col].equalsIgnoreCase("Level")) continue;
                PropertyList field = new PropertyList();
                String val = currComponentsList.getValue(i, colnames[col], "");
                if (colnames[col].equalsIgnoreCase("Component")) {
                    if (val.length() <= 0) continue;
                    field.setProperty("name", policyDef.getTranslate(colnames[col]));
                    field.setProperty("value", val);
                    component_found = true;
                    field.setProperty("type", "string");
                    fields.add(field);
                    continue;
                }
                if (colnames[col].equalsIgnoreCase("Value")) {
                    if (val.length() <= 0) continue;
                    field.setProperty("name", policyDef.getTranslate(colnames[col]));
                    field.setProperty("value", val);
                    value_found = true;
                    field.setProperty("type", "string");
                    fields.add(field);
                    continue;
                }
                field.setProperty("name", policyDef.getTranslate(colnames[col]));
                field.setProperty("value", val);
                field.setProperty("type", "string");
                fields.add(field);
            }
            if (!component_found) {
                PropertyList field = new PropertyList();
                field.setProperty("name", policyDef.getTranslate("Component"));
                field.setProperty("value", currComponentsList.getValue(i, policyDef.getTranslate("Component")));
                field.setProperty("type", "string");
                fields.add(field);
            }
            if (!value_found) {
                PropertyList field = new PropertyList();
                field.setProperty("name", policyDef.getTranslate("Component"));
                field.setProperty("value", "");
                field.setProperty("type", "string");
                fields.add(field);
            }
            currentEmpowerComponent.setProperty("fields", fields);
            currEmpowerComponents.add(currentEmpowerComponent);
        }
        return currEmpowerComponents;
    }
}

