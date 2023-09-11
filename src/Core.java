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


public class Core {

    public Path copyFile(String sourcePath, String targetPath) throws Exception {
        try {
            return Files.copy(Path.of(sourcePath), Path.of(targetPath));
        } catch (IOException e) {
            throw new Exception("Failed to copy file");
        }
    }

    private Path[] handleImageMove(String name, String dayWallpaperPath, String nightWallpaperPath) throws Exception {
        String username = System.getProperty("user.name");
        String basePath = "/home/" + username + "/.local/share/backgrounds/";

        Path dayPath = this.copyFile(dayWallpaperPath, basePath + name + "--day.jpg");
        Path nightPath = this.copyFile(nightWallpaperPath, basePath + name + "--night-jpg");

        return new Path[] {
                dayPath,
                nightPath
        };
    }

    private void handleXMLCreation(String name, Path dayWallpaperPath, Path nightWallpaperPath) {
         try {
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           DocumentBuilder builder = factory.newDocumentBuilder();
           Document document = builder.newDocument();

           Element wallpapers = document.createElement("wallpapers");
           document.appendChild(wallpapers);

           Element wallpaper = document.createElement("wallpaper");
           wallpaper.setAttribute("deleted", "false");

           Element nameNode = document.createElement("name");
           Text nameText =  document.createTextNode(name);
           nameNode.appendChild(nameText);

           Element dayWallpaper = document.createElement("filename");
           Text dayWallpaperText = document.createTextNode(dayWallpaperPath.toString());
           dayWallpaper.appendChild(dayWallpaperText);

           Element nightWallpaper = document.createElement("filename-dark");
           Text nightWallpaperText = document.createTextNode(nightWallpaperPath.toString());
           nightWallpaper.appendChild(nightWallpaperText);

           Element options = document.createElement("options");
           Text optionsText = document.createTextNode("zoom");
           options.appendChild(optionsText);

           Element shadeType = document.createElement("shade_type");
           Text shadeTypeText = document.createTextNode("solid");
           shadeType.appendChild(shadeTypeText);

           Element pColor = document.createElement("pcolor");
           Text pColorText = document.createTextNode("#FFFFFF");
           pColor.appendChild(pColorText);

           Element sColor = document.createElement("scolor");
           Text sColorText = document.createTextNode("#000000");
           sColor.appendChild(sColorText);

           wallpapers.appendChild(wallpaper);

           wallpaper.appendChild(nameNode);
           wallpaper.appendChild(dayWallpaper);
           wallpaper.appendChild(nightWallpaper);
           wallpaper.appendChild(options);
           wallpaper.appendChild(shadeType);
           wallpaper.appendChild(pColor);
           wallpaper.appendChild(sColor);


           TransformerFactory transformerFactory = TransformerFactory.newInstance();
           Transformer transformer = transformerFactory.newTransformer();


           transformer.setOutputProperty(OutputKeys.INDENT, "yes");
           transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
           transformer.setOutputProperty(OutputKeys.METHOD, "xml");

           DOMImplementation domImpl = document.getImplementation();
           DocumentType doctype = domImpl.createDocumentType("doctype",
                     "wallpapers",
                     "gnome-wp-list.dtd");

           transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());

           DOMSource source = new DOMSource(document);
           String username = System.getProperty("user.name");
           StreamResult result = new StreamResult(new File("/home/" + username + "/.local/share/gnome-background-properties/" + name + ".xml"));

           transformer.transform(source, result);

           System.out.println("Finished!");

       } catch (Exception exception) {
           System.out.println("Error: " + exception.getMessage());
       }
    }

    public void createWallpaper(String name, String dayWallpaperPath, String nightWallpaperPath) {
        try {
            Path[] files = this.handleImageMove(name, dayWallpaperPath, nightWallpaperPath);
            this.handleXMLCreation(name, files[0], files[1]);
        } catch (Exception genericException) {
            System.out.println("Error: " + genericException.getMessage());
        }
    }
}
