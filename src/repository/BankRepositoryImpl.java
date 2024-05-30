package repository;

import model.*;
import service.AuditService;

import java.util.*;

public class BankRepositoryImpl implements BankRepository {
    private static double interestRate = 2.5;
    private static double mmInterestRate = 7.5;
    private static double withdrawalLimit = 5000;
    private static double overdraftLimit = 1000;
    private static int accountCount = 0;
    private Map<Customer, List<BankAccount>> customers;
    private List<Transaction> transactions;

    public BankRepositoryImpl() {
        this.customers = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    @Override
    public List<Transaction> getTransactions() {
        AuditService.getInstance().logAction("Got transactions");
        return transactions;
    }

    @Override
    public Map<Customer, List<BankAccount>> getCustomers() {
        AuditService.getInstance().logAction("Got customers with their accounts");
        return customers;
    }

    @Override
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
            case "MM" -> account = new MoneyMarketAccount(accountCount, mmInterestRate, withdrawalLimit);
            default -> account = new CertificateOfDepositAccount(accountCount, interestRate, 12);
        }

        accountCount++;
        account.deposit(initialBalance);
        if (!found) {
            customers.put(customer, new ArrayList<>());
        }
        customers.get(customer).add(account);
        AuditService.getInstance().logAction("Created account");
    }


    @Override
    public Customer getCustomer(int accountNumber) {
        for (Customer customer : customers.keySet()) {
            for (BankAccount account : customers.get(customer)) {
                if (account.getAccountNumber() == accountNumber) {
                    AuditService.getInstance().logAction("Got customer");
                    return customer;
                }
            }
        }
        AuditService.getInstance().logAction("Got customer");
        return null;
    }

    @Override
    public BankAccount getAccount(Customer customer, int accountNumber) {
        for (BankAccount account : customers.get(customer)) {
            if (account.getAccountNumber() == accountNumber) {
                AuditService.getInstance().logAction("Got account");
                return account;
            }
        }
        AuditService.getInstance().logAction("Got account");
        return null;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        return;
    }

    @Override
    public void addDeposit(int accountNumber, double amount) {
        return;
    }

    public void withdraw(int accountNumber, double amount) {
        return;
    }
}

