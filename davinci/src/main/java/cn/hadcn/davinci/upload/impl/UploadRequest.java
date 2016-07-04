package cn.hadcn.davinci.upload.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;

import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.volley.AuthFailureError;
import cn.hadcn.davinci.volley.NetworkResponse;
import cn.hadcn.davinci.volley.ParseError;
import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.Response;
import cn.hadcn.davinci.volley.Response.*;
import cn.hadcn.davinci.volley.toolbox.HttpHeaderParser;


public class UploadRequest extends Request<JSONObject> {
    private static final String FILE_PART_NAME = "file";
    private static final String BOUNDARY = "----WebKitFormBoundarysU2wZJMAVKl3MW6Q";
    private static final String CHARSET = "utf-8";
    private MultipartEntity mEntity;
    private final Response.Listener<JSONObject> mListener;
    protected Map<String, String> headers;

    public UploadRequest(String url, Listener<JSONObject> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;

        mEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, Charset.forName(CHARSET));
    }

    public void addFile(String filePartName, File file) {
        if ( null == filePartName ) {
            filePartName = FILE_PART_NAME;
        }
        mEntity.addPart(filePartName, new FileBody(file, "application/octet-stream", CHARSET));
    }

    public void addExtra(String name, final JSONObject object) {
        AbstractContentBody body = new AbstractContentBody("application/json") {
            @Override
            public String getFilename() {
                return null;
            }

            @Override
            public void writeTo(OutputStream outputStream) throws IOException {
                outputStream.write(object.toString().getBytes());
            }

            @Override
            public String getCharset() {
                return CHARSET;
            }

            @Override
            public String getTransferEncoding() {
                return "binary";
            }

            @Override
            public long getContentLength() {
                return object.length();
            }
        };
        VinciLog.d(name + ":" + object.toString());
        mEntity.addPart(name, body);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }

        headers.put("Accept", "application/json");

        return headers;
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public String getBodyContentType()
    {
        return "multipart/form-data; boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() throws AuthFailureError
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            mEntity.writeTo(bos);
        }
        catch (IOException e)
        {
            VinciLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.", e);
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response)
    {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, CHARSET));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response)
    {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
}
