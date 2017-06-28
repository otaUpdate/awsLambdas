package net.otaupdate.lambdas.handlers.devController;

import java.util.HashMap;

import net.otaupdate.lambdas.handlers.AbstractUnauthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ObjectHelper;
import net.otaupdate.lambdas.util.S3Helper;


public class GetFirmwareDownloadLinkHandler extends AbstractUnauthorizedRequestHandler
{
	@SuppressWarnings("unused")
	private class ReturnValue
	{
		public final boolean downloadAvailable;
		public final String name;
		public final String url;
		
		
		ReturnValue(String nameIn, String urlIn)
		{
			this.downloadAvailable = (nameIn != null) && (urlIn != null);
			this.name = nameIn;
			this.url = urlIn;
		}
	}
	
	
	private String targetFwUuid = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		this.targetFwUuid = ObjectHelper.parseObjectFromMap(paramsIn, "targetFwUuid", String.class);
		if( this.targetFwUuid == null ) return false;
		
		return true;
	}
	

	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn)
	{
		String name = dbManIn.getNameFirmwareUuidd(this.targetFwUuid);
		String retUrl = S3Helper.getLimitedAccessDownloadUrlForFirmwareWithUuid(this.targetFwUuid);
			
    	return new ReturnValue(name, retUrl);
	}

}
