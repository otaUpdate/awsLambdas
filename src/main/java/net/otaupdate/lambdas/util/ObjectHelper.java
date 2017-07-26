package net.otaupdate.lambdas.util;

import java.util.List;
import java.util.Map;

public class ObjectHelper
{
	public static <T> T castObjectToType(Object objectIn, Class<T> typeIn)
	{
		if( objectIn == null ) return null;
		
		T retVal = null;
		try
		{
			retVal = typeIn.cast(objectIn);
		}
		catch( ClassCastException e ) { }
		
		return retVal;
	}
	
	
	public static <T> T parseObjectFromMap(Map<?, ?> mapIn, String keyIn, Class<T> typeIn)
	{
		if( mapIn == null ) return null;
		
		Object value_raw = mapIn.get(keyIn);
		if( value_raw == null ) return null;
		
		return castObjectToType(value_raw, typeIn);
	}
	
	
	public static <T> T parseObjectFromNestedMaps(Map<?, ?> mapIn, String pathIn[], Class<T> typeIn)
	{
		if( mapIn == null ) return null;
		
		T retVal = null;
		
		Map<?, ?> currMap = mapIn;
		for( int i = 0; i < pathIn.length; i++ )
		{
			if( i == (pathIn.length-1) )
			{
				// this is our last entry...get our target entry 
				retVal = parseObjectFromMap(currMap, pathIn[i], typeIn);
			}
			else
			{
				// not our last entry...keep digging into the maps
				currMap = parseObjectFromMap(currMap, pathIn[i], Map.class);
				if( currMap == null ) return null;
			}
		}
		
		return retVal;
	}
	
	
	public static <T> T parseObjectFromList(List<?> listIn, int indexIn, Class<T> typeIn)
	{
		if( listIn == null ) return null;
		
		Object value_raw = listIn.get(indexIn);
		if( value_raw == null ) return null;
		
		return castObjectToType(value_raw, typeIn);
	}
}
