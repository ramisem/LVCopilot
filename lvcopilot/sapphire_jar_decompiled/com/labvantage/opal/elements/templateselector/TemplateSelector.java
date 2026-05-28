/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.templateselector;

import com.labvantage.opal.util.SdcInfo;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.tagext.SDITagUtil;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TemplateSelector
extends BaseElement {
    public String LABVANTAGE_CVS_ID = "$Revision: 101920 $";
    private TranslationProcessor __Tp;
    private String __SdcId = "";
    private String __Keyid1 = "";
    private String __Keyid2 = "";
    private String __Keyid3 = "";
    private String __DefaultTemplate = "";
    private String __Style = "";
    private String __LookupPage = "";
    private String __QueryId = "";
    private String __QueryFrom = "";
    private String __QueryWhere = "";
    private String __Rows = "";
    private String __OnChangeCallback = "";
    private String __TemplateInfoLinkPage = "";
    private String __MaxRows = "";
    private String __DefaultCopies = "";
    private String __TemplateInputBoxName = "";
    private String __CopiesInputBoxName = "";
    private boolean __TemplateMandatory = false;
    private boolean __AllowMods = true;
    private boolean __ShowButtonToFireCallback = false;
    private boolean __ShowTemplateInfoLink = true;
    private boolean __FireOnchangeForTemplateSelector = true;
    private boolean __FireOnchangeForCopies = true;
    private boolean __ShowSelector = true;
    private boolean __ShowCopies = true;
    private final String TABLE_CLASS = "tblTemplateSelector";
    private final String TEMPLATE_TEXT = "tempateSelectorText";
    private final String TR_CLASS = "trTemplateSelectorRow";
    private final String TD_CLASS = "tdTemplateSelectorCell";
    private final String DD_CLASS = "ddTemplateSelector";
    private final String INP_TEMPLATE_CLASS = "inpTemplateSelector";
    private final String INP_COPIES_CLASS = "inpTemplateCopies";
    private final String TR_TEMPLATE_HEADER_CLASS = "trTemplateSelectorHeader";
    private final String TABLE_BUTTONS_CLASS = "tblAddRemoveButtons";
    private String __Label_SelectTemplate = "Select Template";
    private String __Label_Copies = "Copies";
    private String __Label_TemplateDetails = "Details";
    private String __Label_CallbackButton = "Reload Using Template";
    private String lookupimg = "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg";

    private void setTranslationProcessor() {
        this.__Tp = this.getTranslationProcessor();
    }

    @Override
    public String getHtml() {
        this.setTranslationProcessor();
        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<!-- START TEMPLATESELECTOR ELEMENT -->\n\n");
        if (this.element != null) {
            this.__SdcId = this.element.getProperty("sdcid");
            if (this.__SdcId.equalsIgnoreCase("")) {
                Trace.logError("OPAL_ERR: TemplateSelector.getHtml -> SDCId not defined.");
                return this.__Tp.translate("SdcId property not defined for the TemplateSelector element. Cannot continue.");
            }
            this.__Keyid1 = this.requestContext.getProperty("keyid1");
            this.__Keyid2 = this.requestContext.getProperty("keyid2");
            this.__Keyid3 = this.requestContext.getProperty("keyid3");
            PropertyList plTemplateRetrieval = this.element.getPropertyList("templateretrieval");
            PropertyList plCallbackButton = this.element.getPropertyList("callbackbutton");
            PropertyList plOnchangeCallback = this.element.getPropertyList("onchangecallback");
            PropertyList plTemplateInfoLink = this.element.getPropertyList("templateinfolink");
            PropertyList plMultipleSelectorRows = this.element.getPropertyList("multipleselectorrows");
            PropertyList plCopies = this.element.getPropertyList("copies");
            PropertyList plTexts = this.element.getPropertyListNotNull("texts");
            this.__Label_Copies = plTexts.getProperty("copieslabel", this.__Label_Copies);
            this.__Label_SelectTemplate = plTexts.getProperty("selecttemplatelabel", this.__Label_SelectTemplate);
            this.__Label_TemplateDetails = plTexts.getProperty("templatedetailslabel", this.__Label_TemplateDetails);
            this.__Label_CallbackButton = plTexts.getProperty("reloadusingtemplatelabel", this.__Label_CallbackButton);
            this.__Style = this.element.getProperty("style");
            if (this.__Style.equalsIgnoreCase("")) {
                this.__Style = "dropdown";
            }
            this.__Rows = this.element.getProperty("selectorrows");
            if (this.__Rows.equalsIgnoreCase("")) {
                this.__Rows = "single";
            }
            this.__TemplateInputBoxName = this.element.getProperty("templateinputboxname");
            this.__TemplateInputBoxName = !this.__TemplateInputBoxName.equalsIgnoreCase("") ? StringUtil.replaceAll(this.__TemplateInputBoxName, "[sdcid]", this.__SdcId) : "template";
            this.__CopiesInputBoxName = this.element.getProperty("copiesinputboxname");
            this.__CopiesInputBoxName = !this.__CopiesInputBoxName.equalsIgnoreCase("") ? StringUtil.replaceAll(this.__CopiesInputBoxName, "[sdcid]", this.__SdcId) : "copies";
            this.__ShowSelector = !plTemplateRetrieval.getProperty("show").equalsIgnoreCase("N");
            this.__DefaultTemplate = plTemplateRetrieval.getProperty("defaulttemplate");
            this.__TemplateMandatory = plTemplateRetrieval.getProperty("templatemandatory").equalsIgnoreCase("Y");
            this.__AllowMods = !plTemplateRetrieval.getProperty("allowmodification").equalsIgnoreCase("N");
            this.__LookupPage = plTemplateRetrieval.getProperty("templatelookuppage");
            this.__QueryId = plTemplateRetrieval.getProperty("templatequeryid");
            this.__QueryFrom = plTemplateRetrieval.getProperty("templatequeryfrom");
            this.__QueryWhere = plTemplateRetrieval.getProperty("templatequerywhere");
            this.__ShowButtonToFireCallback = plCallbackButton.getProperty("show").equalsIgnoreCase("Y");
            this.__OnChangeCallback = plOnchangeCallback.getProperty("action");
            this.__FireOnchangeForTemplateSelector = !plOnchangeCallback.getProperty("enablefortemplate").equalsIgnoreCase("N");
            this.__FireOnchangeForCopies = !plOnchangeCallback.getProperty("enableforcopies").equalsIgnoreCase("N");
            this.__ShowTemplateInfoLink = !plTemplateInfoLink.getProperty("show").equalsIgnoreCase("N");
            this.__TemplateInfoLinkPage = plTemplateInfoLink.getProperty("page");
            if (this.__TemplateInfoLinkPage.equalsIgnoreCase("")) {
                this.__TemplateInfoLinkPage = this.__Tp.translate("Lookup Page not defined");
            }
            this.__MaxRows = plMultipleSelectorRows.getProperty("maxrows");
            String __InitialRows = plMultipleSelectorRows.getProperty("initialrows");
            if (!__InitialRows.equalsIgnoreCase("") && !this.__MaxRows.equalsIgnoreCase("") && Integer.parseInt(__InitialRows) > Integer.parseInt(this.__MaxRows)) {
                __InitialRows = this.__MaxRows;
            }
            this.__ShowCopies = !plCopies.getProperty("show").equalsIgnoreCase("N");
            this.__DefaultCopies = plCopies.getProperty("defaultcopies");
            if (this.__DefaultCopies.equalsIgnoreCase("")) {
                this.__DefaultCopies = "1";
            }
            if (this.__Rows.equalsIgnoreCase("multiple")) {
                // empty if block
            }
            if (this.__ShowCopies) {
                // empty if block
            }
            if (this.__ShowTemplateInfoLink) {
                // empty if block
            }
            if (!this.__Rows.equalsIgnoreCase("single") || this.__ShowButtonToFireCallback) {
                // empty if block
            }
            if (this.__ShowSelector || this.__ShowCopies) {
                sbHtml.append("<script language=\"JavaScript\" src=\"WEB-OPAL/elements/templateselector/scripts/templateselector.js\"></script>\n");
                if (this.__Style.equalsIgnoreCase("lookup")) {
                    sbHtml.append("<script language=\"JavaScript\" src=\"WEB-CORE/elements/scripts/lookup.js\"></script>\n");
                }
                sbHtml.append("<table cellspacing=0 cellpadding=1 border=0 id=templateselectorwrapper>\n");
                sbHtml.append("<tr><td>\n");
                if (this.__Rows.equalsIgnoreCase("multiple")) {
                    sbHtml.append(this.getMultiTemplateSelectorHeader());
                }
                sbHtml.append("</td></tr>\n");
                sbHtml.append("<tr><td>\n");
                sbHtml.append("<table class=tblTemplateSelector cellspacing=0 cellpadding=1 border=0 id=templatetable width=100% height=100%>\n");
                if (this.__Rows.equalsIgnoreCase("single")) {
                    sbHtml.append(this.getSingleTemplateSelectorRow());
                } else {
                    sbHtml.append(this.getMultipleTemplateSelectorRow());
                }
                sbHtml.append("</table>\n");
                sbHtml.append("</td></tr>\n");
                if (this.__Rows.equalsIgnoreCase("multiple")) {
                    sbHtml.append("<tr><td>\n");
                    sbHtml.append("\t<div id=msgdiv class=\"tempateSelectorMsgText red\">").append(this.__Tp.translate("Hit the Add Template button to add templates.")).append("</div>");
                    if (!__InitialRows.equalsIgnoreCase("")) {
                        sbHtml.append("<script>\n");
                        for (int k = 0; k < Integer.parseInt(__InitialRows); ++k) {
                            sbHtml.append("addTemplate(true);\n");
                        }
                        sbHtml.append("</script>\n");
                    }
                    sbHtml.append("</td></tr>\n");
                    if (this.__ShowButtonToFireCallback) {
                        sbHtml.append("<tr><td>\n");
                        sbHtml.append("\t").append(this.getCallbackButton()).append(" \n");
                        sbHtml.append("</td></tr>\n");
                    }
                }
                sbHtml.append("</table>\n");
                if (this.__ShowCopies) {
                    sbHtml.append("<script>");
                    sbHtml.append("function validatedetails() {");
                    sbHtml.append("    var returnvalue = '';");
                    sbHtml.append("    var valifield = '';");
                    sbHtml.append("    var invalidfields = '';");
                    sbHtml.append("        var afieldid = '").append(this.__CopiesInputBoxName).append("';");
                    sbHtml.append("        var afieldel = document.getElementById( afieldid );");
                    sbHtml.append("        valifield = validateField( afieldid, 'Number(0 to )', afieldel );");
                    sbHtml.append("        if ( valifield != '' ) invalidfields += ';' + valifield;");
                    sbHtml.append("    if ( invalidfields.length > 0 ) {");
                    sbHtml.append("        handleValidationResult( invalidfields.substring( 1 ) );");
                    sbHtml.append("        returnvalue = invalidfields.substring( 1 );");
                    sbHtml.append("    }");
                    sbHtml.append("    return returnvalue;");
                    sbHtml.append("}");
                    sbHtml.append("</script>");
                }
            }
        } else {
            Trace.logError("OPAL_ERR: TemplateSelector.getHtml -> Did not get access to the TemplateSelector element.");
        }
        sbHtml.append("<!-- END TEMPLATESELECTOR ELEMENT -->\n\n");
        return sbHtml.toString();
    }

    private String getSingleTemplateSelectorRow() {
        StringBuffer sbHtml = new StringBuffer();
        sbHtml.append(this.getHeader());
        sbHtml.append("<tr class=").append("trTemplateSelectorRow").append(" id=template_1>");
        if (this.__ShowSelector) {
            sbHtml.append("<td class=").append("tdTemplateSelectorCell").append(" nowrap  valign=\"top\">\n");
        } else {
            sbHtml.append("<td class=").append("tdTemplateSelectorCell").append(" nowrap valign=\"top\" style=\"display:none\" >\n");
        }
        sbHtml.append(this.getSingleTemplate(this.__TemplateInputBoxName)).append("\n");
        sbHtml.append("</td>\n");
        if (this.__ShowCopies) {
            sbHtml.append("<td class=").append("tdTemplateSelectorCell").append(" nowrap valign=\"top\">\n");
        } else {
            sbHtml.append("<td class=").append("tdTemplateSelectorCell").append(" nowrap valign=\"top\" style=\"display:none\" >\n");
        }
        sbHtml.append(this.getCopies(this.__CopiesInputBoxName)).append("\n");
        sbHtml.append("</td>\n");
        if (this.__ShowTemplateInfoLink) {
            sbHtml.append("\t<td class=tdTemplateSelectorCell align=middle >\n");
            sbHtml.append(this.getInfo()).append("\n");
            sbHtml.append("\t</td>\n");
        }
        if (this.__ShowButtonToFireCallback) {
            sbHtml.append("\t<td class=tdTemplateSelectorCell >\n");
            sbHtml.append(this.getCallbackButton()).append(" \n");
            sbHtml.append("\t</td>\n");
        }
        sbHtml.append("</tr>\n");
        return sbHtml.toString();
    }

    private String getMultipleTemplateSelectorRow() {
        return this.getHeader();
    }

    private String getSingleTemplate(String templateDDName) {
        StringBuffer sbHtml = new StringBuffer();
        String eventString = "";
        if (this.__FireOnchangeForTemplateSelector && !this.__OnChangeCallback.equalsIgnoreCase("")) {
            eventString = "onchange=\"" + this.__OnChangeCallback + "\" ";
        }
        if (!this.__AllowMods) {
            this.__Style = "lookup";
        }
        if (this.__Style.equalsIgnoreCase("dropdown")) {
            String templateString = this.getTemplateString();
            this.__DefaultTemplate = this.__DefaultTemplate.length() == 0 && templateString.length() == 0 ? " " : this.__DefaultTemplate;
            sbHtml.append("\t").append(this.renderDropDown(templateString, templateDDName, "ddTemplateSelector", eventString, !this.__TemplateMandatory)).append("\n");
        } else {
            this.__DefaultTemplate = this.__DefaultTemplate.length() > 0 ? this.__DefaultTemplate : " ";
            String templateDDNameProxy = templateDDName + "_proxy";
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            boolean isVersioned = sdcProcessor.getProperty(this.__SdcId, "versionedflag").equalsIgnoreCase("Y");
            if (this.__FireOnchangeForTemplateSelector && !this.__OnChangeCallback.equalsIgnoreCase("") && isVersioned) {
                eventString = "onchange=\"" + this.__OnChangeCallback + "\" ";
            } else if (this.__FireOnchangeForTemplateSelector && !this.__OnChangeCallback.equalsIgnoreCase("") && !isVersioned) {
                eventString = "onchange=\"$('#" + templateDDName + "').val($('#" + templateDDNameProxy + "').val());" + this.__OnChangeCallback + "\" ";
            }
            sbHtml.append("<table><tr><td>");
            sbHtml.append("<input type=hidden readonly name=").append(templateDDName).append(" id=").append(templateDDName).append(" class=inpTemplateSelector ").append(eventString).append(" >\n");
            sbHtml.append("<input type=text readonly name=").append(templateDDNameProxy).append(" id=").append(templateDDNameProxy).append(" value='").append(TemplateSelector.getTemplateDisplayValue(this.__DefaultTemplate)).append("'").append(" class=\"inpTemplateSelector input_field\"").append(" style=\"border:1px solid green;width:250px;\" ").append(" edit=\"lookup\" ").append(" onkeyup=\"showSuggestion()\" ").append(eventString).append(" >\n");
            String columnsJson = "";
            if (isVersioned) {
                String keyid1 = sdcProcessor.getProperty(this.__SdcId, "keycolid1");
                String verCol = sdcProcessor.getProperty(this.__SdcId, "keycolid2");
                sbHtml.append("<input type='hidden' name='" + keyid1 + "_keyid1' id='" + keyid1 + "_keyid1' >");
                sbHtml.append("<input type='hidden' name='" + verCol + "_ver' id='" + verCol + "_ver' onchange='setTemplateOnChangeForDynamicDropDown(\"" + keyid1 + "\",\"" + verCol + "\",\"" + templateDDName + "\",\"" + templateDDNameProxy + "\")'>\n");
                columnsJson = "columns: [{id: \"" + keyid1 + "\",mapfieldid: \"" + keyid1 + "_keyid1\",lumode: \"Display and Return\",columnid: \"" + keyid1 + "\",mode: \"Display Text\"},                {id: \"" + verCol + "\",mapfieldid: \"" + verCol + "_ver\",lumode: \"Display and Return\",columnid: \"" + verCol + "\",mode: \"Display Text\"}], ";
            }
            String restrictiveWhere = "templateflag='Y'";
            restrictiveWhere = restrictiveWhere + (this.__QueryWhere != null && !this.__QueryWhere.trim().isEmpty() ? " AND " + this.__QueryWhere.replaceAll("'", "'").replaceAll("\"", "\\") : "");
            sbHtml.append("<script type=\"text/javascript\">var oLUPD_" + templateDDNameProxy + "= {\"selectortype\": \"none\",\"sdcid\": \"" + this.__SdcId + "\"," + columnsJson + " \"restrictivewhere\":\"" + restrictiveWhere + "\"}</script>");
            sbHtml.append("</td>");
            if (this.__AllowMods) {
                PropertyList pageDirectives = null;
                PropertyListCollection sdccols = sdcProcessor.getColumns(this.__SdcId);
                PropertyListCollection columns = new PropertyListCollection();
                String colid = sdcProcessor.getProperty(this.__SdcId, "desccol", "");
                PropertyList sdccol = sdccols.getPropertyList(colid);
                sdccol.setProperty("returnvalue", "N");
                columns.add(0, sdccol);
                pageDirectives = SDITagUtil.getLookupPageDirectives(this.__SdcId, sdcProcessor.getProperty(this.__SdcId, "keycolid1"), sdcProcessor.getProperty(this.__SdcId, "keycolid2"), sdcProcessor.getProperty(this.__SdcId, "versionedflag").equalsIgnoreCase("Y"), "radiobutton", true, restrictiveWhere, "", "", "", "", columns, true, new StringBuffer(""), new StringBuffer(""), true, this.getTranslationProcessor(), sdcProcessor);
                this.__LookupPage = this.pageContext != null && (this.__LookupPage == null || this.__LookupPage.length() == 0) ? "rc?command=file&file=WEB-OPAL/pagetypes/list/maintenance_list.jsp&sdcid=" + this.__SdcId : this.__LookupPage + "&restrictivewhere=" + restrictiveWhere;
                sbHtml.append("<td><a href=\"/").append(this.__Tp.translate("Look up a Template")).append("\" onClick=\"openTemplateLookup( ").append("'").append(this.__SdcId).append("'").append(",'").append(this.getKeyColumnsString()).append("'").append(",'").append(templateDDName).append("'").append(",'").append(templateDDNameProxy).append("'").append(",'").append(SafeHTML.encodeForJavaScript(this.__LookupPage)).append("'").append(",'").append(SafeHTML.encodeForJavaScript(pageDirectives.toJSONString())).append("'").append(");return false\" tabindex=\"0\">").append("<img title=\"").append(this.__Tp.translate("Look up a Template")).append("\" border=\"0\" class=\"template_lookup_img\" src=\"").append(this.lookupimg).append("\"></a></td>\n");
            }
            sbHtml.append("</tr></table>");
        }
        if (!this.__DefaultTemplate.equalsIgnoreCase("")) {
            sbHtml.append("<script>\n");
            sbHtml.append("\tdocument.getElementById('").append(templateDDName).append("').value = '").append(this.__DefaultTemplate).append("';\n");
            sbHtml.append("</script>\n");
        } else if (this.__TemplateMandatory) {
            sbHtml.append("<script>\n");
            sbHtml.append("\tsapphire.events.attachEvent(window,\"onload\"," + this.__OnChangeCallback.substring(0, this.__OnChangeCallback.indexOf("(")) + ");");
            sbHtml.append("</script>\n");
        }
        return sbHtml.toString();
    }

    private String getMultipleTemplate(String templateDDName) {
        StringBuffer sbHtml = new StringBuffer("");
        String eventString = "";
        if (this.__FireOnchangeForTemplateSelector && !this.__OnChangeCallback.equalsIgnoreCase("")) {
            eventString = "onchange=\"" + this.__OnChangeCallback + "\" ";
        }
        if (this.__Style.equalsIgnoreCase("dropdown")) {
            String templateString = this.getTemplateString();
            sbHtml.append("<select name=\"").append(templateDDName).append("\" id=\"").append(templateDDName).append("\" ");
            if (!eventString.equalsIgnoreCase("")) {
                sbHtml.append(eventString);
            }
            if (!"ddTemplateSelector".equalsIgnoreCase("")) {
                sbHtml.append(" class=\"ddTemplateSelector\" ");
            }
            sbHtml.append(" >");
            if (!this.__TemplateMandatory) {
                sbHtml.append("<option value=\"\">-- ").append(this.__Tp.translate("None")).append(" --</option>");
            }
            if (!templateString.equalsIgnoreCase("")) {
                String[] arrTemplates = StringUtil.split(templateString, ";");
                for (int i = 0; i < arrTemplates.length; ++i) {
                    sbHtml.append("<option value=\"").append(arrTemplates[i]).append("\">").append(this.__Tp.translate(TemplateSelector.getTemplateDisplayValue(arrTemplates[i]))).append("</option>");
                }
            }
            sbHtml.append("</select>");
        } else {
            sbHtml.append("<input type=text readonly name=").append(templateDDName).append(" id=").append(templateDDName).append(" class=inpTemplateSelector ").append(eventString).append(" >");
            sbHtml.append("<a href=\"/").append(this.__Tp.translate("Look up a Template")).append("\" onClick=\"lookupfield( '").append(templateDDName).append("', '").append(this.__SdcId).append("','','','','','','', '', '', '', '").append(this.__LookupPage).append("');return false\" tabindex=\"0\"><img title=\"").append(this.__Tp.translate("Look up a Template")).append("\" border=\"0\" src=\"WEB-CORE/elements/images/lookup.gif\"></a>");
        }
        sbHtml = new StringBuffer(StringUtil.replaceAll(sbHtml.toString(), "'", "&#039;"));
        return sbHtml.toString();
    }

    private String getCopies(String copiesName) {
        StringBuffer sbHtml = new StringBuffer("");
        String eventString = "";
        sbHtml.append("\t<input type=hidden  id='copyinputboxname' name='copyinputboxname' value=\"").append(copiesName).append("\"/>");
        if (this.__FireOnchangeForCopies) {
            eventString = "onchange=\"" + this.__OnChangeCallback + "\" ";
        }
        sbHtml.append("\t<input type=text onfocus=\"this.oldvalue=this.value!=''?this.value:this.oldvalue;\" name=\"").append(copiesName).append("\" id=\"").append(copiesName).append("\" class=\"inpTemplateCopies\" ");
        if (!this.__DefaultCopies.equalsIgnoreCase("")) {
            sbHtml.append(" value=").append(this.__DefaultCopies).append(" ");
        }
        if (!this.__OnChangeCallback.equalsIgnoreCase("")) {
            sbHtml.append(eventString);
        }
        sbHtml.append(" >\n");
        return sbHtml.toString();
    }

    private String getInfo() {
        StringBuffer sbHtml = new StringBuffer("");
        String templateInfoLinkJs = "showTemplateDetails";
        if (this.__Rows.equalsIgnoreCase("multiple")) {
            sbHtml.append("\t<a href=\"/").append(this.__Tp.translate("Show details for template")).append("\" onclick=\"").append(templateInfoLinkJs).append("('").append(this.__TemplateInputBoxName).append("_indx','").append(this.__Rows).append("','").append(this.__TemplateInfoLinkPage).append("'); return false;\"><img src=\"WEB-CORE/images/gif/Details.gif\" border=0></a>\n");
        } else {
            sbHtml.append("\t<a href=\"/").append(this.__Tp.translate("Show details for template")).append("\" onclick=\"").append(templateInfoLinkJs).append("('").append(this.__TemplateInputBoxName).append("','").append(this.__Rows).append("','").append(this.__TemplateInfoLinkPage).append("'); return false;\"><img src=\"WEB-CORE/images/gif/Details.gif\" border=0></a>\n");
        }
        return sbHtml.toString();
    }

    private String getCallbackButton() {
        StringBuffer sbHtml = new StringBuffer("");
        PropertyList plCallbackButton = this.element.getPropertyList("callbackbutton");
        sbHtml.append("\t").append(this.getButtonHtml(plCallbackButton, this.__Label_CallbackButton)).append(" \n");
        return sbHtml.toString();
    }

    private String getAddRemoveTemplateButtons() {
        StringBuffer sbHtml = new StringBuffer("");
        PropertyList plAddRemoveTemplateButtons = this.element.getPropertyList("addremovetemplatebuttons");
        PropertyList plAddButton = plAddRemoveTemplateButtons.getPropertyList("add");
        PropertyList plRemoveButton = plAddRemoveTemplateButtons.getPropertyList("remove");
        boolean __ShowAddRowButton = !plAddButton.getProperty("show").equalsIgnoreCase("N");
        boolean __ShowRemoveRowButton = !plRemoveButton.getProperty("show").equalsIgnoreCase("N");
        sbHtml.append("\t<table cellpadding=0 cellspacing=0 border=0 class=\"tblAddRemoveButtons\"><tr>\n");
        if (__ShowAddRowButton) {
            sbHtml.append("\t<td>\n");
            sbHtml.append("\t").append(this.getButtonHtml(plAddButton, "Add")).append("\n");
            sbHtml.append("\t</td><td>&nbsp;</td>\n");
        }
        if (__ShowRemoveRowButton) {
            sbHtml.append("\t<td>\n");
            sbHtml.append("\t").append(this.getButtonHtml(plRemoveButton, "Remove")).append("\n");
            sbHtml.append("\t</td>\n");
        }
        sbHtml.append("\t</tr></table>\n");
        return sbHtml.toString();
    }

    private String getButtonHtml(PropertyList plButton, String defaultText) {
        String buttonText = plButton.getProperty("text");
        if (buttonText.equalsIgnoreCase("")) {
            buttonText = this.__Tp.translate(defaultText);
        }
        String buttonImage = plButton.getProperty("image");
        String buttonWidth = plButton.getProperty("width");
        String buttonId = plButton.getProperty("id");
        if (buttonId.equalsIgnoreCase("")) {
            buttonId = defaultText;
        }
        String buttonAction = plButton.getProperty("action");
        Button button = new Button(this.pageContext);
        button.setText(buttonText);
        button.setImg(buttonImage);
        button.setWidth(buttonWidth);
        button.setTip(buttonText);
        button.setId(buttonId);
        button.setMargin("thin");
        button.setStyle("border: 1px solid rgba(26,26,26,0.4);   border-radius: 3px;   padding: 2px 2px 2px 2px;");
        if (defaultText.equalsIgnoreCase(this.__Label_CallbackButton)) {
            button.setAction(buttonAction);
        } else if (defaultText.equalsIgnoreCase("Add")) {
            button.setAction("addTemplate(true)");
            button.setId("addbutton");
        } else if (defaultText.equalsIgnoreCase("Remove")) {
            button.setAction("removeTemplate()");
            button.setId("removebutton");
        }
        return button.getHtml();
    }

    private String getHeader() {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("<tr class=trTemplateSelectorHeader>\n");
        if (this.__Rows.equalsIgnoreCase("multiple")) {
            sbHtml.append("\t<td>&nbsp;</td>\n");
        }
        if (this.__ShowSelector) {
            sbHtml.append("\t<td nowrap class=\"").append("tempateSelectorText").append("\">").append(this.__Tp.translate(this.__Label_SelectTemplate)).append("</td>\n");
        }
        if (this.__ShowCopies) {
            sbHtml.append("\t<td nowrap class=\"").append("tempateSelectorText").append("\">").append(this.__Tp.translate(this.__Label_Copies)).append("</td>\n");
        }
        if (this.__ShowTemplateInfoLink) {
            sbHtml.append("\t<td nowrap class=\"").append("tempateSelectorText").append("\">").append(this.__Tp.translate(this.__Label_TemplateDetails)).append("</td>\n");
        }
        if (this.__Rows.equalsIgnoreCase("single") && this.__ShowButtonToFireCallback) {
            sbHtml.append("\t<td>&nbsp;</td>\n");
        }
        sbHtml.append("</tr>\n");
        return sbHtml.toString();
    }

    private String getMultiTemplateSelectorHeader() {
        StringBuffer sbHtml = new StringBuffer("");
        sbHtml.append("<table cellspacing=0 cellpadding=0 border=0><tr>\n");
        sbHtml.append("<td>\n");
        if (!this.__MaxRows.equalsIgnoreCase("")) {
            sbHtml.append("<input type=hidden name=\"maxtemplates\" id=\"maxtemplates\" value=").append(this.__MaxRows).append("  readonly>\n");
        } else {
            sbHtml.append("<input type=hidden name=\"maxtemplates\" id=\"maxtemplates\" readonly>\n");
        }
        sbHtml.append("<input type=hidden name=\"templateinputboxname\" id=\"templateinputboxname\" value=\"").append(this.__TemplateInputBoxName).append("\" readonly >\n");
        sbHtml.append("<script>\n");
        sbHtml.append("function printTemplateDropdown(){\n");
        sbHtml.append("    var templates = \"\";\n");
        sbHtml.append("    templates = '").append(this.getMultipleTemplate(this.__TemplateInputBoxName + "_indx")).append("'\n");
        sbHtml.append("    document.writeln(templates);\n");
        sbHtml.append("}\n");
        sbHtml.append(" \n");
        sbHtml.append("function printTemplateText(){\n");
        sbHtml.append("    var templates = \"\";\n");
        sbHtml.append("    templates = '<input type=text name=\"").append(this.__TemplateInputBoxName).append("_indx\" value=\"\" style=\"border:0px solid\">';\n");
        sbHtml.append("    document.writeln(templates);\n");
        sbHtml.append("}\n");
        sbHtml.append("</script>\n");
        sbHtml.append("<table style=\"display:none\" name=\"dummytable1\" id=\"dummytable1\" border=\"1\" bordercolor=\"#B0C4DE\" cellpadding=\"3\" cellspacing=\"0\" width=60%>\n");
        sbHtml.append("\t<tr id=templaterow_indx>\n");
        sbHtml.append("\t\t<td align=\"middle\"><input name=\"useselector\" id=\"use_indx\" type=\"checkbox\"></td>\n");
        sbHtml.append("\t\t<td style=\"white-space: nowrap;\">\n");
        sbHtml.append("            <script>printTemplateDropdown()</script>\n");
        sbHtml.append("\t\t</td>\n");
        sbHtml.append("\t\t<td align=\"middle\">").append(this.getCopies(this.__CopiesInputBoxName + "_indx")).append("</td>\n");
        sbHtml.append("     <td align=\"middle\">").append(this.getInfo()).append("</td>\n");
        sbHtml.append("\t</tr>\n");
        sbHtml.append("</table>\n");
        sbHtml.append("<table style=\"display:none\" name=\"dummytable2\" id=\"dummytable2\" border=\"1\" bordercolor=\"#B0C4DE\" cellpadding=\"3\" cellspacing=\"0\" width=60%>\n");
        sbHtml.append("\t<tr id=templaterow_indx>\n");
        sbHtml.append("\t\t<td align=\"middle\"><input name=\"useselector\" id=\"use_indx\" type=\"checkbox\"></td>\n");
        sbHtml.append("\t\t<td style=\"white-space: nowrap;\">\n");
        sbHtml.append("            <script>printTemplateText()</script>\n");
        sbHtml.append("\t\t</td>\n");
        sbHtml.append("\t\t<td align=\"middle\">").append(this.getCopies(this.__CopiesInputBoxName + "_indx")).append("</td>\n");
        sbHtml.append("     <td align=\"middle\">").append(this.getInfo()).append("</td>\n");
        sbHtml.append("\t</tr>\n");
        sbHtml.append("</table>\n");
        sbHtml.append("</td>\n");
        sbHtml.append("<td>\n");
        sbHtml.append(this.getAddRemoveTemplateButtons()).append("\n");
        sbHtml.append("</td>");
        sbHtml.append("<td class=\"tempateSelectorMsgText\">&nbsp;").append(this.__Tp.translate("No of Templates")).append(": &nbsp;<input type=text name=\"numtemplates\" id=\"numtemplates\" value=0 size=2 readonly style=\"border:0\"></td>\n");
        sbHtml.append("</tr></table>\n");
        return sbHtml.toString();
    }

    private String getTemplateString() {
        String templateString = "";
        templateString = !this.__QueryId.equalsIgnoreCase("") ? SdcInfo.getTemplateStringForSdc(this.__SdcId, this.__QueryId, this.pageContext) : (!this.__QueryFrom.equalsIgnoreCase("") || !this.__QueryWhere.equalsIgnoreCase("") ? SdcInfo.getTemplateStringForSdc(this.__SdcId, this.__Keyid1, this.__Keyid2, this.__Keyid3, this.__QueryFrom, this.__QueryWhere, "", this.pageContext) : SdcInfo.getTemplateStringForSdc(this.__SdcId, this.pageContext));
        return templateString;
    }

    private String renderDropDown(String valueString, String dropDownName, String dropDownStyle, String eventString, boolean enableNone) {
        StringBuilder sbHtml = new StringBuilder();
        sbHtml.append("<select name=\"").append(dropDownName).append("\" id=\"").append(dropDownName).append("\" ");
        if (!eventString.equalsIgnoreCase("")) {
            sbHtml.append(eventString);
        }
        if (!dropDownStyle.equalsIgnoreCase("")) {
            sbHtml.append(" class=\"").append(dropDownStyle).append("\" ");
        }
        sbHtml.append(" >\n");
        if (enableNone) {
            sbHtml.append("\t<option value=\"None\">-- ").append(this.__Tp.translate("None")).append(" --</option>\n");
        }
        if (!valueString.equalsIgnoreCase("")) {
            String[] arrValues;
            for (String arrValue : arrValues = StringUtil.split(valueString, ";")) {
                sbHtml.append("\t<option value=\"").append(arrValue).append("\">").append(this.__Tp.translate(TemplateSelector.getTemplateDisplayValue(arrValue))).append("</option>\n");
            }
        }
        sbHtml.append("</select>\n");
        return sbHtml.toString();
    }

    public static String getTemplateDisplayValue(String templateValue) {
        StringBuilder dispVal = new StringBuilder();
        String[] arrTmpVal = StringUtil.split(templateValue, "|");
        if (arrTmpVal.length != 0) {
            dispVal.append(arrTmpVal[0]);
            if (!(arrTmpVal.length <= 1 || arrTmpVal[1] == null && "".equals(arrTmpVal[1]))) {
                dispVal.append(" (");
                dispVal.append(arrTmpVal[1]);
                if (!(arrTmpVal.length <= 2 || arrTmpVal[2] == null && "".equals(arrTmpVal[2]))) {
                    dispVal.append(",").append(arrTmpVal[2]);
                }
                dispVal.append(")");
            }
        }
        return dispVal.toString();
    }

    public String getKeyColumnsString() {
        String keycolid3;
        StringBuilder keyColumns = new StringBuilder();
        SDCProcessor sdcProcessor = new SDCProcessor(this.pageContext);
        keyColumns.append(sdcProcessor.getProperty(this.__SdcId, "keycolid1"));
        String keycolid2 = sdcProcessor.getProperty(this.__SdcId, "keycolid2", "");
        if (!"".equals(keycolid2)) {
            keyColumns.append(";").append(keycolid2);
        }
        if (!"".equals(keycolid3 = sdcProcessor.getProperty(this.__SdcId, "keycolid3", ""))) {
            keyColumns.append(";").append(keycolid3);
        }
        return keyColumns.toString();
    }
}

