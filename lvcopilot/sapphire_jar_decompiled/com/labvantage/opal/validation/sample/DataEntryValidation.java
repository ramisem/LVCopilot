/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.validation.BaseAjaxValidation;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class DataEntryValidation
extends BaseAjaxValidation {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 53419 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("keyid1");
        String operation = ajaxResponse.getRequestParameter("operation");
        String message = "";
        TranslationProcessor tp = this.getTranslationProcessor();
        ErrorHandler errorHandler = new ErrorHandler();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select s_sampleid from s_sample where samplefamilyid is null and s_sampleid in ( ").append(safeSQL.addIn(sampleid, ";")).append(" )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            errorHandler.add(tp.translate("Sample Accessioning not allowed"), "", "Validation failed", "VALIDATION", tp.translate("One or more of the selected samples are missing Sample Family. These samples may belong to a Protocol Driven Study and not yet received."));
        } else {
            DataSet sampleInfo = DataEntryValidation.getSampleDataSet(this.getQueryProcessor(), sampleid);
            String disposedSample = DataEntryValidation.getSample_StorageStatus(sampleInfo, "Disposed");
            if (disposedSample.length() > 0) {
                errorHandler.add("DataEntry Validation", "", "Validation falied", "VALIDATION", tp.translate("Following samples are disposed:") + "<br>" + disposedSample);
            }
            if (operation.equals("VerificationNeeded")) {
                String noStudy;
                String notStatus = DataEntryValidation.getSample_NotStorageStatus(sampleInfo, "Verification Needed");
                if (notStatus.length() > 0) {
                    errorHandler.add("DataEntry Validation", "", "Validation failed", "VALIDATION", tp.translate("Following Samples are not in Verification Needed status.") + "<br>" + notStatus);
                }
                if ((noStudy = DataEntryValidation.nonStudySamples(sampleInfo)).length() > 0) {
                    errorHandler.add("DataEntry Validation", "", "Validation failed", "VALIDATION", tp.translate("Following Samples does not belong to study.") + "<br>" + noStudy);
                }
            }
        }
        if (errorHandler.hasErrors() || errorHandler.size() > 0) {
            message = ErrorUtil.formatErrorMessage(errorHandler);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private static String nonStudySamples(DataSet ds) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (ds.getValue(i, "sstudyid").length() >= 0) continue;
            sb.append(";").append(ds.getValue(i, "s_sampleid"));
        }
        return sb.length() > 0 ? sb.substring(1) : "";
    }

    private static String getSample_NotStorageStatus(DataSet ds, String storageStatus) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (ds.getValue(i, "storagestatus").equals(storageStatus)) continue;
            sb.append(";").append(ds.getValue(i, "s_sampleid"));
        }
        return sb.length() > 0 ? sb.substring(1) : "";
    }

    private static DataSet getSampleDataSet(QueryProcessor qp, String sampleIds) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("select s.s_sampleid, s.storagestatus, t.custodialdepartmentid, s.confirmeddt, s.confirmedby,");
        sql.append(" s.sstudyid, t.currentstorageunitid, s.samplefamilyid");
        sql.append(" from s_sample s, trackitem t");
        sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleIds, ";")).append(")");
        sql.append(" and t.linksdcid = 'Sample'");
        sql.append(" and t.linkkeyid1 = s.s_sampleid ");
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private static String getSample_StorageStatus(DataSet ds, String storageStatus) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            if (!ds.getValue(i, "storagestatus").equals(storageStatus)) continue;
            sb.append(";").append(ds.getValue(i, "s_sampleid"));
        }
        return sb.length() > 0 ? sb.substring(1) : "";
    }
}

