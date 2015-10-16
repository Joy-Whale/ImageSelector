package cn.joy.imageselector.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.ArrayList;

import cn.joy.imageselector.ImageSelectorActivity;
import cn.joy.imageselector.Logs;

public class MainActivity extends Activity {

	private static final int REQUEST_CODE = 10086;

	RadioGroup radioCamera, radioCrop, radioCount, radioShape;
	EditText inputCount;

	int selectMode = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
	}

	void initView() {
		radioCamera = (RadioGroup) findViewById(R.id.radio_camera);
		radioCrop = (RadioGroup) findViewById(R.id.radio_crop);
		radioCount = (RadioGroup) findViewById(R.id.radio_num);
		radioShape = (RadioGroup) findViewById(R.id.radio_shape);
		inputCount = (EditText) findViewById(R.id.input_num);

		findViewById(R.id.btn_go).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				select();
			}
		});
	}

	void select() {
		Intent intent = new Intent(this, ImageSelectorActivity.class);
		/** selectMode see{@link ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_MODE}  **/
		intent.putExtra(ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_MODE, selectMode = radioCount.getCheckedRadioButtonId() == android.R.id.button1 ? (radioCrop.getCheckedRadioButtonId() == android.R.id.button1 ? ImageSelectorActivity.IMAGE_SELECTOR_MODE_SINGLE_CROP : ImageSelectorActivity.IMAGE_SELECTOR_MODE_SINGLE) : ImageSelectorActivity.IMAGE_SELECTOR_MODE_MULTI);
		intent.putExtra(ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_SHOW_CAMERA, radioCamera.getCheckedRadioButtonId() == android.R.id.button1);
		if (selectMode == ImageSelectorActivity.IMAGE_SELECTOR_MODE_MULTI && !TextUtils.isEmpty(inputCount.getText())) {
			intent.putExtra(ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_COUNT, Integer.parseInt(inputCount.getText().toString()));
		}
		intent.putExtra(ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_CROP_SHAPE, radioShape.getCheckedRadioButtonId() == android.R.id.button1 ? 1 : 2);
		startActivityForResult(intent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data.hasExtra(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH)) {
			ArrayList<String> images = new ArrayList<>();
			if (selectMode != ImageSelectorActivity.IMAGE_SELECTOR_MODE_MULTI) {
				images.add(data.getStringExtra(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH));
			} else {
				images.addAll(data.getStringArrayListExtra(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH));
			}
			Logs.e("MainActivity", "onActivityResult " + data.getExtras().get(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH).toString());
			Intent intent = new Intent(MainActivity.this, ImagesActivity.class);
			intent.putStringArrayListExtra(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH, images);
			startActivity(intent);
		}
	}
}
