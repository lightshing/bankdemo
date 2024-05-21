package com.example.mysqldemo.controller;

import com.example.mysqldemo.domain.Account;
import com.example.mysqldemo.domain.Response;
import com.example.mysqldemo.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountController {

    @Autowired
    private AccountService accountService;

    @RequestMapping("/display")
    public Response<List<Account>> display() {
        return accountService.serviceDisplayALl();
    }

    @PostMapping("/search/{keywords}")
    public Response<List<Account>> search(@PathVariable String keywords) {
        return accountService.serviceSearch(keywords);
    }

    @PostMapping("/edit/{id}")
    public Response<Account> edit(@PathVariable int id, @RequestBody int value) {
        return accountService.serviceEdit(id, value);
    }

    @PostMapping("/walfare/{cutoffage}")
    public Response<List<Account>> walfare(@PathVariable int cutoffage) {
        return accountService.serviceWelfare(cutoffage);
    }

    @PostMapping("/fee/{cutoffage}")
    public Response<Account> fee(@PathVariable int cutoffage, @RequestBody int value) {
        return accountService.serviceFee(cutoffage, value);
    }

}
