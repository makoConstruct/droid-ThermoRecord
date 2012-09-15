package com.mako;
import android.view.*;
import android.content.*;
import android.app.Service;
import android.os.BatteryManager;
import android.os.IBinder;
import java.util.Timer;
import java.util.Iterator;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import com.mako.Datum;
import com.mako.TimeSpan;
import com.mako.SummaryLayer;
import com.mako.mainact;

public class recording extends Service{
	public static final long MEASURE_INTERVAL_IN_MILLISECONDS = 1000* 60* 5; //it's a bit hard-coded /= Lame but I'm sure it'll ever matter;
	protected int currentTemperature;
	protected boolean isCharging;
	protected boolean isBeingUsed;
	protected ScheduledThreadPoolExecutor timer;
	protected ScheduledFuture<?> future;
	protected Runnable recordingTask = new Runnable(){
		@Override public void run(){
			//take our variables and push a protobuf datum to recordingStream;
			
			//if activity:thermo is bound, pass it the new datum.
			
		}
	};
	protected BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			currentTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
			isCharging = ((intent.getIntExtra(BatteryManager.EXTRA_STATUS,0) & BatteryManager.BATTERY_STATUS_CHARGING) != 0);
		}};
	protected BroadcastReceiver screenIsOnReciever = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){   isBeingUsed = true;   }};
	protected BroadcastReceiver screenIsOffReciever = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){   isBeingUsed = false;   }};
	@Override public int onStartCommand(Intent intent, int flags, int startId){
		this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		this.registerReceiver(screenIsOnReciever,  new IntentFilter(Intent.ACTION_SCREEN_ON)); //using SCREEN_ON instead of USER_PRESENT as screen is likely to tax the battery even without active use. I suppose the variable this binds to is a misnomer;
		this.registerReceiver(screenIsOffReciever, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		timer = new ScheduledThreadPoolExecutor(1); //only one.. No action at a distance for me, thanks =J
		return START_STICKY;
	}
	public void startRecording(){
		//open db
		//create db?
		//int initialDelay = 
		future = timer.scheduleAtFixedRate(
			recordingTask,
			0,
			MEASURE_INTERVAL_IN_MILLISECONDS,
			java.util.concurrent.TimeUnit.MILLISECONDS );
	}
	public void haltRecording(){
		future.cancel(false); //will this actuall prevent repetition?
		//close db connection;
	}
	@Override
	public IBinder onBind(Intent intent){return null;}
}