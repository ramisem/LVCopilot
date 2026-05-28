/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.xml.PropertyListTransfer;
import com.labvantage.sapphire.xml.PropertyTreeTransfer;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class Column
implements Cloneable {
    private String columnid;
    private String columnalias;
    private String forceUpdate;
    private String forceNullUpdate;
    private String value;
    private String defaultvalue;
    private String[] valueTokens;
    private String file;
    private String filetype;
    private String filetypeid;
    private String[] fileTokens;
    private PropertyListTransfer propertyListTransfer;
    private PropertyTreeTransfer propertyTreeTransfer;
    private String datatype;
    private boolean primarykey;
    private int columnlength;
    private int updatePos;
    private byte[] byteArray;
    private boolean notnull;
    private String columntype;
    private boolean isCDATA;
    private int keyPos;
    private boolean excluded;

    public Column() {
    }

    public Column(String columnid) {
        this.columnid = columnid;
    }

    public String getColumnid() {
        return this.columnid;
    }

    public void setColumnid(String columnid) {
        this.columnid = columnid;
    }

    public void setColumnalias(String columnalias) {
        this.columnalias = columnalias;
    }

    public String getColumnalias() {
        return this.columnalias;
    }

    public void setForceUpdate(String forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getForceUpdate() {
        return this.forceUpdate;
    }

    public boolean isForceUpdate() {
        return this.forceUpdate != null && (this.forceUpdate.equals("Y") || this.forceUpdate.equals("true"));
    }

    public boolean isForceNullUpdate() {
        return this.forceNullUpdate != null && (this.forceNullUpdate.equals("Y") || this.forceNullUpdate.equals("true"));
    }

    public void setForceNullUpdate(String forceNullUpdate) {
        this.forceNullUpdate = forceNullUpdate;
    }

    public String getForceNullUpdate() {
        return this.forceNullUpdate;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
        this.valueTokens = StringUtil.getTokens(value);
    }

    public String[] getValueTokens() {
        return this.valueTokens;
    }

    public boolean hasValueTokens() {
        return this.valueTokens != null && this.valueTokens.length > 0;
    }

    public String getDefaultvalue() {
        return this.defaultvalue;
    }

    public void setDefaultvalue(String defaultvalue) {
        this.defaultvalue = defaultvalue;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
        this.fileTokens = StringUtil.getTokens(file);
    }

    public void setFiletype(String filetype, String filetypeid) {
        this.filetype = filetype;
        this.filetypeid = filetypeid;
    }

    public String getFiletype() {
        return this.filetype;
    }

    public String getFiletypeid() {
        return this.filetypeid;
    }

    public PropertyListTransfer getPropertyListTransfer() {
        return this.propertyListTransfer;
    }

    public void setPropertyListTransfer(PropertyListTransfer propertyListTransfer) {
        this.propertyListTransfer = propertyListTransfer;
    }

    public PropertyTreeTransfer getPropertyTreeTransfer() {
        return this.propertyTreeTransfer;
    }

    public void setPropertyTreeTransfer(PropertyTreeTransfer propertyTreeTransfer) {
        this.propertyTreeTransfer = propertyTreeTransfer;
    }

    public String[] getFileTokens() {
        return this.fileTokens;
    }

    public boolean hasFileTokens() {
        return this.fileTokens != null && this.fileTokens.length > 0;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void setDDTDefinition(String datatype, String primarykey, String notnull, String columnlength, String columntype) throws SapphireException {
        block2: {
            this.datatype = datatype;
            this.primarykey = primarykey != null && primarykey.equals("Y");
            this.notnull = notnull != null && notnull.equals("Y");
            try {
                this.columnlength = Integer.parseInt(columnlength);
            }
            catch (NumberFormatException nfe) {
                if (!datatype.equals("C")) break block2;
                throw new SapphireException("Column length not a valid number for columnid " + this.columnid);
            }
        }
        this.columntype = columntype;
    }

    public void setUpdateDefinition(String forceupdate, String forcenullupdate) {
        this.forceUpdate = forceupdate;
        this.forceNullUpdate = forcenullupdate;
    }

    public void setValue(String value, boolean isCDATA) {
        this.value = value;
        this.isCDATA = isCDATA;
    }

    public boolean isPrimarykey() {
        return this.primarykey;
    }

    public boolean isNotnull() {
        return this.notnull;
    }

    public boolean isExcluded() {
        return this.excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public void clearValue() {
        this.value = null;
        this.byteArray = null;
        this.isCDATA = false;
    }

    public String getDatatype() {
        return this.datatype;
    }

    public int getColumnlength() {
        return this.columnlength;
    }

    public String getColumntype() {
        return this.columntype;
    }

    public int getUpdatePos() {
        return this.updatePos;
    }

    public void setUpdatePos(int updatePos) {
        this.updatePos = updatePos;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public byte[] getByteArray() {
        return this.byteArray;
    }

    public void setKeyPos(int keyPos) {
        this.keyPos = keyPos;
        this.primarykey = true;
    }

    public boolean isAuditColumn() {
        return Column.isAuditColumn(this.columnid);
    }

    public static boolean isAuditColumn(String columnid) {
        return columnid.equals("createdt") || columnid.equals("createby") || columnid.equals("createtool") || columnid.equals("moddt") || columnid.equals("modby") || columnid.equals("modtool") || columnid.equals("auditsequence") || columnid.equals("auditdeferflag") || columnid.equals("tracelogid");
    }

    public boolean isSecurityColumn() {
        return Column.isSecurityColumn(this.columnid);
    }

    public static boolean isSecurityColumn(String columnid) {
        return columnid.equals("securityset") || columnid.equals("securitydepartment") || columnid.equals("securityuser");
    }

    public boolean isValid() {
        return this.columnid != null && this.columnid.length() > 0;
    }

    public String toString() {
        StringBuffer out = new StringBuffer("Column id=" + this.columnid + " forceupdate=" + this.forceUpdate + " forcenullupdate=" + this.forceNullUpdate + " value=" + (this.value != null ? this.value : "(undefined)"));
        return out.toString();
    }

    public String toXML(int level) {
        String level0 = StringUtil.repeat("\t", level);
        return level0 + "<column columnid=\"" + this.getColumnid() + "\" forceupdate=\"" + (this.isForceUpdate() ? "true" : "false") + "\" forcenullupdate=\"" + (this.isForceNullUpdate() ? "true" : "false") + "\"" + (this.value != null && this.value.length() > 0 ? " value=\"" + this.value + "\"" : "") + (this.file != null && this.file.length() > 0 ? " file=\"" + this.file + "\"" : "") + " excluded=\"" + (this.isExcluded() ? "true" : "false") + "\"/>";
    }
}

