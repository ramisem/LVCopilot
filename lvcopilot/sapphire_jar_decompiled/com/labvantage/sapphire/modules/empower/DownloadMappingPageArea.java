/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.modules.empower.DownloadMappingPage;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.maint.DataView;
import javax.servlet.jsp.PageContext;
import sapphire.pageelements.BaseElement;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DownloadMappingPageArea
extends BaseElement {
    private int[] selectedItems = new int[0];
    private PageArea pageArea = PageArea.QCBatch;
    private EmpowerPolicyDef policyDef;
    private String areaTitle = "";
    private DataSet tableData;

    public DownloadMappingPageArea(PageArea area, DataSet areaData, PageContext pageContext, EmpowerPolicyDef policyDef, String areaTitle) {
        this.pageContext = pageContext;
        this.pageArea = area;
        this.tableData = areaData;
        this.policyDef = policyDef;
        this.areaTitle = areaTitle;
    }

    public String getTitle() {
        return this.areaTitle;
    }

    public void setTitle(String title) {
        this.areaTitle = title;
    }

    public void setSelected(int[] selected) {
        this.selectedItems = selected == null ? new int[]{} : selected;
    }

    public int[] getSelected() {
        return this.selectedItems == null ? new int[]{} : this.selectedItems;
    }

    public void clearSelected() {
    }

    public DataSet getTableData() {
        return this.tableData;
    }

    public void setTableData(DataSet data) {
        this.tableData = data;
    }

    public PageArea getPageArea() {
        return this.pageArea;
    }

    public void setPageArea(PageArea area) {
        this.pageArea = area;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        switch (this.pageArea) {
            case QCBatch: {
                this.renderQCBatchHtml(html, this.areaTitle);
                break;
            }
            case QCBatchSampleTypes: {
                this.renderQCBatchSampleTypesHtml(html, this.areaTitle);
                break;
            }
            case Reagents: {
                this.renderReagentsHtml(html, this.areaTitle);
                break;
            }
            case UnknownSamples: {
                this.renderUnknownSamplesHtml(html, this.areaTitle);
                break;
            }
            case SampleSetMethod: {
                this.renderSampleSetMethodHtml(html, this.areaTitle);
                break;
            }
            default: {
                this.renderErrorHtml(html, this.getTranslationProcessor().translate("Unknown page area type."));
            }
        }
        return html.toString();
    }

    private void renderErrorHtml(StringBuffer html, String msg) {
        html.append("<br><font color=\"red\">").append(this.getTranslationProcessor().translate(msg)).append("</font>");
    }

    private void renderUnknownSamplesHtml(StringBuffer html, String areaTitle) {
        html.append(this.getTranslationProcessor().translate(areaTitle));
        if (this.tableData != null) {
            html.append("<table style=\"table-layout:fixed;width:100%;\"><tbody>");
            html.append("<tr><td>");
            html.append("<div class=\"innergrid\" style=\"\">");
            DataSet usedata = DownloadMappingPage.getFilteredData(this.tableData);
            this.logger.debug("Mapped rows = " + (this.tableData.getRowCount() - usedata.getRowCount()));
            DataView dataView = new DataView(this.pageContext, this.pageArea.toString(), usedata, "[default]", this.getConnectionId());
            dataView.getSDIInfo().setSdcid("Sample");
            PropertyList dataViewProperties = this.createDataViewProperties(this.pageArea, this.pageArea.toString());
            PropertyList plno = this.addUnkSampleDataViewColumn(dataViewProperties, "Position", "No.", 30);
            plno.setProperty("pseudocolumn", "<div class='dm_unknown dm_draggable' style='width:30px;' pagearea='" + PageArea.UnknownSamples.toString() + "' rowid='[rowid]'>[columnid]<img id='" + PageArea.UnknownSamples.toString() + "_drag[rowid]' src='WEB-CORE/images/png/Preparation.png' title=''></div>");
            this.addUnkSampleDataViewColumn(dataViewProperties, this.policyDef.getEmpowerCoreMapping("empowerlimssampleid"), "Sample", 100);
            this.addUnkSampleDataViewColumn(dataViewProperties, "workitemid", "TestMethod", 150);
            this.addUnkSampleDataViewColumn(dataViewProperties, "sampledesc", "Description", 200);
            this.addUnkSampleDataViewColumn(dataViewProperties, "SampleWeight", "Weight", 20);
            dataView.setElementProperties(dataViewProperties);
            dataView.setElementid(this.pageArea.toString());
            dataView.setKeyCols(new String[]{this.policyDef.getEmpowerCoreMapping("empowerlimssampleid")});
            dataView.setRenderTagsJS(false);
            html.append(dataView.getHtml());
            html.append("</div>");
            html.append("</td></tr>");
            html.append("<tr><td>");
            this.renderMappingButtons(html, this.pageArea);
            html.append("</td></tr>");
            html.append("</tbody></table>");
        } else {
            html.append("<table style=\"table-layout:fixed;width:100%;\"><tbody>");
            html.append("<tr><td>");
            html.append("<div class=\"innergrid\" style=\"\">");
            html.append("</div>");
            html.append("</td></tr>");
            html.append("<tr><td>");
            this.renderMappingButtons(html, this.pageArea);
            html.append("</td></tr>");
            html.append("</tbody></table>");
        }
    }

    private PropertyList createDataViewProperties(PageArea pageArea, String dataset) {
        PropertyList properties = new PropertyList();
        properties.setProperty("style", "GridWithCheckBox");
        properties.setProperty("dataset", dataset);
        properties.setId(pageArea.toString());
        properties.setProperty("elementid", pageArea.toString());
        PropertyListCollection columns = new PropertyListCollection();
        properties.setProperty("columns", columns);
        return properties;
    }

    private PropertyList addQCBatchDataViewColumn(PropertyList dataViewProperties, String columnId, String columnTitle, int width) {
        if (width < 1) {
            width = 100;
        }
        PropertyList pl = this.addDataViewColumn(dataViewProperties, columnId, columnTitle, 0);
        pl.setProperty("pseudocolumn", "$G{ (QCBatch.sampletype.indexOf( \"Standard\" ) > -1 || QCBatch.sampletype.indexOf( \"Control\" ) > -1 ) ? \"<div style='color:green;width:" + width + "px;'>[columnid]</div>\": \"<div style='color:blue;width:" + width + "px;'>[columnid]</div>\"}");
        return pl;
    }

    private PropertyList addSampleSetDataViewColumn(PropertyList dataViewProperties, String columnId, String columnTitle, int width, String excludeFlagCol, String limscol) {
        if (width < 1) {
            width = 100;
        }
        PropertyList pl = this.addDataViewColumn(dataViewProperties, columnId, columnTitle, 0);
        if (columnId.equalsIgnoreCase("NumOfInjs")) {
            pl.setProperty("pseudocolumn", "$G{SampleSetMethod.numofinjs < 1 ? \"<div style='color:gray;width:" + width + "px;'>\" + (SampleSetMethod.numofinjs < 1 ? \"\" : \"[columnid]\") + \"</div>\" : ( SampleSetMethod." + this.policyDef.getTranslate("Function").toLowerCase() + ".indexOf(\"" + this.policyDef.getTranslate("Inject Samples") + "\") > -1 ? \"<div style='color:blue;width:" + width + "px;'>\" + (SampleSetMethod.numofinjs < 1 ? \"\" : \"[columnid]\") + \"</div>\" : \"<div span style='color:green;width:" + width + "px;'>\" + (SampleSetMethod.numofinjs < 1 ? \"\" : \"[columnid]\") + \"</div>\" )}");
        } else if (columnId.equalsIgnoreCase("__fromrow")) {
            pl.setProperty("pseudocolumn", "$G{SampleSetMethod.numofinjs < 1 || SampleSetMethod." + excludeFlagCol + ".startsWith( 'Y' ) ? \"<div style='color:gray;width:" + width + "px;font-weight:bold;'>\" +  ( ( SampleSetMethod." + "__fromrow" + " != null && SampleSetMethod." + "__fromrow" + ".length() > 0 ) ? (Integer.parseInt(SampleSetMethod." + "__fromrow" + ") + 1) : \"\" ) + \"</div>\" : ( SampleSetMethod." + this.policyDef.getTranslate("Function").toLowerCase() + ".indexOf(\"Sample\") > -1 ? \"<div style='color:blue;width:" + width + "px;font-weight:bold;'>\" +  ( ( SampleSetMethod." + "__fromrow" + " != null && SampleSetMethod." + "__fromrow" + ".length() > 0 ) ? (Integer.parseInt(SampleSetMethod." + "__fromrow" + ") + 1) : \"\" ) + \"</div>\" : \"<div span style='color:green;width:" + width + "px;font-weight:bold;'>\" +  ( ( SampleSetMethod." + "__fromrow" + " != null && SampleSetMethod." + "__fromrow" + ".length() > 0 ) ? (Integer.parseInt(SampleSetMethod." + "__fromrow" + ") + 1) : \"\" ) + \"</div>\" )}");
        } else if (columnId.equalsIgnoreCase(limscol)) {
            pl.setProperty("pseudocolumn", "$G{SampleSetMethod.numofinjs < 1 ? \"<div style='color:gray;width:" + width + "px;font-weight:bold;'>[columnid]</div>\" : ( SampleSetMethod." + this.policyDef.getTranslate("Function").toLowerCase() + ".indexOf(\"" + this.policyDef.getTranslate("Inject Samples") + "\") > -1 ? \"<div style='color:blue;width:" + width + "px;font-weight:bold;'>[columnid]</div>\" : \"<div span style='color:green;width:" + width + "px;font-weight:bold;'>[columnid]</div>\" )}");
        } else {
            pl.setProperty("pseudocolumn", "$G{SampleSetMethod.numofinjs < 1 || SampleSetMethod." + excludeFlagCol + ".startsWith( 'Y' ) ? \"<div style='color:gray;width:" + width + "px;'>[columnid]</div>\" : ( SampleSetMethod." + this.policyDef.getTranslate("Function").toLowerCase() + ".indexOf(\"" + this.policyDef.getTranslate("Inject Samples") + "\") > -1 ? \"<div style='color:blue;width:" + width + "px;'>[columnid]</div>\" : \"<div span style='color:green;width:" + width + "px;'>[columnid]</div>\" )}");
        }
        return pl;
    }

    private PropertyList addReagentDataViewColumn(PropertyList dataViewProperties, String columnId, String columnTitle, int width) {
        if (width < 1) {
            width = 100;
        }
        PropertyList pl = this.addDataViewColumn(dataViewProperties, columnId, columnTitle, 0);
        pl.setProperty("pseudocolumn", "<div style='color:green;width:" + width + "px;'>[columnid]</div>");
        return pl;
    }

    private PropertyList addUnkSampleDataViewColumn(PropertyList dataViewProperties, String columnId, String columnTitle, int width) {
        if (width < 1) {
            width = 100;
        }
        PropertyList pl = this.addDataViewColumn(dataViewProperties, columnId, columnTitle, 0);
        pl.setProperty("pseudocolumn", "<div style='color:blue;width:" + width + "px;'>[columnid]</div>");
        return pl;
    }

    private PropertyList addDataViewColumn(PropertyList dataViewProperties, String columnId, String columnTitle, int width) {
        PropertyListCollection columns = dataViewProperties.getCollection("columns");
        if (columns == null) {
            columns = new PropertyListCollection();
            dataViewProperties.setProperty("columns", columns);
        }
        PropertyList column = new PropertyList();
        column.setProperty("id", columnId);
        column.setProperty("columnid", columnId);
        column.setProperty("title", this.getTranslationProcessor().translate(columnTitle));
        column.setProperty("mode", "readonly");
        if (width > 0) {
            column.setProperty("size", "" + width);
        }
        columns.add(column);
        return column;
    }

    private void renderQCBatchHtml(StringBuffer html, String title) {
        html.append("<B>").append(this.getTranslationProcessor().translate(title)).append("</B>");
        if (this.tableData != null) {
            html.append("<table style=\"table-layout:fixed;width:100%;\"><tbody>");
            html.append("<tr><td>");
            html.append("<div class=\"innergrid\" style=\"\">");
            DataSet usedata = DownloadMappingPage.getFilteredData(this.tableData);
            this.logger.debug("Mapped rows = " + (this.tableData.getRowCount() - usedata.getRowCount()));
            DataView dataView = new DataView(this.pageContext, this.pageArea.toString(), usedata, "[default]", this.getConnectionId());
            dataView.getSDIInfo().setSdcid("QCBatch");
            PropertyList dataViewProperties = this.createDataViewProperties(this.pageArea, this.pageArea.toString());
            PropertyList plno = this.addQCBatchDataViewColumn(dataViewProperties, "Sample Position".replaceAll(" ", "_space_"), "No.", 30);
            plno.setProperty("pseudocolumn", "$G{( QCBatch.sampletype.indexOf( \"Standard\" ) > -1 || QCBatch.sampletype.indexOf( \"Control\" ) > -1  )? \"<div style='width:30px;' class='dm_qcbatchstandard dm_draggable' pagearea='" + PageArea.QCBatch.toString() + "' rowid='[rowid]'>[columnid]<img id='" + PageArea.QCBatch.toString() + "_drag[rowid]' src='WEB-CORE/images/png/Preparation.png' title=''></div>\": \"<div class='dm_qcbatchunknown dm_draggable' style='width:" + 30 + "px;' pagearea='" + PageArea.QCBatch.toString() + "' rowid='[rowid]'>[columnid]<img id='" + PageArea.QCBatch.toString() + "_drag[rowid]' src='WEB-CORE/images/png/Preparation.png' title=''></div>\"}");
            this.addQCBatchDataViewColumn(dataViewProperties, "SampleType".replaceAll(" ", "_space_"), "QCType", 100);
            this.addQCBatchDataViewColumn(dataViewProperties, "Level", "Level", 30);
            this.addQCBatchDataViewColumn(dataViewProperties, this.policyDef.getEmpowerCoreMapping("empowerlimssampleid"), "Sample", 125);
            this.addQCBatchDataViewColumn(dataViewProperties, "SampleWeight", "Weight", 50);
            dataView.setElementProperties(dataViewProperties);
            dataView.setElementid(this.pageArea.toString());
            dataView.setKeyCols(new String[]{"eu_LIMSSampleID"});
            dataView.setRenderTagsJS(false);
            html.append(dataView.getHtml());
            html.append("</div>");
            html.append("</td></tr>");
            html.append("<tr><td>");
            this.renderMappingButtons(html, this.pageArea);
            html.append("</td></tr>");
            html.append("</tbody></table>");
        } else {
            this.renderErrorHtml(html, "No qc batch data.");
        }
    }

    private void renderMappingButtons(StringBuffer html, PageArea pageArea) {
        Button btn;
        String textprefix;
        String prefix;
        String image;
        html.append("<table class=\"mapbuttons\" style=\"\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\"><tbody><tr>");
        html.append("<td align=\"left\">");
        if (pageArea == PageArea.SampleSetMethod) {
            image = "Back";
            prefix = "doUnm";
            textprefix = "Unm";
        } else {
            image = "Forward";
            prefix = "doM";
            textprefix = "M";
        }
        if (pageArea == PageArea.Reagents || pageArea == PageArea.UnknownSamples) {
            btn = new Button(this.pageContext);
            btn.setText(this.getTranslationProcessor().translate("  Add  "));
            btn.setImg("WEB-CORE/images/png/AddRow.png");
            btn.setId(pageArea.toString() + "_add");
            btn.setTip("Add new item");
            btn.setAction("downloadMappingPage.doAdd( '" + pageArea.toString() + "')");
            html.append(btn.getHtml());
            html.append("&nbsp;&nbsp;");
            html.append("&nbsp;&nbsp;");
        }
        btn = new Button(this.pageContext);
        btn.setText(this.getTranslationProcessor().translate("  " + textprefix + "ap Selected  "));
        btn.setImg("WEB-CORE/images/png/" + image + ".png");
        btn.setId(pageArea.toString() + "_map");
        btn.setTip(this.getTranslationProcessor().translate(textprefix + "ap Selected Items"));
        btn.setAction("downloadMappingPage." + prefix + "ap( '" + pageArea.toString() + "')");
        html.append(btn.getHtml());
        html.append("&nbsp;&nbsp;");
        if (pageArea != PageArea.Reagents && pageArea != PageArea.QCBatchSampleTypes) {
            btn = new Button(this.pageContext);
            btn.setText(this.getTranslationProcessor().translate(textprefix + "ap All"));
            btn.setImg("WEB-CORE/images/png/" + image + ".png");
            btn.setId(pageArea.toString() + "_mapall");
            btn.setTip(this.getTranslationProcessor().translate(textprefix + "ap All Items"));
            btn.setAction("downloadMappingPage." + prefix + "apAll( '" + pageArea.toString() + "')");
            html.append(btn.getHtml());
        }
        html.append("</td>");
        html.append("</tr></tbody></table>");
    }

    private void renderReagentsHtml(StringBuffer html, String areaTitle) {
        html.append("<B>").append(this.getTranslationProcessor().translate(areaTitle)).append("</B>");
        if (this.tableData != null) {
            for (int i = 0; i < this.tableData.getRowCount(); ++i) {
                this.tableData.setNumber(i, "Position", i + 1);
            }
            html.append("<table style=\"table-layout:fixed;width:100%;\"><tbody>");
            html.append("<tr><td>");
            html.append("<div class=\"innergrid\" style=\"\">");
            DataSet usedata = DownloadMappingPage.getFilteredData(this.tableData);
            this.logger.debug("Mapped rows = " + (this.tableData.getRowCount() - usedata.getRowCount()));
            DataView dataView = new DataView(this.pageContext, this.pageArea.toString(), usedata, "[default]", this.getConnectionId());
            dataView.getSDIInfo().setSdcid("LV_ReagentLot");
            PropertyList dataViewProperties = this.createDataViewProperties(this.pageArea, this.pageArea.toString());
            PropertyList plno = this.addReagentDataViewColumn(dataViewProperties, "Position", "No.", 30);
            plno.setProperty("pseudocolumn", "<div  class='dm_reagents dm_draggable'  style='width:30px;'  pagearea='" + PageArea.Reagents.toString() + "' rowid='[rowid]'>[columnid]<img id='" + PageArea.Reagents.toString() + "_drag[rowid]' src='WEB-CORE/images/png/Preparation.png' title=''></div>");
            this.addReagentDataViewColumn(dataViewProperties, "reagenttypeid", "Reagent Type", 125);
            this.addReagentDataViewColumn(dataViewProperties, "reagentlotid", "Reagent Lot", 125);
            this.addReagentDataViewColumn(dataViewProperties, "reagentlotdesc", "Description", 125);
            dataView.setElementProperties(dataViewProperties);
            dataView.setElementid(this.pageArea.toString());
            dataView.setKeyCols(new String[]{"reagentlotid"});
            dataView.setRenderTagsJS(false);
            html.append(dataView.getHtml());
            html.append("</div>");
            html.append("</td></tr>");
            html.append("<tr><td>");
            this.renderMappingButtons(html, this.pageArea);
            html.append("</td></tr>");
            html.append("</tbody></table>");
        } else {
            html.append("<table style=\"table-layout:fixed;width:100%;\"><tbody>");
            html.append("<tr><td>");
            html.append("<div class=\"innergrid\" style=\"\">");
            html.append("</div>");
            html.append("</td></tr>");
            html.append("<tr><td>");
            this.renderMappingButtons(html, this.pageArea);
            html.append("</td></tr>");
            html.append("</tbody></table>");
        }
    }

    private void renderQCBatchSampleTypesHtml(StringBuffer html, String areaTitle) {
        html.append("<B>").append(this.getTranslationProcessor().translate(areaTitle)).append("</B>");
        if (this.tableData != null) {
            for (int i = 0; i < this.tableData.getRowCount(); ++i) {
                this.tableData.setNumber(i, "Position", i + 1);
            }
            html.append("<table style=\"table-layout:fixed;width:100%;\"><tbody>");
            html.append("<tr><td>");
            html.append("<div class=\"innergrid\" style=\"\">");
            DataSet usedata = DownloadMappingPage.getFilteredData(this.tableData);
            this.logger.debug("Mapped rows = " + (this.tableData.getRowCount() - usedata.getRowCount()));
            DataView dataView = new DataView(this.pageContext, this.pageArea.toString(), usedata, "[default]", this.getConnectionId());
            dataView.getSDIInfo().setSdcid("QCBatchSampleType");
            PropertyList dataViewProperties = this.createDataViewProperties(this.pageArea, this.pageArea.toString());
            PropertyList plno = this.addReagentDataViewColumn(dataViewProperties, "Position", "No.", 30);
            plno.setProperty("pseudocolumn", "<div class='dm_qcsampletype dm_draggable' style='width:30px;'  pagearea='" + PageArea.QCBatchSampleTypes.toString() + "' rowid='[rowid]'>[columnid]<img id='" + PageArea.QCBatchSampleTypes.toString() + "_drag[rowid]' src='WEB-CORE/images/png/Preparation.png' title=''></div>");
            this.addReagentDataViewColumn(dataViewProperties, "QCSampleType", "QCType", 100);
            this.addReagentDataViewColumn(dataViewProperties, "Level", "Level", 30);
            this.addReagentDataViewColumn(dataViewProperties, "reagenttypeid", "Reagent Type", 125);
            this.addReagentDataViewColumn(dataViewProperties, "reagentlotid", "Reagent Lot", 125);
            dataView.setElementProperties(dataViewProperties);
            dataView.setElementid(this.pageArea.toString());
            dataView.setKeyCols(new String[]{"reagentlotid"});
            dataView.setRenderTagsJS(false);
            html.append(dataView.getHtml());
            html.append("</div>");
            html.append("</td></tr>");
            html.append("<tr><td>");
            this.renderMappingButtons(html, this.pageArea);
            html.append("</td></tr>");
            html.append("</tbody></table>");
        } else {
            this.renderErrorHtml(html, "No reagent data.");
        }
    }

    private void renderSampleSetMethodHtml(StringBuffer html, String title) {
        html.append("<B>").append(this.getTranslationProcessor().translate(title)).append("</B>");
        if (this.tableData != null) {
            for (int i = 0; i < this.tableData.getRowCount(); ++i) {
                this.tableData.setNumber(i, "Position", i + 1);
            }
            html.append("<table style=\"table-layout:fixed;width:100%;\"><tbody>");
            html.append("<tr><td>");
            html.append("<div class=\"innergrid\" style=\"\">");
            DataView dataView = new DataView(this.pageContext, this.pageArea.toString(), this.tableData, "[default]", this.getConnectionId());
            PropertyList dataViewProperties = this.createDataViewProperties(this.pageArea, this.pageArea.toString());
            String excludeFlagCol = this.policyDef.getEmpowerCoreMapping("empowerexcludeflag").toLowerCase();
            String limscol = this.policyDef.getEmpowerCoreMapping("empowerlimssampleid").toLowerCase();
            PropertyList plno = this.addSampleSetDataViewColumn(dataViewProperties, "Position", "No.", 30, excludeFlagCol, limscol);
            plno.setProperty("pseudocolumn", "$G{ ( SampleSetMethod.numofinjs < 1 || SampleSetMethod.__fromrow || SampleSetMethod." + excludeFlagCol + ".startsWith( 'Y' ) ) ? \"<div style='width:" + 30 + "px;' class='dm_ssmstandard dm_nontarget' pagearea='" + PageArea.SampleSetMethod.toString() + "' rowid='[rowid]'>[columnid]</div>\" : ( SampleSetMethod." + this.policyDef.getTranslate("Function").toLowerCase() + ".indexOf(\"" + this.policyDef.getTranslate("Inject Samples") + "\") > -1 ? \"<div class='dm_ssmstandard dm_target' style='cursor:crosshair;color:blue;width:" + 30 + "px;' pagearea='" + PageArea.SampleSetMethod.toString() + "' rowid='[rowid]'>[columnid]<img id='" + PageArea.SampleSetMethod.toString() + "_drop[rowid]' src='WEB-CORE/images/png/MarkEmpty.png' title=''></div>\" : \"<div span style='width:" + 30 + "px;' class='dm_ssmunknown dm_target' pagearea='" + PageArea.SampleSetMethod.toString() + "' rowid='[rowid]'>[columnid]<img id='" + PageArea.SampleSetMethod.toString() + "_drop[rowid]' src='WEB-CORE/images/png/MarkEmpty.png' title=''></div>\" )}");
            this.addSampleSetDataViewColumn(dataViewProperties, this.policyDef.getTranslate("Function"), this.policyDef.getTranslate("Function"), 100, excludeFlagCol, limscol);
            this.addSampleSetDataViewColumn(dataViewProperties, "__fromrow", "From", 30, excludeFlagCol, limscol);
            this.addSampleSetDataViewColumn(dataViewProperties, this.policyDef.getTranslate("Level"), this.policyDef.getTranslate("Level"), 30, excludeFlagCol, limscol);
            this.addSampleSetDataViewColumn(dataViewProperties, limscol, "LIMS ID", 125, excludeFlagCol, limscol);
            DataSet additionalSSMCols = this.policyDef.getAdditionalSSMCols();
            if (additionalSSMCols != null && additionalSSMCols.getRowCount() > 0) {
                for (int i = 0; i < additionalSSMCols.getRowCount(); ++i) {
                    String ssl = additionalSSMCols.getValue(i, "sslcolumn", "");
                    int width = additionalSSMCols.getInt(i, "width", 100);
                    String ssltitle = additionalSSMCols.getValue(i, "title", "");
                    if (ssltitle.length() <= 0 && ssl.length() <= 0) continue;
                    this.addSampleSetDataViewColumn(dataViewProperties, ssl, ssltitle, width, excludeFlagCol, limscol);
                }
            }
            dataView.setElementProperties(dataViewProperties);
            dataView.setElementid(this.pageArea.toString());
            dataView.setKeyCols(new String[]{"SampleName"});
            dataView.setRenderTagsJS(false);
            html.append(dataView.getHtml());
            html.append("</div>");
            html.append("</td></tr>");
            html.append("<tr><td>");
            this.renderMappingButtons(html, this.pageArea);
            html.append("</td></tr>");
            html.append("</tbody></table>");
        } else {
            this.renderErrorHtml(html, "No Sample Set Line data.");
        }
    }

    public static void updateSampleSetLinesData(PropertyList sampleSetMethod, DataSet lines, String linescollection) {
        PropertyListCollection plclines = sampleSetMethod.getCollection(linescollection);
        if (lines.getRowCount() == plclines.size()) {
            for (int row = 0; row < lines.getRowCount(); ++row) {
                PropertyList line = plclines.getPropertyList(row);
                PropertyListCollection fields = line.getCollection("fields");
                if (fields == null) {
                    fields = new PropertyListCollection();
                    line.setProperty("fields", fields);
                } else {
                    for (int column = 0; column < lines.getColumnCount(); ++column) {
                        String fieldid = lines.getColumnId(column);
                        PropertyList field = fields.find("name", fieldid, true);
                        if (field != null) {
                            if (field.getProperty("type", "string").equalsIgnoreCase("enum")) {
                                field.setProperty("enumvalue", lines.getValue(row, fieldid, ""));
                                field.setProperty("value", "");
                                continue;
                            }
                            field.setProperty("value", lines.getValue(row, fieldid, ""));
                            continue;
                        }
                        field = new PropertyList();
                        field.setProperty("name", fieldid);
                        field.setProperty("type", "string");
                        field.setProperty("value", lines.getValue(row, fieldid, ""));
                        fields.add(field);
                    }
                }
                PropertyListCollection components = line.getCollection("components");
                if (components != null) continue;
                components = new PropertyListCollection();
                line.setProperty("components", components);
            }
        }
    }

    public static DataSet getSampleSetLinesData(ConnectionInfo connectionInfo, PropertyList sampleSetMethod, String linescollection) {
        DataSet sampleSetLines = new DataSet(connectionInfo);
        PropertyListCollection lines = sampleSetMethod.getCollection(linescollection);
        if (lines != null) {
            for (int i = 0; i < lines.size(); ++i) {
                PropertyList line = lines.getPropertyList(i);
                int row = sampleSetLines.addRow();
                for (Object o : line.keySet()) {
                    String propertyId = o.toString();
                    Object objectValue = line.get(propertyId);
                    if (objectValue instanceof String) {
                        String columnId = propertyId.toLowerCase();
                        if (!sampleSetLines.isValidColumn(columnId)) {
                            sampleSetLines.addColumn(columnId, 0);
                        }
                        sampleSetLines.setValue(row, columnId, objectValue.toString());
                        continue;
                    }
                    if (!(objectValue instanceof PropertyListCollection) || !propertyId.equalsIgnoreCase("fields")) continue;
                    PropertyListCollection fields = (PropertyListCollection)objectValue;
                    for (int k = 0; k < fields.size(); ++k) {
                        PropertyList field = fields.getPropertyList(k);
                        String columnId = field.getProperty("name").toLowerCase();
                        String type = field.getProperty("type").toLowerCase();
                        String value = field.getProperty("value");
                        if (type.equals("enum") && field.containsKey("enumvalue") && field.getProperty("enumvalue").length() > 0) {
                            value = field.getProperty("enumvalue");
                        }
                        if (!sampleSetLines.isValidColumn(columnId)) {
                            sampleSetLines.addColumn(columnId, type.equals("number") || type.equals("integer") ? 1 : 0);
                        }
                        if (type.equals("integer") && value.equals("-2147483648")) {
                            value = "";
                        } else if (type.equals("number") && value.equals("-50000")) {
                            value = "";
                        }
                        sampleSetLines.setValue(row, columnId, value);
                    }
                }
            }
            if (!sampleSetLines.isValidColumn("__fromrow")) {
                sampleSetLines.addColumn("__fromrow", 0);
            }
            if (!sampleSetLines.isValidColumn("__fromarea")) {
                sampleSetLines.addColumn("__fromarea", 0);
            }
        } else {
            Logger.logWarn("No lines in property list.");
        }
        return sampleSetLines;
    }

    public static enum PageArea {
        UnknownSamples,
        QCBatch,
        Reagents,
        QCBatchSampleTypes,
        SampleSetMethod;

    }
}

