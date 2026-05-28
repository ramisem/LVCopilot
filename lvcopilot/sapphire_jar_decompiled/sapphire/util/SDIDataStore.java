/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.DDTConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.DataStore;
import sapphire.util.JsonArray;
import sapphire.util.JsonObject;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;

public class SDIDataStore
implements DDTConstants {
    public static final String KEYS = "keys";
    public static final String TYPES = "types";
    public static final String SDIDATASTORES = "sdidatastores";
    public static final String SDIDATASTORENAMES = "sdidatastorenames";
    public static final String DATASETS = "datasets";
    public static final String DATASETNAME = "datasetname";
    public static final String DATASETNAMES = "datasetnames";
    public static final String UPDATEABLE = "updateable";
    private static final String RSETID = "rsetid";
    private static final String LINKID = "linkid";
    private String sdcid;
    private String rsetid;
    private String linkid;
    private List<String> dataSetNames = new ArrayList<String>();
    private HashMap<String, DataStore> dataStores = new HashMap();
    private HashMap<String, String> keys = new HashMap();
    private HashMap<String, String> datasetType = new HashMap();
    private HashMap<String, SDIDataStore> sdiDataStores = new HashMap();

    public SDIDataStore(SDIData sdiData) {
        this.sdcid = sdiData.getSdcid();
        this.rsetid = sdiData.getRsetid();
        this.linkid = sdiData.getLinkid();
        Set datasetNames = sdiData.getDatasets();
        for (String datasetName : datasetNames) {
            DataSet ds = sdiData.getDataset(datasetName);
            this.dataSetNames.add(datasetName);
            DataStore store = new DataStore(ds);
            this.dataStores.put(datasetName, store);
            String[] keys = sdiData.getKeys(datasetName);
            StringBuffer out = new StringBuffer();
            for (int i = 0; i < keys.length; ++i) {
                String key = keys[i];
                if (key == null || key.length() <= 0) continue;
                if (out.length() > 0) {
                    out.append(";");
                }
                out.append(key);
            }
            this.keys.put(datasetName, out.toString());
        }
        Set names = sdiData.getSDIData();
        for (String name : names) {
            SDIData nested = sdiData.getSDIData(name);
            this.sdiDataStores.put(name, new SDIDataStore(nested));
        }
    }

    public SDIDataStore(JsonObject jso) throws JSONException {
        this(jso, null);
    }

    public SDIDataStore(JsonObject jso, ConnectionInfo connectionInfo) throws JSONException {
        this.sdcid = jso.getString("sdcid");
        this.rsetid = jso.getString(RSETID);
        this.linkid = jso.getString(LINKID);
        this.dataSetNames = new ArrayList<String>();
        JsonArray jsoNames = jso.getJsonArray(DATASETNAMES);
        for (int i = 0; i < jsoNames.length(); ++i) {
            this.dataSetNames.add(jsoNames.getString(i));
        }
        JsonObject jsoDataSets = jso.getJsonObject(DATASETS);
        JsonObject jsoKeys = jso.getJsonObject(KEYS);
        for (String name : this.dataSetNames) {
            this.dataStores.put(name, new DataStore(jsoDataSets.getJsonObject(name), connectionInfo));
            this.keys.put(name, jsoKeys.getString(name));
        }
        JsonArray sdiDataStoreNames = jso.getJsonArray(SDIDATASTORENAMES);
        JsonObject sdiDataStores = jso.getJsonObject(SDIDATASTORES);
        for (String name : sdiDataStoreNames.toStringArray()) {
            this.sdiDataStores.put(name, new SDIDataStore(sdiDataStores.getJsonObject(name), connectionInfo));
        }
    }

    public JsonObject toJsonObject() throws SapphireException {
        JsonObject jso = new JsonObject();
        jso.put("sdcid", this.sdcid);
        jso.put(RSETID, this.rsetid);
        jso.put(LINKID, this.linkid);
        JsonArray jsoDataSetNames = new JsonArray(this.dataSetNames);
        JsonObject jsoDataStores = new JsonObject();
        JsonObject jsoKeys = new JsonObject();
        JsonObject jsoSDIDataStores = new JsonObject();
        JsonArray jsoSDIDataStoreNames = new JsonArray();
        for (String name : this.dataSetNames) {
            jsoDataStores.put(name, this.dataStores.get(name).toJsonObject());
            jsoKeys.put(name, this.keys.get(name));
        }
        for (String name : this.sdiDataStores.keySet()) {
            jsoSDIDataStoreNames.put(name);
            jsoSDIDataStores.put(name, this.sdiDataStores.get(name).toJsonObject());
        }
        jso.put(DATASETNAMES, jsoDataSetNames);
        jso.put(DATASETS, jsoDataStores);
        jso.put(SDIDATASTORENAMES, jsoSDIDataStoreNames);
        jso.put(SDIDATASTORES, jsoSDIDataStores);
        jso.put(KEYS, jsoKeys);
        return jso;
    }

    public DataStore getDataStore(String datasetName) {
        return this.dataStores.get(datasetName);
    }

    public String[] getKeys(String datasetName) {
        return StringUtil.split(this.keys.get(datasetName), ";");
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public String getLinkid() {
        return this.linkid;
    }

    public List<String> getDataSetNames() {
        return this.dataSetNames;
    }

    public List<String> getSDIDataStoreNames() {
        return new ArrayList<String>(this.sdiDataStores.keySet());
    }

    public SDIDataStore getSDIDataStore(String name) {
        return this.sdiDataStores.get(name);
    }

    public SDIDataStore findSDIDataStore(String sdcid) {
        if (sdcid.length() == 0 || this.getSdcid().equalsIgnoreCase(sdcid)) {
            return this;
        }
        for (String sdidatastore : this.getSDIDataStoreNames()) {
            SDIDataStore sdids = this.getSDIDataStore(sdidatastore);
            if (sdids.getSdcid().equalsIgnoreCase(sdcid)) {
                return sdids;
            }
            SDIDataStore sdids2 = sdids.findSDIDataStore(sdcid);
            if (sdids2 == null) continue;
            return sdids2;
        }
        return null;
    }
}

