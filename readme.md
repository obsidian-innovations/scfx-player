
About
-----

A simple music player which support all the basic features and a little beyond.

The player was created as a maturity test for the [ScalaFX](http://www.scalafx.org/) framework.
[Scala](http://www.scala-lang.org/) and [JavaFX](http://www.oracle.com/technetwork/java/javase/overview/javafx-overview-2158620.html) 
is the primary tech stack, see SBT file for more detail.

How to run
----------

Make sure you have Java and SBT installed. Use SBT run command to launch the player:

     $> sbt run


Player key features
-------------------

 - Major functions: play, rewind backwards and forwards, pause compositions
 - Seek within a composition using Drag&Drop slider
 - Volume control slider
 - 'Time left' indicator
 - Support for MP3 files (easy to extend to other formats)
 - Reading metadata from the files: Artist, Album, Duration, Track
 - Adding and removing MP3 files to and from a playlist
 - Default playlist and settings
 - Handling playlists (saving and loading)
 - Rearranging compositions in a playlist using Drag&Drop
 - Sorting / grouping compositions in a playlist by metadata (not persistent)
 - Bespoke minimalistic theme with custom icons
 - Order of the columns with metadata can be rearranged using Drag&Drop
 - Customize currently displayed metadata (show/hide columns)
 - Responsive UI: columns with the metadata get re-sized and adjusted to the player window size


Other technical details
-----------------------

Current limitation of the executable jar: non-latin characters are not supported 
if not launched with the following option "-Dfile.encoding=UTF-8".


License
-------

Copyright (c) 2013-2015 Obsidian Innovations, Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


