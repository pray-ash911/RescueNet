<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RescueNet - Available Vehicles</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css"> <%-- Ensure this CSS file exists --%>
</head>
<body>
    <%-- Header --%>
    <jsp:include page="header.jsp" />

    <%-- Main Content Area --%>
    <main class="main-container home-content">

        <%-- Banner Image --%>
        <div class="banner-container">
            <img class="banner-image" src="${pageContext.request.contextPath}/resources/images/system/homebanner.jpg" alt="RescueNet Banner" />
        </div>
<div class="SIDE -banner-container">
            <img class="banner-image" src="${pageContext.request.contextPath}/resources/images/system/qr.jpg" alt="RescueNet Banner" />
        </div>
        <%-- Top Section: Title, Welcome, Search --%>
        <header class="page-header">
             <h1 class="page-title">Available Vehicles</h1>
             <div class="user-welcome">
                 <c:if test="${not empty sessionScope.user}">
                     <span>Welcome, ${sessionScope.user.fullName}</span>
                     <a href="${pageContext.request.contextPath}/logout" class="user-action-link">Logout</a>
                 </c:if>
                 <c:if test="${empty sessionScope.user}">
                     <a href="${pageContext.request.contextPath}/login" class="user-action-link">Login</a>
                 </c:if>
             </div>
        </header>

        <!-- Display error/status messages -->
        <c:if test="${not empty requestScope.errorMessage}">
            <div class="message error-message">${requestScope.errorMessage}</div>
        </c:if>
        <c:if test="${empty requestScope.errorMessage && not empty param.error}">
            <div class="message error-message">${param.error}</div>
        </c:if>
         <c:if test="${not empty param.success}">
            <div class="message success-message">${param.success}</div>
        </c:if>

        <!-- Search Form -->
        <section class="search-container">
            <form action="${pageContext.request.contextPath}/home" method="get" class="search-form">
                <label for="searchQuery" class="search-label">Search Vehicles:</label>
                <input type="text" name="searchQuery" id="searchQuery" class="search-input" placeholder="e.g., Ambulance, Ford, SN123" value="${searchQuery}">
                <button type="submit" class="search-button">Search</button>
                 <c:if test="${not empty searchQuery}">
                    <a href="${pageContext.request.contextPath}/home" class="clear-search-link">Clear Search</a>
                </c:if>
            </form>
        </section>

        <%-- Conditional Display: Search Results OR Categories --%>
        <section class="vehicle-display-area">
            <c:choose>
                <%-- ========== SEARCH RESULTS VIEW ========== --%>
                <c:when test="${not empty searchQuery}">
                    <h2 class="results-heading">Search Results for: "${searchQuery}"</h2>
                    <c:if test="${empty searchResults}">
                        <div class="no-results">
                            <p>No available vehicles found matching your search criteria.</p>
                        </div>
                    </c:if>
                    <c:if test="${not empty searchResults}">
                        <div class="vehicle-grid">
                            <%-- Loop through search results --%>
                            <c:forEach var="vehicle" items="${searchResults}">
                                <%-- *** Start Inlined Vehicle Card HTML *** --%>
                                <article class="vehicle-card">
                                    <div class="card-image-container">
                                        <c:if test="${not empty vehicle.imagePath}">
                                            <img class="card-image" src="${pageContext.request.contextPath}/${vehicle.imagePath}" alt="${vehicle.brandName} ${vehicle.model}">
                                        </c:if>
                                        <c:if test="${empty vehicle.imagePath}">
                                            <div class="card-image-placeholder">No Image</div>
                                        </c:if>
                                    </div>
                                    <div class="card-body">
                                        <h3 class="card-title">${vehicle.brandName} ${vehicle.model}</h3>
                                        <div class="card-details">
                                            <%-- Show Type in search results --%>
                                            <p class="detail-item"><span class="detail-label">Type:</span> ${vehicle.type}</p>
                                            <p class="detail-item">
                                                <span class="detail-label">Status:</span>
                                                <span class="status-badge status-${vehicle.status.toLowerCase()}">${vehicle.status}</span>
                                            </p>
                                            <c:if test="${not empty vehicle.manufacturedDate}">
                                                <p class="detail-item"><span class="detail-label">Manufactured:</span> ${vehicle.manufacturedDate}</p>
                                            </c:if>
                                            <c:if test="${not empty vehicle.description}">
                                                <p class="detail-item card-description"><span class="detail-label">Description:</span> ${vehicle.description}</p>
                                            </c:if>
                                        </div>
                                        <c:if test="${not empty vehicle.price}">
                                            <div class="card-price-tag">$${vehicle.price} <span class="price-unit">/ day</span></div>
                                        </c:if>
                                        <%-- Potential Actions Button Here --%>
                                    </div>
                                </article>
                                <%-- *** End Inlined Vehicle Card HTML *** --%>
                            </c:forEach>
                        </div>
                    </c:if>
                </c:when>

                <%-- ========== CATEGORIZED VIEW (Default) ========== --%>
                <c:otherwise>
                    <c:if test="${empty categorizedVehicles}">
                        <div class="no-results">
                            <p>No available vehicles currently listed.</p>
                        </div>
                    </c:if>
                    <c:if test="${not empty categorizedVehicles}">
                        <%-- Iterate over Categories --%>
                        <c:forEach var="categoryEntry" items="${categorizedVehicles}">
                            <section class="vehicle-category-section">
                                <h2 class="category-heading">${categoryEntry.key}s</h2>
                                <div class="vehicle-grid">
                                    <%-- Iterate over vehicles in this category --%>
                                    <c:forEach var="vehicle" items="${categoryEntry.value}">
                                        <%-- *** Start Inlined Vehicle Card HTML *** --%>
                                        <article class="vehicle-card">
                                            <div class="card-image-container">
                                                <c:if test="${not empty vehicle.imagePath}">
                                                    <img class="card-image" src="${pageContext.request.contextPath}/${vehicle.imagePath}" alt="${vehicle.brandName} ${vehicle.model}">
                                                </c:if>
                                                <c:if test="${empty vehicle.imagePath}">
                                                    <div class="card-image-placeholder">No Image</div>
                                                </c:if>
                                            </div>
                                            <div class="card-body">
                                                <h3 class="card-title">${vehicle.brandName} ${vehicle.model}</h3>
                                                <div class="card-details">
                                                    <%-- Type is implied by category heading, so omit here --%>
                                                    <p class="detail-item">
                                                        <span class="detail-label">Status:</span>
                                                        <span class="status-badge status-${vehicle.status.toLowerCase()}">${vehicle.status}</span>
                                                    </p>
                                                    <c:if test="${not empty vehicle.manufacturedDate}">
                                                        <p class="detail-item"><span class="detail-label">Manufactured:</span> ${vehicle.manufacturedDate}</p>
                                                    </c:if>
                                                     <c:if test="${not empty vehicle.description}">
                                                        <p class="detail-item card-description"><span class="detail-label">Description:</span> ${vehicle.description}</p>
                                                    </c:if>
                                                </div>
                                                <c:if test="${not empty vehicle.price}">
                                                    <div class="card-price-tag">$${vehicle.price} <span class="price-unit">/ day</span></div>
                                                </c:if>
                                                 <%-- Potential Actions Button Here --%>
                                            </div>
                                        </article>
                                         <%-- *** End Inlined Vehicle Card HTML *** --%>
                                    </c:forEach>
                                </div> <%-- End vehicle-grid --%>
                            </section> <%-- End vehicle-category-section --%>
                        </c:forEach>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </section> <%-- End vehicle-display-area --%>

    </main> <%-- End main-container --%>

    <%-- Footer --%>
    <jsp:include page="footer.jsp" />
</body>
</html>