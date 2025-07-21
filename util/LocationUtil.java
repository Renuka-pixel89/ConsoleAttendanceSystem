package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class LocationUtil {

    // College coordinates (Chalapathi College, Lam)
    private static final double COLLEGE_LAT = 16.314209;
    private static final double COLLEGE_LON = 80.435028;

    public static double[] getStudentCoordinates() {
        try {
            URL url = new URL("http://ip-api.com/json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            double lat = json.getDouble("lat");
            double lon = json.getDouble("lon");

            System.out.println("📍 Fetched Coordinates via IP API: " + lat + ", " + lon);
            return new double[]{lat, lon};

        } catch (Exception e) {
            System.out.println("❌ Failed to fetch location. Using fallback coordinates.");
            return new double[]{0.0, 0.0};
        }
    }

    public static boolean isAtCollegeLocation(double lat, double lon) {
        double distance = calculateDistance(lat, lon, COLLEGE_LAT, COLLEGE_LON);
        System.out.printf("📏 Distance from college: %.4f km\n", distance);
        return distance <= 50.0; // within 100 meters
    }

    // Haversine formula to calculate distance between two lat/lon pairs
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static void main(String[] args) {
        double[] studentCoords = getStudentCoordinates();
        boolean isEligible = isAtCollegeLocation(studentCoords[0], studentCoords[1]);

        if (isEligible) {
            System.out.println("✅ Attendance Granted");
        } else {
            System.out.println("❌ Attendance Denied – You are not at the college location.");
        }
    }
}
