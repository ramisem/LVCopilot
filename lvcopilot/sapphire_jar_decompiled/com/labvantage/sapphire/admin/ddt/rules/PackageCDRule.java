/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.sapphire.admin.ddt.Department;
import com.labvantage.sapphire.admin.ddt.LV_Package;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.StringUtil;

public class PackageCDRule
extends BaseRule {
    protected String LABVANTAGE_CVS_ID = "$Revision: 84852 $";

    public PackageCDRule() {
    }

    public PackageCDRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public String processRule(String packageids) throws SapphireException {
        if (!this.connectionInfo.hasModule("ASL")) {
            return "";
        }
        if (packageids != null && packageids.length() > 0) {
            String[] arrPackageIds = StringUtil.split(packageids, ";");
            for (int cnt = 0; cnt < arrPackageIds.length; ++cnt) {
                String packageid = arrPackageIds[cnt];
                String source = LV_Package.getSenderDepartmentId(this.database, packageid);
                String destination = LV_Package.getRecipientDepartmentId(this.database, packageid);
                boolean sourceIsCD = Department.isCustodialDomain(this.database, source);
                boolean destinationIsCD = Department.isCustodialDomain(this.database, destination);
                String type = LV_Package.getPackageType(this.getQueryProcessor(), packageid);
                if ("CDT".equals(type)) {
                    if (!sourceIsCD) {
                        throw new SapphireException("Unable to create a CDT. User does not have a default Custodial Department.");
                    }
                    if (!destinationIsCD) {
                        throw new SapphireException("Unable to create a CDT. The destination is not a Custodial Department.");
                    }
                } else if ("PKG".equals(type) && !sourceIsCD && !destinationIsCD) {
                    throw new SapphireException("Unable to create Package. Either source or destination of a Package must be a Custodial Department.");
                }
                if (!LV_Package.hasTILSample(this.getQueryProcessor(), packageid)) continue;
                boolean destinationIsRepository = Department.isRepository(this.database, destination);
                if (destinationIsCD || destinationIsRepository) continue;
                throw new SapphireException("Unable to create Package. The package has a TIL sample, so the destination must be a repository.");
            }
        }
        return "";
    }
}

