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
import com.quikj.server.framework.CaptchaService;

public class CaptchaServlet extends HttpServlet {

	private static final long serialVersionUID = 9120211725683240148L;


	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		byte[] captchaChallengeAsPng = null;
		try {
			String sessionId = request.getSession().getId();

			BufferedImage challenge = CaptchaService.getInstance().generateImage(sessionId);

			PngEncoder encoder = new PngEncoder(challenge);
			captchaChallengeAsPng = encoder.pngEncode();

		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		// flush it in the response
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/png");
		ServletOutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(captchaChallengeAsPng);
		responseOutputStream.flush();
		responseOutputStream.close();
	}
}
