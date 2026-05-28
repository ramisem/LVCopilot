/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.formulation;

import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class PromoteFormulation
extends BaseAction
implements sapphire.action.PromoteFormulation {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54344 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String newProductId = properties.getProperty("promotedproductid", "");
        String parentProductId = properties.getProperty("parentproductid", "");
        String parentProductVersionId = properties.getProperty("parentproductversionid", "");
        this.validate(properties);
        String maxSql = this.connectionInfo.isOracle() ? " nvl( max( to_number( s_productversionid ) ), 0 )" : " isnull( max( cast( s_productversionid AS Integer ) ), 0 )";
        String sql = "SELECT " + maxSql + " FROM " + "s_product" + " WHERE " + "s_productid" + " = ?";
        int nextVersion = this.database.getPreparedCount(sql, new Object[]{newProductId});
        PropertyList props = new PropertyList(properties);
        props.setProperty("sdcid", "Product");
        props.setProperty("keyid1", newProductId);
        props.setProperty("keyid2", String.valueOf(nextVersion + 1));
        props.setProperty("templatekeyid1", parentProductId);
        props.setProperty("templatekeyid2", parentProductVersionId);
        props.setProperty("overrideautokey", "Y");
        props.setProperty("usetemplatedepartment", "Y");
        props.setProperty("copyattachment", "Y");
        props.setProperty("copyattachmentmode", "E");
        props.setProperty("usersequence", "(null)");
        props.setProperty("parentproductid", parentProductId);
        props.setProperty("parentproductversionid", parentProductVersionId);
        props.setProperty("productmodeflag", "S");
        props.setProperty("operation", "promoteformulation");
        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
        properties.setProperty("newproductid", props.getProperty("newkeyid1"));
        properties.setProperty("newproductversionid", props.getProperty("newkeyid2"));
        if (properties.getProperty("changeformulationstatusflag", "N").equals("Y")) {
            String status = properties.getProperty("newparentformulationstatus", "Promoted");
            props = new PropertyList();
            props.setProperty("sdcid", "Product");
            props.setProperty("keyid1", parentProductId);
            props.setProperty("keyid2", parentProductVersionId);
            props.setProperty("formulationstatus", status);
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }

    private void validate(PropertyList properties) throws SapphireException {
        String newProductId = properties.getProperty("promotedproductid", "");
        String parentProductId = properties.getProperty("parentproductid", "");
        String parentProductVersionId = properties.getProperty("parentproductversionid", "");
        if (newProductId.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Mandatory new promoted-product id is missing in action properties"));
        }
        if (parentProductId.length() == 0 || parentProductVersionId.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Mandatory parent-product/version is missing in action properties"));
        }
    }
}

