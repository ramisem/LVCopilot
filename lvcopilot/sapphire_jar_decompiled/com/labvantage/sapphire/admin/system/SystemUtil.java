/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.admin.system.StatusProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SystemUtil {
    public static void loadStats(PageContext pageContext) throws SapphireException {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        String sortby = userConfig.getProperty("statssortby");
        String sortbydirection = userConfig.getProperty("statssortbydirection");
        pageContext.setAttribute("sortby", (Object)(sortby == null || sortby.length() == 0 ? sortby : "identifier"));
        pageContext.setAttribute("sortbydirection", (Object)(sortbydirection == null || sortbydirection.length() == 0 ? sortbydirection : "a"));
        int[] type = new int[]{0, 1, 7, 5, 2, 3, 4, 6, 8};
        String[] tabtitle = new String[]{"Actions", "Business Rules", "Request Commands", "Queries", "SQL", "RSets", "Action Blocks", "Code Blocks", "Auto Cross SDI Calculations"};
        String[] columntitle = new String[]{tp.translate("Action"), tp.translate("Event Type"), tp.translate("Command=Parameter"), tp.translate("Query"), tp.translate("SQL"), tp.translate("RSets"), tp.translate("ActionBlock"), tp.translate("Block Name"), tp.translate("Cross SDI Types")};
        String[] id = new String[]{"action", "businessrule", "requestcommand", "query", "sql", "rsets", "actionblock", "codeblock", "crosssdicalc"};
        StatusProcessor statusProcessor = new StatusProcessor(pageContext);
        ArrayList tabs = new ArrayList();
        for (int i = 0; i < type.length; ++i) {
            sortby = userConfig.getProperty("statssortby");
            HashMap<String, Object> tabdetails = new HashMap<String, Object>();
            tabdetails.put("tabtitle", tabtitle[i]);
            tabdetails.put("columntitle", columntitle[i]);
            tabdetails.put("id", id[i]);
            tabdetails.put("expand", userConfig.getProperty("statsexpand" + id[i]));
            DataSet stats = statusProcessor.getStatistics(type[i]);
            stats.addColumn("identifier", 0);
            if (type[i] == 1) {
                stats.addColumn("identifiersort", 0);
            }
            int rows = stats.getRowCount();
            block12: for (int j = 0; j < rows; ++j) {
                String id1 = stats.getValue(j, "id1");
                String id2 = stats.getValue(j, "id2");
                switch (type[i]) {
                    case 0: {
                        stats.setString(j, "identifier", id1 + (id2.length() > 0 ? " (sdc=" + id2 + ")" : ""));
                        continue block12;
                    }
                    case 1: {
                        stats.setString(j, "identifier", id1 + " " + id2);
                        stats.setString(j, "identifiersort", id1 + " " + (id2.startsWith("Pre") ? id2.substring(3) + "a" : (id2.startsWith("Post") ? id2.substring(4) + "b" : id2)));
                        continue block12;
                    }
                    case 7: {
                        stats.setString(j, "identifier", id1 + (id2.length() > 0 ? "=" + id2 : ""));
                        continue block12;
                    }
                    case 5: {
                        stats.setString(j, "identifier", id1);
                        continue block12;
                    }
                    case 2: {
                        stats.setString(j, "identifier", id1.replace('\t', ' '));
                        continue block12;
                    }
                    case 3: {
                        stats.setString(j, "identifier", id2);
                        stats.setString(j, "identifiersort", id1);
                        continue block12;
                    }
                    case 4: {
                        stats.setString(j, "identifier", id1);
                        continue block12;
                    }
                    case 6: {
                        stats.setString(j, "identifier", id1);
                        continue block12;
                    }
                    case 8: {
                        stats.setString(j, "identifier", id2);
                        stats.setString(j, "identifiersort", id1);
                    }
                }
            }
            if (stats.getColumnType("identifiersort") == 0 && sortby.equals("identifier")) {
                sortby = "identifiersort";
            }
            stats.sort(sortby + " " + sortbydirection);
            tabdetails.put("stats", stats);
            tabs.add(tabdetails);
        }
        pageContext.setAttribute("tabs", tabs);
    }

    public static DataSet loadCacheSizes(PageContext pageContext, boolean includeContents) throws SapphireException {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        StatusProcessor statusProcessor = new StatusProcessor(pageContext);
        DataSet cacheSizes = statusProcessor.getCacheSizes(includeContents);
        pageContext.setAttribute("cacheSizes", (Object)cacheSizes);
        pageContext.setAttribute("cachesizesexpand", (Object)userConfig.getProperty("cachesizesexpand"));
        return cacheSizes;
    }

    public static DataSet loadClassLoaders(PageContext pageContext, boolean includeContents) throws SapphireException {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        StatusProcessor statusProcessor = new StatusProcessor(pageContext);
        DataSet classloaders = statusProcessor.getClassLoaderStats(includeContents);
        pageContext.setAttribute("classLoaders", (Object)classloaders);
        pageContext.setAttribute("classloadersexpand", (Object)userConfig.getProperty("classloadersexpand"));
        return classloaders;
    }

    public static List<String> loadLSMExceptions(PageContext pageContext) throws SapphireException {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        StatusProcessor statusProcessor = new StatusProcessor(pageContext);
        List<String> lsmExceptions = statusProcessor.getLSMExceptions();
        pageContext.setAttribute("lsmExceptions", lsmExceptions);
        pageContext.setAttribute("lsmexceptionsexpand", (Object)userConfig.getProperty("lsmexceptionsexpand"));
        return lsmExceptions;
    }

    public static void loadTableSizes(PageContext pageContext) throws SapphireException {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        StatusProcessor statusProcessor = new StatusProcessor(pageContext);
        DataSet tableSizes = statusProcessor.getTableSizes();
        pageContext.setAttribute("tableSizes", (Object)tableSizes);
        pageContext.setAttribute("tablesizesexpand", (Object)userConfig.getProperty("tablesizesexpand"));
    }

    public static void loadMemoryStats(PageContext pageContext) throws SapphireException {
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        PropertyList userConfig = requestContext.getPropertyList("userconfig");
        StatusProcessor statusProcessor = new StatusProcessor(pageContext);
        DataSet memoryStats = statusProcessor.getMemoryStats();
        pageContext.setAttribute("memoryStats", (Object)memoryStats);
        pageContext.setAttribute("memorystatsexpand", (Object)userConfig.getProperty("memorystatsexpand"));
    }
}

