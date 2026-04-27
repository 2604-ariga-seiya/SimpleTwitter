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

import chapter6.beans.User;
import chapter6.beans.UserMessage;
import chapter6.logging.InitApplication;
import chapter6.service.CommentService;
import chapter6.service.MessageService;

@WebServlet(urlPatterns = { "/comment" })
public class CommentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

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
    public CommentServlet() {
        InitApplication application = InitApplication.getInstance();
        application.init();

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

	  log.info(new Object(){}.getClass().getEnclosingClass().getName() +
        " : " + new Object(){}.getClass().getEnclosingMethod().getName());

        boolean isShowMessageForm = false;
        User user = (User) request.getSession().getAttribute("loginUser");
        if (user != null) {
            isShowMessageForm = true;
        }

        /*
         * String型のuser_idの値をrequest.getParameter("user_id")で
         * JSPから受け取るように設定
         * MessageServiceのselectに引数としてString型のuser_idを追加
         */
        String userId = request.getParameter("user_id");
        List<UserMessage> messages = new MessageService().select(userId);

        request.setAttribute("messages", messages);
        request.setAttribute("isShowMessageForm", isShowMessageForm);
        request.getRequestDispatcher("/top.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

    	log.info(new Object(){}.getClass().getEnclosingClass().getName() +
  	        " : " + new Object(){}.getClass().getEnclosingMethod().getName());

		HttpSession session = request.getSession();
		User loginUser = (User) session.getAttribute("loginUser");
		int userId = loginUser.getId();
		String text = request.getParameter("text");

		 List<String> errorMessages = new ArrayList<String>();

		String messageIdStr = request.getParameter("message_id");

		if (StringUtils.isBlank(messageIdStr) || !messageIdStr.matches(MESSAGE_ID_REGEX)) {
			errorMessages.add("不正なパラメータが入力されました");
			request.setAttribute("errorMessages", errorMessages);
			request.setAttribute("loginUser", loginUser);

			response.sendRedirect("./");
			return;
		}

		int messageId = Integer.parseInt(request.getParameter("message_id"));

		if (!isValid(text, errorMessages)) {
			session.setAttribute("errorMessages", errorMessages);
			response.sendRedirect("./");
			return;
		}

		new CommentService().insert(userId, messageId, text);
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