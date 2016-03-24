package com.example.cameratouch;

import org.opencv.core.Mat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.Log;

public class GraphicsThread extends Thread {
	private Graphicsurface animatesurface;
	private final int refresh_rate=6;
	 private Context context;
	 private boolean running = false;
	 private Mat rect_mat;
	 
	 public GraphicsThread(Graphicsurface animatesurface,Context context_)
		{
			
			 this.animatesurface=animatesurface;	
			 context=context_;
			 rect_mat=new Mat();
		}
			
	 

	public void setRunning(boolean run) {
		        running = run;
		  }
			
	  @SuppressLint("NewApi") public void run()
		 {
		 long previousTime, currentTime;  
		 previousTime = System.currentTimeMillis(); 
		  
			     
	     while(running)
		 {
	    	// Log.v("grphics","running");
	      Canvas c=null;
		  currentTime=System.currentTimeMillis();  
	       while ((currentTime-previousTime)<refresh_rate){  
	    	  currentTime=System.currentTimeMillis();  
		      }  
	       
		   previousTime=currentTime;
		   
		   rect_mat=animatesurface.getMat();
		  /* Log.v("rect_mat" ,rect_mat.dump());
		    Log.v("rect_mat"  , String.valueOf(rect_mat.rows()+","+rect_mat.cols()+","+rect_mat.channels()));
		    for(int i=0;i<rect_mat.rows();i++)
		    {
		    	Log.v("rect point array" ,String.valueOf(rect_mat.get(i,0)[0]+","+rect_mat.get(i,0)[1]));
			    	
		    }
		    */
		   try
		   {
			c=animatesurface.getHolder().lockCanvas();
			
			
			 synchronized(animatesurface.getHolder())
			 {
				 
				 Paint paint = new Paint();
			     paint.setColor(Color.BLUE); 
			       paint.setStrokeWidth(4);
			       paint.setStyle(Paint.Style.STROKE);
			     
			       c.drawColor(Color.TRANSPARENT);
				   c.drawColor(0,Mode.CLEAR);  
					//c.drawLine(0,0,400,400, paint);
			      /*int cw = c.getWidth()/2; // length
					int ch = c.getHeight()/2; //width
					int X=c.getWidth()/4;//origin shifted to
					int Y=c.getHeight()/4;
			      c.drawRect(X, Y,X+cw , Y+ch, paint);
			     */
			      
			       for(int j=0;j<rect_mat.cols();j++)
				    {
			    	  for(int i=0;i<rect_mat.rows();i++)
			    	  {
				   // 	Log.v("GT rect point array" ,String.valueOf(rect_mat.get(i,0)[0]+","+rect_mat.get(i,0)[1]));
			    	   c.drawLine((float)rect_mat.get(i,j)[0]*animatesurface.getScale()+animatesurface.getXoffset(),
			    			   (float)rect_mat.get(i,j)[1]*animatesurface.getScale()+animatesurface.getYoffset(),
			    			   (float)rect_mat.get((i+1)%4,j)[0]*animatesurface.getScale()+animatesurface.getXoffset(),
			    			   (float)rect_mat.get((i+1)%4,j)[1]*animatesurface.getScale()+animatesurface.getYoffset(),
			    			   paint);
			    	 
			    	  }
				    }
				    
			       
			 }
		    }
		    finally
			{
				
			}if (c != null) {
				animatesurface.getHolder().unlockCanvasAndPost(c);
          }
		 }
		 try {  
            Thread.sleep(refresh_rate-5); // Wait some time till I need to display again  
       } catch (InterruptedException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
       }   
    }
	  
	  
	  
	  

}
