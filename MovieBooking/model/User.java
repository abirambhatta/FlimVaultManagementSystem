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
    }

    public User(String username, String email, String password, LocalDate registrationDate) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
    }

    // Save user to file

    /**
     *
     * @param username
     * @param email
     * @param password
     * @return
     */
    public static boolean saveUser(String username, String email, String password) {
        try (FileWriter writer = new FileWriter(USER_FILE, true)) {
            LocalDate registrationDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            writer.write(username + "," + email + "," + password + "," + registrationDate.format(formatter) + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Check if user exists and password matches

    /**
     *
     * @param identifier
     * @param password
     * @return
     */
    public static boolean authenticateUser(String identifier, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                int thirdComma = line.indexOf(',', secondComma + 1);

                if (firstComma != -1 && secondComma != -1) {
                    String username = line.substring(0, firstComma);
                    String email = line.substring(firstComma + 1, secondComma);
                    String userPassword;

                    // Handle both old format (username,email,password) and new format
                    // (username,email,password,date)
                    if (thirdComma != -1) {
                        userPassword = line.substring(secondComma + 1, thirdComma);
                    } else {
                        userPassword = line.substring(secondComma + 1);
                    }

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

    /**
     *
     * @param email
     * @param newPassword
     * @return
     */
    public static boolean updatePassword(String email, String newPassword) {
        List<String> users = new ArrayList<>();
        boolean userFound = false;

        // Read all users
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                int thirdComma = line.indexOf(',', secondComma + 1);

                if (firstComma != -1 && secondComma != -1) {
                    String username = line.substring(0, firstComma);
                    String userEmail = line.substring(firstComma + 1, secondComma);

                    if (userEmail.equals(email)) {
                        // Update password for this user, preserve registration date if exists
                        if (thirdComma != -1) {
                            String registrationDate = line.substring(thirdComma + 1);
                            users.add(username + "," + userEmail + "," + newPassword + "," + registrationDate);
                        } else {
                            users.add(username + "," + userEmail + "," + newPassword);
                        }
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

    /**
     *
     * @param username
     * @param email
     * @return
     */
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

    // Get user details by identifier (username or email)

    /**
     *
     * @param identifier
     * @return
     */
    public static User getUserDetails(String identifier) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                int thirdComma = line.indexOf(',', secondComma + 1);

                if (firstComma != -1 && secondComma != -1) {
                    String username = line.substring(0, firstComma);
                    String email = line.substring(firstComma + 1, secondComma);
                    String password;
                    LocalDate registrationDate = null;

                    // Handle both old format (username,email,password) and new format
                    // (username,email,password,date)
                    if (thirdComma != -1) {
                        password = line.substring(secondComma + 1, thirdComma);
                        String dateStr = line.substring(thirdComma + 1).trim();
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            registrationDate = LocalDate.parse(dateStr, formatter);
                        } catch (Exception e) {
                            registrationDate = LocalDate.now(); // Default to today if parsing fails
                        }
                    } else {
                        password = line.substring(secondComma + 1);
                        registrationDate = LocalDate.now(); // Default to today for old format
                    }

                    if (identifier.equals(username) || identifier.equals(email)) {
                        return new User(username, email, password, registrationDate);
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    // Getters
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

    public static boolean updateUser(String oldEmail, String newUsername, String newEmail, String newPassword) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String username = parts[0];
                    String email = parts[1];
                    String password = parts[2];
                    String regDate = parts.length > 3 ? parts[3] : LocalDate.now().toString();

                    if (email.equals(oldEmail)) {
                        lines.add(String.join(",", newUsername, newEmail, newPassword, regDate));
                        found = true;
                    } else {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    String email = parts[1];
                    if (email.equals(emailToDelete)) {
                        found = true;
                        continue;
                    }
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
