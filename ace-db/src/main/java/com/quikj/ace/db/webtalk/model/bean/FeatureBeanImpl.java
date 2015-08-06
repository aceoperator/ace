/**
 * 
 */
package com.quikj.ace.db.webtalk.model.bean;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.dao.FeatureDao;
import com.quikj.ace.db.webtalk.model.FeatureBean;

/**
 * @author amit
 * 
 */
public class FeatureBeanImpl implements FeatureBean {

	private FeatureDao featureDao;

	public void setFeatureDao(FeatureDao featureDao) {
		this.featureDao = featureDao;
	}

	@Override
	public void createFeature(Feature feature) {
		featureDao.createFeature(feature);
		long id = featureDao.getFeatureId(feature.getName());
		for (FeatureParam param : feature.getParams()) {
			featureDao
					.createFeatureParam(id, param.getName(), param.getValue());
		}
	}

	@Override
	public void setActive(String name, boolean active) {
		int affected = featureDao.setActive(name, active);
		if (affected == 0) {
			throw new WebTalkException("The feature is not found");
		}
	}

	@Override
	public void deleteFeature(String name) {
		int affected = featureDao.deleteFeature(name);
		if (affected == 0) {
			throw new WebTalkException("The feature is not found");
		}
	}

	@Override
	public boolean isFeatureActive(String name) {
		return featureDao.isFeatureActive(name);
	}

	@Override
	public List<Feature> listFeatures(String domain) {
		return featureDao.listFeatures(domain);
	}
	
	@Override
	public void modifyFeature(Feature feature) {
		Feature dbFeature = findByName(feature.getName());
		if (dbFeature == null) {
			throw new WebTalkException("The feature is not found");
		}
		
		featureDao.modifyFeature(feature);
		
		// Remove all the old feature params and re-add them
		for (FeatureParam param: dbFeature.getParams()) {
			featureDao.removeFeatureParam(dbFeature.getId(), param.getName());
		}
		
		for (FeatureParam param: feature.getParams()) {
			featureDao.createFeatureParam(dbFeature.getId(), param.getName(), param.getValue());
		}		
	}
	
	@Override
	public Feature findByName(String name) {
		Feature feature = featureDao.findByName(name);
		if (feature == null) {
			throw new WebTalkException("The feature is not found");
		}
		
		List<FeatureParam> params = featureDao.listParams(feature.getId());
		for (FeatureParam param: params) {
			feature.getParams().add(param);
		}
		
		return feature;
	}
}
