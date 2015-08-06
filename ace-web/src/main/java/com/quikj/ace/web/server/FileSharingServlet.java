/**
 * 
 */
package com.quikj.ace.web.server;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.server.framework.AceConfigFileHelper;
import com.quikj.server.framework.AceLogger;

/**
 * @author amit
 * 
 */

public class FileSharingServlet extends HttpServlet {

	private static final long DELETE_OLDER_THAN = 30 * 60 * 1000L;

	private static final String FILES_UPLOADS_PATH = "files/uploads";

	private static final long serialVersionUID = -620755038859898648L;

	private static final long MAX_UPLOAD_SIZE = 100000L;

	private static DiskFileItemFactory factory = new DiskFileItemFactory();

	private Timer purgeTimer;

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		try {
			ServletRequestContext ctx = new ServletRequestContext(request);

			if (!ServletFileUpload.isMultipartContent(ctx)) {
				sendResponse(response, ResponseMessage.INTERNAL_ERROR,
						"The servlet can only handle multipart requests."
								+ " This is probably a software bug.");
				return;
			}

			ServletFileUpload upload = new ServletFileUpload(factory);

			// Parse the request
			List<?> items = upload.parseRequest(request);

			String session = null;
			FileItem fileItem = null;
			for (Object i : items) {
				FileItem item = (FileItem) i;

				if (item.isFormField()) {
					if (item.getFieldName().equals("session")) {
						session = item.getString();
					}
				} else {
					if (item.getSize() == 0) {
						sendResponse(response, ResponseMessage.NO_CONTENT,
								"The file contains no data");
						return;
					}

					if (item.getSize() > MAX_UPLOAD_SIZE) {
						sendResponse(response, ResponseMessage.NOT_ACCEPTABLE,
								"The file is too big");
						return;
					}

					fileItem = item;
				}
				// else - ignore form fields for now
			}

			String path = AceConfigFileHelper.getAcePath(FILES_UPLOADS_PATH,
					session + "_" + fileItem.getName());
			File file = new File(path);
			fileItem.write(file);

			sendResponse(response, HttpServletResponse.SC_OK, file.getName());
		} catch (Exception e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"Exception "
									+ e.getClass().getName()
									+ " occured while processing file sharing request - "
									+ e.getMessage(), e);
			sendResponse(response, ResponseMessage.INTERNAL_ERROR, e.getClass()
					.getName() + ": " + e.getMessage());
		}

	}

	private void sendResponse(HttpServletResponse response, int status,
			String reason) {
		// GWT provides very poor support for handling servlet response in the
		// FormPanel and associated classes. Basically, it returns only the body
		// part and nothing else. So, the only way, we can indicate
		// an error is to embed the message in the body.

		try {
			response.setStatus(HttpServletResponse.SC_OK);

			// This needs to be a text/html and not text/plain because the GWT
			// uses an iFrame to send form requests
			response.setContentType("text/html");

			String msg = status + "," + (reason == null ? "" : reason);
			response.setContentLength(msg.length());
			response.getWriter().print(msg);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		purgeTimer.cancel();
		super.destroy();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		purgeTimer = new Timer();
		purgeTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				String path = AceConfigFileHelper
						.getAcePath(FILES_UPLOADS_PATH);
				File d = new File(path);
				File[] files = d.listFiles();
				if (files == null || files.length == 0) {
					return;
				}

				long curTime = new Date().getTime();
				for (File file : files) {
					if (file.lastModified() >= curTime - DELETE_OLDER_THAN) {
						if (!file.delete()) {
							AceLogger.Instance().log(
									AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"Error deleteing shared file "
											+ file.getName());
						}
					}
				}
			}
		}, 0L, 15 * 60 * 1000L);
	}
}
