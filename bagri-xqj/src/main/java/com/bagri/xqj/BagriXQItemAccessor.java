package com.bagri.xqj;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXResult;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;

import static javax.xml.xquery.XQItemType.*;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import com.bagri.xquery.api.XQProcessor;

public abstract class BagriXQItemAccessor implements XQItemAccessor {
	
	protected XQItemType type;
	protected Object value;
	protected boolean positioned = false;
	private boolean closed;
	
	private XQProcessor xqProcessor;

	//BagriXQItemAccessor() {
	//}
	
	BagriXQItemAccessor(XQProcessor xqProcessor) {
		this.xqProcessor = xqProcessor;
	}
	
	//BagriXQItemAccessor(XQItemType type, String value) {
	//	this.type = type;
	//	this.value = value;
	//}
	
	public void close() throws XQException {
		
		closed = true;
	}

	public boolean isClosed() {
		
		return closed; // || parent.isClosed() 
	}

	protected void setCurrent(XQItemType type, Object value) {
		this.type = type;
		this.value = value;
		this.positioned = true;
	}
	
	XQProcessor getXQProcessor() {
		return xqProcessor;
	}
	
	@Override
	public boolean getBoolean() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		if (type == null) {
			throw new XQException("ItemType is null");
		}
		if (type.getBaseType() == XQBASETYPE_BOOLEAN) {
			return (Boolean) value;
		}
		throw new XQException("ItemType is not boolean");
	}
	
	private long convertDecimal(long min, long max, String typeName) throws XQException {

		switch (type.getBaseType()) {
			case XQBASETYPE_BYTE: {
				Byte b = (Byte) value;
				if (b.longValue() >= min && b.longValue() <= max) {
					return b.longValue();
				}
				break;
			}
			case XQBASETYPE_INT: {
				Integer i = (Integer) value;
				if (i.longValue() >= min && i.longValue() <= max) {
					return i.longValue();
				}
				break;
			}
			case XQBASETYPE_LONG: {
				Long l = (Long) value;
				if (l.longValue() >= min && l.longValue() <= max) {
					return l.longValue();
				}
				break;
			}
			case XQBASETYPE_SHORT: {
				Short s = (Short) value;
				if (s.longValue() >= min && s.longValue() <= max) {
					return s.longValue();
				}
				break;
			}
			case XQBASETYPE_INTEGER: {
				java.math.BigInteger i = (java.math.BigInteger) value;
				if (i.longValue() >= min && i.longValue() <= max) {
					return i.longValue();
				}
				break;
			}
			case XQBASETYPE_DECIMAL: {
				java.math.BigDecimal d = (java.math.BigDecimal) value;
				//if (d.longValue() >= min && d.longValue() <= max) {
				//	return d.longValue();
				//}
				try {
					return d.longValueExact();
				} catch (ArithmeticException e) {
					//
				}
				break;
			}
			//default: 
		}
		throw new XQException("ItemType is not " + typeName);
		
	}

	@Override
	public byte getByte() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		return (byte) convertDecimal(Byte.MIN_VALUE, Byte.MAX_VALUE, "byte");
	}

	@Override
	public double getDouble() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		if (type.getBaseType() == XQBASETYPE_DOUBLE) {
			return (Double) value;
		}
		if (type.getBaseType() == XQBASETYPE_FLOAT) {
			return ((Float) value).doubleValue();
		}
		throw new XQException("ItemType is not double");
	}

	@Override
	public float getFloat() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		if (type.getBaseType() == XQBASETYPE_FLOAT) {
			return (Float) value;
		}
		if (type.getBaseType() == XQBASETYPE_DOUBLE) {
			return ((Double) value).floatValue();
		}
		throw new XQException("ItemType is not float");
	}

	@Override
	public int getInt() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		return (int) convertDecimal(Integer.MIN_VALUE, Integer.MAX_VALUE, "int");
	}

	@Override
	public XQItemType getItemType() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		if (!positioned) {
			throw new XQException("not positioned on the Item");
		}
		return type;
	}

	@Override
	public String getAtomicValue() throws XQException {

		if (closed) {
			throw new XQException("Item is closed");
		}
		
		//return BagriXQUtils.itemToString(value); //value.toString();
		return xqProcessor.convertToString(value);
	}

	@Override
	public long getLong() throws XQException {

		if (closed) {
			throw new XQException("Item is closed");
		}
		return (long) convertDecimal(Long.MIN_VALUE, Long.MAX_VALUE, "long");
	}

	@Override
	public Node getNode() throws XQException {

		if (closed) {
			throw new XQException("Item is closed");
		}
		
		switch (type.getItemKind()) {
			case XQITEMKIND_ATTRIBUTE: return (org.w3c.dom.Attr) value;
			case XQITEMKIND_COMMENT: return (org.w3c.dom.Comment) value;
			case XQITEMKIND_DOCUMENT: return (org.w3c.dom.Document) value;
			case XQITEMKIND_DOCUMENT_ELEMENT: 
			case XQITEMKIND_ELEMENT: return (org.w3c.dom.Element) value;
			case XQITEMKIND_PI: return (org.w3c.dom.ProcessingInstruction) value;
			case XQITEMKIND_TEXT: return (org.w3c.dom.Text) value;
			default: 
				throw new XQException("ItemType is not Node: " + value.getClass().getName());
		}
	}

	@Override
	public URI getNodeUri() throws XQException {
		
		Node node = getNode();
		try {
			return new URI(node.getBaseURI());
		} catch (URISyntaxException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public Object getObject() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		return value;
	}

	@Override
	public XMLStreamReader getItemAsStream() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		
		return BagriXQUtils.stringToStream(getItemAsString(null));
	}

	@Override
	public String getItemAsString(Properties props) throws XQException {
		if (closed) {
			throw new XQException("Item is closed");
		}
		
        //if (props == null) {
        //    props = new Properties();
        //} else {
            //validateSerializationProperties(props, config);
        //}
		if (value == null) {
			throw new XQException("Value is not accessible");
		}
        //return "<e>" + value.toString() + "</e>";
		//return BagriXQUtils.itemToString(value);
		return xqProcessor.convertToString(value);
	}

	@Override
	public short getShort() throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		return (short) convertDecimal(Short.MIN_VALUE, Short.MAX_VALUE, "short");
	}

	@Override
	public boolean instanceOf(XQItemType type) throws XQException {
		
		if (closed) {
			throw new XQException("Item is closed");
		}
		if (!positioned) {
			throw new XQException("not positioned on the Item");
		}
		if (type == null) {
			throw new XQException("Provided type is null");
		}
		return this.type.equals(type);
	}

	@Override
	public void writeItem(OutputStream os, Properties props) throws XQException {

		if (os == null) {
			throw new XQException("Provided OutputStream is null");
		}
		if (closed) {
			throw new XQException("Item is closed");
		}

		String result = getItemAsString(props);
		try {
			os.write(result.getBytes());
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeItem(Writer ow, Properties props) throws XQException {

		if (ow == null) {
			throw new XQException("Provided Writer is null");
		}
		if (closed) {
			throw new XQException("Item is closed");
		}
		
		try {
			ow.write(getItemAsString(props));
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
	}

	@Override
	public void writeItemToSAX(ContentHandler saxhdlr) throws XQException {

		if (saxhdlr == null) {
			throw new XQException("Provided ContextHandler is null");
		}
		if (closed) {
			throw new XQException("Item is closed");
		}
		
		BagriXQUtils.stringToResult(getItemAsString(null), new SAXResult(saxhdlr));
	}

	@Override
	public void writeItemToResult(Result result) throws XQException {

		if (result == null) {
			throw new XQException("Provided Result is null");
		}
		if (closed) {
			throw new XQException("Item is closed");
		}
		
		BagriXQUtils.stringToResult(getItemAsString(null), result);
	}

}
