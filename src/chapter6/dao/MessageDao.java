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

	public Message select(Connection connection, int loginUser, int messageId) {
		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;

		try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, user_id, text From messages Where user_id = ? AND id = ?");
            ps = connection.prepareStatement(sql.toString());
            ps.setInt(1, loginUser);
            ps.setInt(2, messageId);

            ResultSet rs = ps.executeQuery();
            Message message = null;

            if (rs.next()) {
                message = new Message();
                // ResultSetから取り出してMessageオブジェクトにセットする
                message.setId(rs.getInt("id"));
                message.setUserId(rs.getInt("user_id"));
                message.setText(rs.getString("text"));
                // 必要に応じて他の項目も
            }
    		return message;

		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}


	public void delete(Connection connection,int loginUser, int messageId) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("DELETE FROM messages Where id = ? And user_id = ?");

			ps = connection.prepareStatement(sql.toString());

			ps.setInt(1, messageId);
			ps.setInt(2, loginUser);
			ps.executeUpdate();

		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

    public void update(Connection connection, int messageId, int userId, String text) {

	    log.info(new Object(){}.getClass().getEnclosingClass().getName() +
	    " : " + new Object(){}.getClass().getEnclosingMethod().getName());

	    PreparedStatement ps = null;
	    List<Object> params = new ArrayList<>();

	    try {
	        StringBuilder sql = new StringBuilder();
	        sql.append("UPDATE messages SET ");
	        sql.append("    text = ?, ");
	        params.add(text);
	        sql.append("    updated_date = CURRENT_TIMESTAMP ");
	        sql.append("WHERE id = ? AND user_id = ?");
	        params.add(messageId);
	        params.add(userId);

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