package com.mako;
import java.lang.Math;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.*;
import android.util.Log;

public class RecCheckButton extends CompoundButton {
	protected float bloomAlpha(float t){ //where t starts at 0 and ends at 1, the return value is similar; The shape is like half a bell-curve[like]. Change xstretch to tune it.
		final float xstretch = 2;
		final float reachPartway = xstretch*xstretch/4;
		final float yscale = (float)1/(reachPartway*2);
		if(t==1 || t==0) return t;
		return t<0.5 ? (t*t*xstretch)*yscale:
		               (-t*t*xstretch - 1)*yscale + 1;
	}
	protected float stripeAngle = 30; //where 180 would render the thing all stripe [invisible];
	protected float defaultSpanInches = (float)0.3;
	protected int defaultSpanPx;
	protected float shrinkFactor = (float)0.77;
	protected Paint paintActive;
	protected Paint paintInactive;
	protected boolean directPaint = true; //set this to false to see the anomalous ugliness of what should be clean deferred drawing. Is it the lack of sub-pixel information when using a canvas of a bitmap rather than a canvas of a screen that causes this?
	Bitmap offImage;
	Bitmap onImage;
	protected WindowManager wm;
	public RecCheckButton(Context cont, AttributeSet attrs){
		super(cont, attrs);
		init(cont);
	}
	public RecCheckButton(Context cont){
		super(cont);
		init(cont);
	}
	protected void init(Context cont){
		{
		android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
		((WindowManager)cont.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		defaultSpanPx = (int)(defaultSpanInches * metrics.densityDpi);
		}
		setOnClickListener(clickl);
		setOnCheckedChangeListener(onchch);
		paintActive = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintActive.setColor(0xffff0000);
		paintInactive = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintInactive.setColor(0xff603838);
	}
	CompoundButton.OnCheckedChangeListener onchch = new CompoundButton.OnCheckedChangeListener(){
		public void onCheckedChanged(CompoundButton cpb, boolean state){
			Log.v("frog", "recCheck changed");
		}
	};
	OnClickListener clickl = new OnClickListener(){
		public void onClick(View v){
			Log.v("frog", "recCheck was clicked");
		}
	};
	protected void setMeasuredDimAtDefaultOrSmaller(int specWidth, int specHeight, int xDef, int yDef){
		int xGive = MeasureSpec.getSize(specWidth);
		int yGive = MeasureSpec.getSize(specHeight);
		int xMode = MeasureSpec.getMode(specWidth);
		int yMode = MeasureSpec.getMode(specHeight);
		boolean xUnconstrained = 
			((xMode == MeasureSpec.UNSPECIFIED) ||
			 (xMode == MeasureSpec.AT_MOST && xGive >= xDef));
		boolean yUnconstrained = 
			((yMode == MeasureSpec.UNSPECIFIED) ||
			 (yMode == MeasureSpec.AT_MOST && yGive >= yDef));
		if(xUnconstrained && yUnconstrained){
			setMeasuredDimension(xDef, yDef);
		}else{
			int smallest = Math.min(xGive, yGive);
			setMeasuredDimension(smallest, smallest);
		}
		Log.v("frog", "setting measured as: "+xMode+", "+yMode+"; "+xGive+", "+yGive);
		Log.v("frog", "defaults: "+xDef+", "+yDef);
		Log.v("frog", "UNSPECIFIED's value: "+MeasureSpec.UNSPECIFIED);
		Log.v("frog", "AT_MOST's value: "+MeasureSpec.AT_MOST);
	}
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		onImage=null; offImage=null; //these will be regenerated when needed;
		Log.v("frog", "sized at: "+w+", "+h);
	}
	@Override
	public void onMeasure(int specWidth, int specHeight){
		setMeasuredDimAtDefaultOrSmaller(specWidth, specHeight, defaultSpanPx, defaultSpanPx);
	}
	protected void drawBitmap(Bitmap b, Canvas c){
		Log.v("frog", "canvas span: "+c.getWidth()+", "+c.getHeight());
		Rect sourceRect = new Rect(0, 0, b.getWidth(), b.getHeight());
		int dstL = getLeft()+(getWidth()-b.getWidth())/2;
		int dstT = getTop()+(getHeight()-b.getHeight())/2;
		//Rect dstRect = new Rect(dstL, dstT, dstL+b.getWidth(), dstT+b.getHeight());
		Rect dstRect = new Rect(0, 0, b.getWidth(), b.getHeight());
		Log.v("frog", "drawing like: {"+sourceRect+"}, {"+dstRect+"}");
		c.drawBitmap(b, sourceRect, dstRect, null);
	}
	@Override
	public void onDraw(Canvas c){
		super.onDraw(c);
		if(this.isChecked()){
			if(onImage == null){
				float cx, cy;
				Canvas assetc;
				Bitmap surface = null;
				float radius = Math.min(getWidth(),getHeight())/2*shrinkFactor;
				int span = (int)(2*radius);
				if(span == 0) return;
				if(directPaint){
					cx = getLeft() + getWidth()/2;
					cy = getTop() + getHeight()/2;
					assetc = c;
				}else{
					cx = getWidth()/2;
					cy = getHeight()/2;
					surface = Bitmap.createBitmap(span, span, Bitmap.Config.ARGB_8888);
					assetc = new Canvas(surface);
				}
				assetc.drawArc(new RectF(0, 0, span, span), 0, 360, false, paintActive);
				if(!directPaint){
					drawBitmap(surface, c);
					onImage = surface;
				}
			}else{
				drawBitmap(onImage, c);
			}
		}else{
			if(offImage == null){
				float cx, cy;
				Canvas assetc;
				Bitmap surface = null;
				float radius = Math.min(getWidth(),getHeight())/2*shrinkFactor;
				int span = (int)(2*radius);
				if(span == 0) return;
				if(directPaint){
					cx = getLeft() + getWidth()/2;
					cy = getTop() + getHeight()/2;
					assetc = c;
				}else{
					cx = getWidth()/2;
					cy = getHeight()/2;
					surface = Bitmap.createBitmap(span, span, Bitmap.Config.ARGB_8888);
					assetc = new Canvas(surface);
				}
				
				if(span == 0) return;
				
				float beg = 315 + stripeAngle/2;
				float arclen = 180 - stripeAngle;
				assetc.drawArc(
					new RectF(0, 0, span, span),
					beg, arclen,
					false, paintInactive);
				beg += 180;
				assetc.drawArc(
					new RectF(0, 0, span, span),
					beg, arclen,
					false, paintInactive);
				if(!directPaint){
					drawBitmap(surface, c);
					offImage = surface;
				}
			}else{
				drawBitmap(offImage, c);
			}
		}
	}
}