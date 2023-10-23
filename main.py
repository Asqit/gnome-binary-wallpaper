from platform import system
import argparse
import shutil
import string
import os

# Assign CLI arguments
parser = argparse.ArgumentParser()
parser.add_argument("-n", "--name", help="Show output")
parser.add_argument("-lw", "--light-wallpaper", help="Path of the light wallpaper")
parser.add_argument("-dw", "--dark-wallpaper", help="Path of the dark wallpaper")
args = parser.parse_args()


BACKGROUNDS_DIRECTORY = os.path.expanduser("/.local/share/backgrounds/")
CONFIG_DIRECTORY = os.path.expanduser("/.local/share/gnome-background-properties/")


def copy_file(filepath: str, destination: str) -> None:
    try:
        shutil.copyfile(filepath, destination)
    except Exception as e:
        print(f"Failed to copy file from {filepath} to {destination}\nSee: {e}")


def create_directory_if_not_exists(path: str) -> None:
    if not os.path.exists(path) or not os.path.isdir(path):
        try:
            os.makedirs(path, exist_ok=True)
        except Exception as e:
            print(f"Failed to create directory at {path}\nSee: {e}")


def parse_file_name(path: str, name: str, is_dark: bool) -> str:
    file = path.split("/")[-1]
    bits = file.split(".")

    return name + "--dark" if is_dark else "--light" + "." + bits[1]


def create_config(name: str, lw: str, dw: str):
    xml_template = string.Template(
        """
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
"""
    )

    xml_final = xml_template.substitute(
        name=name,
        path_light=lw,
        path_dark=dw,
    )

    config_path = os.path.join(CONFIG_DIRECTORY, name + ".xml")

    if os.path.exists(config_path):
        print("Configuration already exists")
        return

    try:
        with open(path=config_path, mode="W") as fp:
            fp.write(xml_final)
    except Exception:
        print("Faled to save configuration")


def make_wallpaper(name: str, light_wallpaper: str, dark_wallpaper: str) -> None:
    create_directory_if_not_exists(BACKGROUNDS_DIRECTORY)
    create_directory_if_not_exists(os.path.join(BACKGROUNDS_DIRECTORY, name))
    create_directory_if_not_exists(CONFIG_DIRECTORY)
    parsed_light_wallpaper = parse_file_name(light_wallpaper, name, False)
    parsed_dark_wallpaper = parse_file_name(dark_wallpaper, name, True)

    copy_file(
        light_wallpaper,
        os.path.join(BACKGROUNDS_DIRECTORY, f"/{name}/", parsed_light_wallpaper),
    )

    copy_file(
        dark_wallpaper,
        os.path.join(BACKGROUNDS_DIRECTORY, f"/{name}/", parsed_dark_wallpaper),
    )

    create_config()


def init() -> None:
    name: str = args.name
    light_wallpaper: str = args.light_wallpaper
    dark_wallpaper: str = args.dark_wallpaper

    make_wallpaper(name, light_wallpaper, dark_wallpaper)


# Run only on Linux
if __name__ == "__main__":
    if system() != "Linux":
        print("Unsupported platform")
        exit(1)

    init()
