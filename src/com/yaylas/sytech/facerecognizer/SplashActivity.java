package com.yaylas.sytech.facerecognizer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.yaylas.sytech.facerecognizer.facedatabase.FacesDataSource;
import com.yaylas.sytech.facerecognizer.utils.FaceDetectionUtils;

import android.animation.Animator.AnimatorListener;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class SplashActivity extends Activity{

	private ImageView splashPage;
	private ImageView dot1;
	private ImageView dot2;
	private ImageView dot3;
	private Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		splashPage = (ImageView)findViewById(R.id.splashImage);
		dot1 = (ImageView)findViewById(R.id.splashdot1);
		dot2 = (ImageView)findViewById(R.id.splashdot2);
		dot3 = (ImageView)findViewById(R.id.splashdot3);
		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
				 FaceDetectionUtils.initialize(SplashActivity.this);
//			}
//		}).start();
		openHomePage();
		
		
	}
	


	private void openHomePage(){			
		AlphaAnimation fadeIn = new AlphaAnimation(1, 1);
		fadeIn.setInterpolator(new AccelerateInterpolator()); //add this
		fadeIn.setDuration(1);
		splashPage.setAnimation(fadeIn);
		fadeIn.setAnimationListener(new AnimationListener() {
					
			@Override
			public void onAnimationStart(Animation animation) {
	
			}
					
			@Override
			public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub			
			}
					
			@Override
			public void onAnimationEnd(Animation animation) {
				final int shift = dot1.getMeasuredWidth()+10;
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						dotEnterAnimation(dot1, shift);
					}
				}, 100);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						dotEnterAnimation(dot2, 0);
					}
				}, 400);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						dotEnterAnimation(dot3, -shift);
					}
				}, 700);
			}
		});
		splashPage.invalidate();
		fadeIn.startNow();
		splashPage.startAnimation(fadeIn);
				
	}
	
	private void dotEnterAnimation(final ImageView dot, int shift){
		DisplayMetrics dm = new DisplayMetrics();
		SplashActivity.this.getWindowManager().getDefaultDisplay().getMetrics( dm );
		int originalPos[] = new int[2];
		dot.getLocationOnScreen(originalPos);
		
		int xDest = dm.widthPixels/2;
		xDest -= (dot.getMeasuredWidth()/2) + shift;
		
		final int lastPos = xDest - originalPos[0];
		TranslateAnimation dotSlide = new TranslateAnimation( 0, xDest - originalPos[0] , 0, 0);
		dotSlide.setInterpolator(new DecelerateInterpolator());
		dotSlide.setDuration(1000);
		dotSlide.setFillAfter(true);
		dotSlide.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				if(dot == dot1){
					dotExitAnimation(dot, 900, lastPos);
				} else if(dot == dot2){
					dotExitAnimation(dot, 700, lastPos);
				} else if(dot == dot3){
					dotExitAnimation(dot, 500, lastPos);
				}
			}
		});
		dot.clearAnimation();
		dot.setAnimation(dotSlide);
		dot.invalidate();
		dotSlide.startNow();
		dot.startAnimation(dotSlide);
	}
	
	private void dotExitAnimation(final ImageView dot,int time, final int lastPos){
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				DisplayMetrics dm = new DisplayMetrics();
				SplashActivity.this.getWindowManager().getDefaultDisplay().getMetrics( dm );
				int originalPos[] = new int[2];
				dot.getLocationOnScreen(originalPos);
				
				int xDest = dm.widthPixels;
				
				
				TranslateAnimation dotSlide = new TranslateAnimation( lastPos, lastPos-xDest , 0, 0);
				dotSlide.setInterpolator(new DecelerateInterpolator());
				dotSlide.setDuration(800);
				dotSlide.setFillAfter(true);
				dotSlide.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						if(dot == dot3){
							splashFadeOut();
						}
					}
				});
				dot.clearAnimation();
				dot.setAnimation(dotSlide);
				dot.invalidate();
				dotSlide.startNow();
				dot.startAnimation(dotSlide);
			}
		}, time);
	
	}
	
	private void splashFadeOut(){
		if(FaceDetectionUtils.cascadeFilesLoaded){
			Intent intent = new Intent(SplashActivity.this, MainPageActivity.class);
			SplashActivity.this.startActivity(intent);
			SplashActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
			SplashActivity.this.finish();
		} else {
			openHomePage();
		}
				
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return false;
		}
		return super.onKeyUp(keyCode, event);
	}
}

