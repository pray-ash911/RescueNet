<%-- 
  @author Prayash Rawal
  Purpose: Displays the registration page for the RescueNet application, allowing users to
           create a new account by entering personal details, selecting a role, uploading a
           profile picture, and setting a password, with client-side validation and error/success
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
<title>Register - RescueNet</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/header.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/footer.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/registration.css">
</head>
<body>
	<main class="auth-container">
		<div class="auth-card">
			<div class="logo-container">
				<img
					src="${pageContext.request.contextPath}/resources/images/system/logo.png"
					alt="RescueNet Logo" />
			</div>
			<h2>REGISTRATION</h2>

			<c:if test="${not empty requestScope.errorMessagesList}">
				<div class="message error-message">
					<ul>
						<c:forEach var="errMsg" items="${requestScope.errorMessagesList}">
							<li>${errMsg}</li>
						</c:forEach>
					</ul>
				</div>
			</c:if>
			<c:if test="${not empty param.error}">
				<div class="message error-message">${param.error}</div>
			</c:if>
			<c:if test="${not empty param.success}">
				<div class="message success-message">${param.success}</div>
			</c:if>

			<form id="registerForm"
				action="${pageContext.request.contextPath}/register" method="post"
				enctype="multipart/form-data"
				onsubmit="return validateRegisterForm();">
				<div class="form-group">
					<label for="full_name"><i class="fas fa-user-tag"></i> Full
						Name:</label> <input type="text" id="full_name" name="full_name"
						value="${submittedFullName}"> <span
						class="validation-error" id="fullNameError"></span>
				</div>
				<div class="form-group">
					<label for="username"><i class="fas fa-at"></i> Username:</label> <input
						type="text" id="username" name="username"
						value="${submittedUsername}"> <span
						class="validation-error" id="usernameError"></span>
				</div>
				<div class="form-group">
					<label for="email"><i class="fas fa-envelope"></i> Email
						Address:</label> <input type="email" id="email" name="email"
						value="${submittedEmail}"> <span class="validation-error"
						id="emailError"></span>
				</div>
				<div class="form-group">
					<label for="phone_number"><i class="fas fa-phone"></i>
						Phone Number (Optional):</label> <input type="tel" id="phone_number"
						name="phone_number" value="${submittedPhoneNumber}"
						placeholder="e.g. +1234567890"> <span
						class="validation-error" id="phoneNumberError"></span>
				</div>
				<div class="form-group">
					<label for="profile_picture"><i class="fas fa-camera"></i>
						Profile Picture (Optional):</label> <input type="file"
						id="profile_picture" name="profile_picture"
						accept="image/png, image/jpeg, image/gif, image/webp"> <small
						class="field-hint">Max 2MB. Allowed types: JPG, PNG, GIF,
						WEBP.</small> <span class="validation-error" id="profilePictureError"></span>
					<div class="image-preview-container">
						<img id="imagePreview" src="#" alt="Image Preview" />
					</div>
				</div>

				<div class="form-group">
					<label for="role_id"><i class="fas fa-user-shield"></i>
						Role:</label> <select id="role_id" name="role_id">
						<option value="">-- Select Role --</option>
						<option value="1" ${submittedRoleId == '1' ? 'selected' : ''}>User</option>
						<option value="2" ${submittedRoleId == '2' ? 'selected' : ''}>Admin</option>
					</select> <span class="validation-error" id="roleIdError"></span>
				</div>

				<div class="form-group checkbox-group">
					<input type="checkbox" id="is_active" name="is_active" value="true"
						${submittedIsActive == null || submittedIsActive ? 'checked' : ''}>
					<label for="is_active" class="checkbox-label">Active
						Account</label> <small class="field-hint">(Uncheck to create an
						inactive account initially)</small>
					<%-- No specific error span needed if defaulting or handled broadly --%>
				</div>

				<div class="form-group">
					<label for="password"><i class="fas fa-lock"></i> Password:</label>
					<input type="password" id="password" name="password"> <span
						class="validation-error" id="passwordError"></span>
				</div>
				<div class="form-group">
					<label for="retype_password"><i class="fas fa-redo-alt"></i>
						Retype Password:</label> <input type="password" id="retype_password"
						name="retype_password"> <span class="validation-error"
						id="retypePasswordError"></span>
				</div>
				<div class="form-actions">
					<button type="submit" class="button button-primary full-width">
						<i class="fas fa-user-plus"></i> Register
					</button>
				</div>
				<p class="auth-link">
					Already have an account? <a
						href="${pageContext.request.contextPath}/login">Login here</a>
				</p>
			</form>
		</div>
	</main>

	<script>
        // Image Preview Script
        const profilePictureInput = document.getElementById('profile_picture');
        const imagePreview = document.getElementById('imagePreview');

        if (profilePictureInput && imagePreview) {
            profilePictureInput.onchange = evt => {
                const [file] = profilePictureInput.files;
                if (file && file.type.startsWith('image/')) {
                    imagePreview.src = URL.createObjectURL(file);
                    imagePreview.style.display = 'block';
                } else {
                    imagePreview.src = '#';
                    imagePreview.style.display = 'none';
                    if (file) {
                        document.getElementById('profilePictureError').textContent = 'Please select a valid image file for preview.';
                    } else {
                        document.getElementById('profilePictureError').textContent = '';
                    }
                }
            }
        }

        // Client-Side Validation
        function validateRegisterForm() {
            let isValid = true;
            document.querySelectorAll('.validation-error').forEach(el => el.textContent = '');
            document.querySelectorAll('.form-group input, .form-group select').forEach(el => el.classList.remove('invalid'));

            function setError(elementId, message) {
                const errorSpan = document.getElementById(elementId + 'Error');
                const inputElement = document.getElementById(elementId);
                if (errorSpan) errorSpan.textContent = message;
                if (inputElement) inputElement.classList.add('invalid');
                isValid = false;
            }

            // Full Name
            const fullName = document.getElementById('full_name');
            if (!fullName.value.trim()) setError('fullName', 'Full Name is required.');

            // Username
            const username = document.getElementById('username');
            if (!username.value.trim()) setError('username', 'Username is required.');
            else if (!/^[a-zA-Z0-9_]{3,50}$/.test(username.value.trim())) setError('username', 'Username: 3-50 chars, letters, numbers, or underscores.');

            // Email
            const email = document.getElementById('email');
            if (!email.value.trim()) setError('email', 'Email is required.');
            else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())) setError('email', 'Invalid email format.');

            // Phone Number (Optional, but validate format if entered)
            const phoneNumber = document.getElementById('phone_number');
            if (phoneNumber.value.trim() && !/^\+?[0-9]{7,15}$/.test(phoneNumber.value.trim())) {
                setError('phoneNumber', 'Invalid phone number format (e.g., +1234567890 or 1234567890).');
            }

            // Password
            const password = document.getElementById('password');
            const retypePassword = document.getElementById('retype_password');
            const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&_/-])[A-Za-z\d@$!%*?&_/-]{8,}$/;
            if (!password.value) setError('password', 'Password is required.');
            else if (!passwordRegex.test(password.value)) setError('password', 'Password: 8+ chars, 1 upper, 1 lower, 1 digit, 1 symbol (@$!%*?&_-).');

            // Retype Password
            if (!retypePassword.value) setError('retypePassword', 'Please retype your password.');
            else if (password.value && password.value !== retypePassword.value) setError('retypePassword', 'Passwords do not match.');

            // Role ID (Required)
            const roleIdInput = document.getElementById('role_id');
            if (!roleIdInput.value) {
                setError('roleId', 'Please select a role.');
            }

            // Profile Picture (Optional, but validate if provided)
            const profilePictureInputValidation = document.getElementById('profile_picture');
            if (profilePictureInputValidation.files.length > 0) {
                const file = profilePictureInputValidation.files[0];
                const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
                const maxFileSize = 2 * 1024 * 1024;

                if (!allowedTypes.includes(file.type)) {
                    setError('profilePicture', 'Invalid file type. Use PNG, JPG, GIF, or WEBP.');
                }
                if (file.size > maxFileSize) {
                    setError('profilePicture', 'File is too large (max 2MB).');
                }
            }

            if (!isValid) {
                const firstErrorField = document.querySelector('.form-group input.invalid, .form-group select.invalid');
                if (firstErrorField) {
                    firstErrorField.focus();
                    firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
            return isValid;
        }
    </script>
</body>
</html>