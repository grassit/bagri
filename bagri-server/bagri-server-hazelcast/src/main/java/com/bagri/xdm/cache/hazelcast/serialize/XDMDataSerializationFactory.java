package com.bagri.xdm.cache.hazelcast.serialize;

import com.bagri.xdm.cache.hazelcast.predicate.CollectionPredicate;
import com.bagri.xdm.cache.hazelcast.predicate.DocsAwarePredicate;
import com.bagri.xdm.cache.hazelcast.predicate.QueryPredicate;
import com.bagri.xdm.cache.hazelcast.predicate.ResultsDocPredicate;
import com.bagri.xdm.cache.hazelcast.predicate.ResultsQueryPredicate;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentCreator;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentMapCreator;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentMapProvider;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentRemover;
import com.bagri.xdm.cache.hazelcast.task.auth.UserAuthenticator;
import com.bagri.xdm.cache.hazelcast.task.doc.CollectionDocumentsProvider;
import com.bagri.xdm.cache.hazelcast.task.doc.CollectionDocumentsRemover;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentBeanCreator;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentBeanProvider;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentCleaner;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentCollectionUpdater;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentContentProvider;
import com.bagri.xdm.cache.hazelcast.task.doc.DocumentStructureProvider;
import com.bagri.xdm.cache.hazelcast.task.index.IndexCreator;
import com.bagri.xdm.cache.hazelcast.task.index.IndexRemover;
import com.bagri.xdm.cache.hazelcast.task.index.ValueIndexator;
import com.bagri.xdm.cache.hazelcast.task.library.LibFunctionUpdater;
import com.bagri.xdm.cache.hazelcast.task.library.LibraryCreator;
import com.bagri.xdm.cache.hazelcast.task.library.LibraryRemover;
import com.bagri.xdm.cache.hazelcast.task.model.ModelRegistrator;
import com.bagri.xdm.cache.hazelcast.task.module.ModuleCreator;
import com.bagri.xdm.cache.hazelcast.task.module.ModuleRemover;
import com.bagri.xdm.cache.hazelcast.task.node.NodeCreator;
import com.bagri.xdm.cache.hazelcast.task.node.NodeInfoProvider;
import com.bagri.xdm.cache.hazelcast.task.node.NodeKiller;
import com.bagri.xdm.cache.hazelcast.task.node.NodeOptionSetter;
import com.bagri.xdm.cache.hazelcast.task.node.NodeRemover;
import com.bagri.xdm.cache.hazelcast.task.node.NodeUpdater;
import com.bagri.xdm.cache.hazelcast.task.query.DocumentUrisProvider;
import com.bagri.xdm.cache.hazelcast.task.query.QueryProcessor;
import com.bagri.xdm.cache.hazelcast.task.query.ResultFetcher;
import com.bagri.xdm.cache.hazelcast.task.query.XMLBuilder;
import com.bagri.xdm.cache.hazelcast.task.role.PermissionUpdater;
import com.bagri.xdm.cache.hazelcast.task.role.RoleCreator;
import com.bagri.xdm.cache.hazelcast.task.role.RoleRemover;
import com.bagri.xdm.cache.hazelcast.task.role.RoleUpdater;
import com.bagri.xdm.cache.hazelcast.task.query.QueryExecutor;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaActivator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaAdministrator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaCreator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaDocCleaner;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaHealthAggregator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaDenitiator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaInitiator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaMemberExtractor;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaQueryCleaner;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaRemover;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaStatsAggregator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaUpdater;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticTotalsCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xdm.cache.hazelcast.task.trigger.TriggerCreator;
import com.bagri.xdm.cache.hazelcast.task.trigger.TriggerRemover;
import com.bagri.xdm.cache.hazelcast.task.trigger.TriggerRunner;
import com.bagri.xdm.cache.hazelcast.task.tx.TransactionAborter;
import com.bagri.xdm.cache.hazelcast.task.tx.TransactionCommiter;
import com.bagri.xdm.cache.hazelcast.task.tx.TransactionStarter;
import com.bagri.xdm.cache.hazelcast.task.user.UserCreator;
import com.bagri.xdm.cache.hazelcast.task.user.UserRemover;
import com.bagri.xdm.cache.hazelcast.task.user.UserUpdater;
//import com.bagri.xdm.client.hazelcast.task.doc.DocumentProcessor;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class XDMDataSerializationFactory extends com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory {

	@Override
	public IdentifiedDataSerializable create(int typeId) {
		
		switch (typeId) {
			//case cli_ProcessDocumentTask: return new DocumentProcessor();
			case cli_QueryPredicate: return new QueryPredicate();
			case cli_DocsAwarePredicate: return new DocsAwarePredicate();
			case cli_ResultsDocPredicate: return new ResultsDocPredicate();
			case cli_ResultsQueryPredicate: return new ResultsQueryPredicate();
			case cli_CollectionPredicate: return new CollectionPredicate();
			case cli_ProvideCollectionDocumentsTask: return new CollectionDocumentsProvider();
			case cli_RemoveCollectionDocumentsTask: return new CollectionDocumentsRemover();
			case cli_UpdateDocumentCollectionTask: return new DocumentCollectionUpdater();
			case cli_CreateDocumentTask: return new DocumentCreator();
			case cli_CreateMapDocumentTask: return new DocumentMapCreator();
			case cli_CreateBeanDocumentTask: return new DocumentBeanCreator();
			case cli_RemoveDocumentTask: return new DocumentRemover();
			case cli_BeginTransactionTask: return new TransactionStarter(); 
			case cli_CommitTransactionTask: return new TransactionCommiter();
			case cli_RollbackTransactionTask: return new TransactionAborter();
			case cli_FetchResultsTask: return new ResultFetcher();
			case cli_InitSchemaTask: return new SchemaInitiator();
			case cli_DenitSchemaTask: return new SchemaDenitiator();
			case cli_CleanSchemaTask: return new SchemaDocCleaner();
			case cli_SchemaAdminTask: return new SchemaAdministrator();
			case cli_SchemaMemberTask: return new SchemaMemberExtractor();
			case cli_PopulateSchemaTask: return new SchemaPopulator();
			case cli_ProvideDocumentUrisTask: return new DocumentUrisProvider(); 
			case cli_ProvideDocumentContentTask: return new DocumentContentProvider();
			case cli_ProvideDocumentStructureTask: return new DocumentStructureProvider();
			case cli_ProvideDocumentMapTask: return new DocumentMapProvider(); 
			case cli_ProvideDocumentBeanTask: return new DocumentBeanProvider(); 
			case cli_CleanTxDocumentsTask: return new DocumentCleaner();
			case cli_BuildQueryXMLTask: return new XMLBuilder();
			case cli_ExecQueryTask: return new QueryExecutor();
			case cli_CollectStatisticSeriesTask: return new StatisticSeriesCollector();
			case cli_CollectStatisticTotalsTask: return new StatisticTotalsCollector();
			case cli_ResetStatisticsTask: return new StatisticsReseter();
			case cli_ProcessQueryTask: return new QueryProcessor();
			case cli_ApplyQueryTask: return new QueryPredicate();
			case cli_KillNodeTask: return new NodeKiller();
			case cli_SetNodeOptionTask: return new NodeOptionSetter();
			case cli_AggregateSchemaInfoTask: return new SchemaStatsAggregator();
			case cli_GetNodeInfoTask: return new NodeInfoProvider();
			case cli_CreateIndexTask: return new IndexCreator();
			case cli_RemoveIndexTask: return new IndexRemover();
			case cli_IndexValueTask: return new ValueIndexator();
			case cli_CleanQueryTask:  return new SchemaQueryCleaner();
			case cli_CreateTriggerTask: return new TriggerCreator(); 
			case cli_RemoveTriggerTask: return new TriggerRemover(); 
			case cli_RunTriggerTask: return new TriggerRunner(); 
			case cli_RegisterModelTask: return new ModelRegistrator();
			case cli_AuthenticateTask: return new UserAuthenticator();
			case cli_AggregateSchemaHealthTask: return new SchemaHealthAggregator();
			case cli_CreateRoleTask: return new RoleCreator();
			case cli_UpdateRoleTask: return new RoleUpdater();
			case cli_DeleteRoleTask: return new RoleRemover();
			case cli_UpdateRolePermissionsTask: return new PermissionUpdater();
			case cli_CreateLibraryTask: return new LibraryCreator();
			case cli_UpdateLibraryTask: return new LibFunctionUpdater();
			case cli_DeleteLibraryTask: return new LibraryRemover();
			case cli_CreateModuleTask: return new ModuleCreator();
			case cli_DeleteModuleTask: return new ModuleRemover();
			case cli_CreateNodeTask: return new NodeCreator();
			case cli_UpdateNodeTask: return new NodeUpdater();
			case cli_DeleteNodeTask: return new NodeRemover();
			case cli_CreateUserTask: return new UserCreator();
			case cli_UpdateUserTask: return new UserUpdater();
			case cli_DeleteUserTask: return new UserRemover();
			case cli_CreateSchemaTask: return new SchemaCreator();
			case cli_UpdateSchemaTask: return new SchemaUpdater();
			case cli_DeleteSchemaTask: return new SchemaRemover();
			case cli_ActivateSchemaTask: return new SchemaActivator();
		}
		return super.create(typeId);
	}

}
