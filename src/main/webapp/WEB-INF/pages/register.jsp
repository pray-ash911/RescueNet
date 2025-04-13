<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>RescueNet - Registration Form</title>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/registration.css" />
</head>
<body>
	<div class="form-container">
		<div class="logo-container">
			<img
				src="${pageContext.request.contextPath}/resources/images/system/logo.png" />
		</div>

		<h1>Registration</h1>
		<%
		if (request.getAttribute("error") != null) {
		%>
		<p style="color: red;"><%=request.getAttribute("error")%></p>
		<%
		}
		%>
		<form action="${pageContext.request.contextPath}/register"
			method="POST">
			<div class="form-group">
				<label for="full_name">Full Name:</label> <input type="text"
					id="full_name" name="full_name" maxlength="100" required>
			</div>
			<div class="form-group">
				<label for="username">Username:</label> <input type="text"
					id="username" name="username" maxlength="50" required>
			</div>
			<div class="form-group">
				<label for="password">Password:</label> <input type="password"
					id="password" name="password" maxlength="255" required>
			</div>
			<div class="form-group">
				<label for="retype_password">Retype Password:</label> <input
					type="password" id="retype_password" name="retype_password"
					maxlength="255" required>
			</div>
			<div class="form-group">
				<label for="email">Email:</label> <input type="email" id="email"
					name="email" maxlength="100">
			</div>
			<div class="form-group">
				<label for="phone_number">Phone Number:</label> <input type="tel"
					id="phone_number" name="phone_number" maxlength="20"
					placeholder="+1234567890">
			</div>
			<div class="form-group">
				<label for="role_id">Role:</label> <select id="role_id"
					name="role_id" required>
					<option value="" disabled selected>Select Role</option>
					<option value="1">User</option>
					<option value="2">Admin</option>
				</select>
			</div>
			<div class="form-group">
				<label for="is_active">Active Status:</label> <input type="checkbox"
					id="is_active" name="is_active" value="1" checked> <small>(Uncheck
					to deactivate account)</small>
			</div>
			<button type="submit">Register</button>
		</form>
	</div>
</body>
</html>