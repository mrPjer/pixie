makepdf: fer.bst fer.cls literatura.bib seminar.tex
	pdflatex seminar.tex && bibtex seminar && pdflatex seminar.tex && pdflatex seminar.tex

clean:
	rm -v seminar.toc seminar.aux seminar.dvi seminar.bbl seminar.log seminar.pdf seminar.blg
