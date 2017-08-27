package net.otaupdate.lambdas.handlers.api.organizations.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.CognitoHelper;
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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{
		List<ReturnValue> retVal = new ArrayList<ReturnValue>();
		
		// check user permissions
		if( !userIn.hasPermissionForOrganization(this.orgUuid, dslContextIn) ) return retVal;
		
		Result<Record1<String>> result = dslContextIn.select(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB)
				.from(Organizationusermap.ORGANIZATIONUSERMAP)
				.join(Organizations.ORGANIZATIONS)
				.on(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID.eq(Organizations.ORGANIZATIONS.ID))
				.where(Organizations.ORGANIZATIONS.UUID.eq(this.orgUuid))
				.fetch();
		
		for( Record1<String> currEntry : result )
		{
			String currAwsSub = currEntry.getValue(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB);
			String email = CognitoHelper.getEmailFromAwsSub(currAwsSub);
			if( email != null ) retVal.add( new ReturnValue(email) );
		}
		
		return retVal;
	}
}
