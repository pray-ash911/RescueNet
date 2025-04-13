package com.rescuenet.filter;

import java.io.IOException;

import com.rescuenet.util.CookiesUtil;
import com.rescuenet.util.SessionUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AuthenticationFilter ensures authenticated access to protected pages in the RescueNet application.
 * It checks for session and role cookie, redirecting users accordingly.
 */
@WebFilter(asyncSupported = true, urlPatterns = { "/*" })
public class AuthenticationFilter implements Filter {

    private static final String LOGIN = "/login";
    private static final String REGISTER = "/register";
    private static final String HOME = "/home";
    private static final String ADMIN_DASHBOARD = "/adminDashboard";
    private static final String PORTFOLIO = "/portfolio";
    private static final String ROOT = "/";

    /**
     * Initializes the filter.
     *
     * @param filterConfig the FilterConfig object
     * @throws ServletException if a servlet-specific error occurs
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    /**
     * Filters requests to ensure authenticated access.
     * Redirects unauthenticated users to login and logged-in users to appropriate pages.
     *
     * @param request  the ServletRequest object
     * @param response the ServletResponse object
     * @param chain    the FilterChain object
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet-specific error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();

        // Allow access to static resources
        if (uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(ROOT)) {
            chain.doFilter(request, response);
            return;
        }

        // Check if user is logged in
        boolean isLoggedIn = SessionUtil.getAttribute(req, "username") != null;
        String role = CookiesUtil.getCookie(req, "role") != null ? CookiesUtil.getCookie(req, "role").getValue() : null;

        if (!isLoggedIn) {
            if (uri.endsWith(LOGIN) || uri.endsWith(REGISTER)) {
                chain.doFilter(request, response);
            } else {
                res.sendRedirect(contextPath + LOGIN);
            }
        } else {
            if (uri.endsWith(LOGIN) || uri.endsWith(REGISTER)) {
                String redirectPage = "admin".equals(role) ? ADMIN_DASHBOARD : HOME;
                res.sendRedirect(contextPath + redirectPage);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * Destroys the filter.
     */
    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}