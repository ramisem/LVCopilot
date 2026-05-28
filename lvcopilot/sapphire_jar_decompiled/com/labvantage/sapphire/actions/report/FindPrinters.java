/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SecurityService;
import java.util.HashSet;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;
import sun.awt.AppContext;

public class FindPrinters
extends BaseAction {
    public static final String ID = "FindPrinters";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_REFRESHPRINTERS = "refreshprinters";
    public static final String PROPERTY_LISTPRINTERS = "listprinters";
    public static final String RETURN_COUNT = "count";
    public static final String RETURN_PRINTERS = "printers";
    public static final String RETURN_MSG = "msg";

    /*
     * Unable to fully structure code
     */
    @Override
    public void processAction(PropertyList properties) throws ActionException {
        msg = new StringBuilder();
        printerArray = new JSONArray();
        tp = this.getTranslationProcessor();
        count = 0;
        allow = true;
        connectionId = this.getConnectionid();
        policy = Configuration.getDatabaseSecurityPolicy(SecurityService.getDatabaseId(connectionId), SecurityService.isVirtualUser(connectionId), SecurityService.isPortalUser(connectionId));
        if (policy != null) {
            section = policy.getPropertyList("systemprinters");
            allow = section.getProperty("systemprintersenabled", "N").equals("Y");
        }
        if (allow) {
            refreshPrinters = properties.getProperty("refreshprinters", "Y").equals("Y");
            listPrinters = properties.getProperty("listprinters", "Y").equals("Y");
            printerArray = new JSONArray();
            oldPrinters = new HashSet<String>();
            newPrinters = new HashSet<String>();
            try {
                if (refreshPrinters) {
                    printServices = PrintServiceLookup.lookupPrintServices(null, null);
                    for (PrintService printer : printServices) {
                        printerName = printer.getName();
                        oldPrinters.add(printerName);
                    }
                    var15_18 = classes = PrintServiceLookup.class.getDeclaredClasses();
                    var16_22 = var15_18.length;
                    for (printer = 0; printer < var16_22; ++printer) {
                        aClass = var15_18[printer];
                        if (!"javax.print.PrintServiceLookup$Services".equals(aClass.getName())) continue;
                        AppContext.getAppContext().remove(aClass);
                        break;
                    }
                }
                printServices = PrintServiceLookup.lookupPrintServices(null, null);
                for (PrintService printer : printServices) {
                    ++count;
                    if (refreshPrinters) {
                        printerName = printer.getName();
                        newPrinters.add(printerName);
                    }
                    if (!listPrinters) continue;
                    jsonObject = new JSONObject();
                    jsonObject.put("path", printer.getName());
                    printerArray.put(jsonObject);
                }
                if (!refreshPrinters) ** GOTO lbl71
                msg.append("\n").append(tp.translate("New printers available")).append(":\n");
                for (String newPrinter : newPrinters) {
                    if (oldPrinters.contains(newPrinter)) continue;
                    msg.append("\n").append(newPrinter);
                }
                msg.append("\n").append(tp.translate("Unavailable printers")).append(":\n");
                for (String oldPrinter : oldPrinters) {
                    if (newPrinters.contains(oldPrinter)) continue;
                    msg.append("\n").append(oldPrinter);
                }
            }
            catch (Exception e) {
                this.logger.error("Error getting printer:\n" + e.getMessage());
                msg.append(tp.translate("Could not load printers")).append(".");
            }
        } else {
            this.logger.error("Error getting printer: Printer browsing disabled in SecurityPolicy.");
            msg.append(tp.translate("Could not load printers")).append(".");
        }
lbl71:
        // 4 sources

        properties.setProperty("count", String.valueOf(count));
        properties.setProperty("printers", printerArray.toString());
        properties.setProperty("msg", msg.toString());
    }
}

