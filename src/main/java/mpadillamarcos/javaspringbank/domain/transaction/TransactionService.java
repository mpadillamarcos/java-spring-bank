package mpadillamarcos.javaspringbank.domain.transaction;

import lombok.RequiredArgsConstructor;
import mpadillamarcos.javaspringbank.domain.account.AccountId;
import mpadillamarcos.javaspringbank.domain.money.Money;
import mpadillamarcos.javaspringbank.domain.user.UserId;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    public void createTransaction(UserId userId, AccountId originAccountId, AccountId destinationAccountId, Money amount) {
    }
}
