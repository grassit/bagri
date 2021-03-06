package com.bagri.core.server.api;

import com.bagri.core.system.TriggerDefinition;

/**
 * XDM Trigger Management interface; Adds/Deletes XDM Schema triggers at runtime
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface TriggerManagement {
	
	/**
	 * registers a new trigger with Trigger implementation provided
	 *
	 * @param trigger the {@link TriggerDefinition} definition to register in the current schema
	 * @param impl the {@link Trigger} implementation
	 * @return number of registered trigger actions
	 * 
	 */
	int addTrigger(TriggerDefinition trigger, Trigger impl);
	
	/**
	 * registers a new trigger
	 *
	 * @param trigger the {@link TriggerDefinition} definition to register in the current schema
	 * @return true if trigger registered, false otherwise
	 * 
	 */
	boolean createTrigger(TriggerDefinition trigger);
	
	/**
	 * removes an existing trigger
	 * 
	 * @param trigger the {@link TriggerDefinition} definition to unregister from the current schema
	 * @return true if trigger unregistered, false otherwise
	 */
	boolean deleteTrigger(TriggerDefinition trigger);
	

}
