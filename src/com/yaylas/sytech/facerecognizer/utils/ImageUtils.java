package com.yaylas.sytech.facerecognizer.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ImageUtils {
	
	public static double getDistance(Point p1, Point p2){
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}
	
	public static double getRotationToAlign(Point left_eye, Point right_eye){
		double eye_direction[] = {right_eye.x - left_eye.x, right_eye.y - left_eye.y};
		double distance = getDistance(left_eye, right_eye);
		double deltaY = right_eye.y - left_eye.y;
		double deltaX = right_eye.x - left_eye.x;
		if(deltaY == 0){
			return 0;
		}
		
		double rotation = Math.atan(deltaY/ deltaX) * 180 / Math.PI;
		return rotation;
	}
	
	public static Mat cropFace(Mat image, Point left_eye, Point right_eye,
			double offsetPercentage, int destWidth, int destHeight){
		
		int offset_horizontal = (int)Math.floor(offsetPercentage*(double)destWidth); 
		int offset_vertical = (int)Math.floor(offsetPercentage*(double)destHeight);
		
		double distance = getDistance(left_eye, right_eye);

		double rotation = getRotationToAlign(left_eye, right_eye);
		double reference = destWidth - 2*offset_horizontal;
		double scaleFactor = distance / reference;
		
		
    	Mat rot_mat = Imgproc.getRotationMatrix2D(left_eye, rotation, 1.0);
    	Mat dst = new Mat(image.rows(), image.cols(), image.type());
    	Imgproc.warpAffine(image, dst, rot_mat, new Size(destWidth, destHeight), Imgproc.INTER_CUBIC);
    	
    	Size size = new Size(destWidth*scaleFactor, destHeight*scaleFactor);
    	Point ctr = new Point(left_eye.x - scaleFactor*offset_horizontal+size.width/2,
    			left_eye.y - scaleFactor*offset_vertical+size.height/2);
    	
    	Mat cropped = new Mat();
    	Imgproc.getRectSubPix(dst, size, ctr, cropped);
    	Mat returnMatrix = new Mat();
    	Imgproc.resize(cropped, returnMatrix, new Size(destWidth, destHeight));
		
    	return returnMatrix;
	}
	
	public static boolean rectanglesOverlapping(Rect rect1, Rect rect2){
		return checkCorners(rect1, rect2) || checkCorners(rect2, rect1);
	}
	
	private static boolean checkCorners(Rect rect1, Rect rect2){
		int x1 = rect1.x;
		int x2 = rect2.x;
		int width1 = rect1.width;
		int width2 = rect2.width;
		int y1 = rect1.y;
		int y2 = rect2.y; 
		int height1 = rect1.height;
		int height2 = rect2.height;
		int cornerX = x1;
		int cornerY = y1;
		//Check corners
		if(cornerX<=(x2+width2) && cornerX >=x2 && cornerY >= y2 && cornerY<=(y2+height2)){
			return true;
		} 
		cornerY = y1 + height1;
		if(cornerX<=(x2+width2) && cornerX >=x2 && cornerY >= y2 && cornerY<=(y2+height2)){
			return true;
		} 
		cornerX = x1 + width1;
		if(cornerX<=(x2+width2) && cornerX >=x2 && cornerY >= y2 && cornerY<=(y2+height2)){
			return true;
		} 
		cornerY = y1;
		if(cornerX<=(x2+width2) && cornerX >=x2 && cornerY >= y2 && cornerY<=(y2+height2)){
			return true;
		} 
		return false;
	}
	
	
	
	
	
    public static native void saveImageAsPGM(String savingPath, long image);
}
