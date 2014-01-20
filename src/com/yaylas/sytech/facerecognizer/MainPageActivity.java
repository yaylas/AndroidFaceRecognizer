package com.yaylas.sytech.facerecognizer;


import android.app.Activity;








import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import com.google.ads.AdRequest.ErrorCode;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;



public class MainPageActivity extends Activity {

	private Context mContext;
	private static int screenHeight;
	private static int screenWidth;
	private AdView adView;
	
	private AdListener adListener = new AdListener() {
		@Override
		public void onAdFailedToLoad(int errorCode) {
			System.out.println("----------------- onAdFailedToLoad ec: "+errorCode);
//			System.out.println("---------------- INTERNAL_ERROR"+ErrorCode.INTERNAL_ERROR);
//			System.out.println("---------------- INVALID_REQUEST"+ErrorCode.INVALID_REQUEST);
//			System.out.println("---------------- NETWORK_ERROR"+ErrorCode.NETWORK_ERROR);
//			System.out.println("---------------- NO_FILL"+ErrorCode.NO_FILL);
			super.onAdFailedToLoad(errorCode);
		}
		@Override
		public void onAdLeftApplication() {
			// TODO Auto-generated method stub
			System.out.println("----------------- onAdLeftApplication");
			super.onAdLeftApplication();
		}
		
		public void onAdClosed() {
			System.out.println("----------------- onAdClosed");
		};
		public void onAdLoaded() {
			System.out.println("----------------- onAdLoaded");
		};
		public void onAdOpened() {
			System.out.println("----------------- onAdOpened");
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_page);
		
		
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-4147454460675251/3837655321");
		adView.setAdSize(AdSize.BANNER);
		
		adView.setAdListener(adListener);
		
		RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.main_page_adview_layout);
		adLayout.addView(adView);
		
		RelativeLayout.LayoutParams adViewlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		adViewlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adViewlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		adView.setLayoutParams(adViewlp);

		
		AdRequest request = new AdRequest.Builder()
		.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
	    .addTestDevice("04E14595EE84A505616E50A99B15252D")
	    .build();
		
		
		adView.loadAd(request);
		
		System.out.println("---------------------- istestdevice: "+request.isTestDevice(this));

		
		mContext = this;
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics( dm );
		
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		
		final TextView headerText = (TextView)findViewById(R.id.mainHeaderText);
		RelativeLayout.LayoutParams headerTextParams = (RelativeLayout.LayoutParams)headerText.getLayoutParams();
		headerTextParams.leftMargin = screenHeight/8;
		headerText.setLayoutParams(headerTextParams);
		headerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float)screenHeight/35);
		headerText.setText("Face Recognition");
		headerText.setTextColor(Color.LTGRAY);
		headerText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		headerText.setTypeface(null, Typeface.BOLD);
		
		ImageView headerIcon = (ImageView)findViewById(R.id.mainHeaderIcon);
		RelativeLayout.LayoutParams iconLParams = new RelativeLayout.LayoutParams(screenHeight/11, screenHeight/11);
		iconLParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT | RelativeLayout.CENTER_VERTICAL);
		iconLParams.leftMargin = screenHeight/80;
		headerIcon.setLayoutParams(iconLParams);
		
		
		ListView listView = (ListView)findViewById(R.id.mainListView);
		ListAdapter listAdapter = new ListAdapter();
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(itemClickListener);
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)listView.getLayoutParams();
		params.leftMargin = screenWidth/10;
		params.rightMargin = screenWidth/10;
		params.topMargin = screenHeight/12;
		listView.setLayoutParams(params);
		listView.setVerticalScrollBarEnabled(false);

	}
	
	public static int getScreenHeight(){
		return screenHeight;
	}

	public static int getScreenWidth(){
		return screenWidth;
	}
	
	OnItemClickListener itemClickListener = new OnItemClickListener() {

		private boolean buttonClicked = false;
		@Override
		public void onItemClick(AdapterView<?> a, View view, int position, long id) {
			if(position % 2 == 1){
				return;
			}
			if(buttonClicked){
				return;
			}
			buttonClicked = true;
			final RelativeLayout itemLayout = (RelativeLayout)view;
			//final ImageView bgImage = (ImageView)itemLayout.getChildAt(0);
			final TextView itemText = (TextView)itemLayout.getChildAt(1);
			//bgImage.setImageResource(R.drawable.backdenemepr2);
			final RelativeLayout overLay = new RelativeLayout(mContext);
			overLay.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			overLay.setBackgroundColor(0x66888888);
			itemLayout.addView(overLay);
			itemText.setTextColor(Color.DKGRAY);
			if(position == 0){
				Intent intent = new Intent(MainPageActivity.this, FaceDetectionActivity.class);
				intent.putExtra("Training", true);
				MainPageActivity.this.startActivity(intent);
				overridePendingTransition(R.anim.slide_inrighttoleft, R.anim.slide_outrighttoleft);
				MainPageActivity.this.finish();
			} else if(position == 2){
				Intent intent = new Intent(MainPageActivity.this, FaceRecognitionActivity.class);
				MainPageActivity.this.startActivity(intent);
				overridePendingTransition(R.anim.slide_inrighttoleft, R.anim.slide_outrighttoleft);
				MainPageActivity.this.finish();
			}else if(position == 4){
				Intent intent = new Intent(MainPageActivity.this, EditDatabaseActivity.class);
				MainPageActivity.this.startActivity(intent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				MainPageActivity.this.finish();
			}
			itemLayout.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					buttonClicked = false;
					//bgImage.setImageResource(R.drawable.backdeneme);
					itemLayout.removeView(overLay);
					itemText.setTextColor(Color.BLACK);
				}
			}, 2000);
		}
	};
	
	OnTouchListener onItemsTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();

			switch (action) {
			case MotionEvent.ACTION_DOWN:				
				
				break;
			case MotionEvent.ACTION_UP:
				break;
			default:
				break;
			}
			return false;
		}
	};
	
	
	
	class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 5;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = new RelativeLayout(mContext);
				convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, screenHeight/7));
				if(position % 2 == 1){
					convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, screenHeight/21));
					convertView.setVisibility(View.INVISIBLE);
					return convertView;
				}
				convertView.setBackgroundColor(Color.TRANSPARENT); 
				//if(position != 1) {			
					ImageView bgImage = new ImageView(mContext);
					bgImage.setScaleType(ScaleType.FIT_XY);
					bgImage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
					bgImage.setImageResource(R.drawable.buttonbg);
					((RelativeLayout)convertView).addView(bgImage);
					
					
					TextView itemText = new TextView(mContext);
					RelativeLayout.LayoutParams itemTextLParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
					//itemTextLParams.leftMargin = screenHeight/7;
					itemText.setLayoutParams(itemTextLParams);
					itemText.setGravity(Gravity.CENTER);
					itemText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float)screenHeight/40);
					itemText.setTextColor(Color.BLACK);
					itemText.setBackgroundColor(Color.TRANSPARENT);
					((RelativeLayout)convertView).addView(itemText);
					itemText.setTextColor(Color.WHITE);					
					ImageView icon = new ImageView(mContext);
					RelativeLayout.LayoutParams iconLParams = new RelativeLayout.LayoutParams(screenHeight/7, screenHeight/7);
					iconLParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					iconLParams.topMargin = screenHeight/60;
					iconLParams.bottomMargin = screenHeight/40;
					//iconLParams.leftMargin = screenHeight/80;
					icon.setLayoutParams(iconLParams);
					//((RelativeLayout)convertView).addView(icon);
					if(position == 0){
						itemText.setText("Train Recognizer");
					} else if(position == 2){
						itemText.setText("Face Recognition");
					} else if(position == 4){
						itemText.setText("Edit Database");
					}
				//}
				
				convertView.setOnTouchListener(onItemsTouchListener);
				
			}
			return convertView;
		}
		
	}
	
	@Override
	public void onPause() {
	  adView.pause();
	  super.onPause();
	}

	@Override
	public void onResume() {
	  super.onResume();
	  adView.resume();
	}

	@Override
	public void onDestroy() {
	  adView.destroy();
	  super.onDestroy();
	}

	

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			
		}
		return super.onKeyUp(keyCode, event);
	}

}