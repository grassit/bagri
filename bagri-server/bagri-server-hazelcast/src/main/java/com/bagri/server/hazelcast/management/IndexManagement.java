package com.bagri.server.hazelcast.management;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.system.Index;
import com.bagri.core.system.Schema;
import com.bagri.server.hazelcast.task.index.IndexCreator;
import com.bagri.server.hazelcast.task.index.IndexRemover;
import com.bagri.server.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.support.stats.StatsAggregator;
import com.hazelcast.core.Member;

@ManagedResource(description="Schema Indexes Management MBean")
public class IndexManagement extends SchemaFeatureManagement {
	
	public IndexManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "IndexManagement";
	}
	
    public void setStatsAggregator(StatsAggregator aggregator) {
    	this.aggregator = aggregator;
    }
	
	@Override
	protected Collection getSchemaFeatures(Schema schema) {
		return schema.getIndexes();
	}

	@ManagedAttribute(description="Return indexes defined on Schema")
	public TabularData getIndexes() {
		return getTabularFeatures("index", "Index definition", "name");
    }

	@ManagedAttribute(description="Return aggregated index usage statistics, per index")
	public TabularData getUsageStatistics() {
		return super.getUsageStatistics(new StatisticSeriesCollector(schemaName, "indexStats"), aggregator);
	}

	@ManagedOperation(description="Creates a new Index")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Index name to create"),
		@ManagedOperationParameter(name = "docType", description = "Root path for document type"),
		@ManagedOperationParameter(name = "path", description = "XPath to index"),
		@ManagedOperationParameter(name = "dataType", description = "Indexed value data type"),
		@ManagedOperationParameter(name = "caseSensitive", description = "Is index case-sensitive"),
		@ManagedOperationParameter(name = "range", description = "Is index supports range search"),
		@ManagedOperationParameter(name = "unique", description = "Is index unique"),
		@ManagedOperationParameter(name = "description", description = "Index description")})
	public void addIndex(String name, String docType, String path, String dataType, boolean caseSensitive,
			boolean range, boolean unique, String description) {

		logger.trace("addIndex.enter;");
		int cnt = 0;
		long stamp = System.currentTimeMillis();
		try {
			Index index = schemaManager.addIndex(name, docType, path, dataType, caseSensitive, range, unique, description);
			if (index == null) {
				throw new IllegalStateException("Index '" + name + "' in schema '" + schemaName + "' already exists");
			}
			
			IndexCreator task = new IndexCreator(index);
			Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
			for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
				try {
					if (entry.getValue().get()) {
						cnt++;
					}
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("addIndex.error; ", ex);
				}
			}
		} catch (Exception ex) {
			logger.error("addIndex.error 2; ", ex);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("addIndex.exit; index created on {} members; time Taken: {}", cnt, stamp);
	}
	
	@ManagedOperation(description="Removes an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Index name to delete")})
	public void dropIndex(String name) {
		
		logger.trace("dropIndex.enter;");
		long stamp = System.currentTimeMillis();
		if (!schemaManager.deleteIndex(name)) {
			throw new IllegalStateException("Index '" + name + "' in schema '" + schemaName + "' does not exist");
		}

		IndexRemover task = new IndexRemover(name);
		Map<Member, Future<Boolean>> results = execService.submitToAllMembers(task);
		int cnt = 0;
		for (Map.Entry<Member, Future<Boolean>> entry: results.entrySet()) {
			try {
				if (entry.getValue().get()) {
					cnt++;
				}
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("dropIndex.error; ", ex);
			}
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("dropIndex.exit; index deleted on {} members; time Taken: {}", cnt, stamp);
	}

	@ManagedOperation(description="Enables/Disables an existing Index")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Index name to enable/disable"),
		@ManagedOperationParameter(name = "enable", description = "enable/disable index")})
	public void enableIndex(String name, boolean enable) {
		
		if (!schemaManager.enableIndex(name, enable)) {
			throw new IllegalStateException("Index '" + name + "' in schema '" + schemaName + 
					"' does not exist or already " + (enable ? "enabled" : "disabled"));
		}
	}
	
	@ManagedOperation(description="Rebuilds an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Index to rebuild")})
	public void rebuildIndex(String name) {
		// not implemented yet
	}
	

}
