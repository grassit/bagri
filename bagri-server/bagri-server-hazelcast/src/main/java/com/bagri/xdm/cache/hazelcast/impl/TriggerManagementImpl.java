package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_TRANSACTION;
import static com.bagri.xdm.client.common.XDMCacheConstants.SQN_TRANSACTION;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.cache.api.XDMTriggerManagement;
import com.bagri.xdm.client.hazelcast.impl.IdGeneratorImpl;
import com.bagri.xdm.client.hazelcast.impl.ModelManagementImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMTrigger;
import com.bagri.xdm.system.XDMJavaTrigger;
import com.bagri.xdm.system.XDMTriggerAction;
import com.bagri.xdm.system.XDMTriggerAction.Action;
import com.bagri.xdm.system.XDMTriggerAction.Scope;
import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMXQueryTrigger;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

public class TriggerManagementImpl implements XDMTriggerManagement {

	private static final transient Logger logger = LoggerFactory.getLogger(TriggerManagementImpl.class);

	private HazelcastInstance hzInstance;
	private IMap<Integer, XDMTriggerDef> trgDict;
    private Map<String, XDMTrigger> triggers = new HashMap<>();
	private IExecutorService execService;
    private ModelManagementImpl mdlMgr;
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
	private RepositoryImpl repo = null; //
	
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}
	
	public void setModelManager(ModelManagementImpl mdlMgr) {
		this.mdlMgr = mdlMgr;
	}

	protected Map<Integer, XDMTriggerDef> getTriggerDictionary() {
		return trgDict;
	}
	
	public void setTriggerDictionary(IMap<Integer, XDMTriggerDef> trgDict) {
		this.trgDict = trgDict;
	}

	public void setExecService(IExecutorService execService) {
		this.execService = execService;
	}
	
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }
    
    private String getTriggerKey(int typeId, Action action, Scope scope) {
    	return "" + typeId + ":" + action.name() + ":" + scope.name();
    }
    
    void applyTrigger(final XDMDocument xDoc, final Action action, final Scope scope) {
    	//
		String key = getTriggerKey(xDoc.getTypeId(), action, scope);
    	final XDMTrigger trigger = triggers.get(key);
    	if (trigger != null) {
			logger.trace("applyTrigger; about to fire trigger on document: {}", xDoc);
			
			if (trigger.isSynchronous()) {
				runTrigger(action, scope, xDoc, trigger);
			} else {
				execService.submitToMember(new Runnable() {

					@Override
					public void run() {
						runTrigger(action, scope, xDoc, trigger);
					}
					
				}, hzInstance.getCluster().getLocalMember(), null);
			}
    	}
    }
    
    private void runTrigger(Action action, Scope scope, XDMDocument xDoc, XDMTrigger trigger) {
		String trName = scope + " " + action;
		try {
			if (scope == Scope.before) {
				if (action == Action.insert) {
					trigger.beforeInsert(xDoc, repo);
				} else if (action == Action.delete) {
					trigger.beforeDelete(xDoc, repo);
				} else {
					trigger.beforeUpdate(xDoc, repo);
				}
			} else {
				if (action == Action.insert) {
					trigger.afterInsert(xDoc, repo);
				} else if (action == Action.delete) {
					trigger.afterDelete(xDoc, repo);
				} else {
					trigger.afterUpdate(xDoc, repo);
				}
			}
    		updateStats(trName, true, 1);
		} catch (Throwable ex) {
			logger.error("applyTrigger.error; exception on trigger [" + trName + "] :", ex);
    		updateStats(trName, false, 1);
    		throw ex;
		}
    }

	private void updateStats(String name, boolean success, int count) {
		if (enableStats) {
			if (!queue.offer(new StatisticsEvent(name, success, count))) {
				logger.warn("updateStats; queue is full!!");
			}
		}
	}
	
	@Override
	public boolean createTrigger(XDMTriggerDef trigger) {

		if (trigger instanceof XDMJavaTrigger) {
			return createJavaTrigger((XDMJavaTrigger) trigger);
		}
		return createXQueryTrigger((XDMXQueryTrigger) trigger);
	}
	
	private boolean createJavaTrigger(XDMJavaTrigger trigger) {
		logger.trace("createJavaTrigger.enter; trigger: {}", trigger);
		
		Class tc = null;
		try {
			tc = Class.forName(trigger.getClassName());
		} catch (ClassNotFoundException ex) {
			// load library dynamically..
			logger.debug("createJavaTrigger; ClassNotFound: {}, about to load library..", trigger.getClassName());
			try {
				addURL(FileUtils.path2url(trigger.getLibrary()));
				tc = Class.forName(trigger.getClassName());
			} catch (ClassNotFoundException | IOException ex2) {
				logger.error("createJavaTrigger.error; ", ex2);
			}
		}
		
		if (tc != null) {
			try {
				XDMTrigger triggerImpl = (XDMTrigger) tc.newInstance();
				int typeId = getDocType(trigger);
				for (XDMTriggerAction action: trigger.getActions()) {
					String key = getTriggerKey(typeId, action.getAction(), action.getScope());
					triggers.put(key, triggerImpl);
				}
				logger.trace("createJavaTrigger.exit; trigger created: {}, for type: {}", triggerImpl, typeId);
				return true;
			} catch (InstantiationException | IllegalAccessException ex) {
				logger.error("createJavaTrigger.error; {}", ex);
			}
		}
		return false;
	}

	private boolean createXQueryTrigger(XDMXQueryTrigger trigger) {
		logger.trace("createXQueryTrigger.enter; trigger: {}", trigger);
		return false;
	}
	
	@Override
	public boolean deleteTrigger(XDMTriggerDef trigger) {
		int cnt = 0;
		int typeId = getDocType(trigger);
		for (XDMTriggerAction action: trigger.getActions()) {
			String key = getTriggerKey(typeId, action.getAction(), action.getScope());
			XDMTrigger trgImpl = triggers.remove(key);
			if (trgImpl != null) {
				cnt++;
			}
		}
		logger.trace("deleteTrigger.exit; {} triggers deleted, for type: {}", cnt, typeId);
		return cnt > 0;
	}

	private int getDocType(XDMTriggerDef trigger) {
		
		String type = trigger.getDocType();
		if (type == null) {
			// assign trigger to all docTypes!
			return -1;
		}
		
		int typeId = mdlMgr.getDocumentType(type);
		// check typeId ?
		
		return typeId;
	}

	private void addURL(URL u) throws IOException {
	    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	    try {
	    	Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] {URL.class});
	        method.setAccessible(true);
	        method.invoke(sysloader, new Object[] {u}); 
	    } catch (Throwable ex) {
	        throw new IOException("Error, could not add URL to system classloader", ex);
	    }        
	}
	
}
