package com.bagri.server.hazelcast.task.node;

import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_DeleteNodeTask;

import java.util.Map.Entry;

import com.bagri.core.system.Node;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NodeRemover extends NodeProcessor implements IdentifiedDataSerializable {

	public NodeRemover() {
		//
	}
	
	public NodeRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, Node> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			Node node = entry.getValue();
			if (node.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, node);
				return node;
			} else {
				// throw ex ?
				logger.warn("process; outdated user version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
	@Override
	public int getId() {
		return cli_DeleteNodeTask;
	}
	
}
