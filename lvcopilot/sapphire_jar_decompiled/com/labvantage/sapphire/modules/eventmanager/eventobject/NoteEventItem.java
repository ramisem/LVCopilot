/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;

public class NoteEventItem
extends SDIEventItem {
    private int notenum;

    public NoteEventItem(String sdcid, String keyid1, String keyid2, String keyid3, int notenum) {
        super(sdcid, keyid1, keyid2, keyid3);
        this.notenum = notenum;
    }

    public int getNotenum() {
        return this.notenum;
    }

    @Override
    public String toString() {
        return super.toString() + " " + this.getNotenum();
    }
}

