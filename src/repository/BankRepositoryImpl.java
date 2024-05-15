package repository;

import model.*;

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
        return transactions;
    }

    @Override
    public Map<Customer, List<BankAccount>> getCustomers() {
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


    @Override
    public Customer getCustomer(int accountNumber) {
        for (Customer customer : customers.keySet()) {
            for (BankAccount account : customers.get(customer)) {
                if (account.getAccountNumber() == accountNumber) {
                    return customer;
                }
            }
        }
        return null;
    }

    @Override
    public BankAccount getAccount(Customer customer, int accountNumber) {
        for (BankAccount account : customers.get(customer)) {
            if (account.getAccountNumber() == accountNumber) {
                return account;
            }
        }
        return null;
    }
}

