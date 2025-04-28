<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RescueNet Admin Dashboard</title>
    <%-- Make sure this CSS file exists and is loaded --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css"> <%-- Keep footer CSS link --%>

    <%-- Font Awesome for Icons --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <%-- Main container using Flexbox for sidebar + main content --%>
    <div class="admin-container">

        <%-- Sidebar (Keep as is) --%>
        <aside class="sidebar">
            <div class="logo">
                <h2>RescueNet</h2>
            </div>
            <nav class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/admin" class="nav-link active"><i class="fas fa-tachometer-alt nav-icon"></i>Dashboard</a>
                <a href="${pageContext.request.contextPath}/vehicles" class="nav-link"><i class="fas fa-truck nav-icon"></i>Vehicles</a>
                <a href="${pageContext.request.contextPath}/reservations" class="nav-link"><i class="fas fa-calendar-alt nav-icon"></i>Reservations</a>
            </nav>
        </aside>

        <%-- Main Content Area --%>
        <main class="main-content">
            <%-- Top Header within Main Content (Keep as is) --%>
            <header class="top-header">
                <h1>Admin Dashboard</h1>
                <div class="user-info">
                    <span><i class="fas fa-user-circle"></i> Welcome, ${sessionScope.user.username}</span>
                    <a href="${pageContext.request.contextPath}/logout" class="logout-btn"><i class="fas fa-sign-out-alt"></i> Logout</a>
                </div>
            </header>

            <%-- Dashboard Specific Content --%>
            <section class="dashboard-content">

                <%-- Welcome Message (Keep as is) --%>
                <div class="welcome-message">
                     <p>Welcome back, Admin! This dashboard provides a quick overview of your vehicle fleet status.</p>
                </div>

                 <%-- Overview Heading (Keep as is) --%>
                <h2>Overview</h2>

                <%-- Stats Cards Container --%>
                <div class="stats-container">
                    <%-- Stat Card 1 --%>
                    <div class="stat-card total">
                        <div class="stat-icon">
                            <i class="fas fa-truck-medical fa-3x"></i>
                        </div>
                        <div class="stat-info">
                            <h3>Total Vehicles</h3>
                            <p class="stat-value">${totalVehicles}</p>
                        </div>
                    </div>

                    <%-- Stat Card 2 --%>
                    <div class="stat-card available">
                        <div class="stat-icon">
                            <i class="fas fa-check-circle fa-3x"></i>
                        </div>
                        <div class="stat-info">
                            <h3>Available Vehicles</h3>
                            <p class="stat-value">${availableVehicles}</p>
                        </div>
                    </div>

                    <%-- Stat Card 3 --%>
                    <div class="stat-card service">
                        <div class="stat-icon">
                            <i class="fas fa-tools fa-3x"></i>
                        </div>
                        <div class="stat-info">
                            <h3>Vehicles in Service/Maint.</h3>
                            <p class="stat-value">${vehiclesInService}</p>
                        </div>
                    </div>

                     <%-- *** MOVED Footer Include FROM INSIDE stats-container *** --%>

                </div> <%-- End stats-container --%>

            </section> <%-- End dashboard-content --%>

            <%-- *** CORRECT PLACEMENT for Footer Include *** --%>
            <%-- Place it after the main content section but before the main closing tag --%>

        </main> <%-- End main-content --%>
    </div> <%-- End admin-container --%>
</body>
</html>