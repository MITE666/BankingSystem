package model;

public class SavingsAccount extends BankAccount{
    private double interestRate;

    public SavingsAccount(int accountNumber, double interestRate) {
        super(accountNumber);
        this.interestRate = interestRate;
    }

    public void calculateInterest() {
        double interest = getBalance() * interestRate / 100;
        deposit(interest);
    }

    @Override
    public String toString() {
        return "Savings Account" + super.toString();
    }
}
