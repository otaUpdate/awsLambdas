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


public class InsertSelectHandler extends AbstractAuthorizedRequestHandler
{
	private String insertTableName = null;
	private String columnNames = null;
	private String columnValues = null;
	private String selectTableName = null;
	private String joinClause = null;
	private String whereClause = null;
	private String userIdColumn = null;
	
	
	@Override
	public boolean parseAndValidateParameters(HashMap<String, Object> paramsIn)
	{
		// parse our parameters
		this.insertTableName = ObjectHelper.parseObjectFromMap(paramsIn, "insertTableName", String.class);
    	if( this.insertTableName == null ) return false;
    	
    	this.columnNames = this.parseColumnNames(paramsIn);
    	this.columnValues = this.parseColumnValues(paramsIn);
    	
		this.selectTableName = ObjectHelper.parseObjectFromMap(paramsIn, "selectTableName", String.class);
    	if( this.selectTableName == null ) return false;
    	
    	this.joinClause = this.parseJoinClause(paramsIn);
    	this.whereClause = this.parseWhereClause(paramsIn);
    	this.userIdColumn = this.parseUserIdColumn(paramsIn);
    	
    	return true;
	}
	
	
	@Override
	public Object processRequestWithDatabaseManager(DatabaseManager dbManIn, int userIdIn)
	{
    	// log some important info
    	Logger.getSingleton().debug(String.format("insertTableName: '%s'", this.insertTableName));
    	Logger.getSingleton().debug(String.format("columnNames: '%s'", this.columnNames));
    	Logger.getSingleton().debug(String.format("columnValues: '%s'", this.columnValues));
    	Logger.getSingleton().debug(String.format("selectTableName: '%s'", this.selectTableName));
    	Logger.getSingleton().debug(String.format("joinClause: '%s'", this.joinClause));
    	
    	// append our userId column filter if needed
    	if( userIdColumn != null ) this.whereClause = this.appendUserIdToWhereClause(userIdColumn, userIdIn, this.whereClause);
    	Logger.getSingleton().debug(String.format("whereClause: '%s'", this.whereClause));
    	
    	// do our update
		if( !dbManIn.insertSelect(this.insertTableName, this.columnNames, this.columnValues, this.selectTableName, this.joinClause, this.whereClause) )
		{
			ErrorManager.throwError(ErrorType.BadRequest, "error performing insert select");
		}
		
		return null;
	}
	
	
	private String parseColumnNames(HashMap<String, Object> paramsIn)
	{
		List<?> colNameArray = ObjectHelper.parseObjectFromMap(paramsIn, "columnNames", List.class);
		if( colNameArray == null ) return null;
		
		String retVal = "";
		for( int i = 0; i < colNameArray.size(); i++ )
		{
			if( retVal.isEmpty() ) retVal += "( ";
			
			if( i == 0 ) retVal += colNameArray.get(i);
			else retVal += String.format(", %s", colNameArray.get(i));
			
			if( i == (colNameArray.size() - 1) ) retVal += " )";
		}
		
		return retVal;
	}
	
	
	private String parseColumnValues(HashMap<String, Object> paramsIn)
	{
		List<?> colNameArray = ObjectHelper.parseObjectFromMap(paramsIn, "columnValues", List.class);
		if( colNameArray == null ) return null;
		
		String retVal = "";
		for( int i = 0; i < colNameArray.size(); i++ )
		{			
			if( i == 0 ) retVal += colNameArray.get(i);
			else retVal += String.format(", %s", colNameArray.get(i));
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
