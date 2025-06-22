package api.iteration_1;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseTest {
    protected SoftAssertions softly;

    @BeforeEach
    public void setSoftly() {
        this.softly = new SoftAssertions();
    }
    @AfterEach
    public void afterTest(){
        softly.assertAll();
    }
}
