package com.example.mysqldemo;

import com.example.mysqldemo.mapper.AccountMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MysqldemoApplicationTests {
    @Autowired
    private AccountMapper accountMapper;


}
