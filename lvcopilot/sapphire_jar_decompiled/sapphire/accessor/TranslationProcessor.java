/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package sapphire.accessor;

import com.labvantage.sapphire.BaseAccessor;
import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TranslationProcessor
extends BaseAccessor {
    private PageContext pageContext = null;
    private static final String LANGUAGE_CACHE_NAME = "Language";
    private static Cache autoFillFlagCache = new Cache("AutoFillTransmaster");
    private String language = "";
    private static Cache showTranslationsCache = new Cache("ShowTranslations");
    protected String texttype = "W";
    private ConnectionInfo connectionInfo = null;
    private SapphireConnection sapphireConnection = null;

    public TranslationProcessor(String connectionid) {
        super(connectionid);
        this.connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        this.init(null);
    }

    public TranslationProcessor(String nameserverlist, String connectionid) {
        super(connectionid);
        this.connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        this.init(null);
    }

    public TranslationProcessor(File rakFile, String connectionid) {
        super(rakFile, connectionid);
        this.connectionInfo = new ConnectionProcessor(rakFile, connectionid).getConnectionInfo(connectionid);
        this.init(rakFile);
    }

    public TranslationProcessor(PageContext pageContext) {
        super(pageContext);
        this.pageContext = pageContext;
        RequestContext requestContext = (RequestContext)pageContext.getRequest().getAttribute("RequestContext");
        this.connectionInfo = requestContext != null ? new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionId()) : null;
        this.init(null);
    }

    private void init(File rakFile) {
        if (this.connectionInfo != null) {
            this.language = this.connectionInfo.getLanguage();
            this.sapphireConnection = new SapphireConnection();
            this.sapphireConnection.setConnectionInfo(this.connectionInfo);
            if (autoFillFlagCache.get(this.connectionInfo.getDatabaseId()) == null || showTranslationsCache.get(this.connectionInfo.getDatabaseId()) == null) {
                ConfigurationProcessor configurationProcessor = rakFile == null ? new ConfigurationProcessor(this.connectionInfo.getConnectionId()) : new ConfigurationProcessor(rakFile, this.connectionInfo.getConnectionId());
                autoFillFlagCache.put(this.connectionInfo.getDatabaseId(), configurationProcessor.getProfileProperty("masterupdate", "N"));
                showTranslationsCache.put(this.connectionInfo.getDatabaseId(), configurationProcessor.getProfileProperty("showtranslations", "N"));
            }
        }
    }

    public String getTextType() {
        return this.texttype;
    }

    public void setTextType(String textType) {
        this.texttype = textType;
    }

    public void saveTranslation(String languageid, String textidlist, String transtextlist, String texttypelist) throws SapphireException {
        try {
            if (local) {
                this.getLocalAccessManager().saveTranslation(this.getConnectionid(), languageid, textidlist, transtextlist, texttypelist);
            } else {
                this.getRemoteAccessManager().saveTranslation(this.getConnectionid(), languageid, textidlist, transtextlist, texttypelist);
            }
            String databaseid = SecurityService.getDatabaseId(this.getConnectionid());
            this.clearLanguageCache(databaseid, languageid);
        }
        catch (Exception e) {
            this.setError("Failed to save translations. Exception: " + e.getMessage(), e);
            throw new SapphireException("Failed to save translations. Exception: " + e.getMessage());
        }
    }

    public void saveTranslation(String languageid, String textidlist, String transtextlist) throws SapphireException {
        this.saveTranslation(languageid, textidlist, transtextlist, null);
    }

    public void translateTable(String languageid, HashMap transtable) {
        try {
            if (!"Y".equals(this.connectionInfo != null ? showTranslationsCache.get(this.connectionInfo.getDatabaseId()) : "N")) {
                PropertyList translations = local ? this.getLocalAccessManager().translateTable(this.getConnectionid(), languageid, new PropertyList(transtable)) : this.getRemoteAccessManager().translateTable(this.getConnectionid(), languageid, new PropertyList(transtable));
                transtable.putAll(translations);
            } else {
                for (String translation : transtable.keySet()) {
                    transtable.put(translation, "@" + transtable.get(translation));
                }
            }
        }
        catch (Exception e) {
            this.setError("Failed to get SQL DataSet. Exception: " + e.getMessage(), e);
        }
    }

    public boolean isRTL() {
        return this.connectionInfo == null ? false : this.connectionInfo.isRtl();
    }

    private boolean addToTransmasterTemp(String textid, String texttype) {
        try {
            if (local) {
                this.getLocalAccessManager().addToTransmasterTemp(this.getConnectionid(), textid, texttype);
            } else {
                this.getRemoteAccessManager().addToTransmasterTemp(this.getConnectionid(), textid, texttype);
            }
            return true;
        }
        catch (Exception e) {
            Trace.logError("TranslationProcessor Error", e);
            return false;
        }
    }

    public String translate(String textid, String language, String texttype) {
        String translatedtext = textid;
        try {
            if (language == null || language.length() == 0) {
                if (this.pageContext == null) {
                    translatedtext = textid;
                } else {
                    RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
                    language = requestContext.getPropertyList().getProperty("language");
                }
            }
            if (language == null || language.length() == 0) {
                translatedtext = textid;
            } else {
                String databaseid = SecurityService.getDatabaseId(this.getConnectionid());
                PropertyList transtable = (PropertyList)CacheUtil.get(databaseid, LANGUAGE_CACHE_NAME, language);
                if (transtable == null) {
                    transtable = local ? this.getLocalAccessManager().getWebTranslations(this.getConnectionid(), language) : this.getRemoteAccessManager().getWebTranslations(this.getConnectionid(), language);
                    CacheUtil.put(databaseid, LANGUAGE_CACHE_NAME, language, transtable);
                }
                Object valueObj = transtable.get(textid);
                String ptextid = textid.trim();
                if (ptextid.length() == 0) {
                    translatedtext = textid;
                } else {
                    if (valueObj == null) {
                        valueObj = transtable.get(ptextid);
                    }
                    char lastChar = ptextid.charAt(ptextid.length() - 1);
                    boolean trimedLastCharMatch = false;
                    boolean trimedLastChar = false;
                    if (valueObj == null && (lastChar == '.' || lastChar == ',' || lastChar == ':' || lastChar == ';' || lastChar == '?')) {
                        ptextid = ptextid.substring(0, ptextid.length() - 1);
                        trimedLastChar = true;
                        valueObj = transtable.get(ptextid);
                        if (valueObj != null) {
                            trimedLastCharMatch = true;
                        }
                    }
                    boolean caseInsensitiveMatch = false;
                    if (valueObj == null && (valueObj = transtable.get(ptextid.toLowerCase())) != null) {
                        caseInsensitiveMatch = true;
                    }
                    if (valueObj == null) {
                        translatedtext = textid;
                        if ("Y".equals(autoFillFlagCache.get(databaseid))) {
                            this.addToTransmasterTemp(textid, "W");
                        }
                    } else if (valueObj instanceof String) {
                        translatedtext = (String)valueObj;
                    } else if (valueObj instanceof PropertyList) {
                        PropertyList vL = (PropertyList)valueObj;
                        String tt = vL.getProperty(texttype);
                        if (tt.length() == 0) {
                            while (texttype.indexOf(".") > 0 && (tt = ((PropertyList)valueObj).getProperty(texttype = texttype.substring(0, texttype.lastIndexOf(".")))).length() <= 0) {
                            }
                            if (tt.length() == 0) {
                                tt = ((PropertyList)valueObj).getProperty("W");
                            }
                            if (tt.length() == 0) {
                                Iterator itr = vL.keySet().iterator();
                                while (itr.hasNext() && (tt = vL.getProperty((String)itr.next())).length() <= 0) {
                                }
                            }
                            if (tt.length() > 0) {
                                translatedtext = tt;
                            }
                        } else {
                            translatedtext = tt;
                        }
                    }
                    if (caseInsensitiveMatch) {
                        if (textid.equals(textid.toUpperCase())) {
                            translatedtext = translatedtext.toUpperCase();
                        } else if (Character.isUpperCase(textid.charAt(0)) && !Character.isUpperCase(translatedtext.charAt(0))) {
                            translatedtext = Character.toUpperCase(translatedtext.charAt(0)) + translatedtext.substring(1);
                        }
                    }
                    if (trimedLastCharMatch || trimedLastChar && caseInsensitiveMatch) {
                        translatedtext = translatedtext + lastChar;
                    }
                }
            }
        }
        catch (Exception e) {
            Trace.logError(e.getMessage());
        }
        if (this.connectionInfo != null && "Y".equals(showTranslationsCache.get(this.connectionInfo.getDatabaseId()))) {
            return this.buildShowTranslationsText(textid, translatedtext.equals(textid), texttype, this.connectionInfo.getLanguage());
        }
        return translatedtext;
    }

    private void clearLanguageCache(String databaseid, String language) {
        CacheUtil.remove(databaseid, LANGUAGE_CACHE_NAME, language);
    }

    public String translate(String textid) {
        if (textid == null || textid.length() == 0) {
            return "";
        }
        if (this.pageContext == null && (this.language == null || this.language.length() == 0)) {
            return textid;
        }
        return this.translate(textid, this.language);
    }

    public String translate(String textid, String language) {
        return this.translate(textid, language, this.texttype);
    }

    public String translate(String textid, Map tokenValueMap) {
        String translatedtext = this.translate(textid);
        if (this.connectionInfo != null && "Y".equals(showTranslationsCache.get(this.connectionInfo.getDatabaseId()))) {
            return translatedtext;
        }
        if (tokenValueMap != null) {
            for (String key : tokenValueMap.keySet()) {
                translatedtext = StringUtil.replaceAll(translatedtext, "[" + key + "]", (String)tokenValueMap.get(key));
            }
        }
        return translatedtext;
    }

    public String translatePartial(String textid, String language) {
        String translatedvalue = textid;
        if (textid.indexOf("{{") >= 0) {
            String[] tokens = StringUtil.getTokens(translatedvalue, "{{", "}}");
            int ii = tokens.length;
            for (int i = 0; i < ii; ++i) {
                translatedvalue = language == null || language.length() == 0 || language.equals("(null)") ? StringUtil.replaceAll(translatedvalue, "{{" + tokens[i] + "}}", tokens[i]) : StringUtil.replaceAll(translatedvalue, "{{" + tokens[i] + "}}", this.translate(tokens[i], language));
            }
        }
        return translatedvalue;
    }

    public String translatePartial(String textid) {
        if (this.pageContext != null) {
            RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
            this.language = requestContext.getPropertyList().getProperty("language");
        }
        return this.translatePartial(textid, this.language);
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    private String buildShowTranslationsText(String inputString, boolean isMissing, String texttype, String languageid) {
        if ("english".equals(inputString) || inputString == null || inputString.trim().length() == 0 || inputString.startsWith("$G{")) {
            return inputString;
        }
        if ("rtl".equalsIgnoreCase(languageid)) {
            return "\ufb22" + inputString + " \ufb1d";
        }
        if (isMissing) {
            return "$" + inputString + "$(" + texttype + ")";
        }
        return inputString.substring(0, 1) + inputString.replaceAll("[^\\s]", "_") + (inputString.length() < 5 ? "____" : "") + "(" + texttype + ")";
    }
}

