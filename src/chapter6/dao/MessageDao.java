package chapter6.dao;

import static chapter6.utils.CloseableUtil.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import chapter6.beans.Message;
import chapter6.exception.NoRowsUpdatedRuntimeException;
import chapter6.exception.SQLRuntimeException;
import chapter6.logging.InitApplication;

public class MessageDao {

	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public MessageDao() {
		InitApplication application = InitApplication.getInstance();
		application.init();

	}

	public void insert(Connection connection, Message message) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO messages ( ");
			sql.append("    user_id, ");
			sql.append("    text, ");
			sql.append("    created_date, ");
			sql.append("    updated_date ");
			sql.append(") VALUES ( ");
			sql.append("    ?, "); // user_id
			sql.append("    ?, "); // text
			sql.append("    CURRENT_TIMESTAMP, "); // created_date
			sql.append("    CURRENT_TIMESTAMP "); // updated_date
			sql.append(")");

			ps = connection.prepareStatement(sql.toString());

			ps.setInt(1, message.getUserId());
			ps.setString(2, message.getText());

			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

	public Message select(Connection connection, int messageId) {
		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;

		try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, user_id, text From messages Where id = ?");
            ps = connection.prepareStatement(sql.toString());
            ps.setInt(1, messageId);

            ResultSet rs = ps.executeQuery();
            List<Message> messageList = toUserMessages(rs);

            if(messageList.isEmpty()) {
            	return null;
            }
    		return messageList.get(0);

		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

    private List<Message> toUserMessages(ResultSet rs) throws SQLException {


	  log.info(new Object(){}.getClass().getEnclosingClass().getName() +
        " : " + new Object(){}.getClass().getEnclosingMethod().getName());

        List<Message> messages = new ArrayList<Message>();
        try {
            while (rs.next()) {
                Message message = new Message();
                message.setId(rs.getInt("id"));
                message.setText(rs.getString("text"));
                message.setUserId(rs.getInt("user_id"));

                messages.add(message);
            }
            return messages;
        } finally {
            close(rs);
        }
    }

	public void delete(Connection connection, int messageId) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("DELETE FROM messages Where id = ?");

			ps = connection.prepareStatement(sql.toString());

			ps.setInt(1, messageId);
			ps.executeUpdate();

		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

    public void update(Connection connection, Message message) {

	    log.info(new Object(){}.getClass().getEnclosingClass().getName() +
	    " : " + new Object(){}.getClass().getEnclosingMethod().getName());

	    PreparedStatement ps = null;
	    List<Object> params = new ArrayList<>();

	    try {
	        StringBuilder sql = new StringBuilder();
	        sql.append("UPDATE messages SET ");
	        sql.append("    text = ?, ");
	        params.add(message.getText());
	        sql.append("    updated_date = CURRENT_TIMESTAMP ");
	        sql.append("WHERE id = ?");
	        params.add(message.getId());

	        ps = connection.prepareStatement(sql.toString());

	        for(int i = 0; i < params.size(); i++) {
	        	// i=0 のときは params.get(0) を psの1番目にセット
	            // i=1 のときは params.get(1) を psの2番目にセット
	            ps.setObject(i + 1, params.get(i));
	        }

	        int count = ps.executeUpdate();
	        if (count == 0) {
	    		log.log(Level.SEVERE,"更新対象のレコードが存在しません", new NoRowsUpdatedRuntimeException());
	            throw new NoRowsUpdatedRuntimeException();
	        }
	    } catch (SQLException e) {
		  log.log(Level.SEVERE, new Object(){}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
	        throw new SQLRuntimeException(e);
	    } finally {
	        close(ps);
	    }
    }
}