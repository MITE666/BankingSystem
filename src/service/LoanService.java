package service;

import model.*;
import repository.LoanRepository;
import repository.LoanRepositoryImpl;

import java.util.*;

public class LoanService {
    LoanRepository loanRepository;

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public void addLoan(String customerName, double loanAmount, int repaymentPeriod) {
        loanRepository.addLoan(customerName, loanAmount, repaymentPeriod);
    }

    public void displayLoans(String name) {
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

    public void displayMonthlyPayment(int loanId) {
        for (Customer customer : loanRepository.getLoans().keySet()) {
            for (Loan loan : loanRepository.getLoans().get(customer)) {
                if (loan.getLoanId() == loanId) {
                    System.out.println("\nMonthly payment: " + loan.calculateInterest());
                }
            }
        }
    }

    public void makePayment(int loanId, double amount) {
        for (Customer customer : loanRepository.getLoans().keySet()) {
            for (Loan loan : loanRepository.getLoans().get(customer)) {
                if (loan.getLoanId() == loanId) {
                    loan.makePayment(amount);
                }
            }
        }
    }
}
