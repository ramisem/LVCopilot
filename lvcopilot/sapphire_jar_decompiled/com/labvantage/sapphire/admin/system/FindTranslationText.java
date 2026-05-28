/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.components.table.BaseColumn
 *  net.sf.jasperreports.components.table.StandardTable
 *  net.sf.jasperreports.engine.JRBand
 *  net.sf.jasperreports.engine.JRElement
 *  net.sf.jasperreports.engine.JRGroup
 *  net.sf.jasperreports.engine.JRSection
 *  net.sf.jasperreports.engine.design.JRDesignComponentElement
 *  net.sf.jasperreports.engine.design.JRDesignStaticText
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.xml.JRXmlLoader
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.admin.system.TranslationUtil;
import com.labvantage.sapphire.platform.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import net.sf.jasperreports.components.table.BaseColumn;
import net.sf.jasperreports.components.table.StandardTable;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.design.JRDesignComponentElement;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class FindTranslationText {
    private ConnectionInfo connectionInfo;
    private static String projectPath = "C:\\Development\\Sapphire\\LV_8.8\\maven";

    public DataSet testCollectCodeTexts() {
        String apllicationHome = "";
        try {
            apllicationHome = Configuration.getInstance().getSapphireHome();
        }
        catch (SapphireException e) {
            e.printStackTrace();
        }
        String javaSourceDir = projectPath + "\\labvantage\\java\\src\\main\\java";
        String webSourceDir = projectPath + "\\labvantage\\web\\src\\main\\webapp";
        String gwtSourceDir = projectPath + "\\labvantage\\web\\src\\main\\gwt";
        String stellarJavaSourceDir = projectPath + "\\labvantage\\stellar-java\\src\\main\\java";
        String stellarWebSourceDir = projectPath + "\\labvantage\\stellar-web\\src\\main\\webapp";
        String jasperReportDir = apllicationHome + "\\applications\\labvantage\\reports\\OOB";
        String spreedjsDir = projectPath + "/com/labvantage/sapphire/modules/eln/gwt/server/worksheetitem/spreadsheet_transmaster.json";
        boolean isCollectSuspect = false;
        return FindTranslationText.collectCodeTexts(javaSourceDir, webSourceDir, gwtSourceDir, stellarJavaSourceDir, stellarWebSourceDir, jasperReportDir, isCollectSuspect);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static DataSet collectCodeTexts(String javaFileDir, String webFileDir, String gwtFileDir, String stellarJavaSourceDir, String stellarWebSourceDir, String jasperReportDir, boolean isAddSuspect) {
        DataSet dataset = new DataSet();
        dataset.addColumn("textid", 0);
        dataset.addColumn("texttype", 0);
        dataset.addColumn("cf", 0);
        dataset.addColumn("source", 0);
        FindTranslationText.findInJavaSource(new File(javaFileDir), dataset, isAddSuspect);
        FindTranslationText.findInWebSource(new File(webFileDir), dataset, isAddSuspect);
        FindTranslationText.findInGWTSource(new File(gwtFileDir), dataset, isAddSuspect);
        FindTranslationText.findInJavaSource(new File(stellarJavaSourceDir), dataset, isAddSuspect);
        FindTranslationText.findInStellarWebSource(new File(stellarWebSourceDir), dataset, isAddSuspect);
        FindTranslationText.findInJasperReport(new File(jasperReportDir), dataset);
        if (isAddSuspect) {
            dataset.sort("source, textid");
        } else {
            dataset.sort("textid");
        }
        String previousOne = "";
        String currentline = "";
        Set processed = TranslationUtil.PropertyTranslationProcessor.textidSet;
        OutputStreamWriter writer = null;
        try {
            writer = new FileWriter(new File("C:/temp/TransmasterCode" + new SimpleDateFormat("yyyy-MM-dd-h-mm-ss").format(Calendar.getInstance().getTime()) + ".csv"));
            writer.write("Textid,Context,CF,Source");
            for (int i = 0; i < dataset.getRowCount(); ++i) {
                boolean findIgnoreCase = TranslationUtil.findIgnoreCase;
                String textid = dataset.getValue(i, "textid");
                if ("Y".equals(dataset.getValue(i, "cf"))) {
                    findIgnoreCase = false;
                }
                if (textid.equals(previousOne)) {
                    if (isAddSuspect) {
                        currentline = currentline + "," + dataset.getValue(i, "source");
                    }
                } else if (!processed.contains(findIgnoreCase ? textid.toLowerCase() : textid)) {
                    System.out.println(currentline);
                    writer.write(currentline + "\r\n");
                    String filename = dataset.getValue(i, "source");
                    if (filename.indexOf("java\\classes\\") > 0) {
                        filename = filename.substring(filename.indexOf("java\\classes\\") + 13);
                    } else if (filename.indexOf("java\\gwt\\") > 0) {
                        filename = filename.substring(filename.indexOf("java\\gwt\\") + 9);
                    } else if (filename.indexOf("WEB-CORE") > 0) {
                        filename = filename.substring(filename.indexOf("WEB-CORE"));
                    } else if (filename.indexOf("WEB-OPAL") > 0) {
                        filename = filename.substring(filename.indexOf("WEB-OPAL"));
                    }
                    if (textid.indexOf(",") >= 0) {
                        textid = "\"" + textid + "\"";
                    }
                    currentline = textid + ",W," + dataset.getValue(i, "cf", "N") + "," + filename;
                    processed.add(findIgnoreCase ? textid.toLowerCase() : textid);
                }
                previousOne = textid;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return dataset;
    }

    private static void findInJavaSource(File dir, DataSet dataset, boolean isAddSuspect) {
        FindTranslationText.findText(dir, dataset, new String[]{".translate(", ".translatePartial(", "//translate", "translateText("}, isAddSuspect);
    }

    private static void findInWebSource(File dir, DataSet dataset, boolean isAddSuspect) {
        FindTranslationText.findText(dir, dataset, new String[]{"sapphire.translate(", "sapphire.translatePartial("}, isAddSuspect);
        FindTranslationText.findText(dir, dataset, new String[]{".translate(", ".translatePartial("}, isAddSuspect);
    }

    private static void findInStellarWebSource(File dir, DataSet dataset, boolean isAddSuspect) {
        FindTranslationText.findText(dir, dataset, new String[]{"stellar.translate(", "window.stellar.translatePartial("}, isAddSuspect);
        FindTranslationText.findText(dir, dataset, new String[]{".translate(", ".translatePartial("}, isAddSuspect);
        FindTranslationText.findText(dir, dataset, new String[]{"alert(", "confirm("}, isAddSuspect);
    }

    private static void findInGWTSource(File dir, DataSet dataset, boolean isAddSuspect) {
        FindTranslationText.findText(dir, dataset, new String[]{"Trans.text("}, isAddSuspect);
    }

    private static void findText(File dir, DataSet set, String[] tokens, final boolean isAddSuspect) {
        block9: {
            block10: {
                if (!dir.exists()) break block9;
                if (!dir.isDirectory() || "CVS".equals(dir.getName())) break block10;
                if (dir.getAbsolutePath().contains("java\\com\\labvantage\\sapphire\\modules\\eln\\gwt\\server")) {
                    boolean bl = false;
                }
                File[] files = dir.listFiles(new FileFilter(){

                    @Override
                    public boolean accept(File pathname) {
                        if (isAddSuspect && (pathname.getPath().indexOf("gwt\\modules\\dataentry\\client\\DataEntryToolBar.java") > 0 || pathname.getPath().indexOf("com\\labvantage\\sapphire\\pageelements\\ElementUtil.java") > 0 || pathname.getPath().indexOf("com\\labvantage\\sapphire\\pageelements\\list\\List.java") > 0 || pathname.getPath().indexOf("com\\labvantage\\sapphire\\report\\jasper") > 0 || pathname.getPath().indexOf("com\\labvantage\\sapphire\\gwt\\modules\\dataentry\\client") > 0 || pathname.getPath().indexOf("com\\labvantage\\opal\\elements\\advancedtoolbar\\StandardButtonsUtil.java") > 0)) {
                            return false;
                        }
                        return pathname.getName().indexOf("cache.js") < 0 && pathname.getName().indexOf("jsPlumb.") < 0 && pathname.getName().indexOf("canvg.js") < 0 && pathname.getName().indexOf("TranslationUtil.java") < 0 && pathname.getAbsolutePath().indexOf("gwt\\com\\labvantage\\sapphire\\gwt\\modules\\console\\client") < 0 && (pathname.isDirectory() || pathname.getName().contains("spreadsheet_transmaster.json") || pathname.getName().indexOf(".jsx") == pathname.getName().length() - 4 || pathname.getName().indexOf(".java") == pathname.getName().length() - 5 || pathname.getName().indexOf(".jsp") == pathname.getName().length() - 4 || pathname.getName().indexOf(".js") == pathname.getName().length() - 3);
                    }
                });
                for (int i = 0; i < files.length; ++i) {
                    FindTranslationText.findText(files[i], set, tokens, isAddSuspect);
                }
                break block9;
            }
            if (!dir.isFile()) break block9;
            if (dir.getPath().equalsIgnoreCase(projectPath + "\\labvantage\\stellar-java\\src\\main\\java\\com\\labvantage\\stellar\\messagehandlers\\HandleLoginMessage.java")) {
                boolean files = false;
            }
            try {
                if (dir.getName().contains(".json")) {
                    BufferedReader reader = new BufferedReader(new FileReader(dir));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        int pos1 = line.indexOf(":");
                        int pos2 = line.indexOf("\"");
                        if (pos1 <= 0 || pos2 <= 0 || pos1 >= pos2) continue;
                        int pos3 = line.lastIndexOf("\"");
                        String sub = line.substring(pos2 + 1, pos3);
                        FindTranslationText.addTextid(set, "translate", sub, dir.getPath());
                    }
                }
                BufferedReader reader = new BufferedReader(new FileReader(dir));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    for (int t = 0; t < tokens.length; ++t) {
                        if (line.indexOf(tokens[t]) < 0) continue;
                        FindTranslationText.addTextid(set, line, tokens[t], dir.getPath(), isAddSuspect);
                    }
                }
                reader.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addTextid(DataSet set, String line, String token, String filename, boolean isAddSuspect) {
        ArrayList<String> textids = FindTranslationText.getTextids(line, token);
        for (int t = 0; t < textids.size(); ++t) {
            String textid = textids.get(t);
            if (textid.contains("Click on the <i>Reset Password</i>")) {
                boolean bl = false;
            }
            boolean add = true;
            if (isAddSuspect) {
                if (textid.indexOf(", map") > 0 || textid.indexOf(", valueMap") > 0 || textid.indexOf(", tokenMap") > 0 || textid.indexOf(", transmap") > 0 || textid.indexOf(", new String[]") > 0) {
                    add = false;
                } else if (textid.indexOf("[") >= 0 && textid.indexOf("]") > 0 && filename.indexOf(".js") != filename.length() - 3) {
                    add = true;
                } else if (textid.indexOf("\" ") == 0 || textid.lastIndexOf(" \"") == textid.length() - 2) {
                    add = true;
                } else if (textid.indexOf("'") < 0 && textid.indexOf("\"") < 0 && textid.indexOf("+") < 0 || textid.indexOf("\"") > 0 && textid.indexOf("+") < 0 && (textid.indexOf(".getProperty(") > 0 || textid.indexOf(".getValue(") > 0)) {
                    add = false;
                } else if (".translatePartial(".equals(token) && textid.indexOf("{{") >= 0 && textid.indexOf("}}") > 0) {
                    add = false;
                } else {
                    if (textid.indexOf("\"") == 0 && textid.lastIndexOf("\"") == textid.length() - 1 && textid.indexOf("+") < 0 && textid.indexOf("<") <= 0 && textid.indexOf("\r") <= 0 && textid.indexOf("\n") < 0) {
                        add = false;
                    }
                    if (textid.indexOf("'") == 0 && textid.lastIndexOf("'") == textid.length() - 1 && textid.indexOf("+") < 0 && textid.indexOf("<") <= 0 && textid.indexOf("\r") <= 0 && textid.indexOf("\n") < 0) {
                        add = false;
                    }
                }
            } else {
                add = false;
                if (textid.indexOf("\"") == 0 && textid.lastIndexOf("\"") == textid.length() - 1 && textid.indexOf("+") < 0 && textid.indexOf("<") <= 0 && textid.indexOf("\r") <= 0 && textid.indexOf("\n") < 0) {
                    add = true;
                }
                if (textid.indexOf("'") == 0 && textid.lastIndexOf("'") == textid.length() - 1 && textid.indexOf("+") < 0 && textid.indexOf("<") <= 0 && textid.indexOf("\r") <= 0 && textid.indexOf("\n") < 0) {
                    add = true;
                    textid = "\"" + textid.substring(1, textid.length() - 1) + "\"";
                }
                if (textid.indexOf(", map") > 0 || textid.indexOf(", valueMap") > 0 || textid.indexOf(", tokenMap") > 0 || textid.indexOf(", transmap") > 0 || textid.indexOf(", new String[]") > 0) {
                    textid = textid.substring(0, textid.indexOf("\",") + 1);
                    add = true;
                }
            }
            if (!add) continue;
            if (textid.indexOf("{{") >= 0 && textid.indexOf("}}") > 0) {
                String[] ts = StringUtil.getTokens(textid, "{{", "}}");
                for (int i = 0; i < ts.length; ++i) {
                    FindTranslationText.addTextid(set, token, ts[i], filename);
                }
                continue;
            }
            FindTranslationText.addTextid(set, token, textid, filename);
        }
    }

    private static void addTextid(DataSet set, String token, String textid, String filename) {
        int row = set.addRow();
        if (textid.indexOf("\"") == 0) {
            textid = StringUtil.getTokens(textid, "\"", "\"")[0].trim();
        }
        set.setValue(row, "textid", textid.trim());
        set.setValue(row, "texttype", "W");
        if (token.indexOf("Trans.") == 0 || token.indexOf("sapphire.") == 0) {
            set.setValue(row, "cf", "Y");
        }
        set.setValue(row, "source", filename);
    }

    private static ArrayList<String> getTextids(String line, String token) {
        ArrayList<String> a = new ArrayList<String>();
        if ("//translate".equals(token)) {
            String[] textids = StringUtil.getTokens(line, "\"", "\"");
            if (textids.length == 1) {
                a.add("\"" + textids[0] + "\"");
            } else {
                System.out.println("?????Cannot extract textids from:" + line);
            }
        } else {
            block0: while (line.indexOf(token) >= 0) {
                line = line.substring(line.indexOf(token) + token.length());
                boolean isEnd = true;
                for (int i = 0; i < line.length(); ++i) {
                    char c = line.charAt(i);
                    if (c == '(') {
                        isEnd = false;
                        continue;
                    }
                    if (c != ')') continue;
                    if (!isEnd) {
                        isEnd = true;
                        continue;
                    }
                    a.add(line.substring(0, i).trim());
                    continue block0;
                }
            }
            if (a.size() == 0) {
                a.add(line.trim());
            }
        }
        return a;
    }

    private static void findInJasperReport(File reportDir, DataSet dataset) {
        dataset.addColumn("textid", 0);
        dataset.addColumn("texttype", 0);
        dataset.addColumn("cf", 0);
        dataset.addColumn("source", 0);
        try {
            if (reportDir.exists() && reportDir.isDirectory()) {
                File[] files;
                for (File file : files = reportDir.listFiles(new FilenameFilter(){

                    @Override
                    public boolean accept(File dir, String reportName) {
                        return reportName.contains(".jrxml");
                    }
                })) {
                    JasperDesign jasperDesign = JRXmlLoader.load((File)file);
                    JRGroup[] g = jasperDesign.getGroups();
                    for (int i = 0; i < g.length; ++i) {
                        JRSection section = g[i].getGroupHeaderSection();
                        if (section == null || section.getBands() == null) continue;
                        JRBand[] bands = section.getBands();
                        for (int j = 0; j < bands.length; ++j) {
                            JRBand band = bands[j];
                            if (band == null || band.getElements() == null) continue;
                            JRElement[] elements = band.getElements();
                            for (int k = 0; k < elements.length; ++k) {
                                if (elements[k] instanceof JRDesignStaticText) {
                                    String text = ((JRDesignStaticText)elements[k]).getText();
                                    int row = dataset.addRow();
                                    dataset.setValue(row, "textid", text.trim());
                                    dataset.setValue(row, "texttype", "Report");
                                    dataset.setValue(row, "source", file.getName());
                                    continue;
                                }
                                if (!(elements[k] instanceof JRDesignComponentElement) || !((JRDesignComponentElement)elements[k]).getComponentKey().getName().equals("table")) continue;
                                StandardTable component = (StandardTable)((JRDesignComponentElement)elements[k]).getComponent();
                                List baseColumns = component.getColumns();
                                for (int l = 0; l < baseColumns.size(); ++l) {
                                    JRElement[] tableColumnElements;
                                    if (((BaseColumn)baseColumns.get(l)).getColumnHeader() == null || (tableColumnElements = ((BaseColumn)baseColumns.get(l)).getColumnHeader().getElements()).length <= 0 || !(tableColumnElements[0] instanceof JRDesignStaticText)) continue;
                                    String text = ((JRDesignStaticText)tableColumnElements[0]).getText();
                                    int row = dataset.addRow();
                                    dataset.setValue(row, "textid", text.trim());
                                    dataset.setValue(row, "texttype", "Report");
                                    dataset.setValue(row, "source", file.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}

