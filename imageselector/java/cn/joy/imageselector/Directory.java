package cn.joy.imageselector;

import java.util.List;

/**
 * **********************
 * Author: yu
 * Date:   2015/7/18
 * Time:   17:51
 * **********************
 */
 class Directory {

	private String name;
	private String path;
	private List<Image> images;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<Image> getImages() {
		return images;
	}

	public void setImages(List<Image> images) {
		this.images = images;
	}
}
