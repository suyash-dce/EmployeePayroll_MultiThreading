import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.google.gson.Gson;

public class EmployeePayrollRestAssureTest {

	@Before
	public void init() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}
	
	public EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employee_payroll");
		EmployeePayrollData[] arrayOfEmp = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmp;
	}

	@Test
	public void givenEmpDataInJSONServer_WhenRetrieved_ShouldMatchCount() {
		EmployeePayrollData[] arrayOfEmp = getEmployeeList();
		EmployeePayrollMain employeeFunction = new EmployeePayrollMain();
		employeeFunction.setEmployeeDataList(Arrays.asList(arrayOfEmp));
		assertEquals(2, employeeFunction.countEntries(IOCommand.REST_IO));
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
}
