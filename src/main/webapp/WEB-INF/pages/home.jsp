<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Home</title>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/header.css" />
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/home.css" />
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/footer.css" />
</head>
<body>
	<jsp:include page="header.jsp" />

	<div class="main-body">This is our Home</div>

	<header>
		<h1>RescueNet - Home</h1>
	</header>
	<nav>
		<a href="${pageContext.request.contextPath}/logout">Logout</a>
	</nav>
	<!-- Main Content -->
	<main>
		<section class="welcome-section">
			<h2>Welcome, Admin!</h2>
			<p>You are logged in as an administrator. Use the options below
				to manage the RescueNet system.</p>
		</section>

		<section class="dashboard-options">
			<div class="option-card">
				<h3>Manage Users</h3>
				<p>View, edit, or delete user accounts.</p>
				<a href="#" class="btn">Go to User Management</a>
			</div>
			<div class="option-card">
				<h3>View Reports</h3>
				<p>Generate and view system reports.</p>
				<a href="#" class="btn">View Reports</a>
			</div>
			<div class="option-card">
				<h3>System Settings</h3>
				<p>Configure system-wide settings.</p>
				<a href="#" class="btn">Go to Settings</a>
			</div>
		</section>
	</main>

	<jsp:include page="footer.jsp" />
</body>
</html>