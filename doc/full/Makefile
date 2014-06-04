all: report.pdf

report.pdf: report.hal sections/* ../examples/*
	hal report.hal > report.tex
	pdflatex report.tex
	rm test.txt
