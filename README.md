[![CI](https://github.com/LukeOnuke/lmark/actions/workflows/main.yml/badge.svg)](https://github.com/LukeOnuke/lmark/actions/workflows/main.yml) ![GitHub top language](https://img.shields.io/github/languages/top/LukeOnuke/lmark) ![Supports windows macOS linux](https://img.shields.io/badge/Supports-Win%20%7C%20MacOS%20%7C%20Linux-brightgreen)

# lmark
***Basicly a markdown notepad with extra open sauce!*** Or in academic wording an that allows you to easly open, edit and export md files.

### Main features include
 - Export markdown to ***pdf*** and ***html***!
 - System synchronisable dark / light mode swich
 - Multiplatform support *(windows, linux, macos)*
 - Word like writing expirience
 - Open source and free
 - Desktop built application, made with enterprise technologies like java**fx**.

### Planned stuff
 - Tabs
 - Being able to choose the css on export to pdf or markdown
 - More tools in the toolbar 

## Screenshots
*ℹ Keep in mind there is more stuff than shown on the screenshots.*
### Dark mode
![dark mode open](https://raw.githubusercontent.com/LukeOnuke/lmark/main/images/dark-opened.png)

![dark mode loaded file](https://raw.githubusercontent.com/LukeOnuke/lmark/main/images/dark-open.png)

### Light mode
![light mode open](https://raw.githubusercontent.com/LukeOnuke/lmark/main/images/light-opened.png)

![light mode loaded file](https://raw.githubusercontent.com/LukeOnuke/lmark/main/images/light-open.png)

# Contributions
If you want to contribute to lmark, please do. Contributers are welcome. For build instructions go to [# Builds](#Builds)

# Builds
You can find the newest builds in the [package all platforms action](https://github.com/LukeOnuke/lmark/actions/workflows/main.yml), although be ware they might not work as intended.

## Setting up the project
Preffered IDE is intelij IDEA but you could make it work on others

## Building the code yourself
1. **Git clone the repository**
	``` 
	git clone https://github.com/LukeOnuke/lmark.git
	```

2. **Navigate to project root**
3. **Package with maven**
	```
	mvn compile
	```
4. *(Optional)* **Compile to native application**
	```
	jpackage --type app-image --name lmark-app-image /
	--input C:\Users\lukak\Documents\GitHub\mdedit\target /
	--main-jar lmark.jar --main-class com.lukeonuke.lmark.LMark  /
	--verbose --dest jpkg
	```

# Licence
This program is under the mit licence.

```txt
MIT License

Copyright (c) 2021 Luka Kresoja

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

```
