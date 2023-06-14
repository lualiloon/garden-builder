# Production Build Instructions

## build file

Make sure there is a build file in the base directory of the project, with the filename [build-name].cljs.edn (for example: a 'dev' build needs the file dev.cljs.edn).

For our build, 'example', we need the file example.cljs.edn

## build the code

From the command line, run:

    clojure -M -m figwheel.main --optimizations advanced --build-once example

or the abbreviated version:

    clojure -M -m figwheel.main -O advanced -bo example

## restructure the compiled files

For the project to run directly from the html file, we need to move some things around so the directories line up in the way the html file is expecting.

Create a new directory to hold the publishable project. It can be in the base project directory, or in an entirely separate directory outside the project altogether. Copy the html file over to this directory. Then copy over any directories that the html file links to, including any javascript files. Most likely this is a css directory, and the cljs-out directory (originally found in target/public/cljs-out). Also include any assets the project uses (images, sounds, fonts, etc).

## Test to make sure it runs
Open the html file in a browser. Make sure it runs your project as expected.

That's it! Your project is ready to publish!
