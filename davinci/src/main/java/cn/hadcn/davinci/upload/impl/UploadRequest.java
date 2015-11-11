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
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;


public class UploadRequest<T> extends Request<T> {

    private static final String FILE_PART_NAME = "file";

    private MultipartEntity mEntity;
    private String mContentType;
    private final Response.Listener<T> mListener;
    private final File mFile;
    protected Map<String, String> headers;

    public UploadRequest(String url, File imageFile, String mimeType, Listener<T> listener, ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mFile = imageFile;

        buildMultipartEntity(mimeType);
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

    private void buildMultipartEntity(String mimeType)
    {
        if ( mimeType == null ) {
            mimeType = "application/octet-stream";
        }
        mContentType = mimeType;
        mEntity = new MultipartEntity();
        mEntity.addPart(FILE_PART_NAME, new FileBody(mFile));
        try {
            mEntity.addPart("content-type", new StringBody(mContentType, Charset.forName("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBodyContentType()
    {
        return mContentType;
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
