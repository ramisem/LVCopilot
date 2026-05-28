/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

public class ErrorMsg {
    public static String EU001() {
        return "ERROR( EU001 ): Neither JobType nor SDC is defined for the Job.";
    }

    public static String EU002(String sdcid) {
        return "ERROR( EU002 ): Items from SDC '" + sdcid + "' can not be added.";
    }

    public static String EU003(String jobtypeid) {
        return "ERROR( EU003 ): ItemSDC not defined in JobType " + jobtypeid;
    }

    public static String EU004(String sdcid) {
        return "ERROR( EU004 ): Item SDC '" + sdcid + "' defined in Job Type does not exists.";
    }

    public static String EU005(String sdcid, String jobtypeid) {
        StringBuffer sb = new StringBuffer();
        sb.append("ERROR( EU005 ): The Item SDC '").append(sdcid).append("' in Job Type '").append(jobtypeid).append("' is a multi key SDC. Multi key SDCs can not be added to a Job.");
        return sb.toString();
    }

    public static String EU006(String sdcid) {
        StringBuffer sb = new StringBuffer();
        sb.append("ERROR( EU006 ): The SDC '").append(sdcid).append("' is a multi key SDC. Multi key SDIs can not be added to a Job.");
        return sb.toString();
    }

    public static String EU007(String sdcid) {
        return "ERROR( EU007 ): SDC '" + sdcid + "' does not exists.";
    }

    public static String EU008() {
        return "ERROR( EU008 ): Property missing: type";
    }

    public static String EU009(String elementid) {
        return "No element data found for the " + elementid + " element tag.";
    }

    public static String EU010(String mode) {
        return "Element or Page mode is not valid: " + mode;
    }
}

