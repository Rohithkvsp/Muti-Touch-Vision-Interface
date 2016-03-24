package com.example.cameratouch;

import java.io.IOException;
import java.net.InetAddress;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;


@SuppressLint("NewApi")
public class MainActivity extends Activity implements CvCameraViewListener2 {
	
	private String oscIP;
	private int oscPort;
	private static final int REQUEST_CODE_SETTINGS = 0;
	Zoomcameraview zoomcameraview;
	private int mCameraIndex;
	Detect detect;
	 private boolean detectoption=false;
	 private int indexvalue=0;
	 private Mat mRgba;
	 private Mat mProcessed;
	 private String STATE_INDEX="state";
	 private Mat rect_Points;
	 public static float screenheight;
	 public static float screenwidth; 
	 public static float matheight;
	 public static float matwidth; 
	 public float scale;
	 public float xscale;
	 public float yscale;
	 private Graphicsurface gs;
	 
	 static {
		    if (!OpenCVLoader.initDebug()) {
		        // Handle initialization error
		    } else {
		        System.loadLibrary("process");
		        
		    }
		}
	
	 
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                	zoomcameraview.enableView();
                	screenwidth= (float)zoomcameraview.getWidth();
	 		         screenheight=(float)zoomcameraview.getHeight();
	 				 detect=new Detect();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Window window=getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(savedInstanceState!=null)
        {
        	indexvalue=savedInstanceState.getInt(STATE_INDEX,0);
        	detectoption=savedInstanceState.getBoolean("Detectoption");
        	
        }
        else
        {
        	indexvalue=0;
        	detectoption=false;
        }
        SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
        
        oscIP = settings.getString("myIP", "192.168.1.2");
        oscPort = settings.getInt("myPort", 3333);
        
        final FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new FrameLayout.LayoutParams(  FrameLayout.LayoutParams.MATCH_PARENT,  FrameLayout.LayoutParams.MATCH_PARENT));
       setContentView(layout);
       
       FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
       lp.bottomMargin=20;
       lp.topMargin=0;
       lp.leftMargin=200;
       lp.rightMargin=200;
       lp.setMargins(lp.leftMargin, lp.topMargin,lp.rightMargin, lp.bottomMargin);
       lp.gravity = Gravity.BOTTOM;
       
       SeekBar seekBar = new SeekBar(this);
       
       seekBar.setLayoutParams(lp);
 
      
       zoomcameraview = new Zoomcameraview(this, mCameraIndex);   
       zoomcameraview.setVisibility(SurfaceView.VISIBLE);
       //zoomcameraview.setZoomControl((SeekBar) findViewById(R.id.CameraZoomControls));
       zoomcameraview.setZoomControl(seekBar);
       
       zoomcameraview.setCvCameraViewListener(this);
       zoomcameraview.find_size();
       zoomcameraview.setOSCConnection(oscIP, oscPort);
       zoomcameraview.create_osc();
       zoomcameraview.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
       
       
        layout.addView(zoomcameraview); 


        gs=new Graphicsurface(this);
        gs.getHolder().setFormat( PixelFormat.TRANSPARENT);
        gs.setZOrderOnTop(true);
        gs.setLayoutParams(new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        layout.addView(gs);
        
        layout.addView(seekBar); 
        
     /*   zoomcameraview = (Zoomcameraview)findViewById(R.id.ZoomCameraView);
        zoomcameraview.setVisibility(SurfaceView.VISIBLE);
        zoomcameraview.setZoomControl((SeekBar) findViewById(R.id.CameraZoomControls));
        zoomcameraview.setCvCameraViewListener(this);
        zoomcameraview.find_size();
        zoomcameraview.setOSCConnection(oscIP, oscPort);
        zoomcameraview.create_osc();
       */ 
        
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (zoomcameraview!= null)
        {
            zoomcameraview.disableView(); 
            zoomcameraview.destroy_osc();
        }
        if(gs!=null)
		{
			gs.surfaceDestroyed(gs.holder);
		}
    }

    public void onDestroy() {
        super.onDestroy();
        if (zoomcameraview != null)
        	zoomcameraview.disableView();
        if(gs!=null)
		{
			gs.surfaceDestroyed(gs.holder);
		}
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this,mLoaderCallback );
        zoomcameraview.create_osc();
    }
    
    @Override
   	public void onSaveInstanceState(Bundle savedInstanceState)
       {
       	savedInstanceState.putInt(STATE_INDEX,indexvalue);
       	savedInstanceState.putBoolean("Detectoption",detectoption);
       	 
       }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {   	
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main, menu);
    	return true;
    }

    
    /**
     * Called when the user selects an Item in the Menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
    	// Handle item selection
        switch (item.getItemId()) {
	        case R.id.settings:
	           this.openSettingsActivity();
	            return true;
	        case R.id.item1:
				Toast.makeText(this, "Detected", Toast.LENGTH_SHORT).show();
				detectoption=true;
				
				if(indexvalue==1)
				{
				  recreate();
				}
				
				indexvalue=1;
				break;
	        
	        default:
				break;
			}
	       return super.onOptionsItemSelected(item);	
	       
    }
    

    private void openSettingsActivity (){
    	Intent myIntent = new Intent();
    	myIntent.setClassName(this,"com.example.cameratouch.SettingsActivity"); 
    	myIntent.putExtra("IP_in", oscIP);
    	myIntent.putExtra("Port_in", oscPort);
        startActivityForResult(myIntent, REQUEST_CODE_SETTINGS);
    }
    
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        
   	 // See which child activity is calling us back.
   	if(requestCode == REQUEST_CODE_SETTINGS){
       	
       	switch (resultCode){
       	
       		case RESULT_OK:
       			Bundle dataBundle = data.getExtras(); 
       		            			
       	    	String ip = dataBundle.getString("IP");
       	    	
       	    	try { InetAddress.getByName(ip); } 
       	    	catch (Exception e) {
       	    		Toast.makeText(this, "Invalid host name or IP address!", Toast.LENGTH_LONG).show();
       			}
       	    	
       	    	int port = 3333;
       	    	try { port = Integer.parseInt(dataBundle.getString("Port")); }
       	    	catch (Exception e) { port = 0; }
       	    	if (port<1024) Toast.makeText(this, "Invalid UDP port number!", Toast.LENGTH_LONG).show();
       	    		
       	    	this.oscIP = ip;
           	    this.oscPort = port;        	
           	    Log.v("oscIP",ip);
           	    Log.v("oscPort", String.valueOf(port));
           	    zoomcameraview.setNewOSCConnection(oscIP, oscPort);

       	    	/* Get preferences, edit and commit */
           	    SharedPreferences settings = this.getPreferences(MODE_PRIVATE);
           	    SharedPreferences.Editor editor = settings.edit();
           	    
           	    /* define Key/Value */
           	    editor.putString("myIP", this.oscIP);
           	    editor.putInt("myPort", this.oscPort);
           	   
           	    /* save Settings*/
           	    editor.commit();            	    	        			
        	    	
       	    	break;
       	    
       	    
       	    default:
       	    	// Do nothing
       		
       	}
   	}
   }

  /*  public void measure(int screenwidth,int screenheight,int matwidth, int matheight  )
    {
    	scale=Math.min(((float)screenwidth)/matwidth, ((float)screenheight)/matheight);
    	xscale=(screenwidth-scale*matwidth)/2;
       	yscale=(screenheight-scale*matheight)/2;
    	zoomcameraview.setValues(scale,xscale,yscale);
    	gs.setValues(scale,xscale,yscale);
    }
    */
	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		mRgba = new Mat();
		mProcessed= new Mat();	
	   
	   zoomcameraview.caluclate(width, height);
	   gs.setValues(width,height,zoomcameraview.getWidth(), zoomcameraview.getHeight());
	   
	}
	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		// TODO Auto-generated method stub
		rect_Points = new Mat();
		mRgba=inputFrame.rgba();
		Core.flip(mRgba, mRgba,-1); //for nexus 5x only
		if(detectoption==true)
		{
			detect.process(mRgba,mProcessed,rect_Points);
			/*matwidth = mRgba.cols();
			matheight = mRgba.rows();
			scale=Math.min(screenwidth/ mRgba.cols(), screenheight/ mRgba.rows());
	    	xscale=(screenwidth-scale*matwidth)/2;
	       	yscale=(screenheight-scale*matheight)/2;*/
	       	
			/*if(zoomcameraview.handler!=null)
			{
				Message msg = zoomcameraview.handler.obtainMessage();
				
				  msg.obj=rect_Points;
				
				 Log.v("activity mat", ((Mat) msg.obj).dump());
		
				msg.what=1;
				
	            Bundle msgBundle = new Bundle();
	            msgBundle.putString("btdata", "cool");
	          
	            msgBundle.putFloat("scale", scale);
	            msgBundle.putFloat("x_scale", xscale);
	            msgBundle.putFloat("y_scale", yscale);
	            msgBundle.putInt("matwidth", mRgba.cols());
	            msgBundle.putInt("matheight", mRgba.rows());
	        
	            
	            msg.setData(msgBundle);
	            zoomcameraview.handler.sendMessage(msg);
			}
			*/
			
			
			zoomcameraview.setMat(rect_Points);
			gs.setMat(rect_Points);
	    	//zoomcameraview.setValues(scale,xscale,yscale,rect_Points);
	    	
	    	
			
		    /*Log.v("rect_points" ,rect_Points.dump());
		    Log.v("rect_Points"  , String.valueOf(rect_Points.rows()+","+rect_Points.cols()+","+rect_Points.channels()));
		    for(int i=0;i<rect_Points.rows();i++)
		    {
		    	Log.v("rect point array" ,String.valueOf(rect_Points.get(i,0)[0]+","+rect_Points.get(i,0)[1]));
			    	
		    }
		    */
		    if ( rect_Points != null) 
		    	rect_Points.release();
		    rect_Points = null;
		    
		}
		else
		{
			detect.noprocess(mRgba,mProcessed);
		}
		return mProcessed;
	}





}


