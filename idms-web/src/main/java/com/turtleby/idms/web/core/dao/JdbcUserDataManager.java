package com.turtleby.idms.web.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.turtleby.idms.web.core.entity.User;

/**
 * JDBC-based implementation of {@link UserDataManager} for managing user data operations.
 *
 * <p>This class uses Spring's {@link JdbcTemplate} to execute SQL queries against a relational
 * database. It provides a straightforward implementation for retrieving user information based on
 * username.
 *
 * <p>The SQL query and row mapper are configurable via setter methods, allowing for customization
 * in testing scenarios or when extending functionality.
 *
 * @see UserDataManager
 * @see JdbcTemplate
 */
public class JdbcUserDataManager implements UserDataManager {
  private static final String GET_USER_SQL =
      "select id, username, password, is_active from users where username = ?";

  private RowMapper<User> rowMapper = new UserRowMapper();
  private String getUserSql = GET_USER_SQL;

  private final JdbcTemplate jdbcTemplate;

  public JdbcUserDataManager(final DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  public JdbcUserDataManager(final JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation executes a SQL query to retrieve user information from the {@code users}
   * table.
   *
   * @param username the username to search for; must not be {@code null}
   * @return the {@link User} associated with the given username, or {@code null} if no user is
   *     found
   * @throws DataAccessException if there is an error executing the query
   * @throws IncorrectResultSizeDataAccessException if more than one user is found with the given
   *     username (indicates a data integrity issue)
   */
  @Override
  public User getUserByUsername(final String username)
      throws DataAccessException, IncorrectResultSizeDataAccessException {
    Objects.requireNonNull(username, "username must not be null");
    List<User> users = jdbcTemplate.query(getUserSql, rowMapper, username);
    return DataAccessUtils.singleResult(users);
  }

  /**
   * Sets a custom SQL query for retrieving users by username.
   *
   * @param getUserSql the SQL query to use; must not be {@code null}
   */
  public void setGetUserSql(final String getUserSql) {
    this.getUserSql = Objects.requireNonNull(getUserSql, "getUserSql must not be null");
  }

  /**
   * Sets a custom {@link RowMapper} for mapping result set rows to {@link User} objects.
   *
   * @param rowMapper the row mapper to use; must not be {@code null}
   */
  public void setRowMapper(final RowMapper<User> rowMapper) {
    this.rowMapper = Objects.requireNonNull(rowMapper, "rowMapper must not be null");
  }

  private static class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(final ResultSet rs, final int rowNum) throws SQLException {
      return new User(
          rs.getString("id"),
          rs.getString("username"),
          rs.getString("password"),
          rs.getBoolean("is_active"));
    }
  }
}
