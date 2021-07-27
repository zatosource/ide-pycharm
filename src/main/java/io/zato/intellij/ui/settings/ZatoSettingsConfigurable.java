package io.zato.intellij.ui.settings;

import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import io.zato.http.ZatoHttpResponse;
import io.zato.intellij.http.ZatoHttpService;
import io.zato.intellij.settings.ZatoServerConfig;
import io.zato.intellij.settings.ZatoSettings;
import io.zato.intellij.settings.ZatoSettingsService;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration panel for the settings dialog.
 *
 * @author jansorg
 */
@SuppressWarnings("unchecked")
public class ZatoSettingsConfigurable extends BaseConfigurable {
    private JBList serverList;
    private ZatoSettingsForm form;
    private volatile boolean listenerSuspended = false;
    private final DocumentAdapter listener = new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            applyEditor();
        }
    };

    public ZatoSettingsConfigurable() {
    }

    private static CollectionListModel<ZatoServerConfig> model(JBList serverList) {
        return (CollectionListModel<ZatoServerConfig>) (serverList.getModel());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Zato";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        serverList = new JBList();
        serverList.setEmptyText("No servers configured");
        serverList.setModel(new CollectionListModel<ZatoServerConfig>());

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(serverList).disableUpDownActions();
        toolbarDecorator.setAddAction(button -> {
            CollectionListModel<ZatoServerConfig> model = model(serverList);
            boolean hasDefaultServer = model.getItems().stream().anyMatch(ZatoServerConfig::isDefaultServer);

            ZatoServerConfig config = new ZatoServerConfig(true);
            config.setDefaultServer(!hasDefaultServer);
            model.add(config);

            serverList.setSelectedValue(config, true);
            form.getNameInput().requestFocus();
        });

        toolbarDecorator.setRemoveAction(button -> {
            ZatoServerConfig server = getSelectedServer();
            if (server != null) {
                CollectionListModel<ZatoServerConfig> model = model(serverList);
                model.remove(server);

                if (model.getSize() > 0) {
                    serverList.setSelectedValue(model.getElementAt(0), true);
                } else {
                    serverList.removeAll();
                    serverList.repaint();
                    form.resetEditor();
                }
            }
        });

        serverList.getSelectionModel().addListSelectionListener(e -> {
            ZatoServerConfig server = getSelectedServer();
            form.enableEitorPanel(server != null);
            form.getTestConnectionButton().setEnabled(server != null);

            if (server != null) {
                editServer(server);
            }
        });

        serverList.setCellRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
                ZatoServerConfig config = (ZatoServerConfig) value;
                if (config != null && config.getName() != null) {
                    if (config.isDefaultServer()) {
                        append("* ", SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                    }

                    append(config.getName(), config.isDefaultServer()
                            ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
                            : SimpleTextAttributes.REGULAR_ATTRIBUTES);

                    if (config.getUrl() != null) {
                        append(" - ", SimpleTextAttributes.GRAY_ATTRIBUTES);
                        append(config.getUrl(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                }
            }
        });

        form = new ZatoSettingsForm();
        form.getServerPanel().add(serverList);
        form.getServerPanel().add(toolbarDecorator.createPanel());

        installListener(form.getNameInput());
        installListener(form.getAddressField());
        installListener(form.getUsernameField());
        installListener(form.getPasswordField());
        form.getIsDefaultCheckbox().addActionListener(e -> {
            if (form.getIsDefaultCheckbox().isSelected()) {
                //deselect previous default server
                model(serverList).getItems().forEach(zatoServerConfig -> {
                    zatoServerConfig.setDefaultServer(false);
                });
            }
            applyEditor();
        });

        //disable by default
        form.enableEitorPanel(false);

        form.getTestConnectionButton().addActionListener(this::testConnectionAction);

        return form.getMainPanel();
    }

    private void installListener(JTextField field) {
        field.getDocument().addDocumentListener(listener);
    }

    private void applyEditor() {
        if (listenerSuspended) {
            return;
        }

        ZatoServerConfig config = getSelectedServer();
        if (config != null) {
            config.setName(form.getNameInput().getText());
            config.setUsername(form.getUsernameField().getText());
            config.setSafePassword(String.valueOf(form.getPasswordField().getPassword()));
            config.setDefaultServer(form.getIsDefaultCheckbox().isSelected());
            config.setUrl(form.getAddressField().getText());

            serverList.updateUI();
        }
    }

    private void editServer(ZatoServerConfig server) {
        listenerSuspended = true;
        try {
            form.resetEditor();
            form.getNameInput().setText(server.getName());
            form.getAddressField().setText(server.getUrl());
            form.getUsernameField().setText(server.getUsername());
            form.getPasswordField().setText(server.getSafePassword() == null ? "" : server.getSafePassword());
            form.getIsDefaultCheckbox().setSelected(server.getDefaultServer());
        } finally {
            listenerSuspended = false;
        }
    }

    private ZatoServerConfig getSelectedServer() {
        return (ZatoServerConfig) serverList.getSelectedValue();
    }

    @Override
    public boolean isModified() {
        ZatoSettings settings = new ZatoSettings();
        applyTo(settings);

        return !settings.equals(ZatoSettingsService.getInstance().getState());
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return serverList;
    }

    @Override
    public void apply() {
        //save
        applyTo(ZatoSettingsService.getInstance().getState());
    }

    @Override
    public void reset() {
        serverList.clearSelection();
        CollectionListModel<ZatoServerConfig> model = new CollectionListModel<>(new ArrayList<>());

        List<ZatoServerConfig> configs = ZatoSettingsService.getInstance().getState().getServerConfigurations();
        List<ZatoServerConfig> clonedConfigs = configs.stream()
                .map(ZatoServerConfig::copy)
                .collect(Collectors.toList());
        clonedConfigs.forEach(model::add);

        serverList.setModel(model);

        if (!serverList.isEmpty()) {
            //select the default server or first if there's non
            ZatoServerConfig selected = clonedConfigs.stream()
                    .filter(ZatoServerConfig::isDefaultServer)
                    .findFirst()
                    .orElse(clonedConfigs.get(0));
            serverList.setSelectedValue(selected, true);

            editServer(selected);
        }
    }

    @Override
    public void disposeUIResources() {
        form.getNameInput().getDocument().removeDocumentListener(listener);
        form.getAddressField().getDocument().removeDocumentListener(listener);
        form.getUsernameField().getDocument().removeDocumentListener(listener);
        form.getPasswordField().getDocument().removeDocumentListener(listener);
    }

    /**
     * Applies the configuration in the UI form to the given settings instance.
     *
     * @param settings The instance where the settings will be stored
     */
    private void applyTo(ZatoSettings settings) {
        CollectionListModel<ZatoServerConfig> servers = model(serverList);

        List<ZatoServerConfig> serverConfigs = servers.getItems().stream().map(ZatoServerConfig::copy).collect(Collectors.toList());
        settings.set(serverConfigs);
    }

    private void testConnectionAction(ActionEvent e) {
        JComponent focusedComponent = getPreferredFocusedComponent();

        ZatoHttpResponse response;
        try {
            response = ZatoHttpService.getInstance().testConnection(getSelectedServer());
        } catch (IOException ex) {
            Messages.showErrorDialog(focusedComponent,
                    "The communication with the server failed. Please check your settings and the remote server and try again.",
                    "Connection Test");
            return;
        }

        switch (response.getStatusCode()) {
            case HttpStatus.SC_UNAUTHORIZED: {
                Messages.showErrorDialog(focusedComponent,
                        "The authentication failed. Please check your username and password and try again.",
                        "Connection Test");
                break;
            }

            case HttpStatus.SC_NOT_FOUND: {
                Messages.showErrorDialog(focusedComponent,
                        "The resource wasn't found on the server. Please check the URL and try again.",
                        "Connection Test");
                break;
            }

            case HttpStatus.SC_OK: {
                Messages.showInfoMessage(focusedComponent, "The connection was successfully tested.", "Connection Test");
                break;
            }

            default: {
                Messages.showErrorDialog(focusedComponent,
                        "The connection test failed. Please check your settings and the remote server and try again.\nServer message: " + response.getMessage(),
                        "Connection Test");
            }
        }
    }
}
