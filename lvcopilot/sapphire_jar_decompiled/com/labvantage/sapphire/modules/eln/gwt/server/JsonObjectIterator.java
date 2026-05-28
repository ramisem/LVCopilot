/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.core.JsonFactory
 *  com.fasterxml.jackson.core.JsonParser
 *  com.fasterxml.jackson.core.JsonToken
 *  com.fasterxml.jackson.core.type.TypeReference
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class JsonObjectIterator
implements Iterator<Map<String, Object>>,
Closeable {
    private final InputStream inputStream;
    private JsonParser jsonParser;
    private boolean isInitialized;
    private Map<String, Object> nextObject;

    public JsonObjectIterator(InputStream inputStream) {
        this.inputStream = inputStream;
        this.isInitialized = false;
        this.nextObject = null;
    }

    private void init() {
        this.initJsonParser();
        this.initFirstElement();
        this.isInitialized = true;
    }

    private void initJsonParser() {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonFactory jsonFactory = objectMapper.getFactory();
        try {
            this.jsonParser = jsonFactory.createParser(this.inputStream);
        }
        catch (IOException e) {
            throw new RuntimeException("There was a problem setting up the JsonParser: " + e.getMessage(), e);
        }
    }

    private void initFirstElement() {
        try {
            JsonToken arrayStartToken = this.jsonParser.nextToken();
            if (arrayStartToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException("The first element of the Json structure was expected to be a start array token, but it was: " + arrayStartToken);
            }
            this.initNextObject();
        }
        catch (Exception e) {
            throw new RuntimeException("There was a problem initializing the first element of the Json Structure: " + e.getMessage(), e);
        }
    }

    private void initNextObject() {
        try {
            JsonToken nextToken = this.jsonParser.nextToken();
            if (nextToken == JsonToken.END_ARRAY) {
                this.nextObject = null;
                return;
            }
            if (nextToken != JsonToken.START_OBJECT) {
                throw new IllegalStateException("The next token of Json structure was expected to be a start object token, but it was: " + nextToken);
            }
            this.nextObject = (Map)this.jsonParser.readValueAs((TypeReference)new TypeReference<Map<String, Object>>(){});
            if (this.nextObject == null) {
                throw new IllegalStateException("The next parsed object of the Json structure was null");
            }
        }
        catch (Exception e) {
            throw new RuntimeException("There was a problem initializing the next Object: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasNext() {
        if (!this.isInitialized) {
            this.init();
        }
        return this.nextObject != null;
    }

    @Override
    public Map<String, Object> next() {
        if (!this.isInitialized) {
            this.init();
        }
        Map<String, Object> currentNextObject = this.nextObject;
        this.initNextObject();
        return currentNextObject;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly((Closeable)this.jsonParser);
        IOUtils.closeQuietly((InputStream)this.inputStream);
    }
}

