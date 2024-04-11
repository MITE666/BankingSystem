package service;

import model.*;

import java.util.*;

public class LoanService {
    private static int loanId = 0;
    private double interestRate = 15.0;
    private Map<Customer, List<Loan>> loans;

    public LoanService() {
        this.loans = new HashMap<>();
    }

    public void addLoan(String customerName, double loanAmount, int repaymentPeriod) {
        Customer customer = null;
        boolean found = false;
        for (Customer c : loans.keySet()) {
            if (Objects.equals(c.getName(), customerName)) {
                customer = c;
                found = true;
                break;
            }
        }
        if (customer == null) {
            customer = new Customer(customerName);
        }
        Loan loan = new Loan(loanId, loanAmount, interestRate, repaymentPeriod);
        loanId++;
        if (!found) {
            loans.put(customer, new ArrayList<>());
        }
        loans.get(customer).add(loan);
    }

    public void displayLoans(String name) {
        for (Customer customer : loans.keySet()) {
            if (Objects.equals(name, customer.getName())) {
                System.out.println("\nCustomer: " + customer);
                for (Loan loan : loans.get(customer)) {
                    System.out.println("\nLoan: " + loan);
                }
                return;
            }
        }
    }

    public void displayMonthlyPayment(int loanId) {
        for (Customer customer : loans.keySet()) {
            for (Loan loan : loans.get(customer)) {
                if (loan.getLoanId() == loanId) {
                    System.out.println("\nMonthly payment: " + loan.calculateInterest());
                }
            }
        }
    }
    public void makePayment(int loanId, double amount) {
        for (Customer customer : loans.keySet()) {
            for (Loan loan : loans.get(customer)) {
                if (loan.getLoanId() == loanId) {
                    loan.makePayment(amount);
                }
            }
        }
    }
}
