/*
 * Decompiled with CFR 0.152.
 */
package org.kohsuke.groovy.sandbox.impl;

public final class Super {
    final Class senderType;
    final Object receiver;

    public Super(Class senderType, Object receiver) {
        this.senderType = senderType;
        this.receiver = receiver;
    }
}

