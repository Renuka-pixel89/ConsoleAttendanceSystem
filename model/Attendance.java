package model;

public class Attendance {
    private String rollNo, name, date, status;

    public Attendance(String rollNo, String name, String date, String status) {
        this.rollNo = rollNo;
        this.name = name;
        this.date = date;
        this.status = status;
    }

    public String getRollNo() {
        return rollNo;
    }
    public String getName() {
        return name;
    }
    public String getDate() {
        return date;
    }
    public String getStatus() {
        return status;
    }
}
