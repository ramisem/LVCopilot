/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public class GetSearchingData
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            int i;
            List userRoleList = (List)this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getUserAttributeMap().get("rolelist");
            if (!userRoleList.contains("WebPage-Admin") && !userRoleList.contains("Administrator")) {
                throw new Exception("Must have Administrator or WebPage-Admin role to make GetSearchingData Ajax Request");
            }
            ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
            ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
            TranslationProcessor tp = this.getTranslationProcessor();
            DataSet searchCount = this.getQueryProcessor().getSqlDataSet("SELECT COUNT(*) count FROM search WHERE searchtypeflag = 'Q'");
            DataSet quicklinkCount = this.getQueryProcessor().getSqlDataSet("SELECT COUNT(*) count FROM search WHERE searchtypeflag = 'S'");
            DataSet operationsCount = this.getQueryProcessor().getSqlDataSet("SELECT COUNT(*) count FROM search, searchoperation WHERE search.searchid = searchoperation.searchid AND searchtypeflag = 'Q'");
            DataSet topsearches = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT enteredquery, count(enteredquery) count FROM search WHERE searchtypeflag = 'Q' GROUP BY enteredquery ORDER BY 2 DESC) WHERE ROWNUM < 6" : "SELECT TOP 5 enteredquery, count(enteredquery) FROM search WHERE searchtypeflag = 'Q' GROUP BY enteredquery ORDER BY 2 DESC");
            DataSet topterms = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT term, count(term) count FROM search WHERE searchtypeflag = 'S' AND term IS NOT NULL GROUP BY term ORDER BY 2 DESC) WHERE ROWNUM < 6" : "SELECT TOP 5 term, count(term) count FROM search WHERE searchtypeflag = 'S' AND term IS NOT NULL GROUP BY term ORDER BY 2 DESC");
            DataSet operationsfromsearches = this.getQueryProcessor().getSqlDataSet("SELECT searchoperation.operation, COUNT(*) count FROM   search LEFT OUTER JOIN searchoperation ON search.searchid = searchoperation.searchid WHERE  searchtypeflag='Q' GROUP BY operation ORDER BY 2 DESC");
            DataSet latestsearches = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT enteredquery, searchdt, sysuserid FROM search WHERE searchtypeflag='Q' ORDER BY searchdt DESC) WHERE ROWNUM < 6" : "SELECT TOP 5 enteredquery, searchdt, sysuserid FROM search WHERE searchtypeflag='Q' ORDER BY searchdt DESC");
            StringBuffer searches = new StringBuffer();
            searches.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\">").append("  <tr align=\"center\" style=\"font-weight:bold\"><td>" + tp.translate("Top Queries") + " (" + searchCount.getInt(0, "count") + ")</td><td>" + tp.translate("Top Quick Link Terms") + " (" + quicklinkCount.getInt(0, "count") + ")</td><td>" + tp.translate("Search Operations") + " (" + operationsCount.getInt(0, "count") + ")</td><td>" + tp.translate("Latest Searches") + "</td></tr>\n").append("  <tr valign=\"top\">\n").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Entered Query") + "</td><td align=\"right\">" + tp.translate("Count") + "</td></tr>");
            for (i = 0; i < topsearches.size(); ++i) {
                searches.append("       <tr><td>").append(topsearches.getValue(i, "enteredquery")).append("</td><td align=\"right\">").append(topsearches.getValue(i, "count")).append("</td></tr>");
            }
            searches.append("          </table>").append("      </td>").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Entered Query") + "</td><td align=\"right\">" + tp.translate("Count") + "</td></tr>");
            for (i = 0; i < topterms.size(); ++i) {
                searches.append("       <tr><td>").append(topterms.getValue(i, "term")).append("</td><td align=\"right\">").append(topterms.getValue(i, "count")).append("</td></tr>");
            }
            searches.append("          </table>").append("      </td>").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Operation") + "</td><td align=\"right\">" + tp.translate("Count") + "</td></tr>");
            for (i = 0; i < operationsfromsearches.size(); ++i) {
                searches.append("       <tr><td>").append(operationsfromsearches.getValue(i, "operation", "None")).append("</td><td align=\"right\">").append(operationsfromsearches.getValue(i, "count")).append("</td></tr>");
            }
            searches.append("          </table>").append("      </td>").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Date") + "</td><td>" + tp.translate("User") + "</td><td>" + tp.translate("Entered Query") + "</td></tr>");
            for (i = 0; i < latestsearches.size(); ++i) {
                searches.append("       <tr><td>").append(latestsearches.getValue(i, "searchdt")).append("</td><td>").append(latestsearches.getValue(i, "sysuserid")).append("</td><td>").append(latestsearches.getValue(i, "enteredquery")).append("</td></tr>");
            }
            searches.append("          </table>");
            searches.append("      </td>");
            searches.append("  </tr>");
            searches.append("</table>");
            ajaxResponse.addCallbackArgument("searches", searches.toString());
            DataSet searchtimesbyquerytypeData = this.getQueryProcessor().getSqlDataSet("SELECT queryclass, COUNT(*) count,        MIN(searchtime) minsearchtime, AVG(searchtime) avgsearchtime, MAX(searchtime) maxsearchtime,        MIN(processtime) minprocesstime, AVG(processtime) avgprocesstime, MAX(processtime) maxprocesstime,        MIN(totaltime) mintotaltime, AVG(totaltime) avgtotaltime, MAX(totaltime) maxtotaltime,        MIN(hits) minhits, AVG(hits) avghits, MAX(hits) maxhits FROM   search WHERE  searchtypeflag = 'Q' GROUP BY queryclass ORDER BY 2 DESC");
            int count = 0;
            int minsearchtime = Integer.MAX_VALUE;
            int avgsearchtime = 0;
            int maxsearchtime = 0;
            int minprocesstime = Integer.MAX_VALUE;
            int avgprocesstime = 0;
            int maxprocesstime = 0;
            int mintotaltime = Integer.MAX_VALUE;
            int avgtotaltime = 0;
            int maxtotaltime = 0;
            int minhits = Integer.MAX_VALUE;
            int avghits = 0;
            int maxhits = 0;
            StringBuffer searchtimesbyquerytype = new StringBuffer();
            searchtimesbyquerytype.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\">").append("  <tr class=\"fieldtitle\" valign=\"top\"><td rowspan=\"2\">" + tp.translate("Query Type") + "</td><td rowspan=\"2\">" + tp.translate("Count") + "</td><td colspan=\"3\" align=\"center\">" + tp.translate("Search Times") + " (ms)</td><td colspan=\"3\" align=\"center\">" + tp.translate("Processing Times") + " (ms)</td><td colspan=\"3\" align=\"center\">" + tp.translate("Total Times") + " (ms)</td><td colspan=\"3\" align=\"center\">" + tp.translate("Hits") + "</td></tr>").append("  <tr class=\"fieldtitle\" align=\"right\">").append("    <td width=\"50px\">" + tp.translate("Min") + "</td><td width=\"50px\">" + tp.translate("Ave") + "</td><td width=\"50px\">" + tp.translate("Max") + "</td>").append("    <td width=\"50px\">" + tp.translate("Min") + "</td><td width=\"50px\">" + tp.translate("Ave") + "</td><td width=\"50px\">" + tp.translate("Max") + "</td>").append("    <td width=\"50px\">" + tp.translate("Min") + "</td><td width=\"50px\">" + tp.translate("Ave") + "</td><td width=\"50px\">" + tp.translate("Max") + "</td>").append("    <td width=\"50px\">" + tp.translate("Min") + "</td><td width=\"50px\">" + tp.translate("Ave") + "</td><td width=\"50px\">" + tp.translate("Max") + "</td></tr>");
            if (searchtimesbyquerytypeData.size() > 0) {
                for (int i2 = 0; i2 < searchtimesbyquerytypeData.size(); ++i2) {
                    String queryclass = searchtimesbyquerytypeData.getValue(i2, "queryclass");
                    String querytype = queryclass.equalsIgnoreCase("FuzzyQuery") ? tp.translate("Fuzzy Query (e.g. sluge~)") : (queryclass.equalsIgnoreCase("BooleanQuery") ? tp.translate("Boolean Query (e.g. green and sludge)") : (queryclass.equalsIgnoreCase("PrefixQuery") ? tp.translate("Prefix Query (e.g. gre*)") : (queryclass.equalsIgnoreCase("TermQuery") ? tp.translate("Term Query (e.g. green)") : (queryclass.equalsIgnoreCase("TermRangeQuery") ? tp.translate("Term Range Query (e.g. moddt:[n-7d])") : (queryclass.equalsIgnoreCase("PhraseQuery") ? tp.translate("Phrase Query (e.g. \"green sample\")") : queryclass)))));
                    searchtimesbyquerytype.append("<tr align=\"right\">").append("<td align=\"left\">").append(querytype).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "count")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "minsearchtime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "avgsearchtime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "maxsearchtime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "minprocesstime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "avgprocesstime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "maxprocesstime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "mintotaltime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "avgtotaltime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "maxtotaltime")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "minhits")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "avghits")).append("</td>").append("<td>").append(searchtimesbyquerytypeData.getInt(i2, "maxhits")).append("</td>").append("</tr>");
                    count += searchtimesbyquerytypeData.getInt(i2, "count");
                    minsearchtime = Math.min(minsearchtime, searchtimesbyquerytypeData.getInt(i2, "minsearchtime"));
                    avgsearchtime += searchtimesbyquerytypeData.getInt(i2, "avgsearchtime");
                    maxsearchtime = Math.max(maxsearchtime, searchtimesbyquerytypeData.getInt(i2, "maxsearchtime"));
                    minprocesstime = Math.min(minprocesstime, searchtimesbyquerytypeData.getInt(i2, "minprocesstime"));
                    avgprocesstime += searchtimesbyquerytypeData.getInt(i2, "avgprocesstime");
                    maxprocesstime = Math.max(maxprocesstime, searchtimesbyquerytypeData.getInt(i2, "maxprocesstime"));
                    mintotaltime = Math.min(mintotaltime, searchtimesbyquerytypeData.getInt(i2, "mintotaltime"));
                    avgtotaltime += searchtimesbyquerytypeData.getInt(i2, "avgtotaltime");
                    maxtotaltime = Math.max(maxtotaltime, searchtimesbyquerytypeData.getInt(i2, "maxtotaltime"));
                    minhits = Math.min(minhits, searchtimesbyquerytypeData.getInt(i2, "minhits"));
                    avghits += searchtimesbyquerytypeData.getInt(i2, "avghits");
                    maxhits = Math.max(maxhits, searchtimesbyquerytypeData.getInt(i2, "maxhits"));
                }
                searchtimesbyquerytype.append("<tr class=\"fieldtitle\" align=\"right\">").append("<td align=\"left\">" + tp.translate("Totals") + "</td>").append("<td>").append(count).append("</td>").append("<td>").append(minsearchtime).append("</td>").append("<td>").append(avgsearchtime / searchtimesbyquerytypeData.size()).append("</td>").append("<td>").append(maxsearchtime).append("</td>").append("<td>").append(minprocesstime).append("</td>").append("<td>").append(avgprocesstime / searchtimesbyquerytypeData.size()).append("</td>").append("<td>").append(maxprocesstime).append("</td>").append("<td>").append(mintotaltime).append("</td>").append("<td>").append(avgtotaltime / searchtimesbyquerytypeData.size()).append("</td>").append("<td>").append(maxtotaltime).append("</td>").append("<td>").append(minhits).append("</td>").append("<td>").append(avghits / searchtimesbyquerytypeData.size()).append("</td>").append("<td>").append(maxhits).append("</td>").append("</tr>");
            }
            searchtimesbyquerytype.append("</table>");
            ajaxResponse.addCallbackArgument("searchtimesbyquerytype", searchtimesbyquerytype.toString());
        }
        catch (Exception e) {
            this.logError("Failed to get searching data. Reason: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

