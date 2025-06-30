package ui.pages;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

@Getter
public class DepositMoneyPage extends BasePage{
   private SelenideElement choseAnAccountDropdown = $(".form-control.account-selector");
   private SelenideElement enterAmountInput = $("input.form-control.deposit-input[type='number']");
   private SelenideElement depositButton = $(byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositMoneyPage addDeposit(String account,String amount){
        choseAnAccountDropdown.click(); // открываю дропдаун
        choseAnAccountDropdown.selectOptionContainingText(account); // выбираем нужный акк
        enterAmountInput.setValue(amount);
        depositButton.click();
        return this;
    }
}
