package io.zato.intellij.http;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.jetbrains.python.PythonFileType;
import org.junit.Assert;

/**
 * @author jansorg
 */
public class ZatoHttpServiceTest extends LightPlatformCodeInsightFixtureTestCase {
    public void testIsSupported() {
        ZatoHttpService service = ZatoHttpService.getInstance();

        //unsupported
        Assert.assertFalse(service.isSupported((VirtualFile) null));
        //psi
        Assert.assertFalse(service.isSupported(myFixture.configureByText(PlainTextFileType.INSTANCE, "plain text not supported")));
        Assert.assertFalse(service.isSupported(myFixture.configureByText(JavaFileType.INSTANCE, "java code not supported")));
        //vfs
        Assert.assertFalse(service.isSupported(myFixture.configureByText(PlainTextFileType.INSTANCE, "plain text not supported").getVirtualFile()));
        Assert.assertFalse(service.isSupported(myFixture.configureByText(JavaFileType.INSTANCE, "java code not supported").getVirtualFile()));

        //supported
        Assert.assertTrue(service.isSupported(myFixture.configureByText(PythonFileType.INSTANCE, "")));
        //psi
        Assert.assertTrue(service.isSupported(myFixture.configureByText(PythonFileType.INSTANCE, "# python content")));
        Assert.assertTrue(service.isSupported(myFixture.configureByText(PythonFileType.INSTANCE, "# python content\nprint()")));
        //vfs
        Assert.assertTrue(service.isSupported(myFixture.configureByText(PythonFileType.INSTANCE, "# python content").getVirtualFile()));
        Assert.assertTrue(service.isSupported(myFixture.configureByText(PythonFileType.INSTANCE, "# python content\nprint()").getVirtualFile()));
    }
}