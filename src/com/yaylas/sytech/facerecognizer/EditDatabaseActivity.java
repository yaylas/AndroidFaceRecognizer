package com.yaylas.sytech.facerecognizer;


import android.app.Activity;





import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import com.yaylas.sytech.facerecognizer.facedatabase.Person;
import com.yaylas.sytech.facerecognizer.utils.FaceDetectionUtils;

import android.net.Uri;
import android.net.wifi.WpsInfo;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class EditDatabaseActivity extends Activity {

	private Context mContext;
	private int screenHeight;
	private int screenWidth;
	private int personCount = 0;
	private Vector<Person> persons = new Vector<Person>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.database_editpage);
		final RelativeLayout progressLayout = new RelativeLayout(this);
		ProgressBar progressBar = new ProgressBar(this);
		progressLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		RelativeLayout.LayoutParams pbLParams = new RelativeLayout.LayoutParams(50, 50);
		pbLParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		progressBar.setLayoutParams(pbLParams);
		progressLayout.setBackgroundColor(0x66888888);
		progressLayout.addView(progressBar);
		
		final RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.dbeditLayout);
		mainLayout.addView(progressLayout);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				getData();
				EditDatabaseActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						initialize();
						mainLayout.removeView(progressLayout);
						
					}
				});
			}
		}).start();
		
	}
	
	private void getData(){
		FaceDetectionUtils.faceDataSource.open();
		persons = FaceDetectionUtils.faceDataSource.getAllPersons();
		FaceDetectionUtils.faceDataSource.close();
		personCount = persons.size();
	}
	
	private void initialize(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		
		final TextView headerText = (TextView)findViewById(R.id.dbeditHeaderText);
		RelativeLayout.LayoutParams headerTextParams = (RelativeLayout.LayoutParams)headerText.getLayoutParams();
		headerTextParams.leftMargin = screenHeight/8;
		headerText.setLayoutParams(headerTextParams);
		headerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(float)screenHeight/45);
		headerText.setText("Face DataBase");
		headerText.setTextColor(Color.LTGRAY);
		headerText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		headerText.setTypeface(null, Typeface.BOLD);
		
		
		ListView listView = (ListView)findViewById(R.id.dbeditListView);
		ListAdapter listAdapter = new ListAdapter();
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(itemClickListener);
		listView.setVerticalScrollBarEnabled(true);
		listView.setFastScrollEnabled(true);
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)listView.getLayoutParams();
		params.leftMargin = screenWidth/40;
		params.rightMargin = screenWidth/40;
		//params.topMargin = screenHeight/40;
		listView.setLayoutParams(params);
		listView.setVerticalScrollBarEnabled(false);
	}
	
	OnItemClickListener itemClickListener = new OnItemClickListener() {

		private boolean buttonClicked = false;
		@Override
		public void onItemClick(AdapterView<?> a, View view, int position, long id) {
			if(buttonClicked){
				return;
			}
			buttonClicked = true;
			final RelativeLayout itemLayout = (RelativeLayout)view;
			final TextView itemText = (TextView)itemLayout.getChildAt(1);
			final RelativeLayout overLay = new RelativeLayout(mContext);
			overLay.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			overLay.setBackgroundColor(0x99888888);
			itemLayout.addView(overLay);
			itemText.setTextColor(Color.DKGRAY);
			Intent intent = new Intent(EditDatabaseActivity.this, FaceDetectionActivity.class);
			intent.putExtra("Training", false);
			intent.putExtra("personIndex", position);
			EditDatabaseActivity.this.startActivity(intent);
			EditDatabaseActivity.this.finish();

			itemLayout.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					buttonClicked = false;
					itemLayout.removeView(overLay);
					itemText.setTextColor(Color.BLACK);
				}
			}, 2000);
		}
	};
	
	
	class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return personCount;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = new RelativeLayout(mContext);
			convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, screenHeight/7));
			convertView.setBackgroundColor(Color.TRANSPARENT); 	
			ImageView bgImage = new ImageView(mContext);
			bgImage.setScaleType(ScaleType.FIT_XY);
			bgImage.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			bgImage.setImageResource(R.drawable.itembg);
			((RelativeLayout)convertView).addView(bgImage);
				
				
			TextView itemText = new TextView(mContext);
			RelativeLayout.LayoutParams itemTextLParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			itemTextLParams.leftMargin = screenHeight/6;
			itemText.setLayoutParams(itemTextLParams);
			itemText.setGravity(Gravity.CENTER_VERTICAL);
			itemText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float)screenHeight/50);
			itemText.setTextColor(Color.WHITE);
			itemText.setBackgroundColor(Color.TRANSPARENT);
			((RelativeLayout)convertView).addView(itemText);
			itemText.setText(persons.get(position).getName());
				
			ImageView icon = new ImageView(mContext);
			RelativeLayout.LayoutParams iconLParams = new RelativeLayout.LayoutParams(screenHeight/9, screenHeight/9);
			iconLParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//			iconLParams.topMargin = screenHeight/60;
//			iconLParams.bottomMargin = screenHeight/40;
			iconLParams.addRule(RelativeLayout.CENTER_VERTICAL);
			iconLParams.leftMargin = screenHeight/80;
			icon.setBackgroundColor(Color.BLACK);
			icon.setScaleType(ScaleType.FIT_XY);
			icon.setPadding(5, 5, 5, 5);
			icon.setLayoutParams(iconLParams);
			((RelativeLayout)convertView).addView(icon);
			Mat m = new Mat();
			m = Highgui.imread(persons.get(position).getFacesFolderPath()+"/1.jpg");
			Bitmap bmp = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.RGB_565);
			Utils.matToBitmap(m, bmp);
			icon.setImageBitmap(bmp);
			return convertView;
		}
		
	}
	

	

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			goBack();
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void goBack() {
		Intent intent = new Intent(EditDatabaseActivity.this, MainPageActivity.class);
		EditDatabaseActivity.this.startActivity(intent);
		overridePendingTransition(R.anim.slide_inlefttoright, R.anim.slide_outlefttoright);
		EditDatabaseActivity.this.finish();		
	}

}