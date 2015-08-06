package com.quikj.ace.web.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tika.Tika;

import com.quikj.server.framework.AceConfigFileHelper;
import com.quikj.server.framework.AceLogger;

public class FileDownloadServlet extends HttpServlet {

	private static final long serialVersionUID = -2718588914775408472L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {

		try {
			String fileProperty = request.getParameter("file");
			if (fileProperty == null) {
				throw new ServletException(
						"The 'file' parameter is not specified");
			}

			String file = AceConfigFileHelper.getAcePath("files/uploads",
					fileProperty);
			File f = new File(file);
			if (!f.exists()) {
				sendResponse(response, HttpServletResponse.SC_NOT_FOUND,
						"The file has been removed from the server");
				return;
			}

			outputFile(response, f);
		} catch (Exception e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"Exception "
									+ e.getClass().getName()
									+ " occured while processing file downloading request - "
									+ e.getMessage(), e);
			throw new ServletException(e);
		}
	}

	private void sendResponse(HttpServletResponse response, int status,
			String reason) throws IOException {
		response.setStatus(status);

		response.setContentType("text/html");

		String msg = status + "," + (reason == null ? "" : reason);
		response.setContentLength(msg.length());
		response.getWriter().print(msg);
		response.getWriter().flush();
		response.getWriter().close();
	}

	private void outputFile(HttpServletResponse response, File f)
			throws IOException {
		ServletOutputStream ros = response.getOutputStream();

		// Open the file for detecting content type
		FileInputStream fis = new FileInputStream(f);
		String fileType = new Tika().detect(fis, f.getName());
		fis.close();

		response.setContentType(fileType);
		response.setHeader("Content-Disposition",
				"inline; filename=" + f.getName());

		// Send the content
		fis = new FileInputStream(f);
		byte[] bytes = new byte[1000];
		int count = 0;
		do {
			count = fis.read(bytes);
			if (count > 0) {
				ros.write(bytes, 0, count);
			}
		} while (count > 0);
		fis.close();

		ros.flush();
		ros.close();
	}
}
