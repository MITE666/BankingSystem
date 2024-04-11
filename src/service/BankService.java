package service;

import model.*;

import java.util.*;

public class BankService {
    private static double interestRate = 2.5;
    private static double mmInterestRate = 7.5;
    private static double withdrawalLimit = 5000;
    private static double overdraftLimit = 1000;
    private static int accountCount = 0;
    private Map<Customer, List<BankAccount>> customers;
    private List<Transaction> transactions;

    public BankService() {
        this.customers = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    public void createAccount(String customerName, double initialBalance, String accountType) {
        Customer customer = null;
        boolean found = false;
        for (Customer c : customers.keySet()) {
            if (Objects.equals(c.getName(), customerName)) {
                customer = c;
                found = true;
                break;
            }
        }
        if (customer == null) {
            customer = new Customer(customerName);
        }
        BankAccount account;

        switch(accountType.toLowerCase()) {
            case "savings" -> account = new SavingsAccount(accountCount, interestRate);
            case "checking" -> account = new CheckingAccount(accountCount, overdraftLimit);
            case "moneymarket" -> account = new MoneyMarketAccount(accountCount, mmInterestRate, withdrawalLimit);
            default -> account = new CertificateOfDepositAccount(accountCount, interestRate, 12);
        }

        accountCount++;
        account.deposit(initialBalance);
        if (!found) {
            customers.put(customer, new ArrayList<>());
        }
        customers.get(customer).add(account);
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
        for (Customer customer : customers.keySet()) {
            if (Objects.equals(customer.getName(), name)) {
                System.out.println(customer);
                for (BankAccount account : customers.get(customer)) {
                    System.out.println(account);
                }
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
        for (Customer customer : customers.keySet()) {
            for (BankAccount account : customers.get(customer)) {
                if (account instanceof SavingsAccount) {
                    ((SavingsAccount) account).calculateInterest();
                }
            }
        }
    }

    private Customer findCustomer(int accountNumber) {
        for (Customer customer : customers.keySet()) {
            for (BankAccount account : customers.get(customer)) {
                if (account.getAccountNumber() == accountNumber) {
                    return customer;
                }
            }
        }
        return null;
    }

    private BankAccount findAccount(Customer customer, int accountNumber) {
        for (BankAccount account : customers.get(customer)) {
            if (account.getAccountNumber() == accountNumber) {
                return account;
            }
        }
        return null;
    }
}
