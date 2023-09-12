import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
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
import java.util.ArrayList;


public class AppController {

    /**
     * Copy files from A to B
     * @param sourcePath the original file with its path
     * @param targetPath desired destination
     * */
    public Path copyFile(String sourcePath, String targetPath) throws IOException {
        return Files.copy(Path.of(sourcePath), Path.of(targetPath));
    }

    /**
     * Copy specific two image files to ~/.local/share/backgrounds
     * @param nightWallpaperPath the original path for dark wallpaper
     * @param dayWallpaperPath the original path for light wallpaper
     * @param name desired wallpaper name
     * @return Array of two Paths where first is path of copied light wallpaper, followed by dark.
     * */
    private Path[] handleImageMove(String name, String dayWallpaperPath, String nightWallpaperPath) throws Exception {
        String username = System.getProperty("user.name");
        String basePath = "/home/" + username + "/.local/share/backgrounds/";
        boolean isCreated = new File(basePath + name).mkdirs();

        if (!isCreated) {
            throw new FileSystemException("Failed to create a directory");
        }

        Path dayPath = this.copyFile(dayWallpaperPath, (basePath + name) + "/" + name + "--day.jpg");
        Path nightPath = this.copyFile(nightWallpaperPath, (basePath + name) + "/" + name + "--night-jpg");

        return new Path[] {
                dayPath,
                nightPath
        };
    }

    /**
     * create a simple textual element
     * @param tagName string that specifies the element's tag
     * @param document document used to create the element
     * @param value the textual value of the element
     * @return the text element
     * */
    private Element createTextElement(Document document, String tagName, String value) {
        Element element = document.createElement(tagName);
        Text text = document.createTextNode(value);

        element.appendChild(text);
        return element;
    }

    /**
     * Create a document node, that holds specific children
     * @param tagName string that specifies the Element's tag
     * @param document reference to the document which we used to create the Node
     * @param children other Elements, that should be appended to the created Node
     * @return the wrapper element
     * */
    private Element createWrapperElement(Document document, String tagName, ArrayList<Element> children) {
        Element wrapper = document.createElement(tagName);

        for (Element child : children) {
            wrapper.appendChild(child);
        }

        return wrapper;
    }

    /**
     * Create gnome wallpaper configuration file
     * @param name name of the configuration file and thus the wallpaper
     * @param dayWallpaperPath path to day/light wallpaper
     * @param nightWallpaperPath path to night/dark wallpaper
     * */
    private void createConfigFile(String name, Path dayWallpaperPath, Path nightWallpaperPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document config = builder.newDocument();

            // Append all children of wallpaper
            ArrayList<Element> wallpaperChildren = new ArrayList<>();

            wallpaperChildren.add(this.createTextElement(config, "name", name));
            wallpaperChildren.add(this.createTextElement(config, "filename", dayWallpaperPath.toString()));
            wallpaperChildren.add(this.createTextElement(config, "filename-dark", nightWallpaperPath.toString()));
            wallpaperChildren.add(this.createTextElement(config, "options", "zoom"));
            wallpaperChildren.add(this.createTextElement(config, "shade_type", "solid"));
            wallpaperChildren.add(this.createTextElement(config, "pcolor", "#ffffff"));
            wallpaperChildren.add(this.createTextElement(config, "scolor", "#000000"));

            // Create the actual wallpaper node
            Element wallpaper = this.createWrapperElement(config, "wallpaper", wallpaperChildren);
            wallpaper.setAttribute("deleted", "false");

            // Append wallpaper to wallpapers wrapper
            Element wallpapers = config.createElement("wallpapers");
            wallpapers.appendChild(wallpaper);

            // append wallpapers wrapper to config (document)
            config.appendChild(wallpapers);


            // Save the file in XMl format with desired specifications
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            DOMImplementation domImpl = config.getImplementation();
            DocumentType documentType = domImpl.createDocumentType("documentType",
                    "wallpapers",
                    "gnome-wp-list.dtd");

            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, documentType.getSystemId());

            DOMSource source = new DOMSource(config);
            String username = System.getProperty("user.name");

            if (!Files.exists(Path.of("/home/" + username + "/.local/share/gnome-background-properties/"))) {
                boolean isDirectoryCreated = new File("/home/" + username + "/.local/share/gnome-background-properties/").mkdirs();

                if (!isDirectoryCreated) {
                    throw new Exception("Failed to create directory");
                }
            }

            StreamResult result = new StreamResult(new File("/home/" + username + "/.local/share/gnome-background-properties/" + name + ".xml"));
            transformer.transform(source, result);

        } catch (Exception error) {
            System.out.println("Failed to create configuration file");
        }
    }


    /**
     * the only available public method, that makes the entire application's logic
     * @param name the desired name of the wallpaper & config
     * @param dayWallpaperPath  original path of the light themed wallpaper
     * @param nightWallpaperPath original path of the dark themed wallpaper
     * */
    public void createWallpaper(String name, String dayWallpaperPath, String nightWallpaperPath) {
        try {
            Path[] files = this.handleImageMove(name, dayWallpaperPath, nightWallpaperPath);
            this.createConfigFile(name, files[0], files[1]);
        } catch (Exception genericException) {
            System.out.println("Error: " + genericException.getMessage());
        }
    }
}
