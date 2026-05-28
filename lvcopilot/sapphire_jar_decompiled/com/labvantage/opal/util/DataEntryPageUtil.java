/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletRequest
 *  javax.servlet.jsp.JspWriter
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.KeyidListComparator;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataEntryPageUtil
extends BaseCustom {
    public String sColTitle = "";
    public String sColName = "";
    public String sColShowVal = "";
    public String sColId = "";
    public String sColSave = "";
    public String sColSdcId = "";
    public String sColShow = "";
    public String sColType = "";
    public String sColLookupURL = "";
    public String sColTypeVal = "";
    public String sColData = "";
    public String sColWidth = "";
    public String sColAlign = "";
    public String sColSize = "";
    public String sColMaxLen = "";
    public String sColTab = "";
    public String sColLink = "";
    public String sColDefault = "";
    private String[] aColTitle = null;
    private String[] aColName = null;
    private String[] aColId = null;
    private String[] aColSave = null;
    private String[] aColSdcid = null;
    private String[] aColShow = null;
    private String[] aColType = null;
    private String[] aColTypeVal = null;
    private String[] aColData = null;
    private String[] aColWidth = null;
    private String[] aColSize = null;
    private String[] aColMaxLen = null;
    private String[] aColLink = null;
    private String[] aColTab = null;
    private String[] aColDefault = null;
    private String[] aColLookupURL = null;
    private static final String YES = "Y";
    private static String MISSRESTCLASS = "Y";
    private static String MISSSAMPLETYPE = "Y";
    private static String MISSSUPPCLINDIAGNOSIS = "Y";
    private static String MISSCOLLDATE = "Y";
    private static String MISSTISSUEID = "Y";
    private static String SUBEXTALIASMISSMATCH = "Y";
    private static String MISSCLINICALSITE = "Y";
    private static String MISSCLINICALEVENT = "Y";
    private static String MISSSUBJECTALIAS = "Y";
    boolean isSubmission = false;
    int maxcol = 0;
    boolean isOracle = true;
    public static final SimpleDateFormat dateOnlyFormatter = new SimpleDateFormat("MMM d, yyyy");
    private String studydisplaycolumn = "s_studyid";
    private String studylinkurl = "";
    private PageContext pageContext = null;
    private String sampletypeid;
    private String sdcid = "";
    private String keyid1 = "";
    private String cPage = "";
    private String pageMode = "";
    private DataSet dsClinicalEvent = null;
    private DataSet ds = null;
    private boolean isProtocol = false;
    private String studyError = "";
    private boolean isTaskPage = false;
    private boolean allowSingleSampleEdit = false;
    private JSONObject jsonDropDown;
    private String sampleiddisplaycolumn = "s_sampleid";
    private Map<String, String> localcache = new HashMap<String, String>();
    private Map<String, Object> ddCache = new HashMap<String, Object>();
    Map<String, Map<String, String>> sdidesccache;

    public DataEntryPageUtil(PageContext pageContext) {
        this.setConnectionId(HttpUtil.getConnectionId(pageContext));
        this.pageContext = pageContext;
        this.isOracle = this.getConnectionProcessor().isOra();
    }

    public DataEntryPageUtil(PropertyList pagedata, PageContext pageContext) {
        this.isTaskPage = YES.equals(pagedata.getProperty("istaskpage", "N"));
        this.allowSingleSampleEdit = YES.equals(pagedata.getProperty("allowsingleedit", YES));
        this.setConnectionId(HttpUtil.getConnectionId(pageContext));
        this.isOracle = this.getConnectionProcessor().isOra();
        this.pageContext = pageContext;
        this.jsonDropDown = new JSONObject();
        this.studydisplaycolumn = pagedata.getProperty("studydisplaycolumn", "s_studyid");
        this.studylinkurl = pagedata.getProperty("studylinkurl", "").trim();
        this.sdcid = pagedata.getProperty("sdcid");
        this.keyid1 = pagedata.getProperty("keyid1").replaceAll("%3B", ";");
        this.cPage = pagedata.getProperty("page");
        this.pageMode = pagedata.getProperty("mode");
        this.sampleiddisplaycolumn = pagedata.getProperty("sampleiddisplaycolumn", "s_sampleid");
        if (this.keyid1.length() > 0) {
            String u_studyid = "";
            StringBuilder sql = new StringBuilder();
            sql.append("select distinct sstudyid, (select s_study.clinicalflag from s_study where s_study.s_studyid = s_sample.sstudyid) clinicalflag");
            sql.append(" from s_sample");
            SafeSQL safeSQL = new SafeSQL();
            sql.append(" where s_sampleid in (").append(safeSQL.addIn(this.keyid1, ";")).append(")");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                u_studyid = ds.getColumnValues("sstudyid", ";");
                String clinicalflag = ds.getString(0, "clinicalflag", "N");
                for (int i = 1; i < ds.size(); ++i) {
                    if (ds.getString(i, "clinicalflag", "N").equals(clinicalflag)) continue;
                    this.studyError = this.getTranslationProcessor().translate("All samples must belong to either Non-protocol study or Protocol driven Study");
                }
                this.isProtocol = YES.equals(clinicalflag);
            }
            if (u_studyid.length() > 0) {
                SDIRequest sdiRequestClinicalEvent = new SDIRequest();
                sdiRequestClinicalEvent.setRequestItem("primary[clinicaleventdesc,sstudyid]");
                sdiRequestClinicalEvent.setSDCid("LV_ClinicalEvnt");
                sdiRequestClinicalEvent.setQueryFrom("s_clinicalevent");
                sdiRequestClinicalEvent.setQueryWhere("sstudyid in ('" + SafeSQL.convertToSQLInClause(u_studyid, ";", this.isOracle) + "') and (activeflag = 'Y' or activeflag is null)");
                sdiRequestClinicalEvent.setQueryOrderBy("sstudyid, clinicaleventdesc");
                this.dsClinicalEvent = this.getSDIProcessor().getSDIData(sdiRequestClinicalEvent).getDataset("primary");
            }
        }
        this.initConfArrays(pagedata);
        this.ds = this.getDataEntryDataSet(this.keyid1);
        ServletContext application = pageContext.getServletContext();
        application.setAttribute(this.cPage + "aColName[]", (Object)this.aColName);
        application.setAttribute(this.cPage + "aColId[]", (Object)this.aColId);
        application.setAttribute(this.cPage + "aColSave[]", (Object)this.aColSave);
        application.setAttribute(this.cPage + "aColSdcid[]", (Object)this.aColSdcid);
        application.setAttribute(this.cPage + "aColType[]", (Object)this.aColType);
    }

    public void setIsTaskPage(boolean isTaskPage) {
        this.isTaskPage = isTaskPage;
    }

    private void initConfArrays(PropertyList pagedata) {
        String[] propsName = new String[]{"sColTitle", "sColName", "sColShowVal", "sColId", "sColSave", "sColSdcId", "sColShow", "sColType", "sColTypeVal", "sColData", "sColWidth", "sColAlign", "sColSize", "sColMaxLen", "sColTab", "sColLink", "sColDefault", "sColLookupURL"};
        StringBuffer[] propsValue = new StringBuffer[]{new StringBuffer("Sample"), new StringBuffer("s_sampleid"), new StringBuffer(""), new StringBuffer("keyid1"), new StringBuffer("y"), new StringBuffer("Sample"), new StringBuffer("y"), new StringBuffer("readonly"), new StringBuffer(""), new StringBuffer("String"), new StringBuffer("150"), new StringBuffer("left"), new StringBuffer("150"), new StringBuffer("80"), new StringBuffer("0"), new StringBuffer("LV_SMSDataEntrySing"), new StringBuffer(""), new StringBuffer("")};
        int propsLength = propsName.length;
        for (int tab = 1; tab < 3; ++tab) {
            PropertyListCollection cols = pagedata.getCollectionNotNull("tab" + tab);
            for (int i = 0; i < cols.size(); ++i) {
                PropertyList t = cols.getPropertyList(i);
                for (int n = 0; n < propsLength; ++n) {
                    if (propsName[n].equals("sColTab")) {
                        propsValue[n].append(";").append(tab);
                        continue;
                    }
                    if ("sColShow".equals(propsName[n])) {
                        String show = t.getProperty(propsName[n]);
                        if (YES.equalsIgnoreCase(show)) {
                            if (this.isProtocol) {
                                if ("N".equals(t.getProperty("showinprotocolmode"))) {
                                    propsValue[n].append(";").append("n");
                                    continue;
                                }
                                propsValue[n].append(";").append("y");
                                continue;
                            }
                            if ("N".equals(t.getProperty("showinnonprotocolmode"))) {
                                propsValue[n].append(";").append("n");
                                continue;
                            }
                            propsValue[n].append(";").append("y");
                            continue;
                        }
                        propsValue[n].append(";").append("n");
                        continue;
                    }
                    propsValue[n].append(";").append(t.getProperty(propsName[n]));
                }
                if (!"dropdown".equals(t.getProperty("sColType"))) continue;
                try {
                    this.jsonDropDown.put(t.getProperty("sColName"), t.getPropertyListNotNull("dropdowndefinition"));
                    continue;
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        this.sColTitle = propsValue[0].toString();
        this.sColName = propsValue[1].toString();
        this.sColShowVal = propsValue[2].toString();
        this.sColId = propsValue[3].toString();
        this.sColSave = propsValue[4].toString();
        this.sColSdcId = propsValue[5].toString();
        this.sColShow = propsValue[6].toString();
        this.sColType = propsValue[7].toString();
        this.sColTypeVal = propsValue[8].toString();
        this.sColData = propsValue[9].toString();
        this.sColWidth = propsValue[10].toString();
        this.sColAlign = propsValue[11].toString();
        this.sColSize = propsValue[12].toString();
        this.sColMaxLen = propsValue[13].toString();
        this.sColTab = propsValue[14].toString();
        this.sColLink = propsValue[15].toString();
        this.sColDefault = propsValue[16].toString();
        this.sColLookupURL = propsValue[17].toString();
        this.aColTitle = StringUtil.split(this.sColTitle, ";");
        this.aColName = StringUtil.split(this.sColName, ";");
        this.aColId = StringUtil.split(this.sColId, ";");
        this.aColSave = StringUtil.split(this.sColSave, ";");
        this.aColSdcid = StringUtil.split(this.sColSdcId, ";");
        this.aColShow = StringUtil.split(this.sColShow, ";");
        this.aColType = StringUtil.split(this.sColType, ";");
        this.aColTypeVal = StringUtil.split(this.sColTypeVal, ";");
        this.aColData = StringUtil.split(this.sColData, ";");
        this.aColWidth = StringUtil.split(this.sColWidth, ";");
        this.aColSize = StringUtil.split(this.sColSize, ";");
        this.aColMaxLen = StringUtil.split(this.sColMaxLen, ";");
        this.aColLink = StringUtil.split(this.sColLink, ";");
        this.aColTab = StringUtil.split(this.sColTab, ";");
        this.aColDefault = StringUtil.split(this.sColDefault, ";");
        this.aColLookupURL = StringUtil.split(this.sColLookupURL, ";");
        this.maxcol = this.aColName.length;
        PropertyList plCondApp = pagedata.getPropertyList("conditionalapprovechecks");
        if (plCondApp != null) {
            MISSCOLLDATE = plCondApp.getProperty("missingcollectiondate", YES);
            MISSRESTCLASS = plCondApp.getProperty("missingrestrictionclass", YES);
            MISSSAMPLETYPE = plCondApp.getProperty("missingsampletype", YES);
            MISSSUPPCLINDIAGNOSIS = plCondApp.getProperty("missingsupplclinicdiagnosis", YES);
            MISSTISSUEID = plCondApp.getProperty("missingtissueid", YES);
            SUBEXTALIASMISSMATCH = plCondApp.getProperty("subextaliasmissmatch", YES);
            MISSCLINICALSITE = plCondApp.getProperty("missingclinicalsite", YES);
            MISSCLINICALEVENT = plCondApp.getProperty("missingclinicalevent", YES);
            MISSSUBJECTALIAS = plCondApp.getProperty("missingsubjectalias", YES);
        }
    }

    public int getRowCount() {
        return this.ds.getRowCount();
    }

    public String getRefTypeSelect(String fieldid, String id, String sel, boolean familyReadOnly) {
        StringBuilder sbRefType = new StringBuilder();
        DataSet ds = (DataSet)this.pageContext.getSession().getAttribute("reftype_" + id);
        if (ds == null) {
            ds = this.getQueryProcessor().getRefTypeDataSet(id.replaceAll("'", "''"));
            this.pageContext.getSession().setAttribute("reftype_" + id, (Object)ds);
        }
        if (ds.getRowCount() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String sTemp = ds.getString(i, "refvalueid");
                String sSelect = sel.length() > 0 && sTemp.equals(sel) ? "selected" : "";
                sbRefType.append("<option value=\"").append(sTemp).append("\" ").append(sSelect).append(">").append(sTemp).append("</option>");
            }
        }
        return sbRefType.toString();
    }

    private String formatFamilyReadOnlyView(String samplefamilyid, String columnname, String value) {
        return "<div class='familytarget' columnname='" + columnname + "' familyid='" + samplefamilyid + "'>" + value + "</div>";
    }

    public String getPrepSqlData(String sql, Object[] params) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        if (ds != null && ds.size() > 0) {
            return ds.getColumnValues(ds.getColumnId(0), ";");
        }
        return "";
    }

    private static String createComboDropDown(DataSet dataset, String col, String gro, String val, String sId) {
        String sScript = "<script>";
        sScript = sScript + "dd_dropdownvalues['dd_" + sId + "']=[''";
        if (dataset != null && dataset.getRowCount() > 0) {
            for (int i = 0; i < dataset.getRowCount(); ++i) {
                if (!dataset.getValue(i, gro).equals(val)) continue;
                sScript = sScript + ",";
                sScript = sScript + "'" + dataset.getValue(i, col, "").replaceAll("'", "\\\\'") + "'";
            }
        }
        sScript = sScript + "];</script>";
        return sScript;
    }

    public static String setSelected(String list, String sel, boolean considerInactiveVal, QueryProcessor qp) {
        String sList = "";
        if (list.indexOf("=\"" + sel + "\"") > 0) {
            sList = list.substring(0, list.indexOf("=\"" + sel + "\"") + sel.length() + 3) + " selected" + list.substring(list.indexOf("=\"" + sel + "\"") + sel.length() + 3);
        } else {
            DataSet sampleTypeDataSet;
            if (sel.length() > 0 && considerInactiveVal && (sampleTypeDataSet = qp.getPreparedSqlDataSet("select sampletypedesc from s_sampletype where s_sampletypeid=?", new Object[]{sel})).size() > 0) {
                list = list + "<option value=\"" + sel + "\" selected>" + sampleTypeDataSet.getValue(0, "sampletypedesc", "") + "</option> ";
            }
            sList = list;
        }
        return sList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DataSet getDataEntryDataSet(String keyid1) {
        String sSelected = "";
        for (int i = 0; i < this.maxcol; ++i) {
            String sdcid;
            if (this.aColSdcid[i].length() <= 0 || this.aColName[i].length() <= 0 || this.aColName[i].startsWith("protocol_")) continue;
            if (sSelected.length() > 0) {
                sSelected = sSelected + ",";
            }
            String tableid = sdcid = this.aColSdcid[i];
            if ("Sample".equals(sdcid)) {
                tableid = "s_sample";
            } else if ("LV_SampleFamily".equals(sdcid)) {
                tableid = "s_samplefamily";
            } else if ("Study".equals(sdcid)) {
                tableid = "s_study";
            } else if ("TrackItemSDC".equals(sdcid)) {
                tableid = "trackitem";
            }
            sSelected = sSelected + tableid + "." + this.aColName[i];
        }
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        if (this.isOracle) {
            sql.append("select ").append(sSelected).append(", s_sample.").append(this.sampleiddisplaycolumn).append("");
            sql.append(" , (select s_study.").append(this.studydisplaycolumn).append(" from s_study where s_study.s_studyid = s_sample.sstudyid) studydisplayvalue");
            sql.append(this.isSubmission ? ",'' alias" : ", (select sdialias.aliastype || '|' || sdialias.aliasid typeid from sdialias where sdialias.sdcid='Sample' and sdialias.keyid1=s_sample.s_sampleid and rownum=1 ) alias");
            sql.append(", LV_SMSQuery.GetTissueFullName( s_samplefamily.tissueid ) tissuefullname");
            sql.append(", LV_SMSQuery.GetDiseaseFullName(s_samplefamily.diseaseid) diseasefullname");
            sql.append(", LV_SMSQuery.GetClinicalDiagFullName(s_samplefamily.clinicaldiagid) clinicaldiagfullname");
            sql.append(", department.departmentdesc, tissuedesc, metastasisdesc, diseasedesc, clinicaldiagdesc");
        } else {
            sql.append("select ").append(sSelected).append(", s_sample.").append(this.sampleiddisplaycolumn).append("");
            sql.append(" , (select s_study.").append(this.studydisplaycolumn).append(" from s_study where s_study.s_studyid = s_sample.sstudyid) studydisplayvalue");
            sql.append(this.isSubmission ? ",'' alias" : ", (select top 1 sdialias.aliastype + '|' + sdialias.aliasid typeid from sdialias where sdialias.sdcid='Sample' and sdialias.keyid1=s_sample.s_sampleid ) alias,");
            sql.append(" s_samplefamily.tissueid tissuefullname,");
            sql.append(" s_samplefamily.diseaseid diseasefullname,");
            sql.append(" s_samplefamily.clinicaldiagid clinicaldiagfullname,");
            sql.append(" department.departmentdesc,");
            sql.append(" s_tissue.tissuedesc, s_disease.diseasedesc, s_clinicaldiag.clinicaldiagdesc, s_metastasis.metastasisdesc");
        }
        String s = "select distinct s_study.clinicalflag from s_study where s_study.s_studyid in (select s_sample.sstudyid from s_sample where s_sample.s_sampleid in (" + safeSQL.addIn(keyid1, ";") + "))";
        DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet(s, safeSQL.getValues());
        if (_ds != null && YES.equals(_ds.getString(0, "clinicalflag", ""))) {
            this.isProtocol = true;
        }
        sql.append(" , s_samplefamily.s_samplefamilyid, s_sample.sstudyid, s_samplefamily.studysiteid");
        if (this.isProtocol) {
            sql.append(" , s_samplefamily.participantid, s_participant.cpcohortid, s_samplefamily.participanteventid");
            sql.append(" , s_samplefamily.clinicalprotocolid protocolid, s_samplefamily.clinicalprotocolversionid protocolversionid, s_samplefamily.clinicalprotocolrevision protocolrevision");
            sql.append(" , (select s_participantevent.parentparticipanteventid from s_participantevent where s_participantevent.s_participanteventid = s_samplefamily.participanteventid) parentparticipanteventid");
            sql.append(" , (select pe.eventdefid from s_participantevent pe where pe.s_participanteventid = (select s_participantevent.parentparticipanteventid from s_participantevent where s_participantevent.s_participanteventid = s_samplefamily.participanteventid)) parenteventdefid");
            sql.append(" , (select ed.eventdeflabel from s_eventdef ed where ed.s_eventdefid = (select pe.eventdefid from s_participantevent pe where pe.s_participanteventid = (select ppe.parentparticipanteventid from s_participantevent ppe where ppe.s_participanteventid = s_samplefamily.participanteventid))) parenteventlabel");
            sql.append(" , (select s_participantevent.eventdefid from s_participantevent where s_participantevent.s_participanteventid = s_samplefamily.participanteventid) eventdefid");
            sql.append(" , (select s_participantevent.eventlabel from s_participantevent where s_participantevent.s_participanteventid = s_samplefamily.participanteventid) eventlabel");
            sql.append(" , s_samplefamily.eventdefid familyeventdefid, s_samplefamily.sampletypeid familysampletypeid, s_samplefamily.specimendefid familyspecimendefid");
            sql.append(" , (select s_eventdef.parenteventdefid from s_eventdef where s_eventdef.s_eventdefid = s_samplefamily.eventdefid) familyparenteventdefid");
        }
        if (this.isOracle) {
            sql.append(", (select sm.sourcesampleid from s_samplemap sm where sm.destsampleid = s_sample.s_sampleid and rownum=1) parentsampleid");
        } else {
            sql.append(", (select top 1 sm.sourcesampleid from s_samplemap sm where sm.destsampleid = s_sample.s_sampleid) parentsampleid");
        }
        sql.append(" from s_sample LEFT OUTER JOIN s_study ON s_sample.sstudyid = s_study.s_studyid, trackitem,");
        sql.append(" s_samplefamily LEFT OUTER JOIN s_tissue ON s_samplefamily.tissueid = s_tissue.s_tissueid");
        sql.append(" LEFT OUTER JOIN s_disease ON s_samplefamily.diseaseid = s_disease.s_diseaseid");
        sql.append(" LEFT OUTER JOIN s_clinicaldiag ON s_samplefamily.clinicaldiagid = s_clinicaldiag.s_clinicaldiagid");
        sql.append(" LEFT OUTER JOIN s_metastasis ON s_samplefamily.metastasisid = s_metastasis.s_metastasisid");
        sql.append(" LEFT OUTER JOIN department ON s_samplefamily.initialdepartmentid = department.departmentid");
        if (this.isProtocol) {
            sql.append(" LEFT OUTER JOIN s_participant ON s_samplefamily.participantid = s_participant.s_participantid");
        }
        sql.append(" where s_sample.samplefamilyid = s_samplefamily.s_samplefamilyid ");
        sql.append(" and trackitem.linksdcid = 'Sample'");
        sql.append(" and trackitem.linkkeyid1 = s_sample.s_sampleid ");
        DataSet ds = null;
        String rsetid = null;
        try {
            rsetid = this.getDAMProcessor().createRSet("Sample", keyid1, null, null);
            sql.append(" and s_sample.s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        finally {
            if (OpalUtil.isNotEmpty(rsetid)) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
        if (ds == null) {
            ds = new DataSet();
        }
        if (this.isSubmission) {
            for (int row = 0; row < ds.size(); ++row) {
                for (int col = 0; col < ds.getColumnCount(); ++col) {
                    ds.setValue(row, ds.getColumnId(col), "");
                }
            }
            int newrows = 10;
            for (int i = 1; i < newrows; ++i) {
                ds.addRow();
            }
        } else {
            KeyidListComparator c = new KeyidListComparator("s_sampleid", keyid1, ";");
            Collections.sort(ds, c);
        }
        for (int i = 0; i < ds.size(); ++i) {
            ds.setString(i, "rowid", String.valueOf(i));
        }
        return ds;
    }

    private void hiddenButtonForVerification(JspWriter out) throws IOException {
        out.println("<script>");
        out.println("try {parent.document.getElementById('LookupSubject').style.display = 'none';} catch(e){}");
        out.println("try {parent.document.getElementById('BatchCreateSubject').style.display = 'none';} catch(e){}");
        out.println("try {parent.document.getElementById('ConditionalApprove').style.display = 'none';} catch(e){}");
        out.println("try {parent.document.getElementById('Approve').style.display = 'none';} catch(e){}");
        out.println("try {parent.document.getElementById('RequestVerification').style.display = 'none';} catch(e){}");
        out.println("</script>");
    }

    public String getSampletypeid() {
        return this.sampletypeid == null ? "" : this.sampletypeid;
    }

    public void renderGrid() throws IOException {
        JspWriter out = this.pageContext.getOut();
        StringBuilder h = new StringBuilder();
        StringBuilder sbScript = new StringBuilder();
        String onkeydown = "";
        if ("Validation".equals(this.pageMode)) {
            this.hiddenButtonForVerification(out);
        }
        if (this.studyError.length() > 0) {
            out.println("<div style='padding:6px;color:red;font:normal 12px sans-serif'>");
            out.println(this.studyError);
            out.println("</div>");
            return;
        }
        h.append("<style>");
        h.append("td.datacell {position:relative;}");
        h.append("div.familytarget {position:absolute;top:0;left:0;font-style:italic;color:#888;width:100%;height:100%;cursor:pointer;vertical-align:middle;display:table-cell;text-align:center;}");
        h.append("div.familycell {position:absolute;top:0;left:0;width:2px;height:100%;background:#245C91;}");
        h.append("</style>");
        h.append("<div id='_container'>");
        h.append("<form id='myform' name='myform' method='post' action='rc?command=page&page=LV_SMSDataEntry' target='_top'>");
        h.append("<input type='hidden' name='mode' value='").append(this.pageMode).append("'>");
        h.append("<input type='hidden' name='__pr_rows' id='__pr_rows' value='").append(this.ds.size()).append("'>");
        h.append("<input type='hidden' name='__mode_protocol' id='__mode_protocol' value='").append(this.isProtocol ? YES : "N").append("'>");
        h.append(" <div id='keydiv' style='position:absolute; width:100%; overflow:hidden; left:0; top:0; z-index:100;'>");
        h.append("     <table class='gridmaint_table' border='0' cellspacing='0' cellpadding='0' id='smst0'>");
        h.append("         <tr height='50'>");
        h.append("             <td class='smsdeheaderf'>");
        h.append("                 <div id='tab0header'><table cellpadding=2 cellspacing=0 border=0><tr>");
        h.append("                     <td><input type='checkbox' name='all_selector' id='__all' onClick='selectAll(this)'/></td>");
        h.append("                     <td style='font-weight:bold;'>").append(this.getTranslationProcessor().translate("Sample")).append("<br>[").append(this.getTranslationProcessor().translate("Study")).append("]</td>");
        h.append("                 </tr></table></div>");
        h.append("             </td>");
        h.append("         </tr>");
        sbScript.append("\nvar __maintcells = new Array();");
        sbScript.append("\nvar __rows = '").append(this.ds.size()).append("';");
        LinkedHashMap<Object, DataSet> map = new LinkedHashMap<Object, DataSet>();
        HashMap<String, Object> filter = new HashMap<String, Object>();
        for (int i = 0; i < this.ds.size(); ++i) {
            String sampleid = this.ds.getValue(i, "s_sampleid");
            String parentsampleid = this.ds.getValue(i, "parentsampleid", "");
            if (map.containsKey(sampleid)) continue;
            if (OpalUtil.isNotEmpty(parentsampleid)) {
                if (map.containsKey(parentsampleid)) {
                    DataSet _ds = (DataSet)map.get(parentsampleid);
                    _ds.copyRow(this.ds, i, 1);
                    continue;
                }
                filter.clear();
                filter.put("s_sampleid", parentsampleid);
                int row = this.ds.findRow(filter);
                if (row == -1) {
                    map.put(sampleid, this.ds.getRows(i, i + 1));
                    continue;
                }
                DataSet _ds = this.ds.getRows(row, row + 1);
                _ds.copyRow(this.ds, i, 1);
                map.put(parentsampleid, _ds);
                continue;
            }
            map.put(sampleid, this.ds.getRows(i, i + 1));
        }
        boolean even = true;
        Collection datasets = map.values();
        for (DataSet dataset : datasets) {
            for (int i = 0; i < dataset.size(); ++i) {
                String rowid = dataset.getString(i, "rowid");
                String sampleid = dataset.getValue(i, "s_sampleid");
                String className = even ? "f" : "fo";
                even = !even;
                String studyid = dataset.getString(i, "sstudyid", "");
                h.append("<tr class='datarow'>");
                h.append(" <td nowrap class='").append(className).append("' style='position:relative;'>");
                if (i > 0) {
                    if (i == dataset.size() - 1) {
                        h.append("<div class='itree'><div class='itreetop'></div></div>");
                    } else {
                        h.append("<div class='itree'><div class='itreetop'></div><div class='itreebottom'></div></div>");
                    }
                    h.append("<div style='padding-right:10px;padding-left:42px;position:relative;'>");
                    h.append("<input type='checkbox' name='maint_selector' rowid='").append(rowid).append("' id='__pr").append(rowid).append("' class='myselectorcbxtree selectorcheckbox'>&nbsp;");
                } else {
                    h.append(" <div style='padding-right:10px;padding-left:20px;position:relative;'>");
                    h.append(" <input type='checkbox' name='maint_selector' rowid='").append(rowid).append("' id='__pr").append(rowid).append("' class='myselectorcbx selectorcheckbox'>&nbsp;");
                }
                if (!this.isTaskPage && this.allowSingleSampleEdit) {
                    h.append("<a href='#' id='").append(sampleid).append("' onClick='parent.openSampleLink(\"").append(sampleid).append("\")' title='").append(sampleid).append("'>");
                    h.append(dataset.getValue(i, this.sampleiddisplaycolumn, sampleid));
                    h.append("</a>");
                } else {
                    h.append(dataset.getValue(i, this.sampleiddisplaycolumn, sampleid));
                }
                h.append("</div>");
                h.append("<div style='color:#666;padding-left:").append(i > 0 ? "48" : "22").append("px;padding-right:4px;'>[");
                if (this.studylinkurl.length() > 0) {
                    h.append("<a href='").append(this.studylinkurl).append("&sdcid=Study&keyid1=").append(studyid).append("' target='_blank'>");
                }
                h.append(StringUtil.replaceAll(dataset.getString(i, "studydisplayvalue", ""), " ", "&nbsp;"));
                if (this.studylinkurl.length() > 0) {
                    h.append("</a>");
                }
                h.append("]</div>");
                h.append("<input type='hidden' value=\"").append(dataset.getString(i, "sstudyid")).append("\" name='pr").append(rowid).append("_sstudyid' id='pr").append(rowid).append("_sstudyid'>");
                h.append("<input type='hidden' value=\"").append(sampleid).append("\" name='pr").append(rowid).append("_s_sampleid' id='pr").append(rowid).append("_s_sampleid'>");
                h.append("</td></tr>");
                sbScript.append("\nvar gridrow = [];");
                for (int c = 0; c < this.maxcol; ++c) {
                    if (!YES.equalsIgnoreCase(this.aColShow[c])) continue;
                    if (this.aColType[c].contains("checkbox")) {
                        sbScript.append("\ngridrow.push('pr").append(rowid).append("_").append(this.aColName[c]).append("_chx');");
                    } else {
                        sbScript.append("\ngridrow.push('pr").append(rowid).append("_").append(this.aColName[c]).append("');");
                    }
                    if (this.aColName[c].contains("metastasisid") || this.aColName[c].contains("initialdepartmentid")) {
                        sbScript.append("\ngridrow.push('pr").append(rowid).append("_").append(this.aColName[c]).append("name');");
                        continue;
                    }
                    if (!this.aColName[c].contains("tissueid") && !this.aColName[c].contains("diseaseid") && !this.aColName[c].contains("clinicaldiagid")) continue;
                    sbScript.append("\ngridrow.push('pr").append(rowid).append("_").append(this.aColName[c]).append("name');");
                    sbScript.append("\ngridrow.push('pr").append(rowid).append("_").append(this.aColName[c]).append("namefull');");
                }
                sbScript.append("__maintcells.push( gridrow );");
            }
        }
        h.append("</table></div>");
        sbScript.append("\nvar mainthandler1 = new GridHandler( __maintcells, 'maintmenu1', 'maint');mainthandler1.setContainingdiv( 'datadiv' );");
        sbScript.append("\nvar __currentindex = new Array();");
        sbScript.append("\n__currentindex['primary'] = ").append(this.ds.size()).append(";");
        sbScript.append("\nvar __dropdownDefinition = ").append(this.jsonDropDown.toString()).append(";");
        h.append("<div id='protopartdiv'>Participant Div</div>");
        h.append("<div id='datadiv' style='position:absolute;overflow:auto;left:120px;top:0;z-index:100;'>");
        PropertyList props = new PropertyList();
        for (int tabIdx = 1; tabIdx < 3; ++tabIdx) {
            long tabstart = System.currentTimeMillis();
            String sColValue = "";
            String sColShowValue = "";
            String sChecked = "";
            String refTypeList = "";
            String sAliasType = "";
            String sAliasId = "";
            String sAliasAct = "";
            if (tabIdx == 1) {
                h.append("<div id='tab__").append(tabIdx - 1).append("' style='display:block;position:absolute;top:0;left:0;' class='tab_container'>");
            } else {
                h.append("<div id='tab__").append(tabIdx - 1).append("' style='display:none;position:absolute;top:0;left:0;' class='tab_container'>");
            }
            h.append("<table class='gridmaint_table' border='0' cellspacing='0' cellpadding='0' id='smst").append(tabIdx).append("'>");
            h.append("<tr height='50' id='tab").append(tabIdx).append("header'>");
            for (int i = 0; i < this.maxcol; ++i) {
                if (!YES.equalsIgnoreCase(this.aColShow[i]) || Integer.parseInt(this.aColTab[i]) != tabIdx) continue;
                h.append("<td class='smsdeheader' width='").append(this.aColWidth[i]).append("'>").append(this.aColTitle[i]).append("</td>");
            }
            h.append("</tr>");
            ArrayList<String> sampleFamilyList = new ArrayList<String>();
            if (this.ds.size() > 0) {
                even = false;
                boolean hideSampleFamilyColumn = false;
                for (DataSet dataset : datasets) {
                    for (int i = 0; i < dataset.size(); ++i) {
                        String className;
                        String samplefamilyid = dataset.getString(i, "s_samplefamilyid");
                        if (!sampleFamilyList.contains(samplefamilyid)) {
                            sampleFamilyList.add(samplefamilyid);
                            hideSampleFamilyColumn = false;
                        } else {
                            hideSampleFamilyColumn = true;
                        }
                        String rowid = dataset.getString(i, "rowid");
                        even = !even;
                        String sStudy = dataset.getString(i, "s_studyid", dataset.getString(i, "sstudyid", ""));
                        String sSampleType = dataset.getString(i, "sampletypeid", "");
                        String string = className = even ? "f" : "fo";
                        if (tabIdx == 1) {
                            h.append("<input type='hidden' name='__pr").append(rowid).append("_rs' value='S'>");
                            h.append("<input type='hidden' name='__pr").append(rowid).append("_key' value='").append(rowid).append(";(null);(null)'>");
                        }
                        h.append("<tr height='28' class='datarow'>");
                        for (int c = 0; c < this.maxcol; ++c) {
                            String columnType = this.aColType[c];
                            String columnName = this.aColName[c].toLowerCase();
                            String columnSave = this.aColSave[c];
                            boolean isSampleFamilyColumn = "LV_SampleFamily".equals(this.aColSdcid[c]);
                            if ("N".equalsIgnoreCase(columnSave) && !columnType.contains("readonly")) {
                                columnType = "readonly";
                            }
                            if (tabIdx != Integer.parseInt(this.aColTab[c])) continue;
                            String sLookup = this.getSLookup(columnName);
                            onkeydown = "";
                            if (this.aColSdcid[c].length() > 0) {
                                if (this.aColData[c].equalsIgnoreCase("String")) {
                                    sColValue = dataset.getValue(i, columnName);
                                } else if (this.aColData[c].equalsIgnoreCase("Date")) {
                                    sColValue = dataset.getValue(i, columnName, "");
                                } else if (this.aColData[c].equalsIgnoreCase("int")) {
                                    sColValue = dataset.getValue(i, columnName, "");
                                } else if (this.aColData[c].equalsIgnoreCase("BigDecimal")) {
                                    sColValue = dataset.getValue(i, columnName, "");
                                }
                            }
                            if (columnName.equalsIgnoreCase("aliastype") || columnName.equalsIgnoreCase("aliasid")) {
                                sColValue = dataset.getValue(i, "alias");
                                if (sColValue.contains("|")) {
                                    sAliasType = sColValue.substring(0, sColValue.indexOf("|"));
                                    sAliasId = sColValue.substring(sColValue.indexOf("|") + 1);
                                    sAliasAct = sColValue;
                                } else {
                                    sAliasType = "";
                                    sAliasId = "";
                                    sAliasAct = "";
                                }
                                if (columnName.equalsIgnoreCase("aliasid")) {
                                    sColValue = sAliasId;
                                } else if (columnName.equalsIgnoreCase("aliastype")) {
                                    sColValue = sAliasType;
                                }
                            }
                            String[] showvalue = this.getSColShowValue(dataset, i, columnName);
                            sColShowValue = showvalue[0];
                            String itemfullname = showvalue[1];
                            sColValue = sColValue.replaceAll("\"", "&quot;");
                            if (YES.equalsIgnoreCase(this.aColShow[c])) {
                                String width = this.aColWidth[c];
                                h.append("<td nowrap class='").append(className).append(" datacell' width='").append(width).append("' style='text-align:center'>");
                                if (columnType.equalsIgnoreCase("readonly") && columnName.equalsIgnoreCase("aliasid")) {
                                    h.append("<input type='hidden' value=\"").append(sAliasAct).append("\" id='pr").append(rowid).append("_aliasact'>");
                                    h.append(sColValue);
                                } else {
                                    String fieldid;
                                    if (OpalUtil.isEmpty(sColValue) && OpalUtil.isNotEmpty(this.aColDefault[c])) {
                                        sColValue = this.aColDefault[c];
                                    }
                                    sChecked = sColValue.equalsIgnoreCase(YES) ? "checked" : "unchecked";
                                    String sDisabled = columnType.contains("readonly") ? "disabled" : "";
                                    if (this.isProtocol && columnName.startsWith("protocol_")) {
                                        if ("protocol_cohort".equals(columnName)) {
                                            String familyeventdefid;
                                            String cohortid = dataset.getString(i, "cpcohortid", "");
                                            if (OpalUtil.isEmpty(cohortid) && OpalUtil.isNotEmpty(familyeventdefid = dataset.getString(i, "familyeventdefid"))) {
                                                cohortid = this.getCohortByEventDef(familyeventdefid);
                                                sDisabled = "N";
                                            }
                                            props.clear();
                                            props.setProperty("colValue", cohortid);
                                            props.setProperty("disabled", sDisabled);
                                            props.setProperty("sampletypeid", sSampleType);
                                            props.setProperty("studyid", sStudy);
                                            props.setProperty("columnName", columnName);
                                            props.setProperty("isSampleFamilyColumn", YES);
                                            props.setProperty("hideSampleFamilyColumn", hideSampleFamilyColumn ? YES : "N");
                                            h.append(this.writeDataSetDropDown(dataset, c, i, props, rowid));
                                        } else if ("protocol_visit".equals(columnName)) {
                                            if (hideSampleFamilyColumn) {
                                                String eventlabel = dataset.getString(i, "parenteventlabel", dataset.getString(i, "eventlabel", ""));
                                                h.append(this.formatFamilyReadOnlyView(samplefamilyid, "protocol_visit", eventlabel));
                                            } else {
                                                h.append(this.getProtocolVisitDropDownHTML(dataset, i, "disabled".equals(sDisabled), rowid));
                                            }
                                        } else if ("protocol_timepoint".equals(columnName)) {
                                            if (hideSampleFamilyColumn) {
                                                String parenteventdefid = dataset.getString(i, "parenteventdefid", "");
                                                String familyparenteventdefid = dataset.getString(i, "familyparenteventdefid", "");
                                                if (parenteventdefid.length() > 0) {
                                                    String eventlabel = dataset.getString(i, "eventlabel", "");
                                                    h.append(this.formatFamilyReadOnlyView(samplefamilyid, "protocol_timepoint", eventlabel));
                                                } else if (OpalUtil.isNotEmpty(familyparenteventdefid)) {
                                                    String familyeventdefid = dataset.getString(i, "familyeventdefid", "");
                                                    if (OpalUtil.isNotEmpty(familyeventdefid)) {
                                                        h.append(this.formatFamilyReadOnlyView(samplefamilyid, "protocol_timepoint", "&nbsp;"));
                                                    } else {
                                                        String familyeventdeflabel = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_eventdef", "eventdeflabel", "s_eventdefid = ?", new String[]{familyeventdefid});
                                                        h.append(this.formatFamilyReadOnlyView(samplefamilyid, "protocol_timepoint", familyeventdeflabel));
                                                    }
                                                } else {
                                                    h.append(this.formatFamilyReadOnlyView(samplefamilyid, "protocol_timepoint", "&nbsp;"));
                                                }
                                            } else {
                                                h.append(this.getProtocolVisitTimpointDropDownHTML(dataset, i, "disabled".equals(sDisabled), rowid));
                                            }
                                        }
                                    } else if (this.isProtocol && columnName.equals("externalsubject")) {
                                        fieldid = "pr" + rowid + "_protocol_participantid";
                                        if (hideSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, "externalsubject", sColValue));
                                        } else if ("readonly".equalsIgnoreCase(columnType)) {
                                            h.append("<table cellpadding=4 cellspacing=0 border=0 width='100%'><tr>");
                                            h.append("<td class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'>");
                                            h.append("<input type='hidden' value=\"").append(dataset.getValue(i, "participantid", "")).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("'>");
                                            h.append("<input type='hidden' value=\"").append(sColValue).append("\" id='pr").append(rowid).append("_externalsubject' name='pr").append(rowid).append("_externalsubject'>");
                                            h.append(sColValue);
                                            h.append("</td>");
                                            h.append("</tr></table>");
                                        } else {
                                            String fieldbackground = "white";
                                            String studysiteid = dataset.getString(i, "studysiteid", "");
                                            if (StringUtil.getLen(studysiteid) > 0L && StringUtil.getLen(sColValue) > 0L) {
                                                List plist;
                                                String key = "participant_" + studysiteid;
                                                if (!this.ddCache.containsKey(key)) {
                                                    ArrayList<String> list = new ArrayList<String>();
                                                    DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet("select externalparticipantid from s_participant where studysiteid = ?", new Object[]{studysiteid});
                                                    if (_ds != null && _ds.size() > 0) {
                                                        for (int row = 0; row < _ds.size(); ++row) {
                                                            list.add(_ds.getValue(row, "externalparticipantid", ""));
                                                        }
                                                    }
                                                    this.ddCache.put(key, list);
                                                }
                                                if (!(plist = (List)this.ddCache.get(key)).contains(sColValue)) {
                                                    fieldbackground = "#FFD2D2";
                                                }
                                            }
                                            h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                                            h.append("<td>");
                                            h.append("<input type='hidden' value=\"").append(dataset.getValue(i, "participantid", "")).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("'>");
                                            h.append("<input value=\"").append(sColValue).append("\" id='pr").append(rowid).append("_externalsubject' name='pr").append(rowid).append("_externalsubject'");
                                            h.append(" onChange='lvHandleProtocolParticipantChange(this, ").append(rowid).append(");' style='background:").append(fieldbackground).append("' class='protocolparticipant familysource' rowid='").append(rowid).append("'");
                                            h.append(" columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'>");
                                            h.append("</td><td>");
                                            h.append("<img title='Looking up a Participant' border='0' src='WEB-CORE/elements/images/lookup.gif' onclick='lvLookupProtocolParticipant(").append(rowid).append(");' style='cursor:pointer'></td>");
                                            h.append("</tr></table>");
                                        }
                                    } else if ("reftype".equalsIgnoreCase(columnType)) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                        } else {
                                            refTypeList = this.getRefTypeSelect(fieldid, this.aColTypeVal[c], sColValue, hideSampleFamilyColumn && isSampleFamilyColumn);
                                            h.append("<select id='").append(fieldid).append("' name='").append(fieldid).append("' onchange='parent.setChanges()'");
                                            if (isSampleFamilyColumn) {
                                                h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                            }
                                            h.append(">");
                                            h.append("<option value=\"\"></option>").append(refTypeList).append("</select>");
                                        }
                                    } else if ("dropdown".equalsIgnoreCase(columnType)) {
                                        props.clear();
                                        props.setProperty("colValue", sColValue);
                                        props.setProperty("disabled", sDisabled);
                                        props.setProperty("sampletypeid", sSampleType);
                                        props.setProperty("studyid", sStudy);
                                        props.setProperty("columnName", columnName);
                                        props.setProperty("isSampleFamilyColumn", isSampleFamilyColumn ? YES : "N");
                                        props.setProperty("hideSampleFamilyColumn", hideSampleFamilyColumn ? YES : "N");
                                        String s = this.writeDataSetDropDown(dataset, c, i, props, rowid);
                                        h.append(s);
                                    } else if ("dropdowncombo".equalsIgnoreCase(columnType)) {
                                        if ("clinicalevent".equalsIgnoreCase(columnName)) {
                                            fieldid = "pr" + rowid + "_" + columnName;
                                            if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                                h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                            } else {
                                                h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                                                h.append("<td><input style='width:").append(width).append("' value=\"").append(sColValue).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("' dropdownid='dd_pr").append(rowid).append("_").append(columnName).append("' onkeypress='dd_inputKeyPress(this)' onkeyup='dd_inputKeyUp(this)' ").append(onkeydown).append(" onchange='parent.setChanges()'");
                                                if (isSampleFamilyColumn) {
                                                    h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                                }
                                                h.append("></td>");
                                                h.append("<td><button type=\"button\" onclick='my_dd_toggleDropDown(\"").append(fieldid).append("\")' class='downbutton'>");
                                                h.append("<img src='WEB-CORE/elements/images/down.gif'></button>");
                                                h.append(DataEntryPageUtil.createComboDropDown(this.dsClinicalEvent, "clinicaleventdesc", "sstudyid", sStudy, fieldid));
                                                h.append("</td></tr></table>");
                                            }
                                        }
                                    } else if ("checkbox".equals(columnType)) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, YES.equals(sColValue) || "TRUE".equals(sColValue) ? this.getTranslationProcessor().translate("Yes") : this.getTranslationProcessor().translate("No")));
                                        } else {
                                            h.append("<input style='display:none' id='").append(fieldid).append("' name='").append(fieldid).append("' value=\"").append(sColValue).append("\" />");
                                            h.append("<input id='").append(fieldid).append("_chx' name='").append(fieldid).append("_chx' value=\"").append(sColValue).append("\" type='checkbox' onclick=\"setCheckBoxFieldValue('pr").append(rowid).append("_").append(columnName).append("','TRUE','FALSE');\" onchange=\"parent.setChanges();setCheckBoxValue('").append(fieldid).append("','TRUE','FALSE');\"  ").append(sChecked).append(" ").append(sDisabled).append("/>");
                                        }
                                    } else if ("checkbox|readonly".equals(columnType)) {
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, YES.equals(sColValue) || "TRUE".equals(sColValue) ? this.getTranslationProcessor().translate("Yes") : this.getTranslationProcessor().translate("No")));
                                        } else {
                                            h.append("<span");
                                            if (isSampleFamilyColumn) {
                                                h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                            }
                                            h.append(">");
                                            h.append(YES.equals(sColValue) || "TRUE".equals(sColValue) ? this.getTranslationProcessor().translate("Yes") : this.getTranslationProcessor().translate("No"));
                                            h.append("</span>");
                                        }
                                    } else if ("checkboxyn".equals(columnType)) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, YES.equals(sColValue) || "TRUE".equals(sColValue) ? this.getTranslationProcessor().translate("Yes") : this.getTranslationProcessor().translate("No")));
                                        } else {
                                            h.append("<input style='display:none' id='").append(fieldid).append("' name='").append(fieldid).append("' value=\"").append(sColValue).append("\">");
                                            h.append("<input id='").append(fieldid).append("_chx' name='").append(fieldid).append("_chx' value=\"").append(sColValue).append("\" type='checkbox' onclick=\"setCheckBoxFieldValue('").append(fieldid).append("','Y','N');\" onchange=\"parent.setChanges();setCheckBoxValue('").append(fieldid).append("','Y','N');\"  ").append(sChecked).append(" ").append(sDisabled).append(">");
                                        }
                                    } else if (columnType.equalsIgnoreCase("readonly")) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        if (this.aColLink[c].length() > 0) {
                                            if (columnName.equalsIgnoreCase("protocolname")) {
                                                h.append("<input type='hidden' value=\"").append(sColValue).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("' onchange='parent.setChanges()'>");
                                                h.append("&nbsp;<a target='_blank' href='rc?command=page&page=").append(this.aColLink[c]).append("&sdcid=Study&keyid1=").append(sStudy).append("'>");
                                                h.append(sColValue).append("</a>");
                                            }
                                        } else if (columnName.equalsIgnoreCase("restrictclassid") || columnName.equalsIgnoreCase("studysiteid") || columnName.equalsIgnoreCase("sampletypeid") || columnName.equalsIgnoreCase("samplesourceid") || columnName.equalsIgnoreCase("preptypeid") || columnName.equalsIgnoreCase("collectmethodid") || columnName.equalsIgnoreCase("containertype")) {
                                            props.clear();
                                            props.setProperty("colValue", sColValue);
                                            props.setProperty("disabled", sDisabled);
                                            props.setProperty("sampletypeid", sSampleType);
                                            props.setProperty("studyid", sStudy);
                                            props.setProperty("columnName", columnName);
                                            props.setProperty("isSampleFamilyColumn", isSampleFamilyColumn ? YES : "N");
                                            props.setProperty("hideSampleFamilyColumn", hideSampleFamilyColumn ? YES : "N");
                                            h.append(this.writeDataSetDropDown(dataset, c, i, props, rowid));
                                        } else if (sLookup.length() > 0) {
                                            h.append("<input type='hidden' value=\"").append(itemfullname).append("\" id='").append(fieldid).append("namefull' name='").append(fieldid).append("namefull'>");
                                            h.append("<input type='hidden' id='").append(fieldid).append("' name='").append(fieldid).append("' value=\"").append(sColValue).append("\">");
                                            h.append("<input class='size_").append(this.aColSize[c]).append("' value=\"").append(OpalUtil.isEmpty(sColShowValue) ? sColValue : sColShowValue).append("\" readonly id='").append(fieldid).append("name' name='").append(fieldid).append("name' onMouseOver='showValue(this,true)' onMouseOut='showValue(this,false)' onmousedown='showValue(this,false)' onchange='parent.setChanges()'>");
                                        } else {
                                            h.append("<input type='hidden' value=\"").append(sColValue).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("'>&nbsp;").append(sColValue);
                                        }
                                    } else if (columnType.equalsIgnoreCase("text") || columnType.equalsIgnoreCase("hidden")) {
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                        } else {
                                            h.append(sColValue.length() != 0 ? sColValue : "&nbsp;");
                                        }
                                    } else if (columnType.equalsIgnoreCase("input")) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                        } else if (Integer.parseInt(this.aColMaxLen[c]) > 200) {
                                            String _width = width;
                                            String _rows = "2";
                                            try {
                                                int pipeIndex = width.indexOf("|");
                                                if (pipeIndex != -1) {
                                                    _width = width.substring(0, pipeIndex);
                                                    _rows = width.substring(pipeIndex + 1);
                                                }
                                            }
                                            catch (Exception e) {
                                                _width = width;
                                                _rows = "2";
                                            }
                                            if (StringUtil.getLen(_rows) == 0L) {
                                                _rows = "2";
                                            }
                                            h.append("<textarea id='").append(fieldid).append("' name='").append(fieldid).append("' maxlength='").append(this.aColMaxLen[c]).append("' rows='").append(_rows).append("' style='width:").append(_width).append("' onchange='parent.setChanges()'");
                                            if (isSampleFamilyColumn) {
                                                h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                            }
                                            h.append(">").append(sColValue).append("</textarea>");
                                        } else {
                                            h.append("<input type='text' value=\"").append(sColValue).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("' ").append(onkeydown).append(" onchange='parent.setChanges()' style='width:").append(width).append("'");
                                            if (isSampleFamilyColumn) {
                                                h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                            }
                                            h.append(">");
                                            if (columnName.equalsIgnoreCase("aliasid")) {
                                                h.append("<input type='hidden' value=\"").append(sAliasAct).append("\" id='pr").append(rowid).append("_aliasact' name='pr").append(rowid).append("_aliasact'>");
                                            }
                                        }
                                    } else if (columnType.equalsIgnoreCase("lookup")) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        String lookupurl = this.aColLookupURL[c];
                                        lookupurl = lookupurl == null ? "" : lookupurl.trim();
                                        if (OpalUtil.isNotEmpty(lookupurl)) {
                                            sLookup = lookupurl;
                                        }
                                        if (sLookup.length() > 0) {
                                            if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                                if ("tissueid".equals(columnName) || "diseaseid".equals(columnName) || "clinicaldiagid".equals(columnName) || "metastasisid".equals(columnName)) {
                                                    h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColShowValue));
                                                } else {
                                                    h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                                }
                                            } else {
                                                h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                                                h.append("<td>");
                                                if ("tissueid".equals(columnName) || "diseaseid".equals(columnName) || "clinicaldiagid".equals(columnName) || "metastasisid".equals(columnName)) {
                                                    h.append("<input type='hidden' id='").append(fieldid).append("' name='").append(fieldid).append("' value=\"").append(sColValue).append("\">");
                                                    h.append("<input type='hidden' id='").append(fieldid).append("namefull' name='").append(fieldid).append("namefull' value=\"").append(itemfullname).append("\">");
                                                    h.append("<input edit='lookup' id='").append(fieldid).append("name' name='").append(fieldid).append("name' type='text' value=\"").append(sColShowValue).append("\"");
                                                } else if ("sampletypeid".equals(columnName)) {
                                                    h.append("<input edit='lookup' type='text' value=\"").append(sColValue).append("\" readonly id='").append(fieldid).append("' name='").append(fieldid).append("'");
                                                    h.append(" onChange='lvHandleSampleTypeChange(this, ").append(rowid).append(");' ");
                                                } else {
                                                    h.append("<input edit='lookup' type='text' value=\"").append(sColValue).append("\" readonly id='").append(fieldid).append("' name='").append(fieldid).append("'");
                                                    h.append(" onChange=\"parent.setChanges();\"");
                                                }
                                                if (isSampleFamilyColumn) {
                                                    h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                                }
                                                h.append(onkeydown).append(">");
                                                h.append("</td>");
                                                h.append("<td><a href='/Looking up a ").append(this.aColTitle[c]).append("' onClick=\"lookupfield( 'pr").append(rowid).append("_").append(columnName).append("', '").append(this.aColTypeVal[c]).append("','','','','','','', 'pr', '").append(rowid).append("', '").append(columnName).append("', '").append(sLookup).append("');return false\" tabindex='0'><img title='Looking up a ").append(this.aColTitle[c]).append("' border='0' src='WEB-CORE/elements/images/lookup.gif'></a></td>");
                                                if ("subjectid".equalsIgnoreCase(columnName)) {
                                                    h.append("<td width='16'><a href='#' id='pr").append(rowid).append("_' pg='LV_SubjectNewPopup' onClick='sapphire.page.getTop().editLink(this)'><img title=\"").append(this.getTranslationProcessor().translate("Manage Subject")).append("\" border='0' src='rc?command=image&image=UserInformation&size=16'></a></td>");
                                                }
                                                h.append("</tr></table>");
                                            }
                                        } else if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                        } else if (columnName.contains("subjectid")) {
                                            h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                                            h.append("<td><input edit='lookup' type='text' value=\"").append(sColValue).append("\" readonly id='").append(fieldid).append("' name='").append(fieldid).append("' ").append(onkeydown);
                                            h.append(" onchange='parent.setChanges();'");
                                            if (isSampleFamilyColumn) {
                                                h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                            }
                                            h.append("></td>");
                                            h.append("<td width='16'><a href='/Looking up a ").append(this.aColTitle[c]).append("' onClick=\"lookupfield( '").append(fieldid).append("', '").append(this.aColTypeVal[c]).append("','','','','','','', 'pr', '").append(rowid).append("', '").append(columnName).append("', 'LV_SubjectLookup');return false\" tabindex='0'><img title='Looking up a ").append(this.aColTitle[c]).append("' border='0' src='WEB-CORE/elements/images/lookup.gif'></a></td>");
                                            if (columnName.equalsIgnoreCase("subjectid")) {
                                                h.append("<td width='16'><a href='#' id='pr").append(rowid).append("_' pg='LV_SubjectNewPopup' onClick='sapphire.page.getTop().editLink(this)'><img title=\"").append(this.getTranslationProcessor().translate("Manage Subject")).append("\" border='0' src='rc?command=image&image=UserInformation&size=16'></a></td>");
                                            }
                                            h.append("</tr></table>");
                                        } else {
                                            h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                                            h.append("<td><input edit='lookup' type='text' value=\"").append(sColValue).append("\" readonly id='").append(fieldid).append("' name='pr").append(fieldid).append("' ").append(onkeydown);
                                            h.append(" onchange='parent.setChanges();'");
                                            if (isSampleFamilyColumn) {
                                                h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                            }
                                            h.append("></td>");
                                            h.append("<td><a href='/Looking up a ").append(this.aColTitle[c]).append("' onClick=\"lookupfield( '").append(fieldid).append("', '").append(this.aColTypeVal[c]).append("','','','','','','', 'pr', '").append(rowid).append("', '").append(columnName).append("', '');return false\" tabindex='0'><img title='Looking up a ").append(this.aColTitle[c]).append("' border='0' src='WEB-CORE/elements/images/lookup.gif'></a></td>");
                                            h.append("</tr></table>");
                                        }
                                    } else if (columnType.equalsIgnoreCase("datelookup")) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        if (sColValue.length() == 0) {
                                            sColValue = "";
                                        }
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                        } else {
                                            h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                                            h.append("<td><input type='text' value=\"").append(sColValue).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("' ").append(onkeydown).append(" onchange='parent.setChanges()'");
                                            if (isSampleFamilyColumn) {
                                                h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                            } else {
                                                h.append(" class='field_datelookup'");
                                            }
                                            h.append("></td>");
                                            h.append("<td><a href='/Lookup a date' onClick=\"lookupdate( 'pr").append(rowid).append("_").append(columnName).append("','', 'pr', '").append(rowid).append("', '").append(columnName).append("' );return false\" tabindex='0'><img title='Lookup a date' border='0' src='WEB-CORE/elements/images/lookup_date.gif'></a></td>");
                                            h.append("</tr></table>");
                                        }
                                    } else if (columnType.equalsIgnoreCase("dateonlylookup")) {
                                        fieldid = "pr" + rowid + "_" + columnName;
                                        if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                            h.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue));
                                        } else {
                                            Calendar calendar = dataset.getCalendar(i, columnName);
                                            String string2 = sColValue = calendar != null ? dateOnlyFormatter.format(calendar.getTime()) : "";
                                            if (hideSampleFamilyColumn && isSampleFamilyColumn) {
                                                this.formatFamilyReadOnlyView(samplefamilyid, columnName, sColValue);
                                            } else {
                                                h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                                                h.append("<td><input type='text' style='width:").append(width).append("' value=\"").append(sColValue).append("\" id='").append(fieldid).append("' name='").append(fieldid).append("' ").append(onkeydown).append(" onchange='parent.setChanges()'");
                                                if (isSampleFamilyColumn) {
                                                    h.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
                                                } else {
                                                    h.append(" class='field_datelookup'");
                                                }
                                                h.append("></td>");
                                                h.append("<td><a href='/Lookup a date' onClick=\"lookupdate( 'pr").append(rowid).append("_").append(columnName).append("','', 'pr', '").append(rowid).append("', '").append(columnName).append("', 'M/d/yy' );return false\" tabindex='0'><img title='Lookup a date' border='0' src='WEB-CORE/elements/images/lookup_date.gif'></a></td>");
                                                h.append("</tr></table>");
                                            }
                                        }
                                    }
                                }
                                h.append("</td>");
                                continue;
                            }
                            h.append("<input type='hidden' value=\"").append(sColValue).append("\" id='pr").append(rowid).append("_").append(columnName).append("' name='pr").append(rowid).append("_").append(columnName).append("'>");
                        }
                        h.append("</tr>");
                    }
                }
            } else if (tabIdx > 0) {
                h.append("<tr><td colspan='").append(this.maxcol).append("'>No items found for request.</td></tr>");
            }
            h.append("</table>");
            h.append("</div>");
            this.logger.info("Rendering tab took " + (System.currentTimeMillis() - tabstart) + " ms");
        }
        h.append("</div>");
        for (int c = 0; c < this.maxcol; ++c) {
            if (!"y".equalsIgnoreCase(this.aColSave[c])) continue;
            h.append("\n<input type='hidden' id='__u_").append(this.aColId[c]).append("_").append(this.aColSdcid[c]).append("' name='__u_").append(this.aColId[c]).append("_").append(this.aColSdcid[c]).append("' value=''>");
        }
        h.append("\n<input type='hidden' name='__u_aliasact_' id='__u_aliasact_'>");
        h.append("\n<input type='hidden' name='keyid1' id='keyid1' value=\"").append(this.keyid1).append("\">");
        h.append("\n<input type='hidden' name='sdcid' id='sdcid' value=\"").append(this.sdcid).append("\">");
        h.append("\n<input type='hidden' name='nextpage' id='nextpage' value=\"").append(this.cPage).append("\">");
        h.append("\n<input type='hidden' name='act' id='act' value='G'>");
        h.append("\n<input type='hidden' name='auditreason' clearable='yes' />");
        h.append("\n<input type='hidden' name='auditactivity' clearable='yes' />");
        h.append("\n<input type='hidden' name='auditsignedflag' clearable='yes' />");
        h.append("\n</form>");
        h.append("</div>");
        h.append(this.renderGridCommonHtml());
        h.append("\n<script language='javascript'>");
        h.append("var __storagestatusarray = new Array();");
        for (int i = 0; i < this.ds.size(); ++i) {
            h.append("__storagestatusarray['").append(this.ds.getValue(i, "s_sampleid")).append("'] = '").append(this.ds.getValue(i, "storagestatus")).append("';");
        }
        h.append(sbScript.toString());
        h.append("</script>");
        out.println((Object)h);
        out.flush();
    }

    private String getCohortByEventDef(String eventdefid) {
        String key = "eventdefcohort-" + eventdefid;
        if (!this.localcache.containsKey(key)) {
            this.localcache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_eventdef", "cohortid", "s_eventdefid = ?", new String[]{eventdefid}));
        }
        return this.localcache.get(key);
    }

    private String renderGridCommonHtml() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<textarea style=\"display:none;width:0;height:0\" id=\"clipboard\"></textarea>");
        sb.append("<div id=\"maintmenu1\" selectblockerid=\"maintmenu1selectblocker\" style=\"position: absolute; display:none;z-index:1000\">");
        sb.append("<iframe id=\"maintmenu1selectblocker\" frameborder=\"0\" width=\"100\" style=\"position:relative; top:0px; left:0px;\" src=\"WEB-CORE/blank.html\"></iframe>");
        sb.append("<div class=\"menuholder\" style=\"position:absolute; top:0px; left:0px\">");
        sb.append("<div class=\"menu\" onclick=\"maintmenu1.handler.cut()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"maintcut\">&nbsp;Cut</div>");
        sb.append("<div class=\"menu\" onclick=\"maintmenu1.handler.copy()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"maintcopy\">&nbsp;Copy</div>");
        sb.append("<div class=\"menu\" onclick=\"maintmenu1.handler.paste()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"maintpaste\">&nbsp;Paste</div>");
        sb.append("<div class=\"menusep1\" id=\"maintsep1_1\"></div>");
        sb.append("<div class=\"menusep2\" id=\"maintsep1_2\"></div>");
        sb.append("<div class=\"menu\" onclick=\"maintmenu1.handler.fillDown()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"maintfilldown\">&nbsp;Fill Down</div>");
        sb.append("<div class=\"menu\" onclick=\"maintmenu1.handler.fillAcross()\" onmouseover=\"this.className='menuselected'\" onmouseout=\"this.className='menu'\" id=\"maintfillacross\">&nbsp;Fill Across</div>");
        sb.append("</div></div>");
        return sb.toString();
    }

    public void renderForm() throws IOException {
        JspWriter out = this.pageContext.getOut();
        StringBuilder sbScript = new StringBuilder();
        int iCol = 2;
        int currTab = 0;
        int prevTab = 0;
        int iColValue = 0;
        int count = 0;
        String sColValue = "";
        String sColShowValue = "";
        String sChecked = "";
        String refTypeList = "";
        String sAliasType = "";
        String sAliasId = "";
        String sAliasAct = "";
        BigDecimal bdColValue = new BigDecimal(0);
        BigDecimal bdZero = new BigDecimal(0);
        if ("Validation".equals(this.pageMode)) {
            this.hiddenButtonForVerification(out);
        }
        sbScript.append("\n<script language='javascript'>");
        sbScript.append("\nvar __maintcells = new Array();");
        out.println("<style>");
        out.println(".maintform_fieldtitle {width:150px;white-space: nowrap;}");
        out.println("</style>");
        out.println("<form id='myform' name='myform' method='post' action='rc?command=page&page=LV_SMSDataEntrySing' target='_top'>");
        out.println("<input type='hidden' name='__pr_rows' value='" + this.ds.size() + "'>");
        out.println("<input type='hidden' name='__mode_protocol' value='" + (this.isProtocol ? YES : "N") + "'>");
        if (this.ds.getRowCount() > 0) {
            StringBuilder h = new StringBuilder();
            PropertyList props = new PropertyList();
            out.println("<table width='800' class='maintform_table' cellspacing='0' cellpadding='3px'><tr height='28'>");
            out.println("<input type='hidden' name='__pr0_rs' value='S'>");
            out.println("<input type='hidden' name='__pr0_key' value='0;(null);(null)'>");
            sbScript.append("\nvar __dropdownDefinition = ").append(this.jsonDropDown.toString()).append(";");
            sbScript.append("\n__maintcells[0] = new Array(");
            String sStudy = "";
            String sSampleType = "";
            String sDisabled = "";
            String sLookup = "";
            for (int c = 0; c < this.maxcol; ++c) {
                String columnType = this.aColType[c];
                String columnName = this.aColName[c];
                String columnSave = this.aColSave[c];
                if ("N".equalsIgnoreCase(columnSave) && !columnType.contains("readonly")) {
                    columnType = "readonly";
                }
                sLookup = this.getSLookup(this.aColName[c]);
                sColValue = "";
                if (this.aColSdcid[c].length() > 0) {
                    if (this.aColData[c].equalsIgnoreCase("String")) {
                        sColValue = this.ds.getValue(0, this.aColName[c]);
                    } else if (this.aColData[c].equalsIgnoreCase("Date")) {
                        sColValue = this.ds.getValue(0, this.aColName[c], "");
                    } else if (this.aColData[c].equalsIgnoreCase("int")) {
                        iColValue = this.ds.getInt(0, this.aColName[c], 0);
                        sColValue = String.valueOf(iColValue);
                    } else if (this.aColData[c].equalsIgnoreCase("BigDecimal")) {
                        bdColValue = this.ds.getBigDecimal(0, this.aColName[c], bdZero);
                        sColValue = String.valueOf(bdColValue);
                    }
                }
                if (StringUtil.getLen(this.aColMaxLen[c]) > 0L && Integer.parseInt(this.aColMaxLen[c]) > 200) {
                    while (count % iCol > 0) {
                        out.println("<td class='maintform_fieldtitle'>&nbsp;</td><td>&nbsp;</td>");
                        ++count;
                    }
                    out.println("</tr><tr height='28'>");
                    out.println("<td class='maintform_fieldtitle' valign='top'>" + this.aColTitle[c] + "</td>");
                    out.println("<td nowrap class='maintform_field' colspan='" + (iCol * 2 - 1) + "'><textarea id='pr0_" + this.aColName[c] + "' name='pr0_" + this.aColName[c] + "' maxlength='" + this.aColMaxLen[c] + "' onchange='parent.setChanges()' rows='5' cols='97' >" + sColValue + "</textarea></td>");
                    if (c > 0) {
                        sbScript.append(",");
                    }
                    sbScript.append("'pr0_").append(this.aColName[c]).append("'");
                    continue;
                }
                if ((this.aColShow[c].equalsIgnoreCase(YES) || this.aColShow[c].equalsIgnoreCase("E")) && !this.aColType[c].equalsIgnoreCase("hidden")) {
                    currTab = Integer.parseInt(this.aColTab[c]);
                    if (prevTab > 0 && currTab > prevTab) {
                        while (count > 0 && count % iCol > 0) {
                            out.println("<td class='maintform_fieldtitle'>&nbsp;</td><td>&nbsp;</td>");
                            ++count;
                        }
                        out.println("</tr><tr height='28'>");
                        count = 0;
                    } else if (count > 0 && count % iCol == 0) {
                        out.println("</tr><tr height='28'>");
                    }
                    prevTab = currTab;
                    out.println("<td class='maintform_fieldtitle'>" + this.aColTitle[c] + "</td>");
                    ++count;
                }
                this.aColSize[c] = "200";
                if (this.aColName[c].equalsIgnoreCase("s_studyid")) {
                    sStudy = sColValue;
                }
                if (this.aColName[c].equalsIgnoreCase("sampletypeid")) {
                    sSampleType = sColValue;
                }
                if (this.aColName[c].equalsIgnoreCase("aliastype")) {
                    sColValue = this.ds.getValue(0, "alias");
                    if (sColValue.contains("|")) {
                        sAliasType = sColValue.substring(0, sColValue.indexOf("|"));
                        sAliasId = sColValue.substring(sColValue.indexOf("|") + 1);
                        sAliasAct = sColValue;
                    } else {
                        sAliasType = "";
                        sAliasId = "";
                        sAliasAct = "";
                    }
                }
                if (this.aColName[c].equalsIgnoreCase("aliasid")) {
                    sColValue = sAliasId;
                }
                if (this.aColName[c].equalsIgnoreCase("aliastype")) {
                    sColValue = sAliasType;
                }
                String[] showvalue = this.getSColShowValue(this.ds, 0, this.aColName[c]);
                sColShowValue = showvalue[0];
                String itemfullname = showvalue[1];
                sColValue = sColValue.replaceAll("\"", "&quot;");
                if (this.aColShow[c].equalsIgnoreCase("y") || this.aColShow[c].equalsIgnoreCase("e")) {
                    if (this.aColDefault[c].length() > 0) {
                        sColValue = this.aColDefault[c];
                    }
                    sChecked = sColValue.equalsIgnoreCase("true") ? "checked" : "unchecked";
                    if (c > 0) {
                        sbScript.append(",");
                    }
                    sbScript.append("'pr0_").append(this.aColName[c]).append("'");
                    sDisabled = columnType.contains("readonly") ? "disabled" : "";
                    if (this.isProtocol && columnName.startsWith("protocol_")) {
                        h.setLength(0);
                        if ("protocol_cohort".equals(columnName)) {
                            h.append("<td nowrap class='maintform_field'>");
                            String cohortid = this.ds.getString(0, "cpcohortid", "");
                            props.clear();
                            props.setProperty("colValue", cohortid);
                            props.setProperty("disabled", sDisabled);
                            props.setProperty("sampletypeid", sSampleType);
                            props.setProperty("studyid", sStudy);
                            h.append(this.writeDataSetDropDown(this.ds, c, 0, props, "0"));
                            h.append("</td>");
                        } else if ("protocol_visit".equals(columnName)) {
                            h.append("<td nowrap class='maintform_field'>");
                            h.append(this.getProtocolVisitDropDownHTML(this.ds, 0, "disabled".equals(sDisabled), "0"));
                            h.append("</td>");
                        } else if ("protocol_timepoint".equals(columnName)) {
                            h.append("<td nowrap class='maintform_field'>");
                            h.append(this.getProtocolVisitTimpointDropDownHTML(this.ds, 0, "disabled".equals(sDisabled), "0"));
                            h.append("</td>");
                        }
                        out.println(h.toString());
                    } else if (this.isProtocol && "externalsubject".equals(columnName)) {
                        h.setLength(0);
                        h.append("<td nowrap class='maintform_field'>");
                        if ("readonly".equalsIgnoreCase(columnType)) {
                            h.append("<table cellpadding=4 cellspacing=0 border=0 width='100%'><tr>");
                            h.append("<td>");
                            h.append("<input type='hidden' value=\"").append(this.ds.getValue(0, "participantid", "")).append("\" name='pr").append(0).append("_protocol_participantid' id='pr").append(0).append("_protocol_participantid'>");
                            h.append("<input type='hidden' value=\"").append(sColValue).append("\" name='pr").append(0).append("_externalsubject' id='pr").append(0).append("_externalsubject'>");
                            h.append(sColValue);
                            h.append("</td>");
                            h.append("</tr></table>");
                        } else {
                            String fieldbackground = "white";
                            String studysiteid = this.ds.getString(0, "studysiteid", "");
                            if (StringUtil.getLen(studysiteid) > 0L && StringUtil.getLen(sColValue) > 0L) {
                                ArrayList<String> list = new ArrayList<String>();
                                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select externalparticipantid from s_participant where studysiteid = ?", new Object[]{studysiteid});
                                if (ds != null && ds.size() > 0) {
                                    for (int row = 0; row < ds.size(); ++row) {
                                        list.add(ds.getValue(row, "externalparticipantid", ""));
                                    }
                                }
                                if (!list.contains(sColValue)) {
                                    fieldbackground = "#FFD2D2";
                                }
                            }
                            h.append("<table cellpadding=0 cellspacing=0 border=0 width='100%'><tr>");
                            h.append("<td>");
                            h.append("<input type='hidden' value=\"").append(this.ds.getValue(0, "participantid", "")).append("\" name='pr").append(0).append("_protocol_participantid' id='pr").append(0).append("_protocol_participantid'>");
                            h.append("<input value=\"").append(sColValue).append("\" name='pr").append(0).append("_externalsubject' id='pr").append(0).append("_externalsubject'");
                            h.append(" onChange='lvHandleProtocolParticipantChange(this, ").append(0).append(");' style='background:").append(fieldbackground).append("' class='protocolparticipant' rowindex='").append(0).append("'>");
                            h.append("</td><td>");
                            h.append("<img title='Looking up a Participant' border='0' src='WEB-CORE/elements/images/lookup.gif' onclick='lvLookupProtocolParticipant(").append(0).append(");' style='cursor:pointer'></td>");
                            h.append("</tr></table>");
                        }
                        h.append("</td>");
                        out.println(h.toString());
                    } else if (this.aColType[c].equalsIgnoreCase("reftype")) {
                        String fieldid = "pr0_" + this.aColName[c];
                        refTypeList = this.getRefTypeSelect(fieldid, this.aColTypeVal[c], sColValue, false);
                        out.println("<td nowrap class='maintform_field'><select class='size_" + this.aColSize[c] + "' name='" + fieldid + "' id='" + fieldid + "' onchange='parent.setChanges()'><option value=\"\"></option>" + refTypeList + "</select></td>");
                    } else if (this.aColType[c].equalsIgnoreCase("dropdown")) {
                        out.println("<td nowrap class='maintform_field'>");
                        props.clear();
                        props.setProperty("colValue", sColValue);
                        props.setProperty("sampletypeid", sSampleType);
                        props.setProperty("studyid", sStudy);
                        out.println(this.writeDataSetDropDown(this.ds, c, 0, props, "0"));
                        out.println("</td>");
                    } else if (this.aColType[c].equalsIgnoreCase("dropdowncombo")) {
                        if (this.aColName[c].equalsIgnoreCase("clinicalevent")) {
                            out.println("<td nowrap class='maintform_field'><input class='size_" + this.aColSize[c] + "' style='width:180' type='text' value=\"" + sColValue + "\" name='pr0_" + this.aColName[c] + "' id='pr0_" + this.aColName[c] + "' onchange='parent.setChanges()' dropdownid='dd_pr0_" + this.aColName[c] + "' onkeypress='dd_inputKeyPress(this)' onkeyup='dd_inputKeyUp(this)' /><button type=\"button\" onclick='my_dd_toggleDropDown(\"pr0_" + this.aColName[c] + "\")' class='downbutton'><img src='WEB-CORE/elements/images/down.gif'></button>" + DataEntryPageUtil.createComboDropDown(this.dsClinicalEvent, "clinicaleventdesc", "sstudyid", sStudy, "pr0_" + this.aColName[c]) + "</td>");
                        }
                    } else if (this.aColType[c].contains("checkbox")) {
                        sDisabled = this.aColType[c].contains("readonly") ? "disabled" : "";
                        out.println("<td nowrap class='maintform_field'><input style='display:none' type='text' name='pr0_" + this.aColName[c] + "' id='pr0_" + this.aColName[c] + "' onchange='parent.setChanges()'  value=\"" + sColValue + "\" />");
                        out.println("<input id='pr0_" + this.aColName[c] + "_chx' value=\"" + sColValue + "\" type='checkbox' onclick=\"setCheckBoxFieldValue('pr0_" + this.aColName[c] + "','TRUE','FALSE');\" onchange=\"setCheckBoxValue('pr0_" + this.aColName[c] + "','TRUE','FALSE');\"  " + sChecked + " " + sDisabled + "/></td>");
                    } else if (this.aColType[c].equalsIgnoreCase("readonly")) {
                        if (this.aColLink[c].length() > 0) {
                            if (this.aColName[c].equalsIgnoreCase("s_sampleid")) {
                                out.println("<td nowrap class='maintform_field'><input type='hidden' value=\"" + sColValue + "\" name='pr0_" + this.aColName[c] + "' id='pr0_" + this.aColName[c] + "'>&nbsp;<a href='#' id='" + sColValue + "' pg='LV_SMSSampleView' onClick='sapphire.page.getTop().openLink(this,false)'>" + sColValue + "</a></td>");
                            } else if (this.aColName[c].equalsIgnoreCase("protocolname")) {
                                out.println("<td nowrap class='maintform_field'><input type='hidden' value=\"" + sColValue + "\" name='pr0_" + this.aColName[c] + "' id='pr0_" + this.aColName[c] + "'>&nbsp;<a href='#' id='" + sStudy + "' pg='" + this.aColLink[c] + "' onClick='sapphire.page.getTop().openLink(this,false)'>" + sColValue + "</a></td>");
                            }
                        } else {
                            out.println("<td nowrap class='maintform_field'><input type='hidden' value=\"" + sColValue + "\" name='pr0_" + this.aColName[c] + "' id='pr0_" + this.aColName[c] + "'>&nbsp;" + sColValue + "</td>");
                        }
                    } else if (this.aColType[c].equalsIgnoreCase("text") || this.aColType[c].equalsIgnoreCase("hidden")) {
                        out.println("<td nowrap class='maintform_field' width='" + this.aColWidth[c] + "'>" + (sColValue.length() != 0 ? sColValue : "&nbsp;") + "</td>");
                    } else if (this.aColType[c].equalsIgnoreCase("input")) {
                        out.println("<td nowrap class='maintform_field'><input class='size_" + this.aColSize[c] + "' type='text' value=\"" + sColValue + "\" name='pr0_" + this.aColName[c] + "' id='pr0_" + this.aColName[c] + "' onchange='parent.setChanges()'></td>");
                        if (this.aColName[c].equalsIgnoreCase("aliasid")) {
                            out.println("<input type='hidden' value=\"" + sAliasAct + "\" name='pr0_aliasact' id='pr0_aliasact'>");
                        }
                    } else if (this.aColType[c].equalsIgnoreCase("lookup")) {
                        String lookupurl = this.aColLookupURL[c];
                        lookupurl = lookupurl == null ? "" : lookupurl.trim();
                        if (OpalUtil.isNotEmpty(lookupurl)) {
                            sLookup = lookupurl;
                        }
                        if (sLookup.length() > 0) {
                            out.println("<td nowrap class='maintform_field'>");
                            if ("tissueid".equals(columnName) || "diseaseid".equals(columnName) || "clinicaldiagid".equals(columnName) || "metastasisid".equals(columnName)) {
                                out.println("<input type='hidden' id='pr0_" + columnName + "' value=\"" + sColValue + "\">");
                                out.println("<input type='hidden' value=\"" + itemfullname + "\" id='pr0_" + columnName + "namefull'>");
                                out.println("<input class='size_" + this.aColSize[c] + " editlookup' edit='lookup' type='text' value=\"" + sColShowValue + "\" id='pr0_" + columnName + "name' onMouseOver='showValue(this,true)' onMouseOut='showValue(this,false)'>");
                            } else {
                                out.println("<input class='size_" + this.aColSize[c] + "' style='width:180' edit='lookup' type='text' value=\"" + sColValue + "\" readonly name='pr0_" + columnName + "' id='pr0_" + columnName + "' onchange='parent.setChanges()'>");
                            }
                            out.println("<a href='/Looking up a " + this.aColTitle[c] + "' onClick=\"lookupfield( 'pr0_" + columnName + "', '" + this.aColTypeVal[c] + "','','','','','','', 'pr', '0', '" + columnName + "', '" + sLookup + "');return false;\" tabindex='0'>");
                            out.println("<img title='Looking up a " + this.aColTitle[c] + "' border='0' src='WEB-CORE/elements/images/lookup.gif'></a>");
                            if (columnName.equalsIgnoreCase("subjectid")) {
                                out.println("<a href='#' id='pr0_' pg='LV_SubjectView' onClick='sapphire.page.getTop().editLink(this)'><img title='Edit " + this.aColTitle[c] + "' border='0' src='rc?command=image&image=UserInformation&size=16'></a></td>");
                            }
                            out.println("</td>");
                        } else if (columnName.contains("subjectid")) {
                            out.println("<td nowrap class='maintform_field'>");
                            out.println("<input class='size_" + this.aColSize[c] + "' style='width:180' edit='lookup' type='text' value=\"" + sColValue + "\" readonly name='pr0_" + columnName + "' id='pr0_" + columnName + "' onchange='parent.setChanges()'><a href='/Looking up a " + this.aColTitle[c] + "' onClick=\"lookupfield( 'pr0_" + columnName + "', '" + this.aColTypeVal[c] + "','','','','','','', 'pr', '0', '" + columnName + "', 'LV_SubjectLookup');return false\" tabindex='0'><img title='Looking up a " + this.aColTitle[c] + "' border='0' src='WEB-CORE/elements/images/lookup.gif'></a>");
                            if (columnName.equalsIgnoreCase("subjectid")) {
                                out.println("<a href='#' id='pr0_' pg='LV_SubjectView' onClick='sapphire.page.getTop().editLink(this)'><img title='Edit " + this.aColTitle[c] + "' border='0' src='rc?command=image&image=UserInformation&size=16'></a></td>");
                            }
                        } else {
                            out.println("<td nowrap class='maintform_field'><input class='size_" + this.aColSize[c] + "' edit='lookup' type='text' value=\"" + sColValue + "\" readonly name='pr0_" + columnName + "' id='pr0_" + columnName + "' onchange='parent.setChanges()'><a href='/Looking up a " + this.aColTitle[c] + "' onClick=\"lookupfield( 'pr0_" + columnName + "', '" + this.aColTypeVal[c] + "','','','','','','', 'pr', '0', '" + columnName + "', '');return false\" tabindex='0'><img title='Looking up a " + this.aColTitle[c] + "' border='0' src='WEB-CORE/elements/images/lookup.gif'></a></td>");
                        }
                    } else if (this.aColType[c].equalsIgnoreCase("datelookup")) {
                        if (sColValue.length() == 0) {
                            sColValue = "";
                        }
                        out.println("<td nowrap class='maintform_field'><input class='size_" + this.aColSize[c] + "' type='text' value=\"" + sColValue + "\" name='pr0_" + columnName + "' id='pr0_" + columnName + "' onchange='parent.setChanges()'><a href='/Lookup a date' onClick=\"lookupdate( 'pr0_" + columnName + "','', 'pr', '0', '" + columnName + "' );return false\" tabindex='0'><img title='Lookup a date' border='0' src='WEB-CORE/elements/images/lookup_date.gif'></a></td>");
                    }
                } else {
                    out.println("<input type='hidden' value=\"" + sColValue + "\" name='pr0_" + columnName + "' id='pr0_" + columnName + "'>");
                }
                if (c != this.maxcol - 1) continue;
                while (count % iCol > 0) {
                    out.println("<td width='175' bgcolor='silver'>&nbsp;</td><td>&nbsp;</td> ");
                    ++count;
                }
            }
            sbScript.append(");");
            out.println("</tr></table><br>");
        }
        sbScript.append("\nvar __currentindex = new Array();");
        sbScript.append("\n__currentindex['primary'] = ").append(this.ds.getRowCount()).append(";");
        sbScript.append("\n</script>");
        for (int c = 0; c < this.maxcol; ++c) {
            if (!this.aColSave[c].equalsIgnoreCase("y") && !this.aColSave[c].equalsIgnoreCase("e")) continue;
            out.println("<input type='hidden' id='__u_" + this.aColId[c] + "_" + this.aColSdcid[c] + "' name='__u_" + this.aColId[c] + "_" + this.aColSdcid[c] + "' value=\"\">");
        }
        out.println("<input type='hidden' id='__u_aliasact_' name='__u_aliasact_'>");
        out.println("<input type='hidden' id='keyid1' name='keyid1' value=\"" + this.keyid1 + "\">");
        out.println("<input type='hidden' id='sdcid' name='sdcid' value=\"" + this.sdcid + "\">");
        out.println("<input type='hidden' id='nextpage' name='nextpage' value=\"" + this.cPage + "\">");
        out.println("<input type='hidden' id='act' name='act' value='G'>");
        out.print("<input name=\"auditreason\" type=\"hidden\"  clearable=\"yes\" />");
        out.print("<input name=\"auditactivity\" type=\"hidden\"  clearable=\"yes\" />");
        out.print("<input name=\"auditsignedflag\" type=\"hidden\"  clearable=\"yes\" />");
        out.print("</form>");
        out.println(sbScript.toString());
    }

    private String[] getSColShowValue(DataSet dataset, int dsrowindex, String aColName) {
        String[] r = new String[2];
        if (aColName.equalsIgnoreCase("initialdepartmentid")) {
            r[0] = dataset.getValue(dsrowindex, "departmentdesc");
            if (r[0] == null || r[0].length() == 0) {
                r[0] = dataset.getValue(dsrowindex, "initialdepartmentid");
            }
            r[1] = "";
        } else if (aColName.contains("verifiedmetastasis")) {
            r[0] = dataset.getValue(dsrowindex, "verifiedmetastasisname");
            r[1] = "";
        } else if (aColName.contains("metastasisid")) {
            r[0] = dataset.getValue(dsrowindex, "metastasisdesc");
            r[1] = "";
        } else if (aColName.contains("diseaseid")) {
            r[0] = dataset.getValue(dsrowindex, "diseasedesc");
            r[1] = dataset.getValue(dsrowindex, "diseasefullname");
        } else if (aColName.contains("tissueid")) {
            r[0] = dataset.getValue(dsrowindex, "tissuedesc");
            r[1] = dataset.getValue(dsrowindex, "tissuefullname");
        } else if (aColName.contains("microdiseaseid")) {
            r[0] = dataset.getValue(dsrowindex, "microdiseasename");
            r[1] = dataset.getValue(dsrowindex, "microdiseasefullname");
        } else if (aColName.contains("microtissueid")) {
            r[0] = dataset.getValue(dsrowindex, "microtissuename");
            r[1] = dataset.getValue(dsrowindex, "microtissuefullname");
        } else if (aColName.contains("clinicaldiagid")) {
            r[0] = dataset.getValue(dsrowindex, "clinicaldiagdesc");
            r[1] = dataset.getValue(dsrowindex, "clinicaldiagfullname");
        } else if (aColName.contains("sampletypeid")) {
            r[0] = dataset.getValue(dsrowindex, "sampletypeid");
            r[1] = dataset.getValue(dsrowindex, "sampletypeid");
        }
        return r;
    }

    private String getSLookup(String aColName) {
        String sLookup = "";
        if (aColName.equalsIgnoreCase("initialdepartmentid")) {
            sLookup = "LV_ShipLocationLooku&lookupcallback=SetOrigName";
        } else if (aColName.contains("metastasis")) {
            sLookup = "LV_MetastasisLookup&lookupcallback=setLookupFieldValue";
        } else if (aColName.contains("clinicaldiag")) {
            sLookup = "LV_ClinicalLookup&lookupcallback=setLookupFieldValue";
        } else if (aColName.contains("tissue")) {
            sLookup = "LV_TissueLookup&lookupcallback=setLookupFieldValue";
        } else if (aColName.contains("disease")) {
            sLookup = "LV_DiseaseLookup&lookupcallback=setLookupFieldValue";
        } else if (aColName.contains("sampletypeid")) {
            sLookup = "SampleTypeLookSingle&lookupcallback=setSampleTypeLookupFieldValue";
        }
        return sLookup;
    }

    private String writeDataSetDropDown(DataSet dataset, int columnIndex, int dsrowindex, PropertyList props, String rowid) throws IOException {
        String displayvalue;
        HashMap<String, String> filter;
        String whereclause;
        SDIRequest sdiRequest;
        StringBuilder sb = new StringBuilder();
        String name = this.aColName[columnIndex];
        String querywhere = "";
        String queryorderby = "";
        String valuecolumn = "";
        String displaycolumn = "";
        String disabled = props.getProperty("disabled", "N");
        String colValue = props.getProperty("colValue");
        String studyid = props.getProperty("studyid");
        String sampletypeid = props.getProperty("sampletypeid");
        String columnName = props.getProperty("columnName");
        String samplefamilyid = dataset.getString(dsrowindex, "s_samplefamilyid");
        boolean hideSampleFamilyColumn = YES.equals(props.getProperty("hideSampleFamilyColumn"));
        boolean isSampleFamilyColumn = YES.equals(props.getProperty("isSampleFamilyColumn"));
        boolean familyreadonly = isSampleFamilyColumn && hideSampleFamilyColumn;
        try {
            JSONObject dropdowndefinition = this.jsonDropDown.has(name) ? this.jsonDropDown.getJSONObject(name) : new JSONObject();
            querywhere = dropdowndefinition.has("querywhere") ? dropdowndefinition.getString("querywhere") : "";
            queryorderby = dropdowndefinition.has("queryorderby") ? dropdowndefinition.getString("queryorderby") : "";
            valuecolumn = dropdowndefinition.has("valuecolumn") ? dropdowndefinition.getString("valuecolumn") : "";
            displaycolumn = dropdowndefinition.has("displaycolumn") ? dropdowndefinition.getString("displaycolumn") : "";
        }
        catch (JSONException dropdowndefinition) {
            // empty catch block
        }
        boolean readonly = "disabled".equals(disabled);
        if (StringUtil.getLen(querywhere) > 0L) {
            querywhere = this.parseDropDownWhereSubstitutions(querywhere, dsrowindex);
        }
        String onchange = name.equalsIgnoreCase("sampletypeid") ? "parent.setChanges();lvHandleSampleTypeChange(this, " + rowid + ");" : "parent.setChanges();";
        String key = "";
        if (name.equalsIgnoreCase("restrictclassid")) {
            key = name + columnIndex + studyid;
            if (!this.ddCache.containsKey(key)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_restrictclassid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "restrictclassdesc";
                SDIRequest sdiRequestRestClass = new SDIRequest();
                sdiRequestRestClass.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                sdiRequestRestClass.setSDCid("LV_RestClass");
                sdiRequestRestClass.setQueryFrom("s_restrictclass");
                if (StringUtil.getLen(querywhere) > 0L) {
                    sdiRequestRestClass.setQueryWhere("sstudyid in ('" + studyid + "') and " + querywhere);
                } else {
                    sdiRequestRestClass.setQueryWhere("sstudyid in ('" + studyid + "')");
                }
                sdiRequestRestClass.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                sdiRequestRestClass.setShowTemplates(false);
                this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequestRestClass).getDataset("primary"));
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else if (name.equalsIgnoreCase("studysiteid")) {
            if (this.isProtocol) {
                onchange = "lvHandleProtocolStudySiteChange( this, " + rowid + ");";
            }
            if (!this.ddCache.containsKey(key = name + columnIndex + studyid)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_studysiteid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "studysitedesc";
                SDIRequest sdiRequestStudySite = new SDIRequest();
                sdiRequestStudySite.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                sdiRequestStudySite.setSDCid("LV_StudySite");
                sdiRequestStudySite.setQueryFrom("s_studysite");
                if (StringUtil.getLen(querywhere) > 0L) {
                    sdiRequestStudySite.setQueryWhere("sstudyid in ('" + studyid + "') and " + querywhere);
                } else {
                    sdiRequestStudySite.setQueryWhere("sstudyid in ('" + studyid + "')");
                }
                sdiRequestStudySite.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequestStudySite).getDataset("primary"));
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else if (name.equalsIgnoreCase("sampletypeid")) {
            key = name + columnIndex;
            if (!this.ddCache.containsKey(key)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_sampletypeid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "sampletypedesc";
                SDIRequest sdiRequestClinicalEvent = new SDIRequest();
                sdiRequestClinicalEvent.setSDCid("SampleType");
                sdiRequestClinicalEvent.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                sdiRequestClinicalEvent.setQueryFrom("s_sampletype");
                sdiRequestClinicalEvent.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                if (StringUtil.getLen(querywhere) > 0L) {
                    sdiRequestClinicalEvent.setQueryWhere(querywhere);
                }
                this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequestClinicalEvent).getDataset("primary"));
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else if (name.equalsIgnoreCase("ageunits") || name.equalsIgnoreCase("initialvolumeunits") || name.equalsIgnoreCase("concentrationunits") || name.equalsIgnoreCase("initialmassunits")) {
            key = name + columnIndex;
            if (!this.ddCache.containsKey(key)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "unitsid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "unitsid";
                String categoryid = "";
                if (name.equalsIgnoreCase("ageunits")) {
                    categoryid = "AgeUnits";
                } else if (name.equalsIgnoreCase("initialvolumeunits")) {
                    categoryid = "VolumeUnits";
                } else if (name.equalsIgnoreCase("concentrationunits")) {
                    categoryid = "ConcUnits";
                } else if (name.equalsIgnoreCase("initialmassunits")) {
                    categoryid = "MassUnits";
                }
                SDIRequest sdiRequestAgeUnits = new SDIRequest();
                sdiRequestAgeUnits.setSDCid("Units");
                sdiRequestAgeUnits.setRequestItem("primary[" + valuecolumn + (!valuecolumn.equals(displaycolumn) ? "," + displaycolumn : "") + "]");
                sdiRequestAgeUnits.setQueryFrom("units, categoryitem");
                sdiRequestAgeUnits.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                if (StringUtil.getLen(querywhere) > 0L) {
                    sdiRequestAgeUnits.setQueryWhere("categoryitem.sdcid='Units' and categoryitem.keyid1=units.unitsid and categoryitem.categoryid='" + categoryid + "' and " + querywhere);
                } else {
                    sdiRequestAgeUnits.setQueryWhere("categoryitem.sdcid='Units' and categoryitem.keyid1=units.unitsid and categoryitem.categoryid='" + categoryid + "'");
                }
                this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequestAgeUnits).getDataset("primary"));
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else if (name.equalsIgnoreCase("preptypeid")) {
            key = name + columnIndex + sampletypeid;
            if (!this.ddCache.containsKey(key)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_preptypeid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "preptypedesc";
                sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_PrepType");
                sdiRequest.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                sdiRequest.setQueryFrom("s_preptype");
                sdiRequest.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                whereclause = "s_preptypeid in (select t.s_preptypeid from s_preptypesampletypemap t where t.destsampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, this.isOracle) + "' and t.activeflag != 'N')";
                if (StringUtil.getLen(querywhere) > 0L) {
                    whereclause = whereclause + " and " + querywhere;
                }
                sdiRequest.setQueryWhere(whereclause);
                this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary"));
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else if (name.equalsIgnoreCase("collectmethodid")) {
            key = name + columnIndex + sampletypeid;
            if (!this.ddCache.containsKey(key)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_collectmethodid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "collectmethoddesc";
                sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_CollectMeth");
                sdiRequest.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                sdiRequest.setQueryFrom("s_collectmethod");
                sdiRequest.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                whereclause = "s_collectmethodid in (select t.s_collectmethodid from s_sampletypecollectmethodmap t where t.s_sampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, this.isOracle) + "' and t.activeflag != 'N')";
                if (StringUtil.getLen(querywhere) > 0L) {
                    whereclause = whereclause + " and " + querywhere;
                }
                sdiRequest.setQueryWhere(whereclause);
                this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary"));
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else if (name.equalsIgnoreCase("containertypeid")) {
            key = name + columnIndex + sampletypeid;
            if (!this.ddCache.containsKey(key)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "containertypeid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "containertypedesc";
                sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("ContainerType");
                sdiRequest.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                sdiRequest.setQueryFrom("containertype");
                sdiRequest.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                whereclause = "containertypeid in (select t.containertypeid from s_sampletypecontainertype t where t.s_sampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, this.isOracle) + "' and t.activeflag != 'N')";
                if (StringUtil.getLen(querywhere) > 0L) {
                    whereclause = whereclause + " and " + querywhere;
                }
                sdiRequest.setQueryWhere(whereclause);
                this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary"));
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else if (name.equalsIgnoreCase("protocol_cohort")) {
            if (this.isProtocol) {
                onchange = "lvHandleProtocolCohortChange( this, " + rowid + ");";
            }
            if (!this.ddCache.containsKey(key = name + columnIndex + samplefamilyid)) {
                valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_cpcohortid";
                displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "s_cpcohortid";
                StringBuilder sql = new StringBuilder();
                sql.append("select s_cpcohort.s_cpcohortid");
                sql.append("  from s_cpcohort, s_samplefamily, s_clinicalprotocol");
                sql.append("  where s_cpcohort.s_clinicalprotocolid = s_clinicalprotocol.s_clinicalprotocolid");
                sql.append("  and s_cpcohort.s_clinicalprotocolversionid = s_clinicalprotocol.s_clinicalprotocolversionid");
                sql.append("  and s_cpcohort.s_clinicalprotocolrevision = s_clinicalprotocol.s_clinicalprotocolrevision");
                sql.append("  and s_clinicalprotocol.s_clinicalprotocolid = s_samplefamily.sstudyid");
                sql.append("  and s_clinicalprotocol.s_clinicalprotocolrevision = s_samplefamily.clinicalprotocolrevision");
                sql.append("  and s_samplefamily.s_samplefamilyid = ?");
                sql.append("  and s_clinicalprotocol.versionstatus = 'C'");
                sql.append("  order by s_cpcohort.s_cpcohortid");
                DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{samplefamilyid});
                this.ddCache.put(key, _ds);
                this.ddCache.put(key + "_valuecolumn", valuecolumn);
                this.ddCache.put(key + "_displaycolumn", displaycolumn);
            }
        } else {
            String link = this.aColTypeVal[columnIndex];
            key = name + columnIndex;
            String idcolumn = "";
            String desccolumn = "";
            if (!this.ddCache.containsKey(key)) {
                HashMap sdcProps = this.getSDCProcessor().getSDCProperties(link);
                if (sdcProps.size() > 0) {
                    idcolumn = (String)sdcProps.get("keycolid1");
                    desccolumn = (String)sdcProps.get("desccol");
                    valuecolumn = valuecolumn.length() == 0 ? idcolumn : valuecolumn;
                    displaycolumn = displaycolumn.length() == 0 ? desccolumn : displaycolumn;
                    SDIRequest sdiRequest2 = new SDIRequest();
                    sdiRequest2.setSDCid(link);
                    sdiRequest2.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
                    sdiRequest2.setQueryFrom((String)sdcProps.get("tableid"));
                    sdiRequest2.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
                    if (StringUtil.getLen(querywhere) > 0L) {
                        sdiRequest2.setQueryWhere(querywhere);
                    }
                    this.ddCache.put(key, this.getSDIProcessor().getSDIData(sdiRequest2).getDataset("primary"));
                    this.ddCache.put(key + "_valuecolumn", valuecolumn);
                    this.ddCache.put(key + "_displaycolumn", displaycolumn);
                } else {
                    valuecolumn = "refvalueid";
                    displaycolumn = "refvaluedesc";
                    DataSet dsReftype = this.getQueryProcessor().getRefTypeDataSet(link);
                    if (dsReftype != null) {
                        this.ddCache.put(key, dsReftype);
                    } else {
                        this.ddCache.put(key, new DataSet());
                    }
                    this.ddCache.put(key + "_valuecolumn", valuecolumn);
                    this.ddCache.put(key + "_displaycolumn", displaycolumn);
                }
            }
        }
        DataSet _ds = (DataSet)this.ddCache.get(key);
        valuecolumn = (String)this.ddCache.get(key + "_valuecolumn");
        displaycolumn = (String)this.ddCache.get(key + "_displaycolumn");
        String fieldid = "pr" + rowid + "_" + name;
        if (familyreadonly) {
            if (dataset != null) {
                filter = new HashMap<String, String>();
                filter.put(valuecolumn, colValue);
                int row = _ds.findRow(filter);
                displayvalue = colValue;
                if (row != -1) {
                    displayvalue = _ds.getString(row, displaycolumn, colValue);
                }
                sb.append(this.formatFamilyReadOnlyView(samplefamilyid, columnName, displayvalue));
            }
        } else if (readonly) {
            if (dataset != null) {
                filter = new HashMap();
                filter.put(valuecolumn, colValue);
                int row = _ds.findRow(filter);
                displayvalue = colValue;
                if (row != -1) {
                    displayvalue = _ds.getString(row, displaycolumn, colValue);
                }
                sb.append("<input type='hidden' name='").append(fieldid).append("' id='").append(fieldid).append("'");
                sb.append(" value=\"").append(colValue).append("\">");
                sb.append("<div style='padding:4px;'>").append(displayvalue).append("</div>");
            }
        } else {
            StringBuilder sb_option = new StringBuilder();
            boolean valueExists = false;
            if (_ds != null) {
                for (int i = 0; i < _ds.size(); ++i) {
                    String datavalue = _ds.getString(i, valuecolumn);
                    String displayvalue2 = _ds.getString(i, displaycolumn, datavalue);
                    String selected = "";
                    if (colValue.equals(datavalue)) {
                        valueExists = true;
                        selected = " selected";
                    }
                    sb_option.append("<option value=\"").append(datavalue).append("\"").append(selected).append(">").append(displayvalue2).append("</option>");
                }
                if (!valueExists && StringUtil.getLen(colValue) > 0L) {
                    if (name.equalsIgnoreCase("restrictclassid")) {
                        sb_option.append("<option value=\"").append(colValue).append("\" selected>?-").append(this.getSDIDescValue("LV_RestClass", colValue)).append("-?</option>");
                    } else if (name.equalsIgnoreCase("studysiteid")) {
                        sb_option.append("<option value=\"").append(colValue).append("\" selected>?-").append(this.getSDIDescValue("LV_StudySite", colValue)).append("-?</option>");
                    } else if (name.equalsIgnoreCase("containertypeid")) {
                        sb_option.append("<option value=\"").append(colValue).append("\" selected>?-").append(this.getSDIDescValue("ContainerType", colValue)).append("-?</option>");
                    } else if (name.equalsIgnoreCase("preptypeid")) {
                        sb_option.append("<option value=\"").append(colValue).append("\" selected>?-").append(this.getSDIDescValue("LV_PrepType", colValue)).append("-?</option>");
                    } else if (name.equalsIgnoreCase("collectmethodid")) {
                        sb_option.append("<option value=\"").append(colValue).append("\" selected>?-").append(this.getSDIDescValue("LV_CollectMeth", colValue)).append("-?</option>");
                    } else {
                        sb_option.append("<option value=\"").append(colValue).append("\" selected>?-").append(colValue).append("-?</option>");
                    }
                }
            }
            sb.append("<select name='").append(fieldid).append("' id='").append(fieldid).append("'");
            sb.append(" ").append(YES.equals(disabled) ? "disabled" : "").append(" onchange='").append(onchange).append("'");
            sb.append(!valueExists && StringUtil.getLen(colValue) > 0L ? " style='background:#FFD2D2'" : "");
            if (isSampleFamilyColumn) {
                sb.append(" class='familysource' columnname='").append(columnName).append("' familyid='").append(samplefamilyid).append("'");
            }
            sb.append(">");
            sb.append("<option value=''></option>");
            sb.append((CharSequence)sb_option);
            sb.append("</select>");
        }
        return sb.toString();
    }

    private String getSDIDescValue(String sdcid, String keyid) {
        Map<String, String> cache;
        HashMap sdcProps = this.getSDCProcessor().getSDCProperties(sdcid);
        String tableid = (String)sdcProps.get("tableid");
        String keycolid1 = (String)sdcProps.get("keycolid1");
        String desccol = (String)sdcProps.get("desccol");
        if (this.sdidesccache == null) {
            this.sdidesccache = new HashMap<String, Map<String, String>>();
        }
        if (!this.sdidesccache.containsKey(sdcid)) {
            this.sdidesccache.put(sdcid, new HashMap());
        }
        if (!(cache = this.sdidesccache.get(sdcid)).containsKey(keyid)) {
            cache.put(keyid, OpalUtil.getColumnValue(this.getQueryProcessor(), tableid, desccol, keycolid1 + " = ?", new String[]{keyid}));
        }
        return StringUtil.getLen(cache.get(keyid)) > 0L ? cache.get(keyid) : keyid;
    }

    private String getProtocolVisitDropDownHTML(DataSet dataset, int dsrowindex, boolean readonly, String rowid) {
        StringBuilder sb = new StringBuilder();
        String name = "protocol_visit";
        String studyid = dataset.getString(dsrowindex, "s_studyid", dataset.getString(dsrowindex, "sstudyid", ""));
        String studysiteid = dataset.getString(dsrowindex, "studysiteid", "");
        String cpcohortid = dataset.getString(dsrowindex, "cpcohortid", "");
        String eventdefid = dataset.getString(dsrowindex, "parenteventdefid", dataset.getString(dsrowindex, "eventdefid", ""));
        String eventlabel = dataset.getString(dsrowindex, "parenteventlabel", dataset.getString(dsrowindex, "eventlabel", ""));
        if (readonly) {
            sb.append("<div style='padding:4px'");
            sb.append(" class='familysource' columnname='").append(name).append("' familyid='").append(dataset.getString(dsrowindex, "s_samplefamilyid")).append("'>");
            sb.append(eventlabel);
            sb.append("<input type='hidden' name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
            sb.append(" value='").append(eventdefid).append("|").append(eventlabel).append("'>");
            sb.append("</div>");
        } else {
            boolean valueExists = false;
            String option = "";
            if (OpalUtil.isEmpty(eventdefid)) {
                String familyeventdefid = dataset.getString(dsrowindex, "familyparenteventdefid", dataset.getString(dsrowindex, "familyeventdefid"));
                if (OpalUtil.isNotEmpty(familyeventdefid)) {
                    String cacheKey = "familyeventdefoptions-" + familyeventdefid;
                    if (!this.localcache.containsKey(cacheKey)) {
                        StringBuilder sb_option = new StringBuilder();
                        StringBuilder sql = new StringBuilder();
                        sql.append("select e2.s_eventdefid, e2.eventdeflabel, e2.eventdeftype, e1.eventdeflabel eventlabel1");
                        sql.append(" from s_eventdef e1, s_eventdef e2");
                        sql.append(" where e1.s_eventdefid = ?");
                        sql.append(" and e2.clinicalprotocolid = e1.clinicalprotocolid");
                        sql.append(" and e2.clinicalprotocolversionid = e1.clinicalprotocolversionid");
                        sql.append(" and e2.clinicalprotocolrevision = e1.clinicalprotocolrevision");
                        sql.append(" and e2.cohortid = e1.cohortid");
                        sql.append(" and e2.eventdeftype = 'Visit'");
                        sql.append(" order by e2.usersequence");
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{familyeventdefid});
                        if (ds != null) {
                            valueExists = true;
                            for (int i = 0; i < ds.size(); ++i) {
                                String _eventdefid = ds.getString(i, "s_eventdefid", "");
                                String _eventlabel = ds.getString(i, "eventdeflabel", "");
                                String eventlabel1 = ds.getString(i, "eventlabel1", "");
                                String selected = "";
                                if (_eventlabel.equals(eventlabel1)) {
                                    selected = " selected";
                                }
                                sb_option.append("<option value=\"").append(_eventdefid).append("|").append(_eventlabel).append("\"").append(selected).append(">").append(_eventlabel).append("</option>");
                            }
                        }
                        this.localcache.put(cacheKey, sb_option.toString());
                    }
                    option = this.localcache.get(cacheKey);
                } else {
                    String protocolid = dataset.getString(dsrowindex, "protocolid");
                    String protocolversionid = dataset.getString(dsrowindex, "protocolversionid");
                    String protocolrevision = dataset.getString(dsrowindex, "protocolrevision");
                    if (OpalUtil.isNotEmpty(protocolid) && OpalUtil.isNotEmpty(protocolrevision) && OpalUtil.isNotEmpty(protocolrevision) && OpalUtil.isNotEmpty(cpcohortid)) {
                        String cacheKey = "eventoptions_" + cpcohortid + protocolid + protocolrevision + protocolversionid;
                        if (!this.localcache.containsKey(cacheKey)) {
                            StringBuilder sb_option = new StringBuilder();
                            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_eventdefid, eventdeflabel from s_eventdef where eventdeftype = 'Visit' and cohortid = ? and clinicalprotocolid = ? and clinicalprotocolrevision = ? and clinicalprotocolversionid = ? order by usersequence", (Object[])new String[]{cpcohortid, protocolid, protocolrevision, protocolversionid});
                            if (ds != null && ds.size() > 0) {
                                for (int i = 0; i < ds.size(); ++i) {
                                    String _eventdefid = ds.getString(i, "s_eventdefid", "");
                                    String _eventlabel = ds.getString(i, "eventdeflabel", "");
                                    sb_option.append("<option value=\"").append(_eventdefid).append("|").append(_eventlabel).append("\">").append(_eventlabel).append("</option>");
                                }
                            }
                            this.localcache.put(cacheKey, sb_option.toString());
                        }
                        option = this.localcache.get(cacheKey);
                    }
                }
            } else {
                ArrayList _ds = null;
                String participantid = dataset.getString(dsrowindex, "participantid");
                if (OpalUtil.isNotEmpty(participantid)) {
                    String key = "protocol_visit_" + participantid;
                    if (!this.ddCache.containsKey(key)) {
                        StringBuilder sql = new StringBuilder();
                        sql.append("select ed.s_eventdefid, ed.eventdeflabel, cp.versionstatus");
                        sql.append(" from s_eventdef ed, s_clinicalprotocol cp, s_participant p");
                        sql.append(" where ed.eventdeftype = 'Visit'");
                        sql.append(" and ed.cohortid = p.cpcohortid");
                        sql.append(" and ed.clinicalprotocolid = p.clinicalprotocolid");
                        sql.append(" and ed.clinicalprotocolrevision = p.clinicalprotocolrevision");
                        sql.append(" and ed.CLINICALPROTOCOLVERSIONID = cp.S_CLINICALPROTOCOLVERSIONID");
                        sql.append(" and cp.S_CLINICALPROTOCOLID = p.CLINICALPROTOCOLID");
                        sql.append(" and cp.S_CLINICALPROTOCOLREVISION = p.CLINICALPROTOCOLREVISION");
                        sql.append(" and cp.versionstatus = 'C'");
                        sql.append(" and p.s_participantid = ?");
                        sql.append(" order by ed.usersequence");
                        this.ddCache.put(key, this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{participantid}));
                    }
                    _ds = (DataSet)this.ddCache.get(key);
                }
                if (_ds != null) {
                    StringBuilder sb_option = new StringBuilder();
                    for (int i = 0; i < _ds.size(); ++i) {
                        String _eventdefid = ((DataSet)_ds).getString(i, "s_eventdefid", "");
                        String _eventlabel = ((DataSet)_ds).getString(i, "eventdeflabel", "");
                        String _versionstatus = ((DataSet)_ds).getString(i, "versionstatus", "");
                        String selected = "";
                        if (_eventlabel.equals(eventlabel)) {
                            selected = " selected";
                        }
                        if ("C".equals(_versionstatus)) {
                            sb_option.append("<option value=\"").append(_eventdefid).append("|").append(_eventlabel).append("\"").append(selected).append(">").append(_eventlabel).append("</option>");
                        }
                        if (!_eventdefid.equals(eventdefid)) continue;
                        valueExists = true;
                    }
                    if (!valueExists && StringUtil.getLen(eventlabel) > 0L) {
                        sb_option.append("<option value=\"").append(eventlabel).append("\" selected>?-").append(eventlabel).append("-?</option>");
                    }
                    option = sb_option.toString();
                }
            }
            sb.append("<select name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
            sb.append(" onchange='lvHandleProtocolVisitChange(this, ").append(rowid).append(")'");
            sb.append(" class='familysource' columnname='").append(name).append("' familyid='").append(dataset.getString(dsrowindex, "s_samplefamilyid")).append("'");
            sb.append(!valueExists && StringUtil.getLen(eventlabel) > 0L ? " style='background:#FFD2D2'" : "").append(">");
            sb.append("<option value=''></option>");
            sb.append(option == null ? "" : option);
            sb.append("</select>");
        }
        return sb.toString();
    }

    private String getProtocolVisitTimpointDropDownHTML(DataSet dataset, int dsrowindex, boolean readonly, String rowid) {
        StringBuilder sb = new StringBuilder();
        String name = "protocol_timepoint";
        String parenteventdefid = dataset.getString(dsrowindex, "parenteventdefid", "");
        String familyparenteventdefid = dataset.getString(dsrowindex, "familyparenteventdefid", "");
        String eventdefid = dataset.getString(dsrowindex, "eventdefid", "");
        String eventlabel = dataset.getString(dsrowindex, "eventlabel", "");
        if (parenteventdefid.length() > 0) {
            String key = name + parenteventdefid;
            if (!this.ddCache.containsKey(key)) {
                this.ddCache.put(key, this.getQueryProcessor().getPreparedSqlDataSet("select ed.s_eventdefid, ed.eventdeflabel from s_eventdef ed where ed.parenteventdefid = ? order by ed.eventdeflabel", (Object[])new String[]{parenteventdefid}));
            }
            if (readonly) {
                sb.append("<div style='padding:4px'");
                sb.append(" class='familysource' columnname='").append(name).append("' familyid='").append(dataset.getString(dsrowindex, "s_samplefamilyid")).append("'>");
                sb.append(eventlabel);
                sb.append("<input type='hidden' name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
                sb.append(" value='").append(eventdefid).append("|").append(eventlabel).append("'>");
                sb.append("</div>");
            } else {
                DataSet _ds = (DataSet)this.ddCache.get(key);
                StringBuilder sb_option = new StringBuilder();
                if (_ds != null) {
                    HashSet<String> set = new HashSet<String>();
                    for (int i = 0; i < _ds.size(); ++i) {
                        String _eventdefid = _ds.getString(i, "s_eventdefid", "");
                        String _eventlabel = _ds.getString(i, "eventdeflabel", "");
                        String selected = "";
                        if (_eventlabel.equals(eventlabel)) {
                            selected = " selected";
                        }
                        if (!set.contains(_eventlabel)) {
                            sb_option.append("<option value=\"").append(_eventdefid).append("|").append(_eventlabel).append("\"").append(selected).append(">").append(_eventlabel).append("</option>");
                        }
                        set.add(_eventlabel);
                    }
                }
                sb.append("<select name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
                sb.append(" class='familysource' columnname='").append(name).append("' familyid='").append(dataset.getString(dsrowindex, "s_samplefamilyid")).append("'");
                sb.append(" onchange='lvHandleProtocolVisitTimepointChange(this, ").append(rowid).append(")'>");
                sb.append("<option value=''></option>");
                sb.append((CharSequence)sb_option);
                sb.append("</select>");
            }
        } else if (OpalUtil.isNotEmpty(familyparenteventdefid)) {
            String familyeventdefid = dataset.getString(dsrowindex, "familyeventdefid", "");
            String familyeventdeflabel = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_eventdef", "eventdeflabel", "s_eventdefid = ?", new String[]{familyeventdefid});
            if (readonly) {
                sb.append("<div style='padding:4px'");
                sb.append(" class='familysource' columnname='").append(name).append("' familyid='").append(dataset.getString(dsrowindex, "s_samplefamilyid")).append("'");
                sb.append(">").append(familyeventdeflabel);
                sb.append("<input type='hidden' name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
                sb.append(" value='").append(familyeventdefid).append("|").append(familyeventdeflabel).append("'>");
                sb.append("</div>");
            } else {
                String key = "familyparenteventdefid-" + familyparenteventdefid;
                if (!this.ddCache.containsKey(key)) {
                    StringBuilder sql = new StringBuilder();
                    sql.append("select ed.s_eventdefid, ed.eventdeflabel");
                    sql.append("  from s_eventdef ed");
                    sql.append("  where ed.parenteventdefid = ").append("?").append("");
                    sql.append("  order by ed.usersequence");
                    this.ddCache.put(key, this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{familyparenteventdefid}));
                }
                DataSet _ds = (DataSet)this.ddCache.get(key);
                StringBuilder sb_option = new StringBuilder();
                if (_ds != null) {
                    for (int i = 0; i < _ds.size(); ++i) {
                        String _eventdefid = _ds.getString(i, "s_eventdefid", "");
                        String _eventlabel = _ds.getString(i, "eventdeflabel", "");
                        String selected = "";
                        if (_eventlabel.equals(familyeventdeflabel)) {
                            selected = " selected";
                        }
                        sb_option.append("<option value=\"").append(_eventdefid).append("|").append(_eventlabel).append("\"").append(selected).append(">").append(_eventlabel).append("</option>");
                    }
                }
                sb.append("<select name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
                sb.append(" class='familysource' columnname='").append(name).append("' familyid='").append(dataset.getString(dsrowindex, "s_samplefamilyid")).append("'");
                sb.append(" onchange='lvHandleProtocolVisitTimepointChange(this, ").append(rowid).append(")'>");
                sb.append("<option value=''></option>");
                sb.append((CharSequence)sb_option);
                sb.append("</select>");
            }
        } else if (readonly) {
            sb.append("<div style='padding:4px'>&nbsp;");
            sb.append("<input type='hidden' name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
            sb.append(" value=''>");
            sb.append("</div>");
        } else {
            sb.append("<select name='pr").append(rowid).append("_").append(name).append("' id='pr").append(rowid).append("_").append(name).append("'");
            sb.append(" class='familysource' columnname='").append(name).append("' familyid='").append(dataset.getString(dsrowindex, "s_samplefamilyid")).append("'");
            sb.append(" onchange='lvHandleProtocolVisitTimepointChange(this, ").append(rowid).append(")'>");
            sb.append("<option value=''></option>");
            sb.append("</select>");
        }
        return sb.toString();
    }

    public static String parseDropDownWhereSubstitutions(String dropdownWhere, String currentuser) {
        return StringUtil.replaceAll(dropdownWhere, "[currentuser]", currentuser);
    }

    private String parseDropDownWhereSubstitutions(String dropdownWhere, int dsrowindex) {
        String[] tokens;
        for (String token : tokens = StringUtil.getTokens(dropdownWhere)) {
            if ("currentuser".equals(token)) {
                dropdownWhere = StringUtil.replaceAll(dropdownWhere, "[currentuser]", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                continue;
            }
            if (!this.ds.isValidColumn(token)) continue;
            dropdownWhere = StringUtil.replaceAll(dropdownWhere, "[" + token + "]", this.ds.getValue(dsrowindex, token, ""));
        }
        return dropdownWhere;
    }

    public String processDataEntryAction() throws IOException {
        ServletRequest request = this.pageContext.getRequest();
        JspWriter out = this.pageContext.getOut();
        String dataentryaction = request.getParameter("dataentryaction");
        if ("batchcreatesubject".equals(dataentryaction)) {
            this.batchCreateSubject(request);
        } else if ("savesubmission".equals(dataentryaction)) {
            this.saveSubmission();
        } else if ("saveall".equals(dataentryaction)) {
            this.saveAllDataEntry();
        } else if ("subject".equals(dataentryaction)) {
            this.lookupSubject(out, request);
        } else if (dataentryaction != null && dataentryaction.contains("approve") || "verification".equals(dataentryaction) || "requestverification".equals(dataentryaction)) {
            this.approveDataEntry(out, request);
        }
        return "";
    }

    private void batchCreateSubject(ServletRequest request) throws IOException {
        JspWriter out = this.pageContext.getOut();
        String sError = "";
        boolean isSubmission = YES.equalsIgnoreCase(request.getParameter("isSampleSubmission"));
        String newsubjectid = "";
        String fieldid = request.getParameter("fieldid");
        String keyid1 = request.getParameter("itemid");
        Object[] keyid1s = StringUtil.split(keyid1, ";");
        StringBuilder sb = new StringBuilder();
        Arrays.sort(keyid1s);
        for (Object keyid11 : keyid1s) {
            sb.append(";").append((String)keyid11);
        }
        String[] aFid = StringUtil.split(fieldid, ";");
        String copies = "" + aFid.length;
        try {
            keyid1 = sb.substring(1);
            Object[] cols = StringUtil.split(request.getParameter("__pr_cols"), ";");
            Arrays.sort(cols);
            DataSet columns = this.getSDCProcessor().getColumnData("LV_Subject");
            ActionBlock ab = new ActionBlock();
            ab.setAction("Subject", "AddSDI", "1");
            ab.setActionProperty("Subject", "sdcid", "LV_Subject");
            ab.setActionProperty("Subject", "keyid1", "(auto)");
            ab.setActionProperty("Subject", "copies", copies);
            for (int i = 0; i < columns.getRowCount(); ++i) {
                String colid = columns.getValue(i, "columnid");
                if (Arrays.binarySearch(cols, colid) < 0) continue;
                ab.setActionProperty("Subject", colid, request.getParameter(colid) != null ? request.getParameter(colid) : "");
            }
            ab.setActionProperty("Subject", "newkeyid1", "[newsubjectid]");
            if (!isSubmission) {
                ab.setAction("SampleFamily", "EditSDI", "1");
                ab.setActionProperty("SampleFamily", "sdcid", "LV_SampleFamily");
                ab.setActionProperty("SampleFamily", "keyid1", keyid1);
                ab.setActionProperty("SampleFamily", "subjectid", "[newsubjectid]");
            }
            this.getActionProcessor().processActionBlock(ab);
            newsubjectid = ab.getActionProperty("Subject", "newkeyid1");
        }
        catch (ActionException ae) {
            ErrorHandler ehander = ae.getErrorHandler();
            sError = "Failed to process batch create subject.<br>" + ((ErrorDetail)ehander.get(0)).getMessage() + "<br>";
        }
        catch (Exception e) {
            Logger.logError("Batch Create Subject Failed", e);
            sError = "Batch Create Subject Failed. Unexpected Error When Batch Create Subject!";
            out.println("<script language='javascript'>alert( 'Batch Create Subject Failed. Unexpected Error When Batch Create Subject!' );</script>");
        }
        out.println("<script language='javascript'>");
        if (sError.length() == 0) {
            if (isSubmission && newsubjectid != null && newsubjectid.length() > 0) {
                String[] subjectids = StringUtil.split(newsubjectid, ";");
                if (subjectids.length == aFid.length) {
                    for (int i = 0; i < aFid.length; ++i) {
                        String tempfieldid = aFid[i].substring(2) + "_subjectid";
                        out.println("if(sapphire.page.getTop().maint_iframe.document.getElementById('" + tempfieldid + "'))");
                        out.println("sapphire.page.getTop().maint_iframe.document.getElementById('" + tempfieldid + "').value = '" + subjectids[i] + "';");
                        out.println("try { parent.mySetPageLoadCompleted(); } catch (e) {}");
                    }
                }
            } else {
                out.println("sapphire.page.getTop().refreshData();");
            }
        } else {
            if (sError.contains("::") && sError.contains("|")) {
                sError = sError.substring(sError.lastIndexOf("::") + 2, sError.lastIndexOf("|"));
            }
            out.println("parent.sapphire.alert('" + DataEntryPageUtil.escapeError(sError) + "', 'error');");
            out.println("try { parent.mySetPageLoadCompleted(); } catch (e) {}");
        }
        out.println("</script>");
    }

    private void lookupSubject(JspWriter out, ServletRequest request) throws IOException {
        String act = request.getParameter("nextact");
        boolean allFound = true;
        if (act != null && act.length() > 0) {
            String fieldid = request.getParameter("fieldid");
            String aliasname = request.getParameter("itemid");
            String keyid1 = request.getParameter("keyid1");
            if (keyid1 != null) {
                keyid1 = keyid1.replaceAll("%3B", ";");
            }
            String[] aFid = StringUtil.split(fieldid, ";");
            String[] aItem = StringUtil.split(aliasname, ";");
            String[] sampleids = StringUtil.split(keyid1, ";");
            HashMap<String, String> sampleidStudyMap = null;
            String sql = "select sf.subjectid from s_sample sa, s_samplefamily sf, s_study st, s_studysite ss, s_subject su where sa.sstudyid=st.s_studyid and sa.samplefamilyid=sf.s_samplefamilyid and ss.s_studysiteid=sf.studysiteid and sf.subjectid=su.s_subjectid and su.activeflag != 'N'";
            for (int i = 0; i < aFid.length; ++i) {
                fieldid = aFid[i].substring(2);
                String[] aaItem = StringUtil.split(aItem[i], "{{");
                String protocolid = aaItem[0];
                String siteid = aaItem[1];
                String aliasid = aaItem[2];
                SafeSQL parentSafeSql = new SafeSQL();
                String newSql = sql;
                if (protocolid.length() == 0) {
                    if (sampleidStudyMap == null) {
                        sampleidStudyMap = new HashMap<String, String>();
                        SafeSQL safeSQL = new SafeSQL();
                        String s = "select sstudyid, s_sampleid from s_sample where s_sampleid in (" + safeSQL.addIn(keyid1, ";") + ")";
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(s, safeSQL.getValues());
                        for (int k = 0; k < ds.getRowCount(); ++k) {
                            sampleidStudyMap.put(ds.getString(k, "s_sampleid"), ds.getString(k, "sstudyid"));
                        }
                    }
                    int rowid = Integer.parseInt(fieldid.substring(2));
                    String studyid = (String)sampleidStudyMap.get(sampleids[rowid]);
                    newSql = siteid.length() == 0 ? "select sf.subjectid from s_sample sa, s_samplefamily sf, s_subject su where sa.samplefamilyid=sf.s_samplefamilyid and sf.subjectid=su.s_subjectid and su.activeflag != 'N' and sf.sstudyid=" + parentSafeSql.addVar(studyid) + " and sf.studysiteid is null and sf.externalsubject=" + parentSafeSql.addVar(aliasid) + "" : newSql + " and sf.sstudyid=" + parentSafeSql.addVar(studyid) + " and ss.studysitedesc=" + parentSafeSql.addVar(siteid) + " and sf.externalsubject=" + parentSafeSql.addVar(aliasid) + "";
                } else {
                    newSql = siteid.length() == 0 ? "select sf.subjectid from s_sample sa, s_samplefamily sf, s_study st, s_subject su where sa.sstudyid=st.s_studyid and sa.samplefamilyid=sf.s_samplefamilyid and sf.subjectid=su.s_subjectid and su.activeflag != 'N' and st.protocolname=" + parentSafeSql.addVar(protocolid) + " and sf.studysiteid is null and sf.externalsubject=" + parentSafeSql.addVar(aliasid) + "" : newSql + " and st.protocolname=" + parentSafeSql.addVar(protocolid) + " and ss.studysitedesc=" + parentSafeSql.addVar(siteid) + " and sf.externalsubject=" + parentSafeSql.addVar(aliasid) + "";
                }
                if (aliasid.length() > 0) {
                    String subjectid = "";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(newSql, parentSafeSql.getValues());
                    if (ds != null && ds.size() > 0) {
                        subjectid = ds.getString(0, "subjectid");
                    }
                    if (StringUtil.getLen(subjectid) == 0L) {
                        allFound = false;
                    }
                    out.println("<script language='javascript'>");
                    out.println("if(sapphire.page.getTop().maint_iframe.document.getElementById('" + fieldid + "_subjectid'))");
                    out.println("sapphire.page.getTop().maint_iframe.document.getElementById('" + fieldid + "_subjectid').value = '" + subjectid + "';");
                    out.println("</script>");
                    continue;
                }
                allFound = false;
            }
        }
        if (!allFound) {
            out.println("<script language='javascript'>sapphire.page.getTop().showMessage('" + this.getTranslationProcessor().translate("Lookup for subject for some or all of the samples did not return any existing subjects.") + "');</script>");
        }
    }

    private String saveSubmission() throws IOException {
        ServletRequest request = this.pageContext.getRequest();
        String sPage = request.getParameter("sPage");
        String sError = "";
        String newSampleList = "";
        String newGroupName = "";
        String[] aColName = (String[])this.pageContext.getServletContext().getAttribute(sPage + "aColName[]");
        String[] aColSdcid = (String[])this.pageContext.getServletContext().getAttribute(sPage + "aColSdcid[]");
        try {
            int maxcol = aColName.length;
            int rows = 0;
            while (request.getParameter("__pr" + rows + "_key") != null) {
                ++rows;
            }
            if (rows > 0) {
                ActionBlock ab = new ActionBlock();
                ab.setAction("SampleFamily", "AddSDI", "1");
                ab.setActionProperty("SampleFamily", "sdcid", "LV_SampleFamily");
                if (YES.equals(request.getParameter("confirm"))) {
                    ab.setActionProperty("SampleFamily", "__sdcruleconfirm", YES);
                }
                ab.setActionProperty("SampleFamily", "newkeyid1", "[newsamplefamilyid]");
                ab.setAction("Sample", "AddSDI", "1");
                ab.setActionProperty("Sample", "sdcid", "Sample");
                ab.setActionProperty("Sample", "samplefamilyid", "[newsamplefamilyid]");
                ab.setActionProperty("Sample", "storagestatus", "Allocated");
                ab.setActionProperty("Sample", "newkeyid1", "[newsampleid]");
                ab.setAction("Alias", "AddSDIAlias", "1");
                ab.setActionProperty("Alias", "sdcid", "Sample");
                ab.setActionProperty("Alias", "keyid1", "[newsampleid]");
                for (int i = 0; i < maxcol; ++i) {
                    String colname = aColName[i];
                    String sdcid = aColSdcid[i];
                    if (sdcid == null || sdcid.equals("")) {
                        sdcid = "Alias";
                    }
                    if (sdcid.equals("Study")) {
                        String studyid = request.getParameter("pr0_s_studyid");
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT defaultglpflag FROM s_study WHERE s_studyid = ?", new Object[]{studyid});
                        String glpflag = ds.getValue(0, "defaultglpflag");
                        String rationing = ds.getValue(0, "u_defaultrationing");
                        StringBuilder studyidlist = new StringBuilder();
                        StringBuilder glplist = new StringBuilder();
                        for (int row = 0; row < rows; ++row) {
                            studyidlist.append(";").append(studyid);
                            glplist.append(";").append(glpflag);
                        }
                        ab.setActionProperty("Sample", "sstudyid", studyidlist.substring(1));
                        ab.setActionProperty("Sample", "glpflag", glplist.substring(1));
                        ab.setActionProperty("SampleFamily", "sstudyid", studyidlist.substring(1));
                        continue;
                    }
                    StringBuilder values = new StringBuilder();
                    for (int row = 0; row < rows; ++row) {
                        String value = request.getParameter("pr" + row + "_" + colname);
                        values.append(";").append(value == null ? "" : value);
                    }
                    if (sdcid.equals("LV_SampleFamily")) {
                        ab.setActionProperty("SampleFamily", colname, values.substring(1));
                        continue;
                    }
                    if (sdcid.equals("TrackItemSDC")) continue;
                    ab.setActionProperty(sdcid, colname, values.substring(1));
                }
                this.getActionProcessor().processActionBlock(ab);
                newSampleList = ab.getActionProperty("Sample", "newkeyid1");
                HashMap<String, String> propsFolder = new HashMap<String, String>();
                propsFolder.put("sdcid", "Sample");
                propsFolder.put("keyid1", newSampleList);
                this.getActionProcessor().processAction("AddFolder", "1", propsFolder);
            }
        }
        catch (ActionException e) {
            ErrorHandler ehandler = e.getErrorHandler();
            sError = "Unable to create new Samples: <br>" + ((ErrorDetail)ehandler.get(0)).getMessage() + "<br>";
        }
        catch (Exception ne) {
            Logger.logError(ne.getMessage(), ne);
            sError = "An unexpected error happened. Possibly due to session expiration or server restarted.";
        }
        JspWriter out = this.pageContext.getOut();
        if (sError.length() > 0) {
            out.println("<script>");
            out.println("try { parent.mySetPageLoadCompleted(); } catch (e) {}");
            String confirm = "N";
            if (sError.contains("::CONFIRM::")) {
                confirm = YES;
            }
            if (sError.contains("::") && sError.contains("|")) {
                sError = sError.substring(sError.lastIndexOf("::") + 2, sError.lastIndexOf("|"));
            }
            out.println("sapphire.page.getTop().openPopup('LV_ErrorPage&confirm=" + confirm + "','_newWin','" + DataEntryPageUtil.escapeError(sError) + "');");
            out.println("</script>");
        } else {
            out.println("<script>");
            out.println("  sapphire.page.getTop().location = 'rc?command=page&page=LV_AdminSampleList&keyid1=" + HttpUtil.encodeURIComponent(newSampleList) + "&message=" + HttpUtil.encodeURIComponent(newGroupName) + "';");
            out.println("</script>");
        }
        return newSampleList;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void saveAllDataEntry() throws IOException {
        ServletRequest request = this.pageContext.getRequest();
        String sPage = request.getParameter("sPage");
        String[] aColName = (String[])this.pageContext.getServletContext().getAttribute(sPage + "aColName[]");
        String[] aColId = (String[])this.pageContext.getServletContext().getAttribute(sPage + "aColId[]");
        String[] aColSave = (String[])this.pageContext.getServletContext().getAttribute(sPage + "aColSave[]");
        String[] aColSdcid = (String[])this.pageContext.getServletContext().getAttribute(sPage + "aColSdcid[]");
        String[] aColType = (String[])this.pageContext.getServletContext().getAttribute(sPage + "aColType[]");
        StringBuilder sError = new StringBuilder();
        String sAct = request.getParameter("act");
        if (sAct.equalsIgnoreCase("P")) {
            block71: {
                try {
                    int maxcol = aColName.length;
                    StringBuilder mis_clinicalsiteid = new StringBuilder();
                    StringBuilder mis_dobdaymonth = new StringBuilder();
                    String subjectid = "";
                    String studyid = "";
                    String s_sampleid = "";
                    String externalsubjectid = "";
                    String aliastype = "";
                    String aliasid = "";
                    String aliasact = "";
                    int rows = Integer.parseInt(request.getParameter("__pr_rows"));
                    for (int c = 0; c < maxcol; ++c) {
                        String columnName = aColName[c];
                        if (columnName.startsWith("protocol_") || !aColSave[c].equalsIgnoreCase(YES)) continue;
                        String propertyid = "__u_" + aColId[c] + "_" + aColSdcid[c];
                        if (aColId[c].equalsIgnoreCase("externalsubject")) {
                            externalsubjectid = request.getParameter(propertyid);
                            continue;
                        }
                        if (aColId[c].equalsIgnoreCase("subjectid")) {
                            subjectid = request.getParameter(propertyid);
                            continue;
                        }
                        if (aColId[c].equalsIgnoreCase("keyid1")) {
                            if (aColSdcid[c].equalsIgnoreCase("Study")) {
                                studyid = request.getParameter(propertyid);
                                continue;
                            }
                            if (!aColSdcid[c].equalsIgnoreCase("Sample")) continue;
                            s_sampleid = request.getParameter(propertyid);
                            continue;
                        }
                        if (aColId[c].equalsIgnoreCase("aliastype")) {
                            aliastype = request.getParameter(propertyid);
                            continue;
                        }
                        if (!aColId[c].equalsIgnoreCase("aliasid")) continue;
                        aliasid = request.getParameter(propertyid);
                    }
                    aliasact = request.getParameter("__u_aliasact_");
                    String sAuditreason = request.getParameter("auditreason");
                    String sAuditActivity = request.getParameter("auditactivity");
                    String sAuditSignedFlag = request.getParameter("auditsignedflag");
                    String[] aStudyid = StringUtil.split(studyid, ";");
                    String[] aSampleid = StringUtil.split(s_sampleid, ";");
                    String[] aExternalsubjectid = StringUtil.split(externalsubjectid, ";");
                    String[] aSubjectid = StringUtil.split(subjectid, ";");
                    HashMap<String, String> studyMap = new HashMap<String, String>();
                    StringBuilder sql = new StringBuilder();
                    sql.append("select s_study.s_studyid, s_study.hipaaflag, s_studysite.studysitedesc ");
                    sql.append(" from s_study, s_studysite");
                    sql.append(" where s_study.s_studyid = s_studysite.sstudyid ");
                    SafeSQL safeSQL = new SafeSQL();
                    sql.append(" and s_study.s_studyid in (").append(safeSQL.addIn(studyid, ";")).append(")");
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null) {
                        for (int i = 0; i < ds.size(); ++i) {
                            studyMap.put(ds.getValue(i, "s_studyid"), ds.getValue(i, "studysitedesc"));
                        }
                    }
                    HashMap<String, String> hipaaMap = new HashMap<String, String>();
                    sql.setLength(0);
                    safeSQL.reset();
                    sql.append("select s_studyid, hipaaflag from s_study");
                    sql.append(" where s_studyid in (").append(safeSQL.addIn(studyid, ";")).append(")");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null) {
                        for (int i = 0; i < ds.size(); ++i) {
                            hipaaMap.put(ds.getValue(i, "s_studyid"), ds.getValue(i, "hipaaflag"));
                        }
                    }
                    HashMap<String, String> subjectMap = new HashMap<String, String>();
                    if (OpalUtil.isNotEmpty(subjectid)) {
                        sql.setLength(0);
                        SafeSQL childSafeSQL = new SafeSQL();
                        if (this.isOracle) {
                            sql.append("select s_subjectid, trim(to_char(birthdt,'DD')) dobday, trim(to_char(birthdt,'MM')) dobmonth");
                            sql.append(" from s_subject");
                            sql.append(" where ( ( trim(to_char(birthdt,'DD')) is not null ");
                            sql.append(" and trim(to_char(birthdt,'DD')) != '01' ) or ( trim(to_char(birthdt,'MM')) is not null");
                            sql.append(" and trim(to_char(birthdt,'MM')) != '01'  ) )");
                            sql.append(" and s_subjectid in (").append(childSafeSQL.addIn(subjectid, ";")).append(")");
                        } else {
                            sql.append("select s_subjectid, DATEPART( dd, birthdt ) dobday, DATEPART( mm, birthdt ) dobmonth");
                            sql.append(" from s_subject");
                            sql.append(" where ( ( DATEPART( dd, birthdt) is not null and DATEPART( dd, birthdt ) != '01' ) ");
                            sql.append(" or ( DATEPART( mm, birthdt ) is not null and DATEPART( mm, birthdt ) != '01' ) ) ");
                            sql.append(" and s_subjectid in (").append(childSafeSQL.addIn(subjectid, ";")).append(")");
                        }
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), childSafeSQL.getValues());
                        if (ds != null) {
                            for (int i = 0; i < ds.size(); ++i) {
                                subjectMap.put(ds.getValue(i, "s_subjectid"), ds.getValue(i, "dobmonth"));
                            }
                        }
                    }
                    for (int i = 0; i < aStudyid.length; ++i) {
                        String dobdaymonth;
                        String hippaflag;
                        String sampleid = aSampleid[i];
                        String clinicalsite = (String)studyMap.get(aStudyid[i]);
                        if (clinicalsite != null && clinicalsite.length() < 1) {
                            mis_clinicalsiteid.append(sampleid).append(", ");
                        }
                        if (aSubjectid.length <= i || !YES.equalsIgnoreCase(hippaflag = (String)hipaaMap.get(aStudyid[i])) || (dobdaymonth = (String)subjectMap.get(aSubjectid[i])) == null || dobdaymonth.length() <= 0 || mis_dobdaymonth.indexOf(sampleid) >= 0) continue;
                        mis_dobdaymonth.append(sampleid).append(", ");
                    }
                    if (mis_clinicalsiteid.length() > 0) {
                        mis_clinicalsiteid.setLength(mis_clinicalsiteid.length() - 2);
                    }
                    if (mis_dobdaymonth.length() > 0) {
                        mis_dobdaymonth.setLength(mis_dobdaymonth.length() - 2);
                        sError.append("The following sample(s) with Study HIPPA but the DOB Day and/or DOB Month of the");
                        sError.append(" correponding subjects are filled:<br>").append((CharSequence)mis_dobdaymonth).append("<p>");
                    }
                    boolean protocolmode = YES.equals(request.getParameter("__mode_protocol"));
                    if (sError.length() != 0) break block71;
                    try {
                        ActionBlock ab = new ActionBlock();
                        String trackItems = "";
                        HashMap<String, String> tiMap = new HashMap<String, String>();
                        for (int i = 0; i < maxcol; ++i) {
                            String sdcid = aColSdcid[i];
                            String columnid = aColId[i];
                            if (!OpalUtil.isNotEmpty(sdcid) || columnid.startsWith("protocol_") || "Study".equals(sdcid) || aColType[i].contains("checkbox|readonly") || !aColSave[i].equalsIgnoreCase(YES) && (!sPage.equals("LV_SMSDataEntrySing") || !aColSave[i].equalsIgnoreCase("e")) || protocolmode && "LV_SampleFamily".equals(sdcid) && ("externalsubject".equals(columnid) || "subjectid".equals(columnid) || "clinicalevent".equals(columnid) || "studysiteid".equals(columnid))) continue;
                            String propertyid = "__u_" + columnid + "_" + sdcid;
                            String value = request.getParameter(propertyid);
                            if (columnid.equalsIgnoreCase("keyid1")) {
                                ab.setAction("edit" + sdcid, "EditSDI", "1");
                                ab.setActionProperty("edit" + sdcid, "sdcid", sdcid);
                                ab.setActionProperty("edit" + sdcid, "auditreason", sAuditreason);
                                ab.setActionProperty("edit" + sdcid, "auditactivity", sAuditActivity);
                                ab.setActionProperty("edit" + sdcid, "auditsignedflag", sAuditSignedFlag);
                                if (sdcid.equals("Sample") && value != null) {
                                    trackItems = this.getTrackItems(value);
                                }
                            }
                            if (value == null) continue;
                            if (sdcid.equals("TrackItemSDC")) {
                                tiMap.put(columnid, value);
                                continue;
                            }
                            ab.setActionProperty("edit" + sdcid, columnid, value);
                        }
                        if (trackItems.length() > 0) {
                            tiMap.put("sdcid", "TrackItemSDC");
                            tiMap.put("keyid1", trackItems);
                            tiMap.put("auditreason", sAuditreason);
                            tiMap.put("auditactivity", sAuditActivity);
                            tiMap.put("auditsignedflag", sAuditSignedFlag);
                            ab.setAction("EditTrackItems", "EditSDI", "1", tiMap);
                        }
                        if (StringUtil.getLen(aliasid) > 0L && StringUtil.getLen(aliastype) > 0L) {
                            this.updateAlias(s_sampleid, aliasid, aliastype, aliasact);
                        }
                        this.getActionProcessor().processActionBlock(ab);
                    }
                    catch (ActionException ae) {
                        ErrorHandler ehandler = ae.getErrorHandler();
                        sError.append("<font color=red><b>").append(this.getTranslationProcessor().translate("Failed to save data")).append("</b></font><hr>");
                        sError.append(((ErrorDetail)ehandler.get(0)).getMessage()).append("<br>");
                    }
                    catch (Throwable t) {
                        Logger.logError(t.getMessage(), t);
                    }
                    if (protocolmode) {
                        DataSet protocolds = new DataSet();
                        for (int i = 0; i < rows; ++i) {
                            int index;
                            String visit = DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_protocol_visit");
                            String timepoint = DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_protocol_timepoint");
                            String eventdefid = "";
                            String eventlabel = "";
                            if (StringUtil.getLen(timepoint) > 0L) {
                                index = timepoint.indexOf("|");
                                eventdefid = index != -1 ? timepoint.substring(0, index) : timepoint;
                                eventlabel = index != -1 ? timepoint.substring(index + 1) : "";
                            } else if (StringUtil.getLen(visit) > 0L) {
                                index = visit.indexOf("|");
                                eventdefid = index != -1 ? visit.substring(0, index) : visit;
                                eventlabel = index != -1 ? visit.substring(index + 1) : "";
                            }
                            int row = protocolds.addRow();
                            protocolds.setString(row, "s_studyid", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_s_studyid"));
                            protocolds.setString(row, "s_sampleid", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_s_sampleid"));
                            protocolds.setString(row, "s_samplefamilyid", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_s_samplefamilyid"));
                            protocolds.setString(row, "sampletypeid", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_sampletypeid"));
                            protocolds.setString(row, "studysiteid", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_studysiteid"));
                            protocolds.setString(row, "externalsubject", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_externalsubject"));
                            protocolds.setString(row, "participantid", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_protocol_participantid"));
                            protocolds.setString(row, "cpcohortid", DataEntryPageUtil.getRequestParameter(request, "pr" + i + "_protocol_cohort"));
                            protocolds.setString(row, "eventdefid", eventdefid);
                            protocolds.setString(row, "eventlabel", eventlabel);
                        }
                        sql.setLength(0);
                        sql.append("select sf.s_samplefamilyid, sf.sstudyid, sf.subjectid, sf.studysiteid, sf.subjectid, sf.externalsubject, sf.participantid,");
                        sql.append(" sf.participanteventid, p.cpcohortid, pe.eventdefid, pe.eventlabel");
                        sql.append(" from s_samplefamily sf left outer join s_participant p on p.s_participantid = sf.participantid");
                        sql.append(" left outer join s_participantevent pe on pe.s_participanteventid = sf.participanteventid");
                        ArrayList familyds = null;
                        if (protocolds.size() > 1000) {
                            String rsetid = this.getDAMProcessor().createRSet("LV_SampleFamily", protocolds.getColumnValues("s_samplefamilyid", ";"), null, null);
                            try {
                                sql.append(" where sf.s_samplefamilyid in (select r.keyid1 from rsetitems r where r.rsetid = ").append("?").append(")");
                                familyds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{rsetid});
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                if (rsetid != null && rsetid.length() > 0) {
                                    this.getDAMProcessor().clearRSet(rsetid);
                                }
                            }
                        } else {
                            SafeSQL childSafeSQL = new SafeSQL();
                            sql.append(" where sf.s_samplefamilyid in (").append(childSafeSQL.addIn(protocolds.getColumnValues("s_samplefamilyid", "','"))).append(")");
                            familyds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), childSafeSQL.getValues());
                        }
                        if (familyds != null && familyds.size() > 0) {
                            DataSet familyUpdateDataSet = new DataSet();
                            HashSet<String> familyset = new HashSet<String>();
                            HashMap<String, String> filter = new HashMap<String, String>();
                            for (int i = 0; i < protocolds.size(); ++i) {
                                Map<String, String> participantEventMap;
                                Map<String, String> participantMap;
                                String familyid = protocolds.getString(i, "s_samplefamilyid");
                                if (familyset.contains(familyid)) continue;
                                familyset.add(familyid);
                                DataSet update = new DataSet();
                                update.addRow();
                                filter.put("s_samplefamilyid", familyid);
                                int row = ((DataSet)familyds).findRow(filter);
                                if (row == -1) continue;
                                boolean updateSampleFamily = false;
                                boolean actionPropsSet = false;
                                update.setString(0, "keyid1", familyid);
                                String studysiteid = protocolds.getString(i, "studysiteid");
                                String cpcohortid = protocolds.getString(i, "cpcohortid");
                                String externalsubject = protocolds.getString(i, "externalsubject");
                                String participantsubjectid = "";
                                String participantid = "";
                                String eventdefid = protocolds.getString(i, "eventdefid");
                                String parenteventlabel = "";
                                String eventdeflabel = "";
                                String participanteventid = "";
                                if (studysiteid == null) continue;
                                if (!studysiteid.equals(((DataSet)familyds).getString(row, "studysiteid", ""))) {
                                    updateSampleFamily = true;
                                    if (OpalUtil.isNotEmpty(studysiteid)) {
                                        update.setString(0, "studysiteid", studysiteid);
                                    } else {
                                        update.setString(0, "studysiteid", "");
                                        update.setString(0, "cpcohortid", "");
                                        update.setString(0, "subjectid", "");
                                        update.setString(0, "externalsubject", "");
                                        update.setString(0, "participantid", "");
                                        update.setString(0, "participanteventid", "");
                                        update.setString(0, "clinicalevent", "");
                                        actionPropsSet = true;
                                    }
                                }
                                if (cpcohortid == null) continue;
                                if (!actionPropsSet && !cpcohortid.equals(((DataSet)familyds).getString(row, "cpcohortid", ""))) {
                                    updateSampleFamily = true;
                                    if (OpalUtil.isNotEmpty(studysiteid) && OpalUtil.isNotEmpty(cpcohortid)) {
                                        update.setString(0, "cpcohortid", cpcohortid);
                                    } else {
                                        update.setString(0, "cpcohortid", "");
                                        update.setString(0, "subjectid", "");
                                        update.setString(0, "externalsubject", "");
                                        update.setString(0, "participantid", "");
                                        update.setString(0, "participanteventid", "");
                                        update.setString(0, "clinicalevent", "");
                                        actionPropsSet = true;
                                    }
                                }
                                if (externalsubject == null) continue;
                                if (!actionPropsSet && !externalsubject.equals(((DataSet)familyds).getString(row, "externalsubject", ""))) {
                                    updateSampleFamily = true;
                                    if (OpalUtil.isNotEmpty(studysiteid) && OpalUtil.isNotEmpty(cpcohortid) && OpalUtil.isNotEmpty(externalsubject)) {
                                        participantMap = this.getParticipantID(studysiteid, cpcohortid, externalsubject);
                                        participantid = participantMap.get("participantid");
                                        participantsubjectid = participantMap.get("subjectid");
                                        update.setString(0, "subjectid", participantsubjectid);
                                        update.setString(0, "externalsubject", externalsubject);
                                        update.setString(0, "participantid", participantid);
                                        update.setString(0, "clinicalprotocolrevision", participantMap.get("clinicalprotocolrevision"));
                                        if (OpalUtil.isNotEmpty(eventdefid)) {
                                            participantEventMap = this.getParticipantEventMap(participantid, eventdefid);
                                            if (participantEventMap != null && participantEventMap.containsKey("s_participanteventid")) {
                                                participanteventid = participantEventMap.get("s_participanteventid");
                                                eventdeflabel = participantEventMap.get("eventlabel");
                                                parenteventlabel = participantEventMap.get("parenteventlabel");
                                            }
                                            update.setString(0, "participanteventid", participanteventid);
                                            update.setString(0, "clinicalevent", OpalUtil.isNotEmpty(parenteventlabel) ? parenteventlabel + " " + eventdeflabel : eventdeflabel);
                                        } else {
                                            update.setString(0, "participanteventid", "");
                                            update.setString(0, "clinicalevent", "");
                                        }
                                    } else {
                                        update.setString(0, "cpcohortid", "");
                                        update.setString(0, "subjectid", "");
                                        update.setString(0, "externalsubject", "");
                                        update.setString(0, "participantid", "");
                                        update.setString(0, "participanteventid", "");
                                        update.setString(0, "clinicalevent", "");
                                        update.setString(0, "clinicalprotocolrevision", "");
                                    }
                                } else if (!actionPropsSet && !eventdefid.equals(((DataSet)familyds).getString(row, "eventdefid", ""))) {
                                    updateSampleFamily = true;
                                    if (OpalUtil.isNotEmpty(studysiteid) && OpalUtil.isNotEmpty(cpcohortid) && OpalUtil.isNotEmpty(externalsubject) && OpalUtil.isNotEmpty(eventdefid)) {
                                        participantMap = this.getParticipantID(studysiteid, cpcohortid, externalsubject);
                                        participantid = participantMap.get("participantid");
                                        participantEventMap = this.getParticipantEventMap(participantid, eventdefid);
                                        if (participantEventMap != null && participantEventMap.containsKey("s_participanteventid")) {
                                            participanteventid = participantEventMap.get("s_participanteventid");
                                            eventdeflabel = participantEventMap.get("eventlabel");
                                            parenteventlabel = participantEventMap.get("parenteventlabel");
                                        }
                                        update.setString(0, "participanteventid", participanteventid);
                                        update.setString(0, "clinicalevent", OpalUtil.isNotEmpty(parenteventlabel) ? parenteventlabel + " " + eventdeflabel : eventdeflabel);
                                    } else {
                                        update.setString(0, "participanteventid", "");
                                        update.setString(0, "clinicalevent", "");
                                    }
                                }
                                if (!updateSampleFamily) continue;
                                familyUpdateDataSet.copyRow(update, 0, 1);
                            }
                            if (familyUpdateDataSet.size() > 0) {
                                PropertyList props = new PropertyList();
                                props.setProperty("sdcid", "LV_SampleFamily");
                                for (int i = 0; i < familyUpdateDataSet.getColumnCount(); ++i) {
                                    String columnid = familyUpdateDataSet.getColumnId(i);
                                    props.setProperty(columnid, familyUpdateDataSet.getColumnValues(columnid, ";"));
                                }
                                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                            }
                        }
                    }
                }
                catch (SapphireException se) {
                    se.printStackTrace(System.out);
                    String error = se.getMessage();
                    if (OpalUtil.isNotEmpty(error)) {
                        sError.append(this.getTranslationProcessor().translate("An unexpected error happened. Please report to your administrator."));
                        sError.append("<br><br>");
                        sError.append(error);
                    } else {
                        sError.append(this.getTranslationProcessor().translate("An unexpected error happened. Possibly due to session expiration or server restarted. Please report to your administrator."));
                    }
                }
                catch (Exception e) {
                    sError.append(this.getTranslationProcessor().translate("An unexpected error happened. Possibly due to session expiration or server restarted. Please report to your administrator.") + " [" + e.getMessage() + "]");
                }
            }
            JspWriter out = this.pageContext.getOut();
            if (sError.length() > 0) {
                out.println("<script>");
                if (sError.indexOf("::") >= 0 && sError.indexOf("|") >= 0) {
                    sError.append(sError.substring(sError.lastIndexOf("::") + 2, sError.lastIndexOf("|")));
                }
                out.println("parent.sapphire.alert('" + DataEntryPageUtil.escapeError(sError.toString()) + "', 'error');");
                out.println("try { parent.mySetPageLoadCompleted(); } catch (e) {}");
                out.println("</script>");
            } else {
                out.println("<script>parent.refreshData()</script>");
            }
        }
    }

    private static String getRequestParameter(ServletRequest request, String parameter) {
        String value = request.getParameter(parameter);
        return value == null ? "" : value;
    }

    private Map<String, String> getParticipantEventMap(String participantid, String eventdefid) throws SapphireException {
        String key = "__participantevent_" + participantid + eventdefid;
        if (!this.ddCache.containsKey(key)) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
            String addEventPolicy = policy != null ? policy.getProperty("addeventpolicy", "Always add new event") : "Always add new event";
            HashMap<String, String> map = new HashMap<String, String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select pe.s_participanteventid, pe.participantid, pe.sstudysiteid, pe.eventdefid, pe.eventlabel, pe.parentparticipanteventid, ed.allowmultipleflag,");
            sql.append("        (select ppe.eventdefid from s_participantevent ppe where ppe.s_participanteventid = pe.parentparticipanteventid) parenteventdefid,");
            sql.append("        (select ppe.eventlabel from s_participantevent ppe where ppe.s_participanteventid = pe.parentparticipanteventid) parenteventlabel");
            sql.append("  from s_participantevent pe, s_eventdef ed");
            sql.append("  where pe.participantid = ? and pe.eventdefid = ? and ed.s_eventdefid = pe.eventdefid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{participantid, eventdefid});
            if (ds != null && ds.size() > 0) {
                if (YES.equals(ds.getString(0, "allowmultipleflag"))) {
                    if ("Update existing event".equals(addEventPolicy)) {
                        map.put("s_participanteventid", ds.getString(0, "s_participanteventid"));
                        map.put("eventlabel", ds.getString(0, "eventlabel"));
                        map.put("parenteventlabel", ds.getString(0, "parenteventlabel", ""));
                    } else {
                        this.addNewParticipantEvent(participantid, eventdefid, map);
                    }
                } else {
                    if ("Always add new event".equals(addEventPolicy)) {
                        String parenteventlabel = ds.getString(0, "parenteventlabel", "");
                        String eventlabel = ds.getString(0, "eventlabel", "");
                        if (OpalUtil.isNotEmpty(parenteventlabel)) {
                            eventlabel = parenteventlabel + " / " + eventlabel;
                        }
                        throw new SapphireException(this.getTranslationProcessor().translate("Multiple Event Error"), "VALIDATION", this.getTranslationProcessor().translate("Event already exist for the Participant and does not allow multiple instances") + "<br><br>" + this.getTranslationProcessor().translate("Participant: ") + participantid + "<br>" + this.getTranslationProcessor().translate("Event: ") + eventlabel);
                    }
                    map.put("s_participanteventid", ds.getString(0, "s_participanteventid"));
                    map.put("eventlabel", ds.getString(0, "eventlabel"));
                    map.put("parenteventlabel", ds.getString(0, "parenteventlabel", ""));
                }
            } else {
                this.addNewParticipantEvent(participantid, eventdefid, map);
            }
            this.ddCache.put(key, map);
        }
        return this.ddCache.containsKey(key) ? (Map)this.ddCache.get(key) : new HashMap<String, String>();
    }

    private void addNewParticipantEvent(String participantid, String eventdefid, Map<String, String> map) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ed.s_eventdefid, ed.eventdeflabel, ed.clinicalprotocolid, ed.eventdeftype, ped.eventdeflabel parenteventdeflabel, ped.s_eventdefid parenteventdefid,");
        sql.append("        (select pe.s_participanteventid from s_participantevent pe where pe.participantid = ").append("?").append(" and pe.eventdefid = ped.s_eventdefid) parentparticipanteventid");
        sql.append("  from s_eventdef ed left outer join s_eventdef ped on ped.s_eventdefid = ed.parenteventdefid");
        sql.append("  where ed.s_eventdefid = ").append("?").append("");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{participantid, eventdefid});
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            String eventdeftype = ds.getString(0, "eventdeftype", "Visit");
            if ("Timepoint".equals(eventdeftype)) {
                String parentparticipanteventid = ds.getString(0, "parentparticipanteventid", "");
                if (StringUtil.getLen(parentparticipanteventid) == 0L) {
                    try {
                        props.setProperty("sdcid", "LV_ParticipantEvent");
                        props.setProperty("copies", "1");
                        props.setProperty("participantid", participantid);
                        props.setProperty("eventdt", "n");
                        props.setProperty("eventstatus", "Completed");
                        props.setProperty("sstudysiteid", this.getParticipantStudySite(participantid));
                        props.setProperty("eventdefid", ds.getString(0, "parenteventdefid", ""));
                        props.setProperty("eventlabel", ds.getString(0, "parenteventdeflabel", ""));
                        props.setProperty("activeflag", YES);
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        parentparticipanteventid = props.getProperty("newkeyid1");
                    }
                    catch (ActionException e) {
                        e.printStackTrace(System.out);
                    }
                }
                if (StringUtil.getLen(parentparticipanteventid) > 0L) {
                    try {
                        props.setProperty("sdcid", "LV_ParticipantEvent");
                        props.setProperty("copies", "1");
                        props.setProperty("participantid", participantid);
                        props.setProperty("eventdt", "n");
                        props.setProperty("eventstatus", "Completed");
                        props.setProperty("sstudysiteid", this.getParticipantStudySite(participantid));
                        props.setProperty("eventdefid", eventdefid);
                        props.setProperty("eventlabel", ds.getValue(0, "eventdeflabel", ""));
                        props.setProperty("parentparticipanteventid", parentparticipanteventid);
                        props.setProperty("activeflag", YES);
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        map.put("s_participanteventid", props.getProperty("newkeyid1"));
                        map.put("eventlabel", props.getProperty("eventlabel"));
                        map.put("parenteventlabel", ds.getString(0, "parenteventdeflabel", ""));
                    }
                    catch (ActionException e) {
                        e.printStackTrace(System.out);
                    }
                }
            } else {
                try {
                    props.setProperty("sdcid", "LV_ParticipantEvent");
                    props.setProperty("copies", "1");
                    props.setProperty("participantid", participantid);
                    props.setProperty("eventdt", "n");
                    props.setProperty("eventstatus", "Completed");
                    props.setProperty("sstudysiteid", this.getParticipantStudySite(participantid));
                    props.setProperty("eventdefid", eventdefid);
                    props.setProperty("eventlabel", ds.getValue(0, "eventdeflabel", ""));
                    props.setProperty("activeflag", YES);
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    map.put("s_participanteventid", props.getProperty("newkeyid1"));
                    map.put("eventlabel", props.getProperty("eventlabel"));
                    map.put("parenteventlabel", ds.getString(0, "parenteventdeflabel", ""));
                }
                catch (ActionException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    private String getParticipantStudySite(String participantid) {
        String key = "__participant_studysite_" + participantid;
        if (!this.ddCache.containsKey(key)) {
            this.ddCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "studysiteid", "s_participantid = ?", new String[]{participantid}));
        }
        return (String)this.ddCache.get(key);
    }

    private Map<String, String> getParticipantID(String studysiteid, String cpcohortid, String externalsubject) {
        HashMap<String, String> map = null;
        String key = "__participantid_" + studysiteid + externalsubject;
        if (!this.ddCache.containsKey(key)) {
            StringBuilder sql = new StringBuilder();
            sql.append("select s_participantid, externalparticipantid, subjectid, clinicalprotocolrevision from s_participant");
            sql.append(" where studysiteid = ? and externalparticipantid = ?");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{studysiteid, externalsubject});
            if (ds != null && ds.size() > 0) {
                map = new HashMap<String, String>();
                map.put("participantid", ds.getString(0, "s_participantid", ""));
                map.put("subjectid", ds.getString(0, "subjectid", ""));
                map.put("clinicalprotocolrevision", ds.getString(0, "clinicalprotocolrevision", ""));
            } else if (StringUtil.getLen(cpcohortid) > 0L) {
                sql.setLength(0);
                sql.append("select sstudyid, clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision");
                sql.append(" from s_studysite where s_studysiteid = ?");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{studysiteid});
                if (ds != null && ds.size() > 0) {
                    try {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_Subject");
                        props.setProperty("copies", "1");
                        props.setProperty("activeflag", YES);
                        props.setProperty("__sdcruleconfirm", YES);
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        String subjectid = props.getProperty("newkeyid1");
                        props.clear();
                        props.setProperty("sdcid", "LV_Participant");
                        props.setProperty("copies", "1");
                        props.setProperty("sstudyid", ds.getString(0, "sstudyid", ""));
                        props.setProperty("studysiteid", studysiteid);
                        props.setProperty("clinicalprotocolid", ds.getString(0, "clinicalprotocolid", ""));
                        props.setProperty("clinicalprotocolversionid", ds.getString(0, "clinicalprotocolversionid", ""));
                        props.setProperty("clinicalprotocolrevision", ds.getString(0, "clinicalprotocolrevision", ""));
                        props.setProperty("cpcohortid", cpcohortid);
                        props.setProperty("subjectid", subjectid);
                        props.setProperty("externalparticipantid", externalsubject);
                        props.setProperty("participantstatus", "Enrolled");
                        props.setProperty("enrolldt", "n");
                        props.setProperty("activeflag", YES);
                        props.setProperty("__sdcruleconfirm", YES);
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        map = new HashMap();
                        map.put("participantid", props.getProperty("newkeyid1"));
                        map.put("subjectid", subjectid);
                        map.put("clinicalprotocolrevision", ds.getString(0, "clinicalprotocolrevision", ""));
                    }
                    catch (ActionException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.ddCache.put(key, map);
        }
        return this.ddCache.containsKey(key) ? (Map)this.ddCache.get(key) : new HashMap<String, String>();
    }

    private void updateAlias(String keyid1, String aliasid, String aliastype, String act) throws SapphireException {
        String[] aKeyid1 = StringUtil.split(keyid1, ";");
        String[] aAliasid = StringUtil.split(aliasid, ";");
        String[] aAliasType = StringUtil.split(aliastype, ";");
        String[] aAct = StringUtil.split(act, ";");
        if (aAct.length != aKeyid1.length) {
            return;
        }
        String sdcid = "Sample";
        DataSet alias = new DataSet();
        alias.addColumn("keyid1", 0);
        alias.addColumn("aliasid", 0);
        alias.addColumn("aliastype", 0);
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < aKeyid1.length; ++i) {
            String aliasType = "";
            String tAliasid = "";
            if (aAct[i] != null && aAct[i].length() > 0) {
                aliasType = aAct[i].substring(0, aAct[i].indexOf("|"));
                tAliasid = aAct[i].substring(aAct[i].indexOf("|") + 1);
            }
            if (aliasType.equals(aAliasType[i]) && tAliasid.equals(aAliasid[i])) continue;
            try {
                if (aAliasType[i].length() > 0 && aAliasid[i].length() > 0) {
                    if (aAct[i].length() > 1) {
                        sql.setLength(0);
                        SafeSQL safeSQL = new SafeSQL();
                        sql.append("delete from sdialias where sdcid=").append(safeSQL.addVar(sdcid)).append("");
                        sql.append(" and keyid1=").append(safeSQL.addVar(aKeyid1[i])).append("");
                        sql.append(" and ( aliastype=").append(safeSQL.addVar(aAliasType[i])).append("");
                        sql.append(" or aliasid=").append(safeSQL.addVar(aAliasid[i])).append(")");
                        this.getQueryProcessor().execPreparedUpdate(sql.toString(), safeSQL.getValues());
                    }
                    int row = alias.addRow();
                    alias.setValue(row, "keyid1", aKeyid1[i]);
                    alias.setValue(row, "aliasid", aAliasid[i]);
                    alias.setValue(row, "aliastype", aAliasType[i]);
                    continue;
                }
                if (aAct[i].length() <= 0) continue;
                sql.setLength(0);
                SafeSQL safeSQL = new SafeSQL();
                sql.append("delete from sdialias where sdcid=").append(safeSQL.addVar(sdcid)).append("");
                sql.append(" and keyid1=").append(safeSQL.addVar(aKeyid1[i])).append("");
                if (aliasType.length() > 0) {
                    sql.append(" and aliastype=").append(safeSQL.addVar(aliasType)).append("");
                }
                if (tAliasid.length() > 0) {
                    sql.append(" and aliasid=").append(safeSQL.addVar(tAliasid)).append("");
                }
                this.getQueryProcessor().execPreparedUpdate(sql.toString(), safeSQL.getValues());
                continue;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to update sdialias table.", e);
            }
        }
        if (alias.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", alias.getColumnValues("keyid1", ";"));
            props.setProperty("aliasid", alias.getColumnValues("aliasid", ";"));
            props.setProperty("aliastype", alias.getColumnValues("aliastype", ";"));
            this.getActionProcessor().processAction("AddSDIAlias", "1", props);
        }
    }

    private void approveDataEntry(JspWriter out, ServletRequest request) throws IOException {
        ActionProcessor actionProcessor = new ActionProcessor(this.pageContext);
        StringBuilder sSqlApprove = new StringBuilder();
        sSqlApprove.append("select ");
        sSqlApprove.append("s_sample.s_sampleid, ");
        sSqlApprove.append("s_samplefamily.s_samplefamilyid, ");
        sSqlApprove.append("s_samplefamily.restrictclassid, ");
        sSqlApprove.append("s_sample.sampletypeid, ");
        sSqlApprove.append("s_sample.storagestatus, ");
        sSqlApprove.append("s_sample.glpflag, ");
        sSqlApprove.append("s_samplefamily.diseaseid, ");
        sSqlApprove.append("s_samplefamily.clinicaldiagid, ");
        sSqlApprove.append("s_sample.sstudyid, ");
        sSqlApprove.append("s_samplefamily.subjectid, ");
        sSqlApprove.append("s_study.collectinforequiredflag, ");
        sSqlApprove.append("s_study.verifiedby, ");
        sSqlApprove.append("s_study.subjectrequiredflag, ");
        sSqlApprove.append("s_samplefamily.tissueid, ");
        sSqlApprove.append("s_samplefamily.collectiondt, ");
        sSqlApprove.append("s_samplefamily.studysiteid, ");
        sSqlApprove.append("s_samplefamily.externalsubject, ");
        sSqlApprove.append("s_samplefamily.clinicalevent, ");
        sSqlApprove.append("s_samplefamily.conditionalapprovalflag, ");
        sSqlApprove.append("s_study.protocolname ");
        sSqlApprove.append("from s_sample,s_samplefamily,s_study ");
        sSqlApprove.append("where s_sample.sstudyid=s_study.s_studyid and s_sample.samplefamilyid=s_samplefamily.s_samplefamilyid ");
        sSqlApprove.append("and s_sample.s_sampleid in ");
        String sSqlClinicalSite = "select studysitedesc from s_studysite where sstudyid=";
        String sSqlClinicalEvent = "select s_clinicalevent.s_clinicaleventid from s_study, s_clinicalevent where s_study.s_studyid=s_clinicalevent.sstudyid and s_study.s_studyid=";
        String sSqlRestClass = "select verificationrequiredflag from s_restrictclass where s_restrictclassid=";
        String act = request.getParameter("nextact");
        if (act != null && act.length() > 0) {
            String validateCondition = "";
            String keyid1 = request.getParameter("itemid");
            String user = request.getParameter("currentuser");
            String reason = request.getParameter("message");
            String sAuditreason = request.getParameter("auditreason");
            String sAuditActivity = request.getParameter("auditactivity");
            String sAuditSignedFlag = request.getParameter("auditsignedflag");
            String mis_childsample = this.getNoneRootSamples(keyid1);
            String sError = "";
            String s_sampleid = "";
            String list_s_sampleid = "";
            String u_samplefamilyid = "";
            String list_u_samplefamilyid = "";
            String rcid = "";
            String mis_rcid = "";
            String sampletypeid = "";
            String mis_sampletypeid = "";
            String samplestatus = "";
            String mis_samplestatus = "";
            String u_glp = "";
            String lost_u_glp = "";
            String mis_verificationby = "";
            String supplierdiseaseid = "";
            String mis_supplierdiseaseid = "";
            String supplierclinical = "";
            String studyid = "";
            String mis_studyid = "";
            String subjectid = "";
            String mis_subjectid = "";
            String u_reqcollectioninfo = "";
            String u_reqpatientlink = "";
            String collectiondate = "";
            String mis_collectiondate = "";
            String clinicalsiteid = "";
            String mis_clinicalsiteid = "";
            String externalsubjectid = "";
            String mis_externalsubjectid = "";
            String clinicalevent = "";
            String mis_clinicalevent = "";
            String mis_subjextaliasname = "";
            String status = "";
            String list_status = "";
            String clinicalsite = "";
            String clinicaleventid = "";
            String conditionalapproval = "";
            String tissueid = "";
            String mis_tissueid = "";
            String verifiedBy = "";
            SafeSQL safeSQL = new SafeSQL();
            sSqlApprove.append("(").append(safeSQL.addIn(keyid1, ";")).append(")");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sSqlApprove.toString(), safeSQL.getValues());
            if (ds != null && ds.getRowCount() > 0) {
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    s_sampleid = ds.getValue(i, "s_sampleid", "");
                    u_samplefamilyid = ds.getValue(i, "s_samplefamilyid", "");
                    rcid = ds.getValue(i, "restrictclassid", "");
                    sampletypeid = ds.getValue(i, "sampletypeid", "");
                    samplestatus = ds.getValue(i, "storagestatus", "");
                    u_glp = ds.getValue(i, "glpflag", "N");
                    supplierdiseaseid = ds.getValue(i, "diseaseid", "");
                    supplierclinical = ds.getValue(i, "clinicaldiagid", "");
                    studyid = ds.getValue(i, "sstudyid", "");
                    subjectid = ds.getValue(i, "subjectid", "");
                    u_reqcollectioninfo = ds.getValue(i, "collectinforequiredflag", "N");
                    u_reqpatientlink = ds.getValue(i, "subjectrequiredflag", "N");
                    collectiondate = ds.getValue(i, "collectiondt", "");
                    clinicalsiteid = ds.getValue(i, "studysiteid", "");
                    externalsubjectid = ds.getValue(i, "externalsubject", "");
                    clinicalevent = ds.getValue(i, "clinicalevent", "");
                    conditionalapproval = ds.getValue(i, "conditionalapprovalflag", "");
                    tissueid = ds.getValue(i, "tissueid", "");
                    verifiedBy = ds.getValue(i, "verifiedby", "");
                    clinicalsite = this.getPrepSqlData(sSqlClinicalSite + "?", new Object[]{studyid});
                    clinicaleventid = this.getPrepSqlData(sSqlClinicalEvent + "?", new Object[]{studyid});
                    if (rcid.length() > 0) {
                        status = this.getPrepSqlData(sSqlRestClass + "?", new Object[]{rcid});
                    }
                    status = status.equalsIgnoreCase(YES) ? "Verification Needed" : "In Circulation";
                    if (rcid.length() < 1) {
                        mis_rcid = mis_rcid + s_sampleid + ", ";
                    }
                    if (sampletypeid.length() < 1) {
                        mis_sampletypeid = mis_sampletypeid + s_sampleid + ", ";
                    }
                    if (supplierdiseaseid.length() < 1 && supplierclinical.length() < 1) {
                        mis_supplierdiseaseid = mis_supplierdiseaseid + s_sampleid + ", ";
                    }
                    if (studyid.length() < 1) {
                        mis_studyid = mis_studyid + s_sampleid + ", ";
                    }
                    if (subjectid.length() < 1) {
                        mis_subjectid = mis_subjectid + s_sampleid + ", ";
                    }
                    if (u_reqcollectioninfo.equalsIgnoreCase(YES) && collectiondate.length() < 1) {
                        mis_collectiondate = mis_collectiondate + s_sampleid + ", ";
                    }
                    if (YES.equals(u_glp)) {
                        lost_u_glp = lost_u_glp + s_sampleid + "|";
                    }
                    if (act.equalsIgnoreCase("fullapprove")) {
                        if (clinicalsite.length() > 0 && clinicalsiteid.length() < 1 && !mis_clinicalsiteid.contains(s_sampleid)) {
                            mis_clinicalsiteid = mis_clinicalsiteid + s_sampleid + ", ";
                        }
                        if (u_reqpatientlink.equalsIgnoreCase(YES) && externalsubjectid.length() < 1 && !mis_externalsubjectid.contains(s_sampleid)) {
                            mis_externalsubjectid = mis_externalsubjectid + s_sampleid + ", ";
                        }
                        if (clinicaleventid.length() > 0 && clinicalevent.length() < 1 && !mis_clinicalevent.contains(s_sampleid)) {
                            mis_clinicalevent = mis_clinicalevent + s_sampleid + ", ";
                        }
                        if (!mis_samplestatus.contains(s_sampleid)) {
                            if (samplestatus.equalsIgnoreCase("In Circulation") && !conditionalapproval.equalsIgnoreCase(YES)) {
                                mis_samplestatus = mis_samplestatus + "<li>" + s_sampleid + ": Sample has already been fully approved</li>";
                            } else if (!samplestatus.equalsIgnoreCase("Received") && !samplestatus.equalsIgnoreCase("Temporary In Lab")) {
                                mis_samplestatus = mis_samplestatus + "<li>" + s_sampleid + ": Sample's status is neither " + "Received" + " nor " + "Temporary In Lab" + "</li>";
                            }
                        }
                    }
                    if (act.equalsIgnoreCase("condapprovecont") || act.equalsIgnoreCase("fullapprove") || act.contains("approve")) {
                        if (!(samplestatus.equalsIgnoreCase("Received") || samplestatus.equalsIgnoreCase("In Circulation") || samplestatus.equalsIgnoreCase("Temporary In Lab") || mis_samplestatus.contains(s_sampleid))) {
                            mis_samplestatus = mis_samplestatus + s_sampleid + ", ";
                        }
                        if (tissueid.trim().length() == 0) {
                            mis_tissueid = mis_tissueid + s_sampleid + ", ";
                        }
                    }
                    if (act.equalsIgnoreCase("verification")) {
                        if (!samplestatus.equalsIgnoreCase("Verification Needed") && !mis_samplestatus.contains(s_sampleid)) {
                            mis_samplestatus = mis_samplestatus + s_sampleid + ", ";
                        }
                        if (user != null && !user.equals(verifiedBy)) {
                            mis_verificationby = mis_verificationby + verifiedBy + ",";
                        }
                    }
                    if (act.equalsIgnoreCase("requestverification") && !samplestatus.equalsIgnoreCase("In Circulation") && !mis_samplestatus.contains(s_sampleid)) {
                        mis_samplestatus = mis_samplestatus + s_sampleid + ", ";
                    }
                    if (i > 0) {
                        list_s_sampleid = list_s_sampleid + ";";
                        list_u_samplefamilyid = list_u_samplefamilyid + ";";
                        list_status = list_status + ";";
                    }
                    list_s_sampleid = list_s_sampleid + s_sampleid;
                    list_u_samplefamilyid = list_u_samplefamilyid + u_samplefamilyid;
                    list_status = list_status + status;
                }
                if (mis_childsample.length() > 0) {
                    sError = sError + "The following sample(s) are not root sample:<br>" + mis_childsample + "<p>";
                }
                if (YES.equals(MISSRESTCLASS) && mis_rcid.length() > 0) {
                    sError = sError + "The following sample(s) missing Restriction Class:<br>" + mis_rcid + "<p>";
                }
                if (YES.equals(MISSSAMPLETYPE) && mis_sampletypeid.length() > 0) {
                    sError = sError + "The following sample(s) missing Sample type:<br>" + mis_sampletypeid + "<p>";
                }
                if (YES.equals(MISSSUPPCLINDIAGNOSIS) && mis_supplierdiseaseid.length() > 0) {
                    sError = sError + "The following sample(s) missing both Supplier Diagnosis and Clinical Diagnosis:<br>" + mis_supplierdiseaseid + "<p>";
                }
                if (mis_studyid.length() > 0) {
                    sError = sError + "The following sample(s) missing Study Id:<br>" + mis_studyid + "<p>";
                }
                if (mis_subjectid.length() > 0) {
                    sError = sError + "The following sample(s) missing Subject Id:<br>" + mis_subjectid + "<p>";
                }
                if (YES.equals(MISSCOLLDATE) && mis_collectiondate.length() > 0) {
                    sError = sError + "The following sample(s) missing Collection Date:<br>" + mis_collectiondate + "<p>";
                }
                if (YES.equals(SUBEXTALIASMISSMATCH) && mis_subjextaliasname.length() > 0) {
                    sError = sError + "The following sample(s) Subject External Alias Name mis-matching :<br>" + mis_subjextaliasname + "<p>";
                }
                if (YES.equals(MISSTISSUEID) && mis_tissueid.length() > 0) {
                    sError = sError + "The following sample(s) missing tissueid:<br>" + mis_tissueid + "<p>";
                }
                if (sError.length() > 0) {
                    validateCondition = "cond";
                }
                if (act.equalsIgnoreCase("fullapprove")) {
                    if (YES.equals(MISSCLINICALSITE) && mis_clinicalsiteid.length() > 0) {
                        sError = sError + "The following sample(s) missing Clinical Site:<br>" + mis_clinicalsiteid + "<p>";
                    }
                    if (YES.equals(MISSSUBJECTALIAS) && mis_externalsubjectid.length() > 0) {
                        sError = sError + "The following sample(s) missing Primary Subject Alias:<br>" + mis_externalsubjectid + "<p>";
                    }
                    if (YES.equals(MISSCLINICALEVENT) && mis_clinicalevent.length() > 0) {
                        sError = sError + "The following sample(s) missing Clinical Event:<br>" + mis_clinicalevent + "<p>";
                    }
                }
                if (sError.length() > 0) {
                    validateCondition = "full";
                }
            } else {
                sError = "<br>There is no valid data. The following sample(s) missing Study Id and/or Sample Family :<br><br> " + keyid1 + "<P>";
            }
            if (act.equalsIgnoreCase("verification")) {
                if (validateCondition.equals("cond")) {
                    sError = "";
                }
                if (mis_samplestatus.length() > 0) {
                    sError = sError + "The Sample Status of following sample(s) is not Verification Needed:<br>" + mis_samplestatus + "<p>";
                }
                if (mis_verificationby.length() > 0) {
                    mis_verificationby = mis_verificationby.substring(0, mis_verificationby.length() - 1);
                    sError = sError + "The selected sample(s) were assigned to other user(s):" + mis_verificationby + "<br/> or no Verification By is specified for the study.<p>";
                }
            }
            if (act.equalsIgnoreCase("condapprovecont") || act.equalsIgnoreCase("fullapprove") || act.contains("approve")) {
                if (mis_samplestatus.length() > 0) {
                    sError = sError + "One or more of the sample(s) cannot be approved.<br><ul>" + mis_samplestatus + "</ul>";
                }
            } else if (act.equalsIgnoreCase("requestverification")) {
                sError = "";
                if (mis_samplestatus.length() > 0) {
                    sError = sError + "The Sample Status of following sample(s) is not In Circulation:<br>" + mis_samplestatus + "<p>";
                }
            }
            if (sError.length() < 1 && keyid1 != null && keyid1.length() > 0) {
                if (act.equalsIgnoreCase("condapprovecont") || act.equalsIgnoreCase("fullapprove")) {
                    try {
                        String tempInLabChilds;
                        ActionBlock ab = new ActionBlock();
                        ab.setAction("editSample", "EditSDI", "1");
                        ab.setActionProperty("editSample", "sdcid", "Sample");
                        ab.setActionProperty("editSample", "keyid1", list_s_sampleid);
                        ab.setActionProperty("editSample", "storagestatus", list_status);
                        ab.setActionProperty("editSample", "auditreason", sAuditreason);
                        ab.setActionProperty("editSample", "auditactivity", sAuditActivity);
                        ab.setActionProperty("editSample", "auditsignedflag", sAuditSignedFlag);
                        ArrayList<String> child = new ArrayList<String>();
                        DataEntryPageUtil.populateTempInLabChildList(this.getQueryProcessor(), list_s_sampleid, child);
                        if (child.size() > 0 && StringUtil.getLen(tempInLabChilds = OpalUtil.toDelimitedString(child, ";")) > 0L) {
                            ab.setAction("editChildSample", "EditSDI", "1");
                            ab.setActionProperty("editChildSample", "sdcid", "Sample");
                            ab.setActionProperty("editChildSample", "keyid1", tempInLabChilds);
                            ab.setActionProperty("editChildSample", "storagestatus", "In Circulation");
                        }
                        ab.setAction("editSampleFamily", "EditSDI", "1");
                        ab.setActionProperty("editSampleFamily", "sdcid", "LV_SampleFamily");
                        ab.setActionProperty("editSampleFamily", "keyid1", list_u_samplefamilyid);
                        ab.setActionProperty("editSampleFamily", "approvedby", user);
                        ab.setActionProperty("editSampleFamily", "approveddt", "n");
                        if (act.equalsIgnoreCase("condapprovecont")) {
                            ab.setActionProperty("editSampleFamily", "conditionalapprovalreason", reason);
                            ab.setActionProperty("editSampleFamily", "conditionalapprovalflag", YES);
                        } else if (act.equalsIgnoreCase("fullapprove")) {
                            ab.setActionProperty("editSampleFamily", "conditionalapprovalreason", "(null)");
                            ab.setActionProperty("editSampleFamily", "conditionalapprovalflag", "N");
                        }
                        actionProcessor.processActionBlock(ab);
                    }
                    catch (ActionException ae) {
                        ErrorHandler ehandler = ae.getErrorHandler();
                        sError = "Failed to approve samples.<br>" + ((ErrorDetail)ehandler.get(0)).getMessage() + "<br>";
                    }
                } else if (act.equalsIgnoreCase("verification")) {
                    try {
                        ActionBlock ab = new ActionBlock();
                        ab.setAction("editSample", "EditSDI", "1");
                        ab.setActionProperty("editSample", "sdcid", "Sample");
                        ab.setActionProperty("editSample", "keyid1", list_s_sampleid);
                        ab.setActionProperty("editSample", "storagestatus", "In Circulation");
                        ab.setAction("editSampleFamily", "EditSDI", "1");
                        ab.setActionProperty("editSampleFamily", "sdcid", "LV_SampleFamily");
                        ab.setActionProperty("editSampleFamily", "keyid1", list_u_samplefamilyid);
                        ab.setActionProperty("editSampleFamily", "verifiedby", user);
                        ab.setActionProperty("editSampleFamily", "verifieddt", "n");
                        if (validateCondition.equals("full")) {
                            ab.setActionProperty("editSampleFamily", "conditionalapprovalreason", "(null)");
                            ab.setActionProperty("editSampleFamily", "conditionalapprovalflag", "N");
                        }
                        ab.setActionProperty("editSample", "auditreason", sAuditreason);
                        ab.setActionProperty("editSample", "auditactivity", sAuditActivity);
                        ab.setActionProperty("editSample", "auditsignedflag", sAuditSignedFlag);
                        actionProcessor.processActionBlock(ab);
                    }
                    catch (ActionException ae) {
                        ErrorHandler ehandler = ae.getErrorHandler();
                        sError = "Failed to complete verification.<br>" + ((ErrorDetail)ehandler.get(0)).getMessage() + "<br>";
                    }
                } else if (act.equalsIgnoreCase("requestverification")) {
                    try {
                        ActionBlock ab = new ActionBlock();
                        ab.setAction("editSample", "EditSDI", "1");
                        ab.setActionProperty("editSample", "sdcid", "Sample");
                        ab.setActionProperty("editSample", "keyid1", list_s_sampleid);
                        ab.setActionProperty("editSample", "storagestatus", "Verification Needed");
                        ab.setActionProperty("editSample", "auditreason", sAuditreason);
                        ab.setActionProperty("editSample", "auditactivity", sAuditActivity);
                        ab.setActionProperty("editSample", "auditsignedflag", sAuditSignedFlag);
                        actionProcessor.processActionBlock(ab);
                    }
                    catch (ActionException ae) {
                        ErrorHandler ehandler = ae.getErrorHandler();
                        sError = "Failed to request verification.<br>" + ((ErrorDetail)ehandler.get(0)).getMessage() + "<br>";
                    }
                }
            }
            out.println("<script language='javascript'>");
            if (sError.length() > 0) {
                if (sError.contains("::") && sError.contains("|")) {
                    sError = sError.substring(sError.lastIndexOf("::") + 2, sError.lastIndexOf("|"));
                }
                out.println("parent.sapphire.alert('" + DataEntryPageUtil.escapeError(sError) + "', 'error');");
                out.println("try { parent.mySetPageLoadCompleted(); } catch (e) {}");
            } else if (!act.equalsIgnoreCase("condapprove")) {
                out.println("sapphire.page.getTop().refreshData();");
            } else {
                out.println("sapphire.page.getTop().condApprove('" + lost_u_glp + "');");
            }
            out.println("</script>");
        }
    }

    private String getNoneRootSamples(String sampleids) {
        SafeSQL safeSQL = new SafeSQL();
        String sSqlnotRootSample = "select distinct destsampleid from s_samplemap where destsampleid in (" + safeSQL.addIn(sampleids, ";") + ")";
        return this.getQueryProcessor().getPreparedSqlDataSet(sSqlnotRootSample, safeSQL.getValues()).getColumnValues("destsampleid", ";");
    }

    public static void populateTempInLabChildList(QueryProcessor queryProcessor, String sampleid, List<String> child) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select s_samplemap.destsampleid");
        sql.append(" from s_samplemap");
        sql.append(" where s_samplemap.sourcesampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
        sql.append(" and (select s_sample.storagestatus from s_sample where s_sample.s_sampleid = s_samplemap.sourcesampleid ) = 'Temporary In Lab'");
        sql.append(" and (select s_sample.storagestatus from s_sample where s_sample.s_sampleid = s_samplemap.destsampleid ) = 'Temporary In Lab'");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String childid = ds.getValue(i, "destsampleid");
                child.add(childid);
                DataEntryPageUtil.populateTempInLabChildList(queryProcessor, childid, child);
            }
        }
    }

    private static String escapeError(String error) {
        return error.replaceAll("::", "<BR>").replaceAll("'", "\\\\'").replaceAll("\n", "//");
    }

    private String getTrackItems(String samples) throws SapphireException {
        String[] aSamples = StringUtil.split(samples, ";");
        ArrayList tiDs = null;
        if (aSamples.length > 750) {
            String rsetid = this.getDAMProcessor().createRSet("Sample", samples, null, null);
            if (StringUtil.getLen(rsetid) > 0L) {
                tiDs = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid, linkkeyid1 from trackitem where linksdcid='Sample' and linkkeyid1 in ( select r.keyid1 from rsetitems r where r.rsetid = ? )", new Object[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            }
        } else {
            SafeSQL safeSQL = new SafeSQL();
            String s = "select trackitemid, linkkeyid1 from trackitem where linksdcid='Sample' and linkkeyid1 in (" + safeSQL.addIn(samples, ";") + ")";
            tiDs = this.getQueryProcessor().getPreparedSqlDataSet(s, safeSQL.getValues());
        }
        if (tiDs != null && aSamples.length == tiDs.size()) {
            HashMap<String, String> map = new HashMap<String, String>();
            ArrayList<String> list = new ArrayList<String>();
            String[] s = StringUtil.split(samples, ";");
            int slength = s.length;
            for (int i = 0; i < slength; ++i) {
                map.put("linkkeyid1", s[i]);
                int row = ((DataSet)tiDs).findRow(map);
                if (row == -1) continue;
                list.add(((DataSet)tiDs).getString(row, "trackitemid"));
            }
            return OpalUtil.toDelimitedString(list, ";");
        }
        this.logger.error("DataEntryPageUtil Error: TrackItem not found for the all Samples");
        return "";
    }
}

