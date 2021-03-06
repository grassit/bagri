package com.bagri.client.hazelcast.serialize.query;

import java.io.IOException;
import java.util.List;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.query.AxisType;
import com.bagri.core.query.PathBuilder;
import com.bagri.core.query.PathSegment;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class PathBuilderSerializer implements StreamSerializer<PathBuilder> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_PathBuilder;
	}

	@Override
	public PathBuilder read(ObjectDataInput in) throws IOException {
		PathBuilder pb = new PathBuilder();
		int cnt = in.readInt();
		for (int i=0; i < cnt; i++) {
			AxisType axis = AxisType.valueOf(in.readUTF());
			String namespace = in.readUTF();
			String segment = in.readUTF();
			pb.addPathSegment(axis, namespace, segment);
		}
		return pb;
	} 

	@Override
	public void write(ObjectDataOutput out, PathBuilder path)	throws IOException {
		List<PathSegment> segments = path.getSegments(); 
		out.writeInt(segments.size());
		for (PathSegment segment: segments) {
			out.writeUTF(segment.getAxis().name());
			out.writeUTF(segment.getNamespace());
			out.writeUTF(segment.getSegment());
		}
	}

}
