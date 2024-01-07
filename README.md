# gnome-binary-wallpaper

Gnome-binary-wallpaper is simple program that helps you create a dark / light wallpapers for linux gnome user environment.

## Build it yourself

clone the repository

```shell
git clone https://github.com/asqit/gnome-binary-wallpaper && cd ./gnome-binary-wallpaper
```

Build the executable for your platform.

```shell
go build ./cmd/gnome-binary-wallpaper/main.go
```

run the program

```shell
cd ./cmd/gnome-binary-wallpaper
./gnome-binary-wallpaper -name dome -lw ~/Downloads/dome-light.jpg -dw ~/Downloads/dome-dark.jpg
```
