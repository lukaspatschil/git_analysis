package com.tuwien.gitanalyser.configuration.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.function.Function;

@Component
public class MdcInterceptor implements HandlerInterceptor {

    private final Function<Void, String> getRandomString = (nothing) -> UUID.randomUUID()
                                                                            .toString()
                                                                            .replaceAll("-", "");

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) {

        MDC.put("requestId", getRandomString.apply(null));
        return true;
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                                final Object handler, final Exception ex) {
        MDC.remove("requestId");
    }
}
