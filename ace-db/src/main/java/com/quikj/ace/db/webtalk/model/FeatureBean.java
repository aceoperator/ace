/**
 * 
 */
package com.quikj.ace.db.webtalk.model;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Feature;

/**
 * @author amit
 *
 */
public interface FeatureBean {
	@Transactional(propagation = Propagation.REQUIRED)
	void createFeature(Feature feature);
	
	@Transactional(propagation = Propagation.REQUIRED)
	void setActive(String name, boolean active);
	
	@Transactional(propagation = Propagation.REQUIRED)
	void deleteFeature(String name);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	boolean isFeatureActive(String name);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	List<Feature> listFeatures(String domain);
	
	@Transactional(propagation = Propagation.REQUIRED)
	void modifyFeature(Feature feature);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	Feature findByName(String name);
}
