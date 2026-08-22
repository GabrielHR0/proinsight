package com.prosup.proinsight.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AcademiaScopingAspect {

    private static final Logger log = LoggerFactory.getLogger(AcademiaScopingAspect.class);

    @Around("execution(* org.springframework.data.mongodb.core.MongoOperations.find(org.springframework.data.mongodb.core.query.Query, ..))")
    public Object scopeFind(ProceedingJoinPoint pjp) throws Throwable {
        applyIfScoped(pjp);
        return pjp.proceed();
    }

    @Around("execution(* org.springframework.data.mongodb.core.MongoOperations.findOne(org.springframework.data.mongodb.core.query.Query, ..))")
    public Object scopeFindOne(ProceedingJoinPoint pjp) throws Throwable {
        applyIfScoped(pjp);
        return pjp.proceed();
    }

    @Around("execution(* org.springframework.data.mongodb.core.MongoOperations.count(org.springframework.data.mongodb.core.query.Query, ..))")
    public Object scopeCount(ProceedingJoinPoint pjp) throws Throwable {
        applyIfScoped(pjp);
        return pjp.proceed();
    }

    @Around("execution(* org.springframework.data.mongodb.core.MongoOperations.exists(org.springframework.data.mongodb.core.query.Query, ..))")
    public Object scopeExists(ProceedingJoinPoint pjp) throws Throwable {
        applyIfScoped(pjp);
        return pjp.proceed();
    }

    @Around("execution(* org.springframework.data.mongodb.core.MongoOperations.findAll(..))")
    public Object scopeFindAll(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args.length > 0 && args[0] instanceof Class<?> entityClass) {
            // findAll(Class) ou findAll(Class, String collection) — sem Query,
            // re-despacha para find(Query, Class[, collection]) com escopo.
            if (!entityClass.isAnnotationPresent(ScopedByAcademia.class)) {
                return pjp.proceed();
            }
            Query query = scopedQueryOrThrow(entityClass);
            if (query == null) return pjp.proceed();
            String collectionName = args.length > 1 && args[1] instanceof String s ? s : null;
            MongoOperations ops = (MongoOperations) pjp.getTarget();
            return collectionName != null
                    ? ops.find(query, entityClass, collectionName)
                    : ops.find(query, entityClass);
        }
        applyIfScoped(pjp);
        return pjp.proceed();
    }

    @Around("execution(* org.springframework.data.mongodb.core.MongoOperations.remove(org.springframework.data.mongodb.core.query.Query, ..))")
    public Object scopeRemove(ProceedingJoinPoint pjp) throws Throwable {
        applyIfScoped(pjp);
        return pjp.proceed();
    }

    private void applyIfScoped(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args.length < 2) return;

        Query query = null;
        Class<?> entityClass = null;

        for (Object arg : args) {
            if (arg instanceof Query q) {
                query = q;
            } else if (arg instanceof Class<?> cls) {
                entityClass = cls;
            }
        }

        if (query == null || entityClass == null) return;
        if (!entityClass.isAnnotationPresent(ScopedByAcademia.class)) return;

        String tenantId = TenantContext.getAcademiaId();
        if (tenantId == null || tenantId.isBlank()) {
            if (TenantContext.isInHttpRequest()) {
                throw new AccessDeniedException(
                        "Acesso a dados de academia exige X-Academia-Id");
            }
            return;
        }

        Document queryDoc = query.getQueryObject();
        if (queryDoc != null && containsAcademiaId(queryDoc)) return;

        query.addCriteria(Criteria.where("academiaId").is(tenantId));
    }

    private Query scopedQueryOrThrow(Class<?> entityClass) {
        String tenantId = TenantContext.getAcademiaId();
        if (tenantId == null || tenantId.isBlank()) {
            if (TenantContext.isInHttpRequest()) {
                throw new AccessDeniedException(
                        "Acesso a dados de academia exige X-Academia-Id");
            }
            return null;
        }
        return Query.query(Criteria.where("academiaId").is(tenantId));
    }

    private static boolean containsAcademiaId(Document doc) {
        if (doc == null || doc.isEmpty()) return false;
        if (doc.containsKey("academiaId")) return true;
        for (var value : doc.values()) {
            if (value instanceof Document nested && containsAcademiaId(nested)) {
                return true;
            }
            if (value instanceof Iterable<?> iterable) {
                for (var item : iterable) {
                    if (item instanceof Document nestedItem && containsAcademiaId(nestedItem)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
