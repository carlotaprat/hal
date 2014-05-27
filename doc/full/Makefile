all: report.pdf

report.pdf: report.hal sections/* examples/* haltex/*
	hal report.hal > report.tex
	pdflatex report.tex
