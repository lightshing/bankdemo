package com.example.mysqldemo.service;

import com.example.mysqldemo.domain.Account;
import com.example.mysqldemo.domain.Response;
import com.example.mysqldemo.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {
    @Autowired
    private AccountMapper accountMapper;
    private Object Collectors;

    public Response<List<Account>> serviceDisplayALl() {
            Response<List<Account>> response = new Response<>();
            response.setCode(200);
            response.setMessage("success");
            response.setData(accountMapper.displayAll());
            return response;
        }

        public Response<List<Account>> serviceSearch(String keywords) {
            Response<List<Account>> response = new Response<>();
            response.setCode(200);
            response.setMessage("success");
            response.setData(accountMapper.search(keywords));
            return response;
        }

        //交易控制
       //@Transactional
       public Response<Account> serviceEdit(int id, int value){
        Response<Account> response = new Response<>();
        Account tempAccount = accountMapper.getAccountById(id);
            try{
                if(tempAccount == null){
                    response.setCode(404);
                    throw new Exception("用户不存在");
                } else if (tempAccount.getStatuscode() == 1) {
                    response.setCode(-500);
                    throw new Exception("账户异常");
                } else if (tempAccount.getBalance() < value) {
                    response.setCode(-200);
                    throw new Exception("余额不足");
                }
                tempAccount.setBalance(tempAccount.getBalance() - value);
                accountMapper.insertAccount(tempAccount);
                accountMapper.deleteAccountById(id);
                response.setCode(200);
                response.setMessage("交易成功");
                response.setData(tempAccount);
            }catch (Exception e){
                response.setMessage(e.getMessage());
                response.setData(tempAccount);
            }
            return response;
        }

//对cutoff年龄之上的账户扣款value，如遇异常账户（statuscode1），对所有交易回滚
    //单线程
    @Transactional
    public Response<Account> serviceFee(int cutoff, int value){
        Response<Account> response = new Response<>();
        List<Account> relatedAccounts = accountMapper.searchOverAge(cutoff);
        try{
            for(Account relatedAccount : relatedAccounts){
                response.setData(relatedAccount);
                if(relatedAccount == null){
                    response.setCode(404);
                    throw new Exception("用户不存在");
                } else if (relatedAccount.getStatuscode() == 1) {
                    response.setCode(-500);
                    throw new Exception("账户异常");
                } else if (relatedAccount.getBalance() < value) {
                    response.setCode(-200);
                    throw new Exception("余额不足");
                }
                relatedAccount.setBalance(relatedAccount.getBalance() - value);
                accountMapper.deleteAccountById(relatedAccount.getId());
                accountMapper.insertAccount(relatedAccount);
                response.setCode(200);
                response.setMessage("交易成功");
                response.setData(null);
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            response.setMessage(e.getMessage());
        }
        return response;
    }


//多线程
    //对于一定年龄之上的账户发放100000的福利，仅能撤销对非法账户单笔交易回滚（线程内）

    private ExecutorService executorService = Executors.newFixedThreadPool(6);
    @Transactional
    public Response<List<Account>> serviceWelfare(int cutoff){
        Response<List<Account>> response = new Response<>();
        List<Account> relatedAccounts = accountMapper.searchOverAge(cutoff);
        List<CompletableFuture<Void>> futures = relatedAccounts.stream()
                .map(account -> CompletableFuture.runAsync(() -> {
                    try {
                        if (account.getStatuscode() == 1) {
                            throw new IllegalArgumentException("非法账户");
                        }
                        account.setBalance(account.getBalance() + 100000);
                        accountMapper.deleteAccountById(account.getId());
                        accountMapper.insertAccount(account);
                    } catch (Exception e) {
                        synchronized (response) {
                            response.setMessage(e.getMessage());
                            response.setCode(-500);
                            response.setData(relatedAccounts);
                        }
                    }
                }, executorService))
                .collect(java.util.stream.Collectors.toList());
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();
        if (response.getCode() == -500) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }else{
            response.setCode(200);
            response.setMessage("success");
            response.setData(relatedAccounts);
        }
        return response;
    }

}




