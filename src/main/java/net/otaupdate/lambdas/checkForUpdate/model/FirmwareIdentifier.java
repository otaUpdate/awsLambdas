package net.otaupdate.lambdas.checkForUpdate.model;

public class FirmwareIdentifier
{
	private final String uuid;
	
	
	public FirmwareIdentifier(String uuidIn)
	{
		this.uuid = uuidIn;
	}
	
	
	public String getFirmwareUuid()
	{
		return this.uuid;
	}
	
	
	public String toString()
	{
		return String.format("{\"firmwareUuid\": \"%s\"}", 
				this.getFirmwareUuid());
	}
}
