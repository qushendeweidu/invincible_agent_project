package com.laodeng.laodengaiagent.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
      * @param sortField 输入参数为排序字段，用于防止 SQL 注入
      * @return 是否合法的判断变量，true 表示合法，false 表示非法
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            //当排序字段为空时直接返回不合法sql
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
