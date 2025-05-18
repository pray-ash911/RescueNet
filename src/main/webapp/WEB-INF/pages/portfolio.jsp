<%-- 
  @author Prayash Rawal
  Purpose: Displays the user portfolio page for the RescueNet application, allowing users to
           view and edit their profile details, including username, full name, email, phone
           number, profile picture, and password, with client-side validation and error/success
           message handling.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
</head>
<body>
	<jsp:include page="header.jsp" />

	<main class="main-container portfolio-page-container">
		<header class="page-header">
			<h1 class="page-title">User Portfolio</h1>
		</header>

		<c:if test="${not empty sessionScope.globalError}">
			<div class="message error-message">${sessionScope.globalError}</div>
			<c:remove var="globalError" scope="session" />
		</c:if>
		<c:if
			test="${not empty requestScope.errorMessagesList || not empty param.error}">
			<div class="message error-message">
				<c:if test="${not empty requestScope.errorMessagesList}">
					<ul>
						<c:forEach var="errMsg" items="${requestScope.errorMessagesList}">
							<li>${errMsg}</li>
						</c:forEach>
					</ul>
				</c:if>
				<c:if test="${not empty param.error}">${param.error}</c:if>
			</div>
		</c:if>
		<c:if
			test="${not empty requestScope.success || not empty param.success}">
			<div class="message success-message">${not empty requestScope.success ? requestScope.success : param.success}</div>
		</c:if>

		<section class="profile-section card-style">
			<c:choose>
				<c:when test="${empty user}">
					<p class="no-data-message">
						Unable to load profile. Please <a
							href="${pageContext.request.contextPath}/login">log in</a> again.
					</p>
				</c:when>
				<c:otherwise>
					<div id="view-mode" class="profile-view active">
						<header class="card-header">
							<h2 class="section-title">User Profile</h2>
							<button type="button"
								class="button button-outline button-edit-profile"
								onclick="toggleEditMode(true)">
								<i class="fas fa-edit"></i> Edit Profile
							</button>
						</header>
						<div class="profile-picture-container">
							<c:choose>
								<c:when test="${not empty user.profilePicturePath}">
									<img
										src="${pageContext.request.contextPath}/${user.profilePicturePath}"
										alt="Profile Picture">
								</c:when>
								<c:otherwise>
									<div class="profile-picture-placeholder">
										<i class="fas fa-user-circle"></i>
									</div>
								</c:otherwise>
							</c:choose>
						</div>
						<div class="profile-details">
							<div class="profile-item">
								<span class="detail-label"><i class="fas fa-user fa-fw"></i>
									Username</span><span class="detail-value">${user.username}</span>
							</div>
							<div class="profile-item">
								<span class="detail-label"><i
									class="fas fa-id-card fa-fw"></i> Full Name</span><span
									class="detail-value">${user.fullName}</span>
							</div>
							<div class="profile-item">
								<span class="detail-label"><i
									class="fas fa-envelope fa-fw"></i> Email</span><span
									class="detail-value">${user.email}</span>
							</div>
							<div class="profile-item">
								<span class="detail-label"><i class="fas fa-phone fa-fw"></i>
									Phone</span><span class="detail-value">${not empty user.phoneNumber ? user.phoneNumber : 'N/A'}</span>
							</div>
						</div>
					</div>

					<div id="edit-mode" class="profile-edit">
						<header class="card-header">
							<h2 class="section-title">Edit Profile</h2>
							<button type="button" class="button button-secondary"
								onclick="cancelEdit()">
								<i class="fas fa-times"></i> Cancel
							</button>
						</header>
						<p class="edit-mode-info">Update your details. Leave password
							fields blank to keep your current password.</p>
						<form id="portfolioForm"
							action="${pageContext.request.contextPath}/portfolio"
							method="post" enctype="multipart/form-data"
							onsubmit="return validatePortfolioForm();">
							<div class="form-group">
								<label for="username_edit">Username:</label> <input type="text"
									id="username_edit" name="username" value="${user.username}"
									required> <span class="validation-error"
									id="usernameEditError"></span>
							</div>
							<div class="form-group">
								<label for="full_name">Full Name:</label> <input type="text"
									id="full_name" name="full_name" value="${user.fullName}"
									required> <span class="validation-error"
									id="fullNameError"></span>
							</div>
							<div class="form-group">
								<label for="email">Email:</label> <input type="email" id="email"
									name="email" value="${user.email}" required> <span
									class="validation-error" id="emailError"></span>
							</div>
							<div class="form-group">
								<label for="phone_number">Phone Number:</label> <input
									type="tel" id="phone_number" name="phone_number"
									value="${user.phoneNumber}" placeholder="e.g., +1xxxxxxxxxx">
								<span class="validation-error" id="phoneNumberError"></span>
							</div>

							<hr class="form-divider">
							<h3 class="form-section-title">Change Profile Picture</h3>
							<div class="form-group">
								<label for="profile_picture_new">New Profile Picture
									(Optional):</label> <input type="file" id="profile_picture_new"
									name="profile_picture_new" accept="image/*"> <small
									class="field-hint">Max 2MB. Replaces current picture if
									chosen.</small> <span class="validation-error"
									id="profilePictureNewError"></span>
								<div class="image-preview-container-edit">
									<p>Current:</p>
									<c:choose>
										<c:when test="${not empty user.profilePicturePath}">
											<img
												src="${pageContext.request.contextPath}/${user.profilePicturePath}"
												alt="Current Profile Picture"
												style="max-width: 100px; max-height: 100px; border-radius: 50%;">
										</c:when>
										<c:otherwise>
											<div class="profile-picture-placeholder"
												style="width: 100px; height: 100px; font-size: 2em;">
												<i class="fas fa-user-circle"></i>
											</div>
										</c:otherwise>
									</c:choose>
									<p>New Preview:</p>
									<img id="newProfilePicturePreview" src="#"
										alt="New Image Preview" />
								</div>
							</div>

							<hr class="form-divider">
							<h3 class="form-section-title">Change Password</h3>
							<div class="form-group">
								<label for="new_password">New Password:</label> <input
									type="password" id="new_password" name="new_password"
									placeholder="Leave blank to keep current"> <small
									class="field-hint">Min 8 chars, 1 upper, 1 lower, 1
									digit, 1 symbol.</small> <span class="validation-error"
									id="newPasswordError"></span>
							</div>
							<div class="form-group">
								<label for="retype_new_password">Retype New Password:</label> <input
									type="password" id="retype_new_password"
									name="retype_new_password" placeholder="Confirm new password">
								<span class="validation-error" id="retypeNewPasswordError"></span>
							</div>
							<div class="form-actions">
								<button type="submit" class="button button-success">
									<i class="fas fa-save"></i> Save Changes
								</button>
							</div>
						</form>
					</div>
				</c:otherwise>
			</c:choose>
		</section>
	</main>

	<jsp:include page="footer.jsp" />

	<script>
        const viewModeDiv = document.getElementById("view-mode");
        const editModeDiv = document.getElementById("edit-mode");
        const portfolioForm = document.getElementById('portfolioForm'); // Get form by ID
        const newProfilePictureInput = document.getElementById('profile_picture_new');
        const newProfilePicturePreview = document.getElementById('newProfilePicturePreview');

        function toggleEditMode(isOpeningEdit) {
            if (viewModeDiv && editModeDiv) {
                if (isOpeningEdit) {
                    viewModeDiv.classList.remove('active');
                    editModeDiv.classList.add('active');
                    const firstInput = editModeDiv.querySelector('#username_edit');
                    if(firstInput) setTimeout(() => firstInput.focus(), 50);
                } else { // This part is not strictly needed if cancelEdit handles it
                    editModeDiv.classList.remove('active');
                    viewModeDiv.classList.add('active');
                }
            }
        }

        function cancelEdit() {
             if (viewModeDiv && editModeDiv) {
                editModeDiv.classList.remove('active');
                viewModeDiv.classList.add('active');
                if (portfolioForm) portfolioForm.reset(); // Reset form fields
                if (newProfilePicturePreview) { // Clear image preview
                    newProfilePicturePreview.src = '#';
                    newProfilePicturePreview.style.display = 'none';
                }
                document.querySelectorAll('.portfolio-form .validation-error').forEach(el => el.textContent = '');
                document.querySelectorAll('.portfolio-form input.invalid').forEach(el => el.classList.remove('invalid'));
            }
        }

         // Initial state setup
         if (editModeDiv) editModeDiv.classList.remove('active');
         if (viewModeDiv) viewModeDiv.classList.add('active');

        // Image Preview for New Profile Picture
        if (newProfilePictureInput && newProfilePicturePreview) {
            newProfilePictureInput.onchange = evt => {
                const [file] = newProfilePictureInput.files;
                if (file && file.type.startsWith('image/')) {
                    newProfilePicturePreview.src = URL.createObjectURL(file);
                    newProfilePicturePreview.style.display = 'block';
                } else {
                    newProfilePicturePreview.src = '#';
                    newProfilePicturePreview.style.display = 'none';
                    if(file) document.getElementById('profilePictureNewError').textContent = 'Invalid file type for preview.';
                    else document.getElementById('profilePictureNewError').textContent = '';
                }
            }
        }

        // Client-Side Validation for Portfolio Form
        function validatePortfolioForm() {
            let isValid = true;
            document.querySelectorAll('.portfolio-form .validation-error').forEach(el => el.textContent = '');
            document.querySelectorAll('.portfolio-form input.invalid').forEach(el => el.classList.remove('invalid'));

            function setError(elementId, message) {
                const errorSpan = document.getElementById(elementId + 'Error');
                const inputElement = document.getElementById(elementId);
                if (errorSpan) errorSpan.textContent = message;
                if (inputElement) inputElement.classList.add('invalid');
                isValid = false;
            }

            // Username Validation
            const usernameInput = document.getElementById('username_edit');
            if (!usernameInput.value.trim()) setError('usernameEdit', 'Username is required.');
            else if (!/^[a-zA-Z0-9_]{3,50}$/.test(usernameInput.value.trim())) setError('usernameEdit', 'Invalid username (3-50 chars, letters, numbers, _).');

            // Full Name
            const fullName = document.getElementById('full_name');
            if (!fullName.value.trim()) setError('fullName', 'Full Name is required.');

            // Email
            const email = document.getElementById('email');
            if (!email.value.trim()) setError('email', 'Email is required.');
            else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())) setError('email', 'Invalid email format.');

            // Phone Number
            const phoneNumber = document.getElementById('phone_number');
            if (phoneNumber.value.trim() && !/^\+?[0-9]{7,15}$/.test(phoneNumber.value.trim())) {
                 setError('phoneNumber', 'Invalid phone number format.');
            }

            // New Password Validation (only if something is entered)
            const newPasswordInput = document.getElementById('new_password');
            const retypeNewPasswordInput = document.getElementById('retype_new_password');
            const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_/-])[A-Za-z\d@$!%*?&_/-]{8,}$/;

            if (newPasswordInput.value) { // If user is trying to change password
                if (!passwordRegex.test(newPasswordInput.value)) {
                    setError('newPassword', 'Password: 8+ chars, 1 upper, 1 lower, 1 digit, 1 symbol.');
                }
                if (newPasswordInput.value !== retypeNewPasswordInput.value) {
                    setError('retypeNewPassword', 'New passwords do not match.');
                }
            } else if (retypeNewPasswordInput.value) { // If only retype is filled
                 setError('newPassword', 'Please enter the new password first.');
                 setError('retypeNewPassword', 'New passwords do not match.');
            }

            // New Profile Picture Validation (if file selected)
            if (newProfilePictureInput.files.length > 0) {
                const file = newProfilePictureInput.files[0];
                const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
                const maxFileSize = 2 * 1024 * 1024; // 2MB

                if (!allowedTypes.includes(file.type)) {
                    setError('profilePictureNew', 'Invalid file type. Use PNG, JPG, GIF, or WEBP.');
                }
                if (file.size > maxFileSize) {
                    setError('profilePictureNew', 'File is too large (max 2MB).');
                }
            }

            if (!isValid) {
                const firstErrorField = document.querySelector('.portfolio-form input.invalid');
                if (firstErrorField) {
                    firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstErrorField.focus();
                }
            }
            return isValid;
        }
    </script>
</body>
</html>