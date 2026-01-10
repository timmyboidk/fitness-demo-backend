package com.example.fitness.common.handler;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 敏感字段自动加解密 Handler
 * 使用 AES 算法
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    private static AES aes;

    /**
     * 设置加密密钥，由外部配置类初始化时调用
     */
    public static void setKey(String key) {
        if (StringUtils.hasText(key)) {
            aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
        }
    }

    private AES getAes() {
        if (aes == null) {
            // 以此作为 fallback 或抛出异常
            aes = SecureUtil.aes("default-fallback-key".getBytes(StandardCharsets.UTF_8));
        }
        return aes;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
            throws SQLException {
        if (StringUtils.hasText(parameter)) {
            // 加密存储
            ps.setString(i, getAes().encryptBase64(parameter));
        } else {
            ps.setString(i, parameter);
        }
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return decrypt(columnValue);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValue = rs.getString(columnIndex);
        return decrypt(columnValue);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String columnValue = cs.getString(columnIndex);
        return decrypt(columnValue);
    }

    private String decrypt(String value) {
        if (StringUtils.hasText(value)) {
            try {
                // 解密返回
                return getAes().decryptStr(value);
            } catch (Exception e) {
                // 解密失败（可能是老数据未加密），返回原值
                return value;
            }
        }
        return value;
    }
}
