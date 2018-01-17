package io.zato.http;

import net.jcip.annotations.Immutable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Defines the response as returned by the Zato server.
 *
 * @author jansorg
 */
@Immutable
public class ZatoHttpResponse {
    //the status code of the HTTP response
    private final int statusCode;
    //the message contained in the response's JSON data
    @NotNull
    private final String message;
    //the success flag contained in the response's JSON data
    private final boolean successfullyDeployed;

    ZatoHttpResponse(int statusCode, String message, boolean successfullyDeployed) {
        this.statusCode = statusCode;
        this.message = message == null ? "" : message;
        this.successfullyDeployed = successfullyDeployed;
    }

    public int getStatusCode() {
        return statusCode;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    public boolean isSuccessfullyDeployed() {
        return successfullyDeployed;
    }

    @Override
    public String toString() {
        return "ZatoHttpResponse{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", successfullyDeployed=" + successfullyDeployed +
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
        ZatoHttpResponse that = (ZatoHttpResponse) o;
        return statusCode == that.statusCode &&
                successfullyDeployed == that.successfullyDeployed &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, message, successfullyDeployed);
    }
}
