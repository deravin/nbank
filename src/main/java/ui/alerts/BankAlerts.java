package ui.alerts;

import lombok.Getter;

@Getter
public enum BankAlerts {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USER_MUST_BE_BETWEEN_3_AND_15("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred $"),
    ERROR_INVALID_TRANSFER("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited $"),
    ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount.");

    private final String message;

    BankAlerts(String message){
        this.message = message;
    }
}
