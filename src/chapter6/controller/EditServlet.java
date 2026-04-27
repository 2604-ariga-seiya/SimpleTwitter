package chapter6.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import chapter6.beans.Message;
import chapter6.beans.User;
import chapter6.logging.InitApplication;
import chapter6.service.MessageService;

@WebServlet(urlPatterns = { "/edit" })
public class EditServlet extends HttpServlet {

	/** メッセージID判定用正規表現：1文字以上の数字 */
	private static final String MESSAGE_ID_REGEX = "^[0-9]+$";

	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public EditServlet() {
		InitApplication application = InitApplication.getInstance();
		application.init();

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		HttpSession session = request.getSession();

		Message message = null;

		List<String> errorMessages = new ArrayList<String>();

		String messageIdStr = request.getParameter("message_id");

		if (!StringUtils.isBlank(messageIdStr) && messageIdStr.matches(MESSAGE_ID_REGEX)) {
			int messageId = Integer.parseInt(request.getParameter("message_id"));
			message = new MessageService().select(messageId);
		}

		if (message == null) {
			errorMessages.add("不正なパラメータが入力されました");
			session.setAttribute("errorMessages", errorMessages);

			response.sendRedirect("./");
			return;
		}

		request.setAttribute("message", message);
		request.getRequestDispatcher("/edit.jsp").forward(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		int messageId = Integer.parseInt(request.getParameter("message_id"));
		String text = request.getParameter("text");
		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		List<String> errorMessages = new ArrayList<String>();
		Message message = new Message();

		if (!isValid(text, errorMessages)) {
			request.setAttribute("errorMessages", errorMessages);

			message.setId(messageId);
			message.setText(text);
			message.setUserId(loginUser.getId());

			request.setAttribute("message", message);
			request.getRequestDispatcher("edit.jsp").forward(request, response);
			return;

		}

		message.setId(messageId);
		message.setText(text);
		new MessageService().update(message);

		response.sendRedirect("./");
	}

	private boolean isValid(String text, List<String> errorMessages) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		if (StringUtils.isBlank(text)) {
			errorMessages.add("メッセージを入力してください");
		} else if (140 < text.length()) {
			errorMessages.add("140文字以下で入力してください");
		}

		if (errorMessages.size() != 0) {
			return false;
		}
		return true;
	}

}
