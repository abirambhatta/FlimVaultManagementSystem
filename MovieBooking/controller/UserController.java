package MovieBooking.controller;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.BoxLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import MovieBooking.model.User;
import MovieBooking.model.Movie;
import MovieBooking.model.Ticket;
import MovieBooking.view.MovieBookingView;
import MovieBooking.view.AuthenticationView;
import MovieBooking.controller.AuthenticationController;

public class UserController {
    private MovieBookingView view;
    private Set<javax.swing.JButton> activeButtons;
    private String loggedInUserIdentifier;
    private ArrayList<Movie> movieList;
    private JPanel dynamicGalleryPanel; // New panel for movies only
    private static final String MOVIE_FILE = "src/MovieBooking/movies.txt";

    private Movie currentMovie;
    private Set<javax.swing.JToggleButton> selectedSeats;
    private javax.swing.JToggleButton selectedTimeBtn;
    private String selectedDate;

    public UserController(MovieBookingView view, String loggedInUserIdentifier) {
        this.view = view;
        this.loggedInUserIdentifier = loggedInUserIdentifier;
        this.activeButtons = new HashSet<>();
        this.selectedSeats = new HashSet<>();
        this.movieList = new ArrayList<>();
        java.awt.CardLayout cl = (java.awt.CardLayout) view.getContentPane().getLayout();
        cl.show(view.getContentPane(), "card3");

        initBrowsePageLayout();
        initUserController();
        initBookingListeners();
        updateWelcomeBar();
        configureTables();
        loadMoviesFromFile(); // Ensure movies are loaded
        populateFilters(true); // Force "all" selection on first load
        showUserHome();
        refreshBookingTables();
        updateUserDashboard();
    }

    private void initUserController() {
        javax.swing.JButton[][] buttonSets = {
                { view.getHomeButton1(), view.getMoviesButton1(), view.getUsersButton1(), view.getUsersButton2(),
                        view.getLogoutButton1() },
                { view.getHomeButton2(), view.getMoviesButton2(), view.getUsersButton3(), view.getUsersButton4(),
                        view.getLogoutButton2() },
                { view.getHomeButton3(), view.getMoviesButton3(), view.getUsersButton5(), view.getUsersButton6(),
                        view.getLogoutButton3() },
                { view.getHomeButton4(), view.getMoviesButton4(), view.getUsersButton7(), view.getUsersButton8(),
                        view.getLogoutButton4() }
        };
        for (javax.swing.JButton[] buttons : buttonSets) {
            setupPageButtons(buttons[0], buttons[1], buttons[2], buttons[3], buttons[4]);
        }
        initSearchAndFilters();
        initMyBookingListeners();
        initProfileButtons();
    }

    private void initProfileButtons() {
        view.getSignupbutton1().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleUpdateProfile();
            }
        });
        view.getSignupbutton2().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleDeleteAccount();
            }
        });
        view.getCloseTicketButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshBookingTables();
            }
        });
        view.getViewTicketButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handleViewTicket();
            }
        });
    }

    private void setupPageButtons(javax.swing.JButton homeBtn, javax.swing.JButton moviesBtn,
            javax.swing.JButton bookingBtn, javax.swing.JButton profileBtn, javax.swing.JButton logoutBtn) {
        homeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showUserHome();
            }
        });
        addButtonHoverListeners(homeBtn);

        moviesBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showBrowseMovies();
            }
        });
        addButtonHoverListeners(moviesBtn);

        bookingBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showMyBooking();
            }
        });
        addButtonHoverListeners(bookingBtn);

        profileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showProfile();
            }
        });
        addButtonHoverListeners(profileBtn);

        logoutBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performLogout();
            }
        });
    }

    private void addButtonHoverListeners(javax.swing.JButton button) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!activeButtons.contains(button)) {
                    button.setOpaque(true);
                    button.setBackground(new java.awt.Color(255, 200, 200));
                    button.setForeground(new java.awt.Color(229, 9, 20));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!activeButtons.contains(button)) {
                    button.setOpaque(false);
                    button.setForeground(new java.awt.Color(0, 0, 0));
                    button.setBackground(new java.awt.Color(255, 255, 255));
                }
            }
        });
    }

    private void showUserHome() {
        showCard("card2", view.getHomeButton1(), view.getHomeButton2(), view.getHomeButton3(), view.getHomeButton4());
    }

    private void showBrowseMovies() {
        showCard("card3", view.getMoviesButton1(), view.getMoviesButton2(), view.getMoviesButton3(),
                view.getMoviesButton4());
        populateMoviePanels();
    }

    private void showMyBooking() {
        showCard("card4", view.getUsersButton1(), view.getUsersButton3(), view.getUsersButton5(),
                view.getUsersButton7());
    }

    private void showProfile() {
        User user = User.getUserDetails(loggedInUserIdentifier);
        if (user != null) {
            view.getUsernameTextField().setText(user.getUsername());
            view.getEmailTextField6().setText(user.getEmail());
            view.getPasswordTextField1().setText(user.getPassword());
        }

        showCard("card5", view.getUsersButton2(), view.getUsersButton4(), view.getUsersButton6(),
                view.getUsersButton8());
    }

    private void handleUpdateProfile() {
        String newUsername = view.getUsernameTextField().getText().trim();
        String newEmail = view.getEmailTextField6().getText().trim();
        String newPassword = view.getPasswordTextField1().getText().trim();

        if (!MovieBooking.model.validation.validateProfileUpdate(newUsername, newEmail, newPassword, view)) {
            return;
        }

        User user = User.getUserDetails(loggedInUserIdentifier);
        if (user == null) {
            JOptionPane.showMessageDialog(view, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view, "Are you sure you want to update your profile?",
                "Update Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (User.updateUser(user.getEmail(), newUsername, newEmail, newPassword)) {
                JOptionPane.showMessageDialog(view, "Profile updated successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loggedInUserIdentifier = newEmail; // Update identifier in case email changed
                updateWelcomeBar();
                showProfile();
            } else {
                JOptionPane.showMessageDialog(view, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteAccount() {
        int confirm = JOptionPane.showConfirmDialog(view,
                "Are you sure you want to delete your account? This action cannot be undone.",
                "Delete Account Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String password = JOptionPane.showInputDialog(view, "Please enter your password to confirm deletion:");
            if (password == null)
                return;

            User user = User.getUserDetails(loggedInUserIdentifier);
            if (user != null && user.getPassword().equals(password)) {
                if (User.deleteUser(user.getEmail())) {
                    JOptionPane.showMessageDialog(view, "Account deleted successfully.", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    view.dispose();
                    AuthenticationView authView = new AuthenticationView();
                    new AuthenticationController(authView);
                    authView.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(view, "Failed to delete account.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCard(String cardName, javax.swing.JButton... buttons) {
        java.awt.CardLayout cl = (java.awt.CardLayout) view.getUserPanel().getLayout();
        cl.show(view.getUserPanel(), cardName);
        resetAllButtons();
        setActiveButtons(buttons);
    }

    private void resetAllButtons() {
        javax.swing.JButton[] allButtons = {
                view.getHomeButton1(), view.getMoviesButton1(), view.getUsersButton1(), view.getUsersButton2(),
                view.getHomeButton2(), view.getMoviesButton2(), view.getUsersButton3(), view.getUsersButton4(),
                view.getHomeButton3(), view.getMoviesButton3(), view.getUsersButton5(), view.getUsersButton6(),
                view.getHomeButton4(), view.getMoviesButton4(), view.getUsersButton7(), view.getUsersButton8()
        };
        for (javax.swing.JButton btn : allButtons) {
            btn.setOpaque(false);
            btn.setForeground(new java.awt.Color(0, 0, 0));
            btn.setBackground(new java.awt.Color(255, 255, 255));
        }
    }

    private void setActiveButtons(javax.swing.JButton... buttons) {
        activeButtons.clear();
        for (javax.swing.JButton btn : buttons) {
            btn.setOpaque(true);
            btn.setBackground(new java.awt.Color(229, 9, 20));
            btn.setForeground(new java.awt.Color(255, 255, 255));
            activeButtons.add(btn);
        }
    }

    private void updateWelcomeBar() {
        // Get user details
        User user = User.getUserDetails(loggedInUserIdentifier);

        if (user != null) {
            // Update home page welcome bar
            view.getWelcomeLabel().setText("Welcome " + user.getUsername());
            view.getEmailLabel().setText(user.getEmail());

            // Update today's date
            LocalDate today = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
            view.getDateLabel().setText("Today's Date: " + today.format(dateFormatter));

            // Update member since date
            if (user.getRegistrationDate() != null) {
                view.getMemberSinceLabel().setText("Member Since: " + user.getRegistrationDate().format(dateFormatter));
            } else {
                view.getMemberSinceLabel().setText("Member Since: " + today.format(dateFormatter));
            }
        }
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

    private void populateMoviePanels() {
        loadMoviesFromFile();
        populateFilters(false); // Restore selection if navigating
        displayMovies(movieList);
    }

    private void displayMovies(ArrayList<Movie> movies) {
        if (dynamicGalleryPanel == null)
            return;

        dynamicGalleryPanel.removeAll();

        for (Movie movie : movies) {
            dynamicGalleryPanel.add(createMoviePanel(movie));
        }

        dynamicGalleryPanel.revalidate();
        dynamicGalleryPanel.repaint();
    }

    private void initBrowsePageLayout() {
        JPanel mainPanel = view.getMoviePanelContainer(); // This is jPanel7

        // Save references to existing components from jPanel7
        java.awt.Component[] components = mainPanel.getComponents();

        // Find specific components we want to keep in the header
        JLabel browseTitle = null;
        javax.swing.JTextField sBar = view.getSearchBar();
        JButton sBtn = view.getSearchButton();
        JLabel filterLabel1 = null; // genre label
        JLabel filterLabel2 = null; // language label
        javax.swing.JComboBox<String> fGenre = view.getFilterGenre();
        javax.swing.JComboBox<String> fLang = view.getFilterLanguage();
        JLabel recentLabel = null;

        for (java.awt.Component comp : components) {
            if (comp instanceof JLabel) {
                JLabel lbl = (JLabel) comp;
                String text = lbl.getText() != null ? lbl.getText() : "";
                if (text.contains("Browse Movie"))
                    browseTitle = lbl;
                else if (text.contains("Genre:"))
                    filterLabel1 = lbl;
                else if (text.contains("Language:"))
                    filterLabel2 = lbl;
                else if (text.contains("Now Showing"))
                    recentLabel = lbl;
            }
        }

        // If we didn't find the label by text, try to find it by name
        if (recentLabel == null) {
            for (java.awt.Component comp : components) {
                if (comp.getName() != null && comp.getName().equals("RecentBooking4")) {
                    recentLabel = (JLabel) comp;
                    break;
                }
            }
        }

        // Clear the main panel to reset layout
        mainPanel.removeAll();
        mainPanel.setLayout(new java.awt.BorderLayout(0, 0));

        // Create Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new java.awt.Color(249, 249, 249));
        headerPanel.setLayout(new javax.swing.BoxLayout(headerPanel, javax.swing.BoxLayout.Y_AXIS));
        headerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 26, 10, 0));

        // Row 1: Title
        if (browseTitle == null) {
            browseTitle = new JLabel("Browse Movies");
            browseTitle.setFont(new java.awt.Font("Segoe UI", 1, 24));
        }
        browseTitle.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        headerPanel.add(browseTitle);
        headerPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 15)));

        // Row 2: Search Bar
        JPanel searchRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        searchRow.setOpaque(false);
        sBar.setPreferredSize(new java.awt.Dimension(300, 32));
        searchRow.add(sBar);
        searchRow.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(18, 0)));
        searchRow.add(sBtn);
        searchRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        headerPanel.add(searchRow);
        headerPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 15)));

        // Row 3: "Now Showing" Label (Above filters as requested)
        if (recentLabel == null) {
            recentLabel = new JLabel("Now Showing");
            recentLabel.setFont(new java.awt.Font("Segoe UI", 1, 18));
        } else {
            recentLabel.setText("Now Showing");
        }
        recentLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        headerPanel.add(recentLabel);
        headerPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 10)));

        // Row 4: Filters
        JPanel filterRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        filterRow.setOpaque(false);
        if (filterLabel1 == null)
            filterLabel1 = new JLabel("genre: ");
        else
            filterLabel1.setText("genre: "); // Lowercase label

        if (filterLabel2 == null)
            filterLabel2 = new JLabel("language: ");
        else
            filterLabel2.setText("language: "); // Lowercase label

        filterRow.add(filterLabel1);
        filterRow.add(fGenre);
        filterRow.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(18, 0)));
        filterRow.add(filterLabel2);
        filterRow.add(fLang);
        filterRow.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        headerPanel.add(filterRow);
        headerPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 20)));

        // Gallery Panel
        dynamicGalleryPanel = new JPanel();
        dynamicGalleryPanel.setBackground(new java.awt.Color(249, 249, 249));
        // Exactly 2 columns
        dynamicGalleryPanel.setLayout(new java.awt.GridLayout(0, 2, 30, 30));
        dynamicGalleryPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 26, 0, 26));

        mainPanel.add(headerPanel, java.awt.BorderLayout.NORTH);
        mainPanel.add(dynamicGalleryPanel, java.awt.BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private boolean isPopulatingFilters = false;

    private void populateFilters(boolean forceAll) {
        isPopulatingFilters = true;

        String currentGenre = null;
        String currentLang = null;

        if (!forceAll) {
            currentGenre = (String) view.getFilterGenre().getSelectedItem();
            currentLang = (String) view.getFilterLanguage().getSelectedItem();
        }

        view.getFilterGenre().removeAllItems();
        view.getFilterLanguage().removeAllItems();

        view.getFilterGenre().addItem("all genre");
        view.getFilterLanguage().addItem("all language");

        TreeSet<String> genres = new TreeSet<>();
        TreeSet<String> languages = new TreeSet<>();

        for (Movie m : movieList) {
            if (m.getGenre() != null && !m.getGenre().isEmpty()) {
                genres.add(m.getGenre());
            }
            if (m.getLanguage() != null && !m.getLanguage().isEmpty()) {
                languages.add(m.getLanguage());
            }
        }

        for (String g : genres)
            view.getFilterGenre().addItem(g);
        for (String l : languages)
            view.getFilterLanguage().addItem(l);

        if (currentGenre != null && !forceAll)
            view.getFilterGenre().setSelectedItem(currentGenre);
        else
            view.getFilterGenre().setSelectedItem("all genre");

        if (currentLang != null && !forceAll)
            view.getFilterLanguage().setSelectedItem(currentLang);
        else
            view.getFilterLanguage().setSelectedItem("all language");

        isPopulatingFilters = false;
    }

    private void initSearchAndFilters() {
        // Search triggers only on button click
        view.getSearchButton().addActionListener(e -> handleFiltering());

        // Dropdown filters remain instant
        view.getFilterGenre().addActionListener(e -> {
            if (!isPopulatingFilters)
                handleFiltering();
        });

        view.getFilterLanguage().addActionListener(e -> {
            if (!isPopulatingFilters)
                handleFiltering();
        });
    }

    private void handleFiltering() {
        String searchText = view.getSearchBar().getText().toLowerCase();
        String selectedGenre = (String) view.getFilterGenre().getSelectedItem();
        String selectedLang = (String) view.getFilterLanguage().getSelectedItem();

        ArrayList<Movie> filteredList = new ArrayList<>();
        for (Movie movie : movieList) {
            boolean matchesSearch = movie.getName().toLowerCase().contains(searchText);
            boolean matchesGenre = selectedGenre == null || selectedGenre.equals("all genre")
                    || movie.getGenre().equals(selectedGenre);
            boolean matchesLang = selectedLang == null || selectedLang.equals("all language")
                    || movie.getLanguage().equals(selectedLang);

            if (matchesSearch && matchesGenre && matchesLang) {
                filteredList.add(movie);
            }
        }
        displayMovies(filteredList);
    }

    private javax.swing.JPanel createMoviePanel(Movie movie) {
        // Theme Colors
        java.awt.Color themeBackground = new java.awt.Color(249, 249, 249);
        java.awt.Color themeRed = new java.awt.Color(229, 9, 20);

        // Outer Panel
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(themeBackground);
        // Using theme red for border
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(themeRed));
        panel.setLayout(new java.awt.BorderLayout());
        panel.setPreferredSize(new java.awt.Dimension(250, 420));

        // Poster Section
        javax.swing.JPanel posterPanel = new javax.swing.JPanel();
        posterPanel.setBackground(java.awt.Color.WHITE); // Keep poster background white for contrast
        posterPanel.setPreferredSize(new java.awt.Dimension(250, 201));
        posterPanel.setLayout(new java.awt.BorderLayout());
        // Red separator line
        posterPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, themeRed));

        if (movie.getImagePath() != null && !movie.getImagePath().isEmpty()) {
            try {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(movie.getImagePath());
                // Scale image to fit poster area
                java.awt.Image img = icon.getImage();
                java.awt.Image scaledImg = img.getScaledInstance(250, 201, java.awt.Image.SCALE_SMOOTH);
                posterPanel.add(new javax.swing.JLabel(new javax.swing.ImageIcon(scaledImg)),
                        java.awt.BorderLayout.CENTER);
            } catch (Exception e) {
                posterPanel.add(new javax.swing.JLabel("No Image", javax.swing.SwingConstants.CENTER));
            }
        } else {
            posterPanel.add(new javax.swing.JLabel("No Image", javax.swing.SwingConstants.CENTER));
        }

        // Detail Section
        javax.swing.JPanel detailPanel = new javax.swing.JPanel();
        // Light red tint for the background to match theme
        detailPanel.setBackground(new java.awt.Color(255, 245, 245));
        detailPanel.setLayout(new javax.swing.BoxLayout(detailPanel, javax.swing.BoxLayout.Y_AXIS));
        detailPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 19, 10, 19));

        javax.swing.JLabel nameLabel = new javax.swing.JLabel(movie.getName());
        nameLabel.setForeground(themeRed); // Red title
        nameLabel.setFont(new java.awt.Font("Segoe UI", 1, 14));

        javax.swing.JLabel genreLabel = new javax.swing.JLabel("Genre: " + movie.getGenre());
        genreLabel.setForeground(java.awt.Color.DARK_GRAY);

        javax.swing.JLabel timeLabel = new javax.swing.JLabel("Time: " + movie.getDuration());
        timeLabel.setForeground(java.awt.Color.DARK_GRAY);

        javax.swing.JLabel langLabel = new javax.swing.JLabel("Language: " + movie.getLanguage());
        langLabel.setForeground(java.awt.Color.DARK_GRAY);

        javax.swing.JButton bookBtn = new javax.swing.JButton("Book Now");
        bookBtn.setBackground(themeRed);
        bookBtn.setForeground(java.awt.Color.WHITE);
        bookBtn.setFont(new java.awt.Font("Segoe UI", 1, 12));
        bookBtn.setPreferredSize(new java.awt.Dimension(100, 30));
        bookBtn.setFocusPainted(false);
        bookBtn.addActionListener(e -> {
            this.currentMovie = movie;
            showBookingDialog();
        });

        // Container to center the button horizontally
        javax.swing.JPanel buttonContainer = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
        buttonContainer.setOpaque(false); // Transparent to show detailPanel color
        buttonContainer.add(bookBtn);

        // Add spacing between labels
        detailPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 5)));
        detailPanel.add(nameLabel);
        detailPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 8)));
        detailPanel.add(genreLabel);
        detailPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 3)));
        detailPanel.add(timeLabel);
        detailPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 3)));
        detailPanel.add(langLabel);
        // Larger space before the button
        detailPanel.add(javax.swing.Box.createRigidArea(new java.awt.Dimension(0, 20)));
        detailPanel.add(buttonContainer);

        panel.add(posterPanel, java.awt.BorderLayout.NORTH);
        panel.add(detailPanel, java.awt.BorderLayout.CENTER);

        return panel;
    }

    private void showBookingDialog() {
        selectedSeats.clear();
        selectedTimeBtn = null;
        resetBookingUI();
        view.getPriceLabel().setText("0");
        java.awt.CardLayout cl = (java.awt.CardLayout) view.getBookingDialog().getContentPane().getLayout();
        cl.show(view.getBookingDialog().getContentPane(), "card2");
        view.getBookingDialog().pack();
        view.getBookingDialog().setLocationRelativeTo(view);
        view.getMovieNameLabel().setText(currentMovie.getName());
        view.getBookingDialog().setVisible(true);
    }

    private void resetBookingUI() {
        javax.swing.JToggleButton[] allSeats = {
                view.getSeatA1(), view.getSeatA2(), view.getSeatA3(), view.getSeatA4(), view.getSeatA5(),
                view.getSeatA6(),
                view.getSeatA7(), view.getSeatA8(),
                view.getSeatB1(), view.getSeatB2(), view.getSeatB3(), view.getSeatB4(), view.getSeatB5(),
                view.getSeatB6(),
                view.getSeatB7(), view.getSeatB8(),
                view.getSeatC1(), view.getSeatC2(), view.getSeatC3(), view.getSeatC4(), view.getSeatC5(),
                view.getSeatC6(),
                view.getSeatC7(), view.getSeatC8(),
                view.getSeatD1(), view.getSeatD2(), view.getSeatD3(), view.getSeatD4(), view.getSeatD5(),
                view.getSeatD6(),
                view.getSeatD7(), view.getSeatD8()
        };
        for (javax.swing.JToggleButton seat : allSeats) {
            seat.setSelected(false);
            seat.setBackground(null);
        }
        javax.swing.JToggleButton[] timeBtns = { view.getTimeBtn1(), view.getTimeBtn2(), view.getTimeBtn3(),
                view.getTimeBtn4() };
        for (javax.swing.JToggleButton btn : timeBtns) {
            btn.setSelected(false);
            btn.setBackground(null);
        }
        view.getTodayButton().setSelected(false);
        view.getTodayButton().setBackground(null);
        view.getTomorrowButton().setSelected(false);
        view.getTomorrowButton().setBackground(null);
        selectedDate = null;
    }

    private void initBookingListeners() {
        javax.swing.JToggleButton[] allSeats = {
                view.getSeatA1(), view.getSeatA2(), view.getSeatA3(), view.getSeatA4(), view.getSeatA5(),
                view.getSeatA6(),
                view.getSeatA7(), view.getSeatA8(),
                view.getSeatB1(), view.getSeatB2(), view.getSeatB3(), view.getSeatB4(), view.getSeatB5(),
                view.getSeatB6(),
                view.getSeatB7(), view.getSeatB8(),
                view.getSeatC1(), view.getSeatC2(), view.getSeatC3(), view.getSeatC4(), view.getSeatC5(),
                view.getSeatC6(),
                view.getSeatC7(), view.getSeatC8(),
                view.getSeatD1(), view.getSeatD2(), view.getSeatD3(), view.getSeatD4(), view.getSeatD5(),
                view.getSeatD6(),
                view.getSeatD7(), view.getSeatD8()
        };
        for (javax.swing.JToggleButton seat : allSeats) {
            seat.addActionListener(e -> handleSeatSelection(seat));
        }

        javax.swing.JToggleButton[] timeBtns = { view.getTimeBtn1(), view.getTimeBtn2(), view.getTimeBtn3(),
                view.getTimeBtn4() };
        for (javax.swing.JToggleButton btn : timeBtns) {
            btn.addActionListener(e -> handleTimeSelection(btn));
        }

        view.getSeatTypeCombo().addActionListener(e -> updateTotalPrice());

        view.getCancelBookingButton().addActionListener(e -> view.getBookingDialog().setVisible(false));

        // Date selection listeners
        view.getTodayButton().addActionListener(e -> handleDateSelection(view.getTodayButton(), "Today"));
        view.getTomorrowButton().addActionListener(e -> handleDateSelection(view.getTomorrowButton(), "Tomorrow"));

        view.getGenerateTicketButton().addActionListener(e -> {
            if (selectedSeats.isEmpty() || selectedTimeBtn == null || selectedDate == null) {
                JOptionPane.showMessageDialog(view.getBookingDialog(),
                        "Please select seats, a time, and a date.",
                        "Incomplete Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            populateTicket();
            // Transition to ticket view
            java.awt.CardLayout cl = (java.awt.CardLayout) view.getBookingDialog().getContentPane().getLayout();
            cl.show(view.getBookingDialog().getContentPane(), "card3");
        });
    }

    private void handleDateSelection(javax.swing.JToggleButton btn, String dateType) {
        boolean isSelected = btn.isSelected();

        view.getTodayButton().setSelected(false);
        view.getTodayButton().setBackground(null);
        view.getTomorrowButton().setSelected(false);
        view.getTomorrowButton().setBackground(null);

        if (isSelected) {
            btn.setSelected(true);
            btn.setBackground(new java.awt.Color(153, 255, 153)); // Light green for selected date

            LocalDate date = LocalDate.now();
            if (dateType.equals("Tomorrow")) {
                date = date.plusDays(1);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            selectedDate = date.format(formatter);
        } else {
            selectedDate = null;
        }
    }

    private void populateTicket() {
        view.getMovieNameLabel().setText("Movie: " + currentMovie.getName());
        view.getTicketDateLabel().setText("Date: " + selectedDate);
        view.getTicketTimeLabel().setText("Time: " + selectedTimeBtn.getText());

        StringBuilder seatsStr = new StringBuilder("Seat: ");
        TreeSet<String> seatNames = new TreeSet<>();
        for (javax.swing.JToggleButton seat : selectedSeats) {
            seatNames.add(seat.getText());
        }
        seatsStr.append(String.join(", ", seatNames));
        view.getTicketSeatLabel().setText(seatsStr.toString());

        view.getTicketSeatTypeLabel().setText("Seat Type: " + view.getSeatTypeCombo().getSelectedItem());
        view.getTicketPriceLabel().setText("Price: $" + view.getPriceLabel().getText());

        saveBooking();
        refreshBookingTables();
    }

    private void saveBooking() {
        StringBuilder seatsStr = new StringBuilder();
        TreeSet<String> seatNames = new TreeSet<>();
        for (javax.swing.JToggleButton seat : selectedSeats) {
            seatNames.add(seat.getText());
        }
        seatsStr.append(String.join(", ", seatNames));

        String bookingData = String.join(";",
                loggedInUserIdentifier,
                currentMovie.getName(),
                currentMovie.getGenre(),
                currentMovie.getLanguage(),
                currentMovie.getRating(),
                selectedDate,
                selectedTimeBtn.getText(),
                seatsStr.toString(),
                (String) view.getSeatTypeCombo().getSelectedItem(),
                view.getPriceLabel().getText());

        if (Ticket.saveBooking(bookingData)) {
            // Message removed as per user request
        } else {
            JOptionPane.showMessageDialog(view, "Error saving booking!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshBookingTables() {
        List<String[]> allBookings = getAllUserBookings();
        populateMyBookingTable(new ArrayList<>(allBookings));

        javax.swing.table.DefaultTableModel homeBookingModel = (javax.swing.table.DefaultTableModel) view.getJTable1()
                .getModel();
        homeBookingModel.setRowCount(0);

        // Fill Home Recent Bookings (top 3)
        int count = 0;
        for (int i = allBookings.size() - 1; i >= 0 && count < 3; i--) {
            String[] b = allBookings.get(i);
            // Table columns: Name, Genre, Language, Rated
            homeBookingModel.addRow(new Object[] { b[1], b[2], b[3], b[4] });
            count++;
        }
        updateUserDashboard();
    }

    private List<String[]> getAllUserBookings() {
        return Ticket.getBookingsForUser(loggedInUserIdentifier);
    }

    private void populateMyBookingTable(ArrayList<String[]> bookings) {
        javax.swing.table.DefaultTableModel userBookingModel = (javax.swing.table.DefaultTableModel) view.getJTable4()
                .getModel();
        userBookingModel.setRowCount(0);

        // Fill My Booking Table
        for (int i = bookings.size() - 1; i >= 0; i--) {
            String[] b = bookings.get(i);
            // Table columns: Name, Genre, Language, Rated, Date
            userBookingModel.addRow(new Object[] { b[1], b[2], b[3], b[4], b[5] });
        }
    }

    private void initMyBookingListeners() {
        view.getSearchButtonForMyBooking().addActionListener(e -> handleSearchMyBooking());
        view.getSortByMovieNameButtonMyBooking().addActionListener(e -> handleSortMyBookingByName());
        view.getSortByDateButtonMyBooking().addActionListener(e -> handleSortMyBookingByDate());
    }

    private void handleSearchMyBooking() {
        String query = view.getSearchBarForMyBooking().getText().toLowerCase().trim();
        List<String[]> all = getAllUserBookings();
        if (query.isEmpty()) {
            populateMyBookingTable(new ArrayList<>(all));
            return;
        }

        ArrayList<String[]> filtered = new ArrayList<>();
        for (String[] b : all) {
            if (b[1].toLowerCase().contains(query)) {
                filtered.add(b);
            }
        }
        populateMyBookingTable(filtered);
    }

    private boolean isSortNameAsc = true;

    private void handleSortMyBookingByName() {
        List<String[]> bookings = new ArrayList<>(getAllUserBookings());
        bookings.sort((a, b) -> {
            int res = a[1].compareToIgnoreCase(b[1]);
            return isSortNameAsc ? res : -res;
        });
        isSortNameAsc = !isSortNameAsc;
        populateMyBookingTable(new ArrayList<>(bookings));
    }

    private boolean isSortDateAsc = false;

    private void handleSortMyBookingByDate() {
        List<String[]> bookings = new ArrayList<>(getAllUserBookings());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        bookings.sort((a, b) -> {
            try {
                LocalDate d1 = LocalDate.parse(a[5].trim(), formatter);
                LocalDate d2 = LocalDate.parse(b[5].trim(), formatter);
                int res = d1.compareTo(d2);
                return isSortDateAsc ? res : -res;
            } catch (Exception e) {
                return 0;
            }
        });
        isSortDateAsc = !isSortDateAsc;
        populateMyBookingTable(new ArrayList<>(bookings));
    }

    private void handleSeatSelection(javax.swing.JToggleButton seat) {
        if (seat.isSelected()) {
            selectedSeats.add(seat);
            seat.setBackground(new java.awt.Color(153, 255, 153)); // Light green for selected
        } else {
            selectedSeats.remove(seat);
            seat.setBackground(null);
        }
        updateTotalPrice();
    }

    private void handleTimeSelection(javax.swing.JToggleButton btn) {
        if (selectedTimeBtn != null) {
            selectedTimeBtn.setSelected(false);
            selectedTimeBtn.setBackground(null);
        }
        if (btn.isSelected()) {
            selectedTimeBtn = btn;
            btn.setBackground(new java.awt.Color(255, 204, 153)); // Light orange for selected time
        } else {
            selectedTimeBtn = null;
        }
    }

    private void updateTotalPrice() {
        int basePrice = 0;
        String seatType = (String) view.getSeatTypeCombo().getSelectedItem();
        if ("Standard Seat".equals(seatType))
            basePrice = 185;
        else if ("Reclinear Seat".equals(seatType))
            basePrice = 225;
        else if ("Luxury Seat".equals(seatType))
            basePrice = 300;

        int total = selectedSeats.size() * basePrice;
        view.getPriceLabel().setText(String.valueOf(total));
    }

    private void configureTables() {
        view.getJTable1().setDefaultEditor(Object.class, null);
        view.getJTable4().setDefaultEditor(Object.class, null);
    }

    private void handleViewTicket() {
        int selectedRow = view.getJTable4().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "Please select a booking to view!", "No Selection",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<String[]> userBookings = Ticket.getBookingsForUser(loggedInUserIdentifier);
        if (userBookings.isEmpty()) {
            JOptionPane.showMessageDialog(view, "No bookings found!", "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        int dataIndex = userBookings.size() - 1 - selectedRow;
        if (dataIndex < 0 || dataIndex >= userBookings.size()) {
            return;
        }

        String[] b = userBookings.get(dataIndex);

        // Populate labels
        view.getMovieNameLabel().setText("Movie: " + b[1]);
        view.getTicketDateLabel().setText("Date: " + b[5]);
        view.getTicketTimeLabel().setText("Time: " + b[6]);
        view.getTicketSeatLabel().setText("Seat: " + b[7]);
        view.getTicketSeatTypeLabel().setText("Seat Type: " + b[8]);
        view.getTicketPriceLabel().setText("Price: " + b[9]);

        // Show dialog and transition to ticket card (card3)
        java.awt.CardLayout cl = (java.awt.CardLayout) view.getBookingDialog().getContentPane().getLayout();
        cl.show(view.getBookingDialog().getContentPane(), "card3");
        view.getBookingDialog().pack();
        view.getBookingDialog().setLocationRelativeTo(view);
        view.getBookingDialog().setVisible(true);
    }

    private void updateUserDashboard() {
        // Total Movies
        view.getUserTotalMoviesLabel().setText(String.valueOf(movieList.size()));

        // Total Bookings and Money Spent for User
        int bookingCount = 0;
        double totalSpent = 0.0;

        List<String[]> userBookings = Ticket.getBookingsForUser(loggedInUserIdentifier);
        for (String[] parts : userBookings) {
            bookingCount++;
            try {
                double price = Double.parseDouble(parts[9].trim());
                totalSpent += price;
            } catch (NumberFormatException e) {
                // Ignore invalid price
            }
        }

        view.getUserBookingCountLabel().setText(String.valueOf(bookingCount));
        view.getMoneySpentOnTicketLabel().setText(String.valueOf((int) totalSpent));
    }
}
