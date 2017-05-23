SimplePortals (SP)
==================
SimplePortals (SP) is a minecraft mod that adds constructable portals that allow intra- and interdimensional travel.

Download
========
Get the lastest version at [CurseForge](https://minecraft.curseforge.com/projects/simpleportals/files)

Design
======
This is supposed to serve as a rough overview of the mods inner workings.

All portal related data is stored in a central registry. This registry is saved and loaded utilizing world save data. This way there is no need for the usual setup of other mods that use some sort of tile entity backed controller block.

Setting up Eclipse Workspace and Compiling
==========================================
- Clone the repo to the folder you want to work in (aka the working directory).
- Check out the branch you're interested in.
- Open build.gradle in an editor and find the Forge version the mod is compiled against (e.g. version = "1.7.10-10.13.0.1188")
- Go to http://files.minecraftforge.net/ and download the appropriate MDK (Forge Src zip before 1.8).
- Extract the zip to wherever you want but not to your working directory.
- Copy the eclipse folder into your working directory.
- Open up a console and execute "gradlew setupDecompWorkspace" then "gradlew eclipse" in your working directory.
- Your workspace is now located in "yourWorkingDir\eclipse". You can open it with Eclipse and hack away.
- To compile, simply type "gradlew build" in the console. The binary will be located in "yourWorkingDir\build\libs".
