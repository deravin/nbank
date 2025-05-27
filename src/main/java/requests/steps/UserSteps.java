package requests.steps;

import generators.RandomData;
import io.restassured.specification.ResponseSpecification;
import models.*;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UserSteps {
    public static AccountInfoResponse createAccount(CreateUserRequest userRequest) {
        AccountInfoResponse accountInfoResponse = new ValidatedCrudRequester<AccountInfoResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.CREATE_ACCOUNT,
                ResponseSpecs.entityWasCreated())
                .post(null);

        return accountInfoResponse;
    }

    public static AccountInfoResponse addDeposit(AddDepositRequest depositRequest, CreateUserRequest userRequest, ResponseSpecification responseSpecs) {
         return new ValidatedCrudRequester<AccountInfoResponse>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                responseSpecs)
                .post(depositRequest);
    }

    public static void transferMoney(CreateUserRequest userRequest,AccountInfoResponse accountFrom, AccountInfoResponse accountTo, float transferAmount,
                                     ResponseSpecification responseSpecs){
        // Создаем тело запроса для перевода денег
        TransferMoneyRequest moneyRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountFrom.getId())
                .receiverAccountId(accountTo.getId())
                .amount(transferAmount)
                .build();

        // Переводим деньги на счет
        new CrudRequester(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                responseSpecs)
                .post(moneyRequest);
    }

    public static CustomerAccountsList accountsList(CreateUserRequest userRequest){
        return new ValidatedCrudRequester<CustomerAccountsList>(
                RequestSpecs.authAsUserSpec(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.GET_ACCOUNT,
                ResponseSpecs.requestReturnsOK())
                .get();
    }

    public static AddDepositRequest generateDepositSum(AccountInfoResponse account){
        return AddDepositRequest.builder()
                .id(account.getId())
                .balance(RandomData.getBalance())
                .build();
    }

    public static AccountInfoResponse updateAccount(AccountInfoResponse account, CustomerAccountsList listOfAccounts){
        AccountInfoResponse accountInfoResponse1Updated = null;
        for (AccountInfoResponse acc : listOfAccounts.getAccounts()) {
            if (acc.getId() == account.getId()) {
                accountInfoResponse1Updated = acc;
            }
        }
        return accountInfoResponse1Updated;
    }

    public static float generateTransferAmount(AccountInfoResponse account){
        float maxAllowedTransfer = account.getBalance();
        if (maxAllowedTransfer < 0.01f) {
            throw new IllegalStateException("Недостаточно средств для перевода: баланс = " + maxAllowedTransfer);
        }
        float transferAmount;
        do {
            transferAmount = (float) (Math.random() * maxAllowedTransfer);
        } while (transferAmount <= 1.00f); // Минимальная сумма = 1.00
        return transferAmount;
    }
}
