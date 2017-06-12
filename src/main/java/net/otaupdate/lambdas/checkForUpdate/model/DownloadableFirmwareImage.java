package net.otaupdate.lambdas.checkForUpdate.model;

import java.net.URL;
import java.util.Date;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

public class DownloadableFirmwareImage
{
	private static final int LINK_EXPIRATION_S = 60;
	
	
	private final String name;
	private final String uuid;
	private final String s3bucket;
	private final String s3key;
	
	
	public DownloadableFirmwareImage(String nameIn, String uuidIn, String s3bucketIn, String s3keyIn)
	{
		this.name = nameIn;
		this.uuid = uuidIn;
		this.s3bucket = s3bucketIn;
		this.s3key = s3keyIn;
	}
	
	
	public boolean hasStoredFirmwareFile()
	{
		return (this.s3bucket != null) && (this.s3key != null);
	}
	
	
	public String getName()
	{
		return this.name;
	}
	
	
	public String getUuid()
	{
		return this.uuid;
	}
	
	
	public String getLimitedAccessUrl()
	{
		if( !this.hasStoredFirmwareFile() ) return null;
		
		// create our request
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(this.s3bucket, this.s3key);
		req.setMethod(HttpMethod.GET);
		
		// set our expiration
		Date expiration = new Date();
		expiration.setTime(expiration.getTime() + (LINK_EXPIRATION_S * 1000));
		req.setExpiration(expiration);
		
		// get our URL
		URL s = AmazonS3ClientBuilder.defaultClient().generatePresignedUrl(req);
		return (s != null) ? s.toString() : null;
	}
}
