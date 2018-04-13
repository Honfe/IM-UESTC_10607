package com.qq.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qq.database.DatabaseOperator;

/**
 * �����޸������Servlet����
 * @author ZiQin
 * @version V 1.0.0
 */
@WebServlet("/ChangeKey")
public class ChangeKey extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * ״̬����
	 */
	private static final byte SUCCESS = 0;
	private static final byte FAIL = 1;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ChangeKey() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			switch (change(request, response)) {
			case SUCCESS:
				response.sendRedirect("Success.html");
				break;
			case FAIL:
			default:
				response.sendRedirect("Fail.html");
			}
		}
		catch (IOException ioException) {
			System.out.println("[ERROR] IO exception");
			ioException.printStackTrace();
		}
		catch (SQLException sqlException) {
			System.out.println("[ERROR] SQL exception");
			sqlException.printStackTrace();
		}
		catch (ServletException servletException) {
			System.out.println("[ERROR] servlet exception");
			servletException.printStackTrace();
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	/**
	 * �޸����ݿ��������߼�����
	 * @param request ����
	 * @param response ��Ӧ
	 * @return �������״̬��
	 * @throws ServletException Servlet�쳣
	 * @throws IOException IO�쳣
	 * @throws SQLException SQL�쳣
	 */
	private byte change(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		String id = (String) this.getServletContext().getAttribute("id");
		DatabaseOperator databaseOperator = (DatabaseOperator) this.getServletContext().getAttribute("db");
		String key = request.getParameter("key");
		String stat = new String("UPDATE user SET password='" + key + "' WHERE id='" + id + "';");
		if (databaseOperator.update(stat) > 0) {
			databaseOperator.disconnectDatabase();
			return SUCCESS;
		}
		else {
			databaseOperator.disconnectDatabase();
			return FAIL;
		}
	}

}
