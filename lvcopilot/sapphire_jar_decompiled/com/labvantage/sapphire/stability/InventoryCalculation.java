/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.opal.validation.misc.ConvertUnits;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;

public class InventoryCalculation {
    DataSet containerGroups = new DataSet();
    QueryProcessor qp;
    TranslationProcessor tp;
    FormatUtil fmutil;
    int sampleCount = 1;
    boolean multiplyBySampleCount = true;

    public InventoryCalculation(String connectionId) {
        this.qp = new QueryProcessor(connectionId);
        this.tp = new TranslationProcessor(connectionId);
    }

    public InventoryCalculation(String connectionId, SapphireConnection connection) {
        this.qp = new QueryProcessor(connectionId);
        this.tp = new TranslationProcessor(connectionId);
        this.fmutil = FormatUtil.getInstance(connection);
    }

    public void setSampleCount(int c) {
        this.sampleCount = c;
    }

    public void setMultiplyBySampleCount(boolean v) {
        this.multiplyBySampleCount = v;
    }

    public HashMap getInventoryAmount(DataSet dsWorkitems, String ppflag, double containerSize, String containerSizeUnit, StringBuffer log) {
        HashMap<String, String> hm = new HashMap<String, String>();
        DataSet ds = this.getContainerGroupData(dsWorkitems, ppflag, containerSize, containerSizeUnit, log);
        if (ds.size() > 0) {
            double counter = 0.0;
            double amount = 0.0;
            String firstUnit = ds.getValue(0, "amountunit");
            boolean fail = false;
            for (int i = 0; i < ds.size() && !fail; fail |= !ds.getValue(i, "amountunit").equals(firstUnit), ++i) {
                counter += ds.getDouble(i, "quantity");
            }
            if (fail) {
                log.append("<font color='red'>" + this.tp.translate("Unit mismatch. Unable to calculate total") + "</font>");
                hm.put("amount", "0");
                hm.put("amountunit", "(Containers)");
            } else {
                amount = counter;
                amount = (double)Math.round(amount * 100.0) / 100.0;
                hm.put("amount", "" + amount);
                hm.put("amountunit", ds.getValue(0, "quantityunit"));
                if (this.multiplyBySampleCount && this.sampleCount > 1) {
                    String displayUnit;
                    String displayAmount;
                    for (int k = 1; k < this.sampleCount; ++k) {
                        amount += counter;
                    }
                    String amtUnit = ds.getValue(0, "quantityunit");
                    if ("(Containers)".equals(amtUnit)) {
                        displayAmount = "" + (int)amount;
                        displayUnit = " Containers";
                    } else {
                        displayAmount = "" + amount;
                        displayUnit = amtUnit;
                    }
                    log.append("<b>[ Quantity multiplied by Sample Count " + this.sampleCount + " requires " + displayAmount + " " + displayUnit + " ]</b>");
                }
            }
        } else {
            log.append("<font color='red'>" + this.tp.translate("No container groups found. Returning zero") + "</font>");
            hm.put("amount", "0");
            hm.put("amountunit", "(Containers)");
        }
        return hm;
    }

    public DataSet getContainerGroupData(DataSet dsWorkitems, String ppflag, double containerSize, String containerSizeUnit, StringBuffer log) {
        this.containerGroups.addColumn("departmentid", 0);
        this.containerGroups.addColumn("containergroupid", 0);
        this.containerGroups.addColumn("quantity", 1);
        this.containerGroups.addColumn("quantityunit", 0);
        this.containerGroups.addColumn("teststring", 0);
        dsWorkitems.sort("departmentid");
        ArrayList<DataSet> departmentWorkitems = dsWorkitems.getGroupedDataSets("departmentid");
        int departments = departmentWorkitems.size();
        for (int deptItemCnt = 0; deptItemCnt < departments; ++deptItemCnt) {
            DataSet departmentTests = departmentWorkitems.get(deptItemCnt);
            String departmentid = departmentTests.getValue(0, "departmentid", "");
            this.createPullGroups(departmentTests, containerSize, containerSizeUnit, ppflag, departmentid, log);
        }
        return this.containerGroups;
    }

    private void createPullGroups(DataSet tests, double containerSize, String containerUnit, String ppflag, String departmentid, StringBuffer log) {
        if (departmentid != null && departmentid.length() > 0) {
            log.append("-----" + departmentid + "------<br>");
        }
        if (ppflag.equalsIgnoreCase("Y")) {
            this.processPartialPulls(tests, containerSize, containerUnit, log, departmentid);
        } else {
            HashMap<String, String> findYY = new HashMap<String, String>();
            findYY.put("containerpertestflag", "Y");
            findYY.put("containerperrepeatflag", "Y");
            HashMap<String, String> findYN = new HashMap<String, String>();
            findYN.put("containerpertestflag", "Y");
            findYN.put("containerperrepeatflag", "N");
            HashMap<String, String> findN = new HashMap<String, String>();
            findN.put("containerpertestflag", "N");
            DataSet dsYY = tests.getFilteredDataSet(findYY);
            DataSet dsYN = tests.getFilteredDataSet(findYN);
            DataSet dsN = tests.getFilteredDataSet(findN);
            dsYY.sort("destructivetestflag");
            dsYN.sort("destructivetestflag");
            dsN.sort("destructivetestflag");
            if (dsYY.size() > 0) {
                this.createYYGroups(dsYY, departmentid, containerSize, containerUnit, log);
            }
            if (dsYN.size() > 0) {
                this.createYNGroups(dsYN, departmentid, containerSize, containerUnit, log);
            }
            if (dsN.size() > 0) {
                this.createNGroups(dsN, departmentid, containerSize, containerUnit, log);
            }
        }
    }

    private void processPartialPulls(DataSet tests, double containerSize, String containerUnit, StringBuffer log, String departmentid) {
        StringBuffer workitemList = new StringBuffer();
        double sumQuantity = 0.0;
        double maxQuantity = 0.0;
        for (int i = 0; i < tests.size(); ++i) {
            try {
                int repeats = tests.getInt(i, "numrepeats", 0);
                double quantity = tests.getDouble(i, "quantity", 0.0);
                String quantityUnit = tests.getString(i, "quantityunit");
                double quantityPer = quantity = this.getCorrectedQuantity(quantity, quantityUnit, containerSize, containerUnit);
                quantity *= (double)repeats;
                boolean destructive = tests.getValue(i, "destructivetestflag", "N").equalsIgnoreCase("Y");
                if (destructive) {
                    sumQuantity += quantity;
                } else {
                    maxQuantity = Math.max(maxQuantity, quantity);
                }
                String workitemid = tests.getValue(i, "workitemid");
                for (int j = 0; j < repeats; ++j) {
                    workitemList.append(";").append(workitemid);
                }
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("repeats", "" + repeats);
                token.put("qtyPer", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(quantityPer)) : "" + quantityPer);
                token.put("containerUnit", containerUnit);
                token.put("workitemid", workitemid);
                log.append(this.tp.translate("[repeats] repeats of [qtyPer] [containerUnit] for [workitemid]" + (destructive ? " (Destructive)" : " (Non-Destructive)"), token) + "<br>");
                continue;
            }
            catch (SapphireException e) {
                Logger.logStackTrace(e);
                log.append("<font color=\"red\">" + this.tp.translate("Unable to process inventory: ") + e.getMessage() + "</font>");
            }
        }
        if (workitemList.length() > 0) {
            if (sumQuantity > 0.0 && maxQuantity > 0.0) {
                log.append("<div style=\"font-style: italic; margin-left: 20px\">");
                log.append(this.tp.translate("Destructive testing requires") + " ").append(this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(sumQuantity)) : Double.valueOf(sumQuantity)).append(" " + containerUnit + "<br>");
                log.append(this.tp.translate("Non-Destructive testing requires") + " ").append(this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(maxQuantity)) : Double.valueOf(maxQuantity)).append(" " + containerUnit + "<br>");
                log.append("</div>");
            }
            double totalPullAmount = Math.max(sumQuantity, maxQuantity);
            this.addContainerGroup(departmentid, totalPullAmount, containerUnit, workitemList.substring(1));
        }
    }

    private void createYYGroups(DataSet tests, String departmentid, double containerSize, String containerUnit, StringBuffer log) {
        for (int i = 0; i < tests.getRowCount(); ++i) {
            try {
                int repeats = tests.getInt(i, "numrepeats", 0);
                double quantity = tests.getDouble(i, "quantity", 0.0);
                String quantityUnit = tests.getValue(i, "quantityunit");
                quantity = this.getCorrectedQuantity(quantity, quantityUnit, containerSize, containerUnit);
                int containers = (int)Math.ceil(quantity / containerSize);
                String workitemid = tests.getValue(i, "workitemid");
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("workitemid", workitemid);
                token.put("quantity", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(quantity)) : "" + quantity);
                token.put("containerUnit", containerUnit);
                token.put("containers", "" + containers);
                token.put("repeats", "" + repeats);
                log.append(this.tp.translate("[workitemid] requires [quantity] [containerUnit] = [containers] " + (containers == 1 ? "Container" : "Containers") + " for each of the [repeats] repeats", token) + "<br>");
                for (int nor = 0; nor < repeats; ++nor) {
                    this.addContainerGroup(departmentid, containers, "(Containers)", workitemid);
                }
                continue;
            }
            catch (SapphireException e) {
                Logger.logStackTrace(e);
                log.append("<font color=\"red\">" + this.tp.translate("Unable to process inventory") + ": " + e.getMessage() + "</font>");
            }
        }
    }

    private void createYNGroups(DataSet tests, String departmentid, double containerSize, String containerUnit, StringBuffer log) {
        for (int i = 0; i < tests.getRowCount(); ++i) {
            try {
                int repeats = tests.getInt(i, "numrepeats", 0);
                double quantity = tests.getDouble(i, "quantity", 0.0);
                String quantityUnit = tests.getValue(i, "quantityunit", "");
                double quantityPer = quantity = this.getCorrectedQuantity(quantity, quantityUnit, containerSize, containerUnit);
                boolean destructive = tests.getValue(i, "destructivetestflag", "N").equalsIgnoreCase("Y");
                int containers = destructive ? (int)Math.ceil((double)repeats * quantity / containerSize) : (int)Math.ceil(quantity / containerSize);
                String workitemid = tests.getValue(i, "workitemid", "");
                StringBuffer workitemList = new StringBuffer();
                for (int j = 0; j < repeats; ++j) {
                    workitemList.append(";").append(workitemid);
                }
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("repeats", "" + repeats);
                token.put("workitemid", workitemid);
                token.put("quantityPer", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(quantityPer)) : "" + quantityPer);
                token.put("containerUnit", containerUnit);
                String containersStr = "<b>" + containers + " " + (containers == 1 ? this.tp.translate("Container") : this.tp.translate("Containers")) + "</b>";
                token.put("containers", containersStr);
                log.append(this.tp.translate("[repeats] repeats of [quantityPer] [containerUnit] for [workitemid]" + (destructive ? " (Destructive)" : " (Non-Destructive)") + " requires [containers] ", token) + "<br>");
                if (workitemList.length() <= 0) continue;
                this.addContainerGroup(departmentid, containers, "(Containers)", workitemList.substring(1));
                continue;
            }
            catch (SapphireException e) {
                Logger.logStackTrace(e);
                log.append("<font color=\"red\">" + this.tp.translate("Unable to process inventory") + ": " + e.getMessage());
            }
        }
    }

    private void createNGroups(DataSet tests, String departmentid, double containerSize, String containerUnit, StringBuffer log) {
        int maxRepeats = 1;
        int realMaxRepeats = 1;
        log.append("---" + this.tp.translate("Calculating shared testing") + "...<br>");
        for (int i = 0; i < tests.size(); ++i) {
            realMaxRepeats = Math.max(realMaxRepeats, tests.getInt(i, "numrepeats"));
            if (!tests.getValue(i, "containerperrepeatflag").equals("Y")) continue;
            maxRepeats = Math.max(maxRepeats, tests.getInt(i, "numrepeats"));
        }
        for (int repeat = 1; repeat <= maxRepeats; ++repeat) {
            double sumQuantity = 0.0;
            double maxQuantity = 0.0;
            StringBuffer workitemList = new StringBuffer();
            for (int i = 0; i < tests.getRowCount(); ++i) {
                try {
                    HashMap<String, String> token;
                    int repeats = tests.getInt(i, "numrepeats", 0);
                    double quantity = tests.getDouble(i, "quantity", 0.0);
                    String quantityUnit = tests.getValue(i, "quantityunit", "");
                    quantity = this.getCorrectedQuantity(quantity, quantityUnit, containerSize, containerUnit);
                    boolean destructive = tests.getValue(i, "destructivetestflag", "N").equalsIgnoreCase("Y");
                    String workitemid = tests.getValue(i, "workitemid", "");
                    if (repeat <= repeats && tests.getValue(i, "containerperrepeatflag").equals("Y")) {
                        if (destructive) {
                            sumQuantity += quantity;
                        } else {
                            maxQuantity = Math.max(maxQuantity, quantity);
                        }
                        token = new HashMap<String, String>();
                        token.put("repeat", "" + repeat);
                        token.put("workitemid", workitemid);
                        token.put("quantity", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(quantity)) : "" + quantity);
                        token.put("containerUnit", containerUnit);
                        log.append(this.tp.translate("Repeat [repeat] of [workitemid]" + (destructive ? " (Destructive)" : " (Non-Destructive)") + " requires [quantity] [containerUnit]", token) + "<br>");
                        workitemList.append(";").append(workitemid);
                    }
                    if (repeat != 1 || !tests.getValue(i, "containerperrepeatflag").equals("N")) continue;
                    if (destructive) {
                        sumQuantity += quantity * (double)repeats;
                        token = new HashMap();
                        token.put("repeats", "" + repeats);
                        token.put("workitemid", workitemid);
                        token.put("quantity", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(quantity)) : "" + quantity);
                        token.put("containerUnit", containerUnit);
                        log.append(this.tp.translate("[repeats] " + (repeats == 1 ? "repeat" : "repeats") + " of [workitemid] each requiring [quantity] [containerUnit]", token) + "<br>");
                    } else {
                        maxQuantity = Math.max(maxQuantity, quantity);
                        token = new HashMap();
                        token.put("repeats", "" + repeats);
                        token.put("workitemid", workitemid);
                        token.put("quantity", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(quantity)) : "" + quantity);
                        token.put("containerUnit", containerUnit);
                        log.append(this.tp.translate("[repeats] " + (repeats == 1 ? "repeat" : "repeats") + " of [workitemid] share [quantity] [containerUnit]", token) + "<br>");
                    }
                    for (int j = 0; j < repeats; ++j) {
                        workitemList.append(";").append(workitemid);
                    }
                    continue;
                }
                catch (SapphireException e) {
                    Logger.logStackTrace(e);
                    log.append("<font color=\"red\">" + this.tp.translate("Unable to process inventory") + ": " + e.getMessage() + "</font>");
                }
            }
            if (workitemList.length() <= 0) continue;
            HashMap<String, String> token = new HashMap<String, String>();
            if (sumQuantity > 0.0 && maxQuantity > 0.0) {
                log.append("<div style=\"font-style: italic; margin-left: 20px\">");
                token.put("repeat", "" + repeat);
                token.put("sumQty", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(sumQuantity)) : "" + sumQuantity);
                token.put("maxQty", this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(maxQuantity)) : "" + maxQuantity);
                token.put("containerUnit", containerUnit);
                log.append(this.tp.translate("Repeat [repeat] of Destructive testing requires [sumQty] [containerUnit]", token) + "<br>");
                log.append(this.tp.translate("Repeat [repeat] of Non-Destructive testing requires [maxQty] [containerUnit]", token) + "<br>");
                log.append("</div>");
            }
            double totalPullAmount = Math.max(sumQuantity, maxQuantity);
            int containers = (int)Math.ceil(totalPullAmount / containerSize);
            token.clear();
            token.put("repeat", "" + repeat);
            token.put("containers", "" + containers);
            log.append("<b>" + this.tp.translate("Repeat [repeat] requires [containers] " + (repeat == 1 ? "Container" : "Containers"), token) + "</b><br>");
            this.addContainerGroup(departmentid, containers, "(Containers)", workitemList.substring(1));
        }
        if (realMaxRepeats > maxRepeats) {
            log.append("<i>" + this.tp.translate("Additional repeats can reuse the same containers") + "</i><br>");
        }
    }

    private double getCorrectedQuantity(double quantity, String quantityUnit, double containerSize, String containerUnit) throws SapphireException {
        if (quantityUnit == null || quantityUnit.length() == 0 || quantityUnit.equals("(Containers)")) {
            quantity *= containerSize;
            quantity = new BigDecimal(quantity).setScale(5, 4).doubleValue();
            quantityUnit = containerUnit;
        } else if (!quantityUnit.equalsIgnoreCase(containerUnit)) {
            quantity = this.convertToContainerSizeUnit(quantity, quantityUnit, containerUnit);
        }
        return quantity;
    }

    private double convertToContainerSizeUnit(double quantity, String fromUnit, String toUnit) throws SapphireException {
        double newQuantity = 0.0;
        newQuantity = FormatUtil.getInstance().parseBigDecimal(ConvertUnits.convertUnits(this.qp, fromUnit, toUnit, "" + quantity)).doubleValue();
        return newQuantity;
    }

    private void addContainerGroup(String departmentid, double quantity, String quantityUnit, String workitemList) {
        int newrow = this.containerGroups.addRow();
        this.containerGroups.setValue(newrow, "departmentid", departmentid);
        this.containerGroups.setValue(newrow, "containergroupid", "" + (1 + newrow));
        this.containerGroups.setNumber(newrow, "quantity", quantity);
        this.containerGroups.setValue(newrow, "quantityunit", quantityUnit);
        this.containerGroups.setValue(newrow, "teststring", workitemList);
    }
}

