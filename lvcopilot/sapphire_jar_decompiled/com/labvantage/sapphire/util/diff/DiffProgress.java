/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.diff;

public class DiffProgress {
    private boolean cancelled;
    private String progress = "";
    private int percentComplete = -1;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getProgress() {
        return this.progress + (this.percentComplete >= 0 ? ": " + this.percentComplete + "%" : "");
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public void setPercentComplete(int percentComplete) {
        this.percentComplete = percentComplete;
    }
}

