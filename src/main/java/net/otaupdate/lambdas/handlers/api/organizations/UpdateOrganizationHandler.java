package net.otaupdate.lambdas.handlers.api.organizations;

import java.util.Map;

import org.jooq.DSLContext;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;

public class UpdateOrganizationHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STR = "error updating organization";
	
	
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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		// check user permissions
		if( !userIn.hasPermissionForOrganization(this.orgUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		int numRecordsModified = 
				dslContextIn.update(Organizations.ORGANIZATIONS)
				.set(Organizations.ORGANIZATIONS.NAME, this.name)
				.where(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.execute();
		if( numRecordsModified < 1 ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		return null;
	}

}