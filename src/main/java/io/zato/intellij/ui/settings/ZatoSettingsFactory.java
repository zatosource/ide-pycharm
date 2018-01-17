package io.zato.intellij.ui.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import org.jetbrains.annotations.Nullable;

/**
 * @author jansorg
 */
public class ZatoSettingsFactory extends ConfigurableProvider {
    @Nullable
    @Override
    public Configurable createConfigurable() {
        return new ZatoSettingsConfigurable();
    }
}
