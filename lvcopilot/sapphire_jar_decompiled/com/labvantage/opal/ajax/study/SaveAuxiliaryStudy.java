/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.study;

import com.labvantage.opal.util.FormUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDIDetail;
import com.labvantage.sapphire.actions.sdi.AddSDISecuritySet;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SaveAuxiliaryStudy
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studyid = ajaxResponse.getRequestParameter("studyid", "").trim();
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        String widata = ajaxResponse.getRequestParameter("widata", "").trim();
        String auditreason = ajaxResponse.getRequestParameter("auditreason", "").trim();
        String message = "";
        if (studyid.length() > 0) {
            int i;
            ArrayList addSecuritySetDS = null;
            String[] samples = StringUtil.split(sampleid, ";");
            PropertyList props = new PropertyList();
            String studySecuritySet = null;
            boolean isPrimaryStudy = false;
            boolean isAuxiliaryStudy = false;
            boolean isInactiveStudy = false;
            SafeSQL safeSQL = new SafeSQL();
            DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, sstudyid from s_sample where s_sampleid in ( " + safeSQL.addIn(sampleid, ";") + " )", safeSQL.getValues());
            if (_ds != null && _ds.size() > 0) {
                for (i = 0; i < _ds.size(); ++i) {
                    if (!_ds.getString(i, "sstudyid").equals(studyid)) continue;
                    isPrimaryStudy = true;
                    break;
                }
            }
            safeSQL.reset();
            _ds = this.getQueryProcessor().getPreparedSqlDataSet("select ss.s_sampleid, ss.s_studyid from s_samples_study ss where ss.s_sampleid in ( " + safeSQL.addIn(sampleid, ";") + " )", safeSQL.getValues());
            if (_ds != null && _ds.size() > 0) {
                for (i = 0; i < _ds.size(); ++i) {
                    if (!_ds.getString(i, "s_studyid").equals(studyid)) continue;
                    isAuxiliaryStudy = true;
                    break;
                }
            }
            if (!"Active".equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "studystatus", "s_studyid = ?", new String[]{studyid}))) {
                isInactiveStudy = true;
            }
            if (isPrimaryStudy || isAuxiliaryStudy || isInactiveStudy) {
                message = "<div style='color:red;font-weight:bold;'>" + this.getTranslationProcessor().translate("ERROR") + "</div><hr>";
                if (isInactiveStudy) {
                    message = message + "&#8226 " + this.getTranslationProcessor().translate("Only Active Study can be added as Auxiliary Study");
                }
                if (isPrimaryStudy) {
                    message = message + "&#8226 " + this.getTranslationProcessor().translate("Primary study can not be added to Sample as an Auxiliary Study");
                }
                if (isAuxiliaryStudy) {
                    message = message + "<br>&#8226 " + this.getTranslationProcessor().translate("Study is already an Auxiliary Study on Sample");
                }
            }
            if (OpalUtil.isEmpty(message)) {
                for (String sample : samples) {
                    try {
                        props.clear();
                        props.setProperty("sdcid", "Sample");
                        props.setProperty("keyid1", sample);
                        props.setProperty("linkid", "Auxiliary Study");
                        props.setProperty("auditreason", auditreason);
                        props.setProperty("s_studyid", studyid);
                        this.getActionProcessor().processActionClass(AddSDIDetail.class.getName(), props);
                        this.addBlankDocumentsFromAuxiliaryStudy(sample, studyid);
                        if (!"S".equals(this.getSDCProcessor().getProperty("Study", "accesscontrolledflag")) || !"S".equals(this.getSDCProcessor().getProperty("Sample", "accesscontrolledflag"))) continue;
                        if (studySecuritySet == null) {
                            studySecuritySet = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "securityset", "s_studyid = ?", new String[]{studyid});
                        }
                        if (!OpalUtil.isNotEmpty(studySecuritySet)) continue;
                        if (addSecuritySetDS == null) {
                            addSecuritySetDS = new DataSet();
                        }
                        int row = ((DataSet)addSecuritySetDS).addRow();
                        ((DataSet)addSecuritySetDS).setString(row, "keyid1", sample);
                        ((DataSet)addSecuritySetDS).setString(row, "securityset", studySecuritySet);
                    }
                    catch (SapphireException e) {
                        message = this.getTranslationProcessor().translate("Error adding Study as Auxiliary Study. Please see labvantage error log for more details.");
                        this.logger.error(message);
                    }
                }
                if (addSecuritySetDS != null && addSecuritySetDS.size() > 0) {
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", ((DataSet)addSecuritySetDS).getColumnValues("keyid1", ";"));
                    props.setProperty("securityset", ((DataSet)addSecuritySetDS).getColumnValues("securityset", ";"));
                    props.setProperty("propsmatch", "Y");
                    try {
                        this.getActionProcessor().processActionClass(AddSDISecuritySet.class.getName(), props);
                    }
                    catch (SapphireException e) {
                        message = this.getTranslationProcessor().translate("Error adding Security Set to Sample. Please see labvantage error log for more details.");
                        this.logger.error(message);
                    }
                }
                if (widata.length() > 0) {
                    String[] workitems;
                    StringBuilder sql = new StringBuilder();
                    DataSet addwids = new DataSet();
                    DataSet linkwids = new DataSet();
                    for (String w : workitems = StringUtil.split(widata, ";")) {
                        int row;
                        String[] s = StringUtil.split(w, "|");
                        String sample = s[0];
                        String workitemid = s[1];
                        String workitemversionid = s[2];
                        String operation = s[3];
                        if ("Add".equals(operation) || "Repeat".equals(operation)) {
                            row = addwids.addRow();
                            addwids.setString(row, "keyid1", sample);
                            addwids.setString(row, "workitemid", workitemid);
                            addwids.setString(row, "workitemversionid", workitemversionid);
                            addwids.setString(row, "sourcesstudyid", studyid);
                            sql.setLength(0);
                            sql.append("select applyonaddflag, s_assigneddepartment");
                            sql.append(" from sdiworkitem");
                            sql.append(" where sdcid = 'Study'");
                            sql.append(" and keyid1 = ?");
                            sql.append(" and workitemid = ?");
                            sql.append(" and workitemversionid = ?");
                            sql.append(" and workiteminstance = '1'");
                            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{studyid, workitemid, workitemversionid});
                            if (ds != null && ds.size() > 0) {
                                String applyonaddflag = ds.getString(0, "applyonaddflag", "N");
                                addwids.setString(row, "applyworkitem", applyonaddflag);
                                addwids.setString(row, "s_assigneddepartment", ds.getString(0, "s_assigneddepartment", ""));
                                continue;
                            }
                            addwids.setString(row, "applyworkitem", "Y");
                            addwids.setString(row, "s_assigneddepartment", "");
                            continue;
                        }
                        if (!"Link".equals(operation)) continue;
                        row = linkwids.addRow();
                        linkwids.setString(row, "keyid1", sample);
                        linkwids.setString(row, "workitemid", workitemid);
                        linkwids.setString(row, "workitemversionid", workitemversionid);
                        linkwids.setString(row, "sourcesstudyid", studyid);
                    }
                    try {
                        if (addwids.size() > 0) {
                            props.clear();
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", addwids.getColumnValues("keyid1", ";"));
                            props.setProperty("workitemid", addwids.getColumnValues("workitemid", ";"));
                            props.setProperty("workitemversionid", addwids.getColumnValues("workitemversionid", ";"));
                            props.setProperty("forcenew", "Y");
                            props.setProperty("auditreason", auditreason);
                            props.setProperty("propsmatch", "Y");
                            props.setProperty("applyworkitem", addwids.getColumnValues("applyworkitem", ";"));
                            props.setProperty("s_assigneddepartment", addwids.getColumnValues("s_assigneddepartment", ";"));
                            props.setProperty("sourcesstudyid", addwids.getColumnValues("sourcesstudyid", ";"));
                            this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                        }
                        if (linkwids.size() > 0) {
                            props.clear();
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", linkwids.getColumnValues("keyid1", ";"));
                            props.setProperty("workitemid", linkwids.getColumnValues("workitemid", ";"));
                            props.setProperty("workitemversionid", linkwids.getColumnValues("workitemversionid", ";"));
                            props.setProperty("forcenew", "N");
                            props.setProperty("auditreason", auditreason);
                            props.setProperty("propsmatch", "Y");
                            props.setProperty("sourcesstudyid", linkwids.getColumnValues("sourcesstudyid", ";"));
                            this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                        }
                    }
                    catch (SapphireException e) {
                        message = this.getTranslationProcessor().translate("Error adding Tests to Samples. Please see labvantage error log for more details.");
                        this.logger.error(message);
                    }
                }
            }
        } else {
            message = this.getTranslationProcessor().translate("Missing Study to add as Auxiliary Study");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.addCallbackArgument("studyid", studyid);
        ajaxResponse.print();
    }

    public void addBlankDocumentsFromAuxiliaryStudy(String sampleid, String studyid) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        sql.append("select sfr.formid, sfr.forminstance, sfr.formversionid, sfr.formrule,");
        sql.append(" ( select max(form.formversionid) from form where form.formid = sfr.formid ) defaultformversionid");
        sql.append(" from sdiformrule sfr");
        sql.append(" where sfr.sdcid = 'Study'");
        sql.append(" and sfr.keyid1 = ?");
        sql.append(" and sfr.formrule = 'Sample: Allocation'");
        DataSet formds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), new Object[]{studyid});
        DataSet formsDataSet = new DataSet();
        if (formds != null && formds.size() > 0) {
            String samplefamilyid = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sample", "samplefamilyid", "s_sampleid = ?", new String[]{sampleid});
            for (int i = 0; i < formds.size(); ++i) {
                formds.setString(i, "s_samplefamilyid", samplefamilyid);
                formsDataSet.copyRow(formds, i, 1);
            }
        }
        if (formsDataSet.size() > 0) {
            try {
                formsDataSet.setString(-1, "sstudyid", studyid);
                FormUtil.addBlankDocument(formsDataSet, this.getActionProcessor(), "LV_SampleFamily", "s_samplefamilyid");
            }
            catch (ActionException e) {
                this.logger.error("Unable to add Blank Document to Sample " + formsDataSet.getColumnValues("s_sampleid", ";") + ". Exception raised is: " + e.getMessage(), e);
            }
        }
    }
}

