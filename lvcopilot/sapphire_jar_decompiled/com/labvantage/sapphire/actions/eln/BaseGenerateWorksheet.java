/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.actions.eln.AddWorksheetItemSDI;
import com.labvantage.sapphire.actions.eln.AddWorksheetSDI;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.SectionBehaviorResolver;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BaseGenerateWorksheet
extends BaseELNAction {
    protected SapphireConnection sapphireConnection;
    private M18NUtil m18NUtil;
    protected DataSet template;
    private DataSet templatesections;
    private DataSet templateitems;
    protected PropertyList templateoptions;
    private ActionBlock ab = new ActionBlock();
    private int sectionSequence = 0;
    protected DataSet repeatset;
    protected DataSet workitemrepeatset;
    protected int repeatsetRow;
    String currentSDIWorkItemId = "";
    String currentSDIWorkItemInstance = "";
    private HashMap<String, PropertyList> worksheetSDIs = new HashMap();
    protected String templateid;
    protected String templateversionid;
    protected String workbookid;
    protected String workbookversionid;
    protected String workitemid;
    protected String workitemversionid;
    protected String authorflag = "C";
    protected String authorid;
    protected String sdiworkitemid;
    protected DataSet sdiworkitem;
    protected DataSet sdiworkitemitemworkitems;
    protected DataSet sdiworkitemitemparamlists;
    protected int sdiperworksheet = 100;
    boolean preview;
    SectionBehaviorResolver sbr;
    protected String metadata_id;
    protected String metadata_value;
    protected String limsdata_sdcid;
    protected String limsdata_keyid1;
    protected String limsdata_keyid2;
    protected String limsdata_keyid3;
    public static final String LOOPKEY_OUTER = "OUTER";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.sbr = new SectionBehaviorResolver(this.getQueryProcessor());
        this.m18NUtil = new M18NUtil(this.connectionInfo);
        this.sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        this.templateid = properties.getProperty("templateid");
        this.templateversionid = properties.getProperty("templateversionid");
        this.workbookid = properties.getProperty("workbookid");
        this.workbookversionid = properties.getProperty("workbookversionid");
        this.authorid = properties.getProperty("authorid");
        this.preview = properties.getProperty("preview").equals("Y");
        this.sdiperworksheet = Integer.parseInt(properties.getProperty("maxsdiperworksheet", "-9999"));
        this.metadata_id = properties.getProperty("metadata_id");
        this.metadata_value = properties.getProperty("metadata_value");
        this.limsdata_sdcid = properties.getProperty("limsdata_sdcid");
        this.limsdata_keyid1 = properties.getProperty("limsdata_keyid1");
        this.limsdata_keyid2 = properties.getProperty("limsdata_keyid2");
        this.limsdata_keyid3 = properties.getProperty("limsdata_keyid3");
    }

    protected void loadTemplate(String sdcid, String keyid1, String keyid2, String keyid3, String rule) throws SapphireException {
        if (!this.preview) {
            DataSet sdiworksheetrule;
            if ((this.templateid == null || this.templateid.length() == 0) && rule != null && rule.length() > 0) {
                sdiworksheetrule = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND worksheetrule = ?", new Object[]{sdcid, keyid1, keyid2, keyid3, rule});
            } else {
                String currentversionid = BaseGenerateWorksheet.resolveVersion(this.getQueryProcessor(), this.templateid, "", "worksheet");
                sdiworksheetrule = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdiworksheetrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND worksheetid = ?", new Object[]{sdcid, keyid1, keyid2, keyid3, this.templateid});
                if (sdiworksheetrule.size() > 1 && rule != null && rule.length() > 0) {
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("worksheetrule", rule);
                    sdiworksheetrule = sdiworksheetrule.getFilteredDataSet(filter);
                }
                for (int i = sdiworksheetrule.size() - 1; i >= 0; --i) {
                    if ((sdiworksheetrule.getValue(i, "worksheetversionid").length() <= 0 || this.templateversionid.equals(sdiworksheetrule.getValue(i, "worksheetversionid"))) && (sdiworksheetrule.getValue(i, "worksheetversionid").length() != 0 || this.templateversionid.length() <= 0 || this.templateversionid.equals(currentversionid))) continue;
                    sdiworksheetrule.deleteRow(i);
                }
                if (sdiworksheetrule.size() == 1 && sdiworksheetrule.getValue(0, "worksheetversionid").length() == 0) {
                    sdiworksheetrule.setValue(0, "worksheetversionid", currentversionid);
                }
            }
            if (sdiworksheetrule.size() == 1) {
                this.templateid = sdiworksheetrule.getValue(0, "worksheetid");
                this.templateversionid = BaseGenerateWorksheet.resolveVersion(this.getQueryProcessor(), this.templateid, sdiworksheetrule.getValue(0, "worksheetversionid"), "worksheet");
                if (this.workbookid.length() == 0) {
                    this.workbookid = sdiworksheetrule.getValue(0, "workbookid");
                    this.workbookversionid = BaseGenerateWorksheet.resolveVersion(this.getQueryProcessor(), this.workbookid, sdiworksheetrule.getValue(0, "workbookversionid"), "workbook");
                }
                if (this.workbookid.length() == 0) {
                    String[] userworkbook = BaseGenerateWorksheet.getUserWorkbook(this.sapphireConnection.getSysuserId(), this.database, this.getActionProcessor(), new ConfigurationProcessor(this.sapphireConnection.getConnectionId()), true);
                    this.workbookid = userworkbook[0];
                    this.workbookversionid = userworkbook[1];
                }
                if (this.workbookversionid.length() == 0) {
                    this.workbookversionid = "1";
                }
                this.authorflag = sdiworksheetrule.getValue(0, "authorflag", "C");
                this.sdiperworksheet = Integer.parseInt(sdiworksheetrule.getValue(0, "maxsdiperworksheet", "-9999"));
            } else {
                throw new SapphireException("Worksheet template not found");
            }
        }
        SDIData templateData = this.loadWorksheet(this.templateid, this.templateversionid, new PropertyList());
        this.template = templateData.getDataset("primary");
        SDIData sectionData = templateData.getSDIData("sections");
        this.templatesections = sectionData.getDataset("primary");
        SDIData itemData = templateData.getSDIData("items");
        this.templateitems = itemData.getDataset("primary");
        try {
            this.templateoptions = new PropertyList(new JSONObject(this.template.getValue(0, "options")));
        }
        catch (Exception e) {
            this.templateoptions = new PropertyList();
            this.logger.error("Faile to parse worksheet options", e);
        }
    }

    protected void startWorksheet(String defaultworksheetname) throws SapphireException {
        PropertyList substitutions = new PropertyList();
        if (this.metadata_id.length() > 0 && this.metadata_value.length() > 0) {
            String[] metadata_ids = StringUtil.split(this.metadata_id, ";");
            String[] metadata_values = StringUtil.split(this.metadata_value, ";");
            for (int i = 0; i < metadata_values.length; ++i) {
                substitutions.setProperty(metadata_ids[i], metadata_values[i]);
            }
        }
        substitutions.setProperty("templateid", this.template.getValue(0, "worksheetid"));
        substitutions.setProperty("templateversionid", this.template.getValue(0, "worksheetversionid"));
        substitutions.setProperty("workbookid", this.workbookid);
        substitutions.setProperty("workbookversionid", this.workbookversionid);
        substitutions.setProperty("templatename", this.template.getValue(0, "worksheetname"));
        substitutions.setProperty("workitemid", this.workitemid);
        substitutions.setProperty("workitemversionid", this.workitemversionid);
        PropertyList extra = this.getWorksheetNameSubstitutions();
        if (extra != null) {
            substitutions.putAll(extra);
        }
        String worksheetname = BaseGenerateWorksheet.resolveWorksheetName(this.sapphireConnection, this.getSequenceProcessor(), this.templateoptions.getProperty("worksheetnametemplate", defaultworksheetname), substitutions, null);
        PropertyList wsProps = new PropertyList();
        wsProps.setProperty("sdcid", "LV_Worksheet");
        wsProps.setProperty("worksheetversionid", "1");
        wsProps.setProperty("worksheetdesc", worksheetname);
        wsProps.setProperty("worksheetname", worksheetname);
        wsProps.setProperty("authorid", this.authorid.length() > 0 ? this.authorid : (this.authorflag.equals("C") ? (this.connectionInfo.getSysuserId().equals("(system)") ? "(null)" : this.connectionInfo.getSysuserId()) : "(null)"));
        wsProps.setProperty("authordt", wsProps.getProperty("authorid").equals("(null)") ? "(null)" : "now");
        wsProps.setProperty("workbookid", this.workbookid);
        wsProps.setProperty("workbookversionid", this.workbookversionid);
        wsProps.setProperty("worksheetstatus", "Pending");
        wsProps.setProperty("coreflag", "N");
        wsProps.setProperty("templateflag", "N");
        wsProps.setProperty("templatetypeflag", "(null)");
        wsProps.setProperty("templateprivacyflag", "(null)");
        wsProps.setProperty("templatekeyid1", this.template.getValue(0, "worksheetid"));
        wsProps.setProperty("templatekeyid2", this.template.getValue(0, "worksheetversionid"));
        wsProps.setProperty("templateid", this.template.getValue(0, "worksheetid"));
        wsProps.setProperty("templateversionid", this.template.getValue(0, "worksheetversionid"));
        wsProps.setProperty("copyattachment", "Y");
        wsProps.setProperty("excludeworksheetsdi", "Y");
        wsProps.setProperty("excludeworksheetcontributor", "Y");
        wsProps.setProperty("excludeworksheetactivitylog", "Y");
        wsProps.setProperty("worksheet_action", "Y");
        this.ab.setAction("AddWorksheet", "AddSDI", "1", wsProps);
        PropertyList wssActivityProps = new PropertyList();
        wssActivityProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
        wssActivityProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
        wssActivityProps.setProperty("targetsdcid", "LV_Worksheet");
        wssActivityProps.setProperty("targetkeyid1", "[$G{AddWorksheet.newkeyid1}]");
        wssActivityProps.setProperty("targetkeyid2", "[$G{AddWorksheet.newkeyid2}]");
        wssActivityProps.setProperty("activitytype", "Add");
        wssActivityProps.setProperty("activitylog", "Start Worksheet Generation");
        this.ab.setActionClass("EditSectionsActivityLog", AddWorksheetActivity.class.getName(), wssActivityProps);
    }

    protected void addWorksheetSDIs(String sdcid, String keyid1, String keyid2, String keyid3) throws SapphireException {
        PropertyList wssdiProps = new PropertyList();
        wssdiProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
        wssdiProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
        wssdiProps.setProperty("sdcid", sdcid);
        wssdiProps.setProperty("keyid1", keyid1);
        wssdiProps.setProperty("keyid2", keyid2);
        wssdiProps.setProperty("keyid3", keyid3);
        this.ab.setActionClass("AddWorksheetSDI_" + sdcid, AddWorksheetSDI.class.getName(), wssdiProps);
        this.worksheetSDIs.put(sdcid, wssdiProps);
    }

    protected void generateSections() throws SapphireException {
        int sectionrow = 0;
        while (sectionrow < this.templatesections.size()) {
            sectionrow = this.generateSection(sectionrow, false, "", LOOPKEY_OUTER)[0];
        }
        this.getActionProcessor().processActionBlock(this.ab);
    }

    private int[] generateSection(int sectionrow, boolean bypassRepeat, String parentid, String loopkey) throws SapphireException {
        PropertyList wssOptions = null;
        try {
            wssOptions = new PropertyList(new JSONObject(this.templatesections.getString(sectionrow, "options")));
        }
        catch (JSONException e) {
            throw new SapphireException("Failed to parse worksheet section options");
        }
        String repeat = wssOptions.getProperty("generatesectionrepeat");
        if (!bypassRepeat && repeat.length() > 0) {
            int start = sectionrow;
            int startlevel = this.templatesections.getInt(sectionrow, "sectionlevel");
            int subsections = 0;
            HashSet<String> done = new HashSet<String>();
            this.repeatset = repeat.equals("SDIWorkItem_AllWorkItem") || repeat.equals("SDIWorkItem_WorkItem") ? (this.workitemrepeatset = this.getRepeatSet(repeat, parentid)) : this.getRepeatSet(repeat, parentid);
            if (this.repeatset != null) {
                for (int i = 0; i < this.repeatset.size(); ++i) {
                    String sdiworkiteminstance;
                    String key = this.getRepeatKey(repeat, i);
                    if (done.contains(key)) continue;
                    this.repeatsetRow = i;
                    String sdiworkitemid = repeat.equals("SDIWorkItem_WorkItem") ? this.repeatset.getValue(i, "itemkeyid1") : "";
                    String string = sdiworkiteminstance = repeat.equals("SDIWorkItem_WorkItem") ? this.repeatset.getValue(i, "iteminstance") : "";
                    if (sdiworkitemid.length() > 0 && sdiworkiteminstance.length() > 0) {
                        this.currentSDIWorkItemId = sdiworkitemid;
                        this.currentSDIWorkItemInstance = sdiworkiteminstance;
                    }
                    if (repeat.equals("SDIWorkItem_AllWorkItem")) {
                        this.workitemid = this.repeatset.getValue(i, "workitemid");
                        this.workitemversionid = this.repeatset.getValue(i, "workitemversionid");
                    }
                    loopkey = loopkey + "_" + i;
                    sectionrow = this.generateSection(sectionrow, true, sdiworkitemid, loopkey)[0];
                    while (this.templatesections.getInt(sectionrow, "sectionlevel") > startlevel) {
                        int[] ret = this.generateSection(sectionrow, false, sdiworkitemid, loopkey);
                        sectionrow = ret[0];
                        if (i != 0) continue;
                        subsections += ret[1];
                    }
                    sectionrow = start;
                    done.add(key);
                    if (!repeat.equals("SDIWorkItem_AllWorkItem") && !repeat.equals("SDIWorkItem_WorkItem")) continue;
                    this.repeatset = this.workitemrepeatset;
                }
            }
            return new int[]{sectionrow += subsections + 1, subsections + 1};
        }
        this.sbr.addSectionToSequence(this.templatesections.getValue(sectionrow, "worksheetsectionid"), this.templatesections.getValue(sectionrow, "worksheetsectionversionid"), loopkey, this.sectionSequence);
        PropertyList wssProps = new PropertyList();
        wssProps.setProperty("sdcid", "LV_WorksheetSection");
        wssProps.setProperty("worksheetsectionversionid", "1");
        wssProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
        wssProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
        wssProps.setProperty("worksheetsectiondesc", StringUtil.replaceAll(this.resolveSubstitutions(this.templatesections.getValue(sectionrow, "worksheetsectiondesc")), ";", ","));
        wssProps.setProperty("sectionstatus", "InProgress");
        wssProps.setProperty("sectionlevel", this.templatesections.getValue(sectionrow, "sectionlevel"));
        wssProps.setProperty("availabilityflag", "Y");
        wssProps.setProperty("usersequence", String.valueOf(this.sectionSequence));
        wssProps.setProperty("templatekeyid1", this.templatesections.getValue(sectionrow, "worksheetsectionid"));
        wssProps.setProperty("templatekeyid2", this.templatesections.getValue(sectionrow, "worksheetsectionversionid"));
        wssProps.setProperty("templateid", this.template.getValue(0, "worksheetid"));
        wssProps.setProperty("templateversionid", this.template.getValue(0, "worksheetversionid"));
        wssProps.setProperty("copyattachment", "Y");
        wssProps.setProperty("worksheet_action", "Y");
        wssOptions.setProperty("sbr_loopkey", loopkey);
        wssProps.setProperty("options", wssOptions.toXMLString());
        this.ab.setAction("AddWorksheetSection_" + this.sectionSequence, "AddSDI", "1", wssProps);
        this.templatesections.setString(sectionrow, "__sectionavailabilityflag", wssProps.getProperty("availabilityflag"));
        this.templatesections.setString(sectionrow, "__itemavailability", wssOptions.getProperty("itemavailability", "A"));
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("worksheetsectionid", this.templatesections.getValue(sectionrow, "worksheetsectionid"));
        filter.put("worksheetsectionversionid", this.templatesections.getValue(sectionrow, "worksheetsectionversionid"));
        DataSet sectionitems = this.templateitems.getFilteredDataSet(filter);
        String sectionAvailabilityFlag = this.templatesections.getValue(sectionrow, "__sectionavailabilityflag", "Y");
        String itemAvailability = this.templatesections.getValue(sectionrow, "__itemavailability", "A");
        for (int j = 0; j < sectionitems.size(); ++j) {
            PropertyList wsisdiProps;
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(this.sapphireConnection, (DBUtil)this.database, (HashMap)sectionitems.get(j));
            PropertyList config = new PropertyList();
            config.setPropertyList(this.resolveSubstitutions(sectionitems.getClob(j, "config", "")));
            if (this.currentSDIWorkItemId.length() > 0 && config.getProperty("workitemid").equals(this.currentSDIWorkItemId)) {
                config.setProperty("workiteminstance", this.currentSDIWorkItemInstance);
            }
            this.sbr.addItemToSequence(sectionitems.getValue(j, "worksheetitemid"), sectionitems.getValue(j, "worksheetitemversionid"), loopkey, this.sectionSequence, j);
            PropertyList wsiProps = new PropertyList();
            wsiProps.setProperty("sdcid", "LV_WorksheetItem");
            wsiProps.setProperty("worksheetitemversionid", "1");
            wsiProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
            wsiProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
            wsiProps.setProperty("worksheetsectionid", "[$G{AddWorksheetSection_" + this.sectionSequence + ".newkeyid1}]");
            wsiProps.setProperty("worksheetsectionversionid", "[$G{AddWorksheetSection_" + this.sectionSequence + ".newkeyid2}]");
            wsiProps.setProperty("itemstatus", "InProgress");
            wsiProps.setProperty("availabilityflag", j == 0 ? sectionAvailabilityFlag : (itemAvailability.equals("S") ? "N" : (itemAvailability.equals("R") ? "Y" : sectionAvailabilityFlag)));
            wsiProps.setProperty("config", config.toXMLString());
            if (sectionitems.getValue(j, "propertytreeid").equals("RichTextControl")) {
                wsiProps.setProperty("contents", this.resolveSubstitutions(sectionitems.getValue(j, "contents")));
            }
            wsiProps.setProperty("usersequence", String.valueOf(j));
            wsiProps.setProperty("templatekeyid1", sectionitems.getValue(j, "worksheetitemid"));
            wsiProps.setProperty("templatekeyid2", sectionitems.getValue(j, "worksheetitemversionid"));
            wsiProps.setProperty("templateid", this.template.getValue(0, "worksheetid"));
            wsiProps.setProperty("templateversionid", this.template.getValue(0, "worksheetversionid"));
            wsiProps.setProperty("excludeworksheetitemsdi", "Y");
            wsiProps.setProperty("copyattachment", "Y");
            wsiProps.setProperty("worksheet_action", "Y");
            this.ab.setBlockProperty("WSI_templateid_" + this.sectionSequence + "_" + j, sectionitems.getValue(j, "worksheetitemid"));
            this.ab.setAction("AddWorksheetItem_" + this.sectionSequence + "_" + j, "AddSDI", "1", wsiProps);
            String defaultsdc = worksheetItem.getWorksheetItemOptions().getOption("defaultsdcid");
            if (!worksheetItem.getWorksheetItemOptions().getOption("supportssdis").equals("Y") || defaultsdc.length() <= 0 || this.worksheetSDIs.get(defaultsdc) == null || !config.getProperty("source").equalsIgnoreCase("control") || (wsisdiProps = this.worksheetSDIs.get(defaultsdc).copy()) == null) continue;
            wsisdiProps = wsisdiProps.copy();
            wsisdiProps.setProperty("worksheetitemid", "[$G{AddWorksheetItem_" + this.sectionSequence + "_" + j + ".newkeyid1}]");
            wsisdiProps.setProperty("worksheetitemversionid", "[$G{AddWorksheetItem_" + this.sectionSequence + "_" + j + ".newkeyid2}]");
            this.ab.setActionClass("AddWorksheetItemSDI_" + this.sectionSequence + "_" + j, AddWorksheetItemSDI.class.getName(), wsisdiProps);
        }
        ++this.sectionSequence;
        return new int[]{++sectionrow, 1};
    }

    protected DataSet getRepeatSet(String repeat, String parentid) {
        if (repeat.equals("SDIWorkItem_WorkItem")) {
            if (this.sdiworkitemitemworkitems != null) {
                this.sdiworkitemitemworkitems.sort("keyid1, keyid2, keyid3, usersequence");
                return this.sdiworkitemitemworkitems;
            }
            new DataSet();
        } else if (repeat.equals("SDIWorkItem_AllWorkItem")) {
            if (this.sdiworkitem != null) {
                this.sdiworkitem.sort("usersequence");
                return this.sdiworkitem;
            }
            new DataSet();
        } else if (repeat.equals("SDIWorkItem_ParamList")) {
            DataSet repeatSet = null;
            if (this.sdiworkitemitemparamlists != null) {
                HashMap<String, String> plFilterMap = new HashMap<String, String>();
                if (parentid.length() > 0) {
                    plFilterMap.put("workitemid", parentid);
                    repeatSet = this.sdiworkitemitemparamlists.getFilteredDataSet(plFilterMap);
                } else {
                    plFilterMap.put("workitemid", this.workitemid);
                    repeatSet = this.sdiworkitemitemparamlists.getFilteredDataSet(plFilterMap);
                }
                repeatSet.sort("keyid1, keyid2, keyid3, usersequence");
            }
            return repeatSet;
        }
        return null;
    }

    protected String getRepeatKey(String repeat, int repeatRow) {
        if (repeat.equals("SDIWorkItem_WorkItem") || repeat.equals("SDIWorkItem_ParamList")) {
            return this.repeatset.getValue(repeatRow, "itemkeyid1") + ";" + this.repeatset.getValue(repeatRow, "itemkeyid2") + ";" + this.repeatset.getValue(repeatRow, "itemkeyid3");
        }
        if (repeat.equals("SDIWorkItem_AllWorkItem")) {
            return this.repeatset.getValue(repeatRow, "workitemid") + ";" + this.repeatset.getValue(repeatRow, "workitemversionid");
        }
        return null;
    }

    protected String[] finalizeWorksheet() throws SapphireException {
        String worksheetid = this.ab.getActionProperty("AddWorksheet", "newkeyid1");
        String worksheetversionid = this.ab.getActionProperty("AddWorksheet", "newkeyid2");
        this.sbr.resolveBehaviorReferences(worksheetid, worksheetversionid);
        DataSet sdiattributes = new DataSet();
        DataSet attributecontrols = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid, config FROM worksheetitem WHERE propertytreeid = 'AttributesControl' AND worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid}, true);
        for (int i = 0; i < attributecontrols.size(); ++i) {
            DataSet worksheetitemattributes;
            PropertyList config = new PropertyList();
            config.setPropertyList(attributecontrols.getClob(i, "config", ""));
            if (!config.getProperty("attributemode").equalsIgnoreCase("worksheet") || (worksheetitemattributes = this.getAttributeControlAttributes(config)) == null) continue;
            BaseSDIAttributeAction.coreCopyDownAttributes(sdiattributes, worksheetitemattributes, this.getSDCProcessor().getPropertyList("LV_WorksheetItem"), attributecontrols.getValue(i, "worksheetitemid"), attributecontrols.getValue(i, "worksheetitemversionid"), "(null)", null, this.m18NUtil, this.logger);
        }
        if (sdiattributes.size() > 0) {
            DataSetUtil.insert(this.database, sdiattributes, "sdiattribute");
        }
        if (this.metadata_id.length() > 0 && this.metadata_value.length() > 0) {
            DataSet attributes = this.getQueryProcessor().getPreparedSqlDataSet("SELECT attributeid FROM sdiattribute WHERE sdcid='LV_Worksheet' AND keyid1=? AND keyid2=?", (Object[])new String[]{worksheetid, worksheetversionid});
            StringBuffer addMetaData_id = new StringBuffer();
            StringBuffer addMetaData_value = new StringBuffer();
            String[] metadata_ids = StringUtil.split(this.metadata_id, ";");
            String[] metadata_values = StringUtil.split(this.metadata_value, ";");
            int count = 0;
            for (int i = 0; i < metadata_values.length; ++i) {
                if (attributes.findRow("attributeid", metadata_ids[i]) < 0) continue;
                addMetaData_id.append(";").append(metadata_ids[i]);
                addMetaData_value.append(";").append(metadata_values[i]);
                ++count;
            }
            if (addMetaData_id.length() > 0) {
                PropertyList addMetadataValues = new PropertyList();
                addMetadataValues.setProperty("sdcid", "LV_Worksheet");
                addMetadataValues.setProperty("keyid1", worksheetid);
                addMetadataValues.setProperty("keyid2", worksheetversionid);
                addMetadataValues.setProperty("attributeid", addMetaData_id.substring(1));
                addMetadataValues.setProperty("value", addMetaData_value.substring(1));
                addMetadataValues.setProperty("attributesdcid", StringUtil.repeat(";LV_Worksheet", count).substring(1));
                addMetadataValues.setProperty("attributeinstance", StringUtil.repeat(";1", count).substring(1));
                this.getActionProcessor().processAction("EditSDIAttribute", "1", addMetadataValues);
            }
        }
        if (this.limsdata_sdcid.length() > 0 && this.limsdata_keyid1.length() > 0) {
            DataSet limsdata = new DataSet();
            limsdata.addColumnValues("sdcid", 0, this.limsdata_sdcid, ";");
            limsdata.addColumnValues("keyid1", 0, this.limsdata_keyid1, ";");
            limsdata.addColumnValues("keyid2", 0, this.limsdata_keyid2, ";");
            limsdata.addColumnValues("keyid3", 0, this.limsdata_keyid3, ";");
            limsdata.padColumns();
            limsdata.sort("sdcid");
            ArrayList<DataSet> limsdataBySDC = limsdata.getGroupedDataSets("sdcid");
            for (DataSet limsdataForSDC : limsdataBySDC) {
                String sdcid = limsdataForSDC.getValue(0, "sdcid");
                PropertyList addwsi = new PropertyList();
                addwsi.setProperty("worksheetid", worksheetid);
                addwsi.setProperty("worksheetversionid", worksheetversionid);
                addwsi.setProperty("sdcid", sdcid);
                addwsi.setProperty("keyid1", limsdataForSDC.getColumnValues("keyid1", ";"));
                addwsi.setProperty("keyid2", limsdataForSDC.getColumnValues("keyid2", ";"));
                addwsi.setProperty("keyid3", limsdataForSDC.getColumnValues("keyid3", ";"));
                this.getActionProcessor().processActionClass(AddWorksheetSDI.class.getName(), addwsi);
            }
        }
        DataSet worksheetitemparams = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemparam.* FROM worksheetitemparam, worksheetitem WHERE worksheetitemparam.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemparam.worksheetitemversionid = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
        for (int i = 0; i < worksheetitemparams.size(); ++i) {
            String value = this.getSubstitution(worksheetitemparams.getValue(i, "paramname"));
            if (value.length() <= 0) continue;
            worksheetitemparams.setValue(i, "paramvalue", value);
        }
        DataSetUtil.update(this.database, worksheetitemparams, "worksheetitemparam", new String[]{"worksheetitemid", "worksheetitemversionid", "paramname"});
        return new String[]{worksheetid, worksheetversionid};
    }

    protected DataSet getAttributeControlAttributes(PropertyList config) {
        return null;
    }

    protected DataSet getWorkItemAttributeControlAttributes(PropertyList config) {
        String attributeid = config.getProperty("attributeid");
        String worksheetcontext = config.getProperty("worksheetcontext");
        String sourcerelation = config.getProperty("sourcerelation", "WorkItem");
        String workitemid = config.getProperty("workitemid");
        String workiteminstance = config.getProperty("workiteminstance");
        String sdiWIVersion = "";
        if (this.sdiworkitem != null && this.sdiworkitem.getRowCount() > 0) {
            int r;
            HashMap<String, Object> find = new HashMap<String, Object>();
            find.put("workitemid", workitemid);
            if (workiteminstance.length() > 0) {
                try {
                    find.put("workiteminstance", new BigDecimal(workiteminstance));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if ((r = this.sdiworkitem.findRow(find)) > -1) {
                sdiWIVersion = this.sdiworkitem.getValue(r, "workitemversionid");
            }
        }
        String paramlistid = config.getProperty("paramlistid");
        String paramlistversionid = config.getProperty("paramlistversionid");
        String variantid = config.getProperty("variantid");
        StringBuffer sql = new StringBuffer();
        ArrayList<String> params = new ArrayList<String>();
        sql.append("SELECT * FROM sdiattribute WHERE sdcid = 'WorkItem' AND keyid1 = ? AND keyid2 = ? AND attributesdcid = ? ");
        params.add(workitemid);
        params.add(sdiWIVersion.length() > 0 ? sdiWIVersion : this.workitemversionid);
        params.add("LV_WorksheetItem");
        if (sourcerelation.equalsIgnoreCase("ParamList")) {
            StringBuilder sql2 = new StringBuilder();
            SafeSQL safeSQL = new SafeSQL();
            sql2.append("SELECT paramlist.paramlistversionid,wii.keyid2 FROM workitemitem wii, paramlist WHERE workitemid =").append(safeSQL.addVar(workitemid));
            sql2.append("  AND wii.sdcid = 'ParamList' AND wii.keyid1 =").append(safeSQL.addVar(paramlistid)).append(" AND wii.keyid3 =").append(safeSQL.addVar(variantid));
            sql2.append("  AND wii.keyid1 = paramlist.paramlistid AND wii.keyid3 = paramlist.variantid AND versionstatus IN ('C','P') ORDER BY versionstatus, CAST( paramlist.paramlistversionid as integer ) DESC");
            DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql2.toString(), safeSQL.getValues());
            if (OpalUtil.isNotEmpty(ds2) && ds2.getString(0, "keyid2", "").equalsIgnoreCase("C") && !ds2.getString(0, "paramlistversionid").equals(paramlistversionid)) {
                return null;
            }
            sql.append(" AND copydowncontext = ( SELECT workitemitemid FROM workitemitem WHERE workitemid = ? and workitemversionid = ? AND sdcid = 'ParamList' AND keyid1 = ? AND ( keyid2 = ? OR keyid2 = 'C') AND keyid3 = ? ) ");
            params.add(workitemid);
            params.add(sdiWIVersion.length() > 0 ? sdiWIVersion : this.workitemversionid);
            params.add(paramlistid);
            params.add(paramlistversionid);
            params.add(variantid);
        } else {
            sql.append(" AND copydowncontext IS NULL ");
        }
        if (attributeid.length() > 0) {
            sql.append(" AND attributeid = ?");
            params.add(attributeid);
        }
        if (worksheetcontext.length() > 0) {
            sql.append(" AND worksheetcontext = ?");
            params.add(worksheetcontext);
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), params.toArray());
        return ds;
    }

    protected PropertyList getWorksheetNameSubstitutions() {
        return null;
    }

    private String resolveSubstitutions(String value) throws SapphireException {
        if (value != null && value.length() > 0 && value.contains("$S{") && value.contains("}")) {
            String[] tokens = StringUtil.getTokens(value, "$S{", "}", false);
            if (tokens.length > 0) {
                for (String token : tokens) {
                    String replaceWith = "";
                    if (replaceWith.length() == 0) {
                        replaceWith = this.getSubstitution(token);
                    }
                    if (replaceWith == null || replaceWith.length() <= 0) continue;
                    value = StringUtil.replaceAll(value, "$S{" + token + "}", replaceWith);
                }
            }
            return value;
        }
        return value;
    }

    protected String getSubstitution(String token) {
        return "";
    }

    protected DataSet loadWorkItem(String sdiworkitemid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        DataSet workitems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT workitem.workitemid, workitem.workitemversionid FROM sdiworkitem, workitem WHERE sdiworkitem.workitemid = workitem.workitemid AND sdiworkitem.workitemversionid = workitem.workitemversionid AND sdiworkitemid IN (" + safeSQL.addIn(sdiworkitemid, ";") + ")", safeSQL.getValues());
        if (workitems.size() == 1) {
            this.workitemid = workitems.getValue(0, "workitemid");
            this.workitemversionid = workitems.getValue(0, "workitemversionid");
            return workitems;
        }
        throw new SapphireException((workitems.size() > 1 ? "Multiple workitems found" : "No workitem found") + " for the passed in sdiworkitemid: " + sdiworkitemid);
    }

    protected DataSet loadWorkItems(String sdiworkitemid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        DataSet workitems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT workitem.workitemid, workitem.workitemversionid FROM sdiworkitem, workitem WHERE sdiworkitem.workitemid = workitem.workitemid AND sdiworkitem.workitemversionid = workitem.workitemversionid AND sdiworkitemid IN (" + safeSQL.addIn(sdiworkitemid, ";") + ")", safeSQL.getValues());
        return workitems;
    }

    protected void loadWorkItemSamples(String sdiworkitemid) throws SapphireException {
        this.sdiworkitemid = sdiworkitemid;
        SDIList sdiList = new SDIList();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        List<String> list = this.getSDIWIIDListBySupportedLimit(sdiworkitemid);
        for (String sdiwiid : list) {
            sql.setLength(0);
            safeSQL.reset();
            sql.append(this.database.isOracle() ? "SELECT  sdcid, keyid1, keyid2, keyid3 FROM sdiworkitem, TABLE (LV_OrderTab (" + safeSQL.addVar(sdiwiid) + ")) t " : "SELECT sdcid, keyid1, keyid2, keyid3 FROM sdiworkitem, LV_OrderTab (" + safeSQL.addVar(sdiwiid) + ",default,default,default) t ");
            sql.append(" WHERE sdiworkitem.sdiworkitemid = t.id_value ORDER BY t.seq_value");
            this.database.createPreparedResultSet(sql.toString(), safeSQL.getValues());
            while (this.database.getNext()) {
                sdiList.setSdcid(this.database.getValue("sdcid"));
                sdiList.addSDI(this.database.getValue("keyid1"), this.database.getValue("keyid2"), this.database.getValue("keyid3"));
            }
        }
        if (sdiList.size() <= 0) {
            throw new SapphireException("Parent SDIs not found for sdiworkitemid: " + sdiworkitemid);
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDIList(sdiList);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("sdiworkitem");
        sdiRequest.setRequestItem("sdiworkitemitem");
        sdiRequest.setRequestItem("datarelation");
        sdiRequest.setRequestItem("workitemrelation");
        sdiRequest.setRequestItem("attribute");
        sdiRequest.setExtendedDataTypes(true);
        SDIData workitemData = this.getSDIProcessor().getSDIData(sdiRequest);
        this.sdiworkitem = workitemData.getDataset("sdiworkitem");
        this.sdiworkitem.sort("workitemtypeflag");
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("itemsdcid", "WorkItem");
        this.sdiworkitemitemworkitems = workitemData.getDataset("sdiworkitemitem").getFilteredDataSet(filterMap);
        filterMap.put("itemsdcid", "ParamList");
        this.sdiworkitemitemparamlists = workitemData.getDataset("sdiworkitemitem").getFilteredDataSet(filterMap);
        this.addWorksheetSDIs(sdiList.getSdcid(), sdiList.getKeyid1(), sdiList.getKeyid2(), sdiList.getKeyid3());
        this.addWorksheetSDIs("SDIWorkItem", sdiworkitemid, "", "");
    }

    protected void loadWorkOrderSamples(String workorderid) throws SapphireException {
        SDIList sdiList = new SDIList();
        this.database.createPreparedResultSet("SELECT s_sampleid from s_sample where workorderid = ?", new String[]{workorderid});
        while (this.database.getNext()) {
            sdiList.setSdcid("Sample");
            sdiList.addSDI(this.database.getValue("s_sampleid"), "(null)", "(null)");
        }
        if (sdiList.size() > 0) {
            this.addWorksheetSDIs(sdiList.getSdcid(), sdiList.getKeyid1(), sdiList.getKeyid2(), sdiList.getKeyid3());
        }
    }

    protected void setTemplateSectionsItems(SDIData templateData) {
        this.template = templateData.getDataset("primary");
        SDIData sectionData = templateData.getSDIData("sections");
        this.templatesections = sectionData.getDataset("primary");
        SDIData itemData = templateData.getSDIData("items");
        this.templateitems = itemData.getDataset("primary");
        try {
            this.templateoptions = new PropertyList(new JSONObject(this.template.getValue(0, "options")));
        }
        catch (Exception e) {
            this.templateoptions = new PropertyList();
            this.logger.error("Faile to parse worksheet options", e);
        }
    }

    public List<String> getSDIWIIDListBySupportedLimit(String sdiworkitemid) {
        ArrayList<String> arrayList = new ArrayList<String>();
        int limit = 4000;
        while (sdiworkitemid.length() > limit) {
            String temp = sdiworkitemid.substring(0, limit);
            int lastIndx = temp.lastIndexOf(";");
            temp = sdiworkitemid.substring(0, lastIndx);
            arrayList.add(temp);
            sdiworkitemid = sdiworkitemid.substring(lastIndx + 1);
        }
        arrayList.add(sdiworkitemid);
        return arrayList;
    }

    protected void renewActionBlock() {
        this.ab = new ActionBlock();
    }
}

