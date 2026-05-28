/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import java.util.ArrayList;
import java.util.Iterator;

public class StatsBlock {
    public static final int INC_COUNT = 0;
    public static final int SET_START = 1;
    public static final int SET_END = 2;
    private String _name;
    private String _threadname;
    private int _count = 0;
    private long _starttime = 0L;
    ArrayList _times = new ArrayList();

    public StatsBlock(String name, String threadname) {
        this._name = name;
        this._threadname = threadname;
    }

    public String getName() {
        return this._name;
    }

    public String getThreadName() {
        return this._threadname;
    }

    public void incCount() {
        ++this._count;
    }

    public int getCount() {
        return this._count;
    }

    public void setStart() {
        this._starttime = System.currentTimeMillis();
    }

    public void setEnd() {
        if (this._starttime > 0L) {
            this._times.add(new Long(System.currentTimeMillis() - this._starttime));
        }
    }

    public int getTimeCount() {
        return this._times.size();
    }

    public long getTimeLast() {
        Long last = (Long)this._times.get(this._times.size() - 1);
        return last != null ? last : 0L;
    }

    public long getTimeTotal() {
        long total = 0L;
        Iterator it = this._times.iterator();
        while (it.hasNext()) {
            total += ((Long)it.next()).longValue();
        }
        return total;
    }

    public long getTimeAverage() {
        long ave = 0L;
        Iterator it = this._times.iterator();
        while (it.hasNext()) {
            ave += ((Long)it.next()).longValue();
        }
        return this._times.size() == 0 ? 0L : ave / (long)this._times.size();
    }

    public long getTimeMin() {
        long min = Long.MAX_VALUE;
        Iterator it = this._times.iterator();
        while (it.hasNext()) {
            min = Math.min(min, (Long)it.next());
        }
        return this._times.size() == 0 ? 0L : min;
    }

    public long getTimeMax() {
        long max = Long.MIN_VALUE;
        Iterator it = this._times.iterator();
        while (it.hasNext()) {
            max = Math.max(max, (Long)it.next());
        }
        return this._times.size() == 0 ? 0L : max;
    }
}

