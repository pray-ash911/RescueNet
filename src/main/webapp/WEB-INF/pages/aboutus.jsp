<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>About Us - RescueNet</title>
    <!-- Favicon -->
    <link rel="icon" href="${pageContext.request.contextPath}/resources/images/system/favicon.ico" type="image/x-icon">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <!-- CSS -->
    <link rel="stylesheet" type="text/css"
        href="${pageContext.request.contextPath}/css/header.css" />
    <link rel="stylesheet" type="text/css"
        href="${pageContext.request.contextPath}/css/footer.css" />
    <link rel="stylesheet" type="text/css"
        href="${pageContext.request.contextPath}/css/aboutus.css" />
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <main class="about-container">
        <section class="hero-banner">
            <div class="hero-content">
                <h1>About RescueNet</h1>
                <p class="tagline">Empowering Emergency Services Through Technology</p>
            </div>
        </section>

        <section class="mission-section">
            <div class="section-content">
                <h2><i class="fas fa-bullseye"></i> Our Mission</h2>
                <p>RescueNet provides emergency services with an intelligent platform for fleet management that saves critical time during emergencies. Our system reduces response times by up to 40% through optimized coordination and real-time tracking.</p>
                
                <div class="stats-grid">
                    <div class="stat-card">
                        <i class="fas fa-clock"></i>
                        <h3>40% Faster</h3>
                        <p>Average response time improvement</p>
                    </div>
                    <div class="stat-card">
                        <i class="fas fa-ambulance"></i>
                        <h3>500+</h3>
                        <p>Emergency vehicles managed</p>
                    </div>
                    <div class="stat-card">
                        <i class="fas fa-city"></i>
                        <h3>25 Cities</h3>
                        <p>Serving communities nationwide</p>
                    </div>
                </div>
            </div>
        </section>

        <section class="features-section">
            <h2><i class="fas fa-lightbulb"></i> Why We Built RescueNet</h2>
            <p>Traditional emergency vehicle management systems were failing to meet modern demands:</p>
            
            <div class="problem-solution">
                <div class="problem">
                    <h3><i class="fas fa-exclamation-triangle"></i> The Problems</h3>
                    <ul>
                        <li><i class="fas fa-hourglass-half"></i> Slow manual dispatch processes</li>
                        <li><i class="fas fa-map-marked-alt"></i> Poor vehicle location tracking</li>
                        <li><i class="fas fa-clipboard-list"></i> Disorganized paper records</li>
                        <li><i class="fas fa-user-shield"></i> Inadequate security protocols</li>
                    </ul>
                </div>
                <div class="solution">
                    <h3><i class="fas fa-check-circle"></i> Our Solutions</h3>
                    <ul>
                        <li><i class="fas fa-bolt"></i> Automated dispatch system</li>
                        <li><i class="fas fa-map-marker-alt"></i> Real-time GPS tracking</li>
                        <li><i class="fas fa-database"></i> Digital record management</li>
                        <li><i class="fas fa-lock"></i> Military-grade encryption</li>
                    </ul>
                </div>
            </div>
        </section>

        <section class="team-section">
            <h2><i class="fas fa-users"></i> Our Team</h2>
            <p>We are emergency responders, technologists, and public safety advocates united by a common purpose:</p>
            
            <div class="team-values">
                <div class="value-card">
                    <i class="fas fa-heartbeat"></i>
                    <h3>Save Lives</h3>
                    <p>Every feature is designed with life-saving potential in mind</p>
                </div>
                <div class="value-card">
                    <i class="fas fa-shield-alt"></i>
                    <h3>Ensure Safety</h3>
                    <p>Security and reliability are our top priorities</p>
                </div>
                <div class="value-card">
                    <i class="fas fa-rocket"></i>
                    <h3>Innovate Constantly</h3>
                    <p>We evolve with the changing needs of emergency services</p>
                </div>
            </div>
        </section>

        <section class="features-highlight">
            <h2><i class="fas fa-star"></i> Key Features</h2>
            <div class="features-grid">
                <div class="feature-card">
                    <i class="fas fa-tachometer-alt"></i>
                    <h3>Real-Time Dashboard</h3>
                    <p>Monitor all vehicles at a glance with our comprehensive dashboard</p>
                </div>
                <div class="feature-card">
                    <i class="fas fa-search"></i>
                    <h3>Instant Search</h3>
                    <p>Find any vehicle by name, ID, or location in seconds</p>
                </div>
                <div class="feature-card">
                    <i class="fas fa-user-tag"></i>
                    <h3>Role-Based Access</h3>
                    <p>Custom permissions for admins, dispatchers, and field staff</p>
                </div>
                <div class="feature-card">
                    <i class="fas fa-history"></i>
                    <h3>Complete History</h3>
                    <p>Detailed logs of all vehicle activity and maintenance</p>
                </div>
                <div class="feature-card">
                    <i class="fas fa-lock"></i>
                    <h3>Bank-Level Security</h3>
                    <p>Enterprise-grade protection for sensitive emergency data</p>
                </div>
                <div class="feature-card">
                    <i class="fas fa-mobile-alt"></i>
                    <h3>Mobile Ready</h3>
                    <p>Full functionality on any device, anywhere</p>
                </div>
            </div>
        </section>
    </main>

    <jsp:include page="footer.jsp" />
    
    <!-- JavaScript -->
    <script src="${pageContext.request.contextPath}/js/animations.js"></script>
</body>
</html>