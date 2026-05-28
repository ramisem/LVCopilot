/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import com.labvantage.sapphire.modules.adhocbrowser.AdhocMetaData;
import com.labvantage.sapphire.modules.adhocbrowser.HqlBuilder;
import com.labvantage.sapphire.modules.adhocbrowser.SapphireHibernateUtil;
import sapphire.util.StringUtil;

public class DataEntryHqlBuilder {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String sdcid;
    private SapphireHibernateUtil shu;
    private AdhocMetaData adhocmetadata;
    private HqlBuilder hqlBuilder;

    public DataEntryHqlBuilder(String sdcid, SapphireHibernateUtil shu, AdhocMetaData adhocmetadata, HqlBuilder hqlbuilder) {
        this.sdcid = sdcid;
        this.shu = shu;
        this.adhocmetadata = adhocmetadata;
        this.hqlBuilder = hqlbuilder;
    }

    String getDataEntryJoin(String tableid, String columnid, int joinNo) {
        StringBuffer s = new StringBuffer();
        String prefix = "";
        if (columnid.indexOf("sdidataitem[") > 0) {
            prefix = columnid.substring(0, columnid.lastIndexOf(".") + 1);
        }
        String dataforsdcid = this.sdcid;
        if (columnid.indexOf(".sdidataitem[") > 0) {
            try {
                String datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".sdidataitem[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        s.append(" join " + tableid + ".sdidataitem as sdidataitem" + joinNo + " ");
        return s.toString();
    }

    String getDataEntryWhere(String columnid, String sdcid, String tableid, String keycolid1, String keycolid2, String keycolid3, int joinNo, String operator, String value, String columntype) {
        StringBuffer s = new StringBuffer();
        String[] dataitem = StringUtil.getTokens(columnid);
        String[] dataitemKeys = StringUtil.split(dataitem[0], "|");
        s.append(" (sdidataitem" + joinNo + ".sdcid='" + sdcid + "' ");
        s.append("  and sdidataitem" + joinNo + ".keyid1=" + tableid + "." + keycolid1 + " ");
        if (keycolid2 != null && keycolid2.length() > 0) {
            s.append(" and sdidataitem" + joinNo + ".keyid2=" + tableid + "." + keycolid2 + " ");
            if (keycolid3 != null && keycolid3.length() > 0) {
                s.append(" and sdidataitem" + joinNo + ".keyid3=" + tableid + "." + keycolid3 + " ");
            }
        }
        if (dataitemKeys.length == 2) {
            s.append(" and sdidataitem" + joinNo + ".paramid_column=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[0]) + " ");
            s.append(" and sdidataitem" + joinNo + ".paramtype=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[1]) + " ");
        } else if (dataitemKeys.length == 4) {
            s.append(" and sdidataitem" + joinNo + ".paramlistid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[0]) + " ");
            s.append(" and sdidataitem" + joinNo + ".variantid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[1]) + " ");
            s.append(" and sdidataitem" + joinNo + ".paramid_column=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[2]) + " ");
            s.append(" and sdidataitem" + joinNo + ".paramtype=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[3]) + " ");
        } else if (dataitemKeys.length == 5) {
            s.append(" and sdidataitem" + joinNo + ".paramlistid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[0]) + " ");
            s.append(" and sdidataitem" + joinNo + ".paramlistversionid='" + dataitemKeys[1] + "' ");
            s.append(" and sdidataitem" + joinNo + ".variantid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[2]) + " ");
            s.append(" and sdidataitem" + joinNo + ".paramid_column=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[3]) + " ");
            s.append(" and sdidataitem" + joinNo + ".paramtype=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[4]) + " ");
        }
        String leftCr = "";
        if ("N".equals(columntype)) {
            leftCr = "sdidataitem" + joinNo + ".enteredvalue";
            columntype = "big_decimal";
        } else if ("D".equals(columntype) || "O".equals(columntype)) {
            leftCr = "sdidataitem" + joinNo + ".transformdt";
            columntype = "timestamp";
        } else {
            leftCr = "sdidataitem" + joinNo + ".enteredtext";
            columntype = "string";
        }
        if ("O".equals(columntype)) {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, false) + ") ");
        } else {
            s.append(" and " + this.hqlBuilder.getOperatorValueClause(operator, value, columntype, leftCr, true) + ") ");
        }
        return s.toString();
    }

    String getDataItemViewJoin(String tableid, String columnid, int joinNo) {
        String joinname = "sdidataitem";
        if (columnid.indexOf(".sdidataitem[") > 0) {
            joinname = columnid.substring(0, columnid.indexOf("["));
        }
        String dataforsdcid = this.sdcid;
        if (columnid.indexOf(".sdidataitem[") > 0) {
            try {
                String datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".sdidataitem[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return " join " + tableid + "." + joinname + " as sdidataitem" + joinNo + " ";
    }

    String getDataItemViewJoinWhere(String tableid, String columnid, int joinNo, boolean isRootDataEntry, boolean hasSDIDataJoin, boolean hasSDIDataItemJoin) {
        StringBuffer s = new StringBuffer();
        String dataforsdcid = this.sdcid;
        if (columnid.indexOf(".sdidataitem[") > 0) {
            try {
                String datafortableid = this.shu.getReferenceEntityName(tableid, columnid.substring(0, columnid.indexOf(".sdidataitem[")));
                dataforsdcid = this.adhocmetadata.getSdcId(datafortableid);
            }
            catch (Exception datafortableid) {
                // empty catch block
            }
        }
        String[] dataitem = StringUtil.getTokens(columnid);
        String[] dataitemKeys = StringUtil.split(dataitem[0], "|");
        String sdidataitemAlias = "sdidataitem" + joinNo;
        s.append(" (" + sdidataitemAlias + ".sdcid='" + dataforsdcid + "' ");
        if (columnid.indexOf(".sdidataitem[") > 0) {
            s.append(" and " + sdidataitemAlias + ".keyid1=" + tableid + "." + columnid.substring(0, columnid.indexOf(".sdidataitem[")) + " ");
        }
        if (dataitemKeys.length == 2) {
            s.append(" and " + sdidataitemAlias + ".paramid_column=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[0]) + " ");
            s.append(" and " + sdidataitemAlias + ".paramtype=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[1]) + " ");
        } else if (dataitemKeys.length == 4) {
            s.append(" and " + sdidataitemAlias + ".paramlistid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[0]) + " ");
            s.append(" and " + sdidataitemAlias + ".variantid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[1]) + " ");
            s.append(" and " + sdidataitemAlias + ".paramid_column=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[2]) + " ");
            s.append(" and " + sdidataitemAlias + ".paramtype=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[3]) + " ");
        } else if (dataitemKeys.length == 5) {
            s.append(" and " + sdidataitemAlias + ".paramlistid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[0]) + " ");
            s.append(" and " + sdidataitemAlias + ".paramlistversionid='" + dataitemKeys[1] + "' ");
            s.append(" and " + sdidataitemAlias + ".variantid=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[2]) + " ");
            s.append(" and " + sdidataitemAlias + ".paramid_column=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[3]) + " ");
            s.append(" and " + sdidataitemAlias + ".paramtype=:" + this.hqlBuilder.addStringTypeToParamMap(dataitemKeys[4]) + " ");
        }
        if (isRootDataEntry) {
            s.append(" and " + sdidataitemAlias + ".keyid1=rsetitemsds.keyid1");
            s.append(" and " + sdidataitemAlias + ".keyid2=rsetitemsds.keyid2");
            s.append(" and " + sdidataitemAlias + ".keyid3=rsetitemsds.keyid3");
            s.append(" and " + sdidataitemAlias + ".paramlistid=rsetitemsds.paramlistid");
            s.append(" and " + sdidataitemAlias + ".paramlistversionid=rsetitemsds.paramlistversionid");
            s.append(" and " + sdidataitemAlias + ".variantid=rsetitemsds.variantid");
            s.append(" and " + sdidataitemAlias + ".dataset=rsetitemsds.dataset ");
        }
        if (hasSDIDataJoin) {
            s.append(" and " + sdidataitemAlias + ".keyid1=sdidata.keyid1");
            s.append(" and " + sdidataitemAlias + ".keyid2=sdidata.keyid2");
            s.append(" and " + sdidataitemAlias + ".keyid3=sdidata.keyid3");
            s.append(" and " + sdidataitemAlias + ".paramlistid=sdidata.paramlistid");
            s.append(" and " + sdidataitemAlias + ".paramlistversionid=sdidata.paramlistversionid");
            s.append(" and " + sdidataitemAlias + ".variantid=sdidata.variantid");
            s.append(" and " + sdidataitemAlias + ".dataset=sdidata.dataset ");
        }
        if (hasSDIDataItemJoin) {
            s.append(" and " + sdidataitemAlias + ".keyid1=sdidataitem.keyid1");
            s.append(" and " + sdidataitemAlias + ".keyid2=sdidataitem.keyid2");
            s.append(" and " + sdidataitemAlias + ".keyid3=sdidataitem.keyid3");
            s.append(" and " + sdidataitemAlias + ".paramlistid=sdidataitem.paramlistid");
            s.append(" and " + sdidataitemAlias + ".paramlistversionid=sdidataitem.paramlistversionid");
            s.append(" and " + sdidataitemAlias + ".variantid=sdidataitem.variantid");
            s.append(" and " + sdidataitemAlias + ".dataset=sdidataitem.dataset ");
            s.append(" and " + sdidataitemAlias + ".paramid=sdidataitem.paramid ");
            s.append(" and " + sdidataitemAlias + ".paramtype=sdidataitem.paramtype ");
            s.append(" and " + sdidataitemAlias + ".replicateid=sdidataitem.replicateid ");
        }
        s.append(")");
        return s.toString();
    }

    String getDataItemViewSelect(int joinNo, String valuecolumnid) {
        StringBuffer s = new StringBuffer();
        s.append("sdidataitem" + joinNo + ".paramlistid");
        s.append(",sdidataitem" + joinNo + ".paramlistversionid");
        s.append(",sdidataitem" + joinNo + ".variantid");
        s.append(",sdidataitem" + joinNo + ".dataset");
        s.append(",sdidataitem" + joinNo + ".paramid");
        s.append(",sdidataitem" + joinNo + ".paramtype");
        s.append(",sdidataitem" + joinNo + ".replicateid");
        s.append(",sdidataitem" + joinNo + "." + valuecolumnid);
        return s.toString();
    }
}

