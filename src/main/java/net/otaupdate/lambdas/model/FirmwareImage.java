package net.otaupdate.lambdas.model;

import net.otaupdate.lambdas.util.S3Helper;


public class FirmwareImage
{
	private final String name;
	private final String uuid;
	
	
	public FirmwareImage(String nameIn, String uuidIn)
	{
		this.name = nameIn;
		this.uuid = uuidIn;
	}
	
	
	public boolean hasStoredFirmwareFile()
	{
		return S3Helper.doesImageExistForFirmwareWithUuid(this.uuid);
	}
	
	
	public String getName()
	{
		return this.name;
	}
	
	
	public String getUuid()
	{
		return this.uuid;
	}
}
