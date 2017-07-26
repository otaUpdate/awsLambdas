package net.otaupdate.lambdas.handlers.api.organizations;

import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class UpdateOrganizationHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STRING = "error updating organization";
	
	
	private String orgUuid = null;
	private String name = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;
		
		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;
		
		this.name = ObjectHelper.parseObjectFromMap(jsonBodyMap, "name", String.class);
		if( (this.name == null) || this.name.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{	
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForOrganization(userIdIn, this.orgUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);
		
		int numRecordsModified = 
				dslContextIn.update(Organizations.ORGANIZATIONS)
				.set(Organizations.ORGANIZATIONS.NAME, this.name)
				.where(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.execute();
		if( numRecordsModified < 1 ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);
		
		return null;
	}

}