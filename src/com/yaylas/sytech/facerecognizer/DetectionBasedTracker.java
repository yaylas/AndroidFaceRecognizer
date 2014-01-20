package com.yaylas.sytech.facerecognizer;

import java.util.Vector;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

import android.widget.Toast;

public class DetectionBasedTracker
{
    public DetectionBasedTracker(String cascadeName, int minFaceSize, boolean isFaceDetector) {
        mNativeObj = nativeCreateObject(cascadeName, minFaceSize, isFaceDetector);
    }

    public long getNativeObj(){
    	return mNativeObj;
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }

    public void detect(Mat imageGray, MatOfRect faces) {
        nativeDetect(mNativeObj, imageGray.getNativeObjAddr(), faces.getNativeObjAddr());
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }
    public static Vector<Mat> imageVector = new Vector<Mat>();
    public static void addElement(Mat m){
    	imageVector.add(m);
    	System.out.println("--------------------------------- elementAdded: "+imageVector.size());
    }
    
    public static long getElementAt(int index){
    	if(index >= imageVector.size() || index < 0){
    		return 0;
    	}
    	
    	System.out.println("-------------------------***** get element: "+imageVector.size()+"  index: "+index);
    	return imageVector.elementAt(index).getNativeObjAddr();
    }
    
    public static int getImageCount(){
    	return imageVector.size();
    }
    

    private long mNativeObj = 0;

    private static native long nativeCreateObject(String cascadeName, int minFaceSize, boolean faceDetection);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    private static native void nativeSetFaceSize(long thiz, int size);
    private static native void nativeDetect(long thiz, long inputImage, long faces);
    
}
