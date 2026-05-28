/*
 * Decompiled with CFR 0.152.
 */
package sapphire.tagext;

import com.labvantage.sapphire.tagext.QueryData;
import sapphire.tagext.BaseTagInfo;

public class RefTypeTagInfo
extends BaseTagInfo {
    public static final String TAG_VAR_NAME = "reftypeinfo";
    private QueryData _querydata = null;

    public RefTypeTagInfo(QueryData querydata) {
        this._querydata = querydata;
    }

    public int getRowCount() {
        return this._querydata.getRowCount();
    }

    public int getCurrentRow() {
        return this._querydata.getCurrentRow();
    }

    public int getColumnCount() {
        return this._querydata.getColumnCount();
    }

    public int getCurrentCol() {
        return this._querydata.getCurrentCol();
    }

    public String getValue(String columnid) {
        return this._querydata.getValue(columnid, this._querydata.getNullValue());
    }

    public String getValue(int row, String columnid) {
        return this._querydata.getValue(row, columnid, this._querydata.getNullValue());
    }
}

