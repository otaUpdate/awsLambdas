package net.otaupdate.lambdas.handlers.organization;

import java.util.HashMap;
import java.util.UUID;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class CreateOrganizationHandler extends AbstractAuthorizedRequestHandler
{
	private String orgName = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		this.orgName = ObjectHelper.parseObjectFromMap(paramsIn, "name", String.class);
		if( (orgName == null) || orgName.isEmpty() ) return false;
		
		return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{
		String orgUuid = UUID.randomUUID().toString();
		
		if( !dbManIn.insert("organizations", "(uuid, name)", String.format("('%s', '%s')", orgUuid, this.orgName)) ) ErrorManager.throwError(ErrorType.ServerError, "error creating organization"); 
		
		
		if( !dbManIn.addUserToOrganization(userIdIn, orgUuid) )
		{
			Logger.getSingleton().debug(String.format("error assigning creating user to organization, organization orphaned '%s'", orgUuid));
			ErrorManager.throwError(ErrorType.ServerError, "error creating organization");
		}
		
		return null;
	}

}
