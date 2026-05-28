/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.storage;

import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class Label {
    static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    protected static final int WRAPPING_OPTION_INCREMENT = 1;
    protected static final int WRAPPING_OPTION_REPEAT = 2;
    protected static final int LABEL_TYPE_ALPHABET = 1;
    protected static final int LABEL_TYPE_NUMERIC = 2;
    protected int labelType = 0;
    protected int wrappingOption = 0;
    protected int numericStartAt = 0;
    protected int numericEndAt = 0;
    protected int numericCurrentLabel = 0;
    protected String alphaStartAt = null;
    protected String alphaEndAt = null;
    protected String alphaCurrentLabel = null;
    protected int[] repeatNumericLabels = null;
    protected int step = 1;
    private boolean isLabelGenerationStarted = false;
    private int lastIndex = 0;
    private static final int ALPHABET_COUNT = 26;

    public Label(PropertyList labelProps) throws SapphireException {
        this.initialize(labelProps);
    }

    public Label(PropertyList labelProps, int lastIndex) throws SapphireException {
        this.lastIndex = lastIndex;
        this.initialize(labelProps);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void initialize(PropertyList labelProps) throws SapphireException {
        if (labelProps == null) return;
        String wOption = labelProps.getProperty("wrappingoption", "Increment");
        if ("Increment".equalsIgnoreCase(wOption)) {
            this.wrappingOption = 1;
        } else if ("Repeat".equalsIgnoreCase(wOption)) {
            this.wrappingOption = 2;
            if (labelProps.getProperty("endat").length() == 0) {
                throw new SapphireException("Label generation \"End At\" not defined.");
            }
        }
        String type = labelProps.getProperty("type", "Numeric");
        if ("Numeric".equalsIgnoreCase(type)) {
            this.labelType = 2;
            try {
                this.numericStartAt = Integer.parseInt(labelProps.getProperty("startat", "1")) + this.lastIndex;
                if (this.wrappingOption != 2) return;
                this.numericEndAt = Integer.parseInt(labelProps.getProperty("endat"));
                this.numericStartAt = Integer.parseInt(labelProps.getProperty("startat", "1"));
                return;
            }
            catch (Exception ex) {
                throw new SapphireException("Label Generation options not set properly. ");
            }
        } else {
            if (!"Alphabet".equalsIgnoreCase(type)) return;
            this.labelType = 1;
            this.alphaStartAt = this.lastIndex == 0 ? labelProps.getProperty("startat", "A") : this.nextAlpha(labelProps.getProperty("startat", "A").charAt(0), this.lastIndex);
            this.alphaEndAt = labelProps.getProperty("endat");
            if (this.wrappingOption != 2) return;
            this.alphaStartAt = labelProps.getProperty("startat", "A");
        }
    }

    public String getIndices(int size) {
        StringBuffer sb = new StringBuffer();
        String indexList = "";
        for (int count = this.lastIndex + 1; count <= this.lastIndex + size; ++count) {
            sb.append(Integer.toString(count)).append(";");
        }
        if (sb.toString().trim().length() > 0) {
            indexList = sb.substring(0, sb.length() - 1);
        }
        return indexList;
    }

    public String getLabels(int size) {
        StringBuffer labelsList = new StringBuffer();
        String labels = "";
        for (int count = 1; count <= size; ++count) {
            if (this.labelType == 2) {
                labelsList.append(this.getNextNumericLabel()).append(";");
                continue;
            }
            if (this.labelType != 1) continue;
            labelsList.append(this.getNextAlphaLabel()).append(";");
        }
        if (labelsList.toString().trim().length() > 0) {
            labels = labelsList.substring(0, labelsList.length() - 1);
        }
        return labels;
    }

    private int getNextNumericLabel() {
        int label = 0;
        if (this.isLabelGenerationStarted) {
            if (this.wrappingOption == 1) {
                this.numericCurrentLabel += this.step;
            } else if (this.wrappingOption == 2) {
                this.numericCurrentLabel = this.numericCurrentLabel == this.numericEndAt ? this.numericStartAt : (this.numericCurrentLabel += this.step);
            }
        } else {
            this.numericCurrentLabel = this.numericStartAt;
            if (this.wrappingOption == 2) {
                this.numericCurrentLabel = this.revisedNumericStartAt(this.numericStartAt, this.numericEndAt, this.lastIndex);
            }
            this.isLabelGenerationStarted = true;
        }
        label = this.numericCurrentLabel;
        return label;
    }

    private String getNextAlphaLabel() {
        String label = "";
        if (this.isLabelGenerationStarted) {
            if (this.wrappingOption == 1) {
                this.alphaCurrentLabel = this.incrementAlpha(this.alphaCurrentLabel);
            } else if (this.wrappingOption == 2) {
                this.alphaCurrentLabel = this.alphaCurrentLabel.equals(this.alphaEndAt) ? this.alphaStartAt : this.incrementAlpha(this.alphaCurrentLabel);
            }
        } else {
            this.alphaCurrentLabel = this.alphaStartAt;
            if (this.wrappingOption == 2) {
                this.alphaCurrentLabel = this.revisedAlphaStartAt(this.alphaStartAt, this.alphaEndAt, this.lastIndex);
            }
            this.isLabelGenerationStarted = true;
        }
        label = this.alphaCurrentLabel;
        return label;
    }

    public String incrementAlpha(String alpha) {
        StringBuffer newAlpha = new StringBuffer();
        boolean carryOver = true;
        if (alpha != null) {
            for (int count = 1; count <= alpha.length(); ++count) {
                char character = alpha.charAt(alpha.length() - count);
                if (character == 'Z' && carryOver) {
                    newAlpha.append('A');
                    carryOver = true;
                    continue;
                }
                if (character == 'z' && carryOver) {
                    newAlpha.append('a');
                    carryOver = true;
                    continue;
                }
                if (carryOver) {
                    newAlpha.append((char)(character + '\u0001'));
                } else {
                    newAlpha.append(character);
                }
                carryOver = false;
            }
            if (carryOver) {
                if (Character.isLowerCase(alpha.charAt(0))) {
                    newAlpha.append('a');
                } else if (Character.isUpperCase(alpha.charAt(0))) {
                    newAlpha.append('A');
                }
            }
        }
        return newAlpha.reverse().toString();
    }

    public String nextAlpha(char startchar, int lastIndex) {
        char begin = Character.isLowerCase(startchar) ? (char)'a' : 'A';
        int diff = startchar - begin;
        int revisedLastIndex = lastIndex + diff;
        return this.incrementAlpha(this.calulatedString(revisedLastIndex, begin));
    }

    private String calulatedString(int revisedLastIndex, char begin) {
        if (revisedLastIndex < 26) {
            return String.valueOf((char)(begin + revisedLastIndex - 1));
        }
        String str = "";
        int quotient = revisedLastIndex / 26;
        int rem = revisedLastIndex % 26;
        str = this.calulatedString(quotient, begin) + String.valueOf((char)(begin + rem - 1));
        return str;
    }

    public int revisedNumericStartAt(int startAt, int endAt, int lastIndex) {
        int diff = endAt - startAt + 1;
        int mod = lastIndex % diff;
        return startAt + mod;
    }

    public String revisedAlphaStartAt(String startAt, String endAt, int lastIndex) {
        char startat = startAt.charAt(0);
        char endat = endAt.charAt(0);
        int diff = endat - startat + 1;
        int mod = lastIndex % diff;
        return String.valueOf((char)(startat + mod));
    }
}

