package io.zato.intellij.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@State(name = "zato", storages = @Storage("zato-servers.xml"))
public class ZatoSettingsService implements PersistentStateComponent<ZatoSettings> {
    private ZatoSettings settings = new ZatoSettings();

    public ZatoSettingsService() {
    }

    public static ZatoSettingsService getInstance() {
        return ServiceManager.getService(ZatoSettingsService.class);
    }

    @NotNull
    @Override
    public synchronized ZatoSettings getState() {
        return settings;
    }

    @Override
    public synchronized void loadState(@Nullable ZatoSettings state) {
        if (state == null) {
            this.settings = new ZatoSettings();
        } else {
            this.settings = state;
        }
    }

    @NonNls
    public synchronized Optional<ZatoServerConfig> getDefaultServer() {
        return settings.getServerConfigurations()
                .stream()
                .filter(ZatoServerConfig::isDefaultServer)
                .findFirst();
    }
}
