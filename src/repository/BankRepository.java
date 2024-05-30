package repository;

import model.BankAccount;
import model.Customer;
import model.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface BankRepository {
    void createAccount(String customerName, double initialBalance, String accountType);

    void addTransaction(Transaction transaction);

    void addDeposit(int accountNumber, double amount) throws SQLException;

    Customer getCustomer(int accountNumber) throws SQLException;

    BankAccount getAccount(Customer customer, int accountNumber) throws SQLException;

    List<Transaction> getTransactions() throws SQLException;

    Map<Customer, List<BankAccount>> getCustomers() throws SQLException;

    public void withdraw(int accountNumber, double amount) throws SQLException;
}
