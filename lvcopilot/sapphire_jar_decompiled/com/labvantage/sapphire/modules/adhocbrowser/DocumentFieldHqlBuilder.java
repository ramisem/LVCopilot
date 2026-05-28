/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.HqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import sapphire.util.StringUtil;

public class DocumentFieldHqlBuilder {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String sdcid;
    private SapphireHibernateUtil shu;
    private AdhocMetaData adhocmetadata;
    private HqlBuilder hqlBuilder;

    public DocumentFieldHqlBuilder(String sdcid, SapphireHibernateUtil shu, AdhocMetaData adhocmetadata, HqlBuilder hqlbuilder) {
        this.sdcid = sdcid;
        this.shu = shu;
        this.adhocmetadata = adhocmetadata;
        this.hqlBuilder = hqlbuilder;
    }

    String getDocumentFieldJoin(String tableid, String dataforsdcid, int joinNo) {
        StringBuffer s = new StringBuffer();
        s.append(" join " + tableid + ".sdidocument as sdidocument join sdidocument.documentfield as documentfield" + joinNo);
        return s.toString();
    }

    String getDocumentFieldWhere(String columnid, String sdcid, int joinNo, String operator, String value, String columntype) {
        StringBuffer s = new StringBuffer();
        String fieldid = StringUtil.getTokens(columnid)[0];
        s.append(" (documentfield" + joinNo + ".fieldid='" + fieldid + "' ");
        String leftCr = "";
        if ("N".equals(columntype)) {
            leftCr = "documentfield" + joinNo + ".numericvalue";
            columntype = "big_decimal";
        } else if ("D".equals(columntype) || "O".equals(columntype)) {
            leftCr = "documentfield" + joinNo + ".datevalue";
            columntype = "timestamp";
        } else {
            leftCr = "documentfield" + joinNo + ".enteredtext";
            columntype = "string";
        }
        if ("O".equals(columntype)) {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, false) + ") ");
        } else {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, true) + ") ");
        }
        return s.toString();
    }

    String getDocumentFieldViewJoin(String tableid, String columnid, int joinNo, boolean needSDIDocumentJoin) {
        String dataforsdcid = this.sdcid;
        String datafortableid = tableid;
        if (columnid.indexOf(".documentfield[") > 0) {
            try {
                datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".documentfield[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (needSDIDocumentJoin) {
            return " join " + tableid + "." + (columnid.indexOf(".documentfield[") > 0 ? columnid.substring(0, columnid.indexOf(".documentfield[")) + "." : "") + "sdidocument as sdidocument join sdidocument.documentfield as documentfield" + joinNo;
        }
        return " join sdidocument.documentfield as documentfield" + joinNo;
    }

    String getDocumentFieldViewJoinWhere(String tableid, String columnid, int joinNo) {
        StringBuffer s = new StringBuffer();
        String dataforsdcid = this.sdcid;
        if (columnid.indexOf(".documentfield[") > 0) {
            try {
                String datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".documentfield[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception datafortableid) {
                // empty catch block
            }
        }
        String fieldid = StringUtil.getTokens(columnid)[0];
        s.append(" ( documentfield" + joinNo + ".fieldid='" + fieldid + "' ");
        if (columnid.indexOf(".documentfield[") > 0) {
            s.append(" and sdidocument.keyid1=" + tableid + "." + columnid.substring(0, columnid.indexOf(".documentfield[")) + " ");
        }
        s.append(")");
        return s.toString();
    }

    String getDocumentFieldViewSelect(int joinNo, String valuecolumnid) {
        StringBuffer s = new StringBuffer();
        s.append("documentfield" + joinNo + ".documentid");
        s.append(",documentfield" + joinNo + ".documentversionid");
        s.append(",documentfield" + joinNo + ".fieldid");
        s.append(",documentfield" + joinNo + ".fieldinstance");
        s.append(",documentfield" + joinNo + "." + valuecolumnid);
        return s.toString();
    }
}

