package com.rescuenet.controller;

import java.io.IOException;

import com.rescuenet.util.CookiesUtil;
import com.rescuenet.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * LogoutController handles logout requests for the RescueNet application.
 * It invalidates the session, deletes the role cookie, and redirects to the login page.
 */
@WebServlet(asyncSupported = true, urlPatterns = { "/logout" })
public class LogoutController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Handles POST requests for user logout.
     * Invalidates the session, deletes the role cookie, and redirects to the login page.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CookiesUtil.deleteCookie(resp, "role");
        SessionUtil.invalidateSession(req);
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    /**
     * Handles GET requests for user logout by delegating to doPost.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}