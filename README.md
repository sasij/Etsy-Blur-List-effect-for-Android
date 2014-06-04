# Blur Etsy List for Android

I like the Etsy app and I tried develop a similar effect to its blur-parallax effect when you enter in the detailed item view. I started my project when I read the Nicolas Pomepuy's post you can read [here](http://nicolaspomepuy.fr/blur-effect-for-android-design/).

Also, I recorded a video that you can wath [here](http://youtu.be/vf2K-W2hQlE)

##Usage

Because of  rendescript requires the api level 11, I use a Blur.java developed by Mario Klingemann, in order to generate a blur image that is swapped with the normal image when scrolling up or down. Now, I just read, that renderscript is available with support library v8.

In order to use this project, you only need to copy the Blur.java and BlurScrollView.java in your project and follow the next steps:


```xml
<dimen name="blur_image_height">235dp</dimen>
```
After that, include in your layout the viewPager
```xml
    <ImageView
        android:id="@+id/normalImage"
        android:layout_width="match_parent"
        android:scaleType="fitXY"
        android:src="@drawable/photo_landscape"
        android:layout_height="@dimen/blur_image_height" />

    <ImageView
        android:id="@+id/blurredImage"
        android:layout_width="match_parent"
        android:layout_height="@dimen/blur_image_height"
        android:alpha="0" />

    <ImageView
        android:id="@+id/backgroundImage"
        android:layout_width="match_parent"
        android:layout_height="@dimen/blur_image_height"
        android:alpha="0"
        android:src="#fff" />

    <com.etsyblurlist.app.BlurScrollView
        android:id="@+id/list"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        //
        //Complete with your views
        //...
        
    </com.etsyblurlist.app.BlurScrollView>

```
First, check if the blur image is generated
```java
final File blurredImage = new File(getFilesDir() + BLURRED_IMG_PATH);

		if (!blurredImage.exists()) {
			generateBlurImage(blurredImage);
		} else {
			// The image has been found. Let's update the view
			updateView();
		}

```

Second, set the listener

```java
mScrollView.setOnScrollViewListener(this);
```

```java
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
```

The magic occurs when you scroll up or down...

```java

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

```

Feel free to contact me if you have any problem.
