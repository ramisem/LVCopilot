/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox.impl;

import org.kohsuke.groovy.sandbox.impl.InvokerChain;

abstract class TwoArgInvokerChain
extends InvokerChain {
    protected TwoArgInvokerChain(Object receiver) {
        super(receiver);
    }

    @Override
    public final Object call(Object receiver, String method) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object call(Object receiver, String method, Object arg1) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object call(Object receiver, String method, Object ... args) throws Throwable {
        if (args.length != 2) {
            throw new UnsupportedOperationException();
        }
        return this.call(receiver, method, args[0], args[1]);
    }
}

