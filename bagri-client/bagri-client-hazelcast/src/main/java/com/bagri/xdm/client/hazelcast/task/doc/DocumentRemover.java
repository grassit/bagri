package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_RemoveDocumentTask;

import java.util.concurrent.Callable;

import com.bagri.xdm.domain.XDMDocument;

public class DocumentRemover extends DocumentAwareTask implements Callable<XDMDocument> {

	public DocumentRemover() {
		super();
	}

	public DocumentRemover(String clientId, long txId, String uri) {
		super(clientId, txId, uri, null);
	}

	@Override
	public int getId() {
		return cli_RemoveDocumentTask;
	}

	@Override
	public XDMDocument call() throws Exception {
		return null;
	}

}
