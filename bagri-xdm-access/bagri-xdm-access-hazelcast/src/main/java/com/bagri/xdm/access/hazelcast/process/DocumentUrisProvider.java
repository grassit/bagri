package com.bagri.xdm.access.hazelcast.process;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_DocumentUrisProviderTask;

import java.util.Collection;
import java.util.concurrent.Callable;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;

public class DocumentUrisProvider extends ResultBuilder implements Callable<Collection<String>> {
	
	public DocumentUrisProvider() {
		super();
	}
	
	public DocumentUrisProvider(ExpressionContainer exp) {
		super(exp);
	}

	@Override
	public int getId() {
		return cli_DocumentUrisProviderTask;
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}


}
