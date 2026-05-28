/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import sapphire.util.StringUtil;

public class FormatUtil
implements Serializable {
    private Locale locale;
    private char decimalSeparator;
    private char groupingSeparator;
    private boolean allowGroupingSeparator = true;
    private boolean validateGroupingPos = true;
    private int groupingSize;
    private static final BigDecimal zero = new BigDecimal("0");

    private FormatUtil(Locale locale) {
        this.locale = locale;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);
        this.decimalSeparator = decimalFormatSymbols.getDecimalSeparator();
        this.groupingSeparator = decimalFormatSymbols.getGroupingSeparator();
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        String test = numberFormat.format(new Double("123456789123456789"));
        int last = test.lastIndexOf(this.groupingSeparator);
        if (last > 0) {
            test = test.substring(0, last - 1);
            int oneBeforeLast = test.lastIndexOf(this.groupingSeparator);
            this.groupingSize = last - oneBeforeLast;
            if (this.groupingSize <= 0) {
                this.groupingSize = 3;
            }
        }
    }

    public static FormatUtil getInstance() {
        return new FormatUtil(Locale.getDefault());
    }

    public static FormatUtil getInstance(Locale locale) {
        return new FormatUtil(locale);
    }

    public static FormatUtil getInstance(ConnectionInfo connectionInfo) {
        return new FormatUtil(I18nUtil.getConnectionLocale(connectionInfo));
    }

    public String format(BigDecimal bigDecimal) {
        return this.format(bigDecimal, false);
    }

    public String format(BigDecimal bigDecimal, boolean group) {
        return this.format(bigDecimal, group, false);
    }

    public String format(BigDecimal bigDecimal, boolean group, boolean stripTrailingZeros) {
        String original;
        int groupCount = 0;
        StringBuffer value = new StringBuffer();
        String string = original = stripTrailingZeros ? this.stripTrailingZero(bigDecimal).toPlainString() : bigDecimal.toPlainString();
        if (original.length() > 126) {
            throw new NumberFormatException("NUMBER TOO BIG:" + bigDecimal.toEngineeringString());
        }
        boolean decimal = original.indexOf(46) >= 0;
        for (int i = original.length() - 1; i >= 0; --i) {
            if (group && !decimal && original.charAt(i) >= '0' && original.charAt(i) <= '9') {
                ++groupCount;
            }
            if (groupCount >= this.groupingSize) {
                groupCount = 1;
                value.insert(0, this.groupingSeparator);
            }
            if (original.charAt(i) == '.') {
                decimal = false;
                value.insert(0, this.decimalSeparator);
                continue;
            }
            value.insert(0, original.charAt(i));
        }
        return value.toString();
    }

    private BigDecimal stripTrailingZero(BigDecimal number) {
        number = number.compareTo(zero) == 0 ? zero : number.stripTrailingZeros();
        return number;
    }

    public BigDecimal parseBigDecimal(String value) {
        StringBuffer newValue = new StringBuffer();
        int size = value.length();
        for (int i = 0; i < size; ++i) {
            if (value.charAt(i) == this.decimalSeparator) {
                newValue.append('.');
                continue;
            }
            if (this.allowGroupingSeparator && value.charAt(i) == this.groupingSeparator) {
                String[] groups;
                if (!this.validateGroupingPos) continue;
                String tempvalue = value;
                if (value.indexOf(this.decimalSeparator) >= 0) {
                    if (value.substring(value.indexOf(this.decimalSeparator)).indexOf(this.groupingSeparator) >= 0) {
                        throw new NumberFormatException("INVALID_GROUP_SEPARATOR_POSITION");
                    }
                    tempvalue = value.substring(0, value.indexOf(this.decimalSeparator));
                }
                if ((groups = StringUtil.split(tempvalue, "" + this.groupingSeparator)).length > 1 && this.isScientificNotation(groups)) {
                    throw new NumberFormatException("NUMBER FORMAT EXCEPTION");
                }
                for (int g = 0; g < groups.length; ++g) {
                    if (groups[0].length() <= 3 && (g <= 0 || groups[g].length() == 3)) continue;
                    throw new NumberFormatException("INVALID_GROUP_SEPARATOR_POSITION");
                }
                continue;
            }
            newValue.append(value.charAt(i));
        }
        return new BigDecimal(newValue.toString());
    }

    public BigDecimal parseBigDecimal(String value, char decimalSeparator, char groupingSeparator, boolean allowGroupingSeparator, boolean validateGroupingPos) {
        StringBuffer newValue = new StringBuffer();
        int size = value.length();
        for (int i = 0; i < size; ++i) {
            if (value.charAt(i) == decimalSeparator) {
                newValue.append('.');
                continue;
            }
            if (allowGroupingSeparator && value.charAt(i) == groupingSeparator) {
                String[] groups;
                if (!validateGroupingPos) continue;
                String tempvalue = value;
                if (value.indexOf(decimalSeparator) >= 0) {
                    if (value.substring(value.indexOf(decimalSeparator)).indexOf(groupingSeparator) >= 0) {
                        throw new NumberFormatException("INVALID_GROUP_SEPARATOR_POSITION");
                    }
                    tempvalue = value.substring(0, value.indexOf(decimalSeparator));
                }
                if ((groups = StringUtil.split(tempvalue, "" + groupingSeparator)).length > 1 && this.isScientificNotation(groups)) {
                    throw new NumberFormatException("NUMBER FORMAT EXCEPTION");
                }
                for (int g = 0; g < groups.length; ++g) {
                    if (groups[0].length() <= 3 && (g <= 0 || groups[g].length() == 3)) continue;
                    throw new NumberFormatException("INVALID_GROUP_SEPARATOR_POSITION");
                }
                continue;
            }
            newValue.append(value.charAt(i));
        }
        return new BigDecimal(newValue.toString());
    }

    private boolean isScientificNotation(String[] groups) {
        boolean isPresent = false;
        for (int g = 0; g < groups.length; ++g) {
            if (!groups[g].toUpperCase().contains("E")) continue;
            isPresent = true;
            break;
        }
        return isPresent;
    }

    public char getDecimalSeparator() {
        return this.decimalSeparator;
    }

    public char getGroupingSeparator() {
        return this.groupingSeparator;
    }

    public int getGroupingInterval() {
        return this.groupingSize;
    }
}

