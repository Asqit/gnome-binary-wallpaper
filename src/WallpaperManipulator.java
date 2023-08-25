import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// https://stackoverflow.com/questions/4142046/create-xml-file-using-java

public class WallpaperManipulator {
    public void createWallpaper(String name, String dayWallpaperPath, String nightWallpaperPath) {
       try {
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           DocumentBuilder builder = factory.newDocumentBuilder();
           Document document = builder.newDocument();

           // Root element <wallpaper>...</wallpaper>
           Element wallpapers = document.createElement("wallpapers");
           document.appendChild(wallpapers);

           // <wallpaper><name></name></wallpaper>
           Element nameNode = document.createElement("name");
           nameNode.setNodeValue(name);
           document.appendChild(nameNode);

           Element dayWallpaper = document.createElement("filename");
           dayWallpaper.setNodeValue(dayWallpaperPath);



       } catch (Exception exception) {
           System.out.println("Error: " + exception);
       }
    }
}
