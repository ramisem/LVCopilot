/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.HqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import sapphire.util.StringUtil;

public class WorksheetItemFieldHqlBuilder {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String sdcid;
    private SapphireHibernateUtil shu;
    private AdhocMetaData adhocmetadata;
    private HqlBuilder hqlBuilder;
    private boolean worksheetitemjoinExist = false;

    public WorksheetItemFieldHqlBuilder(String sdcid, SapphireHibernateUtil shu, AdhocMetaData adhocmetadata, HqlBuilder hqlbuilder) {
        this.sdcid = sdcid;
        this.shu = shu;
        this.adhocmetadata = adhocmetadata;
        this.hqlBuilder = hqlbuilder;
    }

    String getWorksheetItemFieldJoin(String tableid, String dataforsdcid, int joinNo) {
        StringBuffer s = new StringBuffer();
        if (!this.worksheetitemjoinExist) {
            s.append(" join " + tableid + ".worksheetitem_worksheetid as worksheetitem ");
            this.worksheetitemjoinExist = true;
        }
        s.append(" join worksheetitem.worksheetitemfield as worksheetitemfield" + joinNo);
        return s.toString();
    }

    String getWorksheetItemFieldWhere(String columnid, String sdcid, int joinNo, String operator, String value, String columntype) {
        StringBuffer s = new StringBuffer();
        String fieldid = StringUtil.getTokens(columnid)[0];
        s.append(" (worksheetitemfield" + joinNo + ".fieldname='" + fieldid + "' ");
        String leftCr = "";
        if ("N".equals(columntype)) {
            leftCr = "worksheetitemfield" + joinNo + ".numericvalue";
            columntype = "big_decimal";
        } else if ("D".equals(columntype) || "O".equals(columntype)) {
            leftCr = "worksheetitemfield" + joinNo + ".datevalue";
            columntype = "timestamp";
        } else {
            leftCr = "worksheetitemfield" + joinNo + ".enteredtext";
            columntype = "string";
        }
        if ("O".equals(columntype)) {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, false) + ") ");
        } else {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, true) + ") ");
        }
        return s.toString();
    }

    String getWorksheetItemFieldViewJoin(String tableid, String columnid, int joinNo, boolean needWorksheetItemJoin) {
        String dataforsdcid = this.sdcid;
        String datafortableid = tableid;
        if (columnid.indexOf(".worksheetitemfield[") > 0) {
            try {
                datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".documentfield[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (needWorksheetItemJoin) {
            return " join " + tableid + "." + (columnid.indexOf(".worksheetitemfield[") > 0 ? columnid.substring(0, columnid.indexOf(".worksheetitemfield[")) + "." : "") + "worksheetitem_worksheetid as worksheetitem join worksheetitem.worksheetitemfield as worksheetitemfield" + joinNo;
        }
        return " join worksheetitem.worksheetitemfield as worksheetitemfield" + joinNo;
    }

    String getWorksheetItemFieldViewJoinWhere(String tableid, String columnid, int joinNo) {
        StringBuffer s = new StringBuffer();
        String dataforsdcid = this.sdcid;
        if (columnid.indexOf(".worksheetitemfield[") > 0) {
            try {
                String datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".worksheetitemfield[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception datafortableid) {
                // empty catch block
            }
        }
        String fieldid = StringUtil.getTokens(columnid)[0];
        s.append(" ( worksheetitemfield" + joinNo + ".fieldname='" + fieldid + "' ");
        if (columnid.indexOf(".worksheetitemfield[") > 0) {
            s.append(" and worksheetitem.worksheetid=" + tableid + "." + columnid.substring(0, columnid.indexOf(".worksheetitemfield[")) + " ");
        }
        s.append(")");
        return s.toString();
    }

    String getWorksheetItemFieldViewSelect(int joinNo, String valuecolumnid) {
        StringBuffer s = new StringBuffer();
        s.append("worksheetitemfield" + joinNo + ".worksheetitemid");
        s.append(",worksheetitemfield" + joinNo + ".worksheetitemversionid");
        s.append(",worksheetitemfield" + joinNo + ".fieldname");
        s.append(",worksheetitemfield" + joinNo + ".datatype");
        s.append(",worksheetitemfield" + joinNo + ".fieldinstance");
        s.append(",worksheetitemfield" + joinNo + "." + valuecolumnid);
        return s.toString();
    }
}

