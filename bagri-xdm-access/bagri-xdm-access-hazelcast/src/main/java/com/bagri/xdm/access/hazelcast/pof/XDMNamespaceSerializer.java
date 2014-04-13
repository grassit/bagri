package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;

import com.bagri.xdm.XDMNamespace;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMNamespaceSerializer implements StreamSerializer<XDMNamespace> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMNamespace;
	}

	@Override
	public void destroy() {
	}

	@Override
	public XDMNamespace read(ObjectDataInput in) throws IOException {
		
		return new XDMNamespace(in.readUTF(), in.readUTF(), in.readUTF());
	}

	@Override
	public void write(ObjectDataOutput out, XDMNamespace xNS) throws IOException {
		
		out.writeUTF(xNS.getUri());
		out.writeUTF(xNS.getPrefix());
		out.writeUTF(xNS.getLocation());
	}

}
