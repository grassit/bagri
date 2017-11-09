package com.bagri.server.hazelcast.bean;

import java.util.HashMap;
import java.util.Map;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.DocumentTrigger;
import com.bagri.core.server.api.TransactionTrigger;
import com.bagri.core.system.TriggerAction;
import com.bagri.core.system.TriggerAction.Order;
import com.bagri.core.system.TriggerAction.Scope;

public class GenericTriggerImpl implements DocumentTrigger, TransactionTrigger {

	private Map<TriggerAction, Integer> fires = new HashMap<>();

	@Override
	public void beforeInsert(Document doc, SchemaRepository repo) throws BagriException {
		System.out.println("beforeInsert; doc: " + doc); 
		addFires(Order.before, Scope.insert);
	}

	@Override
	public void afterInsert(Document doc, SchemaRepository repo) throws BagriException {
		System.out.println("afterInsert; doc: " + doc); 
		addFires(Order.after, Scope.insert);
	}

	@Override
	public void beforeUpdate(Document doc, SchemaRepository repo) throws BagriException {
		System.out.println("beforeUpdate; doc: " + doc); 
		addFires(Order.before, Scope.update);
	}

	@Override
	public void afterUpdate(Document doc, SchemaRepository repo) throws BagriException {
		System.out.println("afterUpdate; doc: " + doc); 
		addFires(Order.after, Scope.update);
	}

	@Override
	public void beforeDelete(Document doc, SchemaRepository repo) throws BagriException {
		System.out.println("beforeDelete; doc: " + doc); 
		addFires(Order.before, Scope.delete);
	}

	@Override
	public void afterDelete(Document doc, SchemaRepository repo) throws BagriException {
		System.out.println("afterDelete; doc: " + doc);
		addFires(Order.after, Scope.delete);
	}
	
	@Override
	public void beforeBegin(Transaction tx, SchemaRepository repo) throws BagriException {
		addFires(Order.before, Scope.begin);
		System.out.println("beforeBegin; tx: " + tx); 
	}

	@Override
	public void afterBegin(Transaction tx, SchemaRepository repo) throws BagriException {
		addFires(Order.after, Scope.begin);
		System.out.println("afterBegin; tx: " + tx); 
	}

	@Override
	public void beforeCommit(Transaction tx, SchemaRepository repo) throws BagriException {
		addFires(Order.before, Scope.commit);
		System.out.println("beforeCommit; tx: " + tx); 
	}

	@Override
	public void afterCommit(Transaction tx, SchemaRepository repo) throws BagriException {
		addFires(Order.after, Scope.commit);
		System.out.println("afterCommit; tx: " + tx); 
	}

	@Override
	public void beforeRollback(Transaction tx, SchemaRepository repo) throws BagriException {
		addFires(Order.before, Scope.rollback);
		System.out.println("beforeRollback; tx: " + tx); 
	}

	@Override
	public void afterRollback(Transaction tx, SchemaRepository repo) throws BagriException {
		addFires(Order.after, Scope.rollback);
		System.out.println("afterRollback; tx: " + tx); 
	}


	private void addFires(Order order, Scope scope) {
		TriggerAction ta = getTA(order, scope);
		Integer cnt = fires.get(ta);
		if (cnt == null) {
			cnt = 0;
		}
		cnt++;
		fires.put(ta, cnt);
	}
	
	private TriggerAction getTA(Order order, Scope scope) {
		return new TriggerAction(order, scope);
	}
	
	public int getFires(Order order, Scope scope) {
		TriggerAction ta = getTA(order, scope);
		Integer cnt = fires.get(ta);
		return cnt == null ? 0 : cnt;
	}

}