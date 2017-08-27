package net.otaupdate.lambdas.handlers.api.organizations;

import java.util.Map;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.Result;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.model.db.otaupdates.tables.Organizations;
import net.otaupdate.lambdas.model.db.otaupdates.tables.records.OrganizationsRecord;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;

public class CreateOrganizationHandler extends AbstractAuthorizedRequestHandler
{
	private static final String TAG = CreateOrganizationHandler.class.getSimpleName();
	private static final String ERR_STR = "error creating organization";


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
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{
		String orgUuid = UUID.randomUUID().toString();

		// create the organization
		Result<OrganizationsRecord> result = 
				dslContextIn.insertInto(Organizations.ORGANIZATIONS, Organizations.ORGANIZATIONS.UUID, Organizations.ORGANIZATIONS.NAME)
				.values(orgUuid, this.orgName)
				.returning(Organizations.ORGANIZATIONS.ID)
				.fetch();
		if( result.size() < 1 ) throw new BreakwallAwsException(ErrorType.BadRequest, ERR_STR);


		// add the user
		if( !dbManIn.addUserToOrganization(userIn.getAwsSub(), result.get(0).getId()) )
		{
			Logger.getSingleton().warn(TAG, String.format("error assigning creating user to organization, organization orphaned '%s'", orgUuid));
			throw new BreakwallAwsException(ErrorType.ServerError, ERR_STR);
		}

		return new ReturnValue(orgUuid);
	}

}
