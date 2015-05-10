rsync --progress ./build/libs/satori-all.jar users:satori/satori-all.jar
rsync rstproc.nim users:satori/rstproc.nim
rsync -rv res/ users:satori/res/
