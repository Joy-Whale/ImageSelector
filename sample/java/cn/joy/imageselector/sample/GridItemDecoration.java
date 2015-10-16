package cn.joy.imageselector.sample;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * **********************
 * Author: yu
 * Date:   2015/8/10
 * Time:   16:48
 * **********************
 */
public class GridItemDecoration extends RecyclerView.ItemDecoration {

	private int mSpace;
	private int mColumn;

	public GridItemDecoration(int space, int column) {
		this.mSpace = space;
		this.mColumn = column;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		int position = parent.getLayoutManager().getPosition(view);
		int column = position % mColumn;
		outRect.left = column * mSpace / mColumn;
		outRect.right = mSpace - (column + 1) * mSpace / mColumn;
		outRect.top = mSpace;
	}
}
