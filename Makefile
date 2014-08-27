TARGET       = hal
TARGET_CLASS = Hal

# Special definitions
noop=
space = $(noop) $(noop)

# Detect OS
ifeq ($(OS),Windows_NT)
	OS = windows
else
	UNAME_S := $(shell uname -s)
    ifeq ($(UNAME_S),Linux)
    	OS = linux
    endif
    ifeq ($(UNAME_S),Darwin)
    	OS = macosx
    endif
endif

# Directories
ROOT     = $(PWD)
SRCDIR   = $(ROOT)/src
LIBDIR   = $(ROOT)/libs
CLASSDIR = $(ROOT)/classes
MAIN     = $(SRCDIR)/hal
PARSER   = $(MAIN)/parser
INTERP   = $(MAIN)/interpreter
JAVADOC  = $(ROOT)/javadoc
BIN      = $(ROOT)/bin
NATIVES  = $(ROOT)/natives

# Executable
EXEC     = $(BIN)/$(TARGET)
JARFILE  = $(BIN)/$(TARGET).jar
MANIFEST = $(BIN)/$(TARGET)_Manifest.txt

# Main libraries
LIBS = $(LIBDIR)/antlr3.jar $(LIBDIR)/commons-cli.jar $(LIBDIR)/jline-2.11.jar

# Lightweight Java Game Library
LWJGL_VERSION = 2.9.1
LIBS += $(LIBDIR)/lwjgl-$(LWJGL_VERSION)/jar/lwjgl.jar $(LIBDIR)/lwjgl_util.jar
LIB_NATIVES = $(LIBDIR)/lwjgl-$(LWJGL_VERSION)/native/$(OS)/*

# Classpath
CLASSPATH = $(subst $(space),:,$(LIBS)):$(SRCDIR)
JARPATH   = "$(LIBS)"

# Distribution (tar) file
DATE      = $(shell date +"%d%b%y")
DISTRIB   = $(TARGET)_$(DATE).tgz

# Flags
JFLAGS   = -classpath $(CLASSPATH) -d $(CLASSDIR)
DOCFLAGS = -classpath $(CLASSPATH) -d $(JAVADOC) -private

# Source files
GRAMMAR     = $(PARSER)/$(TARGET_CLASS).g
MAIN_SRC    = $(MAIN)/$(TARGET_CLASS).java
PARSER_SRC := $(shell find $(PARSER) -name '*.java')
INTERP_SRC := $(shell find $(INTERP) -name '*.java')

ALL_SRC     = $(MAIN_SRC) $(PARSER_SRC) $(INTERP_SRC)

TIMESTAMP  = $(shell date +'%Y %b %d, %H:%M')
LASTCOMMIT   = $(shell git rev-parse HEAD | cut -c -8)
MAINFILE   = $(MAIN)/Hal.java

# Make rules
all: compile exec natives

compile:
	antlr3 -o $(PARSER) $(GRAMMAR)
	if [ ! -e $(CLASSDIR) ]; then\
	  mkdir $(CLASSDIR);\
	fi
	sed -i.bkp "s|\$$DATE|$(TIMESTAMP)|" $(MAINFILE)
	sed -i.bkp "s|\$$GIT|$(LASTCOMMIT)|" $(MAINFILE)
	-javac $(JFLAGS) $(ALL_SRC)
	mv $(MAINFILE).bkp $(MAINFILE);

docs:
	javadoc $(DOCFLAGS) $(ALL_SRC)

exec:
	if [ ! -e $(BIN) ]; then\
	  mkdir $(BIN);\
	fi
	echo "Main-Class: $(TARGET).$(TARGET_CLASS)" > $(MANIFEST)
	echo "Class-Path: $(JARPATH)" >> $(MANIFEST)
	cd $(CLASSDIR); jar -cmf $(MANIFEST) $(JARFILE) *
	printf "#!/bin/sh\n\n" > $(EXEC)
	printf 'exec java -enableassertions -Djava.library.path=$(NATIVES) -jar $(JARFILE) "$$@"' >> $(EXEC)
	chmod a+x $(EXEC)

natives:
	mkdir -p $(NATIVES)
	cp -u $(LIB_NATIVES) $(NATIVES)

clean:
	rm -rf $(PARSER)/*.java $(PARSER)/*.tokens
	rm -rf $(CLASSDIR)

distrib: clean
	rm -rf $(JAVADOC)
	rm -rf $(BIN)

tar: distrib
	cd ..; tar cvzf $(DISTRIB) $(TARGET); mv $(DISTRIB) $(TARGET); cd $(TARGET)
