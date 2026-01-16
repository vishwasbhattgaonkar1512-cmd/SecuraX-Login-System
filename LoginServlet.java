package com.example;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if ("admin".equals(username) && "1234".equals(password)) {
            out.println("<h2>Login Successful</h2>");
            out.println("<p>Welcome, " + username + "</p>");
        } else {
            out.println("<h2>Login Failed</h2>");
            out.println("<p>Invalid username or password</p>");
        }
    }
}
