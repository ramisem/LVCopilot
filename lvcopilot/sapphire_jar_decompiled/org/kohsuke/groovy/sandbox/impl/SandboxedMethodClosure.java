/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.codehaus.groovy.runtime.InvokerHelper
 *  org.codehaus.groovy.runtime.InvokerInvocationException
 *  org.codehaus.groovy.runtime.MethodClosure
 */
package org.kohsuke.groovy.sandbox.impl;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.MethodClosure;
import org.kohsuke.groovy.sandbox.impl.Checker;

public class SandboxedMethodClosure
extends MethodClosure {
    public SandboxedMethodClosure(Object owner, String method) {
        super(owner, method);
    }

    protected Object doCall(Object[] arguments) {
        try {
            return Checker.checkedCall(this.getOwner(), false, false, this.getMethod(), arguments);
        }
        catch (Throwable e) {
            throw new InvokerInvocationException(e);
        }
    }

    protected Object doCall() {
        Object[] emptyArgs = new Object[]{};
        return this.doCall(emptyArgs);
    }

    protected Object doCall(Object arguments) {
        return this.doCall(InvokerHelper.asArray((Object)arguments));
    }
}

