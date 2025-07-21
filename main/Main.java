package main;

import model.Student;
import service.AttendanceService;
import exception.ValidationException;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AttendanceService service = new AttendanceService();

        while (true) {
            System.out.println("\n===== E-Attendance Console System =====");
            System.out.println("1. Register Student");
            System.out.println("2. Student Login & Attendance");
            System.out.println("3. Staff View & Export Attendance");
            System.out.println("4. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            try {
                if (choice == 1) {
                    // Student Registration
                    System.out.print("Name: ");
                    String name = sc.nextLine();

                    String email = "";
                    while (true) {
                        System.out.print("Email: ");
                        email = sc.nextLine();
                        try {
                            service.validateEmail(email);
                            break;
                        } catch (ValidationException e) {
                            System.out.println("❌ " + e.getMessage());
                        }
                    }

                    String password = "";
                    while (true) {
                        System.out.print("Password (6–10 chars): ");
                        password = sc.nextLine();
                        if (password.length() < 6 || password.length() > 10) {
                            System.out.println("❌ Password must be between 6 and 10 characters.");
                        } else {
                            break;
                        }
                    }

                    System.out.print("Place: ");
                    String place = sc.nextLine();

                    String phone = "";
                    while (true) {
                        System.out.print("Phone (10 digits): ");
                        phone = sc.nextLine();
                        if (phone.length() != 10 || !isNumeric(phone)) {
                            System.out.println("❌ Phone must be 10 digits and numeric.");
                        } else {
                            break;
                        }
                    }

                    System.out.print("Roll No: ");
                    String roll = sc.nextLine();

                    Student s = new Student(name, email, password, place, phone, roll);
                    service.registerStudent(s);
                    System.out.println("✅ Registration Successful. Please login again.");

                } else if (choice == 2) {
                    // Student Login
                    String email = "";
                    while (true) {
                        System.out.print("Email: ");
                        email = sc.nextLine();
                        if (!service.isRegisteredEmail(email)) {
                            System.out.println("❌ Email not registered. Please try again.");
                        } else {
                            break;
                        }
                    }

                    System.out.print("Password: ");
                    String password = sc.nextLine();

                    boolean success = service.loginAndMarkAttendance(email, password);
                    if (!success) {
                        System.out.println("❌ Incorrect password.");
                    }

                } else if (choice == 3) {
                    // Staff View
                    System.out.print("Staff Email: ");
                    String staffEmail = sc.nextLine();
                    System.out.print("Staff Password: ");
                    String staffPassword = sc.nextLine();

                    if (service.isValidStaff(staffEmail, staffPassword)) {
                        service.exportAttendanceForStaff();
                    } else {
                        System.out.println("❌ Unauthorized Access – Staff Only");
                    }

                } else if (choice == 4) {
                    System.out.println("👋 Exiting...");
                    break;

                } else {
                    System.out.println("❌ Invalid option. Try again.");
                }

            } catch (ValidationException e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
    }

    private static boolean isNumeric(String input) {
        for (char c : input.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }
}
