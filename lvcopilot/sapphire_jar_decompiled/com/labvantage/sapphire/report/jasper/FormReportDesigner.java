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
 *  net.sf.jasperreports.engine.type.PenEnum
 *  net.sf.jasperreports.engine.type.StretchTypeEnum
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.ReportDesigner;
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
import net.sf.jasperreports.engine.type.PenEnum;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FormReportDesigner
extends ReportDesigner {
    public FormReportDesigner(String title, PropertyList element) {
        super(title, element);
    }

    public FormReportDesigner(String title, PropertyList element, JasperDesign jasperDesign) {
        super(title, element, jasperDesign);
    }

    @Override
    public JasperDesign modifyJasperDesign() {
        return this.createJasperDesign();
    }

    @Override
    public JasperDesign createJasperDesign() {
        try {
            JRDesignBand detailBand = new JRDesignBand();
            detailBand.setHeight(20);
            ((JRDesignSection)this.jasperDesign.getDetailSection()).addBand((JRBand)detailBand);
            JRDesignBand headerBand = new JRDesignBand();
            headerBand.setHeight(20);
            this.jasperDesign.setColumnHeader((JRBand)headerBand);
            PropertyListCollection columns = this.element.getCollection("columns");
            int colcount = columns.size();
            for (int i = 0; i < colcount; ++i) {
                PropertyList column = columns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                int datatype = 0;
                if (this.exampleData != null) {
                    datatype = this.exampleData.getColumnType(columnid);
                }
                this.addJasperFormColumn(column, this.jasperDesign, datatype);
            }
            this.setFormFieldLayout();
            this.jasperDesign.setPageFooter((JRBand)this.getFooterBand());
            JRDesignBand emptyBand = new JRDesignBand();
            emptyBand.setHeight(0);
            this.jasperDesign.setPageHeader((JRBand)emptyBand);
            this.jasperDesign.setColumnFooter((JRBand)emptyBand);
            this.jasperDesign.setSummary((JRBand)emptyBand);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return this.jasperDesign;
    }

    private void addJasperFormColumn(PropertyList column, JasperDesign jasperDesign, int columntype) {
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
            ((JRDesignBand)jasperDesign.getDetailSection().getBands()[0]).addElement((JRDesignElement)fieldTitle);
            ((JRDesignBand)jasperDesign.getDetailSection().getBands()[0]).addElement((JRDesignElement)fieldValue);
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    private void setFormFieldLayout() {
        ((JRDesignBand)this.jasperDesign.getDetailSection().getBands()[0]).setHeight(660);
        JRElement[] textfields = ((JRDesignBand)this.jasperDesign.getDetailSection().getBands()[0]).getElements();
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
}

