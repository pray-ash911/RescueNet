<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RescueNet - Vehicle Management</title>
    <%-- Link common admin styles and specific vehicle styles --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_dashboard.css"> <%-- Re-use dashboard layout --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/vehicles.css"> <%-- Specific styles for vehicles page --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <%-- Font Awesome --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <%-- Main container --%>
    <div class="admin-container">

        <%-- Sidebar --%>
        <aside class="sidebar">
            <div class="logo">
                <h2>RescueNet</h2>
            </div>
            <nav class="sidebar-nav">
                 <%-- Added span for text to allow hiding on small screens via CSS --%>
                <a href="${pageContext.request.contextPath}/admin" class="nav-link"><i class="fas fa-tachometer-alt nav-icon"></i><span>Dashboard</span></a>
                <a href="${pageContext.request.contextPath}/vehicles" class="nav-link active"><i class="fas fa-truck nav-icon"></i><span>Vehicles</span></a>
                <a href="${pageContext.request.contextPath}/reservations" class="nav-link"><i class="fas fa-calendar-alt nav-icon"></i><span>Reservations</span></a>
            </nav>
        </aside>

        <%-- Main Content Area --%>
        <main class="main-content">
            <%-- Top Header --%>
            <header class="top-header">
                <h1>Vehicle Management</h1>
                <div class="user-info">
                     <%-- Using sessionScope for user object --%>
                    <span><i class="fas fa-user-circle"></i> Welcome, ${sessionScope.user.username}</span> <%-- Changed to username as fullName might not be in session --%>
                    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Logout</a>
                </div>
            </header>

            <%-- Page Specific Content --%>
            <section class="page-content vehicle-content">

                <!-- Display success/error messages -->
                 <c:if test="${not empty param.success}">
                    <div class="message success-message">${param.success}</div>
                </c:if>
                <c:if test="${not empty param.error}">
                    <div class="message error-message">${param.error}</div>
                </c:if>
                <c:if test="${not empty requestScope.error}">
                    <div class="message error-message">${requestScope.error}</div>
                </c:if>

                <!-- Form Section -->
                <div class="form-container card-style">
                    <%-- Dynamic title based on if 'vehicle' object exists (for editing) --%>
                    <h2>${not empty vehicle ? 'Edit Vehicle' : 'Add New Vehicle'}</h2>

                    <%-- Form points to create or update path --%>
                    <form action="${pageContext.request.contextPath}/vehicles/${not empty vehicle ? 'update' : 'create'}"
                          method="post" enctype="multipart/form-data" class="vehicle-form">

                        <%-- Hidden field for vehicle ID only needed when updating --%>
                        <c:if test="${not empty vehicle}">
                           <input type="hidden" name="vehicleId" value="${vehicle.vehicleId}">
                        </c:if>

                        <%-- Grid layout for form fields --%>
                        <div class="form-grid">

                            <div class="form-group">
                                <label for="serialNumber">Serial Number:</label>
                                <input type="text" id="serialNumber" name="serialNumber" value="${vehicle.serialNumber}" required>
                            </div>

                            <div class="form-group">
                                <label for="brandName">Brand Name:</label>
                                <input type="text" id="brandName" name="brandName" value="${vehicle.brandName}" required>
                            </div>

                            <div class="form-group">
                                <label for="model">Model:</label>
                                <input type="text" id="model" name="model" value="${vehicle.model}" required>
                            </div>

                            <div class="form-group">
                                <label for="type">Type:</label>
                                <input type="text" id="type" name="type" value="${vehicle.type}" required placeholder="e.g., Ambulance, Police Car">
                            </div>

                            <div class="form-group">
                                <label for="status">Status:</label>
                                <select name="status" id="status" required>
                                    <option value="">-- Select Status --</option>
                                    <option value="Available" ${'Available' == vehicle.status ? 'selected' : ''}>Available</option>
                                    <option value="Reserved" ${'Reserved' == vehicle.status ? 'selected' : ''}>Reserved</option>
                                    <option value="Maintenance" ${'Maintenance' == vehicle.status ? 'selected' : ''}>Maintenance</option>
                                    <option value="Rented" ${'Rented' == vehicle.status ? 'selected' : ''}>Rented</option>
                                    <option value="Unavailable" ${'Unavailable' == vehicle.status ? 'selected' : ''}>Unavailable</option>
                                </select>
                            </div>

                             <div class="form-group">
                                <label for="manufacturedDate">Manufactured Date:</label>
                                 <%-- Assuming vehicle.manufacturedDate returns LocalDate or null --%>
                                <input type="date" id="manufacturedDate" name="manufacturedDate" value="${vehicle.manufacturedDate}">
                             </div>

                             <div class="form-group form-group-full"> <%-- Spans full width --%>
                                <label for="description">Description:</label>
                                <textarea id="description" name="description" rows="3">${vehicle.description}</textarea>
                             </div>

                             <div class="form-group">
                                <label for="price">Price (per day):</label>
                                 <%-- Assuming vehicle.price returns BigDecimal or null --%>
                                <input type="number" id="price" name="price" step="0.01" min="0" value="${vehicle.price}" placeholder="e.g., 150.00">
                             </div>

                            <div class="form-group form-group-full">
                                <label for="image">Image:</label>
                                <input type="file" id="image" name="image" accept="image/*">
                                 <%-- Show current image preview if editing --%>
                                 <c:if test="${not empty vehicle.imagePath}">
                                     <img src="${pageContext.request.contextPath}/${vehicle.imagePath}" alt="Current Image" class="current-image-preview">
                                     <small>Current Image (uploading a new one will replace it)</small>
                                 </c:if>
                            </div>

                        </div> <%-- End form-grid --%>

                        <%-- Form Buttons --%>
                        <div class="form-actions">
                             <button type="submit" class="button button-primary">
                                <i class="fas ${not empty vehicle ? 'fa-save' : 'fa-plus-circle'}"></i>
                                ${not empty vehicle ? 'Update Vehicle' : 'Add Vehicle'}
                            </button>
                             <%-- Show Cancel button only when editing --%>
                             <c:if test="${not empty vehicle}">
                                <a href="${pageContext.request.contextPath}/vehicles" class="button button-secondary">Cancel</a>
                            </c:if>
                        </div>

                    </form>
                </div> <%-- End form-container --%>

                

            </section> <%-- End page-content --%>
        </main> <%-- End main-content --%>
    </div> <%-- End admin-container --%>
</body>
</html>