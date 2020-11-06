import static org.junit.Assert.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.google.gson.Gson;

public class EmployeePayrollRestAssureTest {

	EmployeePayrollMain employeeFunction;
	EmployeePayrollData[] arrayOfEmp;
	
	@Before
	public void init() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
		arrayOfEmp = getEmployeeList();
		employeeFunction = new EmployeePayrollMain();
		employeeFunction.setEmployeeDataList(Arrays.asList(arrayOfEmp));
	}
	
	public EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		EmployeePayrollData[] arrayOfEmp = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmp;
	}
	
	public Response addEmployeeToJsonServer(EmployeePayrollData emp) {
		String empJson = new Gson().toJson(emp);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}
	
	public Response addEmployeeToJsonServer(List<EmployeePayrollData> empList) {
		RequestSpecification request = null;
		for(EmployeePayrollData emp:empList) {
			String empJson = new Gson().toJson(emp);
			request = RestAssured.given();
			request.header("Content-Type", "application/json");
			request.body(empJson);
		}
		return request.post("/employees");
	}

	@Test
	public void givenEmpDataInJSONServer_WhenRetrieved_ShouldMatchCount() {
		assertEquals(2, employeeFunction.countEntries(IOCommand.REST_IO));
	}
	
	@Test
	public void givenNewEmployeeWhenAdded_shouldMatch201ResponseAndCount() {
		EmployeePayrollData emp = new EmployeePayrollData(1, "Suyash", 300000.0,
				LocalDate.now());
		
		Response response = addEmployeeToJsonServer(emp);
		emp = new Gson().fromJson(response.asString(),EmployeePayrollData.class);
		employeeFunction.addEmployeeToPayroll(emp);
		
		int statusCode = response.getStatusCode();
		assertEquals(201, statusCode);
		assertEquals(3, employeeFunction.countEntries(IOCommand.REST_IO));
	}
	
	@Test
	public void givenMultipleEmployeesWhenAdded_shouldMatch201ResponseAndCount() {
		arrayOfEmp = new EmployeePayrollData[] {
				new EmployeePayrollData(4, "Sarvagya", 600000.0, LocalDate.now()),
				new EmployeePayrollData(5, "Raman", 500000.0, LocalDate.now()),
				new EmployeePayrollData(6, "Akshit", 300000.0, LocalDate.now())
		};
		
		Response response = addEmployeeToJsonServer(Arrays.asList(arrayOfEmp));
		Arrays.asList(arrayOfEmp).forEach(emp -> {
			emp = new Gson().fromJson(response.asString(),EmployeePayrollData.class);
		});
		employeeFunction.updateEmployeeDataInJSONUsingThreads(Arrays.asList(arrayOfEmp));
		
		int statusCode = response.getStatusCode();
		assertEquals(201, statusCode);
		assertEquals(6, employeeFunction.countEntries(IOCommand.REST_IO));
	}
	
	@Test
	public void givenSalaryForEmployee_WhenUpdated_ShouldReturn200Response() {
		employeeFunction.updateEmployeeSalary("Akshit", 400000.0);
		EmployeePayrollData emp =  employeeFunction.getEmployeePayrollData("Harhit");
		
		String empJson = new Gson().toJson(emp);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);		
		Response respose = request.put("/employees/"+emp.id);
		
		int statusCode = respose.getStatusCode();
		assertEquals(200, statusCode);
	}
	
	@Test
	public void givenEmpDataInJSONServer_WhenRetrieved_ShouldMatchCount() {
		assertEquals(2, employeeFunction.countEntries(IOCommand.REST_IO));
	}
	
	@Test
	public void givenEmployeeToDelete_WhenDeleted_shouldMatch200ResponseAndCount() {
		EmployeePayrollData emp = employeeFunction.getEmployeePayrollData("Sarvagya");
		RequestSpecification requestSpecification = RestAssured.given();
		requestSpecification.header("Content-Type","application/json");
		Response response = requestSpecification.delete("/employee_payroll/"+emp.id);
		
		int statusCode = response.getStatusCode();
		assertEquals(200,statusCode);
		
		employeeFunction.deleteEmployeePayroll(emp.name);
		assertEquals(5,employeeFunction.countEntries(IOCommand.REST_IO));
	}
}
