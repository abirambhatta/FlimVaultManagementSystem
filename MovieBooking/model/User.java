package MovieBooking.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User class handles user registration and authentication using file storage
 */
public class User {
    private String username;
    private String email;
    private String password;
    
    private static final String USER_FILE = "src/MovieBooking/users.txt";
    
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // Save user to file
    public static boolean saveUser(String username, String email, String password) {
        try (FileWriter writer = new FileWriter(USER_FILE, true)) {
            writer.write(username + "," + email + "," + password + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    // Check if user exists and password matches
    public static boolean authenticateUser(String identifier, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                
                if (firstComma != -1 && secondComma != -1) {
                    String username = line.substring(0, firstComma);
                    String email = line.substring(firstComma + 1, secondComma);
                    String userPassword = line.substring(secondComma + 1);
                    
                    if ((identifier.equals(username) || identifier.equals(email)) 
                        && password.equals(userPassword)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
    
    // Update user password in file
    public static boolean updatePassword(String email, String newPassword) {
        List<String> users = new ArrayList<>();
        boolean userFound = false;
        
        // Read all users
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                
                if (firstComma != -1 && secondComma != -1) {
                    String username = line.substring(0, firstComma);
                    String userEmail = line.substring(firstComma + 1, secondComma);
                    
                    if (userEmail.equals(email)) {
                        // Update password for this user
                        users.add(username + "," + userEmail + "," + newPassword);
                        userFound = true;
                    } else {
                        users.add(line);
                    }
                } else {
                    users.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }
        
        if (!userFound) {
            return false;
        }
        
        // Write all users back to file
        try (FileWriter writer = new FileWriter(USER_FILE)) {
            for (String user : users) {
                writer.write(user + "\n");
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    // Check if username or email already exists
    public static boolean userExists(String username, String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                
                if (firstComma != -1 && secondComma != -1) {
                    String fileUsername = line.substring(0, firstComma);
                    String fileEmail = line.substring(firstComma + 1, secondComma);
                    
                    if (fileUsername.equals(username) || fileEmail.equals(email)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }
}