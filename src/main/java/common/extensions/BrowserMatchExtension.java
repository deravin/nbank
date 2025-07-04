package common.extensions;

import com.codeborne.selenide.Configuration;
import common.annotations.Browsers;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class BrowserMatchExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        Browsers annotation = extensionContext.getElement()
                .map(el -> el.getAnnotation(Browsers.class))
                .orElse(null);
        if (annotation == null){
            return ConditionEvaluationResult.enabled("Нет ограничений к браузеру");
        }
        String currentBrowser = Configuration.browser;
        boolean matches = Arrays.stream(annotation.values())
                .anyMatch(browser -> browser.equals(currentBrowser));

        if (matches){
            return ConditionEvaluationResult.enabled("Текущий браузер - " + currentBrowser + " удовлетворяет условию");
        }
        return ConditionEvaluationResult.disabled(
                "Тест пропущен, тк текущий браузер - " + currentBrowser +
                        " не находится в списке допустимых браузеров для теста - "
                + Arrays.toString(annotation.values()));
    }
}
