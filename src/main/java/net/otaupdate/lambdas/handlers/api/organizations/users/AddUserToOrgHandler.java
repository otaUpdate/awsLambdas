package net.otaupdate.lambdas.handlers.api.organizations.users;

import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class AddUserToOrgHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STRING = "error adding user";

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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForOrganization(userIdIn, this.orgUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);

		// find the userID for this email address
		UInteger userIdToAdd = dbManIn.getUserIdForEmailAddress(this.emailAddress);
		if( (userIdToAdd == null) || !dbManIn.addUserToOrganization(userIdToAdd, this.orgUuid) ) ErrorManager.throwError(ErrorType.ServerError, ERR_STRING);

		return null;
	}
}
