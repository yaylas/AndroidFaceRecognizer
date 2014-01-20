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
import android.content.Context;
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

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FaceDetectionActivity extends Activity implements CvCameraViewListener2 {

    private Mat                    mRgba;
    private Mat                    mGray;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private Vector<ImageView> faceImages = new Vector<ImageView>();
    private Vector<ImageView> deleteIcons = new Vector<ImageView>();
    private HashMap<Integer, Mat> capturedMats = new HashMap<Integer, Mat>();
    private ImageView backButton;
    OnClickListener deleteIconClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int index = deleteIcons.indexOf(v);
			if(index != -1) {
				v.setVisibility(View.GONE);
				faceImages.get(index).setImageResource(R.drawable.icon);
				if(capturedMats.size() == 10) {
					if(!isTraining) {
						yTranslateAnimation(deleteButton, 0, deleteButtonFirstPos - deleteButtonSecondPos, true, true);
					}
					yTranslateAnimation(nameEdit, 0, screenHeight, false, false);
					yTranslateAnimation(saveButton, 0, 3*screenHeight, false, false);
				}
				if(capturedMats.containsKey(index)){
					capturedMats.remove(index);
				}
			}
		}
	};
	
	private boolean capturingImage = false;
	private ImageView captureButton;
	private TextView captureText;
	private Button saveButton;
	private EditText nameEdit;
	private Button deleteButton;
	
	private Person thisPerson;
	private boolean isTraining = true;
	private Vector<Person> persons = new Vector<Person>();
	private boolean detectionInProgress = false;
	private int screenWidth;
	private int screenHeight;
	private long lastDetectionTime = 0;
	private int deleteButtonFirstPos;
	private int deleteButtonSecondPos;
	private LinearLayout bgLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isTraining = getIntent().getBooleanExtra("Training", true);
        setContentView(R.layout.face_detect_surface_view);

        screenWidth = MainPageActivity.getScreenHeight();
        screenHeight = MainPageActivity.getScreenWidth();
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        
        captureButton = (ImageView)findViewById(R.id.capturebutton);
        captureText = (TextView)findViewById(R.id.capturetext);
        captureButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeSoftInput();
				if(capturingImage) {
					captureText.setText("Start Capturing");
					capturingImage = false;
					captureButton.setImageResource(R.drawable.capturestart);
				} else {
					captureText.setText("Stop Capturing");
					captureButton.setImageResource(R.drawable.capturestop);
					capturingImage = true;
				}
			}
		});
        
        backButton = (ImageView)findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				goBack();
			}
		});

        
        saveButton = (Button) findViewById(R.id.addtodbbutton);
        saveButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				closeSoftInput();
				if(isTraining){ 
					insertToDatabase();
				} else {
					//deleteFromDatabase();
					updateThisPerson();
				}
			}
		});
        
        nameEdit = (EditText)findViewById(R.id.nameedit);
        nameEdit.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        
        deleteButton = (Button) findViewById(R.id.deletefromdbbutton);
        resetImagesForTraining();
        initOtherViews();
        if(!isTraining) {
        	final int personIndex = getIntent().getIntExtra("personIndex", -1);
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					FaceDetectionUtils.faceDataSource.open();
					persons = FaceDetectionUtils.faceDataSource.getAllPersons();
					FaceDetectionUtils.faceDataSource.close();
					thisPerson = persons.get(personIndex);
					setImagesForDatabaseEdit();
					FaceDetectionActivity.this.runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							nameEdit.setText(thisPerson.getName());
							saveButton.setText("Update this person");
						}
					});
				}
			}).start();
        	deleteButton.setVisibility(View.VISIBLE);
        	deleteButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					closeSoftInput();
					deleteFromDatabase();
				}
			});
        }
        mOpenCvCameraView.enableView();
        bgLayout = (LinearLayout)findViewById(R.id.face_detect_layout);
        bgLayout.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				closeSoftInput();
				return false;
			}
		});
    }
    
    private void closeSoftInput(){
    	InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		IBinder windowToken = bgLayout.getWindowToken();
		inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
    }
    
    private void resetImagesForTraining(){
        faceImages.clear();
        deleteIcons.clear();
        capturedMats.clear();
        initFaceImageViews();
        initDeleteIcons();
		captureText.setText("Start Capturing");
		captureButton.setImageResource(R.drawable.capturestart);
		capturingImage = false;
		nameEdit.setVisibility(View.GONE);
		saveButton.setVisibility(View.GONE);
    }
    
    private void setImagesForDatabaseEdit() {
    	for(int i = 0; i < faceImages.size(); i++) {
    		Mat m = Highgui.imread(thisPerson.getFacesFolderPath()+"/"+i+".jpg");
    		if(m != null) {
    			onFaceCaptured(m);
    		}
    	}
    }
    
    private void showAlert(String title, String message, String buttonText){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
			alertDialogBuilder.setTitle(title);
 
			alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(buttonText,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
						if(isTraining){
							resetImagesForTraining();
						} else {
							goBack();
						}
					}
				  });
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
    }
    
    
    private void initFaceImageViews(){
    	int wrapperHeight = screenHeight/5;
    	LinearLayout.LayoutParams wrapperLayoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, wrapperHeight);
    	int imageWrappers[] = {R.id.imageViewWrapper1, R.id.imageViewWrapper2, R.id.imageViewWrapper3, R.id.imageViewWrapper4,
    			R.id.imageViewWrapper5, R.id.imageViewWrapper6, R.id.imageViewWrapper7,
    			R.id.imageViewWrapper8, R.id.imageViewWrapper9, R.id.imageViewWrapper10};
    	int imageViews[] = {R.id.imageView1, R.id.imageView2, R.id.imageView3, R.id.imageView4, R.id.imageView5,
    			R.id.imageView6, R.id.imageView7, R.id.imageView8, R.id.imageView9, R.id.imageView10};
    	
    	for(int i = 0; i < imageWrappers.length; i++) {
    		RelativeLayout wrapper = (RelativeLayout)findViewById(imageWrappers[i]);
    		wrapper.setBackgroundResource(R.drawable.borders);
    		wrapper.setLayoutParams(wrapperLayoutParams);
    	}
    	for(int i = 0; i < imageViews.length; i++) {
    		 ImageView faceImage = (ImageView)findViewById(imageViews[i]);
    		 faceImage.setPadding(1, 1, 1, 1);
    		 faceImage.setScaleType(ScaleType.FIT_XY);
    		 faceImage.setScaleType(ScaleType.FIT_XY);
    		 faceImage.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    		 faceImage.setBackgroundResource(R.drawable.borders);
    		 faceImage.setImageResource(R.drawable.icon);
    		 faceImages.add(faceImage);
    	}
    	
    }
    
    private void initDeleteIcons(){
    	int deleteIconViews[] = {R.id.deleteIcon1, R.id.deleteIcon2, R.id.deleteIcon3, R.id.deleteIcon4, R.id.deleteIcon5,
    			R.id.deleteIcon6, R.id.deleteIcon7, R.id.deleteIcon8, R.id.deleteIcon9, R.id.deleteIcon10};
    	RelativeLayout.LayoutParams deleteIconParams = new RelativeLayout.LayoutParams(screenHeight / 20, screenHeight / 20);
    	deleteIconParams.alignWithParent = true;
    	deleteIconParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    	deleteIconParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    	deleteIconParams.bottomMargin = 3;
    	deleteIconParams.rightMargin = 3;
    	for(int i = 0; i < deleteIconViews.length; i++) {
    		ImageView deleteIcon = (ImageView)findViewById(deleteIconViews[i]);
    		deleteIcon.setLayoutParams(deleteIconParams);
    		deleteIcon.setOnClickListener(deleteIconClickListener);
    		deleteIcon.setVisibility(View.GONE);
    		deleteIcons.add(deleteIcon);
    	}
        
    }
    
    private void initOtherViews(){
    	RelativeLayout.LayoutParams captureButtonParams = new RelativeLayout.LayoutParams(3*screenWidth/10, screenHeight/8);
    	captureButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
    	captureButtonParams.topMargin = screenHeight / 30;
    	captureButton.setLayoutParams(captureButtonParams);
    	captureText.setLayoutParams(captureButtonParams);
    	captureText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/40);
    	
    	RelativeLayout.LayoutParams nameEditParams = new RelativeLayout.LayoutParams(3*screenWidth/10, screenHeight/8);
    	nameEditParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
    	nameEditParams.addRule(RelativeLayout.BELOW, captureButton.getId());
    	nameEditParams.topMargin = screenHeight / 30;
    	nameEdit.setLayoutParams(nameEditParams);
    	nameEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/42);
    	nameEdit.setTextColor(Color.BLACK);

    	RelativeLayout.LayoutParams saveButtonParams = new RelativeLayout.LayoutParams(3*screenWidth/10, screenHeight/8);
    	saveButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
    	saveButtonParams.addRule(RelativeLayout.BELOW, nameEdit.getId());
    	saveButtonParams.topMargin = screenHeight / 30;
    	saveButton.setLayoutParams(saveButtonParams);
    	saveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/40);
    	
    	if(!isTraining){
    		deleteButtonFirstPos = screenHeight / 8 + screenHeight /15;
    		deleteButtonSecondPos = 3*screenHeight / 8 + 2*screenHeight /15;
        	RelativeLayout.LayoutParams deleteButtonParams = new RelativeLayout.LayoutParams(3*screenWidth/10, screenHeight/8);
        	deleteButtonParams.topMargin = deleteButtonFirstPos;
        	deleteButtonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        	deleteButton.setLayoutParams(deleteButtonParams);
        	deleteButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, screenHeight/42);

    	}
    }
    
    private void yTranslateAnimation(final View v,final int yStart, final int yEnd, final boolean visibleAfter, final boolean fillAfter){
    	TranslateAnimation anim = new TranslateAnimation(0, 0, yStart, yEnd);
    	anim.setDuration(600);
    	anim.setFillEnabled(fillAfter);
    	if(!visibleAfter || v == deleteButton){
    		anim.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if(!visibleAfter){
						v.setVisibility(View.GONE);
					}
					if(fillAfter && v == deleteButton) {
						RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)v.getLayoutParams();
						if(yEnd > 0) {
							params.topMargin = deleteButtonSecondPos;
						} else {
							params.topMargin = deleteButtonFirstPos;
						}
						deleteButton.setLayoutParams(params);
					}
				}
			});
    	}
    	v.clearAnimation();
    	v.setAnimation(anim);
    	v.invalidate();
    	v.startAnimation(anim);
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
		for(int i = 0; i < faceImages.size(); i++) {
			if(!capturedMats.containsKey(i)) {
				capturedMats.put(i, faceMat);
				final Bitmap bmp = Bitmap.createBitmap(faceMat.cols(), faceMat.rows(), Bitmap.Config.RGB_565);
				Utils.matToBitmap(faceMat, bmp);
				final int index = i;
				FaceDetectionActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						faceImages.get(index).setImageBitmap(bmp);
						deleteIcons.get(index).setVisibility(View.VISIBLE);
					}
				});
				break;
			}
		}
		if(capturedMats.size() == 10) {
			FaceDetectionActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					nameEdit.setVisibility(View.VISIBLE);
					saveButton.setVisibility(View.VISIBLE);
					capturingImage = false;
					captureButton.setImageResource(R.drawable.capturestart);
					captureText.setText("Start Capturing");
					if(!isTraining) {
						yTranslateAnimation(deleteButton, 0, deleteButtonSecondPos - deleteButtonFirstPos, true, true);
					}
					yTranslateAnimation(nameEdit, screenHeight, 0, true, false);
					yTranslateAnimation(saveButton, 3*screenHeight, 0, true, false);
				}
			});
		}
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
    
    private void deleteFromDatabase(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				FaceDetectionUtils.faceDataSource.open();
				FaceDetectionUtils.faceDataSource.deletePerson(thisPerson);
				FaceDetectionUtils.faceDataSource.close();
				FaceDetectionActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						showAlert("Face Database Edition","Person is deleted!", "OK");
					}
				});
			}
		}).start();
    }
    
    private void updateThisPerson(){
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				FaceDetectionUtils.faceDataSource.open();
				//FaceDetectionUtils.faceDataSource.deletePerson(thisPerson);
				thisPerson.setName(nameEdit.getText().toString());
				FaceDetectionUtils.faceDataSource.updatePerson(thisPerson);
				FaceDetectionUtils.faceDataSource.deleteFacesFolder(thisPerson.getFacesFolderPath());
				//FaceDetectionUtils.faceDataSource.createPerson(thisPerson.getId(), thisPerson.getName(), thisPerson.getFacesFolderPath());
				FaceDetectionUtils.faceDataSource.close();
				File faceFolder = new File(thisPerson.getFacesFolderPath());
				faceFolder.mkdirs();
				for(int i = 0; i < capturedMats.size(); i++) {
					if(capturedMats.containsKey(i)){
						ImageUtils.saveImageAsPGM(thisPerson.getFacesFolderPath()+"/"+i+".jpg", capturedMats.get(i).getNativeObjAddr());
					}
				}
				FaceDetectionActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						showAlert("Face Database Edition","Person editing is completed!", "OK");
					}
				});
			}
		}).start();
    }
    
    private void insertToDatabase(){
    	String name = nameEdit.getText().toString();
    	long id = -1;
    	String properPath = null;
    	Person p = null;
    	File photoFolder;
    	while(true){
    		id++;
    		String path = ""+Environment.getExternalStorageDirectory();
    		path += "/Android/data/"+getPackageName()+"/.faces/"+id;
    		photoFolder = new File(path);
    		if(!photoFolder.exists()){
    			properPath = path;
    			break;
    		}
    	}   
    	if(name == null ||name.length() == 0) {
    		name = "unNamed" + id;
    	}
    	final long idToSave = id;
    	final String nameToSave = name;
    	final String pathToSave = properPath;
    	final File faceFolder = photoFolder;
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				FaceDetectionUtils.faceDataSource.open();
				FaceDetectionUtils.faceDataSource.createPerson(idToSave, nameToSave, pathToSave);
				FaceDetectionUtils.faceDataSource.close();
				faceFolder.mkdirs();
				for(int i = 0; i < capturedMats.size(); i++) {
					if(capturedMats.containsKey(i)){
						ImageUtils.saveImageAsPGM(pathToSave+"/"+i+".jpg", capturedMats.get(i).getNativeObjAddr());
					}
				}
				FaceDetectionActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(isTraining){
							showAlert("Face Recognition Training","Images are saved!", "OK");
						}
					}
				});
				
			}
		}).start();
    }


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			goBack();
		}
		return super.onKeyUp(keyCode, event);
	}
	
	private void goBack() {
		Intent intent = isTraining?new Intent(FaceDetectionActivity.this, MainPageActivity.class)
										:new Intent(FaceDetectionActivity.this, EditDatabaseActivity.class);
		FaceDetectionActivity.this.startActivity(intent);
		FaceDetectionActivity.this.finish();		
	}
	
	
}
