import string
import os

# exit if on windows
if os.name == "nt":
    print("unsupported platform")
    exit(1)

BACKGROUNDS_DIRECTORY = "~/.local/share/backgrounds/"
CONFIGURATION_DIRECTORY = "~/.local/share/gnome-background-properties/"

def get_proper_path(msg: str) -> str:
    attempt = input(msg)

    while not (os.path.exists(attempt) or os.path.expanduser(attempt)):
        print("Invalid path")
        attempt = input("please try again: ")

    return attempt

def create_dir_if_not_exist(path: str) -> None:
    if os.path.exists(path) and os.path.isdir(path):
        return
    
    os.makedirs(path, exist_ok=True)

def get_new_image_name(origin: str, is_dark: bool, name: str) -> str:
    filename = origin.split("/")[-1]
    file_extension = filename.split(".")[1]
    dark_toggler = "--d" if is_dark else "--l"

    return BACKGROUNDS_DIRECTORY + name + "/" + name + dark_toggler + "." + file_extension

def copy_file(origin: str, destination: str) -> str:
    os.rename(origin,os.path.join(destination))


# get required inputs 
wallpaper_name = input("wallpaper name: ")
light_wallpaper_path = get_proper_path("light wallpaper path: ")
new_light_wallpaper_path = get_new_image_name(light_wallpaper_path, False, wallpaper_name)
dark_wallpaper_path = get_proper_path("dark wallpaper path: ")
new_dark_wallpaper_path = get_new_image_name(dark_wallpaper_path, True, wallpaper_name)


# creating directories if they don't exist
create_dir_if_not_exist(BACKGROUNDS_DIRECTORY + wallpaper_name)
create_dir_if_not_exist(CONFIGURATION_DIRECTORY)

# copy images
copy_file(light_wallpaper_path, new_light_wallpaper_path)
copy_file(dark_wallpaper_path, new_dark_wallpaper_path)


xml_template = string.Template("""
<?xml version="1.0"?>
<!DOCTYPE wallpapers SYSTEM "gnome-wp-list.dtd">
<wallpapers>
  <wallpaper deleted="false">
    <name>${name}</name>
    <filename>${path_light}</filename>
    <filename-dark>${path_dark}</filename-dark>
    <options>zoom</options>
    <shade_type>solid</shade_type>
    <pcolor>#FFFFFF</pcolor>
    <scolor>#000000</scolor>
  </wallpaper>
</wallpapers>
""")

xml_final = xml_template.substitute(name=wallpaper_name, path_light=new_light_wallpaper_path, path_dark=new_dark_wallpaper_path)

CONFIG_PATH = CONFIGURATION_DIRECTORY + "/" + wallpaper_name + ".xml"

if os.path.exists(CONFIG_PATH):
    print('file already exists')
else:
    with open(CONFIG_PATH, 'w') as fp:
        fp.write(xml_final)
        fp.close()