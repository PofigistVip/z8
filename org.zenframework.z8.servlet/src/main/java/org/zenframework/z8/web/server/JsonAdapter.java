package org.zenframework.z8.web.server;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zenframework.z8.server.base.file.FileInfo;
import org.zenframework.z8.server.engine.ISession;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.JsonWriter;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.types.encoding;
import org.zenframework.z8.web.servlet.Servlet;

// Access restriction: import sun.misc.BASE64Decoder; instead: org.apache.commons.codec.binary.Base64

public class JsonAdapter extends Adapter {

	private static final Object AdapterPath = "/request.json";

	public JsonAdapter(Servlet servlet) {
		super(servlet);
	}

	@Override
	public boolean canHandleRequest(HttpServletRequest request) {
		return request.getServletPath().equals(AdapterPath);
	}

	@Override
	protected void service(ISession session, Map<String, String> parameters, List<FileInfo> files, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//		String path = parameters.get(Json.path);

//		if(path == null)
			super.service(session, parameters, files, request, response);
//		else 
			// reports, files - все то, что не приаттачено. 
			// ConverterAdapter нужно научить downloadить и такие файлы (с конкретного сервера)
			// z8 classic научить не слать такие запросы
			// IApplicationServer.download(String) - извести
//			downloadFile(session.getServerInfo(), path, parameters, response);
	}
/*
	private void downloadFile(ServerInfo serverInfo, String filePath, Map<String, String> parameters, HttpServletResponse response) throws IOException {
		FileStorage storage = new FileStorage(new File(getServlet().getWebInfPath(), Folders.Files));
		storage.save(serverInfo.getApplicationServer().download(filePath), filePath);

		JsonWriter writer = new JsonWriter();
		writer.startResponse(null, true);
		writer.writeProperty(new string(Json.source), filePath);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString().getBytes(encoding.Default.toString()));
	}
*/
	private void writeError(HttpServletResponse response, String errorText, int status) throws IOException {
		JsonWriter writer = new JsonWriter();

		if(errorText == null || errorText.isEmpty()) {
			errorText = "Internal server error.";
			status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		}

		writer.startResponse(null, false, status);
		writer.writeInfo(errorText);
		writer.startArray(Json.data);
		writer.finishArray();
		writer.finishResponse();

		writeResponse(response, writer.toString().getBytes(encoding.Default.toString()));
	}

	@Override
	protected void processAccessDenied(HttpServletResponse response) throws IOException {
		super.processAccessDenied(response);
		writeError(response, Resources.get("Exception.accessDenied"), HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	protected void processError(HttpServletResponse response, Throwable ex) throws IOException {
		writeError(response, ex.getMessage(), 0);
	}
}
