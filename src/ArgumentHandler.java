import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.io.File;
import java.nio.file.FileSystemException;

public class ArgumentHandler {
    private final Options options = new Options();
    private final CommandLineParser parser = new DefaultParser();
    private CommandLine cmd;

    public ArgumentHandler(String[] args) {
        try {
            this.cmd = this.parser.parse(this.options, args);
        } catch (Exception error) {
            System.out.println("Error: " + error.getMessage());
        }

        this.appendOptions();
        this.handleArguments();
    }

    private void createWallpaper(String name, String dayWallpaperPath, String nightWallpaperPath) throws FileSystemException {
        WallpaperManipulator manipulator = new WallpaperManipulator();
        String username = System.getProperty("user.name");
        String basePath = "/home/" + username + "/.local/share/backgrounds/";

        File dayWallpaper = new File(dayWallpaperPath);
        File nightWallpaper = new File(nightWallpaperPath);

        boolean isDayWallpaperMoved = dayWallpaper.renameTo(new File( basePath + name + "--day.jpg"));
        boolean isNightWallpaperMoved = nightWallpaper.renameTo(new File(basePath + name + "--night.jpg"));

        if (!isDayWallpaperMoved || !isNightWallpaperMoved) {
            throw new FileSystemException("Failed to move files");
        }

        manipulator.createWallpaper(
                name,
                basePath + name + "--day.jpg",
                basePath + name + "--night.jpg"
        );
    }

    private void printHelp() {
        System.out.println("Gnome-binary-wallpaper");
        System.out.println("program for creating light/dark wallpapers for gnome");
        System.out.println("options:");
        System.out.println("1. h - prints this message");
        System.out.println("2. example: ./gnome-binary-wallpaper -n 'dome' -dw '~/Downloads/dome-day.jpg' -nw '~/Download/dome-night.jpg'");
    }

    private void appendOptions() {
        this.options.addOption("h", "help", false, "Show help");
        this.options.addOption("n", "name", true, "name of the wallpaper");
        this.options.addOption("dw", "day-wallpaper", true, "wallpaper for day");
        this.options.addOption("nw", "night-wallpaper", true, "wallpaper for night");
    }

    private void handleArguments() {
        if (this.cmd.hasOption("h")) {
            this.printHelp();
        }

        if (cmd.hasOption("n") && cmd.hasOption("dw") && cmd.hasOption("nw")) {
            String wallpaperName = cmd.getOptionValue("n");
            String dayWallpaperPath = cmd.getOptionValue("dw");
            String nightWallpaperPath = cmd.getOptionValue("nw");

            try {

                this.createWallpaper(wallpaperName, dayWallpaperPath, nightWallpaperPath);
            } catch (Exception error) {
                System.out.println("Error: " + error.getMessage());
            }

        } else {
            System.out.println("Invalid arguments. Use -h for help.");
        }
    }
}
