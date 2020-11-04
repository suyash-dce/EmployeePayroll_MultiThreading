import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class EmployeePayrollDBIO {
	
	public enum StatementType 
		{PREPARED_STATEMENT, STATEMENT}

	private PreparedStatement payrollDataStatement;
	private static EmployeePayrollDBIO payrollDBobj;
	
	private Connection getConnection() throws SQLException {
		String jdbcURL = "jdbc:mysql://localhost:3306/employee_payroll?useSSL=false";
		String userName = "root";
		String password = "jain1234";
		Connection connection;
		System.out.println("Connecting to database: " + jdbcURL);
		connection = DriverManager.getConnection(jdbcURL, userName, password);
		System.out.println("Connection successful!!" + connection);
		return connection;
	}
	
	private void preparedStatementForEmployeeData() {
		try {
			Connection connection = this.getConnection();
			String query = "SELECT * FROM employee_payroll WHERE NAME = ?";
			payrollDataStatement = connection.prepareStatement(query);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}
	
	public static EmployeePayrollDBIO getInstance() {
		if (payrollDBobj == null)
			payrollDBobj = new EmployeePayrollDBIO();
		return payrollDBobj;
	}
	
	public List<EmployeePayrollData> getEmployeePayrollDataUsingSQLQuery(String query){
		List<EmployeePayrollData> payrollList = new ArrayList<EmployeePayrollData>();
		try(Connection connection = this.getConnection()) {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			payrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return payrollList;
	}
	
	public List<EmployeePayrollData> getEmployeePayrollData(String name) {
		List<EmployeePayrollData> payrollList = null;
		if (this.payrollDataStatement == null)
			this.preparedStatementForEmployeeData();
		try {
			payrollDataStatement.setString(1, name);
			ResultSet resultSet = payrollDataStatement.executeQuery();
			payrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return payrollList;
	}
	
	public List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) {
		List<EmployeePayrollData> payrollList = new ArrayList<>();
		try {
			while (resultSet.next()) {
				int id = resultSet.getInt("ID");
				String name = resultSet.getString("NAME");
				double salary = resultSet.getDouble("SALARY");
				LocalDate startDate = resultSet.getDate("START").toLocalDate();
				payrollList.add(new EmployeePayrollData(id, name, salary, startDate));
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return payrollList;
	}
	
	public List<EmployeePayrollData> readData() {
		String query = "SELECT * FROM employee_payroll;";
		return this.getEmployeePayrollDataUsingSQLQuery(query);
	}
	
	public int updateEmployeeData(String name, double salary, StatementType type) {
		switch (type) {
		case STATEMENT:
			return this.updateDataUsingStatement(name, salary);
		case PREPARED_STATEMENT:
			return this.updateDataUsingPreparedStatement(name, salary);
		default:
			return 0;
		}
	}
	
	public int updateDataUsingStatement(String name, double salary) {
		String query = String.format("UPDATE employee_payroll SET SALARY = %.2f WHERE NAME = '%s';", salary, name);
		try (Connection connection = this.getConnection();) {
			Statement statement = connection.createStatement();
			return statement.executeUpdate(query);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	
	private int updateDataUsingPreparedStatement(String name, double salary) {
		String query = "UPDATE employee_payroll SET SALARY = ? WHERE NAME = ?";
		try (Connection connection = this.getConnection();) {
			PreparedStatement preparedStatementUpdate = connection.prepareStatement(query);
			preparedStatementUpdate.setDouble(1, salary);
			preparedStatementUpdate.setString(2, name);
			return preparedStatementUpdate.executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return 0;
	}
	
	public List<EmployeePayrollData> getEmployeesInGivenDateRange(String date1, String date2) {
		String query = String.format("SELECT * FROM employee_payroll WHERE START BETWEEN '%s' AND '%s';", date1, date2);
		return this.getEmployeePayrollDataUsingSQLQuery(query);
	}
	

	public Map<String, Double> getAverageSalaryByGender() {
		String query = "SELECT GENDER,AVG(SALARY) FROM employee_payroll GROUP BY GENDER;";
		Map<String,Double> genderToSalaryMap = new HashMap<String, Double>();
		try(Connection connection = this.getConnection()){
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			while(resultSet.next()) {
				String gender = resultSet.getString("GENDER");
				double salary = resultSet.getDouble("AVG(salary)");
				genderToSalaryMap.put(gender, salary);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return genderToSalaryMap;
	}
	
	public EmployeePayrollData addEmployeeToPayroll(String name, double salary, LocalDate startDate, String gender) {
		int empId = -1;
		EmployeePayrollData payrollData = null;
		String query = String.format("INSERT INTO employee_payroll VALUES ('%s','%s','%s','%s')", name,
				gender, salary, Date.valueOf(startDate));
		try(Connection connection = this.getConnection()){
			Statement statement = connection.createStatement();
			int rowAffected = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			if(rowAffected==1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if(resultSet.next()) empId =  resultSet.getInt(1);
			}
			payrollData = new EmployeePayrollData(empId, name, salary, startDate);
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
		return payrollData;
	}
	
	public EmployeePayrollData addEmployeeToPayrollWithDeductions(String name, double salary, LocalDate startDate, String gender) {
		int empId = -1;
		Connection connection = null;
		EmployeePayrollData employeePayrollData = null;
		try {
			connection = this.getConnection();
			connection.setAutoCommit(false);
		}catch(SQLException exception) {
			exception.printStackTrace();
		}
		try(Statement statement = connection.createStatement()){
			String query = String.format("INSERT INTO employee_payroll VALUES ('%s','%s','%s','%s')", name,
					gender, salary, Date.valueOf(startDate));
			int rowAffected = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
			if(rowAffected==1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if(resultSet.next()) empId =  resultSet.getInt(1);
			}
		} catch (SQLException exception) {
			try {
				connection.rollback();
				return employeePayrollData;
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			exception.printStackTrace();
		}
		
		try(Statement statement = connection.createStatement()){
			double deductions = salary*0.2;
			double taxablePay = salary-deductions;
			double tax = taxablePay*0.1;
			double netPay = salary - tax;
			String query =  String.format("INSERT INTO payroll_details VALUES"
					+ "( %s, %s, %s ,%s, %s, %s)",empId,salary,deductions,taxablePay,tax,netPay);
			int rowAffected = statement.executeUpdate(query);
			if(rowAffected == 1) {
				employeePayrollData = new EmployeePayrollData(empId,name,salary,startDate);
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			if(connection!=null)
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return employeePayrollData;
	}
	
	public void addEmployeesToPayrollUsingThreads(EmployeePayrollData employeeObj) {
		Map<Integer,Boolean> addStatus = new HashMap<>();
		Runnable task = ()->{
			addStatus.put(employeeObj.hashCode(),false);
			this.addEmployeeToPayrollWithDeductions(employeeObj.name, employeeObj.salary, employeeObj.startDate, "M");
			addStatus.put(employeeObj.hashCode(),true);
		};
		Thread thread=new Thread(task);
		thread.start();
		while(addStatus.containsValue(false)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
