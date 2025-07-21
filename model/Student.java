package model;

public class Student {
    private String name, email, password, dob, place, phone, rollNo;

    public Student(String name, String email, String password, String place, String phone, String roll) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.place = place;
        this.phone = phone;
        this.rollNo = rollNo;
    }

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public String getPlace() {
        return place;
    }
    public String getPhone() {
        return phone;
    }
    public String getRollNo() {
        return rollNo;
    }
}
