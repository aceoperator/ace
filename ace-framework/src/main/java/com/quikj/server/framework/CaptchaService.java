/**
 * 
 */
package com.quikj.server.framework;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import com.github.cage.GCage;
import com.google.gson.Gson;

/**
 * @author amit
 * 
 */
public class CaptchaService {
	public enum CaptchaType {
		IMAGE, RECAPTCHA
	}

	private class CaptchaInfo {
		protected String token;
		protected int countDown = 5;

		public CaptchaInfo(String token) {
			this.token = token;
		}

		public int decrement() {
			return --countDown;
		}
	}

	private static CaptchaService instance;

	Map<String, CaptchaInfo> buffer = new HashMap<String, CaptchaInfo>();

	private Timer timer;

	private static final String URL = "https://www.google.com/recaptcha/api/siteverify";

	private String secretKey;

	public CaptchaService() {
		init();
		instance = this;
	}

	public int getBufferSize() {
		return buffer.size();
	}

	private void init() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				synchronized (buffer) {
					Iterator<Entry<String, CaptchaInfo>> i = buffer.entrySet().iterator();
					while (i.hasNext()) {
						Entry<String, CaptchaInfo> element = i.next();
						int countDown = element.getValue().decrement();
						if (countDown == 0) {
							i.remove();
						}
					}
				}

				if (buffer.size() > 200) {
					System.err.println("Warning! There are " + buffer.size() + " images in the captcha list");
				}
			}
		};

		timer = new Timer("CaptchaTimer");
		timer.scheduleAtFixedRate(task, 60 * 1000L, 60 * 1000L);
	}

	public void destroy() {
		if (timer != null) {
			timer.cancel();
		}

		synchronized (buffer) {
			buffer.clear();
		}

		instance = null;
	}

	public static CaptchaService getInstance() {
		return instance;
	}

	public BufferedImage generateImage(String sessionId) {
		GCage cage = new GCage();
		String token = cage.getTokenGenerator().next();
		synchronized (buffer) {
			buffer.put(sessionId, new CaptchaInfo(token));
		}
		return cage.drawImage(token);
	}

	public boolean verify(String sessionId, String input, String remoteIp, CaptchaType type) {
		if (type == CaptchaType.IMAGE) {
			return verifyImageCaptcha(sessionId, input);
		} else {
			return verifyRecaptcha(input, remoteIp);
		}
	}

	private boolean verifyImageCaptcha(String sessionId, String input) {
		if (input == null) {
			return false;
		}

		CaptchaInfo info = null;
		synchronized (buffer) {
			info = buffer.get(sessionId);
			if (info == null) {
				return false;
			}
			buffer.remove(sessionId);
		}
		return info.token.equalsIgnoreCase(input);
	}

	// Copied from salesfront/gwt-recaptcha github code
	private boolean verifyRecaptcha(String input, String remoteIp) {
		try {
			if (secretKey == null || secretKey.trim().isEmpty()) {
				System.out.println("Warning! attempt to validate recaptcha with no secret key set");
				return false;
			}
			
			if (input == null) {
				return false;
			}
			
			String parameters = "secret=" + secretKey + "&response=" + input;
			if (remoteIp != null)
				parameters += "&remoteip=" + remoteIp;

			byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
			int postDataLength = postData.length;

			URL obj = new URL(URL);
			HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}

			// get response
			String serverResponse = "";

			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				serverResponse += inputLine;
			in.close();

			Gson gson = new Gson();
			HttpResponse httpResponse = gson.fromJson(serverResponse, HttpResponse.class);

			if (httpResponse.getErrorCodes() != null && !httpResponse.getErrorCodes().isEmpty()) {
				StringBuilder builder = new StringBuilder();
				for (String error : httpResponse.getErrorCodes()) {
					builder.append(error);
					builder.append(",");
				}

				System.err.println("Error response from Recaptcha - " + builder.toString());
				return false;
			}

			return httpResponse.getSuccess();
		} catch (MalformedURLException e) {
			throw new IllegalStateException(e);
		} catch (IOException e) {
			e.printStackTrace(System.err);
			return false;
		}
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
