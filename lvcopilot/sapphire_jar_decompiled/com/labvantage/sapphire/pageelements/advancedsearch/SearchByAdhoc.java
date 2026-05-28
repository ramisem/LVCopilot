/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import com.labvantage.sapphire.pageelements.advancedsearch.SearchContent;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.scrollpanel.ScrollPanel;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;

public class SearchByAdhoc
extends SearchContent {
    String contentName = "adhoc";

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
        TranslationProcessor tp = this.translator;
        SDIProcessor sdiProcessor = new SDIProcessor(this.pageContext);
        String filterwhere = this.element.getPropertyList("adhocsearch").getProperty("filterwhereclause");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("AdhocQuery");
        sdiRequest.setRequestItem("primary[adhocqueryid, adhocquerydesc, shareableflag]");
        ConnectionInfo connectionInfo = new ConnectionProcessor(this.pageContext).getConnectionInfo(requestContext.getConnectionId());
        sdiRequest.setQueryFrom("adhocquery");
        sdiRequest.setQueryWhere("basedonsdcid='" + SafeSQL.encodeForSQL(this.sdcid, connectionInfo.isOracle()) + "' " + (filterwhere.length() > 0 ? " and (" + filterwhere + ")" : "") + " and ( createby='" + SafeSQL.encodeForSQL(connectionInfo.getSysuserId(), connectionInfo.isOracle()) + "' or shareableflag in ('Y','L') )");
        sdiRequest.setQueryOrderBy("adhocquerydesc");
        DataSet adhocqueryDataSet = sdiProcessor.getSDIData(sdiRequest).getDataset("primary");
        html.append("<div id=\"advancedsearch_adhocquerydiv\">");
        Button adhocQryButton = new Button(this.pageContext);
        html.append(SearchByAdhoc.getQueryidLinksButton(adhocqueryDataSet, tp, adhocQryButton));
        html.append("</div>");
        html.append("<script>");
        html.append("var adhocPopup;\nfunction adhocSearch( adhocqueryid ){\nif( adhocPopup ) adhocPopup.close();\n\nadhocPopup = window.open(\"rc?command=page&page=" + (this.element.getPropertyList("adhocsearch") != null && this.element.getPropertyList("adhocsearch").getProperty("adhocpageid").length() == 0 ? "AdhocQueryPopup" : this.element.getPropertyList("adhocsearch").getProperty("adhocpageid")) + "&sdcid=" + this.sdcid + "&fromsearch=Y&adhocqueryid=\" + adhocqueryid + '&layout=navigator',\"adhocpopup\",\"width=840,height=520,status=yes,scrollbars=yes,resizable=yes\");\n\n}");
        html.append("function defaultAdhocPopupCallback( keyid1list, keyid2list, keyid3list ){\n        document.frmSubmit.keyid1.value = keyid1list;\n        document.frmSubmit.keyid2.value = keyid2list;\n        document.frmSubmit.keyid3.value = keyid3list;\n        document.frmSubmit.queryid.value = '';\n        document.frmSubmit.queryfrom.value = '';\n        document.frmSubmit.querywhere.value = '';\n        document.frmSubmit.action = document.frmSubmit.action + '&fromadhoc=Y';\n        document.frmSubmit.submit();\n        //window.close();\n    }");
        html.append("function refreshAdhocQueryPanel( divhtml ) { \n");
        html.append(" document.getElementById( 'advancedsearch_adhocqueryinnerdiv' ).innerHTML = divhtml;\n");
        if (this.maxHeight > 0) {
            html.append(" sapphire.ui.util.scrollTo(__scrolldiv_").append(this.contentName).append(",null,").append(this.maxHeight).append(");\n");
        }
        html.append(" }\n");
        html.append("</script>");
        if (this.maxHeight > 0) {
            ScrollPanel scrollPanel = new ScrollPanel(this.pageContext);
            scrollPanel.setId(this.contentName);
            scrollPanel.setMaxHeight(this.maxHeight);
            scrollPanel.setModernScroll(true);
            scrollPanel.setContent(html);
            return scrollPanel.getHtml();
        }
        return html.toString();
    }

    public static String getQueryidLinks(DataSet adhocqueryDataSet, TranslationProcessor tp) {
        StringBuffer html = new StringBuffer();
        for (int i = 0; i < adhocqueryDataSet.getRowCount(); ++i) {
            String dvalue = tp.translate(adhocqueryDataSet.getValue(i, "adhocquerydesc"));
            html.append("<a href=\"Javascript:adhocSearch('" + adhocqueryDataSet.getValue(i, "adhocqueryid") + "')\" title=\"" + dvalue + "\">" + (dvalue.length() > 25 ? dvalue.substring(0, 22) + "..." : dvalue) + "(" + ("Y".equals(adhocqueryDataSet.getValue(i, "shareableflag")) ? tp.translate("Shared") : ("L".equals(adhocqueryDataSet.getValue(i, "shareableflag")) ? tp.translate("Locked") : tp.translate("My"))) + ")</a><br/>");
        }
        return html.toString();
    }

    public static String getQueryidLinksButton(DataSet adhocqueryDataSet, TranslationProcessor tp, Button button) {
        StringBuffer html = new StringBuffer();
        button.setText(tp.translate("Create Adhoc Query"));
        button.setAction("adhocSearch()");
        button.setAppearance("ribbonsmall");
        button.setStyle("width:70px; height:22px; opacity:1; border: 1px solid rgba(26,26,26,0.4); border-radius:3px; padding:2px 2px 2px 2px");
        html.append("<table width=\"100%\" border=0><tr><td valign=\"top\" align=\"left\" style=\"padding-left:3px\">").append(button.getHtml()).append("</td></tr>");
        html.append("<tr><td style=\"padding-left:5px\"><div id=\"advancedsearch_adhocqueryinnerdiv\"  class=\"modern_href\">");
        html.append(SearchByAdhoc.getQueryidLinks(adhocqueryDataSet, tp));
        html.append("</div></td></tr></table>");
        return html.toString();
    }
}

