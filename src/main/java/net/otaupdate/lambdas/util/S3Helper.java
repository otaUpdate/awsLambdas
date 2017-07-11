package net.otaupdate.lambdas.util;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

public class S3Helper
{
	public static final String FW_S3BUCKET = "net-otaupdate-firmwareimages";
	private static final int LINK_EXPIRATION_S = 60;
	
	
	public static boolean doesImageExistForFirmwareWithUuid(String fwUuidIn)
	{
		return AmazonS3ClientBuilder.defaultClient().doesObjectExist(FW_S3BUCKET, fwUuidIn);
	}
	
	
	public static String getLimitedAccessDownloadUrlForFirmwareWithUuid(String fwUuidIn)
	{
		if( !doesImageExistForFirmwareWithUuid(fwUuidIn) ) return null;
		
		// create our request
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(FW_S3BUCKET, fwUuidIn);
		req.setMethod(HttpMethod.GET);
		
		// set our expiration
		Date expiration = new Date();
		expiration.setTime(expiration.getTime() + (LINK_EXPIRATION_S * 1000));
		req.setExpiration(expiration);
		
		// get our URL
		URL s = AmazonS3ClientBuilder.defaultClient().generatePresignedUrl(req);
		return (s != null) ? s.toString() : null;
	}
	
	
	public static byte[] getBytesForFirmwareUuid(String fwUuidIn, Integer offsetIn, Integer numBytesIn)
	{
		if( !doesImageExistForFirmwareWithUuid(fwUuidIn) ) return null;
		
		GetObjectRequest req = new GetObjectRequest(FW_S3BUCKET, fwUuidIn);
		req.setRange(offsetIn, offsetIn+numBytesIn-1);
		
		// create our request
		S3Object object = AmazonS3ClientBuilder.defaultClient().getObject(req);
		if( object == null ) return null;
		
		// get the bytes
		byte[] retVal = null;
		try
		{
			retVal = IOUtils.toByteArray(object.getObjectContent());
		} catch (IOException e) { }
		
		return retVal;
	}
	
	
	public static String getLimitedAccessUploadUrlForFirmwareWithUuid(String fwUuidIn)
	{
		// create our request
		GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(FW_S3BUCKET, fwUuidIn);
		req.setMethod(HttpMethod.PUT);
		
		// set our expiration
		Date expiration = new Date();
		expiration.setTime(expiration.getTime() + (LINK_EXPIRATION_S * 1000));
		req.setExpiration(expiration);
		
		// get our URL
		URL s = AmazonS3ClientBuilder.defaultClient().generatePresignedUrl(req);
		return (s != null) ? s.toString() : null;
	}
	
	
	public static void deleteImageForFirmwareWithUuid(String fwUuidIn)
	{
		AmazonS3ClientBuilder.defaultClient().deleteObject(FW_S3BUCKET, fwUuidIn);
	}
}
