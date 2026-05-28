/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.GroovyShell
 *  org.codehaus.groovy.control.CompilerConfiguration
 *  org.codehaus.groovy.control.customizers.CompilationCustomizer
 */
package com.labvantage.sapphire.util.groovy;

import groovy.lang.GroovyShell;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;
import org.kohsuke.groovy.sandbox.GroovyValueFilter;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

public class GroovySandBox
extends GroovyValueFilter {
    private final List<String> whiteList = new ArrayList<String>();
    private final List<String> blackList = new ArrayList<String>();

    public GroovySandBox() {
        this.addToWhiteList("java.lang.");
        this.addToWhiteList("java.util.");
        this.addToWhiteList("java.text.");
        this.addToWhiteList("java.math.");
        this.addToWhiteList("com.labvantage.sapphire");
        this.addToWhiteList("sapphire.");
        this.addToWhiteList("[Ljava.lang.");
        this.addToWhiteList("[Ljava.util.");
        this.addToWhiteList("[Ljava.text.");
        this.addToWhiteList("[Ljava.math.");
        this.addToWhiteList("sapphire.");
        this.addToWhiteList("org.apache.commons.collections.");
        this.addToWhiteList("LV#");
        this.addToWhiteList("org.codehaus.groovy.");
        this.addToBlackList("java.lang.System#exit");
        this.addToBlackList("java.lang.Runtime");
        this.addToBlackList("java.lang.Thread");
        this.addToBlackList("java.lang.Process");
    }

    @Override
    public Object onMethodCall(GroovyInterceptor.Invoker invoker, Object receiver, String method, Object ... args) throws Throwable {
        this.checkCallAllowed(receiver, method, args);
        return super.onMethodCall(invoker, receiver, method, args);
    }

    @Override
    public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object ... args) throws Throwable {
        this.checkCallAllowed(receiver, method, args);
        return super.onStaticCall(invoker, receiver, method, args);
    }

    @Override
    public Object onNewInstance(GroovyInterceptor.Invoker invoker, Class receiver, Object ... args) throws Throwable {
        this.checkCallAllowed(receiver, "", args);
        return super.onNewInstance(invoker, receiver, args);
    }

    public static GroovyShell getGroovyShell() {
        CompilerConfiguration conf = new CompilerConfiguration();
        SandboxTransformer customerTransformer = new SandboxTransformer();
        conf.addCompilationCustomizers(new CompilationCustomizer[]{customerTransformer});
        GroovyShell shell = new GroovyShell(conf);
        return shell;
    }

    private String toMethodDescriptor(Object receiver, String method, Object ... args) {
        String objectClass = this.getReceiverClassName(receiver);
        StringBuilder sb = new StringBuilder();
        sb.append(objectClass);
        sb.append("#");
        sb.append(method);
        sb.append("(");
        for (Object arg : args) {
            if (arg == null) {
                sb.append("null");
            } else {
                sb.append(arg.getClass().getName());
            }
            if (arg == args[args.length - 1]) continue;
            sb.append(",");
        }
        sb.append(")");
        return sb.toString();
    }

    private String getReceiverClassName(Object receiver) {
        return receiver instanceof Class ? ((Class)receiver).getName() : receiver.getClass().getName();
    }

    private void checkCallAllowed(Object receiver, String method, Object ... args) throws Throwable {
        String callPattern = this.toMethodDescriptor(receiver, method, args);
        boolean allowed = false;
        if (this.getReceiverClassName(receiver).indexOf("Script") == 0 && method.equals("call")) {
            allowed = true;
        }
        for (String name : this.whiteList) {
            if (callPattern.indexOf(name) != 0) continue;
            allowed = true;
            break;
        }
        if (allowed) {
            for (String blacked : this.blackList) {
                if (callPattern.indexOf(blacked) != 0) continue;
                allowed = false;
                break;
            }
        }
        if (!allowed) {
            throw new Exception("Call not allowed per SecurityPolicy:" + callPattern);
        }
    }

    public void addToWhiteList(String pattern) {
        this.whiteList.add(pattern);
    }

    public void addToBlackList(String pattern) {
        this.blackList.add(pattern);
    }
}

