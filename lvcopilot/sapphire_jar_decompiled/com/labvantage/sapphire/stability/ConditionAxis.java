/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.validation.misc.ConvertUnits;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.PlanItem;
import com.labvantage.sapphire.stability.ScheduleGrid;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ConditionAxis
extends BaseAxis {
    private ArrayList propertyListMaps = null;
    private static final String PROPERTYLISTINDEXCOLUMN = "__propertylistindex";
    public DataSet trackitems = new DataSet();
    FormatUtil fmutil;

    public ConditionAxis(ScheduleGrid grid) {
        this.grid = grid;
        this.idColumn = "scheduleconditionid";
        this.labelColumn = "conditionlabel";
        this.cellIdColumn = "scheduleconditionid";
        this.tableid = "schedulecondition";
        this.buttonLabel = "Condition";
        this.connectionInfo = grid.connectionProcessor.getConnectionInfo(grid.getConnectionId());
        this.fmutil = FormatUtil.getInstance(grid.connectionProcessor.getSapphireConnection());
    }

    @Override
    void setItems(DataSet items) {
        super.setItems(items);
        this.propertyListMaps = new ArrayList();
        this.items.addColumn(PROPERTYLISTINDEXCOLUMN, 1);
        this.items.setSequence(PROPERTYLISTINDEXCOLUMN);
        for (int i = 0; i < items.size(); ++i) {
            String id = items.getString(i, this.idColumn);
            HashMap<String, PropertyList> propertyListMap = new HashMap<String, PropertyList>();
            for (int type = 0; type < this.grid.taskTypes.size(); ++type) {
                String propertytreeid = (String)this.grid.taskTypes.get(type);
                PropertyList propertyList = new PropertyList();
                propertyList.setUsePropertyValues(true);
                try {
                    String valueTree = this.grid.planProcessor.loadConditionValueTree(this.grid.planid, id, propertytreeid);
                    propertyList.setPropertyList(valueTree, false, "__condition", false);
                    propertyListMap.put(propertytreeid, propertyList);
                    continue;
                }
                catch (Exception e) {
                    Logger.logStackTrace(e);
                }
            }
            this.propertyListMaps.add(propertyListMap);
        }
    }

    public PropertyList getPropertyList(int row, String propertyTreeid) {
        PropertyList propertyList;
        int index = this.items.getInt(row, PROPERTYLISTINDEXCOLUMN);
        if (index >= 0) {
            HashMap map = (HashMap)this.propertyListMaps.get(index);
            propertyList = (PropertyList)map.get(propertyTreeid);
            if (propertyList == null) {
                propertyList = new PropertyList();
                propertyList.setUsePropertyValues(true);
                map.put(propertyTreeid, propertyList);
            }
        } else {
            propertyList = new PropertyList();
            propertyList.setUsePropertyValues(true);
        }
        return propertyList;
    }

    public void setPropertyList(int row, String propertyTreeid, PropertyList propertyList) {
        int index = this.items.getInt(row, PROPERTYLISTINDEXCOLUMN);
        HashMap map1 = (HashMap)this.propertyListMaps.get(index);
        map1.put(propertyTreeid, propertyList);
    }

    public void retrieve(String planid) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select * from schedulecondition where scheduleplanid = " + safeSQL.addVar(planid) + " order by usersequence";
        this.setItems(this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues(), true));
        this.items.addColumn("__readonly", 0);
        safeSQL.reset();
        sql = "select tr.*, sct.scheduleconditionid  from trackitem tr, schedulecondition_trackitem sct where sct.scheduleplanid = " + safeSQL.addVar(planid) + " and tr.trackitemid = sct.trackitemid";
        this.setTrackitems(this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues(), true));
        safeSQL.reset();
        sql = "select sp.scheduleconditionid from s_sample s, scheduleplanitem sp where s.eventplan = sp.scheduleplanid  and s.eventplanitem = sp.scheduleplanitemid and sp.scheduleplanid = " + safeSQL.addVar(planid) + " union  select sp.scheduleconditionid from workorder w,  scheduleplanitem sp  where w.scheduleplanid = sp.scheduleplanid and w.scheduleplanitemid = sp.scheduleplanitemid and  sp.scheduleplanid = " + safeSQL.addVar(planid);
        this.grid.conditionsWithSampleWO = this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        safeSQL.reset();
        sql = "select distinct sp.scheduleconditionid from scheduleplanitem sp, scheduleevent e where e.scheduleplanid = sp.scheduleplanid  and  e.scheduleplanitemid = sp.scheduleplanitemid and sp.scheduleplanid = " + safeSQL.addVar(planid) + " and eventstatus = 'D'";
        this.grid.conditionsWithReadOnlyPlanItem = this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
    }

    public void setTrackitems(DataSet trackItems) {
        if (trackItems != null) {
            for (int r = 0; r < trackItems.getRowCount(); ++r) {
                String qtycurrentType = trackItems.getValue(r, "qtycurrenttype", "");
                String qtyUnits = trackItems.getValue(r, "qtyunits", "");
                if (!qtyUnits.equals("") || !"C".equalsIgnoreCase(qtycurrentType)) continue;
                trackItems.setValue(r, "qtyunits", "(Container)");
            }
            this.trackitems = trackItems;
        }
    }

    public DataSet getTrackitems() {
        return this.trackitems;
    }

    @Override
    public void editItem(int row, HashMap values) {
        for (int i = 0; i < this.items.getColumnCount(); ++i) {
            String columnid = this.items.getColumnId(i);
            if (!values.containsKey(columnid)) continue;
            Trace.logInfo("Setting value: columnid: " + columnid + " value: " + values.get(columnid));
            this.items.setValue(row, columnid, values.get(columnid) == null ? "" : (String)values.get(columnid));
        }
    }

    @Override
    public int copyItem(int row, HashMap values) throws SapphireException {
        String fromid = this.items.getValue(row, this.idColumn);
        int newrow = this.addItem(row + 1, values);
        String toid = this.items.getValue(newrow, this.idColumn);
        this.copyPropertyLists(this.grid, row, newrow);
        this.grid.planItems.copyConditionItems(this.grid, fromid, toid);
        return newrow;
    }

    public void copyPropertyLists(ScheduleGrid useGrid, int fromrow, int torow) throws SapphireException {
        HashMap<String, PropertyList> propertyListMap = new HashMap<String, PropertyList>();
        for (int type = 0; type < this.grid.taskTypes.size(); ++type) {
            String propertytreeid = (String)this.grid.taskTypes.get(type);
            PropertyList fromPropertyList = useGrid.conditionAxis.getPropertyList(fromrow, propertytreeid);
            if (fromPropertyList == null) continue;
            propertyListMap.put(propertytreeid, fromPropertyList.copy());
        }
        this.propertyListMaps.add(propertyListMap);
        this.items.setNumber(torow, PROPERTYLISTINDEXCOLUMN, this.propertyListMaps.size() - 1);
    }

    @Override
    public void save(DBUtil database) throws SapphireException, SQLException {
        if (this.deleteItems.size() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select sp.scheduleconditionid, sp.scheduleplanitemid from s_sample s, scheduleplanitem sp where s.eventplan = sp.scheduleplanid  and s.eventplanitem = sp.scheduleplanitemid and sp.scheduleplanid = " + safeSQL.addVar(this.grid.planid) + " union  select sp.scheduleconditionid, sp.scheduleplanitemid from workorder w,  scheduleplanitem sp  where w.scheduleplanid = sp.scheduleplanid and w.scheduleplanitemid = sp.scheduleplanitemid and  sp.scheduleplanid = " + safeSQL.addVar(this.grid.planid);
            DataSet dsWithSampleWO = this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            for (String conditionId : this.deleteItems) {
                if (dsWithSampleWO.findRow("scheduleconditionid", conditionId) <= -1) continue;
                throw new SapphireException("Study Conditions having reference to Samples/WorkOrders cannot be deleted!");
            }
            String schedPlanSDCId = "SchedulePlan";
            SDCProcessor sdcProcessor = new SDCProcessor(this.connectionInfo.getConnectionId());
            boolean isAuditTableExist = !sdcProcessor.getProperty(schedPlanSDCId, "auditedflag").equalsIgnoreCase("N");
            String name1 = "delete " + this.tableid + "axisproertytreees";
            String sql1 = "DELETE FROM scheduleconditiondefaults WHERE scheduleplanid = ? AND scheduleconditionid = ?";
            PreparedStatement ps1 = database.prepareStatement(name1, sql1);
            Iterator it = this.deleteItems.iterator();
            while (it.hasNext()) {
                ps1.setString(1, this.grid.planid);
                ps1.setString(2, (String)it.next());
                ps1.execute();
            }
            database.closeStatement(name1);
            String name2 = "delete " + this.tableid + "_trackitems";
            String sql2 = "DELETE FROM schedulecondition_trackitem WHERE scheduleplanid = ? AND scheduleconditionid = ?";
            PreparedStatement ps2 = database.prepareStatement(name2, sql2);
            StringBuffer updateAuditSql = new StringBuffer();
            if (isAuditTableExist) {
                updateAuditSql.append("UPDATE a_schedulecondition_trackitem SET modby = '" + this.connectionInfo.getSysuserId() + "', modtool = 'DELETE', moddt = {ts '" + DateTimeUtil.getNowTimestamp() + "'}, tracelogid = '" + this.grid.traceLogId + "'").append(" WHERE scheduleplanid = '" + this.grid.planid + "' AND scheduleconditionid = ? AND tracelogid = 'DELETED' AND ").append(" auditsequence = ( SELECT max( auditsequence ) FROM a_schedulecondition_trackitem WHERE scheduleplanid='" + this.grid.planid + "' AND scheduleconditionid = ? AND tracelogid = 'DELETED')");
            }
            Iterator it2 = this.deleteItems.iterator();
            while (it2.hasNext()) {
                ps2.setString(1, this.grid.planid);
                String conditionid = (String)it2.next();
                ps2.setString(2, conditionid);
                ps2.execute();
                if (!isAuditTableExist || this.grid.traceLogId == null || this.grid.traceLogId.trim().length() <= 0) continue;
                try {
                    database.executePreparedUpdate(updateAuditSql.toString(), new Object[]{conditionid, conditionid});
                }
                catch (Exception ex) {
                    String errorMsg = ex.getMessage();
                    int errIndx = database.isOracle() ? errorMsg.indexOf("942") : errorMsg.indexOf("240");
                    if (errIndx >= 0) continue;
                    throw new SapphireException("DB_UPDATE_FAILED", ex);
                }
            }
            database.closeStatement(name2);
        }
        super.save(database);
        for (int row = 0; row < this.items.size(); ++row) {
            String id = this.items.getValue(row, this.idColumn);
            for (int type = 0; type < this.grid.taskTypes.size(); ++type) {
                String propertytreeid = (String)this.grid.taskTypes.get(type);
                PropertyList pl = this.getPropertyList(row, propertytreeid);
                String valuetree = pl == null ? "<propertylist />" : pl.toXMLString();
                try {
                    String sql = "UPDATE scheduleconditiondefaults set valuetree = ?, modtool = '" + this.connectionInfo.getTool() + "' WHERE scheduleplanid = ? AND scheduleconditionid=? AND propertytreeid=? ";
                    PreparedStatement ps = database.getConnection().prepareStatement(sql);
                    ps.setCharacterStream(1, (Reader)new StringReader(valuetree), valuetree.length());
                    ps.setString(2, this.grid.planid);
                    ps.setString(3, id);
                    ps.setString(4, propertytreeid);
                    if (ps.executeUpdate() == 0) {
                        String sql2 = "\tINSERT INTO scheduleconditiondefaults ( valuetree, scheduleplanid, scheduleconditionid, propertytreeid, modtool ) values ( ?, ?, ?, ?, '" + this.connectionInfo.getTool() + "' )";
                        PreparedStatement ps2 = database.getConnection().prepareStatement(sql2);
                        ps2.setCharacterStream(1, (Reader)new StringReader(valuetree), valuetree.length());
                        ps2.setString(2, this.grid.planid);
                        ps2.setString(3, id);
                        ps2.setString(4, propertytreeid);
                        ps2.executeUpdate();
                        ps2.close();
                    }
                    ps.close();
                    continue;
                }
                catch (Exception e) {
                    throw new SapphireException("Unable to update tree: " + id + ";" + propertytreeid, ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                }
            }
        }
    }

    public String getStatus(int row) {
        return this.items.getValue(row, "conditionstatus");
    }

    @Override
    public boolean isReadOnly(int row) {
        String readonly = this.items.getValue(row, "__readonly");
        if (readonly.equals("")) {
            String status = this.items.getValue(row, "conditionstatus");
            readonly = status.equals("R") ? "Y" : "N";
            this.items.setObject(row, "__readonly", readonly);
        }
        return readonly.equals("Y");
    }

    public boolean isConditionReadOnly(int row) {
        String readonly = "";
        String status = this.items.getValue(row, "conditionstatus");
        readonly = status.equals("R") || status.equals("C") || status.equals("X") ? "Y" : "N";
        this.items.setObject(row, "__readonly", readonly);
        return readonly.equals("Y");
    }

    @Override
    public int addItem(int beforeRow, HashMap values) {
        int row = super.addItem(beforeRow, values);
        HashMap propertyListMap = new HashMap();
        this.propertyListMaps.add(propertyListMap);
        this.items.setNumber(row, PROPERTYLISTINDEXCOLUMN, this.propertyListMaps.size() - 1);
        return row;
    }

    @Override
    public void deleteItem(int row) {
        String itemid = this.items.getValue(row, this.idColumn);
        this.grid.planItems.deleteConditionItems(itemid);
        int index = this.items.getInt(row, PROPERTYLISTINDEXCOLUMN);
        this.propertyListMaps.set(index, null);
        super.deleteItem(row);
    }

    public HashMap getPullAmounts(int row) {
        HashMap<String, Double> values = new HashMap<String, Double>();
        String id = this.items.getValue(row, this.idColumn);
        ArrayList items = this.grid.planItems.findByCondition(id);
        for (PlanItem item : items) {
            if (item.getInstancePullAmount() == null) continue;
            double quantity = item.getInstancePullAmount().getQuantity(item);
            String units = item.getInstancePullAmount().getUnits(item);
            double d = values.get(units) == null ? 0.0 : (Double)values.get(units);
            values.put(units, new Double(d + quantity));
        }
        return values;
    }

    public int getTotalContainers(double containerSize, String containerSizeUnit, boolean ppFlag, StringBuffer log) {
        int numContainers = 0;
        try {
            for (int j = 0; j < this.items.size(); ++j) {
                String conditionId = this.items.getValue(j, "scheduleconditionid");
                numContainers += this.getContainersForCondition(conditionId, containerSize, containerSizeUnit, ppFlag, true, log);
            }
        }
        catch (Exception ex) {
            Logger.logStackTrace(ex);
            numContainers = -1;
        }
        TranslationProcessor tp = new TranslationProcessor(this.grid.getConnectionid());
        HashMap<String, String> token = new HashMap<String, String>();
        token.put("numcontainers", "<b>" + numContainers + " " + (numContainers == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b>");
        log.append("<br><table border=1 cellspacing=0 cellpadding=10 bgcolor=lightsteelblue width=600><tr><td>" + tp.translate("Total inventory for all conditions of the plan is [numcontainers]", token) + "</td></tr></table><br><br>\n");
        return numContainers;
    }

    public int getContainersForCondition(String conditionid, double containerSize, String containerSizeUnit, boolean ppFlag, boolean calculateTotal, StringBuffer log) {
        int totalContainers = 0;
        int thisConditionRow = this.findRow(conditionid);
        TranslationProcessor tp = new TranslationProcessor(this.grid.getConnectionid());
        if (thisConditionRow > -1) {
            String quantityType = this.items.getValue(thisConditionRow, "qtypulltype", "");
            boolean autoCalc = !this.items.getValue(thisConditionRow, "autocalcflag", "N").equals("N");
            int qtyPull = this.items.getInt(thisConditionRow, "qtypull", 0);
            String qtyPullUnit = this.items.getValue(thisConditionRow, "qtypullunits", "");
            log.append("<table border=1 cellpadding=5 cellspacing=0 width=600>\n");
            log.append("<tr style=\"background-color:burlywood\"><td><b>" + tp.translate("Condition") + ": " + this.items.getValue(thisConditionRow, "conditionlabel", "[conditionlabel]") + "</b></td></tr>\n");
            log.append("<tr><td><table cellspacing=0 cellpadding=5 border=1><tr><td>" + tp.translate("QuantityType") + " = " + (quantityType.equals("C") ? tp.translate("Containers") : tp.translate("Raw Units")) + "</td>");
            log.append("<td>" + tp.translate("AutoCalc") + " = " + autoCalc + "</td>");
            log.append("<td>" + tp.translate("PullQuantity") + " = " + qtyPull + "</td>");
            log.append("<td>" + tp.translate("PullQuantityUnit") + " = " + qtyPullUnit + "</td></tr></table></td></tr>\n");
            log.append("<tr><td>\n");
            if (calculateTotal) {
                if (ppFlag) {
                    if (autoCalc) {
                        totalContainers = this.getConditionPlanItemContainers(conditionid, containerSize, containerSizeUnit, ppFlag, calculateTotal, log);
                    } else if (quantityType.equals("C")) {
                        HashMap<String, String> token = new HashMap<String, String>();
                        token.put("qtypull", "<b>" + qtyPull + " " + (qtyPull == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b>");
                        log.append(tp.translate("Condition declared as requiring [qtypull]", token) + "<br>\n");
                        totalContainers = qtyPull;
                    } else if (quantityType.equals("U")) {
                        if (qtyPullUnit.equals(containerSizeUnit)) {
                            HashMap<String, String> token = new HashMap<String, String>();
                            token.put("qtypull", "" + qtyPull);
                            token.put("qtypullunit", qtyPullUnit);
                            if (containerSize > 0.0) {
                                totalContainers = (int)Math.ceil((double)qtyPull / containerSize);
                                token.put("totalcontainers", "<b>" + totalContainers + " " + (totalContainers == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b>");
                                log.append(tp.translate("Conditon requires [qtypull] [qtypullunit] = [totalcontainers]", token) + "<br>\n");
                            } else {
                                totalContainers = 0;
                                log.append(tp.translate("Conditon requires [qtypull] [qtypullunit]. Unable to determine the number of containers.", token) + "<br>\n");
                            }
                        } else {
                            HashMap<String, String> token = new HashMap<String, String>();
                            token.put("qtypull", "" + qtyPull);
                            token.put("qtypullunit", qtyPullUnit);
                            if (containerSize > 0.0) {
                                try {
                                    String convertedAmount = ConvertUnits.convertUnits(this.grid.queryProcessor, qtyPullUnit, containerSizeUnit, "" + qtyPull);
                                    totalContainers = (int)Math.ceil(Double.parseDouble(convertedAmount) / containerSize);
                                    token.put("totalcontainers", "<b>" + totalContainers + " " + (totalContainers == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b>");
                                    log.append(tp.translate("Conditon requires [qtypull] [qtypullunit] = [totalcontainers]", token) + "<br>\n");
                                }
                                catch (Exception e) {
                                    log.append("Error: " + e.getMessage() + "<BR>");
                                }
                            } else {
                                totalContainers = 0;
                                log.append(tp.translate("Conditon requires [qtypull] [qtypullunit]. Unable to determine the number of containers.", token) + "<br>\n");
                            }
                        }
                    }
                } else if (autoCalc) {
                    totalContainers = this.getConditionPlanItemContainers(conditionid, containerSize, containerSizeUnit, ppFlag, calculateTotal, log);
                } else if (quantityType.equals("C")) {
                    HashMap<String, String> token = new HashMap<String, String>();
                    token.put("qtypull", "<b>" + qtyPull + " " + (qtyPull == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b>");
                    log.append(tp.translate("Condition declared as requiring [qtypull]", token) + "<br>\n");
                    totalContainers = qtyPull;
                } else if (quantityType.equals("U")) {
                    totalContainers = this.getConditionPlanItemContainers(conditionid, containerSize, containerSizeUnit, ppFlag, calculateTotal, log);
                }
            } else {
                totalContainers = this.getConditionPlanItemContainers(conditionid, containerSize, containerSizeUnit, ppFlag, calculateTotal, log);
            }
            log.append("</td></tr>\n");
            log.append("</table>\n");
        }
        return totalContainers;
    }

    private int getConditionPlanItemContainers(String conditionid, double containerSize, String containerSizeUnit, boolean ppFlag, boolean calculateTotal, StringBuffer log) {
        boolean addReserverToPartial;
        int totalContainers = 0;
        ArrayList planItems = this.grid.planItems.findByCondition(conditionid);
        this.grid.planItems.sortByTime(planItems);
        double partialPullTotal = 0.0;
        TranslationProcessor tp = new TranslationProcessor(this.grid.getConnectionid());
        HashMap<String, String> token = new HashMap<String, String>();
        for (int k = 0; k < planItems.size(); ++k) {
            String displayUnit;
            String displayAmount;
            StringBuffer planItemLog = new StringBuffer();
            PlanItem planItem = (PlanItem)planItems.get(k);
            if (!calculateTotal && !"S".equals(planItem.eventStatus) && planItem.hasEvent) continue;
            HashMap hmInventory = planItem.getRequiredAmount(containerSize, containerSizeUnit, ppFlag, calculateTotal, planItemLog);
            double amount = Double.parseDouble(hmInventory.get("amount").toString());
            String amountUnit = hmInventory.get("amountunit").toString();
            if (!(amount >= 0.0)) continue;
            if ("(Containers)".equals(amountUnit)) {
                displayAmount = "" + (int)amount;
                displayUnit = " Containers";
            } else {
                displayAmount = this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(amount)) : "" + amount;
                displayUnit = amountUnit;
            }
            token.put("propertytreeid", planItem.propertyTreeid);
            token.put("timerulelabel", this.grid.timeAxis.getLabel(planItem.timeruleid));
            token.put("displayamountunit", "<b>" + displayAmount + " " + displayUnit + "</b>");
            log.append(tp.translate("[propertytreeid] at [timerulelabel] requires [displayamountunit]", token));
            if (hmInventory.get("samplecount") != null) {
                try {
                    int sampleCount = Integer.parseInt(hmInventory.get("samplecount").toString());
                    amount *= (double)sampleCount;
                }
                catch (NumberFormatException sampleCount) {
                    // empty catch block
                }
            }
            if (planItemLog.length() > 0) {
                log.append(" <span style=\"color: blue; cursor: pointer\" onclick=\"var x=document.getElementById('detail_" + conditionid + "_" + k + "' );x.style.display=x.style.display=='none'?'block':'none';\"> (" + tp.translate("Details") + "...)</span>");
            }
            log.append("<br>");
            if (planItemLog.length() > 0) {
                log.append("<div id=\"detail_" + conditionid + "_" + k + "\" style=\"margin-left: 20px; display: none\">" + planItemLog + "</div>");
            }
            if (ppFlag) {
                if ("(Containers)".equals(amountUnit)) {
                    totalContainers += (int)amount;
                    continue;
                }
                if (containerSizeUnit.equals(amountUnit)) {
                    BigDecimal partialPullTotalAmount = new BigDecimal(partialPullTotal).setScale(2, RoundingMode.HALF_UP);
                    partialPullTotalAmount = partialPullTotalAmount.add(new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP));
                    partialPullTotal = partialPullTotalAmount.doubleValue();
                    continue;
                }
                try {
                    String convertedAmt = ConvertUnits.convertUnits(this.grid.queryProcessor, amountUnit, containerSizeUnit, "" + amount);
                    partialPullTotal += Double.parseDouble(convertedAmt);
                }
                catch (Exception e) {
                    log.append("<span style=\"margin-left: 20px; color: red\"> " + tp.translate("Error: Unable to process this amount") + e.getMessage() + "</span><br>");
                }
                continue;
            }
            if ("(Containers)".equals(amountUnit)) {
                totalContainers += (int)amount;
                continue;
            }
            if (containerSizeUnit.equals(amountUnit)) {
                if (containerSize > 0.0) {
                    int containers = (int)Math.ceil(amount / containerSize);
                    token.clear();
                    token.put("containers", containers + " " + (containers == 1 ? tp.translate("Container") : tp.translate("Containers")));
                    log.append("<span style=\"margin-left: 20px\">" + tp.translate("Non-partial pull means this must be [containers]", token) + "</span><br>");
                    totalContainers += containers;
                    continue;
                }
                token.clear();
                token.put("amountandunit", (this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(amount)) : Double.valueOf(amount)) + amountUnit);
                log.append(tp.translate("Task requires [amountandunit]. Unable to determine the number of containers.", token)).append("<br>\n");
                continue;
            }
            log.append("<span style=\"margin-left: 20px; color: red\">" + tp.translate("Unable to process this amount") + "</span><br>");
        }
        int conditionRow = this.findRow(conditionid);
        String reserveType = this.items.getValue(conditionRow, "qtyreservetype");
        String reserveUnits = this.items.getValue(conditionRow, "qtyreserveunits");
        BigDecimal reserveDecimal = this.items.getBigDecimal(conditionRow, "qtyreserve");
        double reserve = reserveDecimal == null ? -1.0 : reserveDecimal.doubleValue();
        boolean bl = addReserverToPartial = calculateTotal && partialPullTotal > 0.0 && reserve > 0.0 && !reserveType.equals("C") && (totalContainers == 0 || "U".equals(reserveType));
        if (partialPullTotal > 0.0) {
            if (addReserverToPartial) {
                if ("U".equals(reserveType)) {
                    if (containerSizeUnit.equals(reserveUnits)) {
                        partialPullTotal += reserve;
                    } else {
                        try {
                            String convertedReserveAmount = ConvertUnits.convertUnits(this.grid.queryProcessor, reserveUnits, containerSizeUnit, "" + reserve);
                            double convReserveAmt = Double.parseDouble(convertedReserveAmount);
                            partialPullTotal += convReserveAmt;
                        }
                        catch (Exception e) {
                            log.append("Error: " + e.getMessage() + "<BR>");
                        }
                    }
                    token.clear();
                    String reserveamtunit = this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(reserve)) : "" + reserve;
                    reserveamtunit = reserveamtunit + " " + reserveUnits;
                    token.put("reserveamountandunit", reserveamtunit);
                    log.append("<span style=\"\"><b>" + tp.translate("Reserve: [reserveamountandunit] added", token) + "</b></span><br>");
                } else if ("P".equals(reserveType)) {
                    double reserveAmount = this.sigfig(partialPullTotal * reserve / 100.0, 2);
                    partialPullTotal += reserveAmount;
                    token.clear();
                    String reserveamtunit = this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(reserve)) : "" + reserve;
                    reserveamtunit = reserveamtunit + "% = ";
                    reserveamtunit = reserveamtunit + (this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(reserveAmount)) : "" + reserveAmount);
                    reserveamtunit = reserveamtunit + " " + reserveUnits;
                    token.put("reserveamountandunit", reserveamtunit);
                    log.append("<span style=\"\"><b>" + tp.translate("Reserve: [reserveamountandunit] added", token) + "</b></span><br>");
                }
            }
            if (containerSize > 0.0) {
                int containers = (int)Math.ceil(partialPullTotal / containerSize);
                totalContainers += containers;
                token.clear();
                String ppamtunit = this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(partialPullTotal)) : "" + partialPullTotal;
                ppamtunit = ppamtunit + " " + containerSizeUnit + " = " + containers + " " + (containers == 1 ? tp.translate("Container") : tp.translate("Containers"));
                token.put("ppamtunit", ppamtunit);
                log.append("<span style=\"\"><b>" + tp.translate("Total number of Partial Pulls = [ppamtunit]", token) + "</b></span><br>");
            } else {
                token.clear();
                String pptotal = this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(partialPullTotal)) : "" + partialPullTotal;
                token.put("pptotal", pptotal);
                token.put("containerSizeUnit", containerSizeUnit);
                log.append(tp.translate("Total number of Partial Pulls = [pptotal] [containerSizeUnit]. Unable to determine the number of containers.", token)).append("<br>\n");
            }
        }
        if (calculateTotal && reserve > 0.0 && !addReserverToPartial) {
            if ("C".equals(reserveType)) {
                totalContainers = (int)((double)totalContainers + reserve);
                token.clear();
                token.put("reserve", "" + (int)reserve + " " + ((int)reserve == 1 ? tp.translate("Container") : tp.translate("Containers")));
                log.append("<span style=\"\"><b>").append(tp.translate("Reserve: [reserve] added", token)).append("</b></span><br>");
            } else if ("P".equals(reserveType)) {
                int reserveAmount = (int)Math.ceil((double)totalContainers * reserve / 100.0);
                totalContainers += reserveAmount;
                token.clear();
                String reserveamtunit = this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(reserve)) : "" + reserve;
                reserveamtunit = reserveamtunit + "% = " + reserveAmount + " " + (reserveAmount == 1 ? tp.translate("Container") : tp.translate("Containers"));
                token.put("reserve", reserveamtunit);
                log.append("<span style=\"\"><b>" + tp.translate("Reserve: [reserve] added", token) + "</b></span><br>");
            } else if ("U".equals(reserveType)) {
                if (containerSize > 0.0) {
                    if (containerSizeUnit.equals(reserveUnits)) {
                        int containers = (int)Math.ceil(reserve / containerSize);
                        totalContainers += containers;
                        log.append("<span style=\"\"><b>" + tp.translate("Reserve") + ": ").append(this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(reserve)) : Double.valueOf(reserve)).append(" " + reserveUnits + " = " + containers + " " + (containers == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b></span><br>");
                    } else {
                        try {
                            String convertedReserveAmount = ConvertUnits.convertUnits(this.grid.queryProcessor, reserveUnits, containerSizeUnit, "" + reserve);
                            double convReserveAmt = Double.parseDouble(convertedReserveAmount);
                            int containers = (int)Math.ceil(convReserveAmt / containerSize);
                            totalContainers += containers;
                            log.append("<span style=\"\"><b>" + tp.translate("Reserve") + ": ").append(this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(reserve)) : Double.valueOf(reserve)).append(" " + reserveUnits + " = " + containers + " " + (containers == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b></span><br>");
                        }
                        catch (Exception e) {
                            log.append("Error: " + e.getMessage() + "<BR>");
                        }
                    }
                } else {
                    log.append(tp.translate("Reserve") + ": ").append(this.fmutil != null ? this.fmutil.format(BigDecimal.valueOf(reserve)) : Double.valueOf(reserve)).append(" " + reserveUnits + "." + tp.translate("Unable to determine the number of containers.") + "<br>\n");
                }
            }
        }
        token.put("totalcontainers", "<b>" + totalContainers + " " + (totalContainers == 1 ? tp.translate("Container") : tp.translate("Containers")) + "</b>");
        log.append("<div style=\"background-color:khaki;padding:2px\">" + tp.translate("Total inventory for this condition is [totalcontainers]", token) + "</div><br>\n");
        return totalContainers;
    }

    public void copyAllFromTemplate(String planid) throws Exception {
        this.copyItemFromTemplate(planid, "");
    }

    public void copyItemFromTemplate(String planList, String conditionList) throws Exception {
        this.copyItemFromTemplate(planList, conditionList, null);
    }

    public void copyItemFromTemplate(String planList, String conditionList, Boolean includeAllTimePoints) throws Exception {
        ScheduleGrid templateGrid = null;
        boolean addAll = false;
        if (includeAllTimePoints != null && includeAllTimePoints.booleanValue()) {
            addAll = true;
        }
        if (conditionList == null || conditionList.length() == 0) {
            addAll = true;
            String planid = planList;
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select scheduleconditionid from schedulecondition where scheduleplanid = " + safeSQL.addVar(planid) + " order by usersequence";
            DataSet templateconditions = this.grid.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (templateconditions.size() > 0) {
                planList = "";
                conditionList = "";
                for (int i = 0; i < templateconditions.size(); ++i) {
                    planList = planList + ";" + planid;
                    conditionList = conditionList + ";" + templateconditions.getString(i, "scheduleconditionid");
                }
                planList = planList.substring(1);
                conditionList = conditionList.substring(1);
            }
        }
        if (planList != null && planList.length() > 0) {
            String[] plans = StringUtil.split(planList, ";");
            String[] conditions = StringUtil.split(conditionList, ";");
            String conditionid = "";
            for (int i = 0; i < plans.length; ++i) {
                String planid = plans[i];
                if (conditionList.length() > 0) {
                    conditionid = conditions[i];
                }
                if (templateGrid == null || !templateGrid.planid.equals(planid)) {
                    templateGrid = new ScheduleGrid(this.grid.getRakFile(), this.grid.getConnectionId());
                    templateGrid.retrieve(planid);
                    this.grid.workItems.copyAll(templateGrid.workItems.items);
                }
                if (conditionid.length() > 0) {
                    int templateRow = templateGrid.conditionAxis.findRow(conditionid);
                    HashMap<String, String> values = new HashMap<String, String>();
                    values.put("storageenvid", templateGrid.conditionAxis.items.getValue(templateRow, "storageenvid"));
                    values.put("conditionlabel", templateGrid.conditionAxis.items.getValue(templateRow, "conditionlabel"));
                    values.put("orientation", templateGrid.conditionAxis.items.getValue(templateRow, "orientation"));
                    values.put("notes", templateGrid.conditionAxis.items.getValue(templateRow, "notes"));
                    values.put("startcriteria", templateGrid.conditionAxis.items.getValue(templateRow, "startcriteria"));
                    values.put("qtypull", templateGrid.conditionAxis.items.getValue(templateRow, "qtypull"));
                    values.put("qtypullunits", templateGrid.conditionAxis.items.getValue(templateRow, "qtypullunits"));
                    values.put("qtypulltype", templateGrid.conditionAxis.items.getValue(templateRow, "qtypulltype"));
                    values.put("qtyreserve", templateGrid.conditionAxis.items.getValue(templateRow, "qtyreserve"));
                    values.put("qtyreserveunits", templateGrid.conditionAxis.items.getValue(templateRow, "qtyreserveunits"));
                    values.put("qtyreservetype", templateGrid.conditionAxis.items.getValue(templateRow, "qtyreservetype"));
                    values.put("includetimerulesflag", templateGrid.conditionAxis.items.getValue(templateRow, "includetimerulesflag"));
                    values.put("templatedesc", templateGrid.conditionAxis.items.getValue(templateRow, "templatedesc"));
                    values.put("autocalcflag", templateGrid.conditionAxis.items.getValue(templateRow, "autocalcflag"));
                    values.put("startdt", templateGrid.conditionAxis.items.getValue(templateRow, "startdt"));
                    int newrow = this.addItem(values);
                    String newid = this.items.getValue(newrow, this.idColumn);
                    if (addAll || templateGrid.conditionAxis.items.getValue(templateRow, "includetimerulesflag").equals("Y")) {
                        HashMap<String, String> findRule = new HashMap<String, String>();
                        for (int row = 0; row < templateGrid.timeAxis.items.size(); ++row) {
                            String scheduleRule = templateGrid.timeAxis.items.getValue(row, "schedulerule");
                            findRule.put("schedulerule", scheduleRule);
                            if (this.grid.timeAxis.items.findRow(findRule) == -1) {
                                String scheduleRuleLabel = templateGrid.timeAxis.items.getValue(row, "schedulerulelabel");
                                String executeAhead = templateGrid.timeAxis.items.getValue(row, "executeahead");
                                String executeAheadUnits = templateGrid.timeAxis.items.getValue(row, "executeaheadunits");
                                findRule.put("schedulerulelabel", scheduleRuleLabel);
                                findRule.put("executeahead", executeAhead);
                                findRule.put("executeaheadunits", executeAheadUnits);
                                this.grid.timeAxis.addItem(findRule);
                            }
                            findRule.clear();
                        }
                    }
                    this.copyPropertyLists(templateGrid, templateRow, newrow);
                    this.grid.planItems.copyConditionItems(templateGrid, conditionid, newid);
                    continue;
                }
                HashMap<String, String> findRule = new HashMap<String, String>();
                for (int row = 0; row < templateGrid.timeAxis.items.size(); ++row) {
                    String scheduleRule = templateGrid.timeAxis.items.getValue(row, "schedulerule");
                    findRule.put("schedulerule", scheduleRule);
                    if (this.grid.timeAxis.items.findRow(findRule) == -1) {
                        String scheduleRuleLabel = templateGrid.timeAxis.items.getValue(row, "schedulerulelabel");
                        String executeAhead = templateGrid.timeAxis.items.getValue(row, "executeahead");
                        String executeAheadUnits = templateGrid.timeAxis.items.getValue(row, "executeaheadunits");
                        findRule.put("schedulerulelabel", scheduleRuleLabel);
                        findRule.put("executeahead", executeAhead);
                        findRule.put("executeaheadunits", executeAheadUnits);
                        this.grid.timeAxis.addItem(findRule);
                    }
                    findRule.clear();
                }
            }
        }
        templateGrid = null;
    }

    public String toXML(String conditionid) {
        ArrayList planItems;
        StringBuffer xml = new StringBuffer();
        xml.append("<condition>\n");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("scheduleconditionid", conditionid);
        DataSet details = this.items.getFilteredDataSet(filter);
        DataSet trackItems = this.trackitems.getFilteredDataSet(filter);
        if (details.size() == 1) {
            xml.append("<details>\n");
            xml.append(details.toXML(false, false));
            xml.append("</details>\n");
        }
        if (trackItems != null) {
            xml.append("<trackitems>\n");
            xml.append(trackItems.toXML(false, false));
            xml.append("</trackitems>\n");
        }
        if ((planItems = this.grid.planItems.findByCondition(conditionid)).size() > 0) {
            this.grid.planItems.sortByTime(planItems);
            xml.append("<planitems>\n");
            for (PlanItem planItem : planItems) {
                String id = planItem.planItemid;
                String timepoint = this.grid.timeAxis.getLabel(planItem.timeruleid);
                String propertyTree = planItem.propertyTreeid;
                PropertyList propertyList = planItem.getCollapsedPropertyList();
                DataSet workItems = planItem.getWorkitems();
                xml.append("<planitem id=\"" + id + "\" timepoint=\"" + timepoint + "\" propertytreeid=\"" + propertyTree + "\">\n");
                xml.append("<properties>\n");
                xml.append(propertyList.toXMLString());
                xml.append("</properties>\n");
                xml.append("<workitems>\n");
                xml.append(workItems.toXML(false, false));
                xml.append("</workitems>\n");
                xml.append("</planitem>\n");
            }
            xml.append("</planitems>\n");
        }
        xml.append("</condition>\n");
        return xml.toString();
    }

    private double sigfig(double value, int sigfigs) {
        double LOG10 = Math.log(10.0);
        int btl = (int)Math.ceil(Math.log(value) / LOG10);
        double nf = Math.pow(10.0, btl -= sigfigs);
        double newValue = value / nf;
        newValue = Math.rint(newValue);
        return newValue *= nf;
    }
}

