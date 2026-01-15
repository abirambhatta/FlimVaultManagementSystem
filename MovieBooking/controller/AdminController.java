package MovieBooking.controller;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import MovieBooking.model.Movie;
import MovieBooking.model.User;
import MovieBooking.model.Ticket;
import MovieBooking.view.MovieBookingView;
import MovieBooking.view.AuthenticationView;
import MovieBooking.controller.AuthenticationController;

/**
 *
 * @author lenovo
 */
@SuppressWarnings("unused")
public class AdminController {
    private MovieBookingView view;

    private CardLayout cardLayout;
    private ArrayList<Movie> movieList;
    private List<User> allUsers; // Cached user list

    private static final String MOVIE_FILE = "src/MovieBooking/movies.txt";
    private static final String BOOKING_FILE = "src/MovieBooking/ticket.txt";
    private static final String USER_FILE = "src/MovieBooking/users.txt";

    private String currentPage = "home";
    private static final String HOME_CARD = "card3";
    private static final String MOVIES_CARD = "card2";
    private static final String USERS_CARD = "card4";
    private javax.swing.JButton activeButton;

    /**
     *
     * @param view
     */
    public AdminController(MovieBookingView view) {
        this.view = view;
        this.cardLayout = (CardLayout) view.getContentPanel().getLayout();
        this.movieList = new ArrayList<>();
        this.allUsers = new ArrayList<>();

        initController();
        loadMoviesFromFile();
        loadMovieTable();
        loadRecentBookings();

        // Initialize with default view
        currentPage = "home";
        cardLayout.show(view.getContentPanel(), HOME_CARD);
        activeButton = view.getHomeButton();
        setActiveButton(view.getHomeButton());
        configureTable();
        updateAdminDashboard();
    }

    private void configureTable() {
        view.getMovieTable().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        view.getMovieTable().setRowSelectionAllowed(true);
        view.getMovieTable().setColumnSelectionAllowed(false);
        view.getMovieTable().setDefaultEditor(Object.class, null);

        view.getJTable3().setDefaultEditor(Object.class, null); // Recent bookings table

        view.getAdminUserTable().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        view.getAdminUserTable().setDefaultEditor(Object.class, null);
    }

    private void initController() {
        // Navigation button event handlers
        view.getHomeButton().addActionListener(e -> showHomeCard());
        addButtonHoverListeners(view.getHomeButton());

        view.getMoviesButton().addActionListener(e -> showMoviesCard());
        addButtonHoverListeners(view.getMoviesButton());

        view.getUsersButton().addActionListener(e -> showUsersCard());
        addButtonHoverListeners(view.getUsersButton());

        view.getLogoutButton().addActionListener(e -> performLogout());

        // Movie CRUD operation button handlers
        view.getAddButton().addActionListener(e -> addMovie());
        view.getUpdateButton().addActionListener(e -> updateMovie());
        view.getDeleteButton().addActionListener(e -> deleteMovie());

        // User Management Event Handlers
        view.getSearchButtonUserAdmin().addActionListener(e -> handleSearchUser());
        view.getSortByNameButton().addActionListener(e -> handleSortUser("Name"));
        view.getSortByBookingButton().addActionListener(e -> handleSortUser("Booking"));
        view.getSortByDateButton().addActionListener(e -> handleSortUser("Date"));

        view.getViewUserDetailButton().addActionListener(e -> handleViewUserDetail());
        view.getBlockUnblockUserButton().addActionListener(e -> handleBlockUnblockUser());
        view.getDeleteUserButton().addActionListener(e -> handleDeleteUser());

        view.getUserDetailCloseButton().addActionListener(e -> view.getUserDetailDialog().dispose());
    }

    private void addButtonHoverListeners(javax.swing.JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button != activeButton) {
                    button.setOpaque(true);
                    button.setBackground(new java.awt.Color(255, 200, 200));
                    button.setForeground(new java.awt.Color(229, 9, 20));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button != activeButton) {
                    button.setOpaque(false);
                    button.setForeground(new java.awt.Color(0, 0, 0));
                    button.setBackground(new java.awt.Color(255, 255, 255));
                }
            }
        });
    }

    private void showHomeCard() {
        cardLayout.show(view.getContentPanel(), HOME_CARD);
        currentPage = "home";
        setActiveButton(view.getHomeButton());
        loadRecentBookings();
        updateAdminDashboard();
    }

    private void showMoviesCard() {
        cardLayout.show(view.getContentPanel(), MOVIES_CARD);
        currentPage = "movies";
        setActiveButton(view.getMoviesButton());
    }

    private void showUsersCard() {
        cardLayout.show(view.getContentPanel(), USERS_CARD);
        currentPage = "users";
        setActiveButton(view.getUsersButton());
        refreshUserList(); // Load fresh data
    }

    private void setActiveButton(javax.swing.JButton button) {
        if (activeButton != null) {
            activeButton.setOpaque(false);
            activeButton.setBackground(new java.awt.Color(255, 255, 255));
            activeButton.setForeground(new java.awt.Color(0, 0, 0));
        }
        activeButton = button;
        button.setOpaque(true);
        button.setBackground(new java.awt.Color(229, 9, 20));
        button.setForeground(new java.awt.Color(255, 255, 255));
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to logout?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            view.dispose();
            AuthenticationView authView = new AuthenticationView();
            new AuthenticationController(authView);
            authView.setVisible(true);
        }
    }

    // --- Movie Management Logic (Kept as is) ---
    private void loadMoviesFromFile() {
        movieList.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(MOVIE_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String imagePath = parts.length > 6 ? parts[6] : "";
                    movieList.add(new Movie(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], imagePath));
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error loading movies from file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveMoviesToFile() {
        try (FileWriter writer = new FileWriter(MOVIE_FILE)) {
            for (Movie movie : movieList) {
                writer.write(movie.getName() + "," + movie.getDirector() + "," +
                        movie.getGenre() + "," + movie.getLanguage() + "," +
                        movie.getDuration() + "," + movie.getRating() + "," + movie.getImagePath() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error saving movies to file!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMovieTable() {
        DefaultTableModel model = (DefaultTableModel) view.getMovieTable().getModel();
        model.setRowCount(0);

        for (Movie movie : movieList) {
            Object[] row = {
                    movie.getName(),
                    movie.getDirector(),
                    movie.getGenre(),
                    movie.getLanguage(),
                    movie.getDuration(),
                    movie.getRating()
            };
            model.addRow(row);
        }
    }

    private void addMovie() {
        // ... (Existing implementation for prepare dialog)
        view.getMovieNameField().setText("");
        view.getDirectorField().setText("");
        view.getGenreCombo().setSelectedItem(null);
        view.getLanguageCombo().setSelectedItem(null);
        view.getDurationField().setText("");
        view.getRatingCombo().setSelectedItem(null);
        view.getImageLabel().setText("No Image Selected");
        view.getFileChooser().setSelectedFile(null);
        view.getMovieDialog().setSize(700, 500);
        CardLayout cl = (CardLayout) view.getMovieDialog().getContentPane().getLayout();
        cl.show(view.getMovieDialog().getContentPane(), "card2");

        setupDialogListeners();

        view.getMovieDialog().setLocationRelativeTo(view);
        view.getMovieDialog().setVisible(true);
    }

    private void setupDialogListeners() {
        removeAllListeners(view.getBrowseButton(), view.getSaveButton(), view.getCancelButton());
        addEnterKeyListeners();
        setupBrowseListener();
        setupFileChooserListener();
        setupSaveListener(null);
        setupCancelListener();
    }

    private void removeAllListeners(javax.swing.JButton... buttons) {
        for (javax.swing.JButton btn : buttons) {
            for (ActionListener al : btn.getActionListeners()) {
                btn.removeActionListener(al);
            }
        }
        for (ActionListener al : view.getFileChooser().getActionListeners()) {
            view.getFileChooser().removeActionListener(al);
        }
    }

    private void setupBrowseListener() {
        view.getBrowseButton().addActionListener(e -> {
            CardLayout cl = (CardLayout) view.getMovieDialog().getContentPane().getLayout();
            cl.show(view.getMovieDialog().getContentPane(), "card3");
        });
    }

    private void setupFileChooserListener() {
        view.getFileChooser().addActionListener(e -> {
            if (e.getActionCommand().equals(javax.swing.JFileChooser.APPROVE_SELECTION)) {
                String fileName = view.getFileChooser().getSelectedFile().getName();
                view.getImageLabel().setText(fileName);
            }
            CardLayout cl = (CardLayout) view.getMovieDialog().getContentPane().getLayout();
            cl.show(view.getMovieDialog().getContentPane(), "card2");
        });
    }

    private void setupSaveListener(Movie movieToUpdate) {
        view.getSaveButton().addActionListener(e -> {
            if (validateForm()) {
                String imagePath = movieToUpdate != null ? movieToUpdate.getImagePath() : "";
                if (view.getFileChooser().getSelectedFile() != null) {
                    imagePath = copyImageToPosters(view.getFileChooser().getSelectedFile().getAbsolutePath());
                }

                if (movieToUpdate == null) {
                    Movie movie = new Movie(
                            view.getMovieNameField().getText().trim(),
                            view.getDirectorField().getText().trim(),
                            (String) view.getGenreCombo().getSelectedItem(),
                            (String) view.getLanguageCombo().getSelectedItem(),
                            view.getDurationField().getText().trim(),
                            (String) view.getRatingCombo().getSelectedItem(),
                            imagePath);
                    movieList.add(movie);
                    JOptionPane.showMessageDialog(view, "Movie added successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    movieToUpdate.setName(view.getMovieNameField().getText().trim());
                    movieToUpdate.setDirector(view.getDirectorField().getText().trim());
                    movieToUpdate.setGenre((String) view.getGenreCombo().getSelectedItem());
                    movieToUpdate.setLanguage((String) view.getLanguageCombo().getSelectedItem());
                    movieToUpdate.setDuration(view.getDurationField().getText().trim());
                    movieToUpdate.setRating((String) view.getRatingCombo().getSelectedItem());
                    movieToUpdate.setImagePath(imagePath);
                    JOptionPane.showMessageDialog(view, "Movie updated successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                saveMoviesToFile();
                loadMovieTable();
                view.getMovieDialog().dispose();
            }
        });
    }

    private void setupCancelListener() {
        view.getCancelButton().addActionListener(e -> view.getMovieDialog().dispose());
    }

    private boolean validateForm() {
        if (view.getMovieNameField().getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Movie name is required!", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (view.getDirectorField().getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Director is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (view.getGenreCombo().getSelectedItem() == null) {
            JOptionPane.showMessageDialog(view, "Genre is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (view.getLanguageCombo().getSelectedItem() == null) {
            JOptionPane.showMessageDialog(view, "Language is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (view.getDurationField().getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Duration is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (view.getRatingCombo().getSelectedItem() == null) {
            JOptionPane.showMessageDialog(view, "Rating is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private String copyImageToPosters(String sourcePath) {
        if (sourcePath.isEmpty()) {
            return "";
        }
        try {
            File sourceFile = new File(sourcePath);
            String fileName = sourceFile.getName();
            String destDir = "src/MovieBooking/posters";
            new File(destDir).mkdirs();
            String destPath = destDir + "/" + fileName;
            Files.copy(Paths.get(sourcePath), Paths.get(destPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return destPath;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error copying image file!", "Error", JOptionPane.ERROR_MESSAGE);
            return "";
        }
    }

    private void deleteMovie() {
        int selectedRow = view.getMovieTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a movie to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete this movie?",
                "Delete Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) view.getMovieTable().getModel();
            model.removeRow(selectedRow);
            movieList.remove(selectedRow); // Also remove from list
            saveMoviesToFile();
            JOptionPane.showMessageDialog(view, "Movie deleted successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateMovie() {
        int selectedRow = view.getMovieTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a movie to update!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Movie selectedMovie = movieList.get(selectedRow);

        view.getMovieNameField().setText(selectedMovie.getName());
        view.getDirectorField().setText(selectedMovie.getDirector());
        view.getGenreCombo().setSelectedItem(selectedMovie.getGenre());
        view.getLanguageCombo().setSelectedItem(selectedMovie.getLanguage());
        view.getDurationField().setText(selectedMovie.getDuration());
        view.getRatingCombo().setSelectedItem(selectedMovie.getRating());
        view.getImageLabel().setText(selectedMovie.getImagePath().isEmpty() ? "No Image Selected"
                : new File(selectedMovie.getImagePath()).getName());
        view.getFileChooser().setSelectedFile(null);
        view.getMovieDialog().setSize(700, 500);
        CardLayout cl = (CardLayout) view.getMovieDialog().getContentPane().getLayout();
        cl.show(view.getMovieDialog().getContentPane(), "card2");

        setupUpdateDialogListeners(selectedMovie);

        view.getMovieDialog().setLocationRelativeTo(view);
        view.getMovieDialog().setVisible(true);
    }

    private void setupUpdateDialogListeners(final Movie selectedMovie) {
        removeAllListeners(view.getBrowseButton(), view.getSaveButton(), view.getCancelButton());
        addEnterKeyListeners();
        setupBrowseListener();
        setupFileChooserListener();
        setupSaveListener(selectedMovie);
        setupCancelListener();
    }

    private void addEnterKeyListeners() {
        view.getMovieNameField().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    view.getDirectorField().requestFocus();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });

        // Simplified repeat for brevity as it was in original
        // Ideally we'd use a helper but sticking to structure
        view.getDirectorField().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    view.getGenreCombo().requestFocus();
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        view.getGenreCombo().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    view.getLanguageCombo().requestFocus();
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        view.getLanguageCombo().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    view.getDurationField().requestFocus();
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        view.getDurationField().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    view.getRatingCombo().requestFocus();
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
        view.getRatingCombo().addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    view.getBrowseButton().requestFocus();
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });
    }

    private void loadRecentBookings() {
        DefaultTableModel model = (DefaultTableModel) view.getJTable3().getModel();
        model.setRowCount(0);
        List<String[]> allBookings = Ticket.getAllBookings();

        for (int i = allBookings.size() - 1; i >= 0; i--) {
            String[] b = allBookings.get(i);
            model.addRow(new Object[] { b[0], b[1], b[5] });
        }
    }

    private void updateAdminDashboard() {
        // Total Movies
        view.getAdminTotalMoviesLabel().setText(String.valueOf(movieList.size()));

        // Total Users & Active Users
        int userCount = 0;
        try {
            userCount = (int) Files.lines(Paths.get(USER_FILE)).count();
        } catch (IOException e) {
            System.err.println("Error counting users: " + e.getMessage());
        }
        view.getAdminTotalUsersLabel().setText(String.valueOf(userCount));
        view.getAdminActiveUsersLabel().setText(String.valueOf(userCount));

        // Total Bookings & Revenue
        List<String[]> allBookings = Ticket.getAllBookings();
        int totalRevenue = 0;
        for (String[] parts : allBookings) {
            try {
                totalRevenue += Integer.parseInt(parts[9].trim());
            } catch (NumberFormatException nfe) {
                // Ignore invalid price
            }
        }
        view.getAdminTotalBookingsLabel().setText(String.valueOf(allBookings.size()));
        view.getAdminTotalRevenueLabel().setText(String.valueOf(totalRevenue));
    }

    // --- User Management Logic ---

    /**
     * Reads all users from file and reloads the cache.
     * Use this when underlying data might have changed (e.g., block/delete).
     */
    private void refreshUserList() {
        allUsers = User.getAllUsers();
        displayUserTable(allUsers);
    }

    /**
     * Displays a specific list of users in the table.
     * Useful for search/sort where we don't want to re-read files.
     */
    private void displayUserTable(List<User> usersToDisplay) {
        DefaultTableModel model = (DefaultTableModel) view.getAdminUserTable().getModel();
        model.setRowCount(0);

        // Calculate booking counts for current users efficiently
        Map<String, Integer> bookingCounts = Ticket.getBookingCounts();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (User user : usersToDisplay) {
            int count = bookingCounts.getOrDefault(user.getUsername(), 0);
            // Ensure status is handled gracefully if null (model update)
            String status = user.getStatus() != null ? user.getStatus() : "Active";

            Object[] row = {
                    user.getUsername(),
                    user.getEmail(),
                    user.getRegistrationDate() != null ? user.getRegistrationDate().format(formatter) : "N/A",
                    status,
                    count
            };
            model.addRow(row);
        }
    }

    private void handleSearchUser() {
        String query = view.getSearchBarUserAdmin().getText().trim().toLowerCase();
        if (query.isEmpty()) {
            displayUserTable(allUsers); // Show all if empty
            return;
        }

        // Filter users
        List<User> filtered = allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query) ||
                        u.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toList());

        displayUserTable(filtered);
    }

    private void handleSortUser(String criteria) {
        List<User> sortedList = new ArrayList<>(allUsers);

        // For booking count sort, we need the counts map again unless we cache it in
        // User object.
        // For simplicity, let's just sort by basic fields or re-calculate for booking.

        switch (criteria) {
            case "Name":
                Collections.sort(sortedList, Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
                break;
            case "Date":
                Collections.sort(sortedList, Comparator.comparing(User::getRegistrationDate).reversed()); // Newest
                                                                                                          // first
                break;
            case "Booking":
                // This is expensive to re-calc but correct approach without changing User model
                // too much
                Map<String, Integer> counts = Ticket.getBookingCounts();
                Collections.sort(sortedList, (u1, u2) -> {
                    int c1 = counts.getOrDefault(u1.getUsername(), 0);
                    int c2 = counts.getOrDefault(u2.getUsername(), 0);
                    return Integer.compare(c2, c1); // Descending
                });
                break;
        }
        displayUserTable(sortedList);
    }

    private void handleViewUserDetail() {
        int selectedRow = view.getAdminUserTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a user to view details!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = (String) view.getAdminUserTable().getValueAt(selectedRow, 0); // Name column
        String email = (String) view.getAdminUserTable().getValueAt(selectedRow, 1); // Email column

        // Find user object
        User user = getUserByEmail(email);
        if (user != null) {
            Map<String, Integer> counts = Ticket.getBookingCounts();
            int count = counts.getOrDefault(user.getUsername(), 0);

            // Calculate spent? (Optional/Bonus)
            // int moneySpent = calculateMoneySpent(user.getUsername());

            view.getUserNameLabel().setText("Name: " + user.getUsername());
            view.getUserEmailLabel().setText("Email: " + user.getEmail());
            view.getUserDateLabel().setText("Registered Date: " + user.getRegistrationDate());
            view.getUserDetailBookingCountLabel().setText("Bookings: " + count);
            view.getUserStatusLabel().setText("Status: " + user.getStatus());

            // Real statistics from Ticket model
            String recentMovie = Ticket.getRecentMovieByUser(user.getUsername());
            String moneySpent = Ticket.getTotalSpentByUser(user.getUsername());

            view.getUserRecentWatchLabel().setText("Recent Watch: " + recentMovie);
            view.getUserMoneySpentLabel().setText("Money Spent: $" + moneySpent);

            view.getUserDetailDialog().setSize(500, 500);
            view.getUserDetailDialog().setLocationRelativeTo(view);
            view.getUserDetailDialog().setVisible(true);
        }
    }

    private User getUserByEmail(String email) {
        for (User u : allUsers) {
            if (u.getEmail().equals(email))
                return u;
        }
        return null; // Should not happen if table is sync
    }

    private void handleBlockUnblockUser() {
        int selectedRow = view.getAdminUserTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a user!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String email = (String) view.getAdminUserTable().getValueAt(selectedRow, 1);
        String currentStatus = (String) view.getAdminUserTable().getValueAt(selectedRow, 3);

        String newStatus = "Active".equals(currentStatus) ? "Blocked" : "Active";

        if (User.updateStatus(email, newStatus)) {
            JOptionPane.showMessageDialog(view, "User status updated to " + newStatus, "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshUserList();
        } else {
            JOptionPane.showMessageDialog(view, "Failed to update status!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteUser() {
        int selectedRow = view.getAdminUserTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a user!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String email = (String) view.getAdminUserTable().getValueAt(selectedRow, 1); // Email as unique ID

        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete this user?\nThis cannot be undone.", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (User.deleteUser(email)) {
                JOptionPane.showMessageDialog(view, "User deleted successfully.", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshUserList();
                updateAdminDashboard(); // Update counts
            } else {
                JOptionPane.showMessageDialog(view, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
