package com.nhnacademy.shoppingmall.global.common.mvc.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface BaseController {
    String execute(HttpServletRequest req, HttpServletResponse resp);
}