/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.JstlUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class KeyIdSelector {
    private static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private TranslationProcessor translationProcessor;
    PageContext pageContext;

    public KeyIdSelector(PageContext pageContext) {
        this.pageContext = pageContext;
        this.translationProcessor = new TranslationProcessor(pageContext);
    }

    public String getHTML() {
        StringBuffer html = new StringBuffer();
        String keyIdListVariable = "keyid1";
        String keyIdTargetVariable = "keyid1";
        String formId = "frmSubmit";
        String selectorId = "keyidselector";
        String onChangeFunction = "showId()";
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", this.pageContext);
        if ("Edit".equals(pagedata.getProperty("mode"))) {
            String keyIdList = "";
            if (pagedata != null && (keyIdList = pagedata.getProperty(keyIdListVariable)) != null && keyIdList.trim().length() > 0) {
                String[] keyIdsArr = StringUtil.split(keyIdList, ";");
                html.append(this.getSelectorHtml(selectorId, onChangeFunction, keyIdsArr));
                html.append(this.getJSVariables(selectorId, formId, keyIdTargetVariable));
                pagedata.setProperty(keyIdTargetVariable, keyIdsArr[0]);
            }
        }
        return html.toString();
    }

    private String getSelectorHtml(String selectorId, String onChangeFunction, String[] keyIdsArr) {
        StringBuffer selectorHtml = new StringBuffer();
        selectorHtml.append("<table>");
        selectorHtml.append("<tr>");
        selectorHtml.append(this.getShowFirstCell());
        selectorHtml.append(this.getShowPrevCell());
        selectorHtml.append("<td>");
        selectorHtml.append("<select name='").append(selectorId).append("'");
        selectorHtml.append(" id='").append(selectorId).append("'");
        selectorHtml.append(" onchange = '").append(onChangeFunction).append("'");
        selectorHtml.append(" style='height:20px'");
        selectorHtml.append("> ");
        if (keyIdsArr != null && keyIdsArr.length > 0) {
            for (String aKeyIdsArr : keyIdsArr) {
                selectorHtml.append("<option value='").append(aKeyIdsArr).append("' >");
                selectorHtml.append(aKeyIdsArr);
            }
        }
        selectorHtml.append("</select>");
        selectorHtml.append("</td>");
        selectorHtml.append(this.getShowNextCell());
        selectorHtml.append(this.getShowLastCell());
        selectorHtml.append("</tr>");
        selectorHtml.append("</table>");
        return selectorHtml.toString();
    }

    private String getShowPrevCell() {
        StringBuffer sb = new StringBuffer();
        sb.append("<td>");
        sb.append("<img src='WEB-OPAL/images/1leftarrow.png' height='17' width='17' onclick='showPrev()'");
        sb.append(" style='cursor:pointer'");
        sb.append(" title=\"").append(this.translationProcessor.translate("Show Previous Sample")).append("\"/>");
        sb.append("</td>");
        return sb.toString();
    }

    private String getShowNextCell() {
        StringBuffer sb = new StringBuffer();
        sb.append("<td>");
        sb.append("<img src='WEB-OPAL/images/1rightarrow.png' height='17' width='17' onclick='showNext()'");
        sb.append(" style='cursor:pointer'");
        sb.append(" title=\"").append(this.translationProcessor.translate("Show Next Sample")).append("\"/>");
        sb.append("</td>");
        return sb.toString();
    }

    private String getShowFirstCell() {
        StringBuffer sb = new StringBuffer();
        sb.append("<td>");
        sb.append("<img src='WEB-OPAL/images/2leftarrow.png' height='17' width='17' onclick='showFirst()'");
        sb.append(" style='cursor:pointer'");
        sb.append(" title=\"").append(this.translationProcessor.translate("Show First Sample")).append("\"/>");
        sb.append("</td>");
        return sb.toString();
    }

    private String getShowLastCell() {
        StringBuffer sb = new StringBuffer();
        sb.append("<td>");
        sb.append("<img src='WEB-OPAL/images/2rightarrow.png' height='17' width='17' onclick='showLast()'");
        sb.append(" style='cursor:pointer'");
        sb.append(" title=\"").append(this.translationProcessor.translate("Show Last Sample")).append("\"/>");
        sb.append("</td>");
        return sb.toString();
    }

    private String getJSVariables(String selectorId, String formId, String keyIdTargetVariable) {
        StringBuffer jsVariables = new StringBuffer();
        jsVariables.append("<script language='JavaScript' >");
        jsVariables.append("var selectorId = '").append(selectorId).append("'; ");
        jsVariables.append("var selectorFormId = '").append(formId).append("'; ");
        jsVariables.append("var keyIdTargetVariable = '").append(keyIdTargetVariable).append("'; ");
        jsVariables.append("</script>");
        return jsVariables.toString();
    }
}

