package com.quikj.application.web.talk.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;

import org.junit.Test;

import com.quikj.application.web.talk.plugin.impl.ContentFilterImpl;

public class ContentFilterTest {

	private ContentFilterImpl contentFilter = new ContentFilterImpl();

	@Test
	public void testScrubHtml() throws Exception {

		// Verify that non-restricted tags are not filtered
		String html = contentFilter.scrubHtml("plain text");
		assertNotNull(html);
		assertEquals("plain text", html);

		html = contentFilter.scrubHtml("<b>bold text</b>");
		assertNotNull(html);
		assertEquals("<b>bold text</b>", html);

		html = contentFilter.scrubHtml("<b><i>bold italicized text</i></b>");
		assertNotNull(html);
		assertEquals("<b><i>bold italicized text</i></b>", html);

		// Verify that restricted tags containing external addresses are
		// filtered
		html = contentFilter
				.scrubHtml("<a href='http://www.quik-j.com'>my web site</a>");
		assertNotNull(html);
		assertEquals("<a href=\"/ace-contactcenter/blocked.html\">my web site</a>", html);

		html = contentFilter
				.scrubHtml("<img src='http://www.quik-j.com/img.png'>");
		assertNotNull(html);
		assertEquals("<img src=\"/ace-contactcenter/images/file_locked.png\" />", html);

		html = contentFilter
				.scrubHtml("<a href='http://www.quik-j.com'><img src='http://www.quik-j.com/img.png'></a>");
		assertNotNull(html);
		assertEquals(
				"<a href=\"/ace-contactcenter/blocked.html\"><img src=\"/ace-contactcenter/images/file_locked.png\" /></a>",
				html);

		// Verify that script/object/embed/audio/video tags are filtered out
		html = contentFilter
				.scrubHtml("<script src='http://www.quik-j.com/script.js'></script>");
		assertNotNull(html);
		assertEquals("", html);

		html = contentFilter.scrubHtml("<script>var i = 0;</script>");
		assertNotNull(html);
		assertEquals("", html);

		html = contentFilter
				.scrubHtml("<blockquote>Here is a hidden script<script src='http://localhost/script.js'></script></blockquote>");
		assertNotNull(html);
		assertEquals("<blockquote>\n Here is a hidden script\n</blockquote>",
				html);

		html = contentFilter
				.scrubHtml("<p>Here is a great video<video width=\"320\" height=\"240\" controls>"
						+ "<source src=\"movie.mp4\" type=\"video/mp4\">"
						+ "<object data=\"movie.mp4\" width=\"320\" height=\"240\">"
						+ "<embed src=\"movie.swf\" width=\"320\" height=\"240\">"
						+ "</object>" + "</video></p>");
		assertNotNull(html);
		assertEquals("<p>Here is a great video</p>", html);

		// Verify that restricted tags containing internal addresses are not
		// filtered
		html = contentFilter
				.scrubHtml("<a href='/index.html'><img src='/img.png'></a>");
		assertNotNull(html);
		assertEquals("<a href=\"/index.html\"><img src=\"/img.png\" /></a>",
				html);

		html = contentFilter
				.scrubHtml("<a href='http://localhost'><img src='http://localhost/img.png'></a>");
		assertNotNull(html);
		assertEquals(
				"<a href=\"http://localhost\"><img src=\"http://localhost/img.png\" /></a>",
				html);

		html = contentFilter
				.scrubHtml("<a href='http://127.0.0.1'><img src='http://127.0.0.1/img.png'></a>");
		assertNotNull(html);
		assertEquals(
				"<a href=\"http://127.0.0.1\"><img src=\"http://127.0.0.1/img.png\" /></a>",
				html);

		String host = InetAddress.getLocalHost().getHostName();
		html = contentFilter.scrubHtml("<a href='http://" + host
				+ "'><img src='http://" + host + "/img.png'></a>");
		assertNotNull(html);
		assertEquals("<a href=\"http://" + host + "\"><img src=\"http://"
				+ host + "/img.png\" /></a>", html);
	}
}
