package cn.joy.imageselector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class ImageTools {

	private static final String TAG = "ImageTools";
	private static final int MAX_SIZE = 300 * 1024;
	private static final int MAX_WIDTH = 1080;
	private static final int MAX_HEIGHT = 1920;

	public static String compressBitmapAsFile(Bitmap bmp, String savePath, int targetWidth, int targetHeight) {
		return saveBitmapCompress(savePath, getImageThumbnail(bmp, targetWidth, targetHeight));
	}

	public static String compressBitmapAsFile(Bitmap bmp, String savePath) {
		return compressBitmapAsFile(bmp, savePath, MAX_WIDTH, MAX_HEIGHT);
	}

	/**
	 * 将图片压缩到固定尺寸
	 * @param bmp          图片
	 * @param targetWidth  目标宽度
	 * @param targetHeight 目标高度
	 * @return 图片int数据
	 */
	private static byte[] getImageThumbnail(Bitmap bmp, int targetWidth, int targetHeight) {
		int newWidth = targetWidth > bmp.getWidth() ? bmp.getWidth() : targetWidth;
		int newHeight = targetHeight > bmp.getHeight() ? bmp.getHeight() : targetHeight;
		float scaleWidth = ((float) newWidth) / bmp.getWidth();
		float scaleHeight = ((float) newHeight) / bmp.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap image = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		bmp.recycle();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		int q = 100;
		while (baos.toByteArray().length > MAX_SIZE) {
			baos.reset();
			image.compress(Bitmap.CompressFormat.JPEG, q, baos);
			q -= 10;
		}
		image.recycle();
		return (baos.toByteArray());
	}

	/**
	 * 保存图片byte数据到本地
	 * @param savePath 需要保存的地址
	 * @param image    需要保存的图片数据
	 */
	private static String saveBitmapCompress(String savePath, byte[] image) {
		try {
			FileOutputStream fos = new FileOutputStream(savePath);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(image);
			bos.flush();
			fos.getFD().sync();
			bos.close();
			Logs.i(TAG, "saveBitmap 成功");
			return savePath;

		} catch (IOException e) {
			Logs.i(TAG, "saveBitmap:失败" + e.getMessage());
		}
		return null;
	}

	/**
	 * 获取图片的旋转角度
	 * @param filepath filepath
	 */
	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException ex) {
			ex.printStackTrace();
			Logs.d(TAG, "cannot read exif" + ex);
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_90:
						degree = 90;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						degree = 180;
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						degree = 270;
						break;
				}
			}
		}
		return degree;
	}

	/**
	 * 旋转图片，使图片保持正确的方向。
	 * @param bitmap  原始图片
	 * @param degrees 原始图片的角度
	 * @return Bitmap 旋转后的图片
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
		if (degrees == 0 || null == bitmap) {
			return bitmap;
		}
		Matrix matrix = new Matrix();
		matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		bitmap.recycle();
		return bmp;
	}

	/**
	 * 根据图片地址,获取图片当前旋转角度,纠正并保存
	 * @param filePath 图片地址
	 */
	public static void rotateImage(String filePath) {
		int degrees = getExifOrientation(filePath);
		if (degrees == 0)
			return;
		compressBitmapAsFile(rotateBitmap(BitmapFactory.decodeFile(filePath), degrees), filePath);
	}
}
