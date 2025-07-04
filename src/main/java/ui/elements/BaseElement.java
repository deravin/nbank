package ui.elements;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

public abstract class BaseElement {
    protected final SelenideElement element;

    public BaseElement(SelenideElement element){
        this.element = element;
    }

    protected SelenideElement find(By selector){
       return element.find(selector);
    }

    protected SelenideElement find(String sccSelector){
        return element.find(sccSelector);
    }

    protected SelenideElement findAll(By selector){
        return element.find(selector);
    }

    protected SelenideElement findAll(String sccSelector){
        return element.find(sccSelector);
    }
}
