/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox;

import org.kohsuke.groovy.sandbox.GroovyInterceptor;

public class GroovyValueFilter
extends GroovyInterceptor {
    public Object filterReceiver(Object receiver) {
        return this.filter(receiver);
    }

    public Object filterReturnValue(Object returnValue) {
        return this.filter(returnValue);
    }

    public Object filterArgument(Object arg) {
        return this.filter(arg);
    }

    public Object filterIndex(Object index) {
        return this.filter(index);
    }

    public Object filter(Object o) {
        return o;
    }

    private Object[] filterArguments(Object[] args) {
        for (int i = 0; i < args.length; ++i) {
            args[i] = this.filterArgument(args[i]);
        }
        return args;
    }

    @Override
    public Object onMethodCall(GroovyInterceptor.Invoker invoker, Object receiver, String method, Object ... args) throws Throwable {
        return this.filterReturnValue(super.onMethodCall(invoker, this.filterReceiver(receiver), method, this.filterArguments(args)));
    }

    @Override
    public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver, String method, Object ... args) throws Throwable {
        return this.filterReturnValue(super.onStaticCall(invoker, (Class)this.filterReceiver(receiver), method, this.filterArguments(args)));
    }

    @Override
    public Object onNewInstance(GroovyInterceptor.Invoker invoker, Class receiver, Object ... args) throws Throwable {
        return this.filterReturnValue(super.onNewInstance(invoker, (Class)this.filterReceiver(receiver), this.filterArguments(args)));
    }

    @Override
    public Object onGetProperty(GroovyInterceptor.Invoker invoker, Object receiver, String property) throws Throwable {
        return this.filterReturnValue(super.onGetProperty(invoker, this.filterReceiver(receiver), property));
    }

    @Override
    public Object onSetProperty(GroovyInterceptor.Invoker invoker, Object receiver, String property, Object value) throws Throwable {
        return this.filterReturnValue(super.onSetProperty(invoker, this.filterReceiver(receiver), property, this.filterArgument(value)));
    }

    @Override
    public Object onGetAttribute(GroovyInterceptor.Invoker invoker, Object receiver, String attribute) throws Throwable {
        return this.filterReturnValue(super.onGetAttribute(invoker, this.filterReceiver(receiver), attribute));
    }

    @Override
    public Object onSetAttribute(GroovyInterceptor.Invoker invoker, Object receiver, String attribute, Object value) throws Throwable {
        return this.filterReturnValue(super.onSetAttribute(invoker, this.filterReceiver(receiver), attribute, this.filterArgument(value)));
    }

    @Override
    public Object onGetArray(GroovyInterceptor.Invoker invoker, Object receiver, Object index) throws Throwable {
        return this.filterReturnValue(super.onGetArray(invoker, this.filterReceiver(receiver), this.filterIndex(index)));
    }

    @Override
    public Object onSetArray(GroovyInterceptor.Invoker invoker, Object receiver, Object index, Object value) throws Throwable {
        return this.filterReturnValue(super.onSetArray(invoker, this.filterReceiver(receiver), this.filterIndex(index), this.filterArgument(value)));
    }
}

