package net.otaupdate.lambdas.util;

import java.net.URL;
import java.util.Date;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

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
