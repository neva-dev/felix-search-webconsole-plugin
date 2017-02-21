![Neva logo](doc/neva-logo.png)

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/neva-dev/felix-search-webconsole-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![GitHub stars](https://img.shields.io/github/stars/neva-dev/felix-search-webconsole-plugin.svg)](https://github.com/neva-dev/felix-search-webconsole-plugin/stargazers)
[![Twitter](https://img.shields.io/twitter/url/https/github.com/neva-dev/felix-search-webconsole-plugin.svg?style=social)](https://twitter.com/intent/tweet?text=Wow:&url=%5Bobject%20Object%5D)

# Search Web Console Plugin for Apache Felix

Search for bundles, decompile classes, view services and quickly enter configurations.
Works on OSGi distributions based on Apache Felix such as Apache Sling, Apache Karaf, Apache ServiceMix etc.

![Overview](doc/overview.png)

## Features:

* searching for bundles, services, configurations and classes (with wildcard support),
* searching in decompiled classes sources that come from selected elements (e.g multiple bundles),
* bundle class tree view with jumping between decompiled class sources,
* one-click bundle JAR download.

You liked plugin? Please don't forget to star this project on GitHub :)

## Setup

Manually install ready to use bundle *search-webconsole-plugin-x.x.x.jar* using web console interface.

![Setup](doc/setup.png)

![Web Console Menu](doc/webconsole-menu.png)

## Build

Build and deploy automatically using command: `mvn clean package sling:install`.
Do not hesistate to fork and create pull requests.

## Configuration

If your container is available on different URL than http://localhost:8181/system/console, just override properties in following way:

`mvn clean install sling:install -Dfelix.url=http://localhost:8080/felix/console -Dfelix.user=admin -Dfelix.password=admin`

## License
**Search Web Console Plugin** is licensed under [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)

## Legal notice

Any usage of that tool and legal consequences must be considered as done at your own risk. 
For instance, decompiled source code can be protected by copyrights and author does not take any responsibility for such usages.

Using that tool is absolutely optional. Original purpose of usage of built-in decompiler is to quickly view class sources used at runtime that are even available in public Internet, so that code debugging can take less time.