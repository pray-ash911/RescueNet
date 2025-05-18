package com.rescuenet.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.rescuenet.model.UserModel;
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
import jakarta.servlet.http.HttpSession;

/**
 * @author Prayash Rawal
 */
/**
 * AuthenticationFilter secures the RescueNet application by controlling access
 * to resources. It bypasses static resources, enforces login for protected
 * pages, restricts admin areas to admin users, and redirects users
 * appropriately based on authentication and authorization rules.
 */
@WebFilter(asyncSupported = true, urlPatterns = { "/*" })
public class AuthenticationFilter implements Filter {

	// --- Constants for Paths (Use relative paths from context root) ---
	private static final String LOGIN_PATH = "/login";
	private static final String REGISTER_PATH = "/register";
	private static final String HOME_PATH = "/home";
	private static final String ADMIN_ROOT_PATH = "/admin";
	private static final String LOGOUT_PATH = "/logout";
	// private static final String PUBLIC_PAGE_PATH = "/public-info";

	// --- Constants for Static Resources ---
	private static final Set<String> STATIC_EXTENSIONS = new HashSet<>(Arrays.asList(".css", ".js", ".png", ".jpg",
			".jpeg", ".gif", ".svg", ".ico", ".woff", ".woff2", ".ttf", ".eot"));
	private static final Set<String> STATIC_PREFIXES = new HashSet<>(
			Arrays.asList("/resources/", "/Uploads/", "/css/", "/js/"));
	// --- End Constants ---

	/**
	 * Initializes the AuthenticationFilter.
	 *
	 * @param filterConfig the FilterConfig object
	 * @throws ServletException if an error occurs during initialization
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		System.out.println("AuthenticationFilter Initialized.");
	}

	/**
	 * Filters requests to enforce authentication and authorization. Allows static
	 * resources, public pages for unauthenticated users, and restricts access based
	 * on user roles.
	 *
	 * @param request  the ServletRequest object
	 * @param response the ServletResponse object
	 * @param chain    the FilterChain for invoking the next filter or resource
	 * @throws IOException      if an I/O error occurs
	 * @throws ServletException if a servlet-specific error occurs
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		String contextPath = req.getContextPath();
		String requestURI = req.getRequestURI();
		String pathWithinApp = requestURI.substring(contextPath.length());

		// --- Step 1: Bypass filter entirely for static resources ---
		boolean isStaticResource = false;
		String lowerCasePath = pathWithinApp.toLowerCase();

		for (String prefix : STATIC_PREFIXES) {
			if (pathWithinApp.startsWith(prefix)) {
				isStaticResource = true;
				break;
			}
		}
		if (!isStaticResource) {
			for (String ext : STATIC_EXTENSIONS) {
				if (lowerCasePath.endsWith(ext)) {
					isStaticResource = true;
					break;
				}
			}
		}

		if (isStaticResource) {
			chain.doFilter(request, response);
			return;
		}
		// --- End Static Resource Bypass ---

		// --- Step 2: Process Dynamic Requests (Non-Static) ---
		System.out.println("AuthenticationFilter: Processing dynamic request for path: " + pathWithinApp);

		UserModel loggedInUser = SessionUtil.getUser(req);
		boolean isLoggedIn = (loggedInUser != null);

		String role = null;
		if (isLoggedIn) {
			role = (loggedInUser.getRoleId() == 2) ? "admin" : "user";
		} else {
			role = CookiesUtil.getCookie(req, "role") != null ? CookiesUtil.getCookie(req, "role").getValue() : null;
		}

		System.out.println(
				"AuthenticationFilter: isLoggedIn=" + isLoggedIn + ", Role=" + role + ", Path=" + pathWithinApp);

		// --- Step 3: Apply Authentication and Authorization Rules ---
		if (!isLoggedIn) {
			if (LOGIN_PATH.equals(pathWithinApp) || REGISTER_PATH.equals(pathWithinApp)) {
				System.out.println("AuthenticationFilter: Allowing public access for logged-out user.");
				chain.doFilter(request, response);
			} else {
				System.out.println("AuthenticationFilter: Access denied (Not Logged In). Redirecting to " + LOGIN_PATH);
				res.sendRedirect(contextPath + LOGIN_PATH);
			}
		} else {
			if (LOGIN_PATH.equals(pathWithinApp) || REGISTER_PATH.equals(pathWithinApp)) {
				String redirectTarget = HOME_PATH;
				if ("admin".equals(role)) {
					redirectTarget = ADMIN_ROOT_PATH;
				}
				System.out.println("AuthenticationFilter: Logged-in user (" + role + ") accessing " + pathWithinApp
						+ ". Redirecting to " + redirectTarget);
				res.sendRedirect(contextPath + redirectTarget);
				return;
			}

			if (pathWithinApp.startsWith(ADMIN_ROOT_PATH) && !"admin".equals(role)) {
				System.out.println("AuthenticationFilter: Access Denied (User Role '" + role
						+ "' cannot access Admin Path '" + pathWithinApp + "'). Redirecting to " + HOME_PATH);
				res.sendRedirect(contextPath + HOME_PATH);
				return;
			}

			System.out.println("AuthenticationFilter: Allowing access for logged-in user (" + role + ").");
			chain.doFilter(request, response);
		}
		// --- End Authentication and Authorization Rules ---
	}

	/**
	 * Cleans up resources when the filter is destroyed.
	 */
	@Override
	public void destroy() {
		System.out.println("AuthenticationFilter Destroyed.");
	}
}