/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.lucene.index.DirectoryReader
 *  org.apache.lucene.store.Directory
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.search.Indexer;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;

public class GetIndexingData
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            int i;
            ConnectionProcessor connectionProcessor = this.getConnectionProcessor();
            ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
            TranslationProcessor tp = this.getTranslationProcessor();
            Indexer indexer = Indexer.getInstance(connectionInfo.getDatabaseId());
            String indexsummary = "";
            String indexStatus = tp.translate("OK") + " (" + indexer.getIndexDir().getAbsolutePath() + ")";
            String indexdate = "";
            DataSet lastindexdata = this.getQueryProcessor().getSqlDataSet("SELECT propertyvalue FROM sysconfig WHERE propertyid = 'lastindexdt'");
            DateTimeUtil dtu = new DateTimeUtil();
            if (lastindexdata.size() == 1) {
                M18NUtil m18NUtil = new M18NUtil(connectionInfo);
                indexdate = m18NUtil.format(dtu.getCalendar(lastindexdata.getValue(0, "propertyvalue"))) + " (" + lastindexdata.getValue(0, "propertyvalue") + ")&nbsp;<a href=\"#\" onclick=\"resetLastIndexDt()\" title=\"Reset the last index date to now\">reset</a>";
            } else {
                indexdate = "Undefined";
            }
            try (DirectoryReader indexReader = null;){
                indexReader = DirectoryReader.open((Directory)indexer.getIndexDirectory());
                FormatUtil formatUtil = FormatUtil.getInstance();
                indexsummary = tp.translate("Items") + ": " + indexReader.numDocs() + " / " + tp.translate("Size") + ": " + formatUtil.format(new BigDecimal(FileUtil.fileSize(indexer.getIndexDir())), true) + " bytes";
            }
            DataSet indexData = this.getQueryProcessor().getSqlDataSet("SELECT indexflag, count(*) count FROM indexmap GROUP BY indexflag");
            int indexedItems = 0;
            int pendingIndexingCount = 0;
            int pendingDeleteCount = 0;
            int pendingBacklogCount = 0;
            int errorCount = 0;
            for (int i2 = 0; i2 < indexData.size(); ++i2) {
                if (indexData.getValue(i2, "indexflag").length() == 0) {
                    indexedItems = indexData.getInt(i2, "count");
                    continue;
                }
                if (indexData.getValue(i2, "indexflag").equals("U")) {
                    pendingIndexingCount = indexData.getInt(i2, "count");
                    continue;
                }
                if (indexData.getValue(i2, "indexflag").equals("D")) {
                    pendingDeleteCount = indexData.getInt(i2, "count");
                    continue;
                }
                if (indexData.getValue(i2, "indexflag").equals("B")) {
                    pendingBacklogCount = indexData.getInt(i2, "count");
                    continue;
                }
                if (!indexData.getValue(i2, "indexflag").equals("E")) continue;
                errorCount = indexData.getInt(i2, "count");
            }
            DataSet latestIndexing = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT * FROM indexmap ORDER BY indexdt DESC ) WHERE indexflag IS NULL AND rownum < 6" : "SELECT TOP 5 * FROM indexmap WHERE indexflag IS NULL ORDER BY indexdt DESC");
            DataSet pendingIndexing = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT * FROM indexmap ORDER BY indexdt DESC ) WHERE indexflag = 'U' AND rownum < 6" : "SELECT TOP 5 * FROM indexmap WHERE indexflag = 'U' ORDER BY indexdt DESC");
            DataSet pendingDelete = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT * FROM indexmap ORDER BY indexdt DESC ) WHERE indexflag = 'D' AND rownum < 6" : "SELECT TOP 5 * FROM indexmap WHERE indexflag = 'D' ORDER BY indexdt DESC");
            DataSet pendingBacklog = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT * FROM indexmap ORDER BY indexdt DESC ) WHERE indexflag = 'B' AND rownum < 6" : "SELECT TOP 5 * FROM indexmap WHERE indexflag = 'B' ORDER BY indexdt DESC");
            DataSet errors = this.getQueryProcessor().getSqlDataSet(connectionInfo.isOracle() ? "SELECT * FROM (SELECT * FROM indexmap ORDER BY indexdt DESC ) WHERE indexflag = 'E' AND rownum < 6" : "SELECT TOP 5 * FROM indexmap WHERE indexflag = 'E' ORDER BY indexdt DESC");
            DataSet statsByType = this.getQueryProcessor().getSqlDataSet("SELECT indextype, COUNT(*) count, MIN(indextime) min, MAX( indextime) max, AVG(indextime) avg FROM indexmap WHERE indexflag IS NULL GROUP BY indextype ORDER BY indextype");
            ajaxResponse.addCallbackArgument("indexstatus", indexStatus);
            ajaxResponse.addCallbackArgument("indexsummary", indexsummary);
            ajaxResponse.addCallbackArgument("indexdate", indexdate);
            ajaxResponse.addCallbackArgument("indexeditems", indexedItems);
            ajaxResponse.addCallbackArgument("pendingindexed", pendingIndexingCount);
            ajaxResponse.addCallbackArgument("pendingdelete", pendingDeleteCount);
            ajaxResponse.addCallbackArgument("pendingbacklog", pendingBacklogCount);
            StringBuffer indexmaptop5 = new StringBuffer();
            indexmaptop5.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\">").append("  <tr align=\"center\" style=\"font-weight:bold\"><td>" + tp.translate("Latest Indexing") + " (" + indexedItems + ")</td><td>" + tp.translate("Pending Indexing") + " (" + pendingIndexingCount + ")</td><td>" + tp.translate("Pending Deletes") + " (" + pendingDeleteCount + ")</td><td>" + tp.translate("Pending Backlog") + " (" + pendingBacklogCount + ")</td><td>" + tp.translate("Indexing Errors") + " (" + errorCount + ")</td></tr>\n").append("  <tr valign=\"top\">\n").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\n").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Index Item") + "</td><td>" + tp.translate("Date") + "</td><td>" + tp.translate("Time") + " (ms)</td></tr>\n");
            for (i = 0; i < latestIndexing.size(); ++i) {
                indexmaptop5.append("<tr><td>").append(latestIndexing.getValue(i, "indexitem")).append("</td><td>").append(latestIndexing.getValue(i, "indexdt")).append("</td><td align=\"right\">").append(latestIndexing.getValue(i, "indextime")).append("</td></tr>");
            }
            indexmaptop5.append("          </table>\n").append("      </td>\n").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\n").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Index Item") + "</td><td>" + tp.translate("Date") + "</td></tr>\n");
            for (i = 0; i < pendingIndexing.size(); ++i) {
                indexmaptop5.append("<tr><td>").append(pendingIndexing.getValue(i, "indexitem")).append("</td><td>").append(pendingIndexing.getValue(i, "indexdt")).append("</td></tr>");
            }
            indexmaptop5.append("          </table>\n").append("      </td>\n").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\n").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Index Item") + "</td><td>" + tp.translate("Date") + "</td></tr>\n");
            for (i = 0; i < pendingDelete.size(); ++i) {
                indexmaptop5.append("<tr><td>").append(pendingDelete.getValue(i, "indexitem")).append("</td><td>").append(pendingDelete.getValue(i, "indexdt")).append("</td></tr>");
            }
            indexmaptop5.append("          </table>\n").append("      </td>\n").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\n").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Index Item") + "</td><td>" + tp.translate("Date") + "</td></tr>\n");
            for (i = 0; i < pendingBacklog.size(); ++i) {
                indexmaptop5.append("<tr><td>").append(pendingBacklog.getValue(i, "indexitem")).append("</td><td>").append(pendingBacklog.getValue(i, "indexdt")).append("</td></tr>");
            }
            indexmaptop5.append("          </table>\n").append("      </td>\n").append("      <td>\n").append("          <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\n").append("              <tr class=\"fieldtitle\"><td>" + tp.translate("Index Item") + "</td></tr>\n");
            for (i = 0; i < errors.size(); ++i) {
                indexmaptop5.append("<tr><td title=\"").append(errors.getValue(i, "errortext")).append("\">").append(errors.getValue(i, "indexitem")).append("</td></tr>");
            }
            indexmaptop5.append("          </table>\n").append("      </td>\n").append("  </tr>\n").append("</table>");
            ajaxResponse.addCallbackArgument("indexmaptop5", indexmaptop5.toString());
            int sdiAveTime = 0;
            int noteAveTime = 0;
            int attAveTime = 0;
            StringBuffer statsbytype = new StringBuffer();
            statsbytype.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\n").append("  <tr class=\"fieldtitle\"><td>" + tp.translate("Type") + "</td><td>" + tp.translate("Count") + "</td><td>" + tp.translate("Min") + " (ms)</td><td>" + tp.translate("Max") + " (ms)</td><td>" + tp.translate("Ave") + " (ms)</td></tr>\n");
            for (int i3 = 0; i3 < statsByType.size(); ++i3) {
                statsbytype.append("<tr><td>").append(statsByType.getValue(i3, "indextype")).append("</td><td align=\"right\">").append(statsByType.getValue(i3, "count")).append("</td><td align=\"right\">").append(statsByType.getValue(i3, "min")).append("</td><td align=\"right\">").append(statsByType.getValue(i3, "max")).append("</td><td align=\"right\">").append(statsByType.getInt(i3, "avg")).append("</td></tr>");
                if (statsByType.getValue(i3, "indextype").equals("SDI")) {
                    sdiAveTime = statsByType.getInt(i3, "avg") > 0 ? statsByType.getInt(i3, "avg") : 1;
                    continue;
                }
                if (statsByType.getValue(i3, "indextype").equals("NOTE")) {
                    noteAveTime = statsByType.getInt(i3, "avg") > 0 ? statsByType.getInt(i3, "avg") : 1;
                    continue;
                }
                if (!statsByType.getValue(i3, "indextype").equals("ATTACHMENT")) continue;
                attAveTime = statsByType.getInt(i3, "avg") > 0 ? statsByType.getInt(i3, "avg") : 1;
            }
            statsbytype.append("</table>\n");
            ajaxResponse.addCallbackArgument("statsbytype", statsbytype.toString());
            StringBuffer indexbacklog = new StringBuffer();
            indexbacklog.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">\n").append("  <tr class=\"fieldtitle\"><td></td><td>").append(tp.translate("SDC")).append("</td><td>").append(tp.translate("Percent Indexed")).append("</td><td>").append(tp.translate("Indexed SDIs")).append("</td><td>").append(tp.translate("Indexed Notes")).append("</td><td>").append(tp.translate("Indexed Attachments")).append("</td><td>").append(tp.translate("Estimated Time to Index")).append("</td></tr>\n");
            String[] sdcs = indexer.getIndexedSDCs();
            SDCProcessor sdcProcessor = new SDCProcessor(connectionInfo.getConnectionId());
            int sdcTotal = 0;
            for (int i4 = 0; i4 < sdcs.length; ++i4) {
                float percent;
                String sdcid = sdcs[i4];
                long count = this.getQueryProcessor().getCount("SELECT count(*) FROM " + sdcProcessor.getProperty(sdcid, "tableid"));
                long indexed = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM indexmap WHERE indextype = ? AND indexsdcid = ?", new Object[]{"SDI", sdcid});
                long time = this.getQueryProcessor().getPreparedCount("SELECT avg( indextime ) FROM indexmap WHERE indextype = ? AND indexsdcid = ?", new Object[]{"SDI", sdcid});
                time = time != -999999999L && time > 0L ? time * (count - indexed) : (sdiAveTime > 0 ? (long)sdiAveTime * (count - indexed) : count - indexed);
                boolean indexingreq = count > 0L && count - indexed > 0L;
                long sdcTime = time;
                long sdcIndexed = indexed;
                long sdcCount = count;
                String sdiCol = "<td " + (indexingreq ? "style=\"color:red\"" : "") + ">" + indexed + " of " + count + "</td>";
                count = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM sdinote WHERE sdcid = ?", new Object[]{sdcid});
                indexed = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM indexmap WHERE indextype = ? AND indexsdcid = ?", new Object[]{"NOTE", sdcid});
                time = this.getQueryProcessor().getPreparedCount("SELECT avg( indextime ) FROM indexmap WHERE indextype = ? AND indexsdcid = ?", new Object[]{"NOTE", sdcid});
                time = time != -999999999L && time > 0L ? time * (count - indexed) : (noteAveTime > 0 ? (long)noteAveTime * (count - indexed) : count - indexed);
                sdcTime += time;
                sdcIndexed += indexed;
                sdcCount += count;
                indexingreq = count > 0L && count - indexed > 0L;
                String notesCol = "<td " + (indexingreq ? "style=\"color:red\"" : "") + ">" + indexed + " of " + count + "</td>";
                count = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM sdiattachment WHERE sdcid = ?", new Object[]{sdcid});
                indexed = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM indexmap WHERE indextype = ? AND indexsdcid = ?", new Object[]{"ATTACHMENT", sdcid});
                time = this.getQueryProcessor().getPreparedCount("SELECT avg( indextime ) FROM indexmap WHERE indextype = ? AND indexsdcid = ?", new Object[]{"ATTACHMENT", sdcid});
                time = time != -999999999L && time > 0L ? time * (count - indexed) : (attAveTime > 0 ? (long)attAveTime * (count - indexed) : count - indexed);
                sdcTime += time;
                indexingreq = count > 0L && count - indexed > 0L;
                String attachmentsCol = "<td " + (indexingreq ? "style=\"color:red\"" : "") + ">" + indexed + " of " + count + "</td>";
                float f = percent = sdcCount == 0L ? 100.0f : (float)(sdcIndexed += indexed) / (float)(sdcCount += count) * 100.0f;
                percent = percent > 100.0f ? 100.0f : (percent < 0.0f ? 0.0f : percent);
                String percentCol = "<td><input style=\"background:#C1D1E0;border:solid 1px gray;width: " + Math.round((double)percent * 0.8) + "%; " + (percent == 0.0f ? "display:none" : "") + "\" value=\"\"/>" + (percent > 0.0f && percent < 1.0f ? "<1" : Integer.valueOf(Math.round(percent))) + "%</td>";
                indexbacklog.append("<tr><td><input type=\"radio\" name=\"backlogsdc\" id=\"").append(sdcid).append("\"></td><td>").append(sdcid).append("</td>");
                indexbacklog.append(percentCol);
                indexbacklog.append(sdiCol);
                indexbacklog.append(notesCol);
                indexbacklog.append(attachmentsCol);
                indexbacklog.append("<td>").append(sdcTime == 0L ? "" : (sdcTime < 1000L ? "<1 sec" : String.format("%d hr, %d min, %d sec", TimeUnit.MILLISECONDS.toHours(sdcTime), TimeUnit.MILLISECONDS.toMinutes(sdcTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sdcTime)), TimeUnit.MILLISECONDS.toSeconds(sdcTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sdcTime))))).append("</td>");
                indexbacklog.append("</tr>");
                sdcTotal = (int)((long)sdcTotal + sdcTime);
            }
            indexbacklog.append("  <tr class=\"fieldtitle\"><td></td><td>").append("").append("</td><td></td><td>").append("").append("</td><td>").append("").append("</td><td>").append("").append("</td><td>").append(String.format("%d hr, %d min, %d sec", TimeUnit.MILLISECONDS.toHours(sdcTotal), TimeUnit.MILLISECONDS.toMinutes(sdcTotal) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(sdcTotal)), TimeUnit.MILLISECONDS.toSeconds(sdcTotal) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sdcTotal)))).append("</td></tr>\n");
            indexbacklog.append("</table>\n");
            ajaxResponse.addCallbackArgument("indexbacklog", indexbacklog.toString());
        }
        catch (Exception e) {
            this.logError("Failed to get indexing data. Reason: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

