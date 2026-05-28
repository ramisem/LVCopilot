/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.elements.auditdetails;

import com.labvantage.opal.elements.auditdetails.AuditDetails;
import com.labvantage.opal.elements.auditdetails.AuditElementsContainer;
import com.labvantage.opal.util.ElementInfo;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.Browser;
import sapphire.util.ConnectionInfo;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AuditController {
    static final String LABVANTAGE_CVS_ID = "$Revision: 76484 $";

    public String getAuditInfo(PageContext pageContext) throws Exception {
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        QueryProcessor qp = new QueryProcessor(pageContext);
        Logger logger = new Logger(pageContext);
        ConnectionProcessor connectionProcessor = new ConnectionProcessor(pageContext);
        try {
            ConnectionInfo connectionInfo = connectionProcessor.getConnectionInfo(HttpUtil.getConnectionId(pageContext));
            PropertyList requestdata = (PropertyList)JstlUtil.evaluateExpression("${requestdata}", pageContext);
            PropertyList pagedata = requestdata.getPropertyList("pagedata");
            AuditElementsContainer elementsContainer = new AuditElementsContainer(qp, tp);
            elementsContainer.createAuditElementsInfoPool(requestdata, qp);
            AuditElementsContainer.determineLinkType(elementsContainer, pagedata, qp);
            String topElementId = elementsContainer.getTopElementId();
            ElementInfo elementInfo = elementsContainer.getElementInfo(topElementId);
            String sdcId = elementInfo.getTableSdcid();
            pagedata.setProperty("sdcid", sdcId);
            String tableid = pagedata.getProperty("tableid");
            if (tableid != null && tableid.trim().length() > 0) {
                topElementId = elementsContainer.getElementIdByTableId(tableid);
                elementInfo = elementsContainer.getElementInfo(topElementId);
            }
            AuditController.setInputProperties(pagedata);
            String dbms = requestdata.getProperty("dbms");
            AuditElementsContainer.populateChainElementAuditData(topElementId, elementsContainer, pagedata, dbms, qp, logger, connectionInfo, elementsContainer.isLazyLoad(), null);
            elementsContainer.syncAdvKeyColsType();
            AuditDetails element = elementInfo.getElement();
            element.initData(tp, connectionInfo, new Browser(pageContext));
            return element.getHtml();
        }
        catch (Exception e) {
            logger.stackTrace(e);
            throw e;
        }
    }

    public static void setInputProperties(PropertyList pagedata) {
        AuditController.getAndSetInclauseInputProps(pagedata, "sdcid", "inclausesdcid");
        AuditController.getAndSetInclauseInputProps(pagedata, "keyid1", "inclausekeyid1");
        AuditController.getAndSetInclauseInputProps(pagedata, "keyid2", "inclausekeyid2");
        AuditController.getAndSetInclauseInputProps(pagedata, "keyid3", "inclausekeyid3");
        AuditController.getAndSetInclauseInputProps(pagedata, "paramlistid", "inclauseparamlistid");
        AuditController.getAndSetInclauseInputProps(pagedata, "paramlistversionid", "inclauseparamlistversionid");
        AuditController.getAndSetInclauseInputProps(pagedata, "variantid", "inclausevariantid");
        AuditController.getAndSetInclauseInputProps(pagedata, "dataset", "inclausedataset");
        AuditController.getAndSetInclauseInputProps(pagedata, "paramid", "inclauseparamid");
        AuditController.getAndSetInclauseInputProps(pagedata, "paramtype", "inclauseparamtype");
        AuditController.getAndSetInclauseInputProps(pagedata, "replicateid", "inclausereplicateid");
    }

    private static void getAndSetInclauseInputProps(PropertyList pagedata, String propid, String inclausePropid) {
        String propValue = pagedata.getProperty(propid);
        if (propValue != null && propValue.trim().length() > 0) {
            if ((propValue = propValue.trim()).charAt(propValue.length() - 1) == ';') {
                propValue = propValue.substring(0, propValue.length() - 1);
            }
            String inclausePropValue = StringUtil.replaceAll(propValue, ";", "', '");
            inclausePropValue = "'" + inclausePropValue + "'";
            pagedata.setProperty(inclausePropid, inclausePropValue);
        }
    }

    public static PropertyList getFilteredRequestData(PropertyList requestdata) {
        PropertyList filteredProps = new PropertyList();
        String propertyTreelist = requestdata.getProperty("propertytreelist");
        filteredProps.setProperty("pagedata", requestdata.getPropertyList("pagedata"));
        filteredProps.setProperty("propertytreelist", propertyTreelist);
        filteredProps.setProperty("dbms", requestdata.getProperty("dbms"));
        filteredProps.setProperty("page", requestdata.getProperty("page"));
        return filteredProps;
    }
}

