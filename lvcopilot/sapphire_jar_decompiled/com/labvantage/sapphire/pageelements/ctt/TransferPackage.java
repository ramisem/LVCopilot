/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.ctt;

import com.labvantage.sapphire.util.http.HttpUtil;
import sapphire.accessor.QueryProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class TransferPackage
extends BaseElement {
    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        if (this.sdiInfo != null) {
            DataSet primary = this.sdiInfo.getDataSet("primary");
            if (primary != null && primary.size() == 1) {
                QueryProcessor qp = this.getQueryProcessor();
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT transfermodeflag, transferpackage FROM transferpackage WHERE transferpackageid = " + safeSQL.addVar(primary.getString(0, "transferpackageid")) + " AND transferpackageversionid = " + safeSQL.addVar(primary.getString(0, "transferpackageversionid"));
                DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues(), true);
                String transfermode = ds.getString(0, "transfermodeflag");
                String transferpackagescript = ds.getClob(0, "transferpackage");
                html.append("<script type=\"text/javascript\">\n");
                html.append("function closeEditor( dialogNumber ) {\n");
                html.append("  sapphire.ui.dialog.close( dialogNumber );\n");
                html.append("}\n");
                html.append("</script>\n");
                html.append(HttpUtil.getCoreStyleSheets(false, this.pageContext));
                html.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">");
                html.append("<tr><td>");
                RequestContext rc = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
                html.append("<iframe id=\"transferpackage_iframe\" width=\"800\" height=\"600\" src=\"rc?command=file&file=WEB-CORE/modules/transfer/transferpackage.jsp&transfermode=" + transfermode + "&mode=" + rc.getProperty("mode") + "\">Browser does not support frames!</iframe>");
                html.append("</td></tr></table>\n");
                html.append("<input type=\"hidden\" name=\"transferpackagescript\" id=\"transferpackagescript\" value=\"" + (transferpackagescript != null ? StringUtil.escape(transferpackagescript) : "") + "\"/>\n");
                html.append("<input type=\"hidden\" name=\"__propertyhandler_transferpackagehandler\" id=\"__propertyhandler_transferpackagehandler\" value=\"com.labvantage.sapphire.admin.system.TransferPackagePropertyHandler\"/>\n");
            } else {
                return "Primary data incorrectly defined";
            }
        }
        return html.toString();
    }
}

