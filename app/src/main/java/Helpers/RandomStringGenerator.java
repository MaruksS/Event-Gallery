package Helpers;

import java.util.Random;

/**
 * Created by Sergejs on 12/10/2017.
 */

public class RandomStringGenerator {
    private static String generateRandomString(int length){
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String generated_nr = salt.toString();
        return generated_nr;
    }
    public static String getGeneratedString(int length){
        return generateRandomString(length);
    }

}
