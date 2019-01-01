package com.darts.service.impl;

import com.darts.annotation.DartsService;
import com.darts.service.TestService;

@DartsService("testServiceImpl")
public class TestServiceImpl implements TestService {
    /**
     * 测试方法
     * @param name
     * @param age
     * @return
     */
    @Override
    public String query(String name, Integer age) {
        return "name ===== dartWH,  age ======= 27";
    }
}
