/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.shared.util;

import java.util.HashMap;

public class ModernImages {
    public static final String ADD = "WEB-CORE/imageref/flat/16/flat_green_plus1.svg";
    public static final String EDIT = "WEB-CORE/imageref/flat/16/flat_blue_pencil_draw.svg";
    public static final String DELETE = "WEB-CORE/imageref/flat/16/flat_red_trash.svg";
    public static final String VIEW = "WEB-CORE/imageref/flat/16/flat_black_eye1.svg";
    public static final String COPY = "";
    public static final String MOVE_UP = "WEB-CORE/imageref/flat/16/flat_blue_arrow3_up.svg";
    public static final String MOVE_DOWN = "WEB-CORE/imageref/flat/16/flat_blue_arrow3_down.svg";
    public static final String SAVE = "";
    private static HashMap<String, String> mapping = new HashMap();

    public static String getMappedImage(String originalImage) {
        return mapping.get(originalImage) != null ? mapping.get(originalImage) : originalImage;
    }

    static {
        mapping.put("WEB-CORE/images/png/Add.png", ADD);
        mapping.put("WEB-CORE/images/png32/Add.png", ADD);
        mapping.put("WEB-CORE/images/png/Edit.png", EDIT);
        mapping.put("WEB-CORE/images/png32/Edit.png", EDIT);
        mapping.put("WEB-CORE/images/png/Delete.png", DELETE);
        mapping.put("WEB-CORE/images/png32/Delete.png", DELETE);
        mapping.put("WEB-CORE/images/png/Copy.png", "");
        mapping.put("WEB-CORE/images/png/Copy32.png", "");
        mapping.put("WEB-CORE/images/png/Save.png", "WEB-CORE/images/png/Save.png");
        mapping.put("WEB-CORE/images/png32/Save.png", "WEB-CORE/images/png32/Save.png");
        mapping.put("WEB-CORE/images/gif/MoveUp.gif", MOVE_UP);
        mapping.put("WEB-CORE/images/gif/MoveDown.gif", MOVE_DOWN);
        mapping.put("WEB-CORE/images/png/View.png", VIEW);
        mapping.put("WEB-CORE/images/png32/View.png", VIEW);
        mapping.put("", "");
        mapping.put("WEB-CORE/images/png/DataEntry.png", "");
        mapping.put("WEB-CORE/images/png32/DataEntry.png", "");
        mapping.put("", "");
    }
}

