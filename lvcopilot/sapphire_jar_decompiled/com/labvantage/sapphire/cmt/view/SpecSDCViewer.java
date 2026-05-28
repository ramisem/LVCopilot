/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.cmt.view;

import com.labvantage.sapphire.cmt.SDISnapshotItem;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class SpecSDCViewer
extends SDISnapshotViewer {
    static final String SPECLIMITTYPE_LIMITTYPEID_COL = "limittypeid";
    static final String SPECLIMITTYPE_CONDITION_COL = "condition";
    static final String SPECLIMITTYPE_LIMITTYPESEQUENCE_COL = "limittypesequence";
    static final String SPECPARAMITEMS_PARAMLISTID_COL = "paramlistid";
    static final String SPECPARAMITEMS_PARAMLISTVERSIONID_COL = "paramlistversionid";
    static final String SPECPARAMITEMS_VARIANTID_COL = "variantid";
    static final String SPECPARAMITEMS_PARAMID_COL = "paramid";
    static final String SPECPARAMITEMS_PARAMTYPE_COL = "paramtype";
    static final String SPECPARAMITEMS_DATATYPES_COL = "datatypes";
    static final String SPECPARAMLIMITS_OPERATOR_COL = "operator";
    static final String SPECPARAMLIMITS_VALUE_COL = "value";
    static final String SPECPARAMLIMITS_PARAMLISTID_COL = "paramlistid";
    static final String SPECPARAMLIMITS_PARAMLISTVERSIONID_COL = "paramlistversionid";
    static final String SPECPARAMLIMITS_VARIANTID_COL = "variantid";
    static final String SPECPARAMLIMITS_PARAMID_COL = "paramid";
    static final String SPECPARAMLIMITS_PARAMTYPE_COL = "paramtype";
    static final String SPECPARAMLIMITS_LIMITTYPESEQUENCE_COL = "limittypesequence";
    static final String BANDEDSPECTYPE = "B";
    static final String STRUCTUREDSPECTYPE = "S";

    @Override
    protected void renderItemDetailsDiff(ConfigReportContent configReportContent, SDISnapshotItem sourceItem, SDISnapshotItem refItem, boolean showAuditColumns, boolean showTranslation, boolean hideEmptyColumns) throws SapphireException {
        ConfigReportContent limitTypesContent = new ConfigReportContent("Limit Types", this.translationProcessor);
        String tablelabel = "Limit Types";
        String itemdisplay = "[Limit Type]";
        String[] keycols = new String[]{"Limit Type"};
        DataSet srcLimitTypes = new DataSet();
        if (sourceItem != null) {
            srcLimitTypes = this.getLimitTypes(sourceItem.getSDIData());
        }
        DataSet refLimitTypes = new DataSet();
        if (refItem != null) {
            refLimitTypes = this.getLimitTypes(refItem.getSDIData());
        }
        HashMap<String, String> columnTitleMap = new HashMap();
        if (srcLimitTypes.getRowCount() > 0 || refLimitTypes.getRowCount() > 0) {
            limitTypesContent.startSubSection(tablelabel, "");
            columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "speclimittype", srcLimitTypes.getColumns());
            limitTypesContent.renderDetailTablesDiff(columnTitleMap, "speclimittype", tablelabel, itemdisplay, srcLimitTypes, refLimitTypes, keycols, this.getTranslationProcessor(), hideEmptyColumns);
            configReportContent.appendNodeContent(limitTypesContent, "speclimittype", tablelabel);
        }
        DataSet sourceSpecData = new DataSet();
        if (sourceItem != null) {
            sourceSpecData = this.getSpecData(sourceItem.getSDIData(), this.translationProcessor);
        }
        DataSet refSpecData = new DataSet();
        if (refItem != null) {
            refSpecData = this.getSpecData(refItem.getSDIData(), this.translationProcessor);
        }
        DataSet srcspecparamitems = new DataSet();
        if (sourceItem != null) {
            srcspecparamitems = this.getParamDetails(sourceItem.getSDIData());
        }
        DataSet refspecparamitems = new DataSet();
        if (refItem != null) {
            refspecparamitems = this.getParamDetails(refItem.getSDIData());
        }
        if (srcspecparamitems.getRowCount() > 0 || refspecparamitems.getRowCount() > 0) {
            ConfigReportContent specparamitemscontent = new ConfigReportContent("Parameter Limits", this.translationProcessor);
            tablelabel = "Parameter Items";
            itemdisplay = "[Param List] (v[Version]) [Variant], [Param] ([Param Type])";
            keycols = new String[]{"Param List", "Version", "Variant", "Param", "Param Type"};
            columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "specparamitems", srcspecparamitems.getColumns());
            specparamitemscontent.startSubSection(tablelabel, "");
            specparamitemscontent.renderDetailTablesDiff(columnTitleMap, "specparamitems", tablelabel, itemdisplay, srcspecparamitems, refspecparamitems, keycols, this.getTranslationProcessor(), hideEmptyColumns);
            configReportContent.appendNodeContent(specparamitemscontent, "specparamitems", tablelabel);
        }
        if (sourceSpecData.getRowCount() > 0 || refSpecData.getRowCount() > 0) {
            ConfigReportContent paramLimitsInfo = new ConfigReportContent("Spec limits", this.translationProcessor);
            tablelabel = "Parameter Limits";
            itemdisplay = "[Param List] (v[Version]) [Variant], [Param] ([Param Type])";
            keycols = new String[]{"Param List", "Version", "Variant", "Param", "Param Type"};
            columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "specparamlimits", sourceSpecData.getColumns());
            paramLimitsInfo.startSubSection("Parameter Limits", "");
            paramLimitsInfo.renderDetailTablesDiff(columnTitleMap, "specparamlimits", tablelabel, itemdisplay, sourceSpecData, refSpecData, keycols, this.getTranslationProcessor(), hideEmptyColumns);
            configReportContent.appendNodeContent(paramLimitsInfo, "specparamlimits", tablelabel);
        }
        DataSet srcspecrules = new DataSet();
        if (sourceItem != null) {
            srcspecrules = this.getRules(sourceItem.getSDIData());
        }
        DataSet refspecrules = new DataSet();
        if (refItem != null) {
            refspecrules = this.getRules(refItem.getSDIData());
        }
        if (srcspecrules != null && srcspecrules.getRowCount() > 0 || refspecrules != null && refspecrules.getRowCount() > 0) {
            ConfigReportContent specrulecontent = new ConfigReportContent("Rules", this.translationProcessor);
            tablelabel = "Rules";
            itemdisplay = "[Rule No]";
            keycols = new String[]{"Rule No"};
            columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "specrule", srcspecrules.getColumns());
            specrulecontent.startSubSection(tablelabel, "");
            specrulecontent.renderDetailTablesDiff(columnTitleMap, "specrule", tablelabel, itemdisplay, srcspecrules, refspecrules, keycols, this.getTranslationProcessor(), hideEmptyColumns);
            configReportContent.appendNodeContent(specrulecontent, "specrule", tablelabel);
        }
        DataSet specrequiredparamlists = new DataSet();
        if (sourceItem != null) {
            specrequiredparamlists = this.getRequiredParamLists(sourceItem.getSDIData());
        }
        DataSet refrequiredparamlists = new DataSet();
        if (refItem != null) {
            refrequiredparamlists = this.getRequiredParamLists(refItem.getSDIData());
        }
        if (specrequiredparamlists != null && specrequiredparamlists.getRowCount() > 0 || refrequiredparamlists != null && refrequiredparamlists.getRowCount() > 0) {
            ConfigReportContent specreqpl = new ConfigReportContent("Required Param Lists", this.translationProcessor);
            tablelabel = "Required Parameter Lists";
            itemdisplay = "[Param List] (v[Version]) [Variant]";
            keycols = new String[]{"Param List", "Version", "Variant"};
            columnTitleMap = DDTLabelsUtil.getColumnTitleMap(this.getSDCProcessor(), "specparamlist", specrequiredparamlists.getColumns());
            specreqpl.startSubSection(tablelabel, "");
            specreqpl.renderDetailTablesDiff(columnTitleMap, "specparamlist", tablelabel, itemdisplay, specrequiredparamlists, refrequiredparamlists, keycols, this.getTranslationProcessor(), hideEmptyColumns);
            configReportContent.appendNodeContent(specreqpl, "specparamlist", tablelabel);
        }
        ConfigReportContent str = new ConfigReportContent("categories", this.translationProcessor);
        this.renderCategores(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendNodeContent(str, "categoryitem", "Categories");
        str = new ConfigReportContent("other", this.translationProcessor);
        this.renderOtherCommonDetails(str, sourceItem, refItem, showAuditColumns, showTranslation, hideEmptyColumns);
        configReportContent.appendSpecialContent(str);
    }

    public DataSet getRequiredParamLists(SDIData sdiData) {
        DataSet raw = sdiData.getDataset("specparamlist");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Param List", 0);
        ret.addColumn("Version", 0);
        ret.addColumn("Variant", 0);
        if (raw != null) {
            for (int i = 0; i < raw.getRowCount(); ++i) {
                ret.addRow();
                ret.setString(i, "Param List", raw.getValue(i, "paramlistid", ""));
                ret.setString(i, "Version", raw.getValue(i, "paramlistversionid", ""));
                ret.setString(i, "Variant", raw.getValue(i, "variantid", ""));
            }
        }
        return ret;
    }

    public DataSet getRules(SDIData sdiData) {
        DataSet raw = sdiData.getDataset("specrule");
        DataSet set = new DataSet();
        if (raw != null) {
            raw.sort("ruleno");
            set.setColidCaseSensitive(true);
            String ret = "";
            for (int i = 0; i < raw.getRowCount(); ++i) {
                ret = "";
                String[] cond = StringUtil.split(raw.getString(i, "ruledef", ""), ";");
                if (cond[0].length() > 0) {
                    ret = ret + "If there are any items set to " + cond[0];
                }
                if (cond.length > 1 && cond[1].length() > 0) {
                    ret = ret + " then set the spec condition to " + cond[1];
                    if (i != raw.getRowCount() - 1) {
                        // empty if block
                    }
                }
                set.addRow();
                set.setString(i, "Rule No", raw.getValue(i, "ruleno"));
                set.setString(i, "Rule", ret);
            }
        }
        return set;
    }

    public DataSet getLimitTypes(SDIData sdiData) {
        DataSet raw = sdiData.getDataset("speclimittype");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Limit Type", 0);
        ret.addColumn("Condition", 0);
        if (raw != null && raw.getRowCount() > 0) {
            for (int i = 0; i < raw.getRowCount(); ++i) {
                ret.addRow();
                ret.setString(i, "Limit Type", raw.getString(i, SPECLIMITTYPE_LIMITTYPEID_COL));
                ret.setString(i, "Condition", raw.getString(i, SPECLIMITTYPE_CONDITION_COL));
            }
        }
        return ret;
    }

    public DataSet getSpecData(SDIData specSDI, TranslationProcessor translationProcessor) {
        DataSet paramLimitData = new DataSet();
        ArrayList gridArrayList = new ArrayList();
        String specType = specSDI.getDataset("primary").getValue(0, "spectypeflag");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        DataSet paramData = specSDI.getDataset("specparamitems");
        if (paramData != null) {
            DataSet limitData = specSDI.getDataset("speclimittype");
            if (limitData != null) {
                paramLimitData = specSDI.getDataset("specparamlimits");
                if (paramLimitData != null) {
                    ret = this.getStructuredSpecLimitsData(paramData, limitData, paramLimitData, gridArrayList, translationProcessor);
                }
            } else {
                this.logger.error("No SpecParamLimits data could be found. Please make sure the element is defined correctly.");
            }
        } else {
            this.logger.error("Could not obtain limit types data.");
        }
        return ret;
    }

    DataSet getStructuredSpecLimitsData(DataSet specParamItemsData, DataSet specLimitTypesData, DataSet specParamLimitsData, ArrayList gridArrayList, TranslationProcessor translationProcessor) {
        DataSet paramLimits = this.addStructuredLimitsCols(specLimitTypesData);
        for (int gridRow = 0; gridRow < specParamItemsData.getRowCount(); ++gridRow) {
            String paramListId = specParamItemsData.getValue(gridRow, "paramlistid", "");
            String paramListVersionId = specParamItemsData.getValue(gridRow, "paramlistversionid", "");
            String variantId = specParamItemsData.getValue(gridRow, "variantid", "");
            String paramId = specParamItemsData.getValue(gridRow, "paramid", "");
            String paramType = specParamItemsData.getValue(gridRow, "paramtype", "");
            String dataType = specParamItemsData.getValue(gridRow, SPECPARAMITEMS_DATATYPES_COL, "");
            this.updateParamInfo(paramLimits, gridRow, paramListId, paramListVersionId, variantId, paramId, paramType);
            paramLimits.setString(gridRow, "Limit Label", HttpUtil.htmlEncode(specParamItemsData.getString(gridRow, "limitlabel", "")));
            String[] cols = paramLimits.getColumns();
            for (int gridCol = 0; gridCol < specLimitTypesData.getRowCount(); ++gridCol) {
                String limitTypeSequence = specLimitTypesData.getValue(gridCol, "limittypesequence", "");
                int dataRow = this.getSpecParamLimitsRow(specParamLimitsData, paramListId, paramListVersionId, variantId, paramId, paramType, limitTypeSequence);
                String operationinfo = this.renderParamLimitDataRow(specParamLimitsData, dataRow, paramListId, paramListVersionId, variantId, paramId, paramType, limitTypeSequence, dataType);
                paramLimits.setString(gridRow, gridCol + 6 >= cols.length ? "Invalid" : cols[gridCol + 6], operationinfo);
            }
        }
        return paramLimits;
    }

    private void updateParamInfo(DataSet paramLimits, int row, String paramListId, String paramListVersionId, String variantId, String paramId, String paramType) {
        paramLimits.addRow(row);
        paramLimits.setString(row, "Param List", paramListId);
        paramLimits.setString(row, "Version", paramListVersionId);
        paramLimits.setString(row, "Variant", variantId);
        paramLimits.setString(row, "Param", paramId);
        paramLimits.setString(row, "Param Type", paramType);
    }

    private DataSet addStructuredLimitsCols(DataSet specLimitTypesData) {
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        ds.addColumn("Param List", 0);
        ds.addColumn("Version", 0);
        ds.addColumn("Variant", 0);
        ds.addColumn("Param", 0);
        ds.addColumn("Param Type", 0);
        ds.addColumn("Limit Label", 0);
        for (int gridCol = 0; gridCol < specLimitTypesData.getRowCount(); ++gridCol) {
            String elementid = "speclimits";
            String title = specLimitTypesData.getValue(gridCol, SPECLIMITTYPE_LIMITTYPEID_COL, "&nbsp;");
            String sCond = specLimitTypesData.getValue(gridCol, SPECLIMITTYPE_CONDITION_COL, "");
            if (sCond.length() > 0) {
                title = title + " (" + sCond + ")";
            }
            if (!ds.isValidColumn(title)) {
                ds.addColumn(title, 0);
                continue;
            }
            ds.addColumn(title + " ", 0);
        }
        return ds;
    }

    private int getSpecParamLimitsRow(DataSet specParamLimitsData, String paramListId, String paramListVersionId, String variantId, String paramId, String paramType, String limitTypeSequence) {
        int row;
        boolean found = false;
        for (row = 0; row < specParamLimitsData.getRowCount(); ++row) {
            String curr_ParamListId = specParamLimitsData.getValue(row, "paramlistid", "");
            String curr_ParamListVersionId = specParamLimitsData.getValue(row, "paramlistversionid", "");
            String curr_VariantId = specParamLimitsData.getValue(row, "variantid", "");
            String curr_ParamId = specParamLimitsData.getValue(row, "paramid", "");
            String curr_ParamType = specParamLimitsData.getValue(row, "paramtype", "");
            String curr_LimitTypeSequence = specParamLimitsData.getValue(row, "limittypesequence", "");
            if (!curr_ParamListId.equals(paramListId) || !curr_ParamListVersionId.equals(paramListVersionId) || !curr_VariantId.equals(variantId) || !curr_ParamId.equals(paramId) || !curr_ParamType.equals(paramType) || !curr_LimitTypeSequence.equals(limitTypeSequence)) continue;
            found = true;
            this.logger.debug("Found match at row " + row);
            break;
        }
        if (!found) {
            this.logger.warn("Could not find matching row!");
            row = -1;
        }
        return row;
    }

    private String renderParamLimitDataRow(DataSet specParamLimitsData, int dataRow, String paramListId, String paramListVersionId, String variantId, String paramId, String paramType, String limitTypeSequence, String dataType) {
        String value2;
        String operator2;
        String value1;
        String operator1;
        DataSet limitValues = new DataSet();
        limitValues.setColidCaseSensitive(true);
        if (specParamLimitsData != null && specParamLimitsData.getRowCount() > 0) {
            dataRow = this.getSpecParamLimitsRow(specParamLimitsData, paramListId, paramListVersionId, variantId, paramId, paramType, limitTypeSequence);
            if (dataRow > -1) {
                operator1 = this.getSpecParamLimitsOperator(specParamLimitsData, 1, dataRow);
                value1 = this.getSpecParamLimitsValue(specParamLimitsData, 1, dataRow, dataType);
                if (dataType.equalsIgnoreCase("n") || dataType.equalsIgnoreCase("nc") || dataType.equalsIgnoreCase("d") || dataType.equalsIgnoreCase("o") || dataType.equalsIgnoreCase("a") || dataType.equalsIgnoreCase("dc") || dataType.equalsIgnoreCase("oc")) {
                    this.logger.debug("Data type is of type number or any therefore use value2 and operator2.");
                    operator2 = this.getSpecParamLimitsOperator(specParamLimitsData, 2, dataRow);
                    this.logger.debug("operator2 = " + operator2);
                    value2 = this.getSpecParamLimitsValue(specParamLimitsData, 2, dataRow, dataType);
                    this.logger.debug("value2 = " + value2);
                } else {
                    this.logger.debug("Data type is of type text, ref or sdc therefore do not use value2 and operator2.");
                    operator2 = "";
                    value2 = "";
                }
            } else {
                this.logger.debug("Could not find matching specparamlimts row therefore default to '' for all.");
                operator1 = "";
                operator2 = "";
                value1 = "";
                value2 = "";
            }
        } else {
            this.logger.debug("No specparamlimts data therefore default '' for all.");
            operator1 = "";
            operator2 = "";
            value1 = "";
            value2 = "";
        }
        DataSet ds = new DataSet();
        ds.addRow();
        ds.setString(0, "operator1", operator1);
        ds.setString(0, "value1", value1);
        ds.setString(0, "operator2", operator2);
        ds.setString(0, "value2", value2);
        String ret = "";
        if (operator1.length() > 0) {
            ret = ret + operator1;
            ret = ret + " " + value1;
        }
        if (operator2.length() > 0) {
            if (ret.length() > 0) {
                ret = ret + " and ";
            }
            ret = ret + operator2;
            ret = ret + " " + value2;
        }
        return ret;
    }

    private String getSpecParamLimitsOperator(DataSet specParamLimitsData, int num, int row) {
        String theReturn;
        if (row > -1) {
            theReturn = specParamLimitsData.getValue(row, SPECPARAMLIMITS_OPERATOR_COL + num, "");
        } else {
            this.logger.debug("Could not find operator therefore default to ''.");
            theReturn = "";
        }
        return theReturn;
    }

    private String getSpecParamLimitsValue(DataSet specParamLimitsData, int num, int row, String datatype) {
        String sReturn = null;
        M18NUtil m18server = new M18NUtil();
        M18NUtil m18client = new M18NUtil(new ConnectionProcessor(this.sapphireConnection.getConnectionId()).getConnectionInfo(this.sapphireConnection.getConnectionId()));
        if (row > -1) {
            String valuestring = specParamLimitsData.getValue(row, SPECPARAMLIMITS_VALUE_COL + num, "");
            if (datatype.equalsIgnoreCase("N") && !specParamLimitsData.getValue(row, "operator1", "").equals("In") && !specParamLimitsData.getValue(row, "operator1", "").equals("Not In") || datatype.equalsIgnoreCase("NC") || datatype.equalsIgnoreCase("A")) {
                String valuenum = specParamLimitsData.getValue(row, SPECPARAMLIMITS_VALUE_COL + num + "num", "");
                if (valuestring.length() > 0) {
                    if (valuenum.length() > 0) {
                        if (valuestring.indexOf("/") > -1 && valuestring.length() > 2) {
                            sReturn = valuestring;
                        } else {
                            try {
                                BigDecimal bs = m18server.parseBigDecimal(valuestring);
                                BigDecimal bn = specParamLimitsData.getBigDecimal(row, SPECPARAMLIMITS_VALUE_COL + num + "num");
                                sReturn = m18client.format(bn.setScale(bs.scale()), false, false);
                            }
                            catch (Exception e) {
                                this.logger.warn("Could not convert " + valuestring + " precision to number " + valuenum + ".");
                                sReturn = valuenum;
                            }
                        }
                    } else {
                        sReturn = valuestring;
                    }
                } else {
                    sReturn = valuenum;
                }
            } else if (datatype.equalsIgnoreCase("N") && (specParamLimitsData.getValue(row, "operator1", "").equals("In") || specParamLimitsData.getValue(row, "operator1", "").equals("Not In"))) {
                if (valuestring.length() > 0) {
                    String[] valueArr = valuestring.split(";");
                    for (int i = 0; i < valueArr.length; ++i) {
                        if (valueArr[i].length() <= 0) continue;
                        if (valueArr[i].indexOf("/") > -1 && valueArr[i].length() > 2) {
                            if (i == 0) {
                                sReturn = valueArr[i];
                                continue;
                            }
                            sReturn = sReturn + ";" + valueArr[i];
                            continue;
                        }
                        try {
                            BigDecimal bs = m18server.parseBigDecimal(valueArr[i]);
                            if (i == 0) {
                                sReturn = m18client.format(bs, false, false);
                                continue;
                            }
                            sReturn = sReturn + ";" + m18client.format(bs, false, false);
                            continue;
                        }
                        catch (Exception e) {
                            this.logger.warn("Could not convert " + valueArr[i] + " precision to number.");
                            sReturn = valueArr[i];
                        }
                    }
                } else {
                    sReturn = "";
                }
            } else {
                sReturn = valuestring;
            }
        } else {
            sReturn = "";
            this.logger.debug("Could not find value therefore default to ''.");
        }
        return sReturn;
    }

    private DataSet getParamDetails(SDIData sdiData) {
        DataSet dataSet = new DataSet();
        dataSet.setColidCaseSensitive(true);
        dataSet.addColumn("Param List", 0);
        dataSet.addColumn("Version", 0);
        dataSet.addColumn("Variant", 0);
        dataSet.addColumn("Param", 0);
        dataSet.addColumn("Param Type", 0);
        dataSet.addColumn("Limit Type Rule", 0);
        dataSet.addColumn("Units", 0);
        dataSet.addColumn("Rounding Function", 0);
        dataSet.addColumn("#Digits Display Format", 0);
        dataSet.addColumn("Display Format", 0);
        dataSet.addColumn("Transformation Rule", 0);
        dataSet.addColumn("Any", 0);
        dataSet.addColumn("Report", 0);
        DataSet raw = sdiData.getDataset("specparamitems");
        if (raw != null) {
            for (int i = 0; i < raw.getRowCount(); ++i) {
                dataSet.addRow();
                dataSet.setString(i, "Param List", raw.getString(i, "paramlistid", ""));
                dataSet.setString(i, "Version", raw.getString(i, "paramlistversionid", ""));
                dataSet.setString(i, "Variant", raw.getString(i, "variantid", ""));
                dataSet.setString(i, "Param", raw.getString(i, "paramid", ""));
                dataSet.setString(i, "Param Type", raw.getString(i, "paramtype", ""));
                dataSet.setString(i, "Limit Type Rule", raw.getString(i, "limittyperule", ""));
                dataSet.setString(i, "Units", raw.getString(i, "unitsid", ""));
                dataSet.setString(i, "Rounding Function", raw.getString(i, "roundingfunction", ""));
                dataSet.setString(i, "#Digits", raw.getValue(i, "roundingprecision", ""));
                dataSet.setString(i, "Display Format", raw.getString(i, "displayformat", ""));
                dataSet.setString(i, "Transformation Rule", raw.getString(i, "transformrule", ""));
                dataSet.setString(i, "Any", this.getAllowAnyParamList(raw.getString(i, "allowanyparamlistflag", "")));
                dataSet.setString(i, "Report", raw.getString(i, "reportflag", "No"));
            }
        }
        return dataSet;
    }

    private String getAllowAnyParamList(String val) {
        if (val.equals("Y")) {
            return "Any ParamList";
        }
        if (val.equals("V")) {
            return "Any ParamList Version";
        }
        if (val.equals("A")) {
            return "Any ParamList Versipn & Variant";
        }
        if (val.equals("N")) {
            return "No";
        }
        return "";
    }
}

