/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.maskingrules;

import com.labvantage.sapphire.maskingrules.BaseMasker;
import com.labvantage.sapphire.services.SapphireConnection;
import java.math.BigDecimal;
import java.text.NumberFormat;
import sapphire.xml.PropertyList;

public class BaseNumberMasker
extends BaseMasker {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private NumberFormat _numberFormat;
    private MASKING_LOGIC maskingLogic;

    public final void setMaskingLogic(MASKING_LOGIC maskingLogic) {
        this.maskingLogic = maskingLogic;
    }

    private BaseNumberMasker() {
    }

    public BaseNumberMasker(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.maskingLogic = MASKING_LOGIC.ALL_STAR;
    }

    public final void setNumberFormat(NumberFormat numberFormat) {
        this._numberFormat = numberFormat;
    }

    public String mask(BigDecimal number, PropertyList maskProps) {
        String value = "";
        if (number == null) {
            return "";
        }
        block0 : switch (this.maskingLogic) {
            case RANGE: {
                int rangeSize = Integer.parseInt(maskProps.getProperty("rangeSize", "10"));
                if (rangeSize < 0) {
                    rangeSize = 10;
                }
                int numberIntVal = number.intValue();
                int i = rangeSize;
                while (true) {
                    if (numberIntVal < i) {
                        value = i - rangeSize + " - " + i;
                        break block0;
                    }
                    i += rangeSize;
                }
            }
            case ALL_STAR: 
            case REPLACE_WITH: {
                String replaceChar = maskProps.getProperty("replaceChar", "");
                String replaceStr = maskProps.getProperty("replaceStr", "");
                if (this.maskingLogic == MASKING_LOGIC.ALL_STAR) {
                    replaceChar = "*";
                }
                if (replaceChar.length() > 0) {
                    value = this._numberFormat != null ? this._numberFormat.format(number.doubleValue()) : this.getM18NUtil().format(number);
                    value = value.replaceAll(".", replaceChar.charAt(0) + "");
                    break;
                }
                value = replaceStr;
            }
        }
        return value;
    }

    public PropertyList parsePolicyProperty(PropertyList numberDataTypeProps) {
        PropertyList maskingProps = new PropertyList();
        PropertyList templateProperties = numberDataTypeProps.getPropertyListNotNull((Object)((Object)this.maskingLogic) + "_options");
        switch (this.maskingLogic) {
            case RANGE: {
                maskingProps.setProperty("rangeSize", templateProperties.getProperty("rangeSize"));
                break;
            }
            case REPLACE_WITH: {
                maskingProps.setProperty("replaceChar", templateProperties.getProperty("replaceChar"));
                maskingProps.setProperty("replaceStr", templateProperties.getProperty("replaceStr"));
            }
        }
        return maskingProps;
    }

    public static enum MASKING_LOGIC {
        ALL_STAR,
        RANGE,
        REPLACE_WITH;

    }
}

