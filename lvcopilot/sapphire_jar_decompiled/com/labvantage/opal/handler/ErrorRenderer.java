/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.handler;

import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorHandler;

public interface ErrorRenderer {
    public static final String RULE_VIOLATION_CONFIRM_HANDLER = "ruleViolationConfirmHandler";
    public static final String RULE_VIOLATION_CANCEL_HANDLER = "ruleViolationCancelHandler";
    public static final String ERRORDIVNAME = "errordiv";
    public static final String SDIDIVNAME = "sdidiv";
    public static final String RULEHANDLERFORMNAME = "ruleHandlerForm";

    public ErrorHandler getErrorHandler();

    public void setErrorHandler(ErrorHandler var1);

    public String getHTML(PageContext var1);

    public String getValidationHTML(TranslationProcessor var1);

    public String getValidationHTML(TranslationProcessor var1, boolean var2);

    public String getFormHTML(PageContext var1);

    public String getPageHTML(PageContext var1);

    public String getPageScript(PageContext var1);

    public void setSDIFormName(String var1);

    public String getSDIFormName();
}

