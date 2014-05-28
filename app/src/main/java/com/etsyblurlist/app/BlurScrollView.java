package com.etsyblurlist.app;

/**
 * Created by juanjo on 28/05/14.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class BlurScrollView extends ScrollView {

	private OnScrollViewListener mOnScrollViewListener;

	public interface OnScrollViewListener {
		void onScrollChanged(BlurScrollView v, int l, int t, int oldl, int oldt);
	}

	public BlurScrollView(Context context) {
		super(context);
	}

	public BlurScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BlurScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnScrollViewListener(OnScrollViewListener listener) {
		mOnScrollViewListener = listener;
	}

	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		mOnScrollViewListener.onScrollChanged(this, l, t, oldl, oldt);
		super.onScrollChanged(l, t, oldl, oldt);
	}
}
