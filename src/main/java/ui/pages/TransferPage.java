package ui.pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Selectors.by;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage>{
    private SelenideElement accountsDropdown = $("select.account-selector");
    private SelenideElement recipientAccountNumberInput = $(by("placeholder", "Enter recipient account number"));
    private SelenideElement amountInput = $(by("placeholder", "Enter amount"));
    private SelenideElement confirmCheckbox = $(by("id","confirmCheck"));
    private SelenideElement sendTransferButton = $(byText("\uD83D\uDE80 Send Transfer"));
    private SelenideElement accountFromDropdown=  $("select.account-selector option:checked");

    @Override
    public String url() {
        return "/transfer";
    }

    public TransferPage makeTransfer(String account1Number, String account2Number, String transferAmountString){
        accountsDropdown.click();
        accountsDropdown.selectOptionContainingText(account1Number);
        recipientAccountNumberInput.setValue(account2Number);
        amountInput.setValue(transferAmountString);
        confirmCheckbox.click();
        sendTransferButton.click();
        return this;
    }

    public TransferPage checkSelfAccounts(String accountNumber, float transferAmount){
        // Форматируем сумму до 2 знаков после запятой
        String formattedAmount = String.format("%.2f", transferAmount);
        String expectedText = accountNumber + " (Balance: $" + formattedAmount + ")";

        // Выбираем и проверяем
        accountsDropdown.selectOptionContainingText(accountNumber);
        accountFromDropdown.shouldHave(exactText(expectedText));

        return this;
    }
}
