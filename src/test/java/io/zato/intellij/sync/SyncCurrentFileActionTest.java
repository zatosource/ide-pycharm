package io.zato.intellij.sync;

import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PathUtil;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.PythonLanguage;
import io.zato.intellij.test.MockHttpFixtureTestCase;
import org.junit.Assert;

/**
 * @author jansorg
 */
public class SyncCurrentFileActionTest extends MockHttpFixtureTestCase {

    public void testSuccessfulFileUpload() {
        configureDefaultServer();

        //the context for the upload action
        myFixture.configureByText(PythonFileType.INSTANCE, "print(\"Hello world!\")");

        //use the ID configured in plugin.xml
        AnAction action = findSyncAction();
        Assert.assertNotNull("Action wasn't found", action);

        Presentation presentation = myFixture.testAction(action);
        Assert.assertTrue(presentation.isEnabledAndVisible());

        Assert.assertEquals("Expected content of a single request", 1, httpd.getRequestContent().size());
        Assert.assertEquals("Expected content wasn't uploaded", "{\"payload_name\":\"aaa.py\",\"payload\":\"cHJpbnQoIkhlbGxvIHdvcmxkISIp\"}", httpd.getRequestContent().get(0));
    }

    public void testUploadWithoutServer() {
        //the context for the upload action
        myFixture.configureByText(PythonFileType.INSTANCE, "print(\"Hello world!\")");

        //use the ID configured in plugin.xml
        AnAction action = findSyncAction();
        Assert.assertNotNull("Action wasn't found", action);

        try {
            myFixture.testAction(action);
        } catch (RuntimeException e) {
            Assert.assertEquals("Expected no upload without a server", 0, httpd.getRequestContent().size());
            Assert.assertTrue("A message box must be shown if no default server is configured", e.getMessage().contains("There is no default server available"));
        }
    }

    public void testUploadScratchFile() {
        configureDefaultServer();

        //the context for the upload action
        VirtualFile scratch = ScratchRootType.getInstance().createScratchFile(getProject(), PathUtil.makeFileName("test", "py"), PythonLanguage.getInstance(), "print()", ScratchFileService.Option.create_new_always);
        if (scratch != null) {
            FileEditorManager.getInstance(getProject()).openFile(scratch, true);
        }

        //use the ID configured in plugin.xml
        AnAction action = findSyncAction();
        Presentation presentation = myFixture.testAction(action);
        Assert.assertFalse(presentation.isEnabled());

        Assert.assertEquals("Expected no upload for a scratch file", 0, httpd.getRequestContent().size());
    }

    public void testNonPythonFile() {
        //the context for the upload action
        myFixture.configureByText(PlainTextFileType.INSTANCE, "plain text");

        //use the ID configured in plugin.xml
        Presentation presentation = myFixture.testAction(findSyncAction());
        Assert.assertFalse("The action must be disabled for non-python files", presentation.isEnabled());
    }

    private AnAction findSyncAction() {
        return ActionManager.getInstance().getAction("zato.syncFile");
    }
}