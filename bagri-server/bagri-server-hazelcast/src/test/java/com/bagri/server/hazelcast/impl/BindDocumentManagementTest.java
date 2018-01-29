package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import com.bagri.server.hazelcast.bean.SampleBean;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemAccessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;
import static org.junit.Assert.*;

public class BindDocumentManagementTest extends BagriManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/json/";
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "test.properties");
		System.setProperty(pn_config_path, "src/test/resources");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(SchemaRepositoryImpl.class);
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			Properties props = loadProperties("src/test/resources/test.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			xdmRepo.setSchema(schema);
			
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			setContext(schema.getName(), context);
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
		}
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}

	@Test
	public void createBeanDocumentTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		SampleBean sb1 = new SampleBean(1, false, "XYZ");
	    Properties props = getDocumentProperties();
		props.setProperty(pn_document_data_format, "XML");
		props.setProperty(pn_document_data_source, "BEAN");
		DocumentAccessor bDoc = xRepo.getDocumentManagement().storeDocument("bean_test.xml", sb1, props);
		assertNotNull(bDoc);
		uris.add(bDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT));
		String xml = xRepo.getDocumentManagement().getDocument(bDoc.getUri(), props).getContent();
		assertNotNull(xml);
		
		props.setProperty(pn_document_data_format, "BEAN");
		SampleBean sb2 = (SampleBean) xRepo.getDocumentManagement().getDocument(bDoc.getUri(), props).getContent();
		assertEquals(sb1.getIntProperty(), sb2.getIntProperty());
		assertTrue(sb1.isBooleanProperty() == sb2.isBooleanProperty());
		assertEquals(sb1.getStringProperty(), sb2.getStringProperty());
	}
	
	@Test
	public void createMapDocumentTest() throws Exception {
		long txId = xRepo.getTxManagement().beginTransaction();
		Map<String, Object> m1 = new HashMap<>();
		m1.put("intProp", 1); 
		m1.put("boolProp", Boolean.FALSE);
		m1.put("strProp", "XYZ");
	    Properties props = getDocumentProperties();
		props.setProperty(pn_document_data_format, "MAP");
		DocumentAccessor mDoc = xRepo.getDocumentManagement().storeDocument("map_test1.xml", m1, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		//String xml = xRepo.getDocumentManagement().getDocumentAsString(mDoc.getUri(), null);
		//assertNotNull(xml);
		//System.out.println(xml);
		
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT));
		Map<String, Object> m2 = xRepo.getDocumentManagement().getDocument(mDoc.getUri(), props).getContent();
		assertEquals(m1.get("intProp"), m2.get("intProp"));
		assertEquals(m1.get("boolProp"), m2.get("boolProp"));
		assertEquals(m1.get("strProp"), m2.get("strProp"));
/*
		m2.put("intProp", 2); 
		m2.put("boolProp", Boolean.TRUE);
		m2.put("strProp", "ABC");
		txId = xRepo.getTxManagement().beginTransaction();
		mDoc = xRepo.getDocumentManagement().storeDocumentFromMap("map_test2.xml", m2, props);
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = //"declare default element namespace \"\";\n" + 
				"declare variable $value external;\n" +
				"for $doc in fn:collection()/map\n" +
				//"where $doc/intProp = $value\n" +
				"where $doc[intProp = $value]\n" +
				"return $doc/strProp/text()";
		
		Map<String, Object> params = new HashMap<>();
		params.put("value", 0);
		ResultCursor results = query(query, params, null);
		assertFalse(results.next());
		results.close();
		
		params.put("value", 1);
		results = query(query, params, null);
		assertTrue(results.next());
		
		props = new Properties();
		props.setProperty("method", "text");
		XQItem item = (XQItem) results.getXQItem();
		String text = item.getItemAsString(props);
		assertEquals("XYZ", text);
		assertFalse(results.next());
		results.close();
*/		
	}
		
	@Test
	public void queryDocumentTest() throws Exception {
        String xml = "<map>\n" +
        		"  <boolProp>false</boolProp>\n" +
        		"  <strProp>XYZ</strProp>\n" +
        		"  <intProp>1</intProp>\n" +
        		"</map>";
		
		long txId = xRepo.getTxManagement().beginTransaction();
		DocumentAccessor mDoc = xRepo.getDocumentManagement().storeDocument("map.xml", xml, getDocumentProperties());
		assertNotNull(mDoc);
		assertEquals(txId, mDoc.getTxStart());
		uris.add(mDoc.getUri());
		xRepo.getTxManagement().commitTransaction(txId);
		
		String query = //"declare default element namespace \"\";\n" +
				"declare variable $value external;\n" +
				"for $doc in fn:collection()/map\n" +
				"where $doc/intProp = $value\n" +
				//"where $doc[intProp = $value]\n" +
				"return $doc/strProp/text()";
		
		Map<String, Object> params = new HashMap<>();
		params.put("value", 1);
		checkCursorResult(query, params, null, "XYZ");
	}
		
}
