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

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.validation.BaseValidation;
import com.labvantage.sapphire.admin.ddt.PhysicalStore;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class ValidateRemoveTempStorage
extends BaseValidation {
    public static String LABVANTAGE_CVS_ID = "$Revision: 57902 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackItemIds = ajaxResponse.getRequestParameter("trackitemids");
        String message = "";
        if (trackItemIds != null && trackItemIds.length() > 0) {
            try {
                this.validateRemoveStorage(trackItemIds);
            }
            catch (SapphireException e) {
                message = ErrorUtil.formatErrorMessage(e.getMessage());
            }
        } else {
            message = ErrorUtil.formatErrorMessage("Track Item Id not found for the selected item(s).");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private void validateRemoveStorage(String trackItemIds) throws SapphireException {
        QueryProcessor qp = this.getQueryProcessor();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        sql.append("SELECT currentstorageunitid FROM trackitem");
        sql.append(" WHERE trackitemid IN (").append(safeSQL.addIn(trackItemIds, ";")).append(")");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String currentStorageUnitId = ds.getValue(i, "currentstorageunitid", "");
                String storageNode = "";
                if (currentStorageUnitId.length() > 0) {
                    storageNode = StorageUnitSDC.getStorageNodeBySDC(qp, currentStorageUnitId, "PhysicalStore");
                    if (storageNode.length() > 0) {
                        String physicalstoreid = StorageUnitSDC.getLinkKeyid1ByStorageUnitId(qp, storageNode);
                        if (physicalstoreid == null || physicalstoreid.length() <= 0 || PhysicalStore.isTemporaryStorage(qp, physicalstoreid)) continue;
                        throw new SapphireException(tp.translate("Please use \"Take Custody\" operation to remove an item from a non-temporary storage."));
                    }
                    throw new SapphireException(tp.translate("One or more of the selected item(s) are not in a temporary physical storage."));
                }
                throw new SapphireException(tp.translate("One or more of the selected item(s) are not in storage."));
            }
        }
    }
}

