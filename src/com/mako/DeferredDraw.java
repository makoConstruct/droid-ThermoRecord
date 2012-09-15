package com.mako;
import java.lang.Math;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.view.View;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.graphics.*;
import android.util.Log;

public class DeferredDraw extends View {
	protected boolean directPaint;
	protected boolean biColor;
	protected int defaultWidth = 60;
	protected int defaultHeight = 20;
	protected Bitmap bitmap;
	protected Paint paintDirect;
	protected Paint paintWhite;
	protected Paint paintDeferred;
	protected void parseAttrs(Context cont, AttributeSet attrs){
		TypedArray ats = cont.obtainStyledAttributes(attrs, R.styleable.DeferredDraw);
		directPaint = !ats.getBoolean(R.styleable.DeferredDraw_isDeferred, false);
		biColor = ats.getBoolean(R.styleable.DeferredDraw_biColor, false);
	}
	public DeferredDraw(Context cont, AttributeSet attrs, int defStyle){
		super(cont, attrs, defStyle);
		parseAttrs(cont, attrs);
		init(cont);
	}
	public DeferredDraw(Context cont, AttributeSet attrs){
		super(cont, attrs);
		parseAttrs(cont, attrs);
		init(cont);
	}
	public DeferredDraw(Context cont){
		super(cont);
		init(cont);
	}
	protected void init(Context cont){
		setOnClickListener(clickl);
		paintDirect = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintDirect.setColor(0xffff63ff);
		paintDirect.setStrokeWidth((float)1);
		paintDeferred = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintDeferred.setColor(0xffffff63);
		paintDeferred.setStrokeWidth((float)1);
		paintWhite = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintWhite.setColor(0xffffffff);
		paintWhite.setStrokeWidth((float)1);
	}
	OnClickListener clickl = new OnClickListener(){
		public void onClick(View v){
			Log.v("frog", "drawtest was clicked");
			directPaint = !directPaint;
			bitmap = null;
			invalidate();
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
	}
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh){
		bitmap = null;
	}
	@Override
	public void onMeasure(int specWidth, int specHeight){
		setMeasuredDimAtDefaultOrSmaller(specWidth, specHeight, defaultWidth, defaultHeight);
	}
	protected void drawBitmap(Bitmap b, Canvas c){
		Rect sourceRect = new Rect(0, 0, b.getWidth(), b.getHeight());
		int dstL = getLeft()+(getWidth()-b.getWidth())/2;
		int dstT = getTop()+(getHeight()-b.getHeight())/2;
		//Rect dstRect = new Rect(dstL, dstT, dstL+b.getWidth(), dstT+b.getHeight());
		Rect dstRect = new Rect(0, 0, b.getWidth(), b.getHeight());
		//Log.v("frog", "drawing like: {"+sourceRect+"}, {"+dstRect+"}");
		c.drawBitmap(b, sourceRect, dstRect, null);
	}
	@Override
	public void onDraw(Canvas c){
		super.onDraw(c);
		if(bitmap == null){
			int myWidth = getWidth(),  myHeight = getHeight(); //we'll apply this change when find/replace works again t__t
			float dx, dy;
			Canvas assetc;
			Paint whichPaint;
			if(myWidth == 0 || myHeight == 0) return;
			if(directPaint){
				dx = 0;
				dy = 0;
				assetc = c;
				whichPaint = 
					biColor?
						paintDirect:
						paintWhite;
			}else{
				dx = 0;
				dy = 0;
				bitmap = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.ARGB_8888);
				assetc = new Canvas(bitmap);
				whichPaint = 
					biColor?
						paintDeferred:
						paintWhite;
			}
			float place = (float)0.5;
			while(place < myHeight && place < myWidth){
				assetc.drawLine(dx+place, dy+place, dx+myWidth,  dy+place, whichPaint);
				assetc.drawLine(dx+place, dy+place, dx+place, dy+myHeight, whichPaint);
				place+=2;
			}
			float cx = dx + myWidth/2,    cy = dy + myHeight/2;
			float radius = myWidth*(float)0.23;
			if(!biColor && !directPaint) assetc.drawArc(new RectF(cx-radius, cy-radius, cx+radius, cy+radius), 0, 360, false, whichPaint);
			if(!directPaint){
				drawBitmap(bitmap, c);
			}
		}else{
			drawBitmap(bitmap, c);
		}
	}
}