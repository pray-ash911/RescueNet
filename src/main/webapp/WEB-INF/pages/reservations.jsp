<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- No longer need fmt taglib for the date --%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RescueNet - Reservations</title>
    <%-- Link common admin layout styles --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_dashboard.css">
    <%-- Link specific styles for this page --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/reservations.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <div class="admin-container">

        <%-- Sidebar --%>
        <aside class="sidebar">
            <div class="logo">
                <h2>RescueNet</h2>
            </div>
            <nav class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/admin" class="nav-link"><i class="fas fa-tachometer-alt nav-icon"></i><span>Dashboard</span></a>
                <a href="${pageContext.request.contextPath}/vehicles" class="nav-link"><i class="fas fa-truck nav-icon"></i><span>Vehicles</span></a>
                <a href="${pageContext.request.contextPath}/reservations" class="nav-link active"><i class="fas fa-calendar-alt nav-icon"></i><span>Reservations</span></a>
            </nav>
        </aside>

        <%-- Main Content --%>
        <main class="main-content">
            <%-- Header --%>
            <header class="top-header">
                <h1>Vehicle Reservations</h1>
                <div class="user-info">
                    <span><i class="fas fa-user-circle"></i> Welcome, ${sessionScope.user.username}</span>
                    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Logout</a>
                </div>
            </header>

            <%-- Page Specific Content --%>
            <section class="page-content reservation-content">

                <!-- Display Messages -->
                <c:if test="${not empty param.success}"><div class="message success-message">${param.success}</div></c:if>
                <c:if test="${not empty param.error}"><div class="message error-message">${param.error}</div></c:if>
                <c:if test="${not empty requestScope.error}"><div class="message error-message">${requestScope.error}</div></c:if>

                <!-- Form Section -->
                <div class="form-container card-style">
                    <h2>${not empty reservation ? 'Edit Reservation' : 'Create New Reservation'}</h2>
                    <form action="${pageContext.request.contextPath}/reservations/${not empty reservation ? 'update' : 'create'}" method="post" class="reservation-form">
                        <c:if test="${not empty reservation}">
                            <input type="hidden" name="reservationId" value="${reservation.reservationId}">
                        </c:if>

                        <div class="form-grid"> <%-- Use grid for layout --%>

                            <div class="form-group">
                                <label for="vehicleId">Vehicle:</label>
                                <select name="vehicleId" id="vehicleId" required>
                                    <option value="">-- Select Vehicle --</option>
                                    <c:forEach var="vehicle" items="${availableVehicles}">
                                        <option value="${vehicle.vehicleId}" ${vehicle.vehicleId == reservation.vehicleId ? 'selected' : ''}>
                                            ${vehicle.brandName} ${vehicle.model} (${vehicle.serialNumber})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="userId">User:</label>
                                <select name="userId" id="userId" required>
                                    <option value="">-- Select User --</option>
                                    <c:forEach var="usr" items="${users}">
                                        <option value="${usr.userId}" ${usr.userId == reservation.userId ? 'selected' : ''}>
                                            ${usr.username} (${usr.fullName})
                                        </option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label for="reservationDate">Reservation Date:</label>
                                <input type="date" id="reservationDate" name="reservationDate" value="${reservation.reservationDateString}" required>
                            </div>

                            <div class="form-group">
                                <label for="status">Status:</label>
                                <select name="status" id="status" required>
                                     <option value="">-- Select Status --</option>
                                     <option value="Pending" ${'Pending' == reservation.status ? 'selected' : ''}>Pending</option>
                                     <option value="Confirmed" ${'Confirmed' == reservation.status ? 'selected' : ''}>Confirmed</option>
                                     <option value="Cancelled" ${'Cancelled' == reservation.status ? 'selected' : ''}>Cancelled</option>
                                     <option value="Completed" ${'Completed' == reservation.status ? 'selected' : ''}>Completed</option>
                                </select>
                            </div>
                        </div> <%-- End form-grid --%>

                        <div class="form-actions">
                             <button type="submit" class="button button-primary">
                                 <i class="fas ${not empty reservation ? 'fa-save' : 'fa-plus-circle'}"></i>
                                 ${not empty reservation ? 'Update Reservation' : 'Create Reservation'}
                             </button>
                             <c:if test="${not empty reservation}">
                                <a href="${pageContext.request.contextPath}/reservations" class="button button-secondary">Cancel</a>
                             </c:if>
                        </div>
                    </form>
                </div> <%-- End form-container --%>


                <!-- Reservations Table Section -->
                 <div class="table-container card-style">
                    <h2>All Reservations</h2>
                    <div class="table-wrapper"> <%-- Added wrapper for potential horizontal scroll --%>
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Username</th>
                                    <th>Vehicle</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="res" items="${reservations}">
                                    <tr>
                                        <td>${res.reservationId}</td>
                                        <td>${res.username}</td>
                                        <td>${res.vehicleInfo}</td>
                                        <td>${res.reservationDate}</td> <%-- Direct output of LocalDate --%>
                                        <td><span class="status-label status-${res.status.toLowerCase()}">${res.status}</span></td>
                                        <td class="actions-cell">
                                            <%-- Edit Link (styled as button) --%>
                                            <a href="${pageContext.request.contextPath}/reservations/update?id=${res.reservationId}" class="button button-small button-edit" title="Edit">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                            <%-- Delete Form (styled button) --%>
                                            <form action="${pageContext.request.contextPath}/reservations/delete" method="post" class="delete-form">
                                                <input type="hidden" name="reservationId" value="${res.reservationId}">
                                                <button type="submit" class="button button-small button-delete" title="Delete"
                                                        onclick="return confirm('Delete reservation ${res.reservationId} for ${res.vehicleInfo} on ${res.reservationDate}?');">
                                                    <i class="fas fa-trash-alt"></i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty reservations}">
                                    <tr>
                                        <td colspan="6" class="no-results-row">No reservations found.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div> <%-- End table-wrapper --%>
                </div> <%-- End table-container --%>

            </section> <%-- End page-content --%>


        </main> <%-- End main-content --%>
    </div> <%-- End admin-container --%>
</body>
</html>