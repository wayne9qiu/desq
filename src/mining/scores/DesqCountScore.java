package mining.scores;

import java.util.HashMap;

import mining.statistics.collectors.DesqGlobalDataCollector;
import mining.statistics.collectors.DesqProjDbDataCollector;
import mining.statistics.collectors.DesqResultDataCollector;

public interface DesqCountScore extends DesqScore {

	public double getMaxScoreByPrefix(int[] prefix,  
			HashMap<String,? extends DesqGlobalDataCollector<?,?>> globalDataCollector,
			int[] transaction,
			int position,
			int fstState);
	
	public double getScoreByProjDb(int[] sequence, 
			HashMap<String,? extends DesqGlobalDataCollector<?,?>> globalDataCollectors,
			HashMap<String,? extends DesqProjDbDataCollector<?,?>> finalStateProjDbCollectors);
	
	public double getScoreBySequence(int[] sequence, 
			HashMap<String,? extends DesqGlobalDataCollector<?,?>> globalDataCollectors);

}
