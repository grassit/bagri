package com.bagri.xdm.cache.hazelcast.store;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.bagri.xdm.access.hazelcast.data.DataDocumentKey;

public class DataDocumentKeyWriteConverter implements Converter<DataDocumentKey, DBObject> {

    private static final Logger logger = LoggerFactory.getLogger(DataDocumentKeyWriteConverter.class);

	@Override
	public DBObject convert(DataDocumentKey source) {
		logger.trace("convert.enter; source: {}", source); 
	    DBObject dbo = new BasicDBObject();
	    //dbo.put("_id", source.getId());
	    dbo.put("data_id", source.getDataId());
	    dbo.put("document_id", source.getDocumentId());
	    return dbo;	
	}

}
