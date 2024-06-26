package com.nhnacademy.shoppingmall.global.common.mvc.servlet;

import com.nhnacademy.shoppingmall.domain.category.domain.Categories;
import com.nhnacademy.shoppingmall.domain.category.repository.CategoryRepositoryImpl;
import com.nhnacademy.shoppingmall.domain.point.exception.PointChargeException;
import com.nhnacademy.shoppingmall.global.common.mvc.exception.ControllerNotFoundException;
import com.nhnacademy.shoppingmall.global.common.mvc.exception.PageNotFoundException;
import com.nhnacademy.shoppingmall.global.common.mvc.transaction.DbConnectionThreadLocal;
import com.nhnacademy.shoppingmall.global.common.mvc.view.ViewResolver;
import com.nhnacademy.shoppingmall.global.common.mvc.controller.BaseController;
import com.nhnacademy.shoppingmall.global.common.mvc.controller.ControllerFactory;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static javax.servlet.RequestDispatcher.*;

@Slf4j
@WebServlet(name = "frontServlet", urlPatterns = {"*.do"})
public class FrontServlet extends HttpServlet {
    private ControllerFactory controllerFactory;
    private ViewResolver viewResolver;

    @Override
    public void init() throws ServletException {
        //todo7-1 controllerFactory를 초기화 합니다.
        controllerFactory = (ControllerFactory) getServletContext().getAttribute("CONTEXT_CONTROLLER_FACTORY");
        //todo7-2 viewResolver를 초기화 합니다.
        viewResolver = new ViewResolver();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            //todo7-3 Connection pool로 부터 connection 할당 받습니다. connection은 Thread 내에서 공유됩니다.
            DbConnectionThreadLocal.initialize();

            BaseController baseController = (BaseController) controllerFactory.getController(req);
            String viewName = baseController.execute(req, resp);

            if (viewName != null) {
                if (viewResolver.isRedirect(viewName)) {
                    String redirectUrl = viewResolver.getRedirectUrl(viewName);
                    log.info("redirectUrl:{}", redirectUrl);
                    //todo7-6 redirect: 로 시작하면  해당 url로 redirect 합니다.
                    resp.sendRedirect(redirectUrl);
                } else {
                    String layout = viewResolver.getLayOut(viewName);
                    log.info("viewName:{}", viewResolver.getPath(viewName));

                    if (viewResolver.getPath(viewName).endsWith(".jsp")) {
                        initCategory(req);
                    }

                    req.setAttribute(ViewResolver.LAYOUT_CONTENT_HOLDER, viewResolver.getPath(viewName));
                    RequestDispatcher rd = req.getRequestDispatcher(layout);


                    String filePath = getServletContext().getRealPath("/WEB-INF/views/layout/shop.jsp");
                    log.info("filePath = {}", filePath);
                    File file = new File(filePath);

                    if (!file.exists()) {
                        throw new PageNotFoundException("Not found Page = " + layout);
                    }

                    rd.include(req, resp);
                }
            }
        } catch (Exception e) {
            log.error("error: {}", e.getMessage());
            log.error("error trace : {}", e.getStackTrace());
            log.error("error cause : {}", e.getCause());

            req.setAttribute("status_code", req.getAttribute(ERROR_STATUS_CODE));
            req.setAttribute("exception_type", req.getAttribute(ERROR_EXCEPTION_TYPE));
            req.setAttribute("message", req.getAttribute(ERROR_MESSAGE));
            req.setAttribute("exception", req.getAttribute(ERROR_EXCEPTION));
            req.setAttribute("request_uri", req.getAttribute(ERROR_REQUEST_URI));
            DbConnectionThreadLocal.setSqlError(true);
            RequestDispatcher rd = null;

            //todo7-5 예외가 발생하면 해당 예외에 대해서 적절한 처리를 합니다.
            if (e instanceof PageNotFoundException) {
                rd = req.getRequestDispatcher("/WEB-INF/views/error/500.jsp");
            } else if (e instanceof ControllerNotFoundException) {
                rd = req.getRequestDispatcher("/WEB-INF/views/error/404.jsp");
            } else if (e instanceof PointChargeException) {
                req.setAttribute("message", "포인트 충전에 오류가 발생했습니다. 관리자에게 문의해주세요");
                rd = req.getRequestDispatcher("/WEB-INF/views/error/error.jsp");
            } else {
                rd = req.getRequestDispatcher("/WEB-INF/views/error/500.jsp");
            }
            rd.forward(req, resp);
        } finally {
            //todo7-4 connection을 반납합니다.
            DbConnectionThreadLocal.reset();
        }
    }

    public void initCategory(HttpServletRequest req) {
        CategoryRepositoryImpl categoryRepositoryImpl = new CategoryRepositoryImpl();
        List<Categories> categorys = categoryRepositoryImpl.selectCategory();
        req.setAttribute("categories", categorys);
    }

}