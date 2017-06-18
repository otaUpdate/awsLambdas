package net.otaupdate.lambdas.util;


public class HardwareIdentifier
{
	private final String hardwareUuid;
	
	
	public HardwareIdentifier(String hardwareId)
	{
		this.hardwareUuid = hardwareId;
	}
	
	
	public String getHardwareUuid()
	{
		return this.hardwareUuid;
	}
	
	
	public String toString()
	{
		return String.format("{\"hardwareUuid\": \"%s\"}", 
				this.hardwareUuid);
	}
}
