/*
 * Decompiled with CFR 0.152.
 */
package sapphire.xml.cmt;

import java.util.List;
import org.json.JSONObject;
import sapphire.util.SDIData;
import sapphire.xml.cmt.Snapshot;

public interface SnapshotItem {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    public String getSDCId();

    public String getKeyId1();

    public String getKeyId2();

    public String getKeyId3();

    public String getPolicyNodeId();

    public Snapshot getSnapshot();

    public boolean equals(Object var1);

    public String toString();

    public JSONObject toJSONObject();

    public Snapshot.Type getType();

    public SDIData getSDIData();

    public List<SnapshotItem> getLinkItems();

    public List<SnapshotItem> getLinkItemsByType(LinkType var1);

    public List<String> getLinkIdsByType(LinkType var1);

    public List<SnapshotItem> getLinkItemsByLinkId(LinkType var1, String var2);

    public boolean isIncludedForTransfer();

    public static enum LinkType {
        FK("FK"),
        REVFK("RFK"),
        M2M("M2M"),
        REVSOFTLINK("RSL"),
        SQL("SQL");

        private final String code;

        private LinkType(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

        public static LinkType getByCode(String code) {
            for (LinkType link : LinkType.values()) {
                if (!link.getCode().equals(code)) continue;
                return link;
            }
            return null;
        }
    }
}

