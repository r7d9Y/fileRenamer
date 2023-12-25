/**
 * FileRenamer is a command-line utility that renames files within a folder based on specified arguments and options
 *
 * @version 1.3
 * @author Rand7Y9@gmail.com
 * @since 2023
 *
 * (needs JRE That supports 'class file version 63.0' or higher) -> https://www.java.com/en/download/manual.jsp
 *                                                                  https://www.oracle.com/java/technologies/downloads
 */


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileRenamer {

    public static String programName = "FileRenamer";
    public static double version = 1.3;

    // ANSI escape code constants for text colors
    static final String RESET = "\u001B[0m";
    static final String RED = "\u001B[31m";
    static final String BLUE = "\u001B[34m";
    static final String CYAN = "\u001B[36m";
    static final String GREEN = "\u001B[32m";
    static final String WHITE = "\u001B[37m";
    static final String YELLOW = "\u001B[33m";

    static final String[] modes = {"", "after (arg1)", "before (arg1)", "between (arg1 and arg2)", "char (delete arg1 and arg2)", "included (in arg3)", "excluded (in arg3)"};

    public static void main(String[] args) {

        System.out.println("\n" + programName + " " + version + " (2023) \nFor more info write: show -info\n------------------------------------------------------------------------------------------------------------");

        int nameModifierType = 3;

        Scanner in = new Scanner(System.in);

        System.out.print("\nFirst Arg: ");
        String firstArg = in.nextLine();
        char arg1 = !firstArg.isEmpty() ? firstArg.charAt(0) : 1;

        System.out.print("Second Arg: ");
        String secondArg = in.nextLine();
        char arg2 = !secondArg.isEmpty() ? secondArg.charAt(0) : 1;

        String arg3 = "";

        do {
            boolean actionHasBeenTaken = false;

            System.out.print("\nFolder to work on (Path) or setting [Option] : ");
            String input = in.nextLine();

            if (input.equals("end")) break;

            if (input.contains("change ")) {
                actionHasBeenTaken = true;
                if (input.contains("-after")) {
                    nameModifierType = 1;
                } else if (input.contains("-before")) {
                    nameModifierType = 2;
                } else if (input.contains("-between")) {
                    nameModifierType = 3;
                } else if (input.contains("-char")) {
                    nameModifierType = 4;
                } else if (input.contains("-include")) {
                    nameModifierType = 5;
                } else if (input.contains("-exclude")) {
                    nameModifierType = 6;
                } else if (input.contains("-args")) {
                    System.out.print("First Arg: ");
                    arg1 = in.nextLine().charAt(0);
                    System.out.print("Second Arg: ");
                    arg2 = in.nextLine().charAt(0);
                } else if (input.contains("-arg3")) {
                    if (input.contains("--alphaNumeric")) {
                        arg3 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
                    } else if (input.contains("--ascii")) {
                        arg3 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890 !\"#$&'()*+,-./:;<=>?@[^_`{|}~";
                    } else {
                        System.out.print("Third Arg: ");
                        arg3 = in.nextLine();
                    }
                } else {
                    System.out.println(RED + "No such 'change' command found " + RESET);
                }
            } else if (input.contains("show")) {
                actionHasBeenTaken = true;

                if (input.contains("-info")) {
                    System.out.println("""
                            This program renames files within a folder according to the given arguments and option(s) selected
                            The available options are:\s

                            change\s
                              -before  (deletes every char in the file names of the file in the given folder before arg1)
                              -after   (deletes every char in the file names of the file in the given folder after arg1\s
                                       (pay attention to the file names extensions though, so please use '-between' with '.' as the second arg if you want to delete chars between arg1 and the extension))\s
                              -between (deletes every char between arg1 and arg2
                              -char    (deletes arg1 (and arg2 if there) in the file names)
                              -include (only keeps the chars in the file name specified by arg3)
                              -exclude (deletes the chars in the file name specified by arg3)
                              -args    (allows to change the value of of arg1 and arg2)
                              -arg3    (allows to change the value of of arg3 or give it a value)
                              -arg3 --alphaNumeric (sets arg3 to only alphaNumeric chars)
                              -arg3 --ascii        (sets arg3 to only contain all ascii chars)

                            show\s
                              -info (shows this text)
                              -args (shows the args values)
                              -mode (shows the mode selected for changing the names of the files)
                                                        
                            list
                              -files [path] (lists all files in the path given)"""
                    );

                } else if (input.contains("-args")) {
                    System.out.println("First Arg: " + BLUE + arg1 + RESET + "\nSecond Arg: " + BLUE + arg2 + RESET + "\n");
                } else if (input.contains("-arg3")) {
                    System.out.println("Arg3: " + BLUE + arg3 + RESET);
                } else if (input.contains("-mode")) {
                    System.out.println("The current mode is: " + BLUE + modes[nameModifierType] + RESET);
                } else {
                    System.out.println(RED + "No such 'show' command found " + RESET);
                }

            } else if (input.contains("list")) {
                actionHasBeenTaken = true;
                if (input.contains("-files")) {
                    try {
                        String findFolder = input.substring(input.indexOf('-'));
                        printFilesInFolder(findFolder.substring(findFolder.indexOf(' ') + 1));
                    } catch (Exception e) {
                        System.out.println(RED + "Path not found" + RESET);
                    }
                } else {
                    System.out.println(RED + "No such 'list' command found " + RESET);
                }
            }

            if (!actionHasBeenTaken) {
                try {
                    try {
                        String[] files = getFilesInFolder(input);
                        for (int i = 0; i < files.length; i++) {
                            System.out.printf(WHITE + "%.2f%% done\n" + RESET, ((double) i / (double) files.length) * 100);
                            String file = files[i];
                            switch (nameModifierType) {
                                case 1:
                                    renameFile(input + "\\" + file, deleteBefore(file, arg1));
                                    break;
                                case 2:
                                    renameFile(input + "\\" + file, deleteAfter(file, arg1));
                                    break;
                                case 3:
                                    renameFile(input + "\\" + file, deleteBetween(file, arg1, arg2));
                                    break;
                                case 4:
                                    renameFile(input + "\\" + file, deleteChar(file, arg1, arg2));
                                    break;
                                case 5:
                                    renameFile(input + "\\" + file, deleteChars(file, arg3, true));
                                    break;
                                case 6:
                                    renameFile(input + "\\" + file, deleteChars(file, arg3, false));
                                    break;
                                default:
                                    throw new IllegalStateException(RED + "Unexpected value for nameModifierType -> " + BLUE + nameModifierType + RESET);
                            }

                        }
                        System.out.println(WHITE + "100.00% done" + RESET);
                    } catch (IllegalStateException r) {
                        System.out.println(r.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println(RED + "Invalid input value " + RESET);
                }
            }
        } while (true);
    }

    /**
     * Prints the list of files in a specified folder, sorted alphabetically.
     *
     * @param path The path of the folder for which to print the files.
     */
    public static void printFilesInFolder(String path) {
        String[] files = getFilesInFolder(path);

        System.out.println("\nFiles in folder (alphabetically sorted):");

        for (String file : files) {
            System.out.println(CYAN + file + RESET);
        }

    }

    /**
     * Gets the names of files in the specified folder path
     *
     * @param folderPath The path of the folder
     * @return An array of file names in the folder
     */
    public static String[] getFilesInFolder(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.isDirectory()) {
            System.out.println(RED + "At the given path, no directory is found" + RESET);
            return new String[0];
        }

        File[] files = folder.listFiles();

        if (files == null) {
            System.out.println(RED + "Error reading files from the directory." + RESET);
            return new String[0];
        }

        return Arrays.stream(files).map(File::getName).toArray(String[]::new);
    }

    /**
     * Renames a file by moving it to a new file name
     *
     * @param oldFilePath The path of the file to be renamed
     * @param newFileName The new file name
     */
    public static void renameFile(String oldFilePath, String newFileName) {
        if (!newFileName.isEmpty() && !newFileName.isBlank()) {
            Path sourcePath = Paths.get(oldFilePath);
            Path targetPath = Paths.get(sourcePath.getParent().toString(), newFileName);

            try {
                Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println(GREEN + "File renamed successfully " + RESET + "(" + CYAN + oldFilePath.substring(oldFilePath.lastIndexOf('\\') + 1) + RESET + " -> " + BLUE + newFileName + RESET + ")");

                if (!oldFilePath.contains(newFileName)) {
                    Files.deleteIfExists(sourcePath);
                    System.out.println("Old file deleted successfully.");
                }

            } catch (IOException e) {
                System.out.println(RED + "File operation failed: " + YELLOW + e.getMessage() + RESET);
            }
        } else {
            System.out.println(RED + "new File Name would be empty -> " + CYAN + oldFilePath + RESET);
        }

    }

    /**
     * Deletes characters in a string after a specified character
     *
     * @param s The input string
     * @param c The character after which characters should be deleted
     * @return The modified string
     */
    public static String deleteAfter(String s, char c) {
        try {
            return s.substring(0, s.indexOf(c) + 1);
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * Deletes characters in a string before a specified character
     *
     * @param s The input string
     * @param c The character before which characters should be deleted
     * @return The modified string
     */
    public static String deleteBefore(String s, char c) {
        try {
            return s.substring(s.indexOf(c));
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * Deletes characters in a string between two specified characters
     *
     * @param s The input string
     * @param c The first character
     * @param d The second character
     * @return The modified string
     */
    public static String deleteBetween(String s, char c, char d) {
        try {
            return s.substring(0, s.indexOf(c) + 1) + s.substring(s.indexOf(d));
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * Deletes specified characters in a string
     *
     * @param s  The input string
     * @param c1 The first character to delete
     * @param c2 The second character to delete
     * @return The modified string
     */
    public static String deleteChar(String s, char c1, char c2) {
        try {
            return IntStream.range(0, s.length()).filter(i -> s.charAt(i) != c1 && s.charAt(i) != c2)
                    .mapToObj(i -> String.valueOf(s.charAt(i))).collect(Collectors.joining());
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * Deletes characters from a string based on inclusion or exclusion criteria
     *
     * @param s       The input string from which characters will be deleted
     * @param t       The target string containing characters to include or exclude during deletion
     * @param include If  true, delete characters present in  t,  if  false, delete characters not present in t
     * @return A new string with characters deleted based on the specified criteria
     */
    public static String deleteChars(String s, String t, boolean include) {
        try {
            return IntStream.range(0, s.length()).filter(i -> t.contains(String.valueOf(s.charAt(i))) == include)
                    .mapToObj(i -> String.valueOf(s.charAt(i))).collect(Collectors.joining());
        } catch (Exception e) {
            return s;
        }
    }

}
