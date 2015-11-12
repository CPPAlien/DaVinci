package cn.hadcn.davinci.upload.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;


public class UploadRequest<T> extends Request<T> {
    private static final String FILE_PART_NAME = "file";
    private static final String BOUNDARY = "----xxxxxxx";
    private static final String CHARSET = "utf-8";
    private MultipartEntity mEntity;
    private final Response.Listener<T> mListener;
    protected Map<String, String> headers;

    public UploadRequest(String url, File file, Listener<T> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;

        mEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY, Charset.forName(CHARSET));
        mEntity.addPart(FILE_PART_NAME, new FileBody(file, "application/octet-stream", CHARSET));
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
    protected Response<T> parseNetworkResponse(NetworkResponse response)
    {
        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(T response)
    {
        mListener.onResponse(response);
    }
}
