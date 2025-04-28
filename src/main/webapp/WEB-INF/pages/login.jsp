<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>RescueNet - Login</title>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/login.css" />
</head>
<body>
	<div class="login-container">
		<div class="logo-container">
			<img
				src="${pageContext.request.contextPath}/resources/images/system/logo.png" />
		</div>

		<h1>Login</h1>

		<%
		if (request.getAttribute("error") != null) {
		%>
		<p class="error-message"><%=request.getAttribute("error")%></p>
		<%
		}
		%>
		<%
		if (request.getParameter("success") != null) {
		%>
		<p class="success-message"><%=request.getParameter("success")%></p>
		<%
		}
		%>

		<form action="${pageContext.request.contextPath}/login" method="POST">
			<div class="form-group">
				<label for="username">Username</label> <input type="text"
					id="username" name="username" placeholder="username@gmail.com"
					maxlength="50" required>
			</div>

			<div class="form-group">
				<label for="password">Password</label> <input type="password"
					id="password" name="password" placeholder="Password"
					maxlength="255" required>
			</div>

			<div class="forgot-password">
				<a href="#">Forgot Password?</a>
			</div>

			<button type="submit">Sign in</button>

			<div class="divider">
				<span>or continue with</span>
			</div>

			<div class="social-media">
				<a href="#"><img
					src="${pageContext.request.contextPath}/resources/images/system/facebook.svg"></a>
				<a href="#"><img
					src="${pageContext.request.contextPath}/resources/images/system/instagram.svg"></a>
				<a href="#"><img
					src="${pageContext.request.contextPath}/resources/images/system/twitter.svg"></a>
			</div>

			<div class="register-link">
				Don't have an account yet? <a
					href="${pageContext.request.contextPath}/register">Register for
					free</a>
			</div>
		</form>
	</div>
</body>
</html>