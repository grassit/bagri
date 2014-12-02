package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_InvocationStatsResetTask;

import java.util.concurrent.Callable;

import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentStatsReseter extends DocumentStatsTask implements Callable<Boolean>  {

	@Override
	public int getId() {
		return cli_InvocationStatsResetTask;
	}

	@Override
	public Boolean call() throws Exception {
		
		if (xdmStats == null) {
			//ApplicationContext ctx = HazelcastUtils.findContext();
		//	ApplicationContext ctx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "appContext");
			logger.debug("call; got stats: {}", xdmStats); 
		//	xdmProxy = ctx.getBean("xdmProxy", DocumentManagementProxy.class); 
		}
		
		xdmStats.resetStats();
        return true; 
    }


}
