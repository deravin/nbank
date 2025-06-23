package api.generators;

import org.apache.commons.lang3.RandomStringUtils;

import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData(){}

    public static String getUsername(){
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword(){
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "&!*-";
    }

//    public static float getBalance(){
//        return ThreadLocalRandom.current().nextFloat()*1000;
//    }

    public static float getBalance() {
        // Генерируем значение от 100.01 до 1100
        float randomValue = 100.01f + ThreadLocalRandom.current().nextFloat() * 1100;
        DecimalFormat df = new DecimalFormat("#.##");
        return Float.parseFloat(df.format(randomValue));
    }

}
