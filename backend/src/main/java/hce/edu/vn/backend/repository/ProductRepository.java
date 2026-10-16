package hce.edu.vn.backend.repository;

import hce.edu.vn.backend.model.Product;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
	private static final String SELECT_COLUMNS = "ProductID, ProductName, SupplierID, CategoryID, "
			+ "QuantityPerUnit, UnitPrice, UnitsInStock, UnitsOnOrder, ReorderLevel, Discontinued";
	private static final String INSERT_SQL = "INSERT INTO dbo.Products "
			+ "(ProductName, SupplierID, CategoryID, QuantityPerUnit, UnitPrice, UnitsInStock, "
			+ "UnitsOnOrder, ReorderLevel, Discontinued) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_SQL = "UPDATE dbo.Products SET ProductName = ?, SupplierID = ?, "
			+ "CategoryID = ?, QuantityPerUnit = ?, UnitPrice = ?, UnitsInStock = ?, UnitsOnOrder = ?, "
			+ "ReorderLevel = ?, Discontinued = ? WHERE ProductID = ?";

	private final JdbcTemplate jdbcTemplate;

	public ProductRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Product> findAll() {
		return jdbcTemplate.query("SELECT " + SELECT_COLUMNS + " FROM dbo.Products ORDER BY ProductID",
				this::mapProduct);
	}

	public Optional<Product> findById(int productId) {
		return jdbcTemplate.query("SELECT " + SELECT_COLUMNS + " FROM dbo.Products WHERE ProductID = ?",
				this::mapProduct, productId).stream().findFirst();
	}

	public boolean supplierExists(int supplierId) {
		return exists("dbo.Suppliers", "SupplierID", supplierId);
	}

	public boolean categoryExists(int categoryId) {
		return exists("dbo.Categories", "CategoryID", categoryId);
	}

	public Product insert(Product product) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		PreparedStatementCreator creator = connection -> {
			PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[] { "ProductID" });
			bindProduct(statement, product);
			return statement;
		};
		jdbcTemplate.update(creator, keyHolder);
		Number generatedId = keyHolder.getKey();
		if (generatedId == null) {
			throw new IllegalStateException("SQL Server did not return the generated ProductID.");
		}
		return findById(generatedId.intValue()).orElseThrow();
	}

	public int update(int productId, Product product) {
		return jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(UPDATE_SQL);
			bindProduct(statement, product);
			statement.setInt(10, productId);
			return statement;
		});
	}

	public int deleteById(int productId) {
		return jdbcTemplate.update("DELETE FROM dbo.Products WHERE ProductID = ?", productId);
	}

	private boolean exists(String table, String keyColumn, int id) {
		String sql = "SELECT CAST(CASE WHEN EXISTS (SELECT 1 FROM " + table + " WHERE " + keyColumn
				+ " = ?) THEN 1 ELSE 0 END AS bit)";
		Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, id);
		return Boolean.TRUE.equals(result);
	}

	private Product mapProduct(ResultSet resultSet, int rowNumber) throws SQLException {
		return new Product(
				resultSet.getInt("ProductID"),
				resultSet.getString("ProductName"),
				nullableInteger(resultSet, "SupplierID"),
				nullableInteger(resultSet, "CategoryID"),
				resultSet.getString("QuantityPerUnit"),
				resultSet.getBigDecimal("UnitPrice"),
				nullableShort(resultSet, "UnitsInStock"),
				nullableShort(resultSet, "UnitsOnOrder"),
				nullableShort(resultSet, "ReorderLevel"),
				resultSet.getBoolean("Discontinued"));
	}

	private Integer nullableInteger(ResultSet resultSet, String column) throws SQLException {
		int value = resultSet.getInt(column);
		return resultSet.wasNull() ? null : value;
	}

	private Short nullableShort(ResultSet resultSet, String column) throws SQLException {
		short value = resultSet.getShort(column);
		return resultSet.wasNull() ? null : value;
	}

	private void bindProduct(PreparedStatement statement, Product product) throws SQLException {
		statement.setString(1, product.productName());
		setNullable(statement, 2, product.supplierId(), Types.INTEGER);
		setNullable(statement, 3, product.categoryId(), Types.INTEGER);
		setNullable(statement, 4, product.quantityPerUnit(), Types.NVARCHAR);
		setNullable(statement, 5, product.unitPrice(), Types.DECIMAL);
		setNullable(statement, 6, product.unitsInStock(), Types.SMALLINT);
		setNullable(statement, 7, product.unitsOnOrder(), Types.SMALLINT);
		setNullable(statement, 8, product.reorderLevel(), Types.SMALLINT);
		statement.setBoolean(9, product.discontinued());
	}

	private void setNullable(PreparedStatement statement, int index, Object value, int sqlType) throws SQLException {
		if (value == null) {
			statement.setNull(index, sqlType);
		} else {
			statement.setObject(index, value, sqlType);
		}
	}
}