package service;

import model.*;
import repository.BankRepository;

import java.sql.SQLException;
import java.util.*;

public class BankService {
    private final BankRepository bankRepository;

    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public void createAccount(String customerName, double initialBalance, String accountType) {
        bankRepository.createAccount(customerName, initialBalance, accountType);
    }

    public boolean deposit(int accountNumber, double amount) throws SQLException {
        Customer customer = bankRepository.getCustomer(accountNumber);
        if (customer != null) {
            BankAccount account = bankRepository.getAccount(customer, accountNumber);
            if (account != null) {
                bankRepository.addDeposit(accountNumber, amount);
                return true;
            }
        }
        return false;
    }

    public boolean withdraw(int accountNumber, double amount) throws SQLException {
        Customer customer = bankRepository.getCustomer(accountNumber);
        if (customer != null) {
            BankAccount account = bankRepository.getAccount(customer, accountNumber);
            if (account != null) {
                bankRepository.withdraw(accountNumber, amount);
                return account.withdraw(amount);
            }
        }
        return false;
    }

    public void transfer(int fromAccountNumber, int toAccountNumber, double amount, String description) throws SQLException {
        Customer fromCustomer = bankRepository.getCustomer(fromAccountNumber);
        Customer toCustomer = bankRepository.getCustomer(toAccountNumber);
        if (fromCustomer != null && toCustomer != null) {
            BankAccount fromAccount = bankRepository.getAccount(fromCustomer, fromAccountNumber);
            BankAccount toAccount = bankRepository.getAccount(toCustomer, toAccountNumber);
            if (fromAccount != null && toAccount != null) {
                if (fromAccount.withdraw(amount)) {
                    toAccount.deposit(amount);
                    Transaction transaction = new Transaction(description, amount, fromAccount, toAccount);
                    bankRepository.getTransactions().add(transaction);
                }
            }
        }
    }

    public void displayCustomer(String name) throws SQLException {
        for (Customer customer : bankRepository.getCustomers().keySet()) {
            if (Objects.equals(customer.getName(), name)) {
                System.out.println(customer);
                for (BankAccount account : bankRepository.getCustomers().get(customer)) {
                    System.out.println(account);
                }
                return;
            }
        }
    }

    public void displayTransactions() throws SQLException {
        for (Transaction transaction : bankRepository.getTransactions()) {
            System.out.println(transaction);
        }
    }

    public void incrementSavings() throws SQLException {
        for (Customer customer : bankRepository.getCustomers().keySet()) {
            for (BankAccount account : bankRepository.getCustomers().get(customer)) {
                if (account instanceof SavingsAccount) {
                    ((SavingsAccount) account).calculateInterest();
                }
            }
        }
    }
}
