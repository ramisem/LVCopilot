/*
 * Decompiled with CFR 0.152.
 */
package sapphire.attachmenthandler;

import com.labvantage.sapphire.gwt.shared.JSONable;
import java.util.ArrayList;
import org.json.JSONArray;
import sapphire.attachmenthandler.SDILink;

public class SDILinks
extends ArrayList<SDILink>
implements JSONable {
    public JSONArray toJSONArray() {
        JSONArray jay = new JSONArray();
        for (SDILink sdi : this) {
            jay.put(sdi.toJSONObject());
        }
        return jay;
    }

    @Override
    public String toJSONString() {
        return this.toJSONArray().toString();
    }
}

