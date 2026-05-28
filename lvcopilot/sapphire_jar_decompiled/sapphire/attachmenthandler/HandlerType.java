/*
 * Decompiled with CFR 0.152.
 */
package sapphire.attachmenthandler;

public enum HandlerType {
    TALENDJOB("T"),
    HANDLERCLASS("H");

    String typeflag = "";

    private HandlerType(String flag) {
        this.typeflag = flag;
    }

    public static HandlerType getHandlerType(String flagOrName) {
        if (flagOrName.length() == 0) {
            return HANDLERCLASS;
        }
        if (flagOrName.length() == 1) {
            if (flagOrName.equalsIgnoreCase("T")) {
                return TALENDJOB;
            }
            if (flagOrName.equalsIgnoreCase("H")) {
                return HANDLERCLASS;
            }
            return HANDLERCLASS;
        }
        try {
            return HandlerType.valueOf(flagOrName.toUpperCase());
        }
        catch (Exception e) {
            return HANDLERCLASS;
        }
    }

    public String getTypeFlag() {
        return this.typeflag;
    }
}

