package com.mako;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Iterator;
import java.lang.InterruptedException;
import java.lang.RuntimeException;
import java.lang.Runnable;
import com.mako.Datum;
import com.mako.DatumView;
//import com.mako.FlagShow;

class TemperatureDataView extends ScrollView{
	protected RelativeLayout layout;
	protected View upperView;
	protected static final int upperViewID=0;
	protected View lowerView;
	protected static final int lowerViewID=1;
	protected int upperChild=-1, lowerChild=-1;
	protected int highestID=2; //the highest ID is available;
	protected int dbLength;
	protected boolean seesEnd;
	protected int[] viewsPageCycle; //the ints herein are of layout's indexes. It's a cyclic array, the beginning and end are thought to be at viewsPageEye*pageSize. Index increasing : going down the list. 
	protected int viewsPageEye; //tells which page is the 'first' at this time.
	protected int basalRecord; //tells which record the base of the viewsPageCycle is looking at.
	protected int pageSize; //unit: datums.
	protected int nPages;
	protected boolean sizeGiven = false;
	protected ExecutorService pool;
	protected Future<ArrayList<Datum>> lowerAccess;
	protected Future<ArrayList<Datum>> upperAccess;
	public static final float defaultItemHeightInches = (float)0.4;
	protected int itemHeight;
	//targetDb
	protected RelativeLayout.LayoutParams defItemParams(){
		return new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
	}
	
	protected class AccessTask implements Callable<ArrayList<Datum>>{
		int first, n;
		Datum defdat = new Datum(0, Datum.CONTAMINATED_BY_ENGAGEMENT | Datum.CONTAMINATED_BY_CHARGING | Datum.CONTAMINATED_BY_EMPTIES);
		//DB
		public AccessTask(int firstID, int nItems){ //, DB db){
			first = firstID; n = nItems;
		}
		public ArrayList<Datum> call(){
			ArrayList<Datum> ret = new ArrayList<Datum>(n);
			//get from DB
			
			for(Datum dat : ret) dat = defdat.clone();
			return ret;
		}
	}
	
	protected void increaseUpperPadSize(){
		//remove the views in the highest page, adding their size to that of upperView;
		RelativeLayout.LayoutParams newUppersLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, upperView.getHeight()+pageSize*itemHeight);
		newUppersLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layout.updateViewLayout(upperView, newUppersLayoutParams);
	}
	protected void accessLower(){ //get from return value right before scrolling to the lower page concerned[an asynchronous call is definately unnecessary, but the goal here is not to ship but to learn];
		if(lowerAccess == null) //[otherwise a task is already assigned]
			lowerAccess = pool.submit(new AccessTask(basalRecord+nPages*pageSize, pageSize));
	}
	protected void finalizeShiftViewDown(){ //call accessLower some time before this is needed, then this will be instant;
		ArrayList<Datum> resultList;
		if(lowerAccess == null){
			accessLower();
		}
		try{
			resultList = lowerAccess.get();
		}catch(InterruptedException ex){throw new RuntimeException("db access thread interrupted?");}
		catch(ExecutionException ex){throw new RuntimeException("db access thread interrupted?");}
		
		increaseUpperPadSize();
		int viewsPageCycleIter = viewsPageEye*pageSize;
		int end = viewsPageCycleIter+pageSize;
		int previousLayoutID = 
			(viewsPageEye == 0)?
				viewsPageCycle[nPages*pageSize - 1]:
				viewsPageCycle[viewsPageEye*pageSize -1];
		Iterator<Datum> iter = resultList.iterator();
		while(viewsPageCycleIter != end){
			layout.removeViewAt(viewsPageCycle[viewsPageCycleIter]);
			int newLayoutID = highestID++;
			viewsPageCycle[viewsPageCycleIter] = newLayoutID;
			RelativeLayout.LayoutParams params = defItemParams();
			params.addRule(RelativeLayout.BELOW, previousLayoutID);
			DatumView dv = new DatumView(getContext());
			dv.installDatum(iter.next());
			layout.addView(dv, newLayoutID, params);
			++viewsPageCycleIter;
			previousLayoutID = newLayoutID;
		}
		/*//decrease the lower pad size and reattach it to previousLayoutID; //no. size only changes when dbLength changes.
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lowerView.getHeight()-pageSize*itemHeight);
		params.addRule(RelativeLayout.BELOW, previousLayoutID);
		layout.updateViewLayout(lowerView, params);*/
		++viewsPageEye; if(viewsPageEye == nPages) viewsPageEye = 0;
		basalRecord+=pageSize;
		lowerAccess = null;
	}
	protected void acceptFirstSizing(){
		pageSize = this.getHeight()/itemHeight;
		nPages = 3; //may add more when the ScrollView expands. Fuck changing the page size. Too hard.
		assert pageSize > 0 : "it looks like size isn't assigned before initialization :(. just make pagesize big or something";
		viewsPageCycle = new int[nPages*pageSize];
		for(int i = 0; i < viewsPageCycle.length ; ++i) viewsPageCycle[i] = -1;
		viewsPageEye = 0;
		layout = new RelativeLayout(getContext());
		seesEnd = false;
		lowerView = new View(getContext());
		upperView = new View(getContext());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layout.addView(upperView, upperViewID, params);
		dbLength = 50; //LIE
		params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dbLength*itemHeight);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layout.addView(lowerView, lowerViewID, params);
		addView(
			layout,
			new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		if(dbLength*itemHeight < getHeight()) seesEnd=true;
		relocate(0);
		scrollTo(0,0);
	}
	protected void init(Context cont){
		pool = Executors.newFixedThreadPool(2);
		{//decide itemHeight:
		android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
		((WindowManager)cont.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		itemHeight = (int)(defaultItemHeightInches * metrics.densityDpi);
		}
	}
	public TemperatureDataView(Context cont, AttributeSet attrs, int defStyle){
		super(cont, attrs, defStyle);
		init(cont);
	}
	public TemperatureDataView(Context cont, AttributeSet attrs){
		super(cont, attrs);
		init(cont);
	}
	public TemperatureDataView(Context cont){
		super(cont);
		init(cont);
	}
	protected void addDatumBegin(Datum v){
		DatumView view = new DatumView(getContext());
		view.installDatum(v);
		RelativeLayout.LayoutParams params = defItemParams();
		if(upperChild != -1) params.addRule(RelativeLayout.ABOVE, upperChild);
		else params.addRule(RelativeLayout.ALIGN_PARENT_TOP, upperChild);
		upperChild = highestID++;
		layout.addView(view, upperChild, params);
	}
	protected void addDatumEnd(Datum v){
		DatumView view = new DatumView(getContext());
		view.installDatum(v);
		RelativeLayout.LayoutParams params = defItemParams();
		if(lowerChild != -1) params.addRule(RelativeLayout.BELOW, lowerChild);
		else params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, lowerChild);
		lowerChild = highestID++;
		layout.addView(view, lowerChild, params);
	}
	
	public void notifyFreshDatum(){
		++dbLength;
		if(seesEnd){
			//get the datum and add it to the list
			
		}else{
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dbLength*itemHeight);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layout.updateViewLayout(lowerView, params);
		}
	}
	
	@Override
	public void onSizeChanged(int oldw, int oldh, int w, int h){
		if(!sizeGiven){
			acceptFirstSizing();
			sizeGiven=true;
		}
		//change nPages
		
	}
	
	protected void relocate(int y){ //initializes cached Datums for the new location
		if(y < 0) y=0;
		//clear the cache
		for(int i = 0; i < nPages*pageSize; ++i){
			if(viewsPageCycle[i] != -1) layout.removeViewAt(viewsPageCycle[i]);
		}
		viewsPageEye = 0;
		basalRecord = (y/(itemHeight*pageSize) - itemHeight*(pageSize/2))*pageSize;
		highestID = 2;
		//add the new stuff
			//fetch the needed records
		ArrayList<Datum> data = (new AccessTask(basalRecord, nPages*pageSize)).call();
			//figure out whether and to what degree the data stretches before the beginning of the layout[the portion before is ignorable];
		int validStart = (basalRecord < 0)? -basalRecord : 0;
				//change upperView accordingly
		RelativeLayout.LayoutParams upperLayoutParams;
		if(validStart == 0){
			upperLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, basalRecord*itemHeight);
		}else{
			upperLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
		}
		upperLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		layout.updateViewLayout(upperView, upperLayoutParams);
			//dimension it
		int prevID = upperViewID;
		for(; validStart < data.size(); ++validStart){
			Datum cur = data.get(validStart);
			DatumView curv = new DatumView(getContext());
			if(cur != null) curv.installDatum(cur);
			int id = highestID++;
			RelativeLayout.LayoutParams params = defItemParams();
			params.addRule(RelativeLayout.BELOW, prevID);
			layout.addView(curv, id, params);
			viewsPageCycle[validStart] = id;
			prevID = id;
		}
		//adjust vars
		upperChild = 2;
		lowerChild = prevID;
		if(lowerAccess != null) lowerAccess.cancel(true);
		lowerAccess = null;
		if(upperAccess != null) upperAccess.cancel(true);
		upperAccess = null;
	}
	
	@Override
	public void scrollTo(int x, int y){
		int loc = layout.getScrollY();
		if(loc < y){ //is scrolling down
			int height = getHeight();
			if(y+height > (basalRecord+(int)(nPages*0.75)*pageSize)*itemHeight){
				if(y+height > (basalRecord+nPages*pageSize)*itemHeight){
					finalizeShiftViewDown();
				}else{
					accessLower();
				}
			}
		}
		super.scrollTo(x,y);
	}
	//public void selectDatabase() //pass two file handles, base and summaries. Summaries may be null;
	//public void pushFreshDatum(Datum in) //not exactly sure how I'll do this.
}