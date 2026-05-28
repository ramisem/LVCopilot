/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.BaseFileDetails;

public class PPTFileDetails
extends BaseFileDetails {
    private int fromSlide = 1;
    private int toSlide = 1;
    private int totalSlidesAvailable;
    private int scaleFactor = 100;

    public int getScaleFactor() {
        return this.scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getFromSlide() {
        return this.fromSlide;
    }

    public void setFromSlide(int fromSlide) {
        this.fromSlide = fromSlide;
    }

    public int getToSlide() {
        return this.toSlide;
    }

    public void setToSlide(int toSlide) {
        this.toSlide = toSlide;
    }

    public int getTotalSlidesAvailable() {
        return this.totalSlidesAvailable;
    }

    public void setTotalSlidesAvailable(int totalSlidesAvailable) {
        this.totalSlidesAvailable = totalSlidesAvailable;
    }
}

