/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.util.images.ImageRef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class SDIView
extends BaseCustom {
    private String label = "";
    private SDI sdi;
    private ImageRef imageRef = null;
    private ArrayList<SDIViewResponsiveField> fields = null;
    private String viewURL = "";
    private HashMap<String, String> evalParams = null;
    private String editURL = "";

    public SDIView(String connectionId) {
        this.setConnectionId(connectionId);
        this.fields = new ArrayList();
    }

    public SDIView(String sdcid, String keyid1, String keyid2, String keyid3, String connectionId) {
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        this.setSdi(sdi);
        this.setConnectionId(connectionId);
        this.fields = new ArrayList();
    }

    public SDIView(SDIData sdiData, int row, ArrayList<SDIViewResponsiveField> fields, String connectionId) {
        this.setConnectionId(connectionId);
        this.fromSDIData(sdiData, row, fields);
    }

    public SDI getSdi() {
        return this.sdi;
    }

    public void setSdi(SDI sdi) {
        this.sdi = sdi;
    }

    public String getLabel() {
        if (this.label == null || this.label.length() == 0) {
            if (this.sdi != null) {
                return this.sdi.getKeyid1() + (this.sdi.getKeyid2().length() > 0 && this.sdi.getKeyid2() != null && !this.sdi.getKeyid2().equalsIgnoreCase("(null)") ? " (" + this.sdi.getKeyid2() + (this.sdi.getKeyid3().length() > 0 && this.sdi.getKeyid3() != null && !this.sdi.getKeyid3().equalsIgnoreCase("(null)") ? " - " + this.sdi.getKeyid3() : "") + ")" : "");
            }
            return "";
        }
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ImageRef getImageRef() {
        if (this.imageRef == null) {
            this.imageRef = new ImageRef(this.getConnectionProcessor().getSapphireConnection());
            this.imageRef.setImage("Cat");
        }
        return this.imageRef;
    }

    public void setImageRef(ImageRef imageRef) {
        this.imageRef = imageRef;
    }

    public ArrayList<SDIViewResponsiveField> getFields() {
        Collections.sort(this.fields);
        return this.fields;
    }

    public void setFields(ArrayList<SDIViewResponsiveField> fields) {
        this.fields = fields;
    }

    public void addField(String columId, String label, String value, FieldPriority priority) {
        this.fields.add(new SDIViewResponsiveField(columId, label, value, priority));
    }

    public void fromSDIData(SDIData sdiData, int row, ArrayList<SDIViewResponsiveField> fields) {
        DataSet pri = sdiData.getDataset("primary");
        if (pri != null && pri.getRowCount() > row) {
            String[] keys = sdiData.getKeys("primary");
            this.setSdi(new SDI(sdiData.getSdcid(), pri.getValue(row, keys[0]), keys.length > 1 ? pri.getValue(row, keys[1]) : "", keys.length > 2 ? pri.getValue(row, keys[2]) : ""));
            if (fields != null) {
                this.setFields(fields);
                for (SDIViewResponsiveField field : fields) {
                    field.setValue(pri.getValue(row, field.getColumnid(), ""));
                }
            }
        }
    }

    private HashMap<String, String> getEvalParams() {
        if (this.evalParams == null) {
            this.evalParams = new HashMap();
            this.evalParams.put("sdcid", this.sdi.getSdcid());
            this.evalParams.put("keyid1", this.sdi.getKeyid1());
            this.evalParams.put("keyid2", this.sdi.getKeyid2());
            this.evalParams.put("keyid3", this.sdi.getKeyid3());
        }
        return this.evalParams;
    }

    public String getViewURL() {
        try {
            return this.evaluateExpression(this.viewURL, this.getEvalParams());
        }
        catch (Exception e) {
            return this.viewURL;
        }
    }

    public void setViewURL(String viewURL) {
        this.viewURL = viewURL;
    }

    private String evaluateExpression(String string, HashMap<String, String> evalParams) {
        String[] toks;
        for (String tok : toks = StringUtil.getTokens(string, "[", "]")) {
            string = StringUtil.replaceAll(string, "[" + tok + "]", evalParams.containsKey(tok) ? evalParams.get(tok) : "", false);
        }
        return string;
    }

    public String getEditURL() {
        try {
            return this.evaluateExpression(this.editURL, this.getEvalParams());
        }
        catch (Exception e) {
            return this.editURL;
        }
    }

    public void setEditURL(String editURL) {
        this.editURL = editURL;
    }

    public static class SDIViewResponsiveField
    implements Comparable<SDIViewResponsiveField> {
        private String columnid;
        private String label;
        private String value;
        private FieldPriority priority;

        public SDIViewResponsiveField(String columnid, String label, String value, FieldPriority priority) {
            this.setColumnid(columnid);
            this.setLabel(label);
            this.setValue(value);
            this.setPriority(priority);
        }

        public SDIViewResponsiveField(String columnid, String label, FieldPriority priority) {
            this.setColumnid(columnid);
            this.setLabel(label);
            this.setValue("");
            this.setPriority(priority);
        }

        public String getColumnid() {
            return this.columnid;
        }

        public void setColumnid(String columnid) {
            this.columnid = columnid;
        }

        public String getLabel() {
            if (this.label == null || this.label.length() == 0) {
                return this.columnid;
            }
            return this.label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public FieldPriority getPriority() {
            return this.priority;
        }

        public void setPriority(FieldPriority priority) {
            this.priority = priority;
        }

        @Override
        public int compareTo(SDIViewResponsiveField o) {
            switch (o.getPriority()) {
                case HIGH: {
                    switch (this.getPriority()) {
                        case HIGH: {
                            return 0;
                        }
                        case MEDIUM: 
                        case LOW: {
                            return -1;
                        }
                    }
                }
                case MEDIUM: {
                    switch (this.getPriority()) {
                        case HIGH: {
                            return 1;
                        }
                        case MEDIUM: {
                            return 0;
                        }
                        case LOW: {
                            return -1;
                        }
                    }
                }
                case LOW: {
                    switch (this.getPriority()) {
                        case LOW: {
                            return 0;
                        }
                        case HIGH: 
                        case MEDIUM: {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }
    }

    public static enum FieldPriority {
        HIGH,
        MEDIUM,
        LOW;

    }
}

