package bsi.passwordWallet;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ValidationTest {
    Validation validation;
    @BeforeMethod
    public void setUp() {
        validation = new Validation();
    }

    @Test
    public void validatePassword_ReturnsWarning_IfPasswordIsEmpty() {
        String message = validation.validatePassword("");
        assertEquals(message, Validation.PASSWORD_CANT_BE_EMPTY);
    }

    @DataProvider(name = "validatePasswordValidPasswords")
    public Object[][] validPasswords() {
        return new Object[][] { {"p", ""}, {"3214sdfgfsfe32rtafsafdfsfda432", ""} };
    }

    @Test(dataProvider = "validatePasswordValidPasswords")
    public void validatePassword_ReturnsNoWarning_IfPasswordIsValid(String password, String expMessage) {
        assertEquals(validation.validatePassword(password), expMessage);
    }

    @DataProvider(name = "validatePasswordTooLongPasswords")
    public Object[][] tooLongPasswords() {
        String warningMessage = String.format(Validation.PASSWORD_CANT_BE_LONGER_THAN, Validation.PASSWORD_MAX_LENGTH);
        return new Object[][] {
                {"verylooooooooooooooooooooooooooooooooongpassword", warningMessage},
                {"31characterslongpasswordtesttes", warningMessage}
        };
    }

    @Test(dataProvider = "validatePasswordTooLongPasswords")
    public void validatePassword_ReturnsWarning_IfPasswordTooLong(String password, String expMessage) {
        assertEquals(validation.validatePassword(password), expMessage);
    }

    @Test
    public void validateLogin_ReturnsWarning_IfLoginIsEmpty() {
        assertEquals(validation.validateLogin(""), Validation.LOGIN_CANT_BE_EMPTY);
    }

    @DataProvider(name = "validateLoginValidLogins")
    public Object[][] validLogins() {
        return new Object[][] { {"L", ""}, {"30charactersLongLoginTestTestT", ""} };
    }

    @Test(dataProvider = "validateLoginValidLogins")
    public void validateLogin_ReturnsNoWarning_IfLoginIsValid(String login, String expMessage) {
        assertEquals(validation.validateLogin(login), expMessage);
    }

    @DataProvider(name = "validateLoginTooLongLogins")
    public Object[][] tooLongLogins() {
        String warningMessage = String.format(Validation.LOGIN_CANT_BE_LONGER_THAN, Validation.LOGIN_MAX_LENGTH);

        return new Object[][] {
                {"31charactersLongLoginTestTestTe", warningMessage},
                {"veryyyyyyyyyyyyyyyyyyyyyyylooooooooooongLogin", warningMessage}
        };
    }

    @Test(dataProvider = "validateLoginTooLongLogins")
    public void validateLogin_ReturnsWarning_IfLoginTooLong(String login, String expMessage) {
        assertEquals(validation.validateLogin(login), expMessage);
    }

    @Test
    public void validateWebsite_ReturnsWarning_IfWebsiteIsEmpty() {
        assertEquals(validation.validateWebsite(""), Validation.WEBSITE_CANT_BE_EMPTY);
    }

    @DataProvider(name = "validateWebsiteValidWebsites")
    public Object[][] validWebsites() {
        return new Object[][] {
                {"w", ""},
                {new String(new byte[300]), ""}
        };
    }

    @Test(dataProvider = "validateLoginValidLogins")
    public void validateWebsite_ReturnsNoWarning_IfWebsiteIsValid(String website, String expMessage) {
        assertEquals(validation.validateWebsite(website), expMessage);
    }

    @DataProvider(name = "validateWebsiteTooLongWebsites")
    public Object[][] tooLongWebsites() {
        String warningMessage = String.format(Validation.WEBSITE_CANT_BE_LONGER_THAN, Validation.WEBSITE_MAX_LENGTH);

        return new Object[][] {
                {new String(new byte[301]), warningMessage},
                {new String(new byte[342]), warningMessage}
        };
    }

    @Test(dataProvider = "validateWebsiteTooLongWebsites")
    public void validateWebsite_ReturnsWarning_IfWebsiteTooLong(String website, String expMessage) {
        assertEquals(validation.validateWebsite(website), expMessage);
    }

}