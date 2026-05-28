/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  groovy.lang.Closure
 */
package org.kohsuke.groovy.sandbox.impl;

import groovy.lang.Closure;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ClosureSupport {
    public static final Set<String> BUILTIN_PROPERTIES = new HashSet<String>(Arrays.asList("delegate", "owner", "maximumNumberOfParameters", "parameterTypes", "metaClass", "class", "directive", "resolveStrategy", "thisObject"));

    ClosureSupport() {
    }

    public static List<Object> targetsOf(Closure receiver) {
        Object owner = receiver.getOwner();
        Object delegate = receiver.getDelegate();
        switch (receiver.getResolveStrategy()) {
            case 0: {
                return ClosureSupport.of(owner, delegate);
            }
            case 1: {
                return ClosureSupport.of(delegate, owner);
            }
            case 2: {
                return ClosureSupport.of(owner);
            }
            case 3: {
                return ClosureSupport.of(delegate);
            }
        }
        return Collections.emptyList();
    }

    private static List<Object> of(Object o1, Object o2) {
        if (o1 == null) {
            return ClosureSupport.of(o2);
        }
        if (o2 == null) {
            return ClosureSupport.of(o1);
        }
        if (o1 == o2) {
            return ClosureSupport.of(o1);
        }
        return Arrays.asList(o1, o2);
    }

    private static List<Object> of(Object maybeNull) {
        if (maybeNull == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(maybeNull);
    }
}

