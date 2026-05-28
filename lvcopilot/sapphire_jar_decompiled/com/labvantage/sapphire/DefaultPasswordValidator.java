/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.FileUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Random;
import sapphire.SapphireException;
import sapphire.action.BasePasswordValidator;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DefaultPasswordValidator
extends BasePasswordValidator {
    @Override
    public void checkPasswordFormat(String userid, String password, PropertyList properties) throws SapphireException {
        File f;
        String stopFolder;
        int i;
        int ii;
        int i2;
        int ii2;
        int exactlength;
        if (password == null) {
            password = "";
        }
        int maxlength = this.getMaxLength(properties);
        if (password.length() > maxlength) {
            throw new SapphireException("Password must be no more than " + maxlength + " characters long");
        }
        if (properties.getProperty("exactlength").length() > 0 && (exactlength = this.getExactLength(properties)) > 0 && password.length() != exactlength) {
            throw new SapphireException("Password must be exactly " + exactlength + " characters long");
        }
        if (properties.getProperty("minlength").length() > 0) {
            int minlength = this.getMinLength(properties);
            if (password.length() < minlength) {
                throw new SapphireException("Password must be at least " + minlength + " characters long");
            }
        }
        if (properties.getProperty("lettersonly").equals("Y")) {
            ii2 = password.length();
            for (i2 = 0; i2 < ii2; ++i2) {
                if (Character.isLetter(password.charAt(i2))) continue;
                throw new SapphireException("Password must be letters only");
            }
        }
        if (properties.getProperty("numbersonly").equals("Y")) {
            ii2 = password.length();
            for (i2 = 0; i2 < ii2; ++i2) {
                if (Character.isDigit(password.charAt(i2))) continue;
                throw new SapphireException("Password must be numbers only");
            }
        }
        if (properties.getProperty("alphanumericsonly").equals("Y")) {
            ii2 = password.length();
            for (i2 = 0; i2 < ii2; ++i2) {
                if (Character.isLetterOrDigit(password.charAt(i2))) continue;
                throw new SapphireException("Password must contain letters and numbers only");
            }
        }
        if (properties.getProperty("hasletter").equals("Y")) {
            boolean hasletter = false;
            ii = password.length();
            for (i = 0; i < ii; ++i) {
                if (!Character.isLetter(password.charAt(i))) continue;
                hasletter = true;
                break;
            }
            if (!hasletter) {
                throw new SapphireException("Password must contain at least one letter");
            }
        }
        if (properties.getProperty("hasuppercaseletter").equals("Y")) {
            boolean hasUppercase = false;
            ii = password.length();
            for (i = 0; i < ii; ++i) {
                if (!Character.isUpperCase(password.charAt(i))) continue;
                hasUppercase = true;
                break;
            }
            if (!hasUppercase) {
                throw new SapphireException("Password must contain at least one uppercase letter");
            }
        }
        if (properties.getProperty("haslowercaseletter").equals("Y")) {
            boolean hasLowercase = false;
            ii = password.length();
            for (i = 0; i < ii; ++i) {
                if (!Character.isLowerCase(password.charAt(i))) continue;
                hasLowercase = true;
                break;
            }
            if (!hasLowercase) {
                throw new SapphireException("Password must contain at least one lowercase letter");
            }
        }
        if (properties.getProperty("hasnumber").equals("Y")) {
            boolean hasnumber = false;
            ii = password.length();
            for (i = 0; i < ii; ++i) {
                if (!Character.isDigit(password.charAt(i))) continue;
                hasnumber = true;
                break;
            }
            if (!hasnumber) {
                throw new SapphireException("Password must contain at least one number");
            }
        }
        if (properties.getProperty("hassymbol").equals("Y")) {
            boolean hassymbol = false;
            ii = password.length();
            for (i = 0; i < ii; ++i) {
                if (Character.isLetterOrDigit(password.charAt(i))) continue;
                hassymbol = true;
                break;
            }
            if (!hassymbol) {
                throw new SapphireException("Password must contain at least one non-alphanumeric symbol");
            }
        }
        if (properties.getProperty("stopuserid").equals("Y") && password.equalsIgnoreCase(userid)) {
            throw new SapphireException("Password must not match the user id");
        }
        PropertyListCollection stoplist = properties.getCollection("stoplist");
        if (stoplist != null && stoplist.size() > 0) {
            Iterator it = stoplist.iterator();
            while (it.hasNext()) {
                String stopitem = ((PropertyList)it.next()).getProperty("stopitem");
                if (!password.toLowerCase().contains(stopitem.toLowerCase())) continue;
                throw new SapphireException("Your password is on the list of blocked passwords");
            }
        }
        if ((stopFolder = properties.getProperty("stoplistfolder")).length() > 0 && (f = new File(stopFolder = FileUtil.substituteConfigurationPaths(stopFolder))).isDirectory()) {
            File[] files;
            for (File file : files = f.listFiles((dir, name) -> name.endsWith(".txt"))) {
                try (FileInputStream fis = new FileInputStream(file);
                     BufferedReader inFile = new BufferedReader(new InputStreamReader((InputStream)fis, "UTF-8"));){
                    String str = inFile.readLine();
                    while (str != null) {
                        if (password.equalsIgnoreCase(str)) {
                            throw new SapphireException("Password is on the list of blocked passwords");
                        }
                        str = inFile.readLine();
                    }
                }
                catch (SapphireException e) {
                    throw e;
                }
                catch (Exception e) {
                    this.logger.error("Failed to read password stop list " + file.getAbsolutePath());
                }
            }
        }
        boolean isCaseSensitive = properties.getProperty("iscasesensitive").equals("Y");
        int checkhistorychanges = 0;
        try {
            checkhistorychanges = Integer.parseInt(properties.getProperty("checkhistorychanges"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (checkhistorychanges > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select password, auditsequence from a_sysuser where sysuserid = " + safeSQL.addVar(userid) + " order by auditsequence desc";
            boolean match = false;
            try {
                String lastpassword = "";
                int changecount = 0;
                this.database.createPreparedResultSet(sql, safeSQL.getValues());
                while (this.database.getNext() && changecount < checkhistorychanges && !match) {
                    String oldpassword = this.database.getString("password");
                    if (oldpassword == null || oldpassword.equals(lastpassword)) continue;
                    if (EncryptDecrypt.passwordMatches(password, oldpassword, isCaseSensitive)) {
                        match = true;
                    }
                    ++changecount;
                    lastpassword = oldpassword;
                }
                this.database.closeResultSet();
            }
            catch (SapphireException e) {
                this.logError("Failed to load audit history using " + sql + ". Exception: " + e.getMessage());
                throw new SapphireException("Unable to validate password against audit history. You must turn auditing on for the User SDC to use this option.");
            }
            if (match) {
                throw new SapphireException("Password has already been used in the last " + checkhistorychanges + " changes");
            }
        }
        int checkhistorydays = 0;
        try {
            checkhistorydays = Integer.parseInt(properties.getProperty("checkhistorydays"));
        }
        catch (Exception sql) {
            // empty catch block
        }
        if (checkhistorydays > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT password FROM a_sysuser where sysuserid = " + safeSQL.addVar(userid) + " and moddt > {ts '" + new DateTimeUtil().getTimestamp("now-" + checkhistorydays + "d") + "'}";
            try {
                boolean match = false;
                this.database.createPreparedResultSet(sql, safeSQL.getValues());
                while (this.database.getNext() && !match) {
                    String oldpassword = this.database.getString("password");
                    if (!EncryptDecrypt.passwordMatches(password, oldpassword, isCaseSensitive)) continue;
                    match = true;
                }
                this.database.closeResultSet();
                if (match) {
                    throw new SapphireException("Password has already been used in the last " + checkhistorydays + " days");
                }
            }
            catch (SapphireException e) {
                this.logError("Failed to load audit history using " + sql + ". Exception: " + e.getMessage());
                throw new SapphireException("Unable to validate password against audit history. You must turn auditing on for the User SDC to use this option.");
            }
        }
    }

    @Override
    public String generatePassword(PropertyList properties) {
        Random random = new Random();
        int exactLen = this.getExactLength(properties);
        boolean lettersOnly = properties.getProperty("lettersonly").equals("Y");
        boolean numbersOnly = properties.getProperty("numbersonly").equals("Y");
        boolean alphanumericsOnly = properties.getProperty("alphanumericsonly").equals("Y");
        boolean hasLetter = properties.getProperty("hasletter").equals("Y");
        boolean hasUppercaseLetter = properties.getProperty("hasuppercaseletter").equals("Y");
        boolean hasLowercaseLetter = properties.getProperty("haslowercaseletter").equals("Y");
        boolean hasNumber = properties.getProperty("hasnumber").equals("Y");
        boolean hasSymbol = properties.getProperty("hassymbol").equals("Y");
        char[] passwordChars = exactLen > 0 ? new char[exactLen] : new char[Math.min(Math.max(20, this.getMinLength(properties)), this.getMaxLength(properties))];
        for (int i = 0; i < passwordChars.length; ++i) {
            if (lettersOnly) {
                passwordChars[i] = random.nextInt(2) == 0 ? (char)(random.nextInt(26) + 65) : (char)(random.nextInt(26) + 97);
            } else if (numbersOnly) {
                passwordChars[i] = (char)(random.nextInt(10) + 48);
            } else if (alphanumericsOnly) {
                passwordChars[i] = random.nextInt(2) == 0 ? (random.nextInt(2) == 0 ? (char)(random.nextInt(26) + 65) : (char)(random.nextInt(26) + 97)) : (char)(random.nextInt(10) + 48);
            } else {
                char c = random.nextInt(2) == 0 ? (random.nextInt(2) == 0 ? (char)(random.nextInt(26) + 65) : (char)(random.nextInt(26) + 97)) : (passwordChars[i] = random.nextInt(2) == 0 ? (char)(random.nextInt(2) + 63) : (char)(random.nextInt(10) + 48));
            }
            if (hasUppercaseLetter && i == 0) {
                passwordChars[0] = (char)(random.nextInt(26) + 65);
            }
            if (!hasLowercaseLetter || i != passwordChars.length - 1) continue;
            passwordChars[passwordChars.length - 1] = (char)(random.nextInt(26) + 97);
        }
        String newpassword = new String(passwordChars);
        boolean letterFound = !hasLetter;
        boolean numberFound = !hasNumber;
        boolean symbolFound = !hasSymbol;
        for (int i = 0; i < newpassword.length(); ++i) {
            if (Character.isLetter(newpassword.charAt(i))) {
                letterFound = true;
            }
            if (Character.isDigit(newpassword.charAt(i))) {
                numberFound = true;
            }
            if (newpassword.charAt(i) < '?' || newpassword.charAt(i) > '@') continue;
            symbolFound = true;
        }
        if (hasLetter && !letterFound || hasNumber && !numberFound || hasSymbol && !symbolFound) {
            newpassword = this.generatePassword(properties);
        }
        return newpassword;
    }

    private int getMinLength(PropertyList properties) {
        int minlength = 1;
        try {
            minlength = Integer.parseInt(properties.getProperty("minlength"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return minlength;
    }

    private int getExactLength(PropertyList properties) {
        int exactlength = 0;
        try {
            exactlength = Integer.parseInt(properties.getProperty("exactlength"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return exactlength;
    }

    private int getMaxLength(PropertyList properties) {
        int maxlength = 100;
        try {
            maxlength = Integer.parseInt(properties.getProperty("maxlength"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return maxlength;
    }
}

