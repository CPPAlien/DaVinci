package cn.hadcn.davinci_example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.hadcn.davinci.DaVinci;
import cn.hadcn.davinci.http.OnRequestListener;

public class MainActivity extends AppCompatActivity implements OnRequestListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Map<String, Object> map = new HashMap<>();
        map.put("q", "Beijing,cn");
        map.put("appid", "2de143494c0b295cca9337e1e96b00e0");
        DaVinci.with(this).getHttpRequest().doGet("http://api.openweathermap.org/data/2.5/weather", map, this);

        ImageView imageView = (ImageView)findViewById(R.id.image_view);
        DaVinci.with(this).getImageLoader().load(imageView, "https://cdn-images-1.medium.com/max/800/1*dWGwx6UUjc0tocYzFNBLEw.jpeg");
    }

    @Override
    public void onSuccess(JSONObject jsonObject) {
        Log.i("DaVinciTest", jsonObject.toString());
    }

    @Override
    public void onFailed(String errorInfo) {

    }
}
