package net.otaupdate.lambdas.handlers.api.organizations.users;

import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.CognitoHelper;
import net.otaupdate.lambdas.util.ObjectHelper;


public class AddUserToOrgHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STR = "error adding user";

	private String emailAddress = null;
	private String orgUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		Map<String, Object> jsonBodyMap = bodyIn.getBodyAsJsonMap();
		if( jsonBodyMap == null ) return false;

		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;

		this.emailAddress = ObjectHelper.parseObjectFromMap(jsonBodyMap, "email", String.class);
		if( (this.emailAddress == null) || this.emailAddress.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{
		// check user permissions
		if( !userIn.hasPermissionForOrganization(this.orgUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);

		UInteger orgId = dbManIn.getOrganizationIdForUuid(this.orgUuid);
		if( orgId == null ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		
		// find the userID for this email address
		String username = CognitoHelper.getUsernameFromEmail(this.emailAddress);
		if( (username == null) || !dbManIn.addUserToOrganization(username, orgId) ) throw new BreakwallAwsException(ErrorType.ServerError, ERR_STR);

		return null;
	}
}
