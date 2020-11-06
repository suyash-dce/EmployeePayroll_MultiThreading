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
}
