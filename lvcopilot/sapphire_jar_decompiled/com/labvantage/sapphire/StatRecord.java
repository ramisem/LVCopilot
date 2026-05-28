/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

class StatRecord {
    public long count = 0L;
    public long total = 0L;
    public long min = 999999999L;
    public long max = 0L;
    public long last = 0L;

    StatRecord() {
    }

    public synchronized void setTime(long took) {
        this.total += took;
        ++this.count;
        if (took < this.min) {
            this.min = took;
        }
        if (took > this.max) {
            this.max = took;
        }
        this.last = took;
    }
}

