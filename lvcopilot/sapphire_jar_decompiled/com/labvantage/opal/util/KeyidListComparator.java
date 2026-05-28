/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import sapphire.util.StringUtil;

public class KeyidListComparator
implements Comparator {
    private String[] _sortcolumn;
    private boolean[] _ascending;
    private int _sortcolumns;
    private List _keyid1list;

    public KeyidListComparator() {
    }

    public KeyidListComparator(String sortstring, String keyid1list, String separator) {
        this.setSort(sortstring, keyid1list, separator);
    }

    public void setSort(String sortstring, String keyid1list, String separator) {
        String[] columns = StringUtil.split(sortstring, ",");
        this._sortcolumns = columns.length;
        this._sortcolumn = new String[this._sortcolumns];
        this._ascending = new boolean[this._sortcolumns];
        this._keyid1list = Arrays.asList(StringUtil.split(keyid1list, separator));
        for (int i = 0; i < this._sortcolumns; ++i) {
            if (columns[i].trim().toLowerCase().endsWith(" a")) {
                this._sortcolumn[i] = columns[i].substring(0, columns[i].length() - 2).trim().toLowerCase();
                this._ascending[i] = true;
                continue;
            }
            if (columns[i].trim().toLowerCase().endsWith(" d")) {
                this._sortcolumn[i] = columns[i].substring(0, columns[i].length() - 2).trim().toLowerCase();
                this._ascending[i] = false;
                continue;
            }
            this._sortcolumn[i] = columns[i].trim().toLowerCase();
            this._ascending[i] = true;
        }
    }

    public int compare(Object object1, Object object2) {
        HashMap hm1 = (HashMap)object1;
        HashMap hm2 = (HashMap)object2;
        int compare = 0;
        for (int i = 0; i < this._sortcolumns && compare == 0; ++i) {
            int int1 = this._keyid1list.indexOf(hm1.get(this._sortcolumn[i]));
            int int2 = this._keyid1list.indexOf(hm2.get(this._sortcolumn[i]));
            if (int1 < 0 && int2 < 0) {
                compare = 0;
            } else if (int2 < 0 && int1 >= 0) {
                compare = -1;
            } else if (int1 < 0 && int2 >= 0) {
                compare = 1;
            } else if (int1 < int2) {
                compare = -1;
            } else if (int1 > int2) {
                compare = 1;
            }
            if (this._ascending[i]) continue;
            compare = -compare;
        }
        return compare;
    }
}

