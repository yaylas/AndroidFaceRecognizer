package com.yaylas.sytech.facerecognizer.methods;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.media.FaceDetector;
import android.os.AsyncTask;

import com.yaylas.sytech.facerecognizer.DetectionBasedTracker;
import com.yaylas.sytech.facerecognizer.FaceDetectionActivity;
import com.yaylas.sytech.facerecognizer.utils.FaceDetectionUtils;
import com.yaylas.sytech.facerecognizer.utils.ImageUtils;

public class FaceDetection {
	public static Mat detectFaces(Mat cameraFrame, Mat image, boolean captureImage){
	       MatOfRect faces = new MatOfRect();
	       if (FaceDetectionUtils.mNativeDetector != null)
	    	   FaceDetectionUtils.mNativeDetector.detect(image, faces);

	       Rect[] facesArray = faces.toArray();
	       if(facesArray.length == 0){
	    	   return null;
	       }
	       
	       Mat faceRect = null;
	       Rect leftEye = null;
	       Rect rightEye = null;
	       Rect faceFrame = null;
	       for (int i = 0; i < 1; i++){
	    	   leftEye = null;
	    	   rightEye = null;
	    	   Mat submat = null;
	    	   try{
	    		   submat = image.submat(facesArray[i]);
	    	   } catch(Exception e){
	    		   return null;
	    	   }
		        faceRect = new Mat(200, 200, submat.type());
		        Imgproc.resize(submat, faceRect, faceRect.size());
		        leftEye = detectEyes(FaceDetectionUtils.mNativeLeftEyeDetector, faceRect, true);
		        if(leftEye == null) {
		        	return null;
		        }
		        rightEye = detectEyes(FaceDetectionUtils.mNativeRightEyeDetector, faceRect, false);
		        if(rightEye == null) {
		        	return null;
		        }
		        break;
	        }
	       
	       if(captureImage) {
	    	   Point leftEyeCenter = new Point(leftEye.x +leftEye.width/2, leftEye.y +leftEye.height/2);
	    	   Point rightEyeCenter = new Point(rightEye.x +rightEye.width/2, rightEye.y +rightEye.height/2);
	    	   
	    	   Mat capturedMat = ImageUtils.cropFace(faceRect, leftEyeCenter, rightEyeCenter, 0.2, 200, 200);
	    	   return capturedMat;
	       }
	       return null;
	}
	
	
	
	 private static Rect detectEyes(DetectionBasedTracker detector, Mat image, boolean left){
		 	Rect result = null;
		 	if(detector == null){
		 		return result;
		 	}
		 	detector.setMinFaceSize(30);
		 	MatOfRect eyes = new MatOfRect();
		 	detector.detect(image, eyes);
		 	Rect[] eyesArray = eyes.toArray();
		 	int possibleEyeIndex = -1;
		 	int minEyeRectSize = 1000000;
		 	 for (int j = 0; j < eyesArray.length; j++){
		 		 if(left && (eyesArray[j].x + eyesArray[j].width) > (image.cols()/2)) {
		 			 continue;
		 		 }
		 		 if(!left && (eyesArray[j].x + eyesArray[j].width) < (image.cols()/2)) {
		 			 continue;
		 		 }
		 		 if(eyesArray[j].x > 0
		 				 && eyesArray[j].y > (image.rows() /4) && (eyesArray[j].y + eyesArray[j].height) < (image.rows() /2)){
		 			 int rectSize = eyesArray[j].height * eyesArray[j].width;
		 			 if(rectSize < minEyeRectSize){
		 				 possibleEyeIndex = j;
		 				 rectSize = minEyeRectSize;
		 			 }
		 		 }
		 	 }
		 	 if(possibleEyeIndex != -1) {
		 		 result = eyesArray[possibleEyeIndex];
		 	 }
		 	 return result;
		 }
}
