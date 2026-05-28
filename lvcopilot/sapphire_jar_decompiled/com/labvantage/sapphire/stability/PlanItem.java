/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.opal.validation.misc.ConvertUnits;
import com.labvantage.sapphire.stability.InventoryCalculation;
import com.labvantage.sapphire.stability.PlanWorkItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.stability.task.HasDetails;
import com.labvantage.sapphire.stability.task.PullAmount;
import com.labvantage.sapphire.util.UnitsUtil;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class PlanItem
implements Serializable,
Comparable {
    public String scheduleplanid;
    public String planItemid;
    public String timeruleid;
    public String conditionid;
    public String propertyTreeid;
    private PropertyList propertyList;
    public boolean readonly = false;
    public boolean conditionreadonly = false;
    public String conditionStatus = "";
    public String reviewBy = "";
    public String reviewByDesc = "";
    public Calendar reviewDt = null;
    public String reviewDispositionFlag = "";
    private boolean reviewDispositionFlagUpdated = false;
    public String gridCellId = "";
    public ScheduleGrid grid;
    public String status = "A";
    public String eventStatus = "";
    public boolean hasEvent = false;
    public Calendar eventdt = null;
    public DataSet workitems = new DataSet();
    public DataSet details = new DataSet();
    public boolean firstPlanItemInCell = false;
    private int sequence = 0;

    PlanItem(ScheduleGrid grid, String scheduleplanid, String planitemid, String timeruleid, String conditionid, String propertytreeid, PropertyList propertyList, String status) throws SapphireException {
        if (timeruleid.startsWith("__")) {
            timeruleid = timeruleid.substring(2);
        }
        if (conditionid.startsWith("__")) {
            conditionid = conditionid.substring(2);
        }
        this.grid = grid;
        this.scheduleplanid = scheduleplanid;
        this.planItemid = planitemid;
        this.timeruleid = timeruleid;
        this.conditionid = conditionid;
        if (status != null && status.equals("D")) {
            this.status = "D";
        }
        this.setPropertyTreeid(propertytreeid);
        if (propertyList == null) {
            this.propertyList = new PropertyList();
            this.propertyList.setUsePropertyValues(true);
        } else {
            PropertyList amountPropertyList = propertyList.getPropertyList("pullamount");
            if (amountPropertyList != null) {
                if (scheduleplanid == null) {
                    this.validatePullAmountUnit(amountPropertyList, grid.planid, conditionid, propertytreeid);
                    this.propertyList = propertyList;
                } else {
                    this.propertyList = propertyList;
                    this.validatePullAmountUnit(amountPropertyList, scheduleplanid, conditionid, propertytreeid);
                }
            } else {
                this.propertyList = propertyList;
            }
        }
        int row = grid.conditionAxis.findRow(conditionid);
        this.conditionStatus = grid.conditionAxis.getStatus(row);
        this.conditionreadonly = grid.conditionAxis.isReadOnly(row);
        if (this.conditionreadonly) {
            this.readonly = true;
        }
    }

    public String toString() {
        return this.planItemid + " (Time: " + this.timeruleid + " Source: " + this.conditionid + " Tree: " + this.propertyTreeid + " " + (this.propertyList == null ? 0 : this.propertyList.size()) + " Properties)";
    }

    public void setPropertyList(PropertyList propertyList) throws SapphireException {
        String planId = this.scheduleplanid;
        PropertyList amountPropertyList = propertyList.getPropertyList("pullamount");
        if (amountPropertyList != null) {
            this.validatePullAmountUnit(amountPropertyList, planId, this.conditionid, this.propertyTreeid);
        }
        this.propertyList = propertyList;
    }

    public PropertyList getPropertyList() {
        if (this.propertyList == null) {
            this.propertyList = new PropertyList();
            this.propertyList.setUsePropertyValues(true);
        }
        return this.propertyList;
    }

    public void setReviewDispositionFlag(String flag) {
        this.reviewDispositionFlag = flag;
    }

    public String getReviewDispositionFlag() {
        if (this.reviewDispositionFlag == null) {
            this.reviewDispositionFlag = "";
        }
        return this.reviewDispositionFlag;
    }

    public void setReviewDispositionFlagUpdated(boolean updated) {
        this.reviewDispositionFlagUpdated = updated;
    }

    public boolean getReviewDispositionFlagUpdated() {
        return this.reviewDispositionFlagUpdated;
    }

    public PropertyList getCollapsedPropertyList() {
        try {
            PropertyTree tree = this.grid.getPropertyTree(this.propertyTreeid);
            if (tree != null) {
                Node planNode = tree.getNode("__plan");
                Node conditionNode = tree.createNode("__condition", planNode);
                conditionNode.setPropertyList(this.grid.conditionAxis.getPropertyList(this.grid.conditionAxis.findRow(this.conditionid), this.propertyTreeid));
                Node planItemNode = tree.createNode("__item", conditionNode);
                planItemNode.setPropertyList(this.getPropertyList());
                return tree.getNodePropertyList("__item", true);
            }
            return new PropertyList();
        }
        catch (SapphireException e) {
            return this.getPropertyList();
        }
    }

    public void setPropertyTreeid(String propertyTreeid) throws SapphireException {
        this.propertyTreeid = propertyTreeid;
        this.grid.taskTypes.setExcluded(propertyTreeid, false);
    }

    public void setEventDetails(String eventStatus, Calendar eventdt) {
        this.hasEvent = true;
        this.eventdt = eventdt;
        this.eventStatus = eventStatus;
        if (eventStatus.equals("D")) {
            this.readonly = true;
        }
    }

    public int compareTo(Object o) {
        PlanItem item = (PlanItem)o;
        return this.sequence - item.sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setWorkitems(DataSet planItemWorkItems) {
        this.workitems = planItemWorkItems;
    }

    public DataSet getWorkitems() {
        return this.workitems;
    }

    public int findWorkItemRow(String workitemid) {
        return this.workitems.findRow("workitemid", workitemid);
    }

    public void removeWorkItem(String workitemid) {
        int row = this.workitems.findRow("workitemid", workitemid);
        if (row >= 0) {
            this.workitems.deleteRow(row);
        }
    }

    public void setWorkItem(PlanWorkItem workitem) {
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put("workitemid", workitem.workitemid);
        int row = this.workitems.findRow(findMap);
        if (row < 0) {
            row = this.workitems.addRow();
            this.workitems.setString(row, "workitemid", workitem.workitemid);
            this.workitems.setString(row, "workitemversionid", workitem.workitemversionid);
            this.workitems.setString(row, "scheduleplanitemid", this.planItemid);
        }
        this.workitems.setNumber(row, "quantity", workitem.quantity);
        this.workitems.setString(row, "quantityunit", workitem.quantityUnit);
        this.workitems.setString(row, "quantitytype", workitem.quantityType);
        this.workitems.setString(row, "containerperrepeatflag", workitem.containerPerRepeatFlag ? "Y" : "N");
        this.workitems.setString(row, "containerpertestflag", workitem.containerPerTestFlag ? "Y" : "N");
        this.workitems.setString(row, "destructivetestflag", workitem.destructiveTestFlag ? "Y" : "N");
        this.workitems.setString(row, "reusecontainerflag", workitem.reuseContainerFlag ? "Y" : "N");
        this.workitems.setString(row, "contingentflag", workitem.contingentFlag ? "Y" : "N");
        this.workitems.setNumber(row, "numrepeats", workitem.numRepeats);
        this.workitems.setString(row, "departmentid", workitem.departmentid);
        this.workitems.setNumber(row, "duedtoffset", workitem.duedtOffset);
        this.workitems.setString(row, "duedtoffsettimeunit", workitem.duedtOffsetTimeUnit);
        this.workitems.setNumber(row, "usersequence", workitem.usersequence);
    }

    public HashMap getRequiredAmount(double containerSize, String containerSizeUnit, boolean ppFlag, boolean calculatTotal, StringBuffer log) {
        PullAmount instancePullAmount;
        HashMap<String, Object> hmInventory = new HashMap<String, Object>();
        double amount = 0.0;
        String amountUnit = "";
        PropertyList createSdi = this.getCollapsedPropertyList().getPropertyListNotNull("createsdi");
        boolean multiplyQtyBySampleCount = "Y".equalsIgnoreCase(createSdi.getProperty("multiplyqtybysamplecount", "Y"));
        String copiesStr = createSdi.getProperty("copies", "");
        int copies = 0;
        if (copiesStr.length() > 0) {
            try {
                copies = Integer.parseInt(copiesStr);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        TranslationProcessor tp = new TranslationProcessor(this.grid.getConnectionid());
        try {
            instancePullAmount = this.grid.taskTypes.getPullAmount(this.propertyTreeid);
        }
        catch (SapphireException e) {
            instancePullAmount = null;
        }
        if (instancePullAmount != null && instancePullAmount.getQuantity(this) > 0.0) {
            amount = instancePullAmount.getQuantity(this);
            amountUnit = instancePullAmount.getUnits(this);
            PropertyList pullamount = this.getCollapsedPropertyList().getPropertyList("pullamount");
            String strOrigAmount = pullamount.getProperty("quantity");
            if (ppFlag) {
                if ("".equals(amountUnit)) {
                    amountUnit = "(Containers)";
                }
                if (copies > 1 && multiplyQtyBySampleCount) {
                    String displayUnit;
                    String displayAmount;
                    if ("(Containers)".equals(amountUnit)) {
                        displayAmount = "" + (int)amount * copies;
                        displayUnit = " Containers";
                    } else {
                        displayAmount = "" + amount * (double)copies;
                        displayUnit = amountUnit;
                    }
                    log.append("<b>[ Quantity multiplied by Sample Count " + copies + " requires " + displayAmount + " " + displayUnit + " ]</b>");
                }
            } else {
                double originalAmount = amount;
                String originalUnits = amountUnit;
                if ("".equals(amountUnit)) {
                    amountUnit = "(Containers)";
                }
                if ("(Containers)".equals(amountUnit)) {
                    if (copies > 1 && multiplyQtyBySampleCount) {
                        log.append("<b>[ Quantity multiplied by Sample Count " + copies + " requires " + (int)amount * copies + " Containers ]</b>");
                    }
                } else if (!"(Containers)".equals(amountUnit) && containerSize > 0.0) {
                    if (containerSizeUnit.equals(amountUnit)) {
                        amount = (int)Math.ceil(amount / containerSize);
                        amountUnit = "(Containers)";
                        HashMap<String, String> token = new HashMap<String, String>();
                        token.put("strOrigAmount", strOrigAmount);
                        token.put("originalUnits", originalUnits);
                        token.put("amount", "" + (int)amount);
                        log.append(tp.translate("(Actually required [strOrigAmount] [originalUnits]. Non-partial pull makes this [amount] Containers)", token) + "<br>");
                        if (copies > 1 && multiplyQtyBySampleCount) {
                            log.append("<b>[ Quantity multiplied by Sample Count " + copies + " requires " + (int)amount * copies + " Containers ]</b>");
                        }
                    } else {
                        try {
                            String convertedAmount = ConvertUnits.convertUnits(this.grid.queryProcessor, amountUnit, containerSizeUnit, "" + amount);
                            double convAmt = Double.parseDouble(convertedAmount);
                            amount = (int)Math.ceil(convAmt / containerSize);
                            amountUnit = "(Containers)";
                            HashMap<String, String> token = new HashMap<String, String>();
                            token.put("strOrigAmount", strOrigAmount);
                            token.put("originalUnits", originalUnits);
                            token.put("amount", "" + (int)amount);
                            log.append(tp.translate("(Actually required [strOrigAmount] [originalUnits]. Non-partial pull makes this [amount] Containers)", token) + "<br>");
                            if (copies > 1 && multiplyQtyBySampleCount) {
                                log.append("<b>[ Quantity multiplied by Sample Count " + copies + " requires " + (int)amount * copies + " Containers ]</b>");
                            }
                        }
                        catch (Exception e) {
                            log.append("Error: " + e.getMessage() + "<BR>");
                        }
                    }
                }
            }
        } else if (this.workitems != null && this.workitems.size() > 0) {
            HashMap<String, String> findReuse = new HashMap<String, String>();
            findReuse.put("reusecontainerflag", "Y");
            DataSet workitemsCopy = this.workitems.copy();
            if (this.grid.getPartialDistribution()) {
                workitemsCopy.setString(-1, "departmentid", "");
            }
            for (int i = workitemsCopy.size() - 1; i >= 0; --i) {
                if (!calculatTotal && workitemsCopy.getValue(i, "contingentflag").equals("Y")) {
                    workitemsCopy.deleteRow(i);
                    continue;
                }
                String workitemid = workitemsCopy.getValue(i, "workitemid");
                findReuse.put("workitemid", workitemid);
                if (!workitemsCopy.getValue(i, "reusecontainerflag").equals("Y")) continue;
                ArrayList planitems = this.grid.planItems.findByCondition(this.conditionid);
                this.grid.planItems.sortByTime(planitems);
                boolean stop = false;
                Iterator iterator = planitems.iterator();
                while (!stop && iterator.hasNext()) {
                    PlanItem planItem = (PlanItem)iterator.next();
                    if (planItem == this) {
                        stop = true;
                        continue;
                    }
                    if (planItem.workitems.findRow(findReuse) < 0) continue;
                    HashMap<String, String> token = new HashMap<String, String>();
                    token.put("workitemid", workitemid);
                    token.put("timerulelabel", this.grid.timeAxis.getLabel(planItem.timeruleid));
                    log.append(tp.translate("[workitemid] will reuse the container from [timerulelabel]", token) + "<br>");
                    workitemsCopy.deleteRow(i);
                    stop = true;
                }
            }
            InventoryCalculation invCalc = new InventoryCalculation(this.grid.getConnectionId(), this.grid.connectionProcessor.getSapphireConnection());
            if (copies > 1) {
                invCalc.setSampleCount(copies);
                invCalc.setMultiplyBySampleCount(multiplyQtyBySampleCount);
            }
            HashMap hm = invCalc.getInventoryAmount(workitemsCopy, ppFlag ? "Y" : "N", containerSize, containerSizeUnit, log);
            amount = Double.parseDouble(hm.get("amount").toString());
            amountUnit = hm.get("amountunit").toString();
        } else {
            amount = -1.0;
        }
        hmInventory.put("amount", new Double(amount));
        hmInventory.put("amountunit", amountUnit);
        if (copies > 1 && multiplyQtyBySampleCount) {
            hmInventory.put("samplecount", "" + copies);
        }
        return hmInventory;
    }

    public HasDetails getInstanceHasDetails() {
        HasDetails instanceHasDetails;
        try {
            instanceHasDetails = this.grid.taskTypes.getHasDetails(this.propertyTreeid);
        }
        catch (SapphireException e) {
            instanceHasDetails = null;
        }
        return instanceHasDetails;
    }

    public PullAmount getInstancePullAmount() {
        PullAmount instancePullAmount;
        try {
            instancePullAmount = this.grid.taskTypes.getPullAmount(this.propertyTreeid);
        }
        catch (SapphireException e) {
            instancePullAmount = null;
        }
        return instancePullAmount;
    }

    public GridTask getInstanceGridTask() {
        GridTask instanceGridTask;
        try {
            instanceGridTask = this.grid.taskTypes.getGridTask(this.propertyTreeid);
        }
        catch (SapphireException e) {
            instanceGridTask = null;
        }
        return instanceGridTask;
    }

    private void validatePullAmountUnit(PropertyList amtPL, String planId, String conditionId, String propertyTreeId) throws SapphireException {
        String containerUnit;
        String amountUnit = amtPL.getProperty("units");
        if (amountUnit.length() == 0) {
            amountUnit = ScheduleGridUtil.getAmountUnitFromAncestors(planId, conditionId, propertyTreeId, this.grid.queryProcessor);
        }
        if (amountUnit.length() > 0 && !"(Containers)".equals(amountUnit) && (containerUnit = ScheduleGridUtil.getContainerUnit(planId, this.grid.queryProcessor)) != null && containerUnit.length() > 0 && !containerUnit.equals(amountUnit)) {
            TranslationProcessor tp = new TranslationProcessor(this.grid.getConnectionid());
            if (!UnitsUtil.isUnitCompatible(this.grid.queryProcessor, amountUnit, containerUnit)) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("containerUnit", "<b>'" + containerUnit + "'</b>");
                valueMap.put("amountUnit", "<b>'" + amountUnit + "'</b>");
                throw new SapphireException("VALIDATION", "\n" + tp.translate("Unit conversion not defined between Container Unit [containerUnit] and the Pull Amount Unit [amountUnit].", valueMap));
            }
        }
    }
}

