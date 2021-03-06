package com.bagri.client.hazelcast;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.*;

import java.io.IOException;
import java.io.Serializable;

import com.bagri.core.DataKey;
import com.bagri.core.DocumentKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentPathKey extends DataKey implements IdentifiedDataSerializable, PartitionAware<Integer> { //, Serializable {

	/**
	 * 
	 */
	//private static final long serialVersionUID = -3417262044970162673L;

	public DocumentPathKey() {
		super();
	}

	public DocumentPathKey(long documentKey, int pathId) {
		super(documentKey, pathId);
	}

	@Override
	public Integer getPartitionKey() {
		return DocumentKey.toHash(documentKey);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentPathKey;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		documentKey = in.readLong();
		pathId = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(documentKey);
		out.writeInt(pathId);
	}

}
