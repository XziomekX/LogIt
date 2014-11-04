package io.github.lucaseasedup.logit.security.model;

import io.github.lucaseasedup.logit.security.model.CommonHashingModel.Algorithm;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HashingModelDecoder
{
    public static HashingModel decode(String string)
    {
        if (string.startsWith("authme:"))
        {
            return new AuthMeHashingModel(string.substring("authme:".length()));
        }
        else if (string.equals("bcrypt"))
        {
            return new BCryptHashingModel();
        }
        else
        {
            Matcher roundfulMatcher = ROUNDFUL_MODEL_PATTERN.matcher(string);
            
            if (roundfulMatcher.find())
            {
                String algorithm = roundfulMatcher.group(1);
                int rounds = Integer.parseInt(roundfulMatcher.group(2));
                Algorithm decodedAlgorithm = Algorithm.decode(algorithm);
                
                if (decodedAlgorithm == null)
                    return null;
                
                return new CommonHashingModel(decodedAlgorithm, rounds);
            }
            else
            {
                Algorithm decodedAlgorithm = Algorithm.decode(string);
                
                if (decodedAlgorithm == null)
                    return null;
                
                return new CommonHashingModel(decodedAlgorithm, 1);
            }
        }
    }
    
    private static final Pattern ROUNDFUL_MODEL_PATTERN =
            Pattern.compile("^([A-Za-z0-9_-]+)\\(([0-9]+)\\)$");
}
