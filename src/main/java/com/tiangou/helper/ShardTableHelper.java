package com.tiangou.helper;

import java.util.Objects;

public class ShardTableHelper {

    private final static ThreadLocal<String> LOCAL_TABLE_NAME = new ThreadLocal<String>();


    public static void startShardingTable(String suffixName) {
        LOCAL_TABLE_NAME.set(suffixName);
    }

    protected static String getSuffixName() {
        return LOCAL_TABLE_NAME.get();
    }

    protected static void removeSuffixName() {
        if (Objects.nonNull(getSuffixName())) {
            LOCAL_TABLE_NAME.remove();
        }
    }

}
