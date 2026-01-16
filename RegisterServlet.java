package com.example;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create Connection (XAMPP MySQL)
            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/login_db",
                "root",
                ""
            );

            // Insert user
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users(username, password) VALUES (?, ?)"
            );

            ps.setString(1, username);
            ps.setString(2, password);

            ps.executeUpdate();

            out.println("<h2>Registration Successful</h2>");
            out.println("<a href='login.html'>Go to Login</a>");

            con.close();

        } catch (SQLIntegrityConstraintViolationException e) {
            out.println("<h3>Username already exists!</h3>");
            out.println("<a href='register.html'>Try Again</a>");

        } catch (Exception e) {
            out.println("<h3>Error: " + e.getMessage() + "</h3>");
        }
    }
}
