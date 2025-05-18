<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<!-- Contact Us Page for RescueNet -->
<!-- Provides emergency contact details and a form for user inquiries -->
<!-- @author Prayash Rawal -->

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>RescueNet - Contact Us</title>

<!-- External Styles and Icons -->
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/header.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/footer.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/contactus.css">
</head>
<body>
	<!-- Header Section -->
	<jsp:include page="header.jsp" />

	<!-- Main Content -->
	<main class="main-content">
		<!-- Hero Banner -->
		<section class="hero-banner">
			<div class="hero-content">
				<h1>Emergency Contact</h1>
				<p>Need immediate assistance? Call our 24/7 emergency line</p>
				<a href="tel:+18007372831" class="emergency-btn"> <i
					class="fas fa-phone-alt"></i> +1-800-RESCUE1
				</a>
			</div>
		</section>

		<!-- Contact Section -->
		<section class="contact-container">
			<!-- Contact Information -->
			<div class="contact-details">
				<h2>
					<i class="fas fa-info-circle"></i> Contact Information
				</h2>
				<div class="contact-item">
					<i class="fas fa-envelope"></i> <span>support@rescue-net.org</span>
				</div>
				<div class="contact-item">
					<i class="fas fa-phone"></i> <span>+1-800-737-2831
						(Non-emergency)</span>
				</div>
				<div class="contact-item">
					<i class="fas fa-map-marker-alt"></i> <span>123 Emergency
						Lane, Rescue City, RC 45678</span>
				</div>
				<div class="social-links">
					<a href="#"><i class="fab fa-facebook-f"></i></a> <a href="#"><i
						class="fab fa-twittezr"></i></a> <a href="#"><i
						class="fab fa-instagram"></i></a> <a href="#"><i
						class="fab fa-linkedin-in"></i></a>
				</div>
			</div>

			<!-- Contact Form -->
			<div class="contact-form">
				<h2>
					<i class="fas fa-paper-plane"></i> Send a Message
				</h2>
				<form action="${pageContext.request.contextPath}/contact"
					method="POST">
					<div class="form-field">
						<label for="name">Full Name:</label> <input type="text" id="name"
							name="name" maxlength="100" required>
					</div>
					<div class="form-field">
						<label for="email">Email:</label> <input type="email" id="email"
							name="email" maxlength="100" required>
					</div>
					<div class="form-field">
						<label for="subject">Subject:</label> <select id="subject"
							name="subject" required>
							<option value="">Select a subject</option>
							<option value="Emergency">Emergency Assistance</option>
							<option value="Volunteer">Volunteer Inquiry</option>
							<option value="Donation">Donation Inquiry</option>
							<option value="General">General Question</option>
						</select>
					</div>
					<div class="form-field">
						<label for="message">Message:</label>
						<textarea id="message" name="message" required></textarea>
					</div>
					<button type="submit" class="submit-btn">
						<i class="fas fa-paper-plane"></i> Send Message
					</button>
				</form>
			</div>
		</section>
	</main>

	<!-- Footer Section -->
	<jsp:include page="footer.jsp" />

	<!-- JavaScript for Form Validation -->
	<script
		src="${pageContext.request.contextPath}/js/contact-form-validation.js"></script>
</body>
</html>