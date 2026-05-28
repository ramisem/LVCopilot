/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.Cell
 *  com.aspose.cells.Cells
 *  com.aspose.cells.Font
 *  com.aspose.cells.Style
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.Worksheet
 */
package com.labvantage.sapphire.pageelements.datachart.data;

import com.aspose.cells.Cell;
import com.aspose.cells.Cells;
import com.aspose.cells.Style;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.TitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelColumnConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelExportConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelSortConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.dataexport.ExcelTitleConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.PlotConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.util.DateFormatConverter;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;

public class ExcelExportHelper {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final String DEFAULT_FONT = "Calibri";
    private static final int DEFAULT_FONT_SIZE = 12;
    private final ExcelExportConfiguration excelExportConf;
    private final ChartBindingMap chartBindingMap;
    private final String fileNamePrefix;
    private final Workbook workbook;
    private final Style dateStyle;
    private final Style normalStyle;
    private final Worksheet sheet;
    private final ExcelExportConfiguration.FileType fileType;
    private final ChartConfiguration chartConf;

    public ExcelExportHelper(ExcelExportConfiguration excelExportConf, ChartBindingMap chartBindingMap, ConnectionInfo connectionInfo) {
        if (excelExportConf == null) {
            throw new IllegalArgumentException("Excel export configuration is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        if (connectionInfo == null) {
            throw new IllegalArgumentException("Connection info is null");
        }
        this.excelExportConf = excelExportConf;
        this.chartBindingMap = chartBindingMap;
        this.fileNamePrefix = excelExportConf.getFileNamePrefix().evaluateNoException(chartBindingMap);
        this.fileType = excelExportConf.getFileType();
        this.chartConf = excelExportConf.getParent().getParent();
        this.workbook = this.fileType == ExcelExportConfiguration.FileType.XLS ? new Workbook() : new Workbook();
        this.dateStyle = this.workbook.createStyle();
        com.aspose.cells.Font ssFont = this.dateStyle.getFont();
        Font font = excelExportConf.getFontConfiguration().getFont(new Font(DEFAULT_FONT, 0, 12), Color.BLACK);
        ssFont.setName(font.getName());
        ssFont.setSize((int)((short)font.getSize()));
        ssFont.setBold(font.isBold());
        ssFont.setItalic(font.isItalic());
        Locale locale = I18nUtil.getConnectionLocale(connectionInfo);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(3, 3, locale);
        this.dateStyle.setCustom(DateFormatConverter.convert(locale, dateFormat));
        this.normalStyle = this.workbook.createStyle();
        ssFont = this.normalStyle.getFont();
        ssFont.setName(font.getName());
        ssFont.setSize((int)((short)font.getSize()));
        ssFont.setBold(font.isBold());
        ssFont.setItalic(font.isItalic());
        this.workbook.getWorksheets().removeAt(0);
        this.sheet = this.workbook.getWorksheets().add(excelExportConf.getSheetName());
    }

    private static File createTempFile(String fileNamePrefix, ExcelExportConfiguration.FileType fileType, Workbook workBook) throws SapphireException {
        FileOutputStream fileOut;
        File workBookFile;
        try {
            String suffix = "." + fileType.getName().toLowerCase();
            workBookFile = File.createTempFile(String.format("%-3s", fileNamePrefix).replace(' ', '_'), suffix);
        }
        catch (IOException e) {
            throw new SapphireException("Cannot create temp file for workbook.", e);
        }
        try {
            fileOut = new FileOutputStream(workBookFile);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create workbook.", e);
        }
        try {
            workBook.save((OutputStream)fileOut, fileType.getName().equalsIgnoreCase("XLSX") ? 6 : 5);
        }
        catch (IOException iOException) {
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            fileOut.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return workBookFile;
    }

    private void buildResults(Data data, List<String> rowHeaderColumnIdList, List<List<String>> rowHeaderGrid, List<String> colHeaderColumnIdList, List<List<String>> colHeaderGrid, int rowsStartRow, int colsStartCol, Cells cellArr) {
        Set<String> plotIdList = data.getPlotIdSet();
        for (String plotId : plotIdList) {
            List<TraceableSeriesGroup> traceableSeriesGroupList = data.getTraceableSeriesGroupList(plotId);
            for (int seriesGroupIndex = 0; seriesGroupIndex < data.getTraceableSeriesGroupList(plotId).size(); ++seriesGroupIndex) {
                TraceableSeriesGroup traceableSeriesGroup = traceableSeriesGroupList.get(seriesGroupIndex);
                boolean includeInDataExport = this.getIncludeInDataExportFlag(traceableSeriesGroup);
                if (!includeInDataExport) continue;
                for (int seriesIndex = 0; seriesIndex < traceableSeriesGroup.getSeriesCount(); ++seriesIndex) {
                    String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
                    DataSet seriesDataSet = traceableSeriesGroup.getSeriesDataSet(seriesId);
                    for (int itemIndex = 0; itemIndex < seriesDataSet.getRowCount(); ++itemIndex) {
                        String resultColumnId = traceableSeriesGroup.getSeriesGroupColumnConfiguration().getRangeValueColumn();
                        ArrayList<String> rowKey = new ArrayList<String>();
                        for (String string : rowHeaderColumnIdList) {
                            rowKey.add(this.getValue(seriesDataSet, itemIndex, string));
                        }
                        ArrayList<String> colKey = new ArrayList<String>();
                        for (String columnId3 : colHeaderColumnIdList) {
                            colKey.add(seriesDataSet.getValue(itemIndex, columnId3));
                        }
                        int n = colHeaderGrid.indexOf(colKey);
                        int resultRow = rowHeaderGrid.indexOf(rowKey);
                        while (cellArr.get(resultRow + rowsStartRow, n + colsStartCol).getType() != 3) {
                            ++resultRow;
                        }
                        if (seriesDataSet.isNull(itemIndex, resultColumnId)) continue;
                        int row = resultRow + rowsStartRow;
                        int col = n + colsStartCol;
                        this.setResultCellValue(cellArr.get(row, col), seriesDataSet, itemIndex, resultColumnId);
                    }
                }
            }
        }
    }

    private void setResultCellValue(Cell cell, DataSet dataSet, int row, String columnId) {
        if (dataSet.getColumnType(columnId) == 1) {
            cell.setValue((Object)dataSet.getBigDecimal(row, columnId).doubleValue());
            cell.setStyle(this.normalStyle);
        } else if (dataSet.getColumnType(columnId) == 2) {
            cell.setValue((Object)dataSet.getCalendar(row, columnId));
            cell.setStyle(this.dateStyle);
        } else {
            cell.setValue((Object)dataSet.getValue(row, columnId));
            cell.setStyle(this.normalStyle);
        }
    }

    public File buildWorkbook(Data data) throws SapphireException {
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        ArrayList<String> rowHeaderColumnIdList = new ArrayList<String>();
        this.populateRowHeaders(data, rowHeaderColumnIdList);
        List<List<String>> rowHeaderGrid = this.createRowHeaderGrid(data, rowHeaderColumnIdList);
        int rowCount = rowHeaderGrid.size();
        ArrayList<String> colHeaderColumnIdList = new ArrayList<String>();
        this.populateColHeaders(data, colHeaderColumnIdList);
        List<List<String>> colHeaderGrid = this.createColHeaderGrid(data, colHeaderColumnIdList);
        int colCount = colHeaderGrid.size();
        int firstRow = this.excelExportConf.getFirstRow() - 1;
        int firstCol = this.excelExportConf.getFirstCol() - 1;
        int rowHeaderCount = rowHeaderColumnIdList.size();
        int colHeaderCount = colHeaderColumnIdList.size();
        int maxTitleRow = 0;
        int maxTitleCol = 0;
        for (ExcelTitleConfiguration excelTitleConf : this.excelExportConf.getTitleConfList()) {
            int titleRow = excelTitleConf.getRow();
            int titleCol = excelTitleConf.getColumn();
            if (maxTitleRow < titleRow) {
                maxTitleRow = titleRow;
            }
            if (maxTitleCol >= titleCol) continue;
            maxTitleCol = titleCol;
        }
        int maxRow = this.getMax(rowCount, firstRow, colHeaderCount, maxTitleRow);
        int maxCol = this.getMax(colCount, firstCol, rowHeaderCount, maxTitleCol);
        int rowsStartRow = firstRow + colHeaderCount;
        int colsStartCol = firstCol + rowHeaderCount;
        Cells cellArr = this.createCellArray(maxRow, maxCol);
        this.buildRowHeaders(rowHeaderGrid, data.getDataSet(), rowCount, firstCol, rowHeaderColumnIdList, rowsStartRow, cellArr);
        this.buildColHeaders(colHeaderGrid, data.getDataSet(), colCount, firstRow, colHeaderColumnIdList, colsStartCol, cellArr);
        this.buildResults(data, rowHeaderColumnIdList, rowHeaderGrid, colHeaderColumnIdList, colHeaderGrid, rowsStartRow, colsStartCol, cellArr);
        this.buildTitles(cellArr);
        try {
            for (int i = 0; i <= maxCol; ++i) {
                this.sheet.autoFitColumn((int)((short)i));
            }
        }
        catch (Exception exp) {
            throw new SapphireException("Failed:" + exp.getMessage(), exp);
        }
        return ExcelExportHelper.createTempFile(this.fileNamePrefix, this.fileType, this.workbook);
    }

    private void populateColHeaders(Data data, List<String> colHeaderColumnIdList) {
        for (ExcelColumnConfiguration excelColumnConf : this.excelExportConf.getColHeaderList()) {
            String columnId = excelColumnConf.getColumnId().evaluateNoException(this.chartBindingMap);
            if (colHeaderColumnIdList.contains(columnId)) continue;
            colHeaderColumnIdList.add(columnId);
        }
        String colHeaderColumnId = data.getDataConfiguration().getDataSetConfiguration().getPlotIdColumn();
        if (!colHeaderColumnIdList.contains(colHeaderColumnId)) {
            colHeaderColumnIdList.add(colHeaderColumnId);
        }
        if (!colHeaderColumnIdList.contains(colHeaderColumnId = data.getDataConfiguration().getDataSetConfiguration().getSeriesGroupIdColumn())) {
            colHeaderColumnIdList.add(colHeaderColumnId);
        }
        if (!colHeaderColumnIdList.contains(colHeaderColumnId = data.getDataConfiguration().getDataSetConfiguration().getSeriesIdColumn())) {
            colHeaderColumnIdList.add(colHeaderColumnId);
        }
    }

    private void populateRowHeaders(Data data, List<String> rowHeaderColumnIdList) {
        for (ExcelColumnConfiguration excelColumnConf : this.excelExportConf.getRowHeaderList()) {
            String columnId = excelColumnConf.getColumnId().evaluateNoException(this.chartBindingMap);
            if (rowHeaderColumnIdList.contains(columnId)) continue;
            rowHeaderColumnIdList.add(columnId);
        }
        Set<String> plotIdList = data.getPlotIdSet();
        for (String plotId : plotIdList) {
            List<TraceableSeriesGroup> traceableSeriesGroupList = data.getTraceableSeriesGroupList(plotId);
            for (TraceableSeriesGroup traceableSeriesGroup : traceableSeriesGroupList) {
                String columnId;
                boolean includeInDataExport = this.getIncludeInDataExportFlag(traceableSeriesGroup);
                if (!includeInDataExport || rowHeaderColumnIdList.contains(columnId = traceableSeriesGroup.getSeriesGroupColumnConfiguration().getDomainValueColumn())) continue;
                rowHeaderColumnIdList.add(columnId);
            }
        }
    }

    private boolean getIncludeInDataExportFlag(TraceableSeriesGroup traceableSeriesGroup) {
        boolean includeInDataExport;
        PlotConfiguration.PlotType plotType;
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        String plotId = traceableSeriesGroup.getPlotId();
        PlotConfiguration plotConf = this.chartConf.getPlotConfiguration();
        if (plotConf.hasSubplot()) {
            plotConf = plotConf.getSubplotConfiguration(plotId);
        }
        if ((plotType = plotConf.getPlotType()) == PlotConfiguration.PlotType.XY_PLOT) {
            includeInDataExport = plotConf.getXYPlotConfiguration().getSeriesGroupConfiguration(seriesGroupId).includeInDataExport();
        } else if (plotType == PlotConfiguration.PlotType.CATEGORY_PLOT) {
            includeInDataExport = plotConf.getCategoryPlotConfiguration().getSeriesGroupConfiguration(seriesGroupId).includeInDataExport();
        } else if (plotType == PlotConfiguration.PlotType.PIE_PLOT) {
            includeInDataExport = true;
        } else {
            throw new IllegalArgumentException("Unknown plot type: " + (Object)((Object)plotType));
        }
        return includeInDataExport;
    }

    private void buildTitles(Cells cellArr) {
        List<ExcelTitleConfiguration> titleConfList = this.excelExportConf.getTitleConfList();
        for (ExcelTitleConfiguration excelTitleConf : titleConfList) {
            String titleId = excelTitleConf.getTitleId();
            TitleConfiguration titleConf = this.chartConf.getTitleConfiguration(titleId);
            TitleConfiguration.TitleType titleType = titleConf.getTitleType();
            if (titleType != TitleConfiguration.TitleType.TEXT) {
                throw new IllegalArgumentException("Unknown title type: " + (Object)((Object)titleType));
            }
            String text = titleConf.getTextTitleConfiguration().getText().evaluateNoException(this.chartBindingMap);
            int textRow = excelTitleConf.getRow() - 1;
            int textCol = excelTitleConf.getColumn() - 1;
            ExcelTitleConfiguration.TitleType excelTitleType = excelTitleConf.getTitleType();
            this.setValue(cellArr.get(textRow, textCol), text, excelTitleType.getColumnType());
        }
    }

    private int getMax(int count, int first, int headerCount, int maxTitle) {
        int max = first + headerCount + count;
        if (maxTitle > max) {
            max = maxTitle;
        }
        return max;
    }

    private List<List<String>> createRowHeaderGrid(final Data data, final List<String> rowHeaderColumnIdList) {
        ArrayList<List<String>> rowHeaderGrid = new ArrayList<List<String>>();
        HashMap<List, Integer> maxHeaderCountMap = new HashMap<List, Integer>();
        Set<String> plotIdList = data.getPlotIdSet();
        for (String plotId : plotIdList) {
            List<TraceableSeriesGroup> traceableSeriesGroupList = data.getTraceableSeriesGroupList(plotId);
            for (TraceableSeriesGroup traceableSeriesGroup : traceableSeriesGroupList) {
                boolean includeInDataExport = this.getIncludeInDataExportFlag(traceableSeriesGroup);
                if (!includeInDataExport) continue;
                for (int seriesIndex = 0; seriesIndex < traceableSeriesGroup.getSeriesCount(); ++seriesIndex) {
                    String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
                    DataSet seriesDataSet = traceableSeriesGroup.getSeriesDataSet(seriesId);
                    ArrayList seriesHeaderGrid = new ArrayList();
                    for (int itemIndex = 0; itemIndex < seriesDataSet.getRowCount(); ++itemIndex) {
                        ArrayList<String> headerList = new ArrayList<String>();
                        for (String columnId : rowHeaderColumnIdList) {
                            String value = this.getValue(seriesDataSet, itemIndex, columnId);
                            headerList.add(value);
                        }
                        seriesHeaderGrid.add(headerList);
                    }
                    HashSet uniqueSeriesHeaderGrid = new HashSet(seriesHeaderGrid);
                    for (List headerList : uniqueSeriesHeaderGrid) {
                        int headerCount = Collections.frequency(seriesHeaderGrid, headerList);
                        if (maxHeaderCountMap.containsKey(headerList)) {
                            int maxHeaderCount = (Integer)maxHeaderCountMap.get(headerList);
                            if (headerCount <= maxHeaderCount) continue;
                            maxHeaderCountMap.put(headerList, headerCount);
                            continue;
                        }
                        maxHeaderCountMap.put(headerList, headerCount);
                    }
                }
            }
        }
        for (List headerList : maxHeaderCountMap.keySet()) {
            int headerCount = (Integer)maxHeaderCountMap.get(headerList);
            for (int i = 0; i < headerCount; ++i) {
                rowHeaderGrid.add(headerList);
            }
        }
        Collections.sort(rowHeaderGrid, new Comparator<List<String>>(){

            @Override
            public int compare(List<String> o1, List<String> o2) {
                return ExcelExportHelper.this.sortHeaders(o1, o2, rowHeaderColumnIdList, ExcelExportHelper.this.excelExportConf.getSortConf().getRowHeaderSortList(), ExcelExportHelper.this.excelExportConf.getSortConf().getRowSortDirection(), data.getDataSet());
            }
        });
        return rowHeaderGrid;
    }

    private List<List<String>> createColHeaderGrid(final Data data, final List<String> colHeaderColumnIdList) {
        ArrayList<List<String>> colHeaderGrid = new ArrayList<List<String>>();
        Set<String> plotIdList = data.getPlotIdSet();
        for (String plotId : plotIdList) {
            List<TraceableSeriesGroup> traceableSeriesGroupList = data.getTraceableSeriesGroupList(plotId);
            for (TraceableSeriesGroup traceableSeriesGroup : traceableSeriesGroupList) {
                boolean includeInDataExport = this.getIncludeInDataExportFlag(traceableSeriesGroup);
                if (!includeInDataExport) continue;
                for (int seriesIndex = 0; seriesIndex < traceableSeriesGroup.getSeriesCount(); ++seriesIndex) {
                    String seriesId = traceableSeriesGroup.getSeriesId(seriesIndex);
                    DataSet seriesDataSet = traceableSeriesGroup.getSeriesDataSet(seriesId);
                    for (int itemIndex = 0; itemIndex < seriesDataSet.getRowCount(); ++itemIndex) {
                        ArrayList<String> headerList = new ArrayList<String>();
                        for (String columnId : colHeaderColumnIdList) {
                            String value = this.getValue(seriesDataSet, itemIndex, columnId);
                            headerList.add(value);
                        }
                        if (colHeaderGrid.contains(headerList)) continue;
                        colHeaderGrid.add(headerList);
                    }
                }
            }
        }
        Collections.sort(colHeaderGrid, new Comparator<List<String>>(){

            @Override
            public int compare(List<String> o1, List<String> o2) {
                return ExcelExportHelper.this.sortHeaders(o1, o2, colHeaderColumnIdList, ExcelExportHelper.this.excelExportConf.getSortConf().getColHeaderSortList(), ExcelExportHelper.this.excelExportConf.getSortConf().getColSortDirection(), data.getDataSet());
            }
        });
        return colHeaderGrid;
    }

    private int sortHeaders(List<String> headerList1, List<String> headerList2, List<String> headerColumnIdList, List<ExcelColumnConfiguration> sortColumnConfList, ExcelSortConfiguration.SortDirection sortDirection, DataSet dataSet) {
        int compare = 0;
        if (sortColumnConfList.size() > 0) {
            for (ExcelColumnConfiguration sortColumnConf : sortColumnConfList) {
                int columnType;
                String header2;
                String sortColumnId = sortColumnConf.getColumnId().evaluateNoException(this.chartBindingMap);
                int sortColIndex = headerColumnIdList.indexOf(sortColumnId);
                if (sortColIndex == -1) {
                    throw new IllegalArgumentException("Sort column: " + sortColumnId + " not found in data export header list");
                }
                String header1 = headerList1.get(sortColIndex);
                compare = this.compareValues(header1, header2 = headerList2.get(sortColIndex), columnType = dataSet.getColumnType(sortColumnId));
                if (compare == 0) continue;
                break;
            }
        } else {
            String header1 = headerList1.get(0);
            String header2 = headerList2.get(0);
            int columnType = dataSet.getColumnType(headerColumnIdList.get(0));
            compare = this.compareValues(header1, header2, columnType);
        }
        if (sortDirection == ExcelSortConfiguration.SortDirection.DESCENDING) {
            compare *= -1;
        }
        return compare;
    }

    private int compareValues(String header1, String header2, int columnType) {
        int compare;
        if (columnType == 0) {
            compare = header1.compareTo(header2);
        } else if (columnType == 1) {
            compare = new BigDecimal(header1).compareTo(new BigDecimal(header2));
        } else if (columnType == 2) {
            try {
                compare = DATE_FORMAT.parse(header1).compareTo(DATE_FORMAT.parse(header2));
            }
            catch (ParseException ignore) {
                compare = header1.compareTo(header2);
            }
        } else {
            throw new IllegalArgumentException("Unknown column type: " + columnType);
        }
        return compare;
    }

    private String getValue(DataSet dataSet, int row, String columnId) {
        String value = dataSet.getColumnType(columnId) == 2 && !dataSet.isNull(row, columnId) ? DATE_FORMAT.format(dataSet.getCalendar(row, columnId).getTime()) : (dataSet.getColumnType(columnId) == 1 && !dataSet.isNull(row, columnId) ? dataSet.getBigDecimal(row, columnId).toPlainString() : dataSet.getValue(row, columnId));
        return value;
    }

    private Cells createCellArray(int maxRow, int maxCol) {
        for (int r = 0; r < maxRow; ++r) {
            this.sheet.getCells().insertRow(r);
            for (int c = 0; c < maxCol; ++c) {
                this.sheet.getCells().insertColumn(c);
            }
        }
        return this.sheet.getCells();
    }

    private void buildRowHeaders(List<List<String>> rowHeaderGrid, DataSet dataSet, int rowCount, int firstCol, List<String> rowHeaderColumnIdList, int rowsStartRow, Cells cellArr) {
        for (int r = 0; r < rowCount; ++r) {
            List<String> headerList = rowHeaderGrid.get(r);
            for (int c = 0; c < rowHeaderColumnIdList.size(); ++c) {
                String columnId = rowHeaderColumnIdList.get(c);
                String header = headerList.get(c);
                int columnType = dataSet.getColumnType(columnId);
                this.setValue(cellArr.get(r + rowsStartRow, c + firstCol), header, columnType);
            }
        }
    }

    private void buildColHeaders(List<List<String>> colHeaderGrid, DataSet dataSet, int colCount, int firstRow, List<String> colHeaderColumnIdList, int colsStartCol, Cells cellArr) {
        for (int c = 0; c < colCount; ++c) {
            List<String> headerList = colHeaderGrid.get(c);
            for (int r = 0; r < colHeaderColumnIdList.size(); ++r) {
                String columnId = colHeaderColumnIdList.get(r);
                String header = headerList.get(r);
                int columnType = dataSet.getColumnType(columnId);
                this.setValue(cellArr.get(r + firstRow, c + colsStartCol), header, columnType);
            }
        }
    }

    private void setValue(Cell cell, String header, int columnType) {
        if (columnType == 1 && !header.isEmpty()) {
            BigDecimal bigDecimal = new BigDecimal(header);
            cell.setValue((Object)bigDecimal.doubleValue());
            cell.setStyle(this.normalStyle);
        } else if (columnType == 2 && !header.isEmpty()) {
            try {
                Date date = DATE_FORMAT.parse(header);
                cell.setValue((Object)date);
                cell.setStyle(this.dateStyle);
            }
            catch (ParseException ex) {
                ex.printStackTrace();
                cell.setValue((Object)ex.toString());
            }
        } else {
            cell.setValue((Object)header);
            cell.setStyle(this.normalStyle);
        }
    }
}

