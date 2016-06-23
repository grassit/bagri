package com.bagri.samples.ext.df;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import javax.xml.xquery.XQItemType;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.common.XDMParser;
import com.bagri.xdm.common.df.XDMParserBase;
import com.bagri.xdm.domain.XDMOccurrence;
import com.bagri.xdm.domain.XDMData;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class XDMJaksonParser extends XDMParserBase implements XDMParser {
	
	private static JsonFactory factory = new JsonFactory();

	public static List<XDMData> parseDocument(XDMModelManagement dictionary, String json) throws XDMException {
		XDMJaksonParser parser = new XDMJaksonParser(dictionary);
		return parser.parse(json);
	}
	
	public XDMJaksonParser(XDMModelManagement model) {
		super(model);
	}

	@Override
	public List<XDMData> parse(String json) throws XDMException { 
		try (Reader reader = new StringReader(json)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	@Override
	public List<XDMData> parse(File file) throws XDMException {
		try (Reader reader = new FileReader(file)) {
			return parse(reader);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
	}
	
	@Override
	public List<XDMData> parse(InputStream stream) throws XDMException {
		
		JsonParser jParser = null;
		try {
			jParser = factory.createParser(stream);	
			return parse(jParser);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		} finally {
			if (jParser != null) {
				try {
					jParser.close();
				} catch (IOException ex) {
					// just log it..
				}
			}
		}
	}
	
	@Override
	public List<XDMData> parse(Reader reader) throws XDMException {
		
		JsonParser jParser = null;
		try {
			jParser = factory.createParser(reader);	
			return parse(jParser);
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		} finally {
			if (jParser != null) {
				try {
					jParser.close();
				} catch (IOException ex) {
					// just log it..
				}
			}
		}
	}

	public List<XDMData> parse(JsonParser parser) throws XDMException {
		
		logger.trace("parse.enter; context: {}; schema: {}", parser.getParsingContext(), parser.getSchema());
		
		init();
		try {
			while (parser.nextToken() != null) {
				processToken(parser);
			}
		} catch (IOException ex) {
			throw new XDMException(ex, XDMException.ecInOut);
		}
		cleanup();

		List<XDMData> result = dataList;
		dataList = null;
		logger.trace("parse.exit; returning {} elements", result); //.size());
		return result;
	}
	
	private void processToken(JsonParser parser) throws IOException, XDMException { //, XMLStreamException {

		JsonToken token = parser.getCurrentToken();
		logger.trace("processToken; got token: {}; name: {}; value: {}", token.name(), parser.getCurrentName(), parser.getText());
		
		switch (token) {
			
			case START_OBJECT:
				if (dataStack.size() == 0) {
					processDocument(parser.nextFieldName());
				} 
			case START_ARRAY: 
				if (parser.getCurrentName() != null) {
					processStartElement(parser.getCurrentName());
				}
			case NOT_AVAILABLE:
			case FIELD_NAME:
				break;
			case END_OBJECT:
			case END_ARRAY: 
				if (parser.getCurrentName() != null) {
					processEndElement();
				}
				break;
			case VALUE_EMBEDDED_OBJECT:
			case VALUE_FALSE:
			case VALUE_NULL:
			case VALUE_NUMBER_FLOAT:
			case VALUE_NUMBER_INT:
			case VALUE_TRUE:
			case VALUE_STRING:
				processStartElement(parser.getCurrentName());
				processValueElement(parser.getCurrentName(), parser.getText());
				break;
			default: 
				logger.trace("processToken; unknown token: {}", token);
		}			
	}

	private void processDocument(String name) throws XDMException {

		String root = "/" + (name == null ? "" : name);
		docType = model.translateDocumentType(root);
		XDMPath path = model.translatePath(docType, "", XDMNodeKind.document, XQItemType.XQBASETYPE_ANYTYPE, XDMOccurrence.onlyOne); 
		XDMElement start = new XDMElement();
		start.setElementId(elementId++);
		//start.setParentId(0); // -1 ?
		XDMData data = new XDMData(path, start);
		dataStack.add(data);
		dataList.add(data);
	}
	
	private boolean isAttribute(String name) {
		return name.startsWith("-") || name.startsWith("@");
	}

	private void processStartElement(String name) throws XDMException {
		
		if (name != null && !isAttribute(name)) {
			XDMData parent = dataStack.peek();
			if (name.equals("#text")) {
				// add marker
				dataStack.add(null);
			} else if (!name.equals(parent.getName())) {
				XDMData current = addData(parent, XDMNodeKind.element, "/" + name, null, XQItemType.XQBASETYPE_ANYTYPE, XDMOccurrence.zeroOrOne); 
				dataStack.add(current);
			}
		}
	}

	private void processEndElement() {

		dataStack.pop();
	}

	private void processValueElement(String name, String value) throws XDMException {
		
		//value = value.replaceAll("&", "&amp;");
		if (name == null) {
			XDMData current = dataStack.peek();
			if (current == null) {
				// #text in array; not sure it'll always work.
				// use XDMJsonParser.getTopData instead ?
				current = dataStack.elementAt(dataStack.size() - 2);
			}
			addData(current, XDMNodeKind.text, "/text()", value, XQItemType.XQBASETYPE_ANYATOMICTYPE, XDMOccurrence.zeroOrOne);
		} else if (isAttribute(name)) {
			XDMData current = dataStack.peek(); 
			name = name.substring(1);
			if (name.startsWith("xmlns")) {
				addData(current, XDMNodeKind.namespace, "/#" + name, value, XQItemType.XQBASETYPE_STRING, XDMOccurrence.onlyOne);
			} else {
				addData(current, XDMNodeKind.attribute, "/@" + name, value, XQItemType.XQBASETYPE_ANYATOMICTYPE, XDMOccurrence.zeroOrOne);
			}
		} else {
			XDMData current = dataStack.pop();
			if (current == null) {
				// #text
				current = dataStack.peek(); 
			}
			addData(current, XDMNodeKind.text, "/text()", value, XQItemType.XQBASETYPE_ANYATOMICTYPE, XDMOccurrence.zeroOrOne);
		}
	}
	
}