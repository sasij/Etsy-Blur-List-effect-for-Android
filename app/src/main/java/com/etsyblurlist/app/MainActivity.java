package com.etsyblurlist.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

public class MainActivity extends ActionBarActivity implements
		BlurScrollView.OnScrollViewListener {

	private static final String BLURRED_IMG_PATH = "/blurred_image.png";
	private BlurScrollView mScrollView;
	private ImageView mBlurredImage, mBackground;
	private ImageView mNormalImage;
	private LinearLayout layoutScroll;
	private float alpha, alphaBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		layoutScroll = (LinearLayout) findViewById(R.id.layoutScroll);
		mBlurredImage = (ImageView) findViewById(R.id.blurredImage);
		mNormalImage = (ImageView) findViewById(R.id.normalImage);
		mBackground = (ImageView) findViewById(R.id.backgroundImage);

		mScrollView = (BlurScrollView) findViewById(R.id.list);

		// Try to find the blurred image
		final File blurredImage = new File(getFilesDir() + BLURRED_IMG_PATH);

		if (!blurredImage.exists()) {
			generateBlurImage(blurredImage);
		} else {
			// The image has been found. Let's update the view
			updateView();
		}
		mScrollView.setOnScrollViewListener(this);
	}

	private void generateBlurImage(final File blurredImage) {
		// launch the progressbar in ActionBar
		setSupportProgressBarIndeterminateVisibility(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				// No image found => let's generate it!
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				Bitmap image = BitmapFactory.decodeResource(getResources(),
						R.drawable.photo_landscape, options);
				Bitmap newImg = Blur.fastblur(MainActivity.this, image, 24);
				ImageUtils.storeImage(newImg, blurredImage);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						updateView();
						// And finally stop the progressbar
						setSupportProgressBarIndeterminateVisibility(false);
					}
				});
			}
		}).start();
	}

	private void updateView() {
		final int screenWidth = ImageUtils.getScreenWidth(this);
		Bitmap bmpBlurred = BitmapFactory.decodeFile(getFilesDir()
				+ BLURRED_IMG_PATH);
		bmpBlurred = Bitmap
				.createScaledBitmap(
						bmpBlurred,
						screenWidth,
						(int) (bmpBlurred.getHeight() * ((float) screenWidth) / bmpBlurred
								.getWidth()), false);
		mBlurredImage.setImageBitmap(bmpBlurred);
	}

	@Override
	public void onScrollChanged(BlurScrollView v, int l, int t, int oldl,
			int oldt) {

		// Calculate the ratio between the scroll amount and the list
		// header weight to determinate the top picture alpha
		float paddingTop = layoutScroll.getTop();

		if (paddingTop != 0) {
			alpha = (float) (t + 1) / paddingTop;
			alphaBackground = alpha;
		}

		// Apply a ceil
		if (alpha > 1) {
			alpha = 1;
		}
		if (alphaBackground > 0.85f) {
			alphaBackground = 0.85f;
		}

		// Apply on the ImageView if needed
		if (Build.VERSION.SDK_INT >= 11) {
			mBlurredImage.setAlpha(alpha);
			mBackground.setAlpha(alphaBackground);
		} else {
			Drawable backgroundBlurredImage = mBlurredImage.getBackground();
			Drawable backgroundImage = mBackground.getBackground();
			if (backgroundBlurredImage != null && backgroundImage != null) {
				backgroundBlurredImage.setAlpha((int) alpha * 255);// 255 is max
				backgroundImage.setAlpha((int) alphaBackground * 255);// 255 is
																		// max
			}
		}

		// Parallax effect : we apply half the scroll amount to our
		// three views
		if (Build.VERSION.SDK_INT >= 11) {
			mBlurredImage.setTop(-t / 4);
			mNormalImage.setTop(-t / 4);
			mBackground.setTop(-t / 4);
		} else {
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, -t / 4, 0, 0);
			mBlurredImage.setLayoutParams(layoutParams);
			mNormalImage.setLayoutParams(layoutParams);
			mBackground.setLayoutParams(layoutParams);
		}
	}
}
