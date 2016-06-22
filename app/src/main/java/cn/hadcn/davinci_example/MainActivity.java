package cn.hadcn.davinci_example;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.hadcn.davinci.DaVinci;
import cn.hadcn.davinci.log.LogLevel;
import cn.hadcn.davinci.http.OnDaVinciRequestListener;
import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.upload.OnDaVinciUploadListener;

public class MainActivity extends AppCompatActivity implements OnDaVinciRequestListener, OnDaVinciUploadListener{

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
        DaVinci.with(this).getImageLoader().load( "http://photo.enterdesk.com/2011-11-26/enterdesk.com-1CB20FDF5918603F9264E5BFDC4DF691.jpg").resize(400).into(image3);
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

        String path = "/storage/emulated/0/360/test.png";
        DaVinci.with(this).getUploader().uploadFile("http://192.168.1.103:12521/quicksilver/openapi/web/file/upload", path, this);
    }

    @Override
    public void onDaVinciRequestSuccess(String jsonObject) {
        Log.i("DaVinciTest", toString());
    }

    @Override
    public void onDaVinciRequestFailed(String errorInfo) {

    }

    @Override
    public void onDaVinciUploadSuccess(JSONObject response) {

    }

    @Override
    public void onDaVinciUploadFailed(String reason) {

    }
}
