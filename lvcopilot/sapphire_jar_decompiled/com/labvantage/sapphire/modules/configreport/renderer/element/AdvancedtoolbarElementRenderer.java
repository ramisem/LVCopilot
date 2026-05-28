/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdvancedtoolbarElementRenderer
extends BaseElementRenderer {
    private String sdcid = "";

    @Override
    public ConfigReportContent report(String elementId, PropertyList elementProps, PropertyList refElementProps, PropertyDefinitionList defList, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) throws SapphireException {
        DataSet buttonRoles;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Advanced Toolbar");
        String title = "Advanced Toolbar";
        if (!elementId.equals("advancedtoolbar")) {
            title = "Advanced Toolbar (" + elementId + ")";
        }
        configReportContent.startSubSection(title, "");
        this.sdcid = elementProps.getProperty("sdcid");
        ConfigReportContent toolbar = new ConfigReportContent(this.config, "Element");
        DataSet[] toolBarInfo = this.getToolBarInfo(elementProps, refElementProps, includeDiffReport);
        if (toolBarInfo[0].getRowCount() > 0) {
            toolbar.append("<P>These are the list of buttons displayed:<P>");
            toolbar.renderListTable(toolBarInfo[0], this.translationProcessor);
        }
        if (toolBarInfo[1].getRowCount() > 0) {
            toolbar.append("<P>These are the list of operations displayed:<P>");
            toolbar.renderListTable(toolBarInfo[1], this.translationProcessor);
        }
        if ((buttonRoles = this.getButtonRoleMatrix(elementProps)).getRowCount() > 0 && buttonRoles.getColumnCount() > 2) {
            toolbar.startSubHeading("Role Matrix", "");
            toolbar.renderListTable(buttonRoles, this.translationProcessor);
        }
        configReportContent.appendSubSection(toolbar, title, this.diffOnly);
        configReportContent.endSubSection("", elementId);
        return configReportContent;
    }

    public DataSet[] getToolBarInfo(PropertyList toolbar, PropertyList refToolbar, boolean includeDiffReport) throws SapphireException {
        PropertyListCollection buttons = toolbar.getCollectionNotNull("buttons");
        PropertyListCollection refButtons = null;
        if (includeDiffReport && refToolbar != null) {
            refButtons = refToolbar.getCollectionNotNull("buttons");
        }
        DataSet[] returnVal = new DataSet[2];
        boolean hasButtons = false;
        for (PropertyList button : buttons) {
            PropertyList common = button.getPropertyListNotNull("commonprops");
            hasButtons |= !common.getProperty("show").equals("N");
        }
        DataSet buttonList = new DataSet();
        DataSet operationList = new DataSet();
        buttonList.setColidCaseSensitive(true);
        buttonList.addColumn("Text", 0);
        buttonList.addColumn("Type", 0);
        buttonList.addColumn("Image", 0);
        buttonList.addColumn("Operation", 0);
        buttonList.addColumn("E-signature", 0);
        operationList.setColidCaseSensitive(true);
        operationList.addColumn("Text", 0);
        operationList.addColumn("Type", 0);
        operationList.addColumn("Image", 0);
        operationList.addColumn("Operation", 0);
        operationList.addColumn("E-signature", 0);
        if (hasButtons) {
            String mode;
            String show;
            for (PropertyList buttonProps : buttons) {
                PropertyList commonProps = buttonProps.getPropertyListNotNull("commonprops");
                PropertyList refButtonProps = null;
                PropertyList refCommonProps = null;
                if (refToolbar != null && refButtons != null) {
                    for (PropertyList checkButtonProps : refButtons) {
                        String currId = checkButtonProps.getId();
                        if (!currId.equals(buttonProps.getId())) continue;
                        refButtonProps = checkButtonProps;
                        refCommonProps = checkButtonProps.getPropertyListNotNull("commonprops");
                    }
                }
                show = commonProps.getProperty("show");
                mode = commonProps.getProperty("mode");
                if (show.equals("N")) continue;
                if ("Button".equals(mode) || "Both".equals(mode)) {
                    int buttonrow = buttonList.addRow();
                    this.addDetailsToList(buttonList, buttonrow, buttonProps, commonProps, refButtonProps, refCommonProps, includeDiffReport);
                }
                if (!"Operation".equals(mode) && !"Both".equals(mode)) continue;
                int operationrow = operationList.addRow();
                this.addDetailsToList(operationList, operationrow, buttonProps, commonProps, refButtonProps, refCommonProps, includeDiffReport);
            }
            if (refButtons != null) {
                for (PropertyList refbuttonProps : refButtons) {
                    PropertyList refcommonProps = refbuttonProps.getPropertyListNotNull("commonprops");
                    PropertyList srcButtonProps = null;
                    PropertyList srcCommonProps = null;
                    if (toolbar != null && buttons != null) {
                        for (PropertyList checkButtonProps : buttons) {
                            String currId = checkButtonProps.getId();
                            if (!currId.equals(refbuttonProps.getId())) continue;
                            srcButtonProps = checkButtonProps;
                            srcCommonProps = checkButtonProps.getPropertyListNotNull("commonprops");
                        }
                    }
                    show = refcommonProps.getProperty("show");
                    mode = refcommonProps.getProperty("mode");
                    if (show.equals("N") || srcCommonProps != null && !srcCommonProps.getProperty("show").equals("N")) continue;
                    if ("Button".equals(mode) || "Both".equals(mode)) {
                        int buttonrow = buttonList.addRow();
                        this.addDetailsToList(buttonList, buttonrow, srcButtonProps, srcCommonProps, refbuttonProps, refcommonProps, includeDiffReport);
                    }
                    if (!"Operation".equals(mode) && !"Both".equals(mode)) continue;
                    int operationrow = operationList.addRow();
                    this.addDetailsToList(operationList, operationrow, srcButtonProps, srcCommonProps, refbuttonProps, refcommonProps, includeDiffReport);
                }
            }
        }
        returnVal[0] = buttonList;
        returnVal[1] = operationList;
        return returnVal;
    }

    protected void copyFile(File in, File out) throws Exception {
        if (in.exists()) {
            out.getParentFile().mkdirs();
            FileChannel sourceChannel = new FileInputStream(in).getChannel();
            FileChannel destinationChannel = new FileOutputStream(out).getChannel();
            sourceChannel.transferTo(0L, sourceChannel.size(), destinationChannel);
            sourceChannel.close();
            destinationChannel.close();
        }
    }

    private void addDetailsToList(DataSet list, int row, PropertyList buttonProps, PropertyList commonProps, PropertyList refButtonProps, PropertyList refCommonProps, boolean includeDiffReport) throws SapphireException {
        if (!includeDiffReport) {
            String depsecurity;
            list.setString(row, "Type", buttonProps.getProperty("buttontype", "Unknown"));
            list.setString(row, "Text", commonProps.getProperty("text"));
            list.setString(row, "Image", commonProps.getProperty("image"));
            if (buttonProps.getAttribute("rolelist").length() > 0) {
                list.setString(row, "Roles", buttonProps.getAttribute("rolelist").replaceAll(";", ", "));
            }
            if ((depsecurity = buttonProps.getPropertyListNotNull("depsecurity").getProperty("operation")) != null && depsecurity.length() > 0) {
                if (!list.isValidColumn("Dept-security")) {
                    list.addColumn("Dept-security", 0);
                }
                list.setString(row, "Dept-security", depsecurity);
            }
            String esigval = "Not Required";
            PropertyList esig = buttonProps.getPropertyListNotNull("esig");
            if ("Y".equals(esig.getProperty("required"))) {
                esigval = "Required";
            }
            list.setString(row, "E-signature", esigval);
            String operation = this.getButtonOperation(buttonProps.getProperty("buttontype", "Unknown"), buttonProps);
            list.setString(row, "Operation", operation);
            if (list.getString(row, "Image").length() > 0) {
                String srcImageDir = this.applicationRoot;
                try {
                    String buttonImageName = list.getString(row, "Image");
                    this.copyFile(new File(srcImageDir + buttonImageName), new File(this.folder + "/images/" + buttonImageName));
                    list.setString(row, "Image", "<img src=\"../images/" + buttonImageName + "\"/>");
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        } else if (refButtonProps == null || refCommonProps != null && refCommonProps.getProperty("show").equals("N")) {
            String depsecurity;
            list.setString(row, "Type", ConfigReportContent.getNewString(buttonProps.getProperty("buttontype", "Unknown")));
            list.setString(row, "Text", ConfigReportContent.getNewString(commonProps.getProperty("text")));
            list.setString(row, "Image", commonProps.getProperty("image"));
            if (buttonProps.getAttribute("rolelist").length() > 0) {
                list.setString(row, "Roles", ConfigReportContent.getNewString(buttonProps.getAttribute("rolelist").replaceAll(";", ", ")));
            }
            if ((depsecurity = buttonProps.getPropertyListNotNull("depsecurity").getProperty("operation")) != null && depsecurity.length() > 0) {
                if (!list.isValidColumn("Dept-security")) {
                    list.addColumn("Dept-security", 0);
                }
                list.setString(row, "Dept-security", ConfigReportContent.getNewString(depsecurity));
            }
            String esigval = "Not Required";
            PropertyList esig = buttonProps.getPropertyListNotNull("esig");
            if ("Y".equals(esig.getProperty("required"))) {
                esigval = "Required";
            }
            list.setString(row, "E-signature", ConfigReportContent.getNewString(esigval));
            String operation = this.getButtonOperation(buttonProps.getProperty("buttontype", "Unknown"), buttonProps);
            list.setString(row, "Operation", ConfigReportContent.getNewString(operation));
            if (list.getString(row, "Image").length() > 0) {
                String srcImageDir = this.applicationRoot;
                try {
                    String buttonImageName = list.getString(row, "Image");
                    this.copyFile(new File(srcImageDir + buttonImageName), new File(this.folder + "/images/" + buttonImageName));
                    list.setString(row, "Image", "<img src=\"../images/" + buttonImageName + "\"/>");
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        } else if (buttonProps == null || commonProps != null && commonProps.getProperty("show").equals("N")) {
            String depsecurity;
            list.setString(row, "Type", ConfigReportContent.getDeletedString(refButtonProps.getProperty("buttontype", "Unknown")));
            list.setString(row, "Text", ConfigReportContent.getDeletedString(refCommonProps.getProperty("text")));
            list.setString(row, "Image", refCommonProps.getProperty("image"));
            if (refButtonProps.getAttribute("rolelist").length() > 0) {
                list.setString(row, "Roles", ConfigReportContent.getDeletedString(refButtonProps.getAttribute("rolelist").replaceAll(";", ", ")));
            }
            if ((depsecurity = refButtonProps.getPropertyListNotNull("depsecurity").getProperty("operation")) != null && depsecurity.length() > 0) {
                if (!list.isValidColumn("Dept-security")) {
                    list.addColumn("Dept-security", 0);
                }
                list.setString(row, "Dept-security", ConfigReportContent.getDeletedString(depsecurity));
            }
            String esigval = "Not Required";
            PropertyList esig = refButtonProps.getPropertyListNotNull("esig");
            if ("Y".equals(esig.getProperty("required"))) {
                esigval = "Required";
            }
            list.setString(row, "E-signature", ConfigReportContent.getDeletedString(esigval));
            String operation = this.getButtonOperation(refButtonProps.getProperty("buttontype", "Unknown"), refButtonProps);
            list.setString(row, "Operation", ConfigReportContent.getDeletedString(operation));
            if (list.getString(row, "Image").length() > 0) {
                String srcImageDir = this.applicationRoot;
                try {
                    String buttonImageName = list.getString(row, "Image");
                    this.copyFile(new File(srcImageDir + buttonImageName), new File(this.folder + "/images/" + buttonImageName));
                    list.setString(row, "Image", "<img src=\"../images/" + buttonImageName + "\"/>");
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
        } else {
            String srcImageDir;
            list.setString(row, "Type", ConfigReportContent.getDiffString(buttonProps.getProperty("buttontype", "Unknown"), refButtonProps.getProperty("buttontype", "Unknown")));
            list.setString(row, "Text", ConfigReportContent.getDiffString(commonProps.getProperty("text"), refCommonProps.getProperty("text")));
            String img = this.copyButtonImage(commonProps.getProperty("image"));
            String refimg = this.copyButtonImage(refCommonProps.getProperty("image"));
            if (img.length() > 0) {
                srcImageDir = this.applicationRoot;
                try {
                    this.copyFile(new File(srcImageDir + img), new File(this.folder + "/images/" + img));
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
            if (refimg.length() > 0) {
                srcImageDir = this.applicationRoot;
                try {
                    this.copyFile(new File(srcImageDir + refimg), new File(this.folder + "/images/" + refimg));
                }
                catch (Exception e) {
                    throw new SapphireException(e);
                }
            }
            list.setString(row, "Image", ConfigReportContent.getDiffString(img, refimg));
            String roleList = buttonProps.getAttribute("rolelist");
            roleList = roleList != null ? roleList.replaceAll(";", ", ") : "";
            String refRoleList = refButtonProps.getAttribute("rolelist");
            refRoleList = refRoleList != null ? refRoleList.replaceAll(";", ", ") : "";
            list.setString(row, "Roles", ConfigReportContent.getDiffString(roleList, refRoleList));
            String depsecurity = buttonProps.getPropertyListNotNull("depsecurity").getProperty("operation", "");
            String refdepsecurity = refButtonProps.getPropertyListNotNull("depsecurity").getProperty("operation", "");
            if (depsecurity.length() > 0 || refdepsecurity.length() > 0) {
                list.setString(row, "Dept-security", ConfigReportContent.getDiffString(depsecurity, refdepsecurity));
            }
            String esigval = "Not Required";
            PropertyList esig = buttonProps.getPropertyListNotNull("esig");
            if ("Y".equals(esig.getProperty("required"))) {
                esigval = "Required";
            }
            String refesigval = "Not Required";
            PropertyList refesig = refButtonProps.getPropertyListNotNull("esig");
            if ("Y".equals(refesig.getProperty("required"))) {
                refesigval = "Required";
            }
            list.setString(row, "E-signature", ConfigReportContent.getDiffString(esigval, refesigval));
            String operation = this.getButtonOperation(buttonProps.getProperty("buttontype", "Unknown"), buttonProps);
            String refoperation = this.getButtonOperation(refButtonProps.getProperty("buttontype", "Unknown"), refButtonProps);
            list.setString(row, "Operation", ConfigReportContent.getDiffString(operation, refoperation));
        }
    }

    private String getButtonOperation(String type, PropertyList buttonProps) {
        String operation = "";
        if (type.equals("Standard")) {
            PropertyList standard = buttonProps.getPropertyListNotNull("standardbuttonprops");
            String action = standard.getProperty("action");
            String page = standard.getProperty("page");
            page = StringUtil.replaceAll(page, "[sdcid]", this.sdcid);
            String pageFKLink = ConfigReportContent.renderLink(page, this.sdisIncluded, this.frames, this.connection);
            operation = "Function: " + action;
            if (page.length() > 0) {
                operation = operation + " (Using " + pageFKLink + ")";
            }
        } else if (type.equals("Action")) {
            PropertyList actionprops = buttonProps.getPropertyListNotNull("actionbuttonprops");
            PropertyListCollection actions = actionprops.getCollectionNotNull("actions");
            StringBuffer actionList = new StringBuffer();
            for (PropertyList action : actions) {
                String fklink = ConfigReportContent.createHyperLink("Action", action.getProperty("actionid"), "1", "", this.sdisIncluded, this.frames);
                actionList.append(", ").append(fklink);
            }
            operation = "Actions: " + (actionList.length() > 0 ? actionList.substring(2) : "None");
        } else if (type.equals("User")) {
            PropertyList user = buttonProps.getPropertyListNotNull("userbuttonprops");
            String javascript = user.getProperty("action");
            operation = "JavaScript: " + javascript;
        }
        return operation;
    }

    private String copyButtonImage(String image) throws SapphireException {
        String srcImageDir = this.applicationRoot;
        String url = "";
        if (image.length() > 0) {
            try {
                this.copyFile(new File(srcImageDir + image), new File(this.folder + "/images/" + image));
                url = "<img src=\"../images/" + image + "\"/>";
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
        return url;
    }

    public DataSet getButtonRoleMatrix(PropertyList toolbar) throws SapphireException {
        PropertyListCollection buttons = toolbar.getCollectionNotNull("buttons");
        boolean hasButtons = false;
        for (PropertyList button : buttons) {
            PropertyList common = button.getPropertyListNotNull("commonprops");
            hasButtons |= !common.getProperty("show").equals("N");
        }
        DataSet buttonRoleList = new DataSet();
        buttonRoleList.setColidCaseSensitive(true);
        if (hasButtons) {
            for (PropertyList buttonProps : buttons) {
                PropertyList commonProps = buttonProps.getPropertyListNotNull("commonprops");
                String show = commonProps.getProperty("show");
                if (show.equals("N")) continue;
                String text = commonProps.getProperty("text");
                String roles = buttonProps.getAttribute("rolelist");
                buttonRoleList.addRow();
                buttonRoleList.setString(buttonRoleList.size() - 1, "Button/Operation", text);
                if (roles.length() <= 0) continue;
                String[] rolesArr = StringUtil.split(roles, ";");
                for (int i = 0; i < rolesArr.length; ++i) {
                    if (!buttonRoleList.isValidColumn(rolesArr[i])) {
                        buttonRoleList.addColumn(rolesArr[i], 0);
                    }
                    String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + rolesArr[i] + "\" title=\"" + rolesArr[i] + "\">";
                    buttonRoleList.setString(buttonRoleList.size() - 1, rolesArr[i], includeImg);
                }
            }
        }
        return buttonRoleList;
    }
}

