package io.zato.intellij.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.TestOnly;

import java.util.Objects;
import java.util.UUID;

/**
 * @author jansorg
 */
@Tag("server")
public class ZatoServerConfig {
    private String name = "";
    private String url;
    private String username;
    private boolean defaultServer = false;
    // UUID to define the storage key in the password safe
    // XML serialization doesn't seem to allow this out of the box
    private String uuid;
    // not persisted in the settings,
    // but still with getter and setter to support settings of previous versions
    private transient String oldPassword;
    // matches the value stored in the password safe
    private transient String safePassword;

    public ZatoServerConfig() {
        this(false);
    }

    public ZatoServerConfig(boolean withUUID) {
        if (withUUID) {
            this.uuid = UUID.randomUUID().toString();
        }
    }

    @TestOnly
    public ZatoServerConfig(String name, String url, String username, String password, boolean isDefault) {
        this(name, url, username, password, isDefault, UUID.randomUUID().toString());
    }

    private ZatoServerConfig(String name, String url, String username, String password, boolean isDefault, String uuid) {
        this.name = name;
        this.url = url;
        this.username = username;
        this.defaultServer = isDefault;
        this.uuid = uuid;

        this.safePassword = password;
    }

    @Override
    public String toString() {
        return "ZatoServerConfig{" +
               "name='" + name + '\'' +
               ", url=" + url +
               ", username='" + username + '\'' +
               ", password='*****'" +
               ", defaultServer=" + defaultServer +
               ", uuid=" + uuid +
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
        ZatoServerConfig that = (ZatoServerConfig)o;
        return Objects.equals(name, that.name) &&
               Objects.equals(url, that.url) &&
               Objects.equals(username, that.username) &&
               Objects.equals(safePassword, that.safePassword) &&
               Objects.equals(defaultServer, that.defaultServer) &&
               Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url, username, safePassword, defaultServer, uuid);
    }

    public Boolean isDefaultServer() {
        return defaultServer;
    }

    public boolean hasCredentials() {
        return username != null && !username.isEmpty() && safePassword != null;
    }

    /**
     * Creates a copy of this configuration
     *
     * @return A new instance with the same settings
     */
    public ZatoServerConfig copy() {
        return new ZatoServerConfig(name, url, username, safePassword, defaultServer, uuid);
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
        if (url.contains("/ide-deploy")) {
            return url;
        }

        url = StringUtil.trimEnd(url, "/");
        return url + "/ide-deploy";
    }

    @Attribute("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @SuppressWarnings("unused")
    @Attribute("password")
    @Deprecated
    public String getOldPassword() {
        return oldPassword;
    }

    @SuppressWarnings("unused")
    @Deprecated
    public void setOldPassword(String password) {
        this.oldPassword = password;
    }

    @Transient
    public String getSafePassword() {
        return safePassword;
    }

    @Transient
    public void setSafePassword(String safePassword) {
        this.safePassword = safePassword;
    }

    @Attribute("default")
    public boolean getDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(boolean defaultServer) {
        this.defaultServer = defaultServer;
    }

    @SuppressWarnings("unused")
    @Attribute("uuid")
    public String getUuid() {
        return uuid;
    }

    @SuppressWarnings("unused")
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Reads the password from the password safe.
     * It restores the password of the old settings, where it was stored in the XML data.
     */
    void restoreSafePassword() {
        // allow running in headless tests
        if (ApplicationManager.getApplication() != null) {
            if (this.uuid == null) {
                this.uuid = UUID.randomUUID().toString();
                this.safePassword = this.oldPassword;
                // null to remove it from the persisted XML
                this.oldPassword = null;
                storeSafePassword();
            }
            else {

                this.safePassword = PasswordSafe.getInstance().getPassword(createCredentialsAttribute());
            }
        }
    }

    void storeSafePassword() {
        // support running in headless tests
        if (ApplicationManager.getApplication() != null) {
            PasswordSafe.getInstance().set(createCredentialsAttribute(), new Credentials(username, safePassword));
        }
    }

    private CredentialAttributes createCredentialsAttribute() {
        assert this.uuid != null;
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("zato-server", uuid));
    }
}
