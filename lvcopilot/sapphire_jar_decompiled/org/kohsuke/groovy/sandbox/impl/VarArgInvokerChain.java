/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox.impl;

import org.kohsuke.groovy.sandbox.impl.InvokerChain;

abstract class VarArgInvokerChain
extends InvokerChain {
    private static final Object[] EMPTY_ARRAY = new Object[0];

    protected VarArgInvokerChain(Object receiver) {
        super(receiver);
    }

    @Override
    public final Object call(Object receiver, String method) throws Throwable {
        return this.call(receiver, method, EMPTY_ARRAY);
    }

    @Override
    public final Object call(Object receiver, String method, Object arg1) throws Throwable {
        return this.call(receiver, method, new Object[]{arg1});
    }

    @Override
    public final Object call(Object receiver, String method, Object arg1, Object arg2) throws Throwable {
        return this.call(receiver, method, new Object[]{arg1, arg2});
    }
}

