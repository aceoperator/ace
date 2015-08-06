/**
 * 
 */
package com.quikj.ace.db.webtalk.model;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Blacklist;

/**
 * @author amit
 *
 */
public interface BlacklistBean {
	
	// TODO figure out how to use Jboss distributed cache for cache management.
	
	@Transactional(propagation = Propagation.REQUIRED)
//	@TriggersRemove(cacheName="blacklistCache", removeAll=true)
	void create(Blacklist blacklist);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
//	@Cacheable(cacheName="blacklistCache")
	List<Blacklist> list(String userName);
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
//	@Cacheable(cacheName="blacklistCache")
	Blacklist get(long id);
	
	@Transactional(propagation = Propagation.REQUIRED)
//	@TriggersRemove(cacheName="blacklistCache", removeAll=true)
	void modify(Blacklist blacklist);
	
	@Transactional(propagation = Propagation.REQUIRED)
//	@TriggersRemove(cacheName="blacklistCache", removeAll=true)
	void delete(long id);
}
