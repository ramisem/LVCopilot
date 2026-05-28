/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRBand
 *  net.sf.jasperreports.engine.JRElement
 *  net.sf.jasperreports.engine.JRExpression
 *  net.sf.jasperreports.engine.JRField
 *  net.sf.jasperreports.engine.JRGroup
 *  net.sf.jasperreports.engine.JRSection
 *  net.sf.jasperreports.engine.design.JRDesignBand
 *  net.sf.jasperreports.engine.design.JRDesignElement
 *  net.sf.jasperreports.engine.design.JRDesignExpression
 *  net.sf.jasperreports.engine.design.JRDesignField
 *  net.sf.jasperreports.engine.design.JRDesignGroup
 *  net.sf.jasperreports.engine.design.JRDesignRectangle
 *  net.sf.jasperreports.engine.design.JRDesignSection
 *  net.sf.jasperreports.engine.design.JRDesignStaticText
 *  net.sf.jasperreports.engine.design.JRDesignTextField
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.type.SplitTypeEnum
 *  net.sf.jasperreports.engine.type.StretchTypeEnum
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.report.jasper.DisplayConstants;
import com.labvantage.sapphire.report.jasper.DisplayProperties;
import com.labvantage.sapphire.report.jasper.JasperDesignCreator;
import com.labvantage.sapphire.report.jasper.ReportColumn;
import com.labvantage.sapphire.report.jasper.ReportDesigner;
import com.labvantage.sapphire.report.jasper.ReportGrouping;
import com.labvantage.sapphire.report.jasper.SimpleReportProperties;
import java.awt.Color;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignGroup;
import net.sf.jasperreports.engine.design.JRDesignRectangle;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.SplitTypeEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ListReportDesigner
extends ReportDesigner {
    static boolean hasUnicodeFont = false;

    public ListReportDesigner(String title, PropertyList element) {
        super(title, element);
    }

    public ListReportDesigner(String title, PropertyList element, JasperDesign jasperDesign) {
        super(title, element, jasperDesign);
    }

    @Override
    public JasperDesign createJasperDesign() {
        if ("xls".equals(this.displayType) || "xlsx".equals(this.displayType) || "csv".equals(this.displayType)) {
            this.jasperDesign.setTitle((JRBand)new JRDesignBand());
            String name = this.jasperDesign.getName();
            name = StringUtil.replaceAll(name, "/", " ");
            name = StringUtil.replaceAll(name, "\\", " ");
            name = StringUtil.replaceAll(name, "*", " ");
            name = StringUtil.replaceAll(name, "?", " ");
            name = StringUtil.replaceAll(name, "[", " ");
            name = StringUtil.replaceAll(name, "]", " ");
            this.jasperDesign.setName(name);
            return this.createJasperDesignExcel();
        }
        return this.createJasperDesignDefault();
    }

    @Override
    public JasperDesign modifyJasperDesign() {
        PropertyListCollection columns;
        JRDesignBand columnheader;
        JRDesignStaticText colheaderTemp;
        JRGroup[] groups = this.jasperDesign.getGroups();
        JRSection groupheaderSection = groups[0].getGroupHeaderSection();
        JRElement groupheaderTitle = groupheaderSection.getBands()[0].getElementByKey("staticText");
        PropertyListCollection groupbys = this.element.getCollection("groupby");
        String groupbyfieldid = "";
        JRDesignField groupbyfield = null;
        boolean hasNoGroup = true;
        if (groupbys != null && groupbys.size() > 0) {
            ((JRDesignStaticText)groupheaderTitle).setText(groupbys.getPropertyList(0).getProperty("title"));
            JRElement groupheaderField = groupheaderSection.getBands()[0].getElementByKey("textField");
            groupbyfieldid = groupbys.getPropertyList(0).getProperty("columnid");
            if (groupbyfieldid.length() > 0) {
                groupbyfield = new JRDesignField();
                groupbyfield.setName(groupbyfieldid);
                try {
                    this.jasperDesign.addField((JRField)groupbyfield);
                    hasNoGroup = false;
                }
                catch (Exception e) {
                    Logger.logStackTrace(e);
                }
                JRDesignExpression exp = (JRDesignExpression)((JRDesignTextField)groupheaderField).getExpression();
                exp.setText("$P{REPORT_SCRIPTLET}.format( $F{" + groupbyfieldid.toLowerCase() + "})");
                ((JRDesignGroup)groups[0]).setExpression((JRExpression)exp);
            }
        }
        if (hasNoGroup) {
            this.jasperDesign.removeGroup(groups[0]);
        }
        if ((colheaderTemp = (JRDesignStaticText)(columnheader = (JRDesignBand)groups[1].getGroupHeaderSection().getBands()[0]).getElementByKey("element-90")) != null) {
            columnheader.removeElement((JRDesignElement)colheaderTemp);
        }
        JRDesignBand detail = (JRDesignBand)this.jasperDesign.getDetailSection().getBands()[0];
        JRDesignTextField stringField = (JRDesignTextField)detail.getElementByKey("StringDetailField");
        JRDesignTextField numberField = (JRDesignTextField)detail.getElementByKey("NumericDetailField");
        JRDesignTextField dateField = (JRDesignTextField)detail.getElementByKey("DateDetailField");
        if (stringField != null) {
            detail.removeElement((JRDesignElement)stringField);
        }
        if (numberField != null) {
            detail.removeElement((JRDesignElement)numberField);
        }
        if (dateField != null) {
            detail.removeElement((JRDesignElement)dateField);
        }
        float fontSize = (columns = this.element.getCollection("columns")).size() >= 10 ? 8.0f : (columns.size() > 6 ? 10.0f : colheaderTemp.getFontsize());
        int maxHeaderChars = this.getMaxHeaderChars(columns);
        int headHeight = columns.size() >= 6 ? (maxHeaderChars > 60 ? 5 : (maxHeaderChars > 45 ? 4 : (maxHeaderChars > 30 ? 3 : (maxHeaderChars > 10 ? 2 : 1)))) * colheaderTemp.getHeight() : (maxHeaderChars > 60 ? 4 : (maxHeaderChars > 45 ? 3 : (maxHeaderChars > 30 ? 2 : 1))) * colheaderTemp.getHeight();
        columnheader.setHeight(headHeight + 1);
        ((JRDesignRectangle)columnheader.getElementByKey("element-22")).setHeight(headHeight);
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            if (columnid.indexOf(";") < 0) {
                columnid = RequestParser.parseAlias(column.getProperty("columnid"));
                columnid = columnid.toLowerCase();
            }
            String mode = column.getProperty("mode");
            String format = column.getProperty("format");
            String title = column.getProperty("title");
            if ("Do Not Retrieve".equals(mode) || "Hidden Value".equals(mode) || columnid.trim().length() <= 0 || title.trim().length() <= 1) continue;
            JRDesignStaticText fieldTitle = new JRDesignStaticText();
            title = ListReportDesigner.replaceHtml(title);
            fieldTitle.setText(title);
            fieldTitle.setKey(column.getProperty("title"));
            fieldTitle.setBold(Boolean.valueOf(colheaderTemp.isBold()));
            fieldTitle.getLineBox().getPen().setLineWidth(colheaderTemp.getLineBox().getPen().getLineWidth());
            fieldTitle.getLineBox().getPen().setLineColor(colheaderTemp.getLineBox().getPen().getLineColor());
            fieldTitle.setBackcolor(colheaderTemp.getBackcolor());
            fieldTitle.setFontName(colheaderTemp.getFontName());
            fieldTitle.setFontSize(Float.valueOf(fontSize));
            fieldTitle.setForecolor(colheaderTemp.getForecolor());
            fieldTitle.setHeight(headHeight);
            if (hasUnicodeFont) {
                fieldTitle.setPdfEmbedded(Boolean.valueOf(true));
                fieldTitle.setPdfEncoding("Identity-H");
                fieldTitle.setPdfFontName("ARIALUNI.TTF");
            }
            columnheader.addElement((JRDesignElement)fieldTitle);
            int coltype = this.exampleData == null ? 0 : this.exampleData.getColumnType(columnid);
            try {
                JRDesignField field = null;
                if (columnid != null && !columnid.equals(groupbyfieldid)) {
                    field = new JRDesignField();
                    field.setName(columnid);
                    try {
                        this.jasperDesign.addField((JRField)field);
                    }
                    catch (Exception exception) {}
                } else {
                    field = groupbyfield;
                }
                JRDesignTextField fieldValue = new JRDesignTextField();
                JRDesignExpression expression = new JRDesignExpression();
                expression.setValueClass(String.class);
                fieldValue.setKey(columnid);
                fieldValue.setExpression((JRExpression)expression);
                fieldValue.setStretchWithOverflow(true);
                fieldValue.setStretchType(StretchTypeEnum.RELATIVE_TO_BAND_HEIGHT);
                if (hasUnicodeFont) {
                    fieldValue.setPdfEmbedded(Boolean.valueOf(true));
                    fieldValue.setPdfEncoding("Identity-H");
                    fieldValue.setPdfFontName("ARIALUNI.TTF");
                }
                switch (coltype) {
                    case 0: {
                        if ("Y".equals(column.getProperty("translatevalue"))) {
                            expression.setText("$P{REPORT_SCRIPTLET}.translate( $F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "} )");
                        } else {
                            expression.setText("$F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "}");
                        }
                        field.setValueClass(String.class);
                        ListReportDesigner.addTextFieldToDetail(detail, fieldValue, stringField, fontSize);
                        break;
                    }
                    case 2: {
                        field.setValueClass(Calendar.class);
                        if (format.length() > 0) {
                            expression.setText("com.labvantage.sapphire.pageelements.ElementUtil.getDateFormat( $P{REPORT_SCRIPTLET}.getConnectionInfo(), \"" + format + "\", " + (format.indexOf(" ") > 0 ? "true" : "false") + " ).format( $F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "}.getTime() )");
                        } else {
                            expression.setText("$P{REPORT_SCRIPTLET}.format( $F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "} )");
                        }
                        ListReportDesigner.addTextFieldToDetail(detail, fieldValue, dateField, fontSize);
                        break;
                    }
                    case 1: {
                        field.setValueClass(BigDecimal.class);
                        expression.setText("$P{REPORT_SCRIPTLET}.format( $F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "} )");
                        ListReportDesigner.addTextFieldToDetail(detail, fieldValue, numberField, fontSize);
                        break;
                    }
                }
                continue;
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
            }
        }
        ListReportDesigner.setListFieldLayout(detail.getElements(), columnheader.getElements(), this.jasperDesign.getPageWidth());
        return this.jasperDesign;
    }

    private static void addTextFieldToDetail(JRDesignBand detail, JRDesignTextField fieldValue, JRDesignTextField templateField, float fontSize) {
        fieldValue.getLineBox().getPen().setLineWidth(templateField.getLineBox().getPen().getLineWidth());
        fieldValue.getLineBox().getPen().setLineColor(templateField.getLineBox().getPen().getLineColor());
        fieldValue.setBold(Boolean.valueOf(templateField.isBold()));
        fieldValue.setBackcolor(templateField.getBackcolor());
        fieldValue.setFontName(templateField.getFontName());
        fieldValue.setFontSize(Float.valueOf(fontSize));
        fieldValue.setForecolor(templateField.getForecolor());
        fieldValue.setHeight(templateField.getHeight());
        fieldValue.setBlankWhenNull(templateField.isBlankWhenNull());
        detail.addElement((JRDesignElement)fieldValue);
    }

    private JasperDesign createJasperDesignExcel() {
        this.jasperDesign.setIgnorePagination(true);
        this.jasperDesign.setColumnSpacing(10);
        this.jasperDesign.setFloatColumnFooter(false);
        this.jasperDesign.setLeftMargin(0);
        this.jasperDesign.setTopMargin(0);
        this.jasperDesign.setPageFooter(null);
        this.jasperDesign.setColumnFooter(null);
        this.jasperDesign.setBottomMargin(0);
        this.jasperDesign.setRightMargin(0);
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(15);
        detailBand.setSplitType(SplitTypeEnum.PREVENT);
        ((JRDesignSection)this.jasperDesign.getDetailSection()).addBand((JRBand)detailBand);
        JRDesignBand headerBand = new JRDesignBand();
        this.jasperDesign.setColumnHeader((JRBand)headerBand);
        PropertyListCollection columns = this.element.getCollection("columns");
        int fieldx = 0;
        int fieldwidth = 150;
        int actualcols = 0;
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            if (columnid.indexOf(";") < 0 || columnid.indexOf("(") >= 0 && columnid.indexOf(")") > 0) {
                columnid = RequestParser.parseAlias(column.getProperty("columnid"));
                columnid = columnid.toLowerCase();
            }
            String mode = column.getProperty("mode");
            String coltitle = column.getProperty("title");
            if ("Do Not Retrieve".equals(mode) || "Hidden Value".equals(mode) || columnid.trim().length() <= 0 || coltitle.trim().length() <= 1) continue;
            ++actualcols;
        }
        if (actualcols == 1) {
            fieldwidth = 600;
        } else if (actualcols == 2) {
            fieldwidth = 300;
        } else if (actualcols == 3) {
            fieldwidth = 200;
        }
        int headHeight = (this.getMaxHeaderChars(columns) * 7 / fieldwidth + 1) * 15;
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            String format = column.getProperty("format");
            if (columnid.indexOf(";") < 0 || columnid.indexOf("sdidataitem[") < 0 && columnid.indexOf("(") >= 0 && columnid.indexOf(")") > 0) {
                columnid = RequestParser.parseAlias(column.getProperty("columnid"));
                columnid = columnid.toLowerCase();
            }
            String mode = column.getProperty("mode");
            String coltitle = column.getProperty("title");
            if ("Do Not Retrieve".equals(mode) || "Hidden Value".equals(mode) || columnid.trim().length() <= 0 || coltitle.trim().length() <= 1) continue;
            int cellWidth = fieldwidth;
            if (i > 0) {
                fieldx += cellWidth;
            }
            String title = column.getProperty("title", columnid);
            JRDesignField field = new JRDesignField();
            field.setName(columnid);
            try {
                this.jasperDesign.addField((JRField)field);
            }
            catch (Exception exception) {
                // empty catch block
            }
            int datatype = this.exampleData == null ? 0 : this.exampleData.getColumnType(columnid);
            JRDesignExpression exp = new JRDesignExpression();
            exp.setValueClass(String.class);
            if (datatype == 0 && !"Y".equals(column.getProperty("translatevalue"))) {
                exp.setText("$F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "}");
            } else if (datatype == 2 && format.length() > 0) {
                exp.setText("com.labvantage.sapphire.pageelements.ElementUtil.getDateFormat( $P{REPORT_SCRIPTLET}.getConnectionInfo(), \"" + format + "\", " + (format.indexOf(" ") > 0 ? "true" : "false") + " ).format( $F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "}.getTime() )");
            } else {
                exp.setText("$P{REPORT_SCRIPTLET}.format( $F{" + (columnid.indexOf(";") < 0 ? columnid.toLowerCase() : columnid) + "} )");
            }
            if (datatype == 2) {
                field.setValueClass(Calendar.class);
            } else if (datatype == 1) {
                field.setValueClass(BigDecimal.class);
            }
            JRDesignStaticText fieldTitle = new JRDesignStaticText();
            fieldTitle.setX(fieldx);
            fieldTitle.setY(0);
            fieldTitle.setHeight(headHeight);
            headerBand.setHeight(headHeight);
            fieldTitle.setWidth(cellWidth);
            fieldTitle.setPrintWhenDetailOverflows(true);
            title = ListReportDesigner.replaceHtml(title);
            fieldTitle.setText(title);
            fieldTitle.setBold(Boolean.valueOf(true));
            fieldTitle.getLineBox().getPen().setLineWidth(Float.valueOf(0.5f));
            fieldTitle.getLineBox().getPen().setLineColor(Color.lightGray);
            headerBand.addElement((JRDesignElement)fieldTitle);
            JRDesignTextField fieldValue = new JRDesignTextField();
            fieldValue.setExpression((JRExpression)exp);
            fieldValue.setX(fieldx);
            fieldValue.setY(0);
            fieldValue.setHeight(15);
            fieldValue.setWidth(cellWidth);
            fieldValue.getLineBox().getPen().setLineWidth(Float.valueOf(0.5f));
            fieldValue.getLineBox().getPen().setLineColor(Color.lightGray);
            fieldValue.setStretchWithOverflow(true);
            fieldValue.setBlankWhenNull(true);
            fieldValue.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
            detailBand.addElement((JRDesignElement)fieldValue);
        }
        this.jasperDesign.setPageWidth(actualcols * fieldwidth);
        return this.jasperDesign;
    }

    private JasperDesign createJasperDesignDefault() {
        String sdcid = this.element.getProperty("sdcid");
        ReportColumn[] reportcolumns = new ReportColumn[7];
        PropertyListCollection columns = this.element.getCollection("columns");
        int count = 0;
        for (int i = 0; i < columns.size() && count < 7; ++i) {
            PropertyList column = columns.getPropertyList(i);
            String columnid = column.getProperty("columnid");
            String mode = column.getProperty("mode");
            if ("Hidden Value".equals(mode)) continue;
            ReportColumn reportcol = new ReportColumn();
            reportcol.setName(columnid);
            reportcol.setThTitle(column.getProperty("title"));
            int coltype = this.exampleData == null ? 0 : this.exampleData.getColumnType(columnid);
            switch (coltype) {
                case 0: {
                    reportcol.setJavaClass(String.class);
                    break;
                }
                case 2: {
                    reportcol.setJavaClass(Calendar.class);
                    break;
                }
                case 1: {
                    reportcol.setJavaClass(BigDecimal.class);
                    break;
                }
            }
            reportcol.setDisplayProperties(DisplayProperties.createDefault());
            reportcolumns[count] = reportcol;
            ++count;
        }
        SimpleReportProperties srprops = new SimpleReportProperties();
        srprops.setReportColumns(reportcolumns);
        srprops.setTitle(sdcid + " List");
        srprops.setPaperSize("a4");
        srprops.setLeftToRight(true);
        srprops.setShowCount(false);
        PropertyListCollection groupbys = this.element.getCollection("groupby");
        ReportGrouping rg = new ReportGrouping();
        rg.setGroupingField("batchid");
        rg.setGroupingFieldDesc("Batch:");
        DisplayProperties titleDisplay = DisplayProperties.createDefault();
        titleDisplay.setAlign(DisplayConstants.ALIGN_LEFT);
        rg.setTitleDisplayProperties(titleDisplay);
        rg.setTitleExpression("Batch: #");
        srprops.addReportGrouping(rg);
        srprops.setShowCount(false);
        srprops.setIndexColumn(false);
        JasperDesignCreator jasperDesignCreator = new JasperDesignCreator(srprops);
        try {
            jasperDesignCreator.validate();
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        JasperDesign jd = jasperDesignCreator.getJasperDesign();
        jd.setScriptletClass("sapphire.report.JasperReportScriptlet");
        jd.setTitle((JRBand)this.getTitleBand(this.title));
        JRDesignBand emptyBand = new JRDesignBand();
        emptyBand.setHeight(0);
        jd.setPageHeader((JRBand)emptyBand);
        jd.setColumnFooter((JRBand)emptyBand);
        jd.setSummary((JRBand)emptyBand);
        return jd;
    }

    public static void setListFieldLayout(JRElement[] textfields, JRElement[] titlefields, int pageWidth) {
        int i;
        int cols = textfields.length;
        int fieldWidth = pageWidth / cols;
        int fnum = 0;
        for (i = 0; i < textfields.length; ++i) {
            if (!(textfields[i] instanceof JRDesignTextField)) continue;
            JRDesignTextField fieldValue = (JRDesignTextField)textfields[i];
            fieldValue.setX(fnum * fieldWidth);
            fieldValue.setY(0);
            fieldValue.setWidth(fieldWidth);
            ++fnum;
        }
        fnum = 0;
        for (i = 0; i < titlefields.length; ++i) {
            if (!(titlefields[i] instanceof JRDesignStaticText)) continue;
            JRDesignStaticText fieldTitle = (JRDesignStaticText)titlefields[i];
            fieldTitle.setX(fnum * fieldWidth);
            fieldTitle.setY(0);
            fieldTitle.setWidth(fieldWidth);
            ++fnum;
        }
    }

    private static String replaceHtml(String title) {
        if (title.indexOf("<") >= 0 || title.indexOf("&nbsp;") >= 0) {
            title = StringUtil.replaceAll(title, "<br>", "");
            title = StringUtil.replaceAll(title, "&nbsp;", "");
            title = StringUtil.replaceAll(title, "<br/>", "");
            title = StringUtil.replaceAll(title, "<b>", "");
            title = StringUtil.replaceAll(title, "</b>", "");
        }
        return title;
    }

    private int getMaxHeaderChars(PropertyListCollection columns) {
        int maxcolumnidChars = 0;
        for (int i = 0; i < columns.size(); ++i) {
            if (columns.getPropertyList(i) == null || columns.getPropertyList(i).getProperty("title").length() <= maxcolumnidChars) continue;
            maxcolumnidChars = columns.getPropertyList(i).getProperty("title").length();
        }
        return maxcolumnidChars;
    }

    static {
        try {
            InputStream in = ListReportDesigner.class.getResourceAsStream("/ARIALUNI.TTF");
            if (in != null) {
                hasUnicodeFont = true;
            } else {
                Logger.logInfo("ARIALUNI.TTF not found in classpath. Export to PDF will not support unicode");
            }
        }
        catch (Exception e) {
            Logger.logInfo("ARIALUNI.TTF not found in classpath. Export to PDF will not support unicode:" + e.getMessage());
        }
    }
}

