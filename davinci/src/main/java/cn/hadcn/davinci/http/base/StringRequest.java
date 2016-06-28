package cn.hadcn.davinci.http.base;


import java.io.UnsupportedEncodingException;
import java.util.Map;

import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.volley.AuthFailureError;
import cn.hadcn.davinci.volley.NetworkResponse;
import cn.hadcn.davinci.volley.ParseError;
import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.Response.*;
import cn.hadcn.davinci.volley.toolbox.HttpHeaderParser;

/**
 * A request for retrieving a T type response body at a given URL that also
 * optionally sends along a JSON body in the request specified.
 */
public abstract class StringRequest extends Request<String> {
    protected static final String PROTOCOL_CHARSET = "utf-8";

    private final Listener<String> mListener;
    private final String mRequestBody;


    public StringRequest(int method, String url, String requestBody, Listener<String> listener,
                         ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
        mRequestBody = requestBody;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    /**
     * @deprecated Use {@link #getBodyContentType()}.
     */
    @Override
    public String getPostBodyContentType() {
        return getBodyContentType();
    }

    /**
     * @deprecated Use {@link #getBody()}.
     */
    @Override
    public byte[] getPostBody() {
        return getBody();
    }

    @Override
    public abstract String getBodyContentType();

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VinciLog.e("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

            return Response.success(jsonString,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    abstract public Map<String, String> getHeaders() throws AuthFailureError;
}
