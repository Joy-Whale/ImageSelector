package cn.joy.imageselector.sample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.joy.imageselector.ImageSelectorActivity;

/**
 * **********************
 * Author: yu
 * Date:   2015/10/16
 * Time:   14:50
 * **********************
 */
public class ImagesActivity extends Activity {

	RecyclerView recyclerView;
	List<String> imageList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_images);
		imageList.addAll(getIntent().getStringArrayListExtra(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH));
		recyclerView = (RecyclerView) findViewById(R.id.recycler);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new GridLayoutManager(ImagesActivity.this, 3));
		recyclerView.addItemDecoration(new GridItemDecoration(getResources().getDimensionPixelOffset(R.dimen.size_5), 3));
		recyclerView.setAdapter(new ImageAdapter(imageList));
	}

	class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageHolder> {

		List<String> imageList;

		public ImageAdapter(List<String> imageList) {
			this.imageList = imageList;
		}

		@Override
		public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new ImageHolder(LayoutInflater.from(ImagesActivity.this).inflate(R.layout.li_image, parent, false));
		}

		@Override
		public void onBindViewHolder(ImageHolder holder, int position) {
			int side = (getScreenWidth() - getResources().getDimensionPixelOffset(R.dimen.size_5) * 2) / 3;
			if(holder.itemView.getLayoutParams() != null){
				holder.itemView.getLayoutParams().width = side;
				holder.itemView.getLayoutParams().height = side;
			}else {
				RecyclerView.LayoutParams params = new GridLayoutManager.LayoutParams(side, side);
				holder.itemView.setLayoutParams(params);
			}
			Glide.with(ImagesActivity.this).fromFile().asBitmap().load(new File(imageList.get(position))).into(holder.image);
		}

		@Override
		public int getItemCount() {
			return imageList.size();
		}

		class ImageHolder extends RecyclerView.ViewHolder {

			ImageView image;
			public ImageHolder(View itemView) {
				super(itemView);
				image = (ImageView) itemView.findViewById(R.id.image);
			}
		}
	}

	public int getScreenWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
}
