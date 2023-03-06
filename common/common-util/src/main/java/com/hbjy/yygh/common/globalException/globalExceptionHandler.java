package com.hbjy.yygh.common.globalException;

import com.hbjy.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//全局异常处理
//如果controller出现了异常，则进入全局异常处理，而不是一整页的错误信息
@ControllerAdvice
public class globalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result errors(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }

    //自定义异常类捕捉，如果出现了自定义异常YyghException 就会进入到这里面
    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public Result yyghErrors(Exception e) {
        e.printStackTrace();
        return Result.fail(e.getMessage());
    }
}
