package com.bagri.xdm.cache.hazelcast.task.module;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

//import com.bagri.xdm.cache.hazelcast.task.EntityProcessor.AuditType;
import com.bagri.xdm.system.XDMModule;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class ModuleCreator extends ModuleProcessor implements DataSerializable {
	
	private String fileName;
	private String namespace;
	private String description;

	public ModuleCreator(String admin, String fileName, String namespace, String description) {
		super(1, admin);
		this.fileName = fileName;
		this.namespace = namespace;
		this.description = description;
	}

	@Override
	public Object process(Entry<String, XDMModule> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			String body = "module namespace ns = \"" + namespace + "\";";
			XDMModule module = new XDMModule(getVersion(), new Date(), getAdmin(), 
					name, fileName, description, namespace, body, true);
			entry.setValue(module);
			auditEntity(AuditType.create, module);
			return module;
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		fileName = in.readUTF();
		namespace = in.readUTF();
		description = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(fileName);
		out.writeUTF(namespace);
		out.writeUTF(description);
	}

}