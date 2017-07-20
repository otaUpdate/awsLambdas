package net.otaupdate.lambdas.handlers.devController;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ObjectHelper;


public class GetProcInstanceInfoHandler extends AbstractUnauthorizedRequestHandler
{
	public static class GetProcInstanceInfoResponse
	{
		public final String lastCheckInUTC;
		public final String currFwUuid;
		public final String currFwName;
		public final Boolean updateAvailable;
		
		public GetProcInstanceInfoResponse(Date lastCheckInIn, String currFwUuidIn, String currFwNameIn, Boolean isUpdateAvailableIn)
		{
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			this.lastCheckInUTC = sdf.format(lastCheckInIn);
			
			this.currFwUuid = currFwUuidIn;
			this.currFwName = currFwNameIn;
			this.updateAvailable = isUpdateAvailableIn;
		}
	}
	
	
	private String organizationUuid = null;
	private String deviceUuid = null;
	private String processorUuid = null;
	private String serialNum = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		this.organizationUuid = ObjectHelper.parseObjectFromMap(paramsIn, "organizationUuid", String.class);
		if( this.organizationUuid == null ) return false;
		
		this.deviceUuid = ObjectHelper.parseObjectFromMap(paramsIn, "deviceUuid", String.class);
		if( this.deviceUuid == null ) return false;
		
		this.processorUuid = ObjectHelper.parseObjectFromMap(paramsIn, "processorUuid", String.class);
		if( this.processorUuid == null ) return false;
		
		this.serialNum = ObjectHelper.parseObjectFromMap(paramsIn, "serialNum", String.class);
		if( this.serialNum == null ) return false;
		
		return true;
	}
	

	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn)
	{	
		Object retVal = dbManIn.getProcInstanceInfoResponse(this.serialNum, this.processorUuid, this.deviceUuid, this.organizationUuid);
		if( retVal == null ) retVal = new Object();
		return retVal;
	}
}
