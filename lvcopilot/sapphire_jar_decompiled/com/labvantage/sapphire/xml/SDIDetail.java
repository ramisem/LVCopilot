/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.Column;
import java.util.ArrayList;
import java.util.List;
import sapphire.util.StringUtil;

public class SDIDetail {
    private String detailid;
    private String extendedwhere;
    private String data;
    private boolean excluded;
    private boolean flush;
    ArrayList columns = new ArrayList();

    public SDIDetail(String detailid) {
        this.detailid = detailid;
    }

    public String getDetailid() {
        return this.detailid;
    }

    public void setDetailid(String detailid) {
        this.detailid = detailid;
    }

    public String getExtendedwhere() {
        return this.extendedwhere != null ? this.extendedwhere : "";
    }

    public void setExtendedwhere(String extendedwhere) {
        this.extendedwhere = extendedwhere;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void addColumn(Column column) {
        this.columns.add(column);
    }

    public List getColumns() {
        return this.columns;
    }

    public boolean isExcluded() {
        return this.excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public boolean isFlush() {
        return this.flush;
    }

    public void setFlush(boolean flush) {
        this.flush = flush;
    }

    public static String[] getStandardDetails() {
        String[] sdiDetails = new String[]{"categories", "roles", "attachments", "workflows", "data", "addresses", "workitems", "coc", "specs", "pricelists", "aliases"};
        return sdiDetails;
    }

    public boolean isStandardDetail() {
        String[] standardDetails = SDIDetail.getStandardDetails();
        for (int i = 0; i < standardDetails.length; ++i) {
            if (this.detailid == null || !standardDetails[i].equals(this.detailid)) continue;
            return true;
        }
        return false;
    }

    public boolean isSDIXXXDetail() {
        return this.detailid != null && this.detailid.startsWith("sdi") && !this.detailid.equalsIgnoreCase("sdirole");
    }

    public boolean isRole() {
        return this.detailid != null && this.detailid.equalsIgnoreCase("sdirole");
    }

    public boolean isCategory() {
        return this.detailid != null && this.detailid.equalsIgnoreCase("categoryitem");
    }

    public String toXML(int level) {
        String level0 = StringUtil.repeat("\t", level);
        StringBuffer out = new StringBuffer();
        out.append(level0).append("<sdidetail detailid=\"").append(this.detailid).append("\" extendedwhere=\"").append(this.extendedwhere != null ? this.extendedwhere : "").append("\"");
        if (this.data == null) {
            return out.toString() + "/>\n";
        }
        return out.toString() + ">\n<data>\n" + this.data + "</data>\n</sdidetail>";
    }
}

