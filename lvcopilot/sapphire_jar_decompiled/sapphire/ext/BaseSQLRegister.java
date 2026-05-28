/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

public abstract class BaseSQLRegister {
    private String dbms;

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public String getDbms() {
        return this.dbms;
    }

    public abstract String getSQLStatement(int var1);
}

