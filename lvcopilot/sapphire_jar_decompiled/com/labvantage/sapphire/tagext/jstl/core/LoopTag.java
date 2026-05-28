/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.tagext.Tag
 */
package com.labvantage.sapphire.tagext.jstl.core;

import com.labvantage.sapphire.tagext.jstl.core.LoopTagStatus;
import javax.servlet.jsp.tagext.Tag;

public interface LoopTag
extends Tag {
    public Object getCurrent();

    public LoopTagStatus getLoopStatus();
}

