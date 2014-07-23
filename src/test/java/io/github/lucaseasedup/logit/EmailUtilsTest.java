/*
 * EmailUtilsTest.java
 *
 * Copyright (C) 2012-2014 LucasEasedUp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.lucaseasedup.logit;

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
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testValidationNullEmail()
    {
        EmailUtils.validateEmail(null);
    }
}
