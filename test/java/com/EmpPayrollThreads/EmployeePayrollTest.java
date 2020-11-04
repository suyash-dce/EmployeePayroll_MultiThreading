import static org.junit.Assert.*;
import java.time.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

public class EmployeePayrollTest {
	
	EmployeePayrollMain employeeFunction;
	List<EmployeePayrollData> employeePayrollData;
	
	@Before
	public void init() {
		employeeFunction=new EmployeePayrollMain();
		employeePayrollData = employeeFunction.readData(IOCommand.DB_IO);
	}
	
	public void givenEmployeeData_ShouldPrintInstanceTime_ToConsole() {
		EmployeePayrollData[] arrayOfEmp = {
				new EmployeePayrollData(1, "Suyash", 100000.0, LocalDate.now()),
				new EmployeePayrollData(2, "Harshit", 200000.0, LocalDate.now()),
				new EmployeePayrollData(3, "Raman", 300000.0, LocalDate.now()),
				new EmployeePayrollData(4, "Akshit", 600000.0, LocalDate.now()),
				new EmployeePayrollData(5, "Sarvagya", 500000.0, LocalDate.now()),
				new EmployeePayrollData(6, "Sahil", 300000.0, LocalDate.now())
		};
		Instant start = Instant.now();
		employeeFunction.addEmployeeToPayroll(Arrays.asList(arrayOfEmp));
		Instant end = Instant.now();
		System.out.println("Duration Without Thread: "+java.time.Duration.between(start, end));
	}
}
