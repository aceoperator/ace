/**
 * 
 */
package com.quikj.server.framework;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author amit
 *
 */
public interface MapResult {	
	public Object map(ResultSet resultset) throws SQLException;
}
