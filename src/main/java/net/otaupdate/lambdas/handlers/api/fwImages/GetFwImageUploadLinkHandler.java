package net.otaupdate.lambdas.handlers.api.fwImages;

import java.util.Map;

import org.jooq.DSLContext;

import net.otaupdate.lambdas.AwsPassThroughBody;
import net.otaupdate.lambdas.AwsPassThroughParameters;
import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.handlers.ExecutingUser;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.BreakwallAwsException;
import net.otaupdate.lambdas.util.BreakwallAwsException.ErrorType;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;


public class GetFwImageUploadLinkHandler extends AbstractAuthorizedRequestHandler
{	

	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final String link;

		ReturnValue(String linkIn)
		{
			this.link = linkIn;
		}
	}


	private String orgUuid = null;
	private String devTypeUuid = null;
	private String procTypeUuid = null;
	private String fwUuid = null;


	@Override
	public boolean parseAndValidateParameters(AwsPassThroughParameters paramsIn, AwsPassThroughBody bodyIn)
	{
		Map<String, Object> pathParameters = paramsIn.getPathParameters();
		if( pathParameters == null ) return false;

		// parse our parameters    	
		this.orgUuid = ObjectHelper.parseObjectFromMap(pathParameters, "orgUuid", String.class);
		if( (this.orgUuid == null) || this.orgUuid.isEmpty() ) return false;

		this.devTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "devTypeUuid", String.class);
		if( (this.devTypeUuid == null) || this.devTypeUuid.isEmpty() ) return false;

		this.procTypeUuid = ObjectHelper.parseObjectFromMap(pathParameters, "procTypeUuid", String.class);
		if( (this.procTypeUuid == null) || this.procTypeUuid.isEmpty() ) return false;

		this.fwUuid = ObjectHelper.parseObjectFromMap(pathParameters, "fwUuid", String.class);
		if( (this.fwUuid == null) || this.fwUuid.isEmpty() ) return false;

		return true;
	}


	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, DSLContext dslContextIn, ExecutingUser userIn) throws BreakwallAwsException
	{	
		// check user permissions
		if( !userIn.hasPermissionForFirmware(this.orgUuid, this.devTypeUuid, this.procTypeUuid, this.fwUuid, dslContextIn) ) throw new BreakwallAwsException(ErrorType.BadRequest, "not authorized to access this resource");

		// we're creating a new firmware image
		String url = S3Helper.getLimitedAccessUploadUrlForFirmwareWithUuid(this.fwUuid);
		if( url == null ) throw new BreakwallAwsException(ErrorType.BadRequest, "unable to generate url for given parameters");

		return new ReturnValue(url);
	}
}
