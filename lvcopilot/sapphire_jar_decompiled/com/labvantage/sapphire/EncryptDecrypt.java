/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  de.mkammerer.argon2.Argon2Factory
 *  de.mkammerer.argon2.Argon2Factory$Argon2Types
 *  javax.servlet.ServletRequest
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.BaseCrypt;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.ConfigService;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.ConsoleController;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import de.mkammerer.argon2.Argon2Factory;
import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletRequest;
import org.apache.commons.codec.binary.Base64;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class EncryptDecrypt
extends BaseCrypt {
    public static final String LOGNAME = "EncryptDecrypt";
    public static final String DES_ENCRYPT_PREFIX = "{|}";
    public static final String JS_ENCRYPT_PREFIX = "{|}";
    public static final String JS_ENCRYPT_CHUNK_SEPARATOR = "[!@]";
    public static final String JS_ENCRYPT_EXPONENT = "10001";
    public static final String OBFUSCATE_PREFIX = "{@}";
    public static final String OBFUSCATE_PARAM_START = "[*@";
    public static final String OBFUSCATE_PARAM_END = "@*]";
    public static final String OBFUSCATE_CRLF = "{@lf@}";
    static final Argon2Factory.Argon2Types ARGON_TYPE = Argon2Factory.Argon2Types.ARGON2id;
    static final int DEFAULT_ARGON_SALT_LENGTH = 16;
    static final int DEFAULT_ARGON_HASH_LENGTH = 32;
    static final int DEFAULT_ARGON_ITERATIONS = 3;
    static final int DEFAULT_ARGON_MEMORY = 90000;
    static final int DEFAULT_ARGON_PARALELLISM = 8;
    static int ARGON_SALT_LENGTH = 16;
    static int ARGON_HASH_LENGTH = 32;
    static int ARGON_ITERATIONS = 3;
    static int ARGON_MEMORY = 90000;
    static int ARGON_PARALELLISM = 8;
    private static RSAPrivateKey privateKey;
    private static String publicKeyMod;
    private static RSAPrivateKey consolePrivateKey;
    private static String consolePublicKeyMod;
    public static final String OBFUSCATE_MODE_OFF = "N";
    public static final String OBFUSCATE_MODE_OBF = "O";
    public static String obfMode;
    private static HashMap<String, String> lvInternalKeyMap;

    private static String getConfigProperty(File consoleConfigFile, String propertyid) throws ServiceException {
        if (consoleConfigFile != null) {
            return ConsoleController.getConfigProperty(consoleConfigFile, propertyid);
        }
        return "";
    }

    private static void setConfigProperty(File consoleConfigFile, String propertyid, String propertyvalue) throws ServiceException {
        if (consoleConfigFile != null) {
            ConsoleController.setConfigProperty(consoleConfigFile, propertyid, propertyvalue);
        }
    }

    public static String getPublicKey() {
        return publicKeyMod;
    }

    public static String getPublicKey(File consoleConfigFile) {
        if (consoleConfigFile == null) {
            return EncryptDecrypt.getPublicKey();
        }
        if (consolePublicKeyMod == null) {
            EncryptDecrypt.init(consoleConfigFile);
        }
        return consolePublicKeyMod;
    }

    private static synchronized void init(File consoleConfigFile) {
        try {
            if (!EncryptDecrypt.validateKeys(consoleConfigFile)) {
                EncryptDecrypt.generateNewKeys(consoleConfigFile);
                EncryptDecrypt.setKeysFromConfigProps(consoleConfigFile);
            }
        }
        catch (Throwable t) {
            Trace.logError("Fail to generate RSA encryption key!", t);
        }
    }

    public static void setRSAKeys(String[] rsaKeys) throws Exception {
        publicKeyMod = rsaKeys[0];
        BigInteger privateModulus = new BigInteger(EncryptDecrypt.decrypt(rsaKeys[1], "admindb"), 16);
        BigInteger privateExponent = new BigInteger(EncryptDecrypt.decrypt(rsaKeys[2], "admindb"), 16);
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(privateModulus, privateExponent);
        privateKey = (RSAPrivateKey)KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void setLVInternalKey(String databaseid, String key) {
        HashMap<String, String> hashMap = lvInternalKeyMap;
        synchronized (hashMap) {
            lvInternalKeyMap.put(databaseid, key);
        }
    }

    private static synchronized void setKeysFromConfigProps(File consoleConfigFile) throws Exception {
        if (consoleConfigFile != null) {
            BigInteger privateModulus = new BigInteger(EncryptDecrypt.decrypt(EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivatemodulus")), 16);
            BigInteger privateExponent = new BigInteger(EncryptDecrypt.decrypt(EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivateexponent")), 16);
            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(privateModulus, privateExponent);
            consolePublicKeyMod = EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsapublicmodulus");
            consolePrivateKey = (RSAPrivateKey)KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        }
    }

    private static synchronized boolean validateKeys(File consoleConfigFile) throws Exception {
        boolean isValid = true;
        if (EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivatemodulus") != null && EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivatemodulus").length() > 0 && EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivateexponent") != null && EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivateexponent").length() > 0 && EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsapublicmodulus") != null && EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsapublicmodulus").length() > 0) {
            try {
                EncryptDecrypt.setKeysFromConfigProps(consoleConfigFile);
                if (!"test".equals(EncryptDecrypt.decryptRSA(EncryptDecrypt.encryptRSA("test", publicKeyMod), privateKey))) {
                    isValid = false;
                }
            }
            catch (Throwable t) {
                Trace.logWarn("Failed to Validate RSA keys! Try to generate new keys...", t);
                isValid = false;
            }
        } else {
            isValid = false;
        }
        return isValid;
    }

    public static synchronized String[] generateNewKeys(File consoleConfigFile) throws Exception {
        Trace.logInfo("Start generating RSA keys.");
        String pubKeyModulus = "";
        String priKeyModulus = "";
        String priKeyExponent = "";
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair keyPair = keyGen.genKeyPair();
        pubKeyModulus = ((RSAPublicKey)keyPair.getPublic()).getModulus().toString(16);
        if (consoleConfigFile == null) {
            privateKey = (RSAPrivateKey)keyPair.getPrivate();
            priKeyModulus = privateKey.getModulus().toString(16);
            priKeyExponent = privateKey.getPrivateExponent().toString(16);
        } else {
            consolePublicKeyMod = EncryptDecrypt.getConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsapublicmodulus");
            consolePrivateKey = (RSAPrivateKey)keyPair.getPrivate();
            priKeyModulus = consolePrivateKey.getModulus().toString(16);
            priKeyExponent = consolePrivateKey.getPrivateExponent().toString(16);
            EncryptDecrypt.setConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsapublicmodulus", pubKeyModulus);
            EncryptDecrypt.setConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivatemodulus", EncryptDecrypt.encrypt(priKeyModulus));
            EncryptDecrypt.setConfigProperty(consoleConfigFile, "com.labvantage.sapphire.server.rsaprivateexponent", EncryptDecrypt.encrypt(priKeyExponent));
        }
        Trace.logInfo("Done generating RSA keys.");
        return new String[]{pubKeyModulus, EncryptDecrypt.encrypt(priKeyModulus, "admindb"), EncryptDecrypt.encrypt(priKeyExponent, "admindb")};
    }

    public static String encryptRSA(String text) throws Exception {
        return EncryptDecrypt.encryptRSA(text, EncryptDecrypt.getPublicKey());
    }

    private static String encryptRSA(String password, String publicKeyMod) throws Exception {
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(publicKeyMod, 16), new BigInteger(JS_ENCRYPT_EXPONENT, 16));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey myKey = keyFactory.generatePublic(pubKeySpec);
        Cipher clientCipher = Cipher.getInstance("RSA");
        clientCipher.init(1, myKey);
        byte[] cipherText = clientCipher.doFinal(password.getBytes());
        BigInteger bi = new BigInteger(cipherText);
        String encryptedPassword = bi.toString(16);
        return encryptedPassword;
    }

    public static String decryptRSA(String encryptedPassword) {
        if (privateKey == null) {
            EncryptDecrypt.init(null);
        }
        return EncryptDecrypt.decryptRSA(encryptedPassword, privateKey);
    }

    public static String decryptConsoleRSA(File consoleConfigFile, String encryptedPassword) {
        if (consolePrivateKey == null) {
            EncryptDecrypt.init(consoleConfigFile);
        }
        return EncryptDecrypt.decryptRSA(encryptedPassword, consolePrivateKey);
    }

    private static String decryptRSA(String encryptedPassword, RSAPrivateKey pKey) {
        if (encryptedPassword.contains(JS_ENCRYPT_CHUNK_SEPARATOR)) {
            String[] chunks = StringUtil.split(encryptedPassword, JS_ENCRYPT_CHUNK_SEPARATOR);
            StringBuffer result = new StringBuffer();
            for (int j = 0; j < chunks.length; ++j) {
                result.append(EncryptDecrypt.decryptRSA(chunks[j].indexOf("{|}") == 0 ? chunks[j].substring("{|}".length()) : chunks[j]));
            }
            return result.toString();
        }
        BigInteger bi = new BigInteger(encryptedPassword, 16);
        byte[] ciperText = bi.toByteArray();
        if (ciperText.length == 129) {
            byte[] temp = new byte[128];
            for (int i = 1; i < ciperText.length; ++i) {
                temp[i - 1] = ciperText[i];
            }
            ciperText = temp;
        }
        byte[] clearText = new byte[]{};
        Cipher serverCipher = null;
        try {
            serverCipher = Cipher.getInstance("RSA");
            serverCipher.init(2, pKey);
            clearText = serverCipher.doFinal(ciperText);
        }
        catch (Exception e) {
            Trace.logError("Failed to decrypt RSA password", e);
        }
        return new String(clearText);
    }

    private static Cipher getCipher(String key, boolean encrypt) throws Exception {
        Provider provider = Security.getProvider(Configuration.getInstance().getJCEProvider());
        String keyString = Base64Coder.encode(key.getBytes());
        char[] mykey = new char[keyString.length()];
        for (int i = 0; i < mykey.length; ++i) {
            mykey[i] = keyString.charAt(i);
        }
        byte[] salt = new byte[]{-57, 115, 33, -116, 126, -56, -18, -103};
        int count = 20;
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, count);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(mykey);
        SecretKeyFactory keyFact = SecretKeyFactory.getInstance("PBEWithMD5AndDES", provider);
        SecretKey pbeKey = keyFact.generateSecret(pbeKeySpec);
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(encrypt ? 1 : 2, (Key)pbeKey, pbeParamSpec);
        return pbeCipher;
    }

    @Deprecated
    public static String encryptJCE(String inputs, boolean isCaseSensitive) {
        return EncryptDecrypt.encryptJCE(inputs, null, isCaseSensitive);
    }

    @Deprecated
    public static String encryptJCE(String inputs, String key, boolean isCaseSensitive) {
        return EncryptDecrypt.encrypt2(inputs, key, isCaseSensitive);
    }

    private static String encrypt2(String inputs, String key, boolean isCaseSensitive) {
        String encodedText;
        try {
            if (!isCaseSensitive) {
                inputs = inputs.toUpperCase();
            }
            Cipher pbeCipher = EncryptDecrypt.getCipher(key != null ? key : inputs, true);
            byte[] cleartext1 = inputs.getBytes();
            byte[] ciphertext1 = pbeCipher.doFinal(cleartext1);
            encodedText = Base64Coder.encode(ciphertext1);
        }
        catch (Exception e) {
            Trace.logError("Failed to encrypt JCE password", e);
            throw new RuntimeException(e);
        }
        return encodedText;
    }

    public static String encodePageid(String inputs) {
        return EncryptDecrypt.encrypt2(inputs, null, true);
    }

    public static String encodeComponentKey(String inputs, String key) {
        return EncryptDecrypt.encrypt2(inputs, key, true);
    }

    public static String encodePassword(String password) {
        return Argon2Factory.create((Argon2Factory.Argon2Types)ARGON_TYPE, (int)ARGON_SALT_LENGTH, (int)ARGON_HASH_LENGTH).hash(ARGON_ITERATIONS, ARGON_MEMORY, ARGON_PARALELLISM, password);
    }

    public static boolean passwordMatches(String password, String encodedPassword, boolean isCaseSensitive) {
        long start = System.currentTimeMillis();
        if (Argon2Factory.create((Argon2Factory.Argon2Types)ARGON_TYPE).verify(encodedPassword, password)) {
            long end = System.currentTimeMillis();
            Trace.logDebug(LOGNAME, "Verifying argon hash took: " + (end - start) + "ms");
            return true;
        }
        if (EncryptDecrypt.encrypt2(password, null, isCaseSensitive).equals(encodedPassword)) {
            return true;
        }
        return encodedPassword != null && encodedPassword.equals(EncryptDecrypt.encrypt(EncryptDecrypt.decrypt(encodedPassword))) && password.equalsIgnoreCase(EncryptDecrypt.decrypt(encodedPassword));
    }

    public static boolean passwordNeedUpgrade(String encodedPassword) {
        return !encodedPassword.startsWith("$argon2id$v=19$m=" + ARGON_MEMORY + ",t=" + ARGON_ITERATIONS + ",p=" + ARGON_PARALELLISM + "$");
    }

    public static String encryptAES(String text, String key) {
        try {
            byte[] encryptText = text.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64((String)key), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(1, skeySpec);
            return Base64.encodeBase64String((byte[])cipher.doFinal(encryptText));
        }
        catch (Exception e) {
            return text;
        }
    }

    public static String decryptAES(String text, String key) {
        try {
            byte[] encryptText = Base64.decodeBase64((String)text);
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64((String)key), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(2, skeySpec);
            return new String(cipher.doFinal(encryptText), StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return text;
        }
    }

    public static String encrypt(String text, String databaseid) {
        return "{|}" + EncryptDecrypt.encryptAES(text, lvInternalKeyMap.get(databaseid));
    }

    public static String decrypt(String text, String databaseid) {
        if (text.indexOf("{|}") == 0) {
            String key = lvInternalKeyMap.get(databaseid);
            if (key == null) {
                return "Error decrypting. Cannot find key for " + databaseid;
            }
            return EncryptDecrypt.decryptAES(text.substring("{|}".length()), lvInternalKeyMap.get(databaseid));
        }
        return BaseCrypt.decrypt(text);
    }

    public static String generateRandomAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecureRandom secureRandom = new SecureRandom();
            int keyBitSize = 256;
            keyGenerator.init(keyBitSize, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64Coder.encode(secretKey.getEncoded());
        }
        catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static boolean isObfuscating() {
        return obfMode != null && obfMode.equals(OBFUSCATE_MODE_OBF);
    }

    public static String obfsql(String sql) {
        return EncryptDecrypt.obfsql(sql, false);
    }

    public static String obfsql(String sql, boolean force) {
        if (EncryptDecrypt.isObfuscating() && sql != null && sql.trim().length() > 0 && !sql.startsWith(OBFUSCATE_PREFIX) || force) {
            sql = StringUtil.replaceAll(sql, "\n", OBFUSCATE_CRLF);
            StringBuffer obfsql = new StringBuffer();
            for (int i = 0; i < sql.length(); ++i) {
                if (sql.charAt(i) == '[') {
                    int j;
                    if (i + 1 < sql.length() && sql.charAt(i + 1) == ']') {
                        obfsql.append("[]");
                        ++i;
                        continue;
                    }
                    boolean javascript = i + 11 < sql.length() && sql.substring(i + 1, i + 11).equalsIgnoreCase("javascript");
                    int start = i++;
                    while (i < sql.length() && sql.charAt(i) != ']' && (javascript || Character.isJavaIdentifierPart(sql.charAt(i)))) {
                        ++i;
                    }
                    if (sql.charAt(i) == ']') {
                        for (j = start; j <= i; ++j) {
                            obfsql.append(sql.charAt(j));
                        }
                        continue;
                    }
                    for (j = start; j <= i; ++j) {
                        String hex = "000" + Integer.toHexString(sql.charAt(j));
                        obfsql.append(hex.substring(hex.length() - 4));
                    }
                    continue;
                }
                String hex = "000" + Integer.toHexString(sql.charAt(i) == '\t' ? 32 : (int)sql.charAt(i));
                obfsql.append(hex.substring(hex.length() - 4));
            }
            return OBFUSCATE_PREFIX + obfsql.toString();
        }
        return sql;
    }

    public static String unobfsql(String obfsql) {
        return EncryptDecrypt.unobfsql(null, obfsql, false);
    }

    public static String unobfsql(ServletRequest request, String obfsql, boolean restrictedSQL) {
        if (obfsql == null || obfsql.length() == 0) {
            return obfsql;
        }
        if (obfsql.startsWith(OBFUSCATE_PREFIX)) {
            if (obfsql.contains(OBFUSCATE_PARAM_START)) {
                String[] params = StringUtil.getTokens(obfsql, OBFUSCATE_PARAM_START, OBFUSCATE_PARAM_END);
                for (int i = 0; i < params.length; ++i) {
                    obfsql = params[i].length() > 0 ? StringUtil.replaceAll(obfsql, OBFUSCATE_PARAM_START + params[i] + OBFUSCATE_PARAM_END, EncryptDecrypt.obfsql(params[i], true).substring(OBFUSCATE_PREFIX.length())) : StringUtil.replaceAll(obfsql, "[*@@*]", "");
                }
            }
            StringBuffer unobfsql = new StringBuffer();
            String hex = obfsql.substring(OBFUSCATE_PREFIX.length());
            int i = 0;
            while (i < hex.length()) {
                if (hex.charAt(i) == '[') {
                    if (i + 1 < hex.length() && hex.charAt(i + 1) == ']') {
                        unobfsql.append("[]");
                        i += 2;
                        continue;
                    }
                    boolean javascript = i + 11 < hex.length() && hex.substring(i + 1, i + 11).equalsIgnoreCase("javascript");
                    unobfsql.append(hex.charAt(i));
                    ++i;
                    while (i < hex.length() && hex.charAt(i) != ']' && (javascript || Character.isJavaIdentifierPart(hex.charAt(i)))) {
                        unobfsql.append(hex.charAt(i));
                        ++i;
                    }
                    unobfsql.append("]");
                    ++i;
                    continue;
                }
                unobfsql.append((char)Integer.parseInt(hex.substring(i, i + 4), 16));
                i += 4;
            }
            if (unobfsql.indexOf("{|}") == 0) {
                return EncryptDecrypt.decryptRSA(unobfsql.substring("{|}".length()));
            }
            return StringUtil.replaceAll(unobfsql.toString(), OBFUSCATE_CRLF, "\n");
        }
        if (obfsql.startsWith("{|}")) {
            obfsql = EncryptDecrypt.decryptRSA(obfsql.substring("{|}".length()));
        }
        if (EncryptDecrypt.isObfuscating() && restrictedSQL && !SecurityPolicyUtil.isAllowedQueryWhere(request, obfsql)) {
            throw new RuntimeException("Error: Not Allowed Client Submitted Query Where by SecurityPolicy ( " + obfsql + " )");
        }
        return obfsql;
    }

    public static boolean isObfuscated(String text) {
        return text.startsWith(OBFUSCATE_PREFIX);
    }

    public static String obfReplaceAll(String input, String oldtext, String newtext) {
        return StringUtil.replaceAll(input, oldtext, EncryptDecrypt.isObfuscated(input) ? OBFUSCATE_PARAM_START + newtext + OBFUSCATE_PARAM_END : newtext);
    }

    public static void upgradeBOPasswordEncryption(DBUtil db, String databaseid) throws SapphireException {
        String sql = "SELECT sysuserid, propertyvalue FROM profileproperty WHERE profileid='System' AND propertyid='bopassword'";
        db.createResultSet(sql);
        while (db.getNext()) {
            String sysuserid = db.getString("sysuserid");
            String oldEncryptedPassword = db.getString("propertyvalue");
            String origianlPassword = EncryptDecrypt.decrypt(oldEncryptedPassword);
            String newEncryptedPassword = EncryptDecrypt.encrypt(origianlPassword, databaseid);
            db.executePreparedUpdate("UPDATE profileproperty SET propertyvalue = ? WHERE profileid = ? AND propertyid = ? AND sysuserid = ?", new Object[]{newEncryptedPassword, "System", "bopassword", sysuserid});
        }
    }

    public static String rot13(String input) {
        String output = "";
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'm') {
                c = (char)(c + 13);
            } else if (c >= 'A' && c <= 'M') {
                c = (char)(c + 13);
            } else if (c >= 'n' && c <= 'z') {
                c = (char)(c - 13);
            } else if (c >= 'N' && c <= 'Z') {
                c = (char)(c - 13);
            }
            output = output + c;
        }
        return output;
    }

    static {
        try {
            if (Configuration.isCreated()) {
                ARGON_SALT_LENGTH = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.security.argon_salt_length", Integer.toString(16)));
                ARGON_HASH_LENGTH = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.security.argon_hash_length", Integer.toString(32)));
                ARGON_ITERATIONS = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.security.argon_iterations", Integer.toString(3)));
                ARGON_MEMORY = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.security.argon_memory", Integer.toString(90000)));
                ARGON_PARALELLISM = Integer.parseInt(ConfigService.getConfigProperty("com.labvantage.sapphire.security.argon_paralellism", Integer.toString(8)));
            }
        }
        catch (ServiceException e) {
            Trace.logError(LOGNAME, (Object)"Error when overriding argon properties", e);
        }
        obfMode = OBFUSCATE_MODE_OBF;
        lvInternalKeyMap = new HashMap();
    }

    private static class Base64Coder
    extends AbstractPreferences {
        private String store;
        private static Base64Coder instance = new Base64Coder();

        private Base64Coder() {
            super(null, "");
        }

        public static synchronized String encode(byte[] b) {
            instance.putByteArray(null, b);
            return instance.get(null, null);
        }

        public static synchronized byte[] decode(String base64String) {
            instance.put(null, base64String);
            return instance.getByteArray(null, null);
        }

        @Override
        public String get(String key, String def) {
            return this.store;
        }

        @Override
        public void put(String key, String value) {
            this.store = value;
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            return null;
        }

        @Override
        protected void putSpi(String key, String value) {
        }

        @Override
        protected String getSpi(String key) {
            return null;
        }

        @Override
        protected void removeSpi(String key) {
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            return null;
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            return null;
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
        }
    }
}

