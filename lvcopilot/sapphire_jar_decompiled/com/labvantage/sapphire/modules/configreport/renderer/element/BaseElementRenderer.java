/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.element;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.xml.PropertyList;

public abstract class BaseElementRenderer {
    protected String folder;
    protected String applicationRoot;
    protected String connection;
    protected HashMap sdisIncluded;
    protected QueryProcessor queryProcessor;
    protected boolean hasChanged;
    protected PropertyList config;
    protected boolean frames;
    boolean diffOnly = false;
    protected TranslationProcessor translationProcessor;

    public void initialize(PropertyList config, HashMap sdisIncluded) throws SapphireException {
        this.sdisIncluded = sdisIncluded;
        this.connection = config.getProperty("connection");
        if (this.connection == null || this.connection.length() == 0) {
            throw new SapphireException("connection missing in config");
        }
        this.folder = config.getProperty("folder", "");
        this.applicationRoot = config.getProperty("applicationroot");
        this.queryProcessor = new QueryProcessor(this.connection);
        this.translationProcessor = new TranslationProcessor(this.connection);
        this.frames = "Y".equals(config.getProperty("frames", "Y"));
        this.config = config;
        this.diffOnly = "Y".equals(config.getProperty("diffonlyreport", "N"));
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Confirm.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Confirm.gif"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
    }

    public abstract ConfigReportContent report(String var1, PropertyList var2, PropertyList var3, PropertyDefinitionList var4, boolean var5, boolean var6, boolean var7) throws SapphireException;
}

