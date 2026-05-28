/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.HqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import com.labvantage.sapphire.services.DDTService;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.util.StringUtil;

public class AttributeHqlBuilder {
    private String sdcid;
    private SapphireHibernateUtil shu;
    private AdhocMetaData adhocmetadata;
    private HqlBuilder hqlBuilder;

    public AttributeHqlBuilder(String sdcid, SapphireHibernateUtil shu, AdhocMetaData adhocmetadata, HqlBuilder hqlbuilder) {
        this.sdcid = sdcid;
        this.shu = shu;
        this.adhocmetadata = adhocmetadata;
        this.hqlBuilder = hqlbuilder;
    }

    String getAttributeJoin(String tableid, String dataforsdcid, String columnid, int joinNo, String actualtableid) {
        StringBuffer s = new StringBuffer();
        if ("worksheet".equals(tableid)) {
            s.append(" left join " + tableid + ".v_worksheetmetadata as sdiattribute" + joinNo + " ");
        } else if (actualtableid.length() > 0) {
            s.append(" left join " + tableid + " as " + actualtableid + " left join " + actualtableid + ".sdiattribute as sdiattribute" + joinNo + " ");
        } else {
            s.append(" left join " + tableid + ".sdiattribute as sdiattribute" + joinNo + " ");
        }
        return s.toString();
    }

    String getAttributeWhere(String columnid, String sdcid, int joinNo, String operator, String value, String columntype) {
        StringBuffer s = new StringBuffer();
        String attributeid = StringUtil.getTokens(columnid)[0];
        if ("LV_Worksheet".equals(sdcid)) {
            s.append(" (sdiattribute" + joinNo + ".attributeid='" + attributeid + "' ");
        } else {
            s.append(" (sdiattribute" + joinNo + ".sdcid='" + sdcid + "' and sdiattribute" + joinNo + ".attributeid='" + attributeid + "' ");
        }
        String leftCr = "";
        if ("N".equals(columntype)) {
            leftCr = "sdiattribute" + joinNo + ".numericvalue";
            columntype = "big_decimal";
        } else if ("D".equals(columntype) || "O".equals(columntype)) {
            leftCr = "sdiattribute" + joinNo + ".datevalue";
            columntype = "timestamp";
        } else {
            leftCr = "sdiattribute" + joinNo + ".textvalue";
            columntype = "string";
        }
        if ("O".equals(columntype)) {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, false) + ") ");
        } else {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, true) + ") ");
        }
        return s.toString();
    }

    String getAttributeViewJoin(String tableid, String columnid, int joinNo) {
        return " join " + tableid + "." + (columnid.indexOf(".sdiattribute[") > 0 ? columnid.substring(0, columnid.indexOf(".sdiattribute[")) + "." : "") + ("worksheet".equals(tableid) ? "v_worksheetmetadata" : "sdiattribute") + " as sdiattribute" + joinNo + " ";
    }

    String getAttributeViewJoinWhere(String tableid, String columnid, int joinNo, DDTService ddtService) throws ServiceException {
        StringBuffer s = new StringBuffer();
        String dataforsdcid = this.sdcid;
        String datafortableid = tableid;
        if (columnid.indexOf(".sdiattribute[") > 0) {
            try {
                datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".sdiattribute[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        String attributeid = StringUtil.getTokens(columnid)[0];
        if ("LV_Worksheet".equals(this.sdcid)) {
            s.append(" ( sdiattribute" + joinNo + ".attributeid='" + attributeid + "' ");
        } else {
            s.append(" ( sdiattribute" + joinNo + ".sdcid='" + dataforsdcid + "' and sdiattribute" + joinNo + ".attributeid='" + attributeid + "' ");
        }
        if (columnid.indexOf(".sdiattribute[") > 0) {
            s.append(" and sdiattribute" + joinNo + ".keyid1=" + tableid + "." + columnid.substring(0, columnid.indexOf(".sdiattribute[")) + " ");
        }
        s.append(")");
        return s.toString();
    }

    String getAttributeViewSelect(int joinNo) {
        StringBuffer s = new StringBuffer();
        s.append("sdiattribute" + joinNo + ".attributeid");
        s.append(",sdiattribute" + joinNo + ".attributeinstance");
        s.append(",sdiattribute" + joinNo + ".datatype");
        s.append(",sdiattribute" + joinNo + ".textvalue");
        s.append(",sdiattribute" + joinNo + ".numericvalue");
        s.append(",sdiattribute" + joinNo + ".datevalue");
        return s.toString();
    }
}

