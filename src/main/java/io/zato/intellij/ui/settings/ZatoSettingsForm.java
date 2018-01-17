package io.zato.intellij.ui.settings;

import javax.swing.*;

/**
 * @author jansorg
 */
public class ZatoSettingsForm {
    private JTextField nameInput;
    private JTextField addressField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox isDefaultCheckbox;
    private JButton testConnectionButton;
    private JPanel mainPanel;
    private JPanel serverPanel;
    private JPanel editorPanel;
    private JLabel nameLabel;
    private JLabel addressLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel isDefaultLabel;

    public JTextField getNameInput() {
        return nameInput;
    }

    public JTextField getAddressField() {
        return addressField;
    }

    public JTextField getUsernameField() {
        return usernameField;
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public JCheckBox getIsDefaultCheckbox() {
        return isDefaultCheckbox;
    }

    public JButton getTestConnectionButton() {
        return testConnectionButton;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void createUIComponents() {
    }

    public JPanel getServerPanel() {
        return serverPanel;
    }

    public void setupUIComponents() {
    }

    public void resetEditor() {
        nameInput.setText("");
        addressField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        isDefaultCheckbox.setSelected(false);
    }

    public void enableEitorPanel(boolean enabled) {
        editorPanel.setEnabled(enabled);

        nameLabel.setEnabled(false);
        nameInput.setEnabled(enabled);

        addressLabel.setEnabled(false);
        addressField.setEnabled(enabled);

        usernameLabel.setEnabled(false);
        usernameField.setEnabled(enabled);

        passwordLabel.setEnabled(false);
        passwordField.setEnabled(enabled);

        isDefaultLabel.setEnabled(false);
        isDefaultCheckbox.setEnabled(enabled);

        testConnectionButton.setEnabled(enabled);
    }
}
