package net.otaupdate.lambdas.handlers.general;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.otaupdate.lambdas.handlers.AbstractAuthorizedRequestHandler;
import net.otaupdate.lambdas.model.DatabaseManager;
import net.otaupdate.lambdas.util.ErrorManager;
import net.otaupdate.lambdas.util.ErrorManager.ErrorType;
import net.otaupdate.lambdas.util.Logger;
import net.otaupdate.lambdas.util.ObjectHelper;


public class UpdateHandler extends AbstractAuthorizedRequestHandler
{
	private String tableName = null;
	private String joinClause = null;
	private String setClause = null;
	private String whereClause = null;
	private String userIdColumn = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		this.tableName = ObjectHelper.parseObjectFromMap(paramsIn, "tableName", String.class);
    	if( this.tableName == null ) return false;
    	
    	this.joinClause = this.parseJoinClause(paramsIn);
    	this.setClause = this.parseSetClause(paramsIn);
    	this.whereClause = this.parseWhereClause(paramsIn);
    	this.userIdColumn = this.parseUserIdColumn(paramsIn);
    	
    	return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{
    	// log some important info
    	Logger.getSingleton().debug(String.format("tableName: '%s'", this.tableName));
    	Logger.getSingleton().debug(String.format("joinClause: '%s'", this.joinClause));
    	Logger.getSingleton().debug(String.format("setClause: '%s'", this.joinClause));
    	
    	// append our userId column filter if needed
    	if( userIdColumn != null ) this.whereClause = this.appendUserIdToWhereClause(userIdColumn, userIdIn, this.whereClause);
    	Logger.getSingleton().debug(String.format("whereClause: '%s'", this.whereClause));
    	
    	// do our update
		if( !dbManIn.updateInTable(this.tableName, this.joinClause, this.setClause, this.whereClause) ) ErrorManager.throwError(ErrorType.BadRequest, "error performing update");
		
		return null;
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
	
	
	private String parseSetClause(HashMap<String, Object> paramsIn)
	{
		List<?> setArray = ObjectHelper.parseObjectFromMap(paramsIn, "set", List.class);
		if( setArray == null ) return null;
		
		String retVal = "";
		for( int i = 0; i < setArray.size(); i++ )
		{
			String currEntry = ObjectHelper.parseObjectFromArray(setArray, i, String.class);
			if( currEntry == null ) return null;
			
			if( i == 0 ) retVal += " SET " + currEntry;
			else retVal += ", " + currEntry;
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
