import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.FileSystemException;


public class Core {

    private String[] handleImageMove(String name, String dayWallpaperPath, String nightWallpaperPath) throws FileSystemException {
        String username = System.getProperty("user.name");
        String basePath = "/home/" + username + "/.local/share/backgrounds";

        File dayWallpaper = new File(dayWallpaperPath);
        File nightWallpaper = new File(nightWallpaperPath);

        boolean isDayWallpaperMoved = dayWallpaper.renameTo(new File( basePath + name + "--day.jpg"));
        boolean isNightWallpaperMoved = nightWallpaper.renameTo(new File(basePath + name + "--night.jpg"));

        if (!isDayWallpaperMoved || !isNightWallpaperMoved) {
            throw new FileSystemException("Failed to move files");
        }

        return new String[] {
            basePath + name + "--day.jpg",
            basePath + name + "--night.jpg"
        };
    }

    private void handleXMLCreation(String name, String dayWallpaperPath, String nightWallpaperPath) {
         try {
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           DocumentBuilder builder = factory.newDocumentBuilder();
           Document document = builder.newDocument();

           DOMImplementation documentImplementation = document.getImplementation();
           DocumentType documentType = documentImplementation.createDocumentType("wallpapers", "SYSTEM", "gnome-ws-list.dtd");
           document.appendChild(documentType);

           Element wallpapers = document.createElement("wallpapers");
           document.appendChild(wallpapers);

           Element wallpaper = document.createElement("wallpaper");
           wallpaper.setAttribute("deleted", "false");

           Element nameNode = document.createElement("name");
           Text nameText =  document.createTextNode(name);
           nameNode.appendChild(nameText);

           Element dayWallpaper = document.createElement("filename");
           Text dayWallpaperText = document.createTextNode(dayWallpaperPath);
           dayWallpaper.appendChild(dayWallpaperText);

           Element nightWallpaper = document.createElement("filename-dark");
           Text nightWallpaperText = document.createTextNode(nightWallpaperPath);
           nightWallpaper.appendChild(nightWallpaperText);

           Element options = document.createElement("options");
           Text optionsText = document.createTextNode("zoom");
           options.appendChild(optionsText);

           Element shadeType = document.createElement("shade_type");
           Text shadeTypeText = document.createTextNode("solid");
           shadeType.appendChild(shadeTypeText);

           Element pColor = document.createElement("pcolor");
           Text pColorText = document.createTextNode("#3071AE");
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
            String[] files = this.handleImageMove(name, dayWallpaperPath, nightWallpaperPath);
            
            this.handleXMLCreation(name, files[0], files[1]);
        
        } catch (FileSystemException exception) {
            System.out.println("Failed to move images");
        } catch (Exception genericException) {
            System.out.println("Error: " + genericException.getMessage());
        }
    }
}
