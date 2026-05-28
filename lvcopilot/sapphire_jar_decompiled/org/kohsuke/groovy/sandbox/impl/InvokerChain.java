/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox.impl;

import java.util.Collections;
import java.util.Iterator;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;

abstract class InvokerChain
implements GroovyInterceptor.Invoker {
    protected final Iterator<GroovyInterceptor> chain;
    private static final Iterator<GroovyInterceptor> EMPTY_ITERATOR = Collections.emptyList().iterator();

    protected InvokerChain(Object receiver) {
        this.chain = receiver == null ? EMPTY_ITERATOR : GroovyInterceptor.getApplicableInterceptors().iterator();
    }
}

