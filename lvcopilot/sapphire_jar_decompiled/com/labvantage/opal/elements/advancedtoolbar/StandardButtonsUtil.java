/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.advancedtoolbar;

import com.labvantage.opal.util.ExecuteAction;
import com.labvantage.opal.util.OpalUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class StandardButtonsUtil {
    static String LABVANTAGE_CVS_ID = "$Revision: 77782 $";
    public static final String BUTTON_TYPE_DELETE = "Delete";
    public static final String BUTTON_TYPE_COPY = "Copy";
    public static final String BUTTON_TYPE_NEWVERSION = "NewVersion";
    public static final String BUTTON_TYPE_EXPIREVERSION = "ExpireVersion";
    public static final String BUTTON_TYPE_APPROVEVERSION = "ApproveVersion";
    public static final int ACTION_BLOCK_SUCCESS = 1;
    public static final int ACTION_BLOCK_FAIL = 2;
    public static final int ACTION_BLOCK_FAIL_UNIDENTIFIED_ERROR = 3;

    public static String executeMaintListStandardButton(PageContext pageContext) {
        StringBuffer msg = new StringBuffer();
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
        String mode = pagedata.getProperty("mode");
        if (mode.equalsIgnoreCase(BUTTON_TYPE_DELETE)) {
            msg.append(StandardButtonsUtil.executeDeleteSB(pageContext, pagedata));
        } else if (mode.equalsIgnoreCase(BUTTON_TYPE_COPY)) {
            msg.append(StandardButtonsUtil.executeCopySB(pageContext, pagedata));
        } else if (mode.equalsIgnoreCase(BUTTON_TYPE_NEWVERSION)) {
            msg.append(StandardButtonsUtil.executeNewVersionSB(pageContext, pagedata));
        } else if (mode.equalsIgnoreCase(BUTTON_TYPE_EXPIREVERSION)) {
            msg.append(StandardButtonsUtil.executeExpireVersionSB(pageContext, pagedata));
        } else if (mode.equalsIgnoreCase(BUTTON_TYPE_APPROVEVERSION)) {
            msg.append(StandardButtonsUtil.executeApproveVersionSB(pageContext, pagedata));
        }
        return msg.toString();
    }

    public static String executeMaintFormStandardButton(PageContext pageContext) {
        StringBuffer msg = new StringBuffer();
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", pageContext);
        String mode = pagedata.getProperty("mode");
        if (mode.equalsIgnoreCase(BUTTON_TYPE_APPROVEVERSION)) {
            msg.append(StandardButtonsUtil.executeApproveVersionSB(pageContext, pagedata));
        }
        return msg.toString();
    }

    private static String executeDeleteSB(PageContext pageContext, PropertyList pagedata) {
        StringBuffer msg = new StringBuffer();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", pagedata.getProperty("sdcid"));
        props.put("keyid1", pagedata.getProperty("keyid1"));
        props.put("keyid2", pagedata.getProperty("keyid2"));
        props.put("keyid3", pagedata.getProperty("keyid3"));
        String[] exptraPropsArr = StringUtil.split(pagedata.getProperty("__pr_extraprops"), ";");
        Map extraProps = OpalUtil.toMap(exptraPropsArr);
        if (extraProps != null) {
            String auditsignedflag;
            String auditactivity;
            String auditreason = (String)extraProps.get("auditreason");
            if (auditreason != null) {
                props.put("auditreason", auditreason);
            }
            if ((auditactivity = (String)extraProps.get("auditactivity")) != null) {
                props.put("auditactivity", auditactivity);
            }
            if ((auditsignedflag = (String)extraProps.get("auditsignedflag")) != null) {
                props.put("auditsignedflag", auditsignedflag);
            }
        }
        props.put("applylock", "Y");
        if (pagedata.getProperty("__sdcruleconfirm").equalsIgnoreCase("y")) {
            props.put("__sdcruleconfirm", pagedata.getProperty("__sdcruleconfirm"));
        }
        String actionId = pagedata.getProperty("sdcid").equals("LV_Activity") ? "DeleteActivity" : "DeleteSDI";
        HashMap returnValues = StandardButtonsUtil.executeStandardButton(pageContext, actionId, "1", props);
        msg.append(StandardButtonsUtil.postExecuteDeleteSB(pageContext, pagedata, returnValues));
        return msg.toString();
    }

    private static String postExecuteDeleteSB(PageContext pageContext, PropertyList pagedata, HashMap actionBlockInfo) {
        StringBuffer msg = new StringBuffer();
        int actionBlockStatus = Integer.parseInt((String)actionBlockInfo.get("actionblockstatus"));
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        boolean isSingleSDI = true;
        if (pagedata.getProperty("keyid1").indexOf(";") > 0) {
            isSingleSDI = false;
        }
        String sdcname = OpalUtil.getSDCName(pagedata.getProperty("sdcid"));
        String encodedKeyid1 = SafeHTML.encodeForHTML(pagedata.getProperty("keyid1"));
        if (actionBlockStatus == 1) {
            if (isSingleSDI) {
                msg.append(tp.translate(sdcname)).append(" \"");
                msg.append(encodedKeyid1).append("\" ").append(tp.translate("was successfully deleted")).append(".");
            } else {
                msg.append(tp.translate(sdcname + "(s)")).append(" \"");
                msg.append(encodedKeyid1).append("\" ").append(tp.translate("were successfully deleted")).append(".");
            }
        } else {
            if (isSingleSDI) {
                msg.append(tp.translate(sdcname)).append(" \"");
                msg.append(encodedKeyid1).append("\" ").append(tp.translate("could not be deleted")).append(".");
            } else {
                msg.append(tp.translate(sdcname + "(s)")).append(" \"");
                msg.append(encodedKeyid1).append("\" ").append(tp.translate("could not be deleted")).append(".");
            }
            if (actionBlockStatus == 3) {
                msg.append(" See your system administrator for more help. ");
            }
        }
        return ExecuteAction.getErrorMessage(pageContext, actionBlockInfo, msg.toString());
    }

    private static String executeNewVersionSB(PageContext pageContext, PropertyList pagedata) {
        String templateFlag;
        StringBuffer msg = new StringBuffer();
        HashMap<String, String> props = new HashMap<String, String>();
        String sdcid = pagedata.getProperty("sdcid");
        String keyid1 = pagedata.getProperty("keyid1");
        String keyid2 = pagedata.getProperty("keyid2");
        String keyid3 = pagedata.getProperty("keyid3");
        props.put("sdcid", sdcid);
        props.put("keyid1", keyid1);
        props.put("keyid2", keyid2);
        props.put("keyid3", keyid3);
        String[] exptraPropsArr = StringUtil.split(pagedata.getProperty("__pr_extraprops"), ";");
        Map extraProps = OpalUtil.toMap(exptraPropsArr);
        if (extraProps != null) {
            String auditsignedflag;
            String auditactivity;
            String auditreason = (String)extraProps.get("auditreason");
            if (auditreason != null) {
                props.put("auditreason", auditreason);
            }
            if ((auditactivity = (String)extraProps.get("auditactivity")) != null) {
                props.put("auditactivity", auditactivity);
            }
            if ((auditsignedflag = (String)extraProps.get("auditsignedflag")) != null) {
                props.put("auditsignedflag", auditsignedflag);
            }
        }
        props.put("applylock", "Y");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setKeyid1List(keyid1);
        sdiRequest.setKeyid2List(keyid2);
        sdiRequest.setKeyid3List(keyid3);
        sdiRequest.setRequestItem("primary");
        SDIProcessor sdiProcessor = new SDIProcessor(pageContext);
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet primary = sdiData.getDataset("primary");
        if (primary.getRowCount() > 0 && "Y".equals(templateFlag = primary.getString(0, "templateflag", "N"))) {
            props.put("templateflag", "Y");
        }
        HashMap returnValues = StandardButtonsUtil.executeStandardButton(pageContext, "AddSDIVersion", "1", props);
        msg.append(StandardButtonsUtil.postExecuteNewVersionSB(pageContext, pagedata, returnValues));
        return msg.toString();
    }

    private static String executeExpireVersionSB(PageContext pageContext, PropertyList pagedata) {
        StringBuffer msg = new StringBuffer();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", pagedata.getProperty("sdcid"));
        props.put("keyid1", pagedata.getProperty("keyid1"));
        props.put("keyid2", pagedata.getProperty("keyid2"));
        props.put("keyid3", pagedata.getProperty("keyid3"));
        props.put("versionstatus", "E");
        String[] exptraPropsArr = StringUtil.split(pagedata.getProperty("__pr_extraprops"), ";");
        Map extraProps = OpalUtil.toMap(exptraPropsArr);
        if (extraProps != null) {
            String auditsignedflag;
            String auditactivity;
            String auditreason = (String)extraProps.get("auditreason");
            if (auditreason != null) {
                props.put("auditreason", auditreason);
            }
            if ((auditactivity = (String)extraProps.get("auditactivity")) != null) {
                props.put("auditactivity", auditactivity);
            }
            if ((auditsignedflag = (String)extraProps.get("auditsignedflag")) != null) {
                props.put("auditsignedflag", auditsignedflag);
            }
        }
        props.put("applylock", "Y");
        HashMap returnValues = StandardButtonsUtil.executeStandardButton(pageContext, "SetSDIVersionStatus", "1", props);
        msg.append(StandardButtonsUtil.postExecuteExpireVersionSB(pageContext, pagedata, returnValues));
        return msg.toString();
    }

    private static String executeApproveVersionSB(PageContext pageContext, PropertyList pagedata) {
        StringBuffer msg = new StringBuffer();
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", pagedata.getProperty("sdcid"));
        props.put("keyid1", pagedata.getProperty("keyid1"));
        props.put("keyid2", pagedata.getProperty("keyid2"));
        props.put("keyid3", pagedata.getProperty("keyid3"));
        props.put("versionstatus", "C");
        String[] exptraPropsArr = StringUtil.split(pagedata.getProperty("__pr_extraprops"), ";");
        Map extraProps = OpalUtil.toMap(exptraPropsArr);
        if (extraProps != null) {
            String auditsignedflag;
            String auditactivity;
            String auditreason = (String)extraProps.get("auditreason");
            if (auditreason != null) {
                props.put("auditreason", auditreason);
            }
            if ((auditactivity = (String)extraProps.get("auditactivity")) != null) {
                props.put("auditactivity", auditactivity);
            }
            if ((auditsignedflag = (String)extraProps.get("auditsignedflag")) != null) {
                props.put("auditsignedflag", auditsignedflag);
            }
        }
        props.put("applylock", "Y");
        HashMap returnValues = StandardButtonsUtil.executeStandardButton(pageContext, "SetSDIVersionStatus", "1", props);
        msg.append(StandardButtonsUtil.postExecuteApproveVersionSB(pageContext, pagedata, returnValues));
        return msg.toString();
    }

    private static String postExecuteNewVersionSB(PageContext pageContext, PropertyList pagedata, HashMap actionBlockInfo) {
        StringBuffer msg = new StringBuffer();
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        int actionBlockStatus = Integer.parseInt((String)actionBlockInfo.get("actionblockstatus"));
        String encodedKeyid1 = SafeHTML.encodeForHTML(pagedata.getProperty("keyid1"));
        if (actionBlockStatus == 1) {
            msg.append(tp.translate("New version for")).append(" ").append(encodedKeyid1).append(" ").append(tp.translate("was successfully added."));
            msg.append(ExecuteAction.getRedirectScript(pagedata.getProperty("gotopage", ""), "N", actionBlockInfo, pageContext));
        } else {
            msg.append(tp.translate("New version for")).append(" ").append(encodedKeyid1).append(" ").append(tp.translate(" could not be created."));
            if (actionBlockStatus == 3) {
                msg.append(" ").append(tp.translate("See your system administrator for more help.")).append(" ");
            }
        }
        return ExecuteAction.getErrorMessage(pageContext, actionBlockInfo, msg.toString());
    }

    private static String postExecuteExpireVersionSB(PageContext pageContext, PropertyList pagedata, HashMap actionBlockInfo) {
        StringBuffer msg = new StringBuffer();
        String encodedKeyid1 = SafeHTML.encodeForHTML(pagedata.getProperty("keyid1"));
        int actionBlockStatus = Integer.parseInt((String)actionBlockInfo.get("actionblockstatus"));
        if (actionBlockStatus == 1) {
            msg.append(encodedKeyid1).append(" was successfully Expired.");
            msg.append(ExecuteAction.getRedirectScript(pagedata.getProperty("gotopage", ""), "N", actionBlockInfo, pageContext));
        } else {
            msg.append(encodedKeyid1).append(" could not be Expired.");
            if (actionBlockStatus == 3) {
                msg.append(" See your system administrator for more help. ");
            }
        }
        return ExecuteAction.getErrorMessage(pageContext, actionBlockInfo, msg.toString());
    }

    private static String postExecuteApproveVersionSB(PageContext pageContext, PropertyList pagedata, HashMap actionBlockInfo) {
        StringBuffer msg = new StringBuffer();
        int actionBlockStatus = Integer.parseInt((String)actionBlockInfo.get("actionblockstatus"));
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String encodedKeyid1 = SafeHTML.encodeForHTML(pagedata.getProperty("keyid1"));
        if (actionBlockStatus == 1) {
            msg.append(encodedKeyid1).append(" ").append(tp.translate("was successfully Approved")).append(".");
            msg.append(ExecuteAction.getRedirectScript(pagedata.getProperty("gotopage", ""), "N", actionBlockInfo, pageContext));
        } else {
            msg.append(encodedKeyid1).append(" ").append(tp.translate("could not be Approved")).append(".");
            if (actionBlockStatus == 3) {
                msg.append(" ").append(tp.translate("See your system administrator for more help")).append(". ");
            }
        }
        return ExecuteAction.getErrorMessage(pageContext, actionBlockInfo, msg.toString());
    }

    private static String executeCopySB(PageContext pageContext, PropertyList pagedata) {
        String templateFlag;
        StringBuffer msg = new StringBuffer();
        HashMap<String, String> props = new HashMap<String, String>();
        String sdcid = pagedata.getProperty("copysdcid");
        String templatekeyid1 = pagedata.getProperty("templatekeyid1");
        String templatekeyid2 = pagedata.getProperty("templatekeyid2");
        String templatekeyid3 = pagedata.getProperty("templatekeyid3");
        props.put("sdcid", sdcid);
        props.put("copysdi", "Y");
        props.put("copies", pagedata.getProperty("copycount"));
        props.put("keyid1", pagedata.getProperty("copykeyid1list"));
        props.put("keyid2", pagedata.getProperty("copykeyid2list"));
        props.put("keyid3", pagedata.getProperty("copykeyid3list"));
        props.put("templatekeyid1", templatekeyid1);
        props.put("templatekeyid2", templatekeyid2);
        props.put("templatekeyid3", templatekeyid3);
        String[] exptraPropsArr = StringUtil.split(pagedata.getProperty("__pr_extraprops"), ";");
        Map extraProps = OpalUtil.toMap(exptraPropsArr);
        if (extraProps != null) {
            String auditsignedflag;
            String auditactivity;
            String auditreason = (String)extraProps.get("auditreason");
            if (auditreason != null) {
                props.put("auditreason", auditreason);
            }
            if ((auditactivity = (String)extraProps.get("auditactivity")) != null) {
                props.put("auditactivity", auditactivity);
            }
            if ((auditsignedflag = (String)extraProps.get("auditsignedflag")) != null) {
                props.put("auditsignedflag", auditsignedflag);
            }
        }
        props.put("applylock", "Y");
        if (pagedata.getProperty("__sdcruleconfirm").equalsIgnoreCase("y")) {
            props.put("__sdcruleconfirm", pagedata.getProperty("__sdcruleconfirm"));
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setKeyid1List(templatekeyid1);
        sdiRequest.setKeyid2List(templatekeyid2);
        sdiRequest.setKeyid3List(templatekeyid3);
        sdiRequest.setRequestItem("primary");
        SDIProcessor sdiProcessor = new SDIProcessor(pageContext);
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet primary = sdiData.getDataset("primary");
        if (primary.getRowCount() > 0 && "Y".equals(templateFlag = primary.getString(0, "templateflag", "N"))) {
            props.put("templateflag", "Y");
        }
        HashMap returnValues = StandardButtonsUtil.executeStandardButton(pageContext, "AddSDI", "1", props);
        msg.append(StandardButtonsUtil.postExecuteCopySB(pageContext, pagedata, returnValues));
        return msg.toString();
    }

    private static String postExecuteCopySB(PageContext pageContext, PropertyList pagedata, HashMap actionBlockInfo) {
        StringBuffer msg = new StringBuffer();
        int actionBlockStatus = Integer.parseInt((String)actionBlockInfo.get("actionblockstatus"));
        boolean isSingleSDI = true;
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        if (pagedata.getProperty("keyid1").indexOf(";") > 0) {
            isSingleSDI = false;
        }
        String sdcname = OpalUtil.getSDCName(pagedata.getProperty("sdcid"));
        String encodedtemplatekeyid1 = SafeHTML.encodeForHTML(pagedata.getProperty("templatekeyid1"));
        if (actionBlockStatus == 1) {
            if (isSingleSDI) {
                msg.append(tp.translate(sdcname)).append(" \"");
                msg.append(encodedtemplatekeyid1).append("\" ").append(tp.translate("was successfully copied")).append(".");
            } else {
                msg.append(tp.translate(sdcname + "(s)")).append(" \"");
                msg.append(encodedtemplatekeyid1).append("\" ").append(tp.translate("were successfully copied")).append(".");
            }
            msg.append(ExecuteAction.getRedirectScript(pagedata.getProperty("gotopage", ""), "N", actionBlockInfo, pageContext));
        } else {
            if (isSingleSDI) {
                msg.append(tp.translate(sdcname)).append(" \"");
                msg.append(encodedtemplatekeyid1).append("\" ").append(tp.translate("could not be copied")).append(".");
            } else {
                msg.append(tp.translate(sdcname + "(s)")).append(" \"");
                msg.append(encodedtemplatekeyid1).append("\" ").append(tp.translate("could not be copied")).append(".");
            }
            if (actionBlockStatus == 3) {
                msg.append(" See your system administrator for more help. ");
            }
        }
        return ExecuteAction.getErrorMessage(pageContext, actionBlockInfo, msg.toString());
    }

    private static HashMap executeStandardButton(PageContext pageContext, String actionid, String actionVersionId, HashMap props) {
        ActionBlock ab = new ActionBlock();
        ActionProcessor ap = new ActionProcessor(pageContext);
        HashMap<String, Object> returnValues = new HashMap<String, Object>();
        returnValues.put("actionblockstatus", Integer.toString(2));
        try {
            ab.setAction("StandardAction", actionid, actionVersionId, props);
            ap.processActionBlock(ab);
            returnValues.putAll(ab.getActionProperties("StandardAction"));
            returnValues.put("actionblockstatus", Integer.toString(1));
            returnValues.put("errorhandler", ap.getErrorHandler());
        }
        catch (ActionException ae) {
            ErrorHandler errorHandler = ae.getErrorHandler();
            if (errorHandler != null && errorHandler.hasErrors()) {
                returnValues.put("actionblockstatus", Integer.toString(2));
                returnValues.put("errorhandler", ae.getErrorHandler());
            }
            returnValues.put("actionblockstatus", Integer.toString(3));
            returnValues.put("errorhandler", ap.getErrorHandler());
            returnValues.put("errormsg", ap.getErrorStack().get(0).toString());
        }
        return returnValues;
    }
}

