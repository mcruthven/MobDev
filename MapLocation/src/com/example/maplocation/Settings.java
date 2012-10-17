package com.example.maplocation;

import java.io.File;
import java.util.ArrayList;
import com.example.maplocation.MainActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Settings extends Activity {
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	int mapx,mapy;
	double clx,cly;
	double convx,convy;
	String MAP_URL;
	WebView webView;
	ImageView overlayimage;
	String fileLoc= Environment.getExternalStorageDirectory()
			+ File.separator + "/overlay3.png";
	Bitmap bmp;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	boolean go=false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		mapURL();
		setupWebView();
		makeOverlay();
		startIt();
	}
	public void startIt() {

		Button checkbox= (Button) findViewById(R.id.button1);

		checkbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
	           	Intent intent = new Intent(Settings.this, MainActivity.class);
 	       		intent.putExtra("gps",  ((CheckBox)findViewById(R.id.checkBox1)).isChecked());
 	       		intent.putExtra("network", ((CheckBox)findViewById(R.id.checkBox3)).isChecked());
 	       		startActivity(intent);
			}
		});
	}
		 	             
		 
		 
	public void mapURL() {
		mapx = 360;
		mapy = 441;
		cly = 42.293294;
		clx = -71.263636;
		MAP_URL = "http://maps.googleapis.com/maps/api/staticmap?center=" + cly
				+ "," + clx + "&zoom=17&size=" + mapx + "x" + mapy
				+ "&sensor=false";
	}

	/** Sets up the WebView object and loads the URL of the page **/
	private void setupWebView() {
		sizeURL();
		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		// Wait for the page to load then send the location information
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(MAP_URL);
	}

	@TargetApi(13)
	public void sizeURL() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
	}

	@TargetApi(11)
	public void makeOverlay() {
		overlayimage = (ImageView) findViewById(R.id.colormap_overlay);
		setBmp();

		int height = bmp.getHeight();
		int width = bmp.getWidth();

		convx = 1.0 * width / mapx;
		convy = 1.0 * height / mapy;

	//	offset();

	}

	@SuppressLint("NewApi")
	public void resetBmp(){
		bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.overlayimg);
		bmp.eraseColor(0);
		Log.d("main","IIIIII reset ");
		overlayimage.setImageBitmap(bmp);
		overlayimage.setAlpha((float) .6);
	}
	@SuppressLint("NewApi")
	public void setBmp(){
		File file = new File(fileLoc);
		if (!file.exists()) {	
			Log.d("main","IIIIII read "+file);
			resetBmp();
		} else {
			Log.d("main","IIIIII read ");
			try{
            bmp = BitmapFactory.decodeFile(fileLoc);//, options); 

            adjustOpacity();
            overlayimage.setImageBitmap(bmp);
    		overlayimage.setAlpha((float) .6);
    		Log.d("main","IIII em");
        //    is.close(); 
			}catch (Exception e){
				Log.d("main","IIIIII caught ");
				resetBmp();
			}
		}	
		
	}
	private void adjustOpacity()
	{
		int mx = 360;
		int my = 567;
	    int width = bmp.getWidth();
	    int height = bmp.getHeight();
	    convy = 1.0 * height / my;
	    int h=(int)(height*1.0*mapx/my*convy);
	    
	    int ho=(height-h)/2;
	    Log.d("main","IIII m"+(height-(int)(height*1.0*mapx/my))/2);
	    Bitmap dest = Bitmap.createBitmap(width, h, Bitmap.Config.ARGB_8888);
	    int[] pixels = new int[width * h];
	    bmp.getPixels(pixels, 0, width, 0, ho, width, h);
	    
	    for(int i=0;i<pixels.length;i++){
	    	if(pixels[i]==Color.BLACK){
	    		pixels[i]=0;
	    	}
	    }
	    
	    Log.d("main","IIII   "+pixels[0]);
	    dest.setPixels(pixels, 0, width, 0,0
	    		, width, h);
	    Log.d("main","IIII testing all "+bmp.getHeight());
	    bmp=dest;
	    Log.d("main","IIII testing all"+bmp.getHeight());
	    
	} 


}
