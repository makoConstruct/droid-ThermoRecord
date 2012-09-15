package com.mako;

import android.app.Activity;
import android.os.Bundle;
import android.os.BatteryManager;
import android.view.*;
import android.content.*;
import android.widget.*;
import com.mako.Datum;
import com.mako.TimeSpan;
import com.mako.SummaryLayer;
import java.util.ArrayList;
import java.util.LinkedList;

public class mainact extends Activity{
	protected ArrayList<ArrayList<Datum>> temperaturesBaseRecordings; //interspersed with empty sections;
	protected LinkedList<SummaryLayer> summaries;
	
	public void beginRecording(){
		
	}
	
	public void haltRecording(){
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//access the base db and update the summary db accordingly;
		
		//bind with the service;
		
	}
	
	@Override
	public void onRestoreInstanceState(Bundle state){
		super.onRestoreInstanceState(state);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle state){
		super.onSaveInstanceState(state);
		
	}

	@Override
	public void onResume(){
		super.onResume();
		
	}

	@Override
	public void onRestart(){
		super.onRestart();
		
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		//unbind from the service?
	}
}
