package repository;

import model.Customer;
import model.Loan;

import java.util.*;

public class LoanRepositoryImpl implements LoanRepository {
    private static int loanId = 0;
    private double interestRate = 15.0;
    private Map<Customer, List<Loan>> loans;

    public LoanRepositoryImpl() {
        this.loans = new HashMap<>();
    }

    @Override
    public Map<Customer, List<Loan>> getLoans() {
        return loans;
    }

    @Override
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
}
