package utils;

import java.util.Random;

public class Randoms {

    private static final int LEFT_LIMIT = 97;
    private static final int RIGHT_LIMIT = 122;
    private static final int TARGET_STRING_LENGTH = 10;

    @SuppressWarnings("checkstyle:magicnumber")
    public static String alpha() {
        return alpha(TARGET_STRING_LENGTH);
    }

    public static String alpha(final Integer length) {
        Random random = new Random();

        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1).limit(length)
                     .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public static Integer integer(){
        Random random = new Random();
        return random.nextInt();
    }

    public static Integer integer(int min, int max){
        Random random = new Random();
        return random.nextInt(min, max);
    }

    public static long getLong(){
        Random random = new Random();
        return random.nextLong();
    }

    public static long getLong(int min, int max){
        Random random = new Random();
        return random.nextLong(min, max);
    }
}
