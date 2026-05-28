/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox.impl;

import org.kohsuke.groovy.sandbox.impl.InvokerChain;

abstract class ZeroArgInvokerChain
extends InvokerChain {
    protected ZeroArgInvokerChain(Object receiver) {
        super(receiver);
    }

    @Override
    public final Object call(Object receiver, String method, Object arg1) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object call(Object receiver, String method, Object arg1, Object arg2) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object call(Object receiver, String method, Object ... args) throws Throwable {
        if (args.length != 0) {
            throw new UnsupportedOperationException();
        }
        return this.call(receiver, method);
    }
}

