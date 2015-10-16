package cn.joy.imageselector;

import android.content.Context;
import android.text.TextUtils;

/**
 * **********************
 * Author: yu
 * Date:   2015/10/16
 * Time:   10:36
 * **********************
 */
public class Constant {

	private static final String IMAGE_DIRECTORY = "/image/";

	public static String getImageCachePath(Context context){
		String imageCachePath;
		if(context.getExternalCacheDir() != null && !TextUtils.isEmpty(context.getExternalCacheDir().getAbsolutePath())){
			imageCachePath = context.getExternalCacheDir().getAbsolutePath() + IMAGE_DIRECTORY;
		}else{
			imageCachePath = context.getCacheDir().getAbsolutePath() + IMAGE_DIRECTORY;
		}
		return imageCachePath;
	}
}
