package com.bagri.server.hazelcast.task.schema;

import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_ActivateSchemaTask;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.core.system.Schema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaActivator extends SchemaProcessor implements IdentifiedDataSerializable {
	
	private boolean activate;
	
	public SchemaActivator() {
		//
	}
	
	public SchemaActivator(int version, String admin, boolean activate) {
		super(version, admin);
		this.activate = activate;
	}

	@Override
	public Object process(Entry<String, Schema> entry) {
		logger.debug("process.enter; entry: {}", entry);
		Object result = null;
		if (entry.getValue() != null) {
			Schema schema = entry.getValue();
			if (schema.getVersion() == getVersion()) {
				if (activate) {
					if (!schema.isActive()) {
						if (initSchemaInCluster(schema) > 0) {
							schema.setActive(true);
							schema.updateVersion(getAdmin());
							entry.setValue(schema);
							result = schema;
							auditEntity(AuditType.update, schema);

							//logger.debug("process; schema activated, starting population");
							//SchemaPopulator pop = new SchemaPopulator(schema.getName());
							//execService.submitToAllMembers(pop);
						}
					}
				} else {
					if (schema.isActive()) {
						if (denitSchemaInCluster(schema) == 0) {
							schema.setActive(false);
							schema.updateVersion(getAdmin());
							entry.setValue(schema);
							result = schema;
							auditEntity(AuditType.update, schema);
						}
					}
				}
				// or, write audit record here, even in case of version failure?
			}
		} 
		logger.debug("process.exit; returning: {}", result); 
		return result;
	}

	@Override
	public int getId() {
		return cli_ActivateSchemaTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		activate = in.readBoolean();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(activate);
	}


}
