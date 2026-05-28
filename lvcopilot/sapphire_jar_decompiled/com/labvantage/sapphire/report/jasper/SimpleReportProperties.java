/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.DisplayConstants;
import com.labvantage.sapphire.report.jasper.DisplayProperties;
import com.labvantage.sapphire.report.jasper.ReportColumn;
import com.labvantage.sapphire.report.jasper.ReportGrouping;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleReportProperties
implements Serializable {
    private String paperSize;
    private String title;
    private String logoFile;
    private String oddBackground = "white";
    private String evenBackground = "white";
    private boolean leftToRight = false;
    private boolean landscape;
    private boolean indexColumn = true;
    private boolean showCount = true;
    private byte logoAlign;
    private byte cellBorders = (byte)21;
    private byte pageNumberTop = DisplayConstants.ALIGN_LEFT;
    private byte pageNumberBottom = DisplayConstants.ALIGN_LEFT;
    private DisplayProperties titleDisplayProperties;
    private DisplayProperties filterProperties;
    private DisplayProperties thProperties;
    private DisplayProperties tdDefaultProperties;
    private ReportColumn[] reportColumns;
    private Map reportGroupings;

    public boolean isLeftToRight() {
        return this.leftToRight;
    }

    public void setLeftToRight(boolean leftToRight) {
        this.leftToRight = leftToRight;
    }

    public String getPaperSize() {
        return this.paperSize;
    }

    public void setPaperSize(String paperSize) {
        this.paperSize = paperSize;
    }

    public boolean isLandscape() {
        return this.landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DisplayProperties getTitleDisplayProperties() {
        if (this.titleDisplayProperties == null) {
            this.titleDisplayProperties = DisplayProperties.createDefault().createLike(new DisplayProperties.Editor(){

                @Override
                public void edit(DisplayProperties displayProperties) {
                    displayProperties.setWidth(300);
                }
            });
        }
        return this.titleDisplayProperties;
    }

    public void setTitleDisplayProperties(DisplayProperties titleDisplayProperties) {
        this.titleDisplayProperties = titleDisplayProperties;
    }

    public byte getLogoAlign() {
        return this.logoAlign;
    }

    public void setLogoAlign(byte logoAlign) {
        this.logoAlign = logoAlign;
    }

    public String getLogoFile() {
        return this.logoFile;
    }

    public void setLogoFile(String logoFile) {
        this.logoFile = logoFile;
    }

    public DisplayProperties getFilterProperties() {
        if (this.filterProperties == null) {
            this.filterProperties = DisplayProperties.createDefault().createLike(new DisplayProperties.Editor(){

                @Override
                public void edit(DisplayProperties displayProperties) {
                    displayProperties.setHeight(40);
                }
            });
        }
        return this.filterProperties;
    }

    public void setFilterProperties(DisplayProperties filterProperties) {
        this.filterProperties = filterProperties;
    }

    public DisplayProperties getThProperties() {
        if (this.thProperties == null) {
            this.thProperties = DisplayProperties.createDefault().createLike(new DisplayProperties.Editor(){

                @Override
                public void edit(DisplayProperties displayProperties) {
                    displayProperties.setDecoration((byte)11);
                }
            });
        }
        return this.thProperties;
    }

    public void setThProperties(DisplayProperties thProperties) {
        this.thProperties = thProperties;
    }

    public DisplayProperties getTdDefaultProperties() {
        if (this.tdDefaultProperties == null) {
            this.tdDefaultProperties = DisplayProperties.createDefault();
        }
        return this.tdDefaultProperties;
    }

    public void setTdDefaultProperties(DisplayProperties tdDefaultProperties) {
        this.tdDefaultProperties = tdDefaultProperties;
    }

    public byte getCellBorders() {
        return this.cellBorders;
    }

    public void setCellBorders(byte cellBorders) {
        this.cellBorders = cellBorders;
    }

    public boolean isIndexColumn() {
        return this.indexColumn;
    }

    public void setIndexColumn(boolean indexColumn) {
        this.indexColumn = indexColumn;
    }

    public boolean isShowCount() {
        return this.showCount;
    }

    public void setShowCount(boolean showCount) {
        this.showCount = showCount;
    }

    public String getOddBackground() {
        return this.oddBackground;
    }

    public void setOddBackground(String oddBackground) {
        this.oddBackground = oddBackground;
    }

    public String getEvenBackground() {
        return this.evenBackground;
    }

    public void setEvenBackground(String evenBackground) {
        this.evenBackground = evenBackground;
    }

    public byte getPageNumberTop() {
        return this.pageNumberTop;
    }

    public void setPageNumberTop(byte pageNumberTop) {
        this.pageNumberTop = pageNumberTop;
    }

    public byte getPageNumberBottom() {
        return this.pageNumberBottom;
    }

    public void setPageNumberBottom(byte pageNumberBottom) {
        this.pageNumberBottom = pageNumberBottom;
    }

    public ReportColumn[] getReportColumns() {
        if (this.reportColumns == null) {
            this.reportColumns = new ReportColumn[0];
        }
        return this.reportColumns;
    }

    public void setReportColumns(ReportColumn[] reportColumns) {
        this.reportColumns = reportColumns;
    }

    public int getColumnsCount() {
        return this.getReportColumns().length;
    }

    public Map getReportGroupings() {
        return this.reportGroupings;
    }

    public List getReportGroupingsList() {
        return this.reportGroupings != null ? new ArrayList(this.reportGroupings.values()) : new ArrayList();
    }

    public void addReportGrouping(ReportGrouping grouping) {
        if (grouping == null) {
            return;
        }
        if (this.reportGroupings == null) {
            this.reportGroupings = new LinkedHashMap();
        }
        this.reportGroupings.put(grouping.getGroupingField(), grouping);
    }
}

