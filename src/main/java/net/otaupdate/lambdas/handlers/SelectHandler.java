package net.otaupdate.lambdas.handlers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;


public class SelectHandler extends AbstractMultiplexedRequestHandler
{
	@Override
	public Object handleRequestWithParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		String tableName = ObjectHelper.parseObjectFromMap(paramsIn, "tableName", String.class);
    	if( tableName == null )
    	{
    		ErrorManager.throwError(ErrorType.BadRequest, "problem parsing input parameters");
    	}
    	
    	String joinClause = this.parseJoinClause(paramsIn);
    	String whereClause = this.parseWhereClause(paramsIn);
    	String resultColumns = this.parseResultColumns(paramsIn);
    	String authToken = this.parseAuthToken(paramsIn);
    	String userIdColumn = this.parseUserIdColumn(paramsIn);
		
    	// log some important info
    	Logger.getSingleton().debug(String.format("authToken: '%s", authToken));
    	Logger.getSingleton().debug(String.format("tableName: '%s", tableName));
    	Logger.getSingleton().debug(String.format("joinClause: '%s", joinClause));
    	Logger.getSingleton().debug(String.format("resultColumns: '%s", resultColumns));
    	
		// setup a connection to our database
    	DatabaseManager dbMan = null;
    	try{ dbMan = new DatabaseManager(); } 
    	catch( SQLException e ) { ErrorManager.throwError(ErrorType.ServerError, "problem connecting to database"); }
    	
    	// get the userId (and make sure the authToken is still valid)
    	Integer userId = null;
    	if( (authToken == null) || ((userId = dbMan.getUserIdForLoginToken(authToken)) == null) )
    	{
    		ErrorManager.throwError(ErrorType.Unauthorized, "invalid authorization token for resource");
    	}
    	
    	// append our userId column filter if needed
    	if( userIdColumn != null ) whereClause = this.appendUserIdToWhereClause(userIdColumn, userId.intValue(), whereClause);
    	Logger.getSingleton().debug(String.format("whereClause: '%s", whereClause));
    	
    	// always make sure we return an array (even if it's empty)
    	List<Map<String, Object>> retVal = new ArrayList<Map<String,Object>>();
    	try
    	{
    		retVal = dbMan.listTableContents(tableName, joinClause, whereClause, resultColumns);
    	}
    	catch( Exception e )
    	{
    		ErrorManager.throwError(ErrorType.ServerError, "unhandled exception");
    	}
    	finally
    	{
    		dbMan.close();
    	}
 
    	return retVal;
	}
	
	
	private String parseJoinClause(HashMap<String, Object> paramsIn)
	{
		List<?> joinArray = ObjectHelper.parseObjectFromMap(paramsIn, "join", List.class);
		if( joinArray == null ) return null;
		
		String retVal = "";
		for( int i = 0; i < joinArray.size(); i++ )
		{
			Map<?, ?> currEntry = ObjectHelper.parseObjectFromArray(joinArray, i, Map.class);
			if( currEntry == null ) return null;
			
			String joinType = ObjectHelper.parseObjectFromMap(currEntry, "joinType", String.class);
			if( (joinType == null) || joinType.isEmpty() ) joinType = "JOIN";
			
			String tableName = ObjectHelper.parseObjectFromMap(currEntry, "tableName", String.class);
			if( tableName == null ) return null;
			
			String leftTableColumn = ObjectHelper.parseObjectFromMap(currEntry, "leftTableColumn", String.class);
			if( leftTableColumn == null ) return null;
			
			String rightTableColumn = ObjectHelper.parseObjectFromMap(currEntry, "rightTableColumn", String.class);
			if( rightTableColumn == null ) return null;
			
			retVal += String.format(" %s `%s` ON %s=%s", joinType, tableName, leftTableColumn, rightTableColumn);
		}
		
		return retVal;
	}
	
	
	private String parseWhereClause(HashMap<String, Object> paramsIn)
	{
		List<?> whereArray = ObjectHelper.parseObjectFromMap(paramsIn, "where", List.class);
		if( whereArray == null ) return null;
		
		String retVal = "";
		for( int i = 0; i < whereArray.size(); i++ )
		{
			String currEntry = ObjectHelper.parseObjectFromArray(whereArray, i, String.class);
			if( currEntry == null ) return null;
			
			if( i == 0 ) retVal += " WHERE " + currEntry;
			else retVal += " AND " + currEntry;
		}
		
		return retVal;
	}
	
	
	private String parseResultColumns(HashMap<String, Object> paramsIn)
	{
		List<?> resultColumnArray = ObjectHelper.parseObjectFromMap(paramsIn, "resultColumns", List.class);
		if( resultColumnArray == null ) return null;
		
		String retVal = "";
		for( int i = 0; i < resultColumnArray.size(); i++ )
		{
			String currEntry = ObjectHelper.parseObjectFromArray(resultColumnArray, i, String.class);
			if( currEntry == null ) return null;
			
			if( i != resultColumnArray.size()-1 ) retVal += currEntry + ", ";
			else retVal += currEntry;
		}
		
		return retVal;
	}
	
	
	private String parseUserIdColumn(HashMap<String, Object> paramsIn)
	{
		return ObjectHelper.parseObjectFromMap(paramsIn, "mapUserIdToColumn", String.class);
	}
	
	
	private String appendUserIdToWhereClause(String userIdColumnIn, Integer userIdIn, String whereClauseIn)
	{
		String retVal = whereClauseIn;
		
		if( retVal.isEmpty() ) retVal = " WHERE "; 
		else retVal += " AND ";
		retVal += String.format("%s=%d", userIdColumnIn, userIdIn.intValue());
		
		return retVal;
	}
}
