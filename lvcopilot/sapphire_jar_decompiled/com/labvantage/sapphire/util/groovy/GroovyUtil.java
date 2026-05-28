/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Binding
 *  groovy.lang.GroovyShell
 *  groovy.lang.Script
 *  javax.servlet.jsp.PageContext
 *  org.codehaus.groovy.ast.ClassNode
 *  org.codehaus.groovy.ast.CodeVisitorSupport
 *  org.codehaus.groovy.ast.GroovyCodeVisitor
 *  org.codehaus.groovy.ast.MethodNode
 *  org.codehaus.groovy.ast.ModuleNode
 *  org.codehaus.groovy.ast.expr.Expression
 *  org.codehaus.groovy.ast.expr.PropertyExpression
 *  org.codehaus.groovy.ast.expr.VariableExpression
 *  org.codehaus.groovy.ast.stmt.BlockStatement
 *  org.codehaus.groovy.classgen.GeneratorContext
 *  org.codehaus.groovy.control.CompilationFailedException
 *  org.codehaus.groovy.control.CompilePhase
 *  org.codehaus.groovy.control.CompilerConfiguration
 *  org.codehaus.groovy.control.SourceUnit
 *  org.codehaus.groovy.control.customizers.CompilationCustomizer
 *  org.codehaus.groovy.runtime.typehandling.GroovyCastException
 */
package com.labvantage.sapphire.util.groovy;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.calculations.ExpressionPrefix;
import com.labvantage.sapphire.util.groovy.GroovyBuilder;
import com.labvantage.sapphire.util.groovy.GroovyPolicyUtil;
import com.labvantage.sapphire.util.groovy.GroovySandBox;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.tagext.SDITagInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GroovyUtil {
    public static final String BIND_VAR_DEVMODE = "devmode";
    public static final String BIND_VAR_USER = "user";
    public static final String BIND_VAR_ELEMENT = "element";
    public static final String BIND_VAR_PRIMARY = "primary";
    public static final String BIND_VAR_PRIMARY_DATASET = "primarydataset";
    public static final String BIND_VAR_SDIDATA = "sdidata";
    public static final String BIND_VAR_SDIDATA_DATASET = "sdidatadataset";
    public static final String BIND_VAR_SDIDATAITEM = "sdidataitem";
    public static final String BIND_VAR_SDIDATAITEM_DATASET = "sdidataitemdataset";
    public static final String BIND_VAR_POSTFIX_DATASET = "dataset";
    public static final String BIND_VAR_CURRENTROW = "currentrow";
    public static final String BIND_VAR_SDC = "sdc";
    public static final String BIND_VAR_PAGEDATA = "pagedata";
    public static final String BIND_VAR_POLICY = "policy";
    public static final String BIND_VAR_M18NUTIL = "m18n";
    public static final String UI_GEN_PREFIX = "/*-GAP Editor Generated-*/;";
    private static Cache scriptCache = new Cache("Groovy Scripts", 5000);
    private static Cache scriptVariableCache = new Cache("Groovy Scripts Parsed Variable", 1000);
    private static GroovyShell gshell = new GroovyShell();
    private static GroovyShell sandboxedgshell = GroovySandBox.getGroovyShell();
    private ConnectionInfo connectionInfo = null;
    private ConfigurationProcessor cp;
    private QueryProcessor qp;

    public static GroovyUtil getInstance(PageContext pageContext) {
        return GroovyUtil.getInstance(HttpUtil.getConnectionInfo(pageContext));
    }

    public static GroovyUtil getInstance(ConnectionInfo connectionInfo) {
        GroovyUtil groovyUtil = new GroovyUtil();
        groovyUtil.connectionInfo = connectionInfo;
        return groovyUtil;
    }

    public static GroovyUtil getInstance(ConnectionInfo connectionInfo, ConfigurationProcessor cp, QueryProcessor qp) {
        GroovyUtil groovyUtil = new GroovyUtil();
        groovyUtil.connectionInfo = connectionInfo;
        groovyUtil.cp = cp;
        groovyUtil.qp = qp;
        return groovyUtil;
    }

    public static void clearScriptCache() {
        scriptCache.clear();
    }

    private static synchronized void setScriptIntoCache(String expression, Script script) {
        scriptCache.put(expression, script);
    }

    private static synchronized Script getScriptFromCache(String expression) {
        return (Script)scriptCache.get(expression);
    }

    public static String evaluate(String expression, HashMap bindingMap) throws SapphireException {
        try {
            return GroovyUtil.evaluate(expression, bindingMap, "Expression Error: \"[exception]\". Check Logs for details.");
        }
        catch (SapphireException e) {
            Trace.logError("Expression Error when evaluating following: " + expression);
            throw e;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String evaluate(String expression, HashMap bindingMap, String exceptionMessage) throws SapphireException {
        String result;
        int i;
        if (expression.indexOf("$G{") == 0 && (i = (expression = expression.substring(3)).lastIndexOf("}")) > -1) {
            expression = expression.substring(0, i);
        }
        if (expression.indexOf(UI_GEN_PREFIX) >= 0) {
            expression = GroovyBuilder.buildExpression(expression, bindingMap);
        }
        try {
            Script script = GroovyUtil.getScriptFromCache(expression);
            if (script == null) {
                script = GroovyUtil.parseScript(expression);
                GroovyUtil.setScriptIntoCache(expression, script);
            }
            Binding bd = new Binding((Map)bindingMap);
            Script script2 = script;
            synchronized (script2) {
                script.setBinding(bd);
                result = script.run().toString();
            }
        }
        catch (Throwable t) {
            if (exceptionMessage.indexOf("[exception]") > -1 && (exceptionMessage = StringUtil.replaceAll(exceptionMessage, "[exception]", t.getMessage() != null ? t.getMessage() : t.toString())).contains("//startinsert") && exceptionMessage.contains("//endinsert")) {
                exceptionMessage = exceptionMessage.substring(0, exceptionMessage.indexOf("//startinsert")) + "\n{Generated Inserts}" + exceptionMessage.substring(exceptionMessage.indexOf("//endinsert") + 11);
            }
            throw new SapphireException(exceptionMessage);
        }
        return result;
    }

    public String evaluateSecure(String expression, HashMap bindingMap) throws SapphireException {
        try {
            return this.evaluateSecure(expression, bindingMap, "Expression Error: \"[exception]\". Check Logs for details.");
        }
        catch (SapphireException e) {
            Trace.logError("Expression Error when evaluating following: " + expression);
            throw e;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String evaluateSecure(String expression, HashMap bindingMap, String exceptionMessage) throws SapphireException {
        String result;
        int i;
        if (expression.indexOf("$G{") == 0 && (i = (expression = expression.substring(3)).lastIndexOf("}")) > -1) {
            expression = expression.substring(0, i);
        }
        if (expression.indexOf(UI_GEN_PREFIX) >= 0) {
            expression = GroovyBuilder.buildExpression(expression, bindingMap);
        }
        String newexpression = expression;
        try {
            Script script = GroovyUtil.getScriptFromCache(expression);
            boolean cached = true;
            if (script == null) {
                if (this.qp == null) {
                    this.qp = new QueryProcessor(this.connectionInfo.getConnectionId());
                }
                newexpression = ExpressionPrefix.prependCalcDefs(this.connectionInfo, this.qp, expression);
                script = GroovyUtil.parseScriptSecure(newexpression);
                cached = false;
            }
            Binding bd = new Binding((Map)bindingMap);
            Script script2 = script;
            synchronized (script2) {
                script.setBinding(bd);
                if (!cached) {
                    PropertyList securityPolicy;
                    GroovySandBox groovySandBox = new GroovySandBox();
                    if (this.qp == null) {
                        this.qp = new QueryProcessor(this.connectionInfo.getConnectionId());
                    }
                    DataSet nameSpaces = this.qp.getSqlDataSet("SELECT distinct namespace from expression");
                    for (int i2 = 0; i2 < nameSpaces.getRowCount(); ++i2) {
                        if (nameSpaces.getValue(i2, "namespace").length() <= 0) continue;
                        groovySandBox.addToWhiteList(nameSpaces.getValue(i2, "namespace") + "#");
                    }
                    PropertyList groovyFilter = null;
                    if (this.cp == null) {
                        this.cp = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
                    }
                    if ((groovyFilter = (securityPolicy = this.cp.getPolicy("SecurityPolicy", "Sapphire Custom")).getPropertyList("groovyscriptfilter")) != null && "Y".equals(groovyFilter.getProperty("enable"))) {
                        List<GroovyInterceptor> groovyInterceptorList;
                        PropertyListCollection blacklist;
                        PropertyListCollection whitelist = groovyFilter.getCollection("whitelist");
                        if (whitelist != null) {
                            for (int i3 = 0; i3 < whitelist.size(); ++i3) {
                                String filteritem = whitelist.getPropertyList(i3).getProperty("expression");
                                if (filteritem.length() <= 0) continue;
                                groovySandBox.addToWhiteList(filteritem);
                            }
                        }
                        if ((blacklist = groovyFilter.getCollection("blacklist")) != null) {
                            for (int i4 = 0; i4 < blacklist.size(); ++i4) {
                                String filteritem = blacklist.getPropertyList(i4).getProperty("expression");
                                if (filteritem.length() <= 0) continue;
                                groovySandBox.addToBlackList(filteritem);
                            }
                        }
                        if ((groovyInterceptorList = GroovySandBox.getApplicableInterceptors()) != null) {
                            for (GroovyInterceptor interceptor : groovyInterceptorList) {
                                interceptor.unregister();
                            }
                        }
                        groovySandBox.register();
                    }
                }
                try {
                    Object resultObj = script.run();
                    result = resultObj == null ? null : resultObj.toString();
                }
                catch (GroovyCastException e) {
                    script = GroovyUtil.parseScript(newexpression);
                    script.setBinding(bd);
                    Object resultObj = script.run();
                    String string = result = resultObj == null ? null : resultObj.toString();
                }
                if (!cached) {
                    GroovyUtil.setScriptIntoCache(expression, GroovyUtil.parseScript(newexpression));
                }
            }
        }
        catch (Throwable t) {
            if (exceptionMessage.indexOf("[exception]") > -1 && (exceptionMessage = StringUtil.replaceAll(exceptionMessage, "[exception]", t.getMessage() != null ? t.getMessage() : t.toString())).contains("//startinsert") && exceptionMessage.contains("//endinsert")) {
                exceptionMessage = exceptionMessage.substring(0, exceptionMessage.indexOf("//startinsert")) + "\n{Generated Inserts}" + exceptionMessage.substring(exceptionMessage.indexOf("//endinsert") + 11);
            }
            if (t.getMessage() != null && t.getMessage().indexOf("Call not allowed per SecurityPolicy") >= 0) {
                if (this.cp == null) {
                    this.cp = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
                }
                PropertyList securityPolicy = this.cp.getPolicy("SecurityPolicy", "Sapphire Custom");
                SecurityPolicyUtil.handleViolation(this.connectionInfo.getConnectionId(), securityPolicy, exceptionMessage, exceptionMessage);
            }
            throw new SapphireException(exceptionMessage);
        }
        return result;
    }

    public static Script parseScript(String expression) throws SapphireException {
        Script script;
        try {
            script = gshell.parse(expression);
        }
        catch (Throwable t) {
            throw new SapphireException("Expression Error: " + t.getMessage() + " when evaluating: " + expression + "");
        }
        return script;
    }

    public static Script parseScriptSecure(String expression) throws SapphireException {
        Script script;
        try {
            script = sandboxedgshell.parse(expression);
        }
        catch (Throwable t) {
            System.out.println(t.getMessage());
            throw new SapphireException("Expression Error: " + t.getMessage() + " when evaluating: " + expression + "");
        }
        return script;
    }

    public static HashSet<String> getReferencedProperties(String expression, String variable) throws SapphireException {
        HashSet<String> properties = (HashSet<String>)scriptVariableCache.get(expression);
        if (properties == null) {
            int i;
            if (expression.indexOf("$G{") == 0 && (i = (expression = expression.substring(3)).lastIndexOf("}")) > -1) {
                expression = expression.substring(0, i);
            }
            VariablePropertyCollector compilationCustomizer = new VariablePropertyCollector();
            CompilerConfiguration configuration = new CompilerConfiguration();
            configuration.addCompilationCustomizers(new CompilationCustomizer[]{compilationCustomizer});
            GroovyShell gshell = new GroovyShell(configuration);
            gshell.parse(expression);
            properties = compilationCustomizer.getProperties(variable);
            scriptVariableCache.put(expression, properties);
        }
        return properties;
    }

    public static String evaluate(String expression, sapphire.util.ConnectionInfo connectionInfo, SDITagInfo sdiinfo, PropertyList element, PropertyList pagedata, PropertyList sdc) throws SapphireException {
        return GroovyUtil.getInstance(connectionInfo).evaluateSecure(expression, GroovyUtil.getCommonBindingMap(connectionInfo, sdiinfo, element, pagedata, sdc));
    }

    public static String evaluate(String expression, sapphire.util.ConnectionInfo connectionInfo, SDITagInfo sdiinfo, PropertyList element, PropertyList pagedata, PropertyList sdc, String datasetname) throws SapphireException {
        return GroovyUtil.getInstance(connectionInfo).evaluateSecure(expression, GroovyUtil.getCommonBindingMap(connectionInfo, sdiinfo, element, pagedata, sdc, datasetname));
    }

    public static String evaluate(String expression, sapphire.util.ConnectionInfo connectionInfo, PropertyList element, PropertyList pagedata, PropertyList sdc) throws SapphireException {
        return GroovyUtil.getInstance(connectionInfo).evaluateSecure(expression, GroovyUtil.getCommonBindingMap(connectionInfo, null, element, pagedata, sdc));
    }

    public static String evaluateYN(String expression, sapphire.util.ConnectionInfo connectionInfo, PropertyList element, PropertyList pagedata, PropertyList sdc) throws SapphireException {
        return GroovyUtil.evaluateYN(expression, connectionInfo, null, element, pagedata, sdc);
    }

    public static String evaluateYN(String expression, sapphire.util.ConnectionInfo connectionInfo, SDITagInfo sdiinfo, PropertyList element, PropertyList pagedata, PropertyList sdc) throws SapphireException {
        String result = GroovyUtil.evaluate(expression, connectionInfo, sdiinfo, element, pagedata, sdc);
        if ("true".equals(result)) {
            result = "Y";
        } else if ("false".equals(result)) {
            result = "N";
        }
        return result;
    }

    public static HashMap<String, Object> getCommonBindingMap(sapphire.util.ConnectionInfo connectionInfo, SDITagInfo sdiinfo, PropertyList element, PropertyList pagedata, PropertyList sdc, String datasetname) {
        HashMap<String, Object> map = GroovyUtil.getCommonBindingMap(connectionInfo, sdiinfo, element, pagedata, sdc);
        if (!datasetname.equalsIgnoreCase(BIND_VAR_PRIMARY)) {
            DataSet dataset = sdiinfo.getDataSet(datasetname);
            map.put(BIND_VAR_POSTFIX_DATASET, dataset);
            if (dataset != null) {
                HashMap datasetyrow;
                int currentrow = sdiinfo.getCurrentRow(datasetname);
                HashMap hashMap = datasetyrow = currentrow >= 0 ? sdiinfo.getDataSet(datasetname).get(currentrow) : new HashMap();
                if (dataset.getRowCount() == 1) {
                    currentrow = 0;
                }
                map.put(datasetname, datasetyrow);
                map.put(BIND_VAR_CURRENTROW, new Integer(currentrow));
            }
        }
        return map;
    }

    public static HashMap<String, Object> getCommonBindingMap(sapphire.util.ConnectionInfo connectionInfo, SDITagInfo sdiinfo, PropertyList element, PropertyList pagedata, PropertyList sdc) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (sdiinfo != null) {
            DataSet primary = sdiinfo.getDataSet(BIND_VAR_PRIMARY);
            map.put(BIND_VAR_PRIMARY_DATASET, primary);
            if (primary != null) {
                int currentrow = sdiinfo.getCurrentRow(BIND_VAR_PRIMARY);
                if (primary.getRowCount() == 1) {
                    currentrow = 0;
                }
                if (currentrow == -9999) {
                    currentrow = 0;
                }
                Object primaryrow = sdiinfo.getDataSet(BIND_VAR_PRIMARY).size() == 0 ? new HashMap() : (currentrow >= 0 ? sdiinfo.getDataSet(BIND_VAR_PRIMARY).get(currentrow) : new HashMap());
                map.put(BIND_VAR_PRIMARY, primaryrow);
                map.put(BIND_VAR_CURRENTROW, new Integer(currentrow));
            }
        }
        if (element != null) {
            map.put(BIND_VAR_ELEMENT, element);
        }
        if (connectionInfo != null) {
            map.put(BIND_VAR_USER, connectionInfo.getUserAttributeMap());
        }
        if (pagedata != null) {
            map.put(BIND_VAR_PAGEDATA, pagedata);
        }
        if (sdc != null) {
            map.put(BIND_VAR_SDC, sdc);
        }
        map.put(BIND_VAR_DEVMODE, Configuration.isDevmode(connectionInfo.getDatabaseId()));
        if (sdiinfo != null) {
            map.put(BIND_VAR_POLICY, new GroovyPolicyUtil(sdiinfo.getPageContext()));
        }
        return map;
    }

    private static class VariablePropertyCollector
    extends CompilationCustomizer {
        private HashMap<String, HashSet<String>> variablePropertyListMap = new HashMap();

        public VariablePropertyCollector() {
            super(CompilePhase.CONVERSION);
        }

        public HashSet<String> getProperties(String variable) {
            return this.variablePropertyListMap.get(variable);
        }

        public void call(SourceUnit sourceUnit, GeneratorContext generatorContext, ClassNode classNode) throws CompilationFailedException {
            ModuleNode ast = sourceUnit.getAST();
            CodeVisitorSupport visitor1 = new CodeVisitorSupport(){

                public void visitPropertyExpression(PropertyExpression propertyExpression) {
                    Expression object = propertyExpression.getObjectExpression();
                    if (object instanceof VariableExpression) {
                        String variableName = ((VariableExpression)object).getName();
                        String propertyid = propertyExpression.getPropertyAsString();
                        if (variablePropertyListMap.get(variableName) == null) {
                            variablePropertyListMap.put(variableName, new HashSet());
                        }
                        ((HashSet)variablePropertyListMap.get(variableName)).add(propertyid);
                    }
                }
            };
            ClassNode classNode1 = ast.getScriptClassDummy();
            List methodNodeList = classNode1.getMethods();
            BlockStatement statement = (BlockStatement)((MethodNode)methodNodeList.get(1)).getCode();
            statement.visit((GroovyCodeVisitor)visitor1);
        }
    }
}

