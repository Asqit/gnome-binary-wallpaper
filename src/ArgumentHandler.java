import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

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

    private void createWallpaper(String name, String dayWallpaperPath, String nightWallpaperPath) {
        WallpaperManipulator manipulator = new WallpaperManipulator();

        manipulator.createWallpaper(name, dayWallpaperPath, nightWallpaperPath);
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

            this.createWallpaper(wallpaperName, dayWallpaperPath, nightWallpaperPath);
        } else {
            System.out.println("Invalid arguments. Use -h for help.");
        }
    }
}
