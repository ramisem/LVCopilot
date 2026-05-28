/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.handler;

import com.labvantage.opal.handler.DefaultErrorRenderer;
import com.labvantage.opal.handler.ErrorRenderer;
import com.labvantage.sapphire.platform.Configuration;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.util.ConnectionInfo;
import sapphire.util.Logger;

public class ErrorUtil {
    public static String LABVANTAGE_CVS_ID = "$Revision: 91529 $";

    public static ErrorRenderer getErrorHandler(PageContext pageContext) {
        ErrorRenderer errorRenderer;
        String defaultErrorClassName = "com.labvantage.opal.handler.DefaultErrorRenderer";
        StringBuffer errorMsg = new StringBuffer();
        ConnectionProcessor cp = new ConnectionProcessor(pageContext);
        String customRuleJavaPackage = cp.getProfileProperty("customrulesjavapackage");
        String errorHandlerClassName = customRuleJavaPackage != null && customRuleJavaPackage.trim().length() > 0 ? customRuleJavaPackage + ".ErrorHandlerImpl" : defaultErrorClassName;
        try {
            errorRenderer = (ErrorRenderer)Class.forName(errorHandlerClassName).newInstance();
        }
        catch (InstantiationException ex) {
            errorMsg.append("Instantiation Exception in instanting class: " + errorHandlerClassName + " ");
            errorMsg.append(ex.getMessage());
            Logger.logError(errorMsg.toString());
            errorRenderer = new DefaultErrorRenderer();
        }
        catch (IllegalAccessException ex) {
            errorMsg.append("IllegalAccess Exception in instanting class: " + errorHandlerClassName + " ");
            errorMsg.append(ex.getMessage());
            Logger.logError(errorMsg.toString());
            errorRenderer = new DefaultErrorRenderer();
        }
        catch (ClassNotFoundException ex) {
            Logger.logInfo("Custom ErrorHandlerImpl not defined. Using DefaultErrorRenderer.");
            errorRenderer = new DefaultErrorRenderer();
        }
        return errorRenderer;
    }

    public static String getErrorHTML(PageContext pageContext, ErrorHandler errorHandler, boolean renderRuleHandlerForm, String sdiFormName) {
        StringBuffer html = new StringBuffer();
        ErrorRenderer errorRenderer = ErrorUtil.getErrorHandler(pageContext);
        errorRenderer.setErrorHandler(errorHandler);
        errorRenderer.setSDIFormName(sdiFormName);
        html.append(errorRenderer.getHTML(pageContext));
        if (renderRuleHandlerForm) {
            html.append(errorRenderer.getFormHTML(pageContext));
        } else {
            pageContext.setAttribute("__ruleHandlerForm", (Object)errorRenderer.getFormHTML(pageContext));
        }
        return html.toString();
    }

    public static String getErrorPageHTML(PageContext pageContext, ErrorHandler errorHandler, boolean renderRuleHandlerForm, String sdiFormName) {
        StringBuffer html = new StringBuffer();
        ErrorRenderer errorRenderer = ErrorUtil.getErrorHandler(pageContext);
        errorRenderer.setErrorHandler(errorHandler);
        errorRenderer.setSDIFormName(sdiFormName);
        html.append(errorRenderer.getPageHTML(pageContext));
        if (renderRuleHandlerForm) {
            html.append(errorRenderer.getFormHTML(pageContext));
        } else {
            pageContext.setAttribute("__ruleHandlerForm", (Object)errorRenderer.getFormHTML(pageContext));
        }
        return html.toString();
    }

    public static String getErrorPageScript(PageContext pageContext, ErrorHandler errorHandler, String sdiFormName) {
        StringBuffer html = new StringBuffer();
        ErrorRenderer errorRenderer = ErrorUtil.getErrorHandler(pageContext);
        errorRenderer.setErrorHandler(errorHandler);
        errorRenderer.setSDIFormName(sdiFormName);
        html.append(errorRenderer.getPageScript(pageContext));
        return html.toString();
    }

    public static String getValidationErrorHTML(ConnectionProcessor cp, TranslationProcessor tp, ErrorHandler errorHandler, boolean renderRuleHandlerForm, String sdiFormName) {
        StringBuffer html = new StringBuffer();
        ErrorRenderer errorRenderer = ErrorUtil.getErrorHandler(cp);
        errorRenderer.setErrorHandler(errorHandler);
        errorRenderer.setSDIFormName(sdiFormName);
        html.append(errorRenderer.getValidationHTML(tp));
        return html.toString();
    }

    public static String getValidationErrorHTML(ConnectionProcessor cp, TranslationProcessor tp, ErrorHandler errorHandler, boolean renderRuleHandlerForm, String sdiFormName, boolean renderButtons) {
        StringBuffer html = new StringBuffer();
        ErrorRenderer errorRenderer = ErrorUtil.getErrorHandler(cp);
        errorRenderer.setErrorHandler(errorHandler);
        errorRenderer.setSDIFormName(sdiFormName);
        html.append(errorRenderer.getValidationHTML(tp, renderButtons));
        return html.toString();
    }

    public static ErrorRenderer getErrorHandler(ConnectionProcessor cp) {
        ErrorRenderer errorRenderer = null;
        String errorHandlerClassName = null;
        String defaultErrorClassName = "com.labvantage.opal.handler.DefaultErrorRenderer";
        StringBuffer errorMsg = new StringBuffer();
        String customRuleJavaPackage = cp.getProfileProperty("customrulesjavapackage");
        errorHandlerClassName = customRuleJavaPackage != null && customRuleJavaPackage.trim().length() > 0 ? customRuleJavaPackage + ".ErrorHandlerImpl" : defaultErrorClassName;
        try {
            errorRenderer = (ErrorRenderer)Class.forName(errorHandlerClassName).newInstance();
        }
        catch (InstantiationException ex) {
            errorMsg.append("Instantiation Exception in instanting class: " + errorHandlerClassName + " ");
            errorMsg.append(ex.getMessage());
            Logger.logError(errorMsg.toString());
            errorRenderer = new DefaultErrorRenderer();
        }
        catch (IllegalAccessException ex) {
            errorMsg.append("IllegalAccess Exception in instanting class: " + errorHandlerClassName + " ");
            errorMsg.append(ex.getMessage());
            Logger.logError(errorMsg.toString());
            errorRenderer = new DefaultErrorRenderer();
        }
        catch (ClassNotFoundException ex) {
            Logger.logInfo("Custom ErrorHandlerImpl not defined. Using DefaultErrorRenderer.");
            errorRenderer = new DefaultErrorRenderer();
        }
        return errorRenderer;
    }

    public static String formatErrorMessage(String errorMessage) {
        StringBuffer sb = new StringBuffer();
        sb.append("<div style='font:bold 12px verdana;color:red'>");
        sb.append("Validation Error");
        sb.append("</div>");
        sb.append("<hr style='color:brickred'>");
        sb.append("<div style='font:normal 12px verdana;color:firebrick;height:250px;width:100%;overflow:auto'>");
        sb.append(errorMessage);
        sb.append("</div>");
        return sb.toString();
    }

    public static String formatErrorMessage(ErrorHandler errorHandler) {
        StringBuffer sb = new StringBuffer();
        if (errorHandler != null && errorHandler.size() > 0) {
            sb.append("<div style='height:250px;width:100%;overflow:auto;padding:0'>");
            for (int i = 0; i < errorHandler.size(); ++i) {
                ErrorDetail errorDetail = (ErrorDetail)errorHandler.get(i);
                if (errorDetail.getErrorType().equals("VALIDATION")) {
                    sb.append("<div style='font:normal 12px verdana;color:firebrick;padding:4px'>");
                } else if (errorDetail.getErrorType().equals("CONFIRM")) {
                    sb.append("<div style='font:normal 12px verdana;color:navy;padding:4px'>");
                } else if (errorDetail.getErrorType().equals("INFORMATION")) {
                    sb.append("<div style='font:normal 12px verdana;color:green;padding:4px'>");
                }
                sb.append(errorDetail.getMessage());
                sb.append("</div>");
            }
            sb.append("</div>");
        } else {
            sb.append(ErrorUtil.formatErrorMessage("Unknown error"));
        }
        return sb.toString();
    }

    public static String extractMessage(String exceptionMessage, boolean isAdmin) {
        return isAdmin ? exceptionMessage : ErrorUtil.sanitizeErrorMessage(exceptionMessage);
    }

    private static String sanitizeErrorMessage(String msg) {
        if (msg == null) {
            return msg;
        }
        int oraIndex = msg.indexOf("ORA-");
        if (oraIndex > -1) {
            msg = msg.substring(0, oraIndex);
        }
        msg = ErrorUtil.removeFQCN("com.labvantage.", msg);
        msg = ErrorUtil.removeFQCN("sapphire.", msg);
        msg = ErrorUtil.removeFQCN("java.", msg);
        msg = ErrorUtil.removeFQCN("javax.", msg);
        return msg;
    }

    private static String removeFQCN(String prefix, String msg) {
        int pkgStartIdx = -1;
        while ((pkgStartIdx = msg.indexOf(prefix)) > -1) {
            int lastDotIdx;
            int pkgEndIdx = -1;
            int i = pkgStartIdx + prefix.length();
            int j = 0;
            while (i < msg.length()) {
                char c = msg.charAt(i);
                if (j == 0 && (c == ':' || c == ' ' || c == ';' || c == '-')) break;
                if (c == ':' || c == ' ' || c == ';' || c == '-') {
                    pkgEndIdx = i;
                    break;
                }
                ++i;
                ++j;
            }
            if (pkgEndIdx <= -1 || (lastDotIdx = msg.substring(pkgStartIdx, pkgEndIdx).lastIndexOf(".")) <= -1) break;
            msg = msg.substring(0, pkgStartIdx) + msg.substring(msg.length() >= pkgStartIdx + lastDotIdx + 1 ? pkgStartIdx + lastDotIdx + 1 : pkgStartIdx + lastDotIdx);
        }
        return msg;
    }

    private static String parseExceptionMessage(String exceptionMessage, String userMessage, boolean isAdmin) {
        String finalMsg = isAdmin ? (exceptionMessage = exceptionMessage.replace("Please contact your Administrator.", "")) : userMessage;
        return finalMsg;
    }

    public static boolean isUserAdmin(String connectionID) {
        if (Configuration.isJunitServer()) {
            return true;
        }
        boolean isAdmin = false;
        ConnectionInfo conInfo = new ConnectionProcessor(connectionID).getConnectionInfo(connectionID);
        if (conInfo != null && conInfo.hasRole("Administrator") && conInfo.hasRole("WebPage-Admin") && conInfo.hasModule("Security")) {
            isAdmin = true;
        }
        return isAdmin;
    }

    public static String extractMessageFromException(Throwable exception, boolean isAdmin) {
        String exceptionMessage = "";
        if (isAdmin) {
            exceptionMessage = exception.getMessage();
        } else {
            String simpleClassName = exception.getClass().getSimpleName();
            exceptionMessage = exception instanceof SapphireException || "DocumentUserException".equals(simpleClassName) || "RestException".equals(simpleClassName) || "FileUploadException".equals(simpleClassName) || "JUnitException".equals(simpleClassName) || "LoginException".equals(simpleClassName) || "ManagerException".equals(simpleClassName) || "MalformedStreamException".equals(simpleClassName) || "MaxLoginExceededException".equals(simpleClassName) || "ParseException".equals(simpleClassName) || "PasswordException".equals(simpleClassName) || "QCPositioningException".equals(simpleClassName) || "RuleException".equals(simpleClassName) || "ServiceException".equals(simpleClassName) || "JSONException".equals(simpleClassName) || "BaseException".equals(simpleClassName) ? ErrorUtil.extractMessage(exception.getMessage(), isAdmin) : "Please contact Administrator";
        }
        return exceptionMessage;
    }
}

