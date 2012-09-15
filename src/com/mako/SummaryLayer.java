package com.mako;
import com.mako.Datum;
import com.mako.TimeSpan;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

public class SummaryLayer{
	protected int chunkFactor;
	protected int foreSize; //represents how many virtual datums the top datum of summary summarises, if summary.isEmpty(), it's just chunkFactor.
	protected int foreEmpties; //the number of datums coverred by the front datum of summary that are just virtual and empty.
	protected float rollingForeTotalTemp;
	protected boolean foreIsContaminatedEmptiesByUnderlying;
	protected ArrayList<TimeSpan> base;
	protected ArrayList<TimeSpan> summary;
	protected Datum addChunk(List<Datum> o, TimeSpan f){ //remember to add the EMPTIES flag to the returned Datum if you need to;
		float total=0;
		int flags =0;
		for(Datum dat: o){
			total += dat.temperature;
			flags |= dat.flags;
		}
		Datum newun = new Datum(total/o.size(), flags);
		f.add(newun);
		return newun;
	}
	public SummaryLayer(ArrayList<TimeSpan> baseInput, int chunkFactorInput){ //warn; cannot summarise summaries
		chunkFactor = chunkFactorInput;
		base = baseInput;
		foreSize=0;
		foreEmpties=0;
		rollingForeTotalTemp=0;
		long basePosition=0;
		TimeSpan fTimeSpan = new TimeSpan(0);
		Iterator<TimeSpan> oIter = base.iterator();
		if(!oIter.hasNext()) return;
		TimeSpan oTimeSpan = oIter.next();
		boolean curHasEmpties=false;
		while(true){
			LinkedList<Datum> toAdd = new LinkedList<Datum>();
			long nextPos = basePosition+chunkFactor;
			if(oTimeSpan.position() >= nextPos){ //empty, seek
				basePosition = oTimeSpan.position()/chunkFactor*chunkFactor;
				summary.add(fTimeSpan);
				fTimeSpan = new TimeSpan(basePosition);
				continue;
			}else if(oTimeSpan.position() <= basePosition){
				if(oTimeSpan.ending() >= nextPos){ //it's a bland copy
					addChunk(
						oTimeSpan.subList((int)(basePosition- oTimeSpan.position()), (int)(nextPos- oTimeSpan.position())),
						fTimeSpan  );
					basePosition = nextPos;
					continue;
				}else{ //finish the ending
					toAdd.addAll(oTimeSpan.subList((int)(basePosition- oTimeSpan.position()), oTimeSpan.size()));
					if(oIter.hasNext()) oTimeSpan = oIter.next();
					else{
						addChunk(toAdd, fTimeSpan).flags |= Datum.CONTAMINATED_BY_EMPTIES;
						summary.add(fTimeSpan);
						for(Datum dat: toAdd) rollingForeTotalTemp+=dat.temperature;
						foreSize = toAdd.size();
						return;
					}
				}
			}
			if(oTimeSpan.ending() <= nextPos){
				do{
					//deal with any less than a chunk in size
					toAdd.addAll(oTimeSpan);
					if(oIter.hasNext()) oTimeSpan = oIter.next();
					else{
						addChunk(toAdd, fTimeSpan);
						summary.add(fTimeSpan);
						for(Datum dat: toAdd) rollingForeTotalTemp+=dat.temperature;
						foreSize = (int)((oTimeSpan.position()+oTimeSpan.size())-basePosition);
						foreEmpties = foreSize - toAdd.size();
						return;
					}
				}while(oTimeSpan.ending() <= nextPos);
				if(oTimeSpan.position() < nextPos){
					toAdd.addAll(oTimeSpan.subList(0,(int)(nextPos-oTimeSpan.position())));
				}
			}
			addChunk(toAdd, fTimeSpan).flags |= Datum.CONTAMINATED_BY_EMPTIES;
			basePosition = nextPos;
		}
	}
	public void updateFront(Datum newBaseDat){
		if(foreSize == chunkFactor){
			foreSize = 1;
			rollingForeTotalTemp = newBaseDat.temperature;
			Datum newDat = newBaseDat.clone();
			newDat.flags |= Datum.CONTAMINATED_BY_EMPTIES;
			summary.get(summary.size() - 1).add(newDat);
			foreEmpties = 0;
		}else{
			Datum concerned;
			{
			ArrayList<Datum> endlist = summary.get(summary.size()-1);
			concerned = endlist.get(endlist.size()-1);
			}
			rollingForeTotalTemp += newBaseDat.temperature;
			foreSize += 1;
			concerned.temperature = rollingForeTotalTemp/(foreSize- foreEmpties);
			concerned.flags |= newBaseDat.flags;
			if(foreSize == chunkFactor && foreEmpties == 0)
				concerned.flags &= ~Datum.CONTAMINATED_BY_EMPTIES;
		}
	}
	ArrayList<TimeSpan> getSummary(){return summary;}
	Datum atTime(int position){ //may return a meaningless datum with flag NO_DATA set if position is in a place where we can't even guess the temperature based on nearby chunks;
		for(TimeSpan length : summary){
			if(position < length.position()) return new Datum(0,Datum.NO_DATA);
			else if(position < length.ending()){
				return length.get((int)((position - length.position()))/chunkFactor).clone();
			}
		}
		return new Datum(0,Datum.NO_DATA);
	}
}