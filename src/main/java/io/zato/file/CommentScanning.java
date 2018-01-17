package io.zato.file;

import com.google.common.base.Splitter;

import java.util.Iterator;

/**
 * Scans in comments of a PsiFile to find out whether a given marker text is matching a commented text.
 * A comment matches if it begins with the value passed as {@code markerText}.
 *
 * @author jansorg
 */
public class CommentScanning {
    private final String markerText;

    public CommentScanning(String markerText) {
        this.markerText = markerText;
    }

    /**
     * @param content The content to analyse
     * @return {@code true} if the file contains a comment which begins with the given marker text.
     */
    public boolean matches(String content) {
        int lineIndex = 0;

        Iterator<String> it = Splitter.on("\n").split(content).iterator();
        while (it.hasNext() && lineIndex < 100) {
            String line = it.next();
            if (line.trim().startsWith(markerText)) {
                return true;
            }

            lineIndex++;
        }

        return false;
    }
}