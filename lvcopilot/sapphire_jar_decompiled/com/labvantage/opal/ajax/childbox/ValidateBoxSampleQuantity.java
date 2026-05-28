/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.childbox;

import com.labvantage.opal.validation.misc.ConvertUnits;
import java.math.BigDecimal;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class ValidateBoxSampleQuantity
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        BigDecimal volume;
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String boxid = ajaxResponse.getRequestParameter("boxid", "").trim();
        BigDecimal minquantity = volume = BigDecimal.valueOf(Double.parseDouble(ajaxResponse.getRequestParameter("volume", "0")));
        String volumeunit = ajaxResponse.getRequestParameter("volumeunit", "").trim();
        String quantityok = "Y";
        String qtymessage = "";
        if (volume.doubleValue() > 0.0 && volumeunit.length() > 0 && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid, linksdcid, linkkeyid1, qtycurrent, qtyunits from trackitem where currentstorageunitid in (select su.storageunitid from storageunit su where su.parentid =( select p.storageunitid from storageunit p where p.linksdcid='LV_Box' and p.linkkeyid1 = ?))", (Object[])new String[]{boxid})) != null) {
            for (int i = 0; i < ds.size(); ++i) {
                double vol;
                double qtycurrent = ds.getDouble(i, "qtycurrent", 0.0);
                String qtyunits = ds.getString(i, "qtyunits", "");
                if (qtyunits.length() <= 0) continue;
                if (!volumeunit.equals(qtyunits)) {
                    try {
                        volume = BigDecimal.valueOf(Double.parseDouble(ConvertUnits.convertUnits(this.getQueryProcessor(), volumeunit, qtyunits, String.valueOf(volume))));
                        volumeunit = qtyunits;
                    }
                    catch (SapphireException e) {
                        message = this.getTranslationProcessor().translate("Exception raised while converting units") + " (" + volumeunit + " >> " + qtyunits + ")";
                    }
                }
                if (!((vol = volume.doubleValue()) > qtycurrent)) continue;
                quantityok = "N";
                if (!(minquantity.doubleValue() > qtycurrent)) continue;
                minquantity = BigDecimal.valueOf(qtycurrent);
            }
        }
        if ("N".equals(quantityok)) {
            qtymessage = qtymessage + "<table cellpadding=4 style='width:100%;'><tr><td>";
            qtymessage = qtymessage + this.getTranslationProcessor().translate("Samples in Box does not have enough quantity to create child Box(es).");
            qtymessage = qtymessage + "<br>" + this.getTranslationProcessor().translate("Select one of the following option:");
            qtymessage = qtymessage + "</td></tr></table>";
            qtymessage = qtymessage + "<table cellpadding=4 style='width:100%;'>";
            qtymessage = qtymessage + "<tr>";
            qtymessage = qtymessage + "<td class='maintform_field'><input type='radio' name='qtyradio' id='qty_1' value='" + minquantity + "|" + volumeunit + "' checked></td>";
            qtymessage = qtymessage + "<td class='maintform_field'>" + this.getTranslationProcessor().translate("Set child quantity to minimum available parent quantity") + ":&nbsp;" + minquantity + " " + volumeunit + "</td>";
            qtymessage = qtymessage + "</tr>";
            qtymessage = qtymessage + "<tr>";
            qtymessage = qtymessage + "<td class='maintform_field'><input type='radio' name='qtyradio' id='qty_2'></td>";
            qtymessage = qtymessage + "<td class='maintform_field'>" + this.getTranslationProcessor().translate("Set child quantity to") + "&nbsp;<input style='width:40px;' value='" + volume + "' id='qty_volume' unit='" + volumeunit + "'>&nbsp;" + volumeunit + "<br>(" + this.getTranslationProcessor().translate("Must be less than minimum available parent quantity") + " (" + minquantity + "&nbsp;" + volumeunit + "))</td>";
            qtymessage = qtymessage + "</tr>";
            qtymessage = qtymessage + "</table>";
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("quantityok", quantityok);
        ajaxResponse.addCallbackArgument("qtymessage", qtymessage);
        ajaxResponse.addCallbackArgument("qtyminimun", minquantity.doubleValue());
        ajaxResponse.print();
    }
}

