package io.zato.intellij.settings;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.Objects;

/**
 * @author jansorg
 */
@Tag("server")
public class ZatoServerConfig {
    private String name = "";
    private String url;
    private String username;
    private String password;
    private boolean defaultServer = false;

    public ZatoServerConfig() {
    }

    public ZatoServerConfig(String name, String url, String username, String password, boolean isDefault) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.password = password;
        this.defaultServer = isDefault;
    }

    @Override
    public String toString() {
        return "ZatoServerConfig{" +
                "name='" + name + '\'' +
                ", url=" + url +
                ", username='" + username + '\'' +
                ", password='*****'" +
                ", defaultServer=" + defaultServer +
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
        ZatoServerConfig that = (ZatoServerConfig) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(url, that.url) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(defaultServer, that.defaultServer);
    }

    public Boolean isDefaultServer() {
        return defaultServer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, username, password, defaultServer);
    }

    public boolean hasCredentials() {
        return username != null && !username.isEmpty() && password != null;
    }

    /**
     * Creates a copy of this configuration
     *
     * @return
     */
    public ZatoServerConfig copy() {
        return new ZatoServerConfig(name, url, username, password, defaultServer);
    }

    @Attribute("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Attribute("url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUploadUrl() {
        if (url.contains("zato/ide-deploy")) {
            return url;
        }

        url = StringUtil.trimEnd(url, "/");
        return url + "/zato/ide-deploy";
    }

    @Attribute("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Attribute("password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Attribute("default")
    public boolean getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(boolean defaultServer) {
        this.defaultServer = defaultServer;
    }
}
