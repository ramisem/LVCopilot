/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import com.labvantage.sapphire.actions.storage.EditTrackItem;
import com.labvantage.sapphire.admin.ddt.PhysicalStore;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckOutTrackItem
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 73779 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        String auditreason = ajaxResponse.getRequestParameter("auditreason", "");
        String auditactivity = ajaxResponse.getRequestParameter("auditactivity", "");
        String auditsignedflag = ajaxResponse.getRequestParameter("auditsignedflag", "N");
        String reservelocation = ajaxResponse.getRequestParameter("reservelocation", "N");
        String __sdcruleconfirm = ajaxResponse.getRequestParameter("__sdcruleconfirm", "N");
        String errormsg = "";
        String confirmmsg = "";
        if (StringUtil.getLen(trackitemid) > 0L) {
            trackitemid = StringUtil.replaceAll(trackitemid, "%3B", ";");
            try {
                this.validateCheckOut(trackitemid);
                PropertyList props = new PropertyList();
                props.setProperty("trackitemid", trackitemid);
                props.setProperty("custodialuserid", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId());
                props.setProperty("custodytakendt", "n");
                props.setProperty("currentstorageunitid", "");
                props.setProperty("reservelocation", reservelocation);
                try {
                    props.setProperty("auditreason", URLDecoder.decode(auditreason, "UTF-8"));
                }
                catch (UnsupportedEncodingException e) {
                    props.setProperty("auditreason", auditreason);
                }
                props.setProperty("auditactivity", auditactivity);
                props.setProperty("auditsignedflag", auditsignedflag);
                props.setProperty("__sdcruleconfirm", __sdcruleconfirm);
                this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
            }
            catch (ActionException e) {
                ErrorHandler errorHandler = e.getErrorHandler();
                if (errorHandler != null) {
                    for (int i = 0; i < errorHandler.size(); ++i) {
                        ErrorDetail ed = (ErrorDetail)errorHandler.get(i);
                        if (!ed.getErrorType().equals("CONFIRM")) continue;
                        confirmmsg = StringUtil.replaceAll(ed.getMessage(), "|", "");
                    }
                }
                errormsg = e.getMessage();
            }
            catch (SapphireException e) {
                errormsg = e.getMessage();
            }
        }
        ajaxResponse.addCallbackArgument("msg", errormsg);
        ajaxResponse.addCallbackArgument("trackitemid", trackitemid);
        ajaxResponse.addCallbackArgument("confirmmsg", confirmmsg);
        ajaxResponse.print();
    }

    private void validateCheckOut(String trackItemIds) throws SapphireException {
        QueryProcessor qp = this.getQueryProcessor();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        sql.append("SELECT currentstorageunitid FROM trackitem ");
        sql.append(" WHERE trackitemid IN (").append(safeSQL.addIn(trackItemIds, ";")).append(")");
        sql.append(" AND linksdcid NOT IN ('Plate')");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String currentStorageUnitId = ds.getValue(i, "currentstorageunitid", "");
                String storageNode = "";
                if (currentStorageUnitId.length() > 0) {
                    storageNode = StorageUnitSDC.getStorageNodeBySDC(qp, currentStorageUnitId, "PhysicalStore");
                    if (storageNode.length() > 0) {
                        String physicalstoreid = StorageUnitSDC.getLinkKeyid1ByStorageUnitId(qp, storageNode);
                        if (StringUtil.getLen(physicalstoreid) <= 0L || !PhysicalStore.isTemporaryStorage(qp, physicalstoreid)) continue;
                        throw new SapphireException(tp.translate("You cannot check out from temporary storage. Use 'Remove from Storage'."));
                    }
                    String boxid = StorageUnitSDC.getStorageNodeBySDC(this.getQueryProcessor(), currentStorageUnitId, "LV_Box");
                    if (StringUtil.getLen(boxid) != 0L) continue;
                    throw new SapphireException(tp.translate("Selected item(s) are not in physical storage."));
                }
                throw new SapphireException(tp.translate("Selected item(s) are not in storage."));
            }
        }
    }
}

