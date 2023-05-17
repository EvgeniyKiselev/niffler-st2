package niffler.db.entity;

import com.github.javafaker.Faker;

public class RandomDataForUserEntity {

    private static final Faker fakeData = new Faker();
    public static String randomUsername = fakeData.name().firstName(), randomPassword = fakeData.hobbit().quote();

}