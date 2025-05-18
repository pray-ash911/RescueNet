package com.rescuenet.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

/**
 * @author Prayash Rawal
 */

/**
 * Utility class for managing cookies in the RescueNet application. Provides
 * methods to add, retrieve, and delete cookies.
 */
public class CookiesUtil {

	/**
	 * Adds a cookie with the specified name, value, and maximum age.
	 *
	 * @param response the HttpServletResponse to add the cookie to
	 * @param name     the name of the cookie
	 * @param value    the value of the cookie
	 * @param maxAge   the maximum age of the cookie in seconds
	 */
	public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		// --- Create and Configure Cookie ---
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge(maxAge);
		cookie.setPath("/");

		// --- Add Cookie to Response ---
		response.addCookie(cookie);
	}

	/**
	 * Retrieves a cookie by its name from the HttpServletRequest.
	 *
	 * @param request the HttpServletRequest to get the cookie from
	 * @param name    the name of the cookie to retrieve
	 * @return the Cookie object if found, or null if not found
	 */
	public static Cookie getCookie(HttpServletRequest request, String name) {
		// --- Check for Cookies in Request ---
		if (request.getCookies() != null) {
			// --- Find Cookie by Name ---
			return Arrays.stream(request.getCookies()).filter(cookie -> name.equals(cookie.getName())).findFirst()
					.orElse(null);
		}
		return null;
	}

	/**
	 * Deletes a cookie by setting its maximum age to 0.
	 *
	 * @param response the HttpServletResponse to add the deletion cookie to
	 * @param name     the name of the cookie to delete
	 */
	public static void deleteCookie(HttpServletResponse response, String name) {
		// --- Create Deletion Cookie ---
		Cookie cookie = new Cookie(name, null);
		cookie.setMaxAge(0);
		cookie.setPath("/");

		// --- Add Deletion Cookie to Response ---
		response.addCookie(cookie);
	}
}