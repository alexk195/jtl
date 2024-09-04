# JTL
## Java Template Language Extension
Java Template Language (JTL) is a *template processor*, which can generate arbitrary textual output based on a data and a template. The reason for such tool is to separate varying and fixed patterns about text and code generation into a definition (data) and a template. 
Usually JTL uses template files (stored with extension jtl) and data definition files (stored as def or json or csv files) to generate arbitrary textual document. Additional features make it well suitable for code generation as well. For example modifications of generated files can be preserved in explicit sections.

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Examples](#examples)
- [Definitions](#definitions)
- [Templates](#templates)
- [License](#license)

## Installation
1. Clone the repository:
2. You will require build/JTL.jar and jtl.bat or jtl.sh 

## Usage
1. Write a template file (see example1.jtl in examples/example1)
2. Write a definition file (see example1.def in examples/example1)
3. Write a project file (see project_1.jtlp in examples)
4. Execute jtl.sh or jtl.bat and provide project file as parameter

## Examples
Examples are provided in folder examples
1. Simple printing of text
2. Creating a simple c++ file
3. Dumping definition stored in json and csv files
4. Manual sections, own imports and empty definition

## Definitions
Can be in native def format or in json or in csv. For json and csv there is transformation into def format internally.
In example3 the json and csv files are dumped for clarity.

### JSON files 
The JSON arrays are represented as entities with one parameter with name “_array_”. All children entities of array have name “_elem_”.

### CSV files
The root entity name is “csv”.
Every line is one entity with name “_elem_”. The values are stored as parameters of entity.
The CSV Format corresponds to format generated by Excel.

## Templates
Templates files have extension ".jtl". This file contains a mixture of text and controlling code. The text part is taken as-is to the output. The controlling code implements various generation logic like loops, if-statements etc.
JTL was designed to be a so called “natural template language”. Which means that if jtl file contains no controlling code at all the output is exactly the same as content of jtl file.
For better understanding there are files stored in examples folder for all the tutorials and examples reference in this document. 

### Control code and Expressions
The “@” character is used for escaping into control code section. 
Put this character at the beginning of a line and this line will be control code. 
Control code can be arbitrary Java code which you are allowed to use inside a method.
If your JTL code starts, you will have the entity of type JTLEntity already available and pointing to parsed content of definition.
The second escape sequence is used for replacing parts by using string expressions from control code. 
This escape sequence is "@\[" for opening and “]@” for closing.

The third sequence is used for larger control code blocks. It’s started by “@<” and closed with “@>” everything in between of this sequences is interpreted as Java code. 

You can see this in Example2.

### File creation
In most cases we want to write our output to a certain file. For example if we generate C++ code we would write cpp and h files.  Even if piping out console output to a file is an option we should definitely avoid this and use methods provided by JTL.
This is done by methods "*file()*" and "*close()*"
See Example2 for details.

### Manual sections
A very useful and powerful method for code generation framework is possibility to update portions of the file which is kept during the generation rounds. 
For example implementing generated stubs for method calls or similar. For this reason two methods are available: “*manual_begin()*” and “*manual_end()*”. 
Everything between them is preserved during subsequent generation runs.
See Example4 for details.

### JTL header file
In order to include own import or external imports You need to create additional file with file extension “jtl_header”.
This file will be copied into intermediate java file during template processing. Put your import declarations in this file.
See Example4 files in example folder for example of usage. The file is called example4.jtl_header

### Extended usage
Look into files JTLEntity.java and JTLTemplate.java.
Your templates will be derived from JTLTemplate, so all methods in control code are methods of JTLTemplate.
The definitions are parsed into JTLEntity instance, so everything related to access definition data is implemented in JTLEntity. 

## License
MIT License, see LICENSE file 
