/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.ExcelFileDetails;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import com.labvantage.sapphire.util.file.PPTFileDetails;
import com.labvantage.sapphire.util.file.PdfFileDetails;
import com.labvantage.sapphire.util.file.TextFileDetails;
import com.labvantage.sapphire.util.file.WordFileDetails;

public class BaseFileDetails {
    private int maxAllowed = 100;
    private String renderStyle = "";
    private int imageQuality = 80;
    private int imageScale = 100;
    private boolean imageBorder = false;
    private String uniqueid = "";

    public static BaseFileDetails getFileDetails(FileTypeGroup fileTypeGroup) {
        BaseFileDetails baseFileDetails;
        switch (fileTypeGroup) {
            case WORD: {
                baseFileDetails = new WordFileDetails();
                break;
            }
            case PDF: {
                baseFileDetails = new PdfFileDetails();
                break;
            }
            case EXCEL: {
                baseFileDetails = new ExcelFileDetails();
                break;
            }
            case PPT: {
                baseFileDetails = new PPTFileDetails();
                break;
            }
            case TXT: {
                baseFileDetails = new TextFileDetails();
                break;
            }
            default: {
                baseFileDetails = new BaseFileDetails();
            }
        }
        return baseFileDetails;
    }

    public String getUniqueid() {
        return this.uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public String getRenderStyle() {
        return this.renderStyle;
    }

    public void setRenderStyle(String renderStyle) {
        this.renderStyle = renderStyle;
    }

    public int getMaxAllowed() {
        return this.maxAllowed;
    }

    public void setMaxAllowed(int maxAllowed) {
        this.maxAllowed = maxAllowed;
    }

    public void setImageQuality(int imageQuality) {
        this.imageQuality = imageQuality;
    }

    public int getImageQuality() {
        return this.imageQuality;
    }

    public void setImageScale(int imageScale) {
        this.imageScale = imageScale;
    }

    public int getImageScale() {
        return this.imageScale;
    }

    public void setImageBorder(boolean imageBorder) {
        this.imageBorder = imageBorder;
    }

    public boolean hasImageBorder() {
        return this.imageBorder;
    }
}

