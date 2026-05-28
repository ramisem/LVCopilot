/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import java.util.ArrayList;
import java.util.List;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class DataUtil {
    public static List<String> getRoles(SDIProcessor sdiProcessor) {
        return DataUtil.getRoles(sdiProcessor, "");
    }

    public static List<String> getRoles(SDIProcessor sdiProcessor, String where) {
        DataSet roles;
        ArrayList<String> rolelist = new ArrayList<String>();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Role");
        sdiRequest.setQueryFrom("role");
        sdiRequest.setQueryWhere(where);
        sdiRequest.setQueryOrderBy("roleid");
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        if (sdiData != null && (roles = sdiData.getDataset("primary")) != null) {
            for (int i = 0; i < roles.size(); ++i) {
                rolelist.add(roles.getString(i, "roleid"));
            }
        }
        return rolelist;
    }

    public static List<String> getWebPageCategories(SDIProcessor sdiProcessor) {
        return DataUtil.getCategoriesBySDC(sdiProcessor, "WebPage");
    }

    public static List<String> getCategoriesBySDC(SDIProcessor sdiProcessor, String sdcid) {
        DataSet roles;
        ArrayList<String> list = new ArrayList<String>();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Category");
        sdiRequest.setQueryFrom("category");
        sdiRequest.setQueryOrderBy("categoryid");
        sdiRequest.setQueryWhere("sdcid = '" + sdcid + "'");
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        if (sdiData != null && (roles = sdiData.getDataset("primary")) != null) {
            for (int i = 0; i < roles.size(); ++i) {
                list.add(roles.getString(i, "categoryid"));
            }
        }
        return list;
    }
}

