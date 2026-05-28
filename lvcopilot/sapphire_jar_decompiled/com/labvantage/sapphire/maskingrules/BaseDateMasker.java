/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.maskingrules;

import com.labvantage.sapphire.maskingrules.BaseMasker;
import com.labvantage.sapphire.services.SapphireConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import sapphire.xml.PropertyList;

public class BaseDateMasker
extends BaseMasker {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";
    private String defaultUpperAgeLimit = "2147483647";
    private DateFormat _dateFormat;
    private MASKING_LOGIC maskingLogic;

    public final void setMaskingLogic(MASKING_LOGIC maskingLogic) {
        this.maskingLogic = maskingLogic;
    }

    private BaseDateMasker() {
    }

    public BaseDateMasker(SapphireConnection sapphireConnection) {
        super(sapphireConnection);
        this.maskingLogic = MASKING_LOGIC.REPLACE_WITH;
    }

    public final void setDateFormat(DateFormat dateFormat) {
        this._dateFormat = dateFormat;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String mask(Calendar calendar, PropertyList maskProps) {
        String value = "";
        if (calendar == null) {
            return "";
        }
        if (MASKING_LOGIC.AGE_RANGE == this.maskingLogic) {
            int upperAgeLimit;
            GregorianCalendar todayCal = (GregorianCalendar)GregorianCalendar.getInstance();
            int ageToday = todayCal.get(1) - calendar.get(1);
            int ageRangeSize = Integer.parseInt(maskProps.getProperty("rangeSize", "10"));
            if (ageRangeSize < 0) {
                ageRangeSize = -ageRangeSize;
            }
            if ((upperAgeLimit = Integer.parseInt(maskProps.getProperty("upperAgeLimit", this.defaultUpperAgeLimit))) <= ageToday) {
                return "";
            }
            int i = ageRangeSize;
            while (true) {
                if (ageToday < 0) {
                    if (ageToday > -i) {
                        return i + " - " + (i - ageRangeSize);
                    }
                } else if (ageToday < i) {
                    return i - ageRangeSize + " - " + i;
                }
                i += ageRangeSize;
            }
        }
        if (MASKING_LOGIC.REPLACE_WITH == this.maskingLogic) {
            String replaceChar = maskProps.getProperty("replaceChar", "");
            String replaceStr = maskProps.getProperty("replaceStr", "");
            if (replaceChar.length() <= 0) return replaceStr;
            return this.getFormattedValue(calendar).replaceAll(".", replaceChar.charAt(0) + "");
        }
        if (MASKING_LOGIC.PATTERN == this.maskingLogic || MASKING_LOGIC.MONTH_N_YEAR_ONLY == this.maskingLogic || MASKING_LOGIC.YEAR_ONLY == this.maskingLogic) {
            GregorianCalendar todayCal = (GregorianCalendar)GregorianCalendar.getInstance();
            int ageToday = todayCal.get(1) - calendar.get(1);
            int upperAgeLimit = Integer.parseInt(maskProps.getProperty("upperAgeLimit", this.defaultUpperAgeLimit));
            if (upperAgeLimit <= ageToday) {
                return "";
            }
            String pattern = "";
            switch (this.maskingLogic) {
                case PATTERN: {
                    pattern = maskProps.getProperty("pattern", "");
                    break;
                }
                case MONTH_N_YEAR_ONLY: {
                    pattern = "MMM-yyyy";
                    break;
                }
                case YEAR_ONLY: {
                    pattern = "yyyy";
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(calendar.getTime());
        }
        if (MASKING_LOGIC.ALL_STAR != this.maskingLogic) return value;
        value = this.getFormattedValue(calendar);
        return value.replaceAll(".", "*");
    }

    public PropertyList parsePolicyProperty(PropertyList dateDataTypeProps) {
        PropertyList maskingProps = new PropertyList();
        PropertyList templateProperties = dateDataTypeProps.getPropertyListNotNull((Object)((Object)this.maskingLogic) + "_options");
        switch (this.maskingLogic) {
            case AGE_RANGE: {
                maskingProps.setProperty("rangeSize", templateProperties.getProperty("rangeSize"));
                maskingProps.setProperty("upperAgeLimit", templateProperties.getProperty("upperAgeLimit"));
                break;
            }
            case MONTH_N_YEAR_ONLY: {
                maskingProps.setProperty("upperAgeLimit", templateProperties.getProperty("upperAgeLimit"));
                break;
            }
            case PATTERN: {
                maskingProps.setProperty("pattern", templateProperties.getProperty("pattern"));
                maskingProps.setProperty("upperAgeLimit", templateProperties.getProperty("upperAgeLimit"));
                break;
            }
            case REPLACE_WITH: {
                maskingProps.setProperty("replaceChar", templateProperties.getProperty("replaceChar"));
                maskingProps.setProperty("replaceStr", templateProperties.getProperty("replaceStr"));
                break;
            }
            case YEAR_ONLY: {
                maskingProps.setProperty("upperAgeLimit", templateProperties.getProperty("upperAgeLimit"));
            }
        }
        return maskingProps;
    }

    private String getFormattedValue(Calendar calendar) {
        if (this._dateFormat != null) {
            return this._dateFormat.format(calendar.getTime());
        }
        return this.getM18NUtil().format(calendar);
    }

    public static enum MASKING_LOGIC {
        ALL_STAR,
        AGE_RANGE,
        MONTH_N_YEAR_ONLY,
        PATTERN,
        REPLACE_WITH,
        YEAR_ONLY;

    }
}

