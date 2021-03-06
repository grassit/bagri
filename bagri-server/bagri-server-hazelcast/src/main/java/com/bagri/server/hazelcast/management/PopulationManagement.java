package com.bagri.server.hazelcast.management;

import static com.bagri.core.Constants.pn_schema_store_data_path;
import static com.bagri.support.util.JMXUtils.compositeToTabular;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.server.hazelcast.task.schema.SchemaLoadMonitor;
import com.bagri.server.hazelcast.task.schema.SchemaPopulator;
import com.hazelcast.core.Member;

@ManagedResource(description="Population Manager MBean")
public class PopulationManagement extends SchemaFeatureManagement {

	private int cntKeys = 0;
	private int cntErrors = 0;
	private int cntLoading = 0;
	private int cntLoaded = 0;
	private int cntBatchStarted = 0;
	private int cntBatchFinished = 0;
	private long startTime = 0;
	private long popTime = 0;

	private Map<String, CompositeData> populators = new HashMap<>();
	
    public PopulationManagement(String schemaName) {
    	super(schemaName);
	}
    
	@Override
	protected String getFeatureKind() {
		return "PopulationManagement";
	}
	
	@ManagedAttribute(description="The number of started load batches")
	public int getStartedBatchCount() {
		return cntBatchStarted;
	}
	
	@ManagedAttribute(description="The number of finished load batches")
	public int getFinishedBatchCount() {
		return cntBatchFinished;
	}
	
	@ManagedAttribute(description="The number of not loaded documents")
	public int getErrorCount() {
		return cntErrors;
	}
	
	@ManagedAttribute(description="Schema store key count")
	public int getKeyCount() {
		return cntKeys;
	}
	
	@ManagedAttribute(description="The number of currently loading documents")
	public int getLoadingCount() {
		return cntLoading;
	}

	@ManagedAttribute(description="The number of total loaded documents")
	public int getLoadedCount() {
		return cntLoaded;
	}

	@ManagedAttribute(description="Population data path")
	public String getPopulationPath() {
		return schemaManager.getEntity().getProperty(pn_schema_store_data_path);
	}
	
	@ManagedAttribute(description="Time spent for population")
	public String getPopulationTime() {
		String pm = "";
		long time = popTime;
		long hours = time / (60*60*1000);
		if (hours > 0) {
			time -= hours*60*60*1000;
			pm += hours + "h ";
		}
		long mins = time / (60*1000);
		if (mins > 0) {
			time -= mins*60*1000;
			pm += mins + "m ";
		}
		double sec = time / 1000.0;
		if (sec > 0.0) {
			pm += sec + "s";
		}
		return pm;
	}

	@ManagedAttribute(description="Date/Time when population has been started")
	public String getStartTime() {
		if (startTime > 0) {
			return new java.util.Date(startTime).toString();
		}
		return "";
	}
	
	@ManagedAttribute(description="Population statistics per node")
	public TabularData getPopulationStatistics() {
		//super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "triggerStats"), aggregator);
		logger.trace("getPopulationStatistics.enter;");
		int cnt = 0;
		TabularData result = null;
		Callable<CompositeData> task = new SchemaLoadMonitor();
		Map<Member, Future<CompositeData>> futures = execService.submitToAllMembers(task);
		for (Map.Entry<Member, Future<CompositeData>> entry: futures.entrySet()) {
			try {
				CompositeData loaded = entry.getValue().get();
				String member = entry.getKey().getSocketAddress().toString();
				populators.put(member, loaded);
				//logger.trace("getPopulationStatistics; loaded: {} by member {}", loaded, member);
                result = compositeToTabular("Population", "Monitor", "Member", result, loaded);
				cnt++;
			} catch (InterruptedException | ExecutionException | OpenDataException ex) {
				logger.error("getPopulationStatistics.error: " + ex.getMessage(), ex);
			}
		}
		logger.trace("getPopulationStatistics.exit; got stats from {} nodes", cnt);
		calcAggregates();
		return result;
	}
	
	private void calcAggregates() {

		cntKeys = 0;
		cntErrors = 0;
		cntLoading = 0;
		cntLoaded = 0;
		cntBatchStarted = 0;
		cntBatchFinished = 0;
		popTime = 0;
		startTime = Long.MAX_VALUE;
		long lastTime = Long.MIN_VALUE;
		for (CompositeData loaded: populators.values()) {
			Integer ce = (Integer) loaded.get("Errors");
			cntErrors += ce;
			Integer ck = (Integer) loaded.get("Keys");
			cntKeys += ck;
			Integer cg = (Integer) loaded.get("Loading");
			cntLoading += cg;
			Integer cd = (Integer) loaded.get("Loaded");
			cntLoaded += cd;
			Integer cs = (Integer) loaded.get("StartedBatches");
			cntBatchStarted += cs;
			Integer cf = (Integer) loaded.get("FinishedBatches");
			cntBatchFinished += cf;
			Long st = (Long) loaded.get("StartTime");
			if (st < startTime) {
				startTime = st;
			}
			Long lt = (Long) loaded.get("LastTime");
			if (lt > lastTime) {
				lastTime = lt;
			}
		}
		popTime = lastTime - startTime;
	}

	@ManagedAttribute(description="Schema population state")
	public String getState() {
		if (cntKeys == 0) {
			return "NOT POPULATED";
		}
		if (cntLoaded + cntErrors == cntKeys) {
			return "POPULATED";
		}
		return "POPULATING"; 
	}
	
	@ManagedOperation(description="Initiates schema population process")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "overrideExisting", description = "Override existing documents or not")})
	public void startPopulation(boolean overrideExisting) {
		if (!this.schemaManager.isPersistent()) {
			// throw ex?
			return;
		}
		populators.clear();
		SchemaPopulator pop = new SchemaPopulator(schemaName, overrideExisting, false);
		execService.submitToAllMembers(pop);
	}

	@ManagedOperation(description="Stops on-going schema population process")
	public void stopPopulation() {
		if (!this.schemaManager.isPersistent()) {
			// throw ex?
			return;
		}
		SchemaPopulator pop = new SchemaPopulator(schemaName, false, true);
		execService.submitToAllMembers(pop);
	}
}
