package MovieBooking.controller;

import javax.swing.JOptionPane;

import MovieBooking.model.validation;
import MovieBooking.model.User;
import MovieBooking.view.ForgotPasswordView;
import MovieBooking.view.LoginView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * ForgotPasswordController handles forgot password form actions
 * Manages reset password button and back to login button
 */
public class ForgotPasswordController {
    private ForgotPasswordView view;

    public ForgotPasswordController(ForgotPasswordView view) {
        this.view = view;
        initController();
    }

    // Initialize button listeners
    private void initController() {
        view.getResetButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performReset();
            }
        });

        view.getBackButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                backToLogin();
            }
        });

        view.getBackButton().addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                view.getBackButton().setText("<html><u>Back To Login</u></html>");
            }

            public void mouseExited(MouseEvent evt) {
                view.getBackButton().setText("<html>Back To Login</html>");
            }
        });
    }

    // Validate and perform password reset
    private void performReset() {
        String email = view.getEmailText();
        String newPass = view.getNewPasswordText();
        String confirm = view.getConfirmPasswordText();

        if (!validation.validateForgotPassword(email, newPass, confirm, view)) {
            return;
        }

        // Check if user exists and update password
        if (User.updatePassword(email, newPass)) {
            JOptionPane.showMessageDialog(view, "Password reset successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            backToLogin();
        } else {
            JOptionPane.showMessageDialog(view, "Email not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Return to login form
    private void backToLogin() {
        view.dispose();
        LoginView loginView = new LoginView();
        new LoginController(loginView);
        loginView.setVisible(true);
    }
}
