/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.Trace;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EmpowerPolicyDef
implements Serializable {
    public static final String DEFAULT_NODE = "Sapphire Product";
    private PropertyList empowerCoreMapping;
    private PropertyList empowerUpload;
    private PropertyList empowerDownload;
    private PropertyListCollection translatedColumnInfo;
    private DataSet uploadSampleSetRules;
    private DataSet uploadStandardSampleSetLineRules;
    private DataSet uploadUnknownSampleSetLineRules;
    private DataSet uploadPeakRules;
    private DataSet uploadResultRules;
    private boolean autoReleaseResults;
    private String handlingReleasedResults;
    private String ruletitle;
    private String missingPeakText;
    private String namedPeakResultColumn;
    private DataSet uploadNamedPeakResultRule;
    private String timeZoneOffset;
    private boolean saveUnmatchedComponents;
    private boolean saveUnknownPeaks;
    private String unknownPeakHandlingRule;
    private String thresholdLevel;
    private String unknownPeakParamId;
    private String unknownPeakResultColumn;
    private DataSet uploadUnknownPeakResultRule;
    private DataSet additionalSSMCols;
    private String displayFormat;
    private boolean strictDisplayFormat;
    private boolean validateStrictDataType;
    private DataSet downloadSampleSetMethodRules;
    private DataSet downloadStandardSampleSetLineRules;
    private DataSet downloadStandardReagentLotLineRules;
    private DataSet downloadUnknownSampleSetLineRules;
    private DataSet downloadStandardComponentRules;
    private DataSet downloadUnknownComponentRules;
    private DataSet downloadReagentLotComponentRules;
    public static final String EMPOWERCOLUMN = "empowercolumn";
    public static final String UPLOADRULE = "uploadrule";
    public static final String DOWNLOADRULE = "downloadrule";
    public static final String RULETYPE = "ruletype";
    public static final String CONTEXT = "context";
    public static final String RULETYPE_QCBATCH = "QCBatch";
    public static final String RULETYPE_SAMPLE = "Sample";
    public static final String RULETYPE_DATASET = "DataSet";
    public static final String RULETYPE_DATAITEM = "DataItem";
    public static final String RULETYPE_WORKITEMCOL = "WorkItemCol";
    public static final String RULETYPE_REAGENTLOT = "ReagentLot";
    public static final String RULETYPE_INVALID = "Invalid";
    public static final String CONTEXT_CURRENT = "Current";
    public static final String CONTEXT_REAGENTLOT = "ReagentLot";
    public static final String CONTEXT_PREPSAMPLE = "PrepSample";
    public static final String SAPPHIRECOLUMNID = "columnid";
    public static final String ADDDIIFNEEDED = "AddDIIfNeeded";
    public static final String ADDDSIFNEEDED = "AddDSIfNeeded";
    public static final String PARAMLISTID = "paramlistid";
    public static final String PARAMLISTVERSIONID = "paramlistversionid";
    public static final String VARIANTID = "variantid";
    public static final String PARAMID = "paramid";
    public static final String PARAMTYPE = "paramtype";
    public static final String DATASET = "dataset";
    public static final String REPLICATEID = "replicateid";
    public static final String WORKITEMID = "workitemid";
    public static final String WORKITEMINSTANCE = "workiteminstance";
    public static final String UNKNOWNPEAK_HANDLINGRULE_APPENDRT = "AppendRT";
    public static final String UNKNOWNPEAK_HANDLINGRULE_APPENDRRT = "AppendRRT";
    public static final String UNKNOWNPEAK_HANDLINGRULE_APPENDSEQUENCE = "AppendSequence";

    public EmpowerPolicyDef(PropertyList policy) throws SapphireException {
        PropertyListCollection ssmcols;
        if (policy == null) {
            throw new SapphireException("Policy is null.");
        }
        this.ruletitle = policy.getProperty("ruletitle", "Missing Title");
        this.empowerCoreMapping = policy.getPropertyList("EmpowerCoreMapping");
        if (this.empowerCoreMapping == null) {
            throw new SapphireException("EmpowerCoreMapping propertylist is null.");
        }
        this.translatedColumnInfo = policy.getCollectionNotNull("TranslateColumnInfo");
        this.empowerUpload = policy.getPropertyList("EmpowerUpload");
        if (this.empowerUpload == null) {
            throw new SapphireException("EmpowerUpload propertylist is null.");
        }
        this.empowerDownload = policy.getPropertyList("EmpowerDownload");
        if (this.empowerDownload == null) {
            throw new SapphireException("EmpowerDownload propertylist is null.");
        }
        this.uploadSampleSetRules = this.loadUploadRules("sampleset");
        this.uploadStandardSampleSetLineRules = this.loadUploadRules("standardsamplesetline");
        this.uploadUnknownSampleSetLineRules = this.loadUploadRules("unknownsamplesetline");
        this.uploadResultRules = this.loadUploadRules("result");
        this.uploadPeakRules = this.loadUploadRules("peak");
        this.timeZoneOffset = this.empowerUpload.getProperty("timezoneoffset", "0");
        PropertyList peakPl = this.empowerUpload.getPropertyList("peak");
        if (peakPl == null) {
            throw new SapphireException("EmpowerUpload, peak propertylist is null.");
        }
        PropertyList resultUploadRule = peakPl.getPropertyList("resultmapping");
        if (resultUploadRule == null) {
            throw new SapphireException("EmpowerUpload, peaks, resultmapping propertylist is null.");
        }
        this.namedPeakResultColumn = resultUploadRule.getProperty("empowerresultcolumn", "Amount");
        this.uploadNamedPeakResultRule = EmpowerPolicyDef.parseUploadRuleString(resultUploadRule.getProperty(UPLOADRULE));
        this.autoReleaseResults = peakPl.getProperty("autorelease", "N").equals("Y");
        this.handlingReleasedResults = peakPl.getProperty("handlingreleasedresults", "Error");
        this.missingPeakText = peakPl.getProperty("missingpeaktext", "Missing");
        this.displayFormat = peakPl.getProperty("displayformat", "0.000");
        this.strictDisplayFormat = peakPl.getProperty("strictdisplayformat", "N").equals("Y");
        this.validateStrictDataType = peakPl.getProperty("validatestrictdatatype", "N").equals("Y");
        this.saveUnmatchedComponents = peakPl.getProperty("saveunmatchedcomponents", "N").equals("Y");
        PropertyList unknownPeaksPl = peakPl.getPropertyListNotNull("unknownpeaks");
        if (unknownPeaksPl == null) {
            throw new SapphireException("EmpowerUpload, peaks, unknownpeaks propertylist is null.");
        }
        this.saveUnknownPeaks = unknownPeaksPl.getProperty("saveunknownpeaks", "N").equals("Y");
        this.thresholdLevel = unknownPeaksPl.getProperty("thresholdlevel", "0.0");
        this.unknownPeakResultColumn = unknownPeaksPl.getProperty("resultcolumn", "Area");
        this.uploadUnknownPeakResultRule = EmpowerPolicyDef.parseUploadRuleString(unknownPeaksPl.getProperty(UPLOADRULE));
        this.unknownPeakHandlingRule = unknownPeaksPl.getProperty("handlingrule");
        this.unknownPeakParamId = unknownPeaksPl.getProperty("unknownpeakparamid", "UNKNOWN");
        this.downloadSampleSetMethodRules = this.loadDownloadRules("samplesetmethod");
        this.downloadUnknownSampleSetLineRules = this.loadDownloadRules("unknownsamplesetline");
        this.downloadStandardSampleSetLineRules = this.loadDownloadRules("standardsamplesetline");
        this.downloadStandardReagentLotLineRules = this.loadDownloadRules("standardsamplesetlinesforreagent");
        this.downloadUnknownComponentRules = this.loadDownloadRules("unknowncomponent");
        this.downloadStandardComponentRules = this.loadDownloadRules("standardcomponent");
        this.downloadReagentLotComponentRules = this.loadDownloadRules("standardcomponentfromreagent");
        PropertyList mappingpageconfig = policy.getPropertyList("MappingPageConfig");
        this.additionalSSMCols = new DataSet();
        this.additionalSSMCols.addColumn("title", 0);
        this.additionalSSMCols.addColumn("sslcolumn", 0);
        this.additionalSSMCols.addColumn("width", 1);
        if (mappingpageconfig != null && (ssmcols = mappingpageconfig.getCollection("AdditionalSSLCols")) != null && ssmcols.size() > 0) {
            for (int i = 0; i < ssmcols.size(); ++i) {
                PropertyList currCol = ssmcols.getPropertyList(i);
                int currrow = this.additionalSSMCols.addRow();
                this.additionalSSMCols.setString(currrow, "title", currCol.getProperty("title"));
                this.additionalSSMCols.setString(currrow, "sslcolumn", currCol.getProperty("sslcolumn"));
                this.additionalSSMCols.setNumber(currrow, "width", currCol.getProperty("width", "100"));
            }
        }
    }

    public String getRuleTitle() {
        return this.ruletitle;
    }

    public DataSet getAdditionalSSMCols() {
        return this.additionalSSMCols;
    }

    public String getUploadSampleSetColumns() {
        String std = this.getEmpowerCoreMapping("empowerqcbatchid");
        std = std + ";" + this.getTranslate("SampleSetName");
        String cols = this.uploadSampleSetRules.getColumnValues(EMPOWERCOLUMN, ";");
        if (cols.length() > 0) {
            String[] col = StringUtil.split(cols, ";");
            for (int i = 0; i < col.length; ++i) {
                std = std + ";" + this.getTranslate(col[i]);
            }
        }
        return std;
    }

    public String getUploadSampleSetLineColumns() {
        String std = this.getEmpowerCoreMapping("empowerlimssampleid");
        std = std + ";" + this.getEmpowerCoreMapping("empowerlimsdatasetkey");
        std = std + ";" + this.getEmpowerCoreMapping("empowerlimssdcid");
        std = std + ";" + this.getTranslate("SampleType");
        String otherSSCols = this.uploadStandardSampleSetLineRules.getColumnValues(EMPOWERCOLUMN, ";");
        if (otherSSCols.length() > 0) {
            std = std + ";" + otherSSCols;
        }
        otherSSCols = this.uploadUnknownSampleSetLineRules.getColumnValues(EMPOWERCOLUMN, ";");
        String[] col = StringUtil.split(otherSSCols, ";");
        for (int i = 0; i < col.length; ++i) {
            std = std + ";" + this.getTranslate(col[i]);
        }
        return std;
    }

    public String getUploadPeakColumns() {
        String std = this.getTranslate("Name");
        std = std + ";" + this.getTranslate("RetentionTime");
        std = std + ";" + this.getTranslate("PeakType");
        std = std + ";" + this.getTranslate("RRT~");
        std = std + ";" + this.getTranslate(this.getNamedPeakResultColumn());
        std = std + ";" + this.getTranslate(this.getUnknownPeakResultColumn());
        String otherCols = this.uploadPeakRules.getColumnValues(EMPOWERCOLUMN, ";");
        if (otherCols.length() > 0) {
            String[] col = StringUtil.split(otherCols, ";");
            for (int i = 0; i < col.length; ++i) {
                std = std + ";" + this.getTranslate(col[i]);
            }
        }
        return std;
    }

    public String getUploadResultColumns() {
        String std = this.getTranslate("ResultSetId");
        std = std + ";" + this.getTranslate("ResultId");
        std = std + ";" + this.getEmpowerCoreMapping("empowerlimssampleid");
        std = std + ";" + this.getEmpowerCoreMapping("empowerlimsdatasetkey");
        std = std + ";" + this.getTranslate("DateAcquired");
        std = std + ";" + this.getEmpowerCoreMapping("empoweruploadflag");
        std = std + ";" + this.getTranslate("Injection");
        std = std + ";" + this.getTranslate("ChannelName");
        String resultRuleCols = this.uploadResultRules.getColumnValues(EMPOWERCOLUMN, ";");
        if (resultRuleCols.length() > 0) {
            String[] col = StringUtil.split(resultRuleCols, ";");
            for (int i = 0; i < col.length; ++i) {
                std = std + ";" + this.getTranslate(col[i]);
            }
        }
        return std;
    }

    public DataSet getUploadSampleSetRules() {
        return this.uploadSampleSetRules;
    }

    public DataSet getUploadUnknownSampleSetLineRules() {
        return this.uploadUnknownSampleSetLineRules;
    }

    public DataSet getUploadStandardSampleSetLineRules() {
        return this.uploadStandardSampleSetLineRules;
    }

    public DataSet getUploadPeakRules() {
        return this.uploadPeakRules;
    }

    public DataSet getUploadResultRules() {
        return this.uploadResultRules;
    }

    public DataSet getDownloadSampleSetMethodRules() {
        return this.downloadSampleSetMethodRules;
    }

    public DataSet getDownloadStandardSampleSetLineRules() {
        return this.downloadStandardSampleSetLineRules;
    }

    public DataSet getDownloadStandardReagentLotLineRules() {
        return this.downloadStandardReagentLotLineRules;
    }

    public DataSet getDownloadUnknownSampleSetLineRules() {
        return this.downloadUnknownSampleSetLineRules;
    }

    public DataSet getDownloadStandardComponentRules() {
        return this.downloadStandardComponentRules;
    }

    public DataSet getDownloadUnknownComponentRules() {
        return this.downloadUnknownComponentRules;
    }

    public DataSet getDownloadRegentLotComponentRules() {
        return this.downloadReagentLotComponentRules;
    }

    public String getUnknownPeakResultColumn() {
        return this.getTranslate(this.unknownPeakResultColumn);
    }

    public String getDisplayFormat() {
        return this.displayFormat;
    }

    public String getNamedPeakResultColumn() {
        return this.getTranslate(this.namedPeakResultColumn);
    }

    public DataSet getUploadUnknownPeakResultRule() {
        return this.uploadUnknownPeakResultRule;
    }

    public DataSet getUploadNamedPeakResultRule() {
        return this.uploadNamedPeakResultRule;
    }

    public boolean autoReleaseResults() {
        return this.autoReleaseResults;
    }

    public String getHandlingReleasedResults() {
        return this.handlingReleasedResults;
    }

    public String getMissingPeakText() {
        return this.missingPeakText;
    }

    public int getTimezoneOffset() {
        int offset = 0;
        try {
            offset = Integer.parseInt(this.timeZoneOffset);
        }
        catch (NumberFormatException e) {
            offset = 0;
        }
        return offset;
    }

    public String getTimezone() {
        return this.timeZoneOffset;
    }

    public boolean getStrictDisplayFormat() {
        return this.strictDisplayFormat;
    }

    public boolean getValidateStrictDataType() {
        return this.validateStrictDataType;
    }

    public boolean saveUnmatchedComponents() {
        return this.saveUnmatchedComponents;
    }

    public boolean saveUnknownPeaks() {
        return this.saveUnknownPeaks;
    }

    public String getUnknownPeakHandlingRule() {
        return this.unknownPeakHandlingRule;
    }

    public String getThresholdLevel() {
        return this.thresholdLevel;
    }

    public String getUnknownPeakParamId() {
        return this.unknownPeakParamId;
    }

    public static DataSet parseUploadRuleString(String ruleString) throws SapphireException {
        DataSet ruleDS = new DataSet();
        if (ruleString == null || ruleString.length() == 0) {
            return ruleDS;
        }
        int currow = ruleDS.addRow();
        if (ruleString.startsWith(RULETYPE_QCBATCH)) {
            ruleDS.setString(currow, RULETYPE, RULETYPE_QCBATCH);
            String col = StringUtil.split(ruleString, "=")[1];
            ruleDS.setString(currow, SAPPHIRECOLUMNID, col);
            return ruleDS;
        }
        if (ruleString.startsWith(RULETYPE_SAMPLE)) {
            ruleDS.setString(currow, RULETYPE, RULETYPE_SAMPLE);
            String col = StringUtil.split(ruleString, "=")[1];
            ruleDS.setString(currow, SAPPHIRECOLUMNID, col);
            return ruleDS;
        }
        if (ruleString.startsWith(RULETYPE_DATASET)) {
            String[] parts = StringUtil.split(ruleString, "=");
            ruleDS.setString(currow, RULETYPE, RULETYPE_DATASET);
            String datasetinfo = parts[0].substring(parts[0].indexOf("[") + 1, parts[0].indexOf("]"));
            if (parts[1].indexOf(ADDDSIFNEEDED) > 0) {
                String[] subparts = StringUtil.split(parts[1], ";");
                ruleDS.setString(currow, SAPPHIRECOLUMNID, subparts[0]);
                ruleDS.setString(currow, ADDDSIFNEEDED, subparts[1].charAt(14) + "");
            } else {
                ruleDS.setString(currow, SAPPHIRECOLUMNID, parts[1]);
                ruleDS.setString(currow, ADDDSIFNEEDED, "Y");
            }
            String[] dsparts = StringUtil.split(datasetinfo, ";");
            ruleDS.setString(currow, PARAMLISTID, dsparts[0]);
            if (dsparts.length > 1) {
                ruleDS.setString(currow, VARIANTID, dsparts[1]);
            }
        } else {
            String[] parts = StringUtil.split(ruleString, ".");
            ruleDS.setString(currow, RULETYPE, RULETYPE_DATAITEM);
            String dataiteminfo = parts[0].substring(parts[0].indexOf("[") + 1, parts[0].indexOf("]"));
            if (parts[1].indexOf(ADDDSIFNEEDED) > 0 || parts[1].indexOf(ADDDIIFNEEDED) > 0) {
                String[] subparts = StringUtil.split(parts[1], ";");
                ruleDS.setString(currow, SAPPHIRECOLUMNID, subparts[0]);
                if (parts[1].indexOf(ADDDSIFNEEDED) > 0) {
                    ruleDS.setString(currow, ADDDSIFNEEDED, parts[1].charAt(parts[1].indexOf(ADDDSIFNEEDED) + 14) + "");
                } else {
                    ruleDS.setString(currow, ADDDSIFNEEDED, "Y");
                }
                if (parts[1].indexOf(ADDDIIFNEEDED) > 0) {
                    ruleDS.setString(currow, ADDDIIFNEEDED, parts[1].charAt(parts[1].indexOf(ADDDIIFNEEDED) + 14) + "");
                } else {
                    ruleDS.setString(currow, ADDDIIFNEEDED, "Y");
                }
            } else {
                String sapphirecol = parts[1];
                if (sapphirecol.indexOf("Column=") > -1) {
                    sapphirecol = sapphirecol.replace("Column=", "");
                }
                ruleDS.setString(currow, SAPPHIRECOLUMNID, sapphirecol);
                ruleDS.setString(currow, ADDDIIFNEEDED, "Y");
                ruleDS.setString(currow, ADDDSIFNEEDED, "Y");
            }
            String[] dataitemparts = StringUtil.split(dataiteminfo, "|");
            String[] dsparts = StringUtil.split(dataitemparts[0], ";");
            String[] diparts = StringUtil.split(dataitemparts[1], ";");
            ruleDS.setString(currow, PARAMLISTID, dsparts[0]);
            if (dsparts.length > 1) {
                ruleDS.setString(currow, VARIANTID, dsparts[1]);
            }
            ruleDS.setString(currow, PARAMID, diparts[0]);
            ruleDS.setString(currow, PARAMTYPE, diparts[1]);
            ruleDS.setString(currow, REPLICATEID, diparts[2]);
        }
        return ruleDS;
    }

    private DataSet addUploadRule(DataSet ruleDS, PropertyList rule) {
        String ruleString = rule.getProperty(UPLOADRULE);
        String empowerColumn = rule.getProperty(EMPOWERCOLUMN);
        if (ruleString == null || ruleString.length() == 0 || empowerColumn == null || empowerColumn.length() == 0) {
            Trace.log("Invalid rule, skip it.");
            return ruleDS;
        }
        try {
            DataSet currItem = EmpowerPolicyDef.parseUploadRuleString(ruleString);
            currItem.setString(0, EMPOWERCOLUMN, empowerColumn);
            String[] cols = currItem.getColumns();
            int currrow = ruleDS.addRow();
            for (int i = 0; i < cols.length; ++i) {
                ruleDS.setString(currrow, cols[i], currItem.getString(0, cols[i]));
            }
        }
        catch (SapphireException e) {
            Trace.log("Invalid rule. Skipped." + e.getMessage());
            return ruleDS;
        }
        return ruleDS;
    }

    private DataSet loadUploadRules(String propertylistname) {
        DataSet processingRules = new DataSet();
        PropertyList pl = this.empowerUpload.getPropertyList(propertylistname);
        if (pl == null) {
            Trace.log("Failed to load upload rules. Cannot find propertylist for " + propertylistname);
            return processingRules;
        }
        PropertyListCollection rulesColl = pl.getCollectionNotNull("columns");
        for (int i = 0; i < rulesColl.size(); ++i) {
            processingRules = this.addUploadRule(processingRules, rulesColl.getPropertyList(i));
        }
        return processingRules;
    }

    private DataSet loadDownloadRules(String propertylistname) {
        DataSet processingRules = new DataSet();
        PropertyList pl = this.empowerDownload.getPropertyList(propertylistname);
        if (pl == null) {
            Trace.log("Failed to load download rules. Cannot find propertylist for " + propertylistname);
            return processingRules;
        }
        PropertyListCollection rulesColl = pl.getCollectionNotNull("columns");
        for (int i = 0; i < rulesColl.size(); ++i) {
            processingRules = this.addDownloadRule(processingRules, rulesColl.getPropertyList(i));
        }
        return processingRules;
    }

    private DataSet addDownloadRule(DataSet ruleDS, PropertyList rule) {
        String ruleString = rule.getProperty(DOWNLOADRULE);
        String empowerColumn = rule.getProperty(EMPOWERCOLUMN);
        if (ruleString == null || ruleString.length() == 0 || empowerColumn == null || empowerColumn.length() == 0) {
            Trace.log("Invalid rule, skip it.");
            return ruleDS;
        }
        try {
            DataSet currItem = EmpowerPolicyDef.parseDownloadRuleString(ruleString);
            currItem.setString(0, EMPOWERCOLUMN, empowerColumn);
            String[] cols = currItem.getColumns();
            int currrow = ruleDS.addRow();
            for (int i = 0; i < cols.length; ++i) {
                ruleDS.setString(currrow, cols[i], currItem.getString(0, cols[i]));
            }
        }
        catch (SapphireException e) {
            Trace.log("Cannot parse rule. skipping." + e.getMessage());
            return ruleDS;
        }
        return ruleDS;
    }

    public static DataSet parseDownloadRuleString(String ruleString) throws SapphireException {
        DataSet ruleDS = new DataSet();
        if (ruleString == null || ruleString.length() == 0) {
            return ruleDS;
        }
        int currow = ruleDS.addRow();
        if (ruleString.startsWith(RULETYPE_QCBATCH)) {
            ruleDS.setString(currow, RULETYPE, RULETYPE_QCBATCH);
            String col = StringUtil.split(ruleString, "=")[1];
            ruleDS.setString(currow, SAPPHIRECOLUMNID, col);
            return ruleDS;
        }
        if (ruleString.startsWith(RULETYPE_SAMPLE)) {
            ruleDS.setString(currow, RULETYPE, RULETYPE_SAMPLE);
            String col = StringUtil.split(ruleString, "=")[1];
            ruleDS.setString(currow, SAPPHIRECOLUMNID, col);
            return ruleDS;
        }
        if (ruleString.startsWith("SDIWorkItem")) {
            ruleDS.setString(currow, RULETYPE, RULETYPE_WORKITEMCOL);
            String col = StringUtil.split(ruleString, "=")[1];
            ruleDS.setString(currow, SAPPHIRECOLUMNID, col);
            return ruleDS;
        }
        if (ruleString.startsWith("ReagentLot") && ruleString.indexOf(RULETYPE_DATAITEM) == -1) {
            ruleDS.setString(currow, RULETYPE, "ReagentLot");
            String col = StringUtil.split(ruleString, "=")[1];
            ruleDS.setString(currow, SAPPHIRECOLUMNID, col);
            return ruleDS;
        }
        if (ruleString.startsWith(RULETYPE_DATASET)) {
            String[] parts = StringUtil.split(ruleString, "=");
            ruleDS.setString(currow, RULETYPE, RULETYPE_DATASET);
            String datasetinfo = parts[0].substring(parts[0].indexOf("[") + 1, parts[0].indexOf("]"));
            if (parts[1].indexOf(ADDDSIFNEEDED) > 0) {
                String[] subparts = StringUtil.split(parts[1], ";");
                ruleDS.setString(currow, SAPPHIRECOLUMNID, subparts[0]);
                ruleDS.setString(currow, ADDDSIFNEEDED, subparts[1].charAt(14) + "");
            } else {
                ruleDS.setString(currow, SAPPHIRECOLUMNID, parts[1]);
                ruleDS.setString(currow, ADDDSIFNEEDED, "Y");
            }
            String[] dsparts = StringUtil.split(datasetinfo, ";");
            ruleDS.setString(currow, PARAMLISTID, dsparts[0]);
            if (dsparts.length > 1) {
                ruleDS.setString(currow, VARIANTID, dsparts[1]);
            }
        } else {
            String context = "";
            if (ruleString.startsWith(CONTEXT_CURRENT)) {
                context = CONTEXT_CURRENT;
            } else if (ruleString.startsWith("ReagentLot")) {
                context = "ReagentLot";
            } else if (ruleString.startsWith(CONTEXT_PREPSAMPLE)) {
                context = CONTEXT_PREPSAMPLE;
            }
            if (context.length() > 0) {
                ruleDS.setString(currow, CONTEXT, context);
                ruleString = ruleString.substring(context.length() + 1);
            }
            String[] parts = StringUtil.split(ruleString, ".");
            ruleDS.setString(currow, RULETYPE, RULETYPE_DATAITEM);
            String dataiteminfo = parts[0].substring(parts[0].indexOf("[") + 1, parts[0].indexOf("]"));
            if (parts[1].indexOf(ADDDSIFNEEDED) > 0 || parts[1].indexOf(ADDDIIFNEEDED) > 0) {
                String[] subparts = StringUtil.split(parts[1], ";");
                ruleDS.setString(currow, SAPPHIRECOLUMNID, subparts[0]);
                if (parts[1].indexOf(ADDDSIFNEEDED) > 0) {
                    ruleDS.setString(currow, ADDDSIFNEEDED, parts[1].charAt(parts[1].indexOf(ADDDSIFNEEDED) + 14) + "");
                } else {
                    ruleDS.setString(currow, ADDDSIFNEEDED, "Y");
                }
                if (parts[1].indexOf(ADDDIIFNEEDED) > 0) {
                    ruleDS.setString(currow, ADDDIIFNEEDED, parts[1].charAt(parts[1].indexOf(ADDDIIFNEEDED) + 14) + "");
                } else {
                    ruleDS.setString(currow, ADDDIIFNEEDED, "Y");
                }
            } else {
                String sapphirecol = parts[1];
                if (sapphirecol.indexOf("Column=") > -1) {
                    sapphirecol = sapphirecol.replace("Column=", "");
                }
                ruleDS.setString(currow, SAPPHIRECOLUMNID, sapphirecol);
                ruleDS.setString(currow, ADDDIIFNEEDED, "Y");
                ruleDS.setString(currow, ADDDSIFNEEDED, "Y");
            }
            String[] dataitemparts = StringUtil.split(dataiteminfo, "|");
            String[] dsparts = StringUtil.split(dataitemparts[0], ";");
            String[] diparts = StringUtil.split(dataitemparts[1], ";");
            ruleDS.setString(currow, PARAMLISTID, dsparts[0]);
            if (dsparts.length > 1) {
                ruleDS.setString(currow, VARIANTID, dsparts[1]);
            }
            ruleDS.setString(currow, PARAMID, diparts[0]);
            ruleDS.setString(currow, PARAMTYPE, diparts[1]);
            ruleDS.setString(currow, REPLICATEID, diparts[2]);
        }
        return ruleDS;
    }

    public String getEmpowerCoreMapping(String propertyid) {
        return this.empowerCoreMapping.getProperty(propertyid);
    }

    public String getTranslate(String propertyid) {
        return this.getTranslate(propertyid, true);
    }

    public String getTranslate(String propertyid, boolean casesensitive) {
        PropertyList match;
        if (this.translatedColumnInfo != null && (match = this.translatedColumnInfo.find("englishstr", propertyid, casesensitive)) != null) {
            return match.getProperty("translatedstr", propertyid);
        }
        return propertyid;
    }

    public PropertyList getEmpowerCoreMappings() {
        return this.empowerCoreMapping;
    }

    public PropertyListCollection getTranslations() {
        return this.translatedColumnInfo;
    }
}

