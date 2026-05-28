/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.report.digitalsignature.api.CSRHandler;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class GenerateCSRForGoogleSigning
extends BaseAction {
    public static final String ID = "GenerateCSRForGoogleSigning";
    public static final String VERSIONID = "1";
    public static final String PRINCIPAL = "principal";
    public static final String SIGNING_PUBLIC_KEY = "signingpublickey";
    public static final String SIGNING_PROVIDER = "signingprovider";
    public static final String SIGNING_PROVIDER_NODE = "signingprovidernode";
    public static final String CSR = "csr";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String propertyTreeId = "GoogleSigningProvider";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT objectname, valuetree FROM propertytree WHERE propertytreeid = ? and propertytreetype = ?", (Object[])new String[]{propertyTreeId, "SigningProvider"}, true);
        PropertyList signingProps = new PropertyList();
        String valuetree = ds.getClob(0, "valuetree");
        PropertyDefinitionList propertyDefinitionList = null;
        try {
            propertyDefinitionList = new WebAdminProcessor(this.getConnectionid()).getPropertyDefinitionList(propertyTreeId);
        }
        catch (Exception e) {
            this.logger.error("Unable to retrieve PropertyTree definition for PropertyTree: " + propertyTreeId);
        }
        signingProps.setPropertyTree(valuetree, properties.getProperty(SIGNING_PROVIDER_NODE), propertyDefinitionList);
        LabVantageClassLoader labVantageClassLoader = null;
        String[] excludedJars = new String[]{"sapphire"};
        try {
            labVantageClassLoader = LabVantageClassLoader.getClassLoader(LabVantageClassLoader.ClassLoaderType.APPRESOURCE, "GoogleSigningProvider", signingProps.getProperty("appresourceid"), excludedJars, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        }
        catch (SapphireException e) {
            Trace.logError("Unable to load class loader");
        }
        if (labVantageClassLoader != null) {
            try {
                CSRHandler csrHandler = (CSRHandler)labVantageClassLoader.loadClass("com.labvantage.plugin.report.digitalsignature.google.GoogleCSRHandler").newInstance();
                String csr = csrHandler.generateCSR(signingProps, properties.getProperty(SIGNING_PUBLIC_KEY), properties.getProperty(PRINCIPAL));
                properties.setProperty(CSR, csr);
            }
            catch (Exception e) {
                throw new SapphireException(e.getMessage());
            }
        }
    }
}

