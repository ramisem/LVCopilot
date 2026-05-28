/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.advancedtoolbar;

import com.labvantage.opal.layouts.LayoutUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefMaint;
import com.labvantage.sapphire.servlet.command.ResourceRequest;
import com.labvantage.sapphire.util.groovy.GroovyPolicyUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.policy.CMTPolicy;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdvancedToolbar
extends BaseElement {
    static String LABVANTAGE_CVS_ID = "$Revision: 104191 $";
    private String __SdcId = "[sdcid]";
    private String __DefaultButtonImage = "WEB-OPAL/elements/advancedtoolbar/images/16by1blank.gif";
    private String __DefaultButtonText;
    private StringBuffer __DeferredCallbackScript = new StringBuffer();
    private ToolbarType toolbarType = ToolbarType.buttons;
    private String pagetitle = "";
    private String customGroupText = "";
    private String customGroupContent = "";
    private String securityMode = "";
    private String globalDisableEsig = "N";
    private TranslationProcessor translationProcessor;
    public static final String BUTTON_TYPE_STANDARD = "Standard";
    public static final String BUTTON_TYPE_ACTION = "Action";
    public static final String BUTTON_TYPE_USER = "User";
    public static final String BUTTON_TYPE_TASK = "TASK";
    private static String buttonStyle = null;
    public static final String JS_CALLFUNCION = "sapphire.call";
    public static final String CONSTANT_OPERATION = "Operation";
    public static final String CONSTANT_BOTH = "Both";
    public static final String CONSTANT_BUTTON = "Button";
    public static final String CONSTANT_HORIZONTAL = "horizontal";
    public static final String CONSTANT_VERTICAL = "vertical";
    public static final String CONSTANT_COLLAPSED = "collapsed";
    public static final String CONSTANT_Y = "Y";
    public static final String CONSTANT_N = "N";
    private PropertyList tasksDataCache = null;
    private DataSet allOpsDataSet = null;
    private String __AdvancedToolbarACBIframe = "acb_iframe";
    private ArrayList __AdvancedToolbarFormFields = new ArrayList();
    private boolean isSDCChangeControlled;
    private HashMap __StandardButtonImages;
    private HashMap __StandardButtonActions;
    private HashMap __StandardButtonInitValidationActions;
    private HashMap __StandardButtonTexts;

    public AdvancedToolbar() {
        this.__AdvancedToolbarFormFields.add("sdcid");
        this.__AdvancedToolbarFormFields.add("keyid1");
        this.__AdvancedToolbarFormFields.add("keyid2");
        this.__AdvancedToolbarFormFields.add("keyid3");
        this.__AdvancedToolbarFormFields.add("sdcid");
        this.__AdvancedToolbarFormFields.add("queryid");
        this.__AdvancedToolbarFormFields.add("param1");
        this.__AdvancedToolbarFormFields.add("param2");
        this.__AdvancedToolbarFormFields.add("param3");
        this.__AdvancedToolbarFormFields.add("param4");
        this.__AdvancedToolbarFormFields.add("param5");
        this.__AdvancedToolbarFormFields.add("param6");
        this.__AdvancedToolbarFormFields.add("param7");
        this.__AdvancedToolbarFormFields.add("param8");
        this.__AdvancedToolbarFormFields.add("param9");
        this.__AdvancedToolbarFormFields.add("param10");
        this.__AdvancedToolbarFormFields.add("param11");
        this.__AdvancedToolbarFormFields.add("param12");
        this.__AdvancedToolbarFormFields.add("queryfrom");
        this.__AdvancedToolbarFormFields.add("querywhere");
        this.__AdvancedToolbarFormFields.add("currentuser");
        this.__AdvancedToolbarFormFields.add("esigpage");
        this.__AdvancedToolbarFormFields.add("auditreasonreftype");
        this.__AdvancedToolbarFormFields.add("reasonpromptoption");
        this.__AdvancedToolbarFormFields.add("message");
        this.__AdvancedToolbarFormFields.add("operation");
        this.__AdvancedToolbarFormFields.add("callback");
        this.__AdvancedToolbarFormFields.add("setesigreasoncallback");
        this.__AdvancedToolbarFormFields.add("keysfromlookup");
        this.__AdvancedToolbarFormFields.add("actionbuttonid");
        this.__AdvancedToolbarFormFields.add("action_sdcid");
        this.__AdvancedToolbarFormFields.add("action_keyid1");
        this.__AdvancedToolbarFormFields.add("action_keyid2");
        this.__AdvancedToolbarFormFields.add("action_keyid3");
        this.__AdvancedToolbarFormFields.add("action_currentuser");
        this.__StandardButtonImages = new HashMap();
        this.__StandardButtonImages.put("Add", new String[]{"WEB-CORE/images/gif/Add.gif", "WEB-CORE/images/png/Add.png", "WEB-CORE/images/png32/Add.png"});
        this.__StandardButtonImages.put("Edit", new String[]{"WEB-CORE/images/gif/Edit.gif", "WEB-CORE/images/png/Edit.png", "WEB-CORE/images/png32/Edit.png"});
        this.__StandardButtonImages.put("Copy", new String[]{"WEB-CORE/images/gif/Copy.gif", "WEB-CORE/images/png/Copy.png", "WEB-CORE/images/png32/Copy.png"});
        this.__StandardButtonImages.put("View", new String[]{"WEB-CORE/images/gif/View.gif", "WEB-CORE/images/png/View.png", "WEB-CORE/images/png32/View.png"});
        this.__StandardButtonImages.put("Delete", new String[]{"WEB-CORE/images/gif/Delete.gif", "WEB-CORE/images/png/Delete.png", "WEB-CORE/images/png32/Delete.png"});
        this.__StandardButtonImages.put("Accept", new String[]{"WEB-CORE/images/gif/Accept.gif", "WEB-CORE/images/png/Accept.png", "WEB-CORE/images/png32/Accept.png"});
        this.__StandardButtonImages.put("Export", new String[]{"WEB-CORE/images/gif/Export.gif"});
        this.__StandardButtonImages.put("NewVersion", new String[]{"WEB-CORE/images/gif/NewVersion.gif"});
        this.__StandardButtonImages.put("ExpireVersion", new String[]{"WEB-CORE/images/gif/VersionControl.gif"});
        this.__StandardButtonImages.put("AddToWorkflow", new String[]{"WEB-CORE/images/png/Workflow.png"});
        this.__StandardButtonImages.put("Save", new String[]{"WEB-CORE/images/gif/Save.gif", "WEB-CORE/images/png/Save.png", "WEB-CORE/images/png32/Save.png"});
        this.__StandardButtonImages.put("SaveAsTemplate", new String[]{"WEB-CORE/images/gif/SaveasTemplate.gif"});
        this.__StandardButtonImages.put("ReturnToList", new String[]{"WEB-CORE/images/gif/ReturntoList.gif", "WEB-CORE/images/png/ReturntoList.png", "WEB-CORE/images/png32/ReturntoList.png"});
        this.__StandardButtonImages.put("CloseRefresh", new String[]{"WEB-CORE/images/gif/Refresh2.gif", "WEB-CORE/images/png/Close&Refresh.png", "WEB-CORE/images/png32/Close&Refresh.png"});
        this.__StandardButtonImages.put("Cancel", new String[]{"WEB-CORE/images/gif/Delete.gif", "WEB-CORE/images/png/Cancel.png", "WEB-CORE/images/png32/Cancel.png"});
        this.__StandardButtonImages.put("Reset", new String[]{"WEB-CORE/images/gif/Reload.gif"});
        this.__StandardButtonImages.put("ApproveVersion", new String[]{"WEB-CORE/images/gif/ApproveVersion.gif"});
        this.__StandardButtonImages.put("Report", new String[]{"WEB-CORE/images/gif/More.gif"});
        this.__StandardButtonImages.put("DeliverRunFile", new String[]{"rc?command=image&image=FlatBlackBulletList4"});
        this.__StandardButtonImages.put("Forward", new String[]{"WEB-CORE/images/gif/Forward.gif"});
        this.__StandardButtonImages.put("ManageSecuritySet", new String[]{"WEB-CORE/images/png/ManageSecurity.png"});
        this.__StandardButtonImages.put("ShowNotes", new String[]{"WEB-CORE/imageref/finance_business_and_trade/office/notes/32/note_view.png"});
        this.__StandardButtonImages.put("AddNote", new String[]{"WEB-CORE/imageref/finance_business_and_trade/office/notes/32/note_add.png"});
        this.__StandardButtonActions = new HashMap();
        this.__StandardButtonActions.put("Add", "addSDI");
        this.__StandardButtonActions.put("Edit", "editSDI");
        this.__StandardButtonActions.put("Copy", "copySDI");
        this.__StandardButtonActions.put("View", "viewSDI");
        this.__StandardButtonActions.put("Delete", "deleteSDI");
        this.__StandardButtonActions.put("Accept", "acceptMulti");
        this.__StandardButtonActions.put("Export", "exportSDI");
        this.__StandardButtonActions.put("NewVersion", "newVersion");
        this.__StandardButtonActions.put("ExpireVersion", "expireVersion");
        this.__StandardButtonActions.put("AddToWorkflow", "addToWorkflow");
        this.__StandardButtonActions.put("Save", "save");
        this.__StandardButtonActions.put("SaveAsTemplate", "saveAsTemplate");
        this.__StandardButtonActions.put("ReturnToList", "cancelAndReturnToList");
        this.__StandardButtonActions.put("CloseRefresh", "closeAndRefreshPopup");
        this.__StandardButtonActions.put("Cancel", "cancelPopup");
        this.__StandardButtonActions.put("Reset", "reset");
        this.__StandardButtonActions.put("ApproveVersion", "approveVersion");
        this.__StandardButtonActions.put("Report", "launchReport");
        this.__StandardButtonActions.put("DeliverRunFile", "deliverRunFile");
        this.__StandardButtonActions.put("Forward", "top.forwardTo");
        this.__StandardButtonActions.put("ManageSecuritySet", "manageSecuritySet");
        this.__StandardButtonActions.put("ShowNotes", "toggleSDINotes");
        this.__StandardButtonActions.put("AddNote", "addSDINote");
        this.__StandardButtonInitValidationActions = new HashMap();
        this.__StandardButtonInitValidationActions.put("Edit", "checkAtleastOneSelected()");
        this.__StandardButtonInitValidationActions.put("Copy", "checkAtleastOneSelected()");
        this.__StandardButtonInitValidationActions.put("View", "checkAtleastOneSelected()");
        this.__StandardButtonInitValidationActions.put("Delete", "deleteConfirmation()");
        this.__StandardButtonInitValidationActions.put("Accept", "checkAtleastOneSelectedInAnyPage()");
        this.__StandardButtonInitValidationActions.put("Export", "checkAtleastOneSelected()");
        this.__StandardButtonInitValidationActions.put("NewVersion", "checkOnlyOneSelected()");
        this.__StandardButtonInitValidationActions.put("ExpireVersion", "expireConfirmation()");
        this.__StandardButtonInitValidationActions.put("AddToWorkflow", "checkAtleastOneSelected()");
        this.__StandardButtonInitValidationActions.put("Save", "preSave()");
        this.__StandardButtonInitValidationActions.put("PreSave", "saveSetup()");
        this.__StandardButtonInitValidationActions.put("SaveAsTemplate", "preSaveAsTemplate()");
        this.__StandardButtonInitValidationActions.put("ApproveVersion", "approveConfirmation()");
        this.__StandardButtonInitValidationActions.put("Forward", "confirmChangesMade()");
        this.__StandardButtonInitValidationActions.put("ManageSecuritySet", "checkOnlyOneSelected()");
        this.__StandardButtonTexts = new HashMap();
    }

    private void setButtonTextDefaults() {
        this.__StandardButtonTexts.put("Add", this.translationProcessor.translate("Add"));
        this.__StandardButtonTexts.put("Edit", this.translationProcessor.translate("Edit"));
        this.__StandardButtonTexts.put("Copy", this.translationProcessor.translate("Copy"));
        this.__StandardButtonTexts.put("View", this.translationProcessor.translate("View"));
        this.__StandardButtonTexts.put("Delete", this.translationProcessor.translate("Delete"));
        this.__StandardButtonTexts.put("Accept", this.translationProcessor.translate("Select & Return"));
        this.__StandardButtonTexts.put("Export", this.translationProcessor.translate("Export"));
        this.__StandardButtonTexts.put("NewVersion", this.translationProcessor.translate("New Version"));
        this.__StandardButtonTexts.put("ExpireVersion", this.translationProcessor.translate("Expire Version"));
        this.__StandardButtonTexts.put("AddToWorkflow", this.translationProcessor.translate("Add To Workflow"));
        this.__StandardButtonTexts.put("Save", this.translationProcessor.translate("Save"));
        this.__StandardButtonTexts.put("SaveAsTemplate", this.translationProcessor.translate("Save As Template"));
        this.__StandardButtonTexts.put("ReturnToList", this.translationProcessor.translate("Cancel & Return To List"));
        this.__StandardButtonTexts.put("CloseRefresh", this.translationProcessor.translate("Close & Refresh Caller Window"));
        this.__StandardButtonTexts.put("Cancel", this.translationProcessor.translate("Cancel Popup"));
        this.__StandardButtonTexts.put("Reset", this.translationProcessor.translate("Reset"));
        this.__StandardButtonTexts.put("ApproveVersion", this.translationProcessor.translate("Approve Version"));
        this.__StandardButtonTexts.put("Report", this.translationProcessor.translate("Launch Report"));
        this.__StandardButtonTexts.put("DeliverRunFile", this.translationProcessor.translate("Deliver Run File"));
        this.__StandardButtonTexts.put("Forward", this.translationProcessor.translate("Forward"));
        this.__StandardButtonTexts.put("ManageSecuritySet", this.translationProcessor.translate("Manage SecuritySet"));
        this.__StandardButtonTexts.put("ShowNotes", this.translationProcessor.translate("Toggle Notes"));
        this.__StandardButtonTexts.put("AddNote", this.translationProcessor.translate("Add Note"));
    }

    private PropertyList getTask(String taskdefid, String taskdefversionid, String taskdefvariantid) {
        if (this.tasksDataCache == null) {
            this.tasksDataCache = TaskDefMaint.getTasksData(false, this.getSDIProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
        }
        if (this.tasksDataCache != null && this.tasksDataCache.getCollection("tasks") != null) {
            return this.tasksDataCache.getCollection("tasks").find("taskkey", taskdefid + ";" + taskdefversionid + ";" + taskdefvariantid);
        }
        return null;
    }

    public static boolean isRibbon(RequestContext rc) {
        if (rc != null && rc.getPropertyList().containsKey("advancedtoolbar")) {
            PropertyList tb = rc.getPropertyList("advancedtoolbar");
            return AdvancedToolbar.isRibbon(tb);
        }
        return false;
    }

    public static boolean isRibbon(PropertyList at) {
        String ds = at.getProperty("displaystyle", "");
        boolean isModern = true;
        if (isModern) {
            return true;
        }
        return ds.equalsIgnoreCase("ribbon") || ds.equalsIgnoreCase("compact ribbon") || ds.equalsIgnoreCase("hybrid ribbon");
    }

    public static boolean isTextOnly() {
        return "Text Only".equals(buttonStyle);
    }

    @Override
    public String getHtml() {
        boolean changeControlEnabled;
        this.globalDisableEsig = this.getConfigurationProcessor().getProfileProperty("disableesig");
        this.translationProcessor = this.getTranslationProcessor();
        this.__DefaultButtonText = this.translationProcessor.translate("Text Not defined");
        this.setButtonTextDefaults();
        String modernlayout = this.requestContext.getPropertyList().getProperty("modernlayout");
        this.toolbarType = CONSTANT_Y.equals(modernlayout) && !this.element.getProperty("forcestyle").equalsIgnoreCase(CONSTANT_Y) ? ToolbarType.modern : (this.element.getProperty("displaystyle").equalsIgnoreCase("ribbon") ? (this.browser != null && this.browser.isMobile() ? ToolbarType.compactRibbon : ToolbarType.ribbon) : (this.element.getProperty("displaystyle").equalsIgnoreCase("compact ribbon") ? ToolbarType.compactRibbon : (this.element.getProperty("displaystyle").equalsIgnoreCase("hybrid ribbon") ? ToolbarType.hybridRibbon : (this.element.getProperty("displaystyle").equalsIgnoreCase("modern") ? ToolbarType.modern : ToolbarType.buttons))));
        this.pagetitle = this.element.getProperty("pagetitle");
        if (this.browser.isPhone() || this.element.getProperty("showtitle").equalsIgnoreCase(CONSTANT_N)) {
            this.pagetitle = "";
        }
        this.customGroupText = this.element.getProperty("customgrouptext");
        this.customGroupContent = this.element.getProperty("customgroupcontent");
        if (this.pageContext.getRequest().getParameter("_iframename") == null) {
            this.addNavigatorGroup(this.toolbarType);
        }
        StringBuffer hotkeys = new StringBuffer();
        StringBuffer acbActions = new StringBuffer();
        ArrayList buttons = new ArrayList();
        int noOfButtons = 0;
        String buttonSpacing = this.element.getProperty("buttonspacing");
        String renderMode = this.element.getProperty("rendermode");
        this.__SdcId = this.element.getProperty("sdcid");
        if (this.__SdcId.length() == 0) {
            this.__SdcId = this.pageContext.getRequest().getParameter("sdcid");
            this.__SdcId = this.__SdcId == null ? "" : this.__SdcId;
        }
        PropertyListCollection plcIncludes = this.element.getCollection("includes");
        int noOfIncludes = 0;
        if (plcIncludes != null) {
            noOfIncludes = plcIncludes.size();
        }
        StringBuffer sbHtml = new StringBuffer("");
        if (renderMode.equalsIgnoreCase(CONSTANT_BUTTON)) {
            sbHtml.append("<script language=\"JavaScript\" src=\"WEB-OPAL/scripts/util.js\"></script>\n");
            sbHtml.append("<script language=\"JavaScript\" src=\"WEB-OPAL/scripts/esig.js\"></script>\n");
            sbHtml.append("<script language=\"JavaScript\" src=\"WEB-OPAL/scripts/cocbutton.js\"></script>\n");
            sbHtml.append("<script language=\"JavaScript\" src=\"WEB-OPAL/elements/advancedtoolbar/scripts/validationhandlers.js\"></script>\n");
            sbHtml.append("<script language=\"JavaScript\" src=\"WEB-OPAL/elements/advancedtoolbar/scripts/advancedtoolbar.js\"></script>\n");
            sbHtml.append("<script language=\"JavaScript\" src=\"WEB-OPAL/scripts/actionbuttonscript.js\"></script>\n");
            for (int i = 0; i < noOfIncludes; ++i) {
                PropertyList plInclude = plcIncludes.getPropertyList(i);
                try {
                    sbHtml.append(ResourceRequest.getResourceTag(this.pageContext, new ConnectionProcessor(this.getConnectionid()).getConnectionInfo(this.getConnectionid()).getDatabaseId(), plInclude, "text/javascript")).append("\n");
                    continue;
                }
                catch (SapphireException e) {
                    sbHtml.append("<!--").append(e.getMessage()).append("-->");
                }
            }
        }
        CMTPolicy cmtPolicy = this.__SdcId.length() > 0 ? CMTPolicy.getPolicy(this.getConnectionid(), this.__SdcId) : null;
        String sdcChangeControlledFlag = cmtPolicy != null ? cmtPolicy.getChangeControlledFlag() : "";
        this.isSDCChangeControlled = CONSTANT_Y.equals(sdcChangeControlledFlag) || "T".equals(sdcChangeControlledFlag);
        boolean bl = changeControlEnabled = this.isSDCChangeControlled && !cmtPolicy.isChangeControlDeferToRepository();
        if (renderMode.length() > 0) {
            PropertyListCollection plcButtons;
            String buttonAlignment;
            String operationSpacing;
            int noOfButtonsInOneLine;
            try {
                noOfButtonsInOneLine = Integer.parseInt(this.element.getProperty("buttonsperline", "0"));
            }
            catch (NumberFormatException ex) {
                noOfButtonsInOneLine = 0;
            }
            String operationAlignment = this.element.getProperty("operationalignment");
            if (operationAlignment != null && operationAlignment.length() > 0 && !operationAlignment.equalsIgnoreCase(CONSTANT_VERTICAL)) {
                this.logger.warn("Operation Alignment of \"" + operationAlignment + "\" no longer supported. Will default to \"" + CONSTANT_VERTICAL + "\".");
            }
            if ((operationSpacing = this.element.getProperty("operationspacing")) != null && operationSpacing.length() > 0 && !operationSpacing.equals("1")) {
                this.logger.warn("Operation Spacing of \"" + operationSpacing + "\" no longer supported. Will default to \"1\".");
            }
            if ((buttonAlignment = this.element.getProperty("buttonalignment")) == null || buttonAlignment.length() == 0) {
                buttonAlignment = CONSTANT_HORIZONTAL;
            } else if (!buttonAlignment.equalsIgnoreCase(CONSTANT_HORIZONTAL)) {
                this.logger.warn("Button Alignment of \"" + buttonAlignment + "\" no longer supported. Will default to \"" + CONSTANT_HORIZONTAL + "\".");
                buttonAlignment = CONSTANT_HORIZONTAL;
            }
            if (buttonSpacing == null || buttonSpacing.length() == 0) {
                buttonSpacing = "5";
            }
            if ((plcButtons = this.element.getCollection("buttons")) != null) {
                noOfButtons = plcButtons.size();
            }
            DataSet sdcsec = null;
            try {
                if (StringUtil.getLen(this.__SdcId) > 0L) {
                    this.securityMode = this.getSDCProcessor().getProperty(this.__SdcId, "accesscontrolledflag");
                    if (this.securityMode.equalsIgnoreCase("d") || "SDIWorkItem".equals(this.__SdcId) || this.securityMode.equalsIgnoreCase("b")) {
                        try {
                            SafeSQL safeSQL = new SafeSQL();
                            String sql = "SELECT operationid, accesstype FROM sdcsecurity WHERE sdcid = " + safeSQL.addVar(this.__SdcId) + " AND sysuserid = " + safeSQL.addVar(this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                            sdcsec = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                        }
                        catch (Exception e) {
                            sdcsec = null;
                            this.logger.warn("Could not obtain sdc security.");
                        }
                    }
                }
            }
            catch (Exception e) {
                sdcsec = null;
                this.logger.warn("Could not obtain sdc access control flag.");
            }
            for (int i = 0; i < noOfButtons; ++i) {
                PropertyList plButton = plcButtons.getPropertyList(i);
                HashMap<String, String> hmButtonProps = new HashMap<String, String>();
                String buttonType = plButton.getProperty("buttontype");
                if (buttonType.length() == 0) {
                    buttonType = BUTTON_TYPE_USER;
                }
                if (!this.showButton(plButton)) continue;
                hmButtonProps.putAll(this.getCommonProps(plButton));
                String buttonMode = (String)hmButtonProps.get("mode");
                if (!(renderMode.equalsIgnoreCase(CONSTANT_BUTTON) && buttonMode.equalsIgnoreCase(CONSTANT_BUTTON) || renderMode.equalsIgnoreCase(CONSTANT_OPERATION) && buttonMode.equalsIgnoreCase(CONSTANT_OPERATION) || buttonMode.equalsIgnoreCase(CONSTANT_BOTH)) && this.toolbarType != ToolbarType.modern) continue;
                String buttonId = (String)hmButtonProps.get("id");
                if (buttonType.equalsIgnoreCase(BUTTON_TYPE_STANDARD)) {
                    hmButtonProps.putAll(this.getStandardButtonProps(buttonId, plButton, sdcsec));
                } else if (buttonType.equalsIgnoreCase(BUTTON_TYPE_ACTION)) {
                    hmButtonProps.putAll(this.getActionButtonProps(buttonId, plButton, sdcsec));
                } else if (buttonType.equalsIgnoreCase(BUTTON_TYPE_USER)) {
                    hmButtonProps.putAll(this.getUserButtonProps(buttonId, plButton, sdcsec));
                } else if (buttonType.equalsIgnoreCase(BUTTON_TYPE_TASK)) {
                    hmButtonProps.putAll(this.getTaskButtonProps(buttonId, plButton, sdcsec));
                }
                if (renderMode.equalsIgnoreCase(CONSTANT_BUTTON) && (buttonMode.equalsIgnoreCase(CONSTANT_BUTTON) || buttonMode.equalsIgnoreCase(CONSTANT_BOTH)) && plButton.getPropertyList("commonprops").getProperty("show", CONSTANT_Y).equalsIgnoreCase(CONSTANT_Y)) {
                    hmButtonProps.put("hotkeytext", this.getHotKey(plButton));
                } else {
                    hmButtonProps.put("hotkeytext", "");
                }
                acbActions.append(this.getACBActions(plButton));
                String checkoutoption = plButton.getProperty("checkoutoption", "None");
                if (checkoutoption.indexOf("$G{") == 0) {
                    HashMap<String, Object> bindMap = new HashMap<String, Object>();
                    bindMap.put("element", this.element);
                    bindMap.put("elements", this.requestContext.getPropertyList().getPropertyList("elements"));
                    bindMap.put("pagedata", this.requestContext.getPropertyList().getPropertyList("pagedata"));
                    bindMap.put("user", this.connectionInfo.getUserAttributeMap());
                    bindMap.put("policy", new GroovyPolicyUtil(this.pageContext));
                    if (this.__SdcId.length() > 0) {
                        bindMap.put("sdc", this.getSDCProcessor().getSDCProperties(this.__SdcId));
                    }
                    try {
                        checkoutoption = GroovyUtil.getInstance(this.pageContext).evaluateSecure(checkoutoption, bindMap);
                    }
                    catch (SapphireException se) {
                        Logger.logError(se.getMessage());
                    }
                }
                hmButtonProps.put("checkoutoption", checkoutoption);
                buttons.add(hmButtonProps);
            }
            if (buttons.size() > 0) {
                if (renderMode.equalsIgnoreCase(CONSTANT_BUTTON)) {
                    switch (this.toolbarType) {
                        case ribbon: {
                            sbHtml.append(this.getRibbonHtml(buttons, this.toolbarType));
                            break;
                        }
                        case compactRibbon: {
                            sbHtml.append(this.getRibbonHtml(buttons, this.toolbarType));
                            break;
                        }
                        case hybridRibbon: {
                            sbHtml.append(this.getRibbonHtml(buttons, this.toolbarType));
                            break;
                        }
                        case modern: {
                            sbHtml.append(this.getModernHtml(buttons));
                            break;
                        }
                        default: {
                            sbHtml.append(this.getButtonsHtml(buttonAlignment, buttonSpacing, noOfButtonsInOneLine, buttons));
                            break;
                        }
                    }
                } else if (renderMode.equalsIgnoreCase(CONSTANT_OPERATION)) {
                    sbHtml = new StringBuffer(this.getSidebarHtml(buttons));
                }
            } else if (renderMode.equalsIgnoreCase(CONSTANT_BUTTON) && (this.toolbarType == ToolbarType.ribbon || this.toolbarType == ToolbarType.compactRibbon || this.toolbarType == ToolbarType.hybridRibbon)) {
                sbHtml.append(this.getRibbonHtml(null, this.toolbarType));
            }
        } else {
            Trace.log("Exception: AdvancedToolbar.getHtml -> RenderMode not specified for the toolbar element");
        }
        this.__StandardButtonActions = null;
        this.__StandardButtonImages = null;
        this.__StandardButtonTexts = null;
        if (renderMode.equalsIgnoreCase(CONSTANT_BUTTON)) {
            if (this.element.getProperty("showform").equalsIgnoreCase(CONSTANT_Y)) {
                sbHtml.append(this.getAdvancedToolbarForm());
            }
            if (this.element.getProperty("showacbiframe").equalsIgnoreCase(CONSTANT_Y)) {
                sbHtml.append(this.getACBIframe());
            }
        }
        String pagemode = this.pageContext.getRequest().getParameter("mode");
        sbHtml.append("<script type=\"text/javascript\">\n");
        sbHtml.append("var __cmtToolbarPageType = '';");
        if (this.__SdcId != null && this.__SdcId.length() > 0) {
            String sdcid = this.__SdcId;
            if (this.__SdcId.contains("-")) {
                sdcid = StringUtil.replaceAll(this.__SdcId, "-", "hyphen");
            }
            sbHtml.append("var __cmtSDCChangeControlled_").append(sdcid).append(" = ").append(changeControlEnabled).append(";");
            sbHtml.append("var __cmtToolbarPageMode = '").append(pagemode != null ? SafeHTML.encodeForJavaScript(pagemode) : "").append("';");
            sbHtml.append("var __cmtToolbarUUID = '").append(UUID.randomUUID().toString()).append("';");
            sbHtml.append("var __cmtToolbarSDCID = '").append(this.__SdcId).append("';");
            sbHtml.append("var __cmtToolbarSDCKeyCount = '").append(this.getSDCProcessor().getProperty(this.__SdcId, "keycolumns")).append("';");
            sbHtml.append("var __cmtToolbarSDCChangeControlFlag = '").append(sdcChangeControlledFlag).append("';");
            sbHtml.append("var __cmtButtonOperation = '';");
            sbHtml.append("if (_lvtop.sapphire.cmt !== undefined) {");
            sbHtml.append("_lvtop.sapphire.cmt.setAutoPopulateChangeRequest(").append(cmtPolicy == null || cmtPolicy.isAutoPopulateChangeRequest()).append(");");
            sbHtml.append("}");
        }
        sbHtml.append("function closeThisDialog(){");
        sbHtml.append("sapphire.ui.dialog.close( this.dialogNumber );");
        sbHtml.append("}\n");
        if (this.toolbarType == ToolbarType.modern && this.allOpsDataSet != null && this.allOpsDataSet.getRowCount() > 0) {
            StringBuilder finalscript = new StringBuilder();
            sbHtml.append("function lv_executeOperation_").append(this.elementid).append("( operationid, intarget, inembeddedinlist, bypassCMT ) {");
            sbHtml.append(" __cmtButtonOperation = operationid;\n");
            sbHtml.append(" if ( bypassCMT == undefined ) bypassCMT = false;\n");
            sbHtml.append(" if ( typeof( 'inembeddedinlist' ) != 'undefined' ){ embeddedinlist = inembeddedinlist; }else{ embeddedinlist='N';}\n");
            sbHtml.append(" if ( typeof( 'embeddedinlist' ) != 'undefined' && embeddedinlist=='Y' ){ editset = 't_multisdimode=t_EditSet'; } else { editset = 'multisdimode=EditSet'; }\n");
            for (int i = 0; i < this.allOpsDataSet.getRowCount(); ++i) {
                String operationfinalscript;
                String id = this.allOpsDataSet.getValue(i, "operationid");
                String buttonScript = StringUtil.replaceAll(this.allOpsDataSet.getValue(i, "operationscript"), "&#034;", "\"");
                if (buttonScript.contains("\n")) {
                    buttonScript = StringUtil.replaceAll(StringUtil.replaceAll(buttonScript, "\r", ""), "\n", ";");
                }
                if (buttonScript.contains("\r")) {
                    buttonScript = StringUtil.replaceAll(buttonScript, "\r", ";");
                }
                String defaultTarget = "'_self'";
                if (buttonScript.indexOf("'_parent'") > 0) {
                    defaultTarget = "'_parent'";
                } else if (buttonScript.indexOf("'_blank'") > 0) {
                    defaultTarget = "'_blank'";
                } else if (buttonScript.indexOf("'_top'") > 0) {
                    defaultTarget = "'_top'";
                }
                buttonScript = StringUtil.replaceAll(buttonScript, defaultTarget, "target");
                buttonScript = StringUtil.replaceAll(buttonScript, "embeddedinlist=N'", "embeddedinlist=' + embeddedinlist");
                buttonScript = StringUtil.replaceAll(buttonScript, "multisdimode=EditSet", "' + editset+'");
                sbHtml.append("if ( '").append(id).append("' == operationid ) {").append("sapphire.page.clickedButtonId='").append(id).append("';");
                sbHtml.append("target=").append(defaultTarget).append(";");
                sbHtml.append("if( typeof( intarget ) != 'undefined' ) {target = intarget;}");
                sbHtml.append(buttonScript).append("}\n");
                String operationid = this.allOpsDataSet.getValue(i, "operationid");
                operationid = StringUtil.replaceAll(operationid, " ", "_");
                operationid = StringUtil.replaceAll(operationid, "&", "AND");
                if (!this.isSDCChangeControlled || !OpalUtil.isNotEmpty(operationfinalscript = this.allOpsDataSet.getValue(i, "operationfinalscript", buttonScript)) || !operationfinalscript.contains("(")) continue;
                operationfinalscript = operationfinalscript.substring(0, operationfinalscript.indexOf("("));
                finalscript.append("\nvar button_").append(operationid).append("_function=\"").append(operationfinalscript).append("\";");
                finalscript.append("\nvar button_").append(operationid).append("_checkout=\"").append(this.allOpsDataSet.getValue(i, "checkoutrequired", CONSTANT_N)).append("\";");
            }
            sbHtml.append("}");
            sbHtml.append(finalscript.toString());
            HashMap<String, String> groupfilter = new HashMap<String, String>();
            groupfilter.put("dropdowngroup", this.translationProcessor.translate("Navigators"));
            DataSet navigatorGroupDs = this.allOpsDataSet.getFilteredDataSet(groupfilter);
            if (navigatorGroupDs != null && navigatorGroupDs.getRowCount() == 1) {
                navigatorGroupDs.setValue(0, "dropdowngroup", "");
            }
            sbHtml.append("\nvar OperationsDS_").append(this.elementid).append(" = ").append(this.allOpsDataSet.toJSONString(true, new String[]{"operationid", "operationtype", "text", "hotkeytext", "group", "inline", "dropdowngroup", "tip", "image", "applytoset", "showindetailpanel"}, true, true, true)).append(";\n");
            sbHtml.append("\nvar buttonStyle='").append(buttonStyle).append("';");
            sbHtml.append("\nsapphire.gwt.addGWTElement( \"moderntoolbar\", \"").append(this.elementid).append("\", {} );\n");
        }
        sbHtml.append("\n</script>\n");
        return sbHtml.toString();
    }

    private boolean showButton(PropertyList button) {
        String showbuttonlist;
        boolean hasImage;
        boolean hasText;
        String show = button.getPropertyList("commonprops").getProperty("show");
        String text = button.getPropertyList("commonprops").getProperty("text");
        String image = button.getPropertyList("commonprops").getProperty("image");
        if (button.getProperty("buttontype").equalsIgnoreCase(BUTTON_TYPE_TASK)) {
            hasText = true;
            hasImage = true;
        } else {
            hasText = text.length() > 0 && !text.equals(this.__DefaultButtonText);
            hasImage = image.length() > 0 && !image.equals(this.__DefaultButtonImage);
        }
        String string = showbuttonlist = this.pageContext.getRequest().getParameter("showbuttonlist") != null ? this.pageContext.getRequest().getParameter("showbuttonlist") : "";
        if ("advancedtoolbar".equals(this.elementid) && showbuttonlist.length() > 0 && (";" + showbuttonlist + ";").indexOf(";" + button.getProperty("id") + ";") < 0) {
            button.getPropertyList("commonprops").setProperty("show", CONSTANT_N);
        } else if (show.indexOf("$G{") == 0 || text.indexOf("$G{") == 0) {
            HashMap<String, Object> bindMap = new HashMap<String, Object>();
            bindMap.put("element", this.element);
            bindMap.put("elements", this.requestContext.getPropertyList().getPropertyList("elements"));
            bindMap.put("pagedata", this.requestContext.getPropertyList().getPropertyList("pagedata"));
            bindMap.put("user", this.connectionInfo.getUserAttributeMap());
            bindMap.put("policy", new GroovyPolicyUtil(this.pageContext));
            if (this.element.getProperty("sdcid").length() > 0) {
                bindMap.put("sdc", this.getSDCProcessor().getSDCProperties(this.element.getProperty("sdcid")));
            }
            try {
                if (show.indexOf("$G{") == 0) {
                    show = GroovyUtil.getInstance(this.pageContext).evaluateSecure(show, bindMap);
                }
                if (show != null && show.equalsIgnoreCase("true")) {
                    show = CONSTANT_Y;
                }
                if (text.indexOf("$G{") == 0) {
                    text = GroovyUtil.getInstance(this.pageContext).evaluateSecure(text, bindMap);
                }
                button.getPropertyList("commonprops").setProperty("show", show);
                button.getPropertyList("commonprops").setProperty("text", text);
            }
            catch (SapphireException se) {
                Logger.logError(se.getMessage());
            }
        }
        return !(show.length() != 0 && !show.equalsIgnoreCase(CONSTANT_Y) || !hasText && !hasImage);
    }

    private String getButtonsHtml(String buttonAlignment, String buttonSpacing, int noOfButtonsInOneLine, ArrayList buttons) {
        StringBuffer sbHtml = new StringBuffer();
        boolean buttonsPerLinePropertyFlag = false;
        if (buttonAlignment.equalsIgnoreCase(CONSTANT_HORIZONTAL) && noOfButtonsInOneLine > 0) {
            buttonsPerLinePropertyFlag = true;
        }
        sbHtml.append("\n<table cellspacing=0 cellpadding=0 id=\"buttontoolbar\" class=\"buttontoolbar\">");
        if (buttonAlignment.equalsIgnoreCase(CONSTANT_HORIZONTAL) && !buttonsPerLinePropertyFlag) {
            sbHtml.append("\n<tr><td><dl style=\"display:inline; padding-left: 11px;\">");
        }
        for (int i = 0; i < buttons.size(); ++i) {
            HashMap hmButton = (HashMap)buttons.get(i);
            String hotKeyText = (String)hmButton.get("hotkeytext");
            String buttonText = (String)hmButton.get("text");
            String buttonImg = (String)hmButton.get("image");
            String buttonAction = (String)hmButton.get("action");
            Button button = new Button(this.pageContext);
            button.setModern(this.toolbarType == ToolbarType.modern);
            button.setText(buttonText);
            button.setImg(buttonImg);
            button.setWidth((String)hmButton.get("width"));
            button.setAppearance((String)hmButton.get("appearance"));
            button.setTip(hmButton.get("tip") + " " + hotKeyText);
            button.setId((String)hmButton.get("id"));
            button.setMargin((String)hmButton.get("margin"));
            button.setStyle(hmButton.get("style") + "; margin-bottom: 2px; margin-right: 20px");
            button.setHighlight((String)hmButton.get("highlight"));
            String releaseLockFunction = "";
            String releaseLockFlag = (String)hmButton.get("releaselockflag");
            if (releaseLockFlag != null) {
                releaseLockFunction = this.getReleseLockFunction(releaseLockFlag);
            }
            button.setAction(releaseLockFunction + buttonAction);
            if (buttonsPerLinePropertyFlag) {
                if (i % noOfButtonsInOneLine == 0) {
                    if (i > 0) {
                        sbHtml.append("</tr></table></td></tr>");
                    }
                    sbHtml.append("<tr><td><table cellspacing=0 cellpadding=0 class=\"buttontoolbar\"><tr><td><dl style=\"display:inline; padding-left: 11px;\">");
                } else {
                    sbHtml.append("<td><dl style=\"display:inline; padding-left: 11px;\">");
                }
            }
            if (buttonSpacing.equals("1")) {
                sbHtml.append("<li style=\"display: inline;\">").append(button.getHtml()).append("</li>");
            } else {
                sbHtml.append("<li style=\"display: inline; margin-right:").append(buttonSpacing).append("px\">").append(button.getHtml()).append("</li>");
            }
            if (!buttonsPerLinePropertyFlag) continue;
            sbHtml.append("</dl></td>");
        }
        if (buttonAlignment.equalsIgnoreCase(CONSTANT_HORIZONTAL)) {
            if (buttonsPerLinePropertyFlag) {
                sbHtml.append("</tr></table></td></tr>");
            } else {
                sbHtml.append("</dl></td></tr>");
            }
        }
        sbHtml.append("\n</table>\n");
        sbHtml.append("<script>\n");
        sbHtml.append("if ( typeof(__deferredCallbacks) == 'undefined'){");
        sbHtml.append("var __deferredCallbacks=[];");
        sbHtml.append("}");
        sbHtml.append("\n</script>\n");
        if (this.__DeferredCallbackScript.length() > 0) {
            sbHtml.append("<script id=\"__dcb_button\">").append(this.__DeferredCallbackScript).append("\n</script>\n");
        }
        return sbHtml.toString();
    }

    private String getFullImage(String imagepath, boolean large) {
        if (this.browser.isIE() && this.browser.getVersion() < 7.0) {
            return imagepath;
        }
        String newpath = "";
        if (!large) {
            if (imagepath.contains("WEB-CORE/images/") && imagepath.contains("/gif/") && imagepath.endsWith(".gif") && !new File(newpath = StringUtil.replaceAll(StringUtil.replaceAll(imagepath, "/gif/", "/png/"), ".gif", ".png")).exists()) {
                newpath = "";
            }
        } else if (imagepath.contains("WEB-CORE/images/")) {
            if (imagepath.contains("/gif/") && imagepath.endsWith(".gif")) {
                newpath = StringUtil.replaceAll(StringUtil.replaceAll(imagepath, "/gif/", "/png32/"), ".gif", ".png");
                if (!new File(newpath).exists() && !new File(newpath = StringUtil.replaceAll(StringUtil.replaceAll(imagepath, "/gif/", "/png/"), ".gif", ".png")).exists()) {
                    newpath = "";
                }
            } else if (imagepath.contains("/png/") && imagepath.endsWith(".png") && !new File(newpath = StringUtil.replaceAll(imagepath, "/png/", "/png32/")).exists()) {
                newpath = "";
            }
        }
        if (newpath.length() == 0) {
            newpath = imagepath;
        }
        return newpath;
    }

    public ToolbarType getToolbarType() {
        return this.toolbarType;
    }

    private String getModernHtml(ArrayList buttons) {
        boolean isDetailToolbar;
        boolean notMainLayout;
        if (this.elementid == null || this.elementid.length() == 0) {
            this.elementid = "moderntoolbar";
        }
        try {
            buttonStyle = this.getConfigurationProcessor().getPolicy("GUIPolicy", "Sapphire Custom").getProperty("buttonstyle");
        }
        catch (Exception e) {
            buttonStyle = "Text Only";
        }
        this.allOpsDataSet = (DataSet)this.pageContext.getAttribute(this.elementid + "_allOpsDataSet");
        if (this.allOpsDataSet == null) {
            this.allOpsDataSet = new DataSet();
            this.allOpsDataSet.addColumn("operationid", 0);
            this.allOpsDataSet.addColumn("text", 0);
            this.allOpsDataSet.addColumn("hotkeytext", 0);
            this.allOpsDataSet.addColumn("operationscript", 0);
            this.allOpsDataSet.addColumn("operationfinalscript", 0);
            this.allOpsDataSet.addColumn("checkoutrequired", 0);
            this.allOpsDataSet.addColumn("operationtype", 0);
            this.allOpsDataSet.addColumn("group", 0);
            this.allOpsDataSet.addColumn("inline", 0);
            this.allOpsDataSet.addColumn("dropdowngroup", 0);
            this.allOpsDataSet.addColumn("tip", 0);
            this.allOpsDataSet.addColumn("image", 0);
            this.allOpsDataSet.addColumn("applytoset", 0);
            this.allOpsDataSet.addColumn("showindetailpanel", 0);
            this.allOpsDataSet.addColumn("checkoutoption", 0);
            this.pageContext.setAttribute(this.elementid + "_allOpsDataSet", (Object)this.allOpsDataSet);
        }
        StringBuffer sbHtml = new StringBuffer();
        sbHtml.append("<style>\n.layout_pagebuttons\t{ background-image: none; background-color: white; }\n.pagebuttonsection \t{ background-image: none; background-color: white; }\n</style>");
        sbHtml.append("<table id=\"" + this.elementid + "_buttontoolbar\"><tr>");
        sbHtml.append("<td><div id=\"ribbon_pagetitle\" style=\"white-space:nowrap\" class=\"toolbar_pagetitle\">");
        PropertyList plLayout = (PropertyList)this.pageContext.getAttribute("layout", 2);
        String layoutObjectName = plLayout == null ? "" : plLayout.getProperty("objectname");
        boolean bl = notMainLayout = "navigator".equals(this.pageContext.getRequest().getParameter("layout")) || layoutObjectName.indexOf("blank.jsp") > 0 || layoutObjectName.indexOf("popuplayout.jsp") > 0;
        if (notMainLayout) {
            sbHtml.append(this.pagetitle);
        }
        sbHtml.append("</div></td>");
        sbHtml.append("<td><div class=\"toolbar_pagebuttons\" id=\"" + this.elementid + "\"></div></td>");
        if (this.customGroupText != null && this.customGroupText.length() > 0) {
            if (this.customGroupText.length() > 0) {
                sbHtml.append("<td><table id=\"toolbar_customGroupText\"><tr><td>" + this.getTranslationProcessor().translate(this.customGroupText) + "</td>");
            }
            sbHtml.append("<td nowrap>" + this.customGroupContent + "</td></tr></table></td>");
        }
        if (notMainLayout && "advancedtoolbar".equals(this.elementid)) {
            sbHtml.append(HttpUtil.getEvergreenLink(this.pageContext));
        }
        sbHtml.append("</tr></table>");
        boolean bl2 = isDetailToolbar = this.pageContext.getAttribute("isDetailElementTBPresent") != null;
        if (buttons != null && buttons.size() > 0) {
            for (int i = 0; i < buttons.size(); ++i) {
                HashMap hmButton = (HashMap)buttons.get(i);
                String buttonid = (String)hmButton.get("id");
                String mode = (String)hmButton.get("mode");
                String hotKeyText = (String)hmButton.get("hotkeytext");
                String buttonText = (String)hmButton.get("text");
                String operationScript = (String)hmButton.get("action");
                String buttonFinalScript = (String)hmButton.get("buttonfinalscript");
                String checkoutrequired = (String)hmButton.get("checkoutrequired");
                String tip = hmButton.get("tip") + " " + hotKeyText;
                String releaseLockFlag = (String)hmButton.get("releaselockflag");
                String buttonImg = (String)hmButton.get("image");
                String imageflat = (String)hmButton.get("imageflat");
                if (imageflat != null && imageflat.length() > 0) {
                    buttonImg = imageflat;
                }
                String imgLarge = (String)hmButton.get("imagelarge");
                if ((buttonImg == null || buttonImg.length() == 0) && imgLarge != null && imgLarge.length() > 0) {
                    buttonImg = imgLarge;
                }
                String applytoset = (String)hmButton.get("applytoset");
                String group = hmButton.get("group").toString();
                if (group == null || group.length() == 0) {
                    group = isDetailToolbar ? buttonid : this.translationProcessor.translate("Miscellaneous");
                }
                int row = this.allOpsDataSet.addRow();
                this.allOpsDataSet.setValue(row, "operationid", buttonid);
                this.allOpsDataSet.setValue(row, "operationscript", operationScript);
                this.allOpsDataSet.setValue(row, "operationfinalscript", buttonFinalScript);
                this.allOpsDataSet.setValue(row, "checkoutrequired", checkoutrequired);
                this.allOpsDataSet.setValue(row, "group", group);
                this.allOpsDataSet.setValue(row, "operationtype", "button");
                this.allOpsDataSet.setValue(row, "inline", (String)hmButton.get("inline"));
                String dropdowngroup = (String)hmButton.get("dropdowngroup");
                if (dropdowngroup == null || dropdowngroup.length() == 0) {
                    String string = dropdowngroup = CONSTANT_OPERATION.equals(mode) ? this.translationProcessor.translate("Other Tasks") : "";
                    if (CONSTANT_OPERATION.equals(mode)) {
                        this.allOpsDataSet.setValue(row, "group", dropdowngroup);
                        this.allOpsDataSet.setValue(row, "operationtype", "othertasks");
                    }
                }
                if (dropdowngroup.length() == 0 && "Small".equals(hmButton.get("ribbonstyle"))) {
                    dropdowngroup = group;
                }
                this.allOpsDataSet.setValue(row, "text", buttonText);
                this.allOpsDataSet.setValue(row, "dropdowngroup", dropdowngroup);
                this.allOpsDataSet.setValue(row, "hotkeytext", hotKeyText);
                this.allOpsDataSet.setValue(row, "tip", tip);
                this.allOpsDataSet.setValue(row, "image", StringUtil.replaceAll(buttonImg, "\\", "/"));
                this.allOpsDataSet.setValue(row, "applytoset", applytoset);
                String showindetailpanel = (String)hmButton.get("showindetailpanel");
                this.allOpsDataSet.setValue(row, "showindetailpanel", showindetailpanel);
                this.allOpsDataSet.setValue(row, "checkoutoption", (String)hmButton.get("checkoutoption"));
            }
        }
        if (this.browser != null && !this.browser.isPhone()) {
            PropertyListCollection ngborderbars;
            PropertyListCollection propertyListCollection = ngborderbars = plLayout != null ? plLayout.getCollection("ngborderbars") : null;
            if (ngborderbars != null && ngborderbars.size() > 0) {
                for (int i = 0; i < ngborderbars.size(); ++i) {
                    PropertyList ngPL = ngborderbars.getPropertyList(i);
                    String show = ngPL.getProperty("show");
                    String title = ngPL.getProperty("title");
                    String js = ngPL.getProperty("js");
                    String valiScript = ngPL.getProperty("validationscript");
                    if (CONSTANT_N.equals(show)) continue;
                    if (valiScript.length() > 0) {
                        js = "sapphire.navigator.validate( '" + valiScript.replaceAll("'", "\\\\'") + "','" + js.replaceAll("'", "\\\\'") + "')";
                    }
                    int row = this.allOpsDataSet.addRow();
                    this.allOpsDataSet.setValue(row, "operationid", title);
                    this.allOpsDataSet.setValue(row, "text", title);
                    this.allOpsDataSet.setValue(row, "group", "Navigator");
                    this.allOpsDataSet.setValue(row, "operationtype", "Navigator");
                    this.allOpsDataSet.setValue(row, "operationscript", js);
                    this.allOpsDataSet.setValue(row, "dropdowngroup", this.translationProcessor.translate("Navigators"));
                }
            }
        }
        if (this.browser != null && !this.browser.isPhone() && "advancedtoolbar".equals(this.elementid)) {
            ArrayList lists;
            ArrayList arrayList = lists = plLayout != null && plLayout.getPropertyList("sidebar") != null ? plLayout.getPropertyList("sidebar").getCollection("lists") : null;
            if (lists != null && lists.size() > 0) {
                for (int i = 0; i < lists.size(); ++i) {
                    PropertyListCollection items;
                    PropertyList detailsPL = ((PropertyListCollection)lists).getPropertyList(i);
                    if (!BUTTON_TYPE_USER.equals(detailsPL.getProperty("type")) || !CONSTANT_Y.equals(detailsPL.getProperty("showlist"))) continue;
                    String dropdowngroupid = detailsPL.getProperty("text");
                    if ("details".equals(detailsPL.getProperty("id")) && CONSTANT_Y.equals(this.requestContext.getProperty("ViewOnly")) || (items = detailsPL.getCollection("items")) == null) continue;
                    for (int d = 0; d < items.size(); ++d) {
                        String link;
                        PropertyList itemPL = items.getPropertyList(d);
                        if (CONSTANT_N.equals(itemPL.getProperty("showitem")) || (link = itemPL.getProperty("link")).length() <= 11) continue;
                        String js = link.substring(11);
                        String operationid = itemPL.getProperty("id");
                        String text = itemPL.getProperty("text");
                        int row = this.allOpsDataSet.addRow();
                        this.allOpsDataSet.setValue(row, "operationid", operationid);
                        this.allOpsDataSet.setValue(row, "text", text);
                        this.allOpsDataSet.setValue(row, "group", dropdowngroupid);
                        this.allOpsDataSet.setValue(row, "operationtype", "Detail");
                        this.allOpsDataSet.setValue(row, "operationscript", js);
                        this.allOpsDataSet.setValue(row, "dropdowngroup", dropdowngroupid);
                    }
                }
            }
        }
        sbHtml.append("<script>\n");
        sbHtml.append("if ( typeof(__deferredCallbacks) == 'undefined'){");
        sbHtml.append("var __deferredCallbacks=[];");
        sbHtml.append("}");
        sbHtml.append("\n</script>\n");
        if (this.__DeferredCallbackScript.length() > 0) {
            sbHtml.append("<script id=\"__dcb_button\">").append(this.__DeferredCallbackScript).append("\n</script>\n");
        }
        return sbHtml.toString();
    }

    private String getRibbonHtml(ArrayList buttons, ToolbarType toolbarType) {
        StringBuffer sbHtml = new StringBuffer();
        int rowheight = toolbarType.getHeight();
        int buttonsPerRow = toolbarType.buttonsPerRow();
        String imagebackend = toolbarType == ToolbarType.compactRibbon ? "_sm" : (toolbarType == ToolbarType.hybridRibbon ? "_hb" : "");
        sbHtml.append("<table cellspacing=\"0\" cellpadding=\"0\" id=\"buttontoolbar\" class=\"ribbon_toolbar\" style=\"height:").append(rowheight).append("px;\" border=\"0\">");
        sbHtml.append("<tbody>");
        sbHtml.append("<tr style=\"height:").append(rowheight).append("px;\">");
        sbHtml.append("<td style=\"width:2px;\">");
        sbHtml.append("</td>");
        if (this.pagetitle != null && this.pagetitle.length() > 0) {
            sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(toolbarType == ToolbarType.ribbon ? "_ng" : imagebackend).append(" ribbon_toolbar_start\">");
            sbHtml.append("</td>");
            sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(toolbarType == ToolbarType.ribbon ? "_ng" : imagebackend).append(" ribbon_toolbar_center\" style=\"width:125px;padding-right:8px;\">");
            sbHtml.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:100%;\"><tbody>");
            sbHtml.append("<tr style=\"height:").append(rowheight - 20).append("px\"><td class=\"ribbon_pagetitle\" id=\"ribbon_pagetitle\"").append(toolbarType == ToolbarType.hybridRibbon ? " nowrap" : "").append(">");
            if (toolbarType == ToolbarType.hybridRibbon) {
                this.pagetitle = StringUtil.replaceAll(this.pagetitle, "<br/>", " ");
            }
            sbHtml.append(this.pagetitle);
            sbHtml.append("</td></tr>");
            sbHtml.append("</tbody></table>");
            sbHtml.append("</td>");
            if (buttons != null && buttons.size() > 0) {
                sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(toolbarType == ToolbarType.ribbon ? "_ng" : imagebackend).append(" ribbon_toolbar_endgroup\">");
                sbHtml.append("</td>");
            } else {
                sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(toolbarType == ToolbarType.ribbon ? "_ng" : imagebackend).append(" ribbon_toolbar_endlastgroup\">");
                sbHtml.append("</td>");
            }
        } else if (buttons != null && buttons.size() > 0) {
            sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_start\">");
            sbHtml.append("</td>");
        }
        if (buttons != null && buttons.size() > 0) {
            int i;
            HashMap grouped = new HashMap();
            ArrayList<String> groups = new ArrayList<String>();
            for (i = 0; i < buttons.size(); ++i) {
                String group;
                HashMap button = (HashMap)buttons.get(i);
                String string = group = button.containsKey("group") ? button.get("group").toString() : this.translationProcessor.translate("Miscellaneous");
                if (!grouped.containsKey(group)) {
                    grouped.put(group, new ArrayList());
                }
                ArrayList cur = (ArrayList)grouped.get(group);
                cur.add(button);
                if (groups.contains(group)) continue;
                groups.add(group);
            }
            buttons = new ArrayList();
            for (i = 0; i < groups.size(); ++i) {
                if (toolbarType != ToolbarType.hybridRibbon) {
                    Collections.sort((ArrayList)grouped.get(groups.get(i)), new Comparator<HashMap>(){

                        @Override
                        public int compare(HashMap h1, HashMap h2) {
                            boolean small2;
                            boolean small1 = h1.containsKey("ribbonstyle") && ((String)h1.get("ribbonstyle")).equalsIgnoreCase("small");
                            boolean bl = small2 = h2.containsKey("ribbonstyle") && ((String)h2.get("ribbonstyle")).equalsIgnoreCase("small");
                            if (small1 && small2 || !small1 && !small2) {
                                return 0;
                            }
                            if (small1 && !small2) {
                                return 1;
                            }
                            return -1;
                        }
                    });
                }
                buttons.addAll((ArrayList)grouped.get(groups.get(i)));
            }
            String prevGroup = null;
            int countGroup = 0;
            StringBuffer groupstyle = new StringBuffer("<style id=\"toolbar_group_style\">");
            int countSmall = 0;
            for (int i2 = 0; i2 < buttons.size(); ++i2) {
                int cs;
                HashMap hmButton = (HashMap)buttons.get(i2);
                String hotKeyText = (String)hmButton.get("hotkeytext");
                String buttonText = (String)hmButton.get("text");
                String ribbonStyle = toolbarType == ToolbarType.hybridRibbon ? "small" : (hmButton.containsKey("ribbonstyle") ? (String)hmButton.get("ribbonstyle") : "");
                String buttonImg = (String)hmButton.get("image");
                String imgLarge = (String)hmButton.get("imagelarge");
                String buttonAction = (String)hmButton.get("action");
                if (ribbonStyle.equalsIgnoreCase("small")) {
                    ribbonStyle = "ribbonsmall";
                } else {
                    ribbonStyle = "ribbonlarge";
                    buttonImg = imgLarge;
                }
                Button button = new Button(this.pageContext);
                button.setModern(toolbarType == ToolbarType.modern);
                if (!ribbonStyle.equalsIgnoreCase("ribbonlarge") || toolbarType != ToolbarType.compactRibbon) {
                    button.setText(buttonText);
                }
                button.setImg(buttonImg);
                button.setAppearance(ribbonStyle);
                button.setTip(hmButton.get("tip") + " " + hotKeyText);
                button.setId((String)hmButton.get("id"));
                button.setHighlight("true");
                String releaseLockFunction = "";
                String releaseLockFlag = (String)hmButton.get("releaselockflag");
                if (releaseLockFlag != null) {
                    releaseLockFunction = this.getReleseLockFunction(releaseLockFlag);
                }
                button.setAction(releaseLockFunction + buttonAction);
                String group = hmButton.get("group").toString();
                if (group == null || group.length() == 0) {
                    group = this.translationProcessor.translate("Miscellaneous");
                }
                String repGroup = StringUtil.replaceAll(group, " ", "").toLowerCase();
                if (prevGroup == null || !prevGroup.equalsIgnoreCase(group)) {
                    if (prevGroup != null) {
                        if (countSmall > 0) {
                            for (cs = countSmall; cs < buttonsPerRow; ++cs) {
                                sbHtml.append("<tr style=\"height:22px\"><td></td></tr>");
                            }
                            countSmall = 0;
                            sbHtml.append("</tbody></table>");
                            sbHtml.append("</td>");
                        }
                        sbHtml.append("</tr>");
                        if (toolbarType == ToolbarType.ribbon) {
                            sbHtml.append("<tr><td valign=\"center\" nowrap class=\"ribbon_grouptitle\" colspan=\"").append(countGroup).append("\"").append(this.browser.isIE() && this.browser.getVersion() > 8.0 ? " style=\"padding-bottom:2px;\"" : "").append(">");
                            sbHtml.append(this.getTranslationProcessor().translate(prevGroup));
                            sbHtml.append("</td></tr>");
                        }
                        sbHtml.append("</tbody></table>");
                        sbHtml.append("</td>");
                        sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_endgroup ").append("ribbon_group_").append(repGroup).append("\">");
                        sbHtml.append("</td>");
                        countGroup = 0;
                    }
                    sbHtml.append("<td valign=\"top\" class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_center ").append("ribbon_group_").append(repGroup).append("\" style=\"width:40px;\">");
                    sbHtml.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:100%;height:100%;\"><tbody>");
                    sbHtml.append("<tr style=\"height:").append(rowheight - 20).append("px\">");
                    groupstyle.append("\n.ribbon_group_").append(repGroup).append("{display:table-cell;}");
                }
                ++countGroup;
                if (ribbonStyle.equalsIgnoreCase("ribbonsmall")) {
                    if (countSmall == 0) {
                        sbHtml.append("<td class=\"ribbon_innercell\" style=\"padding-top:").append(toolbarType == ToolbarType.ribbon ? "5" : "0").append("px;\">");
                        sbHtml.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"").append(toolbarType != ToolbarType.ribbon ? "" : "height:100%;").append("width:100%;\"><tbody>");
                    }
                    sbHtml.append("<tr style=\"height:22px\">");
                    sbHtml.append("<td class=\"ribbon_innercell\">");
                    ++countSmall;
                } else {
                    if (countSmall > 0) {
                        for (cs = countSmall; cs < buttonsPerRow; ++cs) {
                            sbHtml.append("<tr style=\"height:22px\"><td></td></tr>");
                        }
                        countSmall = 0;
                        sbHtml.append("</tbody></table>");
                        sbHtml.append("</td>");
                    }
                    sbHtml.append("<td class=\"ribbon_innercell\" style=\"padding-top:").append(toolbarType == ToolbarType.ribbon ? "5" : "0").append("px;\">");
                }
                sbHtml.append(button.getHtml());
                if (ribbonStyle.equalsIgnoreCase("ribbonsmall")) {
                    sbHtml.append("</td>");
                    sbHtml.append("</tr>");
                    if (countSmall == buttonsPerRow) {
                        sbHtml.append("</tbody></table>");
                        sbHtml.append("</td>");
                        countSmall = 0;
                    }
                } else {
                    sbHtml.append("</td>");
                }
                prevGroup = group;
            }
            if (countSmall > 0) {
                for (int cs = countSmall; cs < buttonsPerRow; ++cs) {
                    sbHtml.append("<tr style=\"height:22px\"><td></td></tr>");
                }
                sbHtml.append("</tbody></table>");
                sbHtml.append("</td>");
            }
            sbHtml.append("</tr>");
            if (toolbarType == ToolbarType.ribbon) {
                sbHtml.append("<tr><td align=\"center\" valign=\"center\" nowrap class=\"ribbon_grouptitle\" colspan=\"").append(countGroup).append("\"").append(this.browser.isIE() && this.browser.getVersion() > 8.0 ? " style=\"padding-bottom:2px;\"" : "").append(">");
                sbHtml.append(this.getTranslationProcessor().translate(prevGroup));
                sbHtml.append("</td></tr>");
            }
            sbHtml.append("</tbody></table>");
            groupstyle.append("</style>");
            sbHtml.append(groupstyle);
            sbHtml.append("</td>");
            if (this.customGroupText != null && this.customGroupText.length() > 0) {
                sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_endgroup\">");
                sbHtml.append("</td>");
                sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_center\" style=\"width:125px;padding-right:8px;\">");
                sbHtml.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:100%;\"><tbody>");
                sbHtml.append("<tr style=\"height:").append(rowheight - 20).append("px\"><td class=\"ribbon_pagetitle\" id=\"ribbon_innercell\" style=\"\">");
                sbHtml.append(this.customGroupContent != null && this.customGroupContent.length() > 0 ? this.customGroupContent : "&nbsp;");
                sbHtml.append("</td></tr>");
                if (toolbarType == ToolbarType.ribbon) {
                    sbHtml.append("<tr><td nowrap class=\"ribbon_grouptitle\">");
                    sbHtml.append(this.getTranslationProcessor().translate(this.customGroupText));
                    sbHtml.append("</td></tr>");
                }
                sbHtml.append("</tbody></table>");
                sbHtml.append("</td>");
            }
            sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_endlastgroup\">");
            sbHtml.append("</td>");
        }
        sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_filler\">");
        sbHtml.append("&nbsp;");
        sbHtml.append("</td>");
        sbHtml.append("<td class=\"ribbon_toolbar_imageback").append(imagebackend).append(" ribbon_toolbar_end\">");
        sbHtml.append("</td>");
        sbHtml.append("</tr>");
        sbHtml.append("</tbody>");
        sbHtml.append("</table>");
        sbHtml.append("<script>\n");
        sbHtml.append("if ( typeof(__deferredCallbacks) == 'undefined'){");
        sbHtml.append("var __deferredCallbacks=[];");
        sbHtml.append("}");
        sbHtml.append("\n</script>\n");
        if (this.__DeferredCallbackScript.length() > 0) {
            sbHtml.append("<script id=\"__dcb_button\">").append(this.__DeferredCallbackScript).append("\n</script>\n");
        }
        return sbHtml.toString();
    }

    private String getSidebarHtml(ArrayList buttons) {
        int sideBarWidth;
        String type;
        StringBuffer sbHtml = new StringBuffer();
        String releaseLockFunction = "";
        PropertyList plLayout = (PropertyList)this.pageContext.getAttribute("layout", 2);
        PropertyListCollection items = new PropertyListCollection();
        for (int i = 0; i < buttons.size(); ++i) {
            PropertyList operation = new PropertyList();
            items.add(operation);
            HashMap hmButton = (HashMap)buttons.get(i);
            operation.setProperty("id", (String)hmButton.get("id"));
            operation.setProperty("text", (String)hmButton.get("text"));
            operation.setProperty("action", releaseLockFunction + hmButton.get("action") + "; return false;");
        }
        String title = type = "Operations";
        String sideBarListId = null;
        PropertyList sidebar = plLayout.getPropertyList("sidebar");
        if (sidebar != null) {
            String sidebarwidth = sidebar.getProperty("sidebarwidth", "150");
            try {
                sideBarWidth = Integer.parseInt(sidebarwidth);
            }
            catch (Exception e) {
                sideBarWidth = 150;
            }
            PropertyListCollection plList = sidebar.getCollection("lists");
            if (plList != null) {
                for (int i = 0; i < plList.size(); ++i) {
                    PropertyList list = plList.getPropertyList(i);
                    if (!type.equals(list.getProperty("type"))) continue;
                    title = list.getProperty("text", type);
                    sideBarListId = list.getProperty("id");
                }
            }
        } else {
            sideBarWidth = 150;
        }
        RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        String pagename = requestContext.getProperty("page");
        this.logger.debug("pagename = " + pagename);
        sbHtml.append(LayoutUtil.getSidebarItem(plLayout, type, "", items, "", sideBarWidth, "No operations", this.translationProcessor, userConfig, pagename, title, sideBarListId));
        sbHtml.append("<script>\n");
        sbHtml.append("if ( typeof(__deferredCallbacks) == 'undefined'){");
        sbHtml.append("var __deferredCallbacks=[];");
        sbHtml.append("}");
        sbHtml.append("\n</script>\n");
        if (this.__DeferredCallbackScript.length() > 0) {
            sbHtml.append("<script id=\"__dcb_sidebar\">").append(this.__DeferredCallbackScript).append("\n</script>\n");
        }
        return sbHtml.toString();
    }

    private HashMap getCommonProps(PropertyList plButton) {
        PropertyList plCommonProps = plButton.getPropertyList("commonprops");
        HashMap<String, String> hmProps = new HashMap<String, String>();
        String buttonImage = plCommonProps.getProperty("image");
        String imageflat = plCommonProps.getProperty("imageflat");
        if (buttonImage.length() == 0) {
            buttonImage = this.__DefaultButtonImage;
        }
        StringBuffer buttonDebug = new StringBuffer();
        String buttonText = plCommonProps.getProperty("text");
        if (!(buttonText.length() != 0 || plCommonProps.getProperty("showtext", CONSTANT_Y).equalsIgnoreCase(CONSTANT_N) && buttonImage.length() >= 1)) {
            buttonText = this.__DefaultButtonText;
        }
        buttonDebug.append("buttonText = ").append(buttonText).append(", ");
        String buttonShow = plCommonProps.getProperty("show");
        if (buttonShow.length() == 0) {
            buttonShow = CONSTANT_Y;
        }
        buttonDebug.append("buttonShow = ").append(buttonShow).append(", ");
        buttonDebug.append("buttonImage = ").append(buttonImage).append(", ");
        String imagelarge = buttonImage.startsWith("rc?command=image") ? buttonImage : plCommonProps.getProperty("imagelarge");
        if (imagelarge.length() == 0) {
            imagelarge = buttonImage;
        }
        buttonDebug.append("imagelarge = ").append(imagelarge).append(", ");
        String buttonWidth = plCommonProps.getProperty("width");
        String buttonAppearance = plCommonProps.getProperty("appearance");
        if (buttonAppearance.length() == 0) {
            buttonAppearance = "standard";
        }
        buttonDebug.append("buttonAppearance = ").append(buttonAppearance).append(", ");
        String buttonTip = plCommonProps.getProperty("tip");
        if (buttonTip.length() == 0) {
            buttonTip = buttonText;
        }
        buttonDebug.append("buttonTip = ").append(buttonTip).append(", ");
        String buttonId = plButton.getProperty("id");
        if (buttonId.length() == 0) {
            buttonId = buttonText;
        }
        buttonDebug.append("buttonId = ").append(buttonId).append(", ");
        String buttonMargin = plCommonProps.getProperty("margin");
        if (buttonMargin.length() == 0) {
            buttonMargin = "thin";
        }
        buttonDebug.append("buttonMargin = ").append(buttonMargin).append(", ");
        String buttonStyle = plCommonProps.getProperty("style");
        buttonDebug.append("buttonStyle = ").append(buttonStyle).append(", ");
        String buttonHighlight = plCommonProps.getProperty("highlight");
        if (buttonHighlight.length() == 0) {
            buttonHighlight = CONSTANT_Y;
        }
        buttonDebug.append("buttonHighlight = ").append(buttonHighlight).append(", ");
        String buttonMode = plCommonProps.getProperty("mode");
        if (buttonMode.length() == 0) {
            buttonMode = CONSTANT_BOTH;
        }
        buttonDebug.append("buttonMode = ").append(buttonMode).append(", ");
        String group = plCommonProps.getProperty("group", "");
        buttonDebug.append("group = ").append(group).append(", ");
        String ribbonstyle = plCommonProps.getProperty("ribbonstyle", "");
        buttonDebug.append("ribbonstyle = ").append(ribbonstyle);
        this.logger.debug(buttonDebug);
        hmProps.put("text", buttonText);
        hmProps.put("show", buttonShow);
        hmProps.put("image", buttonImage);
        hmProps.put("imagelarge", imagelarge);
        hmProps.put("imageflat", imageflat);
        hmProps.put("width", buttonWidth);
        hmProps.put("appearance", buttonAppearance);
        hmProps.put("tip", buttonTip);
        hmProps.put("id", buttonId);
        hmProps.put("margin", buttonMargin);
        hmProps.put("style", buttonStyle);
        hmProps.put("highlight", buttonHighlight);
        hmProps.put("mode", buttonMode);
        hmProps.put("inline", plCommonProps.getProperty("inline"));
        hmProps.put("dropdowngroup", plCommonProps.getProperty("dropdowngroup"));
        hmProps.put("group", group);
        hmProps.put("ribbonstyle", ribbonstyle);
        hmProps.put("applytoset", "Only One".equals(plButton.getProperty("initialselection")) ? CONSTANT_N : CONSTANT_Y);
        hmProps.put("showindetailpanel", plCommonProps.getProperty("showindetailpanel"));
        return hmProps;
    }

    private HashMap<String, String> getStandardButtonProps(String buttonId, PropertyList plButton, DataSet sdcsec) {
        String largeImage;
        String buttonImage;
        HashMap<String, String> hmProps = new HashMap<String, String>();
        PropertyList plStandardButtonProps = plButton.getPropertyList("standardbuttonprops");
        PropertyList plCommonProps = plButton.getPropertyList("commonprops");
        Map<String, String> buttonActionProps = this.getButtonAction(BUTTON_TYPE_STANDARD, buttonId, plButton, sdcsec);
        String buttonAction = buttonActionProps.get("buttonaction");
        String buttonFinalScript = buttonActionProps.get("buttonfinalscript");
        hmProps.put("action", buttonAction);
        hmProps.put("buttonfinalscript", buttonFinalScript);
        hmProps.put("checkoutrequired", this.getButtonCheckoutRequired(plButton));
        String[] images = this.getStandardButtonImage(plStandardButtonProps.getProperty("action"));
        if (images == null || images.length == 0) {
            buttonImage = plCommonProps.getProperty("image").length() > 0 ? plCommonProps.getProperty("image") : this.__DefaultButtonImage;
            largeImage = plCommonProps.getProperty("largeimage").length() > 0 ? plCommonProps.getProperty("imagelarge") : buttonImage;
        } else if (images.length > 1) {
            String string = buttonImage = plCommonProps.getProperty("image").length() > 0 ? plCommonProps.getProperty("image") : images[1];
            largeImage = buttonImage.startsWith("rc?command=image") ? buttonImage : (images.length > 2 ? (plCommonProps.getProperty("imagelarge").length() > 0 ? plCommonProps.getProperty("imagelarge") : images[2]) : (plCommonProps.getProperty("imagelarge").length() > 0 ? plCommonProps.getProperty("imagelarge") : buttonImage));
        } else {
            String string = buttonImage = plCommonProps.getProperty("image").length() > 0 ? plCommonProps.getProperty("image") : images[0];
            largeImage = buttonImage.startsWith("rc?command=image") ? buttonImage : (plCommonProps.getProperty("imagelarge").length() > 0 ? plCommonProps.getProperty("imagelarge") : buttonImage);
        }
        hmProps.put("image", buttonImage);
        hmProps.put("imagelarge", largeImage);
        String text = this.getStandardButtonText(plStandardButtonProps.getProperty("action"));
        if (plCommonProps.getProperty("text").length() == 0) {
            hmProps.put("text", text);
        }
        if (plCommonProps.getProperty("tip").length() == 0) {
            hmProps.put("tip", text);
        }
        return hmProps;
    }

    private String getButtonCheckoutRequired(PropertyList plButton) {
        String checkoutrequired = CONSTANT_N;
        if (this.isSDCChangeControlled) {
            String checkoutoption = plButton.getProperty("checkoutoption", "None");
            if (checkoutoption.indexOf("$G{") == 0) {
                try {
                    checkoutoption = GroovyUtil.getInstance(this.pageContext).evaluateSecure(checkoutoption, this.getGroovyBindMap());
                }
                catch (SapphireException se) {
                    Logger.logError(se.getMessage());
                }
            }
            checkoutrequired = !"None".equals(checkoutoption) ? CONSTANT_Y : CONSTANT_N;
        }
        return checkoutrequired;
    }

    private HashMap<String, String> getActionButtonProps(String buttonId, PropertyList plButton, DataSet sdcsec) {
        HashMap<String, String> hmProps = new HashMap<String, String>();
        PropertyList plActionButtonProps = plButton.getPropertyList("actionbuttonprops");
        if (plActionButtonProps != null) {
            String releaseLockFlag = plActionButtonProps.getProperty("releaselock");
            Map<String, String> buttonActionProps = this.getButtonAction(BUTTON_TYPE_ACTION, buttonId, plButton, sdcsec, releaseLockFlag);
            String buttonAction = buttonActionProps.get("buttonaction");
            String buttonFinalScript = buttonActionProps.get("buttonfinalscript");
            String needsSelection = plActionButtonProps.getProperty("needsselection");
            String preAction = plActionButtonProps.getProperty("preaction");
            hmProps.put("action", buttonAction);
            hmProps.put("buttonfinalscript", buttonFinalScript);
            hmProps.put("checkoutrequired", this.getButtonCheckoutRequired(plButton));
            hmProps.put("needsselection", needsSelection);
            hmProps.put("preaction", preAction);
            hmProps.put("releaselockflag", releaseLockFlag);
        }
        return hmProps;
    }

    private HashMap<String, String> getUserButtonProps(String buttonId, PropertyList plButton, DataSet sdcsec) {
        HashMap<String, String> hmProps = new HashMap<String, String>();
        PropertyList plUserButtonProps = plButton.getPropertyList("userbuttonprops");
        if (plUserButtonProps != null) {
            String releaseLockFlag = plUserButtonProps.getProperty("releaselock");
            Map<String, String> buttonActionProps = this.getButtonAction(BUTTON_TYPE_USER, buttonId, plButton, sdcsec, releaseLockFlag);
            String buttonAction = buttonActionProps.get("buttonaction");
            String buttonFinalScript = buttonActionProps.get("buttonfinalscript");
            buttonAction = StringUtil.replaceAll(buttonAction, "[sdcid]", this.__SdcId);
            hmProps.put("action", buttonAction);
            hmProps.put("buttonfinalscript", buttonFinalScript);
            hmProps.put("checkoutrequired", this.getButtonCheckoutRequired(plButton));
            hmProps.put("releaselockflag", releaseLockFlag);
        }
        return hmProps;
    }

    private PropertyList getTaskDef(PropertyList plTaskButtonProps) {
        PropertyList taskdef = null;
        if (plTaskButtonProps != null) {
            if (plTaskButtonProps.containsKey("_taskdef")) {
                taskdef = plTaskButtonProps.getPropertyList("_taskdef");
            } else {
                String t = plTaskButtonProps.getProperty("taskdef", "");
                taskdef = new PropertyList();
                if (t.length() > 0) {
                    try {
                        taskdef.setPropertyList(t);
                    }
                    catch (Exception e) {
                        taskdef = new PropertyList();
                    }
                }
                plTaskButtonProps.setProperty("_taskdef", taskdef);
            }
        }
        return taskdef;
    }

    private HashMap<String, String> getTaskButtonProps(String buttonId, PropertyList plButton, DataSet sdcsec) {
        HashMap<String, String> hmProps = new HashMap<String, String>();
        PropertyList plTaskButtonProps = plButton.getPropertyList("taskbuttonprops");
        if (plTaskButtonProps != null) {
            String taskvarkid;
            String taskverid;
            String releaseLockFlag = plTaskButtonProps.getProperty("releaselock");
            Map<String, String> buttonActionProps = this.getButtonAction(BUTTON_TYPE_TASK, buttonId, plButton, sdcsec, releaseLockFlag);
            String buttonAction = buttonActionProps.get("buttonaction");
            String buttonFinalScript = buttonActionProps.get("buttonfinalscript");
            PropertyList plCommonProps = plButton.getPropertyList("commonprops");
            PropertyList taskdef = this.getTaskDef(plTaskButtonProps);
            String taskid = taskdef.getProperty("taskdefid", plTaskButtonProps.getProperty("taskdefid"));
            PropertyList task = this.getTask(taskid, taskverid = taskdef.getProperty("taskdefversionid", plTaskButtonProps.getProperty("taskdefversionid", "1")), taskvarkid = taskdef.getProperty("taskdefvariantid", plTaskButtonProps.getProperty("taskdefvariantid", "1")));
            if (task != null) {
                if (plCommonProps.getProperty("image").length() == 0) {
                    hmProps.put("image", task.getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage16.png"));
                }
                if (plCommonProps.getProperty("imagelarge").length() == 0) {
                    hmProps.put("imagelarge", task.getProperty("icon", "WEB-CORE/elements/workflow/images/NoImage32.png"));
                }
                if (plCommonProps.getProperty("text").length() == 0) {
                    hmProps.put("text", task.getProperty("shorttitle", task.getProperty("taskdefid")));
                }
                if (plCommonProps.getProperty("tip").length() == 0) {
                    hmProps.put("tip", task.getProperty("longtitle", task.getProperty("shorttitle", task.getProperty("taskdefid"))));
                }
            } else {
                if (plCommonProps.getProperty("image").length() == 0) {
                    hmProps.put("image", "WEB-CORE/elements/workflow/images/NoImage16.png");
                }
                if (plCommonProps.getProperty("text").length() == 0) {
                    hmProps.put("text", taskid);
                }
            }
            hmProps.put("action", buttonAction);
            hmProps.put("buttonfinalscript", buttonFinalScript);
            hmProps.put("checkoutrequired", this.getButtonCheckoutRequired(plButton));
            hmProps.put("releaselockflag", releaseLockFlag);
        }
        return hmProps;
    }

    private String[] getStandardButtonImage(String stdButtonType) {
        String[] img = null;
        if (this.__StandardButtonImages.containsKey(stdButtonType)) {
            img = (String[])this.__StandardButtonImages.get(stdButtonType);
        }
        return img;
    }

    private String getStandardButtonText(String stdButtonType) {
        String text = "";
        if (this.__StandardButtonTexts.get(stdButtonType) != null) {
            text = (String)this.__StandardButtonTexts.get(stdButtonType);
        }
        return text;
    }

    private String getSelectionValidation(String initialValidation, String initialSelection, boolean action, String actionform) {
        if ((initialValidation == null || initialValidation.length() == 0) && initialSelection != null && initialSelection.length() > 0) {
            if (initialSelection.equalsIgnoreCase("None")) {
                initialValidation = "evalButtonScript('checkNoneSelected()',true)";
            } else if (initialSelection.equalsIgnoreCase("Only One")) {
                initialValidation = action ? "evalButtonScript('getAndSetOnlyOneSelectedForACB',true)('" + actionform + "')" : "evalButtonScript('checkOnlyOneSelected()')";
            } else if (initialSelection.equalsIgnoreCase("At Least One")) {
                initialValidation = action ? "evalButtonScript('getAndSetAtLeastOneSelectionForACB',true)('" + actionform + "')" : "evalButtonScript('checkAtleastOneSelected',true)()";
            } else if (initialSelection.equalsIgnoreCase("At Least Two")) {
                initialValidation = action ? "evalButtonScript('getAndSetAtleastNSelectedForACB',true)(2)" : "evalButtonScript('checkAtleastNSelected',true)(2,'" + actionform + "')";
            } else if (initialSelection.equalsIgnoreCase("Not Required")) {
                initialValidation = "evalButtonScript('checkSelectionNotRequired',true)()";
            }
        }
        return initialValidation;
    }

    private Map<String, String> getButtonAction(String buttonType, String buttonId, PropertyList plButton, DataSet sdcsec) {
        return this.getButtonAction(buttonType, buttonId, plButton, sdcsec, "");
    }

    private Map<String, String> getButtonAction(String buttonType, String buttonId, PropertyList plButton, DataSet sdcsec, String releaseLockFlag) {
        String mainBtnScript;
        boolean blnEsigRequired = false;
        boolean blnCocRequired = false;
        PropertyList plEsigDtls = plButton.getPropertyList("esig");
        PropertyList plCocDtls = plButton.getPropertyList("coc");
        String operation = "";
        String accesstype = "world";
        String failurescript = "";
        PropertyList plDepSec = plButton.getPropertyList("depsecurity");
        if (plDepSec != null && (operation = plDepSec.getProperty("operation", "")).length() > 0) {
            if ("S".equalsIgnoreCase(this.securityMode)) {
                accesstype = "SDISecurity";
            } else if (sdcsec != null) {
                if (sdcsec.size() > 0) {
                    HashMap<String, String> find = new HashMap<String, String>();
                    find.put("operationid", operation);
                    int found = sdcsec.findRow(find);
                    accesstype = found > -1 ? sdcsec.getString(found, "accesstype", "") : "none";
                } else {
                    accesstype = "none";
                }
            }
            failurescript = plDepSec.getProperty("failurescript", "");
        }
        String primaryValiation = this.getSelectionValidation(plButton.getProperty("primaryvalidation"), plButton.getProperty("initialselection", ""), false, "");
        boolean backcompatmode = true;
        if (primaryValiation.length() > 0) {
            backcompatmode = false;
        }
        String deferredCallback = plButton.getProperty("deferredcallback");
        String deferredCallbackForm = plButton.getProperty("deferredcallbackform");
        if (deferredCallbackForm.length() == 0) {
            deferredCallbackForm = "submitdetail";
        }
        StringBuffer defferedScript = new StringBuffer();
        String ssValidationURL = plButton.getProperty("ssvalidationclass");
        String esigmode = "";
        if (plEsigDtls != null && !CONSTANT_Y.equals(this.globalDisableEsig)) {
            esigmode = plEsigDtls.getProperty("required", CONSTANT_N);
            boolean bl = blnEsigRequired = !esigmode.equalsIgnoreCase(CONSTANT_N);
        }
        if (plCocDtls != null && !CONSTANT_Y.equals(this.globalDisableEsig)) {
            blnCocRequired = plCocDtls.getProperty("required").equalsIgnoreCase(CONSTANT_Y);
        }
        String initialValidation = this.getInitialValidation(buttonType, plButton);
        if (deferredCallback.length() > 0) {
            if (initialValidation.length() > 0) {
                initialValidation = initialValidation.endsWith(";") ? initialValidation + "__atReturn(false);" : initialValidation + ";__atReturn(false);";
            } else if (primaryValiation.length() > 0) {
                primaryValiation = primaryValiation.endsWith(";") ? primaryValiation + "__atReturn(false);" : primaryValiation + ";__atReturn(false);";
            }
        }
        String buttonFinalScript = mainBtnScript = this.prepareMainButtonScript(buttonType, plButton, buttonId, primaryValiation.length() > 0 || initialValidation.length() > 0);
        String promptPage = "";
        String promptForm = "submitdata";
        String promptPrefix = "action_";
        if (buttonType.equalsIgnoreCase("action")) {
            promptPage = plButton.getPropertyList("actionbuttonprops").getProperty("promptpage", "");
            promptForm = plButton.getPropertyList("actionbuttonprops").getProperty("actionbuttonform", promptForm);
        }
        String buttonText = plButton.getPropertyList("commonprops").getProperty("text");
        String buttonActivity = plButton.getProperty("buttonactivity");
        mainBtnScript = this.prepareEsigAndCOCScript(mainBtnScript, blnEsigRequired, blnCocRequired, backcompatmode, initialValidation, deferredCallback, defferedScript, ssValidationURL, SafeHTML.encodeForJavaScript(StringUtil.replaceAll(buttonText, "&nbsp;", " ")), buttonId, plEsigDtls, plCocDtls);
        if (releaseLockFlag != null) {
            mainBtnScript = this.getReleseLockFunction(releaseLockFlag) + mainBtnScript;
        }
        String checkoutoption = CONSTANT_Y.equals(plButton.getProperty("changecontrolflag")) ? plButton.getProperty("checkoutoption") : "";
        String calledAction = mainBtnScript = this.getPrimaryScript(StringUtil.replaceAll(mainBtnScript, "&#034;", "\\&#034;"), blnEsigRequired, blnCocRequired, backcompatmode, accesstype, primaryValiation, initialValidation, deferredCallback, defferedScript, ssValidationURL, operation, failurescript, promptPage, promptForm, promptPrefix, buttonId, SafeHTML.encodeForJavaScript(StringUtil.replaceAll(buttonText, "&nbsp;", " ")), buttonActivity, checkoutoption);
        if (deferredCallback.length() > 0 && defferedScript.length() > 0) {
            StringBuffer sbScript = new StringBuffer();
            sbScript.append("__deferredCallbacks['").append(buttonId).append("']={");
            sbScript.append("callback:'").append(deferredCallback).append("',");
            sbScript.append(deferredCallback).append(":function(passedinkeys){");
            sbScript.append("try{");
            sbScript.append("try{ document.").append(deferredCallbackForm).append(".keysfromlookup.value=passedinkeys;}catch(ignoreerr){}");
            sbScript.append(StringUtil.replaceAll(defferedScript.toString(), "&#034;", "\"")).append(";");
            sbScript.append("}catch(e){sapphire.alert('Deferred callback javascript ");
            sbScript.append(deferredCallback).append(" error:'+e.message)}}");
            sbScript.append("}\n");
            this.__DeferredCallbackScript.append("\n").append(sbScript.toString());
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("buttonaction", calledAction);
        map.put("buttonfinalscript", buttonFinalScript);
        return map;
    }

    private String getInitialValidation(String buttonType, PropertyList plButton) {
        String out = plButton.getProperty("initialvalidation");
        if (buttonType.equalsIgnoreCase(BUTTON_TYPE_STANDARD)) {
            PropertyList plButtonDtls = plButton.getPropertyList("standardbuttonprops");
            String stdButtonType = plButtonDtls.getProperty("action");
            String defaultInitValidation = "";
            if (this.__StandardButtonInitValidationActions.get(stdButtonType) != null) {
                defaultInitValidation = (String)this.__StandardButtonInitValidationActions.get(stdButtonType);
            }
            if (out.length() == 0 && defaultInitValidation.length() > 0) {
                out = defaultInitValidation;
            }
            if (this.__StandardButtonInitValidationActions.get("Pre" + stdButtonType) != null) {
                out = this.__StandardButtonInitValidationActions.get("Pre" + stdButtonType) + ";" + out;
            }
        } else if (buttonType.equalsIgnoreCase(BUTTON_TYPE_ACTION)) {
            PropertyList plButtonDtls = plButton.getPropertyList("standardbuttonprops");
            String actionButtonForm = plButtonDtls.getProperty("actionbuttonform");
            String defaultInitValidation = "evalButtonScript('getAndSetAtLeastOneSelectionForACB',true)('" + actionButtonForm + "')";
            out = !"None".equals(plButton.getProperty("initialselection")) && !"Not Required".equals(plButton.getProperty("initialselection")) && out.length() == 0 && defaultInitValidation.length() > 0 ? defaultInitValidation : "evalButtonScript('getAndSetAnySelectionForACB',true)('" + actionButtonForm + "');" + out;
        } else if (buttonType.equalsIgnoreCase(BUTTON_TYPE_TASK)) {
            PropertyList taskdef = this.getTaskDef(plButton.getPropertyList("taskbuttonprops"));
            if (taskdef != null && taskdef.getProperty("ioid", "").length() == 0) {
                if (out.length() == 0) {
                    out = "checkNoneSelected()";
                }
            } else if (out.length() == 0) {
                out = "checkAtleastOneSelected()";
            }
        }
        return out;
    }

    private String getPrimaryScript(String buttonscript, boolean blnEsigRequired, boolean blnCocRequired, boolean backCompat, String accesstype, String primaryValiation, String initialValidation, String deferredCallback, StringBuffer defferedScript, String ssValidationURL, String operation, String failurescript, String promptPage, String promptForm, String promptPrefix, String buttonId, String buttonText, String buttonActivity, String checkoutoption) {
        StringBuffer primaryscript = new StringBuffer();
        StringBuffer securityScript = new StringBuffer();
        if (!blnCocRequired && !blnEsigRequired) {
            if (backCompat) {
                primaryValiation = "";
                initialValidation = "";
                ssValidationURL = "";
            } else {
                initialValidation = "";
                ssValidationURL = "";
            }
        } else if (backCompat) {
            primaryValiation = "";
        } else {
            initialValidation = "";
            ssValidationURL = "";
        }
        if (this.securityMode.length() > 0 && !accesstype.equalsIgnoreCase("world")) {
            primaryscript.append(" var keyArray = new Array();");
            int keyCount = Integer.parseInt(this.getSDCProcessor().getProperty(this.__SdcId, "keycolumns"));
            String keycolid1 = this.getSDCProcessor().getProperty(this.__SdcId, "keycolid1");
            String keycolid2 = this.getSDCProcessor().getProperty(this.__SdcId, "keycolid2");
            String keycolid3 = this.getSDCProcessor().getProperty(this.__SdcId, "keycolid3");
            for (int k = 0; k < keyCount; ++k) {
                primaryscript.append("keyArray[").append(k).append("]=sapphire.page.getKeyId").append("( ").append(k + 1).append(",true );");
            }
            StringBuffer keyCols = new StringBuffer(keycolid1);
            if (keyCount > 1) {
                keyCols.append(";" + keycolid2);
            }
            if (keyCount > 2) {
                keyCols.append(";" + keycolid3);
            }
            for (int k = 0; k < keyCount; ++k) {
                primaryscript.append("keyArray[").append(k).append("]=sapphire.page.getKeyId").append("( ").append(k + 1).append(",true );");
            }
            primaryscript.append(" if( getSdcId() != '").append(this.__SdcId).append("') { ").append("var lEl = $('input[name$=\"").append("_linkelement_").append(this.__SdcId).append("\"]').val();").append("var keyCols = '").append(keyCols).append("';").append("var aSelectedItems;").append("if ( lEl != undefined ) { ").append("if (sapphire.page.elements.sdcLinkMaint.isSDCLinkMaint(lEl)) { ").append("        aSelectedItems = sapphire.page.elements.sdcLinkMaint.getSelected(lEl,keyCols).replace(/\\|/g,'|').split('|'); ").append(" if ( aSelectedItems!= 'undefined' ) { ").append(" var keyid1 = '';").append(" var keyid2 = '';").append(" var keyid3 = '';").append(" var keyColCount = keyCols.split(';').length;").append("for ( var i = 0; i < aSelectedItems.length; i++ ) { ").append("var aSelectedKeys = aSelectedItems[i].split( \";\" );").append("keyid1 += ';' + aSelectedKeys[0];").append("if ( keyColCount>1 && aSelectedKeys[1]!= 'undefined') { ").append("keyid2 += ';' + aSelectedKeys[1];").append("}").append("if ( keyColCount>2 && aSelectedKeys[2]!= 'undefined') { ").append("keyid3 += ';' + aSelectedKeys[2];").append("}").append(" }").append("if ( keyid1.length > 0 ) {").append("keyArray[0]=keyid1.substring( 1 );").append("}").append("if ( keyColCount>1 && keyid2.length > 0 ) {").append("keyArray[1]=keyid2.substring( 1 );").append("}").append("if ( keyColCount>2 && keyid2.length > 0 ) {").append("keyArray[2]=keyid2.substring( 1 );").append("}").append("}").append(" }").append(" else { ").append(" aSelectedItems = getSelectedRowID(elementid); ").append(" var selectedKeys = ''; ").append(" if ( aSelectedItems != 'undefined' && aSelectedItems.length != 0 ) { ").append(" var keyid1 = '';").append(" var keyid2 = '';").append(" var keyid3 = '';").append(" var keyColCount = keyCols.split( ';' ).length;").append(" var keyCol = keyCols.split( ';' );").append(" var rows = aSelectedItems.split(';');").append(" for ( var x = 0; x < rows.length; x++ ) { ").append(" keyid1 += ';' + getColumnValue(elementid, rows[x], keyCol[0]); ").append(" if ( keyColCount > 1 ) { ").append(" keyid2 += ';' + getColumnValue(elementid, rows[x], keyCol[1]); ").append(" } ").append(" if ( keyColCount > 2 ) { ").append(" keyid3 += ';' + getColumnValue(elementid, rows[x], keyCol[2]); ").append(" } ").append(" } ").append("if ( keyid1.length > 0 ) {").append("keyArray[0]=keyid1.substring( 1 );").append("}").append("if ( keyColCount>1 && keyid2.length > 0 ) {").append("keyArray[1]=keyid2.substring( 1 );").append("}").append("if ( keyColCount>2 && keyid2.length > 0 ) {").append("keyArray[2]=keyid2.substring( 1 );").append("}").append(" } ").append(" } ").append("}").append("}");
            primaryscript.append("var secObj = {sdcid: &#034;").append(this.__SdcId).append("&#034;, keys:keyArray, securitymode: &#034;").append(this.securityMode).append("&#034;, operation: &#034;").append(operation).append("&#034;, accesstype: &#034;").append(accesstype).append("&#034;,failurescript: &#034;").append(failurescript).append("&#034;,failurefunction:this._failureFunction};");
        }
        if ("CheckOut".equals(checkoutoption)) {
            primaryscript.append("sapphire.alert('" + checkoutoption + "');");
        } else if ("CheckIn".equals(checkoutoption)) {
            primaryscript.append("sapphire.alert('" + checkoutoption + "');");
        }
        primaryscript.append(JS_CALLFUNCION).append("(executeButtonScript,");
        primaryscript.append("[");
        primaryscript.append("&#034;").append(buttonscript).append("&#034;");
        primaryscript.append(", ");
        primaryscript.append("&#034;").append(initialValidation).append("&#034;");
        primaryscript.append(",");
        primaryscript.append("&#034;").append(ssValidationURL).append("&#034;");
        primaryscript.append(",");
        primaryscript.append("&#034;").append("&#034;");
        primaryscript.append(",");
        primaryscript.append("&#034;").append(primaryValiation).append("&#034;");
        primaryscript.append(",");
        primaryscript.append("&#034;").append(buttonId).append("&#034;");
        primaryscript.append(",");
        primaryscript.append("&#034;").append(StringUtil.replaceAll(buttonText, "\"", "\\\"")).append("&#034;");
        primaryscript.append(",");
        primaryscript.append("&#034;").append(buttonActivity).append("&#034;");
        primaryscript.append(",");
        primaryscript.append("{page:&#034;").append(promptPage).append("&#034;,form:&#034;").append(promptForm).append("&#034;,prefix:&#034;").append(promptPrefix).append("&#034;,sdcid:&#034;").append(this.__SdcId).append("&#034;}");
        primaryscript.append(",&#034;").append(checkoutoption).append("&#034;");
        primaryscript.append("]");
        if (this.securityMode.length() > 0 && !accesstype.equalsIgnoreCase("world")) {
            primaryscript.append(", null, secObj");
        }
        primaryscript.append(")");
        if (initialValidation.length() > 0 && deferredCallback.length() > 0 && defferedScript.length() == 0) {
            defferedScript.append(JS_CALLFUNCION).append("(executeButtonScript,");
            defferedScript.append("[");
            defferedScript.append("&#034;").append(buttonscript).append("&#034;");
            defferedScript.append(", ");
            defferedScript.append("&#034;").append("&#034;");
            defferedScript.append(",");
            defferedScript.append("&#034;").append(ssValidationURL).append("&#034;");
            defferedScript.append(",");
            defferedScript.append("&#034;").append("&#034;");
            defferedScript.append(",");
            defferedScript.append("&#034;").append("&#034;");
            defferedScript.append(",");
            defferedScript.append("&#034;").append(buttonId).append("&#034;");
            defferedScript.append(",");
            defferedScript.append("{page:&#034;").append(promptPage).append("&#034;,form:&#034;").append(promptForm).append("&#034;,prefix:&#034;").append(promptPrefix).append("&#034;,sdcid:&#034;").append(this.__SdcId).append("&#034;}");
            defferedScript.append(",&#034;").append(checkoutoption).append("&#034;");
            defferedScript.append("]");
            defferedScript.append(")");
        }
        return primaryscript.toString();
    }

    private String prepareMainButtonScript(String buttonType, PropertyList plButton, String buttonId, boolean hasValidation) {
        StringBuilder tempButtonAction = new StringBuilder();
        if (buttonType.equalsIgnoreCase(BUTTON_TYPE_STANDARD)) {
            PropertyList plButtonDtls = plButton.getPropertyList("standardbuttonprops");
            String buttonTarget = plButtonDtls.getProperty("target").length() == 0 ? "_self" : plButtonDtls.getProperty("target");
            String buttonPage = this.getResolvedButtonPage(plButton);
            if (buttonPage.length() > 0 && "navigator".equals(this.pageContext.getRequest().getParameter("layout"))) {
                buttonPage = buttonPage + "&layout=navigator";
            }
            boolean storeselection = plButtonDtls.getProperty("storeselection", CONSTANT_N).equalsIgnoreCase(CONSTANT_Y);
            buttonPage = StringUtil.replaceAll(buttonPage, "[sdcid]", this.__SdcId);
            String stdButtonType = plButtonDtls.getProperty("action");
            if (buttonTarget.equalsIgnoreCase("")) {
                buttonTarget = stdButtonType.equalsIgnoreCase("Report") ? "_blank" : "_self";
            }
            tempButtonAction.append((String)this.__StandardButtonActions.get(stdButtonType));
            if (buttonPage.length() > 0) {
                if (stdButtonType.equalsIgnoreCase("add") || stdButtonType.equalsIgnoreCase("edit")) {
                    tempButtonAction.append("('").append(buttonPage).append("',");
                    tempButtonAction.append("'").append(buttonTarget).append("',");
                    tempButtonAction.append("null").append(",");
                    tempButtonAction.append(storeselection).append(")");
                } else if (stdButtonType.equalsIgnoreCase("report")) {
                    tempButtonAction.append("('").append(buttonPage).append("',");
                    tempButtonAction.append("'").append(buttonTarget).append("',");
                    tempButtonAction.append("null").append(",");
                    tempButtonAction.append(hasValidation).append(")");
                } else {
                    tempButtonAction.append("('").append(buttonPage).append("',");
                    tempButtonAction.append("'").append(buttonTarget).append("',");
                    tempButtonAction.append("null)");
                }
            } else {
                tempButtonAction.append("()");
            }
        } else if (buttonType.equalsIgnoreCase("action")) {
            PropertyList plButtonDtls = plButton.getPropertyList("actionbuttonprops");
            String actionButtonForm = plButtonDtls.getProperty("actionbuttonform");
            if (actionButtonForm.length() == 0) {
                actionButtonForm = "submitdata";
            }
            String target = plButtonDtls.getProperty("target");
            String advancedToolbarId = this.element.getId();
            tempButtonAction.append("actionbuttonscript(").append("'").append(this.__SdcId).append("',").append("'").append(advancedToolbarId).append("',").append("'").append(buttonId).append("',").append("'").append(actionButtonForm).append("',").append("'").append(target).append("',").append("'',").append("'',").append("'',").append("'','')");
        } else if (buttonType.equalsIgnoreCase(BUTTON_TYPE_TASK)) {
            String varS;
            PropertyList plButtonDtls = plButton.getPropertyList("taskbuttonprops");
            String t = plButtonDtls.getProperty("taskdef", "");
            PropertyList taskdef = new PropertyList();
            if (t.length() > 0) {
                try {
                    taskdef.setPropertyList(t);
                }
                catch (Exception e) {
                    taskdef = new PropertyList();
                }
            }
            String taskid = taskdef.getProperty("taskdefid", plButtonDtls.getProperty("taskdefid"));
            String taskverid = taskdef.getProperty("taskdefversionid", plButtonDtls.getProperty("taskdefversionid", "1"));
            String taskvarkid = taskdef.getProperty("taskdefvariantid", plButtonDtls.getProperty("taskdefvariantid", "1"));
            String ioid = taskdef.getProperty("ioid", plButtonDtls.getProperty("ioid", ""));
            String releaseLock = plButtonDtls.getProperty("releaselock", CONSTANT_Y);
            String target = plButtonDtls.getProperty("target", "");
            PropertyListCollection vars = taskdef.getCollection("variables");
            if (vars == null) {
                vars = plButtonDtls.getCollection("variables");
            }
            String string = varS = vars == null || vars.size() == 0 ? "null" : StringUtil.replaceAll(vars.toJSONString(false, false), "\"", "'");
            if (this.getTask(taskid, taskverid, taskvarkid) != null) {
                tempButtonAction.append("sapphire.task.launch('").append(taskid).append("','").append(taskverid).append("','" + taskvarkid).append("','").append(ioid).append("',").append(varS).append(",'").append(releaseLock).append("','").append(target + "');");
            } else {
                tempButtonAction.append("sapphire.alert('Invalid Task Provided');");
            }
        } else if (buttonType.equalsIgnoreCase(BUTTON_TYPE_USER)) {
            PropertyList plButtonDtls = plButton.getPropertyListNotNull("userbuttonprops");
            tempButtonAction.append(plButtonDtls.getProperty("action"));
        }
        return tempButtonAction.toString();
    }

    private String prepareEsigAndCOCScript(String buttonscript, boolean blnEsigRequired, boolean blnCocRequired, boolean backcompat, String initialValidation, String defferedCallback, StringBuffer defferedScript, String ssValidationURL, String buttonText, String buttonid, PropertyList plEsigDtls, PropertyList plCocDtls) {
        StringBuffer script = new StringBuffer();
        if (!blnEsigRequired && !blnCocRequired) {
            if (backcompat) {
                // empty if block
            }
            script.append(JS_CALLFUNCION).append("(executeButtonScript,");
            script.append("[");
            script.append("&#034;").append(buttonscript).append("&#034;");
            script.append(", ");
            script.append("&#034;").append(initialValidation).append("&#034;");
            script.append(",");
            script.append("&#034;").append(ssValidationURL).append("&#034;");
            script.append(",");
            script.append("&#034;").append("").append("&#034;");
            script.append(",");
            script.append("&#034;").append("").append("&#034;");
            script.append(",");
            script.append("&#034;").append("").append("&#034;");
            script.append(",");
            script.append("").append("{}").append("");
            script.append("]");
            script.append(")");
            if (initialValidation.length() > 0 && defferedCallback.length() > 0 && defferedScript.length() == 0) {
                defferedScript.append(JS_CALLFUNCION).append("(executeButtonScript,");
                defferedScript.append("[");
                defferedScript.append("&#034;").append(buttonscript).append("&#034;");
                defferedScript.append(", ");
                defferedScript.append("&#034;").append("").append("&#034;");
                defferedScript.append(",");
                defferedScript.append("&#034;").append(ssValidationURL).append("&#034;");
                defferedScript.append(",");
                defferedScript.append("&#034;").append("").append("&#034;");
                defferedScript.append(",");
                defferedScript.append("&#034;").append("").append("&#034;");
                defferedScript.append(",");
                defferedScript.append("&#034;").append("").append("&#034;");
                defferedScript.append(",");
                defferedScript.append("").append("{}").append("");
                defferedScript.append("]");
                defferedScript.append(")");
            }
        } else {
            String esigSdcId = this.__SdcId;
            String esigMessage = "";
            String esigPage = "";
            String auditReasonReftype = "";
            String reasonPromptOption = "";
            String setEsigReasonCallback = "";
            String esigForm = "";
            String esigMode = "";
            if (blnEsigRequired) {
                esigMessage = plEsigDtls.getProperty("message");
                esigPage = plEsigDtls.getProperty("esigpage");
                esigMode = plEsigDtls.getProperty("required");
                if (esigMode.indexOf("$G{") == 0) {
                    HashMap<String, Object> bindMap = new HashMap<String, Object>();
                    bindMap.put("element", this.element);
                    bindMap.put("elements", this.requestContext.getPropertyList().getPropertyList("elements"));
                    bindMap.put("pagedata", this.requestContext.getPropertyList().getPropertyList("pagedata"));
                    bindMap.put("user", this.connectionInfo.getUserAttributeMap());
                    bindMap.put("policy", new GroovyPolicyUtil(this.pageContext));
                    String sdcid = this.element.getProperty("sdcid", this.requestContext != null ? this.requestContext.getProperty("sdcid") : "");
                    if (sdcid != null && sdcid.length() > 0) {
                        bindMap.put("sdc", this.getSDCProcessor().getSDCProperties(sdcid));
                    }
                    try {
                        esigMode = GroovyUtil.getInstance(this.pageContext).evaluateSecure(esigMode, bindMap);
                    }
                    catch (SapphireException se) {
                        Logger.logError(se.getMessage());
                    }
                }
                if (esigPage.length() == 0) {
                    esigPage = "rc?command=page&page=StandardESigForm";
                }
                if ((esigForm = plEsigDtls.getProperty("esigform")).length() == 0) {
                    esigForm = "submitdata";
                }
                auditReasonReftype = plEsigDtls.getProperty("auditreasonreftype");
                PropertyList plReasonPromptOption = plEsigDtls.getPropertyList("reasonpromptoption");
                String string = reasonPromptOption = reasonPromptOption.length() == 0 ? "Optional Free Text Reason" : reasonPromptOption;
                if (plReasonPromptOption != null) {
                    String required = plReasonPromptOption.getProperty("required").length() == 0 ? CONSTANT_N : plReasonPromptOption.getProperty("required");
                    required = required.equalsIgnoreCase(CONSTANT_Y) ? "Required " : "Optional ";
                    String reasonType = plReasonPromptOption.getProperty("reasontype").length() == 0 ? "Free Text Reason" : plReasonPromptOption.getProperty("reasontype");
                    reasonPromptOption = required + reasonType;
                }
                if ((setEsigReasonCallback = plEsigDtls.getProperty("setesigreasoncallback")).length() == 0) {
                    setEsigReasonCallback = "setESigReason";
                }
            }
            if (backcompat) {
                ssValidationURL = "";
                initialValidation = "";
            }
            if (blnCocRequired) {
                String cocPage = plCocDtls.getProperty("cocpage");
                String cocForm = plCocDtls.getProperty("cocform");
                if (cocPage.length() == 0) {
                    cocPage = "rc?command=page&page=COCForm";
                }
                if (cocForm.length() == 0) {
                    cocForm = "submitdata";
                }
                if (blnEsigRequired) {
                    if (initialValidation.length() > 0) {
                        script.append(JS_CALLFUNCION).append("(getCocAndEsig,");
                        script.append("[ ");
                        script.append("&#034;").append(esigPage).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(esigForm).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(auditReasonReftype).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(reasonPromptOption).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(esigMessage).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(esigSdcId).append("&#034;");
                        script.append(",");
                        script.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                        script.append(",");
                        script.append("&#034;").append(setEsigReasonCallback).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(cocPage).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(cocForm).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(buttonText).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(initialValidation).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(ssValidationURL).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append("").append("&#034;");
                        script.append(", ");
                        script.append(" &#034;").append("").append("&#034;");
                        script.append(", ");
                        script.append(" &#034;").append(buttonid).append("&#034;");
                        script.append(", ");
                        script.append(" &#034;").append(esigMode).append("&#034;");
                        script.append("]");
                        script.append(")");
                        if (defferedCallback.length() > 0 && defferedScript.length() == 0) {
                            defferedScript.append(JS_CALLFUNCION).append("(getCocAndEsig,");
                            defferedScript.append("[ ");
                            defferedScript.append("&#034;").append(esigPage).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(esigForm).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(auditReasonReftype).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(reasonPromptOption).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(esigMessage).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(esigSdcId).append("&#034;");
                            defferedScript.append(",");
                            defferedScript.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                            defferedScript.append(",");
                            defferedScript.append("&#034;").append(setEsigReasonCallback).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(cocPage).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(cocForm).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(buttonText).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append("").append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append(ssValidationURL).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append("&#034;").append("").append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append(" &#034;").append("").append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append(" &#034;").append(buttonid).append("&#034;");
                            defferedScript.append(", ");
                            defferedScript.append(" &#034;").append(esigMode).append("&#034;");
                            defferedScript.append("]");
                            defferedScript.append(")");
                        }
                    } else {
                        script.append(JS_CALLFUNCION).append("(getCocAndEsig,");
                        script.append("[");
                        script.append("&#034;").append(esigPage).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(esigForm).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(auditReasonReftype).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(reasonPromptOption).append("&#034;");
                        script.append(",");
                        script.append("&#034;").append(esigMessage).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(esigSdcId).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                        script.append(",");
                        script.append("&#034;").append(setEsigReasonCallback).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(cocPage).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(cocForm).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(buttonText).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append("").append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append(ssValidationURL).append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append("").append("&#034;");
                        script.append(", ");
                        script.append("&#034;").append("").append("&#034;");
                        script.append(", ");
                        script.append(" &#034;").append(buttonid).append("&#034;");
                        script.append(", ");
                        script.append(" &#034;").append(esigMode).append("&#034;");
                        script.append("]");
                        script.append(")");
                    }
                } else if (initialValidation.length() > 0) {
                    script.append(JS_CALLFUNCION).append("(getCOC,");
                    script.append(" [");
                    script.append("&#034;").append(cocPage).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(cocForm).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigSdcId).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(initialValidation).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(ssValidationURL).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append("]");
                    script.append(")");
                    if (defferedCallback.length() > 0 && defferedScript.length() == 0) {
                        defferedScript.append(JS_CALLFUNCION).append("(getCOC,");
                        defferedScript.append(" [");
                        defferedScript.append("&#034;").append(cocPage).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(cocForm).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(esigSdcId).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append("").append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(ssValidationURL).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append("").append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append("").append("&#034;");
                        defferedScript.append("]");
                        defferedScript.append(")");
                    }
                } else {
                    script.append(JS_CALLFUNCION).append("(getCOC,");
                    script.append("[");
                    script.append("&#034;").append(cocPage).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(cocForm).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigSdcId).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(ssValidationURL).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(" ]");
                    script.append(" )");
                }
            } else if (blnEsigRequired) {
                if (initialValidation.length() > 0) {
                    script.append(JS_CALLFUNCION).append("( getEsig, ");
                    script.append("[");
                    script.append("&#034;").append(esigPage).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigForm).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(auditReasonReftype).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(reasonPromptOption).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigMessage).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigSdcId).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(setEsigReasonCallback).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(buttonText).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(initialValidation).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(ssValidationURL).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(", ");
                    script.append(" &#034;").append(buttonid).append("&#034;");
                    script.append(", ");
                    script.append(" &#034;").append(esigMode).append("&#034;");
                    script.append("]");
                    script.append(")");
                    if (defferedCallback.length() > 0 && defferedScript.length() == 0) {
                        defferedScript.append(JS_CALLFUNCION).append("( getEsig, ");
                        defferedScript.append("[");
                        defferedScript.append("&#034;").append(esigPage).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(esigForm).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(auditReasonReftype).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(reasonPromptOption).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(esigMessage).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(esigSdcId).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(setEsigReasonCallback).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(buttonText).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append("").append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append(ssValidationURL).append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append("").append("&#034;");
                        defferedScript.append(",");
                        defferedScript.append("&#034;").append("").append("&#034;");
                        defferedScript.append(", ");
                        defferedScript.append(" &#034;").append(buttonid).append("&#034;");
                        defferedScript.append(", ");
                        defferedScript.append(" &#034;").append(esigMode).append("&#034;");
                        defferedScript.append("]");
                        defferedScript.append(")");
                    }
                } else {
                    script.append(JS_CALLFUNCION).append("(getEsig,");
                    script.append("[");
                    script.append("&#034;").append(esigPage).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigForm).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(auditReasonReftype).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(reasonPromptOption).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigMessage).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(esigSdcId).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(this.escapeJS(buttonscript)).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(setEsigReasonCallback).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(buttonText).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(",");
                    script.append("&#034;").append(ssValidationURL).append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(",");
                    script.append("&#034;").append("").append("&#034;");
                    script.append(",");
                    script.append(" &#034;").append(buttonid).append("&#034;");
                    script.append(",");
                    script.append(" &#034;").append(esigMode).append("&#034;");
                    script.append("]");
                    script.append(")");
                }
            }
        }
        return script.toString();
    }

    private String escapeJS(String js) {
        String out = StringUtil.replaceAll(js, "'", "\\'");
        out = StringUtil.replaceAll(out, "&#034;", "\\&#034;");
        return out;
    }

    private String getReleseLockFunction(String releaseLockFlag) {
        if (releaseLockFlag != null && releaseLockFlag.trim().length() > 0) {
            return "releaseLock('" + releaseLockFlag + "');  ";
        }
        return "";
    }

    private String getAdvancedToolbarForm() {
        StringBuffer html = new StringBuffer();
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${requestdata}", this.pageContext);
        html.append("<form id='").append(this.getAdvancedToolbarFormId(this.element.getId()));
        html.append("'  name='").append(this.getAdvancedToolbarFormId(this.element.getId()));
        html.append("'  method='post' target='' action='' > \n");
        for (int count = 0; count < this.__AdvancedToolbarFormFields.size(); ++count) {
            String field = (String)this.__AdvancedToolbarFormFields.get(count);
            String value = field.equalsIgnoreCase("currentuser") || field.equalsIgnoreCase("action_currentuser") ? pagedata.getProperty("sysuserid") : pagedata.getProperty(field);
            value = DOMUtil.convertChars(value);
            html.append("<input type='hidden' name='");
            html.append(field).append("' ");
            html.append("id='").append(field).append("' ");
            html.append("value='").append(value).append("' /> \n");
        }
        html.append("</form>\n");
        return html.toString();
    }

    private String getHotKey(PropertyList button) {
        StringBuffer hotKeyText = new StringBuffer();
        PropertyList hotkey = button.getPropertyList("hotkey");
        String character = "";
        if (hotkey != null) {
            character = hotkey.getProperty("character").trim();
            if (character.length() > 0) {
                boolean shift = hotkey.getProperty("shift", CONSTANT_N).equalsIgnoreCase(CONSTANT_Y);
                String used = HttpUtil.addShortcutKeyObject(character, button.getPropertyList("commonprops").getProperty("text", button.getProperty("id")), button.getProperty("id"), "", shift, true, this.requestContext);
                if (character.length() > 0 && character.equalsIgnoreCase(used)) {
                    if (this.browser != null && this.browser.getOS() == 3) {
                        hotKeyText.append("(CMD");
                    } else {
                        hotKeyText.append("(CTRL");
                    }
                    if (shift) {
                        hotKeyText.append(" + SHIFT ");
                    }
                    hotKeyText.append(" + ").append(character.toUpperCase()).append(")");
                }
            } else {
                character = HttpUtil.addShortcutKeyObject(character, button.getPropertyList("commonprops").getProperty("text", button.getProperty("id")), button.getProperty("id"), "", false, false, this.requestContext);
            }
        } else {
            character = HttpUtil.addShortcutKeyObject(character, button.getPropertyList("commonprops").getProperty("text", button.getProperty("id")), button.getProperty("id"), "", false, false, this.requestContext);
        }
        return hotKeyText.toString();
    }

    private String getACBIframe() {
        StringBuffer html = new StringBuffer();
        html.append("<iframe id='").append(this.__AdvancedToolbarACBIframe).append("' ").append(" name='").append(this.__AdvancedToolbarACBIframe).append("' ").append(" style=advacedtoolbar_iframe").append(" src='").append(this.browser.getBlankSrc()).append("'  >").append("Your browser does not support iframes!").append("</iframe>");
        return html.toString();
    }

    private String getAdvancedToolbarFormId(String advancedToolbarId) {
        return advancedToolbarId + "form";
    }

    private String getACBActions(PropertyList plButton) {
        StringBuffer html = new StringBuffer();
        return html.toString();
    }

    private void addNavigatorGroup(ToolbarType toolbarType) {
        if ("navigator".equals(this.pageContext.getRequest().getParameter("layout")) && this.pageContext.getRequest().getParameter("indexofsetdisplay") != null && this.pageContext.getRequest().getParameter("indexofsetdisplay").length() > 0) {
            PropertyListCollection buttonsCollection = this.element.getCollection("buttons");
            if (buttonsCollection.getPropertyList(0).getProperty("id").equals("tobeginning") || buttonsCollection.getPropertyList(0).getProperty("id").equals("previous") || buttonsCollection.getPropertyList(buttonsCollection.size() - 1).getProperty("id").equals("toend")) {
                return;
            }
            String groupid = "No. " + this.pageContext.getRequest().getParameter("indexofsetdisplay");
            boolean isModernLayout = CONSTANT_Y.equals(this.requestContext.getProperty("modernlayout"));
            if (this.browser.isPhone()) {
                buttonsCollection.add(0, this.buildButton(groupid, "previous", toolbarType == ToolbarType.hybridRibbon ? " " : (isModernLayout ? " " : this.translationProcessor.translate("Prev")), this.translationProcessor.translate("Previous (Ctrl-Arrow Up)"), "WEB-CORE/imageref/basic_application_icons/arrows_and_navigation/standard_arrows/32/arrow2_left_blue.png", "parent.sapphire.navigator.previous()", "#38"));
                buttonsCollection.add(1, this.buildButton(groupid, "next", toolbarType == ToolbarType.hybridRibbon ? " " : (isModernLayout ? " " : this.translationProcessor.translate("Next")), this.translationProcessor.translate("Next (Ctrl-Arrow Down)"), "WEB-CORE/imageref/basic_application_icons/arrows_and_navigation/standard_arrows/32/arrow2_right_blue.png", "parent.sapphire.navigator.next()", "#40"));
            } else {
                buttonsCollection.add(this.buildButton(groupid, "tobeginning", toolbarType == ToolbarType.hybridRibbon ? " " : (isModernLayout ? " " : this.translationProcessor.translate("First")), this.translationProcessor.translate("First (Ctrl-Home)"), "WEB-CORE/imageref/basic_application_icons/arrows_and_navigation/standard_arrows/32/arrow2_leftend_blue.png", "parent.sapphire.navigator.first()", "#36"));
                buttonsCollection.add(this.buildButton(groupid, "previous", toolbarType == ToolbarType.hybridRibbon ? " " : (isModernLayout ? " " : this.translationProcessor.translate("Prev")), this.translationProcessor.translate("Previous (Ctrl-Arrow Up)"), "WEB-CORE/imageref/basic_application_icons/arrows_and_navigation/standard_arrows/32/arrow2_left_blue.png", "parent.sapphire.navigator.previous()", "#38"));
                if (toolbarType == ToolbarType.hybridRibbon) {
                    buttonsCollection.add(this.buildButton(groupid, "indexofsetdisplay", groupid, groupid, "", "", ""));
                }
                buttonsCollection.add(this.buildButton(groupid, "next", toolbarType == ToolbarType.hybridRibbon ? " " : (isModernLayout ? " " : this.translationProcessor.translate("Next")), this.translationProcessor.translate("Next (Ctrl-Arrow Down)"), "WEB-CORE/imageref/basic_application_icons/arrows_and_navigation/standard_arrows/32/arrow2_right_blue.png", "parent.sapphire.navigator.next()", "#40"));
                buttonsCollection.add(this.buildButton(groupid, "toend", toolbarType == ToolbarType.hybridRibbon ? " " : (isModernLayout ? " " : this.translationProcessor.translate("Last")), this.translationProcessor.translate("Last (Ctrl-End)"), "WEB-CORE/imageref/basic_application_icons/arrows_and_navigation/standard_arrows/32/arrow2_rightend_blue.png", "parent.sapphire.navigator.last()", "#35"));
            }
        }
    }

    private PropertyList buildButton(String groupid, String id, String text, String tip, String image, String action, String keycharacter) {
        PropertyList b = new PropertyList();
        b.setProperty("id", id);
        b.setProperty("buttontype", BUTTON_TYPE_USER);
        PropertyList commonProps = new PropertyList();
        commonProps.setProperty("image", image);
        commonProps.setProperty("text", text);
        commonProps.setProperty("tip", tip);
        commonProps.setProperty("group", groupid);
        commonProps.setProperty("show", CONSTANT_Y);
        b.setProperty("commonprops", commonProps);
        PropertyList hotkeyprops = new PropertyList();
        b.setProperty("hotkey", hotkeyprops);
        hotkeyprops.setProperty("character", keycharacter);
        hotkeyprops.setProperty("shift", CONSTANT_N);
        PropertyList userbuttonprops = new PropertyList();
        b.setProperty("userbuttonprops", userbuttonprops);
        userbuttonprops.setProperty("action", action);
        return b;
    }

    private String getResolvedButtonPage(PropertyList plButton) {
        PropertyList plButtonDtls = plButton.getPropertyList("standardbuttonprops");
        String buttonPage = plButtonDtls.getProperty("page");
        if (this.browser.isIE() && plButtonDtls.getProperty("action").equalsIgnoreCase("Report") && plButtonDtls.getProperty("page").contains("command=viewreport") && !plButtonDtls.getProperty("page").contains("category")) {
            if (plButtonDtls.getProperty("page").contains("reportid")) {
                String reportidToken = buttonPage.substring(buttonPage.indexOf("reportid="));
                String reportid = reportidToken.substring(reportidToken.indexOf(61) + 1, reportidToken.contains("&") ? reportidToken.indexOf(38) : reportidToken.length());
                int paramcount = 0;
                try {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "SELECT count(*) FROM reportparam WHERE reportid = " + safeSQL.addVar(reportid) + " AND paramtype != 'hidden'";
                    paramcount = this.getQueryProcessor().getPreparedCount(sql, safeSQL.getValues());
                }
                catch (Exception e) {
                    this.logger.error("Failed to retrieve report param types", e.toString());
                }
                if (paramcount == 0) {
                    buttonPage = "rc/" + reportid + "_" + this.connectionInfo.getSysuserId() + "_" + new SimpleDateFormat("MMM-dd-yyyy-hh-mm-ss").format(new Date()) + buttonPage.substring(buttonPage.indexOf(63));
                }
            } else {
                String pagename = this.requestContext.getProperty("page");
                buttonPage = "rc/" + pagename + "_" + this.connectionInfo.getSysuserId() + "_" + new SimpleDateFormat("MMM-dd-yyyy-hh-mm-ss").format(new Date()) + buttonPage.substring(buttonPage.indexOf(63));
            }
        }
        return buttonPage;
    }

    private HashMap getGroovyBindMap() {
        HashMap<String, Object> bindMap = new HashMap<String, Object>();
        bindMap.put("element", this.element);
        bindMap.put("elements", this.requestContext.getPropertyList().getPropertyList("elements"));
        bindMap.put("pagedata", this.requestContext.getPropertyList().getPropertyList("pagedata"));
        bindMap.put("user", this.connectionInfo.getUserAttributeMap());
        bindMap.put("policy", new GroovyPolicyUtil(this.pageContext));
        String sdcid = this.element.getProperty("sdcid", this.requestContext != null ? this.requestContext.getProperty("sdcid") : "");
        if (sdcid != null && sdcid.length() > 0) {
            bindMap.put("sdc", this.getSDCProcessor().getSDCProperties(sdcid));
        }
        return bindMap;
    }

    public static enum ToolbarType {
        buttons(29, 0),
        ribbon(90, 3),
        compactRibbon(54, 2),
        hybridRibbon(24, 1),
        modern(35, 1);

        int height;
        int buttonsPerRow;

        public int getHeight() {
            return this.height;
        }

        public int buttonsPerRow() {
            return this.buttonsPerRow;
        }

        private ToolbarType(int h, int b) {
            this.height = h;
            this.buttonsPerRow = b;
        }
    }
}

