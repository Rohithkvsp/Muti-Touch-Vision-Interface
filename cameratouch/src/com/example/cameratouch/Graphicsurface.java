package com.example.cameratouch;

import org.opencv.core.Mat;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Graphicsurface  extends SurfaceView implements SurfaceHolder.Callback {
	
	public SurfaceHolder holder;
	private GraphicsThread graphicsthread;
	private float xoffset,yoffset;
	private float m_scale;
	private Mat rect_mat;
	
	public Graphicsurface(Context context)
	{
		super(context);
		rect_mat=new Mat();
		graphicsthread=new GraphicsThread(this,context);
		holder=getHolder();
		holder.addCallback(this);
		
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		 graphicsthread.setRunning(true);
		 graphicsthread.start();
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		stopgraphics();
	}
	
	public void setMat(Mat rectmat)
	{
		
		rectmat.copyTo(rect_mat);
		/*Log.v("rect_mat" ,rect_mat.dump());
	    Log.v("rect_mat"  , String.valueOf(rect_mat.rows()+","+rect_mat.cols()+","+rect_mat.channels()));
	    for(int i=0;i<rect_mat.rows();i++)
	    {
	    	Log.v("rect point array" ,String.valueOf(rect_mat.get(i,0)[0]+","+rect_mat.get(i,0)[1]));
		    	
	    }
	*/
		
	}
	public Mat getMat()
	{
		return rect_mat;
	}
	
	public void setValues(int mat_width, int mat_height,int screen_width, int screen_height)
    {
		float m_width=(float)mat_width;
	   	 float m_height=(float)mat_height;
	   	 float s_width=(float)screen_width;
	   	 float s_height=(float)screen_height;
	   	 float widthratio=s_width/m_width;
	   	 float heightraio=(float)s_height/m_height;
	   	 Log.v("graphic cal", String.valueOf(widthratio)+","+String.valueOf(heightraio));
	   	 
	   	 m_scale=Math.min(((float)s_width)/m_width, ((float)s_height)/ m_height);
	   	 xoffset=(s_width-m_scale*m_width)/2;
	   	 yoffset=(s_height-m_scale*m_height)/2;
	   	 Log.v("garphic cal", String.valueOf(m_scale)+","+String.valueOf(xoffset)+","+String.valueOf(yoffset));
	   	
   // Log.v("scale", String.valueOf(m_scale)+","+String.valueOf(xoffset)+","+String.valueOf(yoffset));
    }
	
	public float getScale()
	{
	return m_scale;	
	}
	
	public float getXoffset()
	{
	return xoffset;
	}
	
    public float getYoffset()
    {
    return yoffset;	
    }
    
	public void stopgraphics()
	{
		
		boolean retry=true;
		graphicsthread.setRunning(false);
		while(retry)
		{
			try
			{
				graphicsthread.join();
				retry=false;
				
			}
			 catch (InterruptedException e) {
             }
		}
		
	}
	

	
	
}
