/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;

public class LockedImage {
    private String lockedImage = "";
    private boolean isLocked = false;
    private boolean isCheckedOut = false;

    public static LockedImage getLockedImage(String lockedby, String checkedoutby, String checkedoutbydept, ConnectionInfo connectionInfo, TranslationProcessor tp) {
        LockedImage lockedImage = new LockedImage();
        if (lockedby != null && lockedby.length() > 0) {
            lockedImage.lockedImage = "<a href=\"javascript:sapphire.notification.send('" + lockedby + "', '', 'Send Message to " + lockedby + "', '" + tp.translate("Enter message") + "')\"><img src=\"WEB-CORE/elements/images/locked.gif\" title=\"" + tp.translate("Locked by") + " " + lockedby + ". " + tp.translate("Click to send message") + "\"/></a>";
            lockedImage.isLocked = true;
        } else if (checkedoutby != null && checkedoutby.length() > 0 && !"{none}".equals(checkedoutby)) {
            String iconDimension = "20";
            if (checkedoutbydept != null && checkedoutbydept.length() > 0 && !"{none}".equals(checkedoutbydept)) {
                if (!connectionInfo.isDepartmentMember(checkedoutbydept)) {
                    lockedImage.lockedImage = "<img width=" + iconDimension + " height=" + iconDimension + " src=\"" + "WEB-CORE/images/svg/checkout_others_dept.svg" + "\" title=\"" + tp.translate("Checked out to department: ") + " " + checkedoutbydept + ". \"/>";
                    lockedImage.isLocked = true;
                } else {
                    lockedImage.lockedImage = "<img width=" + iconDimension + " height=" + iconDimension + " src=\"" + "WEB-CORE/images/svg/checkout_dept.svg" + "\" title=\"" + tp.translate("Checked out to department: ") + " " + checkedoutbydept + ". \"/></a>";
                    lockedImage.isCheckedOut = true;
                }
            } else if (!checkedoutby.equals(connectionInfo.getSysuserId())) {
                lockedImage.lockedImage = "<a href=\"javascript:sapphire.notification.send('" + checkedoutby + "', '', 'Send Message to " + checkedoutby + "', '" + tp.translate("Enter message") + "')\"><img width=" + iconDimension + " height=" + iconDimension + " src=\"" + "WEB-CORE/images/svg/checkout_others.svg" + "\" title=\"" + tp.translate("Checked out by") + " " + checkedoutby + ". " + tp.translate("Click to send message") + "\"/></a>";
                lockedImage.isLocked = true;
            } else {
                String username = connectionInfo.getSysuserName() != null && connectionInfo.getSysuserName().length() > 0 ? connectionInfo.getSysuserName() : connectionInfo.getSysuserId();
                lockedImage.lockedImage = "<img width=" + iconDimension + " height=" + iconDimension + "  src=\"" + "WEB-CORE/images/svg/checkout_user.svg" + "\" title=\"" + tp.translate("Checked out by") + " " + username + "\"/>";
                lockedImage.isCheckedOut = true;
            }
        }
        return lockedImage;
    }

    public String getLockedImage() {
        return this.lockedImage;
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public boolean isCheckedOut() {
        return this.isCheckedOut;
    }
}

