package com.bagri.server.hazelcast.task.doc;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Permission;
import com.bagri.server.hazelcast.impl.AccessManagementImpl;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentMapProvider extends com.bagri.client.hazelcast.task.doc.DocumentMapProvider {

	private transient DocumentManagement docMgr;
    
    @Autowired
	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
		this.docMgr = repo.getDocumentManagement();
	}

    @Override
	//public Map<String, Object> call() throws Exception {
	public Object process(Entry<DocumentKey, Document> entry) {
    	
    	try {
	    	//((SchemaRepositoryImpl) repo).getXQProcessor(clientId);
	    	//checkPermission(Permission.Value.read);
        	((AccessManagementImpl) repo.getAccessManagement()).checkPermission(clientId, Permission.Value.read);
	    	
			return docMgr.getDocumentAsMap(uri, props);
    	} catch (BagriException ex) {
    		return ex;
    	}
	}
}


