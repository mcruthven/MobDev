package com.example.olinsignal;

import java.util.ArrayList;
import java.util.List;



import android.location.*;
import android.os.Bundle;
import 	android.telephony.PhoneStateListener;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MainActivity extends Activity {
	SignalStrengthListener sigStrength;
	TextView signal;
	double lat,lon,alt;
	int old;
	private CommentsDataSource datasource;
	ListView listView;
	Comment comment;
	Location lastLoc=null;
	int strength;
	ArrayAdapter<Comment> adapter;
	LocationManager mLocManager;

	private LocationListener locListener = new MyLocationListener();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Database

		listView= (ListView) findViewById(R.id.listView1);
		datasource = new CommentsDataSource(this);
		datasource.open();

		//List<Comment> values = datasource.getAllComments();
		ArrayList<Comment> values = new ArrayList<Comment>();
		// Use the SimpleCursorAdapter to show the
		// elements in a ListView
		adapter = new ArrayAdapter<Comment>(this,
				android.R.layout.simple_list_item_1, values);
		listView.setAdapter(adapter);
		// Sig Stuff

		signal= (TextView)findViewById(R.id.signal_text);
		sigStrength = new SignalStrengthListener();	           
		Log.d("tatg",""+sigStrength.getStrength());

		//locate();

		mLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		ButtonListener();

	}


	public class MyLocationListener implements LocationListener{        

		public void onLocationChanged(Location loc) {
			MainActivity.this.lon=loc.getLongitude();
			MainActivity.this.lat=loc.getLatitude();
			MainActivity.this.alt=loc.getAltitude();
			String message = String.format(
					"New Location \n Longitude: %1$s \n Latitude: %2$s",
					loc.getLongitude(), loc.getLatitude()
					);
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
		}

		public void onProviderDisabled(String arg0) {

		}
		public void onProviderEnabled(String provider) {

		}
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}       
	}
	public void ButtonListener() {

		Button checkbox= (Button) findViewById(R.id.button1);

		checkbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("Main","just happened");
				Log.d("xyzs","________________________-"+mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
				mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locListener);
				lastLoc=mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			/*	}else if(mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
					mLocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locListener);
					lastLoc=mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
				
				*/
				
				
				((TelephonyManager)getSystemService(TELEPHONY_SERVICE)).listen(sigStrength,SignalStrengthListener.LISTEN_SIGNAL_STRENGTHS);

				//	((TextView)findViewById(R.id.signal_text2)).setText(" ");

				//if (lastLoc!=null)
				if( lastLoc!=null){
					double nalt=lastLoc.getAltitude();
					double nlat=lastLoc.getLatitude();
					double nlon=lastLoc.getLongitude();
					if ((nalt!=alt || nlat!=lat || nlon!=lon || (strength!=old))&&strength!=0){
					old=strength;
					lat=nlat;
					lon=nlon;
					alt=nalt;
					
					adapter.add(datasource.createComment(" "+lastLoc.getAltitude()+" "+lastLoc.getLatitude()+" "+lastLoc.getLongitude()+" "+strength));
					
					((TextView)findViewById(R.id.signal_text2)).setText("loc "+lastLoc.getLatitude()+" "+lastLoc.getLongitude()+" "+lastLoc.getAccuracy()+" "+strength);

				}
				}


			}



		});

	}


	private class SignalStrengthListener extends PhoneStateListener
	{	
		public String strength2;
		int old=0;
		int strengthAmplitude;
		LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
		@Override
		public void onSignalStrengthsChanged(android.telephony.SignalStrength signalStrength) {

			strengthAmplitude = signalStrength.getCdmaDbm();
			strength2=String.valueOf(strengthAmplitude);		
			super.onSignalStrengthsChanged(signalStrength);
			MainActivity.this.strength=strengthAmplitude;

		}
		public int getStrength(){
			return strengthAmplitude;
		}

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();
	}
	@Override
	protected void onStop() {
		super.onStop();
		mLocManager.removeUpdates(locListener);
	
	}
	@Override
	protected void onPause() {
		datasource.close();
		mLocManager.removeUpdates(locListener);
		super.onPause();
	}


}
