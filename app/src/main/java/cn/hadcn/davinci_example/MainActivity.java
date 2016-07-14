package cn.hadcn.davinci_example;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import cn.hadcn.davinci.DaVinci;
import cn.hadcn.davinci.log.LogLevel;
import cn.hadcn.davinci.http.OnDaVinciRequestListener;
import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.other.OnVinciDownloadListener;
import cn.hadcn.davinci.other.OnVinciUploadListener;

public class MainActivity extends AppCompatActivity implements OnDaVinciRequestListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        DaVinci.init(5, LogLevel.DEBUG, "DaVinciTest", this);
        DaVinci.with(this).enableCookie();

        VinciLog.e(null, "a");
        VinciLog.e("test %d, test %s", 1, "a");
        VinciLog.e("test %d, test %s", "a", "a");

        ImageView image1 = (ImageView)findViewById(R.id.image1);
        ImageView image2 = (ImageView)findViewById(R.id.image2);
        ImageView image3 = (ImageView)findViewById(R.id.image3);
        DaVinci.with(this).getImageLoader().load("http://7xlkhg.com2.z0.glb.qiniucdn.com/qbi_cry.gif").into(image1);

        Glide.with(this).load("http://7xlkhg.com2.z0.glb.qiniucdn.com/qbi_cry.gif").into(image2);
        DaVinci.with(this).getImageLoader().load("http://photo.enterdesk.com/2011-11-26/enterdesk.com-1CB20FDF5918603F9264E5BFDC4DF691.jpg").resize(400).into(image3);
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DaVinci.with(MainActivity.this).getHttpRequest().doGet("http://www.baidu.com/", null, null);
            }
        });

        Map<String, Object> map = new HashMap<>();
        map.put("q", "Beijing,cn");
        map.put("appid", "2de143494c0b295cca9337e1e96b00e0");
        DaVinci.with(this).getHttpRequest().doGet("http://api.openweathermap.org/data/2.5/weather", map, this);

        DaVinci.with(this).getHttpRequest().doGet("http://api.openweathermap.org/data/2.5/weather", map, this);

        DaVinci.with(this).getHttpRequest().doGet("http://api.openweathermap.org/data/2.5/weather", map, this);

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListActivity.class));
            }
        });

        String path = "/sdcard/Download/cc_logo.png";
        JSONObject jsonObject = new JSONObject();
        try {
            JSONObject header = new JSONObject();
            header.put("tokenId", "0e5495fb-da46-4b28-95ea-e9f6aec1d69a");
            jsonObject.put("_header_", header);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DaVinci.with(this).getUploader().extra("args", jsonObject).upload("http://192.168.3.117:12821/ecp/openapi/qs/file/upload", path, new OnVinciUploadListener() {
            @Override
            public void onVinciUploadSuccess(JSONObject response) {

            }

            @Override
            public void onVinciUploadFailed(String reason) {

            }
        });

        DaVinci.with().addThreadPool("one", 1);
        DaVinci.with().tag("one").getImageLoader().load("http://y3.ifengimg.com/fashion_spider/dci_2012/02/20a78c36cc31225b1a7efa89f566f591.jpg").resize(600).into(image3);

        OutputStream out;
        try {
            out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/download/" + "a.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        final TextView tv = (TextView)findViewById(R.id.test_text);
        DaVinci.with().getDownloader().body(jsonObject.toString()).download("http://ec2-52-192-96-229.ap-northeast-1.compute.amazonaws.com:12821/ecp/openapi/qs/file/download/p/2016/07/06/03/f5d28e3065244ab9952858f991838246.txt"
                , out, new OnVinciDownloadListener() {
                    @Override
                    public void onVinciDownloadSuccess() {

                    }

                    @Override
                    public void onVinciDownloadFailed(String reason) {

                    }

                    @Override
                    public void onVinciDownloadProgress(int progress) {
                        VinciLog.e("progress = " + progress);

                        tv.setText(String.valueOf(progress));
                    }
                });
    }

    @Override
    public void onDaVinciRequestSuccess(String jsonObject) {
        Log.i("DaVinciTest", toString());
    }

    @Override
    public void onDaVinciRequestFailed(String errorInfo) {

    }
}
