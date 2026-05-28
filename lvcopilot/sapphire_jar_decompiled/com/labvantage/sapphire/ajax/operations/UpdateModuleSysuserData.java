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

import com.labvantage.sapphire.servlet.command.TagRequestPropertyHandler;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class UpdateModuleSysuserData
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String[] modulesysuser;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String insertList = ajaxResponse.getRequestParameter("insertList");
        String deleteList = ajaxResponse.getRequestParameter("deleteList");
        String[] insertModuleUsers = insertList != "" ? insertList.split("%3B") : null;
        String[] deleteModuleUsers = deleteList != "" ? deleteList.split("%3B") : null;
        ajaxResponse.addCallbackArgument("message", "");
        String moduleids = "";
        String sysuserids = "";
        String error = "";
        TranslationProcessor tp = this.getTranslationProcessor();
        ArrayList<String> moduleIdList = new ArrayList<String>();
        ArrayList<String> userIdList = new ArrayList<String>();
        if (insertModuleUsers != null && insertModuleUsers.length > 0) {
            for (int i = 0; i < insertModuleUsers.length; ++i) {
                if (insertModuleUsers[i].equalsIgnoreCase("")) continue;
                modulesysuser = insertModuleUsers[i].split(";");
                moduleIdList.add(modulesysuser[0]);
                userIdList.add(modulesysuser[1]);
            }
            moduleids = String.join((CharSequence)";", moduleIdList);
            sysuserids = String.join((CharSequence)";", userIdList);
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "ModuleSDC");
            actionProps.setProperty("linkid", "modulesysuser");
            actionProps.setProperty("keyid1", moduleids);
            actionProps.setProperty("sysuserid", sysuserids);
            try {
                if (TagRequestPropertyHandler.isSDCOperationAllowed(this.getConnectionid(), "ModuleSDC", "AddSDIDetail", actionProps, new HashMap())) {
                    this.getActionProcessor().processAction("AddSDIDetail", "1", actionProps);
                }
            }
            catch (SapphireException e) {
                error = e.getMessage();
            }
            if (error.length() > 0) {
                ajaxResponse.addCallbackArgument("message", tp.translate("Saving Data Failed"));
            } else {
                ajaxResponse.addCallbackArgument("message", tp.translate("Add changes have been successfully saved."));
            }
        }
        if (deleteModuleUsers != null && deleteModuleUsers.length > 0) {
            moduleIdList.clear();
            userIdList.clear();
            for (int j = 0; j < deleteModuleUsers.length; ++j) {
                if (deleteModuleUsers[j].equalsIgnoreCase("")) continue;
                modulesysuser = deleteModuleUsers[j].split(";");
                moduleIdList.add(modulesysuser[0]);
                userIdList.add(modulesysuser[1]);
            }
            moduleids = String.join((CharSequence)";", moduleIdList);
            sysuserids = String.join((CharSequence)";", userIdList);
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "ModuleSDC");
            actionProps.setProperty("linkid", "modulesysuser");
            actionProps.setProperty("keyid1", moduleids);
            actionProps.setProperty("sysuserid", sysuserids);
            try {
                if (TagRequestPropertyHandler.isSDCOperationAllowed(this.getConnectionid(), "ModuleSDC", "DeleteSDIDetail", actionProps, new HashMap())) {
                    this.getActionProcessor().processAction("DeleteSDIDetail", "1", actionProps);
                }
            }
            catch (SapphireException e) {
                error = e.getMessage();
            }
            if (error.length() > 0) {
                ajaxResponse.addCallbackArgument("message", tp.translate("Saving Data Failed"));
            } else {
                ajaxResponse.addCallbackArgument("message", tp.translate("Add changes have been successfully saved."));
            }
        }
        ajaxResponse.print();
    }
}

