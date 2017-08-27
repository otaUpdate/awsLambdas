package net.otaupdate.lambdas.handlers.api.organizations;

import java.util.ArrayList;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizationusermap;
import net.otaupdate.lambdas.util.BreakwallAwsException;


public class GetOrganizationsHandler extends AbstractAuthorizedRequestHandler
{
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String uuid;
		public final String name;

		public ReturnValue(String uuidIn, String nameIn)
		{
			this.uuid = uuidIn;
			this.name = nameIn;
		}
	}


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{	
		return true;
	}


	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		List<ReturnValue> retVal = new ArrayList<ReturnValue>();

		Result<Record2<String, String>> result = 
				dslContextIn.select(Organizations.ORGANIZATIONS.UUID, Organizations.ORGANIZATIONS.NAME)
				.from(Organizations.ORGANIZATIONS)
				.join(Organizationusermap.ORGANIZATIONUSERMAP)
				.on(Organizations.ORGANIZATIONS.ID.eq(Organizationusermap.ORGANIZATIONUSERMAP.ORGANIZATIONID))
				.where(Organizationusermap.ORGANIZATIONUSERMAP.AWSSUB.eq(userIn.getAwsSub()))
				.fetch();

		for( Record2<String, String> currEntry : result )
		{
			retVal.add( new ReturnValue(currEntry.get(Organizations.ORGANIZATIONS.UUID), currEntry.get(Organizations.ORGANIZATIONS.NAME)) );
		}

		return retVal;
	}
}
