package com.example;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String ipAddress = request.getRemoteAddr();

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/login_db",
                "root",
                ""
            );

            /* ================== STEP 1: BLOCK CHECK ================== */
            PreparedStatement blockCheck = con.prepareStatement(
                "SELECT block_time FROM blocked_users " +
                "WHERE username=? AND ip_address=? " +
                "AND block_time > (NOW() - INTERVAL 5 MINUTE)"
            );
            blockCheck.setString(1, username);
            blockCheck.setString(2, ipAddress);

            ResultSet blockRs = blockCheck.executeQuery();

            if (blockRs.next()) {
                out.println("<h2 style='color:red'>Account Temporarily Blocked</h2>");
                out.println("<p>Too many failed attempts. Try again after 5 minutes.</p>");
                con.close();
                return;
            }

            /* ================== STEP 2: LOGIN CHECK ================== */
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?"
            );
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                /* ✅ SUCCESS */
                HttpSession session = request.getSession();
                session.setAttribute("username", username);
                session.setAttribute("ip", ipAddress);

                PreparedStatement logPs = con.prepareStatement(
                    "INSERT INTO login_logs(username, ip_address, status) VALUES (?, ?, 'SUCCESS')"
                );
                logPs.setString(1, username);
                logPs.setString(2, ipAddress);
                logPs.executeUpdate();

                response.sendRedirect("dashboard.jsp");
                con.close();
                return;
            }

            /* ================== STEP 3: FAILED LOGIN ================== */
            PreparedStatement logFail = con.prepareStatement(
                "INSERT INTO login_logs(username, ip_address, status) VALUES (?, ?, 'FAILED')"
            );
            logFail.setString(1, username);
            logFail.setString(2, ipAddress);
            logFail.executeUpdate();

            /* ================== STEP 4: COUNT FAILURES ================== */
            PreparedStatement countPs = con.prepareStatement(
                "SELECT COUNT(*) FROM login_logs " +
                "WHERE username=? AND ip_address=? " +
                "AND status='FAILED' " +
                "AND attempt_time > (NOW() - INTERVAL 5 MINUTE)"
            );
            countPs.setString(1, username);
            countPs.setString(2, ipAddress);

            ResultSet countRs = countPs.executeQuery();
            countRs.next();
            int attempts = countRs.getInt(1);

            /* ================== STEP 5: BLOCK EXACTLY ON 5 ================== */
            if (attempts == 5) {
                PreparedStatement blockInsert = con.prepareStatement(
                    "INSERT INTO blocked_users(username, ip_address, block_time) VALUES (?, ?, NOW())"
                );
                blockInsert.setString(1, username);
                blockInsert.setString(2, ipAddress);
                blockInsert.executeUpdate();

                out.println("<h2 style='color:red'>Account Temporarily Blocked</h2>");
                out.println("<p>Too many failed attempts. Try again after 5 minutes.</p>");
            } else {
                out.println("<h2 style='color:red'>Login Failed</h2>");
                out.println("<p>Invalid username or password</p>");
                out.println("<p>Failed Attempts: " + attempts + " / 5</p>");
            }

            con.close();

        } catch (Exception e) {
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }
}
