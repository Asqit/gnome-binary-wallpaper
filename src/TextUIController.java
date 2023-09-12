 import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.Scanner;

public class TextUIController {
   private final Scanner scanner = new Scanner(System.in);

   private void cls() {
       for (int i = 0;i < 100;i++) System.out.println();
   }

   private void printHelp() {
       this.cls();
       System.out.println("┌────────────────────────────────────────────────┐");
       System.out.println("│ Help                                           │");
       System.out.println("│ ══════════════════════════════════════════════ │");
       System.out.println("│ 1. create - starts creating the wallpaper      │");
       System.out.println("│ 2. clear - clears your screen                  │");
       System.out.println("│ 3. help - prints this table                    │");
       System.out.println("│ 4. exit - quits the application                │");
       System.out.println("└────────────────────────────────────────────────┘");
   }

    private void printGreetings() {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│ Gnome-binary-wallpaper                                      │");
        System.out.println("│ ═══════════════════════════════════════════════════════════ │");
        System.out.println("│ Create light & dark wallpapers with ease                    │");
        System.out.println("│ an app by Ondřej Tuček                                      │");
        System.out.println("│ see more at https://github.com/asqit/gnome-binary-wallpaper │");
        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    private void createWallpaper() {
        System.out.print("Wallpaper name:");
        String name = this.scanner.nextLine();

        System.out.print("path to light wallpaper:");
        String dayWallpaperPath = this.scanner.nextLine();

        System.out.print("path to dark wallpaper:");
        String nightWallpaperPath = this.scanner.nextLine();

        AppController appController = new AppController();

        appController.createWallpaper(name, dayWallpaperPath, nightWallpaperPath);
        System.out.println("Wallpaper " + name + " has been successfully created!");
    }

    private void runner() {
       while (true) {
           System.out.print("command: ");
           String input = this.scanner.nextLine();

           if (input.equals("exit")) {
               break;
           }

           if (input.equals("help") || input.equals("-h") || input.equals("--help")) {
               this.printHelp();
               continue;
           }

           if (input.equals("create")) {
               this.createWallpaper();
               continue;
           }

           if (input.equals("clear")) {
               this.cls();
               continue;
           }

           System.out.println("Unknown operand: " + input);
       }
    }

   public void listen() {
        this.printGreetings();
        this.runner();
   }
}