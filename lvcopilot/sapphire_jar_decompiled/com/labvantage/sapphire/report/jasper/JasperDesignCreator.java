/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRBand
 *  net.sf.jasperreports.engine.JRBoxContainer
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRExpression
 *  net.sf.jasperreports.engine.JRField
 *  net.sf.jasperreports.engine.JRFont
 *  net.sf.jasperreports.engine.JRGroup
 *  net.sf.jasperreports.engine.JRLineBox
 *  net.sf.jasperreports.engine.JRParameter
 *  net.sf.jasperreports.engine.JRStyleContainer
 *  net.sf.jasperreports.engine.JasperCompileManager
 *  net.sf.jasperreports.engine.JasperReport
 *  net.sf.jasperreports.engine.base.JRBaseFont
 *  net.sf.jasperreports.engine.base.JRBaseLineBox
 *  net.sf.jasperreports.engine.base.JRBaseStyle
 *  net.sf.jasperreports.engine.design.JRDesignBand
 *  net.sf.jasperreports.engine.design.JRDesignElement
 *  net.sf.jasperreports.engine.design.JRDesignExpression
 *  net.sf.jasperreports.engine.design.JRDesignField
 *  net.sf.jasperreports.engine.design.JRDesignFont
 *  net.sf.jasperreports.engine.design.JRDesignGroup
 *  net.sf.jasperreports.engine.design.JRDesignParameter
 *  net.sf.jasperreports.engine.design.JRDesignSection
 *  net.sf.jasperreports.engine.design.JRDesignStaticText
 *  net.sf.jasperreports.engine.design.JRDesignTextElement
 *  net.sf.jasperreports.engine.design.JRDesignTextField
 *  net.sf.jasperreports.engine.design.JRDesignVariable
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.type.CalculationEnum
 *  net.sf.jasperreports.engine.type.ColorEnum
 *  net.sf.jasperreports.engine.type.HorizontalTextAlignEnum
 *  net.sf.jasperreports.engine.type.ModeEnum
 *  net.sf.jasperreports.engine.type.OrientationEnum
 *  net.sf.jasperreports.engine.type.PenEnum
 *  net.sf.jasperreports.engine.type.ResetTypeEnum
 *  net.sf.jasperreports.engine.type.StretchTypeEnum
 *  net.sf.jasperreports.engine.type.VerticalTextAlignEnum
 *  net.sf.jasperreports.engine.util.JRBoxUtil
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.Aggregation;
import com.labvantage.sapphire.report.jasper.DisplayProperties;
import com.labvantage.sapphire.report.jasper.ReportColumn;
import com.labvantage.sapphire.report.jasper.ReportGrouping;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.report.jasper.SimpleReportProperties;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRBoxContainer;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRFont;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRLineBox;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRStyleContainer;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBaseFont;
import net.sf.jasperreports.engine.base.JRBaseLineBox;
import net.sf.jasperreports.engine.base.JRBaseStyle;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignFont;
import net.sf.jasperreports.engine.design.JRDesignGroup;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JRDesignVariable;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.CalculationEnum;
import net.sf.jasperreports.engine.type.ColorEnum;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.type.PenEnum;
import net.sf.jasperreports.engine.type.ResetTypeEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.type.VerticalTextAlignEnum;
import net.sf.jasperreports.engine.util.JRBoxUtil;

public class JasperDesignCreator {
    private static final String REPORT_NAME = "defaultName";
    private static final int PAGE_NUMBER_HEIGHT = 15;
    private static final int PAGE_HEADER_TITLE_HEIGHT = 40;
    private static final int PAGE_FOOTER_HEIGHT = 25;
    private static final int COLUMNS_MARGIN = 0;
    private static final int COLUMN_HEADER_MARGIN = 5;
    private static final int SUMMARY_COLUMN_HEIGHT = 60;
    private static final int INDEX_COLUMN_WIDTH = 40;
    private static final int GROUP_TITLE_HEIGHT = 35;
    private static final int GROUP_AGGREGATION_HEIGHT = 25;
    private static final int GROUP_FOOTER_MARGIN = 25;
    private static final DisplayProperties PAGE_NUMBER_PROPERTIES = new DisplayProperties();
    private static final String PAGE_NUMBER_FONT_FACE = "Helvetica";
    private static final int PAGE_NUMBER_FONT_SIZE = 10;
    private SimpleReportProperties reportProperties;
    private JasperDesign jasperDesign;
    private int pageWidth;
    private Map reportColumns = new HashMap();
    private static final double MILI2PIX = 2.83464567;

    public JasperDesignCreator(SimpleReportProperties reportProperties) {
        this.reportProperties = reportProperties;
    }

    public JasperReport validate() throws JRException {
        this.createJasperDesign();
        SapphireJasperUtil.setJasperCompilerClassPatch();
        return JasperCompileManager.compileReport((JasperDesign)this.jasperDesign);
    }

    public JasperDesign getJasperDesign() {
        return this.jasperDesign;
    }

    private void createJasperDesign() throws JRException {
        this.initMembers();
        this.initializeDesign();
        this.addGroupings();
        this.createPageHeader();
        this.addSelectedColumns();
        this.createPageFooter();
    }

    private void initMembers() {
        ReportColumn[] columns = this.reportProperties.getReportColumns();
        for (int i = 0; i < columns.length; ++i) {
            ReportColumn column = columns[i];
            this.reportColumns.put(column.getName(), column);
        }
    }

    private void initializeDesign() {
        this.jasperDesign = new JasperDesign();
        this.jasperDesign.setName(REPORT_NAME);
        this.jasperDesign.setLanguage("java");
        this.jasperDesign.setOrientation(this.reportProperties.isLandscape() ? OrientationEnum.LANDSCAPE : OrientationEnum.PORTRAIT);
        this.setPageSize();
    }

    private void setPageSize() {
        String paperSize = this.reportProperties.getPaperSize();
        if ("a1".equals(paperSize)) {
            this.jasperDesign.setPageHeight(2383);
            this.jasperDesign.setPageWidth(1683);
        } else if ("a2".equals(paperSize)) {
            this.jasperDesign.setPageHeight(1683);
            this.jasperDesign.setPageWidth(1190);
        } else if ("a3".equals(paperSize)) {
            this.jasperDesign.setPageHeight(1190);
            this.jasperDesign.setPageWidth(841);
        } else if ("a4".equals(paperSize)) {
            this.jasperDesign.setPageHeight(841);
            this.jasperDesign.setPageWidth(595);
        } else if ("a5".equals(paperSize)) {
            this.jasperDesign.setPageHeight(595);
            this.jasperDesign.setPageWidth(419);
        }
        this.jasperDesign.setColumnCount(1);
        this.jasperDesign.setColumnSpacing(0);
        this.jasperDesign.setColumnWidth(this.jasperDesign.getPageWidth() - 20);
        this.jasperDesign.setLeftMargin(10);
        this.jasperDesign.setRightMargin(10);
        this.pageWidth = this.jasperDesign.getColumnWidth();
    }

    private void addGroupings() throws JRException {
        List reportGroupings = this.reportProperties.getReportGroupingsList();
        if (reportGroupings != null && reportGroupings.size() != 0) {
            int size = reportGroupings.size();
            JRDesignBand headerBand = this.createBand(size * 35);
            int y = 0;
            for (int i = 0; i < size; ++i) {
                ReportGrouping grouping = (ReportGrouping)reportGroupings.get(i);
                JRDesignGroup jrDesignGroup = this.createJRGroup(i, grouping);
                if (this.addGroupHeader(headerBand, grouping, y)) {
                    y += 35;
                }
                if (i != size - 1) continue;
                ((JRDesignSection)jrDesignGroup.getGroupHeaderSection()).addBand((JRBand)headerBand);
            }
            headerBand.setHeight(y);
        }
    }

    private JRDesignGroup createJRGroup(int index, ReportGrouping grouping) throws JRException {
        JRDesignGroup jrDesignGroup = new JRDesignGroup();
        jrDesignGroup.setName("group_" + index);
        ReportColumn reportColumn = this.getReportColumn(grouping.getGroupingField());
        this.addField(reportColumn);
        jrDesignGroup.setExpression(this.createExpression("$F{" + reportColumn.getName() + "}", reportColumn.getJavaClass()));
        JRBand footerBand = this.getGroupFooter(grouping, jrDesignGroup);
        ((JRDesignSection)jrDesignGroup.getGroupFooterSection()).addBand(footerBand);
        this.jasperDesign.addGroup(jrDesignGroup);
        return jrDesignGroup;
    }

    private boolean addGroupHeader(JRDesignBand headerBand, ReportGrouping grouping, int y) {
        grouping.getTitleDisplayProperties().setHeight(35);
        grouping.getTitleDisplayProperties().setWidth(this.pageWidth);
        String titleExpression = grouping.getTitleExpression();
        if (titleExpression == null || titleExpression.length() == 0) {
            return false;
        }
        String groupingField = grouping.getGroupingField();
        ReportColumn groupingColumn = this.getReportColumn(groupingField);
        String formattedExpression = this.getFormattedExpression("\\$F{" + grouping.getGroupingField() + "}", groupingColumn);
        titleExpression = "\"" + titleExpression + "\"";
        titleExpression = titleExpression.replaceAll("#", "\" + " + formattedExpression + " + \"");
        headerBand.addElement((JRDesignElement)this.createTextField(this.createExpression(titleExpression, String.class), grouping.getTitleDisplayProperties(), 0, y, null, ModeEnum.OPAQUE.getValue()));
        return true;
    }

    private JRBand getGroupFooter(ReportGrouping grouping, JRDesignGroup jrDesignGroup) throws JRException {
        List aggregations = grouping.getAggregationsList();
        JRDesignBand result = null;
        if (aggregations != null) {
            int y = 0;
            int size = aggregations.size();
            result = this.createBand(25 * size + 25);
            for (int i = 0; i < size; ++i) {
                Aggregation aggregation = (Aggregation)aggregations.get(i);
                JRDesignVariable variable = this.createGroupAggrigationVariable(aggregation, jrDesignGroup, i);
                this.jasperDesign.addVariable(variable);
                aggregation.getDisplayProperties().setHeight(25);
                aggregation.getDisplayProperties().setWidth(this.pageWidth);
                result.addElement((JRDesignElement)this.createTextField(this.createGroupAggregationExpression(variable, aggregation), aggregation.getDisplayProperties(), 0, y, null, ModeEnum.OPAQUE.getValue()));
                y += 25;
            }
        }
        return result;
    }

    private JRDesignVariable createGroupAggrigationVariable(Aggregation aggregation, JRDesignGroup jrDesignGroup, int i) {
        JRDesignVariable result = new JRDesignVariable();
        result.setCalculation(CalculationEnum.getByValue((byte)aggregation.getOperator()));
        ReportColumn column = this.getReportColumn(aggregation.getAggregatee());
        result.setExpression(this.createExpression("$F{" + column.getName() + "}", column.getJavaClass()));
        result.setName("aggregate_" + jrDesignGroup.getName() + "_" + i);
        result.setResetGroup((JRGroup)jrDesignGroup);
        result.setResetType(ResetTypeEnum.GROUP);
        result.setValueClass(column.getJavaClass());
        return result;
    }

    private JRExpression createGroupAggregationExpression(JRDesignVariable variable, Aggregation aggregation) {
        String aggregatee = aggregation.getAggregatee();
        ReportColumn reportColumn = this.getReportColumn(aggregatee);
        String title = reportColumn.getTitle();
        String expression = this.getCalculationExpression(aggregation.getOperator()) + "+ \" " + title + ": \" + " + this.getFormattedExpression("$V{" + variable.getName() + "}", reportColumn);
        return this.createExpression(expression, String.class);
    }

    private void createPageHeader() throws JRException {
        int bandHeight = 0;
        if (this.reportProperties.getPageNumberTop() != -1) {
            bandHeight += 15;
        }
        if (this.reportProperties.getTitle() != null && this.reportProperties.getTitle().length() > 0) {
            bandHeight += 40;
        }
        JRDesignBand pageHeader = this.createBand(bandHeight + 40);
        this.jasperDesign.setPageHeader((JRBand)pageHeader);
        this.initHeaderSize();
        this.addHeaderText(pageHeader);
        this.addFilterText(pageHeader, bandHeight);
        this.addHeaderPageNubmer(pageHeader);
    }

    private void initHeaderSize() {
        DisplayProperties pageHeaderDisplayProperties = this.reportProperties.getTitleDisplayProperties();
        pageHeaderDisplayProperties.setWidth(this.pageWidth);
    }

    private void addHeaderText(JRDesignBand pageHeader) {
        if (this.reportProperties.getTitle() != null && this.reportProperties.getTitle().length() > 0) {
            int y = this.reportProperties.getPageNumberTop() == -1 ? 0 : 15;
            DisplayProperties titleDisplayProperties = this.reportProperties.getTitleDisplayProperties();
            titleDisplayProperties.setHeight(40);
            titleDisplayProperties.setWidth(this.pageWidth);
            JRDesignStaticText textElement = this.createStaticText(this.reportProperties.getTitle(), titleDisplayProperties, 0, y, null, ModeEnum.TRANSPARENT.getValue());
            pageHeader.addElement((JRDesignElement)textElement);
        }
    }

    private void addFilterText(JRDesignBand pageHeader, int y) throws JRException {
        JRDesignParameter parameter = new JRDesignParameter();
        parameter.setValueClass(String.class);
        parameter.setDefaultValueExpression(this.createExpression("\" \"", String.class));
        parameter.setName("filterCondition");
        this.jasperDesign.addParameter((JRParameter)parameter);
        DisplayProperties filterProperties = this.reportProperties.getFilterProperties();
        filterProperties.setWidth(this.pageWidth);
        JRDesignTextField filterCondition = this.createTextField("$P{filterCondition}", filterProperties, 0, y, null, ModeEnum.OPAQUE.getValue());
        pageHeader.addElement((JRDesignElement)filterCondition);
    }

    private void addHeaderPageNubmer(JRDesignBand pageHeader) {
        this.addPageNumber(pageHeader, this.reportProperties.getPageNumberTop());
    }

    private void addSelectedColumns() throws JRException {
        ReportColumn[] columns = this.reportProperties.getReportColumns();
        if (columns != null && columns.length > 0) {
            JRDesignBand headerBand = this.createBand(this.reportProperties.getThProperties().getHeight() + 5);
            JRDesignBand detailBand = this.createBand(this.reportProperties.getTdDefaultProperties().getHeight());
            JRDesignBand summaryBand = this.createBand(60);
            this.jasperDesign.setColumnHeader((JRBand)headerBand);
            ((JRDesignSection)this.jasperDesign.getDetailSection()).addBand((JRBand)detailBand);
            this.jasperDesign.setSummary((JRBand)summaryBand);
            int x = 0;
            List reportColumns = this.getSortedColumns();
            if (this.reportProperties.isIndexColumn()) {
                this.addIndexHeader(headerBand);
                this.addIndexDetail(detailBand);
                this.addCountCell(summaryBand);
                x = 40;
            }
            for (ReportColumn reportColumn : reportColumns) {
                if (!reportColumn.isInclude()) continue;
                this.addField(reportColumn);
                this.addHeaderColumn(headerBand, reportColumn, x);
                this.addDetailColumn(detailBand, reportColumn, x);
                this.addSummaryColumn(summaryBand, reportColumn, x);
                x += reportColumn.getDisplayProperties().getWidth() + 0;
            }
        }
    }

    private List getSortedColumns() {
        HashMap columnsMap = new HashMap(this.reportColumns);
        List reportGroupings = this.reportProperties.getReportGroupingsList();
        if (reportGroupings != null) {
            for (ReportGrouping grouping : reportGroupings) {
                columnsMap.remove(grouping.getGroupingField());
            }
        }
        ArrayList columns = new ArrayList(columnsMap.values());
        Collections.sort(columns, new Comparator(){

            public int compare(Object o1, Object o2) {
                ReportColumn col1 = (ReportColumn)o1;
                ReportColumn col2 = (ReportColumn)o2;
                return col1.getOrder() - col2.getOrder();
            }
        });
        return columns;
    }

    private void addIndexHeader(JRDesignBand headerBand) {
        JRDesignTextField textField = this.createTextField("\"\"", this.reportProperties.getThProperties().createLike(new DisplayProperties.Editor(){

            @Override
            public void edit(DisplayProperties displayProperties) {
                displayProperties.setWidth(40);
            }
        }), 0, 5, (BoxCreator)new ColumnHeaderBoxCreator(this.reportProperties.getCellBorders(), PenEnum.ONE_POINT.getValue()), ModeEnum.OPAQUE.getValue());
        headerBand.addElement((JRDesignElement)textField);
    }

    private void addIndexDetail(JRDesignBand detailBand) {
        this.addDetailElement(detailBand, "String.valueOf($V{REPORT_COUNT})", this.reportProperties.getTdDefaultProperties().createLike(new DisplayProperties.Editor(){

            @Override
            public void edit(DisplayProperties displayProperties) {
                displayProperties.setWidth(40);
            }
        }), 0);
    }

    private void addCountCell(JRDesignBand summaryBand) {
        if (this.reportProperties.isShowCount()) {
            String expression = this.getCalculationExpression(CalculationEnum.COUNT.getValue()) + " + \": \" + $V{" + "REPORT_COUNT" + "}";
            DisplayProperties properties = this.reportProperties.getTdDefaultProperties().createLike(new DisplayProperties.Editor(){

                @Override
                public void edit(DisplayProperties displayProperties) {
                    displayProperties.setHeight(60);
                    displayProperties.setWidth(40);
                }
            });
            JRDesignTextField textField = this.createTextField(expression, properties, 0, 0, null, ModeEnum.OPAQUE.getValue());
            summaryBand.addElement((JRDesignElement)textField);
        }
    }

    private void addField(ReportColumn reportColumn) throws JRException {
        JRDesignField result = new JRDesignField();
        result.setName(reportColumn.getName());
        result.setValueClass(reportColumn.getJavaClass());
        this.jasperDesign.addField((JRField)result);
    }

    private void addHeaderColumn(JRDesignBand headerBand, final ReportColumn reportColumn, int x) {
        JRDesignStaticText header = this.createStaticText(reportColumn.getThTitle(), this.reportProperties.getThProperties().createLike(new DisplayProperties.Editor(){

            @Override
            public void edit(DisplayProperties displayProperties) {
                displayProperties.setWidth(reportColumn.getDisplayProperties().getWidth());
            }
        }), x, 5, new ColumnHeaderBoxCreator(this.reportProperties.getCellBorders(), PenEnum.ONE_POINT.getValue()), ModeEnum.OPAQUE.getValue());
        headerBand.addElement((JRDesignElement)header);
    }

    private void addDetailColumn(JRDesignBand detailBand, ReportColumn reportColumn, int x) {
        String expression = "$P{REPORT_SCRIPTLET}.format($F{" + reportColumn.getName() + "})";
        this.addDetailElement(detailBand, expression, reportColumn.getDisplayProperties(), x);
    }

    private String getFormattedExpression(String originalExpression, ReportColumn reportColumn) {
        String formatterMethod = reportColumn.getFormatterMethod();
        if (formatterMethod != null && formatterMethod.length() > 0) {
            originalExpression = formatterMethod + "(" + originalExpression + ")";
        }
        return originalExpression;
    }

    private void addSummaryColumn(JRDesignBand summaryBand, ReportColumn reportColumn, int x) throws JRException {
        if (reportColumn.getAggregator() != 0) {
            this.jasperDesign.addVariable(this.prepareAggregateVariable(reportColumn));
            this.addAggregateElement(summaryBand, reportColumn, x);
        }
    }

    private void addDetailElement(JRDesignBand detailBand, String valueExpression, DisplayProperties displayProperties, int x) {
        JRExpression expression = this.createExpression(valueExpression, String.class);
        DefaultBoxCreator boxCreator = new DefaultBoxCreator(this.reportProperties.getCellBorders(), PenEnum.THIN.getValue());
        displayProperties.setHeight(this.reportProperties.getTdDefaultProperties().getHeight());
        JRDesignTextField odd = this.createTextField(expression, displayProperties, x, 0, (BoxCreator)boxCreator, ModeEnum.OPAQUE.getValue());
        JRDesignTextField even = this.createTextField(expression, displayProperties, x, 0, (BoxCreator)boxCreator, ModeEnum.OPAQUE.getValue());
        odd.setBackcolor(this.decodeColor(this.reportProperties.getOddBackground()));
        even.setBackcolor(this.decodeColor(this.reportProperties.getEvenBackground()));
        JRExpression oddExpression = this.createExpression("$V{REPORT_COUNT}.intValue() % 2 == 1 ? Boolean.TRUE : Boolean.FALSE", Boolean.class);
        JRExpression evenExpression = this.createExpression("$V{REPORT_COUNT}.intValue() % 2 == 0 ? Boolean.TRUE : Boolean.FALSE", Boolean.class);
        odd.setPrintWhenExpression(oddExpression);
        even.setPrintWhenExpression(evenExpression);
        detailBand.addElement((JRDesignElement)odd);
        detailBand.addElement((JRDesignElement)even);
    }

    private JRDesignVariable prepareAggregateVariable(ReportColumn reportColumn) {
        JRDesignVariable result = new JRDesignVariable();
        result.setCalculation(CalculationEnum.getByValue((byte)reportColumn.getAggregator()));
        result.setName(this.getAggregatorName(reportColumn.getName()));
        result.setExpression(this.createExpression("$F{" + reportColumn.getName() + "}", reportColumn.getJavaClass()));
        result.setResetType(ResetTypeEnum.REPORT);
        result.setValueClass(reportColumn.getJavaClass());
        return result;
    }

    private void addAggregateElement(JRDesignBand summaryBand, ReportColumn reportColumn, int x) {
        String expression = this.getCalculationExpression(reportColumn.getAggregator()) + " + \": \" + $V{" + this.getAggregatorName(reportColumn.getName()) + "}";
        DisplayProperties properties = reportColumn.getDisplayProperties().createLike(new DisplayProperties.Editor(){

            @Override
            public void edit(DisplayProperties displayProperties) {
                displayProperties.setHeight(60);
            }
        });
        JRDesignTextField textField = this.createTextField(expression, properties, x, 0, null, ModeEnum.OPAQUE.getValue());
        summaryBand.addElement((JRDesignElement)textField);
    }

    private String getAggregatorName(String columnName) {
        return "AGGREGATE_" + columnName;
    }

    private String getCalculationExpression(byte calculation) {
        String calc = "";
        switch (calculation) {
            case 1: {
                calc = "";
                break;
            }
            case 2: {
                calc = "";
                break;
            }
            case 3: {
                calc = "";
                break;
            }
            case 5: {
                calc = "";
                break;
            }
            case 4: {
                calc = "";
                break;
            }
            case 7: {
                calc = "";
            }
        }
        return "\"" + calc + "\"";
    }

    private void createPageFooter() {
        if (this.reportProperties.getPageNumberBottom() != -1) {
            JRDesignBand pageFooter = this.createBand(25);
            this.jasperDesign.setPageFooter((JRBand)pageFooter);
            this.addFooterPageNumber(pageFooter);
        }
    }

    private JRDesignTextField addFooterPageNumber(JRDesignBand pageFooter) {
        return this.addPageNumber(pageFooter, this.reportProperties.getPageNumberBottom());
    }

    private JRDesignTextField addPageNumber(JRDesignBand band, final byte alignment) {
        if (alignment != -1) {
            JRDesignTextField pageNumber = this.createTextField("\"\" + \" \" + String.valueOf($V{PAGE_NUMBER})", PAGE_NUMBER_PROPERTIES.createLike(new DisplayProperties.Editor(){

                @Override
                public void edit(DisplayProperties displayProperties) {
                    displayProperties.setAlign(alignment);
                    displayProperties.setWidth(JasperDesignCreator.this.pageWidth);
                    displayProperties.setHeight(15);
                }
            }), 0, 0, null, ModeEnum.TRANSPARENT.getValue());
            pageNumber.setMode(ModeEnum.TRANSPARENT);
            band.addElement((JRDesignElement)pageNumber);
            return pageNumber;
        }
        return null;
    }

    private JRExpression createExpression(String text, Class type) {
        JRDesignExpression result = new JRDesignExpression();
        result.setText(text);
        result.setValueClass(type);
        return result;
    }

    private JRDesignBand createBand(int height) {
        JRDesignBand result = new JRDesignBand();
        result.setHeight(height);
        return result;
    }

    private JRDesignStaticText createStaticText(String text, DisplayProperties properties, int x, int y, BoxCreator boxCreator, byte mode) {
        JRDesignStaticText result = new JRDesignStaticText();
        result.setText(text);
        this.applyDisplayProperties((JRDesignTextElement)result, properties, x, y, boxCreator, mode);
        return result;
    }

    private JRDesignTextField createTextField(JRExpression expression, DisplayProperties properties, int x, int y, BoxCreator boxCreator, byte mode) {
        JRDesignTextField result = new JRDesignTextField();
        result.setExpression(expression);
        this.applyDisplayProperties((JRDesignTextElement)result, properties, x, y, boxCreator, mode);
        return result;
    }

    private JRDesignTextField createTextField(String expressionStr, DisplayProperties properties, int x, int y, BoxCreator boxCreator, byte mode) {
        JRExpression expression = this.createExpression(expressionStr, String.class);
        return this.createTextField(expression, properties, x, y, boxCreator, mode);
    }

    private void applyDisplayProperties(JRDesignTextElement textElement, DisplayProperties displayProperties, int x, int y, BoxCreator boxCreator, byte mode) {
        this.setPositions((JRDesignElement)textElement, x, y, displayProperties.getWidth(), displayProperties.getHeight());
        textElement.setHorizontalTextAlign(HorizontalTextAlignEnum.getByName((String)String.valueOf(displayProperties.getAlign())));
        textElement.setVerticalTextAlign(VerticalTextAlignEnum.MIDDLE);
        textElement.setFont(this.createFont(displayProperties));
        textElement.setForecolor(this.decodeColor(displayProperties.getColor()));
        if (textElement instanceof JRDesignTextField) {
            ((JRDesignTextField)textElement).setStretchWithOverflow(true);
            ((JRDesignTextField)textElement).setBlankWhenNull(true);
            ((JRDesignTextField)textElement).setRemoveLineWhenBlank(false);
        }
        textElement.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
        textElement.setMode(ModeEnum.getByValue((byte)mode));
        if (boxCreator != null) {
            JRBoxUtil.copy((JRLineBox)boxCreator.create(), (JRLineBox)textElement.getLineBox());
        }
    }

    private void setPositions(JRDesignElement element, int x, int y, int w, int h) {
        element.setY(y);
        element.setHeight(h);
        element.setWidth(w);
        if (this.reportProperties.isLeftToRight()) {
            element.setX(x);
        } else {
            element.setX(this.pageWidth - (x + w));
        }
    }

    private Color decodeColor(String color) {
        return ColorEnum.getByName((String)color).getColor();
    }

    private JRFont createFont(DisplayProperties displayProperties) {
        JRBaseFont jrStyleContainer = new JRBaseFont();
        JRDesignFont result = new JRDesignFont((JRStyleContainer)jrStyleContainer);
        switch (displayProperties.getDecoration()) {
            case 11: {
                result.setBold(Boolean.valueOf(true));
                result.setItalic(Boolean.valueOf(false));
                break;
            }
            case 12: {
                result.setBold(Boolean.valueOf(false));
                result.setItalic(Boolean.valueOf(true));
                break;
            }
            case 13: {
                result.setBold(Boolean.valueOf(true));
                result.setItalic(Boolean.valueOf(true));
                break;
            }
            default: {
                result.setBold(Boolean.valueOf(false));
                result.setItalic(Boolean.valueOf(false));
            }
        }
        return result;
    }

    private ReportColumn getReportColumn(String name) {
        return (ReportColumn)this.reportColumns.get(name);
    }

    static {
        PAGE_NUMBER_PROPERTIES.setColor("black");
        PAGE_NUMBER_PROPERTIES.setDecoration((byte)10);
        PAGE_NUMBER_PROPERTIES.setFontFace(PAGE_NUMBER_FONT_FACE);
        PAGE_NUMBER_PROPERTIES.setFontSize(10);
        PAGE_NUMBER_PROPERTIES.setHeight(15);
    }

    private static class DefaultBoxCreator
    implements BoxCreator {
        private byte borders;
        private byte pen;

        public DefaultBoxCreator(byte borders, byte pen) {
            this.borders = borders;
            this.pen = pen;
        }

        @Override
        public JRLineBox create() {
            if (20 == this.borders) {
                return null;
            }
            JRBaseStyle boxContainer = new JRBaseStyle();
            JRBaseLineBox box = new JRBaseLineBox((JRBoxContainer)boxContainer);
            box.getStyle().getLineBox().getPen().setLineColor(Color.BLACK);
            switch (this.borders) {
                case 21: {
                    box.getStyle().getLineBox().getTopPen().setLineWidth(Float.valueOf(this.pen));
                    break;
                }
                case 22: {
                    box.getStyle().getLineBox().getLeftPen().setLineWidth(Float.valueOf(this.pen));
                    box.getStyle().getLineBox().getRightPen().setLineWidth(Float.valueOf(this.pen));
                    break;
                }
                case 23: {
                    box.getStyle().getLineBox().getPen().setLineWidth(Float.valueOf(this.pen));
                }
            }
            return box;
        }
    }

    private static class ColumnHeaderBoxCreator
    implements BoxCreator {
        private byte borders;
        private byte pen;

        public ColumnHeaderBoxCreator(byte borders, byte pen) {
            this.borders = borders;
            this.pen = pen;
        }

        @Override
        public JRLineBox create() {
            JRBaseStyle boxContainer = new JRBaseStyle();
            JRBaseLineBox box = new JRBaseLineBox((JRBoxContainer)boxContainer);
            switch (this.borders) {
                case 20: {
                    box.getStyle().getLineBox().getBottomPen().setLineWidth(Float.valueOf(this.pen));
                    break;
                }
                case 21: {
                    box.getStyle().getLineBox().getBottomPen().setLineWidth(Float.valueOf(this.pen));
                    box.getStyle().getLineBox().getTopPen().setLineWidth(Float.valueOf(this.pen));
                    break;
                }
                case 22: {
                    box.getStyle().getLineBox().getLeftPen().setLineWidth(Float.valueOf(this.pen));
                    box.getStyle().getLineBox().getRightPen().setLineWidth(Float.valueOf(this.pen));
                    box.getStyle().getLineBox().getBottomPen().setLineWidth(Float.valueOf(this.pen));
                    break;
                }
                case 23: {
                    box.getStyle().getLineBox().getPen().setLineWidth(Float.valueOf(this.pen));
                }
            }
            return box;
        }
    }

    private static interface BoxCreator {
        public JRLineBox create();
    }
}

