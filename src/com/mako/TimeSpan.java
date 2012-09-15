package com.mako;
import com.mako.Datum;
import java.util.ArrayList;

public class TimeSpan extends ArrayList<Datum>{
	protected long pos;
	public long position(){return pos;}
	public long ending(){return pos + size();}
	public TimeSpan(long beginningTime){
		pos = beginningTime;
	}
}