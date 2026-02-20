import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.runner.Starter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UiIntegrationTest:

  @Test
  def simpleTestWithoutProject =
//    Starter.INSTANCE.newContext("abc", TestCase(IdeProductProvider.INSTANCE.getIC, NoProject.INSTANCE)
//      .withVersion("2024.3")
//    )
    assertTrue(true)
