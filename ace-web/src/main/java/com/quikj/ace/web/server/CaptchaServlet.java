package com.quikj.ace.web.server;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.keypoint.PngEncoder;
import com.octo.captcha.service.CaptchaServiceException;
import com.quikj.server.framework.CaptchaService;

public class CaptchaServlet extends HttpServlet {

	private static final long serialVersionUID = 9120211725683240148L;

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

	}

	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {

		byte[] captchaChallengeAsPng = null;
		try {
			// get the session id that will identify the generated captcha.
			// the same id must be used to validate the response, the session id
			// is a good candidate!
			String captchaId = httpServletRequest.getSession().getId();
			
			CaptchaService.CaptchaType captchaType = CaptchaService.CaptchaType.LARGE;
			String type = httpServletRequest.getParameter("type");
			if (type != null) {
				captchaType = CaptchaService.CaptchaType.fromValue(type);
			}
			
			// call the ImageCaptchaService getChallenge method
			BufferedImage challenge = CaptchaService.getInstance(captchaType)
					.getImageChallengeForID(captchaId,
							httpServletRequest.getLocale());

			PngEncoder encoder = new PngEncoder(challenge);
			captchaChallengeAsPng = encoder.pngEncode();
			
		} catch (IllegalArgumentException e) {
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (CaptchaServiceException e) {
			httpServletResponse
					.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		// flush it in the response
		httpServletResponse.setHeader("Cache-Control", "no-cache");
		httpServletResponse.setHeader("Pragma", "no-cache");
		httpServletResponse.setDateHeader("Expires", 0);
		httpServletResponse.setContentType("image/png");
		ServletOutputStream responseOutputStream = httpServletResponse
				.getOutputStream();
		responseOutputStream.write(captchaChallengeAsPng);
		responseOutputStream.flush();
		responseOutputStream.close();
	}
}
