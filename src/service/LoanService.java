package service;

import model.*;
import repository.LoanRepository;
import repository.LoanRepositoryImpl;

import java.sql.SQLException;
import java.util.*;

public class LoanService {
    LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public void addLoan(String customerName, double loanAmount, int repaymentPeriod) {
        loanRepository.addLoan(customerName, loanAmount, repaymentPeriod);
    }

    public void displayLoans(String name) throws SQLException {
        for (Customer customer : loanRepository.getLoans().keySet()) {
            if (Objects.equals(name, customer.getName())) {
                System.out.println("\nCustomer: " + customer);
                for (Loan loan : loanRepository.getLoans().get(customer)) {
                    System.out.println("\nLoan: " + loan);
                }
                return;
            }
        }
    }

    public void displayMonthlyPayment(int loanId) throws SQLException {
        for (Customer customer : loanRepository.getLoans().keySet()) {
            for (Loan loan : loanRepository.getLoans().get(customer)) {
                if (loan.getLoanId() == loanId) {
                    System.out.println("\nMonthly payment: " + loan.calculateInterest());
                }
            }
        }
    }

    public void makePayment(int loanId, double amount) throws SQLException {
        for (Customer customer : loanRepository.getLoans().keySet()) {
            for (Loan loan : loanRepository.getLoans().get(customer)) {
                if (loan.getLoanId() == loanId) {
                    loanRepository.makeLoanPayment(loanId, amount);
                    loan.makePayment(amount);
                }
            }
        }
    }
}
