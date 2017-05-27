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
	 * Compress the image to a fixed size
	 * @param bmp          the bitmap to compress
	 * @param targetWidth  target width
	 * @param targetHeight target height
	 * @return byte of bitmap which be compressed
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
	 * save bytes as a file
	 * @param savePath path
	 * @param image    bytes
	 */
	private static String saveBitmapCompress(String savePath, byte[] image) {
		try {
			FileOutputStream fos = new FileOutputStream(savePath);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(image);
			bos.flush();
			fos.getFD().sync();
			bos.close();
			Logs.i(TAG, "saveBitmap success");
			return savePath;

		} catch (IOException e) {
			Logs.i(TAG, "saveBitmap failure" + e.getMessage());
		}
		return null;
	}

	/**
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
	 * rotate bitmap,Keep the bitmap in the right direction.
	 * @param bitmap  be rotate bitmap
	 * @param degrees current degrees of bitmap
	 * @return Bitmap the bitmap that be rotated
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
	 * According to the picture address, get the picture of the
	 * current rotation angle, correct and save
	 * @param filePath path
	 */
	public static void rotateImage(String filePath) {
		int degrees = getExifOrientation(filePath);
		if (degrees == 0)
			return;
		compressBitmapAsFile(rotateBitmap(BitmapFactory.decodeFile(filePath), degrees), filePath);
	}
}
