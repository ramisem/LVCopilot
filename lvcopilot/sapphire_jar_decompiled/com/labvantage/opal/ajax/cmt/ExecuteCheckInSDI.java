/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.cmt;

import com.labvantage.sapphire.actions.cmt.CheckInSDI;
import com.labvantage.sapphire.actions.cmt.RepositoryOperations;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class ExecuteCheckInSDI
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message;
        AjaxResponse ajaxResponse;
        block14: {
            ajaxResponse = new AjaxResponse(request, response);
            message = "";
            String changelogid = ajaxResponse.getRequestParameter("changelogid", "");
            String checkincomments = ajaxResponse.getRequestParameter("checkincomments", "");
            String repositorycheck = ajaxResponse.getRequestParameter("repositorycheck", "");
            if (checkincomments.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("changelogid", changelogid);
                props.setProperty("notes", checkincomments);
                try {
                    CMTPolicy policy = CMTPolicy.getPolicy(this.getConnectionid(), "");
                    if (policy.isMasterRepositoryEnabled()) {
                        props.setProperty("operation", "checkin");
                        if (props.getProperty("changelogid").length() == 0) {
                            props.setProperty("sdcid", ajaxResponse.getRequestParameter("sdcid"));
                            props.setProperty("keyid1", ajaxResponse.getRequestParameter("keyid1"));
                            props.setProperty("keyid2", ajaxResponse.getRequestParameter("keyid2"));
                            props.setProperty("keyid3", ajaxResponse.getRequestParameter("keyid3"));
                            props.setProperty("changerequestid", ajaxResponse.getRequestParameter("changerequestid"));
                            if ("PropertyTree".equals(ajaxResponse.getRequestParameter("sdcid"))) {
                                props.setProperty("propertytreenodeid", ajaxResponse.getRequestParameter("propertytreenodeid"));
                            }
                        }
                        this.getActionProcessor().processActionClass(RepositoryOperations.class.getName(), props);
                        break block14;
                    }
                    this.getActionProcessor().processActionClass(CheckInSDI.class.getName(), props);
                }
                catch (ActionException e) {
                    String errorMsg = e.getMessage();
                    int lastIndex = errorMsg.lastIndexOf("sapphire.accessor.ActionException");
                    if (lastIndex > -1) {
                        message = errorMsg = errorMsg.substring(lastIndex + 35);
                    } else {
                        errorMsg = "";
                        message = this.getTranslationProcessor().translate("Error in checking in. Please contact Administrator.");
                    }
                    this.logger.error(e.getMessage(), e);
                }
            } else if (repositorycheck.length() > 0) {
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("operation", "getremoteuser");
                    this.getActionProcessor().processActionClass(RepositoryOperations.class.getName(), props);
                    if (props.getProperty("error").length() > 0) {
                        message = props.getProperty("error");
                        ajaxResponse.addCallbackArgument("repositoryurl", "");
                        ajaxResponse.addCallbackArgument("databasid", "");
                        ajaxResponse.addCallbackArgument("processasuserid", "");
                        ajaxResponse.addCallbackArgument("dbms", "");
                        ajaxResponse.addCallbackArgument("changerequestmandatory", "");
                    } else {
                        PropertyList repositoryUserPL = props.getPropertyList("repositoryuser");
                        ajaxResponse.addCallbackArgument("repositoryurl", props.getProperty("repositoryurl"));
                        ajaxResponse.addCallbackArgument("databasid", repositoryUserPL.getProperty("databaseid"));
                        ajaxResponse.addCallbackArgument("processasuserid", repositoryUserPL.getProperty("processasuserid"));
                        ajaxResponse.addCallbackArgument("dbms", repositoryUserPL.getProperty("dbms"));
                        ajaxResponse.addCallbackArgument("changerequestmandatory", repositoryUserPL.getProperty("changerequestmandatory"));
                    }
                }
                catch (ActionException e) {
                    message = this.getTranslationProcessor().translate("Error in checking in. Please contact Administrator.");
                    this.logger.error(e.getMessage(), e);
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

