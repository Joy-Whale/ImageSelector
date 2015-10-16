package cn.joy.imageselector;

import java.io.File;
import java.io.IOException;

/**
 * **********************
 * Author: yu
 * Date:   2015/10/16
 * Time:   15:25
 * **********************
 */
public class FileTools {

	public static void createNewFile(File file) throws IOException {
		if (file.exists()) {
			file.delete();
		}
		file.getParentFile().mkdir();
		file.createNewFile();
	}
}
