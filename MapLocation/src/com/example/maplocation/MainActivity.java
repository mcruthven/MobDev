package com.example.maplocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int mapx, mapy, cx, cy;
	private static String MAP_URL;
	private WebView webView;
	private double convx, convy, clx, cly, conlx, conly;
	private Bitmap bmp;
	private final int b = Color.CYAN;
	private final int black = Color.BLACK;
	String fileLoc = Environment.getExternalStorageDirectory() + File.separator
			+ "/overlay3.png";
	int dbmstart = -40;
	int dbmend = -120;
	boolean start = false;
	boolean created = false;
	boolean startcol = false;
	SignalStrengthListener sigStrength;
	double lat, lon, alt;
	int old;
	boolean save = false;
	int red = Color.RED;
	private int diff = black - b;
	private final int blue = Color.CYAN + diff / 2;
	Location lastLoc = null;
	int strength, wid;
	private LocationManager mLocManager, mLocManagerN;
	boolean gps;
	boolean network;
	ImageView overlayimage;
	GestureDetector mGestureDetector;
	private LocationListener locListener = new MyLocationListener();
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	View.OnTouchListener gestureListener;

	@Override
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		created = true;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gps = this.getIntent().getExtras().getBoolean("gps");
		network = this.getIntent().getExtras().getBoolean("network");
		mapURL();
		setupWebView();
		makeOverlay();
		sigStrength = new SignalStrengthListener();

		Log.d("main", "IIIIII created");

		overlayimage.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Log.d("main", "qqq fl");
				resetBmp();
				return true;
			}
		});
	}

	public void mapURL() {
		mapx = 360;
		mapy = 567;
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
		int width = size.x;
		int height = size.y;
	}

	@TargetApi(11)
	public void makeOverlay() {
		overlayimage = (ImageView) findViewById(R.id.colormap_overlay);
		setBmp();

		int height = bmp.getHeight();
		int width = bmp.getWidth();

		convx = 1.0 * width / mapx;
		convy = 1.0 * height / mapy;

		offset();

	}

	@SuppressLint("NewApi")
	public void resetBmp() {
		bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.overlayimg);
		bmp.eraseColor(0);
		Log.d("main", "IIIIII reset ");
		overlayimage.setImageBitmap(bmp);
		overlayimage.setAlpha((float) .6);
	}

	@SuppressLint("NewApi")
	public void setBmp() {
		File file = new File(fileLoc);
		if (!file.exists()) {
			Log.d("main", "IIIIII read " + file);
			resetBmp();
		} else {
			Log.d("main", "IIIIII read ");
			// bmp =
			// BitmapFactory.decodeFile(Environment.getExternalStorageDirectory()
			// + File.separator + "/overlay.jpg");
			try {
				bmp = BitmapFactory.decodeFile(fileLoc);// , options);

				adjustOpacity();
				overlayimage.setImageBitmap(bmp);
				overlayimage.setAlpha((float) .6);
				Log.d("main", "IIII em");
				// is.close();
			} catch (Exception e) {
				Log.d("main", "IIIIII caught ");
				resetBmp();
			}
		}

	}

	private void adjustOpacity() {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap dest = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int i = 0; i < pixels.length; i++) {
			if (pixels[i] == Color.BLACK) {
				pixels[i] = 0;
			}
		}

		Log.d("main", "IIII   " + pixels[0]);
		dest.setPixels(pixels, 0, width, 0, 0, width, height);
		Log.d("main", "IIII testing all");
		bmp = dest;

	}

	// calculates gps to pixel multiplier
	public void offset() {
		cy = (int) (mapy / 2 * convy);
		cx = (int) (mapx / 2 * convx);

		double ly = 42.293128;
		double lx = -71.264341;

		double olx = clx - lx;
		double oly = cly - ly;

		int offx = -178;
		int offy = 27;
		conlx = offx / olx;
		conly = offy / oly;

		// changePixelsGPS(lx, ly);
	}

	public void changePixelsGPS(double lx, double ly) {
		int w = wid;
		// if(provider.equals(LocationManager.GPS_PROVIDER)){
		// w=10;
		// }
		int h = (int) (1.0 * w * convy / convx);
		int x = (int) (cx + (clx - lx) * conlx);
		int y = (int) (cy + (cly - ly) * conly);
		changePixels(x, y, w, h);

	}

	@TargetApi(11)
	public void changePixels(int cx, int cy, int w, int h) {
		int x = cx - w / 2;
		int y = cy - h / 2;
		int[] pixels = new int[w * h];
		bmp.getPixels(pixels, 0, w, x, y, w, h);
		diff = 255;
		double m = .2;
		int add = 0;
		if (gps) {
			m = .5;
		}
		if (strength != 0) {
			for (int i = 0; i < pixels.length; i++) {
				add = (int) (diff * ((strength - dbmstart) * 1.0 / (dbmend - dbmstart)));

				save = true;
				if (pixels[i] == 0) {
					pixels[i] = Color.argb(255, add, add, 20);
				} else {
					int r = Color.red(pixels[i]);
					int c = (int) ((r + add) / 2);
					c = (int) ((add - r) * m) + r;
					Log.d("main", "IIII fl" + c + " " + r + " " + add);
					pixels[i] = Color.argb(255, c, c, 233);
				}

			}
		}
		bmp.setPixels(pixels, 0, w, x, y, w, h);
		overlayimage.setImageBitmap(bmp);
		// overlayimage.setAlpha((float) .2);
	}

	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {
			if (loc != null) {
				lon = loc.getLongitude();
				lat = loc.getLatitude();
				alt = loc.getAltitude();
				((TelephonyManager) getSystemService(TELEPHONY_SERVICE))
						.listen(sigStrength,
								SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);
				String message = String.format(
						"New Location \n Longitude: %1$s \n Latitude: %2$s",
						loc.getLongitude(), loc.getLatitude());

				wid = (int) ((101 - loc.getAccuracy()) * 5);
				if (gps) {
					wid = 20;
				}
				start = true;
				if (strength != 0) {
					Toast.makeText(MainActivity.this, "" + strength,
							Toast.LENGTH_SHORT).show();
					changePixelsGPS(lon, lat);
				}
			}

		}

		public void onProviderDisabled(String arg0) {

		}

		public void onProviderEnabled(String provider) {

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	}

	private class SignalStrengthListener extends PhoneStateListener {
		public String strength2;
		int old = 0;
		int strengthAmplitude;

		@Override
		public void onSignalStrengthsChanged(
				android.telephony.SignalStrength signalStrength) {

			strengthAmplitude = signalStrength.getCdmaDbm();
			strength2 = String.valueOf(strengthAmplitude);
			super.onSignalStrengthsChanged(signalStrength);
			MainActivity.this.strength = strengthAmplitude;
			if (start) {
				Toast.makeText(MainActivity.this, "" + strength,
						Toast.LENGTH_SHORT).show();
				changePixelsGPS(lon, lat);
			}

		}

		public int getStrength() {
			return strengthAmplitude;
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("main", "IIIIII writing ??");
		writeFile();
		if (gps) {
			mLocManager.removeUpdates(locListener);
		}
		if (network) {
			mLocManagerN.removeUpdates(locListener);
		}
		start = false;
	}

	@Override
	protected void onResume() {
		super.onResume(); // Always call the superclass method first
		Log.d("main", "IIIII starting");

		// The activity is either being restarted or started for the first time
		// so this is where we should make sure that GPS is enabled
		if (gps) {
			Log.d("main", "IIIII G" + created);
			mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5,
					0, locListener);
		}
		if (network) {
			Log.d("main", "IIIII N" + created);
			mLocManagerN = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			mLocManagerN.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 5, 0, locListener);
		}

	}

	public void writeFile() {
		if (save) {
			File file = new File(fileLoc);
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				Log.d("main", "IIIIII writing " + fos);
				if (fos != null) {
					bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
					fos.flush();
					fos.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Log.d("main", "IIIIII" + file.exists());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onBackPressed() {
		Log.d("main", "on back pressed");
		Intent intent = new Intent(MainActivity.this, Settings.class);
		writeFile();
		startActivity(intent);
	}

}
