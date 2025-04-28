<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>User Portfolio - RescueNet</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/header.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/footer.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/portfolio.css">
<script>
        // Toggle between view mode and edit mode
        function toggleEditMode() {
            document.getElementById("view-mode").style.display = "none";
            document.getElementById("edit-mode").style.display = "block";
        }

        // Cancel editing and return to view mode
        function cancelEdit() {
            document.getElementById("view-mode").style.display = "block";
            document.getElementById("edit-mode").style.display = "none";
        }

        // Add event listeners when the DOM is fully loaded
        document.addEventListener('DOMContentLoaded', function() {
            // Focus on the first input when edit mode is shown
            const editButton = document.querySelector('.btn-primary');
            if (editButton) {
                editButton.addEventListener('click', function() {
                    setTimeout(function() {
                        document.getElementById('full_name').focus();
                    }, 100);
                });
            }
        });
    </script>
</head>
<body>
	<jsp:include page="header.jsp" />

	<div class="main-container">
		<h1 class="page-title">User Portfolio</h1>

		<!-- Profile Section -->
		<div class="profile-section">

			<c:if test="${not empty error}">
				<div class="message error-message">${error}</div>
			</c:if>
			<c:if test="${not empty success}">
				<div class="message success-message">${success}</div>
			</c:if>

			<!-- View Mode -->
			<div id="view-mode" class="profile-card">
				<c:if test="${not empty user}">
					<div class="profile-details">
						<div class="profile-item">
							<span class="detail-label">UserName:</span> <span
								class="detail-value">${user.username}</span>
						</div>
						<div class="profile-item">
							<span class="detail-label">Full Name:</span> <span
								class="detail-value">${user.fullName}</span>
						</div>
						<div class="profile-item">
							<span class="detail-label">Email:</span> <span
								class="detail-value">${user.email}</span>
						</div>
						<div class="profile-item">
							<span class="detail-label">Phone Number:</span> <span
								class="detail-value">${user.phoneNumber != null ? user.phoneNumber : 'Not provided'}</span>
						</div>
					</div>
					<button class="btn btn-primary" onclick="toggleEditMode()">Edit
						Profile</button>
				</c:if>
			</div>

			<!-- Edit Mode -->
			<div id="edit-mode" class="profile-card" style="display: none;">
				<form action="${pageContext.request.contextPath}/portfolio"
					method="post">
					<div class="form-group">
						<label for="full_name">Full Name:</label> <input type="text"
							id="full_name" name="full_name" value="${user.fullName}" required>
					</div>
					<div class="form-group">
						<label for="email">Email:</label> <input type="email" id="email"
							name="email" value="${user.email}" required>
					</div>
					<div class="form-group">
						<label for="phone_number">Phone Number:</label> <input type="text"
							id="phone_number" name="phone_number" value="${user.phoneNumber}"
							placeholder="+1234567890">
					</div>
					<div class="form-group">
						<label for="password">New Password (optional):</label> <input
							type="password" id="password" name="password"
							placeholder="Leave blank to keep current password">
					</div>
					<div class="form-actions">
						<button type="submit" class="btn btn-success">Save</button>
						<button type="button" class="btn btn-secondary"
							onclick="cancelEdit()">Cancel</button>
					</div>
				</form>
			</div>
		</div>

		
			
		</div>
	</div>

	<jsp:include page="footer.jsp" />
</body>
</html>