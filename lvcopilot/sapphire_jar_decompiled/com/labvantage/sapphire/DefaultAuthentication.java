/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import javax.naming.AuthenticationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import sapphire.SapphireException;
import sapphire.action.BaseAuthentication;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DefaultAuthentication
extends BaseAuthentication {
    public static final String INITIAL_CONTEXT_FACTORY = "INITIAL_CONTEXT_FACTORY";
    public static final String PROVIDER_URL = "PROVIDER_URL";
    public static final String SECURITY_AUTHENTICATION = "SECURITY_AUTHENTICATION";
    public static final String ROOT_USER_DN = "rootuserdn";
    public static final String ROOT_USER_PASSWORD = "rootuserpassword";
    public static final String USERID_ATTRIBUTE_NAME = "useridattributename";
    public static final String USER_SEARCH_CONTEXT = "usersearchdn";
    public static final String PROPS_TOOL = "tool";
    public static final String PROPS_PORTALID = "portalid";
    public static final String PROPS_SSOATTRIBUTES = "ssoattributes";
    private static final String BIND_LDAPUSER = "ldapuser";
    private static final String BIND_SSOUSER = "ssouser";
    private static final String BIND_ENVPROPS = "envprops";
    private static final HashMap dbPrimaryCache = new HashMap();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DirContext getConnection(PropertyList properties) throws Exception {
        DirContext ctx = null;
        try {
            ctx = this.getInitialDirContext(properties);
        }
        catch (Exception e) {
            PropertyListCollection ldapservers = properties.getCollection("additionalldapservers");
            if (ldapservers != null && ldapservers.size() > 0) {
                this.logger.warn("Cannot connect to the current primary LDAP server: " + this.getPrimaryLDAPServer(this.databaseid, properties) + ". Exception is " + e.getMessage() + ". Trying additional servers...");
                for (int s = 0; s < ldapservers.size(); ++s) {
                    PropertyList props = ldapservers.getPropertyList(s);
                    if (props.getProperty(PROVIDER_URL).length() <= 0) continue;
                    properties.setProperty(PROVIDER_URL, props.getProperty(PROVIDER_URL));
                    try {
                        ctx = this.getInitialDirContext(properties);
                        HashMap hashMap = dbPrimaryCache;
                        synchronized (hashMap) {
                            dbPrimaryCache.put(this.databaseid, props.getProperty(PROVIDER_URL));
                        }
                    }
                    catch (Exception ee) {
                        if (s == ldapservers.size() - 1) {
                            this.logError("Cannot connect to any LDAP server. Last try to connection to " + properties.getProperty(PROVIDER_URL), ee);
                            throw ee;
                        }
                        this.logger.warn("Cannot connect to the secondary LDAP server: " + properties.getProperty(PROVIDER_URL) + ". Exception is " + ee.getClass().getName() + "," + ee.getMessage() + ". Trying additional servers...");
                    }
                }
            }
            this.logError("Cannot connect to LDAP Server: " + properties.getProperty(PROVIDER_URL), e);
            throw e;
        }
        return ctx;
    }

    private DirContext getInitialDirContext(PropertyList properties) throws Exception {
        String ldapURL = this.getPrimaryLDAPServer(this.databaseid, properties);
        return this.getInitialDirContext(properties, ldapURL);
    }

    private DirContext getInitialDirContext(PropertyList properties, String ldapURL) throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", properties.getProperty(INITIAL_CONTEXT_FACTORY).length() == 0 ? "com.sun.jndi.ldap.LdapCtxFactory" : properties.getProperty(INITIAL_CONTEXT_FACTORY));
        env.put("java.naming.provider.url", ldapURL);
        env.put("java.naming.security.authentication", properties.getProperty(SECURITY_AUTHENTICATION).length() == 0 ? "simple" : properties.getProperty(SECURITY_AUTHENTICATION));
        env.put("java.naming.referral", "follow");
        String rootuserid = properties.getProperty(ROOT_USER_DN);
        if (this.databaseid != null) {
            properties.setDatabaseid(this.databaseid);
        }
        String rootuserpassword = properties.getDecryptedProperty(ROOT_USER_PASSWORD);
        if (rootuserid.length() > 0 && rootuserpassword.length() > 0) {
            env.put("java.naming.security.principal", rootuserid);
            env.put("java.naming.security.credentials", rootuserpassword);
            this.logTrace("Try to bind to the LDAP server " + ldapURL + " using the root user DN/password combination...");
        } else {
            this.logTrace("Try to bind to the LDAP server " + ldapURL + " anonimously as the root user DN/password not provided...");
        }
        return new InitialDirContext(env);
    }

    private void checkUserIdAttribute(DirContext ctx, PropertyList properties) throws Exception {
        BasicAttributes matchAttrs = new BasicAttributes(true);
        String useridattrid = properties.getProperty(USERID_ATTRIBUTE_NAME);
        matchAttrs.put(new BasicAttribute(useridattrid, "test"));
        String usersdn = properties.getProperty(USER_SEARCH_CONTEXT);
        String[] usersnodes = null;
        usersnodes = StringUtil.split(usersdn, "||");
        for (int n = 0; n < usersnodes.length; ++n) {
            CompositeName usernodeName = new CompositeName(usersnodes[n]);
            NamingEnumeration<SearchResult> namingEnumeration = ctx.search(usernodeName, (Attributes)matchAttrs);
        }
    }

    public void testConfiguration(PropertyList properties) throws Exception {
        try (Context ctx = null;){
            String ldapURL = properties.getProperty(PROVIDER_URL);
            ctx = this.getInitialDirContext(properties, ldapURL);
            this.checkUserIdAttribute((DirContext)ctx, properties);
            PropertyListCollection ldapservers = properties.getCollection("additionalldapservers");
            if (ldapservers != null && ldapservers.size() > 0) {
                for (int s = 0; s < ldapservers.size(); ++s) {
                    PropertyList props = ldapservers.getPropertyList(s);
                    if (props.getProperty(PROVIDER_URL).length() <= 0) continue;
                    ctx = this.getInitialDirContext(properties, props.getProperty(PROVIDER_URL));
                    this.checkUserIdAttribute((DirContext)ctx, properties);
                }
            }
        }
    }

    public String getPrimaryLDAPServer(String databaseid, PropertyList properties) {
        String ldapURL = dbPrimaryCache.get(databaseid) == null ? properties.getProperty(PROVIDER_URL) : (String)dbPrimaryCache.get(databaseid);
        return ldapURL;
    }

    @Override
    public void authenticateUser(String username, String password, PropertyList properties) throws SapphireException {
        DirContext ctx = null;
        String errmsg = "";
        String specificMessage = "";
        try {
            ctx = this.getConnection(properties);
        }
        catch (Exception e) {
            throw new SapphireException("Cannot connect to LDAP Server. Please Ask your administrator for help.");
        }
        try {
            BasicAttributes matchAttrs = new BasicAttributes(true);
            String useridattrid = properties.getProperty(USERID_ATTRIBUTE_NAME);
            matchAttrs.put(new BasicAttribute(useridattrid, username));
            String usersdns = properties.getProperty(USER_SEARCH_CONTEXT);
            String[] usersnodes = null;
            usersnodes = StringUtil.split(usersdns, "||");
            int found = 0;
            String dn = "";
            for (int n = 0; n < usersnodes.length; ++n) {
                String usersdn = usersnodes[n];
                CompositeName usernodeName = new CompositeName(usersdn);
                SearchControls controls = new SearchControls();
                controls.setSearchScope(2);
                NamingEnumeration<SearchResult> answer = ctx.search((Name)usernodeName, useridattrid + "=" + username, controls);
                if (!answer.hasMoreElements() || !answer.hasMore()) continue;
                SearchResult sr = answer.next();
                dn = sr.getNameInNamespace();
                this.ldapUserAttributes = sr.getAttributes();
                try {
                    NamingEnumeration<String> e = this.ldapUserAttributes.getIDs();
                    while (e.hasMoreElements()) {
                        String attrid = (String)e.nextElement();
                        ArrayList<String> attribValueList = this.getAttributeValueList(attrid);
                        String attribValue = this.getAttributeValue(attrid);
                        this.ldapUserAttributesMap.put(attrid.toLowerCase() + "List", attribValueList);
                        this.ldapUserAttributesMap.put(attrid.toLowerCase(), attribValue);
                        this.ldapUserAttributesMap.put(attrid + "List", attribValueList);
                        this.ldapUserAttributesMap.put(attrid, attribValue);
                    }
                }
                catch (Throwable t) {
                    Trace.logError("Error in createUser: Unable to map user attributes for ldapuser " + this.ldapUserAttributesMap.toString(), t);
                }
                this.logTrace("Found user:" + dn);
                ++found;
            }
            if (found == 0) {
                errmsg = "Authentication failed. Incorrect username or password for username '" + username + "'";
                specificMessage = "Authentication failed. User not found for username '" + username + "'";
            }
            if (found > 1) {
                errmsg = "Authentication failed. Incorrect username or password for username '" + username + "'";
                specificMessage = "Authentication failed. More than one user found with username '" + username + "'";
            }
            this.logTrace("LDAP message:" + specificMessage);
            if (errmsg.length() == 0) {
                this.bindUser(dn, password, ctx);
            }
        }
        catch (AuthenticationException ae) {
            errmsg = "Authentication failed. Incorrect username or password for username '" + username + "'";
            specificMessage = ae.getMessage();
            this.logError("LDAP message:" + specificMessage, ae);
        }
        catch (Exception e) {
            errmsg = "Authentication failed. " + e.getMessage() + " for username '" + username + "'";
            specificMessage = e.getMessage();
            this.logError("LDAP message:" + e.getMessage(), e);
        }
        if (errmsg.length() > 0) {
            PropertyListCollection maps = properties.getCollection("errormsgmapping");
            if (maps != null && maps.size() > 0) {
                for (int m = 0; m < maps.size(); ++m) {
                    PropertyList p = maps.getPropertyList(m);
                    if (p == null) continue;
                    String contains = p.getProperty("ldaperrorcontains");
                    String usermessage = p.getProperty("usermessage");
                    if (specificMessage.indexOf(contains) < 0 || usermessage.length() <= 0) continue;
                    usermessage = StringUtil.replaceAll(usermessage, "[currentuser]", username);
                    usermessage = StringUtil.replaceAll(usermessage, "[ldaperrormessage]", specificMessage);
                    errmsg = errmsg + "|%|" + usermessage;
                    break;
                }
            }
            throw new SapphireException(errmsg);
        }
    }

    private void bindUser(String name, String password, DirContext ctx) throws Exception {
        if (password == null || password.length() == 0) {
            throw new Exception("Authentication failed. Incorrect username or password for username '" + name + "'");
        }
        Hashtable<?, ?> env = ctx.getEnvironment();
        env.put("java.naming.security.principal", name);
        env.put("java.naming.security.credentials", password);
        InitialDirContext userctx = new InitialDirContext(env);
        this.logTrace("User Authenticated by LDAP server ");
        userctx.close();
        ctx.close();
    }

    @Override
    public void createUser(String username, String password, PropertyList ldappropertytree) throws SapphireException {
        PropertyList userprops = ldappropertytree.getPropertyListNotNull("defaultuserprops");
        if ("Y".equals(userprops.getProperty("createuser", "N"))) {
            this.addUser(username, password, ldappropertytree, this.ldapUserAttributesMap);
        }
    }

    @Override
    public void synchronizeUser(String username, String password, PropertyList propertyTreeProps) throws SapphireException {
        this.logTrace("Start synchronize the user...");
        PropertyList ldapuserprops = propertyTreeProps.getPropertyList("defaultuserprops");
        PropertyList ssouserprops = propertyTreeProps.getPropertyList("ssouserprops");
        PropertyList userProps = new PropertyList();
        HashMap userAttributes = new PropertyList();
        boolean isLDAP = false;
        boolean isSSO = false;
        if (ldapuserprops != null) {
            isLDAP = true;
            userProps = ldapuserprops;
            userAttributes = this.ldapUserAttributesMap;
        }
        if (ssouserprops != null) {
            isSSO = true;
            userProps = ssouserprops;
            userAttributes = propertyTreeProps.getPropertyList(PROPS_SSOATTRIBUTES);
        }
        PropertyList props = new PropertyList();
        props.put("sdcid", "User");
        DataSet sysuserDs = this.getSysUserDataSet(username);
        props.put("keyid1", sysuserDs.getValue(0, "sysuserid"));
        boolean change = false;
        PropertyListCollection columns = userProps.getCollection("columns");
        Trace.logDebug("Start synchronize for user " + username);
        if (userProps != null && columns != null) {
            this.syncUserAttribute(columns, props, userAttributes);
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList cols = columns.getPropertyList(i);
                String propid = cols.getProperty("columnid");
                if (propid == null || !sysuserDs.isValidColumn(propid) || sysuserDs.getValue(0, propid).equals(userAttributes.get(cols.getProperty("value")))) continue;
                change = true;
            }
            Trace.logDebug("Done synchronizing user " + username);
        }
        if (password == null || password.length() == 0) {
            props.remove("password");
        }
        if (change) {
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        this.syncUserJobType(sysuserDs.getValue(0, "sysuserid"), propertyTreeProps, userAttributes);
        this.logTrace("Done synchronize the user");
    }

    protected DataSet getSysUserDataSet(String username) throws SapphireException {
        this.database.createPreparedResultSet("getCorrectCaseUserId", "select * from sysuser where upper( sysuserid ) = ? or upper( logonname ) = ?", new String[]{username.toUpperCase(), username.toUpperCase()});
        DataSet sysuserDs = new DataSet();
        sysuserDs.setResultSet(this.database.getResultSet("getCorrectCaseUserId"), true, this.database.isOracle() ? "ORA" : "MSS");
        this.database.closeResultSet("getCorrectCaseUserId");
        return sysuserDs;
    }

    private void syncUserAttribute(PropertyListCollection columns, PropertyList props, HashMap userAttributes) throws SapphireException {
        for (int i = 0; i < columns.size(); ++i) {
            PropertyList column = columns.getPropertyList(i);
            String value = column.getProperty("value");
            if (value.indexOf("[") >= 0) {
                String[] tokens = StringUtil.getTokens(value);
                for (int t = 0; t < tokens.length; ++t) {
                    String replaceStr = "";
                    String attrid = tokens[t];
                    int startpos = 0;
                    int endpos = 0;
                    boolean changeCase = false;
                    boolean doSubString = false;
                    try {
                        if (attrid.indexOf("(") > 0) {
                            attrid = tokens[t].substring(0, attrid.indexOf("("));
                            String positionStr = StringUtil.getTokens(tokens[t], "(", ")")[0];
                            startpos = Integer.parseInt(StringUtil.split(positionStr, ",")[0]);
                            endpos = Integer.parseInt(StringUtil.split(positionStr, ",")[1]);
                            doSubString = true;
                            if (tokens[t].indexOf(")") + 1 < tokens[t].length()) {
                                changeCase = true;
                            }
                        }
                        if (userAttributes.containsKey(attrid)) {
                            replaceStr = (String)userAttributes.get(attrid);
                            if (doSubString) {
                                if (endpos > replaceStr.length()) {
                                    endpos = replaceStr.length();
                                }
                                replaceStr = replaceStr.substring(startpos, endpos);
                            }
                            if (changeCase) {
                                if ("U".equalsIgnoreCase(tokens[t].substring(tokens[t].length() - 1))) {
                                    replaceStr = replaceStr.toUpperCase();
                                }
                                if ("L".equalsIgnoreCase(tokens[t].substring(tokens[t].length() - 1))) {
                                    replaceStr = replaceStr.toLowerCase();
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        throw new SapphireException("Cannot parse value syntax:" + value);
                    }
                    value = StringUtil.replaceAll(value, "[" + tokens[t] + "]", replaceStr);
                }
            }
            props.put(column.get("columnid"), value);
        }
    }

    private void syncUserJobType(String sysuserid, PropertyList propertyTreeProps, HashMap userAttributes) throws SapphireException {
        PropertyList advancedusermapping;
        PropertyListCollection userjobtypemappings;
        PropertyList ldapuserprops = propertyTreeProps.getPropertyList("defaultuserprops");
        PropertyList ssouserprops = propertyTreeProps.getPropertyList("ssouserprops");
        PropertyList userprops = new PropertyList();
        boolean isLDAP = false;
        boolean isSSO = false;
        if (ldapuserprops != null) {
            isLDAP = true;
            userprops = ldapuserprops;
        }
        if (ssouserprops != null) {
            isSSO = true;
            userprops = ssouserprops;
        }
        if (userprops != null && "Advanced".equals(userprops.getProperty("usermappingoption")) && (userjobtypemappings = (advancedusermapping = userprops.getPropertyList("advancedusermapping")).getCollection("userjobtypemappings")) != null && userjobtypemappings.size() > 0) {
            HashSet<String> jobtypeSet = new HashSet<String>();
            for (int i = 0; i < userjobtypemappings.size(); ++i) {
                PropertyList mapping = userjobtypemappings.getPropertyList(i);
                String expression = mapping.getProperty("mappingexpression");
                HashMap<String, HashMap> bindmap = new HashMap<String, HashMap>();
                bindmap.put(BIND_ENVPROPS, this.getEnvProps(propertyTreeProps));
                bindmap.put(isSSO ? BIND_SSOUSER : BIND_LDAPUSER, userAttributes);
                try {
                    String result = GroovyUtil.evaluate(expression, bindmap);
                    if (!"true".equals(result)) continue;
                    String jobtypeid = mapping.getProperty("jobtypeid");
                    jobtypeSet.add(jobtypeid);
                    continue;
                }
                catch (Throwable t) {
                    Trace.logError("Error evaluate expression:" + expression + " with ldapuser " + bindmap.toString(), t);
                }
            }
            HashSet<String> existingJobtypeSet = new HashSet<String>();
            this.database.createPreparedResultSet("jobtypes", "select jobtypeid from sysuserjobtype where sysuserid=?", new Object[]{sysuserid});
            while (this.database.getNext("jobtypes")) {
                existingJobtypeSet.add(this.database.getString("jobtypes", "jobtypeid"));
            }
            if (!jobtypeSet.equals(existingJobtypeSet)) {
                HashSet<String> addSet = new HashSet<String>();
                addSet.addAll(jobtypeSet);
                addSet.removeAll(existingJobtypeSet);
                if (addSet.size() > 0) {
                    this.getActionProcessor().processAction("AddSDIDetail", "1", this.createUserJobTypeSDIDetailActionProps(sysuserid, addSet));
                }
                HashSet<String> removeSet = new HashSet<String>();
                removeSet.addAll(existingJobtypeSet);
                removeSet.removeAll(jobtypeSet);
                if (removeSet.size() > 0) {
                    this.getActionProcessor().processAction("DeleteSDIDetail", "1", this.createUserJobTypeSDIDetailActionProps(sysuserid, removeSet));
                }
            }
        }
    }

    @Override
    public void secondaryAuthentication(PropertyList properties) throws SapphireException {
        PropertyList ldapuserprops = properties.getPropertyList("defaultuserprops");
        PropertyList ssouserprops = properties.getPropertyList("ssouserprops");
        PropertyList userprops = new PropertyList();
        boolean isSSO = false;
        boolean isLDAP = false;
        if (userprops != null && userprops.getProperty("allowlogonexpression").equals("Y")) {
            isLDAP = true;
            userprops = ldapuserprops;
        }
        if (ssouserprops != null && ssouserprops.getProperty("allowlogonexpression").equals("Y")) {
            isSSO = true;
            userprops = ssouserprops;
        }
        boolean passedSecondaryAuthentication = false;
        if (!isLDAP && !isSSO) {
            return;
        }
        String expression = userprops.getProperty("logonmappingexpression");
        HashMap<String, HashMap> bindmap = new HashMap<String, HashMap>();
        bindmap.put(BIND_LDAPUSER, isSSO ? properties.getPropertyList(PROPS_SSOATTRIBUTES) : this.ldapUserAttributesMap);
        bindmap.put(BIND_ENVPROPS, this.getEnvProps(properties));
        try {
            String result = GroovyUtil.evaluate(expression, bindmap);
            if ("true".equals(result)) {
                passedSecondaryAuthentication = true;
            }
        }
        catch (Throwable t) {
            Trace.logError("Error evaluate expression:" + expression + " with user " + bindmap.toString(), t);
        }
        if (!passedSecondaryAuthentication) {
            throw new SapphireException("Secondary authentication failed!");
        }
    }

    private ArrayList<String> getAttributeValueList(String attrid) throws NamingException {
        Attribute attr = this.ldapUserAttributes.get(attrid.toLowerCase());
        ArrayList<String> valuelist = new ArrayList<String>();
        if (attr != null) {
            NamingEnumeration<?> enumeration = attr.getAll();
            while (enumeration.hasMoreElements()) {
                valuelist.add(enumeration.nextElement().toString());
            }
        }
        return valuelist;
    }

    private String getAttributeValue(String attrid) throws NamingException {
        Attribute attr = this.ldapUserAttributes.get(attrid.toLowerCase());
        StringBuffer sb = new StringBuffer();
        if (attr != null) {
            NamingEnumeration<?> enumeration = attr.getAll();
            while (enumeration.hasMoreElements()) {
                sb.append((sb.length() > 0 ? ";" : "") + enumeration.nextElement().toString());
            }
        }
        return sb.toString();
    }

    private PropertyList createUserJobTypeSDIDetailActionProps(String sysuserid, HashSet jobtypeSet) {
        PropertyList props = new PropertyList();
        props.put("sdcid", "User");
        props.put("sysuserid", sysuserid);
        props.put("linkid", "user jobtypes");
        props.put("jobtypeid", StringUtil.replaceAll(jobtypeSet.toString().substring(1, jobtypeSet.toString().length() - 1), ", ", ";"));
        return props;
    }

    private PropertyList getEnvProps(PropertyList properties) {
        PropertyList envProps = new PropertyList();
        envProps.setProperty(PROPS_TOOL, properties.getProperty(PROPS_TOOL));
        envProps.setProperty(PROPS_PORTALID, properties.getProperty(PROPS_PORTALID));
        return envProps;
    }

    protected void addUser(String username, String password, PropertyList properties, HashMap userAttributes) throws SapphireException {
        String tool;
        PropertyList ldapuserprops = properties.getPropertyList("defaultuserprops");
        PropertyList ssouserprops = properties.getPropertyList("ssouserprops");
        PropertyList userprops = new PropertyList();
        PropertyListCollection columns = new PropertyListCollection();
        boolean isLDAP = false;
        boolean isSSO = false;
        boolean createUser = false;
        if (ldapuserprops != null) {
            isLDAP = true;
            userprops = ldapuserprops;
        }
        if (ssouserprops != null) {
            isSSO = true;
            userprops = ssouserprops;
        }
        if ("Y".equals(userprops.getProperty("createuser", "N"))) {
            createUser = true;
        }
        columns = userprops.getCollection("columns");
        String templateid = "";
        if (createUser) {
            if ("Advanced".equals(userprops.getProperty("usermappingoption"))) {
                PropertyListCollection usertemplatemappings;
                PropertyList advancedusermapping = userprops.getPropertyList("advancedusermapping");
                if (advancedusermapping != null && (usertemplatemappings = advancedusermapping.getCollection("usertemplatemappings")) != null && usertemplatemappings.size() > 0) {
                    HashMap<String, HashMap> bindmap = new HashMap<String, HashMap>();
                    if (isSSO) {
                        bindmap.put(BIND_SSOUSER, userAttributes);
                    } else {
                        bindmap.put(BIND_LDAPUSER, userAttributes);
                    }
                    bindmap.put(BIND_ENVPROPS, this.getEnvProps(properties));
                    for (int i = 0; i < usertemplatemappings.size(); ++i) {
                        PropertyList mapping = usertemplatemappings.getPropertyList(i);
                        String expression = mapping.getProperty("mappingexpression");
                        try {
                            String result = GroovyUtil.evaluate(expression, bindmap);
                            if (!"true".equals(result)) continue;
                            templateid = mapping.getProperty("usertemplateid");
                            break;
                        }
                        catch (Throwable t) {
                            Trace.logError("Error evaluate expression:" + expression + " with ldapuser " + userAttributes.toString(), t);
                        }
                    }
                }
            } else {
                templateid = userprops.getProperty("usertemplateid");
            }
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "User");
        props.setProperty("password", password);
        if (isLDAP) {
            props.put("bypassformatcheck", "Y");
            props.put("isautocreatedfromldap", "Y");
        }
        if (templateid.length() > 0) {
            props.setProperty("templateid", templateid);
        }
        if (columns != null && !columns.isEmpty()) {
            this.syncUserAttribute(columns, props, userAttributes);
        }
        if (props.getProperty("sysuserid", "").length() == 0 && props.getProperty("keyid1", "").length() == 0) {
            props.setProperty("keyid1", username);
        }
        if (props.getProperty("logonname", "").length() == 0) {
            props.setProperty("logonname", username);
        }
        String namedUserFlag = props.getProperty("nameduserflag");
        String baseDepartment = props.getProperty("basedepartment");
        if (templateid.length() > 0 && (namedUserFlag.length() == 0 || baseDepartment.length() == 0)) {
            this.database.createPreparedResultSet("templateInfo", "SELECT nameduserflag, basedepartment, sysuserdesc FROM sysuser WHERE sysuserid = ?", new Object[]{templateid});
            DataSet templateInfo = new DataSet(this.database.getResultSet("templateInfo"));
            this.database.closeResultSet("templateInfo");
            if (templateInfo.getRowCount() > 0) {
                if (namedUserFlag.length() == 0) {
                    namedUserFlag = templateInfo.getString(0, "nameduserflag", "");
                }
                if (baseDepartment.length() == 0) {
                    baseDepartment = templateInfo.getString(0, "basedepartment", "");
                }
            }
        }
        String baseDepartmentPortalClientFlag = "";
        if (baseDepartment.length() > 0) {
            this.database.createPreparedResultSet("baseDeptInfo", "SELECT portalclientflag FROM department WHERE departmentid = ?", new Object[]{baseDepartment});
            DataSet baseDeptInfo = new DataSet(this.database.getResultSet("baseDeptInfo"));
            this.database.closeResultSet("baseDeptInfo");
            if (baseDeptInfo.getRowCount() > 0) {
                baseDepartmentPortalClientFlag = baseDeptInfo.getString(0, "portalclientflag", "N");
            }
        }
        if ("Stellar".equals(tool = properties.getProperty(PROPS_TOOL))) {
            if (!"P".equals(namedUserFlag) && !"Q".equals(namedUserFlag)) {
                throw new SapphireException("Only Portal Users are allowed to be created from this interface. Check SSO Configuration.");
            }
            if (baseDepartment.length() == 0) {
                throw new SapphireException("Portal Client information is mandatory for Portal Users. Check SSO Configuration.");
            }
            if ("N".equals(baseDepartmentPortalClientFlag)) {
                throw new SapphireException("Base Department is not marked as a Portal Client. Check SSO Configuration.");
            }
        } else {
            if ("P".equals(namedUserFlag) || "Q".equals(namedUserFlag)) {
                throw new SapphireException("Only LIMS Users are allowed to be created from this interface. Check SSO Configuration.");
            }
            if (baseDepartment.length() > 0 && !"N".equals(baseDepartmentPortalClientFlag)) {
                throw new SapphireException("The base department for this User is marked as a Portal Client. Check SSO Configuration.");
            }
        }
        if (namedUserFlag != null && !namedUserFlag.isEmpty()) {
            props.setProperty("nameduserflag", namedUserFlag);
        }
        if (baseDepartment != null && !baseDepartment.isEmpty()) {
            props.setProperty("basedepartment", baseDepartment);
        }
        this.getActionProcessor().processAction("AddSDI", "1", props);
        this.syncUserJobType(username, properties, userAttributes);
    }
}

