/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FixHiddenReadonlyColumnUtil {
    public static String fixColumn(QueryProcessor qp, WebAdminProcessor wp, RequestProcessor requestProcessor, String root, boolean saving) throws Exception {
        String masterFile = FixHiddenReadonlyColumnUtil.getMasterFileName(root);
        String validationCheckFile = FixHiddenReadonlyColumnUtil.getValidationCheckFile(root);
        String allColsFile = FixHiddenReadonlyColumnUtil.getAllColsFile(root);
        String pageColsFile = FixHiddenReadonlyColumnUtil.getPageColsFile(root);
        String sdcColsFile = FixHiddenReadonlyColumnUtil.getSdcColsFile(root);
        String suspectColumnFile = FixHiddenReadonlyColumnUtil.getSuspectColumnFile(root);
        FixHiddenReadonlyColumnUtil.deleteFile(validationCheckFile);
        FixHiddenReadonlyColumnUtil.deleteFile(allColsFile);
        FixHiddenReadonlyColumnUtil.deleteFile(pageColsFile);
        FixHiddenReadonlyColumnUtil.deleteFile(sdcColsFile);
        HashMap<String, PropertyList> pagePropsCache = new HashMap<String, PropertyList>();
        HashMap<String, PropertyList> fullpagePropsCache = new HashMap<String, PropertyList>();
        int ROWID = 0;
        int WEBPAGE = 1;
        int SDCID = 2;
        int FROMNODE = 3;
        int COLUMNID = 4;
        int MODE = 6;
        int BLOCKUPDATE = 11;
        PropertyTree maintElement = wp.getPropertyTree("maint");
        maintElement.setId("maint");
        StringBuilder valdiationErrors = new StringBuilder();
        StringBuilder browserout = new StringBuilder();
        valdiationErrors.append("RowId\tPage\tColumnId\tUpdate Target\tStatus");
        HashSet<String> allcols = new HashSet<String>();
        HashSet<String> sdccols = new HashSet<String>();
        HashSet<String> pagecols = new HashSet<String>();
        HashSet<String> duplicatecolchecker = new HashSet<String>();
        HashMap<String, String> suspectColumns = new HashMap<String, String>();
        HashMap sdcCols = new HashMap();
        SDCProcessor sdcProcessor = new SDCProcessor(qp.getConnectionid());
        HashSet<String> savepages = new HashSet<String>();
        try {
            Throwable throwable = null;
            try (BufferedReader br = new BufferedReader(new FileReader(masterFile));){
                String string;
                while ((string = br.readLine()) != null) {
                    String target;
                    String[] stringArray = string.split("\t");
                    String string2 = stringArray[ROWID];
                    String webpageid = stringArray[WEBPAGE];
                    String sdcid = stringArray[SDCID];
                    String fromnode = stringArray[FROMNODE];
                    String columnid = stringArray[COLUMNID];
                    String mode = stringArray[MODE];
                    String blockupdate = stringArray[BLOCKUPDATE];
                    webpageid = StringUtil.replaceAll(webpageid, " (SDC?)", "");
                    if (string2.length() <= 0 || string2.contains("#")) continue;
                    valdiationErrors.append("\n" + string2 + "\t");
                    valdiationErrors.append(webpageid + "\t");
                    valdiationErrors.append(columnid + "\t");
                    String string3 = fromnode.length() > 0 ? "Node: " + fromnode : (target = webpageid.length() > 0 ? "Page: " + webpageid : "");
                    if (target.length() > 0) {
                        valdiationErrors.append(target + "\t");
                        String newBlockUpdate = "";
                        if (blockupdate.toUpperCase().startsWith("Y")) {
                            newBlockUpdate = "Y";
                        } else if (blockupdate.toUpperCase().startsWith("N")) {
                            newBlockUpdate = "N";
                        } else if (blockupdate.toUpperCase().startsWith("A")) {
                            newBlockUpdate = "A";
                        }
                        String cleancolumnid = columnid;
                        if (columnid.trim().contains(" ")) {
                            int pos = columnid.lastIndexOf(" ");
                            cleancolumnid = columnid.substring(pos + 1).trim();
                        }
                        String pagecol = webpageid + ";" + cleancolumnid;
                        if (cleancolumnid.length() > 0) {
                            if (duplicatecolchecker.contains(pagecol)) {
                                suspectColumns.put(pagecol, "-\tDuplicate column??\t" + columnid);
                            }
                            duplicatecolchecker.add(pagecol);
                        }
                        if (newBlockUpdate.length() > 0) {
                            if (newBlockUpdate.equals("Y") || newBlockUpdate.equals("A")) {
                                allcols.add(columnid);
                                sdccols.add(sdcid + ";" + columnid);
                                pagecols.add(webpageid + ";" + columnid);
                            }
                            if (columnid.length() > 0) {
                                HashSet<String> thissdccols = (HashSet<String>)sdcCols.get(sdcid);
                                try {
                                    if (thissdccols == null) {
                                        thissdccols = new HashSet<String>();
                                        PropertyListCollection columns = sdcProcessor.getColumns(sdcid);
                                        for (Object column : columns) {
                                            thissdccols.add(((PropertyList)column).getProperty("columnid"));
                                        }
                                    }
                                    if (!thissdccols.contains(cleancolumnid)) {
                                        String type = "normal";
                                        if (columnid.toLowerCase().contains("select") && columnid.toLowerCase().contains("from")) {
                                            type = "nested";
                                        } else if (columnid.contains(".")) {
                                            type = "fk column";
                                        }
                                        suspectColumns.put(pagecol, type + "\tNot a real columnid but trying to set to " + newBlockUpdate + "\t" + columnid);
                                    }
                                }
                                catch (Exception type) {
                                    // empty catch block
                                }
                                DataSet pages = qp.getPreparedSqlDataSet("SELECT * FROM webpage WHERE productedition='R5' AND webpageid=?", (Object[])new String[]{webpageid});
                                if (pages.size() == 1) {
                                    PropertyList maint;
                                    PropertyList col;
                                    PropertyList allPageProps = (PropertyList)fullpagePropsCache.get(webpageid);
                                    if (allPageProps == null) {
                                        allPageProps = requestProcessor.getWebPageProperties(webpageid, "R5", new PropertyList(), false);
                                        fullpagePropsCache.put(webpageid, allPageProps);
                                    }
                                    if ((col = (maint = allPageProps.getPropertyList("maint")).getCollectionNotNull("columns").find("columnid", columnid)) != null) {
                                        String plid = col.getId();
                                        if (fromnode.length() > 0) {
                                            Node node = maintElement.getNode(fromnode);
                                            PropertyList props = node.getPropertyList();
                                            boolean save = FixHiddenReadonlyColumnUtil.processProps(props, columnid, plid, mode, newBlockUpdate, valdiationErrors);
                                            if (!save || !saving) continue;
                                            browserout.append("UPDATING node " + fromnode + "<br>");
                                            continue;
                                        }
                                        if (webpageid.length() > 0) {
                                            PropertyList props = (PropertyList)pagePropsCache.get(webpageid);
                                            if (props == null) {
                                                String productvaluetree = qp.getPreparedSqlDataSet("SELECT productvaluetree FROM webpagepropertytree WHERE webpageid=? AND propertytreeid=? AND elementid=? AND productedition=?", (Object[])new String[]{webpageid, "maint", "maint", "R5"}, true).getValue(0, "productvaluetree");
                                                props = new PropertyList();
                                                props.setPropertyList(productvaluetree);
                                                pagePropsCache.put(webpageid, props);
                                            }
                                            boolean save = FixHiddenReadonlyColumnUtil.processProps(props, columnid, plid, mode, newBlockUpdate, valdiationErrors);
                                            if (!saving || !save) continue;
                                            savepages.add(webpageid);
                                            continue;
                                        }
                                        FixHiddenReadonlyColumnUtil.writeError(valdiationErrors, "No FromNode or Pageid");
                                        continue;
                                    }
                                    FixHiddenReadonlyColumnUtil.writeError(valdiationErrors, "Could not find column " + columnid);
                                    continue;
                                }
                                FixHiddenReadonlyColumnUtil.writeError(valdiationErrors, "found more than one page edition for " + webpageid);
                                continue;
                            }
                            FixHiddenReadonlyColumnUtil.writeError(valdiationErrors, "No columnid");
                            continue;
                        }
                        if (blockupdate.length() <= 0) continue;
                        FixHiddenReadonlyColumnUtil.writeError(valdiationErrors, "Unrecognized BlockUpdate: " + blockupdate);
                        continue;
                    }
                    FixHiddenReadonlyColumnUtil.writeError(valdiationErrors, "No Node or WebPage");
                }
            }
            catch (Throwable throwable2) {
                Throwable throwable3 = throwable2;
                throw throwable2;
            }
        }
        catch (Exception e) {
            return "Oops!!!!!!!!!!!! " + e.getMessage();
        }
        if (saving) {
            for (String string : savepages) {
                browserout.append("UPDATING AND SAVING webpage " + string + "<br>");
                PropertyList propertyList = (PropertyList)pagePropsCache.get(string);
                qp.execPreparedUpdate("UPDATE webpagepropertytree SET productvaluetree = ? WHERE webpageid=? AND propertytreeid=? AND elementid=? AND productedition = ?", new String[]{propertyList.toXMLString(), string, "maint", "maint", "R5"});
            }
            browserout.append("SAVING maint element<br>");
            wp.savePropertyTree(maintElement);
        } else {
            StringBuilder sdccolsout = new StringBuilder();
            for (String string : sdccols) {
                String[] stringArray = StringUtil.split(string, ";");
                sdccolsout.append(stringArray[0] + "\t" + stringArray[1] + "\n");
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String string : pagecols) {
                String[] stringArray = StringUtil.split(string, ";");
                stringBuilder.append(stringArray[0] + "\t" + stringArray[1] + "\n");
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            for (String string : allcols) {
                stringBuilder2.append(string + "\n");
            }
            StringBuilder stringBuilder3 = new StringBuilder();
            for (String key : suspectColumns.keySet()) {
                String[] parts = StringUtil.split(key, ";");
                stringBuilder3.append(parts[0] + "\t" + parts[1] + "\t" + (String)suspectColumns.get(key) + "\n");
            }
            try (PrintWriter printWriter = new PrintWriter(validationCheckFile);){
                printWriter.println(valdiationErrors);
            }
            var36_68 = null;
            try (PrintWriter printWriter = new PrintWriter(allColsFile);){
                printWriter.println(stringBuilder2);
            }
            catch (Throwable throwable) {
                var36_68 = throwable;
                throw throwable;
            }
            var36_68 = null;
            try (PrintWriter printWriter = new PrintWriter(pageColsFile);){
                printWriter.println(stringBuilder);
            }
            catch (Throwable throwable) {
                var36_68 = throwable;
                throw throwable;
            }
            var36_68 = null;
            try (PrintWriter printWriter = new PrintWriter(sdcColsFile);){
                printWriter.println(sdccolsout);
            }
            catch (Throwable throwable) {
                var36_68 = throwable;
                throw throwable;
            }
            var36_68 = null;
            try (PrintWriter printWriter = new PrintWriter(sdcColsFile);){
                printWriter.println(sdccolsout);
            }
            catch (Throwable throwable) {
                var36_68 = throwable;
                throw throwable;
            }
            var36_68 = null;
            try (PrintWriter printWriter = new PrintWriter(suspectColumnFile);){
                printWriter.println(stringBuilder3);
            }
            catch (Throwable throwable) {
                var36_68 = throwable;
                throw throwable;
            }
        }
        browserout.append("<br>Done");
        return browserout.toString();
    }

    public static String javaCodeCheck(QueryProcessor qp, WebAdminProcessor wp, RequestProcessor requestProcessor, String root, String javaddtfolder) throws Exception {
        String sdcColsFile = FixHiddenReadonlyColumnUtil.getSdcColsFile(root);
        String javaCodeFileOut = FixHiddenReadonlyColumnUtil.getJavaCollisionsFile(root);
        FixHiddenReadonlyColumnUtil.deleteFile(javaCodeFileOut);
        StringBuilder contentout = new StringBuilder();
        HashMap<String, HashSet<String>> sdccols = new HashMap<String, HashSet<String>>();
        try (BufferedReader br = new BufferedReader(new FileReader(sdcColsFile));){
            String line;
            while ((line = br.readLine()) != null) {
                HashSet<String> cols;
                String[] parts;
                if (line.length() <= 0 || (parts = line.split("\t")).length != 2) continue;
                String sdcid = parts[0];
                String columnid = parts[1];
                if (columnid.trim().contains(" ")) {
                    int pos = columnid.lastIndexOf(" ");
                    columnid = columnid.substring(pos + 1);
                }
                if ((cols = (HashSet<String>)sdccols.get(sdcid)) == null) {
                    cols = new HashSet<String>();
                    sdccols.put(sdcid, cols);
                }
                cols.add(columnid);
            }
            for (String sdcid : sdccols.keySet()) {
                Set columns = (Set)sdccols.get(sdcid);
                String javafile = javaddtfolder + "/" + sdcid + ".java";
                File f = new File(javafile);
                if (!f.exists()) continue;
                StringBuilder javacode = new StringBuilder();
                try (Stream<String> stream = Files.lines(f.toPath(), StandardCharsets.UTF_8);){
                    stream.forEach(s -> javacode.append((String)s).append("\n"));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Checking " + sdcid);
                for (String columnid : columns) {
                    if (!javacode.toString().contains("\"" + columnid + "\"")) continue;
                    contentout.append(sdcid + "\t" + columnid + "\t is referenced in java code\n");
                }
            }
            try (PrintWriter out = new PrintWriter(javaCodeFileOut);){
                out.println(contentout);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Oopps!!!!!!!!!!!! " + e.getMessage();
        }
        return "Done";
    }

    public static String javascriptCodeCheck(QueryProcessor qp, WebAdminProcessor wp, RequestProcessor requestProcessor, String root, String webroot) throws Exception {
        String pageColsFile = FixHiddenReadonlyColumnUtil.getPageColsFile(root);
        String javascriptCodeFileOut = FixHiddenReadonlyColumnUtil.getJavaScriptCollisionsFile(root);
        String javascriptPropertiesCodeFileOut = FixHiddenReadonlyColumnUtil.getJavaScriptPropertiesCollisionsFile(root);
        String allFilesOut = FixHiddenReadonlyColumnUtil.getAllFilesOutFile(root);
        FixHiddenReadonlyColumnUtil.deleteFile(javascriptCodeFileOut);
        FixHiddenReadonlyColumnUtil.deleteFile(javascriptPropertiesCodeFileOut);
        StringBuilder contentout = new StringBuilder();
        StringBuilder propertiesout = new StringBuilder();
        HashMap<String, HashSet<String>> pagecols = new HashMap<String, HashSet<String>>();
        HashSet<String> allFiles = new HashSet<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(pageColsFile));){
            String line;
            while ((line = br.readLine()) != null) {
                HashSet<String> cols;
                String[] parts;
                if (line.length() <= 0 || (parts = line.split("\t")).length != 2) continue;
                String pageid = parts[0];
                String columnid = parts[1];
                if (columnid.trim().contains(" ")) {
                    int pos = columnid.lastIndexOf(" ");
                    columnid = columnid.substring(pos + 1);
                }
                if ((cols = (HashSet<String>)pagecols.get(pageid)) == null) {
                    cols = new HashSet<String>();
                    pagecols.put(pageid, cols);
                }
                cols.add(columnid);
            }
            for (Object webpageid : pagecols.keySet()) {
                Set columns = (Set)pagecols.get(webpageid);
                PropertyList allPageProps = requestProcessor.getWebPageProperties((String)webpageid, "R5", new PropertyList(), false);
                FixHiddenReadonlyColumnUtil.processJavaScriptProperties(allPageProps, propertiesout, (String)webpageid, columns);
                PropertyList pagedata = allPageProps.getPropertyListNotNull("pagedata");
                PropertyListCollection includes = pagedata.getCollectionNotNull("includes");
                FixHiddenReadonlyColumnUtil.processIncludesCollection(webroot, contentout, (String)webpageid, columns, includes, "page", "url", allFiles);
                PropertyList advancedtoolbar = allPageProps.getPropertyListNotNull("advancedtoolbar");
                includes = advancedtoolbar.getCollectionNotNull("includes");
                FixHiddenReadonlyColumnUtil.processIncludesCollection(webroot, contentout, (String)webpageid, columns, includes, "advancedtoolbar", "url", allFiles);
                PropertyList layout = allPageProps.getPropertyListNotNull("layout");
                includes = layout.getCollectionNotNull("includes");
                FixHiddenReadonlyColumnUtil.processIncludesCollection(webroot, contentout, (String)webpageid, columns, includes, "layout", "url", allFiles);
                for (Object key : allPageProps.keySet()) {
                    PropertyList pl;
                    Object o = allPageProps.get(key);
                    if (!(o instanceof PropertyList) || !(pl = (PropertyList)o).getProperty("propertytreeid").equals("sdidetailmaint")) continue;
                    includes = pl.getCollectionNotNull("jsincludes");
                    FixHiddenReadonlyColumnUtil.processIncludesCollection(webroot, contentout, (String)webpageid, columns, includes, pl.getProperty("elementid"), "src", allFiles);
                }
            }
            StringBuilder allfilesout = new StringBuilder();
            for (String file : allFiles) {
                allfilesout.append(file + "\n");
            }
            try (PrintWriter out = new PrintWriter(javascriptCodeFileOut);){
                out.println(contentout);
            }
            out = new PrintWriter(javascriptPropertiesCodeFileOut);
            var18_21 = null;
            try {
                out.println(propertiesout);
            }
            catch (Throwable throwable) {
                var18_21 = throwable;
                throw throwable;
            }
            finally {
                if (out != null) {
                    if (var18_21 != null) {
                        try {
                            out.close();
                        }
                        catch (Throwable throwable) {
                            var18_21.addSuppressed(throwable);
                        }
                    } else {
                        out.close();
                    }
                }
            }
            out = new PrintWriter(allFilesOut);
            var18_21 = null;
            try {
                out.println(allfilesout);
            }
            catch (Throwable throwable) {
                var18_21 = throwable;
                throw throwable;
            }
            finally {
                if (out != null) {
                    if (var18_21 != null) {
                        try {
                            out.close();
                        }
                        catch (Throwable throwable) {
                            var18_21.addSuppressed(throwable);
                        }
                    } else {
                        out.close();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return "Oopps!!!!!!!!!!!! " + e.getMessage();
        }
        return "Done";
    }

    private static void processJavaScriptProperties(PropertyList allPageProps, StringBuilder propertiesout, String webpageid, Set<String> columns) {
        PropertyList maint = allPageProps.getPropertyListNotNull("maint");
        PropertyListCollection columnscol = maint.getCollectionNotNull("columns");
        for (Object o : columnscol) {
            PropertyList col = (PropertyList)o;
            String columnid = col.getProperty("columnid");
            if (columnid.trim().contains(" ")) {
                int pos = columnid.lastIndexOf(" ");
                columnid = columnid.substring(pos + 1);
            }
            FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getPropertyListNotNull("link").getProperty("href"), columnid + ": link-href", true);
            FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getPropertyListNotNull("lookuplink").getProperty("href"), columnid + ": lookup-href", true);
            FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("displayvalue"), columnid + ": display value", false);
            FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("pseudocolumn"), columnid + ": pseudo column", false);
            PropertyListCollection events = col.getCollectionNotNull("events");
            for (Object o2 : events) {
                PropertyList event = (PropertyList)o2;
                FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, event.getProperty("js"), columnid + ": event " + event.getProperty("event"), false);
            }
        }
        PropertyList toolbar = allPageProps.getPropertyListNotNull("advancedtoolbar");
        PropertyListCollection buttons = toolbar.getCollectionNotNull("buttons");
        for (Object o : buttons) {
            PropertyList button = (PropertyList)o;
            FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, button.getProperty("primaryvalidation"), button.getProperty("id") + ": primary validation", true);
            FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, button.getProperty("initialvalidation"), button.getProperty("id") + ": initial validation", true);
            FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, button.getPropertyListNotNull("userbuttonprops").getProperty("action"), button.getProperty("id") + ": user button", true);
        }
        for (Object key : allPageProps.keySet()) {
            String columnid;
            PropertyList col;
            Object o = allPageProps.get(key);
            if (!(o instanceof PropertyList)) continue;
            PropertyList element = (PropertyList)o;
            if (element.getProperty("propertytreeid").equals("sdidetailmaint")) {
                columnscol = maint.getCollectionNotNull("columns");
                for (Object o3 : columnscol) {
                    col = (PropertyList)o3;
                    columnid = col.getProperty("columnid");
                    if (columnid.trim().contains(" ")) {
                        int pos = columnid.lastIndexOf(" ");
                        columnid = columnid.substring(pos + 1);
                    }
                    FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getPropertyListNotNull("link").getProperty("href"), element.getProperty("elementid") + " -> " + columnid + ": link-href", true);
                    FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("pseudo"), element.getProperty("elementid") + " -> " + columnid + ": pseudo column", true);
                    FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("displayvalue"), element.getProperty("elementid") + " -> " + columnid + ": display value", true);
                    FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("lookuppage"), element.getProperty("elementid") + " -> " + columnid + ": lookup page", true);
                }
                continue;
            }
            if (!element.getProperty("propertytreeid").equals("sdclinkmaint")) continue;
            columnscol = maint.getCollectionNotNull("columns");
            for (Object o3 : columnscol) {
                col = (PropertyList)o3;
                columnid = col.getProperty("columnid");
                if (columnid.trim().contains(" ")) {
                    int pos = columnid.lastIndexOf(" ");
                    columnid = columnid.substring(pos + 1);
                }
                FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("customjs"), element.getProperty("elementid") + " -> " + columnid + ": customjs", true);
                FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getPropertyListNotNull("link").getProperty("href"), element.getProperty("elementid") + " -> " + columnid + ": link-href", true);
                FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getPropertyListNotNull("lookuplink").getProperty("href"), element.getProperty("elementid") + " -> " + columnid + ": lookup link-href", true);
                FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("displayvalue"), element.getProperty("elementid") + " -> " + columnid + ": display value", true);
                FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, col.getProperty("pseudo"), element.getProperty("elementid") + " -> " + columnid + ": pseudo column", true);
                PropertyListCollection events = col.getCollectionNotNull("events");
                for (Object o2 : events) {
                    PropertyList event = (PropertyList)o2;
                    FixHiddenReadonlyColumnUtil.checkforJavaScriptProperty(propertiesout, webpageid, columns, event.getProperty("js"), element.getProperty("elementid") + " -> " + columnid + ": event " + event.getProperty("event"), false);
                }
            }
        }
    }

    private static void checkforJavaScriptProperty(StringBuilder propertiesout, String webpageid, Set<String> columns, String href, String label, boolean checkJavaScript) {
        if (href.length() == 0) {
            return;
        }
        if (!checkJavaScript || href.contains("javascript")) {
            for (String columnid : columns) {
                if (!href.contains(columnid)) continue;
                href = href.replace("\n", " -> ");
                href = href.replace("\t", " ");
                propertiesout.append(columnid + "\t" + webpageid + "\t" + label + "\t" + href + "\n");
            }
        }
    }

    public static String blindCodeCheck(QueryProcessor qp, WebAdminProcessor wp, RequestProcessor requestProcessor, String root) throws Exception {
        Object line;
        String allColsFile = FixHiddenReadonlyColumnUtil.getAllColsFile(root);
        String blindFile = FixHiddenReadonlyColumnUtil.getBlindFileSource(root);
        String allFilesFile = FixHiddenReadonlyColumnUtil.getAllFilesOutFile(root);
        String blindCodeFile = FixHiddenReadonlyColumnUtil.getBlindFileCollisions(root);
        FixHiddenReadonlyColumnUtil.deleteFile(blindCodeFile);
        HashSet<String> cols = new HashSet<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(allColsFile));){
            while ((line = br.readLine()) != null) {
                if (((String)line).length() <= 0) continue;
                if (((String)line).trim().contains(" ")) {
                    int pos = ((String)line).lastIndexOf(" ");
                    line = ((String)line).substring(pos + 1);
                }
                cols.add((String)line);
            }
        }
        HashSet<String> files = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(allFilesFile));
        line = null;
        try {
            String line2;
            while ((line2 = br.readLine()) != null) {
                if (line2.length() <= 0) continue;
                files.add(line2);
            }
        }
        catch (Throwable throwable) {
            line = throwable;
            throw throwable;
        }
        finally {
            if (br != null) {
                if (line != null) {
                    try {
                        br.close();
                    }
                    catch (Throwable throwable) {
                        ((Throwable)line).addSuppressed(throwable);
                    }
                } else {
                    br.close();
                }
            }
        }
        StringBuilder contentout = new StringBuilder();
        try (BufferedReader br2 = new BufferedReader(new FileReader(blindFile));){
            String line3;
            String lastfile = "";
            String lastfolder = "";
            while ((line3 = br2.readLine()) != null) {
                if ((line3 = line3.trim()).length() <= 0) continue;
                if (line3.startsWith("WEB")) {
                    lastfolder = line3.substring(0, line3.indexOf(" "));
                    lastfolder = lastfolder.replace(".", "/");
                    continue;
                }
                if (line3.contains("found)")) {
                    lastfile = lastfolder + "/" + line3.trim().substring(0, line3.trim().indexOf(" "));
                    if (files.contains(lastfile)) {
                        lastfile = "";
                        continue;
                    }
                    System.out.println("skipping " + lastfile);
                    continue;
                }
                if (lastfile.length() <= 0) continue;
                for (String col : cols) {
                    if (!line3.contains(col)) continue;
                    contentout.append(lastfile + "\t" + col + "\t" + line3 + "\n");
                }
            }
        }
        var12_15 = null;
        try (PrintWriter out = new PrintWriter(blindCodeFile);){
            out.println(contentout);
        }
        catch (Throwable throwable) {
            var12_15 = throwable;
            throw throwable;
        }
        return "Done";
    }

    private static void processIncludesCollection(String webroot, StringBuilder out, String webpageid, Set<String> columns, PropertyListCollection includes, String label, String urlproperty, HashSet<String> files) {
        if (includes.size() > 0) {
            for (PropertyList pl : includes) {
                String url = pl.getProperty(urlproperty);
                if (url.length() <= 0) continue;
                files.add(url);
                FixHiddenReadonlyColumnUtil.processJavaScriptFile(webroot, out, webpageid, columns, url, label);
            }
        }
    }

    private static void processJavaScriptFile(String webroot, StringBuilder out, String webpageid, Set<String> columns, String url, String label) {
        String jsfile = webroot + "/" + url;
        File f = new File(jsfile);
        if (f.exists()) {
            StringBuilder jscode = new StringBuilder();
            try (Stream<String> stream = Files.lines(f.toPath(), StandardCharsets.UTF_8);){
                stream.forEach(s -> jscode.append((String)s).append("\n"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Checking " + webpageid);
            for (String columnid : columns) {
                if (!jscode.toString().contains(columnid)) continue;
                out.append(url + "\t" + columnid + "\t" + webpageid + "\t" + label + "\n");
            }
        }
    }

    private static void deleteFile(String validationErrorsFile) {
        try {
            Files.delete(Paths.get(validationErrorsFile, new String[0]));
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static void writeError(StringBuilder out, String message) throws IOException {
        out.append("ERROR\t" + message + "\t");
    }

    private static void writeSkip(StringBuilder out, String message) throws IOException {
        out.append("Skipping\t" + message + "\t");
    }

    private static void writeSuccess(StringBuilder out, String message) throws IOException {
        out.append("SUCCESS\t" + message + "\t");
    }

    private static boolean processProps(PropertyList props, String columnid, String plid, String mode, String newBlockUpdate, StringBuilder out) throws IOException {
        boolean save = false;
        PropertyListCollection columns = props.getCollectionNotNull("columns");
        PropertyList col = columns.getPropertyList(plid);
        if (col != null) {
            String foundmode = col.getProperty("mode");
            if (foundmode.equals(mode)) {
                String currentBlockUpdate = col.getProperty("blockupdates");
                if (currentBlockUpdate.length() == 0) {
                    FixHiddenReadonlyColumnUtil.writeSuccess(out, "Setting to " + newBlockUpdate);
                    col.setProperty("blockupdates", newBlockUpdate);
                    save = true;
                } else if (currentBlockUpdate.equals(newBlockUpdate)) {
                    FixHiddenReadonlyColumnUtil.writeSkip(out, "Already set to " + newBlockUpdate);
                } else {
                    FixHiddenReadonlyColumnUtil.writeError(out, "Cannot set to " + newBlockUpdate + " because already set to " + currentBlockUpdate + "!!!!");
                }
            } else {
                FixHiddenReadonlyColumnUtil.writeError(out, "Mode Mismatch (found=" + foundmode + " but expeceting " + mode);
            }
        } else {
            FixHiddenReadonlyColumnUtil.writeError(out, "Failed to find column " + columnid + " with properytlistid = " + plid);
        }
        return save;
    }

    private static String getMasterFileName(String root) {
        return root + "/_master.txt";
    }

    private static String getBlindFileSource(String root) {
        return root + "/_blindtextsearch.txt";
    }

    private static String getValidationCheckFile(String root) {
        return root + "/1_validationcheck.txt";
    }

    private static String getSdcColsFile(String root) {
        return root + "/2_sdccols.txt";
    }

    private static String getSuspectColumnFile(String root) {
        return root + "/2_validcolumn.txt";
    }

    private static String getPageColsFile(String root) {
        return root + "/2_pagescols.txt";
    }

    private static String getAllColsFile(String root) {
        return root + "/2_allcols.txt";
    }

    private static String getJavaCollisionsFile(String root) {
        return root + "/3_java_collisions.txt";
    }

    private static String getJavaScriptCollisionsFile(String root) {
        return root + "/4_javascript_collisions.txt";
    }

    private static String getJavaScriptPropertiesCollisionsFile(String root) {
        return root + "/4_javascript_properties_collisions.txt";
    }

    private static String getAllFilesOutFile(String root) {
        return root + "/4_javascript_files.txt";
    }

    private static String getBlindFileCollisions(String root) {
        return root + "/5_blind_collisions.txt";
    }
}

