package com.darts.controller;

import com.darts.annotation.DartsAutowired;
import com.darts.annotation.DartsController;
import com.darts.annotation.DartsRequestMapping;
import com.darts.annotation.DartsRequestParam;
import com.darts.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@DartsController
@DartsRequestMapping("/darts")
public class TestController {

    @DartsAutowired("testServiceImpl")
    private TestService testService;

    @DartsRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @DartsRequestParam("name") String name, @DartsRequestParam("age") Integer age){
        try {
            PrintWriter printWriter = response.getWriter();
            String result = testService.query(name,age);
            printWriter.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
