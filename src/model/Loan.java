package model;

import java.util.Date;

public class Loan {
    private int loanId;
    private double loanAmount;
    private double interestRate;
    private int repaymentPeriod;
    private double currentBalance;

    public int getLoanId() {
        return loanId;
    }

    public Loan(int loanId, double loanAmount, double interestRate, int repaymentPeriodMonths) {
        this.loanId = loanId;
        this.loanAmount = loanAmount;
        this.interestRate = interestRate;
        this.currentBalance = loanAmount;
        this.repaymentPeriod = repaymentPeriodMonths;
    }

    public double calculateInterest() {
        double monthlyInterestRate = interestRate / repaymentPeriod;
        return loanAmount * monthlyInterestRate;
    }

    public boolean isFullyRepaid() {
        return currentBalance == 0;
    }

    public void makePayment(double amount) {
        currentBalance -= amount;
        if (currentBalance < 0) {
            currentBalance = 0;
        }
    }

    public void setCurrentBalance(double balance) {
        this.currentBalance = balance;
    }

    @Override
    public String toString() {
        return "\nLoan ID: " + loanId +
                "\nMoney left to be repaid: " + currentBalance;
    }
}
