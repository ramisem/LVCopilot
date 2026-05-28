/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.FontConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.DataExportConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelSortConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelTitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ExcelExportConfiguration
implements Serializable {
    private static final String DEFAULT_FILE_NAME = "DataExport";
    private static final String DEFAULT_FIRST_ROW = "4";
    private static final String DEFAULT_FIRST_COL = "1";
    private static final String DEFAULT_SHEET_NAME = "Sheet1";
    private static final String DEFAULT_FILE_TYPE = FileType.XLSX.getName();
    private final DataExportConfiguration parent;
    private final StringExpression fileNamePrefix;
    private final FileType fileType;
    private final int firstRow;
    private final int firstCol;
    private final FontConfiguration fontConf;
    private final List<ExcelColumnConfiguration> rowHeaderList;
    private final List<ExcelColumnConfiguration> colHeaderList;
    private final List<ExcelTitleConfiguration> titleConfList;
    private final String sheetName;
    private final ExcelSortConfiguration sortConf;

    public ExcelExportConfiguration(PropertyList excelExportProps, DataExportConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (excelExportProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.fileNamePrefix = new StringExpression(excelExportProps.getProperty("filenameprefix", DEFAULT_FILE_NAME));
        this.fileType = FileType.fromString(excelExportProps.getProperty("filetype", DEFAULT_FILE_TYPE));
        String firstRowString = excelExportProps.getProperty("firstrow", DEFAULT_FIRST_ROW);
        if (firstRowString.equals("0")) {
            firstRowString = DEFAULT_FIRST_COL;
        }
        this.firstRow = Integer.parseInt(firstRowString);
        String firstColString = excelExportProps.getProperty("firstcol", DEFAULT_FIRST_COL);
        if (firstColString.equals("0")) {
            firstColString = DEFAULT_FIRST_COL;
        }
        this.firstCol = Integer.parseInt(firstColString);
        this.fontConf = new FontConfiguration(excelExportProps.getPropertyListNotNull("fontprops"), this.getParent().getParent());
        this.rowHeaderList = new ArrayList<ExcelColumnConfiguration>();
        PropertyListCollection rowHeaderCollection = excelExportProps.getCollectionNotNull("rowheadercollection");
        for (int i = 0; i < rowHeaderCollection.size(); ++i) {
            PropertyList rowHeaderProps = rowHeaderCollection.getPropertyList(i);
            this.rowHeaderList.add(new ExcelColumnConfiguration(rowHeaderProps));
        }
        PropertyListCollection colHeaderCollection = excelExportProps.getCollectionNotNull("colheadercollection");
        this.colHeaderList = new ArrayList<ExcelColumnConfiguration>();
        for (int i = 0; i < colHeaderCollection.size(); ++i) {
            PropertyList colHeaderProps = colHeaderCollection.getPropertyList(i);
            this.colHeaderList.add(new ExcelColumnConfiguration(colHeaderProps));
        }
        this.sortConf = new ExcelSortConfiguration(excelExportProps.getPropertyListNotNull("sortprops"), this);
        this.titleConfList = new ArrayList<ExcelTitleConfiguration>();
        PropertyListCollection titleCollection = excelExportProps.getCollectionNotNull("titlecollection");
        for (int i = 0; i < titleCollection.size(); ++i) {
            PropertyList titleProps = titleCollection.getPropertyList(i);
            this.titleConfList.add(new ExcelTitleConfiguration(titleProps, this));
        }
        this.sheetName = excelExportProps.getProperty("sheetname", DEFAULT_SHEET_NAME);
    }

    public ExcelSortConfiguration getSortConf() {
        return this.sortConf;
    }

    public FileType getFileType() {
        return this.fileType;
    }

    public List<ExcelTitleConfiguration> getTitleConfList() {
        return this.titleConfList;
    }

    public List<ExcelColumnConfiguration> getRowHeaderList() {
        return this.rowHeaderList;
    }

    public List<ExcelColumnConfiguration> getColHeaderList() {
        return this.colHeaderList;
    }

    public StringExpression getFileNamePrefix() {
        return this.fileNamePrefix;
    }

    public int getFirstRow() {
        return this.firstRow;
    }

    public int getFirstCol() {
        return this.firstCol;
    }

    public FontConfiguration getFontConfiguration() {
        return this.fontConf;
    }

    public DataExportConfiguration getParent() {
        return this.parent;
    }

    public String getSheetName() {
        return this.sheetName;
    }

    public static enum FileType {
        XLS("XLS"),
        XLSX("XLSX");

        private final String name;

        private FileType(String name) {
            this.name = name;
        }

        public static FileType fromString(String name) {
            if (name != null) {
                for (FileType type : FileType.values()) {
                    if (!name.equalsIgnoreCase(type.name)) continue;
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown name: " + name);
        }

        public String getName() {
            return this.name;
        }
    }
}

