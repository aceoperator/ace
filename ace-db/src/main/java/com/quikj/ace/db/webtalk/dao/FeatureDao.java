/**
 * 
 */
package com.quikj.ace.db.webtalk.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;

/**
 * @author amit
 * 
 */
public interface FeatureDao {

	int setActive(@Param("name") String name, @Param("active") boolean active);

	void createFeature(Feature feature);

	void createFeatureParam(@Param("featureId") long featureId,
			@Param("param") String param, @Param("value") String value);

	int deleteFeature(String name);

	boolean isFeatureActive(String name);

	List<Feature> listFeatures(@Param("domain") String domain);

	int modifyFeature(Feature feature);

	Feature findByName(String name);

	List<FeatureParam> listParams(String name);

	long getFeatureId(String name);

	List<FeatureParam> listParams(long id);

	void removeFeatureParam(@Param("featureId") long featureId,
			@Param("param") String param);
}
