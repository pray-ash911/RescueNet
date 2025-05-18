package com.rescuenet.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Prayash Rawal
 */

/**
 * Utility class for managing server-side forwards and client-side redirects in
 * the RescueNet application. Supports forwarding to JSPs with messages and
 * redirecting to URLs with optional query parameters.
 */
public class RedirectionUtil {

	/**
	 * Performs a server-side forward to a JSP, setting a message as a request
	 * attribute. Use this for displaying errors or data on the same logical page
	 * after processing. The browser URL does not change.
	 *
	 * @param msgType       the key for the message attribute (e.g., "error",
	 *                      "success", "info")
	 * @param message       the message string to display
	 * @param targetJspPath the path to the JSP file (e.g.,
	 *                      "/WEB-INF/pages/register.jsp")
	 * @param req           the HttpServletRequest
	 * @param resp          the HttpServletResponse
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException      if an I/O error occurs
	 */
	public void forwardToPage(String msgType, String message, String targetJspPath, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		// --- Set Message Attribute ---
		if (msgType != null && message != null) {
			req.setAttribute(msgType, message);
		}

		// --- Forward to JSP ---
		System.out.println("RedirectionUtil: Forwarding to JSP: " + targetJspPath + " with messageType: " + msgType);
		req.getRequestDispatcher(targetJspPath).forward(req, resp);
	}

	/**
	 * Performs a client-side redirect to a new URL, optionally passing a message as
	 * a query parameter. Use this for the Post-Redirect-Get pattern or to navigate
	 * to a different page. The browser URL changes, and a new GET request is made.
	 *
	 * @param msgType    the query parameter key for the message (e.g., "success",
	 *                   "error"), can be null
	 * @param message    the message string, can be null
	 * @param targetPath the servlet path or page to redirect to (e.g., "/login",
	 *                   "/home")
	 * @param req        the HttpServletRequest to get context path
	 * @param resp       the HttpServletResponse
	 * @throws IOException if an I/O error occurs
	 */
	public void redirectToPage(String msgType, String message, String targetPath, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		// --- Build Redirect URL ---
		String contextPath = req.getContextPath();
		StringBuilder redirectUrlBuilder = new StringBuilder(contextPath);
		redirectUrlBuilder.append(targetPath);

		// --- Append Message Parameter ---
		if (msgType != null && message != null && !message.isEmpty()) {
			String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());
			redirectUrlBuilder.append("?").append(msgType).append("=").append(encodedMessage);
		}

		// --- Perform Redirect ---
		String redirectUrl = redirectUrlBuilder.toString();
		System.out.println("RedirectionUtil: Redirecting to URL: " + redirectUrl);
		resp.sendRedirect(redirectUrl);
	}
}