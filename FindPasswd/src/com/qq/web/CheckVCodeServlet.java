package com.qq.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * �����û�������֤���Servlet
 * @author ZiQin
 * @version V1.0.0
 */
@WebServlet("/CheckVCodeServlet")
public class CheckVCodeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckVCodeServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			checkVcode(request, response);
		}
		catch (ServletException servletException) {
			System.out.println("[ERROR] Servlet �쳣");
			servletException.printStackTrace();
		}
		catch (IOException ioException) {
			System.out.println("[ERROR] IO �쳣");
			ioException.printStackTrace();
		}
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * ����û��������֤�������
	 * @param request ����
	 * @param response ��Ӧ
	 * @throws ServletException servlet�쳣
	 * @throws IOException IO�쳣
	 */
	private void checkVcode(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String vcode = (String) this.getServletContext().getAttribute("VCode");
		String vcodeByUser;
		do {
			vcodeByUser = request.getParameter("vcodetext");
			System.out.println(vcodeByUser);
			if (!vcode.equals(vcodeByUser)) {
				request.getRequestDispatcher("recvVcode.jsp").forward(request,response);
			}
		} while (!vcode.equals(vcodeByUser));
		System.out.println("Ok");
		request.getRequestDispatcher("changeKeys.jsp").forward(request,response);
		
	}
	
}
