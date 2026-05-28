/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.tagext.jstl.core;

public interface LoopTagStatus {
    public Object getCurrent();

    public int getIndex();

    public int getCount();

    public boolean isFirst();

    public boolean isLast();

    public Integer getBegin();

    public Integer getEnd();

    public Integer getStep();
}

