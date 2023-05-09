package com.tuwien.gitanalyser.configuration.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Aspect
@Configuration
public class LoggingAspect {

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("@within(org.springframework.stereotype.Repository )"
                  + " || @within(org.springframework.stereotype.Service )"
                  + " || @within(org.springframework.web.bind.annotation.RestController )")
    public void springBeanPointcut() { }

    /**
     * Pointcut that matches all Spring beans in the application's main packages.
     */
    @Pointcut("execution(* com.tuwien.gitanalyser..*(..)) "
                  + "&& !execution(* com.tuwien.gitanalyser.configuration..*(..)) "
                  + "&& ! execution(* com.tuwien.gitanalyser.security.SecurityConfiguration..*.*()) ")
    public void applicationPackagePointcut() { }

    @Before(value = "applicationPackagePointcut() && springBeanPointcut()")
    public void logBeforeAllMethods(final JoinPoint joinPoint) {
        CodeSignature methodSignature = (CodeSignature) joinPoint.getSignature();
        List<String> parameters = getParameters(joinPoint, methodSignature);
        log(joinPoint, joinPoint.getSignature().getName() + " started (" + String.join("; ", parameters) + ")");
    }

    @AfterThrowing(value = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAllExceptionsInProject(final JoinPoint jp, final Throwable e) {
        logException(jp, e);
    }

    private List<String> getParameters(final JoinPoint joinPoint, final CodeSignature methodSignature) {
        String[] sigParamNames = methodSignature.getParameterNames();
        Object[] signatureArgs = joinPoint.getArgs();
        List<String> parameters = new ArrayList<>();
        if (signatureArgs != null && sigParamNames != null) {
            for (int i = 0; i < signatureArgs.length; i++) {
                if (sigParamNames[i] != null && signatureArgs[i] != null
                        && !Objects.equals(sigParamNames[i], "accessToken")
                        && !Objects.equals(sigParamNames[i], "refreshToken")) {
                    parameters.add(sigParamNames[i] + ": " + signatureArgs[i].toString());
                }
            }
        }
        return parameters;
    }

    private void logException(final JoinPoint joinPoint, final Throwable e) {

        Logger loggerInstance = getLogger(joinPoint);

        if (loggerInstance != null) {
            loggerInstance.error("{}\r\n in {}: ", e.toString(), joinPoint.getSignature().toShortString(), e);
        }
    }

    private Logger getLogger(final JoinPoint joinPoint) {
        return LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringType().getCanonicalName());
    }

    private void log(final JoinPoint joinPoint, final String message) {

        Logger loggerInstance = getLogger(joinPoint);

        if (loggerInstance != null) {
            loggerInstance.info(message);
        }
    }
}
