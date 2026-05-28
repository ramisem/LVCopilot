/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.modules.configreport.renderer.ContentRendererUtil;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ProcessLog {
    private static StringBuffer log = new StringBuffer();
    private static StringBuffer response = new StringBuffer();
    private static ContentRendererUtil htmlUtil = new ContentRendererUtil();
    private DataSet operations = new DataSet();
    private DataSet summary = new DataSet();

    public ProcessLog() {
        log = new StringBuffer();
        response = new StringBuffer();
    }

    public void println(String newline) {
        log.append("<P>");
        log.append(newline);
    }

    public void print(StringBuffer content) {
        log.append(content);
    }

    public void addOp(String op) {
        int row = this.operations.addRow();
        this.operations.setString(row, "Operation", op);
    }

    public void addSummary(String entity, String ids, String desc) {
        int row = this.summary.addRow();
        this.summary.setString(row, "Item", entity);
        this.summary.setString(row, "Id(s)", ids);
        this.summary.setString(row, "Description", desc);
    }

    public void addOp(String rule, String op, String sdc, String id, String sapphireColumn, String empowerDataSet, String empowerColumn, String empowerValue) {
        int row = this.operations.addRow();
        this.operations.setString(row, "Source", empowerDataSet);
        this.operations.setString(row, "Empower Column", empowerColumn);
        this.operations.setString(row, "Map To", rule);
        this.operations.setString(row, "Operation", op);
        this.operations.setString(row, "Key", id);
        this.operations.setString(row, "Column", sapphireColumn);
        this.operations.setString(row, "Value", empowerValue);
    }

    public String getLog() {
        return log.toString();
    }

    public String getResponse() {
        return this.renderResponse();
    }

    public void addTable(PropertyList pl) {
        log.append(htmlUtil.renderPropertyList(pl, true));
    }

    public String renderResponse() {
        htmlUtil.renderListTableTop(response, this.operations);
        return response.toString();
    }

    public void addDiffTable(DataSet src, DataSet ref, String[] keycols) {
        htmlUtil.renderDiffListTable(log, src, ref, keycols);
    }
}

