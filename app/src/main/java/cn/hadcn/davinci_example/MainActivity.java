package cn.hadcn.davinci_example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.hadcn.davinci.DaVinci;
import cn.hadcn.davinci.http.OnDaVinciRequestListener;
import cn.hadcn.davinci.upload.OnDaVinciUploadListener;

public class MainActivity extends AppCompatActivity implements OnDaVinciRequestListener, OnDaVinciUploadListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, Object> map = new HashMap<>();
        map.put("q", "Beijing,cn");
        map.put("appid", "2de143494c0b295cca9337e1e96b00e0");
        DaVinci.init(true, "DaVinciTest", this);
        DaVinci.with(this).enableCookie();
        DaVinci.with(this).getHttpRequest().doGet("http://api.openweathermap.org/data/2.5/weather", map, this);

        ImageView image1 = (ImageView)findViewById(R.id.image1);
        ImageView image2 = (ImageView)findViewById(R.id.image2);
        DaVinci.with(this).getImageLoader().load( "http://img.ugirls.com/uploads/cooperate/baidu/20160408jzx3.jpg").resize(400).into(image1);
        DaVinci.with(this).getImageLoader().load("http://7xlkhg.com2.z0.glb.qiniucdn.com/qbi_cry.gif").into(image2);

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DaVinci.with(MainActivity.this).getHttpRequest().doGet("http://www.baidu.com/", null, null);
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
