package net.otaupdate.lambdas.handlers.api.organizations;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class CreateOrganizationHandler extends AbstractAuthorizedRequestHandler
{
	private static final String TAG = CreateOrganizationHandler.class.getSimpleName();
	private static final String ERR_STRING = "error creating organization";
	
	
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String uuid;

		public ReturnValue(String uuidIn)
		{
			this.uuid = uuidIn;
		}
	}
	
	
	private String orgName = null;
	
	
	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;
		
		this.orgName = ObjectHelper.parseObjectFromMap(jsonBodyMap, "name", String.class);
		if( (orgName == null) || orgName.isEmpty() ) return false;
		
		return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{
		String orgUuid = UUID.randomUUID().toString();
		
		// create the organization
		int numRecordsModified = dslContextIn.insertInto(Organizations.ORGANIZATIONS, Organizations.ORGANIZATIONS.UUID, Organizations.ORGANIZATIONS.NAME)
		.values(orgUuid, this.orgName)
		.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		
		
		// add the user
		if( !dbManIn.addUserToOrganization(userIdIn, orgUuid) )
		{
			Logger.getSingleton().debug(TAG, String.format("error assigning creating user to organization, organization orphaned '%s'", orgUuid));
			ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		}
		
		return new ReturnValue(orgUuid);
	}

}
