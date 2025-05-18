<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
<!-- Admin Dashboard Page for RescueNet -->
<!-- Displays an overview of vehicle fleet status and navigation for admins -->
<!-- @author Prayash Rawal -->

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>RescueNet Admin Dashboard</title>

<!-- Stylesheets -->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/admin_dashboard.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/footer.css">

<!-- Font Awesome for Icons -->
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
	<!-- Main Container -->
	<div class="admin-container">
		<!-- Sidebar Section -->
		<aside class="sidebar">
			<div class="logo">
				<h2>RescueNet</h2>
			</div>
			<nav class="sidebar-nav">
				<a href="${pageContext.request.contextPath}/admin"
					class="nav-link active"> <i
					class="fas fa-tachometer-alt nav-icon"></i><span>Dashboard</span>
				</a> <a href="${pageContext.request.contextPath}/vehicles"
					class="nav-link"> <i class="fas fa-truck nav-icon"></i><span>Vehicles</span>
				</a> <a href="${pageContext.request.contextPath}/reservations"
					class="nav-link"> <i class="fas fa-calendar-alt nav-icon"></i><span>Reservations</span>
				</a> <a href="${pageContext.request.contextPath}/portfolio">Portfolio</a>
			</nav>
		</aside>

		<!-- Main Content Section -->
		<main class="main-content">
			<!-- Top Header -->
			<header class="top-header">
				<h1>Admin Dashboard</h1>
				<div class="user-info">
					<span><i class="fas fa-user-circle"></i> Welcome,
						${sessionScope.user.username}</span> <a
						href="${pageContext.request.contextPath}/logout"
						class="logout-btn"> <i class="fas fa-sign-out-alt"></i> Logout
					</a>
				</div>
			</header>

			<!-- Dashboard Content -->
			<section class="dashboard-content">
				<!-- Success Message -->
				<c:if test="${not empty sessionScope.successMessage}">
					<div class="message success-message">
						<i class="fas fa-check-circle"></i> ${sessionScope.successMessage}
					</div>
					<c:remove var="successMessage" scope="session" />
				</c:if>

				<!-- Parameter-Based Messages -->
				<c:if test="${not empty param.error}">
					<div class="message error-message">${param.error}</div>
				</c:if>
				<c:if test="${not empty param.success}">
					<div class="message success-message">${param.success}</div>
				</c:if>
				<c:if test="${not empty requestScope.error}">
					<div class="message error-message">${requestScope.error}</div>
				</c:if>

				<!-- Welcome Message -->
				<div class="welcome-message">
					<p>Welcome back, Admin! This dashboard provides a quick
						overview of your vehicle fleet status.</p>
				</div>

				<!-- Overview Section -->
				<h2>Overview</h2>

				<!-- Stats Cards -->
				<div class="stats-container">
					<div class="stat-card total">
						<div class="stat-icon">
							<i class="fas fa-truck-medical fa-3x"></i>
						</div>
						<div class="stat-info">
							<h3>Total Vehicles</h3>
							<p class="stat-value">${totalVehicles}</p>
						</div>
					</div>
					<div class="stat-card available">
						<div class="stat-icon">
							<i class="fas fa-check-circle fa-3x"></i>
						</div>
						<div class="stat-info">
							<h3>Available Vehicles</h3>
							<p class="stat-value">${availableVehicles}</p>
						</div>
					</div>
					<div class="stat-card service">
						<div class="stat-icon">
							<i class="fas fa-tools fa-3x"></i>
						</div>
						<div class="stat-info">
							<h3>Vehicles in Service/Maint.</h3>
							<p class="stat-value">${vehiclesInService}</p>
						</div>
					</div>
				</div>
			</section>
		</main>
	</div>
</body>
</html>