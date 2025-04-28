package com.rescuenet.util;

import com.rescuenet.model.UserModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Utility class for managing HTTP sessions in a web application.
 * Provides methods to set, get, remove session attributes and invalidate sessions.
 */
public class SessionUtil {
    private static final String USER_KEY = "user";

    /**
     * Sets an attribute in the session.
     *
     * @param request the HttpServletRequest from which the session is obtained
     * @param key     the key under which the attribute is stored
     * @param value   the value of the attribute to store in the session
     */
    public static void setAttribute(HttpServletRequest request, String key, Object value) {
        HttpSession session = request.getSession();
        session.setAttribute(key, value);
    }

    /**
     * Retrieves an attribute from the session.
     *
     * @param request the HttpServletRequest from which the session is obtained
     * @param key     the key of the attribute to retrieve
     * @return the attribute value, or null if the attribute does not exist or the session is invalid
     */
    public static Object getAttribute(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return session.getAttribute(key);
        }
        return null;
    }

    /**
     * Removes an attribute from the session.
     *
     * @param request the HttpServletRequest from which the session is obtained
     * @param key     the key of the attribute to remove
     */
    public static void removeAttribute(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(key);
        }
    }

    /**
     * Invalidates the current session.
     *
     * @param request the HttpServletRequest from which the session is obtained
     */
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Stores the user in the session.
     *
     * @param request the HttpServletRequest from which the session is obtained
     * @param user    the UserModel to store in the session
     */
    public static void setUser(HttpServletRequest request, UserModel user) {
        setAttribute(request, USER_KEY, user);
    }

    /**
     * Retrieves the user from the session.
     *
     * @param request the HttpServletRequest from which the session is obtained
     * @return the UserModel stored in the session, or null if not found or session is invalid
     */
    public static UserModel getUser(HttpServletRequest request) {
        Object user = getAttribute(request, USER_KEY);
        if (user instanceof UserModel) {
            return (UserModel) user;
        }
        return null;
    }
}