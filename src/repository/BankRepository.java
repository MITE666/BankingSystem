package repository;

import model.BankAccount;
import model.Customer;
import model.Transaction;

import java.util.List;
import java.util.Map;

public interface BankRepository {
    void createAccount(String customerName, double initialBalance, String accountType);

    Customer getCustomer(int accountNumber);

    BankAccount getAccount(Customer customer, int accountNumber);

    List<Transaction> getTransactions();

    Map<Customer, List<BankAccount>> getCustomers();
}
