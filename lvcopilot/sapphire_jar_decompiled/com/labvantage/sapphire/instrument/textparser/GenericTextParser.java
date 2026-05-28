/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Binding
 *  groovy.lang.GroovyShell
 *  groovy.lang.Script
 *  org.codehaus.groovy.control.CompilerConfiguration
 */
package com.labvantage.sapphire.instrument.textparser;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.instrument.textparser.ParsingScriptPolisher;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.codehaus.groovy.control.CompilerConfiguration;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GenericTextParser {
    static GroovyShell gs;
    public static final String TEXT_BIND_VAR = "s";
    private static Cache scriptCache;

    public static HashMap parse(String text, String parsingRule) throws Exception {
        return GenericTextParser.toResultMap(GenericTextParser.parseDataSet(text, parsingRule), false);
    }

    public static HashMap parse(String text, String parsingRule, boolean retainPos) throws Exception {
        return GenericTextParser.toResultMap(GenericTextParser.parseDataSet(text, parsingRule), retainPos);
    }

    public static DataSet parseDataSet(String text, String parsingRule) throws Exception {
        return GenericTextParser.parseDataSet(text, parsingRule, new HashMap());
    }

    private static DataSet parseDataSet(String text, String parsingRule, HashMap bindMap) throws Exception {
        boolean isGroovy;
        boolean bl = isGroovy = parsingRule.indexOf("$G:") == 0;
        if (isGroovy) {
            parsingRule = parsingRule.substring(3);
            return GenericTextParser.parseGroovyDataSet(text, parsingRule, bindMap);
        }
        PropertyList pl = new PropertyList();
        pl.setPropertyList(parsingRule);
        return GenericTextParser.parseText(text, pl, bindMap, 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static DataSet parseText(String text, PropertyList pl, HashMap m, int startindex) throws Exception {
        String parsingrule;
        Object fieldid;
        DataSet resultDataSet = null;
        m.put(TEXT_BIND_VAR, text);
        Binding b = new Binding((Map)m);
        PropertyList splitRule = pl.getPropertyList("splitrule");
        String decimalSeparator = ".";
        boolean repeat = false;
        String splitby = "";
        if (splitRule != null) {
            splitby = GenericTextParser.unescapeASCIIChars(splitRule.getProperty("splitby"), "<", ">");
            decimalSeparator = splitRule.getProperty("decimalseparator");
            if ("line".equals(splitby)) {
                splitby = text.indexOf("\r\n") > 0 ? "\r\n" : (text.indexOf("\n") > 0 ? "\n" : "\r");
            }
            repeat = "Y".equals(splitRule.getProperty("repeat"));
        }
        PropertyListCollection fields = pl.getCollection("fields");
        if (repeat) {
            String[] tokens = StringUtil.split(text, splitby);
            HashMap bindMap = new HashMap();
            DataSet resultDs = null;
            PropertyList repeatProcessPL = new PropertyList();
            repeatProcessPL.setProperty("fields", fields);
            for (int t = 0; t < tokens.length; ++t) {
                String tkn = tokens[t];
                if (t > 0) {
                    startindex += tokens[t - 1].length() + splitby.length();
                }
                resultDs = GenericTextParser.addParsingResult(GenericTextParser.parseText(tkn, repeatProcessPL, bindMap, startindex), resultDs);
            }
            return resultDs;
        }
        int rCount = fields.size();
        StringBuffer parsingScript = new StringBuffer();
        parsingScript.append("def results = new String [" + rCount + "][4];");
        String exp = "setInput(s);" + (splitby.length() > 0 ? "splitby('" + splitby.replaceAll("'", "\\\\'") + "');" : "");
        parsingScript.append(exp);
        if (!".".equals(decimalSeparator)) {
            parsingScript.append("setDecimalSeparator('" + decimalSeparator + "');");
        } else {
            parsingScript.append("setDecimalSeparator('.');");
        }
        HashMap<String, PropertyList> childPLMap = new HashMap<String, PropertyList>();
        for (int r = 0; r < fields.size(); ++r) {
            PropertyList childpl;
            PropertyList field = fields.getPropertyList(r);
            fieldid = field.getProperty("fieldid");
            parsingrule = field.getProperty("parsingrule");
            if (parsingrule.length() == 0) {
                String startrule = field.getProperty("startrule");
                String endrule = field.getProperty("endrule");
                String extractrule = field.getProperty("extractrule");
                parsingrule = startrule + " " + endrule + " " + extractrule;
            }
            try {
                parsingScript.append(GenericTextParser.parseSingleResultScript(r, (String)fieldid, parsingrule, b, startindex));
            }
            catch (Exception e) {
                throw new Exception("Unable to process rule " + r + " name:" + (String)fieldid + " value:" + parsingrule + " Error:" + e.getMessage());
            }
            if (field.getPropertyList("childfield") == null || (childpl = field.getPropertyList("childfield")).getCollection("fields") == null) continue;
            childPLMap.put(HttpUtil.encodeURIComponent((String)fieldid, "UTF-8"), childpl);
        }
        parsingScript.append("return results;");
        String scriptStr = parsingScript.toString();
        Script valueS = null;
        if (scriptCache.get(scriptStr) != null) {
            valueS = (Script)scriptCache.get(scriptStr);
        } else {
            if (scriptCache.getSize() == 1000) {
                fieldid = scriptCache;
                synchronized (fieldid) {
                    scriptCache.clear();
                    gs.resetLoadedClasses();
                }
            }
            valueS = gs.parse(scriptStr);
            scriptCache.put(scriptStr, valueS);
        }
        String[][] results = null;
        parsingrule = valueS;
        synchronized (parsingrule) {
            valueS.setBinding(b);
            results = (String[][])valueS.run();
        }
        for (int i = 0; i < results.length; ++i) {
            resultDataSet = GenericTextParser.addParsingResult(results[i], resultDataSet);
            if (!childPLMap.containsKey(results[i][0])) continue;
            PropertyList childPL = (PropertyList)childPLMap.get(results[i][0]);
            String parentText = results[i][2];
            int startPos = Integer.parseInt(results[i][3]);
            startPos = startPos > 0 ? startPos : 0;
            GenericTextParser.addParsingResult(GenericTextParser.parseText(parentText, childPL, new HashMap(), startPos), resultDataSet);
        }
        return resultDataSet;
    }

    private static String parseSingleResultScript(int rCount, String name, String valueRule, Binding b, int startIndexInText) throws Exception {
        StringBuffer singleScript = new StringBuffer();
        String exp = GenericTextParser.unescapeASCIIChars(valueRule.trim(), "<", ">");
        exp = exp.indexOf("$G:") == 0 ? exp.substring(3) : new ParsingScriptPolisher().polish(exp, b);
        if (name.indexOf("[") == 0) {
            singleScript.append("nameToken=" + new ParsingScriptPolisher().polish(name.substring(1, name.length() - 1), b) + ";");
            singleScript.append("results[" + rCount + "][0]=nameToken.toString();\n");
            singleScript.append("results[" + rCount + "][1]=(nameToken instanceof com.labvantage.sapphire.instrument.textparser.TextToken ? nameToken.getAbsolutePos() + " + startIndexInText + " : -1);\n");
        } else {
            singleScript.append("results[" + rCount + "][0]='" + HttpUtil.encodeURIComponent(name, "UTF-8") + "';\n");
            singleScript.append("results[" + rCount + "][1]='-1';\n");
        }
        String resultToken = "resultToken" + rCount;
        singleScript.append(resultToken + "=" + exp + ";\n");
        singleScript.append("results[" + rCount + "][2]=" + resultToken + ".toString();\n");
        singleScript.append("results[" + rCount + "][3]=(" + resultToken + " instanceof com.labvantage.sapphire.instrument.textparser.TextToken ? " + resultToken + ".getAbsolutePos() + " + startIndexInText + " : -1 );\n");
        singleScript.append("Value" + (rCount + 1) + "=(" + resultToken + ");\n");
        return singleScript.toString();
    }

    private static DataSet toResultDataSet(HashMap newmap) {
        Set keySet = newmap.keySet();
        Iterator itr = keySet.iterator();
        DataSet ds = null;
        while (itr.hasNext()) {
            String name = (String)itr.next();
            String value = (String)newmap.get(name);
            ds = GenericTextParser.addParsingResult(new String[]{name, "-1", value, "-1"}, ds);
        }
        return ds;
    }

    private static HashMap toResultMap(DataSet ds, boolean retainPos) {
        HashMap<String, String> original = new HashMap<String, String>();
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String name = ds.getValue(i, "name");
            if (original.get(name) == null) {
                original.put(name, ds.getValue(i, "value"));
                if (!retainPos) continue;
                original.put(name + "_name_tokenStartPos", ds.getValue(i, "namepos"));
                original.put(name + "_tokenStartPos", ds.getValue(i, "valuepos"));
                continue;
            }
            original.put(name, original.get(name) + ";" + ds.getValue(i, "value"));
            if (!retainPos) continue;
            original.put(name + "_name_tokenStartPos", original.get(name + "_name_tokenStartPos") + ";" + ds.getValue(i, "namepos"));
            original.put(name + "_tokenStartPos", original.get(name + "_tokenStartPos") + ";" + ds.getValue(i, "valuepos"));
        }
        return original;
    }

    public static DataSet addParsingResult(String[] namevalues, DataSet dataset) {
        DataSet ds = dataset;
        if (ds == null) {
            ds = new DataSet();
            ds.addColumn("name", 0);
            ds.addColumn("namepos", 0);
            ds.addColumn("value", 0);
            ds.addColumn("valuepos", 0);
        }
        int row = ds.addRow();
        try {
            ds.setValue(row, "name", HttpUtil.decodeURIComponent(namevalues[0], "UTF-8"));
        }
        catch (Exception e) {
            ds.setValue(row, "name", namevalues[0]);
        }
        ds.setValue(row, "namepos", namevalues[1]);
        ds.setValue(row, "value", namevalues[2]);
        ds.setValue(row, "valuepos", namevalues[3]);
        return ds;
    }

    public static String unescapeASCIIChars(String input, String startdelimiter, String enddelimiter) {
        String[] specialChars = StringUtil.getTokens(input, startdelimiter, enddelimiter);
        for (int i = 0; i < specialChars.length; ++i) {
            int c = Integer.parseInt(specialChars[i]);
            input = StringUtil.replaceAll(input, startdelimiter + specialChars[i] + enddelimiter, (char)c + "");
        }
        return input;
    }

    private static DataSet addParsingResult(DataSet newresults, DataSet dataset) {
        DataSet ds = dataset;
        if (ds == null) {
            ds = newresults;
        } else {
            for (int i = 0; i < newresults.getRowCount(); ++i) {
                int row = ds.addRow();
                ds.setValue(row, "name", newresults.getValue(i, "name"));
                ds.setValue(row, "namepos", newresults.getValue(i, "namepos"));
                ds.setValue(row, "value", newresults.getValue(i, "value"));
                ds.setValue(row, "valuepos", newresults.getValue(i, "valuepos"));
            }
        }
        return ds;
    }

    private static DataSet parseGroovyDataSet(String text, String parseRule, HashMap m) throws Exception {
        HashMap resultMap = GenericTextParser.parseGroovy(text, parseRule, m);
        return GenericTextParser.toResultDataSet(resultMap);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static HashMap parseGroovy(String text, String parseRule, HashMap m) throws Exception {
        Binding b = new Binding((Map)m);
        String scriptStr = "setInput(s);" + parseRule;
        b.setVariable(TEXT_BIND_VAR, (Object)text);
        Script valueS = null;
        if (scriptCache.get(scriptStr) != null) {
            valueS = (Script)scriptCache.get(scriptStr);
        } else {
            if (scriptCache.getSize() == 1000) {
                Cache cache = scriptCache;
                synchronized (cache) {
                    scriptCache.clear();
                    gs.resetLoadedClasses();
                }
            }
            valueS = gs.parse(scriptStr);
            scriptCache.put(scriptStr, valueS);
        }
        valueS.setBinding(b);
        HashMap resultMap = (HashMap)valueS.run();
        return resultMap;
    }

    static {
        scriptCache = new Cache("Groovy Scripts", 1000);
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass("com.labvantage.sapphire.instrument.textparser.TextTokenParser");
        gs = new GroovyShell(config);
    }
}

