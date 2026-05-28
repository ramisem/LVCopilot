/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.BaseClass;
import java.util.HashMap;

public class Timer
extends BaseClass {
    private HashMap _timersstart = new HashMap();
    private HashMap _timersincrement = new HashMap();

    public void startTimer(String timerid) {
        this._timersstart.put(timerid, new Long(System.currentTimeMillis()));
        this._timersincrement.put(timerid, new Long(System.currentTimeMillis()));
    }

    public long stopTimer(String timerid) {
        long time = this.getStartOffset(timerid);
        this._timersstart.remove(timerid);
        this._timersincrement.remove(timerid);
        return time;
    }

    public long getTimerStart(String timerid) {
        Long start = (Long)this._timersstart.get(timerid);
        return start != null ? start : -1L;
    }

    public long getStartOffset(String timerid) {
        Long start = (Long)this._timersstart.get(timerid);
        return start != null ? System.currentTimeMillis() - start : 0L;
    }

    public long getTimerIncrement(String timerid) {
        Long inc = (Long)this._timersincrement.get(timerid);
        return inc != null ? inc : -1L;
    }

    public void setTimerIncrement(String timerid, long time) {
        this._timersincrement.put(timerid, new Long(time));
    }
}

