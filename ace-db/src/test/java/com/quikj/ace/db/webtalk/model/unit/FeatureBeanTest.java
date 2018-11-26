/**
 * 
 */
package com.quikj.ace.db.webtalk.model.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.FeatureBean;

/**
 * @author amit
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/META-INF/AoDbSpringBase.xml",
		"/META-INF/AoDbSpringBeans.xml" , "/AoDbSpringOverride.xml"})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class FeatureBeanTest {

	@Autowired
	private FeatureBean featureBean;

	public void setFeatureBean(FeatureBean featureBean) {
		this.featureBean = featureBean;
	}

	@Test
	public void testFeatureOperations() {
		List<FeatureParam> params = new ArrayList<FeatureParam>();
		Feature feature = new Feature(0L, "testFeature", true, "testDomain",
				"com.quikj.feature.Test", params);
		params.add(new FeatureParam("param1", "value1"));
		params.add(new FeatureParam("param2", "value2"));
		sortParams(params);

		featureBean.createFeature(feature);

		Feature retFeature = featureBean.findByName("testFeature");
		assertNotNull(retFeature);

		assertTrue(retFeature.getParams().size() == 2);
		assertEqualsFeature(feature, retFeature, true);

		List<Feature> features = featureBean.listFeatures("testDomain");
		assertNotNull(features);
		assertEquals(1, features.size());
		assertEqualsFeature(feature, features.get(0), false);
		
		features = featureBean.listFeatures(null);
		assertNotNull(features);
		assertTrue(features.size() > 0);
		
		boolean found = false;
		for (Feature f: features) {
			if (f.getName().equals("testFeature")) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			fail();
		}
		
		assertTrue(featureBean.isFeatureActive("testFeature"));
		
		params = new ArrayList<FeatureParam>();
		feature = new Feature(0L, "testFeature", false, "testDomain2",
				"com.quikj.feature.Test2", params);
		params.add(new FeatureParam("param2", "value20"));
		params.add(new FeatureParam("param3", "value3"));
		
		featureBean.modifyFeature(feature);
		
		retFeature = featureBean.findByName("testFeature");
		assertNotNull(retFeature);
		assertTrue(retFeature.getParams().size() == 2);
		assertEquals(feature.getName(), retFeature.getName());
		assertEquals(feature.getDomain(), retFeature.getDomain());
		assertEquals(feature.getClassName(), retFeature.getClassName());
		assertEquals(feature.getParams().size(), feature.getParams().size());
		for (int i = 0; i < feature.getParams().size(); i++) {
			assertTrue(feature.getParams().get(i).getName()
					.equals(retFeature.getParams().get(i).getName()));
			assertTrue(feature.getParams().get(i).getValue()
					.equals(retFeature.getParams().get(i).getValue()));
		}
		
		// Make sure that the domain and the active/inactive has not changed
		assertTrue(retFeature.isActive());
		
		featureBean.setActive("testFeature", false);
		assertFalse(featureBean.isFeatureActive("testFeature"));
		
		featureBean.deleteFeature("testFeature");
		try {
			featureBean.findByName("testFeature");
			fail();
		} catch (WebTalkException e) {
			// Expected
		}
	}

	private void assertEqualsFeature(Feature feature, Feature retFeature,
			boolean assertParams) {
		assertTrue(retFeature.getId() > 0L);
		assertEquals(feature.getName(), retFeature.getName());
		assertEquals(feature.getClassName(), retFeature.getClassName());
		assertEquals(feature.getDomain(), retFeature.getDomain());
		assertEquals(feature.isActive(), retFeature.isActive());

		if (assertParams) {
			assertEquals(feature.getParams().size(), feature.getParams().size());
			for (int i = 0; i < feature.getParams().size(); i++) {
				assertTrue(feature.getParams().get(i).getName()
						.equals(retFeature.getParams().get(i).getName()));
				assertTrue(feature.getParams().get(i).getValue()
						.equals(retFeature.getParams().get(i).getValue()));
			}
		}
	}

	private void sortParams(List<FeatureParam> params) {
		Collections.sort(params, new Comparator<FeatureParam>() {
			@Override
			public int compare(FeatureParam o1, FeatureParam o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
}
