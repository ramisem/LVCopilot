/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.gwt.shared.JSONable;
import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class SDIList
implements JSONable,
Serializable {
    private DataSet sdiList = new DataSet();
    private String sdcid = "";
    private boolean allowDups = false;
    private ArrayList<String> keyIndex = new ArrayList();

    public SDIList() {
        this.defineDataSet(null);
    }

    private void defineDataSet(String[] attributes) {
        this.sdiList = new DataSet();
        this.sdiList.addColumn("keyid1", 0);
        this.sdiList.addColumn("keyid2", 0);
        this.sdiList.addColumn("keyid3", 0);
        if (attributes != null) {
            for (int i = 0; i < attributes.length; ++i) {
                this.sdiList.addColumn(attributes[i], 0);
            }
        }
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sdcid", this.sdcid);
            String[] columns = this.sdiList.getColumns();
            StringBuffer attributes = new StringBuffer();
            for (int i = 0; i < columns.length; ++i) {
                jsonObject.put(columns[i], this.sdiList.getColumnValues(columns[i], ";"));
                if (columns[i].equals("keyid1") || columns[i].equals("keyid2") || columns[i].equals("keyid3")) continue;
                attributes.append(";").append(columns[i]);
            }
            jsonObject.put("attributes", attributes.length() > 0 ? attributes.substring(1) : "");
        }
        catch (JSONException e) {
            Trace.logError("Failed toJSONObject in SDIList. Reason: " + e.getMessage(), e);
        }
        return jsonObject;
    }

    @Override
    public String toJSONString() {
        return this.toJSONObject().toString();
    }

    public String toString(String delimeter) {
        return this.getSDIList(delimeter);
    }

    public String toString() {
        return this.toString(";");
    }

    public String toText() {
        String text = this.getSDIList(", ");
        if (this.size() == 0) {
            return "";
        }
        if (this.size() == 1) {
            return text;
        }
        int pos = text.lastIndexOf(", ");
        return text.substring(0, pos) + " and " + text.substring(pos + 2);
    }

    public DataSet toDataSet() {
        DataSet ds = new DataSet(this.sdiList.toXML());
        ds.setString(-1, "sdcid", this.getSdcid());
        return ds;
    }

    public void setJSONObject(JSONObject jsonObject) {
        try {
            this.sdcid = jsonObject.getString("sdcid");
            String attributes = jsonObject.getString("attributes");
            String[] attributeCols = null;
            if (attributes != null && attributes.length() > 0) {
                attributeCols = StringUtil.split(attributes, ";");
                this.defineDataSet(attributeCols);
            } else {
                this.defineDataSet(null);
            }
            if (jsonObject.getString("keyid1") != null && jsonObject.getString("keyid1").length() > 0) {
                this.addSDIList(jsonObject.getString("keyid1"), jsonObject.getString("keyid2"), jsonObject.getString("keyid3"));
            }
            if (attributeCols != null) {
                for (int i = 0; i < attributeCols.length; ++i) {
                    String[] values = StringUtil.split(jsonObject.getString(attributeCols[i]), ";");
                    for (int j = 0; j < values.length && j < this.sdiList.size(); ++j) {
                        this.sdiList.setValue(j, attributeCols[i], values[j]);
                    }
                }
            }
        }
        catch (JSONException e) {
            Trace.logError("Failed setJSONObject in SDIList. Reason: " + e.getMessage(), e);
        }
    }

    public void setAllowDups(boolean allowDups) {
        this.allowDups = allowDups;
    }

    public int size() {
        return this.sdiList.size();
    }

    public String getSDIList(KeyId keyId, String delimeter) {
        return this.sdiList.getColumnValues(keyId == KeyId.KEYID1 ? "keyid1" : (keyId == KeyId.KEYID2 ? "keyid2" : "keyid3"), delimeter);
    }

    public String getSDIList(KeyId keyId) {
        return this.getSDIList(keyId, ";");
    }

    public String getSDIList() {
        return this.getSDIList(";");
    }

    public String getSDIList(String delimeter) {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < this.size(); ++i) {
            if (i != 0) {
                output.append(delimeter);
            }
            output.append(this.getKeyid(i));
        }
        return output.toString();
    }

    public String getKeyid(int index) {
        StringBuffer output = new StringBuffer();
        output.append(this.sdiList.getValue(index, "keyid1"));
        String keyid2 = this.sdiList.getValue(index, "keyid2");
        if (!keyid2.equals("(null)")) {
            output.append(" (ver:").append(keyid2);
            String keyid3 = this.sdiList.getValue(index, "keyid3");
            if (!keyid3.equals("(null)")) {
                output.append(" ,var:").append(keyid3);
            }
            output.append(")");
        }
        return output.toString();
    }

    public String getKeyid1(int index) {
        return this.sdiList.getValue(index, "keyid1");
    }

    public String getKeyid1() {
        return this.getSDIList(KeyId.KEYID1);
    }

    public String getKeyid2(int index) {
        return this.sdiList.getValue(index, "keyid2");
    }

    public String getKeyid2() {
        return this.getSDIList(KeyId.KEYID2);
    }

    public String getKeyid3(int index) {
        return this.sdiList.getValue(index, "keyid3");
    }

    public String getKeyid3() {
        return this.getSDIList(KeyId.KEYID3);
    }

    public int addSDIList(String keyid1) {
        return this.addSDIList(keyid1, "", "");
    }

    public int addSDIList(String keyid1, String keyid2, String keyid3) {
        return this.addSDIList(keyid1, keyid2, keyid3, ";");
    }

    public int addSDIList(String keyid1, String keyid2, String keyid3, String delimeter) {
        if (keyid1 != null && keyid1.length() > 0) {
            DataSet add = new DataSet();
            add.addColumnValues("keyid1", 0, keyid1, delimeter, "(null)");
            add.addColumnValues("keyid2", 0, keyid2, delimeter, StringUtil.repeat("(null)", StringUtil.split(keyid1, ";").length, ";"));
            add.addColumnValues("keyid3", 0, keyid3, delimeter, StringUtil.repeat("(null)", StringUtil.split(keyid1, ";").length, ";"));
            add.padColumns();
            if (this.allowDups) {
                for (int i = 0; i < add.size(); ++i) {
                    int row = this.sdiList.addRow();
                    this.sdiList.setValue(row, "keyid1", add.getValue(i, "keyid1"));
                    this.sdiList.setValue(row, "keyid2", add.getValue(i, "keyid2"));
                    this.sdiList.setValue(row, "keyid3", add.getValue(i, "keyid3"));
                    this.keyIndex.add(add.getValue(i, "keyid1") + ";" + add.getValue(i, "keyid2") + ";" + add.getValue(i, "keyid3"));
                }
                return add.size();
            }
            int added = 0;
            for (int i = 0; i < add.size(); ++i) {
                int index = this.getListIndex(add.getValue(i, "keyid1"), add.getValue(i, "keyid2"), add.getValue(i, "keyid3"));
                if (index != -1) continue;
                ++added;
                int row = this.sdiList.addRow();
                this.sdiList.setValue(row, "keyid1", add.getValue(i, "keyid1"));
                this.sdiList.setValue(row, "keyid2", add.getValue(i, "keyid2"));
                this.sdiList.setValue(row, "keyid3", add.getValue(i, "keyid3"));
                this.keyIndex.add(add.getValue(i, "keyid1") + ";" + add.getValue(i, "keyid2") + ";" + add.getValue(i, "keyid3"));
            }
            return added;
        }
        return 0;
    }

    public int addSDI(String keyid1) {
        return this.addSDI(keyid1, "", "");
    }

    public int addSDI(String keyid1, String keyid2, String keyid3) {
        if (keyid1 != null && keyid1.length() > 0 && !keyid1.equals("(null)")) {
            if (this.allowDups) {
                int row = this.sdiList.addRow();
                this.sdiList.setValue(row, "keyid1", keyid1);
                this.sdiList.setValue(row, "keyid2", keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)");
                this.sdiList.setValue(row, "keyid3", keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)");
                this.keyIndex.add(this.sdiList.getValue(row, "keyid1") + ";" + this.sdiList.getValue(row, "keyid2") + ";" + this.sdiList.getValue(row, "keyid3"));
                return row;
            }
            int index = this.getListIndex(keyid1, keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)", keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)");
            if (index == -1) {
                int row = this.sdiList.addRow();
                this.sdiList.setValue(row, "keyid1", keyid1);
                this.sdiList.setValue(row, "keyid2", keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)");
                this.sdiList.setValue(row, "keyid3", keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)");
                this.keyIndex.add(this.sdiList.getValue(row, "keyid1") + ";" + this.sdiList.getValue(row, "keyid2") + ";" + this.sdiList.getValue(row, "keyid3"));
                return row;
            }
        }
        return -1;
    }

    public String getSDIAttributeList(String attributeid) {
        return this.getSDIAttributeList(attributeid, ";");
    }

    public String getSDIAttributeList(String attributeid, String delimeter) {
        return this.sdiList.getColumnValues(attributeid, delimeter);
    }

    public String getSDIAttribute(int index, String attributeid) {
        return this.sdiList.getString(index, attributeid, "");
    }

    public void setSDIAttribute(int index, String attributeid, String attributevalue) {
        this.sdiList.setString(index, attributeid, attributevalue);
    }

    private int removeKeyIndex(String key) {
        for (int i = 0; i < this.keyIndex.size(); ++i) {
            if (!this.keyIndex.get(i).equals(key)) continue;
            this.keyIndex.remove(i);
        }
        return -1;
    }

    private int getKeyIndex(String key) {
        for (int i = 0; i < this.keyIndex.size(); ++i) {
            if (!this.keyIndex.get(i).equals(key)) continue;
            return i;
        }
        return -1;
    }

    public int getListIndex(String keyid1, String keyid2, String keyid3) {
        Integer index = this.getKeyIndex(keyid1 + ";" + (keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)") + ";" + (keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)"));
        return index != null ? index : -1;
    }

    public boolean removeSDI(String keyid1, String keyid2, String keyid3) {
        int rowIndex;
        if (keyid1 != null && keyid1.length() > 0 && !keyid1.equals("(null)") && (rowIndex = this.getListIndex(keyid1, keyid2, keyid3)) >= 0) {
            this.removeKeyIndex(this.getKeyid1(rowIndex) + ";" + this.getKeyid2(rowIndex) + ";" + this.getKeyid3(rowIndex));
            this.sdiList.remove(rowIndex);
            return true;
        }
        return false;
    }

    public boolean removeSDI(String keyid1) {
        return this.removeSDI(keyid1, "", "");
    }

    public void removeSDI(int index) {
        this.removeKeyIndex(this.getKeyid1(index) + ";" + this.getKeyid2(index) + ";" + this.getKeyid3(index));
        this.sdiList.remove(index);
    }

    public static enum KeyId {
        KEYID1,
        KEYID2,
        KEYID3;

    }
}

