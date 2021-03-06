package com.bagri.client.hazelcast.serialize.query;

import java.io.IOException;
import java.util.Collection;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.query.QueryBuilder;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class QueryBuilderSerializer implements StreamSerializer<QueryBuilder> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_QueryBuilder;
	}

	@Override
	public QueryBuilder read(ObjectDataInput in) throws IOException {
		QueryBuilder result = new QueryBuilder();
		int size = in.readInt();
		for (int i=0; i < size; i++) {
			ExpressionContainer ec = in.readObject();
			result.addContainer(ec);
		}
		return result;
	}

	@Override
	public void write(ObjectDataOutput out, QueryBuilder query) throws IOException {
		Collection<ExpressionContainer> containers = query.getContainers();
		out.writeInt(containers.size());
		for (ExpressionContainer ec: containers) {
			out.writeObject(ec);
		}
	}


}
