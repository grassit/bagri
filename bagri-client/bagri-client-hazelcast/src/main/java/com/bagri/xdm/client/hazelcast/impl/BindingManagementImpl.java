package com.bagri.xdm.client.hazelcast.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMBindingManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;

public class BindingManagementImpl implements XDMBindingManagement {
	
    private final static Logger logger = LoggerFactory.getLogger(BindingManagementImpl.class);
	
	protected XDMRepository repo;
	
	void initialize(XDMRepository repo) {
		this.repo = repo;
	}	

	@Override
	public <T> T getDocumentBinding(String uri) throws XDMException {
		
		return null;
	}

	@Override
	public <T> T getDocumentBinding(String uri, Class<T> type) throws XDMException {
		
		logger.trace("getDocumentBinding.enter; uri: {}; type: {}", uri, type);
		Object result = null;
		String xml = repo.getDocumentManagement().getDocumentAsString(uri);
		if (xml != null) {
	        try {
	        	// TODO think about internal static context
	        	JAXBContext jc = JAXBContext.newInstance(type);
	        	Unmarshaller unmarshaller = jc.createUnmarshaller();
	        	result = unmarshaller.unmarshal(new StringReader(xml));
	        } catch (JAXBException ex) {
	        	throw new XDMException(ex, XDMException.ecBinding);
	        }
		}
		logger.trace("getDocumentBinding.exit; returning: {}", result);
        return type.cast(result);
	}

	@Override
	public void setDocumentBinding(String uri, Object value) throws XDMException {
		
		logger.trace("setDocumentBinding.enter; uri{ {}; value: {}", uri, value);
        try {
        	// TODO: think about internal static context
        	JAXBContext jc = JAXBContext.newInstance(value.getClass());
        	Marshaller marshaller = jc.createMarshaller();
        	StringWriter writer = new StringWriter();
        	marshaller.marshal(value, writer);
        	writer.flush();
        	String xml = writer.getBuffer().toString();
        	writer.close();
        	// TODO: set proper properties..
        	repo.getDocumentManagement().storeDocumentFromString(uri, xml, null);
        } catch (JAXBException ex) {
        	throw new XDMException(ex, XDMException.ecBinding);
        } catch (IOException ex) {
        	throw new XDMException(ex, XDMException.ecInOut);
        }
		logger.trace("setDocumentBinding.exit;");
	}
	
}
