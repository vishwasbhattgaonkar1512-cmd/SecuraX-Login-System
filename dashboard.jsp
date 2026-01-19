<%@ page import="java.sql.*" %>
<%@ page import="jakarta.servlet.http.*" %>
<%@ page session="true" %>

<%
    String username = (String) session.getAttribute("username");

    if (username == null) {
        response.sendRedirect("login.html");
        return;
    }

    int failedAttempts = 0;

    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/login_db",
            "root",
            ""
        );

        PreparedStatement ps = con.prepareStatement(
            "SELECT COUNT(*) FROM login_logs WHERE username=? AND status='FAILED' " +
            "AND attempt_time > (NOW() - INTERVAL 5 MINUTE)"
        );
        ps.setString(1, username);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            failedAttempts = rs.getInt(1);
        }

        con.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
%>

<!DOCTYPE html>
<html>
<head>
    <title>SecuraX | Dashboard</title>
    <link rel="stylesheet" href="assets/style.css">
</head>
<body>

<div class="container">
    <div class="auth-card">

        <div class="brand">Secura<span>X</span> Dashboard</div>

        <h2>Welcome, <%= username %></h2>

        <p><strong>Failed Login Attempts (last 5 min):</strong></p>
        <h1 style="color:red"><%= failedAttempts %></h1>

        <%
            if (failedAttempts >= 3) {
        %>
            <p style="color:orange">
                ⚠ Suspicious activity detected. CAPTCHA will be required.
            </p>
        <%
            }
        %>

        <hr>

        <form action="logout" method="post">
            <button type="submit">Logout</button>
        </form>

    </div>
</div>

</body>
</html>
