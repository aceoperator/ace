/*
 * OPMUtil.java
 *
 * Created on March 6, 2003, 7:17 AM
 */

package com.quikj.application.web.talk.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 
 * @author bhm
 * 
 *         Use one instance of this class to handle any number/type of OPMs for
 *         a given key in a given DB table. Key can be one or more strings,
 *         associated with one or more key columns in a DB record. Tell this
 *         class one time what the table name and key column values are. Each DB
 *         record will look like: YourKeyColValue1 (YourKeyColValueN) time_stamp
 *         opm_name opm_value
 * 
 *         Call collectOPM() to accumulate an individual OPM. When you call
 *         averageOPMs(), this class averages the individual OPMs collected
 *         since the last clearOPMs(), per opm_name. Any previous average is
 *         discarded. When you call storeOPMs(), it generates an SQL insert
 *         statement for either the averages (if you call averageOPMs() first)
 *         or the individual OPMs (averageOPMs() not called), and stores the
 *         data in the database.
 * 
 *         Call clearOPMs() to clear out the individual OPMs and the averages.
 * 
 */
public class OPMUtil {

	private class OPMValue {
		private Date timestamp;
		private float opmValue;

		public OPMValue(Date ts, float val) {
			timestamp = ts;
			opmValue = val;
		}

		public float getOpmValue() {
			return opmValue;
		}

		public Date getTimestamp() {
			return timestamp;
		}
	}

	private HashMap<String, List<OPMValue>> opmList = new HashMap<String, List<OPMValue>>();

	private HashMap<String, OPMValue> lastAverages = new HashMap<String, OPMValue>();

	private String tableName;

	private String keyColumnsSql;

	public OPMUtil() {
	}

	public void averageOPMs() {
		averageOPMs(new Date());
	}

	public void averageOPMs(Date timeStamp) {
		lastAverages.clear();

		Iterator<String> e = opmList.keySet().iterator();
		while (e.hasNext()) {
			String opm_name = e.next();
			List<OPMValue> opms = opmList.get(opm_name);

			float sum = (float) 0.0;
			int size = opms.size();

			if (size == 0) {
				return;
			}

			for (int i = 0; i < size; i++) {
				sum += opms.get(i).getOpmValue();
			}

			float average = sum / (float) size;
			lastAverages.put(opm_name, new OPMValue(timeStamp, average));
		}
	}

	public void clearOPMs() {
		opmList.clear();
		lastAverages.clear();
	}

	public void collectOPM(Date timeStamp, String opmName, int opmValue) {
		List<OPMValue> opms = opmList.get(opmName);
		if (opms == null) {
			opms = new ArrayList<OPMValue>();
			opmList.put(opmName, opms);
		}

		opms.add(new OPMValue(timeStamp, opmValue));
	}

	public void collectOPM(String opmName, int opmValue) {
		collectOPM(new Date(), opmName, opmValue);
	}

	private String getDateString(java.util.Date timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(timestamp);

		String dateString = cal.get(Calendar.YEAR) + "-"
				+ (cal.get(Calendar.MONTH) + 1) + "-"
				+ cal.get(Calendar.DAY_OF_MONTH) + " "
				+ cal.get(Calendar.HOUR_OF_DAY) + ":"
				+ cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

		return dateString;
	}

	public int getNumCollectedOPMs(String opmName) {
		List<OPMValue> opms = opmList.get(opmName);
		if (opms != null) {
			return opms.size();
		}

		return 0;
	}

	public void setKeyColumnValue(String val) // if you have just 1 key column
	{
		keyColumnsSql = val;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean storeOPMs() {
		if (OPMHandler.getInstance() == null) {
			return true;
		}

		// use 1 string - INSERT [INTO] tbl_name VALUES ((expression |
		// DEFAULT),...),(...),...

		StringBuffer sql = new StringBuffer("insert into " + tableName
				+ " (groupid, time_stamp, opm_name, opm_value) values ");

		if (lastAverages.size() > 0) // store the averages only and get out
		{
			Set<Entry<String, OPMValue>> entrySet = lastAverages.entrySet();
			if (appendOPM(sql, entrySet) > 0) {
				return OPMHandler.getInstance().executeSQL(sql.toString());
			} else {
				return true;
			}
		}

		// store the individual OPMs
		Set<Entry<String, List<OPMValue>>> entrySet = opmList.entrySet();
		if (appendOPMs(sql, entrySet) > 0) {
			return OPMHandler.getInstance().executeSQL(sql.toString());
		} else {
			return true;
		}
	}

	private int appendOPMs(StringBuffer sql,
			Set<Entry<String, List<OPMValue>>> entrySet) {
		int count = 0;
		for (Entry<String, List<OPMValue>> entry : entrySet) {
			String opmName = entry.getKey();
			List<OPMValue> opms = entry.getValue();

			for (OPMValue opm : opms) {
				if (opm.getOpmValue() > 0.0) {
					if (count > 0) {
						sql.append(",");
					}

					sql.append("('" + keyColumnsSql + "','"
							+ getDateString(opm.getTimestamp()) + "','"
							+ opmName + "'," + opm.getOpmValue() + ")");
					count++;
				}
			}
		}

		return count;
	}

	private int appendOPM(StringBuffer sql,
			Set<Entry<String, OPMValue>> entrySet) {
		int count = 0;
		for (Entry<String, OPMValue> entry : entrySet) {
			String opmName = entry.getKey();
			OPMValue opm = entry.getValue();

			if (opm.getOpmValue() > 0.0) {
				if (count > 0) {
					sql.append(",");
				}

				sql.append("('" + keyColumnsSql + "','"
						+ getDateString(opm.getTimestamp()) + "','" + opmName
						+ "'," + opm.getOpmValue() + ")");
				count++;
			}
		}

		return count;
	}
}
