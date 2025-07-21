package service;

import java.awt.Desktop;
import model.Student;
import model.Attendance;
import util.EmailService;
import util.LocationUtil;
import exception.ValidationException;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

import static util.LocationUtil.isAtCollegeLocation;

public class AttendanceService {
    private Map<String, Student> studentDB = new HashMap<>();
    private final String STAFF_EMAIL = "vemurirenuka1234@gmail.com";
    private final String STAFF_PASSWORD = "Ciet@123";
    private static final String FILE_NAME = System.getProperty("user.dir") + "/ConsoleAttendanceSystem/attendance_report.csv";
    private static final String STUDENT_FILE = "students.csv"; // ✅ CSV to store student data
    private Map<String, String> passwordMap = new HashMap<>(); // In-memory password store


    public void registerStudent(Student s) throws ValidationException {
        validateEmail(s.getEmail());
        validatePassword(s.getPassword());
        validatePhone(s.getPhone());

        studentDB.put(s.getEmail(), s);
        passwordMap.put(s.getEmail(), s.getPassword()); // Store password temporarily
        saveStudentToFile(s); // Save other info to CSV

        System.out.println("✅ Registered Successfully. Please login again.");
    }

    public boolean loginAndMarkAttendance(String email, String password) throws ValidationException {
        validateEmail(email);

        if (!isRegistered(email)) {
            throw new ValidationException("❌ Not Registered – Please Register First");
        }

        Student s = studentDB.get(email); // This only works for current session
        if (s == null) {
            // fallback if studentDB is empty (e.g. after restart)
            for (Student student : getAllStudents()) {
                if (student.getEmail().equalsIgnoreCase(email)) {
                    s = student;
                    break;
                }
            }
        }

        if (s == null) {
            throw new ValidationException("❌ Student not found in file.");
        }
        studentDB.put(email, s);
        validatePassword(password);
        String storedPassword = passwordMap.get(email);
        if (storedPassword == null) {
            throw new ValidationException("❌ Password not available. Please re-register or restart system.");
        }
        if (!storedPassword.equals(password)) {
            throw new ValidationException("❌ Incorrect Password");
        }


        if (isAttendanceAlreadyMarked(s.getRollNo(), LocalDate.now().toString())) {
            throw new ValidationException("❌ Attendance Already Marked for Today");
        }

        System.out.println("✅ Login Successful");

        String otp = EmailService.sendOTPAndQR(email);
        Scanner sc = new Scanner(System.in);

        System.out.println("\n✉️ OTP and QR have been sent to your working email.");
        System.out.print("🔐 Enter OTP: ");
        String enteredOTP = sc.nextLine();

        System.out.print("📎 Enter QR Link: ");
        String qrInput = sc.nextLine();

        if (!otp.equals(enteredOTP)) {
            throw new ValidationException("❌ OTP Mismatch");
        }

        double[] coords = LocationUtil.getStudentCoordinates();
        double currentLat = coords[0];
        double currentLon = coords[1];

        double collegeLat = 16.314209;
        double collegeLon = 80.435028;

        System.out.println("📍 Your Coordinates: " + currentLat + ", " + currentLon);
        System.out.println("🏫 College Coordinates: " + collegeLat + ", " + collegeLon);

        if (!isAtCollegeLocation(currentLat, currentLon)) {
            throw new ValidationException("❌ Attendance Denied – You are not at the college location.");
        }

        Attendance a = new Attendance(s.getRollNo(), s.getName(), LocalDate.now().toString(), "Present");
        saveAttendanceToCSV(a);

        System.out.println("\n✅ Attendance Marked Successfully");
        System.out.println("🔖 Name: " + s.getName());
        System.out.println("🆔 Roll No: " + s.getRollNo());
        System.out.println("📅 Date: " + LocalDate.now());
        return true;
    }

    public void exportAttendanceForStaff() {
        File file = new File(FILE_NAME);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            System.out.println("\n📊 Attendance Report:");
            br.lines().forEach(System.out::println);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("📁 Saved file at: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("❌ No Attendance Found");
        }
    }

    public boolean isRegistered(String email) {
        return studentDB.containsKey(email) || isRegisteredEmail(email);
    }

    public boolean isValidStaff(String email, String password) {
        return email.equals(STAFF_EMAIL) && password.equals(STAFF_PASSWORD);
    }

    public void validateEmail(String email) throws ValidationException {
        if (!email.contains("@") || !email.contains(".")) {
            throw new ValidationException("❌ Email must contain '@' and '.'");
        }

        if (!(email.endsWith(".com") || email.endsWith(".in") || email.endsWith(".ac.in"))) {
            throw new ValidationException("❌ Email must end with '.com', '.in', or '.ac.in'");
        }

        if (!email.equals(email.toLowerCase())) {
            throw new ValidationException("❌ Email must be in lowercase");
        }

        long atCount = email.chars().filter(ch -> ch == '@').count();
        if (atCount != 1) {
            throw new ValidationException("❌ Email must contain exactly one '@'");
        }
    }

    public void validatePassword(String password) throws ValidationException {
        if (password.length() <= 5 || password.length() > 10) {
            throw new ValidationException("❌ Password must be greater than 5 and less than or equal to 10 characters.");
        }

        if (!Character.isUpperCase(password.charAt(0))) {
            throw new ValidationException("❌ Password must start with a capital letter");
        }

        boolean hasDigit = false;
        boolean hasSpecial = false;
        for (char ch : password.toCharArray()) {
            if (Character.isDigit(ch)) hasDigit = true;
            if ("@#$%^&*!".indexOf(ch) != -1) hasSpecial = true;
        }

        if (!hasDigit || !hasSpecial) {
            throw new ValidationException("❌ Password must include at least one digit and one special character (@#$%^&*!)");
        }
    }

    public void validatePhone(String phone) throws ValidationException {
        if (phone.length() != 10 || !phone.chars().allMatch(Character::isDigit)) {
            throw new ValidationException("❌ Phone number must be exactly 10 digits");
        }
    }

    public void saveAttendanceToCSV(Attendance a) {
        File file = new File(FILE_NAME);
        File parentDir = file.getParentFile();

        try {
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            boolean fileExists = file.exists();

            try (FileWriter fw = new FileWriter(file, true)) {
                if (!fileExists) fw.write("RollNo,Name,Date,Status\n");
                fw.write(a.getRollNo() + "," + a.getName() + "," + a.getDate() + "," + a.getStatus() + "\n");
            }
        } catch (IOException e) {
            System.out.println("❌ Failed to save attendance: " + e.getMessage());
        }
    }

    public void saveStudentToFile(Student s) {
        File file = new File(STUDENT_FILE);
        boolean exists = file.exists();

        try (FileWriter fw = new FileWriter(file, true)) {
            if (!exists) {
                // Write header without password
                fw.write("Name,Email,Place,Phone,RollNo\n");
            }

            // Write student data without password
            fw.write(String.join(",",
                    s.getName(),
                    s.getEmail(),
                    s.getPlace(),
                    s.getPhone(),
                    s.getRollNo()) + "\n");

        } catch (IOException e) {
            System.out.println("❌ Failed to save student: " + e.getMessage());
        }
    }


    public static List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        File file = new File(STUDENT_FILE);
        if (!file.exists()) return students;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    Student s = new Student(data[0], data[1], "" , data[2], data[3], data[4]);
                    students.add(s);
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading students file: " + e.getMessage());
        }
        return students;
    }

    public static boolean isRegisteredEmail(String email) {
        List<Student> students = getAllStudents();
        for (Student student : students) {
            if (student.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAttendanceAlreadyMarked(String rollNo, String date) {
        if (rollNo == null || date == null) {
            return false; // or throw an exception — invalid input
        }
        File file = new File(FILE_NAME);
        if (!file.exists()) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(rollNo) && line.contains(date)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Error checking attendance: " + e.getMessage());
        }
        return false;
    }

}
