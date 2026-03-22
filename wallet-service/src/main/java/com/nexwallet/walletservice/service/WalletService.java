package com.nexwallet.walletservice.service;

import com.nexwallet.walletservice.model.Transaction;
import com.nexwallet.walletservice.model.Transaction.TransactionStatus;
import com.nexwallet.walletservice.model.Transaction.TransactionType;
import com.nexwallet.walletservice.model.Wallet;
import com.nexwallet.walletservice.repository.TransactionRepository;
import com.nexwallet.walletservice.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    // Create a new wallet
    public Wallet createWallet(Wallet wallet) {
        if (walletRepository.existsByEmail(wallet.getEmail())) {
            throw new RuntimeException("Wallet already exists for email: " + wallet.getEmail());
        }
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(Wallet.WalletStatus.ACTIVE);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    // Get all wallets
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    // Get wallet by ID
    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
    }

    // Deposit funds
    @Transactional
    public Wallet deposit(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be greater than zero");
        }
        Wallet wallet = getWalletById(id);
        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet saved = walletRepository.save(wallet);

        // Record transaction
        recordTransaction(
            wallet.getId(), TransactionType.DEPOSIT,
            amount, balanceBefore, saved.getBalance(),
            null, "Deposit", TransactionStatus.SUCCESS
        );

        return saved;
    }

    // Withdraw funds
    @Transactional
    public Wallet withdraw(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than zero");
        }
        Wallet wallet = getWalletById(id);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        BigDecimal balanceBefore = wallet.getBalance();
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet saved = walletRepository.save(wallet);

        // Record transaction
        recordTransaction(
            wallet.getId(), TransactionType.WITHDRAWAL,
            amount, balanceBefore, saved.getBalance(),
            null, "Withdrawal", TransactionStatus.SUCCESS
        );

        return saved;
    }

    // Transfer funds between wallets
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new RuntimeException("Cannot transfer to the same wallet");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer amount must be greater than zero");
        }

        Wallet fromWallet = getWalletById(fromId);
        Wallet toWallet   = getWalletById(toId);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        BigDecimal fromBalanceBefore = fromWallet.getBalance();
        BigDecimal toBalanceBefore   = toWallet.getBalance();

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        fromWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(fromWallet);

        toWallet.setBalance(toWallet.getBalance().add(amount));
        toWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(toWallet);

        // Record TRANSFER_OUT for sender
        recordTransaction(
            fromId, TransactionType.TRANSFER_OUT,
            amount, fromBalanceBefore, fromWallet.getBalance(),
            toId, "Transfer to wallet #" + toId, TransactionStatus.SUCCESS
        );

        // Record TRANSFER_IN for receiver
        recordTransaction(
            toId, TransactionType.TRANSFER_IN,
            amount, toBalanceBefore, toWallet.getBalance(),
            fromId, "Transfer from wallet #" + fromId, TransactionStatus.SUCCESS
        );
    }

    // Get transaction history for a wallet
    public List<Transaction> getWalletTransactions(Long walletId) {
        getWalletById(walletId); // validates wallet exists
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

    // Get all transactions
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByCreatedAtDesc();
    }

    // Helper to record a transaction
    private void recordTransaction(
            Long walletId, TransactionType type,
            BigDecimal amount, BigDecimal balanceBefore,
            BigDecimal balanceAfter, Long relatedWalletId,
            String reference, TransactionStatus status) {

        Transaction tx = new Transaction();
        tx.setWalletId(walletId);
        tx.setType(type);
        tx.setAmount(amount);
        tx.setBalanceBefore(balanceBefore);
        tx.setBalanceAfter(balanceAfter);
        tx.setRelatedWalletId(relatedWalletId);
        tx.setReference(reference);
        tx.setStatus(status);
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(tx);
    }
}