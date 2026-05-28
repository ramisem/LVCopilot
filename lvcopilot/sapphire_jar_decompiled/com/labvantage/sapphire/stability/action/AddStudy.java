/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.action;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.stability.ScheduleGridUtil;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;

public class AddStudy
extends BaseAction
implements sapphire.action.AddStudy {
    public static final String DEFAULT_TRACKINGTYPEFLAG = "C";

    @Override
    public int processAction(String actionid, String actionversionid, HashMap properties) {
        int rc = 1;
        String protocolid = (String)properties.get("protocolid");
        String protocolversionid = (String)properties.get("protocolversionid");
        String productsdcid = null;
        String productkeyid1 = null;
        String productkeyid2 = null;
        String productkeyid3 = null;
        try {
            boolean useProtocolProduct = false;
            if (protocolid != null && protocolid.length() > 0) {
                productsdcid = (String)properties.remove("productsdcid");
                productkeyid1 = (String)properties.remove("productkeyid1");
                productkeyid2 = (String)properties.remove("productkeyid2");
                productkeyid3 = (String)properties.remove("productkeyid3");
                if (productsdcid != null && productsdcid.length() > 0 && productkeyid1 != null && productkeyid1.length() > 0) {
                    String containertypeid;
                    useProtocolProduct = true;
                    String columnids = this.getConnectionProcessor().getProfileProperty("studyproductcolumnid");
                    String sdcids = this.getConnectionProcessor().getProfileProperty("protocolproductsdcid");
                    String[] columnList = columnids.split(";");
                    String[] sdcList = sdcids.split(";");
                    String columnid = "";
                    for (int k = 0; k < sdcList.length; ++k) {
                        if (!sdcList[k].equalsIgnoreCase(productsdcid)) continue;
                        columnid = columnList[k];
                        break;
                    }
                    if (columnid.length() > 0) {
                        String currentProduct = (String)properties.get(columnid);
                        if (currentProduct == null || currentProduct.length() == 0) {
                            properties.put(columnid, productkeyid1);
                            useProtocolProduct = true;
                        } else {
                            properties.put(columnid, currentProduct);
                        }
                    }
                    if ((containertypeid = (String)properties.get("containertypeid")) == null || containertypeid.length() == 0) {
                        String sql = "SELECT containertypeid, trackingtypeflag, partialpullflag FROM protocolproduct WHERE protocolid = ? AND protocolversionid = ? AND linksdcid = ? AND linkkeyid1 = ?";
                        this.database.createPreparedResultSet(sql, new Object[]{protocolid, protocolversionid, productsdcid, productkeyid1});
                        if (this.database.getNext()) {
                            properties.put("containertypeid", this.database.getString("containertypeid"));
                            properties.put("partialpullflag", this.database.getString("partialpullflag") != null ? this.database.getString("partialpullflag") : "Y");
                            properties.put("trackingtypeflag", this.database.getString("trackingtypeflag") != null ? this.database.getString("trackingtypeflag") : DEFAULT_TRACKINGTYPEFLAG);
                        }
                    }
                }
            }
            properties.put("sdcid", "StudySDC");
            String studyStatus = (String)properties.get("studystatus");
            if (studyStatus == null || studyStatus.length() == 0) {
                properties.put("studystatus", "N");
            }
            this.getActionProcessor().processAction("AddSDI", "1", properties);
            String studyid = (String)properties.get("newkeyid1");
            if (useProtocolProduct) {
                String sql = "SELECT scheduleplanid FROM protocolprod_scheduleplan, protocolproduct WHERE protocolproduct.protocolid = protocolprod_scheduleplan.protocolid AND protocolproduct.protocolversionid = protocolprod_scheduleplan.protocolversionid AND protocolproduct.protocolproductid = protocolprod_scheduleplan.protocolproductid AND protocolproduct.protocolid = ? AND protocolproduct.protocolversionid = ? AND protocolproduct.linksdcid = ? AND protocolproduct.linkkeyid1 = ?";
                this.database.createPreparedResultSet(sql, new Object[]{protocolid, protocolversionid, productsdcid, productkeyid1});
                DataSet plans = new DataSet(this.database.getResultSet());
                String insert = "INSERT INTO study_scheduleplan ( studyid, scheduleplanid, usersequence, createdt, createby, createtool ) values (?, ?, ?, ?, ?, ? )";
                PreparedStatement statement = this.database.prepareStatement("Insert study_scheduleplan", insert);
                statement.setString(1, studyid);
                statement.setTimestamp(4, DateTimeUtil.getNowTimestamp());
                statement.setString(5, this.connectionInfo.getSysuserId());
                statement.setString(6, "Protocol");
                for (int i = 0; i < plans.size(); ++i) {
                    String planid = plans.getString(i, "scheduleplanid");
                    String newplanid = ScheduleGridUtil.copyPlan(this.connectionInfo.getConnectionId(), planid, "Y");
                    statement.setString(2, newplanid);
                    statement.setInt(3, i + 1);
                    statement.executeUpdate();
                }
                this.database.closeStatement("Insert study_scheduleplan");
            }
        }
        catch (Exception e) {
            rc = 2;
            this.setError("Failed to create Study: " + e.getMessage(), "PROCESSACTION_FAILED");
        }
        return rc;
    }
}

