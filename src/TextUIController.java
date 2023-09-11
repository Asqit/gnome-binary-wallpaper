import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class TextUIController {
    private final Options options = new Options();
    private CommandLine cmd;

    public TextUIController(String[] args) {
        try {
            CommandLineParser parser = new DefaultParser();
            this.cmd = parser.parse(this.options, args);
        } catch (Exception error) {
            System.out.println("Error: " + error.getMessage());
        }

        this.appendOptions();
        this.parseArguments();
    }


    private void printHelp() {
        System.out.println("+---------------------------------------+");
        System.out.println("| Gnome-Binary-Wallpaper                |");
        System.out.println("+---------------------------------------+");
        System.out.println("| utility for creating dark & light     |");
        System.out.println("| wallpapers for gnome                  |");
        System.out.println("+---------------------------------------+");
        System.out.println("| 1. --help (-h) prints this message    |");
        System.out.println("| 2. Example:                           |");
        System.out.println("./gnome-binary-wallpaper -n 'dome' -dw '~/Downloads/dome-day.jpg' -nw '~/Download/dome-night.jpg'");
    }

    private void appendOptions() {
        this.options.addOption("h", "help", false, "Show help");
        this.options.addOption("n", "name", true, "name of the wallpaper");
        this.options.addOption("dw", "day-wallpaper", true, "wallpaper for day");
        this.options.addOption("nw", "night-wallpaper", true, "wallpaper for night");
    }

    private void parseArguments() {
        if (this.cmd.hasOption("h")) {
            this.printHelp();
        }

        if (this.cmd.hasOption('h')){
            this.printHelp();
        }

        if (cmd.hasOption("n") && cmd.hasOption("dw") && cmd.hasOption("nw")) {
            String wallpaperName = cmd.getOptionValue("n");
            String dayWallpaperPath = cmd.getOptionValue("dw");
            String nightWallpaperPath = cmd.getOptionValue("nw");

            new AppController().createWallpaper(wallpaperName, dayWallpaperPath, nightWallpaperPath);
        } else {
            System.out.println("Invalid arguments. Use h for help.");
            for (String arg : this.cmd.getArgs()) {
                System.out.println(arg);
            }
        }
    }
}