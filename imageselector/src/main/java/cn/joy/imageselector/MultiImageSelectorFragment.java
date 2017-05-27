package cn.joy.imageselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class MultiImageSelectorFragment extends Fragment implements OnClickListener {
	private static final String TAG = "MultiImageSelectorF";

	private static final int REQUEST_CAMERA = 1;
	/** loader type */
	private static final int LOADER_ALL = 0;
	private static final int LOADER_CATEGORY = 1;

	/** Column spacing */
	private int columnsWidth;

	private int imageSelectedMode;
	private int imageSelectedCount;
	private boolean isShowCamera;
	private String cameraSavePathBase;
	private String cameraSavePath;
	ImageSelectorCallBack callBack;
	TextView txtTitle;
	TextView btnDone;
	View btnBack;
	private List<Image> allImgs = new ArrayList<>();
	private RecyclerView imgGrid;
	private LinkedList<Directory> imageDirectories = new LinkedList<>();
	private ImagesAdapter mAdapter;
	private List<Image> selectedImages = new ArrayList<>();

	private int currentDirectory = 0;

	private LoaderCallbacks<Cursor> mLoaderCallback = new LoaderCallbacks<Cursor>() {

		private final String[] IMAGE_PROJECTION = new String[]{
				Media.DATA, Media.DISPLAY_NAME, Media.DATE_ADDED, Media._ID
		};

		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
				case LOADER_ALL:
					return new CursorLoader(getActivity(), Media.EXTERNAL_CONTENT_URI, this.IMAGE_PROJECTION, null, null, this.IMAGE_PROJECTION[2] + " DESC");
				case LOADER_CATEGORY:
					return new CursorLoader(getActivity(), Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null, IMAGE_PROJECTION[2] + " DESC");
				default:
					return null;
			}
		}


		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			allImgs.clear();
			imageDirectories.clear();

			if (isShowCamera) {
				Image camera = new Image();
				camera.setIsCamera(true);
				allImgs.add(camera);
			}

			onGetData(data, allImgs);
			mAdapter.changeData(allImgs);

			Directory directory = new Directory();
			directory.setName(getString(R.string.image_selector_all));
			directory.setPath("none");
			directory.setImages(allImgs);
			imageDirectories.addFirst(directory);
		}

		void onGetData(Cursor data, List<Image> imgSaver) {
			if (data == null || data.getCount() == 0)
				return;
			data.moveToFirst();

			do {
				/** 获取图片的ID */
				int imgId = data.getInt(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[3]));
				Image image = new Image();
				image.setPath(data.getString(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[0])));
				image.setName(data.getString(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[1])));
				image.setUri(Uri.parse(Media.EXTERNAL_CONTENT_URI.toString() + "/" + imgId));
				image.setId(imgId);
				image.setDate(data.getLong(data.getColumnIndexOrThrow(this.IMAGE_PROJECTION[2])));
				boolean exist = false;
				File f = new File(image.getPath());
				if (f.exists()) {

					File parent = f.getParentFile();
					for (Directory directory : imageDirectories) {
						if (directory.getPath().equals(parent.getAbsolutePath())) {
							directory.getImages().add(image);
							exist = true;
						}
					}

					if (!exist) {
						Directory directory = new Directory();
						directory.setName(parent.getName());
						directory.setPath(parent.getAbsolutePath());
						directory.setImages(new ArrayList<Image>());
						directory.getImages().add(image);
						imageDirectories.add(directory);
					}
					imgSaver.add(image);
				}
			} while (data.moveToNext());
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {

		}
	};

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof ImageSelectorCallBack) {
			this.callBack = (ImageSelectorCallBack) activity;
		}
	}

	@Nullable
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.image_selector_fragment, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.imageSelectedMode = this.getArguments().getInt(ImageSelectorConstants.EXTRA_IMAGE_SELECT_MODE, ImageSelectorConstants.IMAGE_SELECT_MODE_MULTI);
		this.imageSelectedCount = this.getArguments().getInt(ImageSelectorConstants.EXTRA_IMAGE_SELECT_COUNT, ImageSelectorConstants.IMAGE_SELECT_COUNT_DEFAULT);
		this.isShowCamera = this.getArguments().getBoolean(ImageSelectorConstants.EXTRA_IMAGE_SELECT_SHOW_CAMERA, true);
		this.cameraSavePathBase = this.getArguments().getString(ImageSelectorConstants.EXTRA_IMAGE_SELECT_CAMERA_SAVE_PATH);
		if (TextUtils.isEmpty(this.cameraSavePathBase)) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				this.cameraSavePathBase = Environment.getExternalStorageDirectory().getAbsolutePath();
			} else {
				this.cameraSavePathBase = Environment.getDataDirectory().getAbsolutePath();
			}
		}

		this.btnBack = view.findViewById(R.id.image_selector_back);
		this.btnDone = (TextView) view.findViewById(R.id.image_selector_btn_ok);
		this.txtTitle = (TextView) view.findViewById(R.id.image_selector_title);
		this.imgGrid = (RecyclerView) view.findViewById(R.id.image_selector_images);
		this.imgGrid.setLayoutManager(new GridLayoutManager(this.getActivity(), 3));
		this.imgGrid.setHasFixedSize(true);
		this.imgGrid.setAdapter(this.mAdapter = new ImagesAdapter(this.getActivity(), new ArrayList<Image>()));
		this.btnBack.setOnClickListener(this);
		this.txtTitle.setOnClickListener(this);
		this.btnDone.setOnClickListener(this);
		this.btnDone.setVisibility(this.isSingleMode() ? View.GONE : View.VISIBLE);
	}

	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		imgGrid.post(new Runnable() {
			@Override
			public void run() {
				columnsWidth = getActivity().getResources().getDimensionPixelOffset(R.dimen.size_5);
				mAdapter.setItemSide((int) (((float) imgGrid.getWidth() - (float) columnsWidth * 2) / 3));
				getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CAMERA && this.callBack != null) {
				this.callBack.onImagesSelected(ImageSelectSource.Camera, new ArrayList<>(Arrays.asList(new String[]{this.cameraSavePath})));
			}
		} else if (this.callBack != null) {
			this.callBack.onCancel();
		}
	}

	void showDirectories() {
		if (imageDirectories.size() <= 1) {
			return;
		}
		final ListPopupWindow popupWindow = new ListPopupWindow(this.getActivity());
		popupWindow.setBackgroundDrawable(new ColorDrawable(-1));
		popupWindow.setAdapter(new DirectoryAdapter(this.getActivity(), this.imageDirectories));
		popupWindow.setWidth(-1);
		int itemHeight = this.getActivity().getResources().getDimensionPixelOffset(R.dimen.size_60);
		popupWindow.setHeight(this.imageDirectories.size() >= 5 ? itemHeight * 5 : itemHeight * this.imageDirectories.size() + getActivity().getResources().getDimensionPixelOffset(R.dimen.size_10));
		popupWindow.setAnchorView(this.btnBack);
		popupWindow.setModal(true);
		popupWindow.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				txtTitle.setText(imageDirectories.get(position).getName());
				mAdapter.changeData(imageDirectories.get(position).getImages());
				currentDirectory = position;
				popupWindow.dismiss();
			}
		});
		popupWindow.show();
		popupWindow.getListView().setDividerHeight(getActivity().getResources().getDimensionPixelOffset(R.dimen.size_10));
		popupWindow.getListView().setDivider(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
		popupWindow.setSelection(this.currentDirectory);
	}

	boolean isSingleMode() {
		return this.imageSelectedMode == 1;
	}

	public void onClick(View v) {
		if (v.getId() == R.id.image_selector_title) {
			this.showDirectories();
		} else if (v.getId() == R.id.image_selector_back) {
			if (this.callBack == null)
				return;
			this.callBack.onCancel();
		} else if (v.getId() == R.id.image_selector_btn_ok) {
			if (this.callBack == null)
				return;
			ArrayList<String> paths = new ArrayList<>();
			for (Image image : selectedImages) {
				paths.add(image.getPath());
			}
			this.callBack.onImagesSelected(ImageSelectSource.Album, paths);
		}

	}


	class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageHolder> {
		static final int TYPE_IMAGE = 1;
		static final int TYPE_CAMERA = 2;
		private Context context;
		private List<Image> images;

		private int itemSide = 0;

		ImagesAdapter(Context context, List<Image> images) {
			this.context = context;
			this.images = images;
		}

		public ImagesAdapter.ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			switch (viewType) {
				case TYPE_IMAGE:
					return new ImageHolder(LayoutInflater.from(context).inflate(R.layout.image_selector_item_image, parent, false));
				case TYPE_CAMERA:
					return new CameraHolder(LayoutInflater.from(context).inflate(R.layout.image_selector_item_camera, parent, false));
			}
			return null;
		}

		public int getItemCount() {
			return images.size();
		}

		public void onBindViewHolder(final ImagesAdapter.ImageHolder holder, final int position) {

			if (itemSide != 0) {
				if (holder.itemView.getLayoutParams() == null) {
					GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(itemSide, itemSide);
					holder.itemView.setLayoutParams(params);
				} else {
					holder.itemView.getLayoutParams().width = itemSide;
					holder.itemView.getLayoutParams().height = itemSide;
				}
			}
			if (holder.isCamera) {
				holder.itemView.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						cameraSavePath = cameraSavePathBase + "/" + System.currentTimeMillis() + ".jpg";
						intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(cameraSavePath)));
						intent.putExtra("return-data", true);
						startActivityForResult(intent, REQUEST_CAMERA);
					}
				});
			} else {
				final Image image = images.get(position);
				Glide.with(context).fromUri().asBitmap()
						.load(image.getUri())
						.placeholder(new ColorDrawable(Color.parseColor("#555555")))
						.error(R.drawable.image_selector_load_error).centerCrop().into(holder.img);

				final boolean isSelected = image.isSelected();
				holder.itemView.setSelected(isSelected);
				holder.backView.setVisibility(isSelected ? View.VISIBLE : View.GONE);
				if (isSingleMode()) {
					holder.backView.setVisibility(View.GONE);
					holder.checkBox.setVisibility(View.GONE);
				}

				holder.itemView.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (isSingleMode() && callBack != null) {
							callBack.onImagesSelected(ImageSelectSource.Album, new ArrayList<>(Arrays.asList(new String[]{image.getPath()})));
						}
					}
				});

				holder.checkBox.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						if (!image.isSelected() && selectedImages.size() >= imageSelectedCount) {
							Toast.makeText(context, context.getString(R.string.image_selector_msg_amount_limit, imageSelectedCount), Toast.LENGTH_SHORT).show();
							return;
						}
						image.setSelected(!isSelected);
						mAdapter.notifyItemChanged(position);
						if (image.isSelected()) {
							selectedImages.add(image);
						} else {
							selectedImages.remove(image);
						}

						if (selectedImages.size() > 0) {
							btnDone.setEnabled(true);
							btnDone.setText(context.getString(R.string.image_selector_done_format, selectedImages.size(), imageSelectedCount));
						} else {
							btnDone.setEnabled(false);
							btnDone.setText(R.string.image_selector_done);
						}
					}
				});
			}
		}

		public int getItemViewType(int position) {
			return (images.get(position)).isCamera() ? TYPE_CAMERA : TYPE_IMAGE;
		}

		public void changeData(List<Image> images) {
			clear();
			this.images.addAll(images);
			notifyItemRangeChanged(0, images.size());
		}

		public void insert(Image image) {
			this.images.add(image);
			this.notifyItemInserted(this.images.size() - 1);
		}

		public void clear() {
			this.images.clear();
			this.notifyDataSetChanged();
		}

		public void setItemSide(int itemSide) {
			this.itemSide = itemSide;
		}

		class CameraHolder extends ImagesAdapter.ImageHolder {
			public CameraHolder(View itemView) {
				super(itemView);
				this.isCamera = true;
			}
		}

		class ImageHolder extends android.support.v7.widget.RecyclerView.ViewHolder {
			boolean isCamera = false;
			ImageView img;
			View checkBox;
			View backView;

			public ImageHolder(View itemView) {
				super(itemView);
				this.img = (ImageView) itemView.findViewById(R.id.image);
				this.checkBox = itemView.findViewById(R.id.checkbox);
				this.backView = itemView.findViewById(R.id.bg);
			}
		}
	}

	class DirectoryAdapter extends BaseAdapter {
		private List<Directory> directories;
		private Context context;

		DirectoryAdapter(Context context, List<Directory> directories) {
			this.context = context;
			this.directories = directories;
		}

		public int getCount() {
			return this.directories.size();
		}

		public Object getItem(int position) {
			return this.directories.get(position);
		}

		public long getItemId(int position) {
			return (long) position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			DirectoryAdapter.ViewHolder mHolder;
			if (convertView == null) {
				convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.image_selector_directory_item, parent, false);
				mHolder = new DirectoryAdapter.ViewHolder(convertView);
				convertView.setTag(mHolder);
			} else {
				mHolder = (DirectoryAdapter.ViewHolder) convertView.getTag();
			}

			Directory directory = (Directory) getItem(position);
			mHolder.txtDirect.setText(position == 0 ? directory.getName() : directory.getName() + String.format("(%d)", directory.getImages().size()));
			if (directory.getImages().size() > 1 && isShowCamera) {
				Glide.with(context).fromUri().asBitmap().load(directory.getImages().get(1).getUri()).error(R.drawable.image_selector_load_error).into(mHolder.imgThumb);
			} else if (directory.getImages().size() > 0 && !isShowCamera) {
				Glide.with(context).fromUri().asBitmap().load(directory.getImages().get(0).getUri()).error(R.drawable.image_selector_load_error).into(mHolder.imgThumb);
			}
			return convertView;
		}

		public Context getContext() {
			return this.context;
		}

		class ViewHolder {
			ImageView imgThumb;
			TextView txtDirect;

			ViewHolder(View itemView) {
				this.imgThumb = (ImageView) itemView.findViewById(R.id.image_selector_dir_item_thumb);
				this.txtDirect = (TextView) itemView.findViewById(R.id.image_selector_dir_item_dir);
			}
		}
	}

	public enum ImageSelectSource {
		Album(0x1),
		Camera(0x2);
		int type;

		ImageSelectSource(int type) {
			this.type = type;
		}

		public int getTypeId() {
			return type;
		}
	}


	public interface ImageSelectorCallBack {
		void onImagesSelected(ImageSelectSource selectSource, ArrayList<String> list);

		void onCancel();
	}
}
