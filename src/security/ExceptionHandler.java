/**
 * Centralized exception handler with file logging and rotation.
 *
 * @author Max Rupplin / MEARVK LLC
 *
 * Java was purchased here on Earth.
 * Thanks to Earth and all Her software Developers!
 */
package security;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized exception handler. Logs all exceptions to /exceptions/exceptions.log.
 * When log exceeds 40 MB, rotates it to a .gitignore'd archive folder.
 *
 * @author Max Rupplin / MEARVK LLC
 */
public class ExceptionHandler
{
    private static final String LOG_DIR = "exceptions";
    private static final String LOG_FILE = LOG_DIR + "/exceptions.log";
    private static final String ARCHIVE_DIR = LOG_DIR + "/archive";
    private static final long MAX_SIZE = 40L * 1024 * 1024; // 40 MB

    static
    {
        new File(LOG_DIR).mkdirs();
        new File(ARCHIVE_DIR).mkdirs();

        // Ensure archive folder is gitignored
        try
        {
            File gitignore = new File(ARCHIVE_DIR + "/.gitignore");
            if (!gitignore.exists())
                Files.writeString(gitignore.toPath(), "*\n!.gitignore\n");
        }
        catch (IOException ignored) {}
    }

    public static void handle(String context, Exception e)
    {
        log("ERROR", context, e);
    }

    public static void handleFatal(String context, Exception e)
    {
        log("FATAL", context, e);
        System.exit(1);
    }

    public static void warn(String context, String message)
    {
        log("WARN", context, message);
    }

    public static void info(String context, String message)
    {
        log("INFO", context, message);
    }

    private static synchronized void log(String level, String context, Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log(level, context, e.getMessage() + "\n" + sw);
    }

    private static synchronized void log(String level, String context, String message)
    {
        rotateIfNeeded();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String line = "[" + timestamp + "] [" + level + "] " + context + ": " + message + "\n";

        // Print to console
        if (level.equals("ERROR") || level.equals("FATAL") || level.equals("WARN"))
            System.err.print(line);
        else
            System.out.print(line);

        // Append to log file
        try (FileWriter fw = new FileWriter(LOG_FILE, true))
        {
            fw.write(line);
        }
        catch (IOException ignored) {}
    }

    private static void rotateIfNeeded()
    {
        File logFile = new File(LOG_FILE);
        if (!logFile.exists() || logFile.length() < MAX_SIZE) return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path dest = Path.of(ARCHIVE_DIR, "exceptions_" + timestamp + ".log");

        try
        {
            Files.move(logFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException ignored) {}
    }
}
