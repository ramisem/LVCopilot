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

import com.labvantage.sapphire.util.UnitsUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class CheckTrackItemInventory
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        int tiQty = Integer.parseInt(ajaxResponse.getRequestParameter("tiqty"));
        String tiQtyUnits = ajaxResponse.getRequestParameter("tiqtyunits");
        String tiQtyType = ajaxResponse.getRequestParameter("tiqtytype");
        String containertTypeId = ajaxResponse.getRequestParameter("containertypeid");
        int enteredQty = Integer.parseInt(ajaxResponse.getRequestParameter("enteredqty"));
        String enteredQtyUnits = ajaxResponse.getRequestParameter("enteredqtyunits");
        boolean isQuantityAvailable = false;
        if (tiQtyType.equals("U")) {
            isQuantityAvailable = this.checkInventory(tiQtyUnits, enteredQtyUnits, enteredQty, tiQty, ajaxResponse);
        } else if (tiQtyType.equals("C")) {
            String sql = "SELECT sizevalue, sizeunits FROM containertype WHERE containertypeid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{containertTypeId});
            tiQtyUnits = ds.getValue(0, "sizeunits", "");
            isQuantityAvailable = this.checkInventory(tiQtyUnits, enteredQtyUnits, enteredQty, tiQty *= Integer.parseInt(ds.getValue(0, "sizevalue", "")), ajaxResponse);
        }
        ajaxResponse.addCallbackArgument("response", isQuantityAvailable);
        ajaxResponse.print();
    }

    private boolean checkInventory(String tiQtyUnits, String enteredQtyUnits, int enteredQty, int tiQty, AjaxResponse ajaxResponse) {
        boolean isQuantityAvailable = false;
        if (tiQtyUnits.equals(enteredQtyUnits)) {
            if (enteredQty > tiQty) {
                ajaxResponse.setError("Entered volume must be less than TrackItem available quantity.");
            } else {
                isQuantityAvailable = true;
            }
        } else {
            try {
                String convertedValue = UnitsUtil.getConvertedValue(this.getQueryProcessor(), tiQtyUnits, enteredQtyUnits, String.valueOf(tiQty));
                if (enteredQty > Integer.parseInt(convertedValue)) {
                    ajaxResponse.setError("Entered volume must be less than TrackItem available quantity.");
                } else {
                    isQuantityAvailable = true;
                }
            }
            catch (SapphireException e) {
                ajaxResponse.setError("Entered Volume cannot be compared to Trackitem Available Quantity. Unit Conversion expression not defined.");
            }
        }
        return isQuantityAvailable;
    }
}

