package model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class uses bit-masking to identify which fields are incorrect.
 * It is mostly just used for sending error information to the user.
 */
public class Validator {

    public static final int
        FIRST_NAME=0,
        LAST_NAME=1,
        DEPARTMENT=2,
        MAJOR=3,
        EMAIL=4,
        IMAGE_URL=5;

    /**
     *
     * @param s the string being validated.
     * @param validationType which validation method to use.
     * @return True or False depending on if the string passed the tests.
     */
    public static int validate(String s, int validationType) {
        return switch (validationType) {
            case FIRST_NAME -> s.matches("^[^ ±!@£$%^&*_+§¡€#¢§¶•ªº«\\\\/<>?:;|=.,]{1,20}$")?0:(int)Math.pow(2,FIRST_NAME);
            case LAST_NAME -> s.matches("^[^ ±!@£$%^&*_+§¡€#¢§¶•ªº«\\\\/<>?:;|=.,]{1,20}$")?0:(int)Math.pow(2,LAST_NAME);
            case DEPARTMENT -> s.matches("^[^±!@£$%^&*_+§¡€#¢§¶•ªº«\\\\/<>?:;|=.,]+$")?0:(int)Math.pow(2,DEPARTMENT);
            case MAJOR -> {for (Major m : Major.values()) if (m.equals(s)) yield 0;yield (int)Math.pow(2,MAJOR);}
            case EMAIL -> s.matches("^(?!\\.)[\\w\\-_.]*[^.]@farmingdale.edu$")?0:(int)Math.pow(2,EMAIL);
            case IMAGE_URL -> s.isEmpty() || s.matches("([^?#]*\\/)?([^?#]*\\.([Jj][Pp][Ee]?[Gg]|[Pp][Nn][Gg]|[Gg][Ii][Ff]))(?:\\?([^#]*))?(?:#(.*))?$")?0:(int)Math.pow(2,IMAGE_URL);
            default -> 0;
        };
    }

    /**
     * Converts invalid bits returned from <code>validate()</code> into a readable String.
     * @param bits Invalid Bits
     * @return A readable list of which fields are incorrect.
     */
    public static String invalidBitsToString(int bits) {
        List<String> strings = new ArrayList<>();
        if ( (bits & (1<<FIRST_NAME) )!=0 ) strings.add("First Name");
        if ( (bits & (1<<LAST_NAME) )!=0 )  strings.add("Last Name");
        if ( (bits & (1<<DEPARTMENT) )!=0 ) strings.add("Department");
        if ( (bits & (1<<MAJOR) )!=0 ) strings.add("Major");
        if ( (bits & (1<<EMAIL) )!=0 ) strings.add("Email");
        if ( (bits & (1<<IMAGE_URL) )!=0 ) strings.add("Image");
        return String.join(",", strings);
    }

}
