<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>RescueNet - My Portfolio</title>
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/portofolio.css" />
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/header.css" />
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/footer.css" />

</head>
<body>
	<header>
		<h1>RescueNet - My Portfolio</h1>
		<p>Welcome to your personal profile page.</p>
	</header>
	<jsp:include page="header.jsp" />
	<section class="portfolio-container">
		<!-- User Profile Section -->
		<div class="profile-details">
			<h2>Your Profile</h2>
			<p>
				<span class="label">Username:</span> john_doe
			</p>
			<p>
				<span class="label">Full Name:</span> John Doe
			</p>
			<p>
				<span class="label">Email:</span> john.doe@example.com
			</p>
			<p>
				<span class="label">Phone Number:</span> +1234567890
			</p>
			<p>
				<span class="label">Role:</span> User
			</p>
			<p>
				<span class="label">Last Login:</span> 2025-04-05 10:00:00
			</p>
			<p>
				<span class="label">Status:</span> Active
			</p>
			<a href="edit_profile.jsp" class="edit-link">Edit Profile</a>
		</div>

		<!-- User Actions Section -->
		<div class="user-actions">
			<h2>Your Actions</h2>
			<ul>
				<li><a href="search_vehicles.jsp">Search Vehicles</a> - Find
					available emergency vehicles.</li>
				<li><a href="contact.jsp">Contact Support</a> - Reach out for
					assistance.</li>
				<li><a href="logout.jsp">Logout</a> - End your session.</li>
			</ul>
		</div>
		<jsp:include page="footer.jsp" />

	</section>
</body>
</html>