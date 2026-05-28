/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.storage;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.storage.BaseStorageUnitType;
import com.labvantage.sapphire.modules.storage.Label;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Grid
extends BaseStorageUnitType {
    String LABVANTAGE_CVS_ID = "$Revision: 64119 $";
    public static final String VERTICAL_INDEX_ORDER_TOP_TO_BOTTOM = "Top->Bottom";
    public static final String VERTICAL_INDEX_ORDER_BOTTOM_TO_TOP = "Bottom->Top";
    public static final String HORIZONTAL_INDEX_ORDER_LEFT_TO_RIGHT = "Left->Right";
    public static final String HORIZONTAL_INDEX_ORDER_RIGHT_TO_LEFT = "Right->Left";
    public static final String ORIENTATION_ROW_MAJOR = "Row Major";
    public static final String ORIENTATION_COLUMN_MAJOR = "Column Major";
    private String[] horizontalLabels = null;
    private String[] verticalLabels = null;
    private String[][] displayLabels = null;
    private String[][] displayIndices = null;

    public Grid() {
    }

    public Grid(PropertyList storageUnitType) throws Exception {
        super(storageUnitType);
    }

    public String[][] getDisplayLabels() {
        return this.displayLabels;
    }

    public String[][] getDisplayIndices() {
        return this.displayIndices;
    }

    public String[] getHorizontalLabels() {
        return this.horizontalLabels;
    }

    public String[] getVerticalLabels() {
        return this.verticalLabels;
    }

    @Override
    public void initialize(int size) throws SapphireException {
        int rows = Integer.parseInt(this.storageUnitType.getProperty("rows"));
        int columns = Integer.parseInt(this.storageUnitType.getProperty("columns"));
        String orientation = this.storageUnitType.getProperty("orientation", ORIENTATION_ROW_MAJOR);
        PropertyList indexOrder = this.storageUnitType.getPropertyList("indexorder");
        PropertyList labelGenRule = this.storageUnitType.getPropertyList("labelgenrule");
        if (labelGenRule == null || indexOrder == null) {
            return;
        }
        String horizontalIndexOrder = indexOrder.getProperty("horizontal", HORIZONTAL_INDEX_ORDER_LEFT_TO_RIGHT);
        String verticalIndexOrder = indexOrder.getProperty("vertical", VERTICAL_INDEX_ORDER_TOP_TO_BOTTOM);
        String useIndex = labelGenRule.getProperty("useindex", "N");
        if ("N".equalsIgnoreCase(useIndex)) {
            PropertyList horizontalLabelProps = labelGenRule.getPropertyList("horizontallabelgenrule");
            PropertyList verticalLabelProps = labelGenRule.getPropertyList("verticallabelgenrule");
            if (horizontalLabelProps == null || verticalLabelProps == null) {
                Trace.logInfo("PropertyList is null");
                return;
            }
            Label horizonalLabelGenerator = new Label(horizontalLabelProps);
            Label verticalLabelGenerator = new Label(verticalLabelProps);
            String tempHorizontalLabels = horizonalLabelGenerator.getLabels(columns);
            String tempVerticalLabels = verticalLabelGenerator.getLabels(rows);
            if (HORIZONTAL_INDEX_ORDER_RIGHT_TO_LEFT.equals(horizontalIndexOrder)) {
                tempHorizontalLabels = OpalUtil.reverse(tempHorizontalLabels, ";");
            }
            if (VERTICAL_INDEX_ORDER_BOTTOM_TO_TOP.equals(verticalIndexOrder)) {
                tempVerticalLabels = OpalUtil.reverse(tempVerticalLabels, ";");
            }
            this.horizontalLabels = StringUtil.split(tempHorizontalLabels, ";");
            this.verticalLabels = StringUtil.split(tempVerticalLabels, ";");
            this.generateNonIndexLabels(rows, columns, orientation, horizontalIndexOrder, verticalIndexOrder);
        } else if ("Y".equalsIgnoreCase(useIndex)) {
            this.generateIndexLabels(rows, columns, orientation, horizontalIndexOrder, verticalIndexOrder);
        }
    }

    private void generateNonIndexLabels(int rows, int columns, String orientation, String horizontalIndexOrder, String verticalIndexOrder) {
        int row;
        int col;
        int col2;
        int row2;
        ArrayList<String> indices = new ArrayList<String>();
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<String> rowLabel = new ArrayList<String>();
        ArrayList<String> columnLabel = new ArrayList<String>();
        if (HORIZONTAL_INDEX_ORDER_LEFT_TO_RIGHT.equals(horizontalIndexOrder)) {
            if (VERTICAL_INDEX_ORDER_TOP_TO_BOTTOM.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row2 = 0; row2 < rows; ++row2) {
                        for (col2 = 0; col2 < columns; ++col2) {
                            labels.add(this.verticalLabels[row2] + this.horizontalLabels[col2]);
                            rowLabel.add(this.verticalLabels[row2]);
                            columnLabel.add(this.horizontalLabels[col2]);
                        }
                    }
                } else {
                    for (col = 0; col < columns; ++col) {
                        for (row = 0; row < rows; ++row) {
                            labels.add(this.horizontalLabels[col] + this.verticalLabels[row]);
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row]);
                        }
                    }
                }
            } else if (VERTICAL_INDEX_ORDER_BOTTOM_TO_TOP.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row2 = rows - 1; row2 >= 0; --row2) {
                        for (col2 = 0; col2 < columns; ++col2) {
                            labels.add(this.verticalLabels[row2] + this.horizontalLabels[col2]);
                            rowLabel.add(this.verticalLabels[row2]);
                            columnLabel.add(this.horizontalLabels[col2]);
                        }
                    }
                } else {
                    for (col = 0; col < columns; ++col) {
                        for (row = rows - 1; row >= 0; --row) {
                            labels.add(this.horizontalLabels[col] + this.verticalLabels[row]);
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row]);
                        }
                    }
                }
            }
        } else if (HORIZONTAL_INDEX_ORDER_RIGHT_TO_LEFT.equals(horizontalIndexOrder)) {
            if (VERTICAL_INDEX_ORDER_TOP_TO_BOTTOM.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row2 = 0; row2 < rows; ++row2) {
                        for (col2 = columns - 1; col2 >= 0; --col2) {
                            labels.add(this.verticalLabels[row2] + this.horizontalLabels[col2]);
                            rowLabel.add(this.verticalLabels[row2]);
                            columnLabel.add(this.horizontalLabels[col2]);
                        }
                    }
                } else {
                    for (col = columns - 1; col >= 0; --col) {
                        for (row = 0; row < rows; ++row) {
                            labels.add(this.horizontalLabels[col] + this.verticalLabels[row]);
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row]);
                        }
                    }
                }
            } else if (VERTICAL_INDEX_ORDER_BOTTOM_TO_TOP.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row2 = rows - 1; row2 >= 0; --row2) {
                        for (col2 = columns - 1; col2 >= 0; --col2) {
                            labels.add(this.verticalLabels[row2] + this.horizontalLabels[col2]);
                            rowLabel.add(this.verticalLabels[row2]);
                            columnLabel.add(this.horizontalLabels[col2]);
                        }
                    }
                } else {
                    for (col = columns - 1; col >= 0; --col) {
                        for (row = rows - 1; row >= 0; --row) {
                            labels.add(this.horizontalLabels[col] + this.verticalLabels[row]);
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row]);
                        }
                    }
                }
            }
        }
        for (int i = 1; i <= labels.size(); ++i) {
            indices.add(String.valueOf(i));
        }
        this.storageUnitLabels = OpalUtil.toDelimitedString(labels, ";");
        this.storageUnitIndex = OpalUtil.toDelimitedString(indices, ";");
        this.storageUnitRowLabels = OpalUtil.toDelimitedString(rowLabel, ";");
        this.storageUnitColumnLabels = OpalUtil.toDelimitedString(columnLabel, ";");
    }

    private void generateIndexLabels(int rows, int columns, String orientation, String horizontalIndexOrder, String verticalIndexOrder) {
        int col;
        int row;
        int index = 0;
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<String> rowLabel = new ArrayList<String>();
        ArrayList<String> columnLabel = new ArrayList<String>();
        String horizontalLabel = "A";
        this.horizontalLabels = new String[columns];
        this.horizontalLabels[0] = horizontalLabel;
        for (int i = 1; i < columns; ++i) {
            char charValue = horizontalLabel.charAt(0);
            this.horizontalLabels[i] = horizontalLabel = String.valueOf((char)(charValue + '\u0001'));
        }
        String verticalLabel = "1";
        this.verticalLabels = new String[rows];
        this.verticalLabels[0] = verticalLabel;
        for (int i = 1; i < rows; ++i) {
            this.verticalLabels[i] = verticalLabel = String.valueOf(i + 1);
        }
        if (HORIZONTAL_INDEX_ORDER_LEFT_TO_RIGHT.equals(horizontalIndexOrder)) {
            if (VERTICAL_INDEX_ORDER_TOP_TO_BOTTOM.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row = 0; row < rows; ++row) {
                        for (int col2 = 0; col2 < columns; ++col2) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.verticalLabels[row]);
                            columnLabel.add(this.horizontalLabels[col2]);
                        }
                    }
                } else {
                    for (col = 0; col < columns; ++col) {
                        for (int row2 = 0; row2 < rows; ++row2) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row2]);
                        }
                    }
                }
            } else if (VERTICAL_INDEX_ORDER_BOTTOM_TO_TOP.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row = rows - 1; row >= 0; --row) {
                        for (int col3 = 0; col3 < columns; ++col3) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.verticalLabels[row]);
                            columnLabel.add(this.horizontalLabels[col3]);
                        }
                    }
                } else {
                    for (col = 0; col < columns; ++col) {
                        for (int row3 = rows - 1; row3 >= 0; --row3) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row3]);
                        }
                    }
                }
            }
        } else if (HORIZONTAL_INDEX_ORDER_RIGHT_TO_LEFT.equals(horizontalIndexOrder)) {
            if (VERTICAL_INDEX_ORDER_TOP_TO_BOTTOM.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row = 0; row < rows; ++row) {
                        for (int col4 = columns - 1; col4 >= 0; --col4) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.verticalLabels[row]);
                            columnLabel.add(this.horizontalLabels[col4]);
                        }
                    }
                } else {
                    for (col = columns - 1; col >= 0; --col) {
                        for (int row4 = 0; row4 < rows; ++row4) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row4]);
                        }
                    }
                }
            } else if (VERTICAL_INDEX_ORDER_BOTTOM_TO_TOP.equals(verticalIndexOrder)) {
                if (ORIENTATION_ROW_MAJOR.equals(orientation)) {
                    for (row = rows - 1; row >= 0; --row) {
                        for (int col5 = columns - 1; col5 >= 0; --col5) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.verticalLabels[row]);
                            columnLabel.add(this.horizontalLabels[col5]);
                        }
                    }
                } else {
                    for (col = columns - 1; col >= 0; --col) {
                        for (int row5 = rows - 1; row5 >= 0; --row5) {
                            labels.add(String.valueOf(++index));
                            rowLabel.add(this.horizontalLabels[col]);
                            columnLabel.add(this.verticalLabels[row5]);
                        }
                    }
                }
            }
        }
        this.storageUnitIndex = this.storageUnitLabels = OpalUtil.toDelimitedString(labels, ";");
        this.storageUnitRowLabels = OpalUtil.toDelimitedString(rowLabel, ";");
        this.storageUnitColumnLabels = OpalUtil.toDelimitedString(columnLabel, ";");
    }
}

