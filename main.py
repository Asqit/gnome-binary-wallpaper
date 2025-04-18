#!/usr/bin/python3
import gi
import sys
import os
import shutil

gi.require_version("Gtk", "4.0")

from gi.repository import Gtk, GLib
from typing import Self, Literal


type CurrentSelectionType = Literal["dark"] | Literal["light"] | None


XML_TEMPLATE = """<?xml version="1.0" encoding="UTF-8"?>
<wallpapers>
	<wallpaper>
		<name>{}</name>
		<filename>{}</filename>
		<filename-dark>{}</filename-dark>
		<options>zoom</options>
		<shade_type>solid</shade_type>
		<pcolor>#023c88</pcolor>
		<scolor>#5789ca</scolor>
	</wallpaper>
</wallpapers>
"""


class AppWin(Gtk.Window):
    def __init__(self: Self, *args, **kwargs) -> None:
        super().__init__(*args, **kwargs)

        self.set_default_size(600, 400)
        self.set_title("Gnome Binary Wallpaper")
        self.set_opacity(0.99)  # type: ignore
        self.set_resizable(False)

        # initializing properties
        self.lw_path: str = ""
        self.dw_path: str = ""
        self.current_selection_type: CurrentSelectionType = None

        # Headerbar
        self.header = Gtk.HeaderBar()
        self.set_titlebar(self.header)
        self.about_btn = Gtk.Button()
        self.about_btn.set_icon_name("help-about")
        self.about_btn.connect("clicked", self.show_about)
        self.header.pack_start(self.about_btn)

        # Boxing
        self.outer_box = Gtk.Box(orientation=Gtk.Orientation.VERTICAL)
        self.outer_box.set_halign(Gtk.Align.CENTER)
        self.outer_box.set_valign(Gtk.Align.CENTER)

        self.vbox = Gtk.Box(orientation=Gtk.Orientation.VERTICAL)
        self.vbox.set_spacing(10)

        # File Selectors
        self.lw_btn = Gtk.Button(label="Select light wallpaper")
        self.dw_btn = Gtk.Button(label="Select dark wallpaper")
        self.lw_btn.connect("clicked", self.on_select_btn_clicked)
        self.dw_btn.connect("clicked", self.on_select_btn_clicked)

        # Name entry
        self.name_entry = Gtk.Entry(placeholder_text="wallpaper name...")

        # Submit Button
        self.submit_btn = Gtk.Button(label="create")
        self.submit_btn.connect("clicked", self.on_submit)

        # File Dialog
        self.open_dialog = Gtk.FileChooserNative.new(
            title="Choose a file", parent=self, action=Gtk.FileChooserAction.OPEN
        )

        f = Gtk.FileFilter()
        f.set_name("Image files")
        f.add_mime_type("image/jpeg")
        f.add_mime_type("image/png")

        self.open_dialog.add_filter(f)

        # Error Label
        self.error_label = Gtk.Label()

        # Layout-ing
        self.vbox.append(self.error_label)
        self.vbox.append(self.lw_btn)
        self.vbox.append(self.dw_btn)
        self.vbox.append(self.name_entry)
        self.vbox.append(self.submit_btn)

        self.outer_box.append(self.vbox)
        self.set_child(self.outer_box)

    def show_about(self: Self, btn: Gtk.Button):
        self.about = Gtk.AboutDialog()
        self.about.set_transient_for(self)
        self.about.set_modal(True)
        self.about.set_authors(["Ondřej Tuček"])
        self.about.set_copyright("Copyright 2025 Ondřej Tuček")
        self.about.set_license_type(Gtk.License.GPL_3_0)
        self.about.set_website("https://asqit.space")
        self.about.set_website_label("My Website")
        self.about.set_version("1.0")
        self.about.show()

    def copy_files(
        self: Self, file_paths: list[str], destination_dir: str
    ) -> list[str]:
        os.makedirs(destination_dir, exist_ok=True)
        new_paths = []
        for file_path in file_paths:
            if os.path.isfile(file_path):
                destination = os.path.join(destination_dir, os.path.basename(file_path))
                shutil.copy2(file_path, destination)
                new_paths.append(destination)
        return new_paths

    def on_submit(self: Self, button: Gtk.Button) -> None:
        name: str = self.name_entry.get_text()

        if not self.lw_path or not self.dw_path:
            self.error_label.set_label("Missing paths")
            return

        if not name:
            self.error_label.set_label("Missing name")
            return

        # Create target directory and copy images
        target_dir = os.path.expanduser(f"~/.local/share/backgrounds/{name}")
        copied_paths = self.copy_files([self.lw_path, self.dw_path], target_dir)

        # Expecting exactly 2 files copied
        if len(copied_paths) != 2:
            self.error_label.set_label("Failed to copy files")
            return

        light_path, dark_path = copied_paths

        # Create and save XML with correct paths
        xml_content = XML_TEMPLATE.format(name, light_path, dark_path)
        os.makedirs(
            os.path.expanduser("~/.local/share/gnome-background-properties"),
            exist_ok=True,
        )
        with open(
            os.path.expanduser(
                f"~/.local/share/gnome-background-properties/{name}.xml"
            ),
            "w",
        ) as file:
            file.write(xml_content)

        self.error_label.set_label("Wallpaper config created successfully 🎉")

    def on_select_btn_clicked(self: Self, button: Gtk.Button) -> None:
        if button == self.lw_btn:
            self.current_selection_type = "light"
        else:
            self.current_selection_type = "dark"

        self.open_dialog.connect("response", self.on_file_response)
        self.open_dialog.show()

    def on_file_response(self: Self, dialog: Gtk.Dialog, response: int) -> None:
        if response == Gtk.ResponseType.ACCEPT:
            file = dialog.get_file()  # type: ignore
            filename = file.get_path()

            if self.current_selection_type == "light":
                self.lw_path = filename
                self.lw_btn.set_label(filename)
            else:
                self.dw_path = filename
                self.dw_btn.set_label(filename)

        self.open_dialog.disconnect_by_func(self.on_file_response)


class App(Gtk.Application):
    def __init__(self: Self, *args, **kwargs) -> None:
        super().__init__(*args, **kwargs)
        self.connect("activate", self.on_activate)
        GLib.set_application_name("gnome-binary-wallpaper")

    def on_activate(self: Self, app_ref: Gtk.Application) -> None:
        self.win = AppWin(application=app_ref)
        self.win.present()


def main() -> None:
    app = App(application_id="com.github.asqit.gnome-binary-wallpaper")
    app.run(sys.argv)


if __name__ == "__main__":
    main()
