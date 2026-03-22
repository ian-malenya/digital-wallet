package com.nexwallet.walletservice.controller;

import com.nexwallet.walletservice.model.Transaction;
import com.nexwallet.walletservice.model.Wallet;
import com.nexwallet.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
    org.springframework.web.bind.annotation.RequestMethod.GET,
    org.springframework.web.bind.annotation.RequestMethod.POST,
    org.springframework.web.bind.annotation.RequestMethod.PUT,
    org.springframework.web.bind.annotation.RequestMethod.DELETE,
    org.springframework.web.bind.annotation.RequestMethod.OPTIONS
})
public class WalletController {

    private final WalletService walletService;

    // POST /api/wallets
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody Wallet wallet) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(walletService.createWallet(wallet));
    }

    // GET /api/wallets
    @GetMapping
    public ResponseEntity<List<Wallet>> getAllWallets() {
        return ResponseEntity.ok(walletService.getAllWallets());
    }

    // GET /api/wallets/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWalletById(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    // POST /api/wallets/{id}/deposit
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Wallet> deposit(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> request) {
        return ResponseEntity.ok(walletService.deposit(id, request.get("amount")));
    }

    // POST /api/wallets/{id}/withdraw
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> request) {
        return ResponseEntity.ok(walletService.withdraw(id, request.get("amount")));
    }

    // POST /api/wallets/transfer
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody Map<String, Object> request) {
        Long fromId = Long.valueOf(request.get("fromWalletId").toString());
        Long toId   = Long.valueOf(request.get("toWalletId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        walletService.transfer(fromId, toId, amount);
        return ResponseEntity.ok("Transfer successful");
    }

    // GET /api/wallets/{id}/transactions
    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getWalletTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWalletTransactions(id));
    }

    // GET /api/wallets/transactions/all
    @GetMapping("/transactions/all")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(walletService.getAllTransactions());
    }

    // Handle errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }
}