/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.LV_Box;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class DisposeRule
extends BaseBioBankRule {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54060 $";

    public DisposeRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    @Override
    public String getRuleId() {
        return "Dispose Rule";
    }

    public void processRule(String sdcid, String keyid1) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return;
        }
        if (this.isRuleActive()) {
            if ("LV_Box".equals(sdcid)) {
                this.processBoxDisposeRule(keyid1);
            } else if ("Plate".equals(sdcid)) {
                this.processPlateDisposeRule(keyid1);
            }
        }
    }

    private void processPlateDisposeRule(String plateid) throws SapphireException {
        List list = this.isCustodian("Plate", plateid);
        StringBuffer sb = new StringBuffer();
        if (list.size() > 0) {
            sb.append("<br>");
            sb.append("{{You are not the custodian of following Plate(s)}}");
            sb.append("<ul>");
            for (int i = 0; i < list.size(); ++i) {
                sb.append("<li>").append(list.get(i)).append("</li>");
            }
            sb.append("</ul>");
        }
        if (sb.length() > 0) {
            sb.insert(0, "{{Disposing of Plate(s) not allowed}}");
            throw new SapphireException("PlateDisposeRule", "VALIDATION", this.getTranslationProcessor().translatePartial(sb.toString()));
        }
    }

    private void processBoxDisposeRule(String boxid) throws SapphireException {
        List list = this.isCustodian("LV_Box", boxid);
        StringBuffer sb = new StringBuffer();
        if (list.size() > 0) {
            sb.append("<br>");
            sb.append("{{You are not the custodian of following Box(es)}}");
            sb.append("<ul>");
            for (int i = 0; i < list.size(); ++i) {
                sb.append("<li>").append(list.get(i)).append("</li>");
            }
            sb.append("</ul>");
        }
        String[] box = StringUtil.split(boxid, ";");
        StringBuffer sbEmpty = new StringBuffer();
        for (int i = 0; i < box.length; ++i) {
            DataSet ds = LV_Box.getTrackItems(this.getQueryProcessor(), box[i]);
            if (ds == null || ds.size() <= 0) continue;
            sbEmpty.append("<li>").append(box[i]).append("</li>");
        }
        if (sbEmpty.length() > 0) {
            sbEmpty.insert(0, "<br>{{Following Box(es) are not empty}}<ul>");
            sbEmpty.append("</ul>");
            sb.append(sbEmpty);
        }
        if (sb.length() > 0) {
            sb.insert(0, "{{Disposing of Box(es) not allowed}}");
            throw new SapphireException("BoxDisposeRule", "VALIDATION", this.getTranslationProcessor().translatePartial(sb.toString()));
        }
        String boxStorageUnitIds = StorageUnitSDC.getAllStorageUnits(this.getQueryProcessor(), this.connectionInfo.isOracle(), StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), "LV_Box", boxid));
        StorageUnitSDC.clearReservedStorageUnits(this.database, boxStorageUnitIds);
    }

    private List isCustodian(String sdcid, String keyid1) {
        String sysuserid = this.connectionInfo.getSysuserId();
        ArrayList<String> list = new ArrayList<String>();
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select t.linkkeyid1, t.custodialuserid");
        sql.append(" from trackitem t");
        sql.append(" where t.linksdcid = ").append(safeSQL.addVar(sdcid));
        sql.append(" and t.linkkeyid1 in (").append(safeSQL.addIn(keyid1, ";")).append(")");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                if (sysuserid.equals(ds.getValue(i, "custodialuserid"))) continue;
                list.add(ds.getValue(i, "linkkeyid1"));
            }
        }
        return list;
    }
}

