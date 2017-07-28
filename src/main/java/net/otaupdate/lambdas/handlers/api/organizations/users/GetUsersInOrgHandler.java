package net.otaupdate.lambdas.handlers.api.organizations.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.types.UInteger;
import org.jooq.Record1;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Users;
import net.otaupdate.lambdas.util.ObjectHelper;


public class GetUsersInOrgHandler extends AbstractAuthorizedRequestHandler
{
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String email;

		public ReturnValue(String emailIn)
		{
			this.email = emailIn;
		}
	}
	
	
	private String orgUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;
		
		// parse our parameters
		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;

		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, UInteger userIdIn)
	{
		List<ReturnValue> retVal = new ArrayList<ReturnValue>();
		
		// check user permissions
		if( !dbManIn.doesUserHavePermissionForOrganization(userIdIn, this.orgUuid) ) return retVal;
		
		Result<Record1<String>> result = dslContextIn.select(Users.USERS.EMAIL)
				.from(Organizations.ORGANIZATIONS)
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.ID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID))
				.join(Users.USERS)
				.on(Organizationusermap.ORGANIZATIONUSERMAP.USERID.eq(Users.USERS.ID))
				.where(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.fetch();
		
		for( Record1<String> currEntry : result )
		{
			retVal.add( new ReturnValue(currEntry.getValue(Users.USERS.EMAIL)) );
		}
		
		return retVal;
	}
}
