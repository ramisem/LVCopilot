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
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.BaseAjaxValidation;
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
import sapphire.util.StringUtil;

public class CheckOutValidation
extends BaseAjaxValidation {
    public static String LABVANTAGE_CVS_ID = "$Revision: 54721 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackItemIds = ajaxResponse.getRequestParameter("trackitemids");
        if (trackItemIds != null && trackItemIds.length() > 0) {
            try {
                this.validateCheckOut(trackItemIds);
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

    private void validateCheckOut(String trackItemIds) throws SapphireException {
        QueryProcessor qp = this.getQueryProcessor();
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        TranslationProcessor tp = this.getTranslationProcessor();
        sql.append("SELECT linksdcid, currentstorageunitid FROM trackitem ");
        sql.append(" WHERE trackitemid IN ( ").append(safeSQL.addIn(trackItemIds, ";")).append(" )");
        sql.append(" AND linksdcid NOT IN ( 'Plate' )");
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String linksdcid = ds.getValue(i, "linksdcid", "");
                String currentStorageUnitId = ds.getValue(i, "currentstorageunitid", "");
                String storageNode = "";
                if (currentStorageUnitId.length() > 0) {
                    String physicalstoreid;
                    storageNode = StorageUnitSDC.getStorageNodeBySDC(qp, currentStorageUnitId, "LV_Package");
                    if (StringUtil.getLen(storageNode) > 0L) {
                        if ("LV_Box".equals(linksdcid)) {
                            String packageStatus = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_package", "packagestatus", "s_packageid = ( select s.linkkeyid1 from storageunit s where s.storageunitid = ?)", new String[]{currentStorageUnitId});
                            if ("Shipped".equals(packageStatus)) {
                                throw new SapphireException(tp.translate("You cannot check out Box from a Shipped Package."));
                            }
                        } else {
                            throw new SapphireException(tp.translate("You cannot check out from a Package. Use \"Unpack Package\" operation."));
                        }
                    }
                    if ((storageNode = StorageUnitSDC.getStorageNodeBySDC(qp, currentStorageUnitId, "PhysicalStore")).length() <= 0 || StringUtil.getLen(physicalstoreid = StorageUnitSDC.getLinkKeyid1ByStorageUnitId(qp, storageNode)) <= 0L || !PhysicalStore.isTemporaryStorage(qp, physicalstoreid)) continue;
                    throw new SapphireException(tp.translate("You cannot check out from temporary storage. Use \"Remove from Storage\" operation."));
                }
                throw new SapphireException(tp.translate("Selected item(s) are not in storage."));
            }
        }
    }
}

