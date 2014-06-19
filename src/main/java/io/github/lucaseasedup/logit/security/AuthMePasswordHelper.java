/*
 * AuthMePasswordHelper.java
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
package io.github.lucaseasedup.logit.security;

public final class AuthMePasswordHelper
{
    private AuthMePasswordHelper()
    {
    }
    
    public static boolean comparePasswordWithHash(String password,
                                                  String hashedPassword,
                                                  String encryptionMethod)
    {
    	if (compareWithEncryptionMethod(password, hashedPassword, encryptionMethod))
    	{
    		return true;
    	}
        
        if (OLD_PASSWORDS_SUPPORTED)
        {
        	if (compareWithAllEncryptionMethods(password, hashedPassword))
        	{
        		return true;
        	}
        }
        
        return false;
    }
    
    private static boolean compareWithEncryptionMethod(String password,
                                                       String hashedPassword,
                                                       String encryptionMethod)
    {
        try
        {
            switch (encryptionMethod.toUpperCase())
            {
            case "BCRYPT":
            {
                return BCrypt.checkpw(password, hashedPassword);
            }
            case "DOUBLEMD5":
            {
                return hashedPassword.equals(
                        SecurityHelper.getMd5(SecurityHelper.getMd5(password))
                );
            }
            case "MD5":
            {
                return hashedPassword.equals(SecurityHelper.getMd5(password));
            }
            case "PLAINTEXT":
            {
                return hashedPassword.equals(password);
            }
            case "ROYALAUTH":
            {
                String hash = password;
                
                for (int i = 0; i < 25; i++)
                {
                    hash = SecurityHelper.getSha512(hash);
                }
                
                return hashedPassword.equalsIgnoreCase(hash);
            }
            case "SHA1":
            {
                return hashedPassword.equals(SecurityHelper.getSha1(password));
            }
            case "SHA256":
            {
                String[] line = hashedPassword.split("\\$");
                String hash = SecurityHelper.getSha256(
                        SecurityHelper.getSha256(password) + line[2]
                );
                
                return hashedPassword.equals("$SHA$" + line[2] + "$" + hash);
            }
            case "SHA512":
            {
                return hashedPassword.equals(SecurityHelper.getSha512(password));
            }
            case "WHIRLPOOL":
            {
                return hashedPassword.equals(SecurityHelper.getWhirlpool(password));
            }
            case "XAUTH":
            {
                int saltPos = (password.length() >= hashedPassword.length())
                        ? hashedPassword.length() - 1
                        : password.length();
                String salt = hashedPassword.substring(saltPos, saltPos + 12);
                
                return hashedPassword.equals(getXauthHash(password, salt));
            }
            }
        }
        catch (RuntimeException ex)
        {
            return false;
        }
        
        return false;
    }
    
    private static boolean compareWithAllEncryptionMethods(String password, String hashedPassword)
    {
        if (compareWithEncryptionMethod(password, hashedPassword, "BCRYPT"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "DOUBLEMD5"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "MD5"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "PLAINTEXT"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "ROYALAUTH"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "SHA1"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "SHA256"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "SHA512"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "WHIRLPOOL"))
            return true;
        
        if (compareWithEncryptionMethod(password, hashedPassword, "XAUTH"))
            return true;
        
    	return false;
    }
    
    public static boolean validateEncryptionMethod(String encryptionMethod)
    {
        return encryptionMethod.equalsIgnoreCase("BCRYPT")
            || encryptionMethod.equalsIgnoreCase("DOUBLEMD5")
            || encryptionMethod.equalsIgnoreCase("MD5")
            || encryptionMethod.equalsIgnoreCase("PLAINTEXT")
            || encryptionMethod.equalsIgnoreCase("ROYALAUTH")
            || encryptionMethod.equalsIgnoreCase("SHA1")
            || encryptionMethod.equalsIgnoreCase("SHA256")
            || encryptionMethod.equalsIgnoreCase("SHA512")
            || encryptionMethod.equalsIgnoreCase("WHIRLPOOL")
            || encryptionMethod.equalsIgnoreCase("XAUTH");
    }
    
    private static String getXauthHash(String password, String salt)
    {
        String hash = SecurityHelper.getWhirlpool(salt + password).toLowerCase();
        int saltPos = (password.length() >= hash.length() ? hash.length() - 1 : password.length());
        
        return hash.substring(0, saltPos) + salt + hash.substring(saltPos);
    }
    
    private static final boolean OLD_PASSWORDS_SUPPORTED = true;
}
