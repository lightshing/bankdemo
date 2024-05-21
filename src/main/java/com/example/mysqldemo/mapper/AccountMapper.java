package com.example.mysqldemo.mapper;

import com.example.mysqldemo.domain.Account;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper {
    List<Account> displayAll();
    List<Account> search(String keywords);
    List<Account> searchOverAge(int cutoffAge);
    Account getAccountById(int id);
    void insertAccount(Account account);
    void deleteAccountById(int id);

}
