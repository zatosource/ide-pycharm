package io.zato.intellij.psi;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import io.zato.file.CommentScanning;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jansorg
 */
public class PsiCommentScanningTest extends LightPlatformCodeInsightFixtureTestCase {
    @Test
    public void testMatching() {
        boolean matches = pythonMatches("# zato: ide-deploy=True");
        Assert.assertTrue("Matching comment expected", matches);

        //2nd comment matches
        matches = pythonMatches("# zato: ide-deploy=False\n# zato: ide-deploy=True\n");
        Assert.assertTrue("Match on comment expected", matches);

        //2nd comment matches, without trailing newline
        matches = pythonMatches("# zato: ide-deploy=False\n# zato: ide-deploy=True\n");
        Assert.assertTrue("Match on comment expected", matches);

        //build a larger file to test
        StringBuilder content = new StringBuilder();
        // 10 lines of code
        content.append(StringUtils.repeat("print(\"Hello world\")\n", 10));
        // 10 lines of regular comments
        content.append(StringUtils.repeat("# This is a python comment\n", 10));
        // comments which are not fully matching the marker
        content.append(StringUtils.repeat("# zato: ide-deploy=\n", 10));
        content.append(StringUtils.repeat("# zato: ide-deploy=False\n", 10));
        //finally the matching comment
        content.append(StringUtils.repeat("# zato: ide-deploy=True\n", 10));

        Assert.assertTrue("File content must have a match", pythonMatches(content.toString()));
    }

    @Test
    public void testNoMatching() {
        boolean matches = pythonMatches("print()");
        Assert.assertFalse("No matching comment expected", matches);

        //build a larger file to test
        StringBuilder content = new StringBuilder();
        // 10 lines of code
        content.append(StringUtils.repeat("print(\"Hello world\")\n", 10));
        // 10 lines of regular comments
        content.append(StringUtils.repeat("# This is a python comment\n", 10));
        // comments which are not fully matching the marker
        content.append(StringUtils.repeat("# zato: ide-deploy=\n", 10));
        content.append(StringUtils.repeat("# zato: ide-deploy=False\n", 10));

        Assert.assertFalse("File content must not have a match", pythonMatches(content.toString()));
    }

    private boolean pythonMatches(String fileContent) {
        return new CommentScanning("# zato: ide-deploy=True").matches(fileContent);
    }
}