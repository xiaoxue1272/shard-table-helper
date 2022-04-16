package com.tiangou.helper;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Objects;
import java.util.StringJoiner;


@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "queryCursor", args = {MappedStatement.class, Object.class, RowBounds.class}),
                @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
        }
)
public class ShardTableInterceptor implements Interceptor {

    public ShardTableInterceptor(ShardTableProperties properties) {
        this.properties = properties;
    }

    private ShardTableProperties properties;

    public Object intercept(Invocation invocation) throws Throwable {
        String suffixName = ShardTableHelper.getSuffixName();
        if (properties.isEnable() && Objects.nonNull(suffixName)) {
            Object[] args = invocation.getArgs();
            MappedStatement mappedStatement = (MappedStatement) args[0];
            if (args.length == 6) {
                BoundSql boundSql = (BoundSql) args[5];
                args[5] = new BoundSql(mappedStatement.getConfiguration(), boundSql.getSql().replaceAll(getSuffixNameByPropertiesRule(properties), suffixName), boundSql.getParameterMappings(), args[1]);
            } else {
                args[0] = createNewMappedStatement(suffixName, mappedStatement);
            }
            Object result = invocation.getMethod().invoke(invocation.getTarget(), args);
            ShardTableHelper.removeSuffixName();
            return result;
        }
        return invocation.proceed();
    }

    private String getSuffixNameByPropertiesRule(ShardTableProperties properties) {
        return properties.getReplaceLogic().replaceAll("@", properties.getPlaceholder());
    }

    private MappedStatement createNewMappedStatement(String suffixName, MappedStatement mappedStatement) {
        return new MappedStatement
                .Builder(mappedStatement.getConfiguration(), mappedStatement.getId(),
                (parameter) -> {
                    BoundSql boundSql = mappedStatement.getBoundSql(parameter);
                    return new BoundSql(mappedStatement.getConfiguration(), boundSql.getSql().replaceAll(getSuffixNameByPropertiesRule(properties), suffixName), boundSql.getParameterMappings(), parameter);
                    },
                mappedStatement.getSqlCommandType())
                .cache(mappedStatement.getCache())
                .databaseId(mappedStatement.getDatabaseId())
                .fetchSize(mappedStatement.getFetchSize())
                .flushCacheRequired(mappedStatement.isFlushCacheRequired())
                .useCache(mappedStatement.isUseCache())
                .keyColumn(arrayToLimitedString(mappedStatement.getKeyColumns()))
                .keyGenerator(mappedStatement.getKeyGenerator())
                .keyProperty(arrayToLimitedString(mappedStatement.getKeyProperties()))
                .lang(mappedStatement.getLang())
                .parameterMap(mappedStatement.getParameterMap())
                .resource(mappedStatement.getResource())
                .resultMaps(mappedStatement.getResultMaps())
                .resultOrdered(mappedStatement.isResultOrdered())
                .resultSets(arrayToLimitedString(mappedStatement.getResultSets()))
                .resultSetType(mappedStatement.getResultSetType())
                .statementType(mappedStatement.getStatementType())
                .timeout(mappedStatement.getTimeout())
                .build();
    }

    private String arrayToLimitedString(String... array) {
        if (Objects.nonNull(array) && array.length > 1) {
            StringJoiner joiner = new StringJoiner(",");
            for (String value : array) {
                if (Objects.isNull(value) || value.isEmpty()) {
                    continue;
                }
                joiner.add(value);
            }
            return joiner.toString();
        }
        return null;
    }
}
