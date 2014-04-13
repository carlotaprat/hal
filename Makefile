TARGET       = hal
TARGET_CLASS = Hal

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

# Executable
EXEC     = $(BIN)/$(TARGET)
JARFILE  = $(BIN)/$(TARGET).jar
MANIFEST = $(BIN)/$(TARGET)_Manifest.txt

# Libraries and Classpath
LIB_ANTLR = $(LIBDIR)/antlr3.jar
LIB_CLI   = $(LIBDIR)/commons-cli.jar
LIB_JLINE = $(LIBDIR)/jline-2.11.jar
CLASSPATH = $(LIB_ANTLR):$(LIB_CLI):$(LIB_JLINE):$(SRCDIR)
JARPATH   = "$(LIB_ANTLR) $(LIB_CLI) $(LIB_JLINE)"

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
MAINFILE   = $(MAIN)/Hal.java

all: compile exec

compile:
	antlr3 -o $(PARSER) $(GRAMMAR)
	if [ ! -e $(CLASSDIR) ]; then\
	  mkdir $(CLASSDIR);\
	fi
	sed -i.bkp "s|\$$DATE|$(TIMESTAMP)|" $(MAINFILE)
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
	printf 'exec java -enableassertions -jar $(JARFILE) "$$@"' >> $(EXEC)
	chmod a+x $(EXEC)

clean:
	rm -rf $(PARSER)/*.java $(PARSER)/*.tokens
	rm -rf $(CLASSDIR)

distrib: clean
	rm -rf $(JAVADOC)
	rm -rf $(BIN)

tar: distrib
	cd ..; tar cvzf $(DISTRIB) $(TARGET); mv $(DISTRIB) $(TARGET); cd $(TARGET)
