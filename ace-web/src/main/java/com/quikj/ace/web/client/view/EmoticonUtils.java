/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;

/**
 * @author amit
 * 
 */
public class EmoticonUtils {

	public static class EmoticonInfo {
		public String url;
		public String description;
		public String[] textRepresentations;
		public boolean display;

		public EmoticonInfo(String url, String description,
				String[] textRepresentations, boolean display) {
			this.url = url;
			this.description = description;
			this.textRepresentations = textRepresentations;
			this.display = display;
		}
	}

	private static List<EmoticonInfo> emoticons = new ArrayList<EmoticonUtils.EmoticonInfo>();
	private static boolean initialized = false;

	private static void initEmoticons() {
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0100-smile.gif", "Smile",
				new String[] { ":)", ":=)", ":-)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0101-sadsmile.gif", "Sad Smile",
				new String[] { ":(", ":=(", ":-(" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0102-bigsmile.gif", "Big Smile",
				new String[] { ":D", ":=D", ":-D", ":d", ":=d", ":-d" }, true));
		emoticons
				.add(new EmoticonInfo(GWT.getModuleBaseURL()
						+ "../icons/emoticons/emoticon-0103-cool.gif", "Cool",
						new String[] { "8)", "8=)", "8-)", "B)", "B=)", "B-)",
								"(cool)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0105-wink.gif", "Wink",
				new String[] { ":o", ":=o", ":-o", ":O", ":=O", ":-O" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0106-crying.gif", "Crying",
				new String[] { ";(", ";-(", ";=(" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0107-sweating.gif", "Sweating",
				new String[] { "(sweat)", "(:|" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0109-kiss.gif", "Kiss",
				new String[] { ":*", ":=*", ":-*" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0110-tongueout.gif",
				"Tongue Out", new String[] { ":P", ":=P", ":-P", ":p", ":=p",
						":-p" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0111-blush.gif", "Blush",
				new String[] { "(blush)", ":$", ":-$", ":=$", ":\">" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0112-wondering.gif",
				"Wondering", new String[] { ":^)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0113-sleepy.gif", "Sleepy",
				new String[] { "|-)", "I-)", "I=)", "(snooze)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0115-inlove.gif", "In Love",
				new String[] { "(inlove)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0116-evilgrin.gif", "Evil Grin",
				new String[] { "]:)", ">:)", "(grin)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0117-talking.gif", "Talking",
				new String[] { "(talk)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0118-yawn.gif", "Yawn",
				new String[] { "(yawn)", "|-()" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0119-puke.gif", "Puke",
				new String[] { "(puke)", ":&", ":-&", ":=&" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0120-doh.gif", "Doh",
				new String[] { "(doh)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0121-angry.gif", "Angry",
				new String[] { ":@", ":-@", ":=@", "x(", "x-(", "x=(", "X(",
						"X-(", "X=(" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0122-itwasntme.gif",
				"It Wasn't Me", new String[] { "(wasntme)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0124-worried.gif", "Worried",
				new String[] { ":S", ":-S", ":=S", ":s", ":-s", ":=s" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0125-mmm.gif", "Mmm...",
				new String[] { "(mm" }, true));
		emoticons
				.add(new EmoticonInfo(GWT.getModuleBaseURL()
						+ "../icons/emoticons/emoticon-0126-nerd.gif", "Nerd",
						new String[] { "8-|", "B-|", "8|", "B|", "8=|", "B=|",
								"(nerd)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0127-lipssealed.gif",
				"Lips Sealed", new String[] { ":x", ":-x", ":X", ":-X", ":#",
						":-#", ":=x", ":=X", ":=#" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0128-hi.gif", "Hi",
				new String[] { "(hi)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0129-call.gif", "Call",
				new String[] { "(call)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0130-devil.gif", "Devil",
				new String[] { "(devil)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0131-angel.gif", "Angel",
				new String[] { "(angel)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0132-envy.gif", "Envy",
				new String[] { "(envy)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0133-wait.gif", "Wait",
				new String[] { "(wait)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0135-makeup.gif", "Make-up",
				new String[] { "(makeup)", "(kate)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0137-clapping.gif", "Clapping",
				new String[] { "(clap)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0138-thinking.gif", "Thinking",
				new String[] { "(think)", ":?", ":-?", ":=?" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0140-rofl.gif", "ROFL",
				new String[] { "(rofl)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0141-whew.gif", "Whew",
				new String[] { "(whew)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0143-smirk.gif", "Smirking",
				new String[] { "(smirk)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0144-nod.gif", "Nod",
				new String[] { "(nod)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0145-shake.gif", "Shaking",
				new String[] { "(shake)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0146-punch.gif", "Punch",
				new String[] { "(punch)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0148-yes.gif", "Yes",
				new String[] { "(y)", "(Y)", "(ok)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0149-no.gif", "No",
				new String[] { "(n)", "(N)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0152-heart.gif", "Heart",
				new String[] { "(h)", "(H)", "(l)", "(L)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0153-brokenheart.gif",
				"Broken Heart", new String[] { "(u)", "(U)" }, true));
		emoticons.add(new EmoticonInfo(GWT.getModuleBaseURL()
				+ "../icons/emoticons/emoticon-0169-dance.gif", "Dance",
				new String[] { "(dance)", "\\o/", "\\:D/", "\\:d/" }, true));
	}

	public static List<EmoticonInfo> getEmoticons() {
		initialize();
		return emoticons;
	}

	private static void initialize() {
		if (!initialized) {
			initEmoticons();
			initialized = true;
		}
	}
	
	public static String replaceEmoticonText(String text) {
		initialize();
		for (EmoticonInfo emoticon : emoticons) {
			for (String txtRep : emoticon.textRepresentations) {
				text = text.replace(txtRep, getEmoticonTag(emoticon.url));
			}
		}

		return text;
	}

	public static String getEmoticonTag(String url) {
		initialize();
		return "<img class='ace-emoticon' src=" + "'" + url + "'>";
	}
}
