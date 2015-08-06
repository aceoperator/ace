/**
 * 
 */
package com.quikj.application.web.talk.plugin.impl;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.quikj.application.web.talk.plugin.ContentFilter;
import com.quikj.server.framework.AceLogger;

/**
 * @author amit
 * 
 */
public class ContentFilterImpl implements ContentFilter {

	private boolean enabled = true;
	
	private String blockedLinkHref = "/ace-contactcenter/blocked.html";
	
	private String blockedImageSrc = "/ace-contactcenter/images/file_locked.png";

	private Set<String> ipWhiteList = new HashSet<String>();

	private String[] restrictedTags = { "img", "a", "script", "object",
			"embed", "audio", "video" };

	public ContentFilterImpl() {
		try {
			Enumeration<NetworkInterface> networkAddresses = NetworkInterface
					.getNetworkInterfaces();
			while (networkAddresses.hasMoreElements()) {
				List<InterfaceAddress> list = networkAddresses.nextElement()
						.getInterfaceAddresses();
				for (InterfaceAddress e : list) {
					ipWhiteList.add(e.getAddress().getHostAddress());
				}
			}
		} catch (SocketException e) {
			AceLogger
			.Instance()
			.log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
					"ContentFilterImpl.ContentFilterImpl() -- error resolving host name", e);
		}
	}

	private boolean isMyAddress(String host) {
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			return false;
		}

		for (String ip : ipWhiteList) {
			if (address.getHostAddress().equals(ip)) {
				return true;
			}
		}

		return false;
	}

	private String isRestricted(String tagName) {
		for (String restricted : restrictedTags) {
			if (restricted.equals(tagName.toLowerCase())) {
				return restricted;
			}
		}

		return null;
	}

	@Override
	public String scrubHtml(String html) {
		if (!enabled) {
			return html;
		}

		Document doc = Jsoup.parse(html);
		Elements elements = doc.children();
		scrubNode(elements);

		return doc.body().html();
	}

	private void scrubNode(Elements elements) {
		for (int i = 0; i < elements.size(); i++) {
			Element element = elements.get(i);
			String tagName = element.tagName();
			if (tagName == null) {
				continue;
			}

			String restricted = isRestricted(tagName);
			if (restricted != null) {
				if (restricted.equals("img")) {
					scrubUrl("src", element, true, false, blockedImageSrc);
				} else if (restricted.equals("a")) {
					scrubUrl("href", element, true, false, blockedLinkHref);
				} else if (restricted.equals("script")
						|| restricted.equals("object")
						|| restricted.equals("embed")
						|| restricted.equals("video")
						|| restricted.equals("audio")) {
					element.remove();
					continue;
				}
			}

			Elements children = element.children();
			if (children != null) {
				scrubNode(children);
			}
		}
	}

	private boolean scrubUrl(String attributeName, Element element,
			boolean sameHostCheck, boolean removeAttribute, String filteredValue) {

		Attributes attributes = element.attributes();
		String attrib = attributes.get(attributeName);
		if (attrib == null) {
			return false;
		}

		try {
			if (sameHostCheck) {				
				URL url = new URL(attrib);
				if (url.getProtocol() == null) {
					return false;
				}

				if (isMyAddress(url.getHost())) {
					return false;
				}
			}

			if (removeAttribute) {
				element.removeAttr(attributeName);
			} else {
				element.attr(attributeName, filteredValue);
			}

			return true;

		} catch (MalformedURLException e) {
			// This scenario will occur when the URL does not contain a protocol (http/https/etc.) 
			return false;
		} catch (Exception e) {
			AceLogger
			.Instance()
			.log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
					"ContentFilterImpl.scrubUrl() -- error scrubbing URL", e);
			return false;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setIpWhiteList(Set<String> ipWhiteList) {
		this.ipWhiteList.addAll(ipWhiteList);
	}

	public void setRestrictedTags(String[] restrictedTags) {
		this.restrictedTags = restrictedTags;
	}
	
	public void setBlockedLinkHref(String blockedLinkHref) {
		this.blockedLinkHref = blockedLinkHref;
	}

	public void setBlockedImageSrc(String blockedImageSrc) {
		this.blockedImageSrc = blockedImageSrc;
	}
}
