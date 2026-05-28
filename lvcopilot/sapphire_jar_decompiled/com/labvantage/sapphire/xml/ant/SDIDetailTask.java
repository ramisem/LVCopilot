/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.SDIDetail;
import com.labvantage.sapphire.xml.ant.ColumnTask;
import com.labvantage.sapphire.xml.ant.DataTask;
import java.util.ArrayList;
import org.apache.tools.ant.Task;

public class SDIDetailTask
extends Task {
    private String detailid;
    private String extendedwhere;
    private String data;
    private boolean excluded;
    private boolean flush;
    ArrayList columns = new ArrayList();

    public void setDetailid(String detailid) {
        this.detailid = detailid;
    }

    public void setExtendedwhere(String extendedwhere) {
        this.extendedwhere = extendedwhere;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public void setFlush(boolean flush) {
        this.flush = flush;
    }

    public SDIDetail getSDIDetail() {
        SDIDetail sdiDetail = new SDIDetail(this.detailid);
        sdiDetail.setExtendedwhere(this.extendedwhere);
        sdiDetail.setData(this.data);
        sdiDetail.setExcluded(this.excluded);
        sdiDetail.setFlush(this.flush);
        for (int i = 0; i < this.columns.size(); ++i) {
            sdiDetail.addColumn(((ColumnTask)((Object)this.columns.get(i))).getColumn());
        }
        return sdiDetail;
    }

    public void addConfiguredColumn(ColumnTask column) {
        this.columns.add(column);
    }

    public void addConfiguredData(DataTask data) {
        this.data = data.getData();
    }
}

