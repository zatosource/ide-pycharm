package io.zato.intellij.sync;

import com.intellij.ide.actions.SaveAllAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PythonFileType;
import io.zato.intellij.test.MockHttpFixtureTestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author jansorg
 */
public class UploadOnSaveComponentTest extends MockHttpFixtureTestCase {
    /**
     * Tests that no upload is done if the marker isn't in the file
     */
    @Test
    public void testUploadAfterFileSave_noMarker() throws InterruptedException {
        configureDefaultServer();

        doFileChange("Changed content!");

        Assert.assertEquals("Expected that no content is uploaded if there's not marker comment", 0, httpd.getRequestContent().size());
    }

    /**
     * Tests that no upload is done if the marker isn't in the file
     */
    @Test
    public void testUploadAfterFileSave_withMarker() throws InterruptedException {
        configureDefaultServer();

        doFileChange("print()\n# simple comment\n# zato: ide-deploy=True");

        Assert.assertEquals("Expected that the file was uploaded to the remote server", 1, httpd.getRequestContent().size());
    }

    private void doFileChange(String contentBeforeUpload) throws InterruptedException {
        //the context for the upload action
        PsiFile psiFile = myFixture.configureByText(PythonFileType.INSTANCE, "print(\"Hello world!\")");
        Assert.assertEquals("Expected no upload before save", 0, httpd.getRequestContent().size());

        ApplicationManager.getApplication().runWriteAction(() -> {
            Document document = PsiDocumentManager.getInstance(getProject()).getDocument(psiFile);
            Assert.assertNotNull(document);
            document.setText(contentBeforeUpload);
        });

        CountDownLatch latch = new CountDownLatch(1);
        ActionUtil.invokeAction(new SaveAllAction(),
                myFixture.getEditor().getContentComponent(),
                ActionPlaces.KEYBOARD_SHORTCUT,
                null,
                latch::countDown);
        boolean ok = latch.await(5, TimeUnit.SECONDS);
        assertTrue(ok);
    }
}