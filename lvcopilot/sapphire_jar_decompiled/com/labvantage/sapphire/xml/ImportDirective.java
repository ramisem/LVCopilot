/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.xml;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.services.SequenceService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.xml.Column;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ImportDirective {
    private String connectionid;
    private String sdcid;
    private String keygenrule;
    private String tableid;
    private String columnid;
    private String oldvalue;
    private String newvalue;
    private SequenceService sequenceService;
    private HashMap<String, String> keyMap;
    private Type type;

    private ImportDirective() {
    }

    public static ImportDirective getReplaceValueDirective(String tableid, String columnid, HashMap<String, String> keyMap) {
        ImportDirective importDirective = new ImportDirective();
        importDirective.tableid = tableid;
        importDirective.columnid = columnid;
        importDirective.keyMap = keyMap;
        importDirective.type = Type.REPLACE;
        return importDirective;
    }

    public static ImportDirective getReplaceValueDirective(String tableid, String columnid, String oldvalue, String newvalue) {
        ImportDirective importDirective = new ImportDirective();
        importDirective.tableid = tableid;
        importDirective.columnid = columnid;
        importDirective.oldvalue = oldvalue;
        importDirective.newvalue = newvalue;
        importDirective.type = Type.REPLACE;
        return importDirective;
    }

    public static ImportDirective getResequenceDirective(String tableid, String columnid) {
        ImportDirective importDirective = new ImportDirective();
        importDirective.tableid = tableid;
        importDirective.columnid = columnid;
        importDirective.type = Type.RESEQUENCE;
        return importDirective;
    }

    public static ImportDirective getRegenerateDirective(SequenceService sequenceService, String sdcid, String keygenrule, String keycolumnid) {
        ImportDirective importDirective = new ImportDirective();
        importDirective.sequenceService = sequenceService;
        importDirective.sdcid = sdcid;
        importDirective.keygenrule = keygenrule;
        importDirective.columnid = keycolumnid;
        importDirective.type = Type.REGENERATE;
        return importDirective;
    }

    public static ImportDirective getSDINoteDirective(String connectionid) {
        ImportDirective importDirective = new ImportDirective();
        importDirective.connectionid = connectionid;
        importDirective.type = Type.SDINOTE;
        importDirective.keyMap = new HashMap();
        return importDirective;
    }

    public String getTableid() {
        return this.tableid;
    }

    public String getColumnid() {
        return this.columnid;
    }

    public String getOldvalue() {
        return this.oldvalue;
    }

    public String getNewvalue() {
        return this.newvalue;
    }

    public boolean isReplaceDirective() {
        return this.type == Type.REPLACE;
    }

    public boolean isReseqenceDirective() {
        return this.type == Type.RESEQUENCE;
    }

    public boolean isRegenerateDirective() {
        return this.type == Type.REGENERATE;
    }

    public boolean isSDINoteDirective() {
        return this.type == Type.SDINOTE;
    }

    public String executeReplaceDirective(String value) throws SapphireException {
        if (this.type == Type.REPLACE) {
            if (this.keyMap != null) {
                this.newvalue = this.keyMap.get(value);
                return this.newvalue != null ? this.newvalue : value;
            }
            if (this.oldvalue == null || this.oldvalue.length() == 0) {
                this.oldvalue = value;
                this.newvalue = StringUtil.replaceAll(this.newvalue, "[value]", value);
                return this.newvalue;
            }
            if (value.equals(this.oldvalue)) {
                return this.newvalue;
            }
            return value;
        }
        return value;
    }

    public String executeResequenceDirective(DBAccess database, String value) throws SapphireException {
        this.oldvalue = value;
        this.newvalue = value;
        if (this.type == Type.RESEQUENCE) {
            String[] seq = ((DBUtil)database).getTableSequence(this.getTableid(), 1);
            this.newvalue = seq[0];
            return this.newvalue;
        }
        return value;
    }

    public String executeRegenerateDirective(ArrayList columns, String value) throws SapphireException {
        this.oldvalue = value;
        this.newvalue = value;
        if (this.type == Type.REGENERATE) {
            DataSet keygenrow = new DataSet();
            keygenrow.addRow();
            for (int k = 0; k < columns.size(); ++k) {
                Column keygencolumn = (Column)columns.get(k);
                if (keygencolumn.getDatatype().equals("C")) {
                    keygenrow.setString(0, keygencolumn.getColumnid(), keygencolumn.getValue());
                    continue;
                }
                if (keygencolumn.getDatatype().equals("N")) {
                    keygenrow.setNumber(0, keygencolumn.getColumnid(), keygencolumn.getValue());
                    continue;
                }
                if (!keygencolumn.getDatatype().equals("D")) continue;
                keygenrow.setDate(0, keygencolumn.getColumnid(), keygencolumn.getValue());
            }
            try {
                this.sequenceService.generateKeys(this.sdcid, this.columnid, this.keygenrule, keygenrow);
                this.newvalue = keygenrow.getValue(0, this.columnid);
                return this.newvalue;
            }
            catch (ServiceException e) {
                throw new SapphireException(e);
            }
        }
        return value;
    }

    public void executeSDINoteDirective(DBAccess database, ArrayList columns) throws SapphireException {
        if (this.type == Type.SDINOTE) {
            Column column;
            int i;
            String sdcid = null;
            String keyid1 = null;
            String keyid2 = null;
            String keyid3 = null;
            for (i = 0; i < columns.size(); ++i) {
                column = (Column)columns.get(i);
                if (column.getColumnid().equalsIgnoreCase("sdcid")) {
                    sdcid = column.getValue();
                    continue;
                }
                if (column.getColumnid().equalsIgnoreCase("keyid1")) {
                    keyid1 = column.getValue();
                    continue;
                }
                if (column.getColumnid().equalsIgnoreCase("keyid2")) {
                    keyid2 = column.getValue();
                    continue;
                }
                if (!column.getColumnid().equalsIgnoreCase("keyid3")) continue;
                keyid3 = column.getValue();
            }
            for (i = 0; i < columns.size(); ++i) {
                column = (Column)columns.get(i);
                if (column.getColumnid().equalsIgnoreCase("notenum")) {
                    database.createPreparedResultSet("SELECT MAX( notenum ) notenum FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ?", new Object[]{sdcid, keyid1, keyid2, keyid3});
                    column.setValue(database.getNext() ? String.valueOf(database.getInt("notenum") + 1) : "1");
                    continue;
                }
                if (!column.getColumnid().equalsIgnoreCase("threadnum")) continue;
                if (!this.keyMap.containsKey(column.getValue())) {
                    SequenceProcessor sequenceProcessor = new SequenceProcessor(this.connectionid);
                    this.keyMap.put(column.getValue(), String.valueOf(sequenceProcessor.getSequence("SDINote", "threadnum")));
                }
                column.setValue(this.keyMap.get(column.getValue()));
            }
        }
    }

    public static enum Type {
        REPLACE,
        RESEQUENCE,
        REGENERATE,
        SDINOTE;

    }
}

