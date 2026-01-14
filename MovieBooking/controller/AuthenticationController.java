
package MovieBooking.controller;

import javax.swing.JOptionPane;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import MovieBooking.model.validation;
import MovieBooking.model.Admin;
import MovieBooking.model.User;
import MovieBooking.view.AuthenticationView;
import MovieBooking.view.MovieBookingView;

/**
 * AuthenticationController handles all authentication with CardLayout
 * Manages login, signup, and forgot password with dynamic resizing
 */
public class AuthenticationController {
    private AuthenticationView view;
    private CardLayout cardLayout;

    // Card names
    private static final String LOGIN_CARD = "card2";
    private static final String SIGNUP_CARD = "card3";
    private static final String FORGOT_CARD = "card4";

    /**
     *
     * @param view
     */
    public AuthenticationController(AuthenticationView view) {
        this.view = view;
        this.cardLayout = (CardLayout) view.getContentPane().getLayout();
        initController();
        showLoginCard(); // Start with login
    }

    private void initController() {
        // Login card buttons
        view.getLoginButton4().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        view.getSignUpButton4().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSignUpCard();
            }
        });

        view.getForgotPasswordButton4().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showForgotCard();
            }
        });
        view.getForgotPasswordButton4().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                view.getForgotPasswordButton4().setText("<html><u>Forgot Password?</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                view.getForgotPasswordButton4().setText("Forgot Password?");
            }
        });

        // SignUp card buttons
        view.getSignupbutton1().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSignUp();
            }
        });

        view.getBackToLoginButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLoginCard();
            }
        });
        view.getBackToLoginButton().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                view.getBackToLoginButton().setText("<html><u>Back To Login</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                view.getBackToLoginButton().setText("Back To Login");
            }
        });

        // Forgot password card buttons
        view.getResetPasswordButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performForgotPassword();
            }
        });

        view.getBackToLoginButton1().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLoginCard();
            }
        });
        view.getBackToLoginButton1().addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                view.getBackToLoginButton1().setText("<html><u>Back To Login</u></html>");
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                view.getBackToLoginButton1().setText("Back To Login");
            }
        });

        setupEnterKeyNavigation();
    }

    private void showLoginCard() {
        cardLayout.show(view.getContentPane(), LOGIN_CARD);
        if (view.getExtendedState() != javax.swing.JFrame.MAXIMIZED_BOTH) {
            view.pack();
            view.setLocationRelativeTo(null);
        }
        view.revalidate();
        view.repaint();
        clearFields();
    }

    private void showSignUpCard() {
        cardLayout.show(view.getContentPane(), SIGNUP_CARD);
        if (view.getExtendedState() != javax.swing.JFrame.MAXIMIZED_BOTH) {
            view.pack();
            view.setLocationRelativeTo(null);
        }
        clearFields();
    }

    private void showForgotCard() {
        cardLayout.show(view.getContentPane(), FORGOT_CARD);
        if (view.getExtendedState() != javax.swing.JFrame.MAXIMIZED_BOTH) {
            view.pack();
            view.setLocationRelativeTo(null);
        }
        view.revalidate();
        view.repaint();
        clearFields();
    }

    private void performLogin() {
        String identifier = view.getEmailTextField4().getText().trim();
        String password = new String(view.getPasswordPasswordField4().getPassword());

        if (!validation.validateLogin(identifier, password, view)) {
            return;
        }

        // Check if admin login
        if (Admin.isAdmin(identifier, password)) {
            JOptionPane.showMessageDialog(view, "Admin Login Successful!\nWelcome Administrator", "Admin Access",
                    JOptionPane.INFORMATION_MESSAGE);
            view.dispose();
            MovieBookingView movieBookingView = new MovieBookingView();
            new AdminController(movieBookingView);
            movieBookingView.setVisible(true);
        } else if (User.authenticateUser(identifier, password)) {
            JOptionPane.showMessageDialog(view, "User Login Successful!\nWelcome " + identifier, "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            view.dispose();
            MovieBookingView movieBookingView = new MovieBookingView();
            new UserController(movieBookingView);
            movieBookingView.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(view, "Invalid credentials!", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSignUp() {
        String username = view.getUsernameTextField().getText().trim();
        String email = view.getEmailTextField5().getText().trim();
        String password = new String(view.getPasswordPasswordField5().getPassword());
        String confirm = new String(view.getConfirmPasswordPasswordField().getPassword());

        if (!validation.validateSignUp(username, email, password, confirm, view)) {
            return;
        }

        // Check if user already exists
        if (User.userExists(username, email)) {
            JOptionPane.showMessageDialog(view, "Username or Email already exists!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save user to file
        if (User.saveUser(username, email, password)) {
            JOptionPane.showMessageDialog(view, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            showLoginCard();
        } else {
            JOptionPane.showMessageDialog(view, "Registration failed! Please try again.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performForgotPassword() {
        String email = view.getEmailTextField6().getText().trim();
        String newPass = new String(view.getNewPasswordPasswordField().getPassword());
        String confirm = new String(view.getConfirmPasswordPasswordField1().getPassword());

        if (!validation.validateForgotPassword(email, newPass, confirm, view)) {
            return;
        }

        // Check if user exists and update password
        if (User.updatePassword(email, newPass)) {
            JOptionPane.showMessageDialog(view, "Password reset successful!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            showLoginCard();
        } else {
            JOptionPane.showMessageDialog(view, "Email not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        // Clear login fields
        view.getEmailTextField4().setText("");
        view.getPasswordPasswordField4().setText("");

        // Clear signup fields
        view.getUsernameTextField().setText("");
        view.getEmailTextField5().setText("");
        view.getPasswordPasswordField5().setText("");
        view.getConfirmPasswordPasswordField().setText("");

        // Clear forgot password fields
        view.getEmailTextField6().setText("");
        view.getNewPasswordPasswordField().setText("");
        view.getConfirmPasswordPasswordField1().setText("");
    }

    private void setupEnterKeyNavigation() {
        view.getEmailTextField4().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    view.getPasswordPasswordField4().requestFocus();
                }
            }
        });

        view.getPasswordPasswordField4().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        });

        // SignUp card - Enter key navigation
        view.getUsernameTextField().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    view.getEmailTextField5().requestFocus();
                }
            }
        });

        view.getEmailTextField5().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    view.getPasswordPasswordField5().requestFocus();
                }
            }
        });

        view.getPasswordPasswordField5().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    view.getConfirmPasswordPasswordField().requestFocus();
                }
            }
        });

        view.getConfirmPasswordPasswordField().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    performSignUp();
                }
            }
        });

        // Forgot Password card - Enter key navigation
        view.getEmailTextField6().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    view.getNewPasswordPasswordField().requestFocus();
                }
            }
        });

        view.getNewPasswordPasswordField().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    view.getConfirmPasswordPasswordField1().requestFocus();
                }
            }
        });

        view.getConfirmPasswordPasswordField1().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    performForgotPassword();
                }
            }
        });
    }
}