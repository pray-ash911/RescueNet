package com.rescuenet.filter;

import java.io.IOException;
import java.util.Arrays; // Import Arrays
import java.util.HashSet; // Import HashSet
import java.util.Set; // Import Set

import com.rescuenet.model.UserModel; // Import UserModel if checking user object
import com.rescuenet.util.CookiesUtil;
import com.rescuenet.util.SessionUtil; // Use SessionUtil for session checks

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Import HttpSession if needed directly


@WebFilter(asyncSupported = true, urlPatterns = { "/*" }) // Filter all requests
public class AuthenticationFilter implements Filter {

    // --- Constants for Paths (Use relative paths from context root) ---
    private static final String LOGIN_PATH = "/login";
    private static final String REGISTER_PATH = "/register";
    private static final String HOME_PATH = "/home";
    private static final String ADMIN_ROOT_PATH = "/admin"; // Check paths starting with this
    private static final String LOGOUT_PATH = "/logout";
    // Add any other paths that should be public (accessible without login)
    // private static final String PUBLIC_PAGE_PATH = "/public-info";

    // --- Constants for Static Resources ---
    // Define file extensions for static resources
    private static final Set<String> STATIC_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico", // Common web assets
        ".woff", ".woff2", ".ttf", ".eot" // Fonts
    ));
    // Define path prefixes for directories containing static resources
    private static final Set<String> STATIC_PREFIXES = new HashSet<>(Arrays.asList(
        "/resources/", "/uploads/", "/css/", "/js/" // Add prefixes as needed
    ));
    // --- End Constants ---

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthenticationFilter Initialized.");
        // Filter initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        // Get path relative to the application context root
        String pathWithinApp = requestURI.substring(contextPath.length());

        // --- Step 1: Bypass filter entirely for static resources ---
        boolean isStaticResource = false;
        String lowerCasePath = pathWithinApp.toLowerCase();

        // Check by known static directory prefixes first
        for (String prefix : STATIC_PREFIXES) {
            if (pathWithinApp.startsWith(prefix)) {
                isStaticResource = true;
                break;
            }
        }
        // If not matched by prefix, check by file extension
        if (!isStaticResource) {
            for (String ext : STATIC_EXTENSIONS) {
                if (lowerCasePath.endsWith(ext)) {
                    isStaticResource = true;
                    break;
                }
            }
        }

        if (isStaticResource) {
            // System.out.println("AuthenticationFilter: Allowing static resource - " + pathWithinApp);
            chain.doFilter(request, response); // Let the server handle it directly
            return; // Stop filter processing for this request
        }
        // --- End Static Resource Bypass ---


        // --- Step 2: Process Dynamic Requests (Non-Static) ---
        System.out.println("AuthenticationFilter: Processing dynamic request for path: " + pathWithinApp);

        // Check login status using SessionUtil (preferred)
        UserModel loggedInUser = SessionUtil.getUser(req);
        boolean isLoggedIn = (loggedInUser != null);

        // Get role primarily from the logged-in user object if available, fallback to cookie
        String role = null;
        if (isLoggedIn) {
            // Assuming roleId 1 is user, 2 is admin as per previous context
            role = (loggedInUser.getRoleId() == 2) ? "admin" : "user";
        } else {
            // Fallback to cookie if not logged in via session (less reliable)
            role = CookiesUtil.getCookie(req, "role") != null ? CookiesUtil.getCookie(req, "role").getValue() : null;
        }

        System.out.println("AuthenticationFilter: isLoggedIn=" + isLoggedIn + ", Role=" + role + ", Path=" + pathWithinApp);


        // --- Step 3: Apply Authentication and Authorization Rules ---
        if (!isLoggedIn) {
            // NOT Logged In: Allow access only to specific public pages
            if (LOGIN_PATH.equals(pathWithinApp) || REGISTER_PATH.equals(pathWithinApp) /* || PUBLIC_PAGE_PATH.equals(pathWithinApp) */) {
                 System.out.println("AuthenticationFilter: Allowing public access for logged-out user.");
                chain.doFilter(request, response);
            } else {
                // Not a public page and not logged in, redirect to login
                System.out.println("AuthenticationFilter: Access denied (Not Logged In). Redirecting to " + LOGIN_PATH);
                res.sendRedirect(contextPath + LOGIN_PATH);
            }
        } else {
            // Logged In: Apply rules based on role and requested path
            // Rule 1: Prevent logged-in users from accessing login/register
            if (LOGIN_PATH.equals(pathWithinApp) || REGISTER_PATH.equals(pathWithinApp)) {
                String redirectTarget = HOME_PATH; // Default redirect for users
                if ("admin".equals(role)) {
                    redirectTarget = ADMIN_ROOT_PATH; // Admins go to admin dashboard
                }
                System.out.println("AuthenticationFilter: Logged-in user (" + role + ") accessing " + pathWithinApp + ". Redirecting to " + redirectTarget);
                res.sendRedirect(contextPath + redirectTarget);
                return; // Stop processing
            }

            // Rule 2: Role-based authorization for admin area
            if (pathWithinApp.startsWith(ADMIN_ROOT_PATH) && !"admin".equals(role)) {
                // Non-admin trying to access an admin path
                System.out.println("AuthenticationFilter: Access Denied (User Role '" + role + "' cannot access Admin Path '" + pathWithinApp + "'). Redirecting to " + HOME_PATH);
                res.sendRedirect(contextPath + HOME_PATH); // Redirect non-admins away from admin area
                return; // Stop processing
            }

            // Rule 3: (Optional) Redirect admin away from user home if accessing directly
            // if (HOME_PATH.equals(pathWithinApp) && "admin".equals(role)) {
            //     System.out.println("AuthenticationFilter: Admin accessing user home. Redirecting to " + ADMIN_ROOT_PATH);
            //     res.sendRedirect(contextPath + ADMIN_ROOT_PATH);
            //     return; // Stop processing
            // }

            // If no rules blocked the request, allow it to proceed
            System.out.println("AuthenticationFilter: Allowing access for logged-in user ("+ role +").");
            chain.doFilter(request, response);
        }
        // --- End Authentication and Authorization Rules ---
    }

    @Override
    public void destroy() {
        System.out.println("AuthenticationFilter Destroyed.");
        // Filter cleanup logic if needed
    }
}