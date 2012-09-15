package com.mako;
import android.widget.Button;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
public class FrogButton extends Button{
	public FrogButton (Context context)
		{super(context);}
	public FrogButton (Context context, AttributeSet attrs)
		{super(context, attrs);}
	public FrogButton (Context context, AttributeSet attrs, int defStyle)
		{super(context, attrs, defStyle);}
	@Override
	public void onDraw(Canvas c){
		super.onDraw(c);
		Log.v("frog", "frogButton: redrew");
	}
}