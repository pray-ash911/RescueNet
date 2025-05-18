<%-- 
  @author Prayash Rawal
  Purpose: Displays the vehicle management page for the RescueNet admin dashboard, allowing
           administrators to add or edit vehicle details, including serial number, brand, model,
           type, status, manufactured date, description, price, and image, with client-side
           validation and error/success message handling.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>RescueNet - Vehicle Management</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/admin_dashboard.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/vehicles.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/footer.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
	<div class="admin-container">
		<aside class="sidebar">
			<div class="logo">
				<h2>RescueNet</h2>
			</div>
			<nav class="sidebar-nav">
				<a href="${pageContext.request.contextPath}/admin" class="nav-link"><i
					class="fas fa-tachometer-alt nav-icon"></i><span>Dashboard</span></a> <a
					href="${pageContext.request.contextPath}/vehicles"
					class="nav-link active"><i class="fas fa-truck nav-icon"></i><span>Vehicles</span></a>
				<a href="${pageContext.request.contextPath}/reservations"
					class="nav-link"><i class="fas fa-calendar-alt nav-icon"></i><span>Reservations</span></a>
			</nav>
		</aside>

		<main class="main-content">
			<header class="top-header">
				<h1>Vehicle Management</h1>
				<div class="user-info">
					<span><i class="fas fa-user-circle"></i> Welcome,
						${sessionScope.user.username}</span> <a
						href="${pageContext.request.contextPath}/logout"
						class="logout-btn"><i class="fas fa-sign-out-alt"></i> Logout</a>
				</div>
			</header>

			<section class="page-content vehicle-content">
				<c:if test="${not empty param.success}">
					<div class="message success-message">${param.success}</div>
				</c:if>
				<c:if test="${not empty param.error}">
					<div class="message error-message">${param.error}</div>
				</c:if>
				<c:if test="${not empty requestScope.error}">
					<div class="message error-message">${requestScope.error}</div>
				</c:if>

				<div class="form-container card-style">
					<h2>${not empty vehicle ? 'Edit Vehicle' : 'Add New Vehicle'}</h2>
					<form id="vehicleForm"
						action="${pageContext.request.contextPath}/vehicles/${not empty vehicle ? 'update' : 'create'}"
						method="post" enctype="multipart/form-data" class="vehicle-form"
						onsubmit="return validateVehicleForm();">

						<c:if test="${not empty vehicle}">
							<input type="hidden" name="vehicleId"
								value="${vehicle.vehicleId}">
						</c:if>

						<div class="form-grid">
							<div class="form-group">
								<label for="serialNumber">Serial Number:</label> <input
									type="text" id="serialNumber" name="serialNumber"
									value="${vehicle.serialNumber}"> <span
									class="validation-error" id="serialNumberError"></span>
							</div>
							<div class="form-group">
								<label for="brandName">Brand Name:</label> <input type="text"
									id="brandName" name="brandName" value="${vehicle.brandName}">
								<span class="validation-error" id="brandNameError"></span>
							</div>
							<div class="form-group">
								<label for="model">Model:</label> <input type="text" id="model"
									name="model" value="${vehicle.model}"> <span
									class="validation-error" id="modelError"></span>
							</div>
							<div class="form-group">
								<label for="type">Type:</label> <input type="text" id="type"
									name="type" value="${vehicle.type}"
									placeholder="e.g., Ambulance, Police Car"> <span
									class="validation-error" id="typeError"></span>
							</div>
							<div class="form-group">
								<label for="status">Status:</label> <select name="status"
									id="status">
									<option value="">-- Select Status --</option>
									<option value="Available"
										${'Available' == vehicle.status ? 'selected' : ''}>Available</option>
									<option value="Reserved"
										${'Reserved' == vehicle.status ? 'selected' : ''}>Reserved</option>
									<option value="Maintenance"
										${'Maintenance' == vehicle.status ? 'selected' : ''}>Maintenance</option>
									<option value="Rented"
										${'Rented' == vehicle.status ? 'selected' : ''}>Rented</option>
									<option value="Unavailable"
										${'Unavailable' == vehicle.status ? 'selected' : ''}>Unavailable</option>
								</select> <span class="validation-error" id="statusError"></span>
							</div>
							<div class="form-group">
								<label for="manufacturedDate">Manufactured Date:</label> <input
									type="date" id="manufacturedDate" name="manufacturedDate"
									value="${vehicle.manufacturedDate}"> <span
									class="validation-error" id="manufacturedDateError"></span>
							</div>
							<div class="form-group form-group-full">
								<label for="description">Description:</label>
								<textarea id="description" name="description" rows="3">${vehicle.description}</textarea>
								<span class="validation-error" id="descriptionError"></span>
							</div>
							<div class="form-group">
								<label for="price">Price (per day):</label> <input type="number"
									id="price" name="price" step="0.01" min="0"
									value="${vehicle.price}" placeholder="e.g., 150.00"> <span
									class="validation-error" id="priceError"></span>
							</div>
							<div class="form-group form-group-full">
								<label for="image">Image:</label> <input type="file" id="image"
									name="image" accept="image/*"> <span
									class="validation-error" id="imageError"></span>
								<c:if test="${not empty vehicle.imagePath}">
									<img
										src="${pageContext.request.contextPath}/${vehicle.imagePath}"
										alt="Current Image" class="current-image-preview">
									<small>Current Image (uploading a new one will replace
										it)</small>
								</c:if>
							</div>
						</div>

						<div class="form-actions">
							<button type="submit" class="button button-primary">
								<i
									class="fas ${not empty vehicle ? 'fa-save' : 'fa-plus-circle'}"></i>
								${not empty vehicle ? 'Update Vehicle' : 'Add Vehicle'}
							</button>
							<c:if test="${not empty vehicle}">
								<a href="${pageContext.request.contextPath}/vehicles"
									class="button button-secondary">Cancel</a>
							</c:if>
						</div>
					</form>
				</div>

				<div class="vehicle-list-container card-style">
					<h2>Existing Vehicles</h2>
					<p>(Placeholder: A table or grid displaying existing vehicles
						with edit/delete options would go here. This would typically be
						populated by fetching data in the VehiclesController's doGet
						method and setting it as a request attribute.)</p>
				</div>
			</section>
			<jsp:include page="footer.jsp" />
		</main>
	</div>

	<script>
        function validateVehicleForm() {
            let isValid = true;
            // Clear previous errors and invalid classes
            document.querySelectorAll('.validation-error').forEach(el => el.textContent = '');
            document.querySelectorAll('.form-group input, .form-group select, .form-group textarea').forEach(el => el.classList.remove('invalid'));

            // Helper function to set error
            function setError(elementId, message) {
                document.getElementById(elementId + 'Error').textContent = message;
                document.getElementById(elementId).classList.add('invalid');
                isValid = false;
            }

            // Determine if we are in "edit" mode
            const isEditing = document.querySelector('input[name="vehicleId"]') !== null &&
                              document.querySelector('input[name="vehicleId"]').value !== '';

            // Serial Number (Required, Basic alphanumeric with hyphen)
            const serialNumber = document.getElementById('serialNumber');
            if (!serialNumber.value.trim()) {
                setError('serialNumber', 'Serial Number is required.');
            } else if (!/^[a-zA-Z0-9-]+$/.test(serialNumber.value.trim())) {
                setError('serialNumber', 'Serial Number can only contain letters, numbers, and hyphens.');
            }

            // Brand Name (Required)
            const brandName = document.getElementById('brandName');
            if (!brandName.value.trim()) {
                setError('brandName', 'Brand Name is required.');
            }

            // Model (Required)
            const model = document.getElementById('model');
            if (!model.value.trim()) {
                setError('model', 'Model is required.');
            }

            // Type (Required)
            const type = document.getElementById('type');
            if (!type.value.trim()) {
                setError('type', 'Type is required.');
            }

            // Status (Required)
            const status = document.getElementById('status');
            if (!status.value) {
                setError('status', 'Status is required.');
            }

            // Manufactured Date (Optional, but if entered, must be a valid date and not in future)
            const manufacturedDateInput = document.getElementById('manufacturedDate');
            if (manufacturedDateInput.value) {
                try {
                    // Ensure date is parsed correctly as local date by adding time part
                    const manufacturedDate = new Date(manufacturedDateInput.value + "T00:00:00");
                    const today = new Date();
                    today.setHours(0, 0, 0, 0); // Compare dates only

                    if (isNaN(manufacturedDate.getTime())) { // Check if date is valid
                        setError('manufacturedDate', 'Invalid date format.');
                    } else if (manufacturedDate > today) {
                        setError('manufacturedDate', 'Manufactured date cannot be in the future.');
                    }
                } catch (e) {
                    setError('manufacturedDate', 'Invalid date format.');
                }
            }

            // Description (Optional, length check example)
            const description = document.getElementById('description');
            if (description.value.trim().length > 500) {
                setError('description', 'Description cannot exceed 500 characters.');
            }

            // Price (NOW REQUIRED)
            const priceInput = document.getElementById('price');
            if (!priceInput.value.trim()) {
                setError('price', 'Price is required.');
            } else {
                const priceValue = parseFloat(priceInput.value);
                if (isNaN(priceValue) || priceValue < 0) {
                    setError('price', 'Price must be a non-negative number.');
                }
            }

            // Image (Required for NEW vehicles, optional for EDIT if image already exists)
            const imageInput = document.getElementById('image');
            const currentImagePreview = document.querySelector('.current-image-preview');

            if (!isEditing && imageInput.files.length === 0) {
                // Only required if creating a new vehicle AND no file is selected
                setError('image', 'Image is required for new vehicles.');
            } else if (imageInput.files.length > 0) { // If a file IS selected (for new or update)
                const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
                // Max file size should ideally match your @MultipartConfig on the servlet
                const maxFileSize = 5 * 1024 * 1024; // 5MB
                const file = imageInput.files[0];

                if (!allowedTypes.includes(file.type)) {
                    setError('image', 'Invalid file type. Please upload JPG, PNG, GIF, or WEBP.');
                }
                if (file.size > maxFileSize) {
                    setError('image', 'File size cannot exceed 5MB.');
                }
            }

            if (!isValid) {
                // Optionally, scroll to the first error
                const firstErrorField = document.querySelector('.form-group input.invalid, .form-group select.invalid, .form-group textarea.invalid');
                if (firstErrorField) {
                    firstErrorField.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    firstErrorField.focus(); // Focus on the first invalid field
                }
            }
            return isValid; // Submit form if true, block if false
        }
    </script>
</body>
</html>