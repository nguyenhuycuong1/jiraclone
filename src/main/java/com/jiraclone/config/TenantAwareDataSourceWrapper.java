package com.jiraclone.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

public class TenantAwareDataSourceWrapper implements DataSource {

    private final DataSource dataSource;

    public TenantAwareDataSourceWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        setTenantOnConnection(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = dataSource.getConnection(username, password);
        setTenantOnConnection(connection);
        return connection;
    }

    private void setTenantOnConnection(Connection connection) throws SQLException {
        String tenantId = TenantContextHolder.getTenantId();
        String value = (tenantId == null || tenantId.isEmpty()) ? "" : tenantId;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET app.current_org_id = " + toSqlLiteral(value));
        }
    }

    /** Wrap a value in single quotes, doubling any embedded quote (Postgres escape). */
    private static String toSqlLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @Override
    public java.io.PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() {
        try {
            return dataSource.getParentLogger();
        } catch (SQLFeatureNotSupportedException e) {
            throw new RuntimeException("DataSource does not support getParentLogger", e);
        }
    }

}
