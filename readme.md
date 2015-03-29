
About
-----

A simple music player which support all the basic features and a little beyond.

The player was created as a maturity test for the [ScalaFX](http://www.scalafx.org/) framework.
[Scala](http://www.scala-lang.org/) and [JavaFX](http://www.oracle.com/technetwork/java/javase/overview/javafx-overview-2158620.html) 
is the primary tech stack, see SBT file for more detail.


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
