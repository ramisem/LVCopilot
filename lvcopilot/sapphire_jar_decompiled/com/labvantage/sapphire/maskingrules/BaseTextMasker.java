/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.maskingrules;

import com.labvantage.sapphire.maskingrules.BaseMasker;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BaseTextMasker
extends BaseMasker {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private MASKING_LOGIC maskingLogic;

    public final void setMaskingLogic(MASKING_LOGIC maskingLogic) {
        this.maskingLogic = maskingLogic;
    }

    private BaseTextMasker() {
    }

    public BaseTextMasker(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.maskingLogic = MASKING_LOGIC.ALL_STAR;
    }

    public String mask(String text, PropertyList maskProps) {
        if (text == null) {
            return "";
        }
        if (this.maskingLogic == MASKING_LOGIC.FIRST_N_CHAR || this.maskingLogic == MASKING_LOGIC.LAST_N_CHAR || this.maskingLogic == MASKING_LOGIC.FIRST_N_PERCENT_CHAR || this.maskingLogic == MASKING_LOGIC.LAST_N_PERCENT_CHAR) {
            String noOfCharsStr = maskProps.getProperty("noOfChars");
            if (noOfCharsStr.length() == 0) {
                noOfCharsStr = this.maskingLogic == MASKING_LOGIC.FIRST_N_CHAR || this.maskingLogic == MASKING_LOGIC.LAST_N_CHAR ? "0" : "100";
            }
            int noOfChars = Integer.parseInt(noOfCharsStr);
            String replaceChar = maskProps.getProperty("replaceChar", "");
            String replaceStr = maskProps.getProperty("replaceStr", "");
            text = replaceChar.length() > 0 ? this.replaceNChars(text, noOfChars, this.maskingLogic, replaceChar, true) : this.replaceNChars(text, noOfChars, this.maskingLogic, replaceStr, false);
        } else if (this.maskingLogic == MASKING_LOGIC.ONLY_INITIALS) {
            String splitWith = maskProps.getProperty("splitDelimiter", " ");
            String[] textSplit = StringUtil.split(text, splitWith);
            StringBuffer initials = new StringBuffer();
            for (String str : textSplit) {
                if (str.length() <= 0) continue;
                initials.append(str.charAt(0));
            }
            text = initials.toString();
        } else if (this.maskingLogic == MASKING_LOGIC.ALL_STAR) {
            text = this.replaceNChars(text, 100, MASKING_LOGIC.FIRST_N_PERCENT_CHAR, "*", true);
        }
        return text;
    }

    private String replaceNChars(String source, int numberOfChars, MASKING_LOGIC maskingLogic, String replaceWith, boolean replaceEachChar) {
        StringBuffer sb = new StringBuffer(source);
        switch (maskingLogic) {
            case FIRST_N_PERCENT_CHAR: {
                if (numberOfChars > 100) {
                    numberOfChars = 100;
                }
                if (numberOfChars < 0) {
                    numberOfChars = 0;
                }
                numberOfChars = Math.round((float)(numberOfChars * sb.length()) / 100.0f);
            }
            case FIRST_N_CHAR: {
                if (numberOfChars < 0) {
                    numberOfChars = 0;
                }
                if (replaceEachChar) {
                    for (int i = 0; i < sb.length() && i < numberOfChars; ++i) {
                        sb.setCharAt(i, replaceWith.charAt(0));
                    }
                    break;
                }
                sb.replace(0, numberOfChars > sb.length() ? sb.length() : numberOfChars, replaceWith);
                break;
            }
            case LAST_N_PERCENT_CHAR: {
                if (numberOfChars > 100) {
                    numberOfChars = 100;
                }
                if (numberOfChars < 0) {
                    numberOfChars = 0;
                }
                numberOfChars = Math.round((float)(numberOfChars * sb.length()) / 100.0f);
            }
            case LAST_N_CHAR: {
                if (numberOfChars < 0) {
                    numberOfChars = 0;
                }
                if (replaceEachChar) {
                    int i = sb.length() - 1;
                    for (int j = 0; i >= 0 && j < numberOfChars; --i, ++j) {
                        sb.setCharAt(i, replaceWith.charAt(0));
                    }
                    break;
                }
                sb.replace(numberOfChars > sb.length() ? 0 : sb.length() - numberOfChars, sb.length(), replaceWith);
            }
        }
        return sb.toString();
    }

    public PropertyList parsePolicyProperty(PropertyList textDataTypeProps) {
        PropertyList maskingProps = new PropertyList();
        PropertyList templateProperties = textDataTypeProps.getPropertyListNotNull((Object)((Object)this.maskingLogic) + "_options");
        switch (this.maskingLogic) {
            case FIRST_N_PERCENT_CHAR: 
            case FIRST_N_CHAR: 
            case LAST_N_PERCENT_CHAR: 
            case LAST_N_CHAR: {
                maskingProps.setProperty("noOfChars", templateProperties.getProperty("noOfChars"));
                maskingProps.setProperty("replaceChar", templateProperties.getProperty("replaceChar"));
                maskingProps.setProperty("replaceStr", templateProperties.getProperty("replaceStr"));
                break;
            }
            case ONLY_INITIALS: {
                maskingProps.setProperty("splitDelimiter", templateProperties.getProperty("splitDelimiter"));
            }
        }
        return maskingProps;
    }

    public static enum MASKING_LOGIC {
        ALL_STAR,
        FIRST_N_PERCENT_CHAR,
        FIRST_N_CHAR,
        LAST_N_PERCENT_CHAR,
        LAST_N_CHAR,
        ONLY_INITIALS;

    }
}

