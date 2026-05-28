/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.format;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.evaluator.ExpressionUtil;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import sapphire.util.FormatUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public class NumericFormatter {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 65882 $";

    public static String formatNumber(int value, String format, Locale locale) {
        if (format == null) {
            format = "";
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        try {
            Trace.logDebug("NumericFormat.formatLong: Using supplied PB Format = \"" + format + "\" & Number = " + value);
            String javaCompatibleFormattingString = "";
            if (format.trim().length() == 0 || format.toLowerCase().indexOf("general") >= 0) {
                javaCompatibleFormattingString = "0";
            } else if (format.toLowerCase().indexOf("currency") >= 0) {
                javaCompatibleFormattingString = "$#,##0.00;($#,##0.00)";
            } else if (format.toLowerCase().indexOf("eval") >= 0) {
                javaCompatibleFormattingString = format = NumericFormatter.getEvaluatedFormat(new BigDecimal(value), format);
            } else {
                if (value == 0 && format.replaceAll("#", "").trim().length() == 0) {
                    return "";
                }
                javaCompatibleFormattingString = format;
            }
            DecimalFormat decimalFormat = new DecimalFormat(javaCompatibleFormattingString, new DecimalFormatSymbols(locale));
            if (format.indexOf(";") >= 0) {
                javaCompatibleFormattingString = value < 0 ? javaCompatibleFormattingString.substring(format.indexOf(";") + 1) : javaCompatibleFormattingString.substring(0, format.indexOf(";"));
            }
            if (javaCompatibleFormattingString.length() > 0 && javaCompatibleFormattingString.charAt(0) != '0' && javaCompatibleFormattingString.charAt(0) != '#' && javaCompatibleFormattingString.charAt(0) != '$' && javaCompatibleFormattingString.charAt(0) != '-' && javaCompatibleFormattingString.charAt(0) != '(') {
                int ind;
                int pos = 0;
                String returnString = "";
                String inputValue = "";
                if (value < 0) {
                    value *= -1;
                }
                inputValue = String.valueOf(value);
                String pattern = "";
                int lengthBeforeDecimalPoint = 0;
                for (ind = 0; ind < javaCompatibleFormattingString.length() && javaCompatibleFormattingString.charAt(ind) != '.'; ++ind) {
                    if (format.charAt(ind) != '#' && format.charAt(ind) != '0') continue;
                    pattern = pattern + format.charAt(ind);
                }
                lengthBeforeDecimalPoint = inputValue.indexOf(".") >= 0 ? inputValue.substring(0, inputValue.indexOf(".")).length() : inputValue.length();
                for (int paddingPos = lengthBeforeDecimalPoint; paddingPos < pattern.length(); ++paddingPos) {
                    inputValue = "0" + inputValue;
                }
                for (ind = 0; ind < javaCompatibleFormattingString.length(); ++ind) {
                    if (javaCompatibleFormattingString.charAt(ind) == '#' || javaCompatibleFormattingString.charAt(ind) == '0') {
                        returnString = pos < inputValue.length() ? returnString + inputValue.charAt(pos) : returnString + "0";
                        ++pos;
                        continue;
                    }
                    returnString = returnString + javaCompatibleFormattingString.charAt(ind);
                }
                Trace.logDebug("Formatted Output = " + returnString);
                return returnString;
            }
            String formattedOutput = decimalFormat.format(value);
            Trace.logDebug("Formatted Output = " + formattedOutput);
            return formattedOutput;
        }
        catch (Exception e) {
            Trace.logError("NumericFormatter: An exception was generated.", e);
            return "";
        }
    }

    public static String formatNumber(int value, String format) {
        return NumericFormatter.formatNumber(value, format, null);
    }

    public static String[] formatNumber(int[] values, String format) {
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            int value = values[i];
            result[i] = NumericFormatter.formatNumber(value, format);
        }
        return result;
    }

    public static String[] formatNumber(int[] values, String format, Locale locale) {
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            int value = values[i];
            result[i] = NumericFormatter.formatNumber(value, format, locale);
        }
        return result;
    }

    public static String formatNumber(BigDecimal value, String format, Locale locale, String enteredText) {
        if (value == null) {
            return null;
        }
        if (format == null) {
            format = "";
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        try {
            Trace.logDebug("NumericFormat.formatBigDecimal: Using supplied PB Format = \"" + format + "\" & Number = " + value.toPlainString());
            boolean displayLeadingZero = true;
            String returnString = "";
            String actualFormat = "";
            int scale = 0;
            String decimalSeparator = "" + FormatUtil.getInstance(locale).getDecimalSeparator();
            if (format.toLowerCase().indexOf("[eval") >= 0) {
                format = NumericFormatter.getEvaluatedFormat(value, format);
            }
            if (format.indexOf(";") >= 0 && format.indexOf("[") >= 0 && (format.indexOf("sigfig") > 0 || format.indexOf("astmround") > 0)) {
                return NumericFormatter.formatSigfig(value, format, locale);
            }
            if ("asentered".equalsIgnoreCase(format)) {
                if (enteredText != null && enteredText.length() > 0) {
                    return enteredText;
                }
                return ".".equals(decimalSeparator) ? value.toPlainString() : StringUtil.replaceAll(value.toPlainString(), ".", decimalSeparator);
            }
            if (value.intValue() == 0 && format.length() > 0 && format.indexOf(".") >= 0 && format.substring(0, format.indexOf(".")).replaceAll("#", "").trim().length() == 0) {
                displayLeadingZero = false;
            }
            boolean removedPlus = false;
            if (format.length() > 0 && format.charAt(0) == '0' && format.indexOf("E+") >= 0) {
                format = format.substring(0, format.indexOf("+")) + format.substring(format.indexOf("+") + 1);
                removedPlus = true;
                displayLeadingZero = false;
            }
            if (format.toLowerCase().indexOf("general") >= 0) {
                format = "";
            } else if (format.toLowerCase().indexOf("currency") >= 0) {
                format = "$#,##0.00;($#,##0.00)";
            }
            actualFormat = format.indexOf(";") >= 0 ? (value.doubleValue() < 0.0 ? format.substring(format.indexOf(";") + 1) : format.substring(0, format.indexOf(";"))) : format;
            if (actualFormat.indexOf(".") >= 0) {
                for (int ind = actualFormat.indexOf(".") + 1; ind < actualFormat.length() && actualFormat.charAt(ind) != ';'; ++ind) {
                    if (actualFormat.charAt(ind) != '#' && actualFormat.charAt(ind) != '0') continue;
                    ++scale;
                }
                if (actualFormat.indexOf("E") < 0 && actualFormat.indexOf("%") < 0) {
                    value = value.divide(new BigDecimal(1.0), scale, 4);
                }
            } else if ("0".equals(actualFormat)) {
                value = value.divide(new BigDecimal(1.0), scale, 4);
            }
            int lengthBeforeDecimalPoint = 0;
            int intPartOfIinputValue = 0;
            String inputValue = "";
            inputValue = value.stripTrailingZeros().toPlainString();
            for (int index = 0; index < actualFormat.length() && actualFormat.charAt(index) != '.'; ++index) {
                if (actualFormat.charAt(index) != '0') continue;
                ++lengthBeforeDecimalPoint;
            }
            intPartOfIinputValue = inputValue.indexOf(".") >= 0 ? inputValue.substring(0, inputValue.indexOf(".")).length() : inputValue.length();
            if (lengthBeforeDecimalPoint < intPartOfIinputValue && actualFormat.indexOf("E") < 0) {
                lengthBeforeDecimalPoint = intPartOfIinputValue;
                if (value.doubleValue() < 0.0) {
                    --lengthBeforeDecimalPoint;
                }
            }
            if (actualFormat.length() > 0 && actualFormat.charAt(0) != '0' && actualFormat.charAt(0) != '#' && actualFormat.charAt(0) != '.' && actualFormat.charAt(0) != '$' && actualFormat.charAt(0) != '-' && actualFormat.charAt(0) != '(') {
                int ind;
                int pos = 0;
                if (value.doubleValue() < 0.0) {
                    value = value.multiply(new BigDecimal(-1.0));
                }
                inputValue = value.stripTrailingZeros().toPlainString();
                String pattern = "";
                for (ind = 0; ind < actualFormat.length() && actualFormat.charAt(ind) != '.'; ++ind) {
                    if (actualFormat.charAt(ind) != '#' && actualFormat.charAt(ind) != '0') continue;
                    pattern = pattern + actualFormat.charAt(ind);
                }
                inputValue = (value = value.divide(new BigDecimal(1.0), scale, 4)).stripTrailingZeros().toPlainString();
                lengthBeforeDecimalPoint = inputValue.indexOf(".") >= 0 ? inputValue.substring(0, inputValue.indexOf(".")).length() : inputValue.length();
                for (int paddingPos = lengthBeforeDecimalPoint; paddingPos < pattern.length(); ++paddingPos) {
                    inputValue = "0" + inputValue;
                }
                for (ind = 0; ind < actualFormat.length(); ++ind) {
                    if (actualFormat.charAt(0) != '\'' && (actualFormat.charAt(ind) == '#' || actualFormat.charAt(ind) == '0')) {
                        if (pos < inputValue.length() && inputValue.charAt(pos) == '.') {
                            ++pos;
                        }
                        returnString = pos < inputValue.length() ? returnString + inputValue.charAt(pos) : returnString + "0";
                        ++pos;
                        continue;
                    }
                    if (actualFormat.charAt(ind) == '\'') continue;
                    returnString = returnString + actualFormat.charAt(ind);
                }
                Trace.logDebug("Formatted Output = " + returnString);
                return returnString;
            }
            DecimalFormat decimalFormat = new DecimalFormat(actualFormat);
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            FormatUtil fu = FormatUtil.getInstance(locale);
            dfs.setDecimalSeparator(fu.getDecimalSeparator());
            dfs.setGroupingSeparator(fu.getGroupingSeparator());
            decimalFormat.setDecimalFormatSymbols(dfs);
            if (actualFormat.indexOf("E") >= 0) {
                decimalFormat.setPositivePrefix("+");
            }
            if (actualFormat.indexOf(",") < 0) {
                decimalFormat.setGroupingSize(0);
            } else {
                decimalFormat.setGroupingSize(3);
            }
            if (actualFormat.indexOf(".") >= 0) {
                decimalFormat.setDecimalSeparatorAlwaysShown(true);
            }
            if (displayLeadingZero) {
                decimalFormat.setMinimumIntegerDigits(lengthBeforeDecimalPoint);
            }
            returnString = decimalFormat.format(value);
            if (removedPlus && returnString.indexOf("E-") == -1) {
                returnString = returnString.replaceFirst("E", "E+");
            }
            if (returnString.startsWith("+") || actualFormat.startsWith("(") && returnString.startsWith("-")) {
                Trace.logDebug("Formatted Output = " + returnString.substring(1));
                return returnString.substring(1);
            }
            if (actualFormat.startsWith(".")) {
                return returnString.substring(returnString.indexOf(46));
            }
            Trace.logDebug("Formatted Output = " + returnString);
            return returnString;
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return "";
        }
    }

    public static String formatNumber(BigDecimal value, String format) {
        return NumericFormatter.formatNumber(value, format, null, "");
    }

    public static String formatNumber(BigDecimal value, String format, String enteredText) {
        return NumericFormatter.formatNumber(value, format, null, enteredText);
    }

    public static String formatNumber(BigDecimal value, String format, Locale locale) {
        return NumericFormatter.formatNumber(value, format, locale, "");
    }

    private static String formatSigfig(BigDecimal value, String format, Locale locale) {
        if (value == null) {
            return null;
        }
        if (format == null) {
            format = "";
        }
        format = format.trim();
        try {
            Trace.logDebug("NumericFormat.formatSigFig: Using supplied PB Format = \"" + format + "\" & Number = " + value.toPlainString());
            if (format.indexOf("]") >= 0) {
                BigDecimal val;
                boolean isSigfig = false;
                boolean isPrecision = false;
                boolean isNegative = false;
                boolean isRounded = false;
                int scalecount = 0;
                int ind = 0;
                int scale = 0;
                if (value.doubleValue() < 0.0) {
                    isNegative = true;
                }
                StringBuffer formattedoutput = new StringBuffer("");
                String input = value.stripTrailingZeros().toPlainString();
                if (isNegative) {
                    input = input.substring(1, input.length());
                }
                format = format.toLowerCase();
                boolean isPadded = false;
                scale = Integer.parseInt(format.substring(format.indexOf(";") + 1, format.indexOf("]")));
                if (value.doubleValue() == 0.0) {
                    isSigfig = true;
                }
                if (format.indexOf("sigfig") >= 0) {
                    while (ind < input.length()) {
                        boolean isAdded = false;
                        char digit = input.charAt(ind);
                        if (digit == '-') {
                            formattedoutput.append("-");
                        } else if (digit == '.') {
                            if (isPadded) break;
                            if (isSigfig) {
                                formattedoutput.append(digit);
                            } else {
                                formattedoutput.append("0.");
                            }
                            isPrecision = true;
                        } else if (digit == '0') {
                            if ((isSigfig || isPrecision) && scalecount < scale) {
                                formattedoutput.append(digit);
                                if (isSigfig) {
                                    ++scalecount;
                                    isAdded = true;
                                }
                            }
                        } else if (scalecount < scale) {
                            formattedoutput.append(digit);
                            ++scalecount;
                            isAdded = true;
                            isSigfig = true;
                        }
                        if (scalecount == scale) {
                            if (isPrecision) {
                                if (ind + 1 >= input.length()) break;
                                formattedoutput.append(input.charAt(ind + 1));
                                val = new BigDecimal(formattedoutput.toString());
                                int scaleToRound = val.scale() - 1;
                                val = format.indexOf("clporgsigfig") >= 0 || format.indexOf("clpinorgsigfig") >= 0 && (ind + 2 == input.length() || Double.parseDouble(input.substring(ind + 2)) == 0.0) ? val.divide(new BigDecimal(1.0), scaleToRound, 6) : val.divide(new BigDecimal(1.0), scaleToRound, 4);
                                formattedoutput = new StringBuffer(val.toPlainString());
                                if (Long.parseLong(formattedoutput.substring(formattedoutput.indexOf(".") + 1)) != 0L) break;
                                if (formattedoutput.indexOf(".") >= scale) {
                                    formattedoutput = new StringBuffer(formattedoutput.substring(0, formattedoutput.indexOf(".")));
                                    break;
                                }
                                if (formattedoutput.indexOf(".") < 0 || formattedoutput.indexOf(".") >= scale) break;
                                formattedoutput = new StringBuffer(formattedoutput.substring(0, scale + 1));
                                break;
                            }
                            if (!isAdded) {
                                if (!isRounded) {
                                    if (Integer.parseInt(String.valueOf(digit)) > 5) {
                                        val = new BigDecimal(formattedoutput.toString());
                                        val = val.add(new BigDecimal(1.0));
                                        formattedoutput = new StringBuffer(val.toPlainString());
                                    } else if (Integer.parseInt(String.valueOf(digit)) == 5) {
                                        if (format.indexOf("clporgsigfig") >= 0 || format.indexOf("clpinorgsigfig") >= 0 && (ind + 1 == input.length() || Double.parseDouble(input.substring(ind + 1)) == 0.0)) {
                                            if (Integer.parseInt(input.substring(ind - 1, ind)) % 2 == 1) {
                                                val = new BigDecimal(formattedoutput.toString());
                                                val = val.add(new BigDecimal(1.0));
                                                formattedoutput = new StringBuffer(val.toPlainString());
                                            }
                                        } else {
                                            val = new BigDecimal(formattedoutput.toString());
                                            val = val.add(new BigDecimal(1.0));
                                            formattedoutput = new StringBuffer(val.toPlainString());
                                        }
                                    }
                                }
                                formattedoutput.append('0');
                                isRounded = true;
                                isPadded = true;
                            }
                        }
                        ++ind;
                    }
                    while (scalecount < scale) {
                        if (ind == input.length() && formattedoutput.indexOf(".") < 0) {
                            formattedoutput.append('.');
                        }
                        formattedoutput.append('0');
                        ++scalecount;
                    }
                    if (format.indexOf("maxsigfigdp") >= 0 && (val = new BigDecimal(formattedoutput.toString())).scale() > scale) {
                        val = val.divide(new BigDecimal(1.0), scale, 4);
                        formattedoutput = new StringBuffer(val.toPlainString());
                    }
                } else if (format.indexOf("astmround") >= 0) {
                    val = value.divide(new BigDecimal(1.0), scale, 6);
                    formattedoutput = new StringBuffer(val.toPlainString());
                }
                String output = formattedoutput.toString();
                if (isNegative && formattedoutput.charAt(0) != '-') {
                    output = "-" + output;
                }
                if (locale != null) {
                    output = FormatUtil.getInstance(locale).format(new BigDecimal(output), false, false);
                }
                Trace.logDebug("Formatted Output = " + output);
                return output;
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
        return "";
    }

    public static String getEvaluatedFormat(BigDecimal value, String format) {
        HashMap<String, BigDecimal> values = new HashMap<String, BigDecimal>();
        String ESC_SIGFIG = "@@sigfigtypeformat@@";
        int sigfigFormatCounter = 0;
        HashMap<String, String> replacementMap = new HashMap<String, String>();
        try {
            String returnFormat;
            boolean isSigfigPresent;
            values.put("this", value);
            String expression = format.substring(format.indexOf("if"), format.length() - 1);
            boolean bl = isSigfigPresent = expression.indexOf("[sigfig;") > 0 || expression.indexOf("[clporgsigfig;") > 0 || expression.indexOf("[clpinorgsigfig;") > 0 || expression.indexOf("[maxsigfigdp;") > 0 || expression.indexOf("[astmround;") > 0;
            if (isSigfigPresent) {
                String[] tokens = StringUtil.getTokens(expression);
                for (int i = 0; i < tokens.length; ++i) {
                    String token = tokens[i];
                    if (!token.contains("sigfig") && !token.contains("astmround")) continue;
                    String replacementToken = "@@sigfigtypeformat@@" + String.valueOf(sigfigFormatCounter++);
                    expression = StringUtil.replaceAll(expression, "\"[" + token + "]\"", "\"" + replacementToken + "\"");
                    replacementMap.put(replacementToken, "[" + token + "]");
                }
            }
            return replacementMap.containsKey(returnFormat = ExpressionUtil.evaluate(expression, values)) ? (String)replacementMap.get(returnFormat) : returnFormat;
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
            return null;
        }
    }
}

