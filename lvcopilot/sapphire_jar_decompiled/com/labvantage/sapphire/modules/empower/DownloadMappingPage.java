/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpSession
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.modules.empower.DownloadMappingPageArea;
import com.labvantage.sapphire.modules.empower.EmpowerDownloadProcessor;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DownloadMappingPage
extends BaseElement {
    public static final String MAPPED_COLUMNID = "__mapped";
    public static final String ROWID_COLUMNID = "__rowid";
    public static final String FROMROW_COLUMNID = "__fromrow";
    public static final String FROMAREA_COLUMNID = "__fromarea";
    private String sampleSetMethodTitle = "SampleSetMethod:";
    private String qcBatchTitle = "QC Batch:";
    private String reagentsTitle = "Reagents:";
    private String unknownSamplesTitle = "Unknown Samples:";
    private String qcBatchSampleTypesTitle = "QC Batch Sample Types:";
    private DataSet downloadedSampleSetMethodData = null;
    private DataSet downloadedSampleSetLinesData = null;
    private DataSet downloadedReagentLotLinesData = null;
    private DataSet downloadedSampleData = null;
    private DataSet downloadedSampleComponents = null;
    private DataSet downloadedReagentLotComponentsData = null;
    private PropertyList samplesetPropertyList = null;
    private DataSet samplesetLineData = null;
    private MappingMode mappingMode = MappingMode.AQCMode;
    private EmpowerPolicyDef policyDef;
    private String policyNode;
    private String newSampleSetMethodName = "";

    public DownloadMappingPage(PageContext pageContext, String policyNode) throws SapphireException {
        this.setPageContext(pageContext);
        if (policyNode.length() == 0) {
            policyNode = "Sapphire Product";
        }
        this.policyNode = policyNode;
        PropertyList policy = new ConfigurationProcessor(pageContext).getPolicy("EmpowerPolicy", policyNode);
        this.policyDef = new EmpowerPolicyDef(policy);
    }

    private static void addMappedColumns(DataSet input) {
        DownloadMappingPage.addMappedColumns(input, 0);
    }

    private static void addMappedColumns(DataSet input, int offset) {
        input.addColumn(MAPPED_COLUMNID, 0);
        input.setValue(-1, MAPPED_COLUMNID, "N");
        input.addColumn(ROWID_COLUMNID, 0);
        for (int i = 0; i < input.getRowCount(); ++i) {
            input.setValue(i, ROWID_COLUMNID, "" + (i + offset));
        }
    }

    private static DataSet modifyColNames(ConnectionInfo connectionInfo, DataSet input) {
        String[] colNames;
        DataSet ret = new DataSet(connectionInfo);
        for (String colName : colNames = input.getColumns()) {
            String currCol = colName.replaceAll(" ", "_space_");
            ret.addColumnValues(currCol, input.getColumnType(colName), input.getColumnValues(colName, ";"), ";");
        }
        return ret;
    }

    @Override
    public String getHtml() {
        EmpowerDownloadProcessor processor;
        PropertyList pageinfo;
        StringBuffer html = new StringBuffer();
        RequestContext rc = this.requestContext;
        if (rc == null) {
            rc = RequestContext.getRequestContext(this.pageContext);
        }
        this.mappingMode = (pageinfo = rc.getPropertyList()).getProperty("sampleselectionmode", "AQC Mode").equalsIgnoreCase("AQC Mode") ? MappingMode.AQCMode : MappingMode.CandidateMode;
        try {
            processor = new EmpowerDownloadProcessor(this.policyDef, new ActionProcessor(this.pageContext), new QueryProcessor(this.pageContext), new ConnectionProcessor(this.pageContext));
            this.pageContext.getSession().setAttribute("__EmpowerDownloadProcessor", (Object)processor);
            this.pageContext.getSession().setAttribute("__EmpowerPolicyDef", (Object)this.policyDef);
        }
        catch (Exception e) {
            processor = null;
            this.renderErrorHtml(html, "Could not obtain Empower Policy information.");
        }
        this.newSampleSetMethodName = pageinfo.getProperty("samplesetmethodnewname", "(auto)");
        String qcbatchid = "";
        if (processor != null && this.mappingMode == MappingMode.AQCMode) {
            qcbatchid = pageinfo.getProperty("selectedqcbatch", "");
            if (qcbatchid != null && qcbatchid.length() > 0) {
                try {
                    processor.process(qcbatchid, "AQC Mode");
                    this.downloadedSampleSetMethodData = processor.getSampleSetMethod();
                    this.downloadedSampleSetLinesData = processor.getSampleSetLines();
                    this.downloadedSampleSetLinesData = DownloadMappingPage.modifyColNames(this.connectionInfo, this.downloadedSampleSetLinesData);
                    DownloadMappingPage.addMappedColumns(this.downloadedSampleSetLinesData);
                    this.downloadedSampleComponents = processor.getComponents();
                    this.downloadedReagentLotLinesData = processor.getReagentLotLines();
                    this.downloadedReagentLotLinesData = DownloadMappingPage.modifyColNames(this.connectionInfo, this.downloadedReagentLotLinesData);
                    DownloadMappingPage.addMappedColumns(this.downloadedReagentLotLinesData);
                    this.downloadedReagentLotComponentsData = processor.getReagentLotComponents();
                    this.downloadedReagentLotComponentsData = DownloadMappingPage.modifyColNames(this.connectionInfo, this.downloadedReagentLotComponentsData);
                }
                catch (Exception e) {
                    this.downloadedSampleSetLinesData = null;
                    this.downloadedReagentLotLinesData = null;
                    this.renderErrorHtml(html, this.getTranslationProcessor().translate("Could not obtain sample set data.") + e.getMessage());
                    return html.toString();
                }
            } else {
                this.renderErrorHtml(html, this.getTranslationProcessor().translate("No QC Batch selected."));
                return html.toString();
            }
        }
        String basedonsamplesetmethod = "";
        if (pageinfo.getProperty("basedonsamplesetmethodproperties").length() > 0) {
            try {
                this.samplesetPropertyList = new PropertyList(new JSONObject(pageinfo.getProperty("basedonsamplesetmethodproperties")));
                basedonsamplesetmethod = this.samplesetPropertyList.getProperty("name", "");
                this.samplesetLineData = DownloadMappingPageArea.getSampleSetLinesData(this.connectionInfo, this.samplesetPropertyList, "samplesetlines");
            }
            catch (Exception e) {
                this.samplesetLineData = null;
                this.samplesetPropertyList = null;
                this.renderErrorHtml(html, "No sample set method template properties could be obtained.");
            }
        }
        this.qcBatchTitle = this.getTranslationProcessor().translate("QC Batch: ") + qcbatchid;
        this.sampleSetMethodTitle = "SamplSetMethod: " + this.newSampleSetMethodName + " (" + this.getTranslationProcessor().translate("Based on") + ": " + basedonsamplesetmethod + ")";
        this.cacheData(this.mappingMode, this.downloadedSampleSetLinesData, this.downloadedSampleComponents, this.downloadedReagentLotLinesData, this.downloadedReagentLotComponentsData, this.downloadedSampleData, this.samplesetLineData, this.samplesetPropertyList, this.downloadedSampleSetMethodData, this.pageContext.getSession());
        this.renderScriptAndStyle(html, this.mappingMode);
        this.renderLayoutHtml(html, this.mappingMode, this.downloadedSampleSetLinesData, this.downloadedSampleData, this.downloadedReagentLotLinesData, this.samplesetLineData, qcbatchid, basedonsamplesetmethod);
        return html.toString();
    }

    protected static HashMap<String, DataSet> getCachedDataMap(HttpSession session) {
        Object ret = session.getAttribute("DownloadMappingPage.dataMap");
        if (ret != null && ret instanceof HashMap) {
            return (HashMap)ret;
        }
        return null;
    }

    protected static void setCachedDataMap(HashMap<String, DataSet> dataMap, HttpSession session) {
        session.setAttribute("DownloadMappingPage.dataMap", dataMap);
    }

    protected static HashMap<String, DataSet> getCachedComponentMap(HttpSession session) {
        Object ret = session.getAttribute("DownloadMappingPage.componentMap");
        if (ret != null && ret instanceof HashMap) {
            return (HashMap)ret;
        }
        return null;
    }

    protected static void setCachedComponentMap(HashMap<String, DataSet> componentMap, HttpSession session) {
        session.setAttribute("DownloadMappingPage.componentMap", componentMap);
    }

    protected static PropertyList getCachedSampleSet(HttpSession session) {
        Object ret = session.getAttribute("DownloadMappingPage.samplesetPropertyList");
        if (ret != null && ret instanceof PropertyList) {
            return (PropertyList)ret;
        }
        return null;
    }

    protected static void setCachedSampleSet(PropertyList sampleSet, HttpSession session) {
        session.setAttribute("DownloadMappingPage.samplesetPropertyList", (Object)sampleSet);
    }

    protected static DataSet getCachedSampleSetMethod(HttpSession session) {
        Object ret = session.getAttribute("DownloadMappingPage.samplesetMethodDataSet");
        if (ret != null && ret instanceof DataSet) {
            return (DataSet)ret;
        }
        return null;
    }

    protected static void setCachedSampleSetMethod(DataSet sampleSetMethod, HttpSession session) {
        session.setAttribute("DownloadMappingPage.samplesetMethodDataSet", (Object)sampleSetMethod);
    }

    public static void clearCache(HttpSession session) {
        session.removeAttribute("DownloadMappingPage.dataMap");
        session.removeAttribute("DownloadMappingPage.componentMap");
        session.removeAttribute("DownloadMappingPage.samplesetPropertyList");
        session.removeAttribute("DownloadMappingPage.samplesetMethodDataSet");
    }

    private void cacheData(MappingMode mappingMode, DataSet downloadedSampleSetLinesData, DataSet downloadedSampleComponents, DataSet downloadedReagentLotLinesData, DataSet downloadedReagentLotComponentsData, DataSet downloadedSampleData, DataSet samplesetLineData, PropertyList samplesetPropertyList, DataSet downloadedSamplessetMethod, HttpSession session) {
        HashMap<String, DataSet> dataMap = new HashMap<String, DataSet>();
        HashMap<String, DataSet> componentMap = new HashMap<String, DataSet>();
        if (mappingMode == MappingMode.AQCMode) {
            dataMap.put(DownloadMappingPageArea.PageArea.QCBatch.toString(), downloadedSampleSetLinesData);
            componentMap.put(DownloadMappingPageArea.PageArea.QCBatch.toString(), downloadedSampleComponents);
            dataMap.put(DownloadMappingPageArea.PageArea.QCBatchSampleTypes.toString(), downloadedReagentLotLinesData);
            componentMap.put(DownloadMappingPageArea.PageArea.QCBatchSampleTypes.toString(), downloadedReagentLotComponentsData);
        }
        if (mappingMode == MappingMode.CandidateMode) {
            dataMap.put(DownloadMappingPageArea.PageArea.UnknownSamples.toString(), downloadedSampleData);
            componentMap.put(DownloadMappingPageArea.PageArea.UnknownSamples.toString(), downloadedSampleComponents);
            dataMap.put(DownloadMappingPageArea.PageArea.Reagents.toString(), downloadedReagentLotLinesData);
            componentMap.put(DownloadMappingPageArea.PageArea.Reagents.toString(), downloadedReagentLotComponentsData);
        }
        dataMap.put(DownloadMappingPageArea.PageArea.SampleSetMethod.toString(), samplesetLineData);
        DownloadMappingPage.setCachedDataMap(dataMap, session);
        DownloadMappingPage.setCachedComponentMap(componentMap, session);
        DownloadMappingPage.setCachedSampleSet(samplesetPropertyList, session);
        DownloadMappingPage.setCachedSampleSetMethod(downloadedSamplessetMethod, session);
    }

    private void renderErrorHtml(StringBuffer html, String msg) {
        html.append("<font color=\"red\">").append(this.getTranslationProcessor().translate(msg)).append("</font>");
    }

    private void renderScriptAndStyle(StringBuffer html, MappingMode mappingMode) {
        html.append(JavaScriptAPITag.getJQueryAPI(true, false, null, this.pageContext));
        html.append("<style>");
        html.append("body{overflow:hidden !important;}");
        html.append(".dm_draggable{cursor:move;border:dashed 1px #808080;}");
        html.append(".dm_unknown{color:blue;}");
        html.append(".dm_qcbatchstandard{color:green;}");
        html.append(".dm_qcbatchunknown{color:blue;}");
        html.append(".dm_qcsampletype{color:green;}");
        html.append(".dm_reagents{color:green;}");
        html.append(".dm_target{cursor:crosshair;border:dashed 1px #808080;}");
        html.append(".dm_active{background-color:#FFFF80;}");
        html.append(".dm_hover{background-color:#D3FF84;}");
        html.append(".dm_nontarget{cursor:default;color:gray}");
        html.append(".dm_ssmstandard{color:green;}");
        html.append(".dm_ssmunknown{color:blue;}");
        html.append(".container{border: 1px solid rgb(117, 153, 191); border-image: none; width: 50%; box-sizing: border-box; padding: 2px; }");
        html.append(".lefttop{ top: 0px; height: 50%; position: absolute;}");
        html.append(".leftbottom{height: 50%; bottom: 0px; position: absolute;}");
        html.append(".righttop{height: 100%; float: right;}");
        html.append(".innergrid{width:100%;height:10px;overflow:auto;border-top:solid 1px #b0c4de;display:none;}");
        html.append(".mapbuttons{padding: 0;table-layout:fixed;width:400px;}");
        html.append("</style>");
        html.append("<script language=\"JavaScript\" type=\"text/javascript\" src=\"WEB-CORE/modules/empower/scripts/downloadmappingpage.js\"></script>");
        html.append("<script language=\"JavaScript\" type=\"text/javascript\">");
        html.append("downloadMappingPage.limsqcbatchidcol = '").append(this.policyDef.getEmpowerCoreMapping("empowerqcbatchid")).append("';");
        html.append("downloadMappingPage.limssampleidcol = '").append(this.policyDef.getEmpowerCoreMapping("empowerlimssampleid")).append("';");
        html.append("downloadMappingPage.limsdatasetkeyescol = '").append(this.policyDef.getEmpowerCoreMapping("empowerlimsdatasetkey")).append("';");
        html.append("downloadMappingPage.policynodecol='").append(this.policyDef.getEmpowerCoreMapping("empowerpolicynode")).append("';");
        html.append("downloadMappingPage.policynode='").append(this.policyNode).append("';");
        html.append("downloadMappingPage.limssdcidcol = '").append(this.policyDef.getEmpowerCoreMapping("empowerlimssdcid")).append("';");
        html.append("downloadMappingPage.mappingMode = '").append(mappingMode.toString()).append("';");
        html.append("downloadMappingPage.newSampleSetMethodName= '").append(this.newSampleSetMethodName).append("';");
        if (mappingMode == MappingMode.AQCMode) {
            html.append("downloadMappingPage.areas.push('").append(DownloadMappingPageArea.PageArea.QCBatch.toString()).append("');");
            html.append("downloadMappingPage.titles['" + (Object)((Object)DownloadMappingPageArea.PageArea.QCBatch) + "'] = '").append(this.qcBatchTitle).append("';");
        }
        if (mappingMode == MappingMode.CandidateMode) {
            html.append("downloadMappingPage.areas.push('").append(DownloadMappingPageArea.PageArea.UnknownSamples.toString()).append("');");
            html.append("downloadMappingPage.titles['" + (Object)((Object)DownloadMappingPageArea.PageArea.UnknownSamples) + "'] = '").append(this.getTranslationProcessor().translate(this.unknownSamplesTitle)).append("';");
        }
        if (mappingMode == MappingMode.AQCMode) {
            html.append("downloadMappingPage.areas.push('").append(DownloadMappingPageArea.PageArea.QCBatchSampleTypes.toString()).append("');");
            html.append("downloadMappingPage.titles['" + (Object)((Object)DownloadMappingPageArea.PageArea.QCBatchSampleTypes) + "'] = '").append(this.getTranslationProcessor().translate(this.qcBatchSampleTypesTitle)).append("';");
        }
        if (mappingMode == MappingMode.CandidateMode) {
            html.append("downloadMappingPage.areas.push('").append(DownloadMappingPageArea.PageArea.Reagents.toString()).append("');");
            html.append("downloadMappingPage.titles['" + (Object)((Object)DownloadMappingPageArea.PageArea.Reagents) + "'] = '").append(this.getTranslationProcessor().translate(this.reagentsTitle)).append("';");
        }
        html.append("downloadMappingPage.areas.push('").append(DownloadMappingPageArea.PageArea.SampleSetMethod.toString()).append("');");
        html.append("downloadMappingPage.titles['" + (Object)((Object)DownloadMappingPageArea.PageArea.SampleSetMethod) + "'] = '").append(this.sampleSetMethodTitle).append("';");
        html.append("</script>");
    }

    private void renderLayoutHtml(StringBuffer html, MappingMode mappingMode, DataSet qcBatchData, DataSet sampleData, DataSet reagentData, DataSet samplesetLineData, String qcbatchid, String basedonsamplesetmethod) {
        DownloadMappingPageArea bottomLeft;
        String bottomleftId;
        DownloadMappingPageArea topleft;
        String topleftId;
        if (mappingMode == MappingMode.AQCMode) {
            topleftId = DownloadMappingPageArea.PageArea.QCBatch.toString();
            topleft = new DownloadMappingPageArea(DownloadMappingPageArea.PageArea.QCBatch, qcBatchData, this.pageContext, this.policyDef, this.qcBatchTitle);
        } else {
            topleftId = DownloadMappingPageArea.PageArea.UnknownSamples.toString();
            topleft = new DownloadMappingPageArea(DownloadMappingPageArea.PageArea.UnknownSamples, sampleData, this.pageContext, this.policyDef, this.unknownSamplesTitle);
        }
        html.append("<div id=\"").append(topleftId).append("_container\" class=\"container lefttop\">");
        html.append("<b>");
        html.append(topleft.getHtml());
        html.append("</b>");
        html.append("</div>");
        if (mappingMode == MappingMode.AQCMode) {
            bottomleftId = DownloadMappingPageArea.PageArea.QCBatchSampleTypes.toString();
            bottomLeft = new DownloadMappingPageArea(DownloadMappingPageArea.PageArea.QCBatchSampleTypes, reagentData, this.pageContext, this.policyDef, this.qcBatchSampleTypesTitle);
        } else {
            bottomleftId = DownloadMappingPageArea.PageArea.Reagents.toString();
            bottomLeft = new DownloadMappingPageArea(DownloadMappingPageArea.PageArea.Reagents, reagentData, this.pageContext, this.policyDef, this.reagentsTitle);
        }
        html.append("<div id=\"").append(bottomleftId).append("_container\" class=\"container leftbottom\">");
        html.append(bottomLeft.getHtml());
        html.append("</div>");
        html.append("<div id=\"").append(DownloadMappingPageArea.PageArea.SampleSetMethod.toString()).append("_container\" class=\"container righttop\">");
        DownloadMappingPageArea right = new DownloadMappingPageArea(DownloadMappingPageArea.PageArea.SampleSetMethod, samplesetLineData, this.pageContext, this.policyDef, this.sampleSetMethodTitle);
        html.append(right.getHtml());
        html.append("</div>");
    }

    public static String doUnmap(TranslationProcessor tp, HashMap<String, DataSet> dataMap, DataSet sampleSetLineData, int[] fromSelection, int[] sampleSetLineSelection, ArrayList<DownloadMappingPageArea.PageArea> redolist, EmpowerPolicyDef policyDef) {
        String error = "";
        if (dataMap != null && dataMap.size() > 0 && sampleSetLineData != null && sampleSetLineData.size() > 0) {
            if (sampleSetLineSelection != null && sampleSetLineSelection.length > 0) {
                if (sampleSetLineData.isValidColumn(policyDef.getEmpowerCoreMapping("empowerlimssampleid"))) {
                    for (int i = 0; i < fromSelection.length; ++i) {
                        int row = sampleSetLineSelection[i];
                        sampleSetLineData.setValue(row, policyDef.getEmpowerCoreMapping("empowerlimssampleid"), "");
                        sampleSetLineData.setValue(row, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "");
                        sampleSetLineData.setValue(row, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "0");
                        try {
                            int from = Integer.parseInt(sampleSetLineData.getValue(row, FROMROW_COLUMNID));
                            DownloadMappingPageArea.PageArea area = DownloadMappingPageArea.PageArea.valueOf(sampleSetLineData.getValue(row, FROMAREA_COLUMNID));
                            if (dataMap.containsKey(area.toString())) {
                                DataSet toupdate = dataMap.get(area.toString());
                                if (toupdate != null && toupdate.getRowCount() > from) {
                                    toupdate.setValue(from, MAPPED_COLUMNID, "N");
                                }
                                if (!redolist.contains(area.toString())) {
                                    redolist.add(area);
                                }
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        sampleSetLineData.setValue(row, FROMROW_COLUMNID, "");
                        sampleSetLineData.setValue(row, FROMAREA_COLUMNID, "");
                    }
                    redolist.add(DownloadMappingPageArea.PageArea.SampleSetMethod);
                } else {
                    error = tp.translate("No valid eu_limsid column found.");
                }
            } else {
                error = tp.translate("Select one or more sample set lines to unmap.");
            }
        } else {
            error = tp.translate("Could not obtain sampleset line data.");
        }
        return error;
    }

    public static DataSet getFilteredData(DataSet input) {
        HashMap<String, String> fil = new HashMap<String, String>(1);
        fil.put(MAPPED_COLUMNID, "N");
        return input.getFilteredDataSet(fil);
    }

    public static String doUnmapAll(TranslationProcessor tp, HashMap<String, DataSet> dataMap, DataSet sampleSetLineData, ArrayList<DownloadMappingPageArea.PageArea> redolist, EmpowerPolicyDef policyDef) {
        String error = "";
        if (dataMap != null && dataMap.size() > 0 && sampleSetLineData != null && sampleSetLineData.size() > 0) {
            if (sampleSetLineData.isValidColumn(policyDef.getEmpowerCoreMapping("empowerlimssampleid"))) {
                for (int row = 0; row < sampleSetLineData.size(); ++row) {
                    sampleSetLineData.setValue(row, policyDef.getEmpowerCoreMapping("empowerlimssampleid"), "");
                    sampleSetLineData.setValue(row, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "");
                    sampleSetLineData.setValue(row, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "0");
                    try {
                        int from = Integer.parseInt(sampleSetLineData.getValue(row, FROMROW_COLUMNID));
                        DownloadMappingPageArea.PageArea area = DownloadMappingPageArea.PageArea.valueOf(sampleSetLineData.getValue(row, FROMAREA_COLUMNID));
                        if (dataMap.containsKey(area.toString())) {
                            DataSet toupdate = dataMap.get(area.toString());
                            if (toupdate != null && toupdate.getRowCount() > from) {
                                toupdate.setValue(from, MAPPED_COLUMNID, "N");
                            }
                            if (!redolist.contains(area.toString())) {
                                redolist.add(area);
                            }
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    sampleSetLineData.setValue(row, FROMROW_COLUMNID, "");
                    sampleSetLineData.setValue(row, FROMAREA_COLUMNID, "");
                }
                redolist.add(DownloadMappingPageArea.PageArea.SampleSetMethod);
            } else {
                error = tp.translate("No valid eu_limsid column found.");
            }
        } else {
            error = tp.translate("Could not obtain sampleset line data.");
        }
        return error;
    }

    public static String doAdd(DataSet data, DataSet component, DataSet sdiworkitem, PageContext pageContext) {
        int d;
        EmpowerDownloadProcessor processor = (EmpowerDownloadProcessor)pageContext.getSession().getAttribute("__EmpowerDownloadProcessor");
        String error = "";
        try {
            processor.processCandidateSamples(sdiworkitem);
        }
        catch (SapphireException e) {
            return "Failed to download samples";
        }
        DataSet downloadedSampleData = processor.getSampleSetLines();
        ConnectionProcessor cp = new ConnectionProcessor(pageContext);
        ConnectionInfo connectionInfo = cp.getConnectionInfo(cp.getConnectionid());
        downloadedSampleData = DownloadMappingPage.modifyColNames(connectionInfo, downloadedSampleData);
        if (data == null) {
            DownloadMappingPage.addMappedColumns(downloadedSampleData);
        } else {
            DownloadMappingPage.addMappedColumns(downloadedSampleData, data.getRowCount());
        }
        DataSet downloadedSampleComponents = processor.getComponents();
        if (data == null) {
            data = new DataSet(connectionInfo);
        }
        if (component == null) {
            component = new DataSet(connectionInfo);
        }
        if (downloadedSampleData != null) {
            for (d = 0; d < downloadedSampleData.getRowCount(); ++d) {
                if (data.findRow("sdiworkitemid", downloadedSampleData.getValue(d, "sdiworkitemid")) != -1) continue;
                data.copyRow(downloadedSampleData, d, 1);
            }
        }
        for (int i = 0; i < data.getRowCount(); ++i) {
            data.setString(i, "Position", "" + (i + 1));
        }
        if (downloadedSampleComponents != null) {
            for (d = 0; d < downloadedSampleComponents.getRowCount(); ++d) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("sdiworkitemid", downloadedSampleComponents.getString(d, "sdiworkitemid"));
                filter.put("component", downloadedSampleComponents.getString(d, "component"));
                if (component.getFilteredDataSet(filter).getRowCount() != 0) continue;
                component.copyRow(downloadedSampleComponents, d, 1);
            }
        }
        return error;
    }

    public static String doAdd(DataSet data, DataSet component, String reagentLotIds, PageContext pageContext) {
        EmpowerDownloadProcessor processor = (EmpowerDownloadProcessor)pageContext.getSession().getAttribute("__EmpowerDownloadProcessor");
        String error = "";
        try {
            processor.processCandidateReagentLots(reagentLotIds);
        }
        catch (SapphireException e) {
            return "Failed to download reagents";
        }
        ConnectionProcessor cp = new ConnectionProcessor(pageContext);
        ConnectionInfo connectionInfo = cp.getConnectionInfo(cp.getConnectionid());
        DataSet downloadedReagentLots = processor.getReagentLotLines();
        downloadedReagentLots = DownloadMappingPage.modifyColNames(connectionInfo, downloadedReagentLots);
        if (data == null) {
            DownloadMappingPage.addMappedColumns(downloadedReagentLots);
        } else {
            DownloadMappingPage.addMappedColumns(downloadedReagentLots, data.getRowCount());
        }
        DataSet downloadedComponents = processor.getReagentLotComponents();
        if (data == null) {
            data = new DataSet(connectionInfo);
        }
        if (component == null) {
            component = new DataSet(connectionInfo);
        }
        if (downloadedReagentLots != null) {
            data.copyRow(downloadedReagentLots, -1, 1);
        }
        if (downloadedComponents != null) {
            component.copyRow(downloadedComponents, -1, 1);
        }
        return error;
    }

    public static String doRemove(String idcol, DataSet data, DataSet component, int[] selected) {
        int i;
        String error = "";
        for (int i2 = 0; i2 < selected.length; ++i2) {
            int rowId = selected[i2];
            String id = data.getString(rowId, idcol);
            for (int j = 0; j < component.getRowCount(); ++j) {
                if (!component.getString(j, idcol, "").equals(id)) continue;
                component.setString(j, "remove", "Y");
            }
            data.setString(rowId, "remove", "Y");
        }
        int datasize = data.size();
        int componentsize = component.size();
        for (i = datasize - 1; i >= 0; --i) {
            if (!"Y".equals(data.getString(i, "remove", "N"))) continue;
            data.deleteRow(i);
        }
        for (i = componentsize - 1; i >= 0; --i) {
            if (!"Y".equals(component.getString(i, "remove", "N"))) continue;
            component.deleteRow(i);
        }
        for (i = 0; i < data.getRowCount(); ++i) {
            data.setString(i, "Position", "" + (i + 1));
        }
        return error;
    }

    private static boolean matchSampleTypes(EmpowerPolicyDef policyDef, String from, String to, boolean isReagent) {
        if (isReagent) {
            return to.equals(policyDef.getTranslate("Inject Standards")) || to.equals(policyDef.getTranslate("Inject Controls"));
        }
        if (from.equals("Standard")) {
            return to.equals(policyDef.getTranslate("Inject Standards"));
        }
        if (from.equals("Control")) {
            return to.equals(policyDef.getTranslate("Inject Controls"));
        }
        return to.equals(policyDef.getTranslate("Inject Samples"));
    }

    public static String doMap(TranslationProcessor tp, DataSet fromAllData, DataSet toData, int[] fromSelection, int[] toSelection, DownloadMappingPageArea.PageArea area, EmpowerPolicyDef policyDef) {
        String error = "";
        String limssampleidcol = policyDef.getEmpowerCoreMapping("empowerlimssampleid");
        String excludecol = policyDef.getEmpowerCoreMapping("empowerexcludeflag");
        String limsdatasetkeycol = policyDef.getEmpowerCoreMapping("empowerlimsdatasetkey");
        String sampletypecol = "SampleType".replaceAll(" ", "_space_");
        DataSet fromDisplayedData = DownloadMappingPage.getFilteredData(fromAllData);
        if (fromAllData != null && toData != null) {
            if (fromSelection != null && fromSelection.length > 0) {
                if (toSelection != null && toSelection.length > 0) {
                    if (fromSelection.length == toSelection.length || area == DownloadMappingPageArea.PageArea.QCBatchSampleTypes || area == DownloadMappingPageArea.PageArea.Reagents) {
                        if (toData.isValidColumn(limssampleidcol)) {
                            if ((area == DownloadMappingPageArea.PageArea.QCBatchSampleTypes || area == DownloadMappingPageArea.PageArea.Reagents) && fromSelection.length == 1 && toSelection.length > 1) {
                                int fromrow = fromSelection[0];
                                for (int to = 0; to < toSelection.length; ++to) {
                                    int torow = toSelection[to];
                                    if (fromAllData.isValidColumn(limssampleidcol)) {
                                        String currentsampleid;
                                        String fromSampleType = fromAllData.getString(fromrow, sampletypecol, "");
                                        String toSampleType = toData.getString(torow, policyDef.getTranslate("Function"), "");
                                        String toExcludeFlag = toData.getString(torow, excludecol, "");
                                        if (toExcludeFlag.startsWith("Y")) {
                                            error = tp.translate("Cannot map Sample to ") + (torow + 1) + tp.translate(". Exclude Flag = Y");
                                        }
                                        if ((currentsampleid = toData.getValue(torow, limssampleidcol, "")).length() > 0) {
                                            error = tp.translate("Cannot map Sample to ") + (torow + 1) + tp.translate(" already mapped to ") + currentsampleid;
                                        }
                                        if (!DownloadMappingPage.matchSampleTypes(policyDef, fromSampleType, toSampleType, true)) {
                                            error = tp.translate("Sample ") + fromAllData.getValue(fromrow, limssampleidcol, "") + tp.translate(" cannot be mapped into a SampleSetLine with Function \"") + toSampleType + "\"";
                                            continue;
                                        }
                                        toData.setValue(torow, limssampleidcol, fromAllData.getValue(fromrow, limssampleidcol, "(empty)"));
                                        toData.setValue(torow, limsdatasetkeycol, fromAllData.getString(fromrow, limsdatasetkeycol, ""));
                                        toData.setValue(torow, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "Sample");
                                        toData.setValue(torow, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "1");
                                        fromAllData.setValue(fromrow, MAPPED_COLUMNID, "Y");
                                        toData.setValue(torow, FROMROW_COLUMNID, fromAllData.getValue(fromrow, ROWID_COLUMNID, "" + fromrow));
                                        toData.setValue(torow, FROMAREA_COLUMNID, area.toString());
                                        continue;
                                    }
                                    if (!fromAllData.isValidColumn("reagentlotid")) continue;
                                    String toType = toData.getString(torow, policyDef.getTranslate("Function"), "");
                                    String currid = toData.getValue(torow, limssampleidcol, "");
                                    if (currid.length() > 0) {
                                        error = tp.translate("Cannot map Reagent Lot ") + (torow + 1) + tp.translate(" already mapped to ") + currid;
                                    }
                                    if (toType.indexOf(policyDef.getTranslate("Standard")) == -1 && toType.indexOf(policyDef.getTranslate("Control")) == -1) {
                                        error = tp.translate("Cannot map a reagent lot to a Sample Set Line with Function ") + toType;
                                        continue;
                                    }
                                    toData.setValue(torow, limssampleidcol, fromAllData.getValue(fromrow, "reagentlotid", ""));
                                    toData.setValue(torow, limsdatasetkeycol, fromAllData.getString(fromrow, limsdatasetkeycol, ""));
                                    toData.setValue(torow, FROMROW_COLUMNID, fromAllData.getValue(fromrow, ROWID_COLUMNID, "" + fromrow));
                                    toData.setValue(torow, FROMAREA_COLUMNID, area.toString());
                                    toData.setValue(torow, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "LV_ReagentLot");
                                    toData.setValue(torow, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "0");
                                }
                            } else {
                                for (int i = 0; i < fromSelection.length; ++i) {
                                    int fromrow = fromSelection[i];
                                    int torow = toSelection[i];
                                    if (fromAllData.isValidColumn(limssampleidcol)) {
                                        String currentsampleid;
                                        String fromSampleType = fromAllData.getString(fromrow, sampletypecol, "");
                                        String toSampleType = toData.getString(torow, policyDef.getTranslate("Function"), "");
                                        String toExcludeFlag = toData.getString(torow, excludecol, "");
                                        if (toExcludeFlag.startsWith("Y")) {
                                            error = tp.translate("Cannot map Sample to ") + (torow + 1) + tp.translate(" . Exclude Flag = Y");
                                        }
                                        if ((currentsampleid = toData.getValue(torow, limssampleidcol, "")).length() > 0) {
                                            error = tp.translate("Cannot map Sample to ") + (torow + 1) + tp.translate(" already mapped to ") + currentsampleid;
                                        }
                                        if (!DownloadMappingPage.matchSampleTypes(policyDef, fromSampleType, toSampleType, false)) {
                                            error = tp.translate("Sample ") + fromAllData.getValue(fromrow, limssampleidcol, "") + tp.translate(" cannot be mapped into a SampleSetLine with Function \"") + toSampleType + "\"";
                                            continue;
                                        }
                                        toData.setValue(torow, limssampleidcol, fromAllData.getValue(fromrow, limssampleidcol, "(empty)"));
                                        toData.setValue(torow, limsdatasetkeycol, fromAllData.getString(fromrow, limsdatasetkeycol, ""));
                                        toData.setValue(torow, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "Sample");
                                        toData.setValue(torow, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "1");
                                        fromAllData.setValue(fromrow, MAPPED_COLUMNID, "Y");
                                        toData.setValue(torow, FROMROW_COLUMNID, fromAllData.getValue(fromrow, ROWID_COLUMNID, "" + fromrow));
                                        toData.setValue(torow, FROMAREA_COLUMNID, area.toString());
                                        continue;
                                    }
                                    if (!fromAllData.isValidColumn("reagentlotid")) continue;
                                    String toType = toData.getString(torow, policyDef.getTranslate("Function"), "");
                                    String currid = toData.getValue(torow, limssampleidcol, "");
                                    if (currid.length() > 0) {
                                        error = tp.translate("Cannot map Reagent Lot ") + (torow + 1) + tp.translate(" already mapped to ") + currid;
                                    }
                                    if (toType.indexOf(policyDef.getTranslate("Standard")) == -1 && toType.indexOf(policyDef.getTranslate("Control")) == -1) {
                                        error = tp.translate("Cannot map a reagent lot to a Sample Set Line with Function ") + toType;
                                        continue;
                                    }
                                    toData.setValue(torow, limssampleidcol, fromAllData.getValue(fromrow, "reagentlotid", ""));
                                    toData.setValue(torow, limsdatasetkeycol, fromAllData.getString(fromrow, limsdatasetkeycol, ""));
                                    toData.setValue(torow, FROMROW_COLUMNID, fromAllData.getValue(fromrow, ROWID_COLUMNID, "" + fromrow));
                                    toData.setValue(torow, FROMAREA_COLUMNID, area.toString());
                                    toData.setValue(torow, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "LV_ReagentLot");
                                    toData.setValue(torow, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "0");
                                }
                            }
                        } else {
                            String[] cols = toData.getColumns();
                            error = tp.translate("No valid ") + limssampleidcol + tp.translate(" column found.");
                            for (int i = 0; i < cols.length; ++i) {
                                error = error + tp.translate(" check column:") + cols[i];
                            }
                        }
                    } else {
                        error = tp.translate("Please select ") + fromSelection.length + tp.translate(" lines to map to.");
                    }
                } else {
                    error = tp.translate("Please select ") + fromSelection.length + tp.translate(" lines to map to.");
                }
            } else {
                error = tp.translate("Please select one or more items to map from.");
            }
        } else {
            error = tp.translate("Could not obtain sampleset line data.");
        }
        return error;
    }

    public static String doMapAll(DataSet fromData, DataSet toData, DownloadMappingPageArea.PageArea area, EmpowerPolicyDef policyDef) {
        String error = "";
        String limssampleidcol = policyDef.getEmpowerCoreMapping("empowerlimssampleid");
        String limsdatasetkeycol = policyDef.getEmpowerCoreMapping("empowerlimsdatasetkey");
        String excludeflagcol = policyDef.getEmpowerCoreMapping("empowerexcludeflag");
        String sampletypecol = "SampleType".replaceAll(" ", "_space_");
        int prevstandard = -1;
        int prevunknown = -1;
        int prevcontrol = -1;
        DataSet fromFiltered = DownloadMappingPage.getFilteredData(fromData);
        if (fromData.isValidColumn(limssampleidcol)) {
            for (int fromrow = 0; fromrow < fromData.getRowCount(); ++fromrow) {
                String fromSampleType = fromData.getString(fromrow, sampletypecol, "");
                String fromLevel = fromData.getString(fromrow, "Level", "");
                int torow = -1;
                if (fromSampleType.equals("Standard")) {
                    prevstandard = torow = DownloadMappingPage.determineToRow(policyDef, toData, fromSampleType, fromLevel, prevstandard, excludeflagcol, false);
                } else if (fromSampleType.equals("Control")) {
                    prevcontrol = torow = DownloadMappingPage.determineToRow(policyDef, toData, fromSampleType, fromLevel, prevcontrol, excludeflagcol, false);
                } else {
                    prevunknown = torow = DownloadMappingPage.determineToRow(policyDef, toData, fromSampleType, fromLevel, prevunknown, excludeflagcol, false);
                }
                if (torow < 0) {
                    error = error + "<P>Could not find a position for sample at position:" + fromrow;
                    return error;
                }
                toData.setValue(torow, limssampleidcol, fromData.getString(fromrow, limssampleidcol, ""));
                toData.setValue(torow, limsdatasetkeycol, fromData.getString(fromrow, limsdatasetkeycol, ""));
                fromData.setValue(fromrow, MAPPED_COLUMNID, "Y");
                int filteredrow = fromFiltered.findRow(ROWID_COLUMNID, "" + fromrow);
                toData.setValue(torow, FROMROW_COLUMNID, "" + filteredrow);
                toData.setValue(torow, FROMAREA_COLUMNID, area.toString());
                toData.setValue(torow, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "Sample");
                toData.setValue(torow, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "1");
            }
        } else if (fromData.isValidColumn("reagentlotid")) {
            for (int fromrow = 0; fromrow < fromData.getRowCount(); ++fromrow) {
                int torow;
                String fromLevel = fromData.getString(fromrow, "Level", "");
                prevstandard = torow = DownloadMappingPage.determineToRow(policyDef, toData, "Standard", fromLevel, prevstandard, excludeflagcol, true);
                if (torow < 0) {
                    error = error + "<P>Could not find a position for Reagent Lot";
                    return error;
                }
                toData.setValue(torow, limssampleidcol, fromData.getValue(fromrow, "reagentlotid", "(empty)"));
                fromData.setValue(fromrow, MAPPED_COLUMNID, "Y");
                toData.setValue(torow, FROMROW_COLUMNID, fromData.getValue(fromrow, ROWID_COLUMNID, "" + fromrow));
                toData.setValue(torow, FROMAREA_COLUMNID, area.toString());
                toData.setValue(torow, policyDef.getEmpowerCoreMapping("empowerlimssdcid"), "LV_ReagentLot");
                toData.setValue(torow, policyDef.getEmpowerCoreMapping("empoweruploadflag"), "0");
            }
        } else {
            error = error + "From Data is neither a sample or reagent lot";
        }
        return error;
    }

    private static int determineToRow(EmpowerPolicyDef policyDef, DataSet toData, String sampleType, String level, int prevrow, String excludeflagcol, boolean isReagent) {
        int startrow;
        for (int currrow = startrow = prevrow + 1; currrow < toData.getRowCount(); ++currrow) {
            String currSampleType = toData.getValue(currrow, policyDef.getTranslate("Function"), "");
            String currExcludeFlag = toData.getValue(currrow, excludeflagcol, "N");
            if (!currExcludeFlag.startsWith("N") || !DownloadMappingPage.matchSampleTypes(policyDef, sampleType, currSampleType, isReagent)) continue;
            return currrow;
        }
        return -1;
    }

    public String validateColumns(String samplesetmethodcols, String resultcols, String peakcols) {
        if (samplesetmethodcols == null && samplesetmethodcols.length() == 0) {
            return "samplesetmethodcols is null";
        }
        if (resultcols == null && resultcols.length() == 0) {
            return "resultcols is null";
        }
        if (peakcols == null && peakcols.length() == 0) {
            return "peakcols is null";
        }
        try {
            PropertyList peakColsPL;
            PropertyList resultColsPL;
            PropertyList sampleSetMethodColsPL;
            String limsqcbatchidcol = this.policyDef.getEmpowerCoreMapping("empowerqcbatchid");
            String limsidcol = this.policyDef.getEmpowerCoreMapping("empowerlimssampleid");
            String limsdskeycol = this.policyDef.getEmpowerCoreMapping("empowerlimsdatasetkey");
            String limssdcidcol = this.policyDef.getEmpowerCoreMapping("empowerlimssdcid");
            String limsuploadflagcol = this.policyDef.getEmpowerCoreMapping("empoweruploadflag");
            String limsexcludeflagcol = this.policyDef.getEmpowerCoreMapping("empowerexcludeflag");
            String limsnamedpeakresultcol = this.policyDef.getNamedPeakResultColumn();
            String limsunnamedpeakresultcol = this.policyDef.getUnknownPeakResultColumn();
            try {
                sampleSetMethodColsPL = new PropertyList(new JSONObject(samplesetmethodcols));
                resultColsPL = new PropertyList(new JSONObject(resultcols));
                peakColsPL = new PropertyList(new JSONObject(peakcols));
            }
            catch (Exception e) {
                return "Could not create column property lists from JSON.";
            }
            if (sampleSetMethodColsPL == null) {
                throw new SapphireException("Failed to create sampleSetMethodColsPL");
            }
            if (resultColsPL == null) {
                throw new SapphireException("Failed to create resultColsPL");
            }
            if (peakColsPL == null) {
                throw new SapphireException("Failed to create peakColsPL");
            }
            PropertyListCollection c = sampleSetMethodColsPL.getCollection("fields");
            if (c == null) {
                throw new SapphireException("Failed to get fields collection from sampleSetMethodColsPL");
            }
            if (c.find("name", limsqcbatchidcol) == null) {
                return this.getTranslationProcessor().translate("SampleSetMethod does not have column:") + limsqcbatchidcol;
            }
            PropertyListCollection resultCols = resultColsPL.getCollection("fields");
            if (resultCols == null) {
                throw new SapphireException("Failed to get fields collection from sampleSetMethodColsPL");
            }
            if (resultCols.find("name", limsidcol) == null) {
                return this.getTranslationProcessor().translate("SampleSetLine is missing the field:") + limsidcol;
            }
            if (resultCols.find("name", limsdskeycol) == null) {
                return this.getTranslationProcessor().translate("SampleSetLine is missing the field:") + limsdskeycol;
            }
            if (resultCols.find("name", limssdcidcol) == null) {
                return this.getTranslationProcessor().translate("SampleSetLine is missing the field:") + limssdcidcol;
            }
            if (resultCols.find("name", limsuploadflagcol) == null) {
                return this.getTranslationProcessor().translate("SampleSetLine is missing the field: ") + limsuploadflagcol;
            }
            if (resultCols.find("name", limsexcludeflagcol) == null) {
                return this.getTranslationProcessor().translate("SampleSetLine is missing the field:") + limsexcludeflagcol;
            }
            PropertyListCollection peaksCols = peakColsPL.getCollection("fields");
            if (peaksCols == null) {
                throw new SapphireException("Peaks propetylist does not have fields collection");
            }
            if (peaksCols.find("name", this.policyDef.getTranslate(limsunnamedpeakresultcol)) == null) {
                return this.getTranslationProcessor().translate("Peaks does not have :") + limsunnamedpeakresultcol;
            }
            if (peaksCols.find("name", this.policyDef.getTranslate(limsnamedpeakresultcol)) == null) {
                return this.getTranslationProcessor().translate("Peaks does not have :") + limsnamedpeakresultcol;
            }
        }
        catch (Exception e) {
            return this.getTranslationProcessor().translate("Error:") + e.getMessage();
        }
        return "";
    }

    public static enum MappingMode {
        AQCMode,
        CandidateMode;

    }
}

