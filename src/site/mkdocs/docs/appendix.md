# Welcome to MkDocs

For full documentation visit [mkdocs.org](https://mkdocs.org).

## Commands

* `mkdocs new [dir-name]` - Create a new project.
* `mkdocs serve` - Start the live-reloading docs server.
* `mkdocs build` - Build the documentation site.
* `mkdocs help` - Print this help message.

## Project layout

    basedir: ${projectBaseDir}/src/site/mkdocs

    mkdocs.yml              # The configuration file.
    docs/
        chapter1/           # markdown document base directory
            c1-01.md
            c1-02.md
        chapter2/
            c2-01.md
            c2-02.md
        img/                # Image base directory
            logo.png
        index.md            # The documentation homepage.
        license.md          # Included sample by another markdown document (about.md).
        about.md            # Other markdown pages


## mkdocs.yml
```yaml
site_name: Sample Guide

nav:
  - Home: index.md
  - Chapter1:
      - Chapter 1-1: chapter1/arch-01.md
      - Chapter 1-2: chapter1/arch-02.md
  - 챕터2:
      - 챕터 2-1: chapter2/infra-01.md
      - 챕터 2-2: chapter2/infra-02.md
  - About: appendix.md
    
theme : readthedocs

```


## mkdocs guide

Please see the [mkdocs guide](https://mkdocs.readthedocs.io/en/859/user-guide/writing-your-docs/){
format=_blank} for further details.


<a href="https://mkdocs.readthedocs.io/en/859/user-guide/writing-your-docs/" target="_blank">mkdocs guide</a>  for further details.

