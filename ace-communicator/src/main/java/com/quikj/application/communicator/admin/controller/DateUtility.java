/*
 * DateUtility.java
 *
 * Created on June 4, 2003, 11:13 AM
 */

package com.quikj.application.communicator.admin.controller;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author bhm
 */
public class DateUtility {
	public static Date processInputDate(String date) // takes string of
														// mm/dd/yyyy
	{
		return processInputDate(date, 0, 0, 0);
	}

	public static Date processInputDate(String date, int hr, int min, int sec) // date
																				// =
																				// string
																				// of
																				// mm/dd/yyyy
	{
		if ((date == null) || (date.length() != 10)) {
			return null;
		}

		try {
			Calendar cal = Calendar.getInstance();
			cal.setLenient(false);
			cal.set(Calendar.YEAR, Integer.parseInt(date.substring(6)));
			cal.set(Calendar.MONTH, Integer.parseInt(date.substring(0, 2)) - 1);
			cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date
					.substring(3, 5)));
			cal.set(Calendar.HOUR_OF_DAY, hr);
			cal.set(Calendar.MINUTE, min);
			cal.set(Calendar.SECOND, sec);

			return cal.getTime();
		} catch (NumberFormatException ex) {
		} catch (IllegalArgumentException ex1) {
		}
		return null;
	}

}
