# CyBench Eclipse plugin

## Installation instructions

**Deployment packages from [release page](https://github.com/K2NIO/gocypher-cybench-eclipse/releases)**

* File `CyBenchTools-0.2-beta.zip` contains Eclipse update site for Eclipse 2020-09.
* File `CyBenchTools-0.3-beta.zip` contains Eclipse update site for Eclipse 2021-09.
* File `CyBenchToolsForEclipseMars-0.3-beta.zip` contains Eclipse update site for Eclipse Mars 2.

**Prerequisites**

* Package `CyBenchTools-0.2-beta.zip` is built and tested using
    * JAVA – JDK-11.0.9,
    * Eclipse Version: 2020-09 (4.17.0).

* Package `CyBenchToolsForEclipseMars-0.2-beta.zip` is built and tested using
    * JAVA – JDK-1.8.0_201,
    * Eclipse Version: Eclipse Mars 2 (4.5.2).

* Package `CyBenchTools-0.3-beta.zip` is built and tested using
    * JAVA - JDK-11.0.13,
    * Eclipse Version: 2021-09 (4.21.0).

* Package `CyBenchToolsForEclipseMars-0.3-beta.zip` is built and tested using
    * JAVA - JDK-1.8.0_311,
    * Eclipse Version: Eclipse Mars 2 (4.5.2). 

**Note:** For the CyBench Eclipse Plugin, previous version of JAVA (Before Java 8) or Eclipse (Before v2020-09 (4.17.0) or before Mars 2 (v4.5.2)) are unsupported

### Installation from local file system

Installation process for both version of plugins is the same:

* In Eclipse top menu select: “Help” → “Install new software”.
* In installation dialog window click button “Add”.
* Click button “Archive...”.
* Navigate to folder which contains CyBench Tools update site archive (zip) and select it.
* Type in “Name” - “CyBench Tools Local Install”.

![](./docs/images/image1.png)

* Click button "Add".
* Then list of categories/features shall appear in the table.

![](./docs/images/image2.png)

* Check “CyBench” entry in the table.
* Click button “Next”.
* Installation details window appears. Click button “Next”.
* Licenses review window appears, review and select “I accept the terms of the license agreement”, click button
  “Finish”.
* Warning box appears, click button “Install anyway”.

![](./docs/images/image3.png)

* Restart Eclipse workspace, click button “Restart Now”.
* Plugins shall be visible under Eclipse installation details:
    * CyBench branding icon shall be visible under installed plugins bar.
    * Installed software, Features and Plug-ins shall contain CyBench software items.

![](./docs/images/image4.png)

![](./docs/images/image5.png)

## Usage Instructions

### Project nature “CyBench Nature”

CyBench project nature applies settings to the project that benchmarks are compiled with all annotations and contains
all required dependencies at least for startup. It is possible to override project configuration changes made by CyBench
plugin; it just configures items which are necessary for successful launch. Project nature must be “CyBench Nature” in
order to use further CyBench tools.

**Steps:**

* Right click mouse button on the project.
* Select “Add CyBench Nature”.
* CyBench nature shall be added.

![](./docs/images/image8.png)

Adding CyBench nature will make the following changes to the project settings:

* Will add nature tag to the project settings (`.project file`).

![](./docs/images/image9.png)

* Will add external dependencies to JMH framework
    * If project is JAVA project then will set build path to point to CyBench plugin as external library link.
    * If project is Gradle project then will add dependencies to JMH into build.gradle file.
    * If project is Maven project then will add dependencies to JMH into POM file.
* Will update compiler settings in order to use annotation processing during file compilation:
    * Enables annotation processing and sets extra source folders for processed files.
    * Enables and adds annotation factory which points to CyBench plugin as external library link.

![](./docs/images/image10.png)

![](./docs/images/image11.png)

This configuration triggers, any JMH benchmarks implementing class recompile and its annotations transformation into JMH
types which shall be placed under directories defined in “Annotation Processing” window.

Configuration allows to run CyBench launcher without any other specific rebuild and this does not depend on project
nature. Also this allows to use JMH framework in Eclipse for generic Eclipse JAVA project without Maven usage.

## Generate Benchmarks

Feature generates benchmark class stubs, adds necessary annotations and methods for a selected JAVA file.

**Pre-conditions**

* `CyBench Nature` for a project,
* selected file extension `.java`,
* selected file located not in a benchmarks folder.

**Steps:**

* Select any JAVA class file in the project explorer which meets pre-conditions.
* Right click mouse button on it and Select `CyBench Generate Benchmark Stubs` or click CTRL+7.

![](./docs/images/image15.png)

* A popup will be displayed that shows all the available public methods inside the class.
* Check the checkboxes on the right side to select the methods for which to generate the benchmark methods.
* Select the benchmarking mode from the dropdown list.

**Notice**: If the file have been already generated previously and still exists its contents will not be overwritten.

![](./docs/images/image19.png)

* New JAVA classes with benchmark test stubs shall be generated under new src folder:
    * for JAVA projects - `src-benchmarks`,
    * for Maven and Gradle projects – `src/test/java` (default folder for tests).
* Newly generated classes shall be immediately pre-processed using JMH annotation processors and places under folders
  defined in a project annotation processing settings.

![](./docs/images/image16.png)

## Quick launch benchmarks using CyBench

Launches all JMH benchmarks which are found under selected JAVA file. Quick launch does not collect hardware or JVM
settings and does not send report to CyBench repository.

**Steps:**

* Right-click on the JAVA file or project (quick launch projects does not work in java perspective) which contains JMH
  Benchmarks.
* Select “Run As” → “Run on CyBench”.

![](./docs/images/image17.png)

* CyBench launcher will start display messages in the console view.
* After launcher finishes its work:
    * report file is created under `<selected project>/reports` folder,
    * report is displayed under “Report Details View”,
    * report entry appears in “CyBench Explorer”.

## CyBench Run Configuration and Launch View

Launch configuration view which allows to create a custom launch settings for a selected workspace project and run
CyBench launcher using user defined settings.

**Steps:**

* Select menu “Run” → “Run Configurations”.
* In the “Run Configurations” dialog window click on item “CyBench” and click “Create New” button
* Define the configuration for CyBench launcher in the tab “CyBench Configuration” and ”Execution Settings”.
    * Important options:
        * “Run Project” - select the project which contains JMH benchmarks.
        * “Reports Folder” - folder where CyBench reports are stored, default is the location of the “Run Project”
          option plus “/reports” subdirectory.
        * “Bench Access Token” - by providing the "bench" token that you get after creating a workspace in CyBench UI,
          you can send reports to your private directory, which will be visible only to the users that you authorize.
        * “Send Report To CyBench” - flag which defines that either report should be sent to CyBench repository or not.

![](./docs/images/image18.png)

* “Use CyBench Benchmark Settings” - uses the settings provided below only if selected will use default or annotations
  otherwise.
* "JVM Properties" - Field “JVM Properties“ covers Java system properties, Memory settings (Xmx, Xms, etc.), GC settings
  and other properties  (like -server), but everything must be correctly defined (in a way if you would launch Java
  application from command line) i.e. if it is system property then “-D“ prefix is must, because this value is added as
  it is written to the execution command during benchmark launch.

![](./docs/images/image20.png)

* Click “Apply” → “Run” to start execution of the selected project benchmarks.
* CyBench launcher will start display messages in the console view.
* After launcher finishes its work:
    * report file is created under `<selected project/reports>` folder,
    * report is displayed under “Report Details View”,
    * report entry appears in “CyBench Explorer”.

## CyBench Explorer View

View displays all CyBench reports found in the workspace projects.

* To open this view navigate “Window” → ”Show View” → “Other” → “CyBench Tools” → “CyBench Explorer”. If Java
  perspective is active then it opens on the right side of the screen otherwise opens at the bottom, and can place it
  anywhere.

![](./docs/images/image12.png)

* Double click with mouse on the report entry, and it will open a view with report details.

![](./docs/images/image13.png)

## Report Details View

View displays selected or generated during launch report details.

* Displays a list of benchmark tests on the right.
* View open position is at the bottom of the screen, either when report is selected via “CyBench Explorer” or after
  launch benchmarks using “CyBench launcher”

![](./docs/images/image14.png)

* Double click mouse on “Available Benchmarks” entries on the left size of the view, and it will display selected
  benchmark details on the right side of the view, tab “Benchmark Details”.
* Also, can navigate thorough other report attributes by clicking on the appropriate “tab” - “Summary”, “JVM
  Properties”, “HW Properties”.
* Chrome icon at the top-right view side allows to open the report using default system Web browser (which is defined in
  Eclipse settings) i.e. navigate to CyBench website to particular report details page (if the report was sent to
  CyBench Repository).

## Logging

Plugin logs message to Eclipse default log file which is located
in `<Path to workspace>\.metadata\.plugins\org.eclipse.ui.workbench\log` or `<Path to workspace>\.metadata\.log`. Log
messages are also displayed in `Error Log` view, which is accessible via top menu bar: "Window" → "Show View" → "Error
Log"

## Uninstall

* Applicable if CyBench tools were installed using update site package:
    * Select Eclipse menu "Help" → "About Eclipse IDE" → "Click Installation Details".
    * In the `Installed Software` tab select table item which has name `CyBench`
    * Click button `Uninstall`.
    * Restart Eclipse when requested.

![](./docs/images/image7.png)

## More information on benchmarking your code

* [CyBench Benchmark samples](https://github.com/K2NIO/gocypher-cybench-java-core/tree/main/gocypher-cybench-jvm/src/main/java/com/gocypher/cybench/jmh/jvm/client/tests)
* [Avoiding Benchmarking Pitfalls on the JVM](https://www.oracle.com/technical-resources/articles/java/architect-benchmarking.html#:~:text=JMH%20is%20a%20Java%20harness,to%20unwanted%20virtual%20machine%20optimizations)
* [JMH - Java Microbenchmark Harness](http://tutorials.jenkov.com/java-performance/jmh.html)
* [Java Benchmarks with JMH](https://medium.com/swlh/java-benchmarks-with-jmh-a-preamble-285510a77dd2)
* [Microbenchmarking with Java](https://www.baeldung.com/java-microbenchmark-harness)
