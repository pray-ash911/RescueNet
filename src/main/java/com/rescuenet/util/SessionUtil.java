package com.rescuenet.util;

import com.rescuenet.model.UserModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @author Prayash Rawal
 */

/**
 * Utility class for managing HTTP sessions in the RescueNet application.
 * Provides methods to set, get, remove session attributes, and invalidate
 * sessions.
 */
public class SessionUtil {

	private static final String USER_KEY = "user";

	/**
	 * Sets an attribute in the session.
	 *
	 * @param request the HttpServletRequest to obtain the session
	 * @param key     the key for the session attribute
	 * @param value   the value to store in the session
	 */
	public static void setAttribute(HttpServletRequest request, String key, Object value) {
		// --- Set Session Attribute ---
		HttpSession session = request.getSession();
		session.setAttribute(key, value);
	}

	/**
	 * Retrieves an attribute from the session.
	 *
	 * @param request the HttpServletRequest to obtain the session
	 * @param key     the key of the attribute to retrieve
	 * @return the attribute value, or null if not found or session is invalid
	 */
	public static Object getAttribute(HttpServletRequest request, String key) {
		// --- Get Session Attribute ---
		HttpSession session = request.getSession(false);
		if (session != null) {
			return session.getAttribute(key);
		}
		return null;
	}

	/**
	 * Removes an attribute from the session.
	 *
	 * @param request the HttpServletRequest to obtain the session
	 * @param key     the key of the attribute to remove
	 */
	public static void removeAttribute(HttpServletRequest request, String key) {
		// --- Remove Session Attribute ---
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(key);
		}
	}

	/**
     * Invalidates the current session.
     *
     * @param request the HttpServletRequest to obtain the session
     */

	public static void invalidateSession(HttpServletRequest request) {
		// --- Invalidate Session ---
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	/**
	 * Stores a user in the session.
	 *
	 * @param request the HttpServletRequest to obtain the session
	 * @param user    the UserModel to store in the session
	 */
	public static void setUser(HttpServletRequest request, UserModel user) {
		// --- Store User in Session ---
		setAttribute(request, USER_KEY, user);
	}

	/**
	 * Retrieves the user from the session.
	 *
	 * @param request the HttpServletRequest to obtain the session
	 * @return the UserModel stored in the session, or null if not found or session
	 *         is invalid
	 */
	public static UserModel getUser(HttpServletRequest request) {
		// --- Retrieve User from Session ---
		Object user = getAttribute(request, USER_KEY);
		if (user instanceof UserModel) {
			return (UserModel) user;
		}
		return null;
	}
}