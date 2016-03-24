package com.example.cameratouch;


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Detect {
	public Detect()
	{
		
	}
	public void process(Mat mRgba, Mat mProcessed,Mat rect_Points) {
		//Imgproc.cvtColor( mRgba, mProcessed, Imgproc.COLOR_RGBA2GRAY);
		process(mRgba.getNativeObjAddr(), mProcessed.getNativeObjAddr(), rect_Points.getNativeObjAddr());
	}
	public void noprocess(Mat mRgba, Mat mProcessed)
	{   
		
		mRgba.copyTo(mProcessed);
	}
	public native int process(long matAddrRgba, long matAddrGray,long mat_rect_Points);
    
}
