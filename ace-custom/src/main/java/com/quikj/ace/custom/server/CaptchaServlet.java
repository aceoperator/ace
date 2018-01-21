package com.quikj.ace.custom.server;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.keypoint.PngEncoder;
import com.quikj.server.framework.CaptchaService;

public class CaptchaServlet extends HttpServlet {

	private static final long serialVersionUID = 9120211725683240148L;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		new CaptchaService();
	}

	@Override
	public void destroy() {
		super.destroy();
		if (CaptchaService.getInstance() != null) {
			CaptchaService.getInstance().destroy();
		}
	}

	protected void doGet(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {

		byte[] captchaChallengeAsPng = null;
		try {
			// get the session id that will identify the generated captcha.
			// the same id must be used to validate the response, the session id
			// is a good candidate!
			String sessionId = httpServletRequest.getSession().getId();
			
			BufferedImage challenge = CaptchaService.getInstance().generateImage(sessionId);

			PngEncoder encoder = new PngEncoder(challenge);
			captchaChallengeAsPng = encoder.pngEncode();
			
		} catch (IllegalArgumentException e) {
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (Exception e) {
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
