/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.actions.documents.InvokeInstrumentCertProc;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetCertInstrumentsForArray
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String certflag = ajaxResponse.getRequestParameter("certflag");
        String instrumentType = "";
        String error = "";
        String instrumentid = "";
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT am.arraymethodid,aami.instrumenttypeid FROM arraymethod am, arrayarraymethoditem aami WHERE aami.arrayid IN (" + safeSQL.addIn(ajaxResponse.getRequestParameter("arrayid").replaceAll(";", "', '")) + ") AND aami.arraymethodid = am.arraymethodid AND aami.arraymethodversionid = am.arraymethodversionid AND aami.usersequence = (SELECT MAX(usersequence) FROM arrayarraymethoditem  WHERE arrayarraymethoditem.arrayid = aami.arrayid AND arrayarraymethoditem.arraymethoditemstatus  <> 'Cancelled') ", safeSQL.getValues());
        if (ds.size() == 0) {
            ajaxResponse.setError(this.getTranslationProcessor().translate("Please Apply Array Method first"));
        } else {
            String amId = ds.getColumnValues("arraymethodid", ";");
            String instrumentTypeId = ds.getColumnValues("instrumenttypeid", ";");
            String[] amIdArr = StringUtil.split(amId, ";");
            String[] instrumentTypeIdArr = StringUtil.split(instrumentTypeId, ";");
            for (int i = 0; i < amIdArr.length; ++i) {
                if (instrumentTypeIdArr[i].length() == 0 || instrumentTypeIdArr[i] == null) {
                    error = this.getTranslationProcessor().translate("Instrument Type is not defined for the selected array " + instrumentTypeIdArr[i]);
                    ajaxResponse.setError(error);
                    break;
                }
                if (instrumentType.length() == 0) {
                    instrumentType = instrumentTypeIdArr[i];
                    continue;
                }
                if (instrumentType.equals(instrumentTypeIdArr[i])) continue;
                error = this.getTranslationProcessor().translate("Please select Arrays of same Instrument Type");
                ajaxResponse.setError(error);
                break;
            }
            if (error.length() == 0) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("instrumenttypeid", instrumentType);
                actionProps.setProperty("certflag", certflag);
                actionProps.setProperty("connectionId", this.getConnectionId());
                actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                try {
                    this.getActionProcessor().processActionClass(InvokeInstrumentCertProc.class.getName(), actionProps);
                }
                catch (ActionException e) {
                    throw new ServletException((Throwable)e);
                }
                String rsetid = actionProps.getProperty("rsetResult");
                DataSet rsetDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid=?", new Object[]{rsetid});
                instrumentid = rsetDS.getColumnValues("keyid1", ";");
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
        ajaxResponse.addCallbackArgument("keyid1", instrumentid);
        ajaxResponse.print();
    }
}

