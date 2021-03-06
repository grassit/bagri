package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.bg_schema;
import static com.bagri.core.Constants.pn_schema_cache_resources;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.BagriException;
import com.bagri.core.query.ExpressionContainer;
import com.bagri.core.server.api.DocumentManagement;
import com.bagri.core.server.api.QueryManagement;
import com.bagri.core.server.api.SchemaRepository;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.trans.XPathException;

public class ResourceCollectionImpl implements ResourceCollection {
	
    private static final Logger logger = LoggerFactory.getLogger(ResourceCollectionImpl.class);
    
    private static final Map<Long, Reference<Resource>> resourceCache = Collections.synchronizedMap(new WeakHashMap<Long, Reference<Resource>>()); 
    
	private String uri;
    private SchemaRepository repo;
	private ExpressionContainer query;
	private Collection<Long> docIds = null;
	private Iterator<Long> iter = null;
	private boolean cacheResources = false;
	
	
	//public ResourceCollectionImpl(String uri, Collection<Long> docIds) {
	//	this.uri = uri;
	//	this.docIds = new ArrayList<>(docIds);
	//	this.iter = docIds.iterator();
	//}

	public ResourceCollectionImpl(String uri, SchemaRepository repo, ExpressionContainer query) {
		this.uri = uri;
		this.repo = repo;
		this.query = query;
		String cr = repo.getSchema().getProperty(pn_schema_cache_resources);
		if (cr != null) {
			this.cacheResources = Boolean.parseBoolean(cr);
		}
	}
	
	public static void clear() {
		resourceCache.clear();
	}
	
	private void loadData() { 
		try {
			docIds = ((QueryManagement) repo.getQueryManagement()).getDocumentIds(query);
		} catch (BagriException ex) {
			logger.error("loadData.error;", ex);
			docIds = Collections.emptyList();
		}
		logger.trace("loadData; got {} document ids", docIds.size());
		this.iter = docIds.iterator();
	}
	
	private boolean hasNext() {
		if (docIds == null) {
			loadData();
		}
		boolean result = iter.hasNext();
		logger.trace("hasNext; returning: {}", result);
		return result;
	}
	
	private Long next() { 
		if (docIds == null) {
			loadData();
		}
		Long currentId = iter.next();
		logger.trace("next; returning: {}", currentId);
		return currentId;
	}
	
	@Override
	public String getCollectionURI() {
		return uri;
	}

	@Override
	public Iterator<String> getResourceURIs(XPathContext context) throws XPathException {
		logger.trace("getResourceURIs.enter;");
		return new UriIterator();
	}

	@Override
	public Iterator<? extends Resource> getResources(XPathContext context) throws XPathException {
		logger.trace("getResources.enter;");
		return new ResourceIterator();
	}

	@Override
	public boolean isStable(XPathContext context) {
		return false; // true;
	}

	@Override
	public boolean stripWhitespace(SpaceStrippingRule rules) {
		logger.trace("stripWhitespace.enter; rules: {}", rules);
		return false;
	}

	@Override
	public String toString() {
		return "ResourceCollectionImpl [query=" + query	+ ", docIds=" + docIds  + "]";
	}
	
	public class UriIterator implements Iterator<String> {
		
		@Override
		public boolean hasNext() {
			return ResourceCollectionImpl.this.hasNext();
		}

		@Override
		public String next() {
			Long next = ResourceCollectionImpl.this.next();
			if (next != null) {
				return bg_schema + ":/" + next;
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() not supported");
		}
		
	}

	public class ResourceIterator implements Iterator<Resource> {

		@Override
		public boolean hasNext() {
			return ResourceCollectionImpl.this.hasNext();
		}

		@Override
		public Resource next() {
			Long docKey = ResourceCollectionImpl.this.next();
			if (docKey != null) {
				Resource res = null;
				Reference<Resource> ref;
				if (cacheResources) {
					ref = resourceCache.get(docKey);
					if (ref != null) {
						res = ref.get();
					}
				}
				if (res == null) {
					try {
						DocumentManagement docMgr = (DocumentManagement) repo.getDocumentManagement(); 
						String type = docMgr.getDocumentContentType(docKey);
						
						// we can check isDocVisible here, thus don't do this at loadData phase.. 
						if ("MAP".equals(type)) {
							logger.trace("ResourceIterator.next; returning new MapResource for docKey {}", docKey);
					        res = new MapResourceImpl(docMgr, docKey);
						} else if ("JSON".equals(type)) {
					        res = new JsonResourceImpl(docMgr, docKey);
						} else {
							res = new XmlResourceImpl(docMgr, docKey);
						}
						if (cacheResources) {
							ref = new WeakReference<Resource>(res);
							resourceCache.putIfAbsent(docKey, ref);
						}
					} catch (BagriException ex) {
						logger.error("next.error", ex);
					}
				}
				logger.trace("ResourceIterator.next; returning res: {}", res);
				return res;
			}
			return null;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() not supported");
		}
		
	}

}
