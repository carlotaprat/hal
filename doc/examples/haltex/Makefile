all: document.pdf

document.pdf: document.hal
	hal document.hal > document.tex
	pdflatex document.tex
