package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.common.config.XDMConfigConstants.xdm_config_properties_file;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.api.test.XDMDocumentManagementTest;
import com.bagri.xquery.api.XQProcessor;

public class JsonDocumentManagementTest extends XDMDocumentManagementTest {
	
    private static ClassPathXmlApplicationContext context;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "..\\..\\etc\\samples\\json\\";
		System.setProperty("hz.log.level", "info");
		System.setProperty("xdm.log.level", "trace");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(xdm_config_properties_file, "test.properties");
		context = new ClassPathXmlApplicationContext("spring/cache-xqj-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		//Hazelcast.shutdownAll();
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(RepositoryImpl.class);
		// set xdm.document.format to JSON !
		XQProcessor xqp = ((RepositoryImpl) xRepo).getXQProcessor("test_client");
		xqp.getProperties().setProperty("xdm.document.format", "JSON");
	}

	@After
	public void tearDown() throws Exception {
		// remove documents here!
		removeDocumentsTest();
	}


}