package com.rescuenet.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Prayash Rawal
 */
/**
 * ContactUsController handles requests for the "Contact Us" page in the
 * RescueNet application. It forwards GET requests to the contactus.jsp page for
 * rendering.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/contact" })
public class ContactUsController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Handles GET requests for the "Contact Us" page. Forwards the request to the
	 * contactus.jsp page for rendering.
	 *
	 * @param request  the HttpServletRequest object
	 * @param response the HttpServletResponse object
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.getRequestDispatcher("WEB-INF/pages/contactus.jsp").forward(request, response);
	}
}