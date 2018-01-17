package io.zato.intellij.vfs;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Utilities to work with the Vfs layer of IntelliJ.
 *
 * @author jansorg
 */
public final class VfsUtils {
    private VfsUtils() {
    }

    @NotNull
    public static Path getPath(PsiFile file) {
        return getPath(file.getVirtualFile());
    }

    @NotNull
    public static Path getPath(VirtualFile virtualFile) {
        return VfsUtilCore.virtualToIoFile(virtualFile).toPath();
    }
}
