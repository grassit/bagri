package com.bagri.client.hazelcast.task;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.Constants;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class ContextAwareTask extends TransactionAwareTask {

	protected Properties context;

	public ContextAwareTask() {
		super();
	}
	
	public ContextAwareTask(String clientId, long txId, Properties context) {
		super(clientId, txId);
		this.context = context;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		int size = in.readInt();
		if (size > 0) {
			context = new Properties(); 
			for (int i=0; i < size; i++) {
				context.setProperty(Constants.intToProp(in.readInt()), in.readUTF());
			}
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		if (context == null) {
			out.writeInt(0);
		} else {
			out.writeInt(context.size());
			for (Map.Entry entry: context.entrySet()) {
				out.writeInt(Constants.propToInt(entry.getKey().toString()));
				out.writeUTF(entry.getValue().toString());
			}
		}
	}
	
}
