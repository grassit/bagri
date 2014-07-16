package com.bagri.xdm.cache.hazelcast.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertySource;

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.access.hazelcast.impl.HazelcastSchemaDictionary;
import com.bagri.xdm.cache.hazelcast.store.hive.HiveCacheStore;
import com.bagri.xdm.cache.hazelcast.store.xml.ElementCacheStore;
import com.bagri.xdm.cache.hazelcast.store.xml.DocumentCacheStore;
import com.bagri.xdm.cache.hazelcast.store.xml.XsdCacheStore;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.hazelcast.core.MapStoreFactory;
import com.hazelcast.spring.mongodb.MongoMapStore;

public class XDMMapStoreFactory implements ApplicationContextAware, MapStoreFactory {
	
    private static final Logger logger = LoggerFactory.getLogger(XDMMapStoreFactory.class);
    private static final String st_mongo = "MONGO";
    private static final String st_hive = "HIVE";
    private static final String st_xml = "XML";
    private static final String st_none = "NONE";
    
    private ApplicationContext parentCtx;
    private Map<String, ClassPathXmlApplicationContext> contexts = 
    		new HashMap<String, ClassPathXmlApplicationContext>();
	private PropertySource msProps;
	//private XDMSchemaDictionary schemaDict;
	
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		// it is the first one!?
		parentCtx = context;
		//schemaDict = parentCtx.getBean("xdmDictionary", HazelcastSchemaDictionary.class);
		msProps = ((ConfigurableApplicationContext) context).getEnvironment().getPropertySources().iterator().next(); //get("TPoX");
		logger.debug("setApplicationContext.exit; got properties: {}", msProps);
	}

	@Override
	public MapLoader newMapStore(String mapName, Properties properties) {
		String type = properties.getProperty("xdm.schema.store.type");
		logger.debug("newMapStore.enter; got properties: {} for map: {}", properties, mapName);
		MapStore mStore = null;
		try {
			if (type != null) {
				ClassPathXmlApplicationContext ctx = contexts.get(type);
				if (ctx == null) {
					if (st_mongo.equals(type)) {
			    		ctx = new ClassPathXmlApplicationContext();
			    		ctx.getEnvironment().getPropertySources().addFirst(msProps);
			    		ctx.setConfigLocation("spring/mongo-context.xml");
			    		ctx.refresh();
						contexts.put(type, ctx);
					} else if (st_hive.equals(type)) {
			    		ctx = new ClassPathXmlApplicationContext();
			    		ctx.getEnvironment().getPropertySources().addFirst(msProps);
			    		ctx.setConfigLocation("spring/hive-context.xml");
			    		ctx.refresh();
						contexts.put(type, ctx);
					} else if (st_xml.equals(type)) {
			    		ctx = new ClassPathXmlApplicationContext(parentCtx);
			    		ctx.getEnvironment().getPropertySources().addFirst(msProps);
			    		ctx.setConfigLocation("spring/xml-context.xml");
			    		ctx.refresh();
						contexts.put(type, ctx);
					}
				}
				
				if (ctx != null) {
					
					HazelcastInstance hz = parentCtx.getBean("hzInstance", HazelcastInstance.class);
		    		hz.getUserContext().put("storeContext", ctx);
					if (st_mongo.equals(type)) {
						if ("xdm-element".equals(mapName)) {
							mStore = ctx.getBean("elementCacheStore", 
									com.bagri.xdm.cache.hazelcast.store.mongo.ElementCacheStore.class);
						} else {
							mStore = ctx.getBean("mongoCacheStore", MongoMapStore.class);
						}
					} else if (st_hive.equals(type)) {
						mStore = ctx.getBean("hiveCacheStore", HiveCacheStore.class);
					} else if (st_xml.equals(type)) {
						if ("xdm-document".equals(mapName)) {
							mStore = ctx.getBean("docCacheStore", DocumentCacheStore.class);
						} else if ("xdm-element".equals(mapName)) {
							mStore = ctx.getBean("eltCacheStore", ElementCacheStore.class);
						} else if ("dict-document-type".equals(mapName)) {
							mStore = ctx.getBean("xsdCacheStore", XsdCacheStore.class);
							//logger.debug("newMapStore; got store: {}", mStore);
							//XDMSchemaDictionary schemaDict = parentCtx.getBean("xdmDictionary", HazelcastSchemaDictionary.class);
							//logger.debug("newMapStore; got dictionary: {}", schemaDict);
							//((XsdCacheStore) mStore).setSchemaDictionary(schemaDict);
						} else {
							mStore = new DummyCacheStore();
						}
					} else {
						//
					}
				}
			}
		
			if (st_none.equals(type) || type == null) {
				// 
				mStore = new DummyCacheStore();
			}
		} catch (Exception ex) {
    		logger.error("newMapStore.error: ", ex.getMessage(), ex);
		}
		logger.debug("newMapStore.exit; returning: {}", mStore);
		return mStore; 
	}

}
