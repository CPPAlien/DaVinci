package cn.hadcn.davinci_example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import cn.hadcn.davinci.DaVinci;

public class ListActivity extends AppCompatActivity {
    private ImageAdapter mAdapter;
    private List<String> urls = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ImageAdapter();
        recyclerView.setAdapter(mAdapter);

        urls.add("http://e.hiphotos.baidu.com/image/pic/item/f2deb48f8c5494eef42db15f28f5e0fe99257e6c.jpg");
        urls.add("http://e.hiphotos.baidu.com/image/pic/item/f2deb48f8c5494eef42db15f28f5e0fe99257e6c.jpg");

        mAdapter.notifyDataSetChanged();

        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                urls.add(0, "http://g.hiphotos.baidu.com/image/pic/item/54fbb2fb43166d2219dec065442309f79152d292.jpg");
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DaVinci.with().getImageLoader().load(urls.get(position)).into(holder.ivImage);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;

            public ViewHolder(View itemView) {
                super(itemView);
                ivImage = (ImageView)itemView.findViewById(R.id.item_image);
            }
        }
    }

}
