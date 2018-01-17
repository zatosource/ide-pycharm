package io.zato.intellij.settings;

import com.intellij.util.xmlb.annotations.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author jansorg
 */
public class ZatoSettings {
    private List<ZatoServerConfig> serverConfigurations = new ArrayList<>();

    @Tag("servers")
    public List<ZatoServerConfig> getServerConfigurations() {
        return serverConfigurations;
    }

    public void setServerConfigurations(List<ZatoServerConfig> serverConfigurations) {
        synchronized (this) {
            this.serverConfigurations = serverConfigurations;
        }
    }

    @Override
    public String toString() {
        return "ZatoSettings{" +
                "serverConfigurations=" + serverConfigurations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZatoSettings that = (ZatoSettings) o;
        return Objects.equals(serverConfigurations, that.serverConfigurations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverConfigurations);
    }

    public void add(ZatoServerConfig config) {
        synchronized (this) {
            serverConfigurations.add(config);
        }
    }

    public void set(List<ZatoServerConfig> all) {
        synchronized (this) {
            serverConfigurations.clear();
            serverConfigurations.addAll(all);
        }
    }
}
