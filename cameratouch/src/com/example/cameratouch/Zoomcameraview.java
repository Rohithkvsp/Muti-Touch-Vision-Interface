package com.example.cameratouch;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;


public class Zoomcameraview extends JavaCameraView {
	private OSCInterface oscInterface ;
	private ArrayList<TuioPoint> tuioPoints;
	private String sourceName;
	private long startTime;
	private long lastTime = 0;
	
	private int width,height;
	private float scale = 1.0f;
	private int counter_fseq = 0;
	private int cw, ch = 0;
	private int bx, by = 0;
	private boolean running = false;
	private int sessionId = 0;
	public boolean sendPeriodicUpdates=true;
	private float xoffset,yoffset;
	private float m_scale;
	private static final int FRAME_RATE = 60;
	private Mat rect_mat,rect_mat1;
	private Mat src_mat;
    private Mat dst_mat;
    private Mat perspectiveTransform;
    private boolean found=false;
    int x_w=2*180;
    int y_h=2*100;
    Looper looper;
	 Handler handler;
	public Zoomcameraview(Context context, int cameraId) {
		 super(context, cameraId);
		 rect_mat=new Mat();
		 rect_mat1=new Mat();
		 src_mat=new Mat(4,1,CvType.CV_32FC2);
		 dst_mat=new Mat(4,1,CvType.CV_32FC2);
		 perspectiveTransform=new Mat();
		 
		 
    }

    public Zoomcameraview(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	rect_mat=new Mat();
    }
    
   
    protected SeekBar seekBar;

    public void setZoomControl(SeekBar _seekBar)
    {
    	seekBar=_seekBar;
    }
    
    protected void enableZoomControls(Camera.Parameters params)
    {
    
    	 final int maxZoom = params.getMaxZoom();
    	 seekBar.setMax(maxZoom);
    	 seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
             int progressvalue=0;
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				// TODO Auto-generated method stub
 				progressvalue=progress;
 				Camera.Parameters params = mCamera.getParameters();
                params.setZoom(progress);
                mCamera.setParameters(params);

 				
 				
 			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

 			
         
         }

        );
     
    }
   
    
    protected boolean initializeCamera(int width, int height)
    {
    	
        boolean ret = super.initializeCamera(width, height);
        

        Camera.Parameters params = mCamera.getParameters();
      
        if(params.isZoomSupported())
            enableZoomControls(params);

        mCamera.setParameters(params);

        return ret;
    }

    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    /**	final int actionPeformed = event.getAction();
        
        switch(actionPeformed){
           case MotionEvent.ACTION_DOWN:{
              final float x = event.getX();
              final float y = event.getY();
              Log.v("Touch Down", String.valueOf(x)+","+String.valueOf(y));
              break;
           }
           
           case MotionEvent.ACTION_MOVE:{
              final float x = event.getX();
              final float y = event.getY();
              Log.v("Touch Move", String.valueOf(x)+","+String.valueOf(y));
              break;
           }
        }
    	
    	return true;
    	**/
    	long timeStamp = System.currentTimeMillis() - startTime;
		long dt = timeStamp - lastTime;
		
		//always send on ACTION_DOWN & ACTION_UP
		if ((event.getActionMasked() == MotionEvent.ACTION_DOWN) || (event.getActionMasked() == MotionEvent.ACTION_UP) || (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) || (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)) dt = 1000;
		if (dt>(1000/FRAME_RATE)) lastTime = timeStamp;

		//int pointerCount = event.getPointerCount();
		//android.util.Log.v("PointerCount",""+pointerCount);
		
		/*if (pointerCount > MAX_TOUCHPOINTS) {
			pointerCount = MAX_TOUCHPOINTS;
		}*/
		
	/*	cw = getWidth()/2; // length
		ch = getHeight()/2; //width
		int p=getWidth()/4;//origin shifted to
		int q=getHeight()/4;// origin shifted to
			*/
			if ((event.getActionMasked() == MotionEvent.ACTION_UP) || (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP)) {				
	         //   if(found)
	           // {
				int i =  event.getActionIndex();

				for (int j = 0; j < tuioPoints.size(); j++) {
														
					if(event.getPointerId(i) == tuioPoints.get(j).getTouchId()){
						tuioPoints.remove(j);
						break;
					}	
			  }
			//}
			
			} else if ((event.getActionMasked() == MotionEvent.ACTION_DOWN) || (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)) {

				/*int i =  event.getActionIndex();
				int id = event.getPointerId(i);
				float x = event.getX(i);
				float y = event.getY(i);	
				if(found)
				{
				 float x1=get_wraped_touch_X(x,y);
				 float y1=get_wraped_touch_Y(x,y);
				 Log.v("x1,y1",String.valueOf(x1+","+y1));
				}
                x=x-p;
                y=y-q;
				// add new Point
                if(x>0&&y>0&&x<cw&&y<ch)
                {
				tuioPoints.add(new TuioPoint(sessionId,id,x/cw,y/ch,timeStamp));
				sessionId++;
                }
                */
				if(found)
				{
				 int i =  event.getActionIndex();
				 int id = event.getPointerId(i);
				 float x = event.getX(i);
			   	 float y = event.getY(i);
				 float x1=get_wraped_touch_X(x,y);
				 float y1=get_wraped_touch_Y(x,y);
				 Log.v("x1,y1",String.valueOf(x1+","+y1));
				 if(x1>0&&y1>0&&x1<x_w&&y1<y_h)
				 {
				 tuioPoints.add(new TuioPoint(sessionId,id,x1/x_w,y1/y_h,timeStamp));
				 sessionId++;
				 }
				}

			} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {

				// update existing Points
				for (int i = 0; i < event.getPointerCount(); i++) {
				//
					if(found)
					{
						int id = event.getPointerId(i);
						float x = event.getX(i);
						float y = event.getY(i);
					 float x1=get_wraped_touch_X(x,y);
					 float y1=get_wraped_touch_Y(x,y);
					 if(x1>0&&y1>0&&x1<x_w&&y1<y_h)
					 {
					 Log.v("x1,y1",String.valueOf(x1+","+y1));
					 for(int j=0; j<tuioPoints.size(); j++){
							
							if(tuioPoints.get(j).getTouchId() == id){
								 tuioPoints.get(j).update(x1/x_w,y1/y_h,timeStamp);
								 break;	
							}
						 }
					 }
					}
					//
					/*int id = event.getPointerId(i);
					float x = event.getX(i);
					float y = event.getY(i);
					if(found)
					{
					 float x1=get_wraped_touch_X(x,y);
					 float y1=get_wraped_touch_Y(x,y);
					 Log.v("x1,y1",String.valueOf(x1+","+y1));
					}
					
					x=x-p;
	                y=y-q;
					// add new Point
	                if(x>0&&y>0&&x<cw&&y<ch)
	                {
					
					for(int j=0; j<tuioPoints.size(); j++){
						
						if(tuioPoints.get(j).getTouchId() == id){
							 tuioPoints.get(j).update(x/cw,y/ch,timeStamp);
							 break;	
						}
					 }
	               }
	                */
				}	
			}
			

		if ((!sendPeriodicUpdates) && (dt>(1000/FRAME_RATE)) )
		{
			sendTUIOdata();
		     process();
		}
		
		
			
		return true;
    }
    
    
    public void create_osc()
    {
    	running = true;

		new Thread(new Runnable() {
		    public void run() {
		    	
		    	//HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
		      //  handlerThread.start();
		 
		        // Get the looper from the handlerThread
		        // Note: this may return null
		    //     looper = handlerThread.getLooper();
		 ///       handler = new Handler(looper,callback );
		        
		      boolean network = oscInterface.isReachable();
		      while (running) {
		    	  
    			  oscInterface.checkStatus();
    			  boolean status = oscInterface.isReachable();
		    	  if (network!=status) {
		    		  network = status;
		    		  sourceName = "TUIOdroid@"+getLocalIpAddress();
		    		  //System.out.println("Source Name: " +sourceName);
		    			    		  
		    	  }
		    	 
		    	  if (sendPeriodicUpdates) {
		    		  try {
		    			 
		    			  process();
		    		   sendTUIOdata();
		    		   
		    		 }
		    	  	  catch (Exception e) {}
		    	  }
		    	  try { Thread.sleep(1000/FRAME_RATE); }
		    	  catch (Exception e) {}
		      }
		  //    handlerThread.quit();
		    }
		}).start();
		
    }
    
   /*
    
    Handler.Callback callback = new Handler.Callback() {
        public boolean handleMessage(Message msg) {
        	
        	
        	 Log.v("Handler what", String.valueOf(msg.what));
      
        	 Log.v("Handler Mat", ((Mat)msg.obj).dump());
        	 Bundle b;
        	 b=msg.getData();
        	 Log.v("Handler data", b.getString("btdata"));
        	
        	 Log.v("Handler sclae",  String.valueOf(b.getFloat("scale")));
        	 Log.v("Handler xscale", String.valueOf(b.getFloat("x_scale")));
        	 Log.v("Handler yscale", String.valueOf(b.getFloat("y_scale")));
        	 Log.v("Handler matwidth", String.valueOf(b.getInt("matwidth")));
        	 
        	 Log.v("Handler matheight", String.valueOf(b.getInt("matheight")));
        	 Log.v("Handler screen width", String.valueOf(getWidth()));
        	 Log.v("Handler screen height", String.valueOf(getHeight()));
        	 
        	float m_width=(float)b.getInt("matwidth");
        	 float m_height=(float)b.getInt("matheight");
        	 float s_width=(float)getWidth();
        	 float s_height=(float)getHeight();
        	 float widthratio=s_width/m_width;
        	 float heightraio=(float)s_height/m_height;
        	 Log.v("handler ratio", String.valueOf(widthratio)+","+String.valueOf(heightraio));
        	 
        	 m_scale=Math.min(((float)s_width)/m_width, ((float)s_height)/ m_height);
        	 xoffset=(s_width-m_scale*m_width)/2;
        	 yoffset=(s_height-m_scale*m_height)/2;
        	 Log.v("handler touch", String.valueOf(m_scale)+","+String.valueOf(xoffset)+","+String.valueOf(yoffset));
        	
        	 ///rect_mat1.copyTo((Mat)msg.obj);
        	 //if(msg.obj!=null&&((Mat)msg.obj).rows()>0)
        	// ((Mat)msg.obj).copyTo(rect_mat1);
        	          
        	
        	return true;
        	
        }
    };
    */
    
    public void caluclate(int mat_width,int mat_height)
    {
     float m_width=(float)mat_width;
   	 float m_height=(float)mat_height;
   	 float s_width=(float)getWidth();
   	 float s_height=(float)getHeight();
   	 float widthratio=s_width/m_width;
   	 float heightraio=(float)s_height/m_height;
   	 Log.v("calculate", String.valueOf(widthratio)+","+String.valueOf(heightraio));
   	 
   	 m_scale=Math.min(((float)s_width)/m_width, ((float)s_height)/ m_height);
   	 xoffset=(s_width-m_scale*m_width)/2;
   	 yoffset=(s_height-m_scale*m_height)/2;
   	 Log.v("caluclate", String.valueOf(m_scale)+","+String.valueOf(xoffset)+","+String.valueOf(yoffset));
   	
    }
    
    public void destroy_osc()
    {
    	running = false;
		tuioPoints.clear();
		//counter_fseq = 0;
		sessionId = 0;
		startTime = System.currentTimeMillis();
		lastTime = 0;
    }
    
    public void sendTUIOdata () throws ArrayIndexOutOfBoundsException {
    	
		OSCBundle oscBundle = new OSCBundle();

		/*
		 * SOURCE Message
		 */
		Object outputData[] = new Object[2];
		outputData[0] = "source";
		outputData[1] = sourceName;
		//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
		oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));
		
		/*
		 * ALIVE Message
		 */
		outputData = new Object[tuioPoints.size() + 1];
		outputData[0] = "alive";
	
		for (int i = 0; i < tuioPoints.size(); i++) {
			outputData[1 + i] = (Integer)tuioPoints.get(i).getSessionId(); // ID
		}

		//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
		oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));

		
		/*
		 * SET Message
		 */
		for (int i = 0; i < tuioPoints.size(); i++) {

			outputData = new Object[7];

			outputData[0] = "set";
			outputData[1] = (Integer) tuioPoints.get(i).getSessionId(); // ID

			outputData[2] = (Float) tuioPoints.get(i).getX(); // x KOORD
			outputData[3] = (Float) tuioPoints.get(i).getY(); // y KOORD

			outputData[4] = (Float) tuioPoints.get(i).getXVel(); // Velocity Vector X
			outputData[5] = (Float) tuioPoints.get(i).getYVel(); // Velocity Vector Y

			outputData[6] = (Float) tuioPoints.get(i).getAccel(); // Acceleration

			//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
			oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));
		}

		
		/*
		 * FSEQ Message
		 */
		outputData = new Object[2];
		outputData[0] = (String) "fseq";
		outputData[1] = (Integer) counter_fseq;
		counter_fseq++;

		//oscInterface.printOSCData(new OSCMessage("/tuio/2Dcur", outputData));
		oscBundle.addPacket(new OSCMessage("/tuio/2Dcur", outputData));

		/*
		 * Sending bundle
		 */
		oscInterface.sendOSCBundle(oscBundle);
	}
    
  

    public void setMat(Mat rectmat)
	{
		rectmat.copyTo(rect_mat);
		
	}
	
    
    public void process()
    {
    	/*  Log.v("touch perspective row,cols" ,String.valueOf(perspectiveTransform.rows()
                  +","+perspectiveTransform.cols()));
            */
    	// Log.v("touch scale", String.valueOf(m_scale)+","+String.valueOf(xoffset)+","+String.valueOf(yoffset));
    	   
    //	Log.v("touch rect_mat" ,rect_mat.dump());
	  //  Log.v("touch rect_mat"  , String.valueOf(rect_mat.rows()+","+rect_mat.cols()+","+rect_mat.channels()));
	    if(rect_mat.rows()>0)
	        {
	        	float x0=(float)rect_mat.get(0,0)[0]*m_scale+xoffset;
	        	float y0= (float)rect_mat.get(0,0)[1]*m_scale+yoffset;
	        	float x1=(float)rect_mat.get(1,0)[0]*m_scale+xoffset;
	        	float y1= (float)rect_mat.get(1,0)[1]*m_scale+yoffset;
	        	float x2=(float)rect_mat.get(2,0)[0]*m_scale+xoffset;
	        	float y2= (float)rect_mat.get(2,0)[1]*m_scale+yoffset;
	        	float x3=(float)rect_mat.get(3,0)[0]*m_scale+xoffset;
	        	float y3= (float)rect_mat.get(3,0)[1]*m_scale+yoffset;
	        	Log.v("tocuh mat",String.valueOf(x0)+","+String.valueOf(y0));
	        	Log.v("tocuh mat",String.valueOf(x1)+","+String.valueOf(y1));
	        	Log.v("tocuh mat",String.valueOf(x2)+","+String.valueOf(y2));
	        	Log.v("tocuh mat",String.valueOf(x3)+","+String.valueOf(y3));
	        	
	            src_mat.put(0,0,x0,y0,x1,y1,x2,y2,x3,y3);
	            dst_mat.put(0,0,0,0,x_w-1,0,x_w-1,y_h-1,0,y_h-1);
	            perspectiveTransform=Imgproc.getPerspectiveTransform(src_mat, dst_mat);
	            Log.v("touch perspective Mat" ,perspectiveTransform.dump());
	            /*Log.v("touch presp ",String.valueOf(perspectiveTransform.get(0,0)[0] 
	            		+","+perspectiveTransform.get(0,1)[0] +","+perspectiveTransform.get(0,2)[0] 
	            		+","+perspectiveTransform.get(1,0)[0] +","+perspectiveTransform.get(1,1)[0] 
	            	    +","+perspectiveTransform.get(1,2)[0] 
	            	    +","+perspectiveTransform.get(2,0)[0]
	            	    +","+perspectiveTransform.get(2,1)[0] +","+perspectiveTransform.get(2,2)[0]));
	            */
	            Log.v("touch perspective row,cols" ,String.valueOf(perspectiveTransform.rows()
	            		                                            +","+perspectiveTransform.cols()));
                 found=true;
	        }
	        else
	        {
	          found=false;	
	        }
		    	
	   
	    
    }
    
    float get_wraped_touch_X(float x,float y)
    {
    	
    	float top=(float) (perspectiveTransform.get(0,0)[0]*x+perspectiveTransform.get(0,1)[0]*y+perspectiveTransform.get(0,2)[0]);
    	float bottom=(float) (perspectiveTransform.get(2,0)[0]*x+perspectiveTransform.get(2,1)[0]*y+perspectiveTransform.get(2,2)[0]);
    	
    	return top/bottom;
    }
 
    float get_wraped_touch_Y(float x,float y)
    {
    	
    	float top=(float) (perspectiveTransform.get(1,0)[0]*x+perspectiveTransform.get(1,1)[0]*y+perspectiveTransform.get(1,2)[0]);
    	float bottom=(float) (perspectiveTransform.get(2,0)[0]*x+perspectiveTransform.get(2,1)[0]*y+perspectiveTransform.get(2,2)[0]);
    	
    	return top/bottom;
    }
 
    
    public void find_size()
    {
    	DisplayMetrics dm = getResources().getDisplayMetrics();
		this.scale = dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
		this.width = dm.widthPixels;
		this.height = dm.heightPixels;
		
    }
    
    public void setOSCConnection (String oscIP, int oscPort){	
		
		oscInterface = new OSCInterface(oscIP,oscPort);
		startTime = System.currentTimeMillis();
		setFocusable(true); // make sure we get key events
		setFocusableInTouchMode(true); // make sure we get touch events

		tuioPoints = new ArrayList<TuioPoint>();
		sourceName = "TUIOdroid@"+getLocalIpAddress();
		
	}
    
    public void setNewOSCConnection (String oscIP, int oscPort){	
		oscInterface.closeInteface();
		oscInterface = new OSCInterface(oscIP,oscPort);
		sourceName = "TUIOdroid@"+getLocalIpAddress();
	}
    
    public String getLocalIpAddress() {
        
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if ((!inetAddress.isLoopbackAddress()) && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {}
        return "127.0.0.1";
    }
    
   
    
}
