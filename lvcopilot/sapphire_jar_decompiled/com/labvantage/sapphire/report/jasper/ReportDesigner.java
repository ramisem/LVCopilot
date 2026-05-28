/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRBand
 *  net.sf.jasperreports.engine.JRElement
 *  net.sf.jasperreports.engine.JRExpression
 *  net.sf.jasperreports.engine.JRField
 *  net.sf.jasperreports.engine.design.JRDesignBand
 *  net.sf.jasperreports.engine.design.JRDesignElement
 *  net.sf.jasperreports.engine.design.JRDesignExpression
 *  net.sf.jasperreports.engine.design.JRDesignField
 *  net.sf.jasperreports.engine.design.JRDesignSection
 *  net.sf.jasperreports.engine.design.JRDesignStaticText
 *  net.sf.jasperreports.engine.design.JRDesignTextField
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.type.HorizontalTextAlignEnum
 *  net.sf.jasperreports.engine.type.PenEnum
 *  net.sf.jasperreports.engine.type.StretchTypeEnum
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.ReportConstants;
import java.awt.Color;
import java.math.BigDecimal;
import java.util.Calendar;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignSection;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.type.PenEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class ReportDesigner
implements ReportConstants {
    PropertyList element = null;
    String title = "Sapphire Generated Jasper Report";
    JasperDesign jasperDesign = null;
    DataSet exampleData = null;
    String displayType = "pdf";
    public static final String PAGE_TITLE_KEY = "PageTitle";
    public static final String G1_LABEL = "G1Label";

    public String getDisplayType() {
        return this.displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public ReportDesigner(String title, PropertyList element) {
        this.element = element;
        this.title = title;
        this.initDesign();
    }

    public ReportDesigner(String title, PropertyList element, JasperDesign jasperDesign) {
        this.element = element;
        this.title = title;
        jasperDesign.setScriptletClass("sapphire.report.JasperReportScriptlet");
        JRElement el = jasperDesign.getPageHeader().getElementByKey(PAGE_TITLE_KEY);
        ((JRDesignStaticText)el).setText(title);
        this.jasperDesign = jasperDesign;
    }

    public DataSet getExampleData() {
        return this.exampleData;
    }

    public void setExampleData(DataSet exampleData) {
        this.exampleData = exampleData;
    }

    private void initDesign() {
        if (this.jasperDesign == null) {
            this.jasperDesign = new JasperDesign();
            this.jasperDesign.setName(this.title);
            this.jasperDesign.setScriptletClass("sapphire.report.JasperReportScriptlet");
            this.jasperDesign.setTitle((JRBand)this.getTitleBand(this.title));
        }
    }

    public abstract JasperDesign createJasperDesign();

    public abstract JasperDesign modifyJasperDesign();

    public JasperDesign getJasperDesign(PropertyList element, DataSet ds, int type) {
        JasperDesign jasperDesign = null;
        try {
            jasperDesign = new JasperDesign();
            jasperDesign.setName("BasicReport");
            jasperDesign.setScriptletClass("sapphire.report.JasperReportScriptlet");
            jasperDesign.setTitle((JRBand)this.getTitleBand(element.getProperty("sdcid") + (type == 0 ? " List" : " Form")));
            JRDesignBand detailBand = new JRDesignBand();
            detailBand.setHeight(20);
            ((JRDesignSection)jasperDesign.getDetailSection()).addBand((JRBand)detailBand);
            JRDesignBand headerBand = new JRDesignBand();
            headerBand.setHeight(20);
            jasperDesign.setColumnHeader((JRBand)headerBand);
            PropertyListCollection columns = element.getCollection("columns");
            int colcount = columns.size();
            if (0 == type) {
                jasperDesign.setColumnSpacing(10);
                jasperDesign.setFloatColumnFooter(false);
                jasperDesign.setLeftMargin(0);
                jasperDesign.setTopMargin(0);
                jasperDesign.setPageFooter(null);
                jasperDesign.setColumnFooter(null);
                jasperDesign.setBottomMargin(0);
                jasperDesign.setRightMargin(0);
            }
            int count = 0;
            for (int i = 0; i < colcount && count < 6; ++i) {
                PropertyList column = columns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                String mode = column.getProperty("mode");
                if ("Hidden Value".equals(mode)) continue;
                ++count;
                int columntype = ds.getColumnType(columnid);
                if (0 == type) {
                    this.addJasperColumn(column, jasperDesign, columntype);
                    continue;
                }
                ReportDesigner.addJasperFormColumn(column, jasperDesign, columntype);
            }
            if (0 == type) {
                ReportDesigner.setListFieldLayout(jasperDesign, element);
            } else {
                ReportDesigner.setFormFieldLayout(jasperDesign, element);
            }
            jasperDesign.setPageFooter((JRBand)this.getFooterBand());
            JRDesignBand emptyBand = new JRDesignBand();
            emptyBand.setHeight(0);
            jasperDesign.setPageHeader((JRBand)emptyBand);
            jasperDesign.setColumnFooter((JRBand)emptyBand);
            jasperDesign.setSummary((JRBand)emptyBand);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return jasperDesign;
    }

    protected void addJasperColumn(PropertyList column, JasperDesign jasperDesign, int columntype) {
        String columnid = column.getProperty("columnid");
        String title = column.getProperty("title", columnid);
        try {
            JRDesignField field = null;
            field = new JRDesignField();
            field.setName(columnid);
            jasperDesign.addField((JRField)field);
            JRDesignTextField fieldValue = new JRDesignTextField();
            JRDesignExpression exp = new JRDesignExpression();
            exp.setValueClass(String.class);
            if (columntype == 2) {
                field.setValueClass(Calendar.class);
                exp.setText("$P{REPORT_SCRIPTLET}.formatDate( $F{" + columnid + "} )");
            } else if (columntype == 1) {
                field.setValueClass(BigDecimal.class);
                exp.setText("$P{REPORT_SCRIPTLET}.formatNumber( $F{" + columnid + "} )");
            } else if (columntype == 0) {
                exp.setText("$P{REPORT_SCRIPTLET}.translate( $F{" + columnid + "} )");
            } else {
                field.setValueClass(String.class);
                exp.addFieldChunk(columnid);
            }
            fieldValue.setExpression((JRExpression)exp);
            fieldValue.setBlankWhenNull(true);
            fieldValue.setStretchWithOverflow(true);
            fieldValue.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
            fieldValue.getLineBox().getPen().setLineWidth(Float.valueOf(PenEnum.THIN.getValue()));
            fieldValue.getLineBox().getPen().setLineColor(Color.lightGray);
            JRDesignStaticText fieldTitle = new JRDesignStaticText();
            fieldTitle.setText(title);
            fieldTitle.setBold(Boolean.valueOf(true));
            fieldTitle.getLineBox().getPen().setLineWidth(Float.valueOf(PenEnum.THIN.getValue()));
            fieldTitle.getLineBox().getPen().setLineColor(Color.lightGray);
            fieldTitle.setBackcolor(Color.gray);
            ((JRDesignBand)jasperDesign.getColumnHeader()).addElement((JRDesignElement)fieldTitle);
            ((JRDesignBand)jasperDesign.getDetailSection()).addElement((JRDesignElement)fieldValue);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    private static void addJasperFormColumn(PropertyList column, JasperDesign jasperDesign, int columntype) {
        String columnid = column.getProperty("columnid");
        String title = column.getProperty("title", columnid);
        try {
            JRDesignField field = null;
            field = new JRDesignField();
            field.setName(columnid);
            jasperDesign.addField((JRField)field);
            JRDesignTextField fieldValue = new JRDesignTextField();
            JRDesignExpression exp = new JRDesignExpression();
            exp.setValueClass(String.class);
            if (columntype == 2) {
                field.setValueClass(Calendar.class);
                exp.setText("$P{REPORT_SCRIPTLET}.formatDate( $F{" + columnid + "} )");
            } else if (columntype == 1) {
                field.setValueClass(BigDecimal.class);
                exp.setText("$P{REPORT_SCRIPTLET}.formatNumber( $F{" + columnid + "} )");
            } else if (columntype == 0 && "Y".equals(column.getProperty("translatevalue"))) {
                exp.setText("$P{REPORT_SCRIPTLET}.translate( $F{" + columnid + "} )");
            } else {
                field.setValueClass(String.class);
                exp.addFieldChunk(columnid);
            }
            fieldValue.setExpression((JRExpression)exp);
            fieldValue.setStretchWithOverflow(true);
            fieldValue.setStretchType(StretchTypeEnum.RELATIVE_TO_TALLEST_OBJECT);
            fieldValue.getLineBox().getPen().setLineWidth(Float.valueOf(PenEnum.THIN.getValue()));
            fieldValue.getLineBox().getPen().setLineColor(Color.lightGray);
            JRDesignStaticText fieldTitle = new JRDesignStaticText();
            fieldTitle.setText(title);
            fieldTitle.setBold(Boolean.valueOf(true));
            fieldTitle.getLineBox().getPen().setLineWidth(Float.valueOf(PenEnum.THIN.getValue()));
            fieldTitle.getLineBox().getPen().setLineColor(Color.lightGray);
            fieldTitle.setBackcolor(Color.gray);
            ((JRDesignBand)jasperDesign.getDetailSection()).addElement((JRDesignElement)fieldTitle);
            ((JRDesignBand)jasperDesign.getDetailSection()).addElement((JRDesignElement)fieldValue);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    public static void setListFieldLayout(JasperDesign jasperDesign, PropertyList element) {
        int i;
        JRElement[] textfields = ((JRDesignBand)jasperDesign.getDetailSection()).getElements();
        JRElement[] titlefields = ((JRDesignBand)jasperDesign.getColumnHeader()).getElements();
        int cols = element.getCollection("columns").size();
        int fieldWidth = jasperDesign.getPageWidth() / cols;
        fieldWidth = 90;
        for (i = 0; i < textfields.length; ++i) {
            if (!(textfields[i] instanceof JRDesignTextField)) continue;
            JRDesignTextField fieldValue = (JRDesignTextField)textfields[i];
            fieldValue.setX(i * fieldWidth);
            fieldValue.setY(0);
            fieldValue.setHeight(20);
            fieldValue.setWidth(fieldWidth);
        }
        for (i = 0; i < titlefields.length; ++i) {
            if (!(textfields[i] instanceof JRDesignStaticText)) continue;
            JRDesignStaticText fieldTitle = (JRDesignStaticText)titlefields[i];
            fieldTitle.setX(i * fieldWidth);
            fieldTitle.setY(0);
            fieldTitle.setHeight(20);
            fieldTitle.setWidth(fieldWidth);
        }
    }

    public static void setFormFieldLayout(JasperDesign jasperDesign, PropertyList element) {
        ((JRDesignBand)jasperDesign.getDetailSection()).setHeight(660);
        JRElement[] textfields = ((JRDesignBand)jasperDesign.getDetailSection()).getElements();
        int titleWidth = 100;
        int fieldWidth = 120;
        int xoffset = 0;
        int yoffset = 0;
        for (int i = 0; i < textfields.length; ++i) {
            if (i % 4 == 0) {
                xoffset = 0;
            }
            if (i % 4 == 1) {
                xoffset = 100;
            }
            if (i % 4 == 2) {
                xoffset = 220;
            }
            if (i % 4 == 3) {
                xoffset = 320;
            }
            try {
                JRDesignStaticText fieldTitle = (JRDesignStaticText)textfields[i];
                fieldTitle.setX(xoffset);
                ++xoffset;
                fieldTitle.setY(yoffset * 20);
                fieldTitle.setHeight(20);
                fieldTitle.setWidth(titleWidth);
            }
            catch (Exception fieldTitle) {
                // empty catch block
            }
            try {
                JRDesignTextField fieldValue = (JRDesignTextField)textfields[i];
                fieldValue.setX(xoffset);
                fieldValue.setY(yoffset * 20);
                fieldValue.setHeight(20);
                fieldValue.setWidth(fieldWidth);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if ((i + 1) % 4 != 0) continue;
            ++yoffset;
        }
    }

    protected JRDesignBand getTitleBand(String titletext) {
        JRDesignStaticText title = new JRDesignStaticText();
        JRDesignBand titleBand = new JRDesignBand();
        titleBand.setHeight(50);
        titleBand.addElement((JRDesignElement)title);
        title.setX(0);
        title.setY(0);
        title.setWidth(600);
        title.setHeight(50);
        title.setText(titletext);
        title.setHorizontalTextAlign(HorizontalTextAlignEnum.CENTER);
        title.setFontSize(Float.valueOf(16.0f));
        title.setBold(Boolean.valueOf(true));
        return titleBand;
    }

    protected JRDesignBand getFooterBand() {
        JRDesignBand pageFooter = new JRDesignBand();
        pageFooter.setHeight(15);
        JRDesignStaticText pageLabel = new JRDesignStaticText();
        pageLabel.setX(0);
        pageLabel.setY(0);
        pageLabel.setHeight(15);
        pageLabel.setWidth(40);
        pageLabel.setText("Page:");
        pageFooter.addElement((JRDesignElement)pageLabel);
        JRDesignExpression exp = new JRDesignExpression();
        exp.addVariableChunk("PAGE_NUMBER");
        exp.setValueClass(Integer.class);
        JRDesignTextField pageNumber = new JRDesignTextField();
        pageNumber.setExpression((JRExpression)exp);
        pageNumber.setX(40);
        pageNumber.setY(0);
        pageNumber.setHeight(15);
        pageNumber.setWidth(100);
        pageFooter.addElement((JRDesignElement)pageNumber);
        return pageFooter;
    }
}

