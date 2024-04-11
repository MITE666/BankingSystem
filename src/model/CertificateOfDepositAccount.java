package model;

public class CertificateOfDepositAccount extends BankAccount {
    private double interestRate;
    private int termMonths;

    public CertificateOfDepositAccount(int accountNumber, double interestRate, int termMonths) {
        super(accountNumber);
        this.interestRate = interestRate;
        this.termMonths = termMonths;
    }

    @Override
    public boolean withdraw(double amount) {
        // Cannot withdraw from Certificate of Deposit account before maturity
        return false;
    }

    public void calculateInterest() {
        double interest = getBalance() * interestRate / 100 * (termMonths / 12.0);
        deposit(interest);
    }

    @Override
    public String toString() {
        return "Certificate of Deposit Account" + super.toString();
    }
}
