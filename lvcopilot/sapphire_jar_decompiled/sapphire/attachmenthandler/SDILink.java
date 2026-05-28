/*
 * Decompiled with CFR 0.152.
 */
package sapphire.attachmenthandler;

import com.labvantage.sapphire.gwt.shared.JSONable;
import org.json.JSONObject;

public class SDILink
implements JSONable {
    String sdcid;
    String keyid1;
    String keyid2;
    String keyid3;

    public SDILink(String sdcid, String keyid1, String keyid2, String keyid3) {
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2;
        this.keyid3 = keyid3;
    }

    public String getSDCId() {
        return this.sdcid;
    }

    public String getKeyId1() {
        return this.keyid1;
    }

    public String getKeyId2() {
        return this.keyid2;
    }

    public String getKeyId3() {
        return this.keyid3;
    }

    public JSONObject toJSONObject() {
        JSONObject job = new JSONObject();
        try {
            job.put("sdcid", this.sdcid);
            job.put("keyid1", this.keyid1);
            job.put("keyid2", this.keyid2);
            job.put("keyid3", this.keyid3);
        }
        catch (Exception exception) {
            // empty catch block
        }
        return job;
    }

    @Override
    public String toJSONString() {
        return this.toJSONObject().toString();
    }
}

