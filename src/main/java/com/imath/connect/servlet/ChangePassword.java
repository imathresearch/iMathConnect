package com.imath.connect.servlet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.imath.connect.util.Constants;
import com.imath.connect.util.Security;

import org.json.JSONObject;


public class ChangePassword extends HttpServlet {
    
    @Inject
    Security security;
    
    @Inject private Logger LOG;
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String userName = request.getUserPrincipal().getName();

        String passwordOld = request.getParameter("passwordOld");
        String passwordNew = request.getParameter("passwordNew");
        String passwordNewConf = request.getParameter("passwordNewConf");
        LOG.info(passwordOld + " " + passwordNew);

        // To make sure that both new passwords are equals
        if (!passwordNew.equals(passwordNewConf)) {
            sendCustomResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Different passwords");
            return;
        }

        // To make sure that new passwords are valid
        if (passwordNew.equals("")) {
            sendCustomResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid passwords");
            return;
        }
        
        // To make sure that the old password is correct
        if (!isPasswordCorrect(userName, passwordOld)) {
            sendCustomResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Wrong user or password");
            return;
        }

        
        // Here everything is fine, so we proceed with the password change
        try {
            security.updateSystemPassword(userName, passwordNew);
        } catch (Exception e) {
            e.printStackTrace();
            sendCustomResponse(response, HttpServletResponse.SC_BAD_REQUEST, "New password not valid");
            return;
        }
        
        // Return message if everything was fine;
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        Writer w = response.getWriter();
        w.write("Password changed!");
        w.close();
        return;
    }

    private boolean isPasswordCorrect(String userName, String password) {
        FileReader reader = null;

        try {
            String hexPass = Security.generateHexMd5Password(userName, password);

            // Get property of userName in JBOSS' file
            reader = new FileReader(Constants.USERS_FILE);
            Properties props = new Properties();
            props.load(reader);
            String origProp = props.getProperty(userName);

            // Return if md5 are equals
            return hexPass.toString().equals(origProp);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void sendCustomResponse(HttpServletResponse response, int status, String customMessage)  {
        response.setContentType(MediaType.APPLICATION_JSON);
        response.setStatus(status);
        try {
            Writer w = response.getWriter();
            w.write(customMessage);
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
