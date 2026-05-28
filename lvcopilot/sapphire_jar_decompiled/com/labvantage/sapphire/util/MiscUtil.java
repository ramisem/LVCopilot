/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util;

import java.math.BigDecimal;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;

public class MiscUtil {
    static final String LABVANTAGE_CVS_ID = "$Revision: 62654 $";

    public static class MiscString {
        public static String appendDelimeteredString(String sTheString, String sTheNewString, String sTheDelimeter) {
            String sReturn = sTheString == null || sTheString.length() == 0 ? sTheNewString : sTheString + sTheDelimeter + sTheNewString;
            return sReturn;
        }

        public static void appendDelimeteredString(StringBuffer sbTheStringBuffer, String sTheNewString, String sTheDelimeter) {
            if (sbTheStringBuffer != null) {
                if (sbTheStringBuffer.length() == 0) {
                    sbTheStringBuffer.append(sTheNewString);
                } else {
                    sbTheStringBuffer.append(sTheDelimeter).append(sTheNewString);
                }
            }
        }

        public static void appendDelimeteredString(StringBuffer sbTheStringBuffer, String sTheNewString, String sTheDelimeter, int rowIndex) {
            if (sbTheStringBuffer != null) {
                if (sbTheStringBuffer.length() == 0) {
                    if (rowIndex > 0) {
                        sbTheStringBuffer.append(sTheDelimeter);
                    }
                    sbTheStringBuffer.append(sTheNewString);
                } else {
                    sbTheStringBuffer.append(sTheDelimeter).append(sTheNewString);
                }
            }
        }

        public static void parseComplexNumber(String value, StringBuffer valuenumout, StringBuffer valueout, M18NUtil userM18n, boolean returnNumberInUserLocal) throws NumberFormatException {
            if (value.length() > 0 && !value.toString().equalsIgnoreCase("(null)")) {
                if (value.indexOf("/") > -1) {
                    BigDecimal bd = MiscString.parseFraction(value.toString());
                    valuenumout.append(userM18n.format(bd));
                    valueout.append(value);
                } else {
                    BigDecimal bd = userM18n.parseBigDecimal(value.toString());
                    if (returnNumberInUserLocal) {
                        valuenumout.append(userM18n.format(bd, false, true));
                    } else {
                        valuenumout.append(bd.toString());
                    }
                    M18NUtil m18 = new M18NUtil();
                    valueout.append(m18.format(bd, false, false));
                }
            }
        }

        public static void parseComplexNumber(String value, StringBuffer valuenumout, StringBuffer valueout, M18NUtil userM18n) throws NumberFormatException {
            MiscString.parseComplexNumber(value, valuenumout, valueout, userM18n, false);
        }

        public static BigDecimal parseFraction(String fraction) throws NumberFormatException {
            String[] temp = StringUtil.split(fraction, "/");
            if (temp.length == 2) {
                BigDecimal y;
                BigDecimal x;
                BigDecimal z;
                String t = temp[1].trim();
                if (t.indexOf(" ") == -1) {
                    try {
                        z = new BigDecimal(t);
                    }
                    catch (NumberFormatException e) {
                        throw new NumberFormatException("Invalid fraction. Where x y / z, z is not a number.");
                    }
                } else {
                    throw new NumberFormatException("Invalid fraction. Where x y / z, z is not a valid number.");
                }
                temp = StringUtil.split(temp[0].trim(), " ");
                if (temp.length > 1) {
                    try {
                        x = new BigDecimal(temp[0].trim());
                    }
                    catch (NumberFormatException e) {
                        throw new NumberFormatException("Invalid fraction. Where x y / z, x is not a number.");
                    }
                } else {
                    x = new BigDecimal(0);
                }
                try {
                    y = new BigDecimal(temp[temp.length - 1].trim());
                }
                catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid fraction. Where x y / z, y is not a number.");
                }
                if (z.floatValue() > 0.0f) {
                    if (y.floatValue() != 0.0f) {
                        if (x.floatValue() >= 0.0f) {
                            return y.divide(z, 10, 4).add(x);
                        }
                        return x.subtract(y.divide(z, 10, 4));
                    }
                    throw new NumberFormatException("Invalid fraction. Where x y / z, y cannot be 0.");
                }
                throw new NumberFormatException("Invalid fraction. Where x y / z, z cannot be less than 1.");
            }
            throw new NumberFormatException("Invalid fraction. Requires /.");
        }
    }

    public static class MiscArray {
        public static boolean isStringInArray(String[] saTheArray, String sTheString, boolean lIgnoreCase) {
            return MiscArray.findString(saTheArray, sTheString, 0, lIgnoreCase) > -1;
        }

        public static int findString(String[] saTheArray, String sTheString, int iStartIndex, boolean lIgnoreCase) {
            int iReturn = -1;
            if (iStartIndex < saTheArray.length) {
                for (int iIndex = iStartIndex; iIndex < saTheArray.length; ++iIndex) {
                    if (saTheArray[iIndex] == null) continue;
                    if (!lIgnoreCase && saTheArray[iIndex].equals(sTheString)) {
                        iReturn = iIndex;
                        break;
                    }
                    if (!lIgnoreCase || !saTheArray[iIndex].equalsIgnoreCase(sTheString)) continue;
                    iReturn = iIndex;
                    break;
                }
            }
            return iReturn;
        }

        public static int stringInArray(String[] saTheArray, String sTheString, boolean lIgnoreCase, boolean lDirectMatch) {
            int iReturn = 0;
            for (int iIndex = 0; iIndex < saTheArray.length; ++iIndex) {
                if (saTheArray[iIndex] == null) continue;
                if (lDirectMatch) {
                    if (!lIgnoreCase && saTheArray[iIndex].equals(sTheString)) {
                        ++iReturn;
                        continue;
                    }
                    if (!lIgnoreCase || !saTheArray[iIndex].equalsIgnoreCase(sTheString)) continue;
                    ++iReturn;
                    continue;
                }
                if (lIgnoreCase) {
                    if (saTheArray[iIndex].toLowerCase().indexOf(sTheString.toLowerCase()) <= -1) continue;
                    ++iReturn;
                    continue;
                }
                if (saTheArray[iIndex].indexOf(sTheString) <= -1) continue;
                ++iReturn;
            }
            return iReturn;
        }
    }
}

