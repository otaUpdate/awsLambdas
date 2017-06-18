package net.otaupdate.lambdas.util;

import java.util.List;
import java.util.Map;

public class ObjectHelper
{
	public static <T> T parseObjectFromMap(Map<?, ?> mapIn, String keyIn, Class<T> typeIn)
	{
		Object value_raw = mapIn.get(keyIn);
		if( value_raw == null ) return null;
		
		T retVal = null;
		try
		{
			retVal = typeIn.cast(value_raw);
		}
		catch( ClassCastException e ) { }
		
		// if we made it here, we failed
		return retVal;
	}
	
	
	public static <T> T parseObjectFromArray(List<?> listIn, int indexIn, Class<T> typeIn)
	{
		Object value_raw = listIn.get(indexIn);
		if( value_raw == null ) return null;
		
		T retVal = null;
		try
		{
			retVal = typeIn.cast(value_raw);
		}
		catch( ClassCastException e ) { }
		
		// if we made it here, we failed
		return retVal;
	}
}
