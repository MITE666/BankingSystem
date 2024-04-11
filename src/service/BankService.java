package service;

import model.*;

import java.util.*;

public class BankService {
    private static double interestRate = 2.5;
    private static double overdraftLimit = 1000;
    private static int accountCount = 0;
    private Map<Integer, Customer> customers;
    private List<Transaction> transactions;

    public BankService() {
        this.customers = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    public void createAccount(String customerName, double initialBalance, String accountType) {
        Customer customer = null;
        for (Customer c : customers.values()) {
            if (Objects.equals(c.getName(), customerName)) {
                customer = c;
                break;
            }
        }
        if (customer == null) {
            customer = new Customer(customerName);
        }
        BankAccount account;
        if (accountType.equalsIgnoreCase("Savings")) {
            account = new SavingsAccount(accountCount, interestRate);
        } else {
            account = new CheckingAccount(accountCount, overdraftLimit);
        }
        accountCount++;
        account.deposit(initialBalance);
        customer.addAccount(account);
        customers.putIfAbsent(accountCount, customer);
    }

    public boolean deposit(int accountNumber, double amount) {
        Customer customer = findCustomer(accountNumber);
        if (customer != null) {
            BankAccount account = findAccount(customer, accountNumber);
            if (account != null) {
                account.deposit(amount);
                return true;
            }
        }
        return false;
    }

    public boolean withdraw(int accountNumber, double amount) {
        Customer customer = findCustomer(accountNumber);
        if (customer != null) {
            BankAccount account = findAccount(customer, accountNumber);
            if (account != null) {
                return account.withdraw(amount);
            }
        }
        return false;
    }

    public void transfer(int fromAccountNumber, int toAccountNumber, double amount, String description) {
        Customer fromCustomer = findCustomer(fromAccountNumber);
        Customer toCustomer = findCustomer(toAccountNumber);
        if (fromCustomer != null && toCustomer != null) {
            BankAccount fromAccount = findAccount(fromCustomer, fromAccountNumber);
            BankAccount toAccount = findAccount(toCustomer, toAccountNumber);
            if (fromAccount != null && toAccount != null) {
                if (fromAccount.withdraw(amount)) {
                    toAccount.deposit(amount);
                    Transaction transaction = new Transaction(description, amount, fromAccount, toAccount);
                    transactions.add(transaction);
                }
            }
        }
    }

    public void displayCustomer(String name) {
        for (Customer customer : customers.values()) {
            if (Objects.equals(customer.getName(), name)) {
                System.out.println(customer);
                return;
            }
        }
    }

    public void displayTransactions() {
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    public void incrementSavings() {
        for (Customer customer : customers.values()) {
            for (BankAccount account : customer.getAccounts()) {
                if (account instanceof SavingsAccount) {
                    ((SavingsAccount) account).calculateInterest();
                }
            }
        }
    }

    private Customer findCustomer(int accountNumber) {
        for (Customer customer : customers.values()) {
            for (BankAccount account : customer.getAccounts()) {
                if (account.getAccountNumber() == accountNumber) {
                    return customer;
                }
            }
        }
        return null;
    }

    private BankAccount findAccount(Customer customer, int accountNumber) {
        for (BankAccount account : customer.getAccounts()) {
            if (account.getAccountNumber() == accountNumber) {
                return account;
            }
        }
        return null;
    }
}
