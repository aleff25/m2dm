# Metamodel Driven Measurement (M2DM) Tool for Java
## (Eclipse plugin for Java metrics formalization and collection)
This project was developed within the [Software Systems Engineering group](https://ciencia.iscte-iul.pt/centres/istar-iul/groups/sse) at the [ISTAR Research Center](https://ciencia.iscte-iul.pt/centres/istar-iul) at the [ISCTE-IUL university](https://www.iscte-iul.pt/) in Lisbon, Portugal.

## Introduction
**Metamodel Driven Measurement (M2DM)** is a paradigm-independent approach to formalize and automatically collect software metrics. The latter are defined as OCL queries over a metamodel representing the target domain. M2DM was proposed in 2001 by Fernando Brito e Abreu [5], using the metamodel of the GOODLY language [4]. Then, M2DM was applied in the context of the UML metamodel and the **FLAME** (Formal Libray for Aiding Metrics Extraction) was produced [6-8]. The latter includes the definition of Brito e Abreu’s MOOD (Metrics for Object-Oriented Design) and Chidamber and Kemerer metrics suites. The initial version of the MOOD metrics set was publihed in [1]. Bindings of a revised metrics set, dubbed MOOD2, were published for C++ [2] and Eiffel [3]. M2DM has been used since then within the QUASAR research group in diverse contexts such as component based development, aspect-oriented development, object-relational databases, IT service management or business process modeling, as can be seen in [QUASAR’s publications and dissertation pages](https://sites.google.com/site/quasarresearchgroup/). A fundamental component of our toolset has been the OCL evaluator embedded in the [USE (UML Specification Environment) tool](https://sourceforge.net/projects/useocl/). To facilitate the integration with our research tools in those contexts, as well as in others such as model checking and code generation for several platforms, we have defined a [Java façade API that we named J-USE](https://quasarresearchgroup.github.io/j-use).

Due to the emergence of sanitized open source repositories, namely in Java, one of the most popular programming languages, the quest for mining them for research purposes has increased lately. Research endeavors of this kind require empirical validation and the latter implies defining explanatory and outcome variables. Those variables are expressed in this context by the so-called software metrics. Despite the fact that several object-oriented metrics suites have been proposed in the past (e.g. C&K or MOOD suites), no M2DM open-source tool for Java was available and we kept receiving requests worldwide for such a tool. Therefore, we decided to build one on top of Eclipse, the most popular open-source IDE currently used. Since M2DM requires a metamodel of the target domain, we proposed the EJMM (Eclipse Java Metamodel)[9,10], based upon and instantiated through Eclipse's Java Development Tools.

The provided M2DM plugin allows users to easily define new software metrics in OCL upon the EJMM. We have also ported the aforementioned FLAME library to the EJMM. For more information on the internal details of the M2DM tool please read [9,10].

## 💡 Why OCL?
Declarative and language-independent.

Allows defining formal and reproducible metric expressions.

Integrated with the FLAME metamodel used in the USE environment.

## Notes
### Installation
1. **Clone the repository:**
```
git clone https://github.com/aleff25/m2dm.git
cd m2dm
```
2. Build the project:
```
mvn clean install
```
3. **Run the plugin:**

- In Eclipse → Run Configurations...
- Create a new configuration under Eclipse Application
- Click Run

4. **A new Eclipse instance will open.**
- Import any Java project.

5. **Go to:**

  ```
  Window → Show View → Other → EJM2Metrics → Interactive View
  ```
6. **The plugin will be available in the “EJM2Metrics” package, with the Interactive Metrics View displaying results directly in the IDE.**

### Requirements
- **Java 8+**
- **Maven 3.9+**
- **Eclipse IDE (2023-09 or newer)**

## 🧠 Architecture Overview
M2DM is composed of modular and extensible components:

Component	Description
🟩 FLAME Metamodel (Existing)	Defines the structure of Java code elements.
🟩 OCL Interpreter (Existing)	Executes metric expressions on the metamodel.
🟦 Metric Engine (Developed)	Evaluates custom metrics and aggregates results.
🟦 Visualization Module (Developed)	Displays hierarchical metric results (package → class → method).
🟦 Export Module (Developed)	Exports results to CSV or Excel for further analysis.

All components are integrated into the Eclipse IDE, ensuring extensibility and interoperability with the USE tool.

## 🧩 Defining Custom Metrics
You can define metrics manually (via OCL expressions) or automatically (from predefined .use metamodel files).

### Manual Definition Example

```
context Method inv: self.statements->select(s | s.isConditional())->size()
```

### Automatic Definition
When loading a .use metamodel file, all previously defined metrics are automatically displayed in the grid.

## 📊 Visualization and Export
Metrics are displayed hierarchically by package, class, and method.

Results can be exported as CSV or Excel files.

The interface is simple and fully integrated into Eclipse.

## Screenshots
### Architecture Diagram
<img width="401" height="301" alt="deployment_diagram_updated" src="https://github.com/user-attachments/assets/67222b65-694d-4369-b5b1-a9c8fddecc5e" />

### Workflow Overview
<img width="2356" height="1426" alt="bpmn_workflow_updated" src="https://github.com/user-attachments/assets/5c13439d-078d-4f67-91cb-7ce2fd102462" />

### Interactive View
<img width="1614" height="470" alt="metrics_view_method" src="https://github.com/user-attachments/assets/94ea468f-7a0d-4bd2-b243-59bc66466bab" />
<img width="211" height="265" alt="manual_metric_insertion_updated" src="https://github.com/user-attachments/assets/736b5f51-84a4-4169-83d0-f54df959c892" />

### Metrics Results Table
<img width="470" height="87" alt="image (1)" src="https://github.com/user-attachments/assets/80c8704c-f221-4ff6-8dbb-d80759a99d26" />


## 🧪 Validation
The plugin was tested with real Java codebases and compared to SonarQube and Metrics by Sauer.

Metric	Reference Match	Notes
LOC	✅ Equivalent	Validates metric accuracy
CYCLO	✅ Equivalent	Consistent results
NOL / NCS	🔹 New metrics	Nonexistent in other tools

## Bibliography
* [10] Pedro Janeiro Coimbra, Fernando Brito e Abreu, “[The Eclipse Java Metamodel: Scaffolding Software Engineering Research on Java Projects with Model-Driven Techniques](http://dx.doi.org/10.5220/0004715303920399)”, proceedings of the 2nd International Conference on Model-Driven Engineering and Software Development (MODELSWARD’2014), Lisbon, Portugal, 7-9 January 2014. SCITEPRESS Digital Library, 2014. {DOI: 10.5220/0004715303920399} {ISBN: 978-989-758-007-9}
* [9] Pedro Janeiro Coimbra, supervised by Fernando Brito e Abreu, “[An Eclipse Plugin for Metamodel Driven Measurement](http://hdl.handle.net/10071/8007)”, MSc dissertation, University Institute of Lisbon (ISCTE-IUL), November 2013.
* [8] Aline Lúcia Baroni, Fernando Brito e Abreu, “[Formalizing Object-Oriented Design Metrics upon the UML Meta-Model](http://dx.doi.org/10.5281/zenodo.1217101)”, proceedings of the XVI Simpósio Brasileiro de Engenharia de Software (SBES), pp. 130-145, Gramado, Brazil, October 2002. Biblioteca Digital Brasileira de Computação (BDBComp), Sociedade Brasileira de Computação. {DOI: 10.5281/zenodo.1217101}
* [7] Aline Lúcia Baroni, Miguel Goulão, Fernando Brito e Abreu, “[Avoiding the Ambiguity of Quantitative Data Extraction: An Approach to Improve the Quality of Metrics Results](http://dx.doi.org/10.5281/zenodo.1216853)”, proceedings of the 28th Euromicro Conference (Work in Progress Session), Dortmund, Alemanha, September 2002. {DOI: 10.5281/zenodo.1216853}
* [6] Aline Lúcia Baroni, supervised by Fernando Brito e Abreu (FCT/UNL) and Theo D’Hondt (VUB), “[Formal Definition of Object Oriented Design Metrics](http://www.emn.fr/z-info/emoose/alumni/thesis/abaroni.pdf)”, MSc thesis, 28 August 2002. Vrije Universiteit Brussel (Belgium) in collaboration with Ecole des Mines de Nantes (France) and Universidade Nova de Lisboa (Portugal), in the scope of the [EMOOSE](http://www.emn.fr/x-info/emoose/) (European Master in Object-, component-, aspect-, Oriented Software Engineering technologies) program.
* [5] Fernando Brito e Abreu, “[Using OCL to formalize object oriented metrics definitions](http://dx.doi.org/10.5281/zenodo.1217095)”, INESC Technical Report ES007/2001, June 2001. {DOI: 10.5281/zenodo.1217095}
* [4] Fernando Brito e Abreu, Luís Ochoa, Miguel Goulão, “[The GOODLY Design Language for MOOD2 Metrics Collection](http://dx.doi.org/10.5281/zenodo.1217621)”, INESC Technical Report R16/97, March 1997. {DOI: 10.5281/zenodo.1217621}
* [3] Fernando Brito e Abreu, Rita Esteves, Miguel Goulão, “[The Design of Eiffel Programs: Quantitative Evaluation Using the MOOD Metrics](http://dx.doi.org/10.5281/zenodo.1216932)”, proceedings of the 20th International Conference on Technology of Object Oriented Languages and Systems (TOOLS'96 USA), Raimund Ege (ed.), Santa Barbara, California, USA, July 29 - August 2, 1996. {DOI: 10.5281/zenodo.1216932}
* [2] Fernando Brito e Abreu, Miguel Goulão, Rita Esteves, “[Toward the Design Quality Evaluation of Object-Oriented Software Systems](http://dx.doi.org/10.5281/zenodo.1217073)”, proceedings of the 5th International Conference on Software Quality (5ICSQ), pp. 44-57, American Society for Quality, Austin, Texas, USA, 23-26 October 1995. {DOI: 10.5281/zenodo.1217073}
* [1] Fernando Brito e Abreu, Rogério Carapuça, “[Object-Oriented Software Engineering: Measuring and Controlling the Development Process](http://dx.doi.org/10.5281/zenodo.1217609)”, proceedings of the 4th International Conference on Software Quality (4ICSQ), American Society for Quality, McLean, Virginia, USA, 3-5 October 1994. {DOI: 10.5281/zenodo.1217609}
