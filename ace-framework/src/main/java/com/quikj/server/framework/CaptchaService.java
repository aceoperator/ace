/**
 * 
 */
package com.quikj.server.framework;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import com.octo.captcha.CaptchaFactory;
import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.SimpleTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;
import com.octo.captcha.engine.CaptchaEngine;
import com.octo.captcha.engine.GenericCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;
import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * @author achatte3
 * 
 */

// TODO Add license information for jcaptcha
public class CaptchaService {

	public enum CaptchaType {
		SMALL("small"), LARGE("large");

		String value;

		CaptchaType(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}

		public static CaptchaType fromValue(String value) {
			for (CaptchaType v : values()) {
				if (v.value().equals(value)) {
					return v;
				}
			}

			return null;
		}
	}

	private static Map<CaptchaType, ImageCaptchaService> instances = new HashMap<CaptchaService.CaptchaType, ImageCaptchaService>();

	private CaptchaService(CaptchaType captchaType) {
		ImageCaptchaService service = init(captchaType);
		instances.put(captchaType, service);
	}

	private ImageCaptchaService init(CaptchaType captchType) {
		Color textColor = new Color(255, 255, 255);
		TextPaster textPaster = new SimpleTextPaster(5, 8, textColor);

		Color bgColor = new Color(0, 0, 0);

		BackgroundGenerator background;
		FontGenerator fontGenerator;
		
		Font font = Font.decode("Monospaced");
		Font[] fonts = {font};
		
		if (captchType == CaptchaType.LARGE) {
			fontGenerator = new RandomFontGenerator(40, 50, fonts);
			background = new UniColorBackgroundGenerator(300,
					100, bgColor);
		} else {
			fontGenerator = new RandomFontGenerator(20, 26, fonts);
			background = new UniColorBackgroundGenerator(150,
					50, bgColor);
		}

		WordToImage wordToImage = new ComposedWordToImage(fontGenerator, background, textPaster);
		WordGenerator wordgen = new RandomWordGenerator("abcdefghijklmnopqrstuvwxyz0123456789");

		CaptchaFactory[] factories = new CaptchaFactory[] { new GimpyFactory(wordgen, wordToImage) };
		CaptchaEngine imageEngine = new GenericCaptchaEngine(factories);

		DefaultManageableImageCaptchaService def = new DefaultManageableImageCaptchaService();
		def.setCaptchaEngine(imageEngine);

		return def;
	}

	public static ImageCaptchaService getInstance(CaptchaType captchaType) {
		ImageCaptchaService instance = instances.get(captchaType);
		if (instance == null) {
			new CaptchaService(captchaType);
			instance = instances.get(captchaType);
		}

		return instance;
	}
}
