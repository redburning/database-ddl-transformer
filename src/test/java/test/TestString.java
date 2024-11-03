package test;

public class TestString {

	public static boolean checkIfBoolean(String str) {
		return str.matches("(?i)true|false|t|f");
	}
	
	public static boolean checkIfNumber(String str) {
		boolean isNumber = str.matches("-?\\d+(\\.\\d*)?");
		return isNumber;
	}
	
	public static void main(String[] args) {
		System.out.println(checkIfBoolean("true"));
		System.out.println(checkIfBoolean("True"));
		System.out.println(checkIfBoolean("False"));
		System.out.println(checkIfBoolean("false"));
		System.out.println(checkIfBoolean("F"));
		System.out.println(checkIfBoolean("T"));
		
		System.out.println(checkIfNumber("10."));
		System.out.println(checkIfNumber("0.05"));
		System.out.println(checkIfNumber("20"));
	}
	
}
