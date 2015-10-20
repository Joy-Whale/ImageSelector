# ImageSelector
安卓图片选择器


##功能
1.是否显示相机<br>
2.选择图片的数量（单张、其他）<br>
3.是否需要裁剪（只有图片数量模式选择为单张的时候此功能才会生效）<br>
4.裁剪边框的形状（目前有正方形、圆形以供选择）<br>


![ImageSelector](https://github.com/Joy-Whale/ImageSelector/raw/master/sample/assets/001.gif)
![ImageSelector](https://github.com/Joy-Whale/ImageSelector/raw/master/sample/assets/002.gif)


## 使用

### gradle
  ~~~xml
  compile{
     'cn.joy:imageselector:1.0.1'
  }
  ~~~

### xml
  添加以下代码到AndroidManifest.xml中：
  ~~~xml
    <activity android:name="cn.joy.imageselector.ImageSelectorActivity"/><br>
    <activity android:name="cn.joy.imageselector.crop.ImageCropperActivity"/>
  ~~~


### java
  Example:

  ~~~java
  Intent intent = new Intent(this, ImageSelectorActivity.class);
  intent.putExtra(ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_MODE, ImageSelectorActivity.IMAGE_SELECTOR_MODE_SINGLE_CROP);
  intent.putExtra(ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_SHOW_CAMERA, true);
  intent.putExtra(ImageSelectorActivity.EXTRA_IMAGE_SELECTOR_CROP_SHAPE,ImageSelectorActivityIMAGE_SELECTOR_CROP_SHAPE_CIRCL;
  startActivityForResult(intent, REQUEST_CODE);

  // 1.通过onActivityResult来获取选择的图片地址
  onActivityResult(int requestCode, int resultCode, Intent data){
     if (requestCode == REQUEST_CODE&&resultCode==RESULT_OK&&data.hasExtra(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH){
        // ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH  intent中的该字段返回的为获取的图片地址
        // 如果图像选择方式为单张,则返回的为字符串,如果为多选模式,返回的为ArrayList<String>
        // 接下来可以做其他操作
     }
  }

  // 2.通过监听广播来获取选择的图片
  // 需要注册ImageSelectorActivity.ACTION_IMAGE_SELECTOR 来监听图片选择后返回的广播,
  onReceiver(Context context, Intent intent){
     if(intent.hasExtra(ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH)){
        // ImageSelectorActivity.RESULT_IMAGE_SELECTED_PATH中存放有返回的图片地址,用法同onActivityResult()
     }
  }
  ~~~
