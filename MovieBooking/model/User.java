package MovieBooking.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * User class handles user registration and authentication using file storage
 */
@SuppressWarnings("unused")
public class User {
    private String username;
    private String email;
    private String password;
    private LocalDate registrationDate;
    private String status; // "Active" or "Blocked"

    private static final String USER_FILE = "src/MovieBooking/users.txt";

    /**
     *
     * @param username
     * @param email
     * @param password
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.registrationDate = LocalDate.now();
        this.status = "Active";
    }

    public User(String username, String email, String password, LocalDate registrationDate) {
        this(username, email, password, registrationDate, "Active");
    }

    public User(String username, String email, String password, LocalDate registrationDate, String status) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    // Save user to file
    public static boolean saveUser(String username, String email, String password) {
        try (FileWriter writer = new FileWriter(USER_FILE, true)) {
            LocalDate registrationDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // Format: username,email,password,date,status
            writer.write(
                    username + "," + email + "," + password + "," + registrationDate.format(formatter) + ",Active\n");
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
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String username = parts[0];
                    String email = parts[1];
                    String userPassword = parts[2];

                    if ((identifier.equalsIgnoreCase(username) || identifier.equalsIgnoreCase(email))
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

    /**
     * Checks if a user is blocked.
     * 
     * @param identifier Username or Email
     * @return true if blocked
     */
    public static boolean isUserBlocked(String identifier) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String username = parts[0];
                    String email = parts[1];
                    String status = parts[4];

                    if ((identifier.equalsIgnoreCase(username) || identifier.equalsIgnoreCase(email))
                            && "Blocked".equalsIgnoreCase(status)) {
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
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(email)) {
                    // Update password, preserve other fields
                    String username = parts[0];
                    String date = parts.length > 3 ? parts[3] : LocalDate.now().toString();
                    String status = parts.length > 4 ? parts[4] : "Active";
                    lines.add(username + "," + email + "," + newPassword + "," + date + "," + status);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (found) {
            return writeLines(lines);
        }
        return false;
    }

    public static boolean userExists(String username, String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    if (parts[0].equals(username) || parts[1].equals(email)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static User getUserDetails(String identifier) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String username = parts[0];
                    String email = parts[1];
                    String password = parts[2];

                    if (identifier.equals(username) || identifier.equals(email)) {
                        LocalDate date = LocalDate.now();
                        if (parts.length > 3) {
                            try {
                                date = LocalDate.parse(parts[3], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            } catch (Exception e) {
                            }
                        }
                        String status = parts.length > 4 ? parts[4] : "Active";
                        return new User(username, email, password, date, status);
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public static boolean updateUser(String oldEmail, String newUsername, String newEmail, String newPassword) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(oldEmail)) {
                    String date = parts.length > 3 ? parts[3] : LocalDate.now().toString();
                    String status = parts.length > 4 ? parts[4] : "Active";
                    lines.add(newUsername + "," + newEmail + "," + newPassword + "," + date + "," + status);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (found) {
            return writeLines(lines);
        }
        return false;
    }

    public static boolean updateStatus(String email, String newStatus) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(email)) {
                    // Preserve all fields, update status (index 4) or append it
                    String username = parts[0];
                    String password = parts[2];
                    String date = parts.length > 3 ? parts[3] : LocalDate.now().toString();
                    lines.add(username + "," + email + "," + password + "," + date + "," + newStatus);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (found) {
            return writeLines(lines);
        }
        return false;
    }

    public static boolean deleteUser(String emailToDelete) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    if (parts[1].equals(emailToDelete)) {
                        found = true;
                        continue; // Skip this line
                    }
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (found) {
            return writeLines(lines);
        }
        return false;
    }

    private static boolean writeLines(List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all users from the file
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String username = parts[0];
                    String email = parts[1];
                    String password = parts[2];
                    LocalDate date = LocalDate.now();
                    if (parts.length > 3) {
                        try {
                            date = LocalDate.parse(parts[3], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        } catch (Exception e) {
                        }
                    }
                    String status = parts.length > 4 ? parts[4] : "Active";
                    users.add(new User(username, email, password, date, status));
                }
            }
        } catch (IOException e) {
            return users;
        }
        return users;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public String getStatus() {
        return status;
    }
}
