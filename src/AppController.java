import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class AppController {
    private String backgroundsBasePath;
    private String configBasePath;
    
    public AppController() {
        String username = System.getProperty("user.name");
        backgroundsBasePath = "/home/" + username + "/.local/share/backgrounds";
        configBasePath = "/home/" + username + "/.local/share/gnome-background-properties";
    }

    /**
     * method used to simply copy files from point A to point B
     * @param sourcePath where is the original file
     * @param targetPath where should the file be copied 
     * @return the path of the result
     * @throws IOException
     */
    private Path copyFile(String sourcePath, String targetPath) throws IOException {
        return Files.copy(Path.of(sourcePath), Path.of(targetPath));
    }

    /**
     * if directory and it's parents don't exist, try to create them or throw exception
     * @param directoryPath
     * @throws IOException
     */
    private void createDirectoriesIfNotExist(Path directoryPath) throws IOException {
        if (!Files.exists(directoryPath)) {
            Path createdPath = Files.createDirectories(directoryPath);
            if (!Files.exists(createdPath)) {
                throw new IOException("Failed to create directory");
            }
        }
    }


    /**
     * method for generating final path of user's image
     * @param name name of the image (name of the wallpaper as a whole)
     * @param originalImagePath where is the original image located 
     * @param isDark toggle for when the image is for dark mode or light mode
     * @return the generated path
     */
    private String getImagePath(String name, String originalImagePath, boolean isDark) {
        String[] bits = originalImagePath.split("/");
        String filename = bits[bits.length - 1];
        String extension = filename.split("\\.")[1];

        return (backgroundsBasePath + "/" + name) + "/" + name + (isDark ? "--dark" : "--light") + "." + extension;
    }

    /**
     * method used to actually copy the source images
     * @param name
     * @param lightWallpaperPath
     * @param darkWallpaperPath
     * @return Path array of 2 where the light is first, followed by  dark
     * @throws IOException
     */
    private Path[] handleImageMove(String name, String lightWallpaperPath, String darkWallpaperPath) throws IOException {
        this.createDirectoriesIfNotExist(Path.of(backgroundsBasePath + "/" + name));

        Path lightFinalPath = this.copyFile(lightWallpaperPath, this.getImagePath(name, lightWallpaperPath, false));
        Path darkFinalPath = this.copyFile(darkWallpaperPath, this.getImagePath(name, darkWallpaperPath, true));
        return new Path[] { lightFinalPath, darkFinalPath };
    } 


    /**
     * method used to append a text node into parent node
     * @param config
     * @param parent
     * @param tagName
     * @param textContent
     */
    private void appendTextElement(Document config, Element parent, String tagName, String textContent) {
        Element element = config.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    /**
     * method used to create the config document (future XML file)
     * @param name
     * @param dayWallpaperPath
     * @param nightWallpaperPath
     * @return
     * @throws Exception
     */
    private Document createConfigDocument(String name, Path dayWallpaperPath, Path nightWallpaperPath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document config = builder.newDocument();
    
        Element wallpaper = config.createElement("wallpaper");
        wallpaper.setAttribute("deleted", "false");
    
        appendTextElement(config, wallpaper, "name", name);
        appendTextElement(config, wallpaper, "filename", dayWallpaperPath.toString());
        appendTextElement(config, wallpaper, "filename-dark", nightWallpaperPath.toString());
        appendTextElement(config, wallpaper, "options", "zoom");
        appendTextElement(config, wallpaper, "shade_type", "solid");
        appendTextElement(config, wallpaper, "pcolor", "#ffffff");
        appendTextElement(config, wallpaper, "scolor", "#000000");
    
        Element wallpapers = config.createElement("wallpapers");
        wallpapers.appendChild(wallpaper);
    
        config.appendChild(wallpapers);
    
        DOMImplementation domImpl = config.getImplementation();
        DocumentType documentType = domImpl.createDocumentType(
            "documentType",
            "wallpapers",
            "gnome-wp-list.dtd"
        );
    
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
    
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, documentType.getSystemId());
    
        return config;
    }

    /**
     * method used to save config document to XML file
     * @param config
     * @param filePath
     * @throws Exception
     */
    private void saveConfigToFile(Document config, Path filePath) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
    
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    
        DOMSource source = new DOMSource(config);
        StreamResult result = new StreamResult(filePath.toFile());
        transformer.transform(source, result);
    }
    
    private void deleteDir(File file) {
        try {
            File[] contents = file.listFiles();
            if (contents != null) {
                for (File f : contents) {
                    if (! Files.isSymbolicLink(f.toPath())) {
                        deleteDir(f);
                    }
                }
            }
            file.delete();
        } catch (SecurityException e) {
            System.out.println("Failed to delete a directory:" + file.getAbsolutePath());
        }
    }

    /**
     * method used to create it the XML file
     * @param name
     * @param dayWallpaperPath
     * @param nightWallpaperPath
     */
    private void createConfigFile(String name, Path dayWallpaperPath, Path nightWallpaperPath) {
        try {
            Document config = createConfigDocument(name, dayWallpaperPath, nightWallpaperPath);
    
            Path configFilePath = Path.of(configBasePath, name + ".xml");
    
            createDirectoriesIfNotExist(configFilePath.getParent());
    
            saveConfigToFile(config, configFilePath);
    
        } catch (Exception error) {
            System.err.println("Failed to create configuration file");
            deleteDir(new File(backgroundsBasePath + "/" + name));
        }
    }

    /**
     * method used to create the gnome-binary-wallpaper
     * @param name
     * @param lightWallpaperPath
     * @param darkWallpaperPath
     */
    public void createWallpaper(String name, String lightWallpaperPath, String darkWallpaperPath) throws IOException {
        Path[] files = this.handleImageMove(name, lightWallpaperPath, darkWallpaperPath);
        createConfigFile(name, files[0], files[1]);
    }
}