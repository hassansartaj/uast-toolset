# UAS Testing Toolset
A toolset to support automated system-level testing of unmanned aerial systems (UAS). The toolset utilizes model-based testing (MBT) and artificial intelligence (AI) techniques to automatically generate, execute, and evaluate various test scenarios. Therefore, the toolset consists of two main modules (i) *ModelHandler* responsible for handling MBT side of the approach, and (ii) *AITester* contains the implementation of AI technique for testing UAS. 




# Getting Started

## Basic Requirements
* Machine: minimum 8GB RAM and 4-core processor
* OS: Windows 8/10 or Linux
* Java: JDK 1.8 or higher
* IDE: Eclipse/Netbeans/IntelliJ and PyCharm
* Ardupilot SITL
* Python 3.7 

## Dependencies
* PyTorch 1.5.0
* DroneKit 2.9.2
* pymavlink 2.4.8
* py4j 0.10.8

## Installations on Windows
* Install Ardupilot following the guidelines given in [Ardupilot Docs](https://ardupilot.org/dev/docs/building-setup-windows-cygwin.html). 
* During Cygwin64 installation, make sure to install it in *C* drive with directory name *cygwin*
* Install Anaconda with Python 3.7 
* Using Anaconda command prompt, install the dependencies with specified versions. 
  * To install PyTorch for GPU, use the command: `conda install pytorch==1.5.0 torchvision==0.6.0 cudatoolkit=10.1 -c pytorch`
  * To install PyTorch for CPU, use the command: `conda install pytorch==1.5.0 torchvision==0.6.0 cpuonly -c pytorch`
  * Install TensorBoard 2+ to visualize training data during execution. 

## Using Toolset

### Step: 1 
Clone the repository using the following command.
```
git clone https://github.com/hassansartaj/uast-toolset.git
```

### Step: 2 
* Start the Eclipse and import the *ModelHandler* as Java project in the workspace. 
* Run 'ModelServer.java' class as Java application. 

### Step: 3
* If using PyCharm IDE, import the *AITester* project in the workspace.
* Run 'main.py' file with two command line arguments, [arg1] mode of execution (i.e., train/eval), and [arg2] path to store trained model. 
* If using Python command prompt, run the command: `python main.py <arg1> <arg2>`.
* To visualize graphs, start Tensorboard using command: `tensorboard --logdir=[path to root directory of AITester]\runs`.

