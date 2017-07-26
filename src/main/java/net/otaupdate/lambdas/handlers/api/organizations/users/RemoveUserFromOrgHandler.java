package net.otaupdate.lambdas.handlers.api.organizations.users;

import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;


public class RemoveUserFromOrgHandler extends AbstractAuthorizedRequestHandler
{
	private static final String ERR_STRING = "error removing user";


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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForOrganization(userIdIn, this.orgUuid) ) ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);

		UInteger userIdToRemove = dbManIn.getUserIdForEmailAddress(this.emailAddress);
		if( (userIdToRemove == null) || 
				(dslContextIn.delete(Organizationusermap.ORGANIZATIONUSERMAP)
						.where(Organizationusermap.ORGANIZATIONUSERMAP.USERID.eq(userIdToRemove))
						.execute() < 1) )
		{
			ErrorManager.throwError(ErrorType.BadRequest, ERR_STRING);
		}

		return null;
	}
}
