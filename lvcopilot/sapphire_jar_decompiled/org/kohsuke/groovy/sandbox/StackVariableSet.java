/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox;

import java.util.HashSet;
import java.util.Set;
import org.kohsuke.groovy.sandbox.ScopeTrackingClassCodeExpressionTransformer;

final class StackVariableSet
implements AutoCloseable {
    final ScopeTrackingClassCodeExpressionTransformer owner;
    final StackVariableSet parent;
    private final Set<String> names = new HashSet<String>();

    StackVariableSet(ScopeTrackingClassCodeExpressionTransformer owner) {
        this.owner = owner;
        this.parent = owner.varScope;
        owner.varScope = this;
    }

    void declare(String name) {
        this.names.add(name);
    }

    boolean has(String name) {
        StackVariableSet s = this;
        while (s != null) {
            if (s.names.contains(name)) {
                return true;
            }
            s = s.parent;
        }
        return false;
    }

    @Override
    public void close() {
        this.owner.varScope = this.parent;
    }
}

