/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.stability.ScheduleGrid;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.FormatUtil;
import sapphire.util.StringUtil;

public class RecommendedContainers
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 55323 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            String mode = ajaxResponse.getRequestParameter("mode");
            StringBuffer sbHtml = new StringBuffer("");
            if (mode.equalsIgnoreCase("getcontainersforprotocolproducts")) {
                sbHtml.append(this.getContainersForProtocolProducts(ajaxResponse));
            } else if (mode.equalsIgnoreCase("getprotocolproductscontainersforplan")) {
                sbHtml.append(this.getProtocolProductsContainersForPlan(ajaxResponse));
            }
            ajaxResponse.addCallbackArgument("content", sbHtml.toString());
            ajaxResponse.print();
        }
        catch (Exception e) {
            this.logger.error("Error", e);
            ajaxResponse.addCallbackArgument("content", this.getTranslationProcessor().translate("Error while getting Protocol Product Container count.") + "<br>" + e.getMessage());
        }
    }

    private StringBuffer getContainersForProtocolProducts(AjaxResponse ajaxResponse) {
        StringBuffer sbHtml = new StringBuffer("");
        FormatUtil fmutil = FormatUtil.getInstance(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        String productId = ajaxResponse.getRequestParameter("productid");
        double containerSize = fmutil.parseBigDecimal(ajaxResponse.getRequestParameter("containersize")).doubleValue();
        String containerSizeUnit = ajaxResponse.getRequestParameter("containersizeunit");
        String protocolPlans = ajaxResponse.getRequestParameter("protocolplans");
        String[] arrProtocolPlans = protocolPlans.split(";");
        ScheduleGrid grid = new ScheduleGrid(this.getConnectionId());
        String partialPullTypeFlag = ajaxResponse.getRequestParameter("partialpullflag");
        boolean partialPullFlag = !"".equals(partialPullTypeFlag) && !partialPullTypeFlag.equals("N") && !partialPullTypeFlag.equals("X");
        StringBuffer log = new StringBuffer();
        for (int i = 0; i < arrProtocolPlans.length; ++i) {
            try {
                grid.retrieve(arrProtocolPlans[i]);
                grid.setPartialDistribution("X".equalsIgnoreCase(partialPullTypeFlag));
                int containers = grid.conditionAxis.getTotalContainers(containerSize, containerSizeUnit, partialPullFlag, log);
                sbHtml.append("productPlanContainers[\"" + arrProtocolPlans[i] + "\"][\"" + productId + "\"] = " + containers + ";\n");
                grid.planItems.clear();
                continue;
            }
            catch (Exception ex) {
                this.logger.error("Error", ex);
            }
        }
        return sbHtml;
    }

    private StringBuffer getProtocolProductsContainersForPlan(AjaxResponse ajaxResponse) {
        StringBuffer sbHtml = new StringBuffer("");
        String productIds = ajaxResponse.getRequestParameter("productids");
        String containerSizes = ajaxResponse.getRequestParameter("containersizes");
        String containerSizeUnits = ajaxResponse.getRequestParameter("containersizeunits");
        String partialPullFlags = ajaxResponse.getRequestParameter("partialpullflags");
        String protocolPlan = ajaxResponse.getRequestParameter("protocolplan");
        String[] arrProducts = StringUtil.split(productIds, ";");
        String[] arrContainerSizes = StringUtil.split(containerSizes, ";");
        String[] arrContainerSizeUnits = StringUtil.split(containerSizeUnits, ";");
        String[] arrPartialPullFlags = StringUtil.split(partialPullFlags, ";");
        ScheduleGrid grid = new ScheduleGrid(this.getConnectionId());
        FormatUtil fmutil = FormatUtil.getInstance(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        StringBuffer log = new StringBuffer();
        try {
            grid.retrieve(protocolPlan);
            for (int i = 0; i < arrProducts.length; ++i) {
                double containerSize = fmutil.parseBigDecimal(arrContainerSizes[i]).doubleValue();
                boolean partialPullFlag = "Y".equals(arrPartialPullFlags[i]);
                grid.setPartialDistribution("X".equalsIgnoreCase(arrPartialPullFlags[i]));
                int containers = grid.conditionAxis.getTotalContainers(containerSize, arrContainerSizeUnits[i], partialPullFlag, log);
                sbHtml.append("productPlanContainers[\"" + protocolPlan + "\"][\"" + arrProducts[i] + "\"] = " + containers + ";\n");
            }
        }
        catch (Exception ex) {
            this.logger.error("Error", ex);
        }
        return sbHtml;
    }
}

