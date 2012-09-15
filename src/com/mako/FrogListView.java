package com.mako;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.util.LinkedList;
import java.lang.RuntimeException;
import java.io.IOException;

public class FrogListView extends ListView{
	protected static String TAG = "FrogListView";
	public FrogListView (Context context)
		{super(context); init(context);}
	public FrogListView (Context context, AttributeSet attrs)
		{super(context, attrs); init(context);}
	public FrogListView (Context context, AttributeSet attrs, int defStyle)
		{super(context, attrs, defStyle); init(context);}
	protected void init(Context cont){
		BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.froglist)));
		String line;
		LinkedList<String> strings = new LinkedList<String>();
		try{
			while((line = br.readLine()) != null){
				strings.add(line);
			}
		}catch(IOException ioe){
			throw new RuntimeException("failed reading from resource file.");
		}finally{
			try{
				br.close();
			}catch(IOException ioe){
				throw new RuntimeException("failed closing resource file.");
			}
		}
		setAdapter(new ArrayAdapter<String>(cont, R.layout.item_thing, strings));
		setTextFilterEnabled(true);
	}
	@Override
	public boolean performItemClick(View view, int position, long id){
		boolean ret = super.performItemClick(view, position, id);
		Toast.makeText(getContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
		return ret;
	}
}