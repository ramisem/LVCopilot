/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovyjarjarasm.asm.ClassVisitor
 *  groovyjarjarasm.asm.ClassWriter
 *  net.sf.jasperreports.compilers.DirectExpressionValueFilter
 *  net.sf.jasperreports.compilers.GroovyClassFilterTransformer
 *  net.sf.jasperreports.compilers.GroovyDirectExpressionValueFilter
 *  net.sf.jasperreports.compilers.GroovySandboxEvaluator
 *  net.sf.jasperreports.compilers.JRGroovyGenerator
 *  net.sf.jasperreports.compilers.ReportClassFilter
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRRuntimeException
 *  net.sf.jasperreports.engine.JasperReportsContext
 *  net.sf.jasperreports.engine.design.JRAbstractJavaCompiler
 *  net.sf.jasperreports.engine.design.JRCompilationSourceCode
 *  net.sf.jasperreports.engine.design.JRCompilationUnit
 *  net.sf.jasperreports.engine.design.JRDefaultCompilationSourceCode
 *  net.sf.jasperreports.engine.design.JRSourceCompileTask
 *  net.sf.jasperreports.engine.fill.JREvaluator
 *  net.sf.jasperreports.engine.util.JRClassLoader
 *  org.codehaus.groovy.ast.ClassNode
 *  org.codehaus.groovy.control.CompilationFailedException
 *  org.codehaus.groovy.control.CompilationUnit
 *  org.codehaus.groovy.control.CompilationUnit$ClassgenCallback
 *  org.codehaus.groovy.control.CompilerConfiguration
 *  org.codehaus.groovy.control.customizers.CompilationCustomizer
 */
package net.sf.jasperreports.compilers;

import groovyjarjarasm.asm.ClassVisitor;
import groovyjarjarasm.asm.ClassWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.compilers.DirectExpressionValueFilter;
import net.sf.jasperreports.compilers.GroovyClassFilterTransformer;
import net.sf.jasperreports.compilers.GroovyDirectExpressionValueFilter;
import net.sf.jasperreports.compilers.GroovySandboxEvaluator;
import net.sf.jasperreports.compilers.JRGroovyGenerator;
import net.sf.jasperreports.compilers.ReportClassFilter;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JRAbstractJavaCompiler;
import net.sf.jasperreports.engine.design.JRCompilationSourceCode;
import net.sf.jasperreports.engine.design.JRCompilationUnit;
import net.sf.jasperreports.engine.design.JRDefaultCompilationSourceCode;
import net.sf.jasperreports.engine.design.JRSourceCompileTask;
import net.sf.jasperreports.engine.fill.JREvaluator;
import net.sf.jasperreports.engine.util.JRClassLoader;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

public class JRGroovyCompiler_Patch
extends JRAbstractJavaCompiler {
    protected static final String SOURCE_ENCODING = "UTF-8";
    public static final String EXCEPTION_MESSAGE_KEY_COMPILING_EXPRESSIONS_CLASS_FILE = "compilers.compiling.expressions.class.file";
    public static final String EXCEPTION_MESSAGE_KEY_TOO_FEW_CLASSES_GENERATED = "compilers.groovy.too.few.classes.generated";
    public static final String EXCEPTION_MESSAGE_KEY_TOO_MANY_CLASSES_GENERATED = "compilers.groovy.too.many.classes.generated";
    public static final String EXCEPTION_MESSAGE_KEY_REPORT_NOT_COMPILED_FOR_CLASS_FILTERING = "compilers.groovy.report.not.compiled.for.class.filtering";

    public JRGroovyCompiler_Patch(JasperReportsContext jasperReportsContext) {
        super(jasperReportsContext, false);
    }

    protected DirectExpressionValueFilter directValueFilter() {
        return GroovyDirectExpressionValueFilter.instance();
    }

    protected String compileUnits(JRCompilationUnit[] units, String classpath, File tempDirFile) throws JRException {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding(SOURCE_ENCODING);
        if (this.reportClassFilter.isFilteringEnabled()) {
            config.addCompilationCustomizers(new CompilationCustomizer[]{new GroovyClassFilterTransformer()});
        }
        CompilationUnit unit = new CompilationUnit(config);
        for (int i = 0; i < units.length; ++i) {
            try {
                byte[] sourceBytes = units[i].getSourceCode().getBytes(SOURCE_ENCODING);
                unit.addSource("calculator_" + units[i].getName(), (InputStream)new ByteArrayInputStream(sourceBytes));
                continue;
            }
            catch (UnsupportedEncodingException e) {
                throw new JRRuntimeException((Throwable)e);
            }
        }
        ClassCollector collector = new ClassCollector();
        unit.setClassgenCallback((CompilationUnit.ClassgenCallback)collector);
        try {
            unit.compile(7);
        }
        catch (CompilationFailedException e) {
            throw new JRException(EXCEPTION_MESSAGE_KEY_COMPILING_EXPRESSIONS_CLASS_FILE, new Object[]{e.toString()}, (Throwable)e);
        }
        if (collector.classes.size() < units.length) {
            throw new JRException(EXCEPTION_MESSAGE_KEY_TOO_FEW_CLASSES_GENERATED, (Object[])null);
        }
        if (collector.classCount > units.length) {
            throw new JRException(EXCEPTION_MESSAGE_KEY_TOO_MANY_CLASSES_GENERATED, (Object[])null);
        }
        for (int i = 0; i < units.length; ++i) {
            units[i].setCompileData((Serializable)collector.classes.get(units[i].getName()));
        }
        return null;
    }

    protected void checkLanguage(String language) throws JRException {
        if (!"groovy".equals(language) && !"java".equals(language)) {
            throw new JRException("compilers.language.not.supported", new Object[]{language, "groovy", "java"});
        }
    }

    protected JRCompilationSourceCode generateSourceCode(JRSourceCompileTask sourceTask) throws JRException {
        return new JRDefaultCompilationSourceCode(JRGroovyGenerator.generateClass((JRSourceCompileTask)sourceTask, (ReportClassFilter)this.reportClassFilter), null);
    }

    protected String getSourceFileName(String unitName) {
        return unitName + ".groovy";
    }

    protected Class<?> loadClass(String className, byte[] compileData) {
        return JRClassLoader.loadClassFromBytes((String)className, (byte[])compileData);
    }

    protected JREvaluator loadEvaluator(Serializable compileData, String className) throws JRException {
        JREvaluator evaluator = super.loadEvaluator(compileData, className);
        if (this.reportClassFilter.isFilteringEnabled()) {
            if (!(evaluator instanceof GroovySandboxEvaluator)) {
                throw new JRException(EXCEPTION_MESSAGE_KEY_REPORT_NOT_COMPILED_FOR_CLASS_FILTERING);
            }
            ((GroovySandboxEvaluator)evaluator).setReportClassFilter(this.reportClassFilter);
        }
        return evaluator;
    }

    private static class ClassCollector
    implements CompilationUnit.ClassgenCallback {
        public Map<String, byte[]> classes = new HashMap<String, byte[]>();
        public int classCount;

        private ClassCollector() {
        }

        public void call(ClassVisitor writer, ClassNode node) throws CompilationFailedException {
            ++this.classCount;
            String name = node.getName();
            if (!this.classes.containsKey(name)) {
                byte[] bytes = ((ClassWriter)writer).toByteArray();
                this.classes.put(name, bytes);
            }
        }
    }
}

