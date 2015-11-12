package cn.hadcn.davinci.upload.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONException;
import org.json.JSONObject;


public class UploadRequest extends Request<JSONObject> {
    private static final String FILE_PART_NAME = "file";
    private static final String BOUNDARY = "----xxxxxxx";
    private static final String CHARSET = "utf-8";
    private MultipartEntity mEntity;
    private final Response.Listener<JSONObject> mListener;
    protected Map<String, String> headers;

    public UploadRequest(String url, String filePartName, File file, Listener<JSONObject> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;

        mEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, Charset.forName(CHARSET));
        if ( null == filePartName ) {
            filePartName = FILE_PART_NAME;
        }
        mEntity.addPart(filePartName, new FileBody(file, "application/octet-stream", CHARSET));
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
            VolleyLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.");
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
