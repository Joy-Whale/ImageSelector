package cn.joy.imageselector;

import android.net.Uri;

/**
 * **********************
 * Author: yu
 * Date:   2015/7/18
 * Time:   17:49
 * **********************
 */
 class Image {

	private int id;
	private String name;
	private Uri uri;
	private String path;
	private long date;
	private boolean isCamera = false;
	private boolean isSelected = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


	public boolean isCamera() {
		return isCamera;
	}

	public void setIsCamera(boolean isCamera) {
		this.isCamera = isCamera;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof Image){
			return ((Image) o).id == id;
		}
		return super.equals(o);
	}
}
