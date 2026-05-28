/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.kohsuke.groovy.sandbox.impl.Super;

public abstract class GroovyInterceptor {
    private static final ThreadLocal<List<GroovyInterceptor>> threadInterceptors = new ThreadLocal<List<GroovyInterceptor>>(){

        @Override
        protected List<GroovyInterceptor> initialValue() {
            return new CopyOnWriteArrayList<GroovyInterceptor>();
        }
    };
    private static final ThreadLocal<List<GroovyInterceptor>> threadInterceptorsView = new ThreadLocal<List<GroovyInterceptor>>(){

        @Override
        protected List<GroovyInterceptor> initialValue() {
            return Collections.unmodifiableList((List)threadInterceptors.get());
        }
    };

    public Object onMethodCall(Invoker invoker, Object receiver, String method, Object ... args) throws Throwable {
        return invoker.call(receiver, method, args);
    }

    public Object onStaticCall(Invoker invoker, Class receiver, String method, Object ... args) throws Throwable {
        return invoker.call((Object)receiver, method, args);
    }

    public Object onNewInstance(Invoker invoker, Class receiver, Object ... args) throws Throwable {
        return invoker.call((Object)receiver, (String)null, args);
    }

    public Object onSuperCall(Invoker invoker, Class senderType, Object receiver, String method, Object ... args) throws Throwable {
        return invoker.call((Object)new Super(senderType, receiver), method, args);
    }

    public void onSuperConstructor(Invoker invoker, Class receiver, Object ... args) throws Throwable {
        this.onNewInstance(invoker, receiver, args);
    }

    public Object onGetProperty(Invoker invoker, Object receiver, String property) throws Throwable {
        return invoker.call(receiver, property);
    }

    public Object onSetProperty(Invoker invoker, Object receiver, String property, Object value) throws Throwable {
        return invoker.call(receiver, property, value);
    }

    public Object onGetAttribute(Invoker invoker, Object receiver, String attribute) throws Throwable {
        return invoker.call(receiver, attribute);
    }

    public Object onSetAttribute(Invoker invoker, Object receiver, String attribute, Object value) throws Throwable {
        return invoker.call(receiver, attribute, value);
    }

    public Object onGetArray(Invoker invoker, Object receiver, Object index) throws Throwable {
        return invoker.call(receiver, (String)null, index);
    }

    public Object onSetArray(Invoker invoker, Object receiver, Object index, Object value) throws Throwable {
        return invoker.call(receiver, (String)null, index, value);
    }

    public void register() {
        threadInterceptors.get().add(this);
    }

    public void unregister() {
        threadInterceptors.get().remove(this);
    }

    public static List<GroovyInterceptor> getApplicableInterceptors() {
        return threadInterceptorsView.get();
    }

    public static interface Invoker {
        public Object call(Object var1, String var2) throws Throwable;

        public Object call(Object var1, String var2, Object var3) throws Throwable;

        public Object call(Object var1, String var2, Object var3, Object var4) throws Throwable;

        public Object call(Object var1, String var2, Object ... var3) throws Throwable;
    }
}

