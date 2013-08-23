import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.github.lucaseasedup.logit.util.EmailUtils;
import org.junit.Test;

public class EmailUtilsTest
{
    @Test
    public void testValidation()
    {
        assertTrue(EmailUtils.validateEmail("example123@website321.com"));
        assertFalse(EmailUtils.validateEmail(""));
        assertFalse(EmailUtils.validateEmail("example"));
        assertFalse(EmailUtils.validateEmail("example@"));
        assertFalse(EmailUtils.validateEmail("website.com"));
        assertFalse(EmailUtils.validateEmail("@website.com"));
        assertFalse(EmailUtils.validateEmail("example@@website.com"));
        assertFalse(EmailUtils.validateEmail("examp,le@website.com"));
        assertFalse(EmailUtils.validateEmail("example@website.com "));
        
        try
        {
            assertFalse(EmailUtils.validateEmail(null));
        }
        catch (Throwable ex)
        {
        }
    }
}
