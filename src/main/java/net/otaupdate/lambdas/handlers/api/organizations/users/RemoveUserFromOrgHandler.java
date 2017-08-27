package net.otaupdate.lambdas.handlers.api.organizations.users;

import java.util.Map;

import org.jooq.DSLContext;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.CognitoHelper;
import net.otaupdate.lambdas.util.ObjectHelper;


public class RemoveUserFromOrgHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STR = "error removing user";


	private String orgUuid = null;
	private String emailAddress = null;


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

		String awsSubToRemove = CognitoHelper.getUsernameFromEmail(this.emailAddress);
		if( (awsSubToRemove == null) || 
				(dslContextIn.delete(Organizationusermap.ORGANIZATIONUSERMAP)
						.where(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB.eq(awsSubToRemove))
						.execute() < 1) )
		{
			throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);
		}

		return null;
	}
}
