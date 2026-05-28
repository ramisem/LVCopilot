/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.documents;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.modules.documents.FieldProcessing;
import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.pageelements.controls.HTMLEditorControl;
import com.labvantage.sapphire.pageelements.gwt.shared.DocumentConstants;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class Form
implements FieldProcessing,
DocumentConstants {
    public static final String LOGNAME = "FORM";
    private boolean taskForm = false;
    private boolean formlet = false;
    private boolean worksheetitemForm = false;
    private boolean draftable;
    private boolean submitable;
    private boolean checkable;
    private boolean validateOnSave;
    private boolean saveInvalidData;
    private boolean submitInvalidData;
    private boolean approveInvalidData;
    private boolean dde;
    private boolean ddeuser2alert;
    private boolean ddeuser2recon;
    private boolean lockondone;
    private boolean asyncprocessing;
    private boolean versionable;
    private boolean defaultprocessingfields = true;
    private boolean transientform;
    private boolean translate;
    private boolean hasautosavefields;
    private boolean worksheet;
    private boolean trainingrequired;
    private boolean overrideallowed;
    private boolean trainingexists;
    private boolean attachmentsbyref;
    private String attachmentslocation;
    private int worksheetqty;
    private String formid;
    private String formversionid;
    private String formletid;
    private String formletversionid;
    private String worksheetitemid;
    private String worksheetitemversionid;
    private String taskdefid;
    private String taskdefversionid;
    private String taskdefvariantid;
    private String stepid;
    private String versionstatus;
    private String formtitle;
    private String formdesc;
    private String formLayout;
    private String documentDescRule;
    private String processingType;
    private String processingRule;
    private String approvaltypeid;
    private String reconciliationroleid;
    private String documentmanagerroleid;
    private String worksheettype;
    private PropertyList formObjects;
    private PropertyListCollection pages;
    private PropertyListCollection fields;
    private PropertyListCollection groups;
    private PropertyListCollection sections;
    private PropertyListCollection datasources;
    private PropertyListCollection elements;
    private PropertyListCollection identityFields;
    private HashMap<String, PropertyList> fieldMap = new HashMap();
    private HashMap<String, PropertyList> groupMap = new HashMap();
    private HashMap<String, PropertyList> sectionMap = new HashMap();
    private HashMap<String, PropertyList> pageMap = new HashMap();
    private HashMap<String, PropertyList> datasourceMap = new HashMap();
    private HashMap<String, PropertyList> elementMap = new HashMap();
    private HashMap fileFieldMap;
    private ArrayList<String> worksheetParams;
    private Logger logger;

    private Form() {
    }

    public static Form getInstance(SapphireConnection sapphireConnection, String formid, String formversionid) throws SapphireException {
        return Form.getInstance(sapphireConnection, formid, formversionid, null, null, null, null, null, null, null, null, null, null, new PropertyList());
    }

    public static Form getInstance(SapphireConnection sapphireConnection, String formletid, String formletversionid, String extra) throws SapphireException {
        return Form.getInstance(sapphireConnection, null, null, formletid, formletversionid, null, null, null, null, null, null, null, null, new PropertyList());
    }

    public static Form getInstance(SapphireConnection sapphireConnection, String worksheetitemid, String worksheetitemversionid, String extra, String extra2) throws SapphireException {
        return Form.getInstance(sapphireConnection, null, null, null, null, worksheetitemid, worksheetitemversionid, null, null, null, null, null, null, new PropertyList());
    }

    public static Form getInstance(SapphireConnection sapphireConnection, String formid, String formversionid, boolean debug) throws SapphireException {
        PropertyList directives = new PropertyList();
        directives.setProperty("debug", debug ? "Y" : "N");
        return Form.getInstance(sapphireConnection, formid, formversionid, null, null, null, null, null, null, null, null, null, null, directives);
    }

    public static Form getInstance(SapphireConnection sapphireConnection, String formid, String formversionid, PropertyList directives) throws SapphireException {
        return Form.getInstance(sapphireConnection, formid, formversionid, null, null, null, null, null, null, null, null, null, null, directives);
    }

    public static Form getInstance(SapphireConnection sapphireConnection, String taskdefid, String taskdefversionid, String taskdefvariantid, String stepid, PropertyList directives) throws SapphireException {
        return Form.getInstance(sapphireConnection, null, null, null, null, null, null, taskdefid, taskdefversionid, taskdefvariantid, stepid, null, null, directives);
    }

    public static Form getInstance(SapphireConnection sapphireConnection, PropertyList formProps) throws SapphireException {
        return Form.getInstance(sapphireConnection, null, null, null, null, null, null, null, null, null, null, formProps, null, null);
    }

    public static Form getInstance(SapphireConnection sapphireConnection, String formid, String formversionid, boolean debug, File rakFile) throws SapphireException {
        PropertyList directives = new PropertyList();
        directives.setProperty("debug", debug ? "Y" : "N");
        return Form.getInstance(sapphireConnection, formid, formversionid, null, null, null, null, null, null, null, null, null, rakFile, directives);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static Form getInstance(SapphireConnection sapphireConnection, String formid, String formversionid, String formletid, String formletversionid, String worksheetitemid, String worksheetitemversionid, String taskdefid, String taskdefversionid, String taskdefvariantid, String stepid, PropertyList formProps, File rakFile, PropertyList directives) throws SapphireException {
        String[] translations;
        PropertyListCollection filelocations;
        PropertyList filePolicy;
        PropertyList attachments;
        ConfigurationProcessor configProcessor;
        PropertyList formsPolicy;
        String formPolicy;
        Form form;
        QueryProcessor queryProcessor;
        long start = System.currentTimeMillis();
        if (directives == null) {
            directives = new PropertyList();
        }
        boolean debug = directives.getProperty("debug", "N").equals("Y");
        boolean ignoreTrainingRecs = directives.getProperty("ignoretrainingrecs", "N").equals("Y");
        PropertyList sectionsBindings = null;
        if (directives.containsKey("sectionbindings")) {
            sectionsBindings = directives.getPropertyList("sectionbindings");
        }
        QueryProcessor queryProcessor2 = queryProcessor = rakFile != null ? new QueryProcessor(rakFile, sapphireConnection.getConnectionId()) : new QueryProcessor(sapphireConnection.getConnectionId());
        if (formversionid != null && (formversionid.length() == 0 || formversionid.equalsIgnoreCase("C"))) {
            String sql = "SELECT formversionid FROM form WHERE formid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( formversionid as integer ) DESC";
            DataSet forms = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{formid});
            if (forms.size() <= 0) throw new SapphireException("Failed to find form '" + formid + "'");
            formversionid = forms.getValue(0, "formversionid");
        }
        if ((form = null) != null) return form;
        form = new Form();
        form.logger = new Logger(sapphireConnection.getConnectionId());
        form.logger.setLoggerName(LOGNAME);
        form.logger.info("Loading form with formid=" + formid + ", formversionid=" + formversionid);
        SDCProcessor sdcProcessor = rakFile != null ? new SDCProcessor(rakFile, sapphireConnection.getConnectionId()) : new SDCProcessor(sapphireConnection.getConnectionId());
        SDIProcessor sdiProcessor = rakFile != null ? new SDIProcessor(rakFile, sapphireConnection.getConnectionId()) : new SDIProcessor(sapphireConnection.getConnectionId());
        TranslationProcessor translationProcessor = rakFile != null ? new TranslationProcessor(rakFile, sapphireConnection.getConnectionId()) : new TranslationProcessor(sapphireConnection.getConnectionId());
        DDTService ddtService = new DDTService(sapphireConnection);
        SDIRequest sdiRequest = new SDIRequest();
        SDIData sdiData = null;
        DataSet formData = null;
        if (formid != null && formid.length() > 0) {
            String virtualformflag;
            sdiRequest.setSDCid("LV_Form");
            sdiRequest.setKeyid1List(formid);
            sdiRequest.setKeyid2List(formversionid);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setExtendedDataTypes(true);
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            if (sdiData == null || sdiData.getDataset("primary") == null || sdiData.getDataset("primary").size() != 1) {
                throw new SapphireException("Failed to find form '" + formid + "(" + formversionid + ")'");
            }
            formData = sdiData.getDataset("primary");
            form.logger.info("Load took " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            if ((sapphireConnection.getUserType().equals("V") || sapphireConnection.getUserType().equals("I")) && (virtualformflag = formData.getValue(0, "virtualformflag", "N")).equals("N")) {
                throw new SapphireException("Form access denied - This form is not a virtual form and is not available to you: " + formid);
            }
        } else if (formletid != null && formletid.length() > 0) {
            sdiRequest.setSDCid("LV_Formlet");
            sdiRequest.setKeyid1List(formletid);
            sdiRequest.setKeyid2List(formletversionid);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setExtendedDataTypes(true);
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            if (sdiData == null || sdiData.getDataset("primary") == null || sdiData.getDataset("primary").size() != 1) {
                throw new SapphireException("Failed to find formlet '" + formletid + "(" + formletversionid + ")'");
            }
            formData = sdiData.getDataset("primary");
            form.logger.info("Load took " + (System.currentTimeMillis() - start) + "ms");
            start = System.currentTimeMillis();
            form.formlet = true;
        } else if (worksheetitemid != null && worksheetitemid.length() > 0) {
            sdiRequest.setSDCid("LV_WorksheetItem");
            sdiRequest.setKeyid1List(worksheetitemid);
            sdiRequest.setKeyid2List(worksheetitemversionid);
            sdiRequest.setRequestItem("primary");
            sdiRequest.setExtendedDataTypes(true);
            sdiData = sdiProcessor.getSDIData(sdiRequest);
            if (sdiData == null || sdiData.getDataset("primary") == null || sdiData.getDataset("primary").size() != 1) {
                throw new SapphireException("Failed to find worksheetitem '" + worksheetitemid + "(" + worksheetitemversionid + ")'");
            }
            DataSet wsiData = sdiData.getDataset("primary");
            form.setWorksheetitemid(worksheetitemid);
            form.setWorksheetitemversionid(worksheetitemversionid);
            PropertyList configProps = new PropertyList();
            configProps.setPropertyList(wsiData.getClob(0, "config"));
            PropertyList props = new PropertyList();
            props.setPropertyList(configProps.getProperty("form"));
            formData = new DataSet();
            formData.addRow();
            formData.setString(0, "formid", "");
            formData.setString(0, "formversionid", "");
            formData.setString(0, "versionstatus", "P");
            formData.setString(0, "submitableflag", "Y");
            formData.setString(0, "checkableflag", "Y");
            formData.setString(0, "validateondraftflag", "Y");
            formData.setString(0, "saveinvaliddataflag", "N");
            formData.setString(0, "submitinvaliddataflag", "N");
            formData.setString(0, "translateflag", sapphireConnection.getLanguage() != null && sapphireConnection.getLanguage().length() > 0 ? "Y" : "N");
            formData.setString(0, "formlayout", props.getProperty("formlayout"));
            PropertyList formObjects = props.getPropertyList("formproperties");
            formData.setString(0, "formobjects", formObjects.toXMLString());
            form.worksheetitemForm = true;
        } else if (taskdefid != null && taskdefid.length() > 0) {
            TaskDef taskDef = TaskDef.getInstance(sapphireConnection, taskdefid, taskdefversionid, taskdefvariantid);
            PropertyListCollection steps = taskDef.getTaskdef().getCollection("steps");
            PropertyList step = steps.find("stepid", stepid);
            if (step == null) throw new SapphireException("Form load failed for taskdefid '" + taskdefid + "' - invalid stepid '" + stepid + "'!");
            form.setTaskdefid(taskdefid);
            form.setTaskdefversionid(taskdefversionid);
            form.setTaskdefvariantid(taskdefvariantid);
            form.setStepid(stepid);
            PropertyList stepformProps = step.getPropertyList("Form");
            PropertyList props = new PropertyList();
            props.setPropertyList(stepformProps.getProperty("form"));
            formData = new DataSet();
            formData.addRow();
            formData.setString(0, "formid", "");
            formData.setString(0, "formversionid", "");
            formData.setString(0, "versionstatus", "P");
            formData.setString(0, "submitableflag", "Y");
            formData.setString(0, "checkableflag", "Y");
            formData.setString(0, "saveinvaliddataflag", "N");
            formData.setString(0, "submitinvaliddataflag", "N");
            formData.setString(0, "translateflag", sapphireConnection.getLanguage() != null && sapphireConnection.getLanguage().length() > 0 ? "Y" : "N");
            formData.setString(0, "formlayout", props.getProperty("formlayout"));
            PropertyList formObjects = props.getPropertyList("formproperties");
            formData.setString(0, "formobjects", formObjects.toXMLString());
            form.taskForm = true;
        } else {
            if (formProps == null) throw new SapphireException("Form load failed - no loading parameters specified!");
            formData = new DataSet();
            formData.addRow();
            formData.setString(0, "formid", "");
            formData.setString(0, "formversionid", "");
            formData.setString(0, "versionstatus", "P");
            formData.setString(0, "submitableflag", "Y");
            formData.setString(0, "checkableflag", "Y");
            formData.setString(0, "saveinvaliddataflag", "N");
            formData.setString(0, "submitinvaliddataflag", "N");
            formData.setString(0, "translateflag", "N");
            formData.setString(0, "formlayout", formProps.getProperty("formlayout"));
            PropertyList formObjects = formProps.getPropertyList("formproperties");
            formData.setString(0, "formobjects", formObjects.toXMLString());
            form.taskForm = true;
        }
        form.setFormid(formData.getValue(0, "formid"));
        form.setFormversionid(formData.getValue(0, "formversionid"));
        form.setFormletid(formData.getValue(0, "formletid"));
        form.setFormletversionid(formData.getValue(0, "formletversionid"));
        form.setVersionstatus(formData.getValue(0, "versionstatus"));
        form.setFormtitle(formData.getValue(0, "formtitle"));
        form.setFormdesc(formData.getValue(0, "formdesc"));
        form.setDocumentDescRule(formData.getValue(0, "documentdescrule"));
        form.setProcessingRule(formData.getValue(0, "processingscript"));
        form.setProcessingType(formData.getValue(0, "processingscripttypeflag"));
        form.setDraftable(formData.getValue(0, "draftableflag", "N").equals("Y"));
        form.setSubmitable(formData.getValue(0, "submitableflag", "N").equals("Y"));
        form.setCheckable(formData.getValue(0, "checkableflag", "N").equals("Y"));
        form.setVersionable(formData.getValue(0, "versionableflag", "N").equals("Y"));
        form.setReconciliationroleid(formData.getValue(0, "reconciliationroleid"));
        form.setDocumentmanagerroleid(formData.getValue(0, "documentmanagerroleid"));
        form.setApprovetypeid(formData.getValue(0, "approvaltypeid"));
        form.setValidateOnSave(formData.getValue(0, "validateondraftflag", "N").equals("Y"));
        form.setSaveInvalidData(formData.getValue(0, "saveinvaliddataflag", "N").equals("Y"));
        form.setSubmitInvalidData(formData.getValue(0, "submitinvaliddataflag", "N").equals("Y"));
        form.setApproveInvalidData(formData.getValue(0, "approveinvaliddataflag", "N").equals("Y"));
        form.setDde(formData.getValue(0, "ddeflag", "N").equals("Y"));
        form.setDdeuser2recon(formData.getValue(0, "ddeuser2reconcileflag", "N").equals("Y"));
        form.setDdeuser2alert(formData.getValue(0, "ddeuser2alertflag", "N").equals("Y"));
        form.setLockondone(formData.getValue(0, "lockondoneflag", "N").equals("Y"));
        form.setAsyncprocessing(formData.getValue(0, "asyncprocessingflag", "N").equals("Y"));
        form.setVersionable(formData.getValue(0, "versionableflag", "N").equals("Y"));
        form.setTransientform(formData.getValue(0, "transientformflag", "N").equals("Y"));
        form.setTranslate(formData.getValue(0, "translateflag", "N").equals("Y"));
        form.setWorksheet(formData.getValue(0, "formtype").equals("Worksheet"));
        form.setWorksheettype(formData.getValue(0, "worksheettype"));
        form.setWorksheetqty(Integer.parseInt(formData.getValue(0, "worksheetqty", "1")));
        form.setTrainingrequired(formData.getValue(0, "trainingreqflag", "N").equals("Y"));
        form.setOverrideallowed(formData.getValue(0, "overrideallowedflag", "N").equals("Y"));
        if (form.isTrainingrequired()) {
            if (!ignoreTrainingRecs) {
                DataSet certification = queryProcessor.getPreparedSqlDataSet("SELECT certificationstatus, expirationdt, graceperiod, graceperiodunits FROM s_sdicertification WHERE resourcesdcid = 'User' AND resourcekeyid1 = ? AND resourcekeyid2 = '(null)' AND resourcekeyid3 = '(null)' AND certifiedforsdcid = 'LV_Form' AND certifiedforkeyid1 = ? AND certifiedforkeyid2 = ? AND certifiedforkeyid3 = '(null)' AND certificationtype = 'Analyst Training'", new Object[]{sapphireConnection.getSysuserId(), form.getFormid(), form.getFormversionid()});
                if (certification.size() == 1 && certification.getValue(0, "certificationstatus").equals("Valid")) {
                    Calendar expirationdt = certification.getCalendar(0, "expirationdt");
                    if (expirationdt == null) {
                        form.trainingexists = true;
                    } else {
                        int graceperiod = certification.getInt(0, "graceperiod");
                        if (graceperiod > 0) {
                            String graceperiodunits = certification.getString(0, "graceperiodunits");
                            if ("Days".equalsIgnoreCase(graceperiodunits)) {
                                expirationdt.add(5, graceperiod);
                            } else if ("Weeks".equalsIgnoreCase(graceperiodunits)) {
                                expirationdt.add(5, graceperiod * 7);
                            } else if ("Months".equalsIgnoreCase(graceperiodunits)) {
                                expirationdt.add(2, graceperiod);
                            } else if ("Years".equalsIgnoreCase(graceperiodunits)) {
                                expirationdt.add(1, graceperiod);
                            }
                        }
                        if (DateTimeUtil.getNowCalendar().before(expirationdt)) {
                            form.trainingexists = true;
                        }
                    }
                }
            } else {
                form.trainingexists = true;
            }
        }
        if ((formPolicy = formData.getValue(0, "policynode", "Sapphire Custom")).length() > 0 && (formsPolicy = (configProcessor = rakFile != null ? new ConfigurationProcessor(rakFile, sapphireConnection.getConnectionId()) : new ConfigurationProcessor(sapphireConnection.getConnectionId())).getPolicy("FormsPolicy", formPolicy)) != null && (attachments = formsPolicy.getPropertyList("attachments")) != null && attachments.getProperty("attachmentsbyref", "N").equals("Y") && (filePolicy = configProcessor.getPolicy("FileLocationPolicy", attachments.getProperty("attachbyrefpolicynode"))) != null && (filelocations = filePolicy.getCollection("locations")) != null) {
            for (int i = 0; i < filelocations.size(); ++i) {
                PropertyList filelocation = filelocations.getPropertyList(i);
                if (!filelocation.getProperty("id").equals(attachments.getProperty("attachbyreffilelocation"))) continue;
                String sh = Configuration.getInstance().getSapphireHome();
                form.attachmentslocation = StringUtil.replaceAll(StringUtil.replaceAll(filelocation.getProperty("location"), "[labvantagehome]", sh), "[sapphirehome]", sh);
                form.attachmentsbyref = true;
                break;
            }
        }
        if (form.isTransientForm()) {
            form.setSubmitable(true);
        }
        form.setFormLayout(formData.getString(0, form.isFormlet() ? "formletlayout" : "formlayout"));
        if (form.getFormLayout() == null || form.getFormLayout().length() == 0) {
            throw new SapphireException("No form layout defined!");
        }
        form.formObjects = new PropertyList();
        form.formObjects.setPropertyList(formData.getValue(0, form.isFormlet() ? "formletobjects" : "formobjects"));
        if (form.formObjects.size() > 0) {
            PropertyList section;
            int i;
            PropertyList field;
            int j;
            PropertyListCollection fields;
            String type;
            String sectionid;
            int i2;
            form.setDatasources(form.formObjects.getCollection("datasources"));
            if (form.isFormlet()) {
                PropertyListCollection pages = new PropertyListCollection();
                PropertyList page = new PropertyList();
                page.setProperty("pageid", "page001");
                pages.add(page);
                form.formObjects.setProperty("pages", pages);
            }
            form.setPages(form.formObjects.getCollection("pages"));
            form.setGroups(form.formObjects.getCollection("groups"));
            form.setSections(form.formObjects.getCollection("sections"));
            form.setFields(form.formObjects.getCollection("fields"));
            form.setElements(form.formObjects.getCollection("elements"));
            if (form.fields == null) {
                form.fields = new PropertyListCollection();
            }
            if (form.groups == null) {
                form.groups = new PropertyListCollection();
            }
            if (form.sections == null) {
                form.sections = new PropertyListCollection();
            }
            if (form.pages == null) {
                form.pages = new PropertyListCollection();
            }
            if (form.datasources == null) {
                form.datasources = new PropertyListCollection();
            }
            if (form.elements == null) {
                form.elements = new PropertyListCollection();
            }
            for (i2 = 0; i2 < form.sections.size(); ++i2) {
                boolean found;
                int j2;
                PropertyList sectionBinding;
                PropertyList section2 = form.sections.getPropertyList(i2);
                if (!section2.getProperty("formletbyreference", "N").equals("Y")) continue;
                sectionid = section2.getProperty("sectionid");
                String sectionFormletid = section2.getProperty("formlet");
                String sectionFormletversionid = section2.getProperty("formletversion");
                PropertyList propertyList = sectionBinding = sectionsBindings != null ? sectionsBindings.getPropertyList(sectionid) : null;
                if (sectionBinding != null && sectionBinding.getProperty("formletversionid").length() > 0) {
                    sectionFormletversionid = sectionBinding.getProperty("formletversionid");
                }
                if (sectionFormletversionid.length() == 0 || sectionFormletversionid.equalsIgnoreCase("C")) {
                    String sql = "SELECT formletversionid FROM formlet WHERE formletid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( formletversionid as integer ) DESC";
                    DataSet formlets = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{sectionFormletid});
                    if (formlets.size() <= 0) throw new SapphireException("Failed to find formlet '" + sectionFormletid + "'");
                    sectionFormletversionid = formlets.getValue(0, "formletversionid");
                }
                form.logger.info("Loading formlet with formletid=" + sectionFormletid + ", formletversionid=" + sectionFormletversionid);
                sdiRequest.setSDCid("LV_Formlet");
                sdiRequest.setKeyid1List(sectionFormletid);
                sdiRequest.setKeyid2List(sectionFormletversionid);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setExtendedDataTypes(true);
                sdiData = sdiProcessor.getSDIData(sdiRequest);
                if (sdiData == null || sdiData.getDataset("primary") == null || sdiData.getDataset("primary").size() != 1) {
                    throw new SapphireException("Failed to find formlet '" + sectionFormletid + "(" + sectionFormletversionid + ")'");
                }
                section2.setProperty("formletversionid", sectionFormletversionid);
                DataSet formletData = sdiData.getDataset("primary");
                String formletLayout = formletData.getString(0, "formletlayout");
                if (formletLayout == null || formletLayout.length() == 0) {
                    throw new SapphireException("No layout defined for formlet '" + sectionFormletid + "'!");
                }
                int pos = Form.findStartOfTag("<div ", formletLayout, "sapphire=\"page\"", 0);
                if (pos > 0 && (pos = formletLayout.indexOf(">", pos)) > 0) {
                    formletLayout = formletLayout.substring(pos + 1, formletLayout.indexOf("</div></div>"));
                    String formLayout = form.getFormLayout();
                    pos = formLayout.indexOf("id=\"" + sectionid + "\"");
                    if (pos > 0) {
                        if ((pos = formLayout.indexOf("></div>", pos)) > 0) {
                            formLayout = formLayout.substring(0, pos + 1) + formletLayout + formLayout.substring(pos + 1);
                        } else {
                            pos = formLayout.indexOf("/>", pos);
                            formLayout = formLayout.substring(0, pos) + ">" + formletLayout + "</div>" + formLayout.substring(pos + 2);
                        }
                        form.setFormLayout(formLayout);
                    }
                }
                PropertyList formletObjects = new PropertyList();
                formletObjects.setPropertyList(formletData.getValue(0, "formletobjects"));
                if (formletObjects.size() <= 0) continue;
                PropertyListCollection formletFields = formletObjects.getCollection("fields");
                PropertyListCollection formletGroups = formletObjects.getCollection("groups");
                PropertyListCollection formletSections = formletObjects.getCollection("sections");
                if (formletFields != null) {
                    for (j2 = 0; j2 < formletFields.size(); ++j2) {
                        PropertyList field2 = formletFields.getPropertyList(j2);
                        found = false;
                        for (int k = 0; !found && k < form.fields.size(); ++k) {
                            if (!form.fields.getPropertyList(k).getProperty("fieldid").equals(field2.getProperty("fieldid"))) continue;
                            form.fields.set(k, field2);
                            found = true;
                        }
                        if (found) continue;
                        form.fields.add(field2);
                    }
                }
                if (formletGroups != null) {
                    // empty if block
                }
                if (formletSections == null) continue;
                for (j2 = 0; j2 < formletSections.size(); ++j2) {
                    PropertyList formletSection = formletSections.getPropertyList(j2);
                    found = false;
                    for (int k = 0; !found && k < form.sections.size(); ++k) {
                        if (!form.sections.getPropertyList(k).getProperty("sectionid").equals(formletSection.getProperty("sectionid"))) continue;
                        form.sections.set(k, formletSection);
                        found = true;
                    }
                    if (found) continue;
                    PropertyList binding = section2.getPropertyList("binding");
                    if (binding != null) {
                        formletSection.setProperty("binding", binding);
                    }
                    form.sections.add(formletSection);
                }
            }
            for (i2 = 0; i2 < form.fields.size(); ++i2) {
                PropertyListCollection attributes;
                PropertyList field3 = form.fields.getPropertyList(i2);
                String fieldid = field3.getProperty("fieldid");
                String editorstyleid = field3.getProperty("editorstyleid");
                if (editorstyleid.length() > 0) {
                    PropertyList lookuplink;
                    String mode;
                    PropertyList editorProps = EditorStyleField.getEditorStyleProperties(editorstyleid, field3.getProperty("sdcid"), field3.getProperty("reftypeid"), sapphireConnection, queryProcessor);
                    if (editorProps.getProperty("editorstyledatatype").length() > 0) {
                        field3.setProperty("datatype", editorProps.getProperty("editorstyledatatype").equals("S") ? "string" : (editorProps.getProperty("editorstyledatatype").equals("D") ? "date" : (editorProps.getProperty("editorstyledatatype").equals("O") ? "dateonly" : (editorProps.getProperty("editorstyledatatype").equals("N") ? "number" : "string"))));
                    }
                    field3.setProperty("type", (mode = editorProps.getProperty("mode", "input")).equals("input") ? "text" : (mode.equals("lookup") ? "lookup" : (mode.equals("dropdowncombo") ? "dropdown" : (mode.equals("dropdownlist") ? "dropdown" : (mode.equals("checkbox") ? "checkbox" : (mode.equals("radiobutton/vertical") ? "radiobutton" : (mode.equals("radiobutton/horizontal") ? "radiobutton" : (mode.equals("datelookup") ? "date" : (mode.equals("password") ? "password" : (mode.equals("inputarea") ? "textarea" : "text"))))))))));
                    if (field3.getProperty("datatype").equals("date") || field3.getProperty("datatype").equals("dateonly")) {
                        field3.setProperty("type", "date");
                    }
                    if (editorProps.getProperty("displayvalue").length() > 0) {
                        field3.setProperty("values", editorProps.getProperty("displayvalue"));
                    }
                    if (editorProps.getProperty("sdcid").length() > 0) {
                        field3.setProperty("sdcid", editorProps.getProperty("sdcid"));
                    }
                    if (editorProps.getProperty("queryfrom").length() > 0) {
                        field3.setProperty("valuesqueryfrom", editorProps.getProperty("queryfrom"));
                    }
                    if (editorProps.getProperty("querywhere").length() > 0) {
                        field3.setProperty("valuesquerywhere", editorProps.getProperty("querywhere"));
                    }
                    if (editorProps.getProperty("reftypeid").length() > 0) {
                        field3.setProperty("reftypeid", editorProps.getProperty("reftypeid"));
                    }
                    if (editorProps.getProperty("sql").length() > 0) {
                        field3.setProperty("sql", editorProps.getProperty("sql"));
                    }
                    if (mode.equals("checkbox")) {
                        field3.setProperty("values", editorProps.getProperty("checkedvalue") + ";" + editorProps.getProperty("uncheckedvalue"));
                    } else if (mode.equals("radiobutton/vertical")) {
                        field3.setProperty("radioorientation", "vertical");
                    } else if (mode.equals("radiobutton/horizontal")) {
                        field3.setProperty("radioorientation", "horizontal");
                    } else if (mode.equals("lookup") && (lookuplink = editorProps.getPropertyList("lookuplink")) != null) {
                        field3.setProperty("lookuppageid", lookuplink.getProperty("href"));
                        field3.setProperty("lookupcolumns", lookuplink.getCollection("columns"));
                        field3.setProperty("lookupselectortype", lookuplink.getProperty("selectortype").equals("checkbox") ? "Checkbox" : (lookuplink.getProperty("selectortype").equals("radiobutton") ? "Radio Button" : "None"));
                        if (lookuplink.getProperty("sdcid").length() > 0) {
                            field3.setProperty("sdcid", lookuplink.getProperty("sdcid"));
                        }
                        if (lookuplink.getProperty("reftypeid").length() > 0) {
                            field3.setProperty("reftypeid", lookuplink.getProperty("reftypeid"));
                        }
                        if (lookuplink.getProperty("enablesuggest").length() > 0) {
                            field3.setProperty("enablesuggest", lookuplink.getProperty("enablesuggest"));
                            field3.setProperty("queryfrom", lookuplink.getProperty("queryfrom"));
                            field3.setProperty("querywhere", lookuplink.getProperty("querywhere"));
                        }
                        field3.setProperty("lookupoptions", lookuplink.toJSONString());
                    }
                }
                if ((type = field3.getProperty("type")).length() == 0) {
                    field3.setProperty("type", "text");
                }
                form.fieldMap.put(fieldid, field3);
                if (field3.getProperty("identityfield", "N").equals("Y")) {
                    if (form.identityFields == null) {
                        form.identityFields = new PropertyListCollection();
                    }
                    PropertyList identityfield = new PropertyList();
                    identityfield.setProperty("fieldid", fieldid);
                    form.identityFields.add(identityfield);
                    field3.setProperty("mandatory", "Y");
                }
                if (type.equals("label") || type.equals("display") || type.equals("hidden")) {
                    field3.setProperty("showtoolbar", "N");
                    field3.setProperty("readonly", "Y");
                }
                if (type.equals("hidden")) {
                    field3.setProperty("visible", "N");
                }
                if (type.equals("checkbox") && field3.getProperty("defaultvalue").length() == 0) {
                    String[] values = StringUtil.split(field3.getProperty("values"), field3.getProperty("separator", ";"));
                    if (values.length >= 2) {
                        field3.setProperty("defaultvalue", values[1].indexOf("=") > -1 ? values[1].substring(0, values[1].indexOf("=")) : values[1]);
                    } else {
                        field3.setProperty("defaultvalue", "N");
                    }
                }
                if (type.equals("file")) {
                    if (form.fileFieldMap == null) {
                        form.fileFieldMap = new HashMap();
                    }
                    form.fileFieldMap.put(fieldid, field3);
                    if (field3.getProperty("defaultvalue").length() > 0) {
                        throw new SapphireException("Field '" + fieldid + "' cannot be have a default value for security reasons!");
                    }
                }
                if (field3.getProperty("valuerule").length() > 0 && field3.getProperty("readonly", "N").equals("N")) {
                    throw new SapphireException("Field '" + fieldid + "' cannot be have a value rule as it is not readonly!");
                }
                if (field3.containsKey("bindingmode")) {
                    String bindingmode = field3.getProperty("bindingmode");
                    if (bindingmode.length() > 0) {
                        if (bindingmode.endsWith("as")) {
                            field3.setProperty("autosave", bindingmode.substring(0, bindingmode.length() - 2));
                        } else {
                            field3.setProperty("autosave", "");
                            PropertyList binding = field3.getPropertyList("binding");
                            if (binding == null) {
                                binding = new PropertyList();
                                field3.setProperty("binding", binding);
                            }
                            binding.setProperty("requestitem", bindingmode.equals("datareagent") ? "reagentrelation" : bindingmode);
                        }
                    } else {
                        field3.setProperty("autosave", "");
                        field3.setProperty("requestitem", "");
                        field3.setProperty("binding", new PropertyList());
                    }
                }
                if (field3.getProperty("autolink", "N").equals("Y") && (field3.getProperty("sdcid").length() == 0 || field3.getProperty("keyid1").length() == 0)) {
                    throw new SapphireException("SDCId and keyid1 property required for field '" + fieldid + "' with autolink enabled!");
                }
                if (field3.getProperty("autoattach", "N").equals("Y") && field3.getProperty("autosave").length() == 0 && (field3.getProperty("sdcid").length() == 0 || field3.getProperty("keyid1").length() == 0)) {
                    throw new SapphireException("SDCId and keyid1 property required for field '" + fieldid + "' with autoattach enabled!");
                }
                if (field3.getProperty("autocheck", "N").equals("Y") && field3.getProperty("sdcid").length() == 0 && field3.getProperty("reftypeid").length() == 0 && field3.getProperty("values").length() == 0) {
                    throw new SapphireException("SDCId, reftypeid or values required for field '" + fieldid + "' with autocheck enabled!");
                }
                if (field3.getProperty("datatype").equals("dateonly") && field3.getProperty("defaultvalue").indexOf("[currentdatetime]") > -1) {
                    field3.setProperty("defaultvalue", StringUtil.replaceAll(field3.getProperty("defaultvalue"), "[currentdatetime]", "[currentdate]"));
                }
                if (form.isTranslate() && field3.containsKey("title")) {
                    field3.setProperty("title", translationProcessor.translate(field3.getProperty("title")));
                }
                if (form.isTranslate() && field3.containsKey("help")) {
                    field3.setProperty("help", translationProcessor.translate(field3.getProperty("help")));
                }
                if (field3.containsKey("validation") && (field3.getCollection("validation") == null || field3.getCollection("validation").size() == 0)) {
                    field3.remove("validation");
                }
                if (field3.containsKey("maxlength") && field3.getProperty("maxlength").toLowerCase().startsWith("ddt:") && field3.getProperty("maxlength").indexOf(".") > -1) {
                    String maxlen = field3.getProperty("maxlength");
                    try {
                        maxlen = ddtService.getColumnProperty(maxlen.substring(4, maxlen.indexOf(".")).trim(), maxlen.substring(maxlen.indexOf(".") + 1).trim(), "columnlength");
                    }
                    catch (ServiceException e) {
                        maxlen = "";
                    }
                    field3.setProperty("maxlength", maxlen);
                }
                if (field3.containsKey("lookupcolumns")) {
                    field3.remove("lookupcolumns");
                }
                if (field3.containsKey("lookupselectortype")) {
                    field3.remove("lookupselectortype");
                }
                if (field3.containsKey("lookupreturnfields") && field3.getProperty("lookupreturnfields").length() == 0) {
                    field3.remove("lookupreturnfields");
                }
                if (field3.containsKey("lookupoptions") && field3.getProperty("lookupoptions").length() == 0) {
                    field3.remove("lookupoptions");
                }
                if ((attributes = field3.getCollection("attributes")) != null) {
                    PropertyList attributeMap = new PropertyList();
                    for (int j3 = 0; j3 < attributes.size(); ++j3) {
                        PropertyList attribute = attributes.getPropertyList(j3);
                        attributeMap.setProperty(attribute.getProperty("attributeid"), attribute.getProperty("attributevalue"));
                    }
                    field3.setProperty("attribute", attributeMap);
                }
                if (!type.equals("formattedtext")) continue;
                HTMLEditorControl editor = new HTMLEditorControl(new Logger(sapphireConnection.getConnectionId()));
                editor.setId(fieldid);
                editor.setInline(true);
                editor.setEditorType(HTMLEditorControl.EditorType.BASIC);
                editor.setRtl(sapphireConnection.isRtl());
                editor.setUseFullIncludes(sapphireConnection.getUseFullIncludes());
                editor.setCanUpload(true);
                editor.setWidth("100%");
                boolean devMode = Configuration.isDevmode(sapphireConnection.getDatabaseId());
                editor.setDebug(devMode);
                if (field3.getProperty("phrasetype").length() > 0) {
                    editor.setPhraseType(field3.getProperty("phrasetype"));
                }
                if (field3.getProperty("phraselookup").length() > 0) {
                    editor.setPhraseLookup(field3.getProperty("phraselookup"));
                }
                editor.setViewOnly(false);
                StringBuffer rthtml = new StringBuffer();
                rthtml.append(editor.getHtml());
                rthtml.append("<script>");
                rthtml.append(editor.getScript());
                rthtml.append("</script>");
                field3.setProperty("html", rthtml.toString());
            }
            for (i2 = 0; i2 < form.sections.size(); ++i2) {
                PropertyList section3 = form.sections.getPropertyList(i2);
                sectionid = section3.getProperty("sectionid");
                PropertyListCollection sectionfields = new PropertyListCollection();
                fields = section3.getCollection("fields");
                for (j = 0; j < fields.size(); ++j) {
                    field = form.fieldMap.get(fields.getPropertyList(j).getProperty("fieldid"));
                    field.setProperty("sectionid", sectionid);
                    if (section3.getProperty("repeatable", "N").equals("Y")) {
                        field.setProperty("repeatable", "Y");
                        if (section3.getProperty("separator").length() > 0) {
                            field.setProperty("separator", section3.getProperty("separator"));
                        }
                        if (field.getProperty("identityfield", "N").equals("Y")) {
                            throw new SapphireException("Identity field '" + field.getProperty("fieldid") + "' cannot be defined within a repeating section!");
                        }
                    }
                    sectionfields.add(field);
                }
                section3.setProperty("fields", sectionfields);
                form.sectionMap.put(sectionid, section3);
                form.defineGroovyPrecedents(form, section3, "sectionid", "readonly", section3.getProperty("readonly"));
                form.defineGroovyPrecedents(form, section3, "sectionid", "visible", section3.getProperty("visible"));
            }
            String worksheettype = form.getWorksheettype();
            if (form.isWorksheet() && worksheettype.length() > 0) {
                if (worksheettype.equalsIgnoreCase("sdi")) {
                    form.worksheetParams = new ArrayList();
                    form.worksheetParams.add("sdcid");
                    form.worksheetParams.add("keyid1");
                    form.worksheetParams.add("keyid2");
                    form.worksheetParams.add("keyid3");
                } else if (worksheettype.equalsIgnoreCase("dataset")) {
                    form.worksheetParams = new ArrayList();
                    form.worksheetParams.add("sdcid");
                    form.worksheetParams.add("keyid1");
                    form.worksheetParams.add("keyid2");
                    form.worksheetParams.add("keyid3");
                    form.worksheetParams.add("paramlistid");
                    form.worksheetParams.add("paramlistversionid");
                    form.worksheetParams.add("variantid");
                    form.worksheetParams.add("dataset");
                } else if (worksheettype.equalsIgnoreCase("workitem")) {
                    form.worksheetParams = new ArrayList();
                    form.worksheetParams.add("sdcid");
                    form.worksheetParams.add("keyid1");
                    form.worksheetParams.add("keyid2");
                    form.worksheetParams.add("keyid3");
                    form.worksheetParams.add("workitemid");
                    form.worksheetParams.add("workiteminstance");
                } else if (worksheettype.equalsIgnoreCase("qcbatch")) {
                    form.worksheetParams = new ArrayList();
                    form.worksheetParams.add("qcbatchid");
                }
            }
            block12: for (i = 0; i < form.datasources.size(); ++i) {
                PropertyList datasource = form.datasources.getPropertyList(i);
                String datasourceid = datasource.getProperty("datasourceid");
                type = datasource.getProperty("type");
                form.datasourceMap.put(datasourceid, datasource);
                if (type.equals("sdi") || type.equals("dataset")) {
                    PropertyList sdi = datasource.getPropertyList(type);
                    if (sdi == null || !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("keyid1")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("keyid2")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("keyid3")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("queryfrom")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("querywhere")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("paramlistid")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("paramlistversionid")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("variantid")) && !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", sdi.getProperty("dataset"))) continue;
                    datasource.setProperty("dynamic", "datasource");
                    PropertyListCollection precedents = datasource.getCollection("precedents");
                    for (int j4 = 0; j4 < precedents.size(); ++j4) {
                        PropertyList precedent = precedents.getPropertyList(j4);
                        if (!precedent.getProperty("instanceprecedent", "N").equals("Y")) continue;
                        datasource.setProperty("instancedatasource", "Y");
                        continue block12;
                    }
                    continue;
                }
                if (!type.equals("sql") || datasource.getPropertyList("sql") == null || !form.defineGroovyPrecedents(form, datasource, "datasourceid", "datasource", datasource.getPropertyList("sql").getProperty("select"))) continue;
                datasource.setProperty("dynamic", "datasource");
            }
            for (i = 0; i < form.fields.size(); ++i) {
                PropertyListCollection validation;
                String type2;
                String sectionDatasourceid;
                String sectionid2;
                PropertyList field4 = form.fields.getPropertyList(i);
                String fieldid = field4.getProperty("fieldid");
                String autosave = field4.getProperty("autosave");
                PropertyList binding = field4.getPropertyList("binding");
                if (binding != null) {
                    boolean setNull = true;
                    Iterator it = binding.keySet().iterator();
                    while (setNull && it.hasNext()) {
                        if (binding.getProperty((String)it.next()).length() <= 0) continue;
                        setNull = false;
                    }
                    if (setNull) {
                        binding = null;
                    }
                }
                section = (sectionid2 = field4.getProperty("sectionid")).length() > 0 ? form.getSection(sectionid2) : null;
                String string = sectionDatasourceid = section != null ? section.getProperty("datasourceid") : "";
                if (sectionDatasourceid.length() > 0 || section != null && form.datasources.size() == 1 && !form.datasources.getPropertyList(0).getProperty("type").equals("sql") || autosave.length() > 0 || binding != null && binding.size() > 0) {
                    PropertyListCollection datasourceFields;
                    if (field4.getProperty("datasourceid").length() == 0) {
                        if (sectionDatasourceid.length() > 0) {
                            field4.setProperty("datasourceid", sectionDatasourceid);
                        } else {
                            if (form.datasources.size() != 1) throw new SapphireException("Field '" + fieldid + "' is bound but does not have a datasource uniquely defined!");
                            field4.setProperty("datasourceid", form.datasources.getPropertyList(0).getProperty("datasourceid"));
                        }
                    }
                    PropertyList datasource = form.getDatasource(field4.getProperty("datasourceid"));
                    if (autosave.length() > 0) {
                        form.hasautosavefields = true;
                    } else if (!datasource.getProperty("type").equals("sql")) {
                        field4.setProperty("type", "label");
                        field4.setProperty("showtoolbar", debug ? "Y" : "N");
                    }
                    if (autosave.equals("dataentry")) {
                        field4.setProperty("fieldvalidation", "Y");
                    }
                    if ((datasourceFields = datasource.getCollection("fields")) == null) {
                        datasourceFields = new PropertyListCollection();
                        datasource.setProperty("fields", datasourceFields);
                    }
                    if (field4.getProperty("bindingmode").length() > 0 || datasource.getProperty("type").equals("sql")) {
                        String requestitem;
                        datasourceFields.add(field4);
                        String datasourceRequests = datasource.getProperty("requestitems");
                        String string2 = autosave.equals("dataentry") ? "dataset;dataitem" : (autosave.equals("dataitem") ? "dataset;dataitem" : (autosave.equals("datareagent") ? "dataset;reagentrelation" : (autosave.length() > 0 ? autosave : (requestitem = binding != null && binding.containsKey("requestitem") ? binding.getProperty("requestitem") : ""))));
                        if (datasourceRequests.indexOf(requestitem) == -1) {
                            datasource.setProperty("requestitems", datasourceRequests + ";" + requestitem);
                        }
                        if (binding != null && binding.size() > 0 && binding.containsKey("columnid") && (binding.getProperty("columnid").indexOf(".") > -1 || requestitem.equals("primary"))) {
                            String columnlistid;
                            if (binding.getProperty("columnid").indexOf(".") > -1) {
                                field4.setProperty("type", "display");
                            }
                            if ((columnlistid = (autosave.equals("dataentry") ? "dataitem" : (autosave.equals("datareagent") ? "reagentrelation" : (autosave.equals("dataitem") || autosave.equals("dataset") || autosave.equals("primary") ? autosave : (binding.containsKey("requestitem") ? binding.getProperty("requestitem") : "error")))) + "_cols").equals("error_cols")) {
                                throw new SapphireException("Field '" + fieldid + "' has a column defined but no datasource dataset!");
                            }
                            datasource.setProperty(columnlistid, "," + binding.getProperty("columnid") + datasource.getProperty(columnlistid));
                        }
                        if (section != null) {
                            section.setProperty("initialrepeats", "0");
                            String datasourceRepeater = section.getProperty("datasourcerepeater");
                            if (datasourceRepeater.length() > 0 && datasourceRequests.indexOf(datasourceRepeater) == -1) {
                                datasource.setProperty("requestitems", datasource.getProperty("requestitems") + ";" + datasourceRepeater);
                            }
                            if (section.getProperty("repeatable").equals("Y") && datasourceRepeater.length() == 0 && field4.getProperty("datasourceid").length() > 0) {
                                String actualrequestitem;
                                String string3 = autosave.equals("dataentry") ? "dataitem" : (autosave.equals("datareagent") ? "reagentrelation" : (autosave.length() > 0 ? autosave : (actualrequestitem = binding != null && binding.containsKey("requestitem") ? binding.getProperty("requestitem") : "null")));
                                if (section.getProperty("defactorepeater").length() == 0) {
                                    section.setProperty("defactorepeater", actualrequestitem);
                                } else if (!section.getProperty("defactorepeater").equals(actualrequestitem)) {
                                    throw new SapphireException("Section repeater property not defined for section '" + section.getProperty("sectionid") + "'!");
                                }
                            }
                        }
                    } else {
                        field4.setProperty("nobind", "Y");
                        datasourceFields.add(field4);
                    }
                }
                if ((type2 = field4.getProperty("type")).equals("dropdown") || type2.equals("radiobutton") || type2.equals("checkbox")) {
                    Form.defineValues(sapphireConnection, sdcProcessor, sdiProcessor, queryProcessor, field4);
                }
                form.defineGroovyPrecedents(form, field4, "fieldid", "processingfield", field4.getProperty("processingfield"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "mandatory", field4.getProperty("mandatory"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "readonly", field4.getProperty("readonly"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "visible", field4.getProperty("visible"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "valuerule", field4.getProperty("valuerule"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "values", field4.getProperty("values"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "sdcid", field4.getProperty("sdcid"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "valuesqueryfrom", field4.getProperty("valuesqueryfrom"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "valuesquerywhere", field4.getProperty("valuesquerywhere"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "sql", field4.getProperty("sql"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "reftypeid", field4.getProperty("reftypeid"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "instrumentid", field4.getProperty("instrumentid"));
                form.defineGroovyPrecedents(form, field4, "fieldid", "instrumenttypeid", field4.getProperty("instrumenttypeid"));
                if (field4.getProperty("sql").length() > 0) {
                    field4.setProperty("sql", EncryptDecrypt.obfsql(field4.getProperty("sql")));
                }
                if (field4.getProperty("valuesqueryfrom").length() > 0) {
                    field4.setProperty("valuesqueryfrom", EncryptDecrypt.obfsql(field4.getProperty("valuesqueryfrom")));
                }
                if (field4.getProperty("valuesquerywhere").length() > 0) {
                    field4.setProperty("valuesquerywhere", EncryptDecrypt.obfsql(field4.getProperty("valuesquerywhere")));
                }
                if ((validation = field4.getCollection("validation")) != null) {
                    for (int j5 = 0; j5 < validation.size(); ++j5) {
                        PropertyList validationitem = validation.getPropertyList(j5);
                        form.defineGroovyPrecedents(form, field4, "fieldid", "value1", validationitem.getProperty("value1"));
                    }
                }
                if (field4.getProperty("autocheck", "N").equals("Y")) {
                    String sdcid = field4.getProperty("sdcid");
                    String keyid1Field = field4.getProperty("keyid1");
                    String keyid2Field = field4.getProperty("keyid2");
                    String keyid3Field = field4.getProperty("keyid3");
                    if (sdcid.length() > 0 && keyid1Field.length() > 0 && keyid1Field.equals(fieldid)) {
                        int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                        PropertyListCollection keyset = new PropertyListCollection();
                        if (keycols > 1 && keyid2Field.length() > 0) {
                            field4.setProperty("keyset", keyset);
                            PropertyList keyid1 = new PropertyList();
                            keyid1.setProperty("fieldid", keyid1Field);
                            keyset.add(keyid1);
                            PropertyList keyid2 = new PropertyList();
                            keyid2.setProperty("fieldid", keyid2Field);
                            keyset.add(keyid2);
                            form.getField(keyid2Field).setProperty("keyset", keyset);
                            form.getField(keyid2Field).setProperty("sdcid", sdcid);
                            form.getField(keyid2Field).setProperty("autocheck", "Y");
                        }
                        if (keycols > 2 && keyid3Field.length() > 0) {
                            PropertyList keyid3 = new PropertyList();
                            keyid3.setProperty("fieldid", keyid3Field);
                            keyset.add(keyid3);
                            form.getField(keyid3Field).setProperty("keyset", keyset);
                            form.getField(keyid3Field).setProperty("sdcid", sdcid);
                            form.getField(keyid3Field).setProperty("autocheck", "Y");
                        }
                    }
                }
                if (field4.getProperty("identityfield", "N").equals("Y")) {
                    field4.setProperty("identityset", form.identityFields);
                }
                if (field4.getProperty("processingfield").startsWith("$G{") || field4.getProperty("mandatory").startsWith("$G{") || field4.getProperty("readonly").startsWith("$G{") || field4.getProperty("visible").startsWith("$G{") || field4.getCollection("dependents") != null && field4.getCollection("dependents").size() > 0 || !field4.getProperty("dynamic", "N").equals("N")) {
                    field4.setProperty("fieldevaluation", "Y");
                }
                if (field4.getProperty("processingfield").startsWith("$G{") || !field4.getProperty("mandatory", "N").equals("N") || field4.getProperty("mandatory").startsWith("$G{") || field4.getProperty("readonly").startsWith("$G{") || field4.getProperty("visible").startsWith("$G{") || field4.getProperty("dynamic", "N").equals("Y") || field4.getProperty("datatype").equals("date") || field4.getProperty("datatype").equals("number") || field4.getProperty("type").equals("date") || !field4.getProperty("autocheck", "N").equals("N") || field4.getProperty("autosave").equals("dataentry") || field4.getCollection("validation") != null && field4.getCollection("validation").size() != 0 || field4.getCollection("dependents") != null && field4.getCollection("dependents").size() != 0 || field4.getCollection("identityset") != null && field4.getCollection("identityset").size() != 0 || field4.getCollection("keyset") != null && field4.getCollection("keyset").size() != 0) continue;
                field4.setProperty("fieldvalidation", "N");
            }
            for (i = 0; i < form.groups.size(); ++i) {
                PropertyList group = form.groups.getPropertyList(i);
                if (form.isTranslate() && group.containsKey("title")) {
                    group.setProperty("title", translationProcessor.translate(group.getProperty("title")));
                }
                String groupid = group.getProperty("groupid");
                PropertyListCollection memberfields = new PropertyListCollection();
                PropertyListCollection members = group.getCollection("members");
                for (int j6 = 0; j6 < members.size(); ++j6) {
                    PropertyList field5 = form.fieldMap.get(members.getPropertyList(j6).getProperty("fieldid"));
                    memberfields.add(field5);
                }
                group.setProperty("members", memberfields);
                form.groupMap.put(groupid, group);
                form.defineGroovyPrecedents(form, group, "groupid", "visible", group.getProperty("visible"));
                form.defineGroovyPrecedents(form, group, "groupid", "readonly", group.getProperty("readonly"));
            }
            for (i = 0; i < form.pages.size(); ++i) {
                PropertyListCollection sections;
                PropertyList page = form.pages.getPropertyList(i);
                String pageid = page.getProperty("pageid");
                form.pageMap.put(pageid, page);
                form.defineGroovyPrecedents(form, page, "pageid", "visible", page.getProperty("visible"));
                form.defineGroovyPrecedents(form, page, "pageid", "readonly", page.getProperty("readonly"));
                fields = page.getCollection("fields");
                if (fields != null) {
                    for (j = 0; j < fields.size(); ++j) {
                        field = fields.getPropertyList(j);
                        form.getField(field.getProperty("fieldid")).setProperty("pageid", pageid);
                    }
                }
                if ((sections = page.getCollection("sections")) == null) continue;
                for (int j7 = 0; j7 < sections.size(); ++j7) {
                    section = sections.getPropertyList(j7);
                    form.getSection(section.getProperty("sectionid")).setProperty("pageid", pageid);
                }
            }
            for (i = 0; i < form.elements.size(); ++i) {
                PropertyList element = form.elements.getPropertyList(i);
                String elementid = element.getProperty("elementid");
                form.elementMap.put(elementid, element);
                form.defineGroovyPrecedents(form, element, "elementid", "visible", element.getProperty("visible"));
                form.defineGroovyPrecedents(form, element, "elementid", "readonly", element.getProperty("readonly"));
                form.defineGroovyPrecedents(form, element, "elementid", "color", element.getProperty("color"));
                form.defineGroovyPrecedents(form, element, "elementid", "class", element.getProperty("class"));
            }
        }
        if ((translations = StringUtil.getTokens(form.getFormLayout(), "{{", "}}")) != null && translations.length > 0) {
            String languageid = sapphireConnection.getLanguage();
            if (form.isTranslate() && languageid != null && languageid.length() > 0) {
                int i;
                HashMap<String, String> transtext = new HashMap<String, String>();
                for (i = 0; i < translations.length; ++i) {
                    transtext.put(translations[i], translations[i]);
                }
                translationProcessor.translateTable(languageid, transtext);
                for (i = 0; i < translations.length; ++i) {
                    form.setFormLayout(StringUtil.replaceAll(form.getFormLayout(), "{{" + translations[i] + "}}", (String)transtext.get(translations[i])));
                }
            } else {
                for (int i = 0; i < translations.length; ++i) {
                    form.setFormLayout(StringUtil.replaceAll(form.getFormLayout(), "{{" + translations[i] + "}}", translations[i]));
                }
            }
        }
        String tagstart = "<div ";
        for (int i = 0; i < form.pages.size(); ++i) {
            String layoutLower = form.formLayout.toLowerCase();
            PropertyList page = form.pages.getPropertyList(i);
            int sapphirePos = Form.findStartOfTag(tagstart, form.formLayout, "id=\"" + page.getProperty("pageid") + "shadow\"", 0);
            if (sapphirePos <= -1) continue;
            int pagedivpos = form.formLayout.indexOf("</div>", sapphirePos) + 6;
            page.setProperty("width", Form.findCSSValue(layoutLower, "width:", pagedivpos));
            page.setProperty("height", Form.findCSSValue(layoutLower, "height:", pagedivpos));
            page.setProperty("margin-top", Form.findCSSValue(layoutLower, "padding-top:", pagedivpos));
            page.setProperty("margin-bottom", Form.findCSSValue(layoutLower, "padding-bottom:", pagedivpos));
            page.setProperty("margin-left", Form.findCSSValue(layoutLower, "padding-left:", pagedivpos));
            page.setProperty("margin-right", Form.findCSSValue(layoutLower, "padding-right:", pagedivpos));
            form.setFormLayout(form.formLayout.substring(0, sapphirePos) + form.formLayout.substring(form.formLayout.indexOf("</div>", sapphirePos) + 6));
        }
        form.setFormLayout(StringUtil.replaceAll(form.formLayout, "contenteditable=\"true\"", ""));
        form.logger.info("Processing took " + (System.currentTimeMillis() - start) + "ms");
        return form;
    }

    public static String findCSSValue(String layout, String cssattribute, int startpos) {
        int pos = layout.indexOf(cssattribute, startpos);
        int endpos = layout.indexOf(";", pos + cssattribute.length());
        if (endpos == -1 && (endpos = layout.indexOf("\"", pos + cssattribute.length())) == -1) {
            endpos = layout.indexOf(" ", pos + cssattribute.length());
        }
        if (endpos != -1) {
            String value = layout.substring(pos + cssattribute.length(), endpos);
            if ((pos = value.indexOf(" !important")) > -1) {
                value = value.substring(0, pos);
            }
            if ((pos = value.indexOf("\"")) > -1) {
                value = value.substring(0, pos);
            }
            return value.trim();
        }
        return "";
    }

    public static int findStartOfTag(String tagstart, String html, String initialFind, int startPos) {
        int pos = html.indexOf(initialFind, startPos);
        pos -= tagstart.length();
        while (pos > -1 && !html.substring(pos, pos + tagstart.length()).equals(tagstart)) {
            --pos;
        }
        return pos;
    }

    public static String[] getGroovy(String input) {
        String starttoken = "$G{";
        String endtoken = "}";
        ArrayList<String> tokenlist = new ArrayList<String>();
        int startoffset = starttoken.length();
        if (input != null && input.length() > 0) {
            int pos2;
            int pos1;
            int searchfrom = 0;
            while ((pos1 = input.indexOf(starttoken, searchfrom)) >= 0 && (pos2 = input.indexOf(endtoken, pos1 + 1)) > 0) {
                if (input.substring(pos1 + startoffset, pos2).indexOf("{") > -1) {
                    pos2 = input.indexOf(endtoken, pos2 + 1);
                }
                searchfrom = pos2 + 1;
                String newvalue = input.substring(pos1 + startoffset, pos2);
                if (tokenlist.contains(newvalue)) continue;
                tokenlist.add(newvalue);
            }
        }
        return tokenlist.toArray(new String[tokenlist.size()]);
    }

    public static String defineValues(SapphireConnection sapphireConnection, String sdcid, String sql, String reftypeid, String values, String queryfrom, String querywhere) {
        PropertyList field = new PropertyList();
        field.setProperty("sdcid", sdcid);
        field.setProperty("sql", sql);
        field.setProperty("reftypeid", reftypeid);
        field.setProperty("values", values);
        field.setProperty("valuesqueryfrom", queryfrom);
        field.setProperty("valuesquerywhere", querywhere);
        Form.defineValues(sapphireConnection, null, null, null, field);
        return field.getProperty("values");
    }

    private static void defineValues(SapphireConnection sapphireConnection, SDCProcessor sdcProcessor, SDIProcessor sdiProcessor, QueryProcessor queryProcessor, PropertyList field) {
        String values = field.getProperty("values");
        String sdcid = field.getProperty("sdcid");
        String reftypeid = field.getProperty("reftypeid");
        String sql = field.getProperty("sql");
        String queryfrom = field.getProperty("valuesqueryfrom");
        String querywhere = field.getProperty("valuesquerywhere");
        String separator = field.getProperty("separator", ";");
        if (values.length() == 0) {
            StringBuffer valuelist = new StringBuffer();
            if (sql.length() > 0) {
                if (sql.startsWith("$G{") && sql.endsWith("}")) {
                    field.setProperty("dynamic", "sql");
                } else {
                    int pos;
                    if (queryProcessor == null) {
                        queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
                    }
                    sql = EncryptDecrypt.unobfsql(sql);
                    DataSet sqlvalues = null;
                    sqlvalues = sql.startsWith("bindS@L:") && sql.endsWith("]]") ? ((pos = sql.indexOf(":v@rs[[")) > 0 ? queryProcessor.getPreparedSqlDataSet(sql.substring(8, pos), (Object[])StringUtil.split(sql.substring(pos + 7, sql.lastIndexOf("]]")), "]];[[")) : queryProcessor.getSqlDataSet(sql.substring(8))) : queryProcessor.getSqlDataSet(sql);
                    if (sqlvalues != null) {
                        String[] columns = sqlvalues.getColumns();
                        for (int i = 0; i < sqlvalues.size(); ++i) {
                            valuelist.append(separator).append(sqlvalues.getValue(i, columns[0])).append(columns.length > 1 ? "=" + sqlvalues.getValue(i, columns[1]) : "");
                        }
                    } else {
                        valuelist.append(separator).append("<font color=\"Red\">Failed to get sql values</font>");
                    }
                }
            } else if (sdcid.length() > 0) {
                if (sdcid.startsWith("$G{") && sdcid.endsWith("}") || queryfrom.startsWith("$G{") && queryfrom.endsWith("}") || querywhere.startsWith("$G{") && querywhere.endsWith("}")) {
                    field.setProperty("dynamic", "sdcid");
                } else {
                    if (sdcProcessor == null) {
                        sdcProcessor = new SDCProcessor(sapphireConnection.getConnectionId());
                    }
                    if (sdiProcessor == null) {
                        sdiProcessor = new SDIProcessor(sapphireConnection.getConnectionId());
                    }
                    PropertyList sdcProps = sdcProcessor.getProperties(sdcid);
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid(sdcid);
                    sdiRequest.setQueryFrom(sdcProps.getProperty("tableid") + (queryfrom.length() > 0 ? "," + EncryptDecrypt.unobfsql(queryfrom) : ""));
                    if (querywhere.length() > 0) {
                        sdiRequest.setQueryWhere(EncryptDecrypt.unobfsql(querywhere));
                    }
                    sdiRequest.setQueryOrderBy(sdcProps.getProperty("keycolid1"));
                    sdiRequest.setRequestItem("primary");
                    SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
                    if (sdiData != null && sdiData.getDataset("primary") != null) {
                        DataSet sdis = sdiData.getDataset("primary");
                        for (int i = 0; i < sdis.size(); ++i) {
                            valuelist.append(separator).append(sdis.getValue(i, sdcProps.getProperty("keycolid1")));
                        }
                    } else {
                        valuelist.append(separator).append("<font color=\"Red\">Failed to get sdc values</font>");
                    }
                }
            } else if (reftypeid.length() > 0) {
                if (reftypeid.startsWith("$G{") && reftypeid.endsWith("}")) {
                    field.setProperty("dynamic", "reftypeid");
                } else {
                    DataSet refvalues;
                    if (queryProcessor == null) {
                        queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
                    }
                    if ((refvalues = queryProcessor.getRefTypeDataSet(reftypeid)).size() > 0) {
                        for (int i = 0; i < refvalues.size(); ++i) {
                            valuelist.append(separator).append(refvalues.getValue(i, "refvalueid")).append("=").append(refvalues.getValue(i, "refdisplayvalue"));
                        }
                    } else {
                        valuelist.append(separator).append("<font color=\"Red\">Failed to get reftype values</font>");
                    }
                }
            }
            field.setProperty("values", valuelist.length() > 0 ? valuelist.substring(separator.length()) : "");
        } else if (values.startsWith("$G{") && values.endsWith("}")) {
            field.setProperty("dynamic", "values");
        }
    }

    private boolean defineGroovyPrecedents(Form form, PropertyList expressionSource, String dependentTypeid, String propertyid, String propertyvalue) throws SapphireException {
        if (propertyvalue.length() > 0 && propertyvalue.startsWith("$G{") && propertyvalue.endsWith("}") && !propertyvalue.contains("worksheet.field")) {
            PropertyListCollection precedents = expressionSource.getCollection("precedents");
            if (precedents == null) {
                precedents = new PropertyListCollection();
            }
            this.definePrecedents(form, expressionSource, dependentTypeid, propertyid, propertyvalue, "fields.", precedents, false);
            this.definePrecedents(form, expressionSource, dependentTypeid, propertyid, propertyvalue, "fieldinstance.", precedents, true);
            expressionSource.setProperty("precedents", precedents);
            if (precedents.size() == 0) {
                expressionSource.setProperty("expression", "Y");
            }
            return true;
        }
        return false;
    }

    private void definePrecedents(Form form, PropertyList expressionSource, String dependentTypeid, String propertyid, String propertyvalue, String token, PropertyListCollection precedents, boolean fieldinstancePrecedent) throws SapphireException {
        int pos = propertyvalue.indexOf(token);
        while (pos >= 0 && pos < propertyvalue.length()) {
            int start = pos += token.length();
            if (Character.isJavaIdentifierStart(propertyvalue.charAt(pos))) {
                ++pos;
                while (pos < propertyvalue.length() && Character.isJavaIdentifierPart(propertyvalue.charAt(pos))) {
                    ++pos;
                }
                String precedentFieldid = propertyvalue.substring(start, pos);
                boolean defined = false;
                for (int i = 0; i < precedents.size() && !defined; ++i) {
                    PropertyList precedent = precedents.getPropertyList(i);
                    if (!precedent.getProperty("fieldid").equals(precedentFieldid) || !precedent.getProperty("functionid").equals(propertyid)) continue;
                    defined = true;
                }
                if (!defined) {
                    PropertyList precedent = new PropertyList();
                    precedent.setProperty("fieldid", precedentFieldid);
                    precedent.setProperty("instanceprecedent", fieldinstancePrecedent ? "Y" : "N");
                    precedent.setProperty("functionid", propertyid);
                    precedents.add(precedent);
                    PropertyList precedentField = form.getField(precedentFieldid);
                    if (precedentField != null) {
                        precedentField.setProperty("fieldvalidation", "Y");
                        PropertyListCollection dependents = precedentField.getCollection("dependents");
                        if (dependents == null) {
                            dependents = new PropertyListCollection();
                        }
                        PropertyList dependent = new PropertyList();
                        dependent.setProperty(dependentTypeid, expressionSource.getProperty(dependentTypeid));
                        dependent.setProperty("dependenttype", dependentTypeid.equals("fieldid") ? "field" : (dependentTypeid.equals("groupid") ? "group" : (dependentTypeid.equals("sectionid") ? "section" : (dependentTypeid.equals("pageid") ? "page" : (dependentTypeid.equals("datasourceid") ? "datasource" : (dependentTypeid.equals("elementid") ? "element" : "error"))))));
                        if (dependent.getProperty("dependenttype").equals("error")) {
                            Logger.logError("Unrecognized dependent type: " + dependentTypeid);
                        }
                        dependent.setProperty("functionid", propertyid);
                        if (precedentField.getProperty("repeatable", "N").equals("N")) {
                            dependent.setProperty("repeateddependent", expressionSource.getProperty("repeatable", "N"));
                        }
                        dependents.add(dependent);
                        precedentField.setProperty("dependents", dependents);
                    } else {
                        throw new SapphireException("Failed to find dependent field '" + precedentFieldid + "' in " + propertyvalue);
                    }
                }
            }
            pos = propertyvalue.indexOf(token, pos);
        }
    }

    public void setOverrides(PropertyList overrides) {
        if (overrides != null && overrides.size() > 0) {
            this.setDde(overrides.getProperty("dde", this.isDde() ? "Y" : "N").equals("Y"));
        }
    }

    public void setApprovetypeid(String approvaltypeid) {
        this.approvaltypeid = approvaltypeid;
    }

    public String getApprovaltypeid() {
        return this.approvaltypeid;
    }

    public boolean isApprovable() {
        return this.getApprovaltypeid() != null && this.getApprovaltypeid().length() > 0;
    }

    public boolean isVersionable() {
        return this.versionable;
    }

    public void setVersionable(String versionable) {
        this.versionable = versionable.equals("Y");
    }

    public boolean isTaskForm() {
        return this.taskForm;
    }

    public boolean isFormlet() {
        return this.formlet;
    }

    public boolean isWorksheetitemForm() {
        return this.worksheetitemForm;
    }

    public String getFormid() {
        return this.formid;
    }

    public void setFormid(String formid) {
        this.formid = formid;
    }

    public String getFormversionid() {
        return this.formversionid;
    }

    public void setFormversionid(String formversionid) {
        this.formversionid = formversionid;
    }

    public String getFormletid() {
        return this.formletid;
    }

    public void setFormletid(String formletid) {
        this.formletid = formletid;
    }

    public String getFormletversionid() {
        return this.formletversionid;
    }

    public void setFormletversionid(String formletversionid) {
        this.formletversionid = formletversionid;
    }

    public String getWorksheetitemid() {
        return this.worksheetitemid;
    }

    public void setWorksheetitemid(String worksheetitemid) {
        this.worksheetitemid = worksheetitemid;
    }

    public String getWorksheetitemversionid() {
        return this.worksheetitemversionid;
    }

    public void setWorksheetitemversionid(String worksheetitemversionid) {
        this.worksheetitemversionid = worksheetitemversionid;
    }

    public String getTaskdefid() {
        return this.taskdefid;
    }

    public void setTaskdefid(String taskdefid) {
        this.taskdefid = taskdefid;
    }

    public String getTaskdefversionid() {
        return this.taskdefversionid;
    }

    public void setTaskdefversionid(String taskdefversionid) {
        this.taskdefversionid = taskdefversionid;
    }

    public String getTaskdefvariantid() {
        return this.taskdefvariantid;
    }

    public void setTaskdefvariantid(String taskdefvariantid) {
        this.taskdefvariantid = taskdefvariantid;
    }

    public String getStepid() {
        return this.stepid;
    }

    public void setStepid(String stepid) {
        this.stepid = stepid;
    }

    public String getVersionstatus() {
        return this.versionstatus;
    }

    public void setVersionstatus(String versionstatus) {
        this.versionstatus = versionstatus;
    }

    public String getFormtitle() {
        return this.formtitle;
    }

    public void setFormtitle(String formtitle) {
        this.formtitle = formtitle;
    }

    public void setFormdesc(String formdesc) {
        this.formdesc = formdesc;
    }

    public boolean isDraftable() {
        return this.draftable;
    }

    public void setDraftable(boolean draftable) {
        this.draftable = draftable;
    }

    public boolean isSubmitable() {
        return this.submitable;
    }

    public void setSubmitable(boolean submitable) {
        this.submitable = submitable;
    }

    public boolean isCheckable() {
        return this.checkable;
    }

    public void setCheckable(boolean checkable) {
        this.checkable = checkable;
    }

    public boolean isValidateOnSave() {
        return this.validateOnSave;
    }

    public void setValidateOnSave(boolean validateOnSave) {
        this.validateOnSave = validateOnSave;
    }

    public boolean isSaveInvalidData() {
        return this.saveInvalidData;
    }

    public void setSaveInvalidData(boolean saveInvalidData) {
        this.saveInvalidData = saveInvalidData;
    }

    public boolean isSubmitInvalidData() {
        return this.submitInvalidData;
    }

    public void setSubmitInvalidData(boolean submitInvalidData) {
        this.submitInvalidData = submitInvalidData;
    }

    public boolean isApproveInvalidData() {
        return this.approveInvalidData;
    }

    public void setApproveInvalidData(boolean approveInvalidData) {
        this.approveInvalidData = approveInvalidData;
    }

    public boolean isDde() {
        return this.dde;
    }

    public void setDde(boolean dde) {
        this.dde = dde;
    }

    public boolean isDdeuser2alert() {
        return this.ddeuser2alert;
    }

    public void setDdeuser2alert(boolean ddeuser2alert) {
        this.ddeuser2alert = ddeuser2alert;
    }

    public boolean isDdeuser2recon() {
        return this.ddeuser2recon;
    }

    public void setDdeuser2recon(boolean ddeuser2recon) {
        this.ddeuser2recon = ddeuser2recon;
    }

    public boolean isLockondone() {
        return this.lockondone;
    }

    public void setLockondone(boolean lockondone) {
        this.lockondone = lockondone;
    }

    public void setAsyncprocessing(boolean asyncprocessing) {
        this.asyncprocessing = asyncprocessing;
    }

    public void setVersionable(boolean versionable) {
        this.versionable = versionable;
    }

    public boolean isDefaultprocessingfields() {
        return this.defaultprocessingfields;
    }

    public String getReconciliationroleid() {
        return this.reconciliationroleid;
    }

    public void setReconciliationroleid(String reconciliationroleid) {
        this.reconciliationroleid = reconciliationroleid;
    }

    public String getDocumentmanagerroleid() {
        return this.documentmanagerroleid;
    }

    public void setDocumentmanagerroleid(String documentmanagerroleid) {
        this.documentmanagerroleid = documentmanagerroleid;
    }

    public boolean isDocumentManager(String rolelist) {
        return this.getDocumentmanagerroleid().length() > 0 && (";" + rolelist + ";").indexOf(this.getDocumentmanagerroleid()) > -1;
    }

    public PropertyListCollection getFields() {
        return this.fields;
    }

    public void setFields(PropertyListCollection fields) {
        this.fields = fields;
    }

    public HashMap<String, HashMap> getFormMap() {
        HashMap<String, HashMap> formMap = new HashMap<String, HashMap>();
        formMap.put("fields", this.getFieldMap());
        formMap.put("groups", this.getGroupMap());
        formMap.put("sections", this.getSectionMap());
        return formMap;
    }

    public HashMap getFieldMap() {
        return this.fieldMap;
    }

    public HashMap getGroupMap() {
        return this.groupMap;
    }

    public HashMap getSectionMap() {
        return this.sectionMap;
    }

    public HashMap getFileFieldMap() {
        return this.fileFieldMap;
    }

    public PropertyList getDatasource(String datasourceid) {
        return this.datasourceMap.get(datasourceid);
    }

    public PropertyList getElement(String elementid) {
        return this.elementMap.get(elementid);
    }

    @Override
    public PropertyList getField(String fieldid) {
        return this.fieldMap.get(fieldid);
    }

    public PropertyList getGroup(String groupid) {
        return this.groupMap.get(groupid);
    }

    public PropertyList getSection(String sectionid) {
        return this.sectionMap.get(sectionid);
    }

    public PropertyList getPage(String pageid) {
        return this.pageMap.get(pageid);
    }

    public PropertyListCollection getPages() {
        return this.pages;
    }

    public void setPages(PropertyListCollection pages) {
        this.pages = pages;
    }

    public void setDatasources(PropertyListCollection datasources) {
        this.datasources = datasources;
    }

    public PropertyListCollection getDatasources() {
        return this.datasources;
    }

    public PropertyListCollection getElements() {
        return this.elements;
    }

    public void setElements(PropertyListCollection elements) {
        this.elements = elements;
    }

    public PropertyListCollection getGroups() {
        return this.groups;
    }

    public void setGroups(PropertyListCollection groups) {
        this.groups = groups;
    }

    public PropertyListCollection getSections() {
        return this.sections;
    }

    public void setSections(PropertyListCollection sections) {
        this.sections = sections;
    }

    public String getFormLayout() {
        return this.formLayout;
    }

    public void setFormLayout(String formLayout) {
        this.formLayout = formLayout;
    }

    public String getProcessingType() {
        return this.processingType != null && this.processingType.length() > 0 ? this.processingType : "G";
    }

    public void setProcessingType(String processingType) {
        this.processingType = processingType;
    }

    public String getProcessingRule() {
        return this.processingRule;
    }

    public void setProcessingRule(String processingRule) {
        this.processingRule = processingRule;
    }

    public boolean hasProcessing() {
        return this.processingRule != null && this.processingRule.length() > 0;
    }

    public boolean hasAutoSaveFields() {
        return this.hasautosavefields;
    }

    public String getDocumentDescRule() {
        return this.documentDescRule;
    }

    public void setDocumentDescRule(String documentDescRule) {
        this.documentDescRule = documentDescRule;
    }

    public boolean isTransientForm() {
        return this.transientform;
    }

    public void setTransientform(boolean transientform) {
        this.transientform = transientform;
    }

    public boolean isTranslate() {
        return this.translate;
    }

    public void setTranslate(boolean translate) {
        this.translate = translate;
    }

    public boolean isWorksheet() {
        return this.worksheet;
    }

    public void setWorksheet(boolean worksheet) {
        this.worksheet = worksheet;
    }

    public int getWorksheetqty() {
        return this.worksheetqty;
    }

    public void setWorksheetqty(int worksheetqty) {
        this.worksheetqty = worksheetqty;
    }

    public String getWorksheettype() {
        return this.worksheettype;
    }

    public void setWorksheettype(String worksheettype) {
        this.worksheettype = worksheettype;
    }

    public ArrayList<String> getWorksheetParams() {
        return this.worksheetParams;
    }

    public boolean isTrainingrequired() {
        return this.trainingrequired;
    }

    public void setTrainingrequired(boolean trainingrequired) {
        this.trainingrequired = trainingrequired;
    }

    public boolean isOverrideallowed() {
        return this.overrideallowed;
    }

    public void setOverrideallowed(boolean overrideallowed) {
        this.overrideallowed = overrideallowed;
    }

    public boolean isTrainingexists() {
        return this.trainingexists;
    }

    public String getAttachmentslocation() {
        return this.attachmentslocation != null ? this.attachmentslocation : "";
    }

    public PropertyList getFormPropertyList() {
        PropertyList formProps = new PropertyList();
        formProps.setProperty("formid", this.getFormid());
        formProps.setProperty("formversionid", this.getFormversionid());
        formProps.setProperty("formletid", this.getFormletid());
        formProps.setProperty("formletversionid", this.getFormletversionid());
        formProps.setProperty("worksheetitemid", this.getWorksheetitemid());
        formProps.setProperty("worksheetitemversionid", this.getWorksheetitemversionid());
        formProps.setProperty("taskdefid", this.getTaskdefid());
        formProps.setProperty("taskdefversionid", this.getTaskdefversionid());
        formProps.setProperty("taskdefvariantid", this.getTaskdefvariantid());
        formProps.setProperty("stepid", this.getStepid());
        formProps.setProperty("versionstatus", this.getVersionstatus());
        formProps.setProperty("formtitle", this.getFormtitle());
        formProps.setProperty("transient", this.isTransientForm() ? "Y" : "N");
        formProps.setProperty("translate", this.isTranslate() ? "Y" : "N");
        formProps.setProperty("submitable", this.isSubmitable() ? "Y" : "N");
        formProps.setProperty("submitinvaliddata", this.isSubmitInvalidData() ? "Y" : "N");
        formProps.setProperty("approveinvaliddata", this.isApproveInvalidData() ? "Y" : "N");
        formProps.setProperty("draftable", this.isDraftable() ? "Y" : "N");
        formProps.setProperty("checkable", this.isCheckable() ? "Y" : "N");
        formProps.setProperty("approvable", this.isApprovable() ? "Y" : "N");
        formProps.setProperty("versionable", this.isVersionable() ? "Y" : "N");
        formProps.setProperty("dde", this.isDde() ? "Y" : "N");
        formProps.setProperty("ddeuser2alert", this.isDdeuser2alert() ? "Y" : "N");
        formProps.setProperty("ddeuser2recon", this.isDdeuser2recon() ? "Y" : "N");
        formProps.setProperty("approvaltypeid", this.getApprovaltypeid());
        formProps.setProperty("reconciliationroleid", this.getReconciliationroleid());
        formProps.setProperty("documentmanagerroleid", this.getDocumentmanagerroleid());
        formProps.setProperty("worksheet", this.isWorksheet() ? "Y" : "N");
        formProps.setProperty("worksheettype", this.getWorksheettype());
        formProps.setProperty("worksheetqty", String.valueOf(this.getWorksheetqty()));
        formProps.setProperty("trainingrequired", this.isTrainingrequired() ? "Y" : "N");
        formProps.setProperty("overrideallowed", this.isOverrideallowed() ? "Y" : "N");
        formProps.setProperty("trainingexists", this.isTrainingexists() ? "Y" : "N");
        formProps.setProperty("attachmentstoretype", this.attachmentsbyref ? "U" : "S");
        formProps.setProperty("attachmentstorelocation", this.attachmentsbyref ? this.getAttachmentslocation() : "");
        PropertyList formOptions = this.formObjects.getPropertyList("options");
        if (formOptions != null) {
            for (String option : formOptions.keySet()) {
                formProps.setProperty(option, formOptions.getProperty(option));
            }
        }
        return formProps;
    }

    public void dump() {
        PropertyList formProps = new PropertyList();
        formProps.setProperty("form", this.getFormPropertyList());
        formProps.setProperty("fields", this.getFields());
        formProps.setProperty("groups", this.getGroups());
        formProps.setProperty("sections", this.getSections());
        formProps.setProperty("pages", this.getPages());
        this.logger.info(formProps.toXMLString());
    }
}

