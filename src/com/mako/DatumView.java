package com.mako;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import java.lang.Math;
import com.mako.Datum;
import android.view.ViewGroup;

class DatumView extends LinearLayout{
	protected Datum v;
	protected TextView tv;
	protected FlagImage fi;
	public static final float defaultHeightInches = (float)0.4;
	protected static BitmapDrawable[] flagBitmaps = new BitmapDrawable[8];
	protected static BitmapDrawable nodatBitmap;
	public static Paint backGroundPaint;
	public static Paint haskPaint;
	public static Paint contaminatedEmpty;
	public static Paint contaminatedUsage;
	public static Paint contaminatedCharging;
	public static Path laef;
	protected class FlagImage extends ImageView{
		public FlagImage(Context cont, AttributeSet attrs, int defStyle){
			super(cont, attrs, defStyle); init(cont); }
		public FlagImage(Context cont, AttributeSet attrs){
			super(cont, attrs); init(cont); }
		public FlagImage(Context cont){
			super(cont); init(cont); }
		protected int flags;
		protected void init(Context cont){
			takeFlag(Datum.NO_DATA);
		}
		protected Bitmap paintBlank(int w, int h){
			Bitmap surf = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(surf);
			c.drawPaint(backGroundPaint);
			//now draw the hask
			float haskrad = (h*8)/10;
			float   xo = w/2 - haskrad,   yo = h/2 - haskrad;
			Path haskPath = new Path();
			RectF arcRect = new RectF(xo,yo,xo + haskrad,yo + haskrad);
			float arcRad = 64; //180 means complete circle;
			haskPath.addArc(arcRect, -45 - arcRad/2, arcRad);
			haskPath.addArc(arcRect, -(45+180) + arcRad/2, -arcRad);
			haskPath.close();
			c.drawPath(haskPath, haskPaint);
			return surf;
		}
		protected Bitmap paintFlags(int w, int h, int flaggings){
			Bitmap surf = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(surf);
			c.drawPaint(backGroundPaint);
			float separation = 2;
			float oneFlagSpan =
				(w <= 5*separation)?
					0:
					(h < 2*separation + (w - 5*separation)/3)?
						h - 2*separation:
						(w - 5*separation)/3;
			float fullWidth = oneFlagSpan*3 + 5*separation;
			float fullHeight = oneFlagSpan + 2*separation;
			Path mlaef = new Path(laef);
			Matrix transf = new Matrix(); transf.setRectToRect(
				new RectF(0,0,1,1),
				new RectF((h - fullHeight)/2 + separation,  (w - fullWidth)/2 + separation,  separation+oneFlagSpan,  separation+oneFlagSpan),
				Matrix.ScaleToFit.CENTER);
			mlaef.transform(transf);
			if((flaggings & Datum.CONTAMINATED_BY_ENGAGEMENT) != 0) c.drawPath(mlaef, contaminatedUsage);
			mlaef.offset(oneFlagSpan + separation, 0);
			if((flaggings & Datum.CONTAMINATED_BY_CHARGING) != 0) c.drawPath(mlaef, contaminatedCharging);
			mlaef.offset(oneFlagSpan + separation, 0);
			if((flaggings & Datum.CONTAMINATED_BY_EMPTIES) != 0) c.drawPath(mlaef, contaminatedEmpty);
			return surf;
		}
		protected void installFlag(){
			if(flags == 0) return;
			if((flags & Datum.NO_DATA) != 0){
				if(nodatBitmap != null) setImageDrawable(nodatBitmap);
				else
					setImageDrawable(( nodatBitmap = new BitmapDrawable(
						getContext().getResources(),
						paintBlank(getWidth(), getHeight())) ));
			}else{
				int flagIndex = flags&(7);
				if(flagBitmaps[flagIndex] != null) setImageDrawable(flagBitmaps[flagIndex]);
				else
					setImageDrawable(( flagBitmaps[flagIndex] = new BitmapDrawable(
						getContext().getResources(),
						paintFlags(getWidth(), getHeight(), flags)) ));
			}
		}
		public void takeFlag(int inFlag){ //also serves as the image update function;
			flags = inFlag;
			if(getHeight() != 0 && getWidth() != 0){
				installFlag();
			}
		}
		@Override
		public void onSizeChanged(int oldw, int oldh, int w, int h){
			for(int i = 0; i < flagBitmaps.length; ++i) flagBitmaps[i] = null;
			nodatBitmap = null;
			takeFlag(flags);
		}
	}
	public DatumView(Context cont, AttributeSet attrs, int defStyle){
		super(cont, attrs, defStyle);
		init(cont);
	}
	public DatumView(Context cont, AttributeSet attrs){
		super(cont, attrs);
		init(cont);
	}
	public DatumView(Context cont){
		super(cont);
		init(cont);
	}
	protected void init(Context cont){
		Log.v("frog", "DatumView created");
		if(backGroundPaint == null){
			backGroundPaint = new Paint();
			backGroundPaint.setColor(0xffb0b0b0);
		}
		if(haskPaint == null){
			haskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			haskPaint.setColor(0xffb0b0b0);
		}
		if(contaminatedCharging == null){
			contaminatedCharging = new Paint(Paint.ANTI_ALIAS_FLAG);
			contaminatedCharging.setColor(0xffffff00);
		}
		if(contaminatedUsage == null){
			contaminatedUsage = new Paint(Paint.ANTI_ALIAS_FLAG);
			contaminatedUsage.setColor(0xff00ff00);
		}
		if(contaminatedEmpty == null){
			contaminatedEmpty = new Paint(Paint.ANTI_ALIAS_FLAG);
			contaminatedEmpty.setColor(0xff00ffff);
		}
		if(laef == null){
			float cornerDeg = (float)0.1; //EG: a value of 1 would make the entire thing an invisible diagonal sliver, 0 would make it a square.
			laef = new Path();
			laef.moveTo(0,cornerDeg);
			laef.lineTo(cornerDeg,0);
			laef.lineTo(1,0);
			laef.lineTo(1,1 - cornerDeg);
			laef.lineTo(1 - cornerDeg,1);
			laef.lineTo(0,1);
			laef.close();
		}
		fi = new FlagImage(cont);
		fi.takeFlag(Datum.NO_DATA);
		tv = new TextView(cont);
		LinearLayout.LayoutParams tvLParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);    tvLParams.gravity = android.view.Gravity.LEFT;     tvLParams.weight = 1;
		LinearLayout.LayoutParams fiLParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0);    tvLParams.gravity = android.view.Gravity.RIGHT;    tvLParams.weight = 0;
		addView( tv,  tvLParams );
		addView( fi,  fiLParams );
	}
	protected void installDatum(Datum in){
		v=in;
		if(v == null || (v.flags & Datum.NO_DATA) != 0){
		}else{
			tv.setText(""+v.temperature);
			fi.takeFlag(v.flags);
		}
	}
}