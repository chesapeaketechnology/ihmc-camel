# IHMC-Camel [![Build Status](https://travis-ci.com/chesapeaketechnology/ihmc-camel.svg?token=hD4AtTH6ehH3aM5obZou&branch=master)](https://travis-ci.com/chesapeaketechnology/ihmc-camel)

Camel component utilizing the IHMC pub-sub libraries

## Subprojects

### [Camel](camel/README.md)

Contains the camel component and common IHMC-DDS wrapper logic.

### [IDL-Tools](idl-tools/README.md)

Contains tools for generating sources and classes from IDL files.

### Impl-Common/Client/Server

An example use case with a basic chat application based off of the default idl file provided in the `idl-tools` project.
Run the server application, then the client application. 
The console outputs/logs will show successful DDS-backed communication.