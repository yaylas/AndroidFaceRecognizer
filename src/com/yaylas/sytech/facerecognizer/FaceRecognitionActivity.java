package com.yaylas.sytech.facerecognizer;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.yaylas.sytech.facerecognizer.facedatabase.FacesDataSource;
import com.yaylas.sytech.facerecognizer.facedatabase.Person;
import com.yaylas.sytech.facerecognizer.methods.FaceDetection;
import com.yaylas.sytech.facerecognizer.utils.FaceDetectionUtils;
import com.yaylas.sytech.facerecognizer.utils.ImageUtils;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FaceRecognitionActivity extends Activity implements CvCameraViewListener2 {

    private Mat                    mRgba;
    private Mat                    mGray;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private static Vector<Person> persons = new Vector<Person>();
    private ImageView backButton;
    private LinearLayout resultView;
	
	private boolean capturingImage = false;
	private Button captureButton;
	private Mat capturedMat;
	private ImageView capturedImage;
	private Button recognizeButton;
	private boolean detectionInProgress = false;
	private int screenWidth;
	private int screenHeight;
	private boolean showingResults = false;
	private long lastDetectionTime = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.recognition_page);
        screenWidth = MainPageActivity.getScreenHeight();
        screenHeight = MainPageActivity.getScreenWidth();
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fr_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        
        captureButton = (Button)findViewById(R.id.frcapturebutton);
        captureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(capturingImage) {
					capturingImage = false;
					captureButton.setBackgroundResource(R.drawable.capturestart);
					captureButton.setText("Start Capturing");
				} else {
					capturingImage = true;
					captureButton.setBackgroundResource(R.drawable.capturestop);
					captureButton.setText("Stop Capturing");
					if(showingResults){
						hideResults();
					}
				}
			}
		});
        recognizeButton = (Button)findViewById(R.id.frrecognizebutton);
        recognizeButton.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		recognize();
        	}
        });
        
        backButton = (ImageView)findViewById(R.id.frbackbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goBack();
			}
		});
        capturedImage = (ImageView)findViewById(R.id.frcapturedImage);
        
        resultView = (LinearLayout)findViewById(R.id.frresultLayout);
        
        mOpenCvCameraView.enableView();
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				FaceDetectionUtils.faceDataSource.open();
				persons = FaceDetectionUtils.faceDataSource.getAllPersons();
				FaceDetectionUtils.faceDataSource.close();
			}
		}).start();
        initViewPositions();
    }
    
    private void initViewPositions(){
    	RelativeLayout imageWrapper = (RelativeLayout) findViewById(R.id.imglyt);
    	RelativeLayout.LayoutParams imageWrapperParams = new RelativeLayout.LayoutParams(screenWidth/4, screenWidth/4);
    	imageWrapperParams.topMargin = screenHeight / 40;
    	imageWrapperParams.leftMargin = 3*screenWidth / 20;
    	imageWrapper.setLayoutParams(imageWrapperParams);
    	capturedImage.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	capturedImage.setPadding(1, 1, 1, 1);
    	
    	RelativeLayout.LayoutParams captureButtonParams = new RelativeLayout.LayoutParams(screenWidth/4, screenHeight/8);
    	captureButtonParams.addRule(RelativeLayout.BELOW, imageWrapper.getId());
    	captureButtonParams.topMargin = screenHeight / 40;
    	captureButtonParams.leftMargin = 3*screenWidth / 20;
    	captureButton.setLayoutParams(captureButtonParams);
    	captureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/40);
    	
    	RelativeLayout.LayoutParams recognizeButtonParams = new RelativeLayout.LayoutParams(2*screenWidth/7, screenHeight/7);
    	recognizeButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
    	recognizeButtonParams.topMargin = (screenHeight + screenWidth) / 4;
    	recognizeButton.setLayoutParams(recognizeButtonParams);
    	recognizeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/35);
    	
    	TextView resultHeader = (TextView)findViewById(R.id.frresultheader);
    	TextView resultTextView = (TextView)findViewById(R.id.frresulttextview);
    	resultHeader.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/40);
    	resultTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/40);
    	
    	RelativeLayout.LayoutParams resultViewParams = new RelativeLayout.LayoutParams(screenWidth / 4, 3*screenHeight / 5);
    	resultViewParams.topMargin = screenHeight / 40;
    	resultViewParams.leftMargin = 3*screenWidth/10;
    	resultView.setLayoutParams(resultViewParams);
    	
    	
    }
    
    public static String getFaceFolder(int index){
    	return persons.get(index).getFacesFolderPath();
    }

    public static long getPersonID(int index){
    	return persons.get(index).getId();
    }
    
    private void recognize(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				int result = faceRecognition(capturedMat.getNativeObjAddr(), persons.size());
				for(int i = 0; i < persons.size(); i++) {
					int id = (int)persons.get(i).getId();
					if(result == id) {
						final int index = i;
						FaceRecognitionActivity.this.runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								ImageView image = (ImageView)findViewById(R.id.frresultimage);
								Mat m = Highgui.imread(persons.get(index).getFacesFolderPath()+"/1.jpg");
								final Bitmap bmp = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.RGB_565);
								Utils.matToBitmap(m, bmp);
								image.setImageBitmap(bmp);
								TextView resultText = (TextView)findViewById(R.id.frresulttextview);
								resultText.setText(persons.get(index).getName());
								if(!showingResults) {
									showResults();
								}
							}
						});
					}
				}
			}
		}).start();
    }
    
    private void showAlert(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			alertDialogBuilder.setTitle("Face Detection Training");
 
			alertDialogBuilder
				.setMessage("Ten samples should be for saving!")
				.setCancelable(false)
				.setPositiveButton("OK",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				  });
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
    }
    
    private void showResults(){
    	showingResults = true;
    	final RelativeLayout imageWrapper = (RelativeLayout) findViewById(R.id.imglyt);
    	TranslateAnimation captureAnim = new TranslateAnimation(0, screenHeight/30 - 3*screenWidth/20, 0, 0);
    	captureAnim.setDuration(400);
    	captureAnim.setFillEnabled(true);
    	captureAnim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout.LayoutParams cbparams = (RelativeLayout.LayoutParams)captureButton.getLayoutParams();
				RelativeLayout.LayoutParams ciparams = (RelativeLayout.LayoutParams)imageWrapper.getLayoutParams();
				cbparams.leftMargin = screenHeight / 30;
				ciparams.leftMargin = screenHeight / 30;
				captureButton.setLayoutParams(cbparams);
				imageWrapper.setLayoutParams(ciparams);
			}
		});
    	captureButton.clearAnimation();
    	imageWrapper.clearAnimation();
    	captureButton.setAnimation(captureAnim);
    	imageWrapper.setAnimation(captureAnim);
    	captureButton.invalidate();
    	imageWrapper.invalidate();
    	captureButton.startAnimation(captureAnim);
    	imageWrapper.startAnimation(captureAnim);
    	captureAnim.startNow();
    	
    	resultView.setVisibility(View.VISIBLE);
    	TranslateAnimation resultAnim = new TranslateAnimation(screenWidth/3, 0, 0, 0);
    	resultAnim.setDuration(800);
    	resultView.clearAnimation();
    	resultView.setAnimation(resultAnim);
    	resultView.invalidate();
    	resultView.startAnimation(resultAnim);
    	resultAnim.startNow();
    }

    private void hideResults(){
    	showingResults =false;
    	final RelativeLayout imageWrapper = (RelativeLayout) findViewById(R.id.imglyt);
    	TranslateAnimation captureAnim = new TranslateAnimation(0, 3*screenWidth/20 - screenHeight/30, 0, 0);
    	captureAnim.setDuration(400);
    	captureAnim.setFillEnabled(true);
    	captureAnim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				RelativeLayout.LayoutParams cbparams = (RelativeLayout.LayoutParams)captureButton.getLayoutParams();
				RelativeLayout.LayoutParams ciparams = (RelativeLayout.LayoutParams)imageWrapper.getLayoutParams();
				cbparams.leftMargin = 3*screenWidth/20;
				ciparams.leftMargin = 3*screenWidth/20;
				captureButton.setLayoutParams(cbparams);
				imageWrapper.setLayoutParams(ciparams);
			}
		});
    	captureButton.clearAnimation();
    	imageWrapper.clearAnimation();
    	captureButton.setAnimation(captureAnim);
    	imageWrapper.setAnimation(captureAnim);
    	captureButton.invalidate();
    	imageWrapper.invalidate();
    	captureButton.startAnimation(captureAnim);
    	imageWrapper.startAnimation(captureAnim);
    	captureAnim.startNow();
    	
    	resultView.setVisibility(View.VISIBLE);
    	TranslateAnimation resultAnim = new TranslateAnimation(0,screenWidth/3, 0, 0);
    	resultAnim.setDuration(400);
    	resultAnim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				resultView.setVisibility(View.GONE);
			}
		});
    	resultView.clearAnimation();
    	resultView.setAnimation(resultAnim);
    	resultView.invalidate();
    	resultView.startAnimation(resultAnim);
    	resultAnim.startNow();
    }
    
    private void bringRecognizeButtonAnimatedly(){
    	recognizeButton.setVisibility(View.VISIBLE);
    	TranslateAnimation anim = new TranslateAnimation(0, 0, screenHeight, 0);
    	anim.setDuration(700);
    	recognizeButton.clearAnimation();
    	recognizeButton.setAnimation(anim);
    	recognizeButton.invalidate();
    	recognizeButton.startAnimation(anim);
    	anim.startNow();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mOpenCvCameraView.enableView();
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }
    
    private void detectFaceOnFrame(final Mat frame){
    	Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				detectionInProgress = true;
		        if (mAbsoluteFaceSize == 0) {
			          int height = frame.rows();
			          if(Math.round(height * mRelativeFaceSize) > 0) {
			        	  mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
			          }
			          FaceDetectionUtils.mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
			    }
		        Mat faceMat = FaceDetection.detectFaces(null, frame, capturingImage);
		        if(faceMat != null) {
		        	long now = System.currentTimeMillis();
		        	if(now - lastDetectionTime > 400){
			        	Mat m = new Mat(faceMat.rows(), faceMat.cols(), faceMat.type());
	    				faceMat.copyTo(m);
			        	onFaceCaptured(m);
		        	}
		        	lastDetectionTime = now;
		        }
		        detectionInProgress = false;
			}
		});
    	if(!detectionInProgress) {
    		t.start();
    	}
    }
    
	private void onFaceCaptured(Mat faceMat){
		capturingImage = false;
		final boolean willRecognizeButtonAppear = capturedMat == null;
		capturedMat = faceMat;
		final Bitmap bmp = Bitmap.createBitmap(faceMat.cols(), faceMat.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(faceMat, bmp);
		FaceRecognitionActivity.this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				capturedImage.setImageBitmap(bmp);
				captureButton.setBackgroundResource(R.drawable.capturestart);
				captureButton.setText("Start Capturing");
				if(willRecognizeButtonAppear) {
					bringRecognizeButtonAnimatedly();
				}
			}
		});
	}

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        
        mGray = inputFrame.gray();
        
        if(!detectionInProgress){
        	Mat image = new Mat(mGray.rows(), mGray.cols(), mGray.type());
        	mGray.copyTo(image);
        	detectFaceOnFrame(image);
        }


        return mRgba;
    }


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			goBack();
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void goBack() {
		Intent intent = new Intent(FaceRecognitionActivity.this, MainPageActivity.class);
		FaceRecognitionActivity.this.startActivity(intent);
		FaceRecognitionActivity.this.finish();		
	}
	
	
	
	private static native int faceRecognition(long inputImage, int personCount);
	
}
