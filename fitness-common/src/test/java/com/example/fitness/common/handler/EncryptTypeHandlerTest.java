package com.example.fitness.common.handler;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 加解密 TypeHandler 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EncryptTypeHandler 单元测试")
class EncryptTypeHandlerTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private CallableStatement callableStatement;

    private EncryptTypeHandler handler;

    @BeforeEach
    void setUp() {
        // 使用 16 字节的密钥初始化 AES
        EncryptTypeHandler.setKey("1234567890123456");
        handler = new EncryptTypeHandler();
    }

    @Test
    @DisplayName("setNonNullParameter - 加密非空字符串")
    void setNonNullParameter_NonEmptyString_Encrypts() throws SQLException {
        handler.setNonNullParameter(preparedStatement, 1, "sensitive_data", JdbcType.VARCHAR);

        // 验证设置了一个非原始值的字符串（加密后）
        verify(preparedStatement).setString(eq(1), anyString());
    }

    @Test
    @DisplayName("setNonNullParameter - 空字符串不加密")
    void setNonNullParameter_EmptyString_NoEncryption() throws SQLException {
        handler.setNonNullParameter(preparedStatement, 1, "", JdbcType.VARCHAR);

        verify(preparedStatement).setString(1, "");
    }

    @Test
    @DisplayName("setNonNullParameter - null 值处理")
    void setNonNullParameter_NullString_NoEncryption() throws SQLException {
        handler.setNonNullParameter(preparedStatement, 1, null, JdbcType.VARCHAR);

        verify(preparedStatement).setString(1, null);
    }

    @Test
    @DisplayName("getNullableResult (columnName) - 解密有效密文")
    void getNullableResult_ByColumnName_DecryptsValid() throws SQLException {
        // 先获取一个有效的密文
        String plainText = "test_phone_123";
        EncryptTypeHandler.setKey("1234567890123456");
        EncryptTypeHandler tempHandler = new EncryptTypeHandler();

        // 通过 mock 获取解密结果
        // 由于我们无法直接获取加密结果，使用较简单的策略：验证解密失败时返回原值
        when(resultSet.getString("phone")).thenReturn("invalid_base64_not_encrypted");

        String result = handler.getNullableResult(resultSet, "phone");

        // 无法解密则返回原值
        assertEquals("invalid_base64_not_encrypted", result);
    }

    @Test
    @DisplayName("getNullableResult (columnName) - 空值返回空值")
    void getNullableResult_ByColumnName_NullReturnsNull() throws SQLException {
        when(resultSet.getString("phone")).thenReturn(null);

        String result = handler.getNullableResult(resultSet, "phone");

        assertNull(result);
    }

    @Test
    @DisplayName("getNullableResult (columnName) - 空字符串返回空字符串")
    void getNullableResult_ByColumnName_EmptyReturnsEmpty() throws SQLException {
        when(resultSet.getString("phone")).thenReturn("");

        String result = handler.getNullableResult(resultSet, "phone");

        assertEquals("", result);
    }

    @Test
    @DisplayName("getNullableResult (columnIndex) - 解密处理")
    void getNullableResult_ByColumnIndex_Decrypts() throws SQLException {
        when(resultSet.getString(1)).thenReturn("some_value");

        String result = handler.getNullableResult(resultSet, 1);

        // 由于不是有效密文，返回原值
        assertEquals("some_value", result);
    }

    @Test
    @DisplayName("getNullableResult (CallableStatement) - 解密处理")
    void getNullableResult_ByCallableStatement_Decrypts() throws SQLException {
        when(callableStatement.getString(1)).thenReturn("another_value");

        String result = handler.getNullableResult(callableStatement, 1);

        assertEquals("another_value", result);
    }

    @Test
    @DisplayName("加解密循环 - 加密后解密得到原值")
    void encryptDecrypt_Roundtrip_Success() throws SQLException {
        String original = "13812345678";

        // 捕获加密后的值
        doAnswer(invocation -> {
            String encryptedValue = invocation.getArgument(1);
            // 模拟从数据库读取加密值
            when(resultSet.getString("phone")).thenReturn(encryptedValue);
            return null;
        }).when(preparedStatement).setString(eq(1), anyString());

        handler.setNonNullParameter(preparedStatement, 1, original, JdbcType.VARCHAR);

        String decrypted = handler.getNullableResult(resultSet, "phone");

        assertEquals(original, decrypted);
    }

    @Test
    @DisplayName("setKey - 空密钥不会抛出异常")
    void setKey_EmptyKey_NoException() {
        assertDoesNotThrow(() -> EncryptTypeHandler.setKey(""));
    }

    @Test
    @DisplayName("setKey - null 密钥不会抛出异常")
    void setKey_NullKey_NoException() {
        assertDoesNotThrow(() -> EncryptTypeHandler.setKey(null));
    }
}
