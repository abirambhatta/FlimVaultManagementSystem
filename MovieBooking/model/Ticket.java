package MovieBooking.model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ticket model handles ticket booking data, file storage, and reporting
 */
public class Ticket {
    private String username;
    private String movieName;
    private String genre;
    private String language;
    private String rating;
    private String date;
    private String time;
    private String seats;
    private String seatType;
    private String price;

    private static final String BOOKING_FILE = "src/MovieBooking/ticket.txt";

    public Ticket(String[] parts) {
        if (parts.length >= 10) {
            this.username = parts[0];
            this.movieName = parts[1];
            this.genre = parts[2];
            this.language = parts[3];
            this.rating = parts[4];
            this.date = parts[5];
            this.time = parts[6];
            this.seats = parts[7];
            this.seatType = parts[8];
            this.price = parts[9];
        }
    }

    /**
     * Calculates the number of bookings per user.
     * 
     * @return A map of usernames to their booking count.
     */
    public static Map<String, Integer> getBookingCounts() {
        Map<String, Integer> counts = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKING_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(";");
                if (parts.length > 0) {
                    String username = parts[0].trim();
                    counts.put(username, counts.getOrDefault(username, 0) + 1);
                }
            }
        } catch (IOException e) {
            // File might not exist yet
        }
        return counts;
    }

    /**
     * Reads all bookings from the file.
     * 
     * @return List of string arrays representing booking rows.
     */
    public static List<String[]> getAllBookings() {
        List<String[]> bookings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKING_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split(";");
                if (parts.length >= 10) {
                    bookings.add(parts);
                }
            }
        } catch (IOException e) {
            // Ignore
        }
        return bookings;
    }

    /**
     * Reads bookings for a specific user.
     * 
     * @param userIdentifier Username or Email
     * @return List of string arrays for the user.
     */
    public static List<String[]> getBookingsForUser(String userIdentifier) {
        List<String[]> userBookings = new ArrayList<>();
        for (String[] b : getAllBookings()) {
            if (b[0].equalsIgnoreCase(userIdentifier)) {
                userBookings.add(b);
            }
        }
        return userBookings;
    }

    /**
     * Gets the most recent movie booked by a user.
     * 
     * @param userIdentifier Username or Email
     * @return Name of the movie or "N/A"
     */
    public static String getRecentMovieByUser(String userIdentifier) {
        List<String[]> bookings = getBookingsForUser(userIdentifier);
        if (bookings.isEmpty())
            return "N/A";
        // Assuming the last one in the file is the most recent
        return bookings.get(bookings.size() - 1)[1];
    }

    /**
     * Calculates total money spent by a user.
     * 
     * @param userIdentifier Username or Email
     * @return Total spent formatted as string.
     */
    public static String getTotalSpentByUser(String userIdentifier) {
        List<String[]> bookings = getBookingsForUser(userIdentifier);
        int total = 0;
        for (String[] b : bookings) {
            try {
                total += Integer.parseInt(b[9].trim());
            } catch (Exception e) {
            }
        }
        return String.valueOf(total);
    }

    /**
     * Saves a new booking record to the file.
     * 
     * @param bookingData Semi-colon separated booking string.
     * @return true if successful.
     */
    public static boolean saveBooking(String bookingData) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(BOOKING_FILE, true))) {
            writer.println(bookingData);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getMovieName() {
        return movieName;
    }

    public String getGenre() {
        return genre;
    }

    public String getLanguage() {
        return language;
    }

    public String getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getSeats() {
        return seats;
    }

    public String getSeatType() {
        return seatType;
    }

    public String getPrice() {
        return price;
    }
}
