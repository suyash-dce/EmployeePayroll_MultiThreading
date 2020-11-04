import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class EmployeePayrollFileIO {
	
	public static String PAYROLL_FNAME = "payroll.txt";

	/**Method to write list containing Emp Data to file
	 * @param List containing Emp Data
	 */
	public void writeData(List<EmployeePayrollData> employeeDataList) {
		
		StringBuffer empBuffer = new StringBuffer();
		employeeDataList.forEach(employee -> {
			String employeeDataStr = employee.pushData().concat("\n");
			empBuffer.append(employeeDataStr);
		});
		
		try {
			Files.write(Paths.get(PAYROLL_FNAME), empBuffer.toString().getBytes());
		}catch(IOException exception) {
			exception.printStackTrace();
		}
	}

	/**Method for counting entries in File
	 * @return NoOfEntries
	 */
	public int countEntries() {
		
		int entries = 0;
		
		try {
			entries = (int) Files.lines(new File(PAYROLL_FNAME).toPath()).count();
		}catch(IOException exception) {
			exception.printStackTrace();
		}
		return entries;
	}
	
	/**Method to write data from file to console
	 * 
	 */
	public void printData() {
		try {
			Files.lines(new File(PAYROLL_FNAME).toPath())
			.forEach(System.out::println);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	/**Method to read data from file
	 * @return List containing Emp Data
	 */
	public List<EmployeePayrollData> readData() {
		List<EmployeePayrollData> employeeDataList = new ArrayList<EmployeePayrollData>();
		
		try {
			Files.lines(new File(PAYROLL_FNAME).toPath())
			.map(line->line.trim())
			.forEach(line->{
			String data = line.toString();
			String[] dataArr = data.split(",");
			for(int i=0;i<dataArr.length;i++){
				int id = Integer.valueOf(dataArr[i].split(" = ")[1]);
				i++;
				String name = dataArr[i].replaceAll("name =", "");
				i++;
				double salary = Double.parseDouble(dataArr[i].replaceAll("salary =", ""));
				EmployeePayrollData employee = new EmployeePayrollData(id,name,salary);
				employeeDataList.add(employee);
			}
			});
		}catch(IOException exception) {
			exception.printStackTrace();
		}
		return employeeDataList;
	}
}
