/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command.diagnostics;

import com.labvantage.sapphire.admin.command.diagnostics.BaseDiagnosticsCommand;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sapphire.SapphireException;

public class CheckCode
extends BaseDiagnosticsCommand {
    private FileOutputStream fos;
    private PrintWriter out;
    private String[] unitsmatch;
    private String[] attrmatch;
    private String[] obsoletematch;
    private boolean showFiles = true;
    private boolean showLines = true;
    private boolean showHits = true;
    private boolean showErrors = true;
    private boolean showWarnings = true;
    private boolean checkUnits = true;
    private boolean checkAttributes = true;
    private boolean checkTop = true;
    private boolean checkNav = true;
    private boolean checkObsolete = true;
    private HashSet<String> javaExt = new HashSet();
    private HashSet<String> webExt = new HashSet();
    private int pxLookahead = 150;
    private String currentLine = "";
    private String lineIssues = "";

    @Override
    public String getCommandName() {
        return "checkcode";
    }

    @Override
    public String getCommandDescription() {
        return "Checks code for upgrade issues";
    }

    @Override
    public String getCommandUsage() {
        return "checkcode -props=[propsfile]";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processCommand() throws SapphireException {
        try {
            int[] stats;
            File web;
            File java;
            this.setDefaultDir();
            String javaParam = this.getCommandParam("java", "");
            String webParam = this.getCommandParam("webapp", "");
            String output = this.getCommandParam("output", "");
            File outputFile = output.length() == 0 ? new File(this.getCommandParam("dir", ""), "checkcode.htm") : new File(output);
            String javaext = this.getCommandParam("javaext", "");
            this.javaExt = new HashSet<String>(Arrays.asList(this.split(javaext.length() > 0 ? javaext : ".java", " ")));
            String webext = this.getCommandParam("webext", "");
            this.webExt = new HashSet<String>(Arrays.asList(this.split(webext.length() > 0 ? webext : ".js .jsp .html .htm .css", " ")));
            this.showFiles = this.getCommandParam("showfiles", "").equalsIgnoreCase("true") || this.getCommandParam("showfiles", "").equalsIgnoreCase("Y");
            this.showLines = this.getCommandParam("showlines", "").equalsIgnoreCase("true") || this.getCommandParam("showlines", "").equalsIgnoreCase("Y");
            this.showHits = this.getCommandParam("showhits", "").equalsIgnoreCase("true") || this.getCommandParam("showhits", "").equalsIgnoreCase("Y");
            this.showErrors = this.getCommandParam("showerrors", "").equalsIgnoreCase("true") || this.getCommandParam("showerrors", "").equalsIgnoreCase("Y");
            this.showWarnings = this.getCommandParam("showwarnings", "").equalsIgnoreCase("true") || this.getCommandParam("showwarnings", "").equalsIgnoreCase("Y");
            this.checkUnits = this.getCommandParam("checkunits", "").equalsIgnoreCase("true") || this.getCommandParam("checkunits", "").equalsIgnoreCase("Y");
            this.unitsmatch = this.split(this.getCommandParam("unitmatch", ""), " ");
            this.checkAttributes = this.getCommandParam("checkattributes", "").equalsIgnoreCase("true") || this.getCommandParam("checkattributes", "").equalsIgnoreCase("Y");
            this.attrmatch = this.split(this.getCommandParam("attributematch", ""), " ");
            this.checkTop = this.getCommandParam("checktop", "").equalsIgnoreCase("true") || this.getCommandParam("checktop", "").equalsIgnoreCase("Y");
            this.checkNav = this.getCommandParam("checknav", "").equalsIgnoreCase("true") || this.getCommandParam("checknav", "").equalsIgnoreCase("Y");
            this.checkObsolete = this.getCommandParam("checkobsolete", "").equalsIgnoreCase("true") || this.getCommandParam("checkobsolete", "").equalsIgnoreCase("Y");
            this.obsoletematch = this.split(this.getCommandParam("obsoletematch", ""), " ");
            this.pxLookahead = Integer.parseInt(this.getCommandParam("pxlookahead", ""));
            try {
                this.fos = new FileOutputStream(outputFile);
                this.out = new PrintWriter(this.fos);
                this.out.println("<!DOCTYPE html>\n<html>\n<head>\n    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n    <meta charset=\"utf-8\">\n    <title>Check Code</title>\n    <style>\n        body{font-size:12px;font-family: Verdana, Arial, Helvetica}\n        table.matchfiles {border: solid lightgray 1px; border-collapse: collapse}\n        tr.filerow {}\n        tr.mouseoverrow:hover {background-color: #EEE;}\n        tr.matchrow {vertical-align: top}\n        td.filename {padding: 3px; font-weight: bold; border: solid lightgray 1px}\n        td.linenum {padding: 3px; white-space: nowrap; border: solid lightgray 1px}\n        td.codematch {padding: 3px; border: solid lightgray 1px; max-width:1200px; overflow-wrap:break-word}\n        td.matchcause {padding: 3px; white-space: nowrap; border: solid lightgray 1px}\n        table.summary {}\n        td.summary {padding: 3px; font-size:14px;font-weight:bold}\n    </style>\n</head>\n<body>\n");
                this.out.println("<h1>Check Code Output</h1>\n");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to create output file '" + outputFile.getAbsolutePath() + "'!");
            }
            File file = java = javaParam != null && javaParam.length() > 0 ? new File(javaParam) : null;
            if (java != null && !java.exists()) {
                throw new SapphireException("Java dir '" + javaParam + "' does not exist or is not a valid directory!");
            }
            File file2 = web = webParam != null && webParam.length() > 0 ? new File(webParam) : null;
            if (web != null && !web.exists()) {
                throw new SapphireException("Web app dir '" + webParam + "' does not exist or is not a valid directory!");
            }
            if (java != null) {
                this.log("Processing JAVA files: " + java.getAbsolutePath());
                this.log("- looking in files with extensions: " + javaext);
                this.log("\n");
                this.out.println("<h2>Java Dir: " + java.getAbsolutePath() + "</h2>\n");
                this.out.println("<table class=\"matchfiles\">\n");
                stats = this.traverseFiles(java, "J", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                this.log(stats[0] + "\tFiles checked");
                this.log(stats[10] + "\tToken hits");
                this.log(stats[1] + "\tFiles found with upgrade issues");
                this.log(stats[2] + "\tLines found with upgrade issues");
                this.log(stats[3] + "\tErrors found");
                this.log(stats[4] + "\t- missing units");
                this.log(stats[5] + "\t- top references");
                this.log(stats[6] + "\t- obsolete methods");
                this.log(stats[7] + "\tWarnings found");
                this.log(stats[8] + "\t- missing attribure quotes");
                this.log(stats[9] + "\t- window navigation");
                this.out.println("</table>\n");
                this.out.println("<table class=\"summary\">\n");
                this.out.println("<tr><td class=\"summary\" colspan=\"10\">Summary</td></tr>");
                this.out.println("<tr><td class=\"summary\">Files checked:</td><td class=\"summary\">" + stats[0] + "</td><td class=\"summary\"> - number of files checked for issues</td</tr>");
                this.out.println("<tr><td class=\"summary\">Matching files:</td><td class=\"summary\">" + stats[1] + "</td><td class=\"summary\"> - number of files with upgrade issues</td</tr>");
                this.out.println("<tr><td class=\"summary\">Line matches:</td><td class=\"summary\">" + stats[2] + "</td><td class=\"summary\"> - number of lines with issues</td</tr>");
                this.out.println("<tr><td class=\"summary\">Errors:</td><td class=\"summary\">" + stats[3] + "</td><td class=\"summary\"> - number of errors found</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[4] + "</td><td class=\"summary\"> - missing units</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[5] + "</td><td class=\"summary\"> - top references</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[6] + "</td><td class=\"summary\"> - obsolete methods</td</tr>");
                this.out.println("<tr><td class=\"summary\">Warnings:</td><td class=\"summary\">" + stats[7] + "</td><td class=\"summary\"> - number of warnings found</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[8] + "</td><td class=\"summary\"> - missing attribute quotes</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[9] + "</td><td class=\"summary\"> - window navigation</td</tr>");
                this.out.println("<tr><td class=\"summary\">Hits:</td><td class=\"summary\">" + stats[10] + "</td><td class=\"summary\"> - number of hits found</td</tr>");
                this.out.println("</table>");
                this.log("\n");
            }
            if (web != null) {
                this.log("Processing WEB files: " + web.getAbsolutePath());
                this.log("- looking in files with extensions: " + webext);
                this.log("\n");
                this.out.println("<h2>Web App Dir: " + web.getAbsolutePath() + "</h2>\n");
                this.out.println("<table class=\"matchfiles\">\n");
                stats = this.traverseFiles(web, "W", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                this.log(stats[0] + "\tFiles checked");
                this.log(stats[10] + "\tToken hits");
                this.log(stats[1] + "\tFiles found with upgrade issues");
                this.log(stats[2] + "\tLines found with upgrade issues");
                this.log(stats[3] + "\tErrors found");
                this.log(stats[4] + "\t- missing units");
                this.log(stats[5] + "\t- top references");
                this.log(stats[6] + "\t- obsolete methods");
                this.log(stats[7] + "\tWarnings found");
                this.log(stats[8] + "\t- missing attribure quotes");
                this.log(stats[9] + "\t- window navigation");
                this.out.println("</table>\n");
                this.out.println("<table class=\"summary\">\n");
                this.out.println("<tr><td class=\"summary\" colspan=\"10\">Summary</td></tr>");
                this.out.println("<tr><td class=\"summary\">Files checked:</td><td class=\"summary\">" + stats[0] + "</td><td class=\"summary\"> - number of files checked for issues</td</tr>");
                this.out.println("<tr><td class=\"summary\">Matching files:</td><td class=\"summary\">" + stats[1] + "</td><td class=\"summary\"> - number of files with issues</td</tr>");
                this.out.println("<tr><td class=\"summary\">Line matches:</td><td class=\"summary\">" + stats[2] + "</td><td class=\"summary\"> - number of lines with issues</td</tr>");
                this.out.println("<tr><td class=\"summary\">Errors:</td><td class=\"summary\">" + stats[3] + "</td><td class=\"summary\"> - number of errors found</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[4] + "</td><td class=\"summary\"> - missing units</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[5] + "</td><td class=\"summary\"> - top references</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[6] + "</td><td class=\"summary\"> - obsolete methods</td</tr>");
                this.out.println("<tr><td class=\"summary\">Warnings:</td><td class=\"summary\">" + stats[7] + "</td><td class=\"summary\"> - number of warnings found</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[8] + "</td><td class=\"summary\"> - missing attribute quotes</td</tr>");
                this.out.println("<tr><td class=\"summary\">&nbsp;</td><td class=\"summary\">" + stats[9] + "</td><td class=\"summary\"> - window navigation</td</tr>");
                this.out.println("<tr><td class=\"summary\">Hits:</td><td class=\"summary\">" + stats[10] + "</td><td class=\"summary\"> - number of hits found</td</tr>");
                this.out.println("</table>");
            }
            this.log("\nOutput: " + outputFile.getAbsolutePath());
        }
        finally {
            try {
                if (this.out != null) {
                    this.out.println("</body></html>");
                    this.out.close();
                }
                if (this.fos != null) {
                    this.fos.close();
                }
            }
            catch (Exception exception) {}
        }
    }

    private int[] traverseFiles(File file, String type, int basecountfile, int basecountmatchfiles, int baselinematches, int baseerrors, int baseerrors_missingunits, int baseerrors_topreference, int baseerrors_obsolete, int basewarnings, int basewarnings_missingquotes, int basewarnings_navigation, int basehits, int basefailures) {
        if (file.isDirectory()) {
            File[] dirFiles;
            int countfiles = basecountfile;
            int countmatchfiles = basecountmatchfiles;
            int linematches = baselinematches;
            int errors = baseerrors;
            int errors_missingunits = baseerrors_missingunits;
            int errors_topreference = baseerrors_topreference;
            int errors_obsolete = baseerrors_obsolete;
            int warnings = basewarnings;
            int warnings_missingquotes = basewarnings_missingquotes;
            int warnings_navigation = basewarnings_navigation;
            int hits = basehits;
            int failures = basefailures;
            for (File dirFile : dirFiles = file.listFiles()) {
                int[] stats = this.traverseFiles(dirFile, type, countfiles, countmatchfiles, linematches, errors, errors_missingunits, errors_topreference, errors_obsolete, warnings, warnings_missingquotes, warnings_navigation, hits, failures);
                if (dirFile.isFile()) {
                    countfiles += stats[0];
                    countmatchfiles += stats[1];
                    linematches += stats[2];
                    errors += stats[3];
                    errors_missingunits += stats[4];
                    errors_topreference += stats[5];
                    errors_obsolete += stats[6];
                    warnings += stats[7];
                    warnings_missingquotes += stats[8];
                    warnings_navigation += stats[9];
                    hits += stats[10];
                    failures += stats[11];
                    continue;
                }
                countfiles = stats[0];
                countmatchfiles = stats[1];
                linematches = stats[2];
                errors = stats[3];
                errors_missingunits = stats[4];
                errors_topreference = stats[5];
                errors_obsolete = stats[6];
                warnings = stats[7];
                warnings_missingquotes = stats[8];
                warnings_navigation = stats[9];
                hits = stats[10];
                failures = stats[11];
            }
            return new int[]{countfiles, countmatchfiles, linematches, errors, errors_missingunits, errors_topreference, errors_obsolete, warnings, warnings_missingquotes, warnings_navigation, hits, failures};
        }
        if (!(file.getAbsolutePath().toLowerCase().contains("cvs" + File.separator + "base") || file.getAbsolutePath().toLowerCase().contains("web-core" + File.separator + "gwt" + File.separator) || file.getAbsolutePath().toLowerCase().contains("web-core" + File.separator + "jquery" + File.separator) || file.getAbsolutePath().toLowerCase().contains("web-core" + File.separator + "extscripts" + File.separator))) {
            String ext;
            int pos = file.getName().lastIndexOf(".");
            String string = ext = pos >= 0 ? file.getName().substring(pos) : "";
            if (ext.length() > 0 && (type.equals("J") && this.javaExt.contains(ext) || type.equals("W") && this.webExt.contains(ext))) {
                int[] stats = this.processFile(file, type);
                if (this.isVerbose()) {
                    this.log(" - checking - " + file.getAbsolutePath());
                }
                return new int[]{1, stats[1] > 0 ? 1 : 0, stats[0], stats[1], stats[2], stats[3], stats[4], stats[5], stats[6], stats[7], stats[8], stats[9]};
            }
            if (this.isVerbose()) {
                this.log(" - ignoring - " + file.getAbsolutePath());
            }
            return new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        }
        if (this.isVerbose()) {
            this.log(" - ignoring - " + file.getAbsolutePath());
        }
        return new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int[] processFile(File file, String type) {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        int currentLineNum = 0;
        int linematches = 0;
        int errors = 0;
        int errors_missingunits = 0;
        int errors_topreference = 0;
        int errors_obsolete = 0;
        int warnings = 0;
        int warnings_missingquotes = 0;
        int warnings_navigation = 0;
        int hits = 0;
        StringBuffer fileOutput = new StringBuffer();
        try {
            int i;
            fis = new FileInputStream(file);
            isr = new InputStreamReader((InputStream)fis, "UTF-8");
            in = new BufferedReader(isr);
            String line = "";
            int lineNum = 0;
            ArrayList<Integer> matchLineNums = new ArrayList<Integer>();
            ArrayList<String> matchLines = new ArrayList<String>();
            block11: while ((line = in.readLine()) != null) {
                ++lineNum;
                if ((line = line.trim()).startsWith("//") || line.startsWith("*") || line.startsWith("/*") || line.startsWith("*/")) continue;
                if (this.checkUnits) {
                    for (i = 0; i < this.unitsmatch.length; ++i) {
                        if (!line.matches(".+\\b" + this.unitsmatch[i] + "\\b.+") || line.toLowerCase().startsWith("var " + this.unitsmatch[i])) continue;
                        matchLineNums.add(lineNum);
                        matchLines.add(line);
                        break;
                    }
                }
                if (this.checkAttributes && (matchLineNums.size() == 0 || (Integer)matchLineNums.get(matchLineNums.size() - 1) != lineNum)) {
                    for (i = 0; i < this.attrmatch.length; ++i) {
                        if (!line.matches(".+\\b" + this.attrmatch[i] + "\\b.+") || line.toLowerCase().startsWith("var " + this.attrmatch[i].toLowerCase())) continue;
                        matchLineNums.add(lineNum);
                        matchLines.add(line);
                        break;
                    }
                }
                if (this.checkTop && (matchLineNums.size() == 0 || (Integer)matchLineNums.get(matchLineNums.size() - 1) != lineNum) && line.toLowerCase().contains("top.")) {
                    matchLineNums.add(lineNum);
                    matchLines.add(line);
                }
                if (this.checkNav && (matchLineNums.size() == 0 || (Integer)matchLineNums.get(matchLineNums.size() - 1) != lineNum) && (line.toLowerCase().contains("href=\"rc?command=") || line.toLowerCase().contains("window.navigate") || line.toLowerCase().contains("window.location") || line.toLowerCase().contains("window.location.href"))) {
                    matchLineNums.add(lineNum);
                    matchLines.add(line);
                }
                if (!this.checkObsolete || matchLineNums.size() != 0 && (Integer)matchLineNums.get(matchLineNums.size() - 1) == lineNum) continue;
                for (i = 0; i < this.obsoletematch.length; ++i) {
                    if (!line.toLowerCase().contains(this.obsoletematch[i].toLowerCase())) continue;
                    matchLineNums.add(lineNum);
                    matchLines.add(line);
                    continue block11;
                }
            }
            if (matchLines.size() > 0) {
                for (i = 0; i < matchLines.size(); ++i) {
                    Matcher m2;
                    Pattern p2;
                    Matcher m1;
                    int j;
                    this.currentLine = (String)matchLines.get(i);
                    currentLineNum = (Integer)matchLineNums.get(i);
                    this.lineIssues = "";
                    if (this.checkUnits) {
                        for (j = 0; j < this.unitsmatch.length; ++j) {
                            int pos;
                            Pattern p3;
                            Matcher m3;
                            boolean missing = false;
                            Pattern p1 = Pattern.compile("((\\s|;|\")" + this.unitsmatch[j] + "\\s*:)\\s*[-]?\\d+((?!(px|%|em|mm|cm)).)*$");
                            Matcher m12 = p1.matcher(this.currentLine);
                            Pattern p22 = Pattern.compile("((;|\"|\\s*)\\s*" + this.unitsmatch[j] + "\\s*:)\\s*[-]?\\d+(;|\"|,)");
                            Matcher m22 = p22.matcher(this.currentLine);
                            Pattern p8 = Pattern.compile("\\w*\\." + this.unitsmatch[j] + "\\s*=\\s*[-]?\\d+\\s*;");
                            Matcher m8 = p8.matcher(this.currentLine);
                            if (!(!m12.find() && !m22.find() && !m8.find() || this.currentLine.contains(this.unitsmatch[j] + ":0") || this.currentLine.contains(this.unitsmatch[j] + ": 0") || this.currentLine.contains(this.unitsmatch[j] + " : 0") || this.currentLine.contains(this.unitsmatch[j] + " :0") || this.currentLine.contains(this.unitsmatch[j] + "=0") || this.currentLine.contains(this.unitsmatch[j] + "= 0") || this.currentLine.contains(this.unitsmatch[j] + " = 0") || this.currentLine.contains(this.unitsmatch[j] + " =0") || this.currentLine.contains(this.unitsmatch[j] + ":none") || this.currentLine.contains(this.unitsmatch[j] + ": none") || this.currentLine.contains(this.unitsmatch[j] + " : none") || this.currentLine.contains(this.unitsmatch[j] + " :none") || this.currentLine.contains("\"" + this.unitsmatch[j] + "\""))) {
                                errors = this.showErrors(this.unitsmatch[j], "Missing units", errors);
                                ++errors_missingunits;
                                missing = true;
                            }
                            if ((m3 = (p3 = Pattern.compile("((;|\")\\s*" + this.unitsmatch[j] + "\\s*:)\"")).matcher(this.currentLine)).find() && (pos = m3.start()) >= 0) {
                                String lookaheadmatch = this.currentLine.substring(pos, pos + this.pxLookahead < this.currentLine.length() - 1 ? pos + this.pxLookahead : this.currentLine.length() - 1);
                                Pattern p9 = Pattern.compile("\"(\\s*|\\d*)px");
                                Matcher m9 = p9.matcher(lookaheadmatch);
                                if (!m9.find()) {
                                    errors = this.showErrors(this.unitsmatch[j], "Missing units", errors);
                                    ++errors_missingunits;
                                    missing = true;
                                }
                            }
                            Pattern p9 = Pattern.compile("\\.style\\." + this.unitsmatch[j] + "\\s*=\\s*(\\\\|\\s*)\"[-]?\\d+(\\\\|\\s*)\"");
                            Matcher m9 = p9.matcher(this.currentLine);
                            Pattern p10 = Pattern.compile("\\.style\\." + this.unitsmatch[j] + "\\s*=\\s*(\\\\|\\s*)'[-]?\\d+(\\\\|\\s*)'");
                            Matcher m10 = p10.matcher(this.currentLine);
                            if (!(!m9.find() && !m10.find() || this.currentLine.contains(".style." + this.unitsmatch[j] + "=\"0\"") || this.currentLine.contains(".style." + this.unitsmatch[j] + " =\"0\"") || this.currentLine.contains(".style." + this.unitsmatch[j] + "= \"0\"") || this.currentLine.contains(".style." + this.unitsmatch[j] + " = \"0\"") || this.currentLine.contains(".style." + this.unitsmatch[j] + "='0'") || this.currentLine.contains(".style." + this.unitsmatch[j] + " ='0'") || this.currentLine.contains(".style." + this.unitsmatch[j] + "= '0'") || this.currentLine.contains(".style." + this.unitsmatch[j] + " = '0'"))) {
                                errors = this.showErrors(this.unitsmatch[j], "Missing units", errors);
                                ++errors_missingunits;
                                missing = true;
                            }
                            if (missing) continue;
                            hits = this.showHits(this.unitsmatch[j], hits);
                        }
                    }
                    if (this.checkAttributes) {
                        for (j = 0; j < this.attrmatch.length; ++j) {
                            Pattern p1 = Pattern.compile("\\s+" + this.attrmatch[j] + "\\s*=\\s*[#]?(\\d+|\\w+)(px|%|em|mm|cm|\\s*|>)");
                            m1 = p1.matcher(this.currentLine);
                            if (m1.find() && !this.currentLine.endsWith(";")) {
                                warnings = this.showWarnings(this.attrmatch[j], "Missing attribute quotes", warnings);
                                ++warnings_missingquotes;
                                continue;
                            }
                            hits = this.showHits(this.attrmatch[j], hits);
                        }
                    }
                    if (this.checkTop) {
                        Pattern p1 = Pattern.compile("top\\.(document\\.|\\s*)\\w+\\s*\\(");
                        Matcher m13 = p1.matcher(this.currentLine);
                        p2 = Pattern.compile("\\s*target\\s*=\\s*(\\\\|\\s*)\"_top(\\\\|\\s*)\"");
                        m2 = p2.matcher(this.currentLine);
                        if (m13.find() || m2.find() || this.currentLine.contains("top.maint_iframe")) {
                            if (!(this.currentLine.toLowerCase().contains("top.sapphire") || this.currentLine.toLowerCase().contains("top.showmessage") || this.currentLine.toLowerCase().contains("top.showprocessingdiv") || this.currentLine.toLowerCase().contains("top.hideprocessingdiv"))) {
                                errors = this.showErrors("top.", "Top method reference", errors);
                                ++errors_topreference;
                            } else {
                                hits = this.showHits("top.", hits);
                            }
                        } else {
                            hits = this.showHits("top.", hits);
                        }
                    }
                    if (this.checkNav) {
                        Pattern p1 = Pattern.compile("window\\.navigate\\s*\\(\\s*(\\\"|\"|'|\\')rc\\?command=(page|file|history|logoff)");
                        Matcher m14 = p1.matcher(this.currentLine);
                        if (m14.find()) {
                            warnings = this.showWarnings("window.navigate", "window.navigation navigation", warnings);
                            ++warnings_navigation;
                        } else {
                            hits = this.showHits("window.navigate", hits);
                        }
                        p2 = Pattern.compile("window\\.location(\\.href)?\\s*=\\s*(\\\"|\"|'|\\')rc\\?command=(page|file|history|logoff)");
                        m2 = p2.matcher(this.currentLine);
                        if (m2.find()) {
                            warnings = this.showWarnings("window.location", "window.location navigation", warnings);
                            ++warnings_navigation;
                        } else {
                            hits = this.showHits("window.location", hits);
                        }
                        Pattern p3 = Pattern.compile("href\\s*=\\s*(\\\"|\")rc\\?command=(page|file|history|logoff)");
                        Matcher m3 = p3.matcher(this.currentLine);
                        if (m3.find()) {
                            warnings = this.showWarnings("href=", "href navigation", errors);
                            ++warnings_navigation;
                        } else {
                            hits = this.showHits("href=", hits);
                        }
                    }
                    if (this.checkObsolete) {
                        for (int j2 = 0; j2 < this.obsoletematch.length; ++j2) {
                            Pattern p1 = Pattern.compile("(\\s*|\\.)" + this.obsoletematch[j2] + "\\s*\\(");
                            m1 = p1.matcher(this.currentLine);
                            if (m1.find()) {
                                if (!this.currentLine.contains("sapphire.events." + this.obsoletematch[j2]) && !this.currentLine.contains("sdiNotes.attachEvent")) {
                                    errors = this.showErrors(this.obsoletematch[j2], "Obsolete methods", errors);
                                    ++errors_obsolete;
                                    continue;
                                }
                                hits = this.showHits(this.obsoletematch[j2], hits);
                                continue;
                            }
                            hits = this.showHits(this.obsoletematch[j2], hits);
                        }
                    }
                    if (!this.currentLine.contains("__starterror__") && !this.currentLine.contains("__startwarn__") && !this.currentLine.contains("__starthit__")) continue;
                    this.currentLine = this.convertChars(this.currentLine);
                    this.currentLine = this.replaceAll(this.currentLine, "__starterror__", "<font color=\"red\"><b>", true);
                    this.currentLine = this.replaceAll(this.currentLine, "__enderror__", "</b></font>", true);
                    this.currentLine = this.replaceAll(this.currentLine, "__startwarn__", "<font color=\"orange\"><b>", true);
                    this.currentLine = this.replaceAll(this.currentLine, "__endwarn__", "</b></font>", true);
                    this.currentLine = this.replaceAll(this.currentLine, "__starthit__", "<font color=\"green\"><b>", true);
                    this.currentLine = this.replaceAll(this.currentLine, "__endhit__", "</b></font>", true);
                    fileOutput.append("<tr class=\"matchrow mouseoverrow\"><td class=\"linenum\">Line: ").append(matchLineNums.get(i)).append("</td><td class=\"codematch\">").append(this.currentLine).append("</td><td class=\"matchcause\">").append(this.lineIssues).append("</td></tr>");
                    ++linematches;
                }
                if (fileOutput.length() > 0 && this.showFiles) {
                    this.out.println("<tr class=\"filerow\"><td colspan=\"10\" class=\"filename\">File: " + file.getAbsolutePath() + " (" + (this.showHits ? "<font color=\"green\"><b>" + hits + " occurrences</b></font>, " : "") + "<font color=\"red\"><b>" + errors + " errors</b></font>" + (this.showWarnings ? ", <font color=\"orange\"><b>" + warnings + " warnings</b></font>" : "") + " in " + linematches + " lines)</td></tr>");
                    if (this.showLines) {
                        this.out.println(fileOutput);
                    }
                }
            }
            int[] nArray = new int[]{linematches, errors, errors_missingunits, errors_topreference, errors_obsolete, warnings, warnings_missingquotes, warnings_navigation, hits, 0};
            return nArray;
        }
        catch (Exception e) {
            this.out.println("<tr class=\"filerow\"><td colspan=\"10\" class=\"filename\">File: " + file.getAbsolutePath() + "</td></tr>");
            this.out.println("<tr class=\"matchrow\"><td class=\"linenum\">Line: " + currentLineNum + "</td><td class=\"codematch\">" + this.currentLine + "</td></tr>");
            this.log("   FAILED processing line " + currentLineNum + " of file " + file.getAbsolutePath() + ". Error: " + e.getMessage());
            int[] nArray = new int[]{linematches, errors, errors_missingunits, errors_topreference, errors_obsolete, warnings, warnings_missingquotes, warnings_navigation, hits, 1};
            return nArray;
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
            catch (Exception exception) {}
        }
    }

    private int showErrors(String matchtext, String error, int errors) {
        if (this.showErrors) {
            this.currentLine = this.replaceAll(this.currentLine, matchtext, "__starterror__" + matchtext + "__enderror__", false);
            if (!this.lineIssues.contains(error)) {
                this.lineIssues = this.lineIssues + "<font color=\"red\"><b>" + error + "</b></font><br/>";
            }
        }
        return ++errors;
    }

    private int showWarnings(String matchtext, String warning, int warnings) {
        if (this.showWarnings) {
            this.currentLine = this.replaceAll(this.currentLine, matchtext, "__startwarn__" + matchtext + "__endwarn__", false);
            if (!this.lineIssues.contains(warning)) {
                this.lineIssues = this.lineIssues + "<font color=\"orange\"><b>" + warning + "</b></font><br/>";
            }
        }
        return ++warnings;
    }

    private int showHits(String matchtext, int hits) {
        if (this.currentLine.contains(matchtext)) {
            if (this.showHits) {
                this.currentLine = this.replaceAll(this.currentLine, matchtext, "__starthit__" + matchtext + "__endhit__", false);
                if (!this.lineIssues.contains("Hit")) {
                    this.lineIssues = this.lineIssues + "<font color=\"green\"><b>Hit</b></font><br/>";
                }
            }
            ++hits;
        }
        return hits;
    }

    private String[] split(String input, String delimeter) {
        ArrayList<String> tokenlist = new ArrayList<String>();
        if (input != null) {
            if (delimeter != null) {
                int pos = input.indexOf(delimeter);
                if (pos == -1) {
                    tokenlist.add(input);
                } else {
                    int offset = 0;
                    while (pos > -1) {
                        tokenlist.add(input.substring(offset, pos));
                        offset = pos + delimeter.length();
                        pos = input.indexOf(delimeter, offset);
                    }
                    tokenlist.add(input.substring(offset, input.length()));
                }
            } else {
                tokenlist.add(input);
            }
        }
        String[] tokens = new String[tokenlist.size()];
        for (int i = 0; i < tokenlist.size(); ++i) {
            tokens[i] = (String)tokenlist.get(i);
        }
        return tokens;
    }

    public String replaceAll(String inputString, String oldString, String newString, boolean standardBehavior) {
        if (inputString == null || inputString.length() == 0 || oldString == null || oldString.length() == 0 || newString == null) {
            return inputString;
        }
        int oldStringLength = oldString.length();
        String originalInputString = inputString;
        String originalOldString = oldString;
        if (!standardBehavior) {
            inputString = inputString.toUpperCase();
            oldString = oldString.toUpperCase();
        }
        StringBuffer outputString = new StringBuffer(inputString.length());
        int lastpos = 0;
        int pos = inputString.indexOf(oldString);
        while (pos >= 0) {
            outputString.append(originalInputString.substring(lastpos, pos));
            if (standardBehavior) {
                outputString.append(newString);
            } else if (pos <= 2) {
                outputString.append(newString);
            } else if (!(Character.isLetter(inputString.charAt(pos - 1)) || pos + oldString.length() != inputString.length() && Character.isLetter(inputString.charAt(pos + oldString.length())) || inputString.charAt(pos - 1) == '\"' && pos + oldString.length() != inputString.length() && inputString.charAt(pos + oldString.length()) == '\"' || inputString.substring(pos - 2, pos).equals("__"))) {
                outputString.append(newString);
            } else {
                outputString.append(originalOldString);
            }
            lastpos = pos + oldStringLength;
            pos = inputString.indexOf(oldString, lastpos);
        }
        outputString.append(originalInputString.substring(lastpos));
        return outputString.toString();
    }

    private String convertChars(String text) {
        StringBuffer output = new StringBuffer(text.length());
        block7: for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            switch (c) {
                case '&': {
                    output.append("&amp;");
                    continue block7;
                }
                case '<': {
                    output.append("&lt;");
                    continue block7;
                }
                case '>': {
                    output.append("&gt;");
                    continue block7;
                }
                case '\"': {
                    output.append("&quot;");
                    continue block7;
                }
                case '\'': {
                    output.append("&#039;");
                    continue block7;
                }
                default: {
                    output.append(c);
                }
            }
        }
        return output.toString();
    }
}

