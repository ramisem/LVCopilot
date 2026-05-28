/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.wap.activity;

import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;

public enum AssignmentPageButtonType {
    RETURN("Return", "FlatBlackArrowLeft", "_lvtop.modernLayout.navigation.goBack()", "Return to last page", "Page", true),
    ACTIVATE("Activate", "FlatBlackPlay1", "assignment.activateWork()", "Activate selected work", "ActivityLifecycle", false),
    UNACTIVATE("Unactivate", "FlatBlackPause", "assignment.unactivateWork()", "Unactivate selected work", "ActivityLifecycle", false),
    START("Start", "FlatBlackTimerPlay", "assignment.startWork()", "Start the selected activities", "ActivityLifecycle", false),
    STOP("Stop", "FlatBlackTimerStop", "assignment.stopWork()", "Stop the selected activities", "ActivityLifecycle", false),
    EDIT("Edit", "FlatBlackEditBox", "assignment.editActivity()", "Edit selected activity", "Activity", false),
    DELETE("Delete", "FlatBlackDelete", "assignment.deleteActivity()", "Delete the selected activities", "Activity", false),
    CANCEL("Cancel", "FlatBlackStop", "assignment.cancelActivity()", "Cancel the selected activities", "ActivityLifecycle", false),
    FIXEDTIME("Toggle Fixed Start", "FlatBlackClock", "assignment.toggleFixedDate()", "Toggle the if the date is fixed for a selected activity between a floating range", "Activity", false),
    ASSIGNRESOURCE("Assign Resources", "FlatBlackPeopleCheckbox", "assignment.assignResource()", "Assign resources to selected activities", "Resources", false),
    REASSIGNRESOURCE("Reset Resource", "FlatBlackPeopleStatus", "assignment.reassignResource()", "Remove all resources and reassign resources to selected acivity", "Resources", false),
    REFRESH("", "FlatBlackRefresh1", "assignment.refreshContent()", "Refresh View", "View", true),
    CUSTOM("", "", "", "", "Custom", true);

    String tip;
    String text;
    String group;
    String image;
    String function;
    boolean showIfViewOnly;

    private AssignmentPageButtonType(String text, String image, String function, String tip, String group, boolean showIfViewOnly) {
        this.text = text;
        this.group = group;
        this.tip = tip;
        this.image = image;
        this.function = function;
        this.showIfViewOnly = showIfViewOnly;
    }

    public static PropertyList getButton(PropertyList pagebutton, TranslationProcessor translationProcessor) {
        return AssignmentPageButtonType.getButton(pagebutton, false, translationProcessor);
    }

    public static PropertyList getButton(PropertyList pagebutton, boolean viewOnly, TranslationProcessor translationProcessor) {
        PropertyList button = new PropertyList();
        PropertyList commonprops = new PropertyList();
        PropertyList userbuttonprops = new PropertyList();
        button.setProperty("id", pagebutton.getProperty("id"));
        button.setProperty("type", "User");
        AssignmentPageButtonType buttonType = CUSTOM;
        try {
            buttonType = AssignmentPageButtonType.valueOf(pagebutton.getProperty("function", "Custom").toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        button.setProperty("assignmentbuttontype", buttonType.toString());
        commonprops.setProperty("text", translationProcessor.translate(pagebutton.getProperty("text", buttonType.text)));
        String im = pagebutton.getProperty("image", buttonType.image);
        commonprops.setProperty("image", (!im.startsWith("rc?command=image&image=") ? "rc?command=image&image=" : "") + im);
        commonprops.setProperty("imagelarge", "rc?command=image&image=" + pagebutton.getProperty("image", buttonType.image));
        if (!viewOnly || buttonType.showIfViewOnly) {
            commonprops.setProperty("show", pagebutton.getProperty("show"));
        } else {
            commonprops.setProperty("show", "N");
        }
        commonprops.setProperty("ribbonstyle", "Large");
        commonprops.setProperty("appearance", "standard");
        commonprops.setProperty("group", buttonType.group);
        commonprops.setProperty("tip", translationProcessor.translate(pagebutton.getProperty("tip", buttonType.tip)));
        commonprops.setProperty("mode", "Button");
        if (buttonType == CUSTOM) {
            userbuttonprops.setProperty("action", pagebutton.getProperty("custom"));
        } else {
            userbuttonprops.setProperty("action", buttonType.function);
        }
        button.setProperty("commonprops", commonprops);
        button.setProperty("userbuttonprops", userbuttonprops);
        return button;
    }
}

