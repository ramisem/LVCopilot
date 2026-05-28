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

import com.labvantage.opal.ajax.cmt.OperationRequireCheckOut;
import com.labvantage.sapphire.cmt.CMTUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CMTAjaxUtil
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ":  $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block10: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            String method = ajaxResponse.getRequestParameter("method");
            try {
                if ("doForwardToRepositoryOperations".equalsIgnoreCase(method)) {
                    this.doForwardToRepositoryOperations(ajaxResponse);
                    break block10;
                }
                if ("doMethod1".equalsIgnoreCase(method)) {
                    this.doMethod1(ajaxResponse);
                    break block10;
                }
                if ("doGetNodeLinkedSDIs".equalsIgnoreCase(method)) {
                    this.doGetNodeLinkedSDIs(ajaxResponse);
                    break block10;
                }
                throw new SapphireException("No Method specified for execution.");
            }
            catch (Exception e) {
                if (e.getMessage() == null || e.getMessage().length() == 0) {
                    ajaxResponse.setError(e.getClass().getSimpleName());
                } else {
                    ajaxResponse.setError(e.getMessage(), e);
                }
            }
            finally {
                ajaxResponse.print();
            }
        }
    }

    private void doMethod1(AjaxResponse ajaxResponse) {
    }

    private void doGetNodeLinkedSDIs(AjaxResponse ajaxResponse) throws SapphireException {
        String propertyTreeId = ajaxResponse.getRequestParameter("propertytreeid", "");
        String propertyTreeNodeId = ajaxResponse.getRequestParameter("propertytreenodeid", "");
        DataSet linkedSDIs = CMTUtil.getSDIsLinkedToNode(propertyTreeId, propertyTreeNodeId, this.getConnectionId());
        ArrayList<DataSet> groupedSDIs = linkedSDIs.getGroupedDataSets("sdcid");
        StringBuffer message = new StringBuffer();
        for (int i = 0; i < groupedSDIs.size(); ++i) {
            int j;
            DataSet linkedSDIsSDC = groupedSDIs.get(i);
            String groupSDCId = linkedSDIsSDC.getString(0, "sdcid");
            OperationRequireCheckOut.Result result = OperationRequireCheckOut.doOperationRequireCheckout(groupSDCId, linkedSDIsSDC.getColumnValues("keyid1", ";"), linkedSDIsSDC.getString(0, "keyid2", "").length() == 0 ? "" : linkedSDIsSDC.getColumnValues("keyid2", ";"), linkedSDIsSDC.getString(0, "keyid3", "").length() == 0 ? "" : linkedSDIsSDC.getColumnValues("keyid3", ";"), "", this.getConnectionId(), null);
            if (result.checkedOutToMe.getRowCount() <= 0 && result.checkedOutToMe.getRowCount() <= 0 && result.checkOutAble.getRowCount() <= 0) continue;
            message.append("<li>").append(groupSDCId).append("</li>");
            message.append("<ul>");
            if (result.checkedOutToOthers.getRowCount() > 0) {
                message.append("<li>").append(this.getTranslationProcessor().translate("Checked Out to Others:")).append("</li>");
                message.append("<ul>");
                for (j = 0; j < result.checkedOutToOthers.getRowCount(); ++j) {
                    message.append("<li>");
                    message.append(result.checkedOutToOthers.getString(j, "linkkeyid1"));
                    if (!"(null)".equals(result.checkedOutToOthers.getString(j, "linkkeyid2", "(null)"))) {
                        message.append("|").append(result.checkedOutToOthers.getString(j, "linkkeyid2", ""));
                    }
                    if (!"(null)".equals(result.checkedOutToOthers.getString(j, "linkkeyid3", "(null)"))) {
                        message.append("|").append(result.checkedOutToOthers.getString(j, "linkkeyid3", ""));
                    }
                    message.append("</li>");
                }
                message.append("</ul>");
            }
            if (result.checkedOutToMe.getRowCount() > 0) {
                message.append("<li>").append(this.getTranslationProcessor().translate("Checked Out to Me/Department:")).append("</li>");
                message.append("<ul>");
                for (j = 0; j < result.checkedOutToMe.getRowCount(); ++j) {
                    message.append("<li>");
                    message.append(result.checkedOutToMe.getString(j, "linkkeyid1"));
                    if (!"(null)".equals(result.checkedOutToMe.getString(j, "linkkeyid2", "(null)"))) {
                        message.append("|").append(result.checkedOutToMe.getString(j, "linkkeyid2", ""));
                    }
                    if (!"(null)".equals(result.checkedOutToMe.getString(j, "linkkeyid3", "(null)"))) {
                        message.append("|").append(result.checkedOutToMe.getString(j, "linkkeyid3", ""));
                    }
                    message.append("</li>");
                }
                message.append("</ul>");
            }
            if (result.checkOutAble.getRowCount() > 0) {
                message.append("<li>").append(this.getTranslationProcessor().translate("Open for Checkout:")).append("</li>");
                message.append("<ul>");
                for (j = 0; j < result.checkOutAble.getRowCount(); ++j) {
                    message.append("<li>");
                    message.append(result.checkOutAble.getString(j, "keyid1"));
                    if (!"(null)".equals(result.checkOutAble.getString(j, "keyid2", "(null)"))) {
                        message.append("|").append(result.checkOutAble.getString(j, "keyid2", ""));
                    }
                    if (!"(null)".equals(result.checkOutAble.getString(j, "keyid3", "(null)"))) {
                        message.append("|").append(result.checkOutAble.getString(j, "keyid3", ""));
                    }
                    message.append("</li>");
                }
                message.append("</ul>");
            }
            message.append("</ul>");
        }
        if (message.length() > 0) {
            message.insert(0, "<ul>");
            message.append("</ul>");
        } else {
            message.append(this.getTranslationProcessor().translate("No Linked SDIs found."));
        }
        ajaxResponse.addCallbackArgument("linkedsdis", message.toString());
    }

    private void doForwardToRepositoryOperations(AjaxResponse ajaxResponse) throws SapphireException {
        PropertyList requestProps = new PropertyList(new HashMap(ajaxResponse.getRequestParameters()));
        this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.cmt.RepositoryOperations", requestProps);
    }
}

