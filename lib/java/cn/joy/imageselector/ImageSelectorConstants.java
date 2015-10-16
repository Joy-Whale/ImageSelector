package cn.joy.imageselector;

/**
 * **********************
 * Author: yu
 * Date:   2015/7/18
 * Time:   17:03
 * **********************
 */
class ImageSelectorConstants {

	/** 图片选择方式 */
	public static final String EXTRA_IMAGE_SELECT_MODE = "EXTRA_IMAGE_SELECT_MODE";
	/** 单选 */
	public static final int IMAGE_SELECT_MODE_SINGLE = 0x1;
	/** 多选 */
	public static final int IMAGE_SELECT_MODE_MULTI = 0x2;

	/** 单次最多选择数量 */
	public static final String EXTRA_IMAGE_SELECT_COUNT = "EXTRA_IMAGE_SELECT_COUNT";
	/** 单次最多选择数量:9 */
	public static final int IMAGE_SELECT_COUNT_DEFAULT = 9;

	/** 是否显示拍照 */
	public static final String EXTRA_IMAGE_SELECT_SHOW_CAMERA = "EXTRA_IMAGE_SELECT_SHOW_CAMERA";
	/** 拍照后图片保存路径 */
	public static final String EXTRA_IMAGE_SELECT_CAMERA_SAVE_PATH = "EXTRA_IMAGE_SELECT_CAMERA_SAVE_PATH";

	public static final String RESULT_IMAGE_PATH = "RESULT_IMAGE_PATH";
}
