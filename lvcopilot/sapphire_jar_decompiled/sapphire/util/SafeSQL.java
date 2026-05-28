/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.owasp.esapi.ESAPI
 *  org.owasp.esapi.Encoder
 *  org.owasp.esapi.codecs.Codec
 *  org.owasp.esapi.codecs.OracleCodec
 */
package sapphire.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.codecs.Codec;
import org.owasp.esapi.codecs.OracleCodec;
import sapphire.util.StringUtil;

public class SafeSQL {
    private static Encoder encoder = ESAPI.encoder();
    private static OracleCodec oracleCodec = new OracleCodec();
    private ArrayList bindvarList = new ArrayList();
    private String preparedSQL = "";
    private static String regexToTrim = "^[N]['][^,]+[']";

    public String addVar(Object value) {
        this.bindvarList.add(value);
        return "?";
    }

    public String addIn(String valuelist) {
        return this.addIn(valuelist, "','");
    }

    public String addIn(String valuelist, String delimiter) {
        if (valuelist == null || valuelist.trim().length() == 0) {
            return "''";
        }
        if ((valuelist = valuelist.trim()).indexOf("N'") == 0) {
            Pattern p = Pattern.compile(regexToTrim);
            Matcher m = p.matcher(valuelist);
            if (m.find()) {
                valuelist = valuelist.substring(2);
            }
        } else if (valuelist.indexOf("'") == 0) {
            valuelist = valuelist.substring(1);
        }
        if (valuelist.lastIndexOf("'") == valuelist.length() - 1) {
            valuelist = valuelist.substring(0, valuelist.length() - 1);
        }
        if ("','".equals(delimiter) && valuelist.indexOf("','") < 0) {
            if (valuelist.indexOf("',N'") >= 0) {
                delimiter = "',N'";
            } else if (valuelist.indexOf("', '") >= 0) {
                delimiter = "', '";
            }
        }
        String[] values = StringUtil.split(valuelist, delimiter);
        StringBuilder placeHolder = new StringBuilder();
        for (int i = 0; i < values.length; ++i) {
            this.bindvarList.add(values[i]);
            placeHolder.append(",?");
        }
        return placeHolder.substring(1);
    }

    public String addIn(Collection<String> collection) {
        if (collection != null && collection.size() > 0) {
            StringBuilder placeHolder = new StringBuilder();
            for (String var : collection) {
                this.bindvarList.add(var);
                placeHolder.append(",?");
            }
            return placeHolder.substring(1);
        }
        return "";
    }

    public Object[] getValues() {
        Object[] objects = new Object[this.bindvarList.size()];
        for (int i = 0; i < objects.length; ++i) {
            objects[i] = this.bindvarList.get(i);
        }
        return objects;
    }

    public void reset() {
        this.bindvarList.clear();
    }

    public String getPreparedSQL() {
        return this.preparedSQL;
    }

    public void setPreparedSQL(String preparedSQL) {
        this.preparedSQL = preparedSQL;
    }

    public static String replaceAllWithVars(String query, String tokenToReplace, String replaceWithvalue, SafeSQL safeSQL) {
        if (query == null || query.length() == 0 || tokenToReplace == null || tokenToReplace.length() == 0 || replaceWithvalue == null) {
            return query;
        }
        int oldStringLength = tokenToReplace.length();
        String originalInputString = query;
        StringBuilder outputString = new StringBuilder();
        int lastpos = 0;
        int pos = query.indexOf(tokenToReplace);
        while (pos >= 0) {
            outputString.append(originalInputString.substring(lastpos, pos));
            outputString.append(safeSQL.addVar(replaceWithvalue));
            lastpos = pos + oldStringLength;
            pos = query.indexOf(tokenToReplace, lastpos);
        }
        outputString.append(originalInputString.substring(lastpos));
        return outputString.toString();
    }

    public static String replaceAllWithInVars(String query, String tokenToReplace, String replaceWithvaluelist, String listdelimitor, SafeSQL safeSQL) {
        if (query == null || query.length() == 0 || tokenToReplace == null || tokenToReplace.length() == 0 || replaceWithvaluelist == null) {
            return query;
        }
        int oldStringLength = tokenToReplace.length();
        String originalInputString = query;
        StringBuilder outputString = new StringBuilder();
        int lastpos = 0;
        int pos = query.indexOf(tokenToReplace);
        while (pos >= 0) {
            outputString.append(originalInputString.substring(lastpos, pos));
            outputString.append(safeSQL.addIn(replaceWithvaluelist, listdelimitor));
            lastpos = pos + oldStringLength;
            pos = query.indexOf(tokenToReplace, lastpos);
        }
        outputString.append(originalInputString.substring(lastpos));
        return outputString.toString();
    }

    public static Object[] joinArrays(Object[] array1, Object[] array2) {
        if (array1 != null && array1.length > 0 && array2 != null && array2.length > 0) {
            Object[] array = new Object[array1.length + array2.length];
            for (int i = 0; i < array.length; ++i) {
                array[i] = i < array1.length ? array1[i] : array2[i - array1.length];
            }
            return array;
        }
        if (array2 == null || array2.length == 0) {
            return array1;
        }
        return array2;
    }

    public static String encodeForSQL(String input, boolean isOracle) {
        if (isOracle) {
            return encoder.encodeForSQL((Codec)oracleCodec, input);
        }
        return SafeSQL.encodeForSQLServer(input);
    }

    public static String convertToSQLInClause(String input, String delimiter, boolean isOracle) {
        if (input == null) {
            return input;
        }
        String[] items = StringUtil.split(input, delimiter);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.length; ++i) {
            sb.append((i > 0 ? "','" : "") + SafeSQL.encodeForSQL(items[i], isOracle));
        }
        return sb.toString();
    }

    private static String encodeForSQLServer(String input) {
        return StringUtil.replaceAll(input, "'", "''");
    }
}

