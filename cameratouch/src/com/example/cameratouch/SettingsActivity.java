
package com.example.cameratouch;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity that provides Settings for the user
 * @author Tobias Schwirten
 * @author Martin Kaltenbrunner
 */

public class SettingsActivity extends Activity{

	/**
	 *  Called when the activity is first created. 
	 */
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.settingslayout);
	        
	        Button btn_OK = (Button)findViewById(R.id.saveButton);
	        btn_OK.setOnClickListener(listener_OkBtn);
	        
	        //Button btn_KO = (Button)findViewById(R.id.cancelButton);
	        //btn_KO.setOnClickListener(listener_KoBtn);

	        EditText editText_IP = (EditText)findViewById(R.id.et_IP);
	        String ip = (getIntent().getExtras().getString("IP_in"));
	        editText_IP.setText(ip);
	        
	        EditText editText_port = (EditText)findViewById(R.id.et_Port);
	        int port = getIntent().getExtras().getInt("Port_in");
	        editText_port.setText(Integer.toString(port));
	        
	        TextView ipView = (TextView)findViewById(R.id.localIP);
	        String localIP = getLocalIpAddress();
	        if (localIP!=null) ipView.setText("local IP: "+localIP);
	        else {
	        	ipView.setTextColor(Color.RED);
	        	ipView.setText("no active network connection found!");
	        }	        
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
	        return null;
	    }
	 
	 
	 /**
	  * Listener for the OK button
	  */
	 private OnClickListener listener_OkBtn = new OnClickListener(){
	        
		  public void onClick(View v){      
			  
     	    	String ip = ((TextView) findViewById(R.id.et_IP)).getText().toString();
    	    	
    	    	try { InetAddress.getByName(ip); } 
    	    	catch (Exception e) { 
    	    		((TextView) findViewById(R.id.et_IP)).setText("invalid address");
    	    		return;
    	    	}
    	    	
    	    	int port = 3333;
    	    	try { port = Integer.parseInt(((TextView) findViewById(R.id.et_Port)).getText().toString()); }
    	    	catch (Exception e) { port = 0; }
    	    	if (port<1024) {
    	    		((TextView) findViewById(R.id.et_Port)).setText("invalid port");
    	    		return;
    	    	}

			  
	           Intent responseIntent = new Intent();
	           
	           responseIntent.putExtra("IP",((TextView) findViewById(R.id.et_IP)).getText().toString());
	           responseIntent.putExtra("Port", ((TextView) findViewById(R.id.et_Port)).getText().toString());
	           
	           /*Setting result for this activity */
	           setResult(RESULT_OK, responseIntent);
	           
	           finish();
	        }
	    };    

/**
 * Listener for the Cancel button
 */
/*private OnClickListener listener_KoBtn = new OnClickListener(){
       
	  public void onClick(View v){                          
          finish();
       }
   };*/ 



}
