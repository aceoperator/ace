/**
 * 
 * Copyright 2011-2012 QUIK Computing. All rights reserved.
 */
package com.quikj.application.communicator.framework;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.web.context.support.XmlWebApplicationContext;

import com.quikj.server.framework.AceConfigFileHelper;

/**
 * @author Amit Chatterjee
 * 
 */
public class ExternalLocationXmlWebApplicationContext extends
		XmlWebApplicationContext {

	@Override
	public void refresh() throws BeansException, IllegalStateException {
		String home = AceConfigFileHelper.getAcePath("spring/ace-communicator");
		File dir = new File(home);
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".xml")) {
						return true;
					}
					return false;
				}
			});

			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			List<String> newLocations = new ArrayList<String>();

			String[] locations = getConfigLocations();
			if (locations != null) {
				for (String loc : locations) {
					newLocations.add(loc);
				}
			}

			for (File file : files) {
				newLocations.add(file.toURI().toString());
			}

			setConfigLocations(newLocations.toArray(new String[newLocations
					.size()]));
		}
		super.refresh();
	}
}
