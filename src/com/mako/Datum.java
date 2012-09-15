package com.mako;
import android.os.Parcelable;
import android.os.Parcel;

public class Datum implements Parcelable {
	public float temperature;
	public static final int
	 CONTAMINATED_BY_ENGAGEMENT=1 /*that is, if it's being used, Likely to draw more power and raise battery temp*/,
	 CONTAMINATED_BY_CHARGING=2,
	 CONTAMINATED_BY_EMPTIES=4, /*this kind of contamination is not really much of a contamination though, just a roughening. These do not occur in base level datums, just at the edges of contigious recordings in higher summarylayers*/
	 NO_DATA = 5; //means this is a meaningless datum, this only occurs in particular places, so you shouldn't normally worry about it;
	public int flags;
	public Datum(float temp, int flag){temperature = temp; flags = flag;}
	
	
	public int describeContents(){return 0;}
	public void writeToParcel(Parcel out, int flaggings){
		out.writeInt(flags);
		out.writeFloat(temperature);
	}
	public static final Parcelable.Creator<Datum> CREATOR = new Parcelable.Creator<Datum>() {
		public Datum createFromParcel(Parcel in){
			float tempor = in.readFloat();
			int flagings = in.readInt();
			return new Datum(tempor, flagings);
		}
		public Datum [] newArray(int size){
			return new Datum[size];
		}
	};
	@Override
	public Datum clone(){
		return new Datum(temperature, flags);
	}
}	