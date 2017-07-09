package net.otaupdate.lambdas.handlers.devController;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ObjectHelper;


public class CheckForUpdateHandler extends AbstractUnauthorizedRequestHandler
{
	private String currentFwUuid = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		this.currentFwUuid = ObjectHelper.parseObjectFromMap(paramsIn, "currFwUuid", String.class);
		if( this.currentFwUuid == null ) return false;
		
		return true;
	}
	

	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn)
	{	
		return dbManIn.getNextFirmwareForFirmwareUuid(this.currentFwUuid);
	}
}
