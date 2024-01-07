package main

import (
	"encoding/xml"
	"errors"
	"flag"
	"fmt"
	"log"
	"os"
	"path"
	"runtime"
)

// parent wrapper
type WallpapersXML struct {
	XMLName   xml.Name     `xml:"wallpapers"`
	Wallpaper WallpaperXML `xml:"wallpaper"`
}

// body of wallpapers wrapper
type WallpaperXML struct {
	XMLName      xml.Name `xml:"wallpaper"`
	Name         string   `xml:"name"`
	FileName     string   `xml:"filename"`
	FileNameDark string   `xml:"filename-dark"`
	Options      string   `xml:"options"`
	ShadeType    string   `xml:"shade_type"`
	PColor       string   `xml:"pcolor"`
	SColor       string   `xml:"scolor"`
}

func main() {
	if runtime.GOOS != "linux" {
		fmt.Println(fmt.Errorf("invalid platform!\nOnly GNU/linux is supported"))
		os.Exit(1)
	}

	name := flag.String("name", "", "name of the resulting wallpaper")
	lightWallpaperSource := flag.String("lw", "", "source image for the light version of the wallpaper")
	darkWallpaperSource := flag.String("dw", "", "source image for the dark version of the wallpaper")

	// optional arguments
	sType := flag.String("shade", "solid", "specifies how to shade the background\noptions: 'horizontal', 'vertical', 'solid'")
	pOptions := flag.String("options", "zoom", "determines how the image is rendered.\noptions: 'none', 'wallpaper', 'centered', 'scaled', 'stretched', 'zoom', 'spanned'")
	pColor := flag.String("pcolor", "#023c88", "specifies the primary color")
	sColor := flag.String("scolor", "#5789ca", "specifies the secondary color")
	flag.Parse()

	if *name == "" || *lightWallpaperSource == "" || *darkWallpaperSource == "" {
		fmt.Println(fmt.Errorf("invalid usage\nsee 'gnome-binary-wallpaper -help' for more info"))
		os.Exit(1)
	}

	baseDir, err := os.UserHomeDir()
	exitOnErr(err)

	backgroundsDir := baseDir + "/.local/share/backgrounds/"
	configDir := baseDir + "/.local/share/gnome-background-properties/"
	backgroundDir := backgroundsDir + *name + "/"
	xmlName := configDir + *name + ".xml"

	exitOnErr(createDirIfNotExist(backgroundsDir))
	exitOnErr(createDirIfNotExist(configDir))
	exitOnErr(createDirIfNotExist(backgroundDir))

	lw, err := copyFile(*lightWallpaperSource, backgroundDir, *name, false)
	exitOnErr(err)

	dw, err := copyFile(*darkWallpaperSource, backgroundDir, *name, true)
	exitOnErr(err)

	cfg := WallpapersXML{
		Wallpaper: WallpaperXML{
			Name:         *name,
			FileName:     lw,
			FileNameDark: dw,
			Options:      *pOptions,
			ShadeType:    *sType,
			PColor:       *pColor,
			SColor:       *sColor,
		},
	}

	xmlFile, err := os.Create(xmlName)
	exitOnErr(err)

	xmlFile.WriteString(xml.Header)
	encoder := xml.NewEncoder(xmlFile)
	encoder.Indent("", "\t")
	encoder.Encode(&cfg)
}

func copyFile(source, destination, name string, isDark bool) (string, error) {
	ext := path.Ext(source)
	var path string

	if isDark {
		path = destination + name + "--dark" + ext
	} else {
		path = destination + name + ext
	}

	err := os.Rename(source, path)

	return path, err
}

func createDirIfNotExist(path string) error {
	if _, err := os.Stat(path); errors.Is(err, os.ErrNotExist) {
		err := os.Mkdir(path, os.ModePerm)
		if err != nil && errors.Is(err, os.ErrExist) {
			return nil
		} else {
			return err
		}
	}

	return nil
}

func exitOnErr(err error) {
	if err != nil {
		log.Fatal(err.Error())
	}
}
