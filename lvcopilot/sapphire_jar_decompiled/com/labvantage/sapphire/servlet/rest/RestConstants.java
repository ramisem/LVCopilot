/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.rest;

public interface RestConstants {
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String MESSAGE = "message";
    public static final String ERROR_CONNECTION_INVALID = "Missing, invalid or timed out connectionid";
    public static final String ERROR_CONNECTION_REFUSED = "Connection refused - bad credentials";
    public static final String ERROR_INVALID_REQUEST_METHOD = "Invalid request method";
    public static final String ERROR_MALFORMED_REQUEST_URL = "Malformed request URL";
    public static final String ERROR_MALFORMED_REQUEST_BODY = "Malformed request body";
    public static final String ERROR_RESOURCE_NOT_FOUND = "Resource not found";
    public static final String ERROR_RESOURCE_NOT_AVAILABLE = "Resource not available";
    public static final String ERROR_UNEXPECTED_SERVER_ERROR = "Unexpected server error";
    public static final String NAMESPACE_API = "api";
    public static final String NAMESPACE_LABVANTAGE = "labvantage";
    public static final String NAMESPACE_CONNECTIONS = "connections";
    public static final String NAMESPACE_ACTIONS = "actions";
    public static final String NAMESPACE_SDC = "sdc";
    public static final int HTTPCODE_OK = 200;
    public static final int HTTPCODE_CREATED = 201;
    public static final int HTTPCODE_NO_CONTENT = 204;
    public static final int HTTPCODE_NOT_MODIFIED = 304;
    public static final int HTTPCODE_BAD_REQUEST = 400;
    public static final int HTTPCODE_UNAUTHORIZED = 401;
    public static final int HTTPCODE_FORBIDDEN = 403;
    public static final int HTTPCODE_NOT_FOUND = 404;
    public static final int HTTPCODE_METHOD_NOT_ALLOWED = 405;
    public static final int HTTPCODE_CONFLICT = 409;
    public static final int HTTPCODE_SERVER_ERROR = 500;
    public static final String HTTPSTATUS_OK = "OK";
    public static final String HTTPSTATUS_CREATED = "Created";
    public static final String HTTPSTATUS_NO_CONTENT = "No Content";
    public static final String HTTPSTATUS_NOT_MODIFIED = "Not Modified";
    public static final String HTTPSTATUS_BAD_REQUEST = "Bad Request";
    public static final String HTTPSTATUS_UNAUTHORIZED = "Unauthorized";
    public static final String HTTPSTATUS_FORBIDDEN = "Forbidden";
    public static final String HTTPSTATUS_NOT_FOUND = "Not Found";
    public static final String HTTPSTATUS_METHOD_NOT_ALLOWED = "Method Not Allowed";
    public static final String HTTPSTATUS_CONFLICT = "Conflict";
    public static final String HTTPSTATUS_SERVER_ERROR = "Server error";
    public static final String HTTPHEADER_LOCATION = "Location";
}

