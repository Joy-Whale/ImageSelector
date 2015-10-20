package cn.joy.imageselector.crop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.edmodo.cropper.cropwindow.edge.Edge;
import com.edmodo.cropper.util.ImageViewUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.joy.imageselector.Constant;
import cn.joy.imageselector.FileTools;
import cn.joy.imageselector.Logs;
import cn.joy.imageselector.R;


/**
 * **********************
 * Author: yu
 * Date:   2015/8/27
 * Time:   16:08
 * **********************
 */
public class ImageCropperActivity extends Activity {

	private static final String TAG = "ImageCropperActivity";

	public static final String EXTRA_IMAGE_PATH = "EXTRA_IMAGE_PATH";
	public static final String EXTRA_IMAGE_CROP_SHAPE = "EXTRA_IMAGE_CROP_SHAPE";

	/** Crop BroadCase action */
	public static final String ACTION_IMAGE_CROPPER = "ACTION_IMAGE_CROPPER";

	private static final int IMAGE_MAX_SIZE = 1024;

	CropPhotoView photoView;

	CropOverlayView overlayView;

	private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;
	/** the path of image to crop */
	private String imagePath;
	/** the path of image to save which be crop */
	private String imageSavePath;
	private float minScale = 1f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_cropper_activity_main);
		imagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
		imageSavePath = Constant.getImageCachePath(this) + System.currentTimeMillis() +  ".jpg";
		initView();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH);
		photoView.postDelayed(displayRunnable, 1);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		doCancel();
	}

	void initView() {
		findViewById(R.id.image_cropper_save).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doSave();
			}
		});
		findViewById(R.id.image_cropper_discard).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doCancel();
			}
		});
		photoView = (CropPhotoView) findViewById(R.id.image_cropper_image);
		overlayView = (CropOverlayView) findViewById(R.id.image_cropper_overlay);
		overlayView.setOverlayShape(getIntent().getIntExtra(EXTRA_IMAGE_CROP_SHAPE, CropOverlayView.OVERLAY_SHAPE_SQUARE));
		photoView.addListener(new CropPhotoViewAttacher.IGetImageBounds() {
			@Override
			public Rect getImageBounds() {
				return new Rect((int) Edge.LEFT.getCoordinate(), (int) Edge.TOP.getCoordinate(), (int) Edge.RIGHT.getCoordinate(), (int) Edge.BOTTOM.getCoordinate());
			}
		});
		photoView.postDelayed(displayRunnable, 1);
	}

	Runnable displayRunnable = new Runnable() {
		@Override
		public void run() {
			Bitmap b = getBitmap(imagePath);
			Drawable bitmap = new BitmapDrawable(getResources(), b);
			int h = bitmap.getIntrinsicHeight();
			int w = bitmap.getIntrinsicWidth();
			final float cropWindowWidth = overlayView.getOverlayWidth();
			final float cropWindowHeight = overlayView.getOverlayHeight();
			if (h <= w) {
				minScale = (cropWindowHeight + 1f) / h;
			} else if (w < h) {
				minScale = (cropWindowWidth + 1f) / w;
			}

			photoView.setMaximumScale(minScale * 3);
			photoView.setMediumScale(minScale * 2);
			photoView.setMinimumScale(minScale);
			photoView.setImageDrawable(bitmap);
			photoView.setScale(minScale);
		}
	};

	/**
	 * crop and save image
	 */
	void doSave() {
		if (savedCropperImage()) {
			Intent intent = new Intent();
			intent.putExtra(EXTRA_IMAGE_PATH, imageSavePath);
			setResult(RESULT_OK, intent);
			intent.setAction(ACTION_IMAGE_CROPPER);
			sendBroadcast(intent);
			Logs.e(TAG, "doSave cropImageSuccess");
		} else {
			setResult(RESULT_CANCELED);
			Logs.e(TAG, "doSave cropImageFailed");
		}
		finish();
	}

	void doCancel() {
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * save cropper image
	 */
	boolean savedCropperImage() {
		Bitmap croppedImage = getCroppedImage();
		OutputStream outputStream;
		try {
			FileTools.createNewFile(new File(imageSavePath));
			outputStream = new FileOutputStream(imageSavePath);
			croppedImage.compress(mOutputFormat, 90, outputStream);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Bitmap getCroppedImage() {

		Bitmap mCurrentDisplayedBitmap = getCurrentDisplayedImage();
		Rect displayedImageRect = ImageViewUtil.getBitmapRectCenterInside(mCurrentDisplayedBitmap, photoView);

		// Get the scale factor between the actual Bitmap dimensions and the
		// displayed dimensions for width.
		float actualImageWidth = mCurrentDisplayedBitmap.getWidth();
		float displayedImageWidth = displayedImageRect.width();
		float scaleFactorWidth = actualImageWidth / displayedImageWidth;

		// Get the scale factor between the actual Bitmap dimensions and the
		// displayed dimensions for height.
		float actualImageHeight = mCurrentDisplayedBitmap.getHeight();
		float displayedImageHeight = displayedImageRect.height();
		float scaleFactorHeight = actualImageHeight / displayedImageHeight;

		// Get crop window position relative to the displayed image.
		float cropWindowX = Edge.LEFT.getCoordinate() - displayedImageRect.left;
		float cropWindowY = Edge.TOP.getCoordinate() - displayedImageRect.top;
		float cropWindowWidth = Edge.getWidth();
		float cropWindowHeight = Edge.getHeight();

		// Scale the crop window position to the actual size of the Bitmap.
		float actualCropX = cropWindowX * scaleFactorWidth;
		float actualCropY = cropWindowY * scaleFactorHeight;
		float actualCropWidth = cropWindowWidth * scaleFactorWidth;
		float actualCropHeight = cropWindowHeight * scaleFactorHeight;

		// Crop the subset from the original Bitmap.
		return Bitmap.createBitmap(mCurrentDisplayedBitmap, (int) actualCropX, (int) actualCropY, (int) actualCropWidth, (int) actualCropHeight);
	}

	Bitmap getBitmap(String imagePath) {
		Bitmap returnedBitmap;
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, o);
		int scale = 1;
		if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
			scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
		}

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		Bitmap bitmap = BitmapFactory.decodeFile(imagePath, o2);

		//First check
		ExifInterface ei;
		try {
			ei = new ExifInterface(imagePath);
			int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					returnedBitmap = rotateImage(bitmap, 90);
					//Free up the memory
					bitmap.recycle();
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					returnedBitmap = rotateImage(bitmap, 180);
					//Free up the memory
					bitmap.recycle();
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					returnedBitmap = rotateImage(bitmap, 270);
					//Free up the memory
					bitmap.recycle();
					break;
				default:
					returnedBitmap = bitmap;
			}
			return returnedBitmap;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Bitmap rotateImage(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	/**
	 * get image bitmap which is displaying
	 * @return Bitmap of image
	 */
	private Bitmap getCurrentDisplayedImage() {
		Bitmap result = Bitmap.createBitmap(photoView.getWidth(), photoView.getHeight(), Bitmap.Config.RGB_565);
		Canvas c = new Canvas(result);
		photoView.draw(c);
		return result;
	}
}
